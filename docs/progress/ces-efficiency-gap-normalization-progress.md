# CES効率格差の正規化 進捗メモ

## 実施内容
- 賃金格差比（R/N=1.36）に合わせて CES 効率差を導入
- 基準構成（laborTypeRatioR=0.65, cesDelta=0.5, cesRho=0.5）で L_eff ≈ L_total となるよう再正規化
  - `cesAR = 2.32`
  - `cesAN = 1.70`
  - 対象: `benchmark/benchmark/Model/parameters.xml`

## 目的
- 正規の生産性が高いという仮定を反映しつつ、元の労働スケールを維持する

## 未対応
- runFull 再実行による `nominalGDP1.csv` / 失業率系列の再確認

