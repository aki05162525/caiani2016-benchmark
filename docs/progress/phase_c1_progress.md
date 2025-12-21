# Phase C1 実装進捗メモ

## 完了日
2025-12-21

## 概要
CES実効労働関数 `N_eff` を共通関数として追加し、数値安定ガードとパラメータ設定（XML）を整備しました。Phase C1は「基盤追加のみ」で、既存挙動は変更していません。

## 完了済み

### 1. AbstractFirm.java ✓
**ファイル**: `jmab/src/jmab/agents/AbstractFirm.java`

**実装内容**:
- CESパラメータの追加: `cesDelta`, `cesRho`, `cesAR`, `cesAN`, `cesEpsilon`
- getter/setterを追加
- `computeEffectiveLabor(nR, nN)` を実装
  - `delta` の範囲チェック（0 < δ < 1）
  - `rho` のゼロ近傍チェック
  - 係数・εの正値チェック
  - `N_R, N_N` を0以上にクリップ
  - ゼロ割・負値を `epsilon` でガード

### 2. XML設定ファイル ✓
**ファイル**: `benchmark/benchmark/Model/modelBenchmark_full.xml`, `benchmark/benchmark/Model/modelBenchmark_light.xml`

**実装内容**:
- ConsumptionFirm/CapitalFirm プロトタイプにCESパラメータを追加
  - `cesDelta = 0.5`
  - `cesRho = 0.5`
  - `cesAR = 1.0`
  - `cesAN = 1.0`
  - `cesEpsilon = 1.0E-8`

## 実装パターン

### CES実効労働の共通関数
```java
protected double computeEffectiveLabor(double nR, double nN) {
	if (cesDelta <= 0.0 || cesDelta >= 1.0) {
		throw new IllegalArgumentException("cesDelta must be in (0,1)");
	}
	if (Math.abs(cesRho) < 1e-12) {
		throw new IllegalArgumentException("cesRho must be non-zero");
	}
	if (cesAR <= 0.0 || cesAN <= 0.0 || cesEpsilon <= 0.0) {
		throw new IllegalArgumentException("cesAR, cesAN, cesEpsilon must be > 0");
	}
	double adjNR = Math.max(0.0, nR);
	double adjNN = Math.max(0.0, nN);
	if (adjNR == 0.0 && adjNN == 0.0) {
		return 0.0;
	}
	double baseR = Math.max(cesAR * adjNR, cesEpsilon);
	double baseN = Math.max(cesAN * adjNN, cesEpsilon);
	double termR = cesDelta * Math.pow(baseR, cesRho);
	double termN = (1.0 - cesDelta) * Math.pow(baseN, cesRho);
	double sum = Math.max(termR + termN, cesEpsilon);
	return Math.pow(sum, 1.0 / cesRho);
}
```

## Phase C1 完了！✓

完了項目:
1. ✓ CESパラメータの追加とsetter
2. ✓ `N_eff` 共通関数の実装（数値ガード付き）
3. ✓ XMLでCESパラメータの設定

## 次のフェーズ候補
- Phase C2: 比率計算（FOC）とクリップ
- Phase C3: 閉形式分解
- Phase C5/C6: 生産・価格への統合
