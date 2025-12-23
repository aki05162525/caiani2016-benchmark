# CES delta 再調整 進捗メモ

## 完了日
2025-12-23

## 概要
R/Nの失業率が極端化したため、CESのR比重を0.55に再調整しました。

## 完了済み

### 1. cesDelta の再調整 ✓
**ファイル**:
- `benchmark/benchmark/Model/parameters.xml`
- `benchmark/benchmark/Model/modelBenchmark_light.xml`

**実装内容**:
- `cesDelta` を 0.60 → 0.55 に変更

## 次の確認ポイント
- runFullで `unemploymentR/N` の極端さが緩和されるか確認
