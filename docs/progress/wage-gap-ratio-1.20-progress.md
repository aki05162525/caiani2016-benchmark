# R/N賃金格差の緩和（比率1.20）進捗メモ

## 実施内容
- 初期賃金のR/N比率を 1.20 に緩和
  - 対象: `benchmark/benchmark/src/main/java/benchmark/init/SFCSSMacroAgentInitialiser.java`
- 賃金比率に合わせて CES 効率差を再正規化
  - `cesAR = 2.21`, `cesAN = 1.84`
  - 対象: `benchmark/benchmark/Model/parameters.xml`

## 目的
- N雇用への偏りを緩和し、N失業率が0%に張り付く問題を抑える

## 未対応
- runFull 再実行による `unemploymentN1.csv` の再確認

