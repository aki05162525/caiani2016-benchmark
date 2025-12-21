# Phase C6 実装計画: 価格（getPriceLowerBound）を2賃金費用に整合

## 目的

期待変動費 `W^e * N^D` を `W^e_R * N^{D,R} + W^e_N * N^{D,N}` に置換し、価格下限がR/N賃金系列とCES分解に整合するようにする。

**完了の定義**: ConsumptionFirm/CapitalFirm の `getPriceLowerBound()` が2賃金・CES分解を反映し、既存のロジック（在庫ゼロ時など）を壊さない。

---

## 現状確認（要点）

- ConsumptionFirm.getPriceLowerBound(): `EXPECTATIONS_WAGES * getRequiredWorkers()` を使用
- CapitalFirm.getPriceLowerBound(): `EXPECTATIONS_WAGES / laborProductivity` の単一系列
- CES分解（C3）と `computeLaborRatio/computeLaborSplit` は既に実装済み

---

## 実装タスク

### タスク1: 期待賃金の取得を2系列化

**対象ファイル**:
- `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java`
- `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java`

**内容**:
- `EXPECTATIONS_WAGES_R/N` を優先
- 取得できない場合は legacy `EXPECTATIONS_WAGES` を fallback

---

### タスク2: 期待変動費の再計算

**ConsumptionFirm**:
- `N^D = getRequiredWorkers()` を保持
- `ratio = computeLaborRatio(W^e_R, W^e_N)`
- `N^{D,R}, N^{D,N} = computeLaborSplit(N^D, ratio)`
- `expectedVariableCosts = W^e_R * N^{D,R} + W^e_N * N^{D,N}`

**CapitalFirm**:
- `N^D = getRequiredWorkers()` を保持（生産向け）
- 上記と同様に `expectedVariableCosts` を再計算
- `expectedAverageVarCosts = expectedVariableCosts / N^D` または `laborProductivity` への既存係数を再確認

---

### タスク3: 在庫ゼロ分岐への反映

**ConsumptionFirm**:
- inventoriesLeft の分岐内でも2賃金・CES分解を使う
- 既存の requiredWorkers 算出を維持し、最後の費用計算のみ置換

---

### タスク4: 最小検証

**確認項目**:
- `W^e_R↑` で価格下限が上昇
- `N^D=0` の場合は例外にならずゼロ割を回避
- `expectedVariableCosts` が NaN/Infinity にならない

---

## 実装順序

```
タスク1: 期待賃金取得を2系列化
  ↓
タスク2: 期待変動費の再計算
  ↓
タスク3: 在庫ゼロ分岐の反映
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
- 価格下限が負にならないこと

---

## 完了条件（Definition of Done）

- [ ] 期待変動費が2賃金・CES分解に整合
- [ ] ConsumptionFirm/CapitalFirm の `getPriceLowerBound()` が更新済み
- [ ] 在庫ゼロ分岐が破綻しない
- [ ] 最小検証が完了

---

## 重要な設計判断

1. **互換性**: 期待賃金は `W^e_R/N` を優先し、fallbackで `W^e`
2. **分解**: `computeLaborRatio/computeLaborSplit` を必ず使用
3. **ゼロ割**: `cesEpsilon` をガードとして流用
