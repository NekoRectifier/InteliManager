package site.nekorectifier.intelimanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import site.nekorectifier.intelimanager.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences preferences;
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        preferences = this.getSharedPreferences("app",MODE_PRIVATE);


        binding.buttonUnlock.setOnClickListener(view -> {
           //TODO 发送HTTP请求


        });

        binding.buttonJournal.setOnClickListener(view -> {
           //TODO 查看日志
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

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.POST, "192.168.2.243", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}