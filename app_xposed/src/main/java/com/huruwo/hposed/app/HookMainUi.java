package com.huruwo.hposed.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.huruwo.hposed.utils.GsonUtils;
import com.huruwo.hposed.utils.LogXUtils;
import com.virjar.sekiro.api.SekiroClient;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroRequestHandler;
import com.virjar.sekiro.api.SekiroResponse;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static android.widget.LinearLayout.VERTICAL;

/**
 * @author
 * @date 2020/1/19 0019
 * @action
 **/
public class HookMainUi {

    private XC_LoadPackage.LoadPackageParam loadPackageParam;
    private boolean isResit = false;

    public HookMainUi(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        this.loadPackageParam = loadPackageParam;
        HookUi();

    }


    public void HookUi() {
        // hook Ui  需要改的是页面类
        Class aClass = XposedHelpers.findClass("android.app.Activity", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(aClass, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Activity activity = (Activity) param.thisObject;
                String class_name = activity.getClass().getName();
                LogXUtils.e(class_name, false);
                if ("com.xingin.login.activity.WelcomeActivity".equals(class_name)) {
                    activity.startActivity(new Intent(activity, XposedHelpers.findClass("com.xingin.xhs.index.v2.IndexActivityV2", loadPackageParam.classLoader)));
                } else if ("com.xingin.xhs.index.v2.IndexActivityV2".equals(class_name)) {
                    FrameLayout frameLayout = (FrameLayout) activity.getWindow().getDecorView().getRootView();
                    frameLayout.setBackgroundColor(Color.RED);

                    LinearLayout linearLayout = new LinearLayout(activity);
                    LinearLayout.LayoutParams ll_layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    linearLayout.setOrientation(VERTICAL);
                    linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                    linearLayout.setLayoutParams(ll_layoutParams);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(4, 10, 4, 4);

                    TextView imageView = new TextView(activity);
                    imageView.setBackgroundColor(Color.GRAY);
                    imageView.setGravity(Gravity.CENTER);
                    imageView.setText("长连接:WebSocket" + "\n设备号:" + "  抖音版本:" + AppUtils.getAppVersionName() + "\n抓取软件版本:" + AppUtils.getAppVersionName("com.xingin.xhs"));
                    LinearLayout.LayoutParams im_layoutParams = new LinearLayout.LayoutParams(200, 200);
                    im_layoutParams.setMargins(4, 80, 4, 4);
                    imageView.setLayoutParams(im_layoutParams);

                    linearLayout.addView(imageView);
                    frameLayout.addView(linearLayout);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 页面按钮点击测试区
                        }
                    });


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            xxx();
                        }
                    }).start();


                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

            }
        });
    }

    private void xxx() {
        // 注册长连接服务  可修改 设备id |分组(xhs) | action(get_user_info)
        // 调用url: http://111.74.21.150:5601/asyncInvoke?invoke_timeOut=15000&group=xhs&action=get_user_info&bindClient=xhs0001&uid=5a13b53b4eacab190f3d0667
        if (!isResit) {
            LogXUtils.e("注册长连接");
            SekiroClient sekiroClient = SekiroClient.start("111.74.21.150", "xhs0001", "xhs");
            isResit = true;
            sekiroClient.registerHandler("get_user_info", new SekiroRequestHandler() {
                @Override
                public void handleRequest(SekiroRequest sekiroRequest, final SekiroResponse sekiroResponse) {
                    String uid = sekiroRequest.getString("uid");
                    ClassLoader classLoader = loadPackageParam.classLoader;
                    LogXUtils.e("开始主动调用" + uid, false);
                    Class UserServices = XposedHelpers.findClass("com.xingin.matrix.profile.services.UserServices", classLoader);
                    Class C17972a_cls = XposedHelpers.findClass("com.xingin.skynet.a$a", classLoader);
                    Object m33702a = XposedHelpers.callStaticMethod(C17972a_cls, "a", UserServices);
                    Object getUserInfo = XposedHelpers.callMethod(m33702a, "getUserInfo", uid);
                    Class AndroidSchedulers_cls = XposedHelpers.findClass("io.reactivex.a.b.a", classLoader);
                    Object a = XposedHelpers.callStaticMethod(AndroidSchedulers_cls, "a");
                    Object mo51435a = XposedHelpers.callMethod(getUserInfo, "a", a);
                    Class aa = XposedHelpers.findClass("com.uber.autodispose.a.c", classLoader);
                    //动态代理传入
                    Object consumerObject = Proxy.newProxyInstance(classLoader, new Class[]{aa}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if ("onNext".equals(method.getName())) {
                                Object o = args[0];
                                String data = GsonUtils.toJson(o);
                                LogXUtils.e(data);
                                sekiroResponse.success(data);
                            }
                            return null;
                        }
                    });
                    XposedHelpers.callMethod(mo51435a, "subscribe", consumerObject);
                    //LogXUtils.e("结束主动调用", false);
                }
            });
        } else {
            LogXUtils.e("已经注册");
        }


    }
}
