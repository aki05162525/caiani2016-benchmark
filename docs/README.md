# Caiani & Godin (2016) ベンチマークモデル - ドキュメント

このディレクトリには、モデルのコードリーディング結果とパラメータ修正ガイドが含まれています。

---

## ドキュメント一覧

### 1. 📘 包括的調査書
**ファイル:** [`model-analysis.md`](model-analysis.md)

**内容:**
- プロジェクト概要
- ディレクトリ構成
- 6種類のエージェントの詳細（役割、パラメータ、メソッド）
- 8つの市場メカニズム
- シミュレーション実行フロー（38ティック）
- 主要パラメータ設定
- 50種類以上のレポート出力
- パラメータ修正の詳細ガイド

**対象読者:**
- モデルの全体像を理解したい研究者
- エージェントの実装詳細を知りたい開発者
- 経済メカニズムを深く理解したい学生

**推奨使用場面:**
- 初回のモデル理解
- 論文執筆時の参照
- 詳細な実験設計

---

### 2. ⚡ パラメータ修正クイックリファレンス
**ファイル:** [`parameter-quick-reference.md`](parameter-quick-reference.md)

**内容:**
- 主要パラメータの一覧表
- デフォルト値と推奨実験範囲
- XMLファイルの修正方法
- 4つの推奨実験シナリオ
- トラブルシューティング
- 便利なコマンド集

**対象読者:**
- パラメータ修正を頻繁に行う研究者
- 実験を素早く実行したいユーザー

**推奨使用場面:**
- パラメータ修正時の素早い参照
- 実験シナリオの設計
- エラー解決

---

## 推奨学習順序

### 初めての方

1. **まず読む:** [`../README.md`](../benchmark/benchmark/README.md) - プロジェクト全体の概要
2. **環境構築:** [`../../setup-windows.md`](../../setup-windows.md) - Windows環境構築手順
3. **モデル理解:** [`model-analysis.md`](model-analysis.md) - セクション1-5を読む
4. **試しに実行:** IntelliJ IDEAで`Benchmark Main (Light)`を実行
5. **パラメータ修正:** [`parameter-quick-reference.md`](parameter-quick-reference.md) - 実験1を試す
6. **詳細分析:** [`model-analysis.md`](model-analysis.md) - セクション6-8を読む

### 研究目的の方

1. **モデル理解:** [`model-analysis.md`](model-analysis.md) - 全体を通読
2. **実験設計:** [`parameter-quick-reference.md`](parameter-quick-reference.md) - 推奨実験から選択
3. **カスタム実験:** [`model-analysis.md`](model-analysis.md) - セクション8を参照
4. **結果分析:** `data/` ディレクトリのCSV出力を確認

---

## 主要ファイルの場所

### 設定ファイル
```
../Model/
├── mainBenchmark_light.xml      # メイン設定（シミュレーション回数など）
├── modelBenchmark_light.xml     # モデル詳細設定（エージェント、パラメータ）
└── reports.xml                  # レポート定義
```

### ソースコード
```
../src/benchmark/
├── agents/                      # エージェント実装
│   ├── CapitalFirmWagesEnd.java
│   ├── ConsumptionFirmWagesEnd.java
│   ├── Bank.java
│   ├── HouseholdsWithDole.java
│   ├── GovernmentAntiCyclical.java
│   └── CentralBank.java
├── strategies/                  # 戦略実装
├── report/                      # レポート計算
└── StaticValues.java            # 定数定義
```

### 出力先
```
../data/                         # CSV形式の出力
```

---

## よくある質問

### Q1. パラメータを変更したい
**A:** [`parameter-quick-reference.md`](parameter-quick-reference.md)のセクション「主要パラメータ一覧」を参照してください。

### Q2. エージェントの動作を理解したい
**A:** [`model-analysis.md`](model-analysis.md)のセクション3「エージェントの種類と役割」を読んでください。

### Q3. 実験シナリオのアイデアが欲しい
**A:** [`parameter-quick-reference.md`](parameter-quick-reference.md)のセクション「推奨実験シナリオ」を参照してください。

### Q4. どのレポートを見ればいい？
**A:** [`model-analysis.md`](model-analysis.md)のセクション7「レポート出力」に全50種類のレポートが記載されています。

主要なもの:
- `nominalGDPCSVReport`: GDP
- `unemploymentCSVReport`: 失業率
- `nominalInvestmentCSVReport`: 投資
- `microBankCreditCSVReport`: 信用供給

### Q5. シミュレーションがエラーで止まる
**A:** [`parameter-quick-reference.md`](parameter-quick-reference.md)のセクション「トラブルシューティング」を確認してください。

---

## コマンドチートシート

### パラメータ検索
```bash
# 失業保険パラメータ
grep 'unemploymentBenefit' ../Model/modelBenchmark_light.xml

# 銀行リスク回避度
grep 'riskAversion' ../Model/modelBenchmark_light.xml
```

### 実行
```bash
# IntelliJ IDEAから
Run Configuration: Benchmark Main (Light)

# コマンドラインから（要設定）
java -Djabm.config=Model/mainBenchmark_light.xml benchmark.Main
```

### 結果確認
```bash
# 出力ファイル一覧
ls -lh ../data/

# GDPデータ確認
tail -20 ../data/nominalGDP*.csv
```

---

## 参考リンク

**論文:**
- Caiani et al. (2016) - http://papers.ssrn.com/sol3/papers.cfm?abstract_id=2664125

**JABMフレームワーク:**
- GitHub: https://github.com/S120/jmab

**環境構築:**
- Windows手順書: [`../../setup-windows.md`](../../setup-windows.md)

---

## 貢献

本ドキュメントは研究・教育目的で作成されました。

**改善提案:**
- 不明点や誤りを発見した場合は、プロジェクト管理者にご連絡ください
- 新しい実験結果や知見の追加を歓迎します

---

**作成日:** 2025-11-08
**対象モデル:** Caiani & Godin (2016) Benchmark Model
**バージョン:** 1.0
