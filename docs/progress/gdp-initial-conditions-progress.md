# GDP初期条件の整合化 進捗メモ

## 実施内容
- ConsumptionFirm の初期資本ストックで age=0 を age=1 にずらし、初期在庫が「当期投資」としてGDPに計上されるのを回避
  - 対象: `benchmark/benchmark/src/main/java/benchmark/init/SFCSSMacroAgentInitialiser.java`

## 目的
- 期初の資本ストックを「既存資本」として扱い、1期目のGDP過大計上を抑制する

## 補足
- `kMat <= 1` の場合は age=0 を維持し、初期資本の即時消滅を防止

## 未対応
- runFull 再実行による `nominalGDP1.csv` の再確認

