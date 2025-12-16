# Modernized Benchmark Model (Caiani et al., 2016) - Gradle Edition

[![Java](https://img.shields.io/badge/Java-8%2B-blue)]()
[![Gradle](https://img.shields.io/badge/Build-Gradle-02303A)]()

このリポジトリは、Caiani et al. (2016) によるマクロ経済エージェントベースモデル（SFC-ABM）のベンチマークモデルを、現代的なビルド環境（Gradle）で再構築したものです。

オリジナルのソースコードに見られる「複雑な依存関係（Dependency Hell）」や「ローカルパス依存」の問題を解消しており、**JDKさえあればコマンド一発でシミュレーションを実行可能**です。

## 📄 元論文
> Caiani, A., Godin, A., Caverzasi, E., Gallegati, M., Kinsella, S., & Stiglitz, J. E. (2016). **Agent based-stock flow consistent macroeconomics: Towards a benchmark model.** *Journal of Economic Dynamics and Control*, 69, 375-408.

---

## ✨ このプロジェクトの特徴

1.  **環境構築不要:** 面倒な `CLASSPATH` の設定や `settings.xml` の編集は一切不要です。
2.  **依存ライブラリの同梱:** Maven Central から入手不可能になった古いライブラリ（`pf-joi`, `idw` など）を `libs/` ディレクトリに救出し、自動参照するように設定しています。
3.  **Gradle Wrapper 対応:** Gradle をインストールしていなくても、同梱のスクリプトでビルド・実行が可能です。
4.  **モダンな構成:** `jabm`, `jmab`, `benchmark` の3つのモジュールを1つのプロジェクト（モノレポ）として統合しています。

## 🚀 クイックスタート

### 前提条件
- **Java JDK 8以上**
- **IntelliJ IDEA** (Community Edition でOK)

### 実行方法

#### 方法1: IntelliJで開く（推奨・最も簡単）

1. **プロジェクトを開く**
   - IntelliJ IDEA を起動
   - `File` → `Open` を選択
   - このフォルダ（`caiani2016-benchmark`）を選択して開く

2. **Gradleの同期を待つ**
   - IntelliJが自動的にGradleプロジェクトとして認識します
   - 右下にプログレスバーが表示されるので完了まで待つ（初回は数分）

3. **シミュレーションを実行**
   - 右側の `Gradle` タブを開く
   - `modern-caiani-benchmark` → `benchmark` → `Tasks` → `application` を展開
   - **`runLight`** をダブルクリック（10ラウンドの高速テスト）
   - または **`runFull`** をダブルクリック（1000ラウンドの完全版）

4. **結果を確認**
   - 実行が完了すると、`benchmark/benchmark/data/` フォルダにCSVファイルが生成されます
   - GDP、失業率、投資などのマクロ指標が時系列データとして出力されます

#### 方法2: コマンドラインから実行

```bash
# Windowsの場合
gradlew.bat runLight

# Mac/Linuxの場合
./gradlew runLight
```

---

## 📊 タスクの違い

| タスク | シミュレーション期間 | 実行時間 | 用途 |
|--------|---------------------|---------|------|
| **runLight** | 10ラウンド | 数秒〜数分 | 動作確認・パラメータテスト |
| **runFull** | 1000ラウンド | 数十分〜 | 論文用データ生成・本格分析 |

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

詳細は `CLAUDE.md` の「Parameter Modification」セクションを参照してください。
