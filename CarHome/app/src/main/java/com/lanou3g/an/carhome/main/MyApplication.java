package com.lanou3g.an.carhome.main;

import android.app.Application;
import android.content.Context;

import com.lanou3g.an.carhome.R;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by anfeng on 16/5/17.
 */
public class MyApplication extends Application {

    private static Context context;

    //Application创建的原因是因为我们需要一个属于自己的大"环境"(context)
    //保证自己的APP拥有单独的context对象

    //第一个生命周期中,我们队context赋值
    @Override
    public void onCreate() {
        super.onCreate();
        //this代表当前的环境
        context = getApplicationContext();

        JPushInterface.setDebugMode(true);    // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);            // 初始化 JPush
    }

    //对外提供一个方法,这个方法就是让别的类获取自己的context对象
    public static Context getContext() {
        return context;
    }

    public static void changeTTT() {
        context.setTheme(R.style.NightTheme);
    }
}
