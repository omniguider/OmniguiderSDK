package com.omni.navisdkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.omni.navisdk.HomeActivity;
import com.omni.navisdk.NaviSDKActivity;

public class MainActivity extends AppCompatActivity {

    private static final String ARG_KEY_DOMAIN_NAME = "arg_key_domain_name";
    private static final String ARG_KEY_MAP_BEARING = "arg_key_map_bearing";
    private static final String ARG_KEY_AUTO_HEADING = "arg_key_auto_heading";
    private static final String ARG_KEY_ENCRYPT_KEY = "arg_key_encrypt_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ((TextView) findViewById(R.id.activity_main_domain_name)).setText("https://nlpiapi.omniguider.com/");

        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra(ARG_KEY_DOMAIN_NAME, "https://navi.taipei/");
        intent.putExtra(ARG_KEY_ENCRYPT_KEY, "doitapp://");
        intent.putExtra(ARG_KEY_MAP_BEARING, 0f);
        intent.putExtra(ARG_KEY_AUTO_HEADING, true);
        startActivity(intent);

        findViewById(R.id.activity_main_nlpi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NaviSDKActivity.class);
                intent.putExtra(ARG_KEY_DOMAIN_NAME, "https://nlpiapi.omniguider.com/");
                intent.putExtra(ARG_KEY_ENCRYPT_KEY, "nlpiapp://");
                intent.putExtra(ARG_KEY_MAP_BEARING, 180f);
                intent.putExtra(ARG_KEY_AUTO_HEADING, true);
                startActivity(intent);
            }
        });

        findViewById(R.id.activity_main_syntrend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NaviSDKActivity.class);
                intent.putExtra(ARG_KEY_DOMAIN_NAME, "https://syntrend-api.omniguider.com/");
                intent.putExtra(ARG_KEY_MAP_BEARING, 15.5f);
                intent.putExtra(ARG_KEY_AUTO_HEADING, true);
                startActivity(intent);
            }
        });

        findViewById(R.id.activity_main_nmns).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NaviSDKActivity.class);
                intent.putExtra(ARG_KEY_DOMAIN_NAME, "https://nmnsapi.omniguider.com/");
                intent.putExtra(ARG_KEY_MAP_BEARING, 237.3f);
                intent.putExtra(ARG_KEY_AUTO_HEADING, true);
                startActivity(intent);
            }
        });

        findViewById(R.id.activity_main_taipei).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra(ARG_KEY_DOMAIN_NAME, "https://navi.taipei/");
                intent.putExtra(ARG_KEY_ENCRYPT_KEY, "doitapp://");
                intent.putExtra(ARG_KEY_MAP_BEARING, 0f);
                intent.putExtra(ARG_KEY_AUTO_HEADING, true);
                startActivity(intent);
            }
        });
    }
}