# anylib

Javaアプリケーション開発で使用する機能をまとめたアプリ基盤ライブラリです。
Java 21を対象としています。
コマンド処理、イベント管理、スケジューリング、設定ファイル操作など、アプリ開発で繰り返し実装される機能を統一APIで提供します。
単一ライブラリとして配布されていますが、各パッケージを利用する際は独立して利用できます。

---

## 特徴

anylibではアプリケーション開発で繰り返し実装されがちな機能を統一されたAPIで利用できます。
単体で、または各種大規模フレームワークと組み合わせて導入する想定です。
アプリ全体のライフサイクルは握りませんが、個々の機能のライフサイクルは握るものがあります。

### 既存ライブラリとの違い

- 「ちょうどいい」独立性と統一感
  - 多くの汎用ライブラリは機能ごとにバラバラの設計・APIになりがちですが、anylib はすべてのモジュールが一貫した設計思想で作られています。
- アプリ全体を縛らない柔軟性
  - アプリ全体のライフサイクルを強制的に握る巨大なフレームワークと異なり、必要なパッケージだけ（単体でも、モジュール単位で再ビルドして組み込んでも）自由に利用できます。

#### 各機能のターゲットユーザ
各機能は以下のような開発者をターゲットとしています。

- コマンドエンジン
  - CLIツールや対話型シェル、サーバの管理コンソールを手軽に作りたい人。
  - 引数の型解析や型変換、引数の数の検証などをコマンドごとに自前実装したくない人。
  - コマンド名だけでなく、サブコマンドや引数の入力補完にも対応したい人。
  - 対話型コマンドやパイプによるコマンド連携など、単発のコマンド実行にとどまらない処理を実装したい人。
  - コマンドの実行権限や実行前後のイベント、非同期実行の管理などを、独自の仕組みを構築せずに利用したい人。
  - 既存のコマンドライブラリでは実現したい入力インターフェースとの連携が難しく、コマンド処理とフロントエンドを分離して設計したい人。
  

引数の自動型チェック・変換、サブコマンド、対話モード、さらにはTypoサジェストやイベントフックまで標準搭載。
コマンドの処理内容を書くだけで複雑な機能を持ったコンソールを作成できます。

- イベントバス
  - コンポーネント間の結合度を下げ、クリーンなアーキテクチャでイベント駆動型の処理を書きたい人。
  - イベントリスナーを一つずつ手動登録する手間を減らし、パッケージ単位で自動登録したい人。
  - アノテーションを使って、イベントリスナーの登録先や登録条件を宣言的に管理したい人。
  - 複数のイベントディスパッチャを使い分け、リスナーの登録先を整理したい人。
  - 登録キーを利用して、用途や機能ごとにリスナーの登録対象を切り替えたい人。
  - 機能追加時にイベントリスナーの登録コードを個別に変更する手間を減らしたい人。

アノテーションによる直感的なリスナー登録に加え、「イベントキー」による同一ディスパッチャ内での購読先振り分けをサポート。
リスナーの自動検出・パッケージ単位での登録ができるため、Spring等の大規模フレームワークを導入せずともボイラープレートコードを大幅に削減できます。

- キャッシュストア
  - 単なる Map では物足りないが、外部の重いキャッシュミドルウェアを導入するほどではない小～中規模のデータ管理を行いたい人。
  - 独自のキャッシュ方式を簡単に実装したい人。
  - 基底クラスの機能を利用しながら、データの整理や削除のルールを独自に定義したい人。
  - アプリケーションの用途に応じたキャッシュ戦略を柔軟に実装したい人。
  - データの取得回数や最終アクセス時刻を利用して、保持するデータを制御したい人。
  - データの登録時刻などをもとに、独自の削除条件や優先順位を設けたい人。
  - データの登録・取得・削除・全消去のタイミングで、独自の処理を実行したい人。
  - キャッシュの状態変化をイベントとして受け取り、アプリケーション側の処理と連携させたい人。

LFU・LRU・TTLの3種類のキャッシュを標準で提供。
さらに、基底クラス（CacheStore）を継承して putData / getData などをオーバーライドするだけで、独自のキャッシュ整理・イベント通知ルールを極めて簡単に自作できます。

- スケジューラ
  - 一定間隔での繰り返し実行や、指定した時刻・曜日・日付などに基づくタスク実行を手軽に実装したい人。
  - 経過時間ではなく、カレンダー上の時刻を基準にタスクを実行したい人。
  - タスクの実行回数や次回実行時刻などを取得し、アプリケーション側で実行状況を管理したい人。
  - タスクの実行時刻や実行回数などの情報を、タスクの処理内容と併せて扱いたい人。
  - タスク実行時の例外処理を共通化し、例外発生時の振る舞いを独自に制御したい人。
  - スケジューリング専用の大規模なフレームワークを導入せず、必要な機能を既存のJavaアプリケーションに組み込みたい人。
  - スケジューラから次回実行予定時間を取得したい人。

単なる ScheduledExecutorService のラッパーにとどまらず、固定レート/固定遅延の切り替え、最大ループ回数、スレッドプール数、
さらにはタスクのライフサイクル（init / task / finished / taskCatchException）までをひとつのオブジェクトで一元管理することもできます。
また、最大実行回数を迎えたスケジューラは自動でシャットダウンするため、シャットダウン処理が不要です。

- ロガー
  - ログを単なる文字列としてではなく、構造化されたデータ（Javaオブジェクト）として扱ったり、メモリ上に保持して柔軟にハンドリングしたい人。
  - ログの出力形式を統一したい人。
  - 複数の機能やクラスで出力するログの形式を統一したい人。
  - ログを単なる文字列ではなく、データとして扱いたい人。
  - ログの発生時刻、レベル、コード、メッセージ、関連オブジェクトなどを個別に取得したい人。
  - 出力済みの文字列を解析するのではなく、ログの情報をそのままプログラム内で利用したい人。
  - ログをメモリ上に保持し、後から参照したい人。
  - コンソールには重要なログだけを表示し、ファイルにはより詳細な情報を記録したい人。
  - 標準的な INFO や ERROR だけでは分類しきれない、独自のログレベルや分類を定義したい人。
  - ログコードや独自レベルを活用して、ログの種類を整理したい人。

独自ログレベルの定義、出力フォーマットの自由なカスタマイズ、ロギングイベントのフックに対応。
「出力文字列のフォーマットを細かく制御したい」「特定のコード体系（DefaultLog）でログを管理したい」という要望に直球で応えます。

- 設定ファイル操作
  - Properties・JSON・XML など、複数の設定ファイル形式を統一的な API で扱いたい人。
  - ファイル形式ごとのデータ構造の違いを意識せず、共通のキー表現で設定値を参照・更新したい人。
  - 階層構造や配列を含む設定値を、ドット区切りのキーやインデックスを使って簡潔に操作したい人。
  - 設定値の取得時に必要となる数値や Boolean などへの型変換処理を共通化したい人。
  - 設定値の読み込みから追加・更新・削除・保存までを、一貫した API で扱いたい人。
  - 設定データを別のファイル形式に変換し、形式ごとの操作コードを個別に実装する手間を減らしたい人。
  - クラスパス上の設定ファイルも含め、アプリケーション内の設定管理を共通化したい人。

拡張子やフォーマットの違いを意識させない統一APIを提供。
.cast(FileType.TYPE_JSON) のように設定オブジェクトを別の形式へワンライナーで変換できる他、ソースコードの修正を最小限に抑えられます。

- ソケット通信
  - プロセス間やコンピュータ間で、手軽にTCP通信やカスタムデータの送受信を実装したい人。

単方向・双方向・マルチクライアント対応サーバなどのモジュールを用途に応じて選択可能。
フィルター機能や通信イベントフック、try-with-resources への完全対応により、リソースリークの心配なく安全かつ簡潔に通信処理を記述できます。


### 設計思想

自分が理想とする開発体験を得られることを目標に
複数のプロジェクトで汎用的に使用できる機能を
1つのライブラリとしてまとめることを挙げています。

また、基本的な考え方として
- 簡単なことは簡単に、難しいこともできるように
- 最小限の設定で動くように
- 直感的なAPIであるように
- 一貫性のあるAPIであるように
というのを持っています。


### 主な機能

- コマンドの定義・管理・実行
- イベントの登録・購読・ディスパッチ
- キーによるイベントの振り分け
- データのキャッシュ管理
- 遅延・間隔・時刻ベースのタスクスケジューリング
- ログの管理・出力・保存
- Properties / JSON / XML形式の設定ファイル操作
- TCPソケット通信
- 型変換・文字列変換などのユーティリティ
- 複数の値を保持できるコンテナ

---

## 導入

`gkuri.anylib_jdk21_X.X.X.jar` をプロジェクトの依存関係に追加してください。
jarファイルはreleasesから取得できます。
https://github.com/G-Kuri/gkuri.anylib/releases

### Gradle

dependencies { 
	implementation files("libs/gkuri.anylib_jdk21_X.X.X.jar") 
}


### Maven

<dependency>
	<groupId>local</groupId>
	<artifactId>anylib</artifactId>
	<version>X.X.X</version>
	<scope>system</scope>
	<systemPath>${project.basedir}/libs/gkuri.anylib_jdk21_X.X.X.jar</systemPath>
</dependency>


### 参考

jarを解体し、必要なパッケージのみを残して再ビルドする場合は
各パッケージの依存関係にご注意ください。

gkuri.anylib配下の各依存関係は以下の通りです。
| パッケージ名 | 依存パッケージ |
| --- | --- |
| cache | (依存なし) |
| command | setting, exception, utils |
| communication | utils |
| eventbus | (依存なし) |
| exception | (依存なし) |
| file | exception, utils |
| logs | utils |
| setting | file, utils |
| user | exception |
| utils | (依存なし) |

---

# モジュール

## コマンドエンジン

コマンドの登録・管理・解析・実行を行うためのコマンドエンジンです。
CLIアプリケーションやサーバ管理機能など、コマンドベースの操作を必要とするアプリケーションで利用できます。



### 特徴

- 引数の型解析・変換：引数の型パターンを定義し、数値や日時などへの変換、引数の数の検証を行えます。
- 入力補完：コマンド名、サブコマンド、引数の候補を取得でき、独自のコンソールや入力インターフェースに組み込めます。
- 階層型コマンド・対話型コマンド：サブコマンドによる階層構造や、複数段階の入力を必要とするコマンドを実装できます。
- コマンド連携：パイプでつないだコマンド間で実行結果を受け渡せます。
- 実行制御：権限チェック、実行前後のイベント、例外処理、非同期実行の同時実行数制限などを利用できます。

### 機能

- コマンド登録
- サブコマンド対応
- 対話型コマンド対応
- コマンド引数解析
- オプション解析
- 引数の自動型チェック・変換
- コマンドルーティング
- コマンド実行
- 実行結果取得
- 権限管理
- 非同期実行
- コマンドイベントフック
- 全体・コマンド別・実行結果ステータス別のイベントフック
- 補完候補取得
- Typoサジェスト
- 試験的なコマンドアノテーション
- 標準コマンド

### 標準コマンド

- `help`
- `history`
- `echo`
- `sudo`

### 例

```java
CommandManager manager = new CommandManagerBuilder().addEchoCommand(true).build();

CommandResult result = manager.executeCommand( new CommandArgument("echo test"));

System.out.println(result.getResult()); // 出力結果 : test
```

---

## イベントバス

イベントの発火元となるディスパッチャと、イベントを受け取るリスナーを分離して管理するイベントバス機能です。
アノテーションを利用してイベントリスナーを登録でき、イベントキーによる購読先の振り分けにも対応しています。

### 特徴

- イベントディスパッチャの定義
- イベントリスナーの登録
- アノテーションによるイベント購読
- イベントキーによる購読先の振り分け
- パッケージ単位でのイベント登録
- ディスパッチャとリスナーの疎結合化
- リスナーの自動検出・登録

### イベントディスパッチャ

`EventDispatcher` を実装することで、独自のイベントディスパッチャを定義できます。

`@ListenerClass` にリスナーの型を指定します。

```java
@ListenerClass(MyEventListener.class) 
public class MyEventDis implements EventDispatcher<MyEventListener> {

	@Override 
	public void addEventListener(MyEventListener listener) { 
		//このメソッドは必須です。 
	} 
}
```

### イベントリスナー

`@ListenEvent` を使用して、どのディスパッチャのイベントを購読するかを指定できます。

```java
@ListenEvent( dispatcher = MyEventDis.class, key = "" ) 
public class MyEventListener implements EventListener {

	//EventListenerの必須メソッドはありません。
	
	@Override 
	public void on() { 
		System.out.println("onOn"); 
	}
}
```

### キー付きイベント

イベントにキーを指定することで、同一ディスパッチャ内でも購読先を分けることができます。

```java
@ListenEvent( dispatcher = MyEventDis.class, key = "hoge" ) 
public class MyEventLisWithKey extends MyEventListener {
	@Override public void on() { 
		System.out.println("onOnWithHogeKey"); 
	} 
}
```

イベントディスパッチャの登録時にキーを指定します。

```java
MyEventDis eventDis = new MyEventDis();

EventRegister.registEvent( this.class.getPackageName(), eventDis, "hoge" );


// キーを指定しないイベントと、'hoge' のようなキー付きイベントをそれぞれ登録できます。

EventRegister.registEvent( this.class.getPackageName(), new MyEventDis() );

EventRegister.registEvent( this.class.getPackageName(), new MyEventDis(), "hoge" );
```

---

## キャッシュストア

キーと値によるデータの保持・取得・削除を行うためのキャッシュストア機能です。

キャッシュクリアに自前の条件を持たせたい場合に、CacheStoreクラスを基底に利用することで
簡単かつ手早く実装することができます。

また、LFU・LRU・TTLの3種類のキャッシュストアを提供します。


### 特徴
- キーによるデータの登録・取得・更新・削除
- キャッシュデータの一括削除
- 登録済みキーの一覧取得
- データの取得回数・最終取得時刻の管理
- LFU・LRU・TTLによるキャッシュデータの整理
- キャッシュ操作に応じたイベント通知
- 基底クラスの継承による独自キャッシュストアを実装可能

```java
// LFU
CacheStore<String> lfu = new LFUCacheStore<>(100);

// LRU
CacheStore<String> lru = new LRUCacheStore<>(100);

// TTL：容量100、有効期間5分のキャッシュ
CacheStore<String> ttl =
    new TTLCacheStore<>(100, 5, TimeUnit.MINUTES);

// データの登録
lfu.put("user:1", "Alice");

// データの取得
String value = lfu.get("user:1");

// データの削除
lfu.remove("user:1");

// すべてのデータを削除
lfu.clear();
```

キャッシュイベント
CacheEventListener を登録することで、キャッシュの登録・取得・削除・全件削除・整理に関するイベントを受け取れます。

独自キャッシュストア
CacheStore<T> を継承することで、独自のキャッシュ管理方式を実装できます。

以下のメソッドをオーバーライドすることで、キャッシュデータの登録・取得・削除に伴う処理を拡張できます。

- putData：データ登録時の処理
- getData：データ取得時の判定処理
- removeData：データ削除時の処理
- clearData：全データ削除時の処理
これにより、用途に応じた独自のキャッシュ管理方式を定義できます。

---

## スケジューラ

タスクを一定間隔・一定遅延・指定時刻などの条件で実行するためのスケジューラ機能です。

繰り返し実行するタスクを、実行間隔・実行方式・回数・ライフサイクルまでまとめて制御できるため
自動終了や実行時間調整を自前実装せずに手早く実現したいケースに最適です。

### 特徴

- 間隔ベースのタスク実行
- 固定レート実行
- 固定遅延実行
- `TimeUnit` による時間単位指定
- 実行回数制限
- スケジューラのスレッドプール数指定
- 指定時刻ベースのタスク実行
- 次回実行時刻の取得
- タスク実行コンテキスト
- タスクライフサイクル管理
- タスク実行時の例外処理

### LoopScheduler

`LoopScheduler` は、一定間隔でタスクを繰り返し実行するためのスケジューラです。

実行方式として、固定レートと固定遅延を選択できます。

### 固定レート

一定の周期を基準としてタスクを実行します。

```java
LoopScheduler scheduler = new DelaySchedulerBuilder() .setSpan(500) .setLoopSchedulerMode(LoopSchedulerMode.FIXED_RATE_MODE) .setTask(new DelayTask("rate")) .build();

scheduler.start();
```

### 固定遅延

タスクの実行完了後から指定した時間を待って、次のタスクを実行します。
setLoopSchedulerModeを指定しない場合のデフォルトは固定遅延モードです。

```java
LoopScheduler scheduler = new DelaySchedulerBuilder() .setSpan(500) .setLoopSchedulerMode(LoopSchedulerMode.FIXED_DELAY_MODE) .setTask(new DelayTask("delay")) .build();

scheduler.start();
```

### 時間単位の指定

`TimeUnit` を使用して、ミリ秒以外の時間単位も指定できます。

```java
LoopScheduler scheduler = new DelaySchedulerBuilder() .setSpan(5) .setSpanTimeUnit(TimeUnit.SECONDS) .setMaxLoopCount(3) .setTask(new DelayTask("seconds")) .build();

scheduler.start();
```

### スレッドプール

スケジューラのタスク実行に使用するスレッド数を指定できます。

```java
LoopScheduler scheduler = new DelaySchedulerBuilder() .setSpan(300) .setSchedulerThreadPool(3) .setTask(new DelayTask("multi-thread")) .build();

scheduler.start();
```

### 時刻ベースのスケジューリング

`TimeSchedulerBuilder` を使用すると、指定した時刻を基準としてタスクを実行できます。

```java
SchedulerTime time = new SchedulerTime(); 
time.setMillSec(250);

LoopScheduler scheduler = new TimeSchedulerBuilder() .setTimeSchedulerMode(TimeSchedulerMode.EVERY_SEC) .setMaxLoopCount(15) .setSchedulerTime(time) .setTask(new DelayTask("every-second")) .build();

// このタスクはXX時YY分ZZ.250秒を目標に毎秒実行されます。
scheduler.start();


// 分単位などのスケジュールも指定できます。

LoopScheduler scheduler = new TimeSchedulerBuilder() .setTimeSchedulerMode(TimeSchedulerMode.EVERY_MIN) .setMaxLoopCount(2) .setSchedulerTime( new SchedulerTime().setSec(45) ) .setTask(new DelayTask("every-minute")) .build();

//このタスクはXX時YY分45.000秒を目標に毎分実行されます。
scheduler.start();
```

### 次回実行時刻

スケジューラから次回の実行時刻を取得できます。

```java
System.out.println( scheduler.getNextExecuteCurrentTimeMillis());

System.out.println( scheduler.getNextExecuteTime());
```

### タスク

`LoopScheduleTask` を実装することで、独自のスケジュールタスクを作成できます。

```java
public class SampleTask implements LoopScheduleTask {

	@Override 
	public void init(TaskContext context) { 
		System.out.println("task initialized"); 
	}

	//必須メソッドはtaskのみです。ラムダ式でも定義可能です。
	@Override 
	public void task(TaskContext context) { 
		System.out.println( "loop count : " + context.getLoopCount() ); 
	}

	@Override 
	public void finished(TaskContext context) { 
		System.out.println("task finished"); 
	}

	@Override 
	public void taskCatchException( TaskContext context, Exception exception) {
		System.err.println( exception.getMessage() ); 
	} 
}
```

タスクのライフサイクルに応じて、以下のメソッドを利用できます。

- `init`
- `task`
- `finished`
- `taskCatchException`

また、`TaskContext` から現在のループ回数など、タスク実行に関する情報を取得できます。

---

## ロガー

柔軟なログ管理システムを提供します。

ログのメモリ保持、ログレベル管理、ファイル出力、フォーマット変更、イベントフックなどに対応しています。

ログを単なる出力文字列ではなくJavaオブジェクトとして扱いたいケースや
ロギングイベントをフックしたい、出力フォーマットを指定したいようなケースに最適です。

### 特徴

- ログのメモリ保持
- 保持するログ数の上限設定
- ログレベル管理
- 標準ログレベル
  - `ERROR`
  - `WARN`
  - `INFO`
  - `DEBUG`
  - `TRACE`
- 独自ログレベルの定義
- 出力ログレベルの指定
- ファイル出力
- ログ関連イベントフック
- 出力フォーマット変更
- 独自フォーマットの定義
- ログのテンプレート(DefaultLog)定義

### 例

```java
LogManager logManager = new LogManager();

//                                      コード、タイトル、ログレベル 
DefaultLog defaultLog = new DefaultLog( 16, "default title", LogManager.DEBUG_LEVEL );

logManager.setLogFormat( "log[ code : %c || %t : %m || level : %l ]" );

logManager.addLog( new Log("Application started") );

// 出力: log[ code : 0 || Application started : null || level : INFO ]

logManager.setLogFormat( "log[ code : 0x%xc || %t : %m || level : %l || caller : %C ]" );

logManager.addLog( new Log(defaultLog, "default msg") );

// 出力: log[ code : 0x00000010 || default title : default msg // || level : DEBUG_LEVEL || caller : gkuri.anylib.Sample.main ]
```

---

## 設定ファイル操作

異なる設定ファイル形式を統一されたAPIで扱うための機能です。
設定ファイルの形式を意識せずに値の取得・変更・保存ができ、設定ファイル形式の変換にも対応しています。

のちに設計変更される可能性があるプロジェクトや、設定ファイルに複数のフォーマットを使用している
プロジェクトにおいて、ソース変更や修正を最小限に済ませたいケースに最適です。

### サポートするフォーマット

- Properties
- JSON
- XML
- TOML（予定）
- YAML（予定）

### 特徴

- 設定ファイルの読み込み
- 設定値の取得
- 設定値の変更
- 設定値の削除
- 設定ファイルの保存
- 異なる形式への変換
- 拡張子や形式を意識しない統一API

### 例

```java
SettingFile propertiesSetting = SettingFile.loadSetting(propertiesFilePath);

String serverName = propertiesSetting.getString("server.name");

Integer serverPort = propertiesSetting.getInt("server.port");

propertiesSetting.put( "server.dev.port", 8005 );

propertiesSetting.remove( "server.test.port" );

SettingFile jsonSetting = propertiesSetting.cast(FileType.TYPE_JSON);

propertiesSetting .get("server.name") .equals(jsonSetting.get("server.name")); // true

jsonSetting.save();
```

---

## ソケット通信

コンピュータ間やプロセス間のデータ通信に使用できるソケット通信機能です。

用途に応じて、送信・受信・クライアント・サーバなどの通信モジュールを選択できます。

### 使用可能なモジュール

- 単方向受信モジュール
- 単方向送信モジュール
- 双方向クライアントモジュール
- 双方向サーバモジュール
- 複数クライアント対応双方向サーバモジュール

### 特徴

- 用途に応じた通信モジュール選択
- フィルタ機能
- 独自フィルタの作成
- 通信データの送受信
- ping機能
- 通信イベントフック
- try-with-resources対応

### 通信イベント

以下のような通信イベントを扱えます。

- 送信
- 受信
- フィルタブロック
- ping
- 通信確立
- 通信切断

### 例

```java
//                           送信先ホスト、送信先ポート 
TCPSender sender = new TCPSender("localhost", 25565);

//                                  待受ポート 
TCPReceiver receiver = new TCPReceiver( 25565, (data) -> { 
															System.out.println(data); 
														}, new CommunicationBlackList());

receiver.open(); sender.open();

CommunicationData<MyObj> data = new CommunicationData<>();

// 自前実装クラスを送信する場合は、Jacksonでデシリアライズ可能にするため引数なしコンストラクタを用意してください。 
data.setData(myObj);

sender.send(data);

sender.close(); receiver.close();
```

すべての通信モジュールでtry-with-resourcesを利用できます。

---

# ユーティリティ

## 型変換

安全な型変換や文字列から各種型への変換を提供します。

### オブジェクト型変換

```java
MyObj myObj = ClassCastUtil.cast(anyObj, MyObj.class);

// castは変換に失敗した場合、例外ではなく `null` が返されます。

// 例外をスローしたい場合は
MyObj myObj = ClassCastUtil.cast(anyObj, MyObj.class, true);
// としてください。
```

### 数値変換

```java
int intNum = ClassCastUtil.stringToNumber("123").intValue();

int intNum16 = ClassCastUtil.stringToNumber("0xff").intValue(); // 255

int intNum8 = ClassCastUtil.stringToNumber("077").intValue(); // 63

int intNum2 = ClassCastUtil.stringToNumber("0b11").intValue(); // 3

long longNum = ClassCastUtil.stringToNumber("123L").longValue();

double dNum = ClassCastUtil.stringToNumber("12.3").doubleValue();

float fNum = ClassCastUtil.stringToNumber("12.3f").floatValue();
```

16進数、8進数、2進数、サフィックス付き数値などの変換に対応しています。

### Boolean変換

```java
boolean bool = ClassCastUtil.stringToBoolean("true");
```

### 日付・時間変換

```java
LocalDate date = ClassCastUtil.stringToDate("2000/01/01");

LocalTime time = ClassCastUtil.stringToTime("12:34:56");

LocalDateTime dateTime = ClassCastUtil.stringToDateTime( "2000-01-01'T'12.34.56" );
```

日付・時間について、複数の表記方法を扱えます。

```java
LocalDateTime dateTime = ClassCastUtil.stringToDateTime( "2000年01月01日 12時34分56秒" );
```

日付では、

- `-`
- `/`
- `.`
- スペース
- `年月日`
- 連結表記

などの区切り形式に対応しています。

時間では、

- `:`
- `.`
- スペース
- `時分秒`
- 連結表記

などの形式に対応しています。

---

## 複数値コンテナ

複数の値をひとつのオブジェクトで保持するためのコンテナクラスを提供します。

### Duo

```java
Duo<String, Integer> duo = new Duo<>("test", 100);
```

### コンテナシリーズ

- `Duo`
- `Trio`
- `Quart`

複数の異なる型の値をまとめて扱うことができます。

---

# 設計方針

anylibは以下を目的として設計されています。

- アプリケーション開発で頻出する処理を統一する
- 毎回作り直すコードを削減する
- APIを可能な限りシンプルに維持する
- 機能ごとに独立して利用できる構造を提供する
- アプリケーションに合わせて拡張しやすい構造を提供する

特に、アプリケーションの基盤部分で利用される機能をまとめて提供することを目指しています。

---

# 計画

今後、以下の機能・改善を予定しています。

- `SettingFile` のYAML対応
- `SettingFile` のTOML対応
- 各モジュールのAPI改善

---

# ステータス

**正式版 / Stable**

※今後も内部実装を変更されたりAPIが非推奨に変更される可能性があります。

各パッケージのAPIの安定度評価(作者評価、10段階、1=不安定 10=安定)は以下の通りです。
※1.あくまで目安となります。
※2.同一パッケージ内でも、クラスによって安定度は異なります。
| パッケージ名 | 内部実装安定度 | API安定度 |
| --- | --- | --- |
| cache | 7 | 10 |
| command | 9 | 10 |
| communication | 6 | 8 |
| eventbus | 8 | 7 |
| exception | 9 | 10 |
| file | 7 | 9 |
| logs | 8 | 8 |
| setting | 8 | 10 |
| user | 10 | 10 |
| utils | 8 | 9 |

---

# ライセンス

## このプロジェクトは MIT License のもとで公開されています。

このソフトウェアは、商用利用を含め、自由に使用、改変、再配布することができます。
このソフトウェアを再配布する場合、またはその実質的な部分を再配布する場合は、元の著作権表示および MIT License の本文を含める必要があります。
このソフトウェアは「現状のまま（AS IS）」提供され、明示・黙示を問わず、いかなる保証もありません。作者および著作権者は、このソフトウェアの使用に起因する請求、損害、その他の責任について責任を負いません。

### 商用利用について（任意）
このソフトウェアを商用利用する場合、ご連絡いただけると幸いです。
ご連絡は完全に任意であり、このソフトウェアを利用するための条件ではありません。
連絡先：gkuri730@gmail.com

ライセンスの全文については、MIT License を参照してください。