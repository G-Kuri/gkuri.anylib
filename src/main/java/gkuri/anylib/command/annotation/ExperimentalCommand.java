package gkuri.anylib.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 試験的なコマンドのクラスにつけるアノテーション。
 * CommandManagerBuilder.addExperimentalCommandメソッドから
 * 追加するか否かを指定できるため、引数に動作環境が
 * 試験的環境か否かを入れることで
 * ソースを変更せずコマンドの登録を制御できる。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExperimentalCommand {
	
	boolean value() default true;
	
}
