# Phase C2 実装計画: 需要比率（FOC）とクリップ

## 目的

CES需要分解で使用する比率 `ratio` を導出し、極端な値を防ぐためのクリップ処理を実装する。

**完了の定義**: `ratio_raw` の計算式と `clip` が実装され、`W^e_R/W^e_N` の変化に整合する挙動が確認できる。

---

## 実装方針

### 比率の定義

```
ratio_raw = (δ/(1-δ))^(1/(1-ρ)) * (A_R/A_N)^(ρ/(1-ρ)) * (W^e_N/W^e_R)^(1/(1-ρ))
ratio = clip(ratio_raw, φ_min, φ_max)
```

### クリップの目的

- 極端な賃金比・生産性比でも需要が発散しない
- 数値的な安定性を確保

---

## 実装タスク

### タスク1: パラメータの追加

**対象**: `AbstractFirm`（Phase C1で追加したCESパラメータと同じ場所）

**追加するパラメータ**:
- `phiMin`（> 0）
- `phiMax`（phiMin < phiMax）

**対応**:
- フィールド追加
- getter/setter追加
- コメントで範囲を明記

---

### タスク2: ratio計算関数の実装

**対象**: `AbstractFirm` に protected helper として追加

**内容**:
- `computeLaborRatio(expWageR, expWageN)` を追加
- `ratio_raw` を上式で計算
- `phiMin/phiMax` でクリップ
- `expWageR`, `expWageN` が0近傍の場合は `epsilon` でガード

---

### タスク3: XML設定の追加

**対象ファイル**:
- `benchmark/benchmark/Model/modelBenchmark_light.xml`
- `benchmark/benchmark/Model/modelBenchmark_full.xml`

**追加内容**:
- `phiMin`, `phiMax` を Consumption/Capital プロトタイプへ追加
- 既定値は安全域（例: 0.2, 5.0 など）を採用

---

### タスク4: 最小検証

**確認項目**:
- `W^e_R` 上昇で ratio が低下
- `W^e_N` 上昇で ratio が上昇
- `phiMin/phiMax` が正しく効く

---

## 実装順序

```
タスク1: パラメータ追加
  ↓
タスク2: ratio計算関数の実装
  ↓
タスク3: XML設定追加
  ↓
タスク4: 最小検証
```

---

## 検証方法

### 1. コンパイル確認
```
./gradlew :benchmark:compileJava
```

### 2. 目視確認
- `ratio_raw` とクリップ後の値が期待通りに変化すること
- 例外（NaN, Infinity）が出ないこと

---

## 完了条件（Definition of Done）

- [ ] `phiMin/phiMax` が追加されている
- [ ] ratio計算関数が実装されている
- [ ] XMLで設定可能になっている
- [ ] 簡易チェックで単調性が確認できる

---

## 重要な設計判断

1. **配置場所**: Phase C1と同様、`AbstractFirm` に実装
2. **賃金のガード**: `cesEpsilon` を使用してゼロ割を回避
3. **クリップ範囲**: 明示的に `phiMin < phiMax` を要求
