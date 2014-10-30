
package koth.system;

import sun.net.www.protocol.file.FileURLConnection;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Provides helpers for loading and instancing classes.
 */
public final class ClassManager {

    private ClassManager() {}

    /*
        These methods are used to dynamically load JAR files.
        Original code by Allain Lalonde on StackOverflow
        http://stackoverflow.com/questions/60764/how-should-i-load-jars-dynamically-at-runtime
     */

    /**
     * Add a new JAR to class loader.
     */
    public static void add(String path) throws IOException {
        add(new File(path));
    }

    /**
     * Add a new JAR to class loader.
     */
    public static void add(File path) throws IOException {
        add(path.toURI().toURL());
    }

    /**
     * Add a new JAR to class loader.
     */
    public static void add(URL path) throws IOException {
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;
        try {
            @SuppressWarnings("unchecked")
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, path);
        } catch (Exception e) {
            throw new IOException("Error, could not add URL to system classloader", e);
        }
    }

    /*
        These methods are used to find classes in loaded resources.
        Original code by BrainStone on StackOverflow
        http://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection
     */

    private static void checkDirectory(File directory, String pckgname, ArrayList<Class<?>> classes) throws ClassNotFoundException {
        File tmpDirectory;
        if (directory.exists() && directory.isDirectory()) {
            String[] files = directory.list();
            for (String file : files) {
                if (file.endsWith(".class")) {
                    try {
                        classes.add(Class.forName(pckgname + '.' + file.substring(0, file.length() - 6)));
                    } catch (NoClassDefFoundError e) {
                        // do nothing. this class hasn't been found by the loader, and we don't care.
                    }
                } else if ((tmpDirectory = new File(directory, file)).isDirectory())
                    checkDirectory(tmpDirectory, pckgname + "." + file, classes);
            }
        }
    }

    private static void checkJarFile(JarURLConnection connection, String pckgname, ArrayList<Class<?>> classes) throws ClassNotFoundException, IOException {
        JarFile jarFile = connection.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();
        String name;
        for (JarEntry jarEntry; entries.hasMoreElements() && ((jarEntry = entries.nextElement()) != null);) {
            name = jarEntry.getName();
            if (name.contains(".class")) {
                name = name.substring(0, name.length() - 6).replace('/', '.');
                if (name.contains(pckgname))
                    classes.add(Class.forName(name));
            }
        }
    }

    /**
     * Get all classes of given package, using current <code>ClassLoader</code>.
     */
    public static List<Class<?>> getClasses(String pckgname) throws ClassNotFoundException {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null)
                throw new ClassNotFoundException("Can't get class loader.");
            Enumeration<URL> resources = cld.getResources(pckgname.replace('.', '/'));
            URLConnection connection;
            for (URL url; resources.hasMoreElements() && ((url = resources.nextElement()) != null);) {
                try {
                    connection = url.openConnection();
                    if (connection instanceof JarURLConnection) {
                        checkJarFile((JarURLConnection) connection, pckgname, classes);
                    } else if (connection instanceof FileURLConnection) {
                        try {
                            checkDirectory(new File(URLDecoder.decode(url.getPath(), "UTF-8")), pckgname, classes);
                        } catch (UnsupportedEncodingException e) {
                            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)", e);
                        }
                    } else
                        throw new ClassNotFoundException(pckgname + " (" + url.getPath() + ") does not appear to be a valid package");
                } catch (IOException e) {
                    throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname, e);
                }
            }
        } catch (NullPointerException e) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)", e);
        } catch (IOException e) {
            throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname, e);
        }
        return classes;
    }

    /**
     * Builder for classes of type <code>T</code>.
     */
    public static final class Factory<T> {

        private final Class<T> clazz;
        private final String name;

        /**
         * Create a new factory for given class with given name.
         */
        public Factory(Class<T> clazz, String name) {
            if (clazz == null || name == null)
                throw new NullPointerException();
            this.clazz = clazz;
            this.name = name;
        }

        /**
         * Get underlying class object.
         */
        public Class<T> getClazz() {
            return clazz;
        }

        /**
         * Get given name.
         */
        public String getName() {
            return name;
        }

        /**
         * Create a new instance of this class.
         */
        public T create() {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to instanciate " + clazz + "!", e);
            }
        }

        @Override
        public boolean equals(Object o) {
            return !(o == null || o.getClass() != Factory.class) && equals((Factory<?>)o);
        }

        public boolean equals(Factory<?> o) {
            return o != null && clazz.equals(o.clazz);
        }

        @Override
        public int hashCode() {
            return clazz.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }

    }

    /**
     * Get subclasses of specified type with public empty constructor, in given package.
     */
    public static <T> Map<String, Factory<T>> getSubclasses(Class<T> type, String pckgname) {
        Map<String, Factory<T>> factories = new TreeMap<String, Factory<T>>();
        try {
            for (Class<?> c : getClasses(pckgname)) {
                if (!type.isAssignableFrom(c))
                    continue;
                try {
                    c.getConstructor();
                } catch (Exception e) {
                    continue;
                }
                String name = c.getCanonicalName();
                if (name == null)
                    continue;
                name = name.substring(pckgname.length() + 1);
                @SuppressWarnings("unchecked")
                Factory<T> f = new Factory<T>((Class<T>)c, name);
                factories.put(name, f);
            }
        } catch (ClassNotFoundException e) {}
        return factories;
    }

    /**
     * Create a list of classes.
     */
    public static <T> List<T> create(List<Factory<T>> factories) {
        List<T> result = new ArrayList<T>();
        for (Factory<T> f : factories)
            result.add(f.create());
        return result;
    }

}
