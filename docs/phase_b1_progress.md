# Phase B1 実装進捗メモ

## 完了日
2025-12-21

## 概要
Phase Cで必要となる`W^e_R, W^e_N`（type別期待賃金）を準備するため、期待賃金を2系列化しました。既存の`EXPECTATIONS_WAGES`は後方互換性のため維持し、3系列を並行運用します。

## 完了済み

### 1. StaticValues.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/StaticValues.java`
- `EXPECTATIONS_WAGES_R = 5` 追加（Regular labor wage expectation）
- `EXPECTATIONS_WAGES_N = 6` 追加（Non-regular labor wage expectation）
- 既存の定数（0-4）は変更せず、後方互換性を維持

### 2. XML設定ファイル ✓
**ファイル**: `modelBenchmark_light.xml`, `modelBenchmark_full.xml`
- `expWagesR` bean定義追加（SimpleAdaptiveExpectation, numberPeriod=4, adaptiveParam=0.25）
- `expWagesN` bean定義追加（同上）
- CapitalFirm/ConsumptionFirmWagesEnd の expectations マップに登録
  - EXPECTATIONS_WAGES_R エントリ追加
  - EXPECTATIONS_WAGES_N エントリ追加

### 3. SFCSSMacroAgentInitialiser.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/init/SFCSSMacroAgentInitialiser.java`

#### CapitalFirm初期化（行294-312）
- EXPECTATIONS_WAGES_R の初期化：`hhWage * (1 + distr.nextDouble())`
- EXPECTATIONS_WAGES_N の初期化：`hhWage * (1 + distr.nextDouble())`

#### ConsumptionFirm初期化（行447-465）
- EXPECTATIONS_WAGES_R の初期化：同上
- EXPECTATIONS_WAGES_N の初期化：同上

### 4. ConsumptionFirm.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java`
**変更箇所**: updatePreTaxProfits() 行690-734

**実装内容**:
- Type別wagebillとカウントの集計（R/N分離）
- `countR > 0` の場合: `avWageR = wagebillR / countR`
- `countR == 0` の場合: 前回の `W^e_R` を保持
- `countN > 0` の場合: `avWageN = wagebillN / countN`
- `countN == 0` の場合: 前回の `W^e_N` を保持
- EXPECTATIONS_WAGES_R/N へ観測値を追加
- Legacy: 既存のEXPECTATIONS_WAGESも並行維持（後方互換性）

### 5. CapitalFirm.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java`
**変更箇所**: updatePreTaxProfits() 行751-795

ConsumptionFirm.javaと同じパターンを実装。

### 6. WagesEnd系の確認 ✓
**ファイル**: ConsumptionFirmWagesEnd.java, CapitalFirmWagesEnd.java

**確認結果**: updatePreTaxProfits()をオーバーライドしていない
- 親クラス（ConsumptionFirm/CapitalFirm）の実装が自動的に適用される
- 追加の変更不要

## 実装パターン

### Type別平均賃金の計算
```java
// Type別wagebillとカウントの集計
double wagebill = 0;
double wagebillR = 0, wagebillN = 0;
int countR = 0, countN = 0;

for(MacroAgent employee : this.employees) {
    LaborSupplier worker = (LaborSupplier) employee;
    double wage = worker.getWage();
    wagebill += wage;  // Legacy用

    if(worker.getLaborType() == StaticValues.LABOR_TYPE_R) {
        wagebillR += wage;
        countR++;
    } else {
        wagebillN += wage;
        countN++;
    }
}
```

### Type別期待賃金の更新
```java
// Phase B1: Type別期待賃金の更新
double[] avWageR = new double[1];
if(countR > 0) {
    avWageR[0] = wagebillR / countR;
} else {
    // 従業員がいない場合は前回の期待値を保持
    avWageR[0] = this.getExpectation(StaticValues.EXPECTATIONS_WAGES_R).getExpectation();
}
this.getExpectation(StaticValues.EXPECTATIONS_WAGES_R).addObservation(avWageR);

double[] avWageN = new double[1];
if(countN > 0) {
    avWageN[0] = wagebillN / countN;
} else {
    avWageN[0] = this.getExpectation(StaticValues.EXPECTATIONS_WAGES_N).getExpectation();
}
this.getExpectation(StaticValues.EXPECTATIONS_WAGES_N).addObservation(avWageN);
```

### 後方互換性の維持
```java
// Legacy: 既存のEXPECTATIONS_WAGESも並行維持（後方互換性）
double[] avWage = new double[1];
if(this.employees.size() > 0) {
    avWage[0] = wagebill / this.employees.size();
} else {
    avWage[0] = this.getExpectation(StaticValues.EXPECTATIONS_WAGES).getExpectation();
}
this.getExpectation(StaticValues.EXPECTATIONS_WAGES).addObservation(avWage);
```

## Phase B1 完全完了！✓

全タスク完了:
1. ✓ StaticValues.java: EXPECTATIONS_WAGES_R/N 定数追加
2. ✓ XML設定ファイル: expWagesR/N bean定義とマップ登録
3. ✓ SFCSSMacroAgentInitialiser: CapitalFirm/ConsumptionFirm の初期化
4. ✓ ConsumptionFirm.java: updatePreTaxProfits() でtype別平均賃金更新
5. ✓ CapitalFirm.java: updatePreTaxProfits() でtype別平均賃金更新
6. ✓ WagesEnd系: 親クラスの実装が適用されることを確認

## Phase Cへの準備完了

企業が `W^e_R, W^e_N` を安定して参照できる基盤が整いました。

Phase Cで以下の箇所が`W^e_R, W^e_N`を個別参照するよう更新される予定:
- ConsumptionFirm/CapitalFirm の getPriceLowerBound()
- ConsumptionFirm/CapitalFirm の computeLaborDemand()（CES分解時）

**次のフェーズ候補**:
- Phase B3: マッチング経路の検証（onAgentArrival/commit）
- Phase C: CES分解・実効労働・価格統合
