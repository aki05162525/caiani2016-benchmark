# R/N採用コスト差別化 進捗メモ

## 完了日
2025-12-23

## 概要
R/Nの採用コストを明示的に差別化し、採用時の評価賃金に上乗せする方式で反映しました。設定はparameters.xmlから変更可能です。

## 完了済み

### 1. 採用選択ロジックの拡張 ✓
**ファイル**: `jmab/src/main/java/jmab/strategies/SelectCheapestWorkerStrategy.java`

**実装内容**:
- hiringCostRateR/N を追加
- 有効賃金 = 賃金 * (1 + 採用コスト率) で最安労働者を選択

### 2. パラメータ追加 ✓
**ファイル**: `benchmark/benchmark/Model/parameters.xml`

**実装内容**:
- `hiringCostRateR`（デフォルト 0.10）を追加
- `hiringCostRateN`（デフォルト 0.00）を追加

### 3. モデル配線 ✓
**ファイル**:
- `benchmark/benchmark/Model/modelBenchmark_full.xml`
- `benchmark/benchmark/Model/modelBenchmark_light.xml`
- `benchmark/benchmark/Model/modelSerialization.xml`
- `benchmark/benchmark/Model/ExperimentsExpectations/modelTrendFollowingAndAdaptiveExp.xml`
- `benchmark/benchmark/Model/ExperimentsExpectations/modelAnchoringAndAdaptiveExp.xml`
- `benchmark/benchmark/Model/ExperimentsExpectations/modelSimpleAdaptive.xml`
- `benchmark/benchmark/Model/ExperimentsExpectations/modelTrendFollowingExp.xml`
- `benchmark/benchmark/Model/ExperimentsExpectations/modelAnchoringAdjustmentExp.xml`

**実装内容**:
- SelectCheapestWorkerStrategy に `hiringCostRateR/N` を注入

## 次の確認ポイント
- R/Nの採用コスト率のキャリブレーション（失業率と賃金格差のバランス調整）
- runFullで unemploymentR/N の変化を確認
