# CES効率パラメータ正規化 進捗メモ

## 実施内容
- CES効率パラメータを基準労働構成（laborTypeRatioR=0.65）で L_eff ≈ L_total となるよう正規化
  - `cesAR = cesAN = 2.05`
  - 対象: `benchmark/benchmark/Model/parameters.xml`

## 目的
- 二重労働市場導入後の有効労働スケール低下を補正し、元モデルと同程度の生産規模を維持する

## 未対応
- runFull 再実行による `nominalGDP1.csv` / 失業率系列の再確認

