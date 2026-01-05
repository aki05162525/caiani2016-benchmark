# Benchmark シミュレーション パフォーマンス分析レポート

**調査日**: 2026-01-05
**対象**: `./benchmark` ディレクトリ内のJavaコード
**目的**: ラウンド300-400で実行時間が長くなる原因の特定

---

## 1. 概要

本レポートでは、シミュレーションのパフォーマンスボトルネックとなりうる箇所を詳細に分析した結果を報告する。分析の結果、以下のカテゴリで計算量が多い箇所を特定した。

| カテゴリ | 影響度 | 実行頻度 |
|---------|--------|----------|
| エージェント内ループ処理 | 高 | 毎ラウンド |
| レポート生成処理 | 高 | 毎ラウンド |
| ポピュレーション管理 | 中 | 毎ラウンド |
| 破産処理 | 中 | イベント発生時 |
| 初期化処理 | 低 | 起動時のみ |

---

## 2. 最重要ボトルネック: エージェント内の繰り返しループ

### 2.1 ConsumptionFirm - 従業員タイプ分類の重複 (最重要)

**ファイル**: `benchmark/agents/ConsumptionFirm.java`

同一メソッド内で従業員リストを**複数回**反復している。これはラウンドごとに全消費財企業で実行される。

#### 問題箇所1: `computeLaborDemand()` (行325-448)
```java
// 1回目のループ: タイプ別分類 (行331-338)
for(MacroAgent emp : this.employees) {
    LaborSupplier worker = (LaborSupplier) emp;
    if(worker.getLaborType() == StaticValues.LABOR_TYPE_R) {
        workersR.add(emp);
    } else {
        workersN.add(emp);
    }
}

// 2回目のループ: ターンオーバー解雇後のカウント (行354-363)
for(MacroAgent emp : this.employees) {
    LaborSupplier worker = (LaborSupplier) emp;
    if(worker.getLaborType() == StaticValues.LABOR_TYPE_R) {
        currentWorkersR++;
    } else {
        currentWorkersN++;
    }
}

// 3回目のループ: レイオフ用再分類 (行394-403)
for(MacroAgent emp : this.employees) {
    LaborSupplier worker = (LaborSupplier) emp;
    if(worker.getLaborType() == StaticValues.LABOR_TYPE_R) {
        emplPopR.add(emp);
    } else {
        emplPopN.add(emp);
    }
}
```

**計算量**: O(3e) where e = 従業員数/企業
**改善案**: 1回のループで全ての分類・カウントを完了させる

#### 問題箇所2: `produce()` (行539-617)
```java
// 従業員カウント (行545-552)
for(MacroAgent emp : this.employees) {
    LaborSupplier worker = (LaborSupplier) emp;
    if(worker.getLaborType() == StaticValues.LABOR_TYPE_R) {
        countR++;
    } else {
        countN++;
    }
}

// 資本ストックのソート用TreeMap構築 (行558-572)
for (Item item:currentCapitalStock){
    CapitalGood capital=(CapitalGood)item;
    double prod = capital.getProductivity();
    // TreeMapに追加...
}
```

**計算量**: O(e + k log k) where k = 資本ストック種類数

#### 問題箇所3: `getRequiredWorkers()` (行285-317)
```java
// TreeMap構築で毎回ソート
TreeMap<Double,ArrayList<CapitalGood>> orderedCapital = new TreeMap<>();
for (Item item:currentCapitalStock){
    // ...
}
```

**重要**: このメソッドは `computeCreditDemand()` からも呼ばれるため、1ラウンドで2回実行される可能性がある。

---

### 2.2 CapitalFirm - 同様のパターン

**ファイル**: `benchmark/agents/CapitalFirm.java`

#### 問題箇所: `computeLaborDemand()` (行454-578)
ConsumptionFirmと同じパターンで、従業員リストを3回反復している。

#### 問題箇所: `produce()` (行413-447)
```java
// 行417-424
for(MacroAgent emp : this.employees) {
    LaborSupplier worker = (LaborSupplier) emp;
    if(worker.getLaborType() == StaticValues.LABOR_TYPE_R) {
        countR++;
    } else {
        countN++;
    }
}
```

---

### 2.3 ローン処理の繰り返し計算

**ファイル**: `benchmark/agents/ConsumptionFirm.java` / `CapitalFirm.java`

#### 問題箇所: `computeDebtPayments()` (ConsumptionFirm: 行454-495, CapitalFirm: 行600-640)
```java
for(int i=0;i<loans.size();i++){
    Loan loan=(Loan)loans.get(i);
    // 複雑な利息計算...
    double amortization = amount*(iRate*Math.pow(1+iRate, length))/(Math.pow(1+iRate, length)-1);
}
```

**注意**: `Math.pow()` は計算コストが高い。同じ値の累乗を何度も計算している。

**計算量**: O(l) where l = ローン数
**ラウンド増加時の影響**: ローンは時間とともに蓄積される可能性があり、lが増加する

---

## 3. レポート生成処理のボトルネック

### 3.1 TFMComputer - 全エージェント走査

**ファイル**: `benchmark/report/TFMComputer.java`

`computeVariables()` メソッドで毎ラウンド全エージェントを走査している。

```java
// 消費財企業ループ (行130-151) - ネストループあり
for (Agent i:cfpop.getAgents()){
    ConsumptionFirm firm= (ConsumptionFirm) i;
    // ...
    List<Item>capGoods=firm.getItemsStockMatrix(true, StaticValues.SM_CAPGOOD);
    for (Item j:capGoods){  // ネストループ
        CapitalGood good= (CapitalGood)j;
        if(good.getAge()<0)
            invCF+=good.getValue();
    }
}

// 資本財企業ループ (行153-169)
for (Agent i:kfpop.getAgents()){ ... }

// 世帯ループ (行172-187) - ネストループあり
for (Agent i:hhpop.getAgents()){
    // ...
    List<Item>loans=hh.getItemsStockMatrix(true, StaticValues.SM_CONSGOOD);
    for (Item j:loans){ ... }  // ネストループ
}

// 銀行ループ (行189-201)
for (Agent i:bpop.getAgents()){ ... }
```

**計算量**: O(cf×k + kf + hh×c + b) where:
- cf = 消費財企業数
- k = 資本財種類数/企業
- kf = 資本財企業数
- hh = 世帯数
- c = 消費財ストック数/世帯
- b = 銀行数

### 3.2 複数のMicroComputer

同様のパターンが多数のMicroComputerで見られる:

| クラス | ファイル位置 | ループ回数 |
|--------|-------------|------------|
| MicroDebtServiceComputer | `report/MicroDebtServiceComputer.java:62-81` | O(f) |
| MicroInterestToPayComputer | `report/MicroInterestToPayComputer.java` | O(f) |
| MicroBankCRComputer | `report/MicroBankCRComputer.java:68-76` | O(b) |
| MicroBankLRComputer | `report/MicroBankLRComputer.java:68-76` | O(b) |
| MicroRealDesiredConsumptionComputer | `report/MicroRealDesiredConsumptionComputer.java:84-96` | O(hh) |

**問題**: これらが全て毎ラウンド実行されると、エージェント全数 × レポート数 の計算が発生する。

---

## 4. ポピュレーション管理のバグ

### 4.1 リスト削除中のイテレーション

**ファイル**: `benchmark/population/NoEntryPopulationHandler.java`

```java
// 行82-87
for (int i=0;i<populationAgents.size();i++){
    MacroAgent agent=(MacroAgent)populationAgents.get(i);
    if (agent.isDead()){
        populationAgents.remove(i);  // 問題: インデックスシフト
    }
}
```

**問題点**:
1. `remove(i)` 後にインデックスがシフトするが、`i++` は継続される
2. 結果として、連続した死亡エージェントがあると一部がスキップされる
3. ArrayList.remove(int) は O(n) の計算量を持つため、最悪 O(n²)

**影響**: 死亡エージェントが正しく削除されない可能性があり、リストが肥大化する恐れ

---

## 5. 破産処理の計算量

### 5.1 銀行破産処理

**ファイル**: `benchmark/strategies/BankBankruptcy.java`

```java
// 最裕福銀行の検索 (行56-62)
for(Agent b:banks.getAgents()){
    MacroAgent tempB = (MacroAgent) b;
    if(highestNW<tempB.getNetWealth()){
        highestNW=tempB.getNetWealth();
        richestBank=(Bank)tempB;
    }
}

// 資産・負債の移転 (行66-100)
for(int i = 0; i<bank.getStocksNames().size();i++){
    List<Item> assets = bank.getItemsStockMatrix(true, i);
    for(Item asset:assets){  // ネストループ
        // ...
    }
    List<Item> liabilities = bank.getItemsStockMatrix(false, i);
    for(Item liability:liabilities){  // ネストループ
        // ...
    }
}
```

**計算量**: O(b + s×i) where s = ストック種類数, i = 各ストック内アイテム数

### 5.2 企業破産処理

**ファイル**: `benchmark/strategies/FirmBankruptcy.java`

```java
// ローンループ (行77-81)
for(int i=0;i<loans.size();i++){
    Loan loan=(Loan)loans.get(i);
    debts[i]=loan.getValue();
    totalDebt+=loan.getValue();
}

// 返済配分ループ (行84-97)
for(int i=0;i<loans.size();i++){
    // 複雑な計算...
}

// 従業員解雇ループ (行100-104)
for(MacroAgent employee:firm.getEmployees()){
    // ...
}
```

---

## 6. ラウンド増加時に重くなる理由の推定

### 6.1 蓄積されるデータ構造

| データ | 蓄積の可能性 | 影響するメソッド |
|--------|-------------|-----------------|
| ローン数 | 高 | `computeDebtPayments()`, `payInterests()` |
| 従業員数 | 中 | `computeLaborDemand()`, `produce()` |
| 資本ストック種類 | 中 | `getRequiredWorkers()`, `produce()` |
| 死亡エージェント | 高(バグ) | 全てのエージェントループ |

### 6.2 計算量の増大パターン

```
ラウンド1:   base_cost
ラウンド100: base_cost + Σ(accumulated_items)
ラウンド300: base_cost + 3×Σ(accumulated_items)
ラウンド400: base_cost + 4×Σ(accumulated_items)
```

特にローン数の蓄積は、企業が新規ローンを取得し続ける限り増加し続ける。

---

## 7. 優先度別改善候補

### 優先度: 高 (即効性が高い)

| 項目 | 場所 | 改善内容 | 予想効果 |
|------|------|----------|----------|
| 従業員分類の一元化 | ConsumptionFirm/CapitalFirm | 3回のループを1回に統合 | 計算量1/3 |
| NoEntryPopulationHandler | population/ | Iterator.remove() を使用 | バグ修正 + O(n²)→O(n) |
| TreeMap構築のキャッシュ | ConsumptionFirm | ラウンド内で再利用 | getRequiredWorkers 2回呼出し対応 |

### 優先度: 中

| 項目 | 場所 | 改善内容 | 予想効果 |
|------|------|----------|----------|
| Math.pow のキャッシュ | computeDebtPayments | 同じ計算の再利用 | 利息計算高速化 |
| レポート処理の統合 | report/ | 複数MicroComputerを1パスで処理 | 全エージェント走査回数削減 |

### 優先度: 低

| 項目 | 場所 | 改善内容 |
|------|------|----------|
| 不要ローンの削除 | 返済完了ローン | リストからの削除ロジック確認 |

---

## 8. 詳細ファイル・行番号リファレンス

### ConsumptionFirm.java
- 行285-317: `getRequiredWorkers()` - TreeMap構築
- 行325-448: `computeLaborDemand()` - 従業員3重ループ
- 行454-495: `computeDebtPayments()` - ローンループ
- 行539-617: `produce()` - 従業員カウント + TreeMap構築
- 行679-696: `updateAfterTaxProfits()` - ローンループ
- 行727-739: `updatePreTaxProfits()` - 従業員ループ

### CapitalFirm.java
- 行413-447: `produce()` - 従業員カウント
- 行454-578: `computeLaborDemand()` - 従業員3重ループ
- 行600-640: `computeDebtPayments()` - ローンループ
- 行752-756: `updateAfterTaxProfits()` - ローンループ
- 行787-799: `updatePreTaxProfits()` - 従業員ループ

### Bank.java
- 行175-192: `onTicArrived()` - 預金・準備金・ローン走査
- 行302-332: `payInterests()` - アドバンスループ
- 行395-406: `payDepositInterests()` - 預金ループ
- 行545-575: `getReferenceVariableForInterestRate()` - ローン・預金・準備金走査

### TFMComputer.java
- 行130-151: 消費財企業ループ (ネスト)
- 行153-169: 資本財企業ループ
- 行172-187: 世帯ループ (ネスト)
- 行189-201: 銀行ループ

### NoEntryPopulationHandler.java
- 行82-87: バグのあるリスト削除ループ

---

## 9. 結論

ラウンド300-400で実行時間が長くなる主な原因は:

1. **従業員リストの重複反復**: 同一メソッド内で3回同じリストをループ
2. **レポート処理の累積**: 毎ラウンド複数のMicroComputerが全エージェントを走査
3. **ローン数の蓄積**: シミュレーション進行に伴いローンリストが増大
4. **ポピュレーション管理のバグ**: 死亡エージェントが正しく削除されない可能性

これらの改善により、特にラウンド数増加時のパフォーマンス低下を軽減できると推定される。
