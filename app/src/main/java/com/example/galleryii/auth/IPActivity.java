package com.example.galleryii.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.galleryii.MainActivity;
import com.example.galleryii.R;

public class IPActivity extends AppCompatActivity {

    EditText etIP;
    Button btnIP;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipactivity);

        etIP = findViewById(R.id.et_ip);
        btnIP = findViewById(R.id.btn_ip);

        sp = getSharedPreferences(MainActivity.AUTH_PREFERENCES, Context.MODE_PRIVATE);
        String ip = sp.getString(MainActivity.IP_VAR, null);

        if (ip != null) {
            etIP.setText(ip);
            MainActivity.DEVELOP_URL = ip;
        }
        else etIP.setText("192.168.");

        btnIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etIP.getText().toString().trim().length() > 0) {
                    String inputIP = etIP.getText().toString();

                    editor = sp.edit();
                    editor.putString(MainActivity.IP_VAR, inputIP);
                    editor.apply();

                    MainActivity.DEVELOP_URL = "http://" + inputIP + ":8000";

                    startActivity(new Intent(IPActivity.this, AuthActivity.class));
                    finish();
                } else {
                    Toast.makeText(IPActivity.this, "Введите IP", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}