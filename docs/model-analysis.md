# Caiani & Godin (2016) ベンチマークモデル - コードリーディング調査書

**調査日:** 2025-11-08
**対象モデル:** "Innovation, Demand, and Finance in an Agent Based-Stock Flow Consistent model"
**著者:** Caiani, A., Godin, A., Caverzasi, E., Ricetti, L., Russo, A., Gallegati, M., Kinsella, S., Stiglitz, J.

---

## 目次

1. [プロジェクト概要](#1-プロジェクト概要)
2. [ディレクトリ構成](#2-ディレクトリ構成)
3. [エージェントの種類と役割](#3-エージェントの種類と役割)
4. [市場メカニズム](#4-市場メカニズム)
5. [シミュレーション実行フロー](#5-シミュレーション実行フロー)
6. [主要パラメータ設定](#6-主要パラメータ設定)
7. [レポート出力](#7-レポート出力)
8. [パラメータ修正ガイド](#8-パラメータ修正ガイド)

---

## 1. プロジェクト概要

### 1.1 モデルの特徴

本モデルは、**エージェントベース・ストックフロー一貫（Agent-Based Stock-Flow Consistent, AB-SFC）マクロ経済モデル**です。

**主な特徴:**
- 6種類の異質なエージェント（資本財企業、消費財企業、銀行、家計、政府、中央銀行）
- 8つの市場（資本財、消費財、信用、労働、預金、債券、準備金×2）
- 内生的な信用創造と金融制約
- 研究開発（R&D）による技術革新
- 景気循環対策（失業保険）
- ストックフロー一貫性（会計恒等式の厳密な遵守）

### 1.2 実装フレームワーク

- **ベース:** JMAB (Java Macroeconomic Agent-Based Modeling Toolkit)
- **依存性注入:** Spring Framework (Bean定義によるパラメータ設定)
- **設定ファイル:** XML形式のSpring Bean設定
- **言語:** Java 8以上

---

## 2. ディレクトリ構成

```
benchmark/benchmark/
├── src/
│   └── benchmark/
│       ├── agents/              # エージェント実装
│       │   ├── Bank.java
│       │   ├── CapitalFirm.java
│       │   ├── CapitalFirmWagesEnd.java      # 使用中
│       │   ├── CentralBank.java
│       │   ├── ConsumptionFirm.java
│       │   ├── ConsumptionFirmWagesEnd.java  # 使用中
│       │   ├── Government.java
│       │   ├── Government2WagesEnd.java
│       │   ├── GovernmentAntiCyclical.java   # 使用中
│       │   ├── Households.java
│       │   └── HouseholdsWithDole.java       # 使用中
│       ├── init/                # 初期化処理
│       │   └── SFCSSMacroAgentInitialiser.java
│       ├── population/          # 人口管理
│       │   └── NoEntryPopulationHandler.java
│       ├── report/              # レポート出力（70ファイル）
│       ├── strategies/          # 戦略実装（価格設定、投資、信用供給など）
│       ├── Main.java            # エントリポイント
│       └── StaticValues.java    # 定数定義
├── Model/                       # 設定ファイル
│   ├── mainBenchmark_light.xml       # メイン設定（軽量版）
│   ├── mainBenchmark_full.xml        # メイン設定（完全版）
│   ├── modelBenchmark_light.xml      # モデル詳細設定（軽量版）141KB
│   ├── modelBenchmark_full.xml       # モデル詳細設定（完全版）138KB
│   ├── reports.xml                   # レポート定義 203KB
│   ├── log4j.xml                     # ログ設定
│   └── ExperimentsExpectations/      # 期待形成実験用設定
├── data/                        # 出力先（CSV形式）
├── paper/                       # 論文・実験データ
├── lib/                         # 依存ライブラリ
└── README.md
```

**主要ファイル数:**
- Javaソースファイル: 70個
- XML設定ファイル: 17個
- レポート定義: 50種類以上

---

## 3. エージェントの種類と役割

### 3.1 エージェント一覧

| ID | エージェント名 | 実装クラス | 役割 |
|----|--------------|-----------|------|
| 0 | 資本財企業 | `CapitalFirmWagesEnd` | 機械の生産・R&D投資 |
| 1 | 消費財企業 | `ConsumptionFirmWagesEnd` | 消費財の生産・投資需要 |
| 2 | 銀行 | `Bank` | 信用仲介・預金受入・国債購入 |
| 3 | 家計 | `HouseholdsWithDole` | 労働供給・消費需要 |
| 4 | 中央銀行 | `CentralBank` | 金融政策・流動性供給 |
| 5 | 政府 | `GovernmentAntiCyclical` | 失業保険・税徴収・国債発行 |

### 3.2 各エージェントの詳細

#### 3.2.1 資本財企業 (CapitalFirmWagesEnd)

**経済的役割:**
- 資本財（機械）の生産・供給
- 研究開発（R&D）によるプロセス革新
- 労働者の雇用と賃金支払い
- 銀行からの信用需要

**主要パラメータ（フィールド変数）:**
- `minWageDiscount`: 最小賃金割引率（デフォルト: 1.0）
- `shareOfExpIncomeAsDeposit`: 予想収入の預金保有割合（デフォルト: 1.0）
- `amountResearch`: R&D投資額
- `capitalProductivity`: 資本生産性
- `capitalLaborRatio`: 資本労働比率
- `debtBurden`: 債務負担額
- `creditDemanded`: 要求信用額

**主要メソッド:**
```java
void payWages()                    // 賃金支払い（資金不足時は破産）
void computeLaborDemand()          // 労働需要計算
void computeCreditDemand()         // 信用需要計算
void onTicArrived(MacroTicEvent)   // イベント駆動型意思決定
```

**重要な特徴:**
- 賃金支払い不能時（minWageDiscount以下）は破産
- R&D投資により資本生産性が向上
- 在庫を目標水準に維持

---

#### 3.2.2 消費財企業 (ConsumptionFirmWagesEnd)

**経済的役割:**
- 消費財の生産・供給
- 資本財への投資需要
- 労働者の雇用と賃金支払い
- 在庫調整による生産計画

**主要パラメータ:**
- `minWageDiscount`: 最小賃金割引率（デフォルト: 1.0）
- `shareOfExpIncomeAsDeposit`: 預金保有割合（デフォルト: 1.0）
- `targetStock`: 目標在庫水準
- `desiredCapacityGrowth`: 希望容量成長率
- `desiredRealCapitalDemand`: 実質資本需要量
- `turnoverLabor`: 労働力回転率

**主要メソッド:**
```java
void payWages()
void computeLaborDemand()
void computeCreditDemand()
void computeDesiredInvestment()    // 投資需要計算
```

**重要な特徴:**
- 複数の資本財供給元との取引
- 投資決定は容量利用率とキャッシュフローに基づく
- 在庫と期待売上に基づく生産計画

---

#### 3.2.3 銀行 (Bank)

**経済的役割:**
- 企業への信用供給（資金仲介）
- 家計からの預金受入
- 国債購入による資産運用
- 中央銀行からの引当金借入
- Basel IIIマクロプルーデンス規制の遵守

**主要パラメータ:**
- `bankInterestRate`: 貸出金利
- `depositInterestRate`: 預金金利
- `reserveInterestRate`: 準備金金利（中央銀行から）
- `advancesInterestRate`: 引当金利（中央銀行への借入利率）
- `totalLoanSupply`: 総貸出供給額
- `capitalRatio`: 自己資本比率（Basel III）
- `liquidityRatio`: 流動性比率
- `riskAversionC`: 消費財企業へのリスク回避度（デフォルト: 3.922445）
- `riskAversionK`: 資本財企業へのリスク回避度（デフォルト: 21.513347）
- `bondDemand`: 国債需要量

**主要メソッド:**
```java
void determineCreditSupply()                  // 信用供給額決定
void determineBankGenericInterestRate()       // 貸出金利決定
void determineDepositInterestRate()           // 預金金利決定
void payDepositInterests()                    // 預金利息支払い
void determineBondDemand()                    // 国債需要決定
void determineAdvancesDemandBasel()           // Basel III準備金需要決定
```

**重要な特徴:**
- 企業タイプ別のリスク評価（資本財企業のリスクが高い）
- 自己資本比率（CAR）と流動性比率（LR）の監視
- 複数の金利設定メカニズム
- 非常に複雑なバランスシート管理

---

#### 3.2.4 家計 (HouseholdsWithDole)

**経済的役割:**
- 労働力供給
- 消費需要
- 預金者として銀行と相互作用
- 失業時は失業保険受給
- 配当・利息収入の受取

**主要パラメータ:**
- `shareDeposits`: 資産の預金保有割合
- `cashAmount`: 現金保有額
- `depositAmount`: 預金額
- `demand`: 消費需要量
- `interestsReceived`: 受取利息
- `dividendsReceived`: 受取配当金

**主要メソッド:**
```java
double getNetIncome()   // 純所得 = 賃金 + 利息 + 配当 + 失業保険
```

**重要な特徴:**
- 基本的にHouseholdsクラスを継承
- GovernmentAntiCyclicalから失業保険を受給

---

#### 3.2.5 政府 (GovernmentAntiCyclical)

**経済的役割:**
- 政府職員の雇用
- **失業保険制度の運営**（景気循環対策）
- 税金の徴収
- 国債の発行と管理
- 財政支出と歳入のバランス
- 中央銀行利益の受取

**主要パラメータ:**
- `unemploymentBenefit`: 失業保険給付率（平均賃金比、デフォルト: 0.4 = 40%）
- `doleExpenditure`: 失業保険支出総額
- `profitsFromCB`: 中央銀行からの利益配分
- `bondPrice`: 国債価格
- `bondInterestRate`: 国債金利（デフォルト: 0.0025 = 0.25%）
- `bondMaturity`: 国債満期
- `fixedLaborDemand`: 固定労働需要（デフォルト: 1360人）
- `turnoverLabor`: 労働力回転率
- `taxedPopulations`: 課税対象人口

**主要メソッド:**
```java
void computeLaborDemand()           // 固定労働需要設定
void collectTaxes()                 // 税金徴収
void payWages()                     // 政府職員への賃金支払い
void payUnemploymentBenefits()      // 失業保険支給
void payInterests()                 // 国債利息支払い
void emitBonds()                    // 国債発行
void receiveCBProfits()             // 中央銀行利益受取
void determineBondsInterestRate()   // 国債金利決定
```

**重要な特徴:**
- 失業率に基づく**自動安定化装置**
- 平均賃金の40%を失業給付として支給
- 複数税源（所得税、法人税など）
- 反景気循環的な失業保険機能

---

#### 3.2.6 中央銀行 (CentralBank)

**経済的役割:**
- 銀行への貨幣供給（引当金）
- 政策金利設定（貸出金利・準備金利）
- 国債の最終購入者（Lender of Last Resort）
- 金融システムの流動性確保
- 利益を政府に配分

**主要パラメータ:**
- `advancesInterestRate`: 引当金利（デフォルト: 0.005 = 0.5%）
- `reserveInterestRate`: 準備金金利（デフォルト: 0%）
- `interestsOnAdvances`: 引当金からの利息収入
- `interestsOnBonds`: 国債からの利息収入
- `bondInterestsReceived`: 国債利息受取額
- `bondDemand`: 国債買取需要

**主要メソッド:**
```java
void determineCBBondsPurchases()   // 国債買取決定
double getCBProfits()              // 中央銀行総利益計算
void transfer()                    // 政府との特別送金
double getInterestRate()           // 貸出金利提供
double getLoanSupply()             // 無制限信用供給
```

**重要な特徴:**
- 引当金への**無制限供給**
- 銀行の返済能力に関わらず信用供給
- 複数資産（引当金、国債）からの利息収入
- 利益全額を政府に配分

---

### 3.3 エージェント間の相互作用図

```
┌─────────────────────────────────────────────────────────────┐
│                    マクロ経済システム                        │
└─────────────────────────────────────────────────────────────┘

   家計 (Households)
      ↓ 労働供給
   企業 (Firms)  ←────────────┐
      │                      │
      │ 信用需要              │ 投資財
      ↓                      │
   銀行 (Banks)  ←────────────┤
      │                      │
      │ 引当需要              │ 国債購入
      ↓                      │
   中央銀行 (CB) ──→ 利益配分 ──→ 政府 (Gov)
                              ↓
                          失業保険
                              ↓
                           家計

【財市場】
- 資本財市場: 資本財企業 → 消費財企業
- 消費財市場: 消費財企業 → 家計

【金融市場】
- 信用市場: 銀行 → 企業（資本財・消費財）
- 預金市場: 家計 → 銀行
- 債券市場: 政府 → 銀行・中央銀行
- 準備金市場: 中央銀行 → 銀行

【労働市場】
- 企業（資本財・消費財）・政府 → 家計
```

---

## 4. 市場メカニズム

### 4.1 市場一覧

| ID | 市場名 | クラス | 取引メカニズム |
|----|--------|--------|---------------|
| 0 | 資本財市場 | `BrochureMarket` | `AtOnceMechanism` |
| 1 | 消費財市場 | `SimpleMarketSimulation` | `AtOnceMechanism` |
| 2 | 信用市場 | `SimpleMarketSimulation` | `ConstrainedCreditMechanism` |
| 3 | 労働市場 | `SimpleMarketSimulation` | `UnconstrainedLaborMechanism` |
| 4 | 預金市場 | `SimpleMarketSimulation` | `DepositMechanism` |
| 5 | 債券市場 | `BrochureMarket` | `BondMechanism` |
| 6 | 準備金市場(債券) | `SimpleMarketSimulation` | `ReservesMechanism` |
| 7 | 準備金市場(Basel) | `SimpleMarketSimulation` | `ReservesMechanism` |

### 4.2 主要市場の詳細

#### 4.2.1 資本財市場 (Capital Good Market)

**売り手:** 資本財企業
**買い手:** 消費財企業
**取引対象:** 機械（資本財）

**メカニズム:**
- **Brochureベース:** 買い手は複数の売り手のカタログを比較
- **価格・品質競争:** 生産性の高い機械が優先的に選択
- **在庫制約:** 売り手の在庫が不足すると取引不成立

#### 4.2.2 消費財市場 (Consumption Good Market)

**売り手:** 消費財企業
**買い手:** 家計

**メカニズム:**
- **RandomRobinMixer:** ランダムな買い手と売り手のマッチング
- **価格競争:** 低価格の企業が優先的に選択される傾向
- **在庫制約:** 売切時は他の企業を探索

#### 4.2.3 信用市場 (Credit Market)

**貸し手:** 銀行
**借り手:** 資本財企業、消費財企業

**メカニズム (`ConstrainedCreditMechanism`):**
1. 企業が信用需要を提示
2. 銀行が企業のリスクを評価（レバレッジ比率、過去の業績など）
3. 銀行の自己資本比率（CAR）と流動性比率（LR）の制約下で信用供給
4. **信用配分:** 銀行が複数の借り手に信用を配分（リスク加重）
5. 企業は要求額の一部しか得られない可能性あり（**信用制約**）

**重要:** 信用制約は投資と生産を抑制する主要なメカニズム

#### 4.2.4 労働市場 (Labor Market)

**供給者:** 家計
**需要者:** 資本財企業、消費財企業、政府

**メカニズム (`UnconstrainedLaborMechanism`):**
- **制約なし:** 企業の労働需要は常に充足
- **失業:** 需要不足時に失業者が発生
- **賃金:** 企業が個別に設定（適応的調整）

#### 4.2.5 債券市場 (Bond Market)

**発行者:** 政府
**購入者:** 銀行、中央銀行

**メカニズム (`BondMechanism`):**
1. 政府が財政赤字に応じて債券発行
2. 銀行が債券需要を提示（準備金の一部を債券に転換）
3. 売れ残りは中央銀行が購入（**Lender of Last Resort**）

---

## 5. シミュレーション実行フロー

### 5.1 1ティック（期間）の処理順序

以下は、`modelBenchmark_light.xml`で定義されたイベント（Tic）の順序です。

| # | Tic名 | 内容 | 関与エージェント |
|---|-------|------|-----------------|
| 0 | `computeExpectationsTic` | 期待値計算 | 全エージェント |
| 1 | `capitalPriceTic` | 資本財価格設定 | 資本財企業 |
| 2 | `consumptionPriceTic` | 消費財価格設定 | 消費財企業 |
| 3 | `capitalMarketTic1` | 資本財市場（第1回） | 資本財企業、消費財企業 |
| 4 | `rdDecisionTic` | R&D投資決定 | 資本財企業 |
| 5 | `investmentDemandTic` | 投資需要決定 | 消費財企業 |
| 6 | `creditSupplyTic` | 信用供給決定 | 銀行 |
| 7 | `depSupplyTic` | 預金供給決定 | 家計 |
| 8 | `depInterestTic` | 預金金利設定 | 銀行 |
| 9 | `creditDemandTic` | 信用需要決定 | 企業 |
| 10 | `creditMarketTic` | **信用市場取引** | 銀行、企業 |
| 11 | `laborSupplyTic` | 労働供給決定 | 家計 |
| 12 | `laborDemandTic` | 労働需要決定 | 企業 |
| 13 | `governmentLaborTic` | 政府労働需要 | 政府 |
| 14 | `laborMarketTic` | **労働市場取引** | 企業、家計、政府 |
| 15 | `productionTic` | **生産実行** | 企業 |
| 16 | `rdOutcomeTic` | R&D成果判定 | 資本財企業 |
| 17 | `consumptionDemandTic` | 消費需要決定 | 家計 |
| 18 | `consumptionMarketTic` | **消費財市場取引** | 消費財企業、家計 |
| 19 | `capitalMarketTic2` | 資本財市場（第2回） | 資本財企業、消費財企業 |
| 20 | `creditInterestsTic` | **信用利息支払い** | 企業 → 銀行 |
| 21 | `payWageTic` | **賃金支払い** | 企業 → 家計 |
| 22 | `bondInterestTic` | 債券利息支払い | 政府 → 銀行 |
| 23 | `advInterestTic` | 引当金利息支払い | 銀行 → 中央銀行 |
| 24 | `taxesTic` | **税金徴収** | 全エージェント → 政府 |
| 25 | `dividendsTic` | **配当支払い** | 企業・銀行 → 家計 |
| 26 | `depDemandTic` | 預金需要決定 | 家計、企業 |
| 27 | `depMarketTic` | 預金市場取引 | 家計 → 銀行 |
| 28 | `bankruptcyTic` | **倒産処理** | 企業、銀行 |
| 29 | `bondSupplyTic` | 債券供給決定 | 政府 |
| 30 | `bondMarket1Tic` | 債券市場取引（第1回） | 政府、銀行 |
| 31 | `bondDemandTic` | 債券需要決定 | 銀行 |
| 32 | `reservesDemandBondTic` | 準備金需要決定 | 銀行 |
| 33 | `reservesMarketBondTic` | 準備金市場取引 | 銀行、中央銀行 |
| 34 | `cbBondsPurchasesTic` | 中央銀行債券購入 | 中央銀行 |
| 35 | `bondMarket2Tic` | 債券市場取引（第2回） | 政府、銀行、中央銀行 |
| 36 | `reservesDemandBaselTic` | Basel準備金需要 | 銀行 |
| 37 | `reservesMarketBaselTic` | Basel準備金市場 | 銀行、中央銀行 |

### 5.2 主要な因果連鎖

```
期待形成 → 価格設定 → 投資決定 → 信用需要
    ↓
信用市場（信用制約が発生）
    ↓
労働需要決定 → 労働市場
    ↓
生産 → 消費財市場
    ↓
賃金・利息・配当支払い
    ↓
税金徴収 → 失業保険支払い
    ↓
倒産処理
    ↓
債券市場 → 中央銀行の流動性供給
```

---

## 6. 主要パラメータ設定

### 6.1 シミュレーション設定

**ファイル:** `mainBenchmark_light.xml`

| パラメータ | デフォルト値 | 説明 |
|-----------|------------|------|
| `numSimulations` | 1 | シミュレーション実行回数 |
| `maximumRounds` | 400 | 最大ティック数（期間） |

### 6.2 エージェント数

**設定方法:** `modelBenchmark_light.xml`内の各エージェントの`agentsList`プロパティで定義

典型的な設定（要確認）:
- 資本財企業: 数十社
- 消費財企業: 数十社
- 銀行: 数社
- 家計: 数千世帯
- 政府: 1
- 中央銀行: 1

### 6.3 主要経済パラメータ

#### 6.3.1 企業パラメータ

| パラメータ | デフォルト値 | 対象 | 説明 |
|-----------|------------|------|------|
| `minWageDiscount` | 1.0 | 資本財・消費財企業 | 最小賃金割引率（1.0 = 100%支払い必須） |
| `shareOfExpIncomeAsDeposit` | 1.0 | 資本財・消費財企業 | 予想収入の預金保有割合 |

#### 6.3.2 銀行パラメータ

| パラメータ | デフォルト値 | 説明 |
|-----------|------------|------|
| `riskAversionC` | 3.922445 | 消費財企業へのリスク回避度 |
| `riskAversionK` | 21.513347 | 資本財企業へのリスク回避度（高い） |

**注:** 資本財企業へのリスク回避度が約5.5倍高い → 資本財企業は信用制約を受けやすい

#### 6.3.3 政府パラメータ

| パラメータ | デフォルト値 | 説明 |
|-----------|------------|------|
| `unemploymentBenefit` | 0.4 | 失業保険給付率（平均賃金の40%） |
| `fixedLaborDemand` | 1360 | 政府職員数 |
| `bondInterestRate` | 0.0025 | 国債金利（0.25%） |

#### 6.3.4 中央銀行パラメータ

| パラメータ | デフォルト値 | 説明 |
|-----------|------------|------|
| `advancesInterestRate` | 0.005 | 引当金利（0.5%） |
| `reserveInterestRate` | 0.0 | 準備金金利（0%） |

### 6.4 パラメータの所在

**XMLファイル内の検索方法:**
```bash
# 特定パラメータを検索
grep 'name="unemploymentBenefit"' Model/modelBenchmark_light.xml

# 全パラメータ値を抽出
grep -E '<property name="[^"]*" value="[^"]*"' Model/modelBenchmark_light.xml
```

---

## 7. レポート出力

### 7.1 出力先

**ディレクトリ:** `benchmark/benchmark/data/`
**形式:** CSV形式（各変数ごとに1ファイル）

### 7.2 主要レポート

**ファイル:** `mainBenchmark_light.xml`の`<property name="reports">`セクションで定義

#### 7.2.1 マクロ集計レポート

| レポート名 | 内容 |
|----------|------|
| `nominalGDPCSVReport` | 名目GDP |
| `nominalInvestmentCSVReport` | 名目投資 |
| `unemploymentCSVReport` | 失業率 |
| `cAvPriceCSVReport` | 消費財平均価格 |
| `kAvPriceCSVReport` | 資本財平均価格 |
| `loanAvInterestCSVReport` | 平均貸出金利 |
| `depAvInterestCSVReport` | 平均預金金利 |
| `avWageCSVReport` | 平均賃金 |
| `avNetIncomeCSVReport` | 平均純所得 |

#### 7.2.2 ミクロレポート（企業レベル）

| レポート名 | 内容 |
|----------|------|
| `microBankCreditCSVReport` | 銀行別信用供給 |
| `microRealInvCSVReport` | 企業別実質投資 |
| `microRealSalesKCSVReport` | 資本財企業別実質売上 |
| `microRealSalesCCSVReport` | 消費財企業別実質売上 |
| `microNominalSalesKCSVReport` | 資本財企業別名目売上 |
| `microNominalSalesCCSVReport` | 消費財企業別名目売上 |
| `microCCapacityCSVReport` | 消費財企業別生産能力 |
| `microCCapacityUtilizationCSVReport` | 消費財企業別容量利用率 |
| `banksCRCSVReport` | 銀行別自己資本比率（CAR） |
| `banksLRCSVReport` | 銀行別流動性比率（LR） |
| `banksLossCSVReport` | 銀行別不良債権損失 |

#### 7.2.3 バランスシート集計レポート

| レポート名 | 内容 |
|----------|------|
| `aggHHBSCSVReport` | 家計集計バランスシート |
| `aggCFBSCSVReport` | 消費財企業集計バランスシート |
| `aggKFBSCSVReport` | 資本財企業集計バランスシート |
| `aggBBSCSVReport` | 銀行集計バランスシート |
| `aggGBSCSVReport` | 政府バランスシート |
| `aggCBBSCSVReport` | 中央銀行バランスシート |

#### 7.2.4 倒産レポート

| レポート名 | 内容 |
|----------|------|
| `kFirmsBankruptcyCSVReport` | 資本財企業倒産数 |
| `cFirmsBankruptcyCSVReport` | 消費財企業倒産数 |
| `banksBankruptcyCSVReport` | 銀行倒産数 |
| `microBailoutCostKCSVReport` | 資本財企業救済コスト |
| `microBailoutCostCCSVReport` | 消費財企業救済コスト |
| `microBailoutCostBanksCSVReport` | 銀行救済コスト |

### 7.3 レポート設定のカスタマイズ

**ファイル:** `Model/reports.xml`（203KB）

**コメントアウトされたレポート:**
- XML内の`<!-- ... -->`で囲まれた部分は出力されない
- 必要に応じてコメントを外すことで追加レポートを有効化可能

---

## 8. パラメータ修正ガイド

### 8.1 修正の基本手順

1. **XMLファイルのバックアップ作成**
   ```bash
   cp Model/modelBenchmark_light.xml Model/modelBenchmark_light_backup.xml
   ```

2. **対象パラメータの検索**
   ```bash
   grep 'name="unemploymentBenefit"' Model/modelBenchmark_light.xml
   ```

3. **XMLファイルを編集**
   - テキストエディタまたはIDEで開く
   - `<property name="パラメータ名" value="値" />`の`value`を変更

4. **シミュレーション実行**
   ```bash
   # IntelliJ IDEAから実行
   Run Configuration: Benchmark Main (Light)
   ```

5. **結果の比較**
   - `data/`ディレクトリ内のCSVファイルを確認

### 8.2 主要な実験パラメータ

#### 8.2.1 失業保険給付率の変更

**目的:** 景気循環対策の効果を検証

**パラメータ:** `unemploymentBenefit`（政府）
**デフォルト:** 0.4（40%）
**実験値例:** 0.2（20%）, 0.6（60%）, 0.8（80%）

**修正箇所:**
```xml
<!-- Model/modelBenchmark_light.xml -->
<bean id="governmentPrototype" scope="prototype" class="benchmark.agents.GovernmentAntiCyclical">
    <property name="unemploymentBenefit" value="0.6" />  <!-- 40% → 60% -->
</bean>
```

**期待される効果:**
- 高い給付率 → 消費需要の安定化、失業者の購買力維持
- 低い給付率 → 財政赤字削減、但し消費需要の不安定化

---

#### 8.2.2 銀行リスク回避度の変更

**目的:** 信用制約の強さを変更

**パラメータ:** `riskAversionC`, `riskAversionK`（銀行）
**デフォルト:**
- `riskAversionC`: 3.922445
- `riskAversionK`: 21.513347

**実験値例:**
- 緩和: `riskAversionC=2.0`, `riskAversionK=10.0`
- 厳格: `riskAversionC=6.0`, `riskAversionK=30.0`

**修正箇所:**
```xml
<bean id="bankPrototype" scope="prototype" class="benchmark.agents.Bank">
    <property name="riskAversionC" value="2.0" />   <!-- 3.922 → 2.0 -->
    <property name="riskAversionK" value="10.0" />  <!-- 21.513 → 10.0 -->
</bean>
```

**期待される効果:**
- 低いリスク回避度 → 信用供給増加、投資促進、成長加速（但し金融不安定化）
- 高いリスク回避度 → 信用制約強化、投資抑制、成長鈍化（但し金融安定化）

---

#### 8.2.3 政策金利の変更

**目的:** 金融政策の効果を検証

**パラメータ:** `advancesInterestRate`（中央銀行）
**デフォルト:** 0.005（0.5%）
**実験値例:** 0.001（0.1%）, 0.01（1.0%）

**修正箇所:**
```xml
<bean id="centralBankPrototype" scope="prototype" class="benchmark.agents.CentralBank">
    <property name="advancesInterestRate" value="0.01" />  <!-- 0.5% → 1.0% -->
</bean>
```

**期待される効果:**
- 低金利 → 銀行の調達コスト低下、貸出金利低下、投資促進
- 高金利 → 銀行の調達コスト上昇、貸出金利上昇、投資抑制

---

#### 8.2.4 政府雇用の変更

**目的:** 政府支出の経済効果を検証

**パラメータ:** `fixedLaborDemand`（政府）
**デフォルト:** 1360人
**実験値例:** 500, 2000

**修正箇所:**
```xml
<bean id="governmentPrototype" scope="prototype" class="benchmark.agents.GovernmentAntiCyclical">
    <property name="fixedLaborDemand" value="2000" />  <!-- 1360 → 2000 -->
</bean>
```

**期待される効果:**
- 高い政府雇用 → 失業率低下、総需要増加、財政赤字拡大
- 低い政府雇用 → 失業率上昇、総需要減少、財政赤字縮小

---

#### 8.2.5 企業の預金保有割合

**目的:** 企業の流動性嗜好を変更

**パラメータ:** `shareOfExpIncomeAsDeposit`（企業）
**デフォルト:** 1.0（100%を預金で保有）
**実験値例:** 0.5（50%）, 0.8（80%）

**修正箇所:**
```xml
<bean id="capitalFirmPrototype" scope="prototype" class="benchmark.agents.CapitalFirmWagesEnd">
    <property name="shareOfExpIncomeAsDeposit" value="0.5" />  <!-- 1.0 → 0.5 -->
</bean>
```

**期待される効果:**
- 低い預金割合 → 現金保有増加、支払能力向上、倒産率低下
- 高い預金割合 → 現金不足、支払困難、倒産率上昇の可能性

---

### 8.3 エージェント数の変更

**パラメータ:** 各エージェントの`agentsList`内の個別設定

**注意:** エージェント数の変更は複雑で、初期値設定（`SFCSSMacroAgentInitialiser.java`）も修正が必要な場合があります。

**推奨:** 初心者は既存のパラメータ値の変更から始め、エージェント数の変更は避ける。

---

### 8.4 実験設計のヒント

1. **1パラメータずつ変更:** 複数パラメータを同時に変更すると因果関係が不明確
2. **複数シード実行:** ランダム性の影響を排除するため`numSimulations`を増やす（但し計算時間増）
3. **ベースライン保存:** デフォルト設定での結果を必ず保存
4. **感度分析:** パラメータを段階的に変更して効果を測定
5. **ストックフロー確認:** 会計恒等式が崩れていないかチェック（TFMレポート使用）

---

### 8.5 トラブルシューティング

#### 8.5.1 シミュレーションが異常終了

**原因候補:**
- パラメータ値が非現実的（負の値、極端な値）
- エージェント数の不整合
- 倒産連鎖による全エージェント消滅

**対策:**
- パラメータ値を段階的に変更
- ログファイル（`log4j.xml`で設定）を確認

#### 8.5.2 XMLパースエラー

**原因:**
- `<idref local=`（古い構文）が残存
- XML構文エラー（閉じタグ忘れなど）

**対策:**
- `setup-windows.md`の修正手順を再確認
- XMLバリデータで構文チェック

#### 8.5.3 結果が不安定

**原因:**
- ランダムシード依存性が高い
- パラメータが臨界点付近

**対策:**
- `numSimulations`を10以上に増やして平均を取る
- 初期値のばらつきを減らす

---

## 9. 参考情報

### 9.1 関連論文

**主論文:**
Caiani, A., Godin, A., Caverzasi, E., Gallegati, M., Kinsella, S., & Stiglitz, J. E. (2016).
*Agent based-stock flow consistent macroeconomics: Towards a benchmark model.*
Journal of Economic Dynamics and Control, 69, 375-408.

**SSRN:** http://papers.ssrn.com/sol3/papers.cfm?abstract_id=2664125

### 9.2 JABMフレームワーク

**GitHub:** https://github.com/S120/jmab
**ドキュメント:** `jmab/documentation/`（JMABプロジェクト内）

### 9.3 主要概念

- **Stock-Flow Consistency (SFC):** ストックフロー一貫性（会計恒等式の厳密な遵守）
- **Agent-Based Model (ABM):** エージェントベースモデル（ミクロの意思決定からマクロ現象を創発）
- **Basel III:** 国際的な銀行規制（自己資本比率、流動性比率）
- **Lender of Last Resort:** 最後の貸し手（中央銀行による流動性供給）

---

## 10. まとめ

### 10.1 モデルの強み

1. **ストックフロー一貫性:** 会計恒等式を厳密に遵守
2. **多様なエージェント:** 6種類の異質なエージェントによる複雑な相互作用
3. **内生的な金融:** 信用創造、倒産、金融制約を明示的にモデル化
4. **柔軟な実験:** XMLパラメータ変更で多様な政策実験が可能
5. **詳細な出力:** 50種類以上のレポートによるミクロ・マクロ分析

### 10.2 研究での活用

**推奨実験:**
1. 失業保険給付率の変更（景気循環対策の効果）
2. 銀行リスク回避度の変更（信用制約の影響）
3. 政策金利の変更（金融政策の波及経路）
4. R&D投資パラメータの変更（技術革新の効果）

**分析のポイント:**
- マクロ変数（GDP、失業率、インフレ率）の時系列
- ミクロ分布（企業規模、所得分布）の変化
- 金融安定性（倒産率、不良債権比率）
- ストックフロー整合性（TFMマトリックス）

---

**調査完了**

本調査書により、Caiani & Godin (2016) ベンチマークモデルの全体像、パラメータ設定、実験手法を把握できます。
研究テーマに応じてパラメータを修正し、独自の政策実験を実施してください。

---

**作成者注:**
- 本文書は`Model/modelBenchmark_light.xml`を基準に記載
- エージェント数などの詳細設定は実際のXMLファイルを確認してください
- 不明点は`StaticValues.java`と各エージェントクラスのソースコードを参照
