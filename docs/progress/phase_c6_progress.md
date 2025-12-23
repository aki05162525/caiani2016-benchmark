# Phase C6 実装進捗メモ

## 完了日
2025-12-21

## 概要
価格下限の期待変動費を2賃金・CES分解に整合させました。`W^e_R/W^e_N` と `N^{D,R}/N^{D,N}` を用いて期待変動費を計算し、ゼロ割ガードを追加しています。

## 完了済み

### 1. ConsumptionFirm.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java`

**実装内容**:
- `EXPECTATIONS_WAGES_R/N` を優先し、legacyをfallback
- `ratio = computeLaborRatio(...)` から `computeLaborSplit(...)` を取得
- `expectedVariableCosts = W^e_R * N^{D,R} + W^e_N * N^{D,N}` に置換
- `desiredOutput` / `inventoriesLeft` でゼロ割ガード

### 2. CapitalFirm.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java`

**実装内容**:
- `EXPECTATIONS_WAGES_R/N` を優先し、legacyをfallback
- `computeLaborRatio/computeLaborSplit` を使用
- `expectedVariableCosts` を2賃金で再計算
- `requiredWorkers<=0` ガードを追加

## 実装パターン

### 期待変動費の置換（抜粋）
```java
double ratio = computeLaborRatio(expWageR, expWageN);
double[] split = computeLaborSplit(requiredWorkers, ratio);
double expectedVariableCosts = expWageR * split[0] + expWageN * split[1];
```

## Phase C6 完了！✓

完了項目:
1. ✓ ConsumptionFirm の価格下限を2賃金に整合
2. ✓ CapitalFirm の価格下限を2賃金に整合
3. ✓ ゼロ割・例外ガード追加

## 次のフェーズ候補
- Phase C7: 賃金更新のtype別化
- Phase C8: 失業給付の加重平均化
