package com.zzzmode.android.remotelogcat;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*
通过toast和logcat的方式展示debug消息
(logcat_level>2才会toast消息)
与toast相比，可以主动输出到屏幕上去
并且可以快速对全局进行切换而不需要读取配置文件
 */
public class LogToaster {
    private static LogToaster INSTANCE = null;
    private Context context;
    private boolean show_toast = false;
    private boolean disable = false;
    private boolean skip_toast = true;

    private int logcat_level = 2;
    private String tag = "";
    private String last_toast_text = "";
    private long last_toast_death_time = 0;
    private int logcat_live_time = 2000;

//    private ConcurrentLinkedQueue<String> toast_cache=new ConcurrentLinkedQueue<>();

    private LogToaster(Context context) {
        this.context = context;
    }

    public static LogToaster getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LogToaster(context);
        }
        return INSTANCE;
    }

    /*    开关toast和log  */
    public void set(boolean work_mode) {
        disable = !work_mode;
    }

    /*    设置工作参数
     *   boolean show_toast, 是否toast
     *   boolean skip_toast,  如果连续toast相同内容，是否跳过
     *   int logcat_level,
     *   String tag
     */

    public void set(boolean show_toast, boolean skip_toast, int logcat_live_time, int logcat_level, String tag) {

        this.show_toast = show_toast;
        this.skip_toast = skip_toast;
        this.logcat_live_time = logcat_live_time;
        this.logcat_level = logcat_level;
        this.tag = tag;
    }

    public void log(String text) {
        this.log(text, tag, logcat_level);
    }

    public void log(String text, String tag) {
        this.log(text, tag, logcat_level);
    }

    public void log(String text,  int logcat_level) {
        this.log(text, tag, logcat_level);
    }

    public void log(String text, String tag, int logcat_level) {

        if (disable)
            return;
        switch (logcat_level) {
            case 1:
                Log.v(tag, text);
                break;
            case 2:
                Log.d(tag, text);
                break;
            case 3:
                Log.i(tag, text);
                break;
            case 4:
                Log.w(tag, text);
                break;
            case 5:
                Log.e(tag, text);
                break;
        }

        if (show_toast && logcat_level > 2) {
            if (skip_toast) {
                // 当前消息的时间
                long time_now = System.currentTimeMillis();
                // 当前消息去除空格和数字
                String toast = text.replaceAll("\\d+", "").replaceAll("\\s+", "");

                if (last_toast_text.equals(toast) && last_toast_death_time > time_now) {
                    // 如果有重复消息，跳过并不保存
                    return;
                }
                // 如果没有重复消息就保存
                last_toast_text = toast;
                last_toast_death_time = time_now + logcat_live_time;

            }
            if (tag.length() > 0)
                run_toast_thread(tag + ":" + text);
            else
                run_toast_thread(text);
        }

    }

private void run_toast_thread(final String text){
    ExecutorService executor = Executors.newSingleThreadExecutor();
    FutureTask<String> future = new FutureTask<String>(new Callable<String>() {//使用Callable接口作为构造参数
                public String call() {
                    Looper.prepare();
//                    Log.w("run_toast_thread toast",text);
                    Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
               //     TastyToast.makeText(context.getApplicationContext(), "Hello World !", TastyToast.LENGTH_LONG, TastyToast.WARNING);
                    Looper.loop();
                    //真正的任务在这里执行，这里的返回值类型为String，可以为任意类型
                    return "";
                }});
    executor.execute(future);
    //在这里可以做别的任何事情
    try {
        String result = future.get(5000, TimeUnit.MILLISECONDS);
        //取得结果，同时设置超时执行时间为5秒。同样可以用future.get()，不设置执行超时时间取得结果
    } catch (InterruptedException e) {
        future.cancel(true);
    } catch (ExecutionException e) {
        future.cancel(true);
    } catch (TimeoutException e) {
        future.cancel(true);
    } finally {
        executor.shutdown();
//        Log.e("run_toast_thread end",text);
    }
}


}
