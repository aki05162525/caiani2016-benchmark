# Phase C8 実装計画: 失業給付の加重平均賃金化

## 目的

失業給付の基準賃金を単一平均から `W_R/W_N` の加重平均に置換し、労働市場のR/N構成に整合させる。

**完了の定義**: GovernmentAntiCyclical の給付計算が type 別平均賃金に基づく加重平均へ置換される。

---

## 現状確認（要点）

- `GovernmentAntiCyclical.payUnemploymentBenefits()` は単一平均賃金を使用
- type別賃金（R/N）は Households に `laborType` があるため集計可能

---

## 実装タスク

### タスク1: type別平均賃金の集計

**対象**: `benchmark/benchmark/src/benchmark/agents/GovernmentAntiCyclical.java`

**内容**:
- R/Nの雇用者数と賃金合計を集計
- `W_R = wagebillR / employedR`（雇用者0なら0）
- `W_N = wagebillN / employedN`（雇用者0なら0）

---

### タスク2: 加重平均賃金の計算

**式**:
```
bar_w = (W_R * N_R + W_N * N_N) / (N_R + N_N)
```

**ガード**:
- `N_R + N_N = 0` の場合は給付ゼロ

---

### タスク3: 給付計算の置換

**内容**:
- `unemploymentBenefit = bar_w * this.unemploymentBenefit`
- 支払いロジックは現行のまま維持

---

### タスク4: 最小検証

**確認項目**:
- R/N構成が変わると給付額が変化する
- 雇用者ゼロのとき例外が出ない

---

## 実装順序

```
タスク1: type別賃金集計
  ↓
タスク2: 加重平均賃金計算
  ↓
タスク3: 給付計算置換
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
- doleExpenditure が負にならないこと

---

## 完了条件（Definition of Done）

- [ ] type別賃金集計が追加されている
- [ ] 加重平均賃金で給付が計算される
- [ ] ゼロ割ガードが入っている
- [ ] 最小検証が完了している

---

## 重要な設計判断

1. **対象**: GovernmentAntiCyclical のみ（他政府系は現状維持）
2. **fallback**: 雇用者ゼロ時は給付ゼロ
3. **集計基準**: Households の `laborType` を利用
