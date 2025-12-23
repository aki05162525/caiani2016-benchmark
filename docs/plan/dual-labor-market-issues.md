# 二重労働市場モデル：潜在的な問題点と懸念事項

## 概要

本文書は、benchmark実装における二重労働市場モデルの潜在的な問題点と今後の改善候補を整理したものです。

---

## 1. 初期化時の失業率設定

### 現状

```java
// SFCSSMacroAgentInitialiser.java:590-593
double aggUnemployment = 0.08*(1+distr.nextDouble());
govt.setAggregateValue(StaticValues.LAG_AGGUNEMPLOYMENT, aggUnemployment);
govt.setAggregateValue(StaticValues.LAG_AGGUNEMPLOYMENT_R, aggUnemployment);
govt.setAggregateValue(StaticValues.LAG_AGGUNEMPLOYMENT_N, aggUnemployment);
```

### 問題点

- R/N別失業率を**同一値**で初期化している
- 初期の労働者分布（65%R / 35%N）と独立した値が設定される
- 初期数期間の賃金調整に影響する可能性

### 影響度

| 項目 | 評価 |
|------|------|
| 重要度 | 低 |
| シミュレーション安定性への影響 | 軽微（数期間で正常化） |
| 分析結果への影響 | 過渡期データを除外すれば無視可能 |

### 改善案

```java
// 実際のR/N別雇用状況から計算
double unempR = 1.0 - (double)employedR / (double)laborForceR;
double unempN = 1.0 - (double)employedN / (double)laborForceN;
govt.setAggregateValue(StaticValues.LAG_AGGUNEMPLOYMENT_R, unempR);
govt.setAggregateValue(StaticValues.LAG_AGGUNEMPLOYMENT_N, unempN);
```

### 推奨度: ⭐⭐ (低優先度)

---

## 2. レイオフの完全分離

### 現状

```java
// ConsumptionFirm.java:380-411
if(nbWorkers > currentWorkers) {
    // 雇用: タイプ別に独立して需要計算
    this.laborDemandR = Math.max(0, nbWorkersR - currentWorkersR);
    this.laborDemandN = Math.max(0, nbWorkersN - currentWorkersN);
} else {
    // レイオフ: タイプ別に独立して超過を計算
    int excessR = Math.max(0, currentWorkersR - nbWorkersR);
    int excessN = Math.max(0, currentWorkersN - nbWorkersN);
    int fireR = probabilisticRound(this.layoffRateR * excessR);
    int fireN = probabilisticRound(this.layoffRateN * excessN);
}
```

### 問題点

1. **タイプ間の代替がない**: R過剰でもNが優先的に解雇されない
2. **実際の労働市場との乖離**: 景気後退時、企業はまずN（非正規）を解雇する傾向
3. **CES最適比率との不整合**: レイオフ時にCES最適比率が考慮されない

### 影響度

| 項目 | 評価 |
|------|------|
| 重要度 | 中 |
| 理論的妥当性への影響 | モデル目的による |
| 分析結果への影響 | 景気循環分析時に顕著 |

### 改善案A: N優先解雇ルール

```java
// 総超過からまずNを解雇、残りをRから解雇
int totalExcess = Math.max(0, currentWorkers - nbWorkers);
int fireN = Math.min(currentWorkersN, probabilisticRound(layoffRateN * totalExcess));
int fireR = probabilisticRound(layoffRateR * Math.max(0, totalExcess - fireN));
```

### 改善案B: CES比率維持ルール

```java
// 目標比率に近づくように調整
double targetRatio = computeLaborRatio(expWageR, expWageN);
double currentRatio = (double)currentWorkersR / Math.max(1, currentWorkersN);
if (currentRatio > targetRatio) {
    // Rが過剰 → R解雇
} else {
    // Nが過剰 → N解雇
}
```

### 推奨度: ⭐⭐⭐ (モデル目的により検討)

---

## 3. 賃金格差の内生的発生

### 現状

```xml
<!-- parameters.xml -->
<bean id="macroThresholdR"><constructor-arg value="0.08" /></bean>
<bean id="macroThresholdN"><constructor-arg value="0.08" /></bean>
<bean id="cesAR"><constructor-arg value="1.0" /></bean>
<bean id="cesAN"><constructor-arg value="1.0" /></bean>
```

### 問題点

1. **同一パラメータ設定**: R/Nで初期賃金・調整パラメータが同一
2. **内生的格差の欠如**: R/N間の賃金格差が発生しにくい
3. **日本の実態との乖離**: 正規/非正規で初期賃金・生産性が大きく異なる

### 日本の実態（参考）

| 項目 | 正規 | 非正規 | 格差比 |
|------|------|--------|--------|
| 時給（2022年） | 約1,500円 | 約1,100円 | 1.36 |
| 年収（2022年） | 約508万円 | 約198万円 | 2.57 |

### 影響度

| 項目 | 評価 |
|------|------|
| 重要度 | 中 |
| 理論的妥当性への影響 | 高（格差分析が目的の場合） |
| 分析結果への影響 | 所得分配分析に直結 |

### 改善案

```xml
<!-- パラメータ差別化 -->
<bean id="cesAR"><constructor-arg value="1.2" /></bean>  <!-- R生産性高 -->
<bean id="cesAN"><constructor-arg value="0.8" /></bean>  <!-- N生産性低 -->

<!-- 初期賃金も分離（SFCSSMacroAgentInitialiser） -->
double initialWageR = 1.5 * baseWage;
double initialWageN = 0.8 * baseWage;
```

### 推奨度: ⭐⭐⭐ (分析目的に応じて調整)

---

## 4. 労働者タイプの固定性

### 現状

```java
// SFCSSMacroAgentInitialiser.java:139-154
if(laborTypeDistr.nextDouble() < this.laborTypeRatioR) {
    hh.setLaborType(StaticValues.LABOR_TYPE_R);
} else {
    hh.setLaborType(StaticValues.LABOR_TYPE_N);
}
// 以降、タイプは変更されない
```

### 問題点

1. **タイプ固定**: 初期設定から生涯変化しない
2. **現実との乖離**:
   - N→R（正規化）: 能力向上・景気拡大時
   - R→N（非正規化）: 失業後の再就職時

### 影響度

| 項目 | 評価 |
|------|------|
| 重要度 | 低～中 |
| 理論的妥当性への影響 | 長期分析では影響 |
| 分析結果への影響 | キャリア経路分析には不向き |

### 改善案

```java
// 失業時のR→N転換確率
public void onFired() {
    if (this.laborType == StaticValues.LABOR_TYPE_R) {
        if (prng.nextDouble() < conversionProbRtoN) {
            this.laborType = StaticValues.LABOR_TYPE_N;
        }
    }
}

// 長期雇用時のN→R転換確率
public void onEmploymentAnniversary(int years) {
    if (this.laborType == StaticValues.LABOR_TYPE_N && years >= 3) {
        if (prng.nextDouble() < conversionProbNtoR) {
            this.laborType = StaticValues.LABOR_TYPE_R;
        }
    }
}
```

### 推奨度: ⭐⭐ (将来の拡張として検討)

---

## 5. 期待賃金の初期化

### 現状

```java
// ConsumptionFirm.updatePreTaxProfits():743-757
if(countR > 0) {
    avWageR[0] = wagebillR / countR;
} else {
    avWageR[0] = this.getExpectation(StaticValues.EXPECTATIONS_WAGES_R).getExpectation();
}
```

### 問題点

- 初期にR/N労働者がいない企業で期待賃金がNaNになる可能性
- CES分解計算に影響

### 影響度

| 項目 | 評価 |
|------|------|
| 重要度 | 低 |
| シミュレーション安定性への影響 | エッジケースで発生 |
| 分析結果への影響 | 軽微 |

### 改善案

初期化時に全企業に適切なデフォルト期待値を設定

### 推奨度: ⭐⭐ (安定性向上として検討)

---

## 6. 優先度サマリー

| 問題 | 重要度 | 実装難易度 | 推奨度 |
|------|--------|-----------|--------|
| 1. 初期失業率設定 | 低 | 低 | ⭐⭐ |
| 2. レイオフ完全分離 | 中 | 中 | ⭐⭐⭐ |
| 3. 賃金格差内生化 | 中 | 低 | ⭐⭐⭐ |
| 4. タイプ固定性 | 低～中 | 高 | ⭐⭐ |
| 5. 期待賃金初期化 | 低 | 低 | ⭐⭐ |

---

## 7. 対応方針

### 短期（現状維持推奨）

- 現行実装は二重労働市場の基本構造を適切に表現
- 上記問題点は分析目的によっては無視可能

### 中期（分析目的に応じて検討）

- 問題2（レイオフ）: 景気循環分析が目的なら対応
- 問題3（賃金格差）: 所得分配分析が目的なら対応

### 長期（モデル拡張）

- 問題4（タイプ固定性）: キャリア経路分析に拡張する場合

---

*作成日: 2025-12-23*
*対象バージョン: poc branch (91600ed)*
