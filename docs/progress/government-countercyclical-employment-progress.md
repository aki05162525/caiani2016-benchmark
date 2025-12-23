# 政府の反循環雇用ロジック追加 進捗メモ

## 実施内容
- 失業率に応じて政府雇用を調整するロジックを追加
  - `unemploymentTarget`, `laborAdjustmentSpeed`, `maxLaborAdjustment` を導入
  - 失業率が目標を上回ると政府雇用を増やす
  - 上限は労働力（R）と最大調整幅で制約
  - 対象: `benchmark/benchmark/src/main/java/benchmark/agents/GovernmentAntiCyclical.java`
- 設定値をモデルXMLに追加
  - `benchmark/benchmark/Model/modelBenchmark_full.xml`
  - `benchmark/benchmark/Model/modelBenchmark_light.xml`
  - `benchmark/benchmark/Model/modelSerialization.xml`

## 目的
- 失業率の急上昇を抑制する反循環的な公共雇用メカニズムを復活させる

## 未対応
- runFull 再実行による `unemployment1.csv` の再確認

