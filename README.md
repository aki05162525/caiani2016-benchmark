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
- **Java JDK 8 以上** (JDK 11, 17 での動作も確認済み)

### 実行方法

1. **リポジトリをクローン:**
   ```bash
   git clone [https://github.com/YOUR_ACCOUNT/modern-caiani-benchmark.git](https://github.com/YOUR_ACCOUNT/modern-caiani-benchmark.git)
   cd modern-caiani-benchmark