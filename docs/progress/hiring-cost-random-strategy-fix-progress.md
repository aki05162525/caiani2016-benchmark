# 採用コスト率の適用範囲修正 進捗メモ

## 完了日
2025-12-23

## 概要
政府の採用戦略が SelectRandomWorkerStrategy のため、hiringCostRateR/N を注入すると起動時にプロパティエラーとなる問題を解消しました。

## 完了済み

### 1. SelectRandomWorkerStrategy から不要プロパティを削除 ✓
**対象ファイル**:
- `benchmark/benchmark/Model/modelBenchmark_full.xml`
- `benchmark/benchmark/Model/modelBenchmark_light.xml`
- `benchmark/benchmark/Model/modelSerialization.xml`
- `benchmark/benchmark/Model/ExperimentsExpectations/modelTrendFollowingAndAdaptiveExp.xml`
- `benchmark/benchmark/Model/ExperimentsExpectations/modelAnchoringAndAdaptiveExp.xml`
- `benchmark/benchmark/Model/ExperimentsExpectations/modelSimpleAdaptive.xml`
- `benchmark/benchmark/Model/ExperimentsExpectations/modelTrendFollowingExp.xml`
- `benchmark/benchmark/Model/ExperimentsExpectations/modelAnchoringAdjustmentExp.xml`

**実装内容**:
- SelectRandomWorkerStrategy を使う governmentLaborStrategy から `hiringCostRateR/N` を削除

## 次の確認ポイント
- runFull を再実行して起動エラーが解消されるか確認
