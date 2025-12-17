# ベンチマークパラメータ設定ガイド

## 概要

このドキュメントでは、Caiani et al. (2016) のAgent Based-Stock Flow Consistentモデルのベンチマークシミュレーションにおけるパラメータ設定方法を説明します。

## 主要な設定ファイル

### 1. メイン設定ファイル
- **`benchmark/benchmark/Model/mainBenchmark_light.xml`** - 軽量ベンチマーク（10ラウンド）
- **`benchmark/benchmark/Model/mainBenchmark_full.xml`** - 完全ベンチマーク（1000ラウンド）

### 2. モデル定義ファイル
- **`benchmark/benchmark/Model/modelBenchmark_light.xml`** - エージェント、市場、パラメータの定義
- **`benchmark/benchmark/Model/reports.xml`** - 出力レポート設定

## シミュレーション基本設定

シミュレーションのラウンド数は `modelBenchmark_light.xml` の190行目付近で設定：

```xml
<bean id="macroSimulation" class="jmab.simulations.OrderedEventsSimulation">
  <property name="maximumRounds" value="10" />  <!-- シミュレーション期間 -->
</bean>
```

## エージェント人口設定

各エージェントの数は `modelBenchmark_light.xml` で定義されています：

| エージェント種別 | 数 | 設定箇所 |
|----------------|-----|---------|
| 資本財企業 | 20 | 1446行目 |
| 消費財企業 | 100 | 1908行目 |
| 銀行 | 10 | 2201行目 |
| 家計 | 8000 | 2602行目 |
| 政府 | 1 | 2794行目 |
| 中央銀行 | 1 | 2895行目 |

変更例：
```xml
<bean id="capitalFirms" class="net.sourceforge.jabm.agent.AgentList">
  <property name="size" value="20" />  <!-- この数値を変更 -->
</bean>
```

## 主要な経済パラメータ

### 生産パラメータ（資本財企業、1545-1547行目）
```xml
<property name="capitalProductivity" value="1"/>      <!-- 資本生産性 -->
<property name="laborProductivity" value="2"/>        <!-- 労働生産性 -->
<property name="capitalLaborRatio" value="6.4"/>      <!-- 資本労働比率 -->
```

### 財政パラメータ

利潤税率（1878行目）：
```xml
<bean id="profitTaxVal" class="net.sourceforge.jabm.util.MutableDoubleWrapper">
  <constructor-arg value="0.18" />  <!-- 18%の利潤税 -->
</bean>
```

失業給付率（政府、2844行目）：
```xml
<property name="unemploymentBenefit" value="0.4" />  <!-- 賃金の40% -->
```

離職率（1880行目）：
```xml
<bean id="turnoverVal" class="net.sourceforge.jabm.util.MutableDoubleWrapper">
  <constructor-arg value="0.05" />  <!-- 5%の離職率 -->
</bean>
```

### 破産パラメータ（1887-1889行目）
```xml
<bean id="haircutVal" class="net.sourceforge.jabm.util.MutableDoubleWrapper">
  <constructor-arg value="0.5" />  <!-- 破産時の投げ売り50%ディスカウント -->
</bean>
```

### 銀行パラメータ（2286-2287行目）
```xml
<property name="riskAversionC" value="3.922445" />   <!-- 消費信用のリスク回避度 -->
<property name="riskAversionK" value="21.513347" />  <!-- 資本信用のリスク回避度 -->
```

### 金利パラメータ

国債金利（政府、2840行目）：
```xml
<property name="bondInterestRate" value="0.0025" />  <!-- 0.25% -->
```

中央銀行金利（2938-2939行目）：
```xml
<property name="advancesInterestRate" value="0.005"/>  <!-- 貸出金利 0.5% -->
<property name="reserveInterestRate" value="0" />      <!-- 準備預金金利 0% -->
```

## 市場パラメータ

市場での取引回数や取引相手数は252-524行目で設定：

```xml
<property name="nbRuns" value="100"/>     <!-- 消費財市場：100回 -->
<property name="nbRuns" value="8000"/>    <!-- 労働市場：8000回 -->
<property name="nbSellers" value="5"/>    <!-- 資本財市場：5社の売り手 -->
<property name="nbSellers" name="3"/>     <!-- 信用市場：3行の貸し手 -->
```

## 期待形成パラメータ

適応的期待（1554-1573行目）：
```xml
<bean id="expSales" class="jmab.expectations.SimpleAdaptiveExpectation">
  <property name="numberPeriod" value="4"/>       <!-- 過去4期を参照 -->
  <property name="adaptiveParam" value="0.25"/>   <!-- 調整パラメータ α -->
</bean>
```

## 初期値設定

経済の初期状態は `sfcAgentsInitialiser` で設定（1385-1419行目）：

```xml
<bean id="sfcAgentsInitialiser" class="benchmark.init.SFCSSMacroAgentInitialiser">
  <property name="hhsDep" value="80704.13311"/>       <!-- 家計預金 -->
  <property name="kPrice" value="2.6875"/>            <!-- 資本財価格 -->
  <property name="cPrice" value="1.030357291"/>       <!-- 消費財価格 -->
  <property name="iLoans" value="0.0075"/>            <!-- 貸出金利 0.75% -->
  <property name="iDep" value="0.0025"/>              <!-- 預金金利 0.25% -->
  <property name="ksEmpl" value="1000"/>              <!-- 資本財企業雇用 -->
  <property name="csEmpl" value="5000"/>              <!-- 消費財企業雇用 -->
</bean>
```

## パラメータ変更方法

### 方法1：XML直接編集
`modelBenchmark_light.xml` のパラメータ値を直接編集します。

### 方法2：Mutable Wrapper経由
`MutableDoubleWrapper` を使用しているパラメータは実行時に変更可能：
```xml
<bean id="profitTaxVal" class="net.sourceforge.jabm.util.MutableDoubleWrapper">
  <constructor-arg value="0.18" />  <!-- この値を変更 -->
</bean>
```

### 方法3：実験用プロパティファイル
`experiment.properties` で感度分析用のパラメータ範囲を指定：
```properties
# 形式: parameter.name=最小値:ステップ:最大値
profitTaxVal.value=0.10:0.05:0.40
governmentPrototype.unemploymentBenefit=0.2:0.1:0.6
bankPrototype.riskAversionC=1:1:8
```

## 出力ディレクトリ

シミュレーション結果の出力先は `mainBenchmark_light.xml` の106行目で設定：
```xml
<bean id="fileNamePrefix" class="net.sourceforge.jabm.util.MutableStringWrapper">
  <constructor-arg value="data/" />
</bean>
```

## 参考

- メインモデル定義：`benchmark/benchmark/Model/modelBenchmark_light.xml`
- レポート設定：`benchmark/benchmark/Model/reports.xml`
- Spring Framework XMLベースの依存性注入を使用
