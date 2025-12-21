# Caiani et al. (2016) AB‑SFC ベンチマーク実装リーディング

ここでは論文 *Agent Based–Stock Flow Consistent Macroeconomics: Towards a Benchmark Model*（Caiani et al., 2016）と実装（`benchmark` モジュール中心）の往復をしやすくするため、主要クラスとファイルの役割・式対応・ストック／フロー・市場イベント・SFC 実装意図を整理する。

## 時間軸とイベントの骨格（StaticValues.java）
- **役割**: 各市場イベント（`TIC_*`）とラグ変数・期待形成 ID の定義。`MacroTicEvent` の順番は XML シナリオ（`Model/mainBenchmark_*.xml`）でこの ID を使って並べられる。
- **論文対応**: セクション 3 全体のタイムライン（本文 12 項目のイベント列挙）に対応。例: 期待計算→価格設定→信用需要→雇用→生産→利払い→配当→預金決定→破綻処理。
- **主なストック/フロー ID**: `SM_*`（現金・預金・消費財在庫・資本財・ローン・国債・準備・現先）と `LAG_*`（在庫・売上・利益・雇用など）。
- **市場/イベント**: `TIC_CONSUMPTIONPRICE`（消費財価格設定）、`TIC_CREDITDEMAND`（信用需要）、`TIC_LABORDEMAND`/`TIC_LABORSUPPLY`、`TIC_PRODUCTION`、`TIC_BOND*` などが各エージェントでスイッチに使われる。
- **SFC**: 各 `TIC_*` は取引の順序を固定し、在庫→生産→利払い→税→配当→預金・債券への最終ポートフォリオ選択というフローの整合を保つ。`SM_*` ID で資産／負債の二重仕訳が強制される。

## 二重労働市場（R/N）の拡張ポイント

- **市場ID**: `MKT_LABOR_R` / `MKT_LABOR_N` を追加し、Households は自分のタイプ市場のみ参加。
- **需要分解**: 企業は CES に基づく `N^D -> (N_R^D, N_N^D)` を計算し、需要がある市場だけアクティブ化。
- **雇用/解雇**: タイプ別需要・解雇率（`eta_R`, `eta_N`）で部分解雇。採用は `laborDemandR/N` を上限にする。
- **賃金期待**: `W_R^e`, `W_N^e` を持ち、家計の賃金更新は `u_R`, `u_N` を参照。
- **政府**: 政府雇用はRのみ。失業給付はタイプ別平均賃金の加重平均を使用。
- **代表的な実装箇所**:
  - `benchmark/benchmark/src/benchmark/agents/Households.java`
  - `jmab/src/jmab/agents/AbstractFirm.java`
  - `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java`
  - `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java`
  - `benchmark/benchmark/src/benchmark/agents/Government.java`
  - `benchmark/benchmark/Model/modelBenchmark_full.xml`

## 初期化（benchmark/init/SFCSSMacroAgentInitialiser.java）
- **役割**: セクション 4（ベースライン校正）に対応し、定常状態で計算した集計値を各エージェントに均等配分しつつ、ローンや資本財の「年齢」「残高」を持つコレクションを作る。初期のネットワーク（どの家計がどの銀行に預け、どの企業がどの銀行から借りるか）もここで固定。
- **主要ストック/フロー**:
  - 家計: `Cash`＋`Deposit`（対応する銀行負債）と消費期待、過去賃金。
  - 消費/資本企業: 在庫（`ConsumptionGood`/`CapitalGood`）、`Deposit`、複数年ローン（`Loan`、発行時の利率・残存期間付き）。
  - 銀行: 準備（中央銀行の負債）、国債、貸出、現先（中央銀行への負債）、自己資本。
  - 政府: 準備口座の赤字を国債発行で埋める。
  - 中央銀行: 現金・準備・国債を保有し、現先で銀行に与信。
- **市場/イベント**: 初期時点で `CheapestGoodSupplierWithSwitching` 等に「前回の相手」を設定し、マッチングが収束済みの状態から開始。
- **SFC**: すべて `addItemStockMatrix(asset, true, …)` と `addItemStockMatrix(asset, false, …)` のペアで記録。例えば家計の預金は同額で銀行負債として登録、政府赤字は国債発行で中央銀行・銀行の資産と突き合わせる。これにより Copeland の四重仕訳を初期時点で満たす。

## 消費財企業（benchmark/agents/ConsumptionFirm.java）
- **役割**: セクション 3.1（特に 3.1.1–3.1.6）で定義された消費財部門。期待売上と目標在庫に基づき生産計画・価格・投資・信用需要を決める。
- **論文中の式**:  
  - 期待更新: (3.1)。  
  - 生産計画と在庫ターゲット: (3.2)–(3.5)。  
  - 価格とマークアップ調整: (3.6)–(3.7)。  
  - 利益計算（在庫評価・利払い・減価償却込み）: (3.8)。  
  - 目標成長と投資需要: (3.9)–(3.10)。  
  - 信用需要（内部資金＋配当計画＋予防的預金 − OCF）: (3.11)。
- **主要ストック/フロー**:
  - ストック: 在庫（`SM_CONSGOOD`）、固定資本（`CapitalGood` コレクション）、ローン負債（多期間、利率付き）、預金、現金。
  - フロー: 売上、賃金、ローン利息支払い・元本償還、預金利息受取、減価償却、配当、税。
- **市場/イベント**:
  - `TIC_COMPUTEEXPECTATIONS`→`computeExpectations` と `determineOutput`（式 (3.1)–(3.5)）。
  - `TIC_CONSUMPTIONPRICE`→`computePrice`（(3.6)–(3.7)）。
  - `TIC_INVESTMENTDEMAND` で資本財サプライヤ選択→`computeDesiredInvestment`（(3.9)–(3.10)）。
  - `TIC_CREDITDEMAND`→`computeCreditDemand`（(3.11)）。
  - `TIC_LABORDEMAND`→`computeLaborDemand`、`TIC_PRODUCTION`→`produce`。
  - `TIC_CREDINTERESTS`/`TIC_DIVIDENDS`/`TIC_DEPOSITDEMAND`/`TIC_UPDATEEXPECTATIONS` で利払い・配当・流動性配分・ラグ更新。
- **SFC**: 取引は `MarketSimulation.commit` 経由で在庫と金融ストック両側を更新。`payInterests`/`payDividends` では預金を減らし銀行負債を同額減少、税支払いは政府準備口座を同額増加。減価償却は実物ストックの簿価を下げ、会計的利益と OCF の差（式 (3.8)）を追跡する。

## 資本財企業（benchmark/agents/CapitalFirm.java）
- **役割**: セクション 3.1（同じ生産・価格・利益・投資方程式を資本財に適用）＋ 3.1.4 の R&D。資本財と労働のみで生産し、消費財企業へ設備を販売。
- **論文中の式**: (3.1)–(3.7)（生産・価格）、(3.8)（減価償却なし版）、(3.9)–(3.11)（投資・信用需要。ただし資本財企業は (3.11) から投資項を除く）。R&D の確率的成果はセクション 3.1.4（式番号なしの記述部分）。
- **主要ストック/フロー**:
  - ストック: 販売用資本財在庫（`SM_CAPGOOD`）、ローン負債、預金、現金。
  - フロー: 売上、賃金、ローン利子・元本、R&D 投資支出、配当・税。
- **市場/イベント**:
  - `TIC_COMPUTEEXPECTATIONS` から生産計画、`TIC_CAPITALPRICE` で価格設定、`TIC_CREDITDEMAND` で信用需要、`TIC_LABORDEMAND`/`TIC_PRODUCTION` で雇用と生産、`TIC_RDDECISION`/`TIC_RDOUTCOME` で R&D 実施と生産性更新、`TIC_DIVIDENDS`/`TIC_DEPOSITDEMAND` で資金配分。
- **SFC**: R&D・投資支出も銀行預金の減少と銀行負債の増加で記帳。複数年ローンの元利返済スケジュール（`debtPayments`）に基づき、銀行資産・企業負債が同期して減少。

## 銀行（benchmark/agents/Bank.java）
- **役割**: セクション 3.2（信用供給・利率設定・預金コスト調整）と 3.3（破綻処理）の実体。Basel III ターゲット資本・流動性比率を持ち、ローン供給と債券需要を決める。
- **論文中の式**:
  - ローン金利のマージン調整（資本比率に応じた競争的設定）: (3.12)。
  - 借り手デフォルト確率のロジット: (3.13)。
  - 預金金利の流動性ターゲット連動: (3.14)。
- **主要ストック/フロー**:
  - ストック（資産）: 企業ローン (`SM_LOAN`)、国債 (`SM_BONDS`)、準備 (`SM_RESERVES`)、中央銀行からの現先貸付 (`SM_ADVANCES` 負債側)。
  - ストック（負債）: 預金 (`SM_DEP`)、現先負債、自己資本（ネットワース）。
  - フロー: ローン利息収入、預金利息支払い、国債利息、税、配当、ベイルアウト負担。
- **市場/イベント**:
  - `TIC_COMPUTEEXPECTATIONS` で期待計算・流動性/資本比率算定し、ターゲットに対する超過/不足を決定。
  - `TIC_CREDITSUPPLY` で供給量計算（`SupplyCreditStrategy`）、`TIC_CREDITMARKET` でローン契約。
  - `TIC_DEPINTERESTS` で預金利払い、`TIC_CREDINTERESTS` でローン受取、`TIC_BOND*` で国債売買。
  - `TIC_TAXES`/`payTaxes` で準備口座から政府へ送金、`TIC_DIVIDENDS` で配当支払い。
- **SFC**: すべての貸出は `Loan` オブジェクトで企業負債と銀行資産に二重登録。利払い・税・配当は預金/準備を減らし、相手の資産を増やす。破綻時は負債側調整（預金ベイルインやローンの `setNonPerforming`）で貸借一致を維持。

## 中央銀行（benchmark/agents/CentralBank.java）
- **役割**: セクション 3.5。最後の貸し手として現先を無制限供給し、売れ残り国債を購入。準備と現金の発行主体。
- **論文中の式**: 政府方程式 (3.17) に出てくる中央銀行利益項 \( \pi^{CB}_t = i^b B_{t-1} + i^{a}_{CB} CA^{cb}_t \) に対応。利率設定は定数（政策金利）。
- **主要ストック/フロー**:
  - ストック: 現先貸付 (`SM_ADVANCES`、銀行負債側)、国債資産、準備・現金負債。
  - フロー: 現先利息、国債利息（`interestsOnBonds`）、政府への利益還元。
- **市場/イベント**:
  - `TIC_COMPUTEEXPECTATIONS` で利息計算し、`setActive(true, MKT_ADVANCES)` で現先市場を開く。
  - `TIC_CBBONDSPURCHASES` で銀行需要後の残余国債を買い取り。
- **SFC**: 現先は銀行の負債・中央銀行資産として記録し、同額が銀行準備に加算される。国債購入時は政府準備を増やし中央銀行資産を同額増加。

## 政府（benchmark/agents/Government.java）
- **役割**: セクション 3.5。公共雇用・失業給付・税徴収・国債発行を担う。預金ではなく中央銀行準備口座を利用。
- **論文中の式**: 予算制約 (3.17) \( p_b \Delta b_t = T_t + \pi^{CB}_t - \sum_{n\in N_g} w_n - U_t d_t - i^b p_b b_{t-1} \)。
- **主要ストック/フロー**:
  - ストック: 準備（中央銀行負債）、発行済み国債（政府負債・投資家資産）。
  - フロー: 賃金支払い、失業給付、国債利息支払い、税収。
- **市場/イベント**:
  - `TIC_GOVERNMENTLABOR` で労働需要（`SelectWorkerStrategy`）を決定。
  - `TIC_TAXES` で各納税者のストラテジを呼び税を徴収（銀行・企業・家計の預金／準備が同額減少、政府準備が増加）。
  - `TIC_BONDINTERESTS` で利払、`TIC_BONDSUPPLY` で新規国債発行。
  - `TIC_UPDATEEXPECTATIONS` で失業率（`UnemploymentRateComputer`）を集計し、次期の労働需要に反映。
- **SFC**: 準備ベースで税・支出を処理し、国債発行時は負債サイド（政府）と資産サイド（銀行／中央銀行）に同額計上。支出超過は国債数量で穴埋めし、残高は `Bond` オブジェクトとしてストックマトリクスに残る。

## 家計（benchmark/agents/Households.java）
- **役割**: セクション 3.4。労働供給と消費需要を決定し、所得税を支払う。預金／現金のポートフォリオを持つ。
- **論文中の式**:
  - 予約賃金の調整（失業履歴と集計失業率に応じた Folded Normal ノイズ）: (3.15)。
  - 消費需要（期待実質可処分所得と期待実質純資産に比例）: (3.16)。
- **主要ストック/フロー**:
  - ストック: 預金 (`SM_DEP`)、現金 (`SM_CASH`)。雇用先リンク（雇用中の企業を参照）。
  - フロー: 賃金・失業給付・預金利息・配当受取、消費支出、所得税。
- **市場/イベント**:
  - `TIC_COMPUTEEXPECTATIONS` で価格・所得の期待形成。
  - `TIC_LABORSUPPLY` で賃金提示（式 (3.15)）、`TIC_CONSUMPTIONDEMAND` で需要決定（式 (3.16)）。
  - `TIC_DEPOSITDEMAND` で現金・預金配分と銀行選択、`onAgentArrival` で消費財市場／預金市場のマッチングをコミット。
  - `TIC_UPDATEEXPECTATIONS` で雇用・消費ラグ更新。
- **SFC**: 消費コミット時に家計預金が減り企業預金が増える。税支払いでは預金から政府準備へ移転。雇用時の賃金支払いも預金→預金の資産／負債を同額更新し、実物市場での在庫減少と売上増加が一致。

---

各クラスは `StockMatrix` による資産負債の二重登録と `MacroTicEvent` によるイベント順序で、論文のトランザクション・フローマトリクス／フルインテグレーションマトリクスの会計整合性をプログラム上で再現している。数式のパラメータ変更やシナリオ変更は XML 設定ファイル（`benchmark/benchmark/Model/*.xml`）で行い、ここで紹介した `TIC_*`・`SM_*` ID を参照している。
