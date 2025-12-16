以下の指示書に従って、ローカル環境の状況を確認し、その結果（ファイルリスト）を教えてください。そのリストをもとに、あなた専用の `build.gradle` を作成します。

---

#ローカル環境調査指示書（Gradle移行準備）##1. 目的現在のプロジェクトに含まれている `.jar` ファイルを洗い出し、以下の2種類に分類するための情報を収集する。

1. **Maven Central対応:** ネットから自動ダウンロードできるもの（削除対象）
2. **レガシー/カスタム:** ネットに存在しないため、手動管理が必要なもの（救出対象）

##2. 調査タスク###Task 1: 全JARファイルのリストアッププロジェクトのルートディレクトリ（`caiani2016-benchmark`）で、以下のコマンドを実行し、**出力されたファイル名の一覧**をコピーして共有してください。

**Linux / Mac (ターミナル):**

```bash
find . -name "*.jar"

```

**Windows (PowerShell):**

```powershell
Get-ChildItem -Recurse -Filter *.jar | Select-Object FullName

```

※もし数が多すぎる場合は、主要な3つの `lib` フォルダの中身だけでも構いません。

* `jabm/lib/` または `jabm/jabm/lib/`
* `jmab/lib/`
* `benchmark/benchmark/lib/`

###Task 2: ソースコードの場所確認Gradleの標準構成と合致しているか確認するため、以下のフォルダが存在するか（ソースコードがどこにあるか）を目視で確認してください。

1. **JABM:** `jabm/jabm/src/main/java` という階層ですか？ それとも `jabm/src` ですか？
2. **JMAB:** `jmab/src` の直下にパッケージ（`net` や `jmab` など）がありますか？
3. **Benchmark:** `benchmark/benchmark/src` の直下に `benchmark` パッケージがありますか？

---

##3. 提出フォーマット（回答例）次回、以下のような形式で調査結果を貼り付けてください。これがあれば完璧な `build.gradle` が書けます。

```text
■ Task 1: JARリスト
./jabm/lib/commons-math-3.2.jar
./jabm/lib/pf-joi-3.0.jar
./jmab/lib/jfreechart-1.0.14.jar
...

■ Task 2: フォルダ構成
JABM: jabm/jabm/src でした
JMAB: jmab/src でした
Benchmark: benchmark/benchmark/src でした

```

---
