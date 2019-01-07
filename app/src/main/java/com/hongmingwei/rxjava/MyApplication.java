package com.hongmingwei.rxjava;

import android.app.Application;

import com.hongmingwei.rxjava.error.ActivityHandlerManager;

/**
 * Created by Hongmingwei on 2018/5/18.
 * Email: 648600445@qq.com
 */

public class MyApplication extends Application {

    private static MyApplication mApplication;

    public synchronized static MyApplication getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        initData();
    }

    private void initData() {
        //当程序发生Uncaught异常的时候,由该类来接管程序,一定要在这里初始化
        ActivityHandlerManager.getInstance().init(this);
    }

}
