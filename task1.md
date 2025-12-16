完璧な調査報告をありがとうございます！フォルダ構成の癖（Standard vs Non-Standard）と、依存ライブラリの重複具合が完全に把握できました。

結論から言うと、**リストアップしていただいたJARファイルの9割は「ゴミ（削除対象）」です。**
現代のGradleなら、たった5〜6個の「救出ファイル」以外はすべてインターネットから自動取得できます。

それでは、**「レガシー環境の解体」**と**「モダン環境の創造」**を行いましょう。

---

###Step 1: 救出作戦（libsフォルダの作成）プロジェクトのルート（`caiani2016-benchmark` 直下）に `libs` というフォルダを作成し、以下の **5つのファイルだけ** をコピーして入れてください。

**救出リスト（これ以外はコピー不要！）:**

1. `./jabm/jabm/lib/programmers-friend/pf-joi/3.0/pf-joi-3.0.jar`
2. `./jabm/jabm/lib/programmers-friend/pf-core/5.5.0/pf-core-5.5.0.jar`
3. `./jabm/jabm/lib/programmers-friend/pf-base/5.5.0/pf-base-5.5.0.jar`
4. `./jabm/jabm/lib/infoNode/idw/1.6.1/idw-1.6.1.jar`
5. `./jabm/jabm/lib/bsh-2.0b4.jar` (念のためローカル保持)

> **解説:** これらは Maven Central に存在しないか、入手困難な「化石」たちです。これら以外（Spring, Commons, Colt, JFreeChart, Jungなど）はすべてGradleがネットから取ってきます。

---

###Step 2: プロジェクト定義 (`settings.gradle`)ルートディレクトリに `settings.gradle` を作成し、以下のコードを貼り付けてください。
ここで「深い階層にあるプロジェクトフォルダ」を強制的にマッピングします。

```groovy
rootProject.name = 'modern-caiani-benchmark'

include 'jabm'
include 'jmab'
include 'benchmark'

// フォルダ構成の補正（ここが重要！）
// 調査結果に基づき、ソースコードがある階層をプロジェクトルートとして認識させます
project(':jabm').projectDir = file('jabm/jabm')
project(':benchmark').projectDir = file('benchmark/benchmark')
// jmabは直下のようなのでそのままでOK
project(':jmab').projectDir = file('jmab')

```

---

###Step 3: ビルドロジック (`build.gradle`)ルートディレクトリに `build.gradle` を作成し、以下を貼り付けてください。
**調査結果に基づき、`sourceSets`（ソースコードの場所）を正確に書き換えました。**

```groovy
// 全プロジェクト共通設定
subprojects {
    apply plugin: 'java'
    apply plugin: 'idea' // IntelliJ用

    // Java 8 互換
    sourceCompatibility = 1.8
    targetCompatibility = 1.8
    
    // 文字コード設定（文字化け防止）
    tasks.withType(JavaCompile) {
        options.encoding = 'UTF-8'
    }

    repositories {
        mavenCentral() // メインのリポジトリ
        
        // ローカルのlibsフォルダをリポジトリとして追加
        flatDir {
            dirs "${rootProject.projectDir}/libs"
        }
    }

    dependencies {
        // 全体で使う共通ライブラリ
        implementation 'log4j:log4j:1.2.16'
        testImplementation 'junit:junit:4.13.2'
    }
}

// --- JABM (基盤フレームワーク) ---
project(':jabm') {
    dependencies {
        // Maven Centralから自動取得（バージョンは元のJARに合わせました）
        implementation 'org.springframework:spring-context:3.2.3.RELEASE'
        implementation 'org.springframework:spring-beans:3.2.3.RELEASE'
        implementation 'org.springframework:spring-core:3.2.3.RELEASE'
        implementation 'org.apache.commons:commons-math3:3.2'
        implementation 'commons-collections:commons-collections:3.2.1'
        implementation 'commons-logging:commons-logging:1.1.1'
        implementation 'net.sourceforge.collections:collections-generic:4.01'
        implementation 'net.sf.jung:jung-api:2.0.1'
        implementation 'net.sf.jung:jung-algorithms:2.0.1'
        implementation 'colt:colt:1.2.0'
        
        // 【救出ファイル】libsフォルダから参照
        implementation name: 'pf-joi-3.0'
        implementation name: 'pf-core-5.5.0'
        implementation name: 'pf-base-5.5.0'
        implementation name: 'idw-1.6.1'
        implementation name: 'bsh-2.0b4'
    }
    
    // ソースフォルダの指定（Maven標準構成のためデフォルトでOKだが明示）
    sourceSets {
        main {
            java {
                srcDirs = ['src/main/java']
            }
            resources {
                srcDirs = ['src/main/resources']
            }
        }
    }
}

// --- JMAB (マクロ経済拡張) ---
project(':jmab') {
    dependencies {
        implementation project(':jabm') // jabmを使う
        
        implementation 'colt:colt:1.2.0'
        implementation 'jfree:jfreechart:1.0.13' // 1.0.14に近いものを指定
        implementation 'jfree:jcommon:1.0.16'
        implementation 'org.springframework:spring-context:3.2.3.RELEASE'
    }

    // ★重要: Eclipse構成への対応
    // jmab/src 直下にソースがあるので場所を上書き
    sourceSets {
        main {
            java {
                srcDirs = ['src']
            }
            // リソースもsrcにある場合が多いので念のため追加
            resources {
                srcDirs = ['src']
            }
        }
    }
}

// --- Benchmark (モデル本体) ---
project(':benchmark') {
    apply plugin: 'application' // 実行可能にする

    mainClassName = 'benchmark.Main'

    dependencies {
        implementation project(':jmab')
        implementation project(':jabm')
        
        // 再掲（推移的依存関係がうまくいかない場合の保険）
        implementation 'colt:colt:1.2.0'
        implementation 'jfree:jfreechart:1.0.13'
    }

    // ★重要: Eclipse構成への対応
    // benchmark/benchmark/src 直下にソースがある
    sourceSets {
        main {
            java {
                srcDirs = ['src']
            }
            resources {
                srcDirs = ['src'] // 設定XMLなどがここにある可能性
            }
        }
    }

    // 実行タスク: Light設定
    task runLight(type: JavaExec) {
        group = 'application'
        description = 'Runs benchmark model (Light)'
        classpath = sourceSets.main.runtimeClasspath
        main = mainClassName
        // システムプロパティで設定ファイルを指定
        systemProperty 'jabm.config', 'Model/mainBenchmark_light.xml'
        // 作業ディレクトリを固定
        workingDir = file('.') 
    }

    // 実行タスク: Full設定
    task runFull(type: JavaExec) {
        group = 'application'
        description = 'Runs benchmark model (Full)'
        classpath = sourceSets.main.runtimeClasspath
        main = mainClassName
        systemProperty 'jabm.config', 'Model/mainBenchmark_full.xml'
        workingDir = file('.')
    }
}

```

---

###Step 4: 動作確認これで準備は整いました。ターミナル（ルートディレクトリ）で以下のコマンドを実行してください。

**1. Gradle Wrapperの生成（初回のみ）**

```bash
gradle wrapper

```

※もしPCにGradleが入っていない場合は、IntelliJの右側のGradleメニューからリロードボタン（🔄）を押すだけでOKです。

**2. 実行テスト**

```bash
# Mac / Linux
./gradlew runLight

# Windows
.\gradlew.bat runLight

```

**成功すれば、シミュレーションが動き出すはずです！**

もし「ClassNotFound」や「Source not found」などのエラーが出たら、エラー文をそのまま貼り付けてください。パスの微調整をします。これが動けば、もう古い `lib` フォルダたちは（バックアップを取った上で）全削除してOKになります。