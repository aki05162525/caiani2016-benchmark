# Caiani(2016) ベンチマークモデル：二重労働市場（R/N）拡張 実装タスク（Task1反映・再構築版）

## 0. 目的・スコープ

* 目的：Caiani(2016) SFC-ABM の構造を極力維持しつつ、**労働市場のみ**を二重化（正規 R / 非正規 N）する。
* スコープ内（差分の中心）

  * 労働市場の分離：R/Nで**市場・失業プール・マッチング**を分離
  * 企業の労働需要：`N^D → (N^{D,R}, N^{D,N})`（CES 分解）
  * 雇用調整：turnover / layoff / vacancy / hire をタイプ別化
  * 賃金：要求賃金更新のタイプ別化、期待賃金を2系列化
  * 失業給付（dole）：平均賃金を加重で再定義
  * 生産・価格：CES 実効労働と2賃金費用に整合
* スコープ外（維持）

  * 財市場配分、投資、信用供給、デフォルト、政府・中銀、銀行会計、SFC恒等式（労働費用項の差替えを除く）

---

## 1. 前提チェック（完了）

* [x] 労働関連の実装箇所を特定し、変更対象を列挙
  → `docs/labor_implementation_mapping.md` を参照

---

# Phase A: 「混線しない」土台（市場・家計・再求人の型）を作る

## A1. StaticValues / MarketID の二重化（最優先）

**目的**：R/N の混線を構造的に防ぐ（同じ `MKT_LABOR` に出さない）

* [x] `StaticValues` に市場IDを追加

  * `MKT_LABOR_R`
  * `MKT_LABOR_N`
* [ ] 既存コードで `StaticValues.MKT_LABOR` を参照している箇所を洗い出し、段階的に置換できるようにする

  * まずは「出す側（労働供給）」と「呼ぶ側（企業）」の最小経路を置換する

**Done**：R/N の労働市場が「IDレベルで分離」され、以後の実装で混線が起きない前提が成立。

---

## A2. 家計へ labor_type（固定属性）を導入

**対象**：`benchmark/.../Households.java`

* [x] `labor_type ∈ {R,N}` を Households に追加（初期割当・以後固定）
* [ ] タイプ別労働力人口 `LF_R`, `LF_N` を算出できるようにする（集計 or Government側）
* [x] 労働市場への参加フラグ/参加市場の決定を `labor_type` に依存させる

**Done**：Households が「常に自分のタイプ市場にのみ参加」することがコードで保証される。

---

## A3. 労働供給（市場参加）をR/Nに分岐させる

**目的**：失業者プールが市場IDで分割されるようにする（マッチング前提）

* [x] `setLaborActive(true)` で労働市場に出る導線を追い、
  **Rなら `MKT_LABOR_R`、Nなら `MKT_LABOR_N`** に出すようにする
* [x] `TIC_LABORSUPPLY (11)` 周辺（賃金設定/市場参加）の流れを確認し、二重化に破綻がないか確認

**Done**：失業者が市場に出る時点で R/N が分離される（市場側の候補者リストが分かれる）。

---

## A4. AbstractFirm の「採用」「支払不能→解雇→再求人」をタイプ対応（最重要）

**対象**：`jmab/src/jmab/agents/AbstractFirm.java`

### A4.1 laborDemand の二重化

* [x] `laborDemand` を `laborDemandR`, `laborDemandN` に置換（または map/struct で保持）
* [ ] 既存 `setActive(true, StaticValues.MKT_LABOR)` の設計を見直し：

  * Rの求人があるなら `MKT_LABOR_R`
  * Nの求人があるなら `MKT_LABOR_N`
  * 両方あるなら両市場に active になる（市場ごとに commit が走る想定）

### A4.2 addEmployee のタイプ認識

* [x] `addEmployee(LaborSupplier worker)` で worker の `labor_type` を取得し、対応する `laborDemand{R/N}` を減らす
* [ ] employees 管理方針を決める

  * **推奨（最小改変）**：employeesは1本のまま、必要箇所で type フィルタ
  * 代替案：employeesR / employeesN に分割（改修範囲が増える）

### A4.3 payWages の支払不能時挙動をタイプ別化

**Task1で判明した最重要副作用**：支払不能 → 解雇 → `laborDemand += 1` で即再求人

* [x] 支払不能時に「解雇した worker の type」に応じて

  * `laborDemandR += 1` もしくは `laborDemandN += 1`
  * 企業の市場参加も同じタイプ市場へ再参加させる
* [ ] `fireAgent()` は既存の雇用関係解消でOK（ただし worker 側が市場参加する際に type 市場へ出ることが前提）

**Done**：支払不能イベントが起きても R/N の求人・再求人・失業プールが混線しない。

---

# Phase B: 企業・政府の「雇用調整」経路（computeLaborDemand + payWages）をタイプ別に通す ✓

**Phase B2.3 完全完了（2025-12-21）**：全7クラス（ConsumptionFirm, CapitalFirm, ConsumptionFirmWagesEnd, CapitalFirmWagesEnd, Government, Government2WagesEnd, GovernmentAntiCyclical）にtype-specific turnover & partial layoff実装、XML設定完了

## B1. 期待賃金（Expectations）を2系列化する設計を確定 ✓

**Phase B1 完全完了（2025-12-21）**：期待賃金2系列化（EXPECTATIONS_WAGES_R/N）実装、XML設定・初期化・更新処理完了

**理由**：CES 分解（ratio式）・価格式は `W^e_R, W^e_N` を参照するが、現状は `EXPECTATIONS_WAGES` が単一。

* [x] `StaticValues.EXPECTATIONS_WAGES_R=5`, `EXPECTATIONS_WAGES_N=6` を追加（既存の0-4は維持）
* [x] 企業側で `W^e_R` と `W^e_N` を取得できる経路を作る
  * [x] XML設定: expWagesR/N bean定義とexpectationsマップ登録
  * [x] SFCSSMacroAgentInitialiser: CapitalFirm/ConsumptionFirmの初期化
  * [x] ConsumptionFirm/CapitalFirm の updatePreTaxProfits() でタイプ別平均賃金更新
  * [x] WagesEnd系: 親クラスの実装が適用されることを確認
  * [x] 既存 EXPECTATIONS_WAGES も並行維持（後方互換性）

**Done**：企業が `W^e_R, W^e_N` を安定して参照できる。Phase Cへの準備完了。

---

## B2. computeLaborDemand() + payWages のタイプ別化（7クラス全対応）

**対象（Task1で特定済み）**

* 企業: ConsumptionFirm / CapitalFirm / ConsumptionFirmWagesEnd / CapitalFirmWagesEnd
* 政府: Government / GovernmentAntiCyclical / Government2WagesEnd

**共通タスク（各クラスに対して）**

* [x] turnover（分離）をタイプ別に
  * `turnoverLabor` → `turnoverLaborR`, `turnoverLaborN`（= ϑ_R, ϑ_N）
* [x] layoff（解雇）をタイプ別に
  * 現状の「全員解雇」から `Fire = η * excess` の部分解雇に差し替え（詳細はB2.3）
* [x] vacancy（求人）をタイプ別に
  * `laborDemand` → `laborDemandR/N`
  * active market も `MKT_LABOR_R/N`
* [x] 賃金支払いが computeLaborDemand 内にあるクラスは、payWages 経路の整合を確認
  * WagesEnd系は独自payWagesを持つが、AbstractFirm.payWagesの再求人ロジックは既にタイプ別化済み

---

## B2.1 Government系の概要と方針決定

**背景**
Government系3クラスは企業と異なる特性を持つ：
* Government（標準版）：turnoverなし、fixedLaborDemand、payWagesオーバーライド（支払不能チェックなし）
* GovernmentAntiCyclical / Government2WagesEnd：turnoverあり、一般企業に近い

**方針タスク**

* [x] Government（標準版）の雇用をR/Nどちらに割り当てるか決定（推奨：**全員R固定**＝保守的）
* [x] GovernmentAntiCyclical / Government2WagesEndの求人・採用もR/N市場分離に整合させる

---

## B2.2 Government.java（標準政府版）の具体的実装

**背景（Task1より）**
Government.java は turnover なし・fixedLaborDemand・payWagesオーバーライド（支払不能チェックなし）で、一般企業と違う。

### B2.2.1 政府雇用のR/N割当方針を固定（推奨：政府雇用=全員R）

* [x] **方針**：「政府雇用は全て R 労働者を雇う」と定義する（Nは雇わない）
  * 目的：最小改変で混線を防ぎ、政府部門をモデル安定装置として扱う
  * 代替案（後回し）：`fixedLaborDemand` を R/N 比率で按分（例：share_R）して雇う

### B2.2.2 実装タスク（Government.computeLaborDemand / payWages）

* [x] `Government.computeLaborDemand()` で、求人を **MKT_LABOR_R のみ**に出す
  * `laborDemandR = max(0, fixedLaborDemand - currentWorkersR)`
  * `laborDemandN = 0`
  * `setActive(true, StaticValues.MKT_LABOR_R)` のみ呼ぶ
* [x] `Government.payWages()`（オーバーライド）で、賃金支払い対象を **R労働者に限定**する（=雇用自体がRのみなら自動的に満たされる）
* [x] `currentWorkersR` の定義を決める
  * employees を1本のままなら「type=Rだけカウント」
  * employeesR/Nに分けるなら employeesR.size()

**Done**：政府はR市場からのみ採用し、N市場に干渉しない（混線なし）。

---

## B2.3 η_R, η_N（部分解雇）仕様の確定

**背景**
現行は excess 全員解雇＝`currentWorkers - nbWorkers` を全て fire。ここを「硬直性（部分解雇）」に変えると、**(1)何人解雇するか (2)誰を解雇するか (3)丸め**が必須仕様になる。

### B2.3.1 何人解雇するか（式・丸め）

* [x] 各タイプ ℓ∈{R,N} で excess を定義：
  `excess_ℓ = max(0, tildeN_ℓ - N^{D,ℓ})`
* [x] 解雇人数：
  `fire_ℓ = probabilisticRound(η_ℓ * excess_ℓ)`（実装済み）
  * AbstractFirm.probabilisticRound() メソッドで確率丸めを実装
* [x] **推奨丸め仕様（再現性と偏り低減）**：確率丸め
  * `x = η_ℓ * excess_ℓ`
  * `fire = floor(x) + Bernoulli(frac(x))`（乱数は既存PRNGを使用）
  * これで期待値がちょうど x になり、長期のバイアスが減る

### B2.3.2 誰を解雇するか（選択規則）

* [x] 解雇対象の選択規則を固定する（推奨：ランダム）
  * **推奨**：employees(type=ℓ) を PRNG で shuffle → 先頭から fire_ℓ 人を解雇
  * 全クラスで AgentList.shuffle(prng) を使用してランダム解雇を実装済み
* [x] 現行コードが `emplPop.get(i)` を順に fire しているなら、これは実質「リスト順依存」なので、必ず明示的にシャッフルする

### B2.3.3 どこに実装するか（適用箇所）

* [x] 以下 **全ての computeLaborDemand()** で、layoff部分を上記仕様に差し替える（Task1で列挙した7クラス）
  * ConsumptionFirm
  * CapitalFirm
  * ConsumptionFirmWagesEnd
  * CapitalFirmWagesEnd
  * GovernmentAntiCyclical
  * Government2WagesEnd
  * Government（政府は固定需要・turnoverなしのためlayoff処理なし、R-only雇用のみ）

**Done**：η_R<η_N により、ショック時に「Rは解雇が遅く、Nが調整弁になる」挙動が実装として保証される。

---

## B2.4 WagesEnd系の payWages タイプ別化

**背景**
WagesEnd系は computeLaborDemand と賃金支払いが分離されており、B2の「computeLaborDemandを直す」だけでは賃金側が取り残される可能性がある。

### B2.4.1 賃金支払い箇所の所在を確定

* [x] ConsumptionFirmWagesEnd / CapitalFirmWagesEnd の「賃金支払いが実行されるメソッド」を特定する
  * 独自 payWages() を実装している（支払不能時にbankruptcy()を呼ぶ）
  * AbstractFirm.payWages() の再求人ロジック（支払不能→laborDemandR/N += 1）は使用していない

### B2.4.2 タスク化（分岐）

* [x] **Case B（独自 payWages 実装）**を確認
  * WagesEnd系の payWages() は支払不能時にbankruptcy()を呼び、そのまま終了
  * 再求人ロジックは含まれていないため、AbstractFirm側のtype別再求人は影響しない
  * computeLaborDemand()のtype別化により、通常の雇用調整は正しく機能

**Done**：WagesEnd系は独自の支払不能処理（bankruptcy）を持ち、通常フローはcomputeLaborDemand()でtype整合が保たれる。

---

## B3. マッチングの呼び出し経路（onAgentArrival / commit）をR/N市場で成立させる

**対象**：ConsumptionFirm の `onAgentArrival`（Task1: 行239-244）等

* [x] 企業が市場から渡される候補者リストは、市場IDにより既に分離されている前提にする
* [x] `SelectWorkerStrategy` は基本そのまま（候補者リストが同一タイプのみなら追加実装不要）
* [x] もし市場側が単一で分離できない場合は、strategy側で type フィルタを追加（ただし最終手段）

**Done**：企業がR市場でN workerを commit されることが構造的に起きない。

---

# Phase C: CES（需要分解・実効労働）と生産・価格・給付を整合させる

## C1. CES 実効労働関数 `N_eff(N_R, N_N)` を実装

* [x] `N_eff = [ δ (A_R N_R)^ρ + (1-δ)(A_N N_N)^ρ ]^(1/ρ)` を関数化
* [x] 数値安定

  * `N_R, N_N ≥ 0` を保証
  * `ε` によるゼロ割/負値ガード
  * `ρ` が 0 近傍のガード（仕様上は ρ≠0 だが防御コードは入れる）

**Done**：単体テストで非負性・単調性・極限ケースが確認できる。

---

## C2. 需要比率（FOC）とクリップを実装

* [x] `ratio_raw = (δ/(1-δ))^(1/(1-ρ)) * (A_R/A_N)^(ρ/(1-ρ)) * (W^e_N/W^e_R)^(1/(1-ρ))`
* [x] `ratio = clip(ratio_raw, φ_min, φ_max)`

**Done**：`W^e_R↑` で ratio↓ など、符号が直観に一致。

---

## C3. `(N^{D,R}, N^{D,N})` の閉形式計算（あなたの4.3を採用）

* [x] **前提**：ratio は clip 後の値を所与として配分（安定性優先）
* [x] `Denom = [ δ * (A_R * ratio)^ρ + (1-δ) * (A_N)^ρ ]^(1/ρ)`

  * `Denom = max(Denom, ε)`
* [x] `N^{D,N} = N^D / Denom`
* [x] `N^{D,R} = ratio * N^{D,N}`
* [x] `N^D < ε` は早期リターンで 0

**Done**：`N_eff(N^{D,R}, N^{D,N}) ≈ N^D`（相対誤差 < tol）を検算で満たす。

---

## C4. getRequiredWorkers() は「N^D（実効労働需要）」として維持し、分解は computeLaborDemand 内で差し込む

**対象**

* ConsumptionFirm.getRequiredWorkers()

* CapitalFirm.getRequiredWorkers()

* [x] 既存 `getRequiredWorkers()` は **N^D（実効労働需要）**として残す（戻り値は従来通り int でもよい）

* [x] computeLaborDemand() 内で `N^D → (N^{D,R}, N^{D,N})` を導入し、以後の調整・求人をタイプ別に行う

**Done**：既存の生産・価格・会計の前提を壊さずに「需要分解」を注入できる。

---

## C5. 生産（produce）を CES 実効労働に整合

**対象**：ConsumptionFirm.produce（Task1: 行446-511）／CapitalFirm 側も同様

* [x] `employees.size()` ベースの労働投入を `N_eff(N_R, N_N)` に差し替える

  * employees 1本運用の場合：`N_R, N_N` を employees から type でカウント
* [x] unit cost 計算等が `outputQty` 依存なので、ゼロ割回避（ε）を入れる

**Done**：R/N構成が生産能力に反映され、労働不足時に矛盾が出ない。

---

## C6. 価格（getPriceLowerBound / mark-up）を2賃金費用に整合

**対象**

* ConsumptionFirm.getPriceLowerBound（Task1: 行835-905）

* CapitalFirm.getPriceLowerBound（Task1: 行817-821）

* WagesEnd系も同様

* [ ] 期待変動費 `W^e * N^D` を

  * `W^e_R * N^{D,R} + W^e_N * N^{D,N}` に置換

* [ ] 期待賃金の更新（観測）は type 別平均賃金から2系列で更新する

**Done**：価格が「CES分解された需要」と「2系列期待賃金」に整合。

---

## C7. 賃金（要求賃金）更新を type 別化

**対象**

* AdaptiveWageStrategy.computeWage（Task1: 行42-54）

* Households.getMicroReferenceVariableForWage（Task1: 行397-407）

* macro threshold の参照変数

* [ ] `macroThreshold` を type 別に（υ_R, υ_N）

* [ ] 必要なら `microAdaptiveParameter/macroAdaptiveParameter` も type 別に（または倍率）

* [ ] Households が macro reference variable を返す箇所で `u_R` / `u_N` を参照できるようにする

**Done**：同一景気でも N の賃金上昇が抑制される等、二重性が設定で表現できる。

---

## C8. ドール（失業給付）を加重平均賃金に置換

**対象**：GovernmentAntiCyclical.payUnemploymentBenefits（Task1: 行111-139）

* [ ] type 別平均賃金 `W_R, W_N` と雇用者数 `N_R_total, N_N_total` を集計
* [ ] `bar_w = (W_R*N_R_total + W_N*N_N_total) / (N_R_total + N_N_total)`
* [ ] `dole = ω * bar_w` に置換（既存の unemploymentBenefit との関係はパラメータ設計で整理）

**Done**：労働市場構造（R/N比率）に応じて給付が自然に変動。

---

# 2. パラメータ（設定）タスク：導入・バリデーション

## P1. type 別パラメータを設定可能にする

* [x] ϑ_R, ϑ_N（turnoverLaborR/N in XML: 0.02, 0.05）
* [x] η_R, η_N（layoffRateR/N in XML: 0.3, 0.8、0 < η_R < η_N ≤ 1 を満たす。※現状は企業向けのみ適用。Government系にはsetterがないためXML注入は削除）
* [ ] χ_R, χ_N
* [ ] υ_R, υ_N
* [x] δ, ρ(≠0), A_R, A_N（CESパラメータをXMLで設定。企業向けのみ適用）
* [x] ε, φ_min, φ_max（CESパラメータ/クリップ範囲をXMLで設定。企業向けのみ適用）
* [ ] ω
* [ ] （任意）c1_R, c1_N, c2（需要側フィードバックを入れるなら）

## P2. 起動時バリデーション

* [ ] 範囲チェック（δ∈(0,1), A>0, φ_min>0, φ_min<φ_max, etc.）
* [ ] ρ の危険域（|ρ|小）警告 or 禁止

**Done**：設定だけで二重性の強弱を調整でき、危険設定は即座に検出される。

---

# 3. テスト計画（最低限）

## T1. 単体テスト

* [ ] `N_eff` 非負・単調・境界
* [ ] C3の分解：`N_eff(N^{D,R}, N^{D,N}) ≈ N^D`
* [ ] ratio clip が効く（極端賃金比でも破綻しない）

## T2. 統合テスト（1ステップ）

* [ ] 支払不能→解雇→再求人が type 市場に戻る（混線しない）
* [ ] R市場求人にN家計が採用されない（市場IDで担保）
* [ ] computeLaborDemand（7クラス）の一貫性（少なくとも主要2クラスで先に通す→残りへ展開）

## T3. 簡易挙動確認

* [ ] `η_R < η_N` でショック時にRが維持されやすい
* [ ] `υ_N < υ_R` で好況時もN賃金が上がりにくい

---

# 4. 実装順序（推奨）

1. **Phase A（市場ID・家計type・payWages再求人）**
2. **Phase B（7クラス computeLaborDemand のタイプ別化、期待賃金2系列の土台）**
3. **Phase C（CES分解→produce→price→wage strategy→dole）**
4. テスト・集計系列の整備

---

# 5. 受け入れ条件（Definition of Done）

* 既存ベンチマークが動作し、SFC整合が維持される
* 労働市場が R/N に分離され、主要系列（u_R, u_N, W_R, W_N, N_R_total, N_N_total）が出力できる
* 支払不能→再求人など「例外経路」でも混線が起きない
* `N_eff ≈ N^D` を満たす需要分解が安定に動く
* 価格・生産が二重賃金とCES労働に整合して決まる
* 最低限の単体テストが通る

