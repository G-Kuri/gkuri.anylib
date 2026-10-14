package gkuri.anylib.eventbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import gkuri.anylib.eventbus.EventDispatcher;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ListenEvent {

	Class<? extends EventDispatcher<?>> dispatcher();
	
	String key() default "";
}
