# Phase C2 実装進捗メモ

## 完了日
2025-12-21

## 概要
CES需要分解の比率（FOC）を計算するヘルパーを追加し、クリップ範囲（phiMin/phiMax）をXMLで設定できるようにしました。Phase C2は計算ロジック追加のみで、既存挙動は未変更です。

## 完了済み

### 1. AbstractFirm.java ✓
**ファイル**: `jmab/src/jmab/agents/AbstractFirm.java`

**実装内容**:
- `phiMin`, `phiMax` を追加（getter/setter含む）
- `computeLaborRatio(expWageR, expWageN)` を実装
  - `ratio_raw` をCES式で計算
  - `phiMin/phiMax` でクリップ
  - `expWageR/expWageN` に `cesEpsilon` でガード
  - パラメータ範囲チェック（delta, rho, A, epsilon, phi）

### 2. XML設定ファイル ✓
**ファイル**: `benchmark/benchmark/Model/modelBenchmark_full.xml`, `benchmark/benchmark/Model/modelBenchmark_light.xml`

**実装内容**:
- ConsumptionFirm/CapitalFirm プロトタイプに以下を追加
  - `phiMin = 0.2`
  - `phiMax = 5.0`
- 既存のCESパラメータ（C1）と同じブロックで整理

## 実装パターン

### 比率計算とクリップ
```java
double invOneMinusRho = 1.0 / (1.0 - cesRho);
double termDelta = Math.pow(cesDelta / (1.0 - cesDelta), invOneMinusRho);
double termA = Math.pow(cesAR / cesAN, cesRho * invOneMinusRho);
double termW = Math.pow(adjWN / adjWR, invOneMinusRho);
double ratioRaw = termDelta * termA * termW;
return clamp(ratioRaw, phiMin, phiMax);
```

## Phase C2 完了！✓

完了項目:
1. ✓ phiMin/phiMax の追加
2. ✓ ratio計算ヘルパーの実装
3. ✓ XMLで設定可能に変更

## 次のフェーズ候補
- Phase C3: 閉形式分解（N^D → N^{D,R}, N^{D,N}）
- Phase C5/C6: 生産・価格への統合
