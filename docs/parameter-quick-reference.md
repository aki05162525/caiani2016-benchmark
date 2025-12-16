# パラメータ修正クイックリファレンス

Caiani & Godin (2016) ベンチマークモデルの主要パラメータ一覧です。

---

## 修正ファイル

**メイン設定:** `Model/modelBenchmark_light.xml`

---

## 主要パラメータ一覧

### 1. 政府パラメータ

| パラメータ | デフォルト | 説明 | 推奨実験範囲 |
|-----------|-----------|------|-------------|
| `unemploymentBenefit` | 0.4 | 失業保険給付率（平均賃金比） | 0.2 - 0.8 |
| `fixedLaborDemand` | 1360 | 政府職員数 | 500 - 2500 |
| `bondInterestRate` | 0.0025 | 国債金利（0.25%） | 0.001 - 0.01 |

**XML検索:**
```bash
grep -A 5 'governmentPrototype' Model/modelBenchmark_light.xml
```

**修正例:**
```xml
<bean id="governmentPrototype" ...>
    <property name="unemploymentBenefit" value="0.6" />
    <property name="fixedLaborDemand" value="2000" />
</bean>
```

---

### 2. 中央銀行パラメータ

| パラメータ | デフォルト | 説明 | 推奨実験範囲 |
|-----------|-----------|------|-------------|
| `advancesInterestRate` | 0.005 | 引当金利（0.5%） | 0.001 - 0.02 |
| `reserveInterestRate` | 0.0 | 準備金金利 | 0.0 - 0.01 |

**XML検索:**
```bash
grep -A 3 'centralBankPrototype' Model/modelBenchmark_light.xml
```

**修正例:**
```xml
<bean id="centralBankPrototype" ...>
    <property name="advancesInterestRate" value="0.01" />
</bean>
```

---

### 3. 銀行パラメータ

| パラメータ | デフォルト | 説明 | 推奨実験範囲 |
|-----------|-----------|------|-------------|
| `riskAversionC` | 3.922445 | 消費財企業へのリスク回避度 | 2.0 - 6.0 |
| `riskAversionK` | 21.513347 | 資本財企業へのリスク回避度 | 10.0 - 30.0 |

**注:** 高い値 = 厳格な信用審査 = 信用制約強化

**XML検索:**
```bash
grep -A 5 'bankPrototype' Model/modelBenchmark_light.xml | grep riskAversion
```

**修正例:**
```xml
<bean id="bankPrototype" ...>
    <property name="riskAversionC" value="2.5" />
    <property name="riskAversionK" value="15.0" />
</bean>
```

---

### 4. 企業パラメータ（資本財・消費財）

| パラメータ | デフォルト | 説明 | 推奨実験範囲 |
|-----------|-----------|------|-------------|
| `minWageDiscount` | 1.0 | 最小賃金支払率（破産閾値） | 0.8 - 1.0 |
| `shareOfExpIncomeAsDeposit` | 1.0 | 予想収入の預金保有割合 | 0.5 - 1.0 |

**XML検索:**
```bash
grep -A 5 'capitalFirmPrototype' Model/modelBenchmark_light.xml
grep -A 5 'consumptionFirmPrototype' Model/modelBenchmark_light.xml
```

**修正例:**
```xml
<bean id="capitalFirmPrototype" ...>
    <property name="shareOfExpIncomeAsDeposit" value="0.7" />
</bean>
```

---

## シミュレーション設定

### 実行設定

**ファイル:** `Model/mainBenchmark_light.xml`

| パラメータ | デフォルト | 説明 |
|-----------|-----------|------|
| `numSimulations` | 1 | 実行回数（モンテカルロ） |
| `maximumRounds` | 400 | 最大ティック数（期間） |

**修正例:**
```xml
<bean id="simulationController" ...>
    <property name="numSimulations" value="10"/>
    <property name="maximumRounds" value="800"/>
</bean>
```

---

## 修正手順テンプレート

### 1. バックアップ作成
```bash
cp Model/modelBenchmark_light.xml Model/modelBenchmark_light_backup.xml
```

### 2. パラメータ検索
```bash
grep 'name="パラメータ名"' Model/modelBenchmark_light.xml
```

### 3. XMLエディタで修正
- IntelliJ IDEAまたはテキストエディタで開く
- `<property name="..." value="新しい値" />`を変更
- 保存

### 4. 実行
- IntelliJ IDEAで`Benchmark Main (Light)`を実行
- または：
  ```bash
  java -Djabm.config=Model/mainBenchmark_light.xml -cp ... benchmark.Main
  ```

### 5. 結果確認
```bash
ls -lh data/
```
- 各CSVファイルを確認

---

## 推奨実験シナリオ

### 実験1: 失業保険の効果

**目的:** 景気循環対策の有効性検証

**変更パラメータ:**
- `unemploymentBenefit`: 0.2, 0.4（ベース）, 0.6, 0.8

**注目指標:**
- `unemploymentCSVReport`: 失業率
- `nominalGDPCSVReport`: 名目GDP
- `aggGBSCSVReport`: 政府バランスシート（赤字）

---

### 実験2: 信用制約の影響

**目的:** 銀行規制と経済成長の関係

**変更パラメータ:**
- `riskAversionC`: 2.0（緩和）, 3.92（ベース）, 6.0（厳格）
- `riskAversionK`: 10.0（緩和）, 21.5（ベース）, 30.0（厳格）

**注目指標:**
- `microBankCreditCSVReport`: 信用供給量
- `nominalInvestmentCSVReport`: 投資額
- `banksCRCSVReport`: 銀行自己資本比率

---

### 実験3: 金融政策の効果

**目的:** 政策金利変更の波及経路

**変更パラメータ:**
- `advancesInterestRate`: 0.001（超低金利）, 0.005（ベース）, 0.02（高金利）

**注目指標:**
- `loanAvInterestCSVReport`: 貸出金利
- `depAvInterestCSVReport`: 預金金利
- `nominalInvestmentCSVReport`: 投資額

---

### 実験4: 政府支出の経済効果

**目的:** 財政政策の乗数効果

**変更パラメータ:**
- `fixedLaborDemand`: 500（緊縮）, 1360（ベース）, 2500（拡張）

**注目指標:**
- `unemploymentCSVReport`: 失業率
- `nominalGDPCSVReport`: GDP
- `aggGBSCSVReport`: 政府債務

---

## トラブルシューティング

### エラー: XMLパースエラー
```
Attribute 'local' is not allowed
```

**原因:** Spring旧構文が残存

**対策:**
```bash
cd Model
find . -name "*.xml" -exec sed -i 's/<idref local=/<idref bean=/g' {} +
```

---

### エラー: シミュレーション異常終了

**原因候補:**
- 極端なパラメータ値
- 倒産連鎖による全エージェント消滅

**対策:**
1. パラメータを段階的に変更
2. ログ確認: `Model/log4j.xml`で詳細ログ有効化
3. バックアップから復元

---

### 結果が不安定

**対策:**
- `numSimulations`を10以上に増やす
- 初期シード固定（詳細はJABMドキュメント参照）

---

## 便利なコマンド集

### パラメータ値の一括確認
```bash
# 政府パラメータ
grep 'governmentPrototype' Model/modelBenchmark_light.xml -A 50 | grep 'property name'

# 銀行パラメータ
grep 'bankPrototype' Model/modelBenchmark_light.xml -A 100 | grep 'riskAversion'

# 全数値パラメータ
grep -E '<property name="[^"]*" value="[0-9.]+"' Model/modelBenchmark_light.xml
```

### 結果の簡易確認
```bash
# 最新のGDPデータ
tail -10 data/nominalGDP*.csv

# 失業率データ
tail -10 data/unemployment*.csv
```

---

## 詳細情報

**包括的な調査書:** `docs/model-analysis.md`

**設定ファイル:**
- メイン: `Model/mainBenchmark_light.xml`
- モデル: `Model/modelBenchmark_light.xml`
- レポート: `Model/reports.xml`

**ソースコード:**
- エージェント: `src/benchmark/agents/`
- 定数: `src/benchmark/StaticValues.java`

---

**最終更新:** 2025-11-08
