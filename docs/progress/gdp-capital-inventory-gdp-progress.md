# GDP算出の資本財在庫整合性 修正メモ

## 実施内容
- GDP算出で資本財在庫を当期加算対象に含めるため、`gdpGoodsAges` の資本財の閾値を 1 に変更
  - 対象: `benchmark/benchmark/Model/reports.xml`

## 目的
- 当期在庫を加算しないのに前期在庫を控除する不整合を解消し、GDPの初期急落を緩和する

## 未対応
- runFull の再実行による `nominalGDP1.csv` の再確認

