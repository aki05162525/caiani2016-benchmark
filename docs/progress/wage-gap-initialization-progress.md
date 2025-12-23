# R/N賃金格差の内生化 進捗メモ

## 実施内容
- R/Nで賃金調整の閾値を分離
  - `macroThresholdR = 0.10`, `macroThresholdN = 0.05`
  - 対象: `benchmark/benchmark/Model/parameters.xml`
- 初期賃金をR/N別に設定し、平均賃金を維持しつつ格差比を導入
  - 比率: R/N = 1.36
  - 平均賃金が `hhWage` になるよう正規化
  - 対象: `benchmark/benchmark/src/main/java/benchmark/init/SFCSSMacroAgentInitialiser.java`
- 企業のR/N賃金期待値も初期賃金に合わせて初期化

## 目的
- R/N賃金格差を内生的に発生させ、分配分析の妥当性を高める

## 未対応
- runFull 再実行による `unemploymentR1.csv` / `unemploymentN1.csv` の再確認

