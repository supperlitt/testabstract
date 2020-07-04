package com.android.testxpabs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.testabstract.TestAb;
import com.android.testabstract.TestModel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class xposed_tool implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if(lpparam.packageName.equals("com.android.testabstract")){
            Log.i("tt===tt", "enter " + lpparam.packageName);
            findAndHookMethod("com.android.testabstract.MainActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    final Object pthis = param.thisObject;
                    hookTest2(pthis);
                }
            });
        }
    }

    private void hookFail(final Object pthis){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TestAb<TestModel> test = new TestAb<TestModel>() {
                        @Override
                        protected void onSuccess(TestModel paramT) {
                            Log.i("testAbProxy", "paramT " + paramT.msg);
                        }
                    };

                    XposedHelpers.callMethod(pthis, "Test", test);
                }
                catch (NoClassDefFoundError fe){
                    Log.i("tt===tt", "fe " + fe.getMessage());
                    fe.printStackTrace();
                }
                catch (Exception e){
                    Log.i("tt===tt", "e " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void hookTest1(final Object pthis){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 把当前APK，添加到，需要hookAPK的dexElements集合中
                    String path = "/data/app/com.android.testxpabs-1/base.apk";
                    if(!new File(path).exists()){
                        path = "/data/app/com.android.testxpabs-2/base.apk";
                    }

                    new LoadApkUtil().LoadApk(path, (Context)pthis);

                    Log.i("tt===tt", "loader " + pthis.getClass().getClassLoader());

                    Class<?> class_ = pthis.getClass().getClassLoader().loadClass("com.android.testxpabs.TestTestAb");
                    Log.i("tt===tt", "class_ " + class_);

                    Object obj_ = class_.newInstance();
                    Log.i("tt===tt", "obj_ " + obj_);

                    Class<?> class_TestModel = pthis.getClass().getClassLoader().loadClass("com.android.testabstract.TestModel");
                    findAndHookMethod("com.android.testxpabs.TestTestAb", pthis.getClass().getClassLoader(),
                            "onSuccess", class_TestModel, new XC_MethodReplacement() {
                                @Override
                                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                    Log.i("tt===tt", "hook replace " + param.args[0]);
                                    return null;
                                }
                            });

                    // 把当前APK加入
                    XposedHelpers.callMethod(pthis, "Test", obj_);
                }
                catch (NoClassDefFoundError fe){
                    Log.i("tt===tt", "fe " + fe.getMessage());
                    fe.printStackTrace();
                }
                catch (Exception e){
                    Log.i("tt===tt", "e " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void hookTest2(final Object pthis){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 这里针对art，直接这样使用，dalvik可能就不一样了
                    String path = "/data/app/com.android.testxpabs-1/base.apk";
                    if(!new File(path).exists()){
                        path = "/data/app/com.android.testxpabs-2/base.apk";
                    }

                    PathClassLoader pathClassLoader = new PathClassLoader(path, pthis.getClass().getClassLoader());
                    Class<?> class_ = pathClassLoader.loadClass("com.android.testxpabs.TestTestAb");
                    Object obj_ = class_.newInstance();
                    Class<?> class_TestModel = pathClassLoader.loadClass("com.android.testabstract.TestModel");

                    findAndHookMethod("com.android.testxpabs.TestTestAb", pathClassLoader,
                            "onSuccess", class_TestModel, new XC_MethodReplacement() {
                                @Override
                                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                    Log.i("tt===tt", "hook replace " + param.args[0]);
                                    return null;
                                }
                            });

                    // 把当前APK加入
                    XposedHelpers.callMethod(pthis, "Test", obj_);
                }
                catch (NoClassDefFoundError fe){
                    Log.i("testAbProxy", "fe " + fe.getMessage());
                    fe.printStackTrace();
                }
                catch (Exception e){
                    Log.i("testAbProxy", "e " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void hookInterface(final ClassLoader classLoader, final Object pthis){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class<?> class_ITest = classLoader.loadClass("com.android.testabstract.ITest");
                    Object obj_proxy = Proxy.newProxyInstance(classLoader, new Class<?>[]{class_ITest}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                            Log.i("testProxy", "method " + method.getName());
                            Log.i("testProxy", "objects.length " + (objects != null ? objects.length : "null"));

                            return null;
                        }
                    });

                    // 把当前APK加入
                    XposedHelpers.callMethod(pthis, "Test2", obj_proxy);
                }
                catch (NoClassDefFoundError fe){
                    Log.i("testProxy", "fe " + fe.getMessage());
                    fe.printStackTrace();
                }
                catch (Exception e){
                    Log.i("testProxy", "e " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
