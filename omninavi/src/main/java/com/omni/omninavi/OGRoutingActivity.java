package com.omni.omninavi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by wiliiamwang on 15/11/2017.
 */

public class OGRoutingActivity extends Activity {

//    @BindView(R.id.open_map)
//    AppCompatButton openMapBtn;
//    @BindView(R.id.open_map_with_navi)
//    Button openMapWithNavi;

    public static void startOGRoutingActivity(Activity previousActivity) {
        Intent intent = new Intent();
        intent.setClass(previousActivity, OGRoutingActivity.class);
        previousActivity.startActivity(intent);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_routing);

//        ButterKnife.bind(this);

        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(mWebViewClient);
        webView.loadUrl("https://doit.utobonus.com/anymap");
    }

//    @OnClick({R.id.open_map, R.id.open_map_with_navi})
//    public void onClick(View view){
//        switch (view.getId()) {
//            case R.id.open_map:
//                startActivity(new Intent(this, OGMapsActivity.class));
//                break;
//
//            case R.id.open_map_with_navi:
//                OGMapsActivity.navigationTo(this, "111", "name", "3F");
//                break;
//        }
//    }

//    public void clickBtn(View view) {
//        startActivity(new Intent(this, OGMapsActivity.class));
//    }
//
//    public void clickNaviBtn(View view) {
//        OGMapsActivity.navigationTo(this, "111", "name", "3F");
//    }

}
