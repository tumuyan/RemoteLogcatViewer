package com.zzzmode.android.remotelogcatsample;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.zzzmode.android.remotelogcat.LogcatRunner;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private boolean waitInit = true;
    private boolean logRunning = false;
    private String wsLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // 不是必须的。此方法用于 模拟输出；展示websocket实际链接
    private void startLog() {
        if (logRunning) {
            return;
        }
        logRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                int i = 0;
                while (logRunning) {
                    if (random.nextBoolean()) {
                        Log.e("testlog", "run --> " + i);
                    } else {
                        Log.w("testlog", "run --> " + i);

                    }
//                    test();test();test();test();test();test();test();
//                    test();test();test();test();test();test();test();
                    SystemClock.sleep(random.nextInt(5000) + 100);
                    i++;
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    if (!waitInit) {
                        wsLink = LogcatRunner.getWebSocketLink();
                        if (wsLink.length() > 2) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView) findViewById(R.id.textView)).setText("ws://" + wsLink);
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }).start();

    }

    private static void test() {
        try {
            throw new RuntimeException("----");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 授权并完成初始化
    @Override
    protected void onStart() {
        super.onStart();

        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        if (waitInit) {
                            try {
                                LogcatRunner.getInstance()
                                        .config(LogcatRunner.LogConfig.builder()
                                                .setWsCanReceiveMsg(false)
                                                .setLogFilePrefix("test")
                                                .write2File(true))
                                        .with(getApplicationContext())
                                        .start();
                                waitInit = false;

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            startLog();

                        }

                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        Toast.makeText(getApplicationContext(), "存储和分享Log需要存储权限，否则应用无法正常工作", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .start();

    }

    @Override
    protected void onStop() {
        super.onStop();
        logRunning = false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 完成销毁
        LogcatRunner.Stop();
    }
}
