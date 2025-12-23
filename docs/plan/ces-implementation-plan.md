# CES Production Function - GDP Negative Value Issue Fix Plan

## 問題の概要

シミュレーション実行時、期間5-10においてGDPが極端なマイナス値（-1.69e13など）を示す問題が発生。

**データ例** (`benchmark/benchmark/data/nominalGDP1.csv`):
```
Period 1: 36,970.47
Period 2: 25,725.35
Period 3: 17,402.68
Period 4: 14,631.28
Period 5: -1.69e13  ← 問題発生
Period 6: -1.27e13
Period 7: -7.43e12
...
Period 11: 20,360.10 ← 回復
```

## 根本原因の特定

### 原因メカニズム

1. **CES関数が極端に小さい有効労働量を返す**
   - 例: `nR=1, nN=0, δ=0.5, ρ=0.5` の場合
   - `computeEffectiveLabor(1, 0) ≈ 0.25`

2. **Math.round()によるゼロ化**
   - `CapitalFirm.java:425-443` の生産ロジック:
   ```java
   double effectiveLabor = computeEffectiveLabor(countR, countN);
   int ActualOutputLabor = Math.round(outputLaborDemand/totalDemandLabor*(float)ActualLabor);
   // Math.round(0.25 * ...) → 0 になる可能性
   outputQty = ActualOutputLabor * this.laborProductivity;
   ```

3. **単価計算の爆発**
   - `outputQty = 0` の場合:
   ```java
   inventories.setUnitCost(this.getWageBill()/Math.max(outputQty, this.getCesEpsilon()));
   // unitCost = wageBill / max(0, 1e-8) = 巨大な値（例: 1e13）
   ```

4. **在庫評価額の爆発**
   - 既存在庫（例: 100単位）が巨大な単価（1e13）で評価される
   - `LAG_NOMINALINVENTORIES = 100 * 1e13 = 1e15`

5. **GDP計算での減算**
   - `NominalGDPComputer.java:56-91`:
   ```java
   gdpGoodsComponent -= pastInventories; // pastInventories が巨大
   nominalGDP = gdpGoodsComponent + publicServantsWages;
   // GDP = (正常な生産) - (1e15) = 巨大なマイナス
   ```

### 影響範囲

- **直接影響**: `AbstractFirm.java`, `CapitalFirm.java`, `ConsumptionFirm.java`
- **間接影響**: GDP計算、在庫評価、企業会計
- **影響を受けるシナリオ**:
  - 片方の労働タイプがゼロまたは極小の場合
  - CESパラメータ設定によっては通常運用でも発生可能

## 実装計画

### 優先度A: GDP問題の修正（必須）

#### A1. computeEffectiveLaborに下限を設定 ⭐⭐⭐⭐⭐

**対象ファイル**: `jmab/src/main/java/jmab/agents/AbstractFirm.java:383-409`

**修正内容**:
```java
protected double computeEffectiveLabor(double nR, double nN) {
    // パラメータ検証
    if (cesRho == 0.0) {
        throw new IllegalStateException("cesRho cannot be 0. Use setCesRho to set a valid value.");
    }

    double laborR = cesAR * nR;
    double laborN = cesAN * nN;

    double term1 = cesDelta * Math.pow(laborR, cesRho);
    double term2 = (1.0 - cesDelta) * Math.pow(laborN, cesRho);
    double effectiveLabor = Math.pow(term1 + term2, 1.0 / cesRho);

    // 【修正】雇用者がいる場合、最低1名分の労働力を保証
    if (nR + nN > 0) {
        effectiveLabor = Math.max(1.0, effectiveLabor);
    }

    return effectiveLabor;
}
```

**理由**:
- 最もシンプルで影響範囲が限定的
- 雇用者がいる限り最低1名分の労働力とみなす
- unitCostの爆発を防止
- 経済学的にも合理的（労働力ゼロで賃金支払いは矛盾）

**影響**:
- `CapitalFirm`, `ConsumptionFirm` の生産計算
- 既存テストケースへの影響は最小限

### 優先度B: Cobb-Douglas近似の追加（推奨）

CESパラメータ `ρ=0` は理論上Cobb-Douglas関数を表すが、現在の実装では例外をスローする。より柔軟な運用のため近似処理を追加。

#### B1. computeEffectiveLaborにCobb-Douglas近似を追加

**対象ファイル**: `jmab/src/main/java/jmab/agents/AbstractFirm.java:383-409`

**修正内容**:
```java
protected double computeEffectiveLabor(double nR, double nN) {
    // 【追加】ρ≈0の場合、Cobb-Douglas近似を使用
    if (Math.abs(cesRho) < 1e-8) {
        double laborR = cesAR * nR;
        double laborN = cesAN * nN;
        double effectiveLabor = Math.pow(laborR, cesDelta) * Math.pow(laborN, 1.0 - cesDelta);

        // 下限保証
        if (nR + nN > 0) {
            effectiveLabor = Math.max(1.0, effectiveLabor);
        }
        return effectiveLabor;
    }

    // 既存のCES計算...
}
```

#### B2. setCesRhoのバリデーション更新

**対象ファイル**: `jmab/src/main/java/jmab/agents/AbstractFirm.java`

**修正内容**:
```java
public void setCesRho(double cesRho) {
    // 【修正】ρ=0を許容（Cobb-Douglas）、範囲を-1 < ρ < 1に制限
    if (cesRho <= -1.0 || cesRho >= 1.0) {
        throw new IllegalArgumentException("cesRho must be in range (-1, 1), got: " + cesRho);
    }
    this.cesRho = cesRho;
}
```

#### B3. computeLaborRatioにCobb-Douglas近似を追加

**対象ファイル**: `jmab/src/main/java/jmab/agents/AbstractFirm.java`

**修正内容**:
```java
protected double computeLaborRatio(double nR, double nN) {
    // 【追加】ρ≈0の場合、Cobb-Douglas比率を使用
    if (Math.abs(cesRho) < 1e-8) {
        // Cobb-Douglas: ratio = (δ/(1-δ)) * (A_N*nN)/(A_R*nR)
        double laborR = cesAR * nR;
        double laborN = cesAN * nN;
        if (laborR < cesEpsilon) return 0.0;
        double ratio = (cesDelta / (1.0 - cesDelta)) * (laborN / laborR);
        return Math.min(phiMax, Math.max(phiMin, ratio));
    }

    // 既存のCES計算...
}
```

#### B4. computeLaborSplitにCobb-Douglas近似を追加

**対象ファイル**: `jmab/src/main/java/jmab/agents/AbstractFirm.java`

**修正内容**:
```java
protected int[] computeLaborSplit(int totalWorkers) {
    // 【追加】ρ≈0の場合、Cobb-Douglas分割を使用
    if (Math.abs(cesRho) < 1e-8) {
        // Cobb-Douglas最適配分
        double ratioTarget = cesDelta / (1.0 - cesDelta);
        double ratio = Math.min(phiMax, Math.max(phiMin, ratioTarget));

        int nR = (int) Math.round(totalWorkers * ratio / (1.0 + ratio));
        int nN = totalWorkers - nR;

        return new int[]{nR, nN};
    }

    // 既存のCES計算...
}
```

### 優先度C: テストの追加（推奨）

#### C1. 下限保証のテスト追加

**対象ファイル**: `jmab/src/test/java/jmab/agents/AbstractFirmCesTest.java`

**追加テストケース**:
```java
@Test
public void testComputeEffectiveLaborMinimumBound() {
    // 極小値が1.0に制限されることを確認
    firm.setCesDelta(0.5);
    firm.setCesRho(0.5);
    firm.setCesAR(1.0);
    firm.setCesAN(1.0);

    double result = firm.computeEffectiveLabor(1, 0);
    assertTrue("Effective labor should be >= 1.0 when workers exist", result >= 1.0);
}

@Test
public void testComputeEffectiveLaborZeroWorkers() {
    // 雇用者ゼロの場合は下限なし
    double result = firm.computeEffectiveLabor(0, 0);
    assertEquals(0.0, result, 1e-8);
}
```

#### C2. Cobb-Douglas近似のテスト追加

```java
@Test
public void testCobbDouglasApproximation() {
    firm.setCesDelta(0.5);
    firm.setCesRho(1e-10); // ρ≈0
    firm.setCesAR(1.0);
    firm.setCesAN(1.0);

    double result = firm.computeEffectiveLabor(2, 2);
    // Cobb-Douglas: (2^0.5) * (2^0.5) = 2.0
    assertEquals(2.0, result, 0.1);
}
```

## 実装スケジュール

### Phase 1: 緊急対応（必須）
- [ ] A1. computeEffectiveLaborに下限を設定
- [ ] C1. 下限保証のテスト追加
- [ ] GDP値が正常範囲に収まることを確認

### Phase 2: 完全性向上（推奨）
- [ ] B1. computeEffectiveLaborにCobb-Douglas近似を追加
- [ ] B2. setCesRhoのバリデーション更新
- [ ] B3. computeLaborRatioにCobb-Douglas近似を追加
- [ ] B4. computeLaborSplitにCobb-Douglas近似を追加
- [ ] C2. Cobb-Douglas近似のテスト追加

## 検証計画

### 1. 単体テスト
- AbstractFirmCesTest.javaで新規テストケースを実行
- すべてのテストがパスすることを確認

### 2. シミュレーション検証
- `benchmark/benchmark/Model/modelBenchmark_full.xml` で実行
- `nominalGDP1.csv` の全期間でGDP > 0 を確認
- 期間5-10で異常値が発生しないことを確認

### 3. パラメータ感度分析
- `cesRho` を 0.0, 0.3, 0.5, 0.7, 0.99 で試験
- `cesDelta` を 0.3, 0.5, 0.7 で試験
- 労働構成 (nR:nN) を (10:0), (5:5), (0:10) で試験

## リスク評価

### 低リスク
- **A1 下限設定**: 既存ロジックに最小限の変更、経済学的に合理的
- **B1-B4 Cobb-Douglas近似**: 新機能追加、既存動作に影響なし

### 考慮事項
- 下限1.0が適切かは経済モデル次第（必要に応じて調整可能）
- ρ=0を許容することで、パラメータ空間が拡大（シナリオ分析に有用）

## 備考

### 対応不要項目（ユーザー確認済み）
- **シリアライゼーション対応**: スナップショット機能を使用しないため不要
- **phiMin/phiMax再利用**: 既に労働比率クリッピングに使用中のため、新規パラメータが必要な場合は別途定義

### 関連ファイル
- `jmab/src/main/java/jmab/agents/AbstractFirm.java` (CES実装)
- `benchmark/benchmark/src/main/java/benchmark/agents/CapitalFirm.java` (生産ロジック)
- `benchmark/benchmark/src/main/java/benchmark/agents/ConsumptionFirm.java` (生産ロジック)
- `jmab/src/main/java/jmab/report/NominalGDPComputer.java` (GDP計算)
- `benchmark/benchmark/Model/parameters.xml` (パラメータ定義)
