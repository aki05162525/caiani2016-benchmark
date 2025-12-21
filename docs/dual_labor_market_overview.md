# 二重労働市場拡張の概要

本ノートは、正規(R)/非正規(N)の二重労働市場がモデル挙動をどう変えるかを要約する。

## 基本構造

- 家計は初期化時に固定の労働タイプを持つ:
  - laborType in {R, N}
- 各タイプは自分の市場にのみ参加:
  - R: `MKT_LABOR_R`
  - N: `MKT_LABOR_N`

## 実効労働と需要分解（CES）

タイプ別雇用 N_R, N_N の実効労働は次式:

N_eff = [ delta * (A_R * N_R)^rho + (1 - delta) * (A_N * N_N)^rho ]^(1/rho)

総労働需要 N^D を比率で分解する:

ratio_raw = (delta / (1 - delta))^(1/(1 - rho)) * (A_R / A_N)^(rho/(1 - rho))
            * (W_N^e / W_R^e)^(1/(1 - rho))

ratio = clip(ratio_raw, phi_min, phi_max)

ここから:

Denom = [ delta * (A_R * ratio)^rho + (1 - delta) * (A_N)^rho ]^(1/rho)
N_N^D = N^D / Denom
N_R^D = ratio * N_N^D

## 採用と解雇（タイプ別）

- 期ごとのタイプ別労働需要:
  - laborDemandR = max(0, N_R^D - N_R)
  - laborDemandN = max(0, N_N^D - N_N)
- 需要がある市場だけをアクティブ化する。
- 採用は laborDemandR/N を上限とし、過剰雇用を防ぐ。
- 部分解雇はタイプ別率を使用:
  - fire_R = probabilisticRound(eta_R * excess_R)
  - fire_N = probabilisticRound(eta_N * excess_N)

## 期待賃金と賃金更新

- 期待賃金は2系列:
  - W_R^e, W_N^e
- 家計の賃金更新はタイプ別失業率を参照:
  - u_R または u_N

## 政府部門

- 政府は R 労働者のみを雇用（R市場に固定需要）。
- 失業給付は加重平均賃金を使用:
  - bar_w = (W_R * N_R + W_N * N_N) / (N_R + N_N)
  - dole = omega * bar_w

## 観測される系列

- 失業率: u_R, u_N
- 平均予約賃金: avWageR, avWageN
- 雇用者数・求人: E_R/E_N, Vac_R/Vac_N

CSV出力の一覧は `docs/csv_outputs_dual_labor_market.md` を参照。
