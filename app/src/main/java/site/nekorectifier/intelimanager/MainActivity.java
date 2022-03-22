package site.nekorectifier.intelimanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.snackbar.Snackbar;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import site.nekorectifier.intelimanager.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences preferences;
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidNetworking.initialize(getApplicationContext());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        server_live_check();

        binding.buttonUnlock.setOnClickListener(view -> AndroidNetworking.post(
                getURL(0))
                .addHeaders("token", preferences.getString("token", ""))
                .addHeaders("DEVICE_TYPE", Build.MODEL)
                .addHeaders("Date", format.format(new Date()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        /***
                         * 返回的示例应该如下:
                         * {
                         *     "result": "ok",
                         *     "reason": null
                         * }
                         */

                        try {
                            if (response.getString("result").equals("ok")) {
                                Snackbar.make(binding.getRoot(),
                                        "门已开",
                                        Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(binding.getRoot(),
                                        "出错了\n" + response.getString("reason"),
                                        Snackbar.LENGTH_LONG).show();
                            }
                            // 实际并不会用到这一块
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Snackbar.make(binding.getRoot(),
                                Objects.requireNonNull(anError.getErrorDetail()),
                                Snackbar.LENGTH_LONG)
                                .setAction("详细信息", view1 -> addDialog("code:" +
                                        anError.getErrorCode() +
                                        "\nresponse:" +
                                        anError.getResponse() +
                                        "\ndetail:" +
                                        anError.getErrorDetail())).show();
                    }
                }));

        binding.buttonJournal.setOnClickListener(view -> {
            //TODO 查看日志
            AndroidNetworking.get(getURL(1))
                    .addHeaders("token", preferences.getString("token", ""))
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONArray(new JSONArrayRequestListener() {
                        @Override
                        public void onResponse(JSONArray response) {
                            // 谁知道get的是什么 到时候再看
                        }

                        @Override
                        public void onError(ANError anError) {

                        }
                    });
        });


        if (!preferences.getBoolean("electricity_check_setup", false)) {
            Snackbar.make(binding.cardView, "点击按钮来设置电费查询", Snackbar.LENGTH_LONG)
                    .setAction("开始设置", view -> {
                        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                        startActivity(intent);
                    })
                    .show();
            // 具体实现上可能不够好
        }
    }

    void addDialog(String content) {
        //TODO 完全接管算了
        new MaterialDialog.Builder(MainActivity.this)
                .title("详细信息")
                .content(content)
                .neutralText("OK")
                .show();
    }

    void server_live_check() {
        ping(preferences.getString("MCU_IP", "192.168.2.20"));
    }

    String getURL(int type) {
        String base = "http://" + preferences.getString("MCU_IP", "192.168.2.20");
        switch (type) {
            case 0: //unlock
                return base + "/unlock";
            case 1: //getlog
                return base + "/getlog";
            default:
                break;
        }
        return "null";
    }

    void ping(String target) {
        Ping.onAddress(target)
                .setTimeOutMillis(5000)
                .setTimes(5)
                .doPing(new Ping.PingListener() {
                    @Override
                    public void onResult(PingResult pingResult) {
                        if (pingResult.isReachable) {
                            handler.sendEmptyMessage(0);
                        } else {
                            handler.sendEmptyMessage(1);
                        }
                    }

                    @Override
                    public void onFinished(PingStats pingStats) {
//                        Log.i(TAG, "onResult: " + pingStats.toString());
                    }

                    @Override
                    public void onError(Exception e) {
                        handler.sendEmptyMessage(1);
                        Snackbar.make(binding.getRoot(), e.toString(), Snackbar.LENGTH_LONG)
                                .setAction("详细信息", view ->
                                        addDialog(e.getLocalizedMessage())).show();
                    }
                });

    }

    private final Handler handler = new Handler(message -> {
        switch (message.what) {
            case 0:
                binding.status.setText("已连接");
                binding.address.setText(preferences.getString("MCU_IP", ""));
                break;
            case 1:
                binding.status.setText("未连接");
                binding.address.setText("0.0.0.0/0");
            default:
                break;
        }
        return false;
    });

    @Override
    protected void onResume() {
        super.onResume();
        server_live_check();
        // 处理设置后的ui相关更新
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}