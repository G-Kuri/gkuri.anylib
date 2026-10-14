package eventbus;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import gkuri.anylib.eventbus.EventRegister;

public class Test {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		TestEventDis testEventDis = new TestEventDis("hoge");
		EventRegister.registEvent(Test.class.getPackageName(), testEventDis, "hoge");
		
		testEventDis = new TestEventDis("fuga");
		EventRegister.registEvent(Test.class.getPackageName(), testEventDis, "fuga");
	}
	
}
