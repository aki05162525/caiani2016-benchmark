# MKT_LABOR → MKT_LABOR_R / MKT_LABOR_N 段階的移行ガイド

## Phase A1 実装完了事項

### 1. StaticValues.java への市場ID追加 ✓

**ファイル**: `benchmark/benchmark/src/benchmark/StaticValues.java`

**追加内容**:
```java
// Line 211: 既存（レガシー）
public static int MKT_LABOR=4; // Legacy: will be gradually replaced by MKT_LABOR_R and MKT_LABOR_N

// Line 215-217: 新規追加
// Dual labor market IDs (Phase A1: R=Regular, N=Non-regular)
public static int MKT_LABOR_R=7; // Regular labor market
public static int MKT_LABOR_N=8; // Non-regular labor market
```

**方針**:
- 既存の `MKT_LABOR=4` は**当面維持**（段階的移行のため）
- 新しい市場ID `MKT_LABOR_R=7`, `MKT_LABOR_N=8` を追加
- コメントで新旧関係を明記

---

## 2. MKT_LABOR 参照箇所の全体像

### 2.1 Javaソースファイル（8クラス）

| ファイル | 参照箇所数 | 主な用途 |
|---------|-----------|---------|
| `Households.java` | 3 | 労働供給（setActive, onAgentArrival） |
| `ConsumptionFirm.java` | 6 | 労働需要（setActive, payWages, onAgentArrival） |
| `CapitalFirm.java` | 6 | 労働需要（setActive, payWages, onAgentArrival） |
| `ConsumptionFirmWagesEnd.java` | 5 | 労働需要・賃金支払い |
| `CapitalFirmWagesEnd.java` | 5 | 労働需要・賃金支払い |
| `Government.java` | 6 | 政府労働需要 |
| `Government2WagesEnd.java` | 3 | 政府労働需要・賃金支払い |
| `GovernmentAntiCyclical.java` | 4 | 反循環的政府労働需要 |

**合計**: 38箇所

### 2.2 XMLファイル（5ファイル）

- `modelBenchmark_full.xml`
- `modelBenchmark_light.xml`
- `modelSerialization.xml`
- `Model/ExperimentsExpectations/*.xml` (4ファイル)

**用途**: 市場設定・シミュレーション設定

---

## 3. 詳細：使用パターン別の参照箇所

### 3.1 労働供給側（Households.java）

| 行番号 | コンテキスト | 用途 |
|-------|-------------|------|
| 152 | `case StaticValues.MKT_LABOR:` | onAgentArrivalでのマッチング処理 |
| 208 | `this.setActive(true, StaticValues.MKT_LABOR);` | 失業時に労働市場へ参加 |
| 597 | `this.setActive(active,StaticValues.MKT_LABOR);` | setLaborActiveメソッド |

**Phase A2/A3での対応**:
- Households に `labor_type` 属性を追加
- labor_type に応じて `MKT_LABOR_R` または `MKT_LABOR_N` へ参加

---

### 3.2 労働需要側（企業）

#### ConsumptionFirm.java

| 行番号 | コンテキスト | 用途 |
|-------|-------------|------|
| 239 | `case StaticValues.MKT_LABOR:` | onAgentArrival（採用処理） |
| 324 | `this.setActive(false, StaticValues.MKT_LABOR);` | 求人終了 |
| 334 | `this.setActive(true, StaticValues.MKT_LABOR);` | 求人開始 |
| 353 | `payWages(deposit,StaticValues.MKT_LABOR);` | 賃金支払い |
| 722 | `case StaticValues.MKT_LABOR:` | getAgentList |
| 1172 | `this.setActive(active, StaticValues.MKT_LABOR);` | setLaborDemandActive |

**Phase A4/B2での対応**:
- `laborDemand` を `laborDemandR` / `laborDemandN` に分離
- 求人がある方の市場（複数可）に `setActive(true, MKT_LABOR_{R/N})`
- `payWages` の支払不能時再求人をタイプ別に

#### CapitalFirm.java

| 行番号 | コンテキスト | 用途 |
|-------|-------------|------|
| 275 | `case StaticValues.MKT_LABOR:` | onAgentArrival |
| 441 | `this.setActive(false, StaticValues.MKT_LABOR);` | 求人終了 |
| 452 | `this.setActive(true, StaticValues.MKT_LABOR);` | 求人開始 |
| 471 | `payWages(deposit,StaticValues.MKT_LABOR);` | 賃金支払い |
| 589 | `case StaticValues.MKT_LABOR:` | getAgentList |
| 1062 | `this.setActive(active, StaticValues.MKT_LABOR);` | setLaborDemandActive |

**対応**: ConsumptionFirm と同様

#### ConsumptionFirmWagesEnd.java

| 行番号 | コンテキスト | 用途 |
|-------|-------------|------|
| 148 | `employee.getPayableStock(StaticValues.MKT_LABOR)` | 賃金支払い処理 |
| 167 | `employee.getPayableStock(StaticValues.MKT_LABOR)` | 賃金支払い処理（支払不能時） |
| 205 | `this.setActive(false, StaticValues.MKT_LABOR);` | 求人終了 |
| 211 | `this.setActive(true, StaticValues.MKT_LABOR);` | 求人開始 |

**対応**: Phase B2.4で対応

#### CapitalFirmWagesEnd.java

| 行番号 | コンテキスト | 用途 |
|-------|-------------|------|
| 145 | `employee.getPayableStock(StaticValues.MKT_LABOR)` | 賃金支払い処理 |
| 162 | `employee.getPayableStock(StaticValues.MKT_LABOR)` | 賃金支払い処理（支払不能時） |
| 196 | `this.setActive(false, StaticValues.MKT_LABOR);` | 求人終了 |
| 208 | `this.setActive(true, StaticValues.MKT_LABOR);` | 求人開始 |

**対応**: Phase B2.4で対応

---

### 3.3 政府系

#### Government.java

| 行番号 | コンテキスト | 用途 |
|-------|-------------|------|
| 164 | `case StaticValues.MKT_LABOR:` | onAgentArrival（Random Robin mixer） |
| 266 | `this.setActive(true, StaticValues.MKT_LABOR);` | 求人開始 |
| 269 | `this.setActive(false, StaticValues.MKT_LABOR);` | 求人終了 |
| 282 | `payWages(deposit,StaticValues.MKT_LABOR);` | 賃金支払い |
| 324 | `employee.setActive(true, StaticValues.MKT_LABOR);` | 解雇時の再求職 |
| 626 | `this.setActive(active, StaticValues.MKT_LABOR);` | setLaborDemandActive |

**Phase B2.2での対応**:
- 政府雇用は**全てR（正規）労働市場**から採用（推奨方針）
- `MKT_LABOR_R` のみに `setActive`

#### Government2WagesEnd.java

| 行番号 | コンテキスト | 用途 |
|-------|-------------|------|
| 79 | `this.setActive(true, StaticValues.MKT_LABOR);` | 求人開始 |
| 82 | `this.setActive(false, StaticValues.MKT_LABOR);` | 求人終了 |
| 98 | `payWages(deposit,StaticValues.MKT_LABOR);` | 賃金支払い |

**対応**: Phase B2.4で対応

#### GovernmentAntiCyclical.java

| 行番号 | コンテキスト | 用途 |
|-------|-------------|------|
| 132 | `unemployed.getPayableStock(StaticValues.MKT_LABOR)` | 失業給付支払い |
| 158 | `this.setActive(true, StaticValues.MKT_LABOR);` | 求人開始 |
| 161 | `this.setActive(false, StaticValues.MKT_LABOR);` | 求人終了 |
| 177 | `payWages(deposit,StaticValues.MKT_LABOR);` | 賃金支払い |

**対応**: Phase B2（企業と同様）+ C8（失業給付の加重平均化）

---

## 4. 段階的置換戦略（最小経路アプローチ）

### Phase A（市場分離の土台）

#### A2/A3: 労働供給側の分離
**対象**: `Households.java` (3箇所)

**変更内容**:
1. `labor_type` 属性を追加（初期化時に R or N を割当）
2. 行208, 597: `labor_type` に応じて `MKT_LABOR_R` または `MKT_LABOR_N` に `setActive`
3. 行152: `case` 文を2つに分離（`MKT_LABOR_R` / `MKT_LABOR_N`）

#### A4: 企業の採用・再求人をタイプ対応
**対象**: `AbstractFirm.java`（存在確認必要）または各企業クラス

**変更内容**:
1. `laborDemand` → `laborDemandR` / `laborDemandN`
2. `addEmployee` で worker の `labor_type` を確認し、対応する需要を減らす
3. `payWages` の支払不能時再求人を type 別に

### Phase B（雇用調整のタイプ別化）

#### B2: computeLaborDemand のタイプ別化
**対象**: 7クラス全て

**変更内容**:
- turnover, layoff, vacancy をタイプ別に
- `setActive` を両市場（必要に応じて）に

#### B2.2: 政府雇用のR固定化
**対象**: `Government.java` (6箇所)

**変更内容**:
- 全ての `MKT_LABOR` を `MKT_LABOR_R` に置換（政府雇用=R のみ）

### Phase C（CES統合後の最終調整）

**対象**: 企業クラスの生産・価格・賃金処理

**変更内容**:
- CES実効労働に基づく生産
- 2系列期待賃金に基づく価格
- タイプ別賃金更新

---

## 5. XMLファイルの対応（Phase A後半〜B）

### 必要な変更

1. **市場定義セクション**:
   - `MKT_LABOR` の設定を `MKT_LABOR_R` / `MKT_LABOR_N` に分離
   - または両市場を並列定義

2. **エージェント設定セクション**:
   - Households の初期化時に `labor_type` パラメータを追加
   - 企業の初期化時に R/N 雇用比率を設定（オプション）

### 対象ファイル
- `modelBenchmark_full.xml`
- `modelBenchmark_light.xml`
- その他実験用XMLファイル

**推奨タイミング**: Phase A4完了後〜Phase B開始前

---

## 6. 実装チェックリスト

### Phase A1 ✓
- [x] StaticValues.java に MKT_LABOR_R, MKT_LABOR_N を追加
- [x] 参照箇所を全て洗い出し
- [x] 移行戦略を文書化

### Phase A2 ✓
- [x] Households に labor_type 属性を追加（int型、LABOR_TYPE_R=0 / LABOR_TYPE_N=1）
- [x] getter/setter、シリアライゼーション対応完了
- [x] Households の労働市場参加をタイプ別に（3箇所）
  - computeWage() (行210-212)
  - setLaborActive() (行618-620)
  - getPayableStock() (行155-157)
- [x] SFCSSMacroAgentInitialiser で初期化ロジック実装
- [x] XML設定ファイルに laborTypeRatioR パラメータ追加（デフォルト0.65）

### Phase A3 ✓
- [x] TIC_LABORSUPPLY イベントフローを確認（computeWage経由で正しく動作）
- [x] 家計側の労働供給ロジック完了（常に自分のタイプ市場に参加）
- [x] 追加調整不要（家計側は完全実装済み）

**Phase A3 Note**: 解雇時の再求職については、企業・政府側の対応（Phase A4/B2）で以下を修正予定：
- Government.java:324 の `employee.setActive(true, MKT_LABOR)` を `emp.setLaborActive(true)` に変更
- 同様の箇所が他のクラスにもある可能性（Phase A4で対応）

### Phase A4（次のステップ）
- [ ] AbstractFirm（または各企業）の laborDemand をタイプ別に
- [ ] addEmployee で worker の labor_type を確認
- [ ] payWages の支払不能時再求人をタイプ別に
- [ ] fireAgent メソッドを setLaborActive 使用に修正

### Phase B2
- [ ] ConsumptionFirm.computeLaborDemand をタイプ別化（6箇所）
- [ ] CapitalFirm.computeLaborDemand をタイプ別化（6箇所）
- [ ] Government.computeLaborDemand を R 固定化（6箇所）
- [ ] 残り4クラス（WagesEnd系、GovernmentAntiCyclical）を対応

### Phase C
- [ ] CES実効労働・価格・賃金の統合
- [ ] 失業給付の加重平均化

### XML更新
- [ ] modelBenchmark_full.xml
- [ ] modelBenchmark_light.xml
- [ ] その他実験用XMLファイル

---

## 7. リスク管理

### 混線リスクが高い箇所

1. **Households の market 参加** (Households.java:208, 597)
   - リスク: `labor_type` が未定義の場合、デフォルトで `MKT_LABOR` に参加してしまう
   - 対策: 初期化時に全 Households に `labor_type` を必ず割当

2. **企業の求人** (各企業の setActive 箇所)
   - リスク: `laborDemandR/N` と市場ID の不一致（R需要があるのにN市場に出す）
   - 対策: setActive 直前で需要の正負を確認するバリデーション追加

3. **payWages の支払不能時再求人** (AbstractFirm または各企業)
   - リスク: 解雇した worker の type を取得し損ねる
   - 対策: fireAgent 時に解雇された worker の type をログし、即座に対応する laborDemand{R/N} を増やす

### テスト戦略

- **Phase A完了時**: 単一ステップでR家計とN家計が異なる市場に出ることを確認
- **Phase B完了時**: 企業が両市場で採用でき、type混線が起きないことを確認
- **Phase C完了時**: CES需要分解が `N_eff ≈ N^D` を満たすことを確認

---

## 8. 補足：AbstractFirm の確認が必要

タスク文書では `AbstractFirm.java` が言及されていますが、このコードベースに存在するか未確認です。次のステップで以下を確認：

- [ ] `jmab/src/jmab/agents/AbstractFirm.java` の存在確認
- [ ] もし存在する場合、laborDemand / addEmployee / payWages の実装状況を確認
- [ ] もし存在しない場合、各企業クラスで個別対応

**検索コマンド例**:
```bash
find . -name "AbstractFirm.java"
grep -r "class.*extends.*Firm" benchmark/benchmark/src/
```

---

このドキュメントは Phase A1 実装完了時点での記録であり、後続フェーズの進行に応じて更新されます。
