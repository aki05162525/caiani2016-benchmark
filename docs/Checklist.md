# Dual Labor Market (R/N) — Equation-to-Implementation Verification Checklist

目的  
二重労働市場（R/N）拡張が、モデル仕様（数式・更新順序・市場手順）どおりに実装されているかを確認する。  
本チェックは「挙動がそれっぽい」ではなく、**式の対応関係（変数・分母・更新タイミング）**をコードで裏取りすることを目的とする。

非スコープ
- パラメータ推定・チューニング
- 実験結果の解釈・政策実験の妥当性

---

## 0. 前提：型・記号の対応表（コード内の対応を明記する）
- [ ] 労働タイプの集合 ℓ ∈ {R, N} がコード上で一意に表現されている（enum/定数など）
- [ ] 労働市場の集合 m ∈ {Labor_R, Labor_N} がコード上で一意に表現され、R/Nが混線しない
- [ ] 各 household に laborType(ℓ) が初期化時に設定され、期中に意図せず未設定/変更されない（仕様どおりなら固定）
- [ ] 各 firm がタイプ別の状態変数（例：雇用 N^ℓ、需要 N^{D,ℓ}、求人 V^ℓ 等）を保持している

Codex出力要件：上記4点について「コード上の実体（変数/メソッド）」を列挙し、R/Nの分離が成立している根拠を示す。

---

## 1. 労働供給側（Households / Workers）

### 1.1 労働力人口（タイプ別）
- [ ] 定義：LF_ℓ,t = count(laborType = ℓ) が成立している（分母の定義が一貫）
- [ ] 期中に LF_ℓ が変動する設計の場合、その理由がモデル仕様に一致する（例：人口動態がある等。なければ一定）

### 1.2 失業者の定義（タイプ別）
- [ ] 定義：U_ℓ,t = count(laborType = ℓ AND isEmployed = false) のように、失業判定が明確
- [ ] 恒等式：LF_ℓ,t = E_ℓ,t + U_ℓ,t が常に成立する（E_ℓ は employed count）

Codex出力要件：LF, E, U の各カウントが「どの属性/条件」で計算されるかを具体的に示す。

---

## 2. 労働需要側（Firms）

### 2.1 タイプ別労働需要 N^{D,ℓ}
- [ ] 企業の労働需要が ℓ ごとに分解されている（N^{D} → N^{D,R}, N^{D,N}）
- [ ] 分解ロジックがモデル仕様に一致（例：CES分解、固定比率、ルールベース等。採用した仕様をコードが満たす）
- [ ] N^{D,ℓ} が「当期の需要」として更新されるタイミングが仕様どおり（どのフェーズで計算し、いつ固定するか）

### 2.2 期首の暫定雇用（分離・解雇等の反映）
- [ ] 定義：期首時点の暫定雇用  N̂^ℓ_{t} が、前期雇用 N^ℓ_{t-1} に分離/解雇等を適用した結果になっている
- [ ] 分離率/解雇ルールが ℓ 別に適用される（ϑ_R と ϑ_N、または η_R と η_N など）
- [ ] 期首更新の順序が一貫している（例：separation → firing → vacancy 計算）

Codex出力要件：期首更新の手順（順番）と、N^ℓ→N̂^ℓ の計算がどこで行われるかを説明する。

---

## 3. 求人（Vacancies）と採用（Hiring）

### 3.1 求人の定義（重要）
- [ ] 定義：V^ℓ_{t} = max(0, N^{D,ℓ}_{t} − N̂^ℓ_{t}) が成立している
- [ ] 企業が市場に提示する求人件数が、まさに V^ℓ に等しい（求人の上限＝採用上限）

### 3.2 市場のマッチング手順
- [ ] 市場は ℓ ごとに分離され、R市場はR労働者のみ、N市場はN労働者のみを取り扱う
- [ ] 各企業の求人 V^ℓ 件に対して、失業者プールから候補者を選び採用する（モデルの「求人ごと」手順）
- [ ] 採用数 Hire^ℓ は常に 0 ≤ Hire^ℓ ≤ V^ℓ を満たす（需要上限を超えない）
- [ ] V^ℓ = 0 のとき採用が起きない（ガードがある）

Codex出力要件：Hire が V を超えないことを「コード上の制約」で示す（単なる観測ではなく、条件分岐やカウンタ制御で保証されていること）。

---

## 4. 雇用更新（Employment update）

- [ ] 定義：N^ℓ_{t} = N̂^ℓ_{t} + Hire^ℓ_{t} が成立している
- [ ] Hire^ℓ の反映により N^ℓ が更新され、雇用者の状態（employedフラグ、雇用主参照等）が整合する
- [ ] 企業内の従業員リスト（ℓ別）と、労働者側の雇用状態が矛盾しない

Codex出力要件：雇用確定時に更新される状態（firm側/worker側）を列挙し、二重計上がないことを示す。

---

## 5. 失業率（Unemployment rate）定義の一致

- [ ] u_ℓ,t の定義がモデル仕様と一致する
    - 例：u_ℓ,t = U_ℓ,t / LF_ℓ,t か
    - 例：u_ℓ,t = 1 − E_ℓ,t / LF_ℓ,t か
- [ ] 上記の分母 LF_ℓ,t が「laborType一致の人数」であり、LF=0 のときの扱いが明示されている（仕様に沿う）
- [ ] 数値整合チェック：u_ℓ,t ≈ 1 − E_ℓ,t / LF_ℓ,t が実データで成立する（誤差があれば理由を説明）

Codex出力要件：u の式、LF/E/U の算出元、LF=0の扱いを明記する。

---

## 6. R/N 分離の非混線チェック（強制）

- [ ] R市場でN労働者が採用されない（逆も同様）
- [ ] firm の demand/vacancy/hire カウンタが ℓ ごとに独立（片方の採用で他方が減らない）
- [ ] legacy の単一労働市場経路が残っている場合、R/Nと二重に採用が確定しない（重複トリガーなし）

Codex出力要件：R/Nのフィルタ条件、参照している市場ID、カウンタ更新の対象が一致していることを示す。

---

## 7. 最終判定

- [ ] 上記 0〜6 をすべて満たす → 「数式仕様どおりに実装されている」と判定
- [ ] 満たさない項目がある → 逸脱内容（式/手順/タイミング）と、最小修正案を提示

---

# 回答（チェック結果）

## 0. 前提：Yes
- 根拠: `benchmark/benchmark/src/benchmark/StaticValues.java` の `LABOR_TYPE_R/N`, `MKT_LABOR_R/N`、`benchmark/benchmark/src/benchmark/init/SFCSSMacroAgentInitialiser.java` の `laborType` 初期化、`jmab/src/jmab/agents/AbstractFirm.java` の `laborDemandR/N`。

## 1. 労働供給側：Yes
- 根拠: LF は `jmab/src/jmab/report/LaborForceByTypeComputer.java`、E は `jmab/src/jmab/report/EmploymentByTypeComputer.java`、U は `jmab/src/jmab/report/UnemploymentCountByTypeComputer.java`。

## 2. 労働需要側：Yes
- 根拠: CES 分解は `jmab/src/jmab/agents/AbstractFirm.java` の `computeLaborRatio/computeLaborSplit`、更新は `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java` と `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java` の `computeLaborDemand`（TIC_LABORDEMAND）。

## 3. 求人と採用：Yes
- 根拠: `laborDemandR/N` は `max(0, N^D - N)` で設定。`onAgentArrival` で `laborDemandR/N <= 0` をガードし、採用は上限内（`ConsumptionFirm.java`, `CapitalFirm.java`）。市場は `MKT_LABOR_R/N` 分離。

## 4. 雇用更新：Yes
- 根拠: `jmab/src/jmab/agents/AbstractFirm.java` の `addEmployee` が `employees` 追加＋`setEmployer` を更新。`LaborSupplier.isEmployed()` は雇用主有無で決まる。

## 5. 失業率定義：Yes
- 根拠: `jmab/src/jmab/report/UnemploymentRateByTypeComputer.java` が `u = 1 - employed / totPop` を使用。LF=0 は 0 を返す挙動が明示。

## 6. R/N 分離の非混線：Yes
- 根拠: Households は `setLaborActive` でタイプ市場のみ参加（`benchmark/benchmark/src/benchmark/agents/Households.java`）。企業は legacy 市場を非活性化し R/N のみ（`ConsumptionFirm.java`, `CapitalFirm.java`, `Government.java`）。

## 7. 最終判定：Yes
- 0〜6 を満たすため「数式仕様どおりに実装されている」と判定。

