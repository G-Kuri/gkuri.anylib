# anylib

Javaアプリケーション開発で度々使用する機能をまとめたアプリ基盤ライブラリです。
コマンドエンジン、イベントバス、スケジューラ、キャッシュストア、ロガー、設定ファイル操作、ソケット通信、各種ユーティリティなどを提供します。
Java 21を対象としています。

---

## 特徴

anylibではアプリケーション開発で繰り返し実装されがちな機能を統一されたAPIで利用できます。
単体または各種大規模フレームワークと組み合わせて導入する想定です。
アプリ全体のライフサイクルは握りませんが、個々の機能のライフサイクルは握るものがあります。

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
CommandManager manager = new CommandManagerBuilder() .addEchoCommand(true) .build();

CommandResult result = manager.executeCommand( new CommandArgument("echo test") );

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
LFU・LRU・TTLの3種類のキャッシュストアを提供し、用途に応じたキャッシュデータの管理方式を選択できます。

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

ループ回数の制限やスレッドプール数の指定、タスク実行時のコンテキスト取得、例外処理などに対応しています。

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

また、ログを単なる出力文字列ではなくJavaオブジェクトとして扱えます。

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

**開発中 / Beta**

現在もAPIや内部実装を変更または非推奨になる可能性があります。

各パッケージのAPIの安定度評価(作者評価、10段階、1=不安定 10=安定)は以下の通りです。
※1.あくまで目安となります。
※2.同一パッケージ内でも、クラスによって安定度は異なります。
| パッケージ名 | 安定度 |
| --- | --- |
| cache | 7 |
| command | 9 |
| communication | 6 |
| eventbus | 7 |
| exception | 9 |
| file | 8 |
| logs | 8 |
| setting | 7 |
| user | 10 |
| utils | 9 |

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