package com.hongmingwei.rxjava.error;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 继承系统默认的为捕获异常的处理类，由该类来接管程序
 * Created by Hongmingwei on 2018/5/18.
 * Email: 648600445@qq.com
 */

public class ActivityHandlerManager implements Thread.UncaughtExceptionHandler {
    /**
     * TAG
     */
    private static final String TAG = ActivityHandlerManager.class.getSimpleName();
    /**
     * Params
     */
    private static ActivityHandlerManager instance;
    private Context mContext;
    private Thread.UncaughtExceptionHandler mHandler;


    private ActivityHandlerManager(){

    }

    public static ActivityHandlerManager getInstance(){
        if (instance == null){
            instance = new ActivityHandlerManager();
        }
        return instance;
    }

    /**
     * 初始化
     * @param context
     */
    public void init(Context context){
        mContext = context;
        Log.e(TAG, "init: ======");
        //获取未捕获的异常
        mHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置Manager为程序默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当监听的Activity发生异常时会此方法会监听到
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!handleException(e) && mHandler != null){
            mHandler.uncaughtException(t, e);
        }
    }

    /**
     * 自定义错误处理，收集错误信息
     * @param e
     * @return
     */
    private boolean handleException(Throwable e){
        if (e == null || mContext == null){
            return false;
        }
        final String report = getAccidentReport(mContext, e);
        Log.e(TAG, "handleException: ============" + report);
        new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                File file = setSaveFile(report);
//                sendAppCrashReport(mContext, report, file);
                Looper.loop();
            }
        }.start();
        return true;
    }

    /**
     * 获取APP崩溃异常报告
     * @param context
     * @param e
     * @return
     */
    private String getAccidentReport(Context context, Throwable e){
        PackageInfo info = getPackageInfo(context);
        StringBuffer sb = new StringBuffer();
        sb.append("Version:" + info.versionName + "(" + info.versionCode+")\r\n");
        sb.append("Android:" + Build.VERSION.RELEASE + "(" + Build.MODEL + ")\r\n");
        sb.append("Exception:" + e.getMessage()+"\r\n");
        StackTraceElement[] elements = e.getStackTrace();
        for (int i = 0; i < elements.length; i++){
            sb.append(elements[i].toString() + "\r\n");
        }
        return sb.toString();
    }

    /**
     * 获取app安装包信息
     * @param context
     * @return
     */
    private PackageInfo getPackageInfo(Context context){
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (info == null){
            info = new PackageInfo();
        }
        return info;
    }


    private File setSaveFile(String report){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateFormat.format(new Date());
        String fileName = "crash-" + time + "-" + System.currentTimeMillis();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            try {
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "crash");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                Log.e(TAG, "setSaveFile: ===========" + dir + "====" + fileName);
                File file = new File(dir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(report.toString().getBytes());
                fos.close();
                return file;
            } catch (Exception e){
                Log.e(TAG, "setSaveFile error ====" + e.getMessage());
            }
        }
        return null;
    }

    private void sendAppCrashReport(final Context context, final String crashReport, final File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("出现错误")
                .setMessage("Activity出现错误")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {

                                    //这以下的内容，只做参考，因为没有服务器
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    String[] tos = {"hongmingwei89@gmail.com"};
                                    intent.putExtra(Intent.EXTRA_EMAIL, tos);

                                    intent.putExtra(Intent.EXTRA_SUBJECT,
                                           " Android客户端 - 错误报告");
                                    if (file != null) {
                                        intent.putExtra(Intent.EXTRA_STREAM,
                                                Uri.fromFile(file));
                                        intent.putExtra(Intent.EXTRA_TEXT,
                                                "请将此错误报告发送给我，以便我尽快修复此问题，谢谢合作！"
                                        );
                                    } else {
                                        intent.putExtra(Intent.EXTRA_TEXT,
                                                "请将此错误报告发送给我，以便我尽快修复此问题，谢谢合作！"
                                                + crashReport);
                                    }
                                    intent.setType("text/plain");
                                    intent.setType("message/rfc882");
                                    Intent.createChooser(intent, "Choose Email Client");
                                    context.startActivity(intent);

                                } catch (Exception e) {
                                    Log.e(TAG, "error:" + e.getMessage());
                                } finally {
                                    dialog.dismiss();
                                    // 退出
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(1);
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                // 退出
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                            }
                        });

        AlertDialog dialog = builder.create();
        //需要的窗口句柄方式，没有这句会报错的
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }
}
