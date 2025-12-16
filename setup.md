# benchmark プロジェクト環境構築手順

このドキュメントは、Caiani & Godin (2016) ベンチマークモデルの実行環境を構築するための手順を説明します。

## プロジェクト構成

このプロジェクトは3つのコンポーネントで構成されています：

```
benchmark (アプリケーション)
    ↓ 依存
jmab (マクロ経済モデリングフレームワーク)
    ↓ 依存
jabm (汎用ABMフレームワーク)
```

## 前提条件

### 必須環境

1. **Java Development Kit (JDK)**
    - Java 6以上が必要（Java 1.6.0_35 または 1.7.0_75 でテスト済み）
    - Mac OSの場合、Oracle版Javaが必要
    - 推奨: Java 8以上

2. **ビルドツール**
    - **Maven 3.x** - jabmプロジェクトのビルドに必要
    - **IntelliJ IDEA** (推奨) - 推奨される開発環境
        - Community Edition または Ultimate Edition

3. **リポジトリ**
    - jabm: Java Agent-Based Modelling toolkit
    - jmab: Java Macro Agent-Based toolkit
    - benchmark: ベンチマークアプリケーション本体

## 環境構築手順

### ステップ1: Java環境の確認

```bash
# Javaバージョンの確認
java -version
javac -version

# JAVA_HOME環境変数の確認
echo $JAVA_HOME
```

**期待される出力例:**
```
java version "1.8.0_xxx" or higher
```

### ステップ2: Mavenのインストール確認

```bash
# Mavenバージョンの確認
mvn -version
```

Maven がインストールされていない場合は、以下のコマンドでインストールしてください：

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install maven

# macOS (Homebrew)
brew install maven

# Windows
# https://maven.apache.org/download.cgi からダウンロード
```

### ステップ3: JABMプロジェクトのビルド

jabmは汎用的なエージェントベースモデリングフレームワークです。

**Option A: IntelliJ IDEA を使用する場合（推奨）**

1. IntelliJ IDEA を起動
2. `File` → `Open` → `/home/aki05162525/project/dev/seminar/caiani2016-benchmark/jabm` を選択
3. Mavenプロジェクトとして認識されるので、右下に表示される「Load Maven Project」をクリック
4. Maven ツールウィンドウを開く（`View` → `Tool Windows` → `Maven`）
5. `jabm` → `Lifecycle` → `install` をダブルクリック
    - または、テストをスキップする場合は `Profiles` で `skipTests` にチェック

**Option B: コマンドラインでビルドする場合**

```bash
cd /home/aki05162525/project/dev/seminar/caiani2016-benchmark/jabm

# Mavenビルドの実行
mvn clean install

# または、テストをスキップする場合
mvn clean install -DskipTests
```

**ビルド成功の確認:**
- `BUILD SUCCESS` メッセージが表示される
- `~/.m2/repository/net/sourceforge/jabm/` 以下にjabmのjarファイルが配置される

### ステップ4: IntelliJ IDEA でプロジェクトを開く

すべてのプロジェクト（jabm、jmab、benchmark）を1つのIntelliJプロジェクトとして開きます。

1. IntelliJ IDEA を起動
2. `File` → `Open` → `/home/aki05162525/project/dev/seminar/caiani2016-benchmark` を選択
3. プロジェクトが開かれたら、IntelliJが自動的に構造を認識します

**または、新規プロジェクトとして作成する場合:**

1. `File` → `New` → `Project from Existing Sources...`
2. `/home/aki05162525/project/dev/seminar/caiani2016-benchmark` を選択
3. 「Create project from existing sources」を選択して Next
4. デフォルト設定のまま進めて Finish

### ステップ5: IntelliJ IDEA でプロジェクト構造を設定

プロジェクトを開いた後、モジュールと依存関係を設定します。

1. **プロジェクト構造を開く**
    - `File` → `Project Structure...` (または `Ctrl+Alt+Shift+S`)

2. **Project 設定**
    - `Project` タブを選択
    - Project SDK: Java 8以上を選択
    - Project language level: 8 - Lambdas, type annotations etc. を選択

3. **Modules 設定**

   **jabm モジュール:**
    - 左ペインで `Modules` → `jabm` を選択
    - `Sources` タブで `jabm/jabm/src/main/java` を Sources としてマーク
    - Mavenプロジェクトなので、IntelliJが自動的に依存関係を解決します

   **jmab モジュール:**
    - `Modules` で新規モジュールを追加（まだなければ）
    - `+` ボタン → `Import Module` → `jmab` ディレクトリを選択
    - `Sources` タブで `jmab/src` を Sources としてマーク
    - `Dependencies` タブで以下を追加：
        - `+` → `Module Dependency` → `jabm` を選択
        - `+` → `JARs or directories` → `jmab/lib` 内のすべてのjarファイルを追加

   **benchmark モジュール:**
    - `Modules` で新規モジュールを追加（まだなければ）
    - `+` ボタン → `Import Module` → `benchmark/benchmark` ディレクトリを選択
    - `Sources` タブで `benchmark/benchmark/src` を Sources としてマーク
    - `Dependencies` タブで以下を追加：
        - `+` → `Module Dependency` → `jmab` を選択
        - `+` → `Module Dependency` → `jabm` を選択
        - `+` → `JARs or directories` → `benchmark/benchmark/lib` 内のすべてのjarファイルを追加

4. **Apply** → **OK** をクリック

5. **プロジェクトをビルド**
    - `Build` → `Build Project` (または `Ctrl+F9`)
    - エラーがないことを確認

## Main クラスの実行

### 実行に必要な設定

`benchmark.Main` クラスは `SimulationManager` を呼び出します。実行時に以下のJVM引数で設定ファイルを指定する必要があります：

```
-Djabm.config=Model/mainBenchmark_light.xml
```

または

```
-Djabm.config=Model/mainBenchmark_full.xml
```

### 利用可能な設定ファイル

- `Model/mainBenchmark_light.xml` - 軽量版ベンチマーク
- `Model/mainBenchmark_full.xml` - 完全版ベンチマーク
- `Model/mainSerialization.xml` - シリアライゼーションテスト用
- `Model/modelBenchmark_light.xml` - モデル軽量版
- `Model/modelBenchmark_full.xml` - モデル完全版

### IntelliJ IDEA での実行方法（推奨）

1. **Run Configuration の作成**
    - `Run` → `Edit Configurations...` を開く
    - 左上の `+` ボタン → `Application` を選択
    - 以下のように設定：
        - **Name:** `Benchmark Main (Light)`
        - **Main class:** `benchmark.Main`
            - 右側の `...` ボタンをクリックして `benchmark.Main` を検索
        - **VM options:** `-Djabm.config=Model/mainBenchmark_light.xml`
        - **Working directory:** `/home/aki05162525/project/dev/seminar/caiani2016-benchmark/benchmark/benchmark`
            - または `$MODULE_WORKING_DIR$` マクロを使用
        - **Use classpath of module:** `benchmark` を選択
    - `Apply` → `OK`

2. **実行**
    - ツールバーの Run Configuration ドロップダウンから `Benchmark Main (Light)` を選択
    - 緑色の実行ボタン（▶）をクリック、または `Shift+F10`

3. **別の設定ファイルで実行する場合**
    - 同様に新しい Run Configuration を作成
    - VM options を変更：
        - Full版: `-Djabm.config=Model/mainBenchmark_full.xml`
        - Serialization版: `-Djabm.config=Model/mainSerialization.xml`

**クイック実行（初回）:**
1. `benchmark/benchmark/src/benchmark/Main.java` を開く
2. エディタ内で右クリック → `Run 'Main.main()'`
3. その後、上記の手順で Run Configuration を編集

### コマンドラインでの実行方法

```bash
cd /home/aki05162525/project/dev/seminar/caiani2016-benchmark/benchmark/benchmark

# クラスパスの設定
CLASSPATH="bin:lib/*:../../jmab/bin:../../jabm/jabm/target/classes"

# 実行
java -cp $CLASSPATH \
  -Djabm.config=Model/mainBenchmark_light.xml \
  benchmark.Main
```

## トラブルシューティング

### Java バージョンの問題

**エラー:** `Unsupported class file major version`

**解決策:** Java 8以上を使用してください。

```bash
# Javaバージョンの切り替え（複数インストールされている場合）
sudo update-alternatives --config java
```

### クラスパスの問題

**エラー:** `ClassNotFoundException: net.sourceforge.jabm.SimulationManager`

**解決策:** jabm がビルドされ、クラスパスに含まれているか確認してください。

```bash
# jabmのビルド確認
ls ~/.m2/repository/net/sourceforge/jabm/

# または
ls jabm/jabm/target/classes/net/sourceforge/jabm/
```

### 依存ライブラリの問題

**エラー:** Spring関連のエラー

**解決策:** `benchmark/benchmark/lib/` 以下のすべてのjarファイルがクラスパスに含まれているか確認してください。

### 設定ファイルが見つからない

**エラー:** `FileNotFoundException: Model/mainBenchmark_light.xml`

**解決策:**
- IntelliJの場合: Run Configuration の Working directory が正しく設定されているか確認
    - 正しい値: `/home/aki05162525/project/dev/seminar/caiani2016-benchmark/benchmark/benchmark`
- コマンドラインの場合: 作業ディレクトリを `benchmark/benchmark/` に設定

```bash
cd /home/aki05162525/project/dev/seminar/caiani2016-benchmark/benchmark/benchmark
```

### IntelliJ で Module not found エラー

**エラー:** モジュールが見つからない、または依存関係が解決できない

**解決策:**
1. `File` → `Invalidate Caches...` → `Invalidate and Restart`
2. Maven プロジェクト（jabm）を再インポート:
    - Maven ツールウィンドウを開く
    - 更新ボタン（Reimport All Maven Projects）をクリック
3. プロジェクトを再ビルド: `Build` → `Rebuild Project`

## 依存ライブラリ一覧

benchmark/benchmark/lib/ に含まれる主要なライブラリ：

- **Spring Framework 3.2.3** - 依存性注入
- **Apache Commons Math 3.2** - 数値計算
- **Colt 1.2.0** - 高性能科学計算
- **JFreeChart 1.0.14** - グラフ描画
- **JUNG 2.0.1** - グラフ理論・ネットワーク分析
- **Log4j 1.2.16** - ロギング

## 参考情報

### プロジェクト構造

```
caiani2016-benchmark/
├── jabm/                      # Java Agent-Based Modelling toolkit
│   ├── jabm/                  # コアライブラリ（Mavenプロジェクト）
│   └── jabm-examples/         # サンプル
├── jmab/                      # Java Macro Agent-Based toolkit
│   ├── src/
│   └── lib/
└── benchmark/                 # ベンチマークアプリケーション
    └── benchmark/             # メインプロジェクト
        ├── src/
        │   └── benchmark/
        │       └── Main.java
        ├── Model/             # 設定ファイル
        ├── lib/               # 依存ライブラリ
        └── data/              # データ出力先
```

### 論文情報

このプロジェクトは以下の論文のベンチマークモデルを実装しています：

- Caiani, A., Godin, A., Caverzasi, E., Gallegati, M., Kinsella, S., & Stiglitz, J. E. (2016). Agent based-stock flow consistent macroeconomics: Towards a benchmark model. Journal of Economic Dynamics and Control, 69, 375-408.

### 関連リンク

- JABM: Java Agent-Based Modelling toolkit
- JMAB: Stock-Flow Consistent Agent-Based Macroeconomic framework
- Spring Framework: https://spring.io/

## 次のステップ

環境構築が完了したら：

1. `mainBenchmark_light.xml` で動作確認
2. 実行結果の確認（data/ディレクトリを確認）
3. パラメータの調整（Model/*.xml ファイルを編集）
4. 本格的なシミュレーション実行（`mainBenchmark_full.xml`）
