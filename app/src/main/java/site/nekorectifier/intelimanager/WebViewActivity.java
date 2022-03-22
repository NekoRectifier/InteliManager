package site.nekorectifier.intelimanager;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;

import com.google.android.material.snackbar.Snackbar;

import site.nekorectifier.intelimanager.databinding.ActivityWebViewBinding;

public class WebViewActivity extends AppCompatActivity {

    ActivityWebViewBinding binding;
    // String url = "http://cas.huat.edu.cn/authserver/login";
    String url = "http://cas.huat.edu.cn/authserver/login";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWebViewBinding.inflate(getLayoutInflater());
        setSupportActionBar(binding.toolbar);
        setContentView(R.layout.activity_web_view);

        WebSettings settings = binding.webview.getSettings();
        settings.setSupportZoom(false);
        settings.setUseWideViewPort(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptEnabled(true);

        binding.webview.loadUrl(url);

        Snackbar.make(binding.webFab, "使用账号登录后再按按钮", Snackbar.LENGTH_LONG).show();

        binding.webFab.setOnClickListener(view -> {
            //TODO 获取cookie并做检测

            String cookies = CookieManager.getInstance().getCookie(url);
            Log.i("WebViewActivity", "onCreate: " +cookies);


        });

    }
}