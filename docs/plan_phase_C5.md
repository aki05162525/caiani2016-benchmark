# Phase C5 実装計画: 生産（produce）をCES実効労働に整合

## 目的

`produce()` の労働投入量を `employees.size()` から `N_eff(N_R, N_N)` に置換し、R/N構成が生産に反映されるようにする。

**完了の定義**: ConsumptionFirm/CapitalFirm の生産計算がCES実効労働に基づき、ゼロ割や負値が発生しない。

---

## 現状確認（要点）

- ConsumptionFirm.produce(): `residualWorkers = employees.size()` を使って生産量を決定
- CapitalFirm.produce(): `ActualLabor = employees.size()` を使って生産/研究配分を決定
- Type別賃金の集計は既に実装済み（Phase B1）
- CES関数 `computeEffectiveLabor()` は AbstractFirm に追加済み（Phase C1）

---

## 実装タスク

### タスク1: Type別人数の集計

**対象ファイル**:
- `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java`
- `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java`

**内容**:
- `employees` から `countR`, `countN` を集計
- `N_eff = computeEffectiveLabor(countR, countN)` を算出

---

### タスク2: ConsumptionFirm.produce() の置換

**変更内容**:
- `residualWorkers` を `N_eff` ベースに変更
- `outputQty` と `unitCost` の計算でゼロ割ガード（epsilon）
- `employees.size()` はログ/ラグ用途以外では使わない

---

### タスク3: CapitalFirm.produce() の置換

**変更内容**:
- `ActualLabor` を `N_eff` に置換
- 制約時の配分（生産/研究）に `N_eff` を使用
- `unitCost` のゼロ割ガード（epsilon）

---

### タスク4: 最小検証

**確認項目**:
- `countR=countN=0` で `outputQty=0`
- `N_eff` が増えると `outputQty` が増える
- `unitCost` に NaN/Infinity が出ない

---

## 実装順序

```
タスク1: Type別人数の集計
  ↓
タスク2: ConsumptionFirm.produce() 置換
  ↓
タスク3: CapitalFirm.produce() 置換
  ↓
タスク4: 最小検証
```

---

## 検証方法

### 1. コンパイル確認
```
./gradlew :benchmark:compileJava
```

### 2. 実行時確認
- NaN/Infinityが出ないこと
- LAG_PRODUCTION が負にならないこと

---

## 完了条件（Definition of Done）

- [ ] ConsumptionFirm/CapitalFirm の `produce()` が `N_eff` ベースになっている
- [ ] ゼロ割ガードが入っている
- [ ] 最小検証が完了している

---

## 重要な設計判断

1. **入力**: `countR/countN` は現行の `employees` から集計
2. **互換性**: 既存の生産ロジックを温存し、労働量のみ置換
3. **ガード**: `cesEpsilon` をゼロ割回避に流用
