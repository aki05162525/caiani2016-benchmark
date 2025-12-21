# Phase C7 実装進捗メモ

## 完了日
2025-12-21

## 概要
賃金更新のtype別化に対応し、R/N別の失業率参照と賃金パラメータ分岐を導入しました。legacy設定が未指定でも動くようfallbackも維持しています。

## 完了済み

### 1. StaticValues.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/StaticValues.java`

**実装内容**:
- `LAG_AGGUNEMPLOYMENT_R`, `LAG_AGGUNEMPLOYMENT_N` を追加

### 2. UnemploymentRateByTypeComputer.java ✓
**ファイル**: `jmab/src/jmab/report/UnemploymentRateByTypeComputer.java`

**実装内容**:
- laborType を指定して失業率を計算する新しいMacroVariableComputerを追加

### 3. Government.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/agents/Government.java`

**実装内容**:
- `uComputerR/N` を追加
- 集計値を `LAG_AGGUNEMPLOYMENT_R/N` に格納

### 4. Households.java ✓
**ファイル**: `benchmark/benchmark/src/benchmark/agents/Households.java`

**実装内容**:
- `getMacroReferenceVariableForWage()` を type別失業率に分岐
- fallbackで `LAG_AGGUNEMPLOYMENT` を使用

### 5. AdaptiveWageStrategy.java ✓
**ファイル**: `jmab/src/jmab/strategies/AdaptiveWageStrategy.java`

**実装内容**:
- type別パラメータ（macroThresholdR/N, microAdaptiveParameterR/N, macroAdaptiveParameterR/N）を追加
- legacyパラメータが未指定なら従来値を使用
- シリアライズは後方互換のまま拡張

### 6. XML設定 ✓
**ファイル**: `benchmark/benchmark/Model/modelBenchmark_full.xml`, `benchmark/benchmark/Model/modelBenchmark_light.xml`

**実装内容**:
- `pastUnemploymentR/N` の passedValues を追加
- `unemploymentComputerR/N` を定義
- `householdsWageStrategy` に type別パラメータを追加
- `Government` に `uComputerR/N` を注入

### 7. 初期化 ✓
**ファイル**: `benchmark/benchmark/src/benchmark/init/SFCSSMacroAgentInitialiser.java`

**実装内容**:
- `LAG_AGGUNEMPLOYMENT_R/N` の初期値を設定

## Phase C7 完了！✓

完了項目:
1. ✓ type別失業率ラグの追加
2. ✓ type別失業率の計算と注入
3. ✓ wage戦略のtype別パラメータ対応
4. ✓ XML設定の追加

## 次のフェーズ候補
- Phase C8: 失業給付の加重平均賃金化
