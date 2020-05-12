package com.huruwo.hposed.app;

import android.app.Activity;
import android.os.Bundle;


import com.huruwo.hposed.utils.LogXUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain {

    private XC_LoadPackage.LoadPackageParam loadPackageParam;

    public HookMain(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        this.loadPackageParam = loadPackageParam;
    }

    public void hookAppUi() {

        LogXUtils.e("xposed进入");

        Class aClass = XposedHelpers.findClass("android.app.Activity", loadPackageParam.classLoader);

        if(aClass!=null){
            XposedHelpers.findAndHookMethod(aClass, "onCreate", Bundle.class, new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Activity activity = (Activity) param.thisObject;
                    String class_name = activity.getClass().getName();
                    LogXUtils.e("界面包名"+class_name);
                    if ("com.xingin.xhs.index.v2.IndexActivityV2".equals(class_name)) {
                        new HookMainUi(loadPackageParam).HookUi();
                    }
                }

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                }
            });
        }else {
            LogXUtils.e("Class为null",true);
        }
    }

}
