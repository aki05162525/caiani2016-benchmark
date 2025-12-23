# Phase C1 実装計画: CES実効労働関数の導入

## 目的

CES 実効労働関数 `N_eff(N_R, N_N)` を共通関数として実装し、後続のPhase C（生産・価格・賃金）で使える基盤を作る。

**完了の定義**: `N_eff` が共通関数として実装され、数値安定のガードが入り、XMLでパラメータが設定できる。

---

## 実装方針

### 共通関数の配置

1. **候補A（推奨）**: `jmab/src/jmab/agents/AbstractFirm.java` に protected helper を追加
   - Consumption/Capital/WagesEnd で共通利用できる
2. **候補B**: `benchmark` 配下にユーティリティクラスを新設
   - 依存範囲が増えるので優先度は低い

---

## 実装タスク

### タスク1: CESパラメータの追加

**対象**: `AbstractFirm`（または新規ユーティリティ）

**追加するパラメータ**:
- `delta`（0 < δ < 1）
- `rho`（ρ ≠ 0）
- `aR`, `aN`（> 0）
- `epsilon`（> 0, 数値安定用）

**対応**:
- フィールド追加
- getter/setter追加
- コメントで範囲を明記

---

### タスク2: `N_eff` 関数の実装

**関数仕様**:
```
N_eff = [ δ (A_R N_R)^ρ + (1-δ)(A_N N_N)^ρ ]^(1/ρ)
```

**数値安定ガード**:
- `N_R, N_N` は 0未満なら0にクリップ
- `delta` は (0,1) の範囲外なら例外 or クリップ（方針決定）
- `rho` が 0 近傍の場合は例外（or 代替のCobb-Douglas近似は後回し）
- `epsilon` でゼロ割・負値を回避

---

### タスク3: XML設定の追加

**対象ファイル**:
- `benchmark/benchmark/Model/modelBenchmark_light.xml`
- `benchmark/benchmark/Model/modelBenchmark_full.xml`

**追加内容**:
- `delta`, `rho`, `aR`, `aN`, `epsilon` の `property` を設定
- 既定値は安全な範囲のものを採用（暫定）

---

### タスク4: 最小検証

**方針**:
- テストは最小限
- `N_R=N_N=0` で `N_eff=0`
- `N_R` または `N_N` 単独増加で `N_eff` が増えることを手元確認

---

## 実装順序

```
タスク1: パラメータ追加
  ↓
タスク2: N_eff実装
  ↓
タスク3: XML設定追加
  ↓
タスク4: 最小検証
```

---

## 検証方法

### 1. コンパイル確認
```
./gradlew :benchmark:compileJava
```

### 2. 実行時確認
- 例外（NaN, Infinity）が出ないこと
- 既存挙動に影響がないこと（Phase C1は未使用のため）

---

## 完了条件（Definition of Done）

- [ ] `N_eff` の共通関数が追加されている
- [ ] 数値安定ガードが入っている
- [ ] XMLでCESパラメータを設定できる
- [ ] 최소限の動作確認ができている

---

## 重要な設計判断

1. **配置場所**: まずは `AbstractFirm` に実装（最小変更・共通利用）
2. **ρの扱い**: 0近傍は例外。Cobb-Douglas近似はPhase C2以降で検討
3. **既存挙動**: Phase C1では呼び出し箇所を変えない（安全に追加のみ）
