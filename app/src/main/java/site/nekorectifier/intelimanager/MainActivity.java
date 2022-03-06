package site.nekorectifier.intelimanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        preferences = this.getSharedPreferences("app", MODE_PRIVATE);

        binding.buttonUnlock.setOnClickListener(view -> {
            //TODO 发送HTTP请求

            JSONObject object = new JSONObject();
            try {
                object.put("token",
                        "226f76b55acb49701e06ded1d95165d179458f6fc37f5c6fc760ae30dec1c378");
                object.put("DEVICE_TYPE", Build.MODEL);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            AndroidNetworking.post(
                    preferences.getString("MCU_IP", "http://192.168.2.243"))
                    .addJSONObjectBody(object)
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
                                            Snackbar.LENGTH_LONG).show();
                                } else {
                                    Snackbar.make(binding.getRoot(),
                                            "出错了\n" + object.getString("reason"),
                                            Snackbar.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Snackbar.make(binding.getRoot(), Objects.requireNonNull(anError.getLocalizedMessage()), Snackbar.LENGTH_LONG).show();
                        }
                    });
        });

        binding.buttonJournal.setOnClickListener(view -> {
            //TODO 查看日志
            AndroidNetworking.get(preferences.getString("MCU_IP", "http://192.168.2.243") + "/getLog")
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

    @Override
    protected void onResume() {
        super.onResume();
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