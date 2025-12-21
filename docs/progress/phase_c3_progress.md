# Phase C3 実装進捗メモ

## 完了日
2025-12-21

## 概要
CES閉形式分解を導入し、総需要 `N^D` を `N^{D,R}` / `N^{D,N}` に分解するロジックを実装しました。従来の固定比率（0.65）は撤去し、C2で導入したratioとC1のCESパラメータを用います。

## 完了済み

### 1. AbstractFirm.java ✓
**ファイル**: `jmab/src/jmab/agents/AbstractFirm.java`

**実装内容**:
- `computeLaborSplit(nTotal, ratio)` を追加
  - `Denom = [ δ (A_R * ratio)^ρ + (1-δ)(A_N)^ρ ]^(1/ρ)`
  - `N^{D,N} = N^D / Denom`
  - `N^{D,R} = ratio * N^{D,N}`
  - `epsilon` によるゼロ割ガード

### 2. 企業の computeLaborDemand 置換 ✓
**対象ファイル**:
- `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java`
- `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java`
- `benchmark/benchmark/src/benchmark/agents/ConsumptionFirmWagesEnd.java`
- `benchmark/benchmark/src/benchmark/agents/CapitalFirmWagesEnd.java`

**実装内容**:
- `ratioR=0.65` の固定分解を削除
- `computeLaborRatio()` + `computeLaborSplit()` に置換
- 期待賃金は `EXPECTATIONS_WAGES_R/N` を優先し、fallbackで legacy を使用
- 分解後は `laborDemandR/N` の既存ロジックを維持

## 実装パターン

### 閉形式分解（抜粋）
```java
double ratio = computeLaborRatio(expWageR, expWageN);
double[] split = computeLaborSplit(nbWorkers, ratio);
int nbWorkersR = Math.min(nbWorkers, Math.max(0, (int) Math.round(split[0])));
int nbWorkersN = Math.max(0, nbWorkers - nbWorkersR);
```

## Phase C3 完了！✓

完了項目:
1. ✓ 分解ヘルパー追加
2. ✓ 企業の分解ロジックを閉形式に置換
3. ✓ 期待賃金入力を統一

## 次のフェーズ候補
- Phase C5: 生産（produce）をCES実効労働に整合
- Phase C6: 価格（getPriceLowerBound）を2賃金費用に整合
