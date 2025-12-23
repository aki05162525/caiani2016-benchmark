# Phase C7 実装計画: 賃金更新のtype別化

## 目的

賃金更新のマクロ閾値や参照変数をR/N別にし、景気変動時の賃金調整が労働タイプで差別化されるようにする。

**完了の定義**: Households の macro 参照変数が `laborType` に応じた指標を返し、`AdaptiveWageStrategy` で type 別パラメータを利用できる。

---

## 現状確認（要点）

- `AdaptiveWageStrategy` は単一の `macroThreshold` / `microAdaptiveParameter` を使用
- `Households.getMacroReferenceVariableForWage()` は `LAG_AGGUNEMPLOYMENT` を返す
- `laborType` はHouseholdsに実装済み（R=0/N=1）

---

## 実装タスク

### タスク1: type別macro参照変数の導入

**対象**: `benchmark/benchmark/src/benchmark/agents/Households.java`

**内容**:
- `laborType` に応じて `LAG_AGGUNEMPLOYMENT_R/N` を返す
- 既存の `LAG_AGGUNEMPLOYMENT` は fallback として維持

---

### タスク2: AdaptiveWageStrategyのtype別パラメータ化

**対象**: `jmab/src/jmab/strategies/AdaptiveWageStrategy.java`

**内容**:
- `macroThresholdR/N` と `microAdaptiveParameterR/N` を追加（または倍率）
- `WageSetterWithTargets` から `laborType` を取得し分岐
- legacy の単一パラメータは fallback として維持

---

### タスク3: XML設定の追加

**対象ファイル**:
- `benchmark/benchmark/Model/modelBenchmark_light.xml`
- `benchmark/benchmark/Model/modelBenchmark_full.xml`

**内容**:
- 新しい type別パラメータを `AdaptiveWageStrategy` の bean に追加
- 既存パラメータは維持（互換性）

---

### タスク4: 最小検証

**確認項目**:
- RとNで macroThreshold が異なる場合、賃金更新が分岐する
- legacyパラメータのみでも動作する

---

## 実装順序

```
タスク1: Households の macro 参照変数分岐
  ↓
タスク2: AdaptiveWageStrategy の type別パラメータ化
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

### 2. 実行時確認
- R/Nで賃金更新が分岐すること（ログ or 値の差）

---

## 完了条件（Definition of Done）

- [ ] Householdsのmacro参照変数がtype別に分岐
- [ ] AdaptiveWageStrategyがtype別パラメータ対応
- [ ] XMLで設定可能
- [ ] 最小検証が完了

---

## 重要な設計判断

1. **互換性**: type別が未設定でも legacy パラメータで動作
2. **参照変数**: `LAG_AGGUNEMPLOYMENT_R/N` の整備が前提
3. **設定粒度**: micro/macro両方を type 別化する
