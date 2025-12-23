# CES delta 調整 進捗メモ

## 完了日
2025-12-23

## 概要
N失業率が高い状態を緩和するため、CESのR比重を引き上げました。

## 完了済み

### 1. cesDelta の更新 ✓
**ファイル**:
- `benchmark/benchmark/Model/parameters.xml`
- `benchmark/benchmark/Model/modelBenchmark_light.xml`

**実装内容**:
- `cesDelta` を 0.50 → 0.60 に変更

## 次の確認ポイント
- runFullで `unemploymentR/N` が日本想定レンジに近づくか確認
