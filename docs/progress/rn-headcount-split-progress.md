# R/N雇用分解の人数ベース化 進捗メモ

## 実施内容
- CESの最適比率（nR/nN）を人数配分に変換する方式へ変更
  - `nR = nbWorkers * ratio / (1 + ratio)` を使用
- 対象クラス
  - `benchmark/benchmark/src/main/java/benchmark/agents/CapitalFirm.java`
  - `benchmark/benchmark/src/main/java/benchmark/agents/ConsumptionFirm.java`
  - `benchmark/benchmark/src/main/java/benchmark/agents/CapitalFirmWagesEnd.java`
  - `benchmark/benchmark/src/main/java/benchmark/agents/ConsumptionFirmWagesEnd.java`

## 目的
- CES有効労働量ベースの分解でNがほぼゼロになる不整合を解消し、N型雇用を発生させる

## 未対応
- runFull 再実行による `unemploymentN1.csv` / `unemployment1.csv` の再確認

