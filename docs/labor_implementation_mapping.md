# Caiani(2016) 労働関連実装マッピング

## 概要
このドキュメントは、Caiani(2016) SFC-ABMベンチマークモデルにおける労働関連実装の詳細マッピングを提供します。
二重労働市場（R/N）拡張の実装において、変更が必要な箇所を特定するための参照資料です。

---

## 1. 企業の必要労働量(N^D)計算

### 1.1 消費財企業 (ConsumptionFirm)
- **ファイル:** `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java`
- **メソッド:** `getRequiredWorkers()` (行267-299)
- **主要変数:**
  - `desiredOutput`: 目標産出量
  - `capitalLaborRatio`: 資本1単位あたりの必要労働量
- **ロジック:**
  - 資本ストックを生産性でソート（TreeMap使用）
  - 最高生産性の資本から順に使用
  - 各資本に対して: `requiredWorkers += capital.getQuantity() / capital.getCapitalLaborRatio()`

### 1.2 資本財企業 (CapitalFirm)
- **ファイル:** `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java`
- **メソッド:** `getRequiredWorkers()` (行542-544)
- **主要変数:**
  - `desiredOutput`: 目標産出量
  - `laborProductivity`: 労働者1人当たり生産量
  - `amountResearch`: R&D投資（行437で労働需要に含まれる）
- **ロジック:**
  - 線形計算: `nbWorkers = (int) Math.round(this.desiredOutput / this.laborProductivity)`

**変更対象:** これらのメソッドをタイプ別需要 `N^{D,R}`, `N^{D,N}` を返すように拡張

---

## 2. 離職（分離）と解雇

**重要:** `computeLaborDemand()` メソッドは `AbstractFirm` には実装されておらず、各具象クラスに個別実装されている。

### 2.1 解雇メカニズム（基底クラス）
- **ファイル:** `jmab/src/jmab/agents/AbstractFirm.java`
- **メソッド:** `fireAgent(MacroAgent employee)` (行130-134)
- **実装:**
```java
public void fireAgent(MacroAgent employee){
    LaborSupplier emp = (LaborSupplier) employee;
    emp.setEmployer(null);  // 雇用関係を切る
    emp.setLaborActive(true);  // 失業者を労働市場に戻す
}
```

### 2.2 労働需要計算（離職・解雇・求人）の完全実装箇所

#### 2.2.1 ConsumptionFirm
- **ファイル:** `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java`
- **メソッド:** `computeLaborDemand()` (行306-356)
- **プロセス:**
  1. 離職（turnover）: 行313-315
  2. 解雇（layoff）: 行329-331
  3. 求人決定: 行320-334
  4. **賃金支払い: 行336-353** ← 同一メソッド内で実行
- **パラメータ:** `turnoverLabor` (行84)

#### 2.2.2 CapitalFirm
- **ファイル:** `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java`
- **メソッド:** `computeLaborDemand()` (行422-475)
- **プロセス:**
  1. 離職（turnover）: 行429-431
  2. R&D労働需要追加: 行437
  3. 解雇（layoff）: 行447-449
  4. 求人決定: 行438-452
  5. **賃金支払い: 行454-471** ← 同一メソッド内で実行
- **パラメータ:** `turnoverLabor` (行85)

#### 2.2.3 ConsumptionFirmWagesEnd（賃金支払い分離版）
- **ファイル:** `benchmark/benchmark/src/benchmark/agents/ConsumptionFirmWagesEnd.java`
- **メソッド:** `computeLaborDemand()` (行183-214)
- **プロセス:**
  1. 離職（turnover）: 行190-192
  2. 解雇（layoff）: 行206-208
  3. 求人決定: 行197-212
  4. 賃金支払いなし（別メソッドで実行される設計）

#### 2.2.4 CapitalFirmWagesEnd（賃金支払い分離版）
- **ファイル:** `benchmark/benchmark/src/benchmark/agents/CapitalFirmWagesEnd.java`
- **メソッド:** `computeLaborDemand()` (行177-209)
- **プロセス:**
  1. 離職（turnover）: 行185-187
  2. R&D労働需要追加: 行191
  3. 解雇（layoff）: 行202-204
  4. 求人決定: 行193-208
  5. 賃金支払いなし（別メソッドで実行される設計）

#### 2.2.5 GovernmentAntiCyclical
- **ファイル:** `benchmark/benchmark/src/benchmark/agents/GovernmentAntiCyclical.java`
- **メソッド:** `computeLaborDemand()` (行145-172)
- **別メソッド:** `payWages()` (行174-179)
- **プロセス:**
  1. 離職（turnover）: 行151-153
  2. 解雇（layoff）: 行167-169
  3. 求人決定: 行157-170
  4. 賃金支払いは別メソッド `payWages()` で実行
- **パラメータ:** `turnoverLabor` (行59), `fixedLaborDemand` (行156)

#### 2.2.6 Government2WagesEnd
- **ファイル:** `benchmark/benchmark/src/benchmark/agents/Government2WagesEnd.java`
- **メソッド:** `computeLaborDemand()` (行65-93), `payWages()` (行95-101)
- **プロセス:**
  1. 離職（turnover）: 行72-74
  2. 解雇（layoff）: 行88-90
  3. 求人決定: 行78-91
  4. 賃金支払いは別メソッド `payWages()` (行95-101) で実行

#### 2.2.7 Government（標準版）
- **ファイル:** `benchmark/benchmark/src/benchmark/agents/Government.java`
- **メソッド:** `computeLaborDemand()` (行262-285)
- **別メソッド:** `payWages()` (行288-304) - オーバーライド版
- **プロセス:**
  1. **離職なし**（turnoverなし）
  2. 解雇（layoff）: 行275-277
  3. 求人決定: 行265-278
  4. **賃金支払い: 行280-282** ← 同一メソッド内で実行
- **パラメータ:** `fixedLaborDemand` (行264) - 固定労働需要
- **特徴:**
  - 分離（turnover）がない
  - `payWages()` をオーバーライドし、**支払不能チェックなし**（常に支払う）
  - 固定労働需要を維持（景気変動と無関係）

### 2.3 共通ロジックパターン（企業系のみ）

**注意:** 以下のパターンは企業（ConsumptionFirm/CapitalFirm系）には適用されるが、**Government.javaには当てはまらない**（turnoverなし、固定労働需要）。

**離職（Turnover/Separation）:**
```java
for(int i=0; i<this.turnoverLabor*currentWorkers; i++){
    fireAgent((MacroAgent)emplPop.get(i));
}
```

**解雇（Layoff - 需要減少時）:**
```java
if(nbWorkers < currentWorkers){
    for(int i=0; i<currentWorkers-nbWorkers; i++){
        fireAgent((MacroAgent)emplPop.get(i));
    }
}
```

**求人（Vacancy）:**
```java
if(nbWorkers > currentWorkers){
    this.laborDemand = nbWorkers - currentWorkers;
    this.setActive(true, StaticValues.MKT_LABOR);
}
```

### 2.4 AbstractFirm.payWages() の支払不能時の重要な挙動

**ファイル:** `jmab/src/jmab/agents/AbstractFirm.java` (行198-218)

**支払不能時の処理（行207-214）:**
```java
if(wage < payingItem.getValue()){
    // 支払可能 → 支払い実行
    payingSupplier.transfer(payingItem, payableStock, wage);
} else {
    // 支払不能 → 即座に解雇 + 再求人
    this.setLaborActive(true);      // 行212: 労働市場に再参加
    fireAgent(employee);             // 行213: 解雇
    this.laborDemand += 1;           // 行214: 労働需要+1
}
```

**重要な設計上の意味:**
- 支払不能時に解雇した労働者は**即座に再求人される**
- `setLaborActive(true)` で労働市場への参加フラグが立つ
- `laborDemand += 1` で同じポジションの再募集が発生

**タイプ別化時の課題:**
- 解雇された労働者のタイプ（R or N）を追跡する必要がある
- 再求人は**同じタイプ**のプールに向けるべきか、別のタイプに切り替えるべきか？
- 設計選択肢:
  1. 同じタイプで再求人（保守的）
  2. タイプを切り替える（柔軟性高）
  3. 企業の判断で選択可能にする

**注意:** Government.javaの `payWages()` (行288-304) はこのメソッドを**オーバーライド**し、支払不能チェックを行わない（常に支払う）。

### 2.5 重要な設計上の注意点

1. **賃金支払いのタイミング:**
   - ConsumptionFirm/CapitalFirm/Government: 労働需要計算と同一メソッド内で賃金支払い
   - WagesEnd系/GovernmentAntiCyclical: 労働需要計算と賃金支払いが分離

2. **支払不能時の挙動:**
   - AbstractFirm.payWages(): 支払不能→即座に解雇+再求人（laborDemand+1）
   - Government.payWages(): 支払不能チェックなし（常に支払う）

3. **実装の一貫性:**
   - 各クラスが独立して同じロジックを実装
   - 基底クラスに共通実装がないため、変更は全クラスに適用必要

4. **政府の特殊性:**
   - Government.java: turnoverなし、固定労働需要、支払不能なし
   - GovernmentAntiCyclical/Government2WagesEnd: turnoverあり

**変更対象:**
- すべての `computeLaborDemand()` 実装箇所（上記7クラス）
- `turnoverLabor` → `turnoverLabor_R`, `turnoverLabor_N` (パラメータ: ϑ_R, ϑ_N)
- 解雇ロジックにタイプ別調整速度を導入 (パラメータ: η_R, η_N)
- 賃金支払いタイミングの考慮（タイプ別賃金支払い）
- **AbstractFirm.payWages()の支払不能時の再求人をタイプ別に対応**

---

## 3. 求人(V)と採用(Hire)

### 3.1 求人供給
- **ファイル:** ConsumptionFirm.java (行306-356)
- **メソッド:** `computeLaborDemand()`
- **主要変数:**
  - `laborDemand` (int): 必要な新規採用者数
  - `nbWorkers` (int): 必要総労働量
  - `currentWorkers`: 現在の従業員数
- **ロジック:**
```java
int nbWorkers = this.getRequiredWorkers();
if(nbWorkers > currentWorkers){
    this.laborDemand = nbWorkers - currentWorkers;
    this.setActive(true, StaticValues.MKT_LABOR);  // 労働市場に参加
}
```

### 3.2 採用
- **ファイル:** `jmab/src/jmab/agents/AbstractFirm.java` (行139-143)
- **メソッド:** `addEmployee(LaborSupplier worker)`
```java
public void addEmployee(LaborSupplier worker) {
    this.laborDemand -= 1;  // 労働需要を減らす
    this.employees.add(worker);
    worker.setEmployer(this);  // 雇用関係を確立
}
```

**変更対象:**
- `laborDemand` → `laborDemand_R`, `laborDemand_N`
- 求人をタイプ別に分割: `V_R`, `V_N`
- 採用メソッドをタイプ認識型に変更

---

## 4. 労働市場マッチング

### 4.1 マッチングプロセス
- **ファイル:** ConsumptionFirm.java (行239-244)
- **メソッド:** `onAgentArrival(AgentArrivalEvent event)`
```java
case StaticValues.MKT_LABOR:
    SelectWorkerStrategy strategy = (SelectWorkerStrategy)
        this.getStrategy(StaticValues.STRATEGY_LABOR);
    MacroAgent worker = (MacroAgent)strategy.selectWorker(event.getObjects());
    macroSim.getActiveMarket().commit(this, worker, marketID);
    break;
```

### 4.2 労働者選択戦略
- **インターフェース:** `jmab/src/jmab/strategies/SelectWorkerStrategy.java`
- **メソッド:**
  - `selectWorker(List<Agent> workers)`: 単一労働者選択
  - `selectWorkers(List<Agent> workers, int n)`: 複数労働者選択

### 4.3 ランダム選択実装
- **ファイル:** `jmab/src/jmab/strategies/SelectRandomWorkerStrategy.java`
- **メソッド:** `selectWorker()` (行41-45)
```java
public MacroAgent selectWorker(List<Agent> workers) {
    AgentList agents = new AgentList(workers);
    agents.shuffle(prng);  // ランダムシャッフル
    return (MacroAgent)agents.get(0);  // 最初の候補者を選択
}
```

**変更対象:**
- タイプ別失業プール: `U_R`, `U_N`
- マッチングを2回実行（タイプごと）
- サンプリング数パラメータ: `χ_R`, `χ_N`

---

## 5. 家計の要求賃金更新

### 5.1 賃金更新メカニズム
- **ファイル:** `jmab/src/jmab/strategies/AdaptiveWageStrategy.java`
- **メソッド:** `computeWage()` (行42-54)
- **主要パラメータ:**
  - `microThreshold`: ミクロレベルの失業閾値
  - `macroThreshold`: マクロレベルの失業率閾値
  - `microAdaptiveParameter`: ミクロ適応パラメータ
  - `macroAdaptiveParameter`: マクロ適応パラメータ
  - `distribution`: ランダム分布

### 5.2 賃金更新ロジック
```java
double microReferenceVariable = worker.getMicroReferenceVariableForWage();
double wage = worker.getWage();

if(microReferenceVariable > microThreshold){
    // 失業期間が長い場合: 賃金減少
    wage -= (microAdaptiveParameter * wage * distribution.nextDouble());
} else {
    double macroReferenceVariable = worker.getMacroReferenceVariableForWage();
    if(macroReferenceVariable <= macroThreshold){
        // マクロ失業率が低い場合: 賃金増加
        wage += (macroAdaptiveParameter * wage * distribution.nextDouble());
    }
}
return Math.max(wage, worker.getWageLowerBound());
```

### 5.3 失業履歴計算
- **ファイル:** `benchmark/benchmark/src/benchmark/agents/Households.java` (行397-407)
- **メソッド:** `getMicroReferenceVariableForWage()`
```java
if(this.employer == null){
    double averageUnemployment = 1;
    for(int i=1; i<this.employmentWageLag; i++){
        averageUnemployment += this.getPassedValue(
            StaticValues.LAG_EMPLOYED, i);
    }
    return averageUnemployment / employmentWageLag;
}
```
- **主要変数:**
  - `LAG_EMPLOYED`: 雇用状態の過去値 (0=失業, 1=雇用)
  - `employmentWageLag`: 考慮する過去期間数

**変更対象:**
- タイプ別閾値: `υ_R`, `υ_N`
- タイプ別適応パラメータ（必要に応じて）
- 家計に `labor_type ∈ {R,N}` 属性を追加

---

## 6. 賃金支払・平均賃金・失業給付

### 6.1 賃金支払
- **ファイル:** `jmab/src/jmab/agents/AbstractFirm.java` (行198-218)
- **メソッド:** `payWages(Item payingItem, int idMarket)`
```java
for(int i=0; i<currentWorkers; i++){
    LaborSupplier employee = (LaborSupplier) emplPop.get(i);
    double wage = employee.getWage();
    if(wage < payingItem.getValue()){
        payingSupplier.transfer(payingItem, payableStock, wage);
    } else {
        fireAgent(employee);  // 賃金支払不可の場合は解雇
    }
}
```

### 6.2 賃金総額
- **ファイル:** AbstractFirm.java (行190-196)
- **メソッド:** `getWageBill()`
```java
double wageBill = 0;
for(MacroAgent employee : employees){
    wageBill += ((LaborSupplier)employee).getWage();
}
return wageBill;
```

### 6.3 平均賃金記録
- **ファイル:** ConsumptionFirm.java (行616-625)
```java
double wagebill = 0;
for(MacroAgent employee : this.employees){
    wagebill += ((LaborSupplier)employee).getWage();
}
double[] avWage = new double[1];
if(this.employees.size() > 0)
    avWage[0] = wagebill / this.employees.size();
this.getExpectation(StaticValues.EXPECTATIONS_WAGES)
    .addObservation(avWage);
```

### 6.4 失業給付（Dole）
- **ファイル:** `benchmark/benchmark/src/benchmark/agents/GovernmentAntiCyclical.java` (行111-139)
- **メソッド:** `payUnemploymentBenefits(SimulationController simulationController)`
- **パラメータ:** `unemploymentBenefit` (失業給付率)
- **実装:**
```java
// 平均賃金を計算
double averageWage = 0;
double employed = 0;
for(Agent agent : households.getAgents()){
    Households worker = (Households) agent;
    if(worker.getEmployer() != null){
        averageWage += worker.getWage();
        employed += 1;
    }
}
averageWage = averageWage / employed;
double unemploymentBenefit = averageWage * this.unemploymentBenefit;

// 失業者に支給
for(Agent agent : households.getAgents()){
    Households worker = (Households) agent;
    if(worker.getEmployer() == null){
        payingSupplier.transfer(depositGov, payableStock,
            unemploymentBenefit);
        doleAmount += unemploymentBenefit;
    }
}
```

**変更対象:**
- タイプ別平均賃金: `W_R`, `W_N`
- 加重平均賃金: `bar_w = (Σ_ℓ W_ℓ * N_ℓ_total) / (Σ_ℓ N_ℓ_total)`
- ドール計算: `dole = ω * bar_w`

---

## 7. 生産決定（労働制約）

### 7.1 生産メカニズム
- **ファイル:** ConsumptionFirm.java (行446-511)
- **メソッド:** `produce()`
- **ロジック:**
```java
protected void produce() {
    double outputQty = 0;
    if(this.employees.size() > 0){
        // 資本をスケジュール順にソート
        TreeMap<Double, ArrayList<CapitalGood>> orderedCapital = ...;

        double residualWorkers = this.employees.size();
        for(Double key : orderedCapital.descendingKeySet()){
            for(CapitalGood capital : orderedCapital.get(key)){
                double employedWorkers = capital.getQuantity()
                    / capital.getCapitalLaborRatio();
                if(employedWorkers < residualWorkers){
                    // この資本全体を使用
                    outputQty += capital.getProductivity()
                        * capital.getQuantity();
                    residualWorkers -= employedWorkers;
                } else {
                    // この資本を一部使用
                    outputQty += capital.getCapitalLaborRatio()
                        * residualWorkers * capital.getProductivity();
                    residualWorkers = 0;
                }
            }
        }
        inventories.setUnitCost((amortisationCosts + this.getWageBill())
            / outputQty);
    }
}
```

**変更対象:**
- `employees.size()` → CES実効労働 `N_eff(N_R, N_N)`
- 資本財企業でも同様の変更
- 労働不足時の安全弁: `y_k = min(y^D_k, μ_N * N_eff)`

---

## 8. 価格設定（単位労働費用×マークアップ）

### 8.1 価格設定戦略
- **ファイル:** `jmab/src/jmab/strategies/AdaptiveMarkUpOnAC.java`
- **メソッド:** `computePrice()` (行50-80)
- **ロジック:**
```java
double referenceVariable = seller.getReferenceVariableForPrice();
double price = seller.getPrice();
double previousLowerBound = price / (1 + markUp);

if(referenceVariable > threshold){
    // 在庫が多い: マークアップ削減
    markUp -= (adaptiveParameter * markUp * distribution.nextDouble());
} else {
    // 在庫が少ない: マークアップ増加
    markUp += (adaptiveParameter * markUp * distribution.nextDouble());
}

// 価格計算: AC * (1 + μ)
if(seller.getPriceLowerBound() != 0){
    price = seller.getPriceLowerBound() * (1 + markUp);
} else {
    price = previousLowerBound * (1 + markUp);
}
```

### 8.2 単位労働費用（消費財企業）
- **ファイル:** ConsumptionFirm.java (行835-905)
- **メソッド:** `getPriceLowerBound()`
```java
double expectedVariableCosts = this.getExpectation(
    StaticValues.EXPECTATIONS_WAGES).getExpectation()
    * this.getRequiredWorkers();
expectedAverageCosts = (expectedVariableCosts)
    / this.getDesiredOutput();
```

### 8.3 単位労働費用（資本財企業）
- **ファイル:** CapitalFirm.java (行817-821)
- **メソッド:** `getPriceLowerBound()`
```java
double expectedAverageVarCosts = this.getExpectation(
    StaticValues.EXPECTATIONS_WAGES).getExpectation()
    / this.getLaborProductivity();
```

**変更対象:**
- `W^e * N^D` → `W^e_R * N^{D,R} + W^e_N * N^{D,N}`
- 期待賃金をタイプ別に管理: `W^e_R`, `W^e_N`

---

## 9. 時間ステップ構造

### 労働市場スケジュール
- `TIC_LABORSUPPLY` (11): 労働者が賃金設定
- `TIC_LABORDEMAND` (12): 企業が労働需要計算・賃金支払
- `TIC_GOVERNMENTLABOR` (13): 政府が労働需要計算
- `TIC_LABORMARKET` (14): 市場マッチング実行
- `TIC_WAGEPAYMENT` (21): 政府が失業給付支払

### 全体構造
- TIC_COMPUTEEXPECTATIONS (0): 期待形成・産出決定
- TIC_CAPITALPRICE (1): 資本財価格設定
- TIC_CONSUMPTIONPRICE (2): 消費財価格設定
- TIC_UPDATEEXPECTATIONS (1001): 期待値更新

**変更箇所:** タイプ別マッチングのために TIC_LABORMARKET を拡張

---

## 10. 関連ファイル一覧

### エージェント実装（基底クラス）
1. `jmab/src/jmab/agents/AbstractFirm.java` - 基底クラス（fireAgent, addEmployee等）
2. `jmab/src/jmab/agents/AbstractHousehold.java` - 家計基底クラス

### エージェント実装（企業バリアント - 全変更必要）
1. `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java` - 消費財企業（標準版）
2. `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java` - 資本財企業（標準版）
3. `benchmark/benchmark/src/benchmark/agents/ConsumptionFirmWagesEnd.java` - 消費財企業（賃金支払い分離版）
4. `benchmark/benchmark/src/benchmark/agents/CapitalFirmWagesEnd.java` - 資本財企業（賃金支払い分離版）

### エージェント実装（政府バリアント - 全変更必要）
1. `benchmark/benchmark/src/benchmark/agents/Government.java` - 政府（標準版）
2. `benchmark/benchmark/src/benchmark/agents/GovernmentAntiCyclical.java` - 政府（景気循環対応版）
3. `benchmark/benchmark/src/benchmark/agents/Government2WagesEnd.java` - 政府（賃金支払い分離版）

### エージェント実装（家計）
1. `benchmark/benchmark/src/benchmark/agents/Households.java` - 家計実装

### 戦略実装
1. `jmab/src/jmab/strategies/AdaptiveWageStrategy.java` - 適応的賃金戦略
2. `jmab/src/jmab/strategies/SelectRandomWorkerStrategy.java` - ランダム労働者選択
3. `jmab/src/jmab/strategies/AdaptiveMarkUpOnAC.java` - 適応的マークアップ戦略
4. `benchmark/benchmark/src/benchmark/strategies/AdaptiveMarkUpAveragePrice.java` - 平均価格マークアップ

### 定数・パラメータ
- `benchmark/benchmark/src/benchmark/StaticValues.java` - 定数定義

---

## 11. 変更対象まとめ

### データ構造の拡張が必要な箇所
1. **AbstractFirm.java**: `employees` → タイプ別管理（または2つのリストに分離）
2. **Households.java**: `labor_type` 属性追加（固定属性: R or N）
3. **StaticValues.java**: タイプ別定数追加（市場ID、パラメータID等）

### ロジックの拡張が必要な箇所（重要な修正）

**企業・政府の労働需要計算（7クラス全て変更必要）:**
1. **ConsumptionFirm**: `computeLaborDemand()` (行306-356) → タイプ別離職・解雇・求人・賃金支払い
2. **CapitalFirm**: `computeLaborDemand()` (行422-475) → タイプ別離職・解雇・求人・賃金支払い
3. **ConsumptionFirmWagesEnd**: `computeLaborDemand()` (行183-214) → タイプ別離職・解雇・求人
4. **CapitalFirmWagesEnd**: `computeLaborDemand()` (行177-209) → タイプ別離職・解雇・求人
5. **Government**: `computeLaborDemand()` (行262-285), `payWages()` (行288-304) → タイプ別対応（離職なし、固定需要）
6. **GovernmentAntiCyclical**: `computeLaborDemand()` (行145-172), `payWages()` (行174-179) → タイプ別対応
7. **Government2WagesEnd**: `computeLaborDemand()` (行65-93), `payWages()` (行95-101) → タイプ別対応

**労働需要分解（CES）:**
8. **ConsumptionFirm/CapitalFirm + WagesEnd系**: `getRequiredWorkers()` → タイプ別需要分解 `(N^{D,R}, N^{D,N})`

**採用・マッチング:**
9. **AbstractFirm**: `addEmployee()` (行139-143) → タイプ認識型に変更
10. **AbstractFirm**: `payWages()` (行198-218) → 支払不能時の再求人をタイプ別に対応
11. **SelectWorkerStrategy系**: タイプ別マッチング実装（または新規戦略）

**賃金戦略:**
12. **AdaptiveWageStrategy**: `computeWage()` (行42-54) → タイプ別閾値 `υ_R`, `υ_N`

**生産:**
13. **ConsumptionFirm**: `produce()` (行446-511) → CES実効労働 `N_eff(N_R, N_N)`
14. **CapitalFirm**: `produce()` → 同様にCES実効労働

**価格設定:**
15. **ConsumptionFirm**: `getPriceLowerBound()` (行835-905) → タイプ別賃金費用
16. **CapitalFirm**: `getPriceLowerBound()` (行817-821) → タイプ別賃金費用
17. **ConsumptionFirmWagesEnd/CapitalFirmWagesEnd**: 同様の変更

**政府・失業給付:**
18. **GovernmentAntiCyclical**: `payUnemploymentBenefits()` (行111-139) → 加重平均賃金 `bar_w`

### 新規追加が必要な要素
1. **CES実効労働関数**: `N_eff(N_R, N_N)` - 数値安定性を考慮した実装
2. **需要分解ソルバ**: `(N^{D,R}, N^{D,N})` 計算 - 閉形式またはソルバ
3. **タイプ別パラメータ**:
   - 分離率: ϑ_R, ϑ_N
   - 解雇調整速度: η_R, η_N (制約: 0 < η_R < η_N ≤ 1)
   - マッチング: χ_R, χ_N
   - 要求賃金閾値: υ_R, υ_N
   - CES: δ, ρ, A_R, A_N, ε, φ_min, φ_max
   - 消費: c1_R, c1_N, c2
   - ドール: ω
4. **タイプ別集計指標**: U_R, U_N, u_R, u_N, W_R, W_N, N_R_total, N_N_total

### 重要な実装上の注意
1. **AbstractFirmにcomputeLaborDemand()は存在しない** - 各クラスに個別実装（7クラス）
2. **全バリアントクラスを変更しないとモデルが不整合になる** - 7クラス全て対応必須
   - 企業: ConsumptionFirm, CapitalFirm, ConsumptionFirmWagesEnd, CapitalFirmWagesEnd
   - 政府: Government, GovernmentAntiCyclical, Government2WagesEnd
3. **賃金支払いタイミングの違い** - 標準版（ConsumptionFirm/CapitalFirm/Government）は労働需要計算内、WagesEnd系は分離
4. **Government.javaの特殊性** - turnoverなし、固定労働需要、支払不能チェックなし
5. **AbstractFirm.payWages()の支払不能時挙動** - 解雇→即座に再求人（laborDemand+1）。タイプ別化時に再求人先を設計する必要あり
6. **設定ファイルでの分岐** - どのバリアントが使われるかを確認する必要あり

---

## 12. 次のステップ

セクション1の前提チェックが完了しました（修正版）。次はタスクファイルのセクション2以降に進みます：
- セクション2: データ構造の拡張
- セクション3: パラメータの追加・管理
- セクション4: 労働需要の分解（CES実装）

## 13. 修正履歴

**2025-12-18 (修正1): 初回作成後の重要な修正**
- Section 2を完全に書き換え: computeLaborDemand()の全実装箇所を特定
- 6つの企業/政府バリアントクラスを追加（WagesEnd系含む）
- 賃金支払いタイミングの違いを明確化
- AbstractFirmにcomputeLaborDemand()が存在しないことを明記
- Section 10（関連ファイル一覧）にバリアントクラスを追加
- Section 11（変更対象まとめ）を16項目に拡張し、全バリアント対応を明記

**2025-12-18 (修正2): 重大な漏れの修正**
- **Government.java（標準政府版）を追加** - 7つ目のバリアントクラス
- Section 2.2.7として Government.java の詳細を追加:
  - turnoverなし、固定労働需要、支払不能チェックなし
  - computeLaborDemand() (行262-285)
  - payWages() (行288-304) のオーバーライド
- **Section 2.4を新設**: AbstractFirm.payWages()の支払不能時挙動を詳細記述
  - 支払不能→解雇→即座に再求人（laborDemand+1）
  - タイプ別化時の設計課題を明記
- Section 2.3に注意書き追加: 共通パターンはGovernment.javaには当てはまらない
- Section 10（関連ファイル一覧）にGovernment.javaを追加
- Section 11（変更対象まとめ）を7クラス・18項目に拡張:
  - AbstractFirm.payWages()の支払不能時対応を追加
  - Government.javaの特殊性を注意事項に追加
