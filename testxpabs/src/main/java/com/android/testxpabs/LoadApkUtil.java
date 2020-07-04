package com.android.testxpabs;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dalvik.system.DexFile;

public class LoadApkUtil {
    private static String tag = "tt===tt";

    public void LoadApk(String apk_path, Context context) {
        try {
            InputStream is = new FileInputStream(apk_path);
            byte[] data = new byte[is.available()];
            is.read(data, 0, data.length);
            is.close();

            File plugin_dir = context.getDir("plugin", Context.MODE_PRIVATE);
            if (!plugin_dir.exists()) {
                plugin_dir.mkdirs();
            }

            String apkPath = plugin_dir + "/a.apk";
            Log.i(tag, apkPath);
            WriteFile(apkPath, data);

            List<File> additionalClassPathEntries = new ArrayList<>();

            // 插件
            additionalClassPathEntries.add(new File(apkPath));

            ClassLoader loader = context.getClassLoader();
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            Log.i(tag, "dexPathList " + dexPathList);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            if(Build.VERSION.SDK_INT >= 23) {
                expandFieldArray(
                        dexPathList,
                        "dexElements",
                        makeDexElements(dexPathList, new ArrayList<File>(
                                        additionalClassPathEntries), plugin_dir,
                                suppressedExceptions, loader));
            }else if(Build.VERSION.SDK_INT >= 19){
                expandFieldArray(
                        dexPathList,
                        "dexElements",
                        makeDexElements19(dexPathList, new ArrayList<File>(
                                        additionalClassPathEntries), plugin_dir,
                                suppressedExceptions, loader));
            }

            if (suppressedExceptions.size() > 0) {
                for (IOException e : suppressedExceptions) {
                    Log.w(tag, "Exception in makeDexElement", e);
                }
                Field suppressedExceptionsField = findField(loader,
                        "dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions = (IOException[]) suppressedExceptionsField
                        .get(loader);

                if (dexElementsSuppressedExceptions == null) {
                    dexElementsSuppressedExceptions = suppressedExceptions
                            .toArray(new IOException[suppressedExceptions
                                    .size()]);
                } else {
                    IOException[] combined = new IOException[suppressedExceptions
                            .size() + dexElementsSuppressedExceptions.length];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions, 0,
                            combined, suppressedExceptions.size(),
                            dexElementsSuppressedExceptions.length);
                    dexElementsSuppressedExceptions = combined;
                }

                suppressedExceptionsField.set(loader,
                        dexElementsSuppressedExceptions);
            }
            Log.i(tag, "加载完成");
            dexPathList = pathListField.get(loader);
            Log.i(tag, "new dexPathList " + dexPathList);
        } catch (Exception e) {
            Log.i(tag, "ex " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void WriteFile(String filePath, byte[] data) {
        try {
            // ShellUtils.setFolderPermissions(filePath);
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
                // ShellUtils.setFolderPermissions(filePath);
            }

            OutputStream fout = new FileOutputStream(filePath);
            fout.write(data);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Method findMethod(Object instance, String name,
                                     Class<?>... parameterTypes) throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz
                .getSuperclass()) {
            try {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    String m_name = method.getName();
                    Log.i(tag, "m_name " + m_name);
                    if (m_name.equals(name)) {
                        if (!method.isAccessible()) {
                            method.setAccessible(true);
                        }

                        return method;
                    }
                }

                throw new NoSuchMethodException();
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }

        throw new NoSuchMethodException("Method " + name + " with parameters "
                + Arrays.asList(parameterTypes) + " not found in "
                + instance.getClass());
    }

    private static Object[] makeDexElements(Object dexPathList,
                                            ArrayList<File> files, File optimizedDirectory,
                                            ArrayList<IOException> suppressedExceptions, ClassLoader loader)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException, InstantiationException {
        Method makeDexElements = null;
        if (Build.VERSION.SDK_INT >= 26) {
            makeDexElements = findMethod(dexPathList, "makePathElements",
                    List.class, File.class, List.class, ClassLoader.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files,
                    optimizedDirectory, suppressedExceptions, loader);
        } else if (Build.VERSION.SDK_INT >= 24) {
            makeDexElements = findMethod(dexPathList, "makeElements",
                    List.class, File.class, List.class, boolean.class, ClassLoader.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files,
                    optimizedDirectory, suppressedExceptions, false, loader);
        } else {
            makeDexElements = findMethod(dexPathList, "loadDexFile", File.class, File.class);
            DexFile dex = (DexFile)makeDexElements.invoke(dexPathList, files.get(0), optimizedDirectory);

            Class<?> class_ = findField(dexPathList, "dexElements").getType().getComponentType();
            Class[] v2 = new Class[4];
            v2[0] = File.class;
            v2[1] = Boolean.TYPE;
            v2[2] = File.class;
            v2[3] = DexFile.class;
            Constructor sElementConstructor = class_.getConstructor(v2);
            return new Object[] { sElementConstructor.newInstance(new File(""), false, files.get(0), dex) };
        }
    }

    private static Object[] makeDexElements19(Object dexPathList,
                                            ArrayList<File> files, File optimizedDirectory,
                                            ArrayList<IOException> suppressedExceptions, ClassLoader loader)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException, InstantiationException {
        Method makeDexElements = null;
            makeDexElements = findMethod(dexPathList, "makeDexElements",
                    ArrayList.class, File.class, ArrayList.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files,
                    optimizedDirectory, suppressedExceptions);
    }

    private static void expandFieldArray(Object instance, String fieldName,
                                         Object[] extraElements) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field jlrField = findField(instance, fieldName);
        Object[] original = (Object[]) jlrField.get(instance);
        Object[] combined = (Object[]) Array.newInstance(original.getClass()
                .getComponentType(), original.length + extraElements.length);
        System.arraycopy(original, 0, combined, 0, original.length);
        System.arraycopy(extraElements, 0, combined, original.length,
                extraElements.length);
        jlrField.set(instance, combined);
    }

    private static Field findField(Object instance, String name)
            throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz
                .getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in "
                + instance.getClass());
    }
}
