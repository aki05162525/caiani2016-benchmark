# CES効率パラメータ1.2倍 進捗メモ

## タスク
- [x] `benchmark/benchmark/Model/parameters.xml` の `cesAR`/`cesAN` を1.2倍スケールに更新
- [x] `benchmark/benchmark/Model/PARAMETERS.md` のデフォルト値と説明を更新

## 実施内容
- 比率 `A_R/A_N=1.46` を維持したまま、絶対水準を1.2倍
  - `cesAN = 1.2`
  - `cesAR = 1.752`

## 目的
- 生産規模の過度な縮小を避け、GDPの収束性を保ちながら水準を引き上げる

## 未対応
- IntelliJ実行での `nominalGDP1.csv` 推移確認
