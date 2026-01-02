# シミュレーションパラメータ設定ガイド

このドキュメントでは、Caiani et al. (2016) ベンチマークモデルの主要なパラメータ設定箇所とシード値の変更方法を説明します。

## シード値の設定

### 設定ファイル

乱数のシード値は以下のファイルで設定されています：

- **Full版**: `benchmark/benchmark/Model/modelBenchmark_full.xml`（2122行目）
- **Light版**: `benchmark/benchmark/Model/modelBenchmark_light.xml`（2136行目）

### シード値の変更方法

```xml
<bean id="prng" class="net.sourceforge.jabm.prng.MersenneTwister">
    <property name="seed" value="1"/>  <!-- この値を変更 -->
</bean>
```

**使用例**：
- モンテカルロシミュレーションで異なる乱数系列を使う場合は、`value`の値を変更します（例: 1, 2, 3, ...）
- デフォルト値: `1`

---

## 主要パラメータの設定箇所

すべてのパラメータは `benchmark/benchmark/Model/modelBenchmark_full.xml` および `modelBenchmark_light.xml` で設定されています。

### 1. エージェント数

| エージェント種別 | Full版 | Light版 | 設定箇所（行番号） |
|-----------------|--------|---------|-------------------|
| **家計 (Households)** | 8000 | 1000 | ~2935行目 |
| **消費財企業 (C Firms)** | 100 | 20 | ~2203行目 |
| **資本財企業 (K Firms)** | 20 | 4 | ~1672行目 |
| **銀行 (Banks)** | 10 | 2 | ~2535行目 |

**変更例**：
```xml
<bean id="households" scope="simulation" class="net.sourceforge.jabm.agent.AgentList"
    init-method="populateFromFactory">
    <property name="size" value="8000" />  <!-- 家計の数 -->
    <property name="agentFactory" ref="householdsFactory" />
</bean>
```

### 2. 税率

| パラメータ | デフォルト値 | 設定箇所（行番号） |
|-----------|-------------|-------------------|
| **法人税率** | 18% (0.18) | ~2174行目 |
| **資産税率** | 0% | ~2169行目 |

**変更例**：
```xml
<bean id="profitTaxVal" scope="simulation" class="net.sourceforge.jabm.util.MutableDoubleWrapper">
    <constructor-arg value="0.18" />  <!-- 法人税率: 18% -->
</bean>
```

### 3. その他の重要なパラメータ

| パラメータ | 説明 | デフォルト値 | 設定箇所 |
|-----------|------|-------------|---------|
| **haircut** | 倒産時の資産減価率 | 50% (0.5) | ~2183行目 |
| **profitShare (配当性向)** | 利益の何%を配当として支払うか | K企業: 90%, 銀行: 60% | ~2189, ~2889行目 |
| **maximumRounds** | 1シミュレーションの最大ラウンド数 | Full: 1000, Light: 10 | mainBenchmark_*.xml 内 |

---

## パラメータ変更の手順

1. **設定ファイルを開く**
   - IntelliJで `benchmark/benchmark/Model/modelBenchmark_full.xml` を開く

2. **該当するパラメータを検索**
   - `Ctrl+F` (Windows/Linux) または `Cmd+F` (Mac) で検索
   - 例: "profitTaxVal" で税率を検索

4. **保存して実行**
   - ファイルを保存 (`Ctrl+S` / `Cmd+S`)
   - Gradleタスク `runFull` または `runLight` を実行

---

## 複数実験の実行（モンテカルロシミュレーション）

異なるシード値で複数回実行する場合：

1. シード値を変更（例: 1 → 2）
2. シミュレーションを実行
3. `benchmark/benchmark/data/` に出力されたCSVファイルを別フォルダに保存
4. シード値を変更（例: 2 → 3）
5. 手順2〜4を繰り返す

**ヒント**: データ出力先を変更したい場合は、`mainBenchmark_*.xml` の `fileNamePrefix` を変更してください（~251行目）。

---

## 注意事項

- パラメータ変更後は必ずGradleプロジェクトを再ビルドしてください
- XMLの構文エラーに注意（閉じタグ、引用符など）
- 変更前にバックアップを取ることを推奨します

---

## 参考

より詳細なパラメータの説明は、論文を参照してください：

> Caiani, A., Godin, A., Caverzasi, E., Gallegati, M., Kinsella, S., & Stiglitz, J. E. (2016). **Agent based-stock flow consistent macroeconomics: Towards a benchmark model.** _Journal of Economic Dynamics and Control_, 69, 375-408.
