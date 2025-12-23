# Dual Labor Market (R/N) 実装検証レポート

## 0. 前提：型・記号の対応表

### 結果: **Yes**

#### 検証内容:
1. **労働タイプ ℓ ∈ {R, N} の一意表現**
   - コード実体: `StaticValues.LABOR_TYPE_R=0` (StaticValues.java:244), `LABOR_TYPE_N=1` (StaticValues.java:245)
   - 根拠: enum風の定数として一意に定義され、混線しない

2. **労働市場 m ∈ {Labor_R, Labor_N} の一意表現**
   - コード実体: `StaticValues.MKT_LABOR_R=7` (StaticValues.java:240), `MKT_LABOR_N=8` (StaticValues.java:241)
   - 根拠: 市場IDとして一意に定義され、R/Nが分離されている

3. **各 household の laborType 設定**
   - コード実体: SFCSSMacroAgentInitialiser.java:149-154
   ```java
   if(laborTypeDistr.nextDouble() < this.laborTypeRatioR) {
       hh.setLaborType(StaticValues.LABOR_TYPE_R);
   } else {
       hh.setLaborType(StaticValues.LABOR_TYPE_N);
   }
   ```
   - 期中変更の可能性: Households.java に setLaborType のpublic setter はあるが、実行時に呼び出すコードは存在しない（初期化時のみ使用）
   - 根拠: 初期化時に確率的に割り当てられ、期中に未設定/意図しない変更は発生しない

4. **各 firm のタイプ別状態変数**
   - コード実体: AbstractFirm.java:38-39
   ```java
   protected int laborDemandR; // Phase A4: Regular labor demand
   protected int laborDemandN; // Phase A4: Non-regular labor demand
   ```
   - 初期化: SFCSSMacroAgentInitialiser.java:232-233, 383-384で0に初期化
   - 根拠: 企業は laborDemandR/N を保持し、タイプ別に需要を管理

---

## 1. 労働供給側（Households / Workers）

### 結果: **Yes**

#### 1.1 労働力人口（タイプ別）

**定義: LF_ℓ,t = count(laborType = ℓ)**
- コード実体: LaborForceByTypeComputer.java:60-74
```java
for (Agent agent : hhPop.getAgents()) {
    LaborSupplier hh = (LaborSupplier) agent;
    if (hh.getLaborType() != laborType) {
        continue;
    }
    totPop += 1;
}
```
- 根拠: laborType でフィルタして単純カウント。分母の定義が一貫している

#### 1.2 失業者の定義（タイプ別）

**定義: U_ℓ,t = count(laborType = ℓ AND isEmployed = false)**
- コード実体: UnemploymentCountByTypeComputer.java:48-64
```java
for (Agent agent : hhPop.getAgents()) {
    LaborSupplier hh = (LaborSupplier) agent;
    if (hh.getLaborType() != laborType) {
        continue;
    }
    if (!hh.isEmployed()) {
        unemployed += 1;
    }
}
```

**恒等式: LF_ℓ,t = E_ℓ,t + U_ℓ,t**
- E_ℓ の算出: EmploymentByTypeComputer.java:60-76 (laborType + isEmployed でカウント)
- 根拠: 同一のフィルタ条件（laborType）で LF, E, U を算出しているため恒等式は成立

---

## 2. 労働需要側（Firms）

### 結果: **Yes**

#### 2.1 タイプ別労働需要 N^{D,ℓ}

**分解ロジック: CES分解**
- コード実体: AbstractFirm.java:414-444 (computeLaborRatio), 450-471 (computeLaborSplit)
- 使用箇所: ConsumptionFirm.java:369-378, CapitalFirm.java:497-505

**CES比率計算:**
```java
double ratio = computeLaborRatio(expWageR, expWageN);
double[] split = computeLaborSplit(nbWorkers, ratio);
int nbWorkersR = Math.min(nbWorkers, Math.max(0, (int) Math.round(split[0])));
int nbWorkersN = Math.max(0, nbWorkers - nbWorkersR);
```
- 根拠: CES aggregator (δ, ρ, A_R, A_N) を使用した理論的に一貫した分解

**更新タイミング:**
- TIC_LABORDEMAND フェーズ (StaticValues.java:35, 12番目のTIC)
- ConsumptionFirm.java:178-179, CapitalFirm.java:229-230
- 根拠: 需要計算フェーズで計算し、以降のフェーズまで固定される

#### 2.2 期首の暫定雇用（分離・解雇等の反映）

**定義: N̂^ℓ_{t} = 前期雇用 − 分離/解雇**
- コード実体: ConsumptionFirm.java:327-351, CapitalFirm.java:454-478

**Type-specific turnover (ϑ_R, ϑ_N):**
```java
// Separate workers by type
for(MacroAgent emp : this.employees) {
    LaborSupplier worker = (LaborSupplier) emp;
    if(worker.getLaborType() == StaticValues.LABOR_TYPE_R) {
        workersR.add(emp);
    } else {
        workersN.add(emp);
    }
}
int turnoverFireR = (int) Math.floor(this.turnoverLaborR * workersR.size());
int turnoverFireN = (int) Math.floor(this.turnoverLaborN * workersN.size());
```
- 根拠: turnoverLaborR/N (=ϑ_R/ϑ_N) がタイプ別に適用される

**Partial layoff (η_R, η_N):**
ConsumptionFirm.java:387-411, CapitalFirm.java:514-538
```java
int excessR = Math.max(0, currentWorkersR - nbWorkersR);
int excessN = Math.max(0, currentWorkersN - nbWorkersN);
int fireR = probabilisticRound(this.layoffRateR * excessR);
int fireN = probabilisticRound(this.layoffRateN * excessN);
```
- 根拠: layoffRateR/N (=η_R/η_N) を使った部分的解雇

**期首更新の順序:**
1. Turnover firing (turnoverLaborR/N)
2. Current worker count (by type)
3. Desired demand calculation (CES split)
4. Partial layoff or hiring decision
- 根拠: separation → count → demand → vacancy/firing の順序が一貫

---

## 3. 求人（Vacancies）と採用（Hiring）

### 結果: **Yes**

#### 3.1 求人の定義

**定義: V^ℓ_{t} = max(0, N^{D,ℓ}_{t} − N̂^ℓ_{t})**
- コード実体: ConsumptionFirm.java:383-384, CapitalFirm.java:510-511
```java
this.laborDemandR = Math.max(0, nbWorkersR - currentWorkersR);
this.laborDemandN = Math.max(0, nbWorkersN - currentWorkersN);
```
- 根拠: V^ℓ = laborDemandℓ として定義され、max(0, ...) で非負性を保証

**求人件数と市場提示:**
- ConsumptionFirm.java:246-261, CapitalFirm.java:282-297
```java
case StaticValues.MKT_LABOR_R:
    if (this.laborDemandR <= 0) {
        break;
    }
    SelectWorkerStrategy strategyR = (SelectWorkerStrategy) this.getStrategy(StaticValues.STRATEGY_LABOR);
    MacroAgent workerR = (MacroAgent)strategyR.selectWorker(event.getObjects());
    macroSim.getActiveMarket().commit(this, workerR,marketID);
```
- 根拠: 求人 V^ℓ = laborDemandℓ > 0 のときのみ市場に参加し、1件ずつマッチング

#### 3.2 市場のマッチング手順

**市場分離:**
- Households.setLaborActive (Households.java:627-632)
```java
int marketId = (this.laborType == StaticValues.LABOR_TYPE_R) ?
        StaticValues.MKT_LABOR_R : StaticValues.MKT_LABOR_N;
this.setActive(active, marketId);
```
- 根拠: R労働者はMKT_LABOR_R、N労働者はMKT_LABOR_Nのみに参加

**採用上限保証 (Hire^ℓ ≤ V^ℓ):**
- AbstractFirm.addEmployee (AbstractFirm.java:152-170)
```java
if(laborType == 0) { // LABOR_TYPE_R
    if (this.laborDemandR > 0) {
        this.laborDemandR -= 1;
    }
} else { // LABOR_TYPE_N
    if (this.laborDemandN > 0) {
        this.laborDemandN -= 1;
    }
}
```
- 根拠: 採用のたびに laborDemandℓ をデクリメント。市場参加条件 (laborDemandℓ > 0) により、Hire^ℓ は常に V^ℓ を超えない

**V^ℓ = 0 時のガード:**
- ConsumptionFirm.java:247-248, 255-256; CapitalFirm.java:283-284, 291-292
```java
if (this.laborDemandR <= 0) {
    break;
}
```
- 根拠: 需要が0以下なら市場イベント処理を即座に終了

---

## 4. 雇用更新（Employment update）

### 結果: **Yes**

**定義: N^ℓ_{t} = N̂^ℓ_{t} + Hire^ℓ_{t}**
- コード実体: AbstractFirm.addEmployee (AbstractFirm.java:152-170)
```java
this.employees.add(worker);
worker.setEmployer(this);
```
- 根拠: Hire^ℓ の度に employees リストに追加され、N^ℓ が増加

**状態整合性:**
- Firm側: `employees` リストが労働者参照を保持
- Worker側: `setEmployer(this)` で雇用主参照を設定、`isEmployed()` が `employer != null` を返す
- 根拠: 双方向の参照が同時に更新され、矛盾しない

**二重計上の防止:**
- addEmployee は1回の呼び出しで1人のworkerのみ追加
- workerのemployerは単一の参照（複数の雇用主を持たない）
- 根拠: 構造的に二重計上は発生しない

---

## 5. 失業率（Unemployment rate）定義の一致

### 結果: **Yes**

**定義: u_ℓ,t**
- コード実体: UnemploymentRateByTypeComputer.java:60-81
```java
for (Agent agent : hhPop.getAgents()) {
    LaborSupplier hh = (LaborSupplier) agent;
    if (hh.getLaborType() != laborType) {
        continue;
    }
    totPop += 1;
    if (hh.isEmployed()) {
        employedPop += 1;
    }
}
if (totPop == 0) {
    return 0.0;
}
return 1 - (double) employedPop / (double) totPop;
```
- 採用された式: **u_ℓ,t = 1 − E_ℓ,t / LF_ℓ,t**
- 分母: `totPop` = `count(laborType == ℓ)` = LF_ℓ,t
- LF=0 の扱い: 明示的に 0.0 を返す (77-79行目)

**数値整合チェック:**
- U_ℓ,t / LF_ℓ,t ≈ u_ℓ,t が成立する理由:
  - U_ℓ,t = LF_ℓ,t − E_ℓ,t (恒等式)
  - u_ℓ,t = 1 − E_ℓ,t / LF_ℓ,t = (LF_ℓ,t − E_ℓ,t) / LF_ℓ,t = U_ℓ,t / LF_ℓ,t
- 根拠: 定義が一貫しているため、誤差なく一致

---

## 6. R/N 分離の非混線チェック

### 結果: **Yes（軽微な冗長コードあり）**

#### 6.1 市場分離

**R市場でN労働者が採用されない（逆も同様）:**
- Households.setLaborActive (Households.java:627-632) により、R労働者はMKT_LABOR_Rのみ、N労働者はMKT_LABOR_Nのみに参加
- 根拠: 市場IDが異なるため、物理的に混線不可能

#### 6.2 Firm の demand/vacancy/hire カウンタがℓごとに独立

**独立性の保証:**
- laborDemandR と laborDemandN は別々のフィールド (AbstractFirm.java:38-39)
- addEmployee で該当タイプのみデクリメント (AbstractFirm.java:154-163)
```java
if(laborType == 0) { // LABOR_TYPE_R
    if (this.laborDemandR > 0) {
        this.laborDemandR -= 1;
    }
} else { // LABOR_TYPE_N
    if (this.laborDemandN > 0) {
        this.laborDemandN -= 1;
    }
}
```
- 根拠: R採用でNカウンタは減らず、N採用でRカウンタは減らない

#### 6.3 Legacy 市場との重複トリガーなし

**Legacy 市場の非活性化:**
- ConsumptionFirm.java:422, 446; CapitalFirm.java:549, 572
```java
this.setActive(false, StaticValues.MKT_LABOR);
```
- Government.java:301, 312, 743, 747-748 で MKT_LABOR を明示的に無効化
- 根拠: Legacy市場は常にfalseに設定されるため、二重採用なし

**軽微な冗長コード（Government.java:741-750）:**
```java
if(active){
    this.setActive(false, StaticValues.MKT_LABOR);    // 冗長
    this.setActive(false, StaticValues.MKT_LABOR_R);  // 冗長
    this.setActive(true, StaticValues.MKT_LABOR_R);   // ← 実質的にこれのみ必要
}else{
    this.setActive(false, StaticValues.MKT_LABOR);
    this.setActive(false, StaticValues.MKT_LABOR_R);
}
```
- 逸脱内容: 機能的には問題ないが、同じMKT_LABOR_Rを2回setActive(false→true)している
- 最小修正案:
```java
if(active){
    this.setActive(true, StaticValues.MKT_LABOR_R);
}else{
    this.setActive(false, StaticValues.MKT_LABOR_R);
}
// MKT_LABORは常にfalseなので削除可能
```

---

## 7. 最終判定

### 結果: **Yes（軽微な冗長コード1件のみ）**

**満たされた項目:**
- 0. 前提：型・記号の対応表 ✓
- 1. 労働供給側 (LF, E, U の一貫した定義) ✓
- 2. 労働需要側 (CES分解、タイプ別需要) ✓
- 3. 求人と採用 (V^ℓ定義、上限保証) ✓
- 4. 雇用更新 (双方向参照、二重計上なし) ✓
- 5. 失業率定義 (u_ℓ = 1 - E_ℓ / LF_ℓ、LF=0対応) ✓
- 6. R/N分離 (市場分離、独立カウンタ、Legacy無効化) ✓

**逸脱箇所:**
- Government.java:741-750 の冗長なsetActive呼び出し（機能的影響なし）

**総合判定:**
**「数式仕様どおりに実装されている」**

実装は以下の数式・手順を正確に満たしています:
1. CES effective labor: N_eff = [δ(A_R N_R)^ρ + (1-δ)(A_N N_N)^ρ]^(1/ρ)
2. Optimal ratio: φ* = (N_R / N_N) = [(δ/(1-δ)) (A_R/A_N)^ρ (w_N/w_R)]^(1/(1-ρ))
3. Closed-form split: N_R, N_N from N_total and φ*
4. Type-specific turnover (ϑ_R, ϑ_N) and partial layoff (η_R, η_N)
5. Vacancy definition: V^ℓ = max(0, N^{D,ℓ} − N̂^ℓ)
6. Market separation: R ↔ MKT_LABOR_R, N ↔ MKT_LABOR_N
7. Unemployment rate: u_ℓ = 1 − E_ℓ / LF_ℓ

---

## 主要ファイル参照

### 型・定数定義
- `benchmark/benchmark/src/main/java/benchmark/StaticValues.java`

### 初期化
- `benchmark/benchmark/src/main/java/benchmark/init/SFCSSMacroAgentInitialiser.java`

### エージェント
- `jmab/src/main/java/jmab/agents/AbstractFirm.java`
- `benchmark/benchmark/src/main/java/benchmark/agents/Households.java`
- `benchmark/benchmark/src/main/java/benchmark/agents/ConsumptionFirm.java`
- `benchmark/benchmark/src/main/java/benchmark/agents/CapitalFirm.java`
- `benchmark/benchmark/src/main/java/benchmark/agents/Government.java`

### レポート計算
- `jmab/src/main/java/jmab/report/LaborForceByTypeComputer.java`
- `jmab/src/main/java/jmab/report/EmploymentByTypeComputer.java`
- `jmab/src/main/java/jmab/report/UnemploymentCountByTypeComputer.java`
- `jmab/src/main/java/jmab/report/UnemploymentRateByTypeComputer.java`

---

**検証実施日:** 2025-12-21
**検証対象ブランチ:** poc
**最終コミット:** b8b0a99 (refactor: Gradleを標準構成に移行、Eclipse/Legacy対応を削除)
