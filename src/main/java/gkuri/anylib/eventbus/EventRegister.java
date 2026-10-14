package gkuri.anylib.eventbus;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import gkuri.anylib.eventbus.annotation.ListenEvent;
import gkuri.anylib.eventbus.annotation.ListenerClass;

public class EventRegister {
	
	private static final Map<String, Set<Class<?>>> classesChash = new ConcurrentHashMap<String, Set<Class<?>>>(10);

	public static <T extends EventListener> EventDispatcher<T> registEvent(String scanPackage, EventDispatcher<T> dispatcher) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return registEvent(scanPackage, dispatcher, "");
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends EventListener> EventDispatcher<T> registEvent(String scanPackage, EventDispatcher<T> dispatcher, String key) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		ListenerClass dispatcherAnnotation = dispatcher.getClass().getAnnotation(ListenerClass.class);
		if(dispatcherAnnotation == null) {
			throw new IllegalArgumentException(dispatcher.getClass().getName() + " class has not ListenerClass annotation.");
		}
		
		if(!(dispatcherAnnotation.value() instanceof Class<? extends EventListener>)) {
			throw new IllegalArgumentException(dispatcherAnnotation.value().getClass().getName() + " class is not impl EventListener interface.");
		}
		
		Class<? extends EventListener> listenerClass = dispatcherAnnotation.value();
		
		Set<Class<?>> classes;
		if(classesChash.get(scanPackage) != null) {
			classes = classesChash.get(scanPackage);
		} else {
			classes = scanPackage(scanPackage, Thread.currentThread().getContextClassLoader());
			classesChash.put(scanPackage, classes);
		}
		
		for(Class<?> c : classes) {
//			System.out.print(c.getName());
			if(listenerClass.isAssignableFrom(c) && !c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
				ListenEvent listenEvent = c.getAnnotation(ListenEvent.class);
//				System.out.print(" listenEvent=" + listenEvent);
				if(listenEvent == null) continue;
				
//				System.out.print(" instance=" + listenEvent.dispatcher().isAssignableFrom(dispatcher.getClass()));
				if(listenEvent.dispatcher().isAssignableFrom(dispatcher.getClass())) {
//					System.out.print(" key=" + listenEvent.key());
					if(listenEvent.key().isEmpty() || key.isEmpty() || listenEvent.key().equals(key)) {
						dispatcher.addEventListener((T) c.getDeclaredConstructor().newInstance());
					}
				}
			}
//			System.out.println();
		}
		
		return dispatcher;
	}
	
	//こいつらは将来Utilとかの別クラスに切り出すかも
	 public static Set<Class<?>> scanPackage(String scanPackage, ClassLoader classLoader) throws IOException {

        Set<Class<?>> result = new LinkedHashSet<>();

        String packagePath = scanPackage.replace('.', '/');

        Enumeration<URL> resources =
                classLoader.getResources(packagePath);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            if ("file".equals(resource.getProtocol())) {
                scanDirectory(
                    scanPackage,
                    Paths.get(URI.create(resource.toString())),
                    classLoader,
                    result
                );

            } else if ("jar".equals(resource.getProtocol())) {
                scanJar(
                    scanPackage,
                    resource,
                    classLoader,
                    result
                );
            }
        }

        return result;
    }

    private static void scanDirectory(
            String packageName,
            Path directory,
            ClassLoader classLoader,
            Set<Class<?>> result) throws IOException {

        try (var stream = Files.walk(directory)) {
            stream
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".class"))
                .forEach(path -> {
                    Path relative = directory.relativize(path);

                    String className = packageName + "." +
                        relative.toString()
                            .replace(File.separatorChar, '.')
                            .replaceAll("\\.class$", "");

                    loadClass(className, classLoader, result);
                });
        }
    }

    private static void scanJar(
            String packageName,
            URL resource,
            ClassLoader classLoader,
            Set<Class<?>> result) throws IOException {

        JarURLConnection connection =
                (JarURLConnection) resource.openConnection();

        try (JarFile jar = connection.getJarFile()) {

            String prefix = packageName.replace('.', '/') + "/";

            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                String name = entry.getName();

                if (entry.isDirectory()
                        || !name.startsWith(prefix)
                        || !name.endsWith(".class")) {
                    continue;
                }

                String className = name
                        .substring(0, name.length() - 6)
                        .replace('/', '.');

                loadClass(className, classLoader, result);
            }
        }
    }

    private static void loadClass(
            String className,
            ClassLoader classLoader,
            Set<Class<?>> result) {

        // 内部クラス等も必要ならこの条件を外す
        if (className.equals("module-info")
                || className.endsWith(".module-info")
                || className.endsWith(".package-info")) {
            return;
        }

        try {
            Class<?> clazz =
                    Class.forName(className, false, classLoader);

            result.add(clazz);

        } catch (ClassNotFoundException
                 | LinkageError ignored) {
            // ロードできないクラスは無視
        }
    }
}
