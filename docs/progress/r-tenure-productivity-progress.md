# R勤続年数による生産性補正 進捗メモ

## 完了日
2025-12-23

## 概要
正規雇用（R）の勤続年数に応じて、生産性を企業内平均で上乗せする仕組みを追加しました。勤続年数は雇用継続で増加し、失業・転職でリセットされます。

## 完了済み

### 1. 勤続年数フィールドの追加 ✓
**ファイル**:
- `jmab/src/main/java/jmab/agents/LaborSupplier.java`
- `jmab/src/main/java/jmab/agents/AbstractHousehold.java`

**実装内容**:
- `getTenure()` を追加
- 勤続年数（tenure）を保持し、シリアライズに含める

### 2. 勤続年数の更新 ✓
**ファイル**: `benchmark/benchmark/src/main/java/benchmark/agents/Households.java`

**実装内容**:
- Rのみ、雇用継続で tenure++
- 失業・雇用先変更で tenure をリセット

### 3. 生産性補正の導入 ✓
**ファイル**: `jmab/src/main/java/jmab/agents/AbstractFirm.java`

**実装内容**:
- 企業内のR平均勤続年数に基づく補正係数を追加
- 有効労働のR側に倍率を適用
- 補正式: 1 + (a-1) * (1 - tau^{-gamma})

### 4. パラメータ設定 ✓
**ファイル**:
- `benchmark/benchmark/Model/parameters.xml`
- `benchmark/benchmark/Model/modelBenchmark_full.xml`
- `benchmark/benchmark/Model/modelBenchmark_light.xml`

**実装内容**:
- `regularTenureProductivityA=1.20`
- `regularTenureProductivityGamma=0.50`
- 企業プロトタイプにパラメータ注入

## 次の確認ポイント
- runFullで `avWageR/N` と `unemploymentR/N` の推移がどう変わるか
- R/N生産性差の過大化がないか（GDPの急変や不安定化の確認）
