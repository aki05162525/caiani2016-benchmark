# 二重労働市場パラメータ解説

本ドキュメントは `parameters.xml` に定義されている二重労働市場モデルのパラメータについて詳しく説明します。

## 目次

1. [概要](#概要)
2. [パラメータ一覧](#パラメータ一覧)
3. [詳細説明](#詳細説明)
   - [初期分布 (Phase A2)](#初期分布-phase-a2)
   - [離職・解雇率 (Phase B2.3)](#離職解雇率-phase-b23)
   - [CES生産関数 (Phase C1/C2)](#ces生産関数-phase-c1c2)
   - [採用・賃金調整 (Phase C7)](#採用賃金調整-phase-c7)
4. [パラメータ間の関係性](#パラメータ間の関係性)
5. [シナリオ別設定例](#シナリオ別設定例)
6. [参考文献](#参考文献)

---

## 概要

このモデルは日本型二重労働市場を表現しており、労働者を以下の2タイプに分類します：

- **R型（Regular）**: 正規雇用労働者
  - 特徴: 低い離職率、解雇されにくい、安定した雇用

- **N型（Non-regular）**: 非正規雇用労働者
  - 特徴: 高い離職率、雇用の調整弁、不安定な雇用

全18個のパラメータが `parameters.xml` に定義されており、労働市場の二重構造、生産技術、賃金決定メカニズムを制御します。

---

## パラメータ一覧

| Phase | パラメータ名 | デフォルト値 | 説明 | 変更頻度 |
|-------|------------|------------|------|---------|
| **A2: 初期分布** |
| A2 | `laborTypeRatioR` | 0.65 | 初期のR型労働者比率（65%） | 高 |
| **B2.3: 離職・解雇** |
| B2.3 | `turnoverLaborRVal` | 0.02 | R型自主離職率（2%/期） | 中 |
| B2.3 | `turnoverLaborNVal` | 0.05 | N型自主離職率（5%/期） | 中 |
| B2.3 | `layoffRateRVal` | 0.3 | R型解雇率（30%） | 高 |
| B2.3 | `layoffRateNVal` | 0.8 | N型解雇率（80%） | 高 |
| **C1/C2: CES生産関数** |
| C1/C2 | `cesDelta` | 0.5 | R型労働の重みパラメータ | 高 |
| C1/C2 | `cesRho` | 0.5 | 代替弾力性パラメータ | 高 |
| C1/C2 | `cesAR` | 1.0 | R型労働の効率パラメータ | 中 |
| C1/C2 | `cesAN` | 1.0 | N型労働の効率パラメータ | 中 |
| C1/C2 | `cesEpsilon` | 1.0E-8 | 数値安定性パラメータ | 低 |
| C1/C2 | `phiMin` | 0.2 | 生産性調整の下限 | 低 |
| C1/C2 | `phiMax` | 5.0 | 生産性調整の上限 | 低 |
| **C7: 採用・賃金調整** |
| C7 | `sampleSizeR` | 0 | R型採用サンプリングサイズ | 低 |
| C7 | `sampleSizeN` | 0 | N型採用サンプリングサイズ | 低 |
| C7 | `macroThresholdR` | 0.08 | R型賃金調整の失業率閾値 | 中 |
| C7 | `macroThresholdN` | 0.08 | N型賃金調整の失業率閾値 | 中 |
| C7 | `macroAdaptiveParameterR` | 1.0 | R型マクロ反応係数 | 中 |
| C7 | `macroAdaptiveParameterN` | 1.0 | N型マクロ反応係数 | 中 |
| C7 | `microAdaptiveParameterR` | 1.0 | R型ミクロ反応係数 | 低 |
| C7 | `microAdaptiveParameterN` | 1.0 | N型ミクロ反応係数 | 低 |

**変更頻度の目安:**
- **高**: 分析の中核パラメータ、シナリオごとに変更する可能性が高い
- **中**: 感度分析や政策シミュレーションで変更する
- **低**: 基本的にデフォルト値のまま使用（技術的パラメータ）

---

## 詳細説明

### 初期分布 (Phase A2)

#### `laborTypeRatioR` (デフォルト: 0.65)

**経済学的意味:**
- 初期状態におけるR型（正規雇用）労働者の比率
- 残り (1 - laborTypeRatioR) がN型（非正規雇用）労働者となる

**影響範囲:**
- シミュレーション開始時の労働市場構造
- 初期の総需要・総供給バランス
- 所得分配の初期状態

**設定ガイドライン:**
- **日本の現状**: 0.60-0.65（非正規雇用率35-40%）
- **欧米型**: 0.80-0.90（非正規雇用率10-20%）
- **極端な二重構造**: 0.40-0.50（非正規雇用率50-60%）

**注意点:**
- この比率は初期値であり、シミュレーション中に内生的に変化する
- 極端な値（0.1未満や0.9超）は現実的でなく、数値的不安定を招く可能性がある

---

### 離職・解雇率 (Phase B2.3)

#### `turnoverLaborRVal` / `turnoverLaborNVal` (デフォルト: 0.02 / 0.05)

**経済学的意味:**
- 自主的な離職率（voluntary turnover rate）
- R型 < N型 は「正規雇用の方が雇用安定性が高い」ことを反映

**影響範囲:**
- 労働市場の流動性
- 企業の採用コスト
- マッチング効率

**設定ガイドライン:**
- **現実的範囲**: R型 0.01-0.03、N型 0.03-0.10
- **高流動性**: 両方とも高めに設定（R型 0.05、N型 0.15など）
- **低流動性**: 両方とも低めに設定（R型 0.005、N型 0.02など）

**パラメータ間の関係:**
- `turnoverLaborNVal >= turnoverLaborRVal` を維持することが推奨される
- 比率 (N/R) が大きいほど、二重構造が顕著になる

---

#### `layoffRateRVal` / `layoffRateNVal` (デフォルト: 0.3 / 0.8)

**経済学的意味:**
- 解雇率（layoff rate）：企業が雇用削減時に各タイプを解雇する確率
- R型 << N型 は「非正規が雇用調整の調整弁」という現実を反映

**影響範囲:**
- 景気変動時の失業率の変化
- 労働タイプ間の失業率格差
- 企業の雇用戦略

**設定ガイドライン:**
- **雇用保護強化**: R型を0.1-0.2に低下（解雇しにくい）
- **雇用柔軟化**: R型を0.5-0.7に上昇（解雇しやすい）
- **N型の役割縮小**: N型を0.5-0.6に低下（調整弁機能の弱体化）

**重要な関係性:**
```
layoffRateNVal >> layoffRateRVal
```
この格差が二重労働市場の核心的特徴です。

**実証的根拠:**
- 日本のデータでは、非正規雇用者の雇用調整確率は正規の2-3倍
- デフォルト値 (0.8/0.3 ≒ 2.67倍) はこれを反映

---

### CES生産関数 (Phase C1/C2)

#### 理論的背景

企業の有効労働量 L_eff は以下のCES（Constant Elasticity of Substitution）関数で計算されます：

```
L_eff = [δ * (A_R * L_R)^ρ + (1-δ) * (A_N * L_N)^ρ]^(1/ρ)
```

ここで：
- L_R, L_N: R型、N型の労働者数
- δ: `cesDelta`（R型の重み）
- ρ: `cesRho`（代替パラメータ）
- A_R, A_N: `cesAR`, `cesAN`（効率パラメータ）

**代替弾力性 σ:**
```
σ = 1 / (1 - ρ)
```

---

#### `cesDelta` (デフォルト: 0.5)

**経済学的意味:**
- 生産におけるR型労働の相対的重要度
- δ = 0.5: 両タイプが同等の重み
- δ > 0.5: R型がより重要
- δ < 0.5: N型がより重要

**影響範囲:**
- 労働タイプ間の賃金格差
- 企業の最適雇用構成
- 労働需要の価格弾力性

**設定ガイドライン:**
- **R型重視**: 0.6-0.8（技能集約的産業）
- **対等**: 0.4-0.6（標準的設定）
- **N型重視**: 0.2-0.4（単純労働集約的産業）

---

#### `cesRho` (デフォルト: 0.5)

**経済学的意味:**
- R型とN型の代替可能性を決定
- 代替弾力性 σ = 1/(1-ρ) で評価

**ρと代替弾力性の関係:**

| ρ値 | σ (代替弾力性) | 意味 |
|-----|--------------|------|
| -∞ | 0 | レオンチェフ型（完全補完） |
| 0 | 1 | コブ・ダグラス型 |
| 0.5 | 2 | **デフォルト（中程度の代替性）** |
| 1 | ∞ | 完全代替 |

**影響範囲:**
- 賃金変化に対する雇用構成の反応
- 労働タイプ間の賃金格差の安定性
- 政策の有効性

**設定ガイドライン:**
- **低代替性（補完的）**: ρ = -0.5 → σ = 0.67
- **コブ・ダグラス**: ρ = 0 → σ = 1.0
- **高代替性**: ρ = 0.8 → σ = 5.0

**注意点:**
- ρ ≥ 1 は避ける（数値的不安定）
- ρ < -2 も避ける（極端な補完性）

---

#### `cesAR` / `cesAN` (デフォルト: 1.0 / 1.0)

**経済学的意味:**
- 各労働タイプの効率パラメータ（生産性）
- A_R > A_N: R型の方が生産的
- A_R = A_N: 同等の生産性（デフォルト）

**影響範囲:**
- 労働タイプ間の賃金格差
- 企業の雇用インセンティブ
- 技術進歩の偏りを表現可能

**設定ガイドライン:**
- **R型優位**: A_R = 1.2-1.5, A_N = 1.0
- **技術進歩のシミュレーション**: 時系列で徐々に変化させる
- **スキル格差の拡大**: A_R を段階的に上昇

**実験例:**
```xml
<!-- 正規雇用が20%高い生産性 -->
<constructor-arg value="1.2" /> <!-- cesAR -->
<constructor-arg value="1.0" /> <!-- cesAN -->
```

---

#### `cesEpsilon` (デフォルト: 1.0E-8)

**技術的意味:**
- CES関数計算時の数値安定性確保のための微小値
- ゼロ除算やアンダーフローを防ぐ

**影響範囲:**
- 実質的な経済的影響はほぼゼロ
- 極端なパラメータ設定時の数値エラー防止

**設定ガイドライン:**
- **通常は変更不要**
- 数値エラーが発生する場合のみ 1.0E-6 〜 1.0E-10 の範囲で調整

---

#### `phiMin` / `phiMax` (デフォルト: 0.2 / 5.0)

**経済学的意味:**
- 生産性調整係数の許容範囲
- 企業の技術水準やショックの範囲を制約

**影響範囲:**
- 企業間の生産性格差
- ショックの伝播メカニズム

**設定ガイドライン:**
- **通常は変更不要**（技術的制約パラメータ）
- 極端な生産性ショックを許容する場合: phiMax を 10.0 に拡大

---

### 採用・賃金調整 (Phase C7)

#### `sampleSizeR` / `sampleSizeN` (デフォルト: 0 / 0)

**経済学的意味:**
- 企業が労働者を雇用する際のサンプリング数
- 0 = 全求職者から選択（完全情報）
- N > 0 = N人からランダムサンプル（限定合理性）

**影響範囲:**
- 労働市場のマッチング効率
- 賃金決定プロセス

**設定ガイドライン:**
- **完全情報**: 0（デフォルト）
- **限定合理性**: 5-20
- **強い摩擦**: 1-3

---

#### `macroThresholdR` / `macroThresholdN` (デフォルト: 0.08 / 0.08)

**経済学的意味:**
- 賃金調整を発動させるマクロ失業率の閾値
- 失業率がこの値を超えると、企業が賃金を引き下げる

**影響範囲:**
- 賃金の下方硬直性
- 失業率と賃金の関係（フィリップス曲線）
- 景気変動の増幅度

**設定ガイドライン:**
- **強い下方硬直性**: 0.10-0.15（高い閾値）
- **柔軟な賃金**: 0.03-0.05（低い閾値）
- **R型とN型で差をつける**: 例 R=0.10, N=0.05

**実験例:**
```xml
<!-- 正規雇用の賃金は硬直的、非正規は柔軟 -->
<constructor-arg value="0.12" /> <!-- macroThresholdR -->
<constructor-arg value="0.05" /> <!-- macroThresholdN -->
```

---

#### `macroAdaptiveParameterR` / `macroAdaptiveParameterN` (デフォルト: 1.0 / 1.0)

**経済学的意味:**
- マクロ失業率に対する賃金調整の反応度
- 値が大きいほど、失業率変化に敏感に賃金が反応

**影響範囲:**
- 賃金調整速度
- 景気循環の振幅
- フィリップス曲線の傾き

**設定ガイドライン:**
- **鈍い反応**: 0.5-0.8
- **通常**: 1.0（デフォルト）
- **敏感な反応**: 1.5-2.0

---

#### `microAdaptiveParameterR` / `microAdaptiveParameterN` (デフォルト: 1.0 / 1.0)

**経済学的意味:**
- 企業レベル（ミクロ）の条件に対する賃金調整の反応度
- 企業の売上、利潤、在庫などに応じた賃金調整

**影響範囲:**
- 企業間の賃金格差
- ミクロレベルのショックの伝播

**設定ガイドライン:**
- **企業特性を重視**: 1.5-2.0
- **マクロ条件優先**: 0.5-0.8

---

## パラメータ間の関係性

### 1. 離職率と解雇率の相互作用

```
雇用流出 = 自主離職 + 解雇
```

**重要な関係:**
- 両方が高い → 労働市場の高流動性、高い摩擦的失業
- turnoverが低く、layoffRateが高い → 景気依存的な雇用変動
- 逆の場合 → 景気に鈍感、構造的な雇用変動

**推奨設定パターン:**
```xml
<!-- パターン1: 流動的労働市場 -->
<constructor-arg value="0.05" /> <!-- turnoverLaborRVal -->
<constructor-arg value="0.10" /> <!-- turnoverLaborNVal -->
<constructor-arg value="0.4" />  <!-- layoffRateRVal -->
<constructor-arg value="0.8" />  <!-- layoffRateNVal -->

<!-- パターン2: 硬直的労働市場 -->
<constructor-arg value="0.01" /> <!-- turnoverLaborRVal -->
<constructor-arg value="0.03" /> <!-- turnoverLaborNVal -->
<constructor-arg value="0.2" />  <!-- layoffRateRVal -->
<constructor-arg value="0.6" />  <!-- layoffRateNVal -->
```

---

### 2. CESパラメータの相互依存性

**δ（cesDelta）とρ（cesRho）の組み合わせ効果:**

| 設定 | δ | ρ | 経済的意味 |
|------|---|---|----------|
| R型重視・低代替性 | 0.7 | -0.5 | R型が不可欠、N型で代替困難 |
| 対等・高代替性 | 0.5 | 0.8 | 両タイプがほぼ完全代替 |
| N型重視・中代替性 | 0.3 | 0.5 | N型が主力、R型は補完的 |

**効率パラメータとの相互作用:**
```
効果的な重み = δ * A_R^ρ / [δ * A_R^ρ + (1-δ) * A_N^ρ]
```

A_Rを上げると、実質的にδが上昇したのと同様の効果。

---

### 3. 賃金調整パラメータの階層構造

```
賃金変化 = f(マクロ条件, ミクロ条件)
         = macroAdaptive * マクロギャップ + microAdaptive * ミクロギャップ
```

**バランスのパターン:**
- **マクロ重視型**: macroAdaptive=1.5, microAdaptive=0.5
- **ミクロ重視型**: macroAdaptive=0.5, microAdaptive=1.5
- **バランス型**: macroAdaptive=1.0, microAdaptive=1.0（デフォルト）

---

## シナリオ別設定例

### シナリオ1: 非正規雇用比率の上昇

**目的:** 1990年代以降の日本の労働市場変化を再現

**変更パラメータ:**
```xml
<!-- 初期比率を変更 -->
<constructor-arg value="0.55" /> <!-- laborTypeRatioR: 正規55%に低下 -->

<!-- 非正規の調整弁機能を強化 -->
<constructor-arg value="0.9" />  <!-- layoffRateNVal: 90%に上昇 -->

<!-- 非正規の離職率を上昇 -->
<constructor-arg value="0.08" /> <!-- turnoverLaborNVal: 8%に上昇 -->
```

**期待される結果:**
- 失業率の全体的な低下（調整弁として機能）
- 賃金格差の拡大
- 景気変動への労働市場の反応性向上

---

### シナリオ2: 雇用保護の強化

**目的:** 解雇規制の強化が労働市場に与える影響を分析

**変更パラメータ:**
```xml
<!-- 正規の解雇をさらに困難に -->
<constructor-arg value="0.15" /> <!-- layoffRateRVal: 15%に低下 -->

<!-- 非正規も若干保護 -->
<constructor-arg value="0.6" />  <!-- layoffRateNVal: 60%に低下 -->
```

**期待される結果:**
- 失業率の変動幅縮小（解雇が困難）
- 労働市場の硬直化
- 企業の採用抑制の可能性

---

### シナリオ3: 同一労働同一賃金政策

**目的:** 労働タイプ間の格差是正政策の効果を検証

**変更パラメータ:**
```xml
<!-- 離職率の格差を縮小 -->
<constructor-arg value="0.02" /> <!-- turnoverLaborRVal -->
<constructor-arg value="0.03" /> <!-- turnoverLaborNVal: 格差縮小 -->

<!-- 解雇率の格差を縮小 -->
<constructor-arg value="0.4" />  <!-- layoffRateRVal -->
<constructor-arg value="0.5" />  <!-- layoffRateNVal: 格差縮小 -->

<!-- CES効率を同等に（既にデフォルト）-->
<constructor-arg value="1.0" />  <!-- cesAR -->
<constructor-arg value="1.0" />  <!-- cesAN -->

<!-- 賃金調整閾値を統一 -->
<constructor-arg value="0.08" /> <!-- macroThresholdR -->
<constructor-arg value="0.08" /> <!-- macroThresholdN -->
```

**期待される結果:**
- 雇用形態間の格差縮小
- 労働市場の二重構造の弱体化
- 総失業率への影響（要検証）

---

### シナリオ4: 技術進歩のR型偏向

**目的:** スキル偏向的技術進歩（Skill-Biased Technical Change）の影響

**変更パラメータ:**
```xml
<!-- R型の生産性を段階的に上昇（時系列で変更） -->
<!-- 初期 -->
<constructor-arg value="1.0" />  <!-- cesAR -->
<constructor-arg value="1.0" />  <!-- cesAN -->

<!-- 10年後 -->
<constructor-arg value="1.3" />  <!-- cesAR: 30%上昇 -->
<constructor-arg value="1.0" />  <!-- cesAN: 据え置き -->

<!-- CESの重みも調整 -->
<constructor-arg value="0.6" />  <!-- cesDelta: R型重視 -->
```

**期待される結果:**
- R型賃金の相対的上昇
- 賃金格差の拡大
- 企業のR型労働需要増加

---

### シナリオ5: 労働市場の柔軟化

**目的:** 規制緩和による労働市場改革の効果

**変更パラメータ:**
```xml
<!-- 解雇を全体的に容易に -->
<constructor-arg value="0.5" />  <!-- layoffRateRVal: 50%に上昇 -->
<constructor-arg value="0.8" />  <!-- layoffRateNVal: 据え置き -->

<!-- 賃金調整を柔軟に -->
<constructor-arg value="0.05" /> <!-- macroThresholdR: 低下 -->
<constructor-arg value="0.03" /> <!-- macroThresholdN: 低下 -->

<!-- 賃金反応を敏感に -->
<constructor-arg value="1.5" />  <!-- macroAdaptiveParameterR -->
<constructor-arg value="1.5" />  <!-- macroAdaptiveParameterN -->
```

**期待される結果:**
- 賃金変動の拡大
- 失業率の短期的変動増加
- マッチング効率の向上（可能性）

---

### シナリオ6: 極端な二重構造（比較分析用）

**目的:** 二重構造を極端にして、メカニズムを明確化

**変更パラメータ:**
```xml
<!-- 初期比率を極端に -->
<constructor-arg value="0.4" />  <!-- laborTypeRatioR: 正規40% -->

<!-- 解雇率の格差を最大化 -->
<constructor-arg value="0.1" />  <!-- layoffRateRVal: ほぼ解雇なし -->
<constructor-arg value="0.95" /> <!-- layoffRateNVal: ほぼ全員解雇 -->

<!-- 離職率の格差も拡大 -->
<constructor-arg value="0.01" /> <!-- turnoverLaborRVal -->
<constructor-arg value="0.15" /> <!-- turnoverLaborNVal -->

<!-- R型を高生産性に -->
<constructor-arg value="1.5" />  <!-- cesAR -->
<constructor-arg value="1.0" />  <!-- cesAN -->
```

**期待される結果:**
- 明確な労働市場の分断
- 賃金・雇用安定性の大きな格差
- 景気変動への非対称的反応

---

## 参考文献

### 理論的背景

1. **二重労働市場理論**
   - Doeringer, P. B., & Piore, M. J. (1971). *Internal Labor Markets and Manpower Analysis*. Lexington Books.

2. **CES生産関数**
   - Arrow, K. J., Chenery, H. B., Minhas, B. S., & Solow, R. M. (1961). "Capital-Labor Substitution and Economic Efficiency." *Review of Economics and Statistics*, 43(3), 225-250.

3. **日本の労働市場**
   - Kambayashi, R., & Kato, T. (2017). "Long-term employment and job security over the past 25 years: A comparative study of Japan and the United States." *ILR Review*, 70(2), 359-394.

### 実証研究

4. **非正規雇用の役割**
   - Asano, H., Ito, T., & Kawaguchi, D. (2013). "Why has the fraction of contingent workers increased? A case study of Japan." *Scottish Journal of Political Economy*, 60(4), 360-389.

5. **雇用調整**
   - Kuroda, S., & Yamamoto, I. (2013). "Impact of employment adjustment on wages: Evidence from Japanese firm-level panel data." *Journal of the Japanese and International Economies*, 30, 49-67.

### モデル構築

6. **元モデル（Caiani et al.）**
   - Caiani, A., Godin, A., Caverzasi, E., Gallegati, M., Kinsella, S., & Stiglitz, J. E. (2016). "Agent based-stock flow consistent macroeconomics: Towards a benchmark model." *Journal of Economic Dynamics and Control*, 69, 375-408.

---

## 補足: パラメータ変更時のチェックリスト

パラメータを変更する前に、以下を確認してください：

### ✅ 変更前チェック

- [ ] 変更の目的（シナリオ、仮説）が明確か
- [ ] 変更するパラメータの経済学的意味を理解しているか
- [ ] 他のパラメータとの関係性を確認したか
- [ ] デフォルト値を記録したか（元に戻せるように）

### ✅ 変更後チェック

- [ ] パラメータ値が推奨範囲内か
- [ ] 論理的整合性（例: layoffRateN >= layoffRateR）が保たれているか
- [ ] シミュレーションが正常に実行されるか
- [ ] 結果が経済学的に解釈可能か
- [ ] 極端な結果の場合、パラメータ設定を再検討

### ✅ 分析時チェック

- [ ] ベースラインケースとの比較を行ったか
- [ ] 感度分析を実施したか（複数のパラメータ値で試す）
- [ ] 結果の頑健性を確認したか
- [ ] 結果を文書化したか

---

**最終更新:** 2025年（二重労働市場パラメータ統合時）
**関連ファイル:** `parameters.xml`, `modelBenchmark_full.xml`
