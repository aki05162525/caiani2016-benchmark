# GDPマイナス対策 修正案（unitCost爆発の抑制）

## 概要

現在のGDP算出は在庫評価の差分を用いており、在庫の`unitCost`が極端に跳ねるとGDPが大幅マイナスになる。
本計画では、`outputQty`が0近傍のときに`unitCost`が発散する経路を抑止する修正案を整理する。

---

## 現状整理（コード根拠）

- GDPは「期中在庫価値 - 前期在庫価値 + 公務員賃金」で算出される。
  - `jmab/src/main/java/jmab/report/NominalGDPComputer.java`
- 在庫価値は生産者保有の財に限り`unitCost * quantity`で評価される。
  - `jmab/src/main/java/jmab/stockmatrix/ConsumptionGood.java`
  - `jmab/src/main/java/jmab/stockmatrix/CapitalGood.java`
- `unitCost`は生産で更新され、`outputQty`が小さい/0でも更新される。
  - `benchmark/benchmark/src/main/java/benchmark/agents/CapitalFirm.java`
  - `benchmark/benchmark/src/main/java/benchmark/agents/ConsumptionFirm.java`
- `outputQty`が0のままでも`unitCost`が更新されると、在庫価値が爆発しGDPが大幅マイナスになる。

---

## 修正方針

### 目的
- `outputQty`が0近傍のときに`unitCost`が発散する経路を遮断する。
- 生産関数や雇用ロジックの意味を大きく変えずに安定化する。

### 対象ファイル
- `benchmark/benchmark/src/main/java/benchmark/agents/CapitalFirm.java`
- `benchmark/benchmark/src/main/java/benchmark/agents/ConsumptionFirm.java`

---

## 実装案（優先度順）

### 案A: unitCost更新のガード（推奨）
- `outputQty <= 0` の場合、`unitCost`を更新しない（前回値を維持）。
- 既存の`inventories.setUnitCost(...)`を`if (outputQty > 0)`で囲む。

**メリット**
- GDPマイナスの直接トリガ（unitCost爆発）を止められる。
- 影響範囲が小さい。

**デメリット**
- 生産が0の期はunitCostが更新されないため、在庫評価が遅延する。

### 案B: ActualOutputLaborの下限設定
- `effectiveLabor > 0`のとき`ActualOutputLabor`を最低1にする。

**メリット**
- outputQty=0を回避しunitCost爆発を抑制。

**デメリット**
- 生産量が強制的に増え、モデル挙動に影響が大きい。

### 案C: unitCostの上限クリップ
- `unitCost`に上限値を設ける（例: 前期`unitCost`の数倍）。

**メリット**
- 爆発を強制的に抑制可能。

**デメリット**
- 原因を隠すだけで、仕様の妥当性が弱い。

---

## 選択肢（推奨度と理由）

1. **案A: unitCost更新ガード**
   - 推奨度: ⭐⭐⭐⭐⭐
   - 理由: GDPマイナスの直接原因に最小変更で効くため。
2. **案B: ActualOutputLabor下限**
   - 推奨度: ⭐⭐⭐
   - 理由: 効く可能性はあるがモデル挙動の歪みが大きい。
3. **案C: unitCost上限制限**
   - 推奨度: ⭐⭐⭐
   - 理由: 応急処置としては有効だが理論的根拠が弱い。

---

## 実装手順（案Aベース）

### Phase 1: unitCost更新ガード
- [x] CapitalFirm: `outputQty > 0` のときのみ`inventories.setUnitCost`を実行
- [x] ConsumptionFirm: 同様のガードを追加

### Phase 2: 動作検証
- [ ] GDPがマイナスに落ちる期が消えるかを確認
- [ ] `nominalGDP`の初期数期の推移をチェック

---

## リスクと対策

- **リスク**: 生産ゼロ期にunitCostが更新されず、在庫評価が遅れる
  - **対策**: 影響の有無をGDP推移で確認。必要なら案Cの導入を再検討。

---

## メモ

- シリアライズ経路は`runLight`/`runFull`では無効のため、本問題の主因ではない。
- `computeEffectiveLabor`を強制的に下限1にする修正は挙動を歪める可能性があるため、慎重に扱う。
