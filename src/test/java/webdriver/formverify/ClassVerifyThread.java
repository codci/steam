package webdriver.formverify;

import org.openqa.selenium.NoSuchElementException;
import webdriver.BaseForm;
import webdriver.elements.BaseElement;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class ClassVerifyThread extends Thread {

    private static String classPath;
    private static webdriver.Logger logger = webdriver.Logger.getInstance();
    private File root;
    static List<String> classNames = new LinkedList<String>();

    public ClassVerifyThread(File root) {
        this.root = root;
    }

    @Override
    public void run() {
        if (root.exists()) {
            if (root.isDirectory()) {
                processDir();
            } else if (!root.getName().contains("$")
                    && root.getName().endsWith(".class")) {
                processClass();
            }
        }
    }

    public static void setClassPath(String absolutePath) {
        classPath = absolutePath;
    }

    public void printNames(FormVerifyJFrame frame) {
        frame.println("=====================  Found  =====================");
        for (String name : classNames) {
            frame.println("form:" + name);
        }
        classNames.clear();
    }

    private static Object lastform = null;

    public void checkForm(String form, FormVerifyJFrame frame) {
        frame.println("=====================  form:" + form + "  =====================");
        File classFile = new File(root.getAbsolutePath() + File.separator + form.replace(".", File.separator) + ".class");
        if (classFile.exists() && classFile.isFile()) {
            ModuleLoader loader = new ModuleLoader(classPath, ClassLoader.getSystemClassLoader());
            try {
                Class<?> clazz = loader.loadClass(form);
                if (BaseForm.class.isAssignableFrom(clazz)) {
                    lastform = clazz.newInstance();
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        try {
                            field.setAccessible(true);
                            if (BaseElement.class.isAssignableFrom(field.getType()) && field.get(lastform) != null) {
                                boolean exists = true;
                                try {
                                    BaseElement element = BaseElement.class.cast(field.get(lastform));
                                    exists = element.exists();
                                } catch (NoSuchElementException e) {
                                    logger.debug(this, e);
                                    exists = false;
                                } catch (Exception | AssertionError e) {
                                    logger.debug(this, e);
                                }
                                frame.println(field.getName() + ", exists:" + exists);
                            }
                        } catch (Exception | AssertionError e) {
                            logger.debug(this, e);
                        }
                    }
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        try {
                            method.setAccessible(true);
                            if (method.getGenericParameterTypes().length == 0) {
                                frame.println("method:" + method.getName());
                            }
                        } catch (Exception | AssertionError e) {
                            logger.debug(this, e);
                        }
                    }
                } else {
                    clazz = null;
                }
            } catch (Exception | AssertionError  e) {
                logger.debug(this, e);
            }
        }
    }

    public void callMethod(String methodName, FormVerifyJFrame frame) {
        frame.println("=====================  method:" + methodName + "  =====================");
        if (lastform != null) {
            Method[] methods = lastform.getClass().getDeclaredMethods();
            for (Method method : methods) {
                try {
                    method.setAccessible(true);
                    if (methodName.equals(method.getName())) {
                        frame.println("result:" + method.invoke(lastform));
                    }
                } catch (Exception e) {
                    logger.debug(this, e);
                    frame.println("error:" + e.getMessage());
                }
            }
        }
    }

    //////////////////
    // Private methods
    //////////////////

    private void processDir() {
        for (String fileNames : root.list()) {
            ClassVerifyThread thread = new ClassVerifyThread(new File(root.getAbsolutePath()
                    + File.separator + fileNames));
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.debug(this, e);
            }
        }
    }

    private void processClass() {
        ModuleLoader loader = new ModuleLoader(classPath, ClassLoader.getSystemClassLoader());
        try {
            String className = root.getAbsolutePath().substring(classPath.length() + 1, root.getAbsolutePath().length() - 6).replace(File.separator, ".");
            Class<?> clazz = loader.loadClass(className);
            try {
                if (BaseForm.class.isAssignableFrom(clazz)) {
                    Object obj = clazz.newInstance();
                    BaseForm.class.cast(obj).assertIsOpen();
                    classNames.add(className);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                logger.debug(this, e);
            }
        } catch (ClassNotFoundException e) {
            logger.debug(this, e);
        }
    }
}
