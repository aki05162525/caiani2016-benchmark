# Phase B3 実装計画: マッチング経路のR/N市場対応

## 目的

企業（ConsumptionFirm/CapitalFirm）のonAgentArrivalメソッドをR/N市場に対応させ、市場IDによる構造的分離を完成させる。

**完了の定義**: 企業がR市場でN労働者を、N市場でR労働者を採用することが構造的に不可能になる。

---

## 実装方針

### 採用方針の確認

1. **採用パターン**: 既存の1人ずつ採用を維持（Government=複数採用、企業=単一採用のパターンを尊重）
2. **TIC分離**: TIC_LABORMARKETを共有し、1つのTICでR/N市場を順次実行
3. **市場実行順序**: R市場 → N市場（正規雇用優先、非正規は調整弁）
4. **Legacy市場**: laborDemand=0で自然に空実行（コード変更不要）
5. **Government**: MKT_LABOR_R対応済み、MKT_LABOR_N不要（政府は全員R雇用）

---

## 実装タスク

### タスク1: ConsumptionFirm.javaの修正

**ファイル**: `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java`

**変更箇所**: `onAgentArrival`メソッド（行210-247）

**変更内容**: `MKT_LABOR_R`と`MKT_LABOR_N`のcaseを追加

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

**理由**:
- 既存の`MKT_LABOR` caseと同じパターンを使用（後方互換性）
- 1人ずつ採用（`selectWorker`）を維持

---

### タスク2: CapitalFirm.javaの修正

**ファイル**: `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java`

**変更箇所**: `onAgentArrival`メソッド（行262-283）

**変更内容**: ConsumptionFirmと同じパターンで`MKT_LABOR_R`と`MKT_LABOR_N`のcaseを追加

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

---

### タスク3: modelBenchmark_light.xml - 市場定義の追加

**ファイル**: `benchmark/benchmark/Model/modelBenchmark_light.xml`

**追加位置**: 既存の`laborMarket` bean定義の後（行365以降）

**追加内容**:

```xml
<!-- Phase B3: Regular labor market (MKT_LABOR_R) -->
<bean id="laborMarketR" class="jmab.simulations.SimpleMarketSimulation" scope="simulation">
    <property name="transaction" ref="laborMktTransactionMechanism"/>
    <property name="sellersId">
        <list>
            <util:constant static-field="benchmark.StaticValues.HOUSEHOLDS_ID"/>
        </list>
    </property>
    <property name="buyersId">
        <list>
            <util:constant static-field="benchmark.StaticValues.CAPITALFIRMS_ID"/>
            <util:constant static-field="benchmark.StaticValues.CONSUMPTIONFIRMS_ID"/>
            <util:constant static-field="benchmark.StaticValues.GOVERNMENT_ID"/>
        </list>
    </property>
    <property name="marketId"><util:constant static-field="benchmark.StaticValues.MKT_LABOR_R"/></property>
    <property name="mixer" ref="laborMarketMixer"/>
    <property name="scheduler" ref="simulationController"/>
    <property name="ticId"><util:constant static-field="benchmark.StaticValues.TIC_LABORMARKET"/></property>
    <property name="initialiser" ref="laborMarketInitialiserR"/>
    <property name="nbRuns" value="8000"/>
</bean>

<bean id="laborMarketInitialiserR" class="jmab.init.RandomMarketInitialiser" scope="simulation">
    <property name="marketId"><util:constant static-field="benchmark.StaticValues.MKT_LABOR_R"/></property>
    <property name="prng" ref="prng"/>
</bean>

<!-- Phase B3: Non-regular labor market (MKT_LABOR_N) -->
<bean id="laborMarketN" class="jmab.simulations.SimpleMarketSimulation" scope="simulation">
    <property name="transaction" ref="laborMktTransactionMechanism"/>
    <property name="sellersId">
        <list>
            <util:constant static-field="benchmark.StaticValues.HOUSEHOLDS_ID"/>
        </list>
    </property>
    <property name="buyersId">
        <list>
            <util:constant static-field="benchmark.StaticValues.CAPITALFIRMS_ID"/>
            <util:constant static-field="benchmark.StaticValues.CONSUMPTIONFIRMS_ID"/>
        </list>
    </property>
    <property name="marketId"><util:constant static-field="benchmark.StaticValues.MKT_LABOR_N"/></property>
    <property name="mixer" ref="laborMarketMixer"/>
    <property name="scheduler" ref="simulationController"/>
    <property name="ticId"><util:constant static-field="benchmark.StaticValues.TIC_LABORMARKET"/></property>
    <property name="initialiser" ref="laborMarketInitialiserN"/>
    <property name="nbRuns" value="8000"/>
</bean>

<bean id="laborMarketInitialiserN" class="jmab.init.RandomMarketInitialiser" scope="simulation">
    <property name="marketId"><util:constant static-field="benchmark.StaticValues.MKT_LABOR_N"/></property>
    <property name="prng" ref="prng"/>
</bean>
```

**重要ポイント**:
- `laborMarketN`の`buyersId`にはGOVERNMENT_IDを含めない（政府はR市場のみ）
- 両市場とも`ticId=TIC_LABORMARKET`を使用（1つのTICで順次実行）
- 既存の`laborMktTransactionMechanism`と`laborMarketMixer`を共有

---

### タスク4: modelBenchmark_light.xml - marketsリストへの登録

**ファイル**: `benchmark/benchmark/Model/modelBenchmark_light.xml`

**変更箇所**: `macroSimulation` beanの`markets`プロパティ（行29-40）

**変更内容**: `laborMarketR`と`laborMarketN`を追加

```xml
<property name="markets">
    <list>
        <ref bean="capitalGoodMarket" />
        <ref bean="consumptionMarket" />
        <ref bean="creditMarket" />
        <ref bean="laborMarket" />  <!-- Legacy: 後方互換性のため維持（実質空実行） -->
        <ref bean="laborMarketR" />  <!-- Phase B3: Regular labor market -->
        <ref bean="laborMarketN" />  <!-- Phase B3: Non-regular labor market -->
        <ref bean="depositMarket" />
        <ref bean="bondMarket" />
        <ref bean="reservesMarketBonds" />
        <ref bean="reservesMarketBasel" />
    </list>
</property>
```

**実行順序**:
1. `laborMarket`（legacy、laborDemand=0で空実行）
2. `laborMarketR`（正規雇用優先）
3. `laborMarketN`（非正規雇用は調整弁）

---

### タスク5: WagesEnd系クラスの確認

**対象ファイル**:
- `benchmark/benchmark/src/benchmark/agents/ConsumptionFirmWagesEnd.java`
- `benchmark/benchmark/src/benchmark/agents/CapitalFirmWagesEnd.java`

**確認事項**:
1. `onAgentArrival`メソッドをオーバーライドしているか確認
2. オーバーライドしていない場合 → 親クラスの実装が自動適用される（変更不要）
3. オーバーライドしている場合 → タスク1,2と同様の修正を適用

**実装済み情報**:
- ConsumptionFirmWagesEnd: `turnoverLaborR`, `turnoverLaborN`フィールドあり
- CapitalFirmWagesEnd: `turnoverLaborR`, `turnoverLaborN`フィールドあり
- 両クラスとも`onAgentArrival`をオーバーライドしていない可能性が高い（要確認）

---

### タスク6: modelBenchmark_full.xmlの同期

**ファイル**: `benchmark/benchmark/Model/modelBenchmark_full.xml`

**変更内容**: タスク3,4と同じ変更を`modelBenchmark_full.xml`にも適用

---

## 実装順序

```
タスク1: ConsumptionFirm.java修正
  ↓
タスク2: CapitalFirm.java修正
  ↓
タスク3: modelBenchmark_light.xml - 市場定義追加
  ↓
タスク4: modelBenchmark_light.xml - marketsリスト登録
  ↓
タスク5: WagesEnd系確認・修正
  ↓
タスク6: modelBenchmark_full.xml同期
  ↓
検証
```

**並行可能**: タスク1-2は独立、タスク3-4はXML内で連続作業
**依存関係**: タスク5はタスク1-2の完了確認が必要

---

## 検証方法

### 1. コンパイル確認
```bash
./gradlew clean build
```

### 2. 実行時ログ確認

**確認ポイント**:
- R/N市場が順次実行されること
- ConsumptionFirm/CapitalFirmが適切な市場IDでcommitすること
- 市場IDの混線がないこと

### 3. マッチング結果の検証

**検証項目**:
- Government: R市場のみから採用している
- ConsumptionFirm/CapitalFirm: employeesのlaborTypeとlaborDemandR/N減少が整合

### 4. 回帰テスト

**Phase B2機能の確認**:
- type-specific turnover/layoff
- 期待賃金2系列更新（EXPECTATIONS_WAGES_R/N）
- computeLaborDemandのtype-specific demand計算

---

## 完了条件（Definition of Done）

- [x] ConsumptionFirm/CapitalFirmのonAgentArrivalにMKT_LABOR_R/Nのcase追加
- [x] XML設定にlaborMarketR/N定義追加
- [x] marketsリストにlaborMarketR/N登録
- [x] WagesEnd系クラスの継承確認・修正
- [x] modelBenchmark_full.xmlの同期
- [x] コンパイル成功
- [x] 実行時にR/N市場が順次実行されることをログ確認
- [x] マッチング結果の型整合性確認（R労働者がR市場、N労働者がN市場）
- [x] 回帰テスト合格（Phase B2機能が破壊されていない）

---

## 重要な設計判断

1. **laborMarket（legacy）の扱い**: Phase B2でlaborDemandR/Nのみ設定するように変更済み。laborDemand=0で自然に空実行されるため、コード変更不要。後方互換性を完全維持。

2. **採用パターン**: Government=複数採用、ConsumptionFirm/CapitalFirm=単一採用のパターンを維持。既存の実装を尊重し、最小変更の原則に従う。

3. **TIC共有**: TIC_LABORMARKETを共有し、1つのTICでR/N市場を順次実行。労働市場全体が同一時点で清算される経済学的整合性を維持。

4. **市場実行順序**: R市場 → N市場。正規雇用（安定性高）が先に決まり、非正規雇用（調整弁）が後に調整される経済学的妥当性を確保。

---

## Critical Files

1. `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java` - onAgentArrival修正
2. `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java` - onAgentArrival修正
3. `benchmark/benchmark/Model/modelBenchmark_light.xml` - 市場定義・登録
4. `benchmark/benchmark/Model/modelBenchmark_full.xml` - 同期
5. `benchmark/benchmark/src/benchmark/agents/ConsumptionFirmWagesEnd.java` - 確認・修正
6. `benchmark/benchmark/src/benchmark/agents/CapitalFirmWagesEnd.java` - 確認・修正

**参照用**:
- `benchmark/benchmark/src/benchmark/agents/Government.java` - Phase B2実装パターン（複数採用）
- `jmab/src/jmab/agents/AbstractFirm.java` - addEmployeeのtype判定（Phase A4）
