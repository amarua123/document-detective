package tasks;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.google.common.reflect.ClassPath;
import org.reflections.*;
import org.reflections.scanners.MethodAnnotationsScanner;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.io.*;

public class ScanAnnotations {

    private static String annotationClassName = "annotations.ClassDocument";
    private static String annotationMethodName = "annotations.MethodDocument";

    public static void main(String[] args) {

        String filePath = "./src/main/resources/OutputDocument.txt";
        String packageName = "com.document.detective.detective";
        int i = 1;
        Set<Class<?>> classes = new HashSet<>();
        Set<Class<?>> annotationClassWithoutJavadoc = new HashSet<>();
        Set<Class<?>> javadocClassWithoutAnnotation = new HashSet<>();

        try(FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            PrintStream printStream = new PrintStream(fileOutputStream)) {

            System.setOut(printStream);

            System.out.println("Classes in the package:-");
            try {
                ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
                i = 1;
                for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClasses(packageName)) {
                    System.out.println(getNSpace(i++) + "|===>"+classInfo.getName());
                    classes.add(classInfo.load());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println();
            System.out.println("====================================================================");
            Reflections reflections2 = new org.reflections.Reflections(packageName, new MethodAnnotationsScanner());

            Reflections reflections = new org.reflections.Reflections(packageName);
            i = 1;
            Set<Class<?>> annotationClasses = reflections.getTypesAnnotatedWith(getClassForName(annotationClassName), true);
            System.out.println("Classes with @" + annotationClassName + ":");
            for (Class<?> annotatedClass : annotationClasses) {
                System.out.println(getNSpace(i++) + "|===>"+annotatedClass.getName());
            }
            System.out.println();
            System.out.println("====================================================================");
            i = 1;
            Set<Method> annotatedMethods = reflections2.getMethodsAnnotatedWith(getClassForName(annotationMethodName));
            System.out.println("Methods with @" + annotationMethodName + ":");
            for (Method annotationMethod : annotatedMethods) {
                System.out.println(getNSpace(i++) + "|===>"+annotationMethod);
            }
            System.out.println();
            System.out.println("====================================================================");
            System.out.println("Classes with javadoc:");
            for (Class<?> clazz : classes) {
                String classPath = clazz.getName().replace(".", "/") + ".java";
//            System.out.println(classPath);
                try {
                    String sourceCode = new String(Files.readAllBytes(Paths.get("src/main/java", classPath)));
                    CompilationUnit cu = StaticJavaParser.parse(sourceCode);

                    cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDeclaration -> {
                        if (classDeclaration.getComment().isPresent() && classDeclaration.getComment().get() instanceof JavadocComment) {
                            String javadoc = ((JavadocComment) classDeclaration.getComment().get()).getContent();
                            System.out.println("Class: " + clazz.getName());
                            System.out.println("Javadoc:\n" + javadoc);
                            System.out.println("--------------------------------------------------------------------");
                            if(!annotationClasses.contains(clazz)){
                                javadocClassWithoutAnnotation.add(clazz);
                            }
                        } else if(annotationClasses.contains(clazz)) {
                            annotationClassWithoutJavadoc.add(clazz);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("====================================================================");
            System.out.println("Methods with javadoc:");
            for (Class<?> clazz : classes) {
                String classPath = clazz.getName().replace(".", "/") + ".java";
                try {
                    String sourceCode = new String(Files.readAllBytes(Paths.get("src/main/java", classPath)));
                    CompilationUnit cu = StaticJavaParser.parse(sourceCode);

                    cu.findAll(MethodDeclaration.class).forEach(method -> {
                        if (method.getComment().isPresent() && method.getComment().get() instanceof JavadocComment) {
                            String javadoc = ((JavadocComment) method.getComment().get()).getContent();
                            System.out.println("Method: " + method.getDeclarationAsString());
                            System.out.println("Javadoc:\n" + javadoc);
                            System.out.println("--------------------------------------------------------------------");
                        }
                    });
                } catch (IOException | ParseProblemException e) {
                    e.printStackTrace();
                }
            }
            i = 1;
            System.out.println();
            System.out.println("====================================================================");
            System.out.println("Class with annotation but no javadoc:" );
            for (Class<?> clazz : annotationClassWithoutJavadoc) {
                System.out.println(getNSpace(i++) + "|===>"+ clazz.getName());
            }
            System.out.println();
            i = 1;
            System.out.println("====================================================================");
            System.out.println("Class with javadoc but no annotation:");
            for (Class<?> clazz : javadocClassWithoutAnnotation) {
                System.out.println(getNSpace(i++) + "|===>"+ clazz.getName());
            }
            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Class<? extends Annotation> getClassForName(String className){
        try{
            return (Class<? extends Annotation>) Class.forName(className);
        }catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load class: " + className, e);
        }
    }
    private static String getNSpace(int n){
        return " ".repeat(Math.max(1, n));
    }
}
