# Phase C3 実装計画: 閉形式分解（N^D → N^{D,R}, N^{D,N}）

## 目的

CES需要分解の閉形式計算を実装し、総需要 `N^D` を `N^{D,R}` と `N^{D,N}` に分解する。

**完了の定義**: `computeLaborDemand()` 内で ratio に基づく分解が CES閉形式に置き換わり、`N_eff ≈ N^D` が成立する。

---

## 実装方針

### 分解式

```
ratio = computeLaborRatio(W^e_R, W^e_N)
Denom = [ δ * (A_R * ratio)^ρ + (1-δ) * (A_N)^ρ ]^(1/ρ)
N^{D,N} = N^D / Denom
N^{D,R} = ratio * N^{D,N}
```

### ガード

- `N^D < epsilon` なら早期リターン（0,0）
- `Denom` は `max(Denom, epsilon)`

---

## 実装タスク

### タスク1: 分解ヘルパーの追加

**対象**: `AbstractFirm`（C1/C2と同じ場所）

**内容**:
- `computeLaborSplit(nTotal, expWageR, expWageN)` を追加
- ratio計算 → Denom計算 → `(nR, nN)` を返す

---

### タスク2: 企業の computeLaborDemand の置換

**対象ファイル**:
- `benchmark/benchmark/src/benchmark/agents/ConsumptionFirm.java`
- `benchmark/benchmark/src/benchmark/agents/CapitalFirm.java`
- `benchmark/benchmark/src/benchmark/agents/ConsumptionFirmWagesEnd.java`
- `benchmark/benchmark/src/benchmark/agents/CapitalFirmWagesEnd.java`

**変更内容**:
- 既存の `ratioR=0.65` の単純分解を削除
- C3の分解ヘルパーを呼び、`nbWorkersR/N` を得る
- `laborDemandR/N` の算定ロジックは現状のまま使用

---

### タスク3: 期待賃金入力の統一

**方針**:
- `EXPECTATIONS_WAGES_R/N` を優先
- 取得できない場合は `EXPECTATIONS_WAGES` を代替に使う（後方互換性）

---

### タスク4: 最小検証

**確認項目**:
- `N^D=0` のとき `N^{D,R}=N^{D,N}=0`
- `N^D` が増えると `N^{D,R}, N^{D,N}` も増える
- `N_eff(N^{D,R}, N^{D,N}) ≈ N^D` が概ね成立

---

## 実装順序

```
タスク1: 分解ヘルパー追加
  ↓
タスク2: computeLaborDemand 置換
  ↓
タスク3: 期待賃金入力の統一
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
- laborDemandR/N が負にならないこと

---

## 完了条件（Definition of Done）

- [ ] C3閉形式分解が実装されている
- [ ] 企業の computeLaborDemand がC3に置き換わっている
- [ ] 期待賃金入力が統一されている
- [ ] 最小検証が完了している

---

## 重要な設計判断

1. **配置場所**: C1/C2と同様 `AbstractFirm` に集約
2. **互換性**: 期待賃金は `W^e_R/N` を優先し、fallbackで `W^e`
3. **ガード**: `epsilon` によるゼロ割回避を必須
