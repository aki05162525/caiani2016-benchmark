# Gradle Toolchain (Corretto 8) 実装進捗メモ

## 完了日
2026-01-04

## 概要
Gradle ToolchainsでAmazon Corretto 8を指定し、ビルド実行JDKを固定しました。

## 完了済み

### 1. build.gradle ✓
**ファイル**: `build.gradle`

**実装内容**:
- `subprojects` に `java.toolchain` を追加
- `languageVersion = 8` と `vendor = AMAZON` を指定
- `JavaExec` の `javaLauncher` をCorretto 8で固定
- `runMonteCarlo` (Exec) タスクを追加してスクリプト実行をGradleから可能に

## 完了項目
1. ✓ Corretto 8のToolchain指定を追加
2. ✓ Gradleタスクからモンテカルロ実行を起動
