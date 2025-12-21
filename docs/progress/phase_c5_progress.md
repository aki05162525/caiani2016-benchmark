# Phase C5 実装進捗メモ

## 完了日
2025-12-21

## 概要
生産（produce）における労働投入を `employees.size()` から CES実効労働 `N_eff(N_R, N_N)` に置換しました。R/N構成が生産量に反映され、ゼロ割のガードも追加しています。

## 完了済み

### 1. ConsumptionFirm.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java`

**実装内容**:
- `countR/countN` を集計し `computeEffectiveLabor` を使用
- `residualWorkers` を `effectiveLabor` ベースに置換
- `unitCost` 計算で `cesEpsilon` ガード

### 2. CapitalFirm.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java`

**実装内容**:
- `countR/countN` を集計し `computeEffectiveLabor` を使用
- `ActualLabor` を `effectiveLabor` に置換
- `unitCost` 計算で `cesEpsilon` ガード

## 実装パターン

### 実効労働の導入（抜粋）
```java
int countR = 0;
int countN = 0;
for (MacroAgent emp : this.employees) {
	LaborSupplier worker = (LaborSupplier) emp;
	if (worker.getLaborType() == StaticValues.LABOR_TYPE_R) {
		countR++;
	} else {
		countN++;
	}
}
double effectiveLabor = computeEffectiveLabor(countR, countN);
```

## Phase C5 完了！✓

完了項目:
1. ✓ ConsumptionFirm.produce の `N_eff` 化
2. ✓ CapitalFirm.produce の `N_eff` 化
3. ✓ ゼロ割ガード追加

## 次のフェーズ候補
- Phase C6: 価格（getPriceLowerBound）を2賃金費用に整合
