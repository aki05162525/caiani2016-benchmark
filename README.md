# Modernized Benchmark Model (Caiani et al., 2016) - Gradle Edition

[![Java](https://img.shields.io/badge/Java-8-blue)]()
[![Gradle](https://img.shields.io/badge/Build-Gradle-02303A)]()

このリポジトリは、Caiani et al. (2016) によるマクロ経済エージェントベースモデル（SFC-ABM）のベンチマークモデルを、現代的なビルド環境（Gradle）で再構築したものです。

オリジナルのソースコードに見られる「複雑な依存関係（Dependency Hell）」や「ローカルパス依存」の問題を解消しており、**JDK さえあればコマンド一発でシミュレーションを実行可能**です。

## 📄 元論文

> Caiani, A., Godin, A., Caverzasi, E., Gallegati, M., Kinsella, S., & Stiglitz, J. E. (2016). **Agent based-stock flow consistent macroeconomics: Towards a benchmark model.** _Journal of Economic Dynamics and Control_, 69, 375-408.

---

## ✨ このプロジェクトの特徴

1.  **環境構築不要:** 面倒な `CLASSPATH` の設定や `settings.xml` の編集は一切不要です。
2.  **依存ライブラリの同梱:** Maven Central から入手不可能になった古いライブラリ（`pf-joi`, `idw` など）を `libs/` ディレクトリに救出し、自動参照するように設定しています。
3.  **Gradle Wrapper 対応:** Gradle をインストールしていなくても、同梱のスクリプトでビルド・実行が可能です。
4.  **モダンな構成:** `jabm`, `jmab`, `benchmark` の 3 つのモジュールを 1 つのプロジェクト（モノレポ）として統合しています。

## 🚀 クイックスタート

### 前提条件

- **IntelliJ IDEA** (Community Edition で OK)
- **Java JDK 8**（依存ライブラリの互換性のため、Java 8を推奨）
  - ※ **事前インストールは不要です。** IntelliJ IDEA の機能を使って、セットアップ中に自動ダウンロードできます。

### 実行方法

#### 方法 1: IntelliJ IDEA で開く（推奨・最も簡単）

コマンド操作や Java の環境構築は一切不要です。

1. **プロジェクトを開く**

   - IntelliJ IDEA を起動します。
   - `File` → `Open` を選択します。
   - クローンしたフォルダ（`caiani2016-benchmark`）を選択して開きます。

2. **Gradle の同期 (JDK の設定)**

   - IntelliJ が自動的に Gradle プロジェクトとして認識します。
   - **もし JDK（Java）が設定されていない場合:**
     - 画面上部にバーが出たら `Load Gradle Project` をクリックします。
     - または、設定画面 (`Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle`) を開きます。
     - `Gradle JVM` の欄で、**`Download JDK...`** を選択し、バージョン `1.8` (Java 8) を選んでダウンロードしてください。
   - 同期完了まで待ちます（初回は数分かかります）。

3. **シミュレーションを実行**

   - 右側のサイドバーにある **`Gradle`** タブを開きます。
   - `caiani2016-benchmark` → `benchmark` → `Tasks` → `application` を展開します。
   - **`runLight`** をダブルクリック（10 ラウンドの高速テスト用）。
   - または **`runFull`** をダブルクリック（1000 ラウンドの完全シミュレーション用）。

4. **結果を確認**
   - 実行が完了すると、以下のフォルダに CSV ファイルが生成されます。
   - `benchmark/benchmark/data/`

#### 方法 2: コマンドラインから実行

```bash
# Mac/Linuxの場合
./gradlew :benchmark:runLight   # 10ラウンド（動作確認用）
./gradlew :benchmark:runFull    # 1000ラウンド（本格分析用）

# Windowsの場合
gradlew.bat :benchmark:runLight
gradlew.bat :benchmark:runFull
```

#### 方法 3: モンテカルロシミュレーション（複数シード実行）

異なる乱数シードで複数回シミュレーションを実行する場合：

```bash
# シード 1〜5 で実行（fullモード）
./scripts/run_monte_carlo.sh 1 5

# シード 1〜10 で実行（lightモード）
./scripts/run_monte_carlo.sh 1 10 light

# シード 3 だけを実行
./scripts/run_monte_carlo.sh 3 3
```

出力は `data/seed_1/`, `data/seed_2/` ... のようにシード値ごとに分離されます。

---

## 📊 タスクの違い

| タスク       | シミュレーション期間 | 実行時間   | 用途                       |
| ------------ | -------------------- | ---------- | -------------------------- |
| **runLight** | 10 ラウンド          | 数秒〜数分 | 動作確認・パラメータテスト |
| **runFull**  | 1000 ラウンド        | 数十分〜   | 論文用データ生成・本格分析 |

**初めての方は `runLight` から試してください！**

---

## 📁 プロジェクト構成

```
caiani2016-benchmark/
├── jabm/           # Java Agent-Based Modelling 基盤フレームワーク
├── jmab/           # Macro-ABM 経済拡張フレームワーク
├── benchmark/      # Caiani 2016 モデル本体
│   └── benchmark/
│       ├── src/    # エージェント実装（企業・銀行・家計など）
│       ├── Model/  # XMLによるシミュレーション設定
│       └── data/   # 出力CSVファイル（実行後に生成）
├── scripts/        # ユーティリティスクリプト
│   └── run_monte_carlo.sh  # モンテカルロシミュレーション用
├── libs/           # レガシー依存ライブラリ（7個のJAR）
├── build.gradle    # ビルド設定
└── settings.gradle # プロジェクト構成定義
```

---

## 🔧 パラメータ調整

シミュレーションのパラメータは `benchmark/benchmark/Model/modelBenchmark_light.xml` (または `_full.xml`) で変更できます。

例:

- エージェント数（企業・銀行・家計）
- 税率・金利
- 政府支出
- 企業の投資行動パラメータ
