# Phase B3 実装進捗メモ

## 完了日
2025-12-21

## 概要
マッチング経路（onAgentArrival）をR/N市場に対応させ、労働市場の構造的分離を完成させました。R市場とN市場の両方が同一TICで順次実行されるよう、XMLの市場定義も追加しています。

## 完了済み

### 1. ConsumptionFirm.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java`
**変更箇所**: onAgentArrival（MKT_LABOR系のswitch）

**実装内容**:
- `MKT_LABOR_R` / `MKT_LABOR_N` のcaseを追加
- 既存の1人ずつ採用（selectWorker）を維持

### 2. CapitalFirm.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java`
**変更箇所**: onAgentArrival（MKT_LABOR系のswitch）

**実装内容**:
- `MKT_LABOR_R` / `MKT_LABOR_N` のcaseを追加
- ConsumptionFirmと同じパターンで単一採用を維持

### 3. modelBenchmark_light.xml ✓
**ファイル**: `benchmark/benchmark/Model/modelBenchmark_light.xml`

**実装内容**:
- `laborMarketR` / `laborMarketN` と初期化beanを追加
- macroSimulationの`markets`にR/N市場を登録
- `laborMarketN`のbuyersから政府を除外

### 4. modelBenchmark_full.xml ✓
**ファイル**: `benchmark/benchmark/Model/modelBenchmark_full.xml`

**実装内容**:
- light版と同様にR/N市場定義を同期
- macroSimulationの`markets`にR/N市場を登録

### 5. WagesEnd系の確認 ✓
**ファイル**: `benchmark/benchmark/src/benchmark/agents/ConsumptionFirmWagesEnd.java`,
`benchmark/benchmark/src/benchmark/agents/CapitalFirmWagesEnd.java`

**確認結果**:
- onAgentArrivalをオーバーライドしていないため、親クラスの実装が適用される
- 追加修正不要

## 実装パターン

### R/N市場での採用
```java
case StaticValues.MKT_LABOR_R:
	SelectWorkerStrategy strategyR = (SelectWorkerStrategy) this.getStrategy(StaticValues.STRATEGY_LABOR);
	MacroAgent workerR = (MacroAgent)strategyR.selectWorker(event.getObjects());
	macroSim.getActiveMarket().commit(this, workerR, marketID);
	break;
case StaticValues.MKT_LABOR_N:
	SelectWorkerStrategy strategyN = (SelectWorkerStrategy) this.getStrategy(StaticValues.STRATEGY_LABOR);
	MacroAgent workerN = (MacroAgent)strategyN.selectWorker(event.getObjects());
	macroSim.getActiveMarket().commit(this, workerN, marketID);
	break;
```

### R/N市場の定義（抜粋）
```xml
<bean id="laborMarketR" class="jmab.simulations.SimpleMarketSimulation" scope="simulation">
	<property name="marketId"><util:constant static-field="benchmark.StaticValues.MKT_LABOR_R"/></property>
	<property name="buyersId">
		<list>
			<util:constant static-field="benchmark.StaticValues.CAPITALFIRMS_ID"/>
			<util:constant static-field="benchmark.StaticValues.CONSUMPTIONFIRMS_ID"/>
			<util:constant static-field="benchmark.StaticValues.GOVERNMENT_ID"/>
		</list>
	</property>
</bean>

<bean id="laborMarketN" class="jmab.simulations.SimpleMarketSimulation" scope="simulation">
	<property name="marketId"><util:constant static-field="benchmark.StaticValues.MKT_LABOR_N"/></property>
	<property name="buyersId">
		<list>
			<util:constant static-field="benchmark.StaticValues.CAPITALFIRMS_ID"/>
			<util:constant static-field="benchmark.StaticValues.CONSUMPTIONFIRMS_ID"/>
		</list>
	</property>
</bean>
```

## Phase B3 完全完了！✓

全タスク完了:
1. ✓ ConsumptionFirm/CapitalFirm のonAgentArrivalにMKT_LABOR_R/N追加
2. ✓ modelBenchmark_light.xmlにlaborMarketR/N定義とmarkets登録
3. ✓ modelBenchmark_full.xmlに同様の変更を同期
4. ✓ WagesEnd系は親クラス実装が適用されることを確認

## 次のフェーズ候補
- Phase C: CES分解・実効労働・価格統合
- 追加検証: R/N市場の実行順序と採用整合のログ確認
