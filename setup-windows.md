# Caiani2016 Benchmark - Windows環境構築手順書

Caiani & Godin (2016) ベンチマークモデルをWindows + IntelliJ IDEAで実行するための手順書です。

## 前提条件

- **Java 8以上** (推奨: Java 8 または 11)
- **IntelliJ IDEA** (Community/Ultimate)

確認コマンド:
```bash
java -version
javac -version
```

---

## 環境構築手順

### 1. 必須修正（ビルド前に実行）

#### 1-1. jabmバージョン修正

`jabm/jabm-examples/pom.xml` 27行目を修正:
```xml
<!-- 修正前 -->
<version>0.9.1-SNAPSHOT</version>
<!-- 修正後 -->
<version>0.9.2-SNAPSHOT</version>
```

#### 1-2. Maven HTTPブロック解除

`C:\Users\<ユーザー名>\.m2\settings.xml` を作成:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>maven-default-http-blocker</id>
      <mirrorOf>external:http:!jabm.sourceforge.net</mirrorOf>
      <name>HTTP blocker except jabm</name>
      <url>http://0.0.0.0/</url>
      <blocked>true</blocked>
    </mirror>
  </mirrors>
</settings>
```

#### 1-3. ローカルJAR参照設定

`jabm/jabm/pom.xml` 153-167行目付近を修正:
```xml
<!-- 修正前 -->
<dependency>
    <groupId>bsh</groupId>
    <artifactId>bsh</artifactId>
    <version>1.3.0</version>
</dependency>
<dependency>
    <groupId>infoNode</groupId>
    <artifactId>idw</artifactId>
    <version>1.6.1</version>
</dependency>
<dependency>
    <groupId>programmers-friend</groupId>
    <artifactId>pf-joi</artifactId>
    <version>3.0</version>
</dependency>

<!-- 修正後 -->
<dependency>
    <groupId>bsh</groupId>
    <artifactId>bsh</artifactId>
    <version>2.0b4</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/bsh-2.0b4.jar</systemPath>
</dependency>
<dependency>
    <groupId>infoNode</groupId>
    <artifactId>idw</artifactId>
    <version>1.6.1</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/infoNode/idw/1.6.1/idw-1.6.1.jar</systemPath>
</dependency>
<dependency>
    <groupId>programmers-friend</groupId>
    <artifactId>pf-joi</artifactId>
    <version>3.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/programmers-friend/pf-joi/3.0/pf-joi-3.0.jar</systemPath>
</dependency>
```

#### 1-4. Spring XML設定修正

**PowerShellで一括修正:**
```powershell
cd benchmark\benchmark\Model
Get-ChildItem -Recurse -Filter *.xml | ForEach-Object {
    (Get-Content $_.FullName) -replace '<idref local=', '<idref bean=' | Set-Content $_.FullName
}
```

**Git Bashで一括修正:**
```bash
cd benchmark/benchmark/Model
find . -name "*.xml" -type f -exec sed -i 's/<idref local=/<idref bean=/g' {} +
```

---

### 2. IntelliJ IDEAでビルド

#### 2-1. プロジェクトを開く
1. `File` → `Open` → `jabm`フォルダを選択
2. `Open as Project`

#### 2-2. JDK設定
1. `File` → `Project Structure` → `Project`
2. **Project SDK**: Java 8以上を選択
3. **Project language level**: 8

#### 2-3. Mavenプロジェクト読み込み
1. 右下の`Load Maven Project`または`Import`をクリック
2. 右側の`Maven`ツールウィンドウを開く
3. 🔄 `Reload All Maven Projects`をクリック

#### 2-4. キャッシュクリア（重要）
1. `File` → `Invalidate Caches...`
2. すべてにチェックを入れて`Invalidate and Restart`
3. 再起動後、再度🔄 `Reload All Maven Projects`

#### 2-5. jabmビルド
1. Mavenツールウィンドウで`jabm-with-examples` → `Lifecycle` → `install`をダブルクリック
2. `BUILD SUCCESS`を確認

---

### 3. プロジェクト全体を開く

#### 3-1. ルートプロジェクトを開く
1. `File` → `Open` → `caiani2016-benchmark`フォルダ（ルート）
2. または`File` → `New` → `Module from Existing Sources...`で`jmab`と`benchmark`を追加

#### 3-2. モジュール依存関係設定
`File` → `Project Structure` → `Modules`:

**jmabモジュール:**
- Sources: `jmab/src`をマーク
- Dependencies:
  - `jabm`モジュールを追加
  - `jmab/lib`内のすべてのJARを追加

**benchmarkモジュール:**
- Sources: `benchmark/benchmark/src`をマーク
- Dependencies:
  - `jmab`と`jabm`モジュールを追加
  - `benchmark/benchmark/lib`内のすべてのJARを追加

#### 3-3. プロジェクトビルド
`Build` → `Rebuild Project`

---

## 実行方法

### Run Configuration設定

1. `Run` → `Edit Configurations...` → `+` → `Application`
2. 以下を設定:
   - **Name**: `Benchmark Main (Light)`
   - **Main class**: `benchmark.Main`
   - **VM options**: `-Djabm.config=Model/mainBenchmark_light.xml`
   - **Working directory**: `C:\Users\Akihi\Develop\seminar\caiani2016-benchmark\benchmark\benchmark`
   - **Use classpath of module**: `benchmark`
3. `Apply` → `OK`

### 実行
ツールバーから`Benchmark Main (Light)`を選択して▶実行ボタンをクリック

---

## トラブルシューティング

### 依存関係エラー
```
Could not resolve dependencies... infoNode:idw:jar:1.6.1
```

**解決策:**
```bash
# Mavenキャッシュクリア（Git Bash）
rm -rf ~/.m2/repository/infoNode ~/.m2/repository/programmers-friend ~/.m2/repository/bsh
find ~/.m2/repository -name "*.lastUpdated" -delete

# PowerShell
Remove-Item -Recurse -Force $env:USERPROFILE\.m2\repository\infoNode
Remove-Item -Recurse -Force $env:USERPROFILE\.m2\repository\programmers-friend
Remove-Item -Recurse -Force $env:USERPROFILE\.m2\repository\bsh
```

その後、IntelliJで🔄 `Reload All Maven Projects`

---

### シンボル解決エラー
```
シンボル 'infonode' を解決できません
```

**解決策:**
1. `File` → `Invalidate Caches...` → `Invalidate and Restart`
2. 再起動後🔄 `Reload All Maven Projects`
3. `Build` → `Rebuild Project`

---

### XMLパースエラー
```
Attribute 'local' is not allowed to appear in element 'idref'
```

**解決策:**
手順1-4のXML修正を実行（`<idref local=` → `<idref bean=`）

---

### 設定ファイル未検出
```
FileNotFoundException: Model/mainBenchmark_light.xml
```

**解決策:**
Run ConfigurationのWorking directoryを確認:
```
C:\Users\Akihi\Develop\seminar\caiani2016-benchmark\benchmark\benchmark
```

---

### ClassNotFoundException
```
ClassNotFoundException: net.sourceforge.jabm.SimulationManager
```

**解決策:**
1. jabmを再ビルド: Mavenツールウィンドウで`jabm` → `clean` → `install`
2. モジュール依存関係を確認: `File` → `Project Structure` → `Modules` → `benchmark` → `Dependencies`

---

## チェックリスト

修正完了確認:
- [ ] `jabm/jabm-examples/pom.xml` バージョン0.9.2-SNAPSHOT
- [ ] `~/.m2/settings.xml` HTTP許可設定
- [ ] `jabm/jabm/pom.xml` ローカルJAR参照
- [ ] `benchmark/benchmark/Model/*.xml` `<idref bean=`に変更
- [ ] jabmビルド成功
- [ ] `benchmark.Main`実行成功

---

## 参考情報

**論文:** Caiani, A., Godin, A., et al. (2016). Agent based-stock flow consistent macroeconomics: Towards a benchmark model. *Journal of Economic Dynamics and Control*, 69, 375-408.

**設定ファイル:**
- `Model/mainBenchmark_light.xml` - 軽量版（推奨）
- `Model/mainBenchmark_full.xml` - 完全版

**出力先:** `benchmark/benchmark/data/` にCSVファイルが出力されます。
