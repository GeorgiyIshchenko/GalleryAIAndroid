package com.example.galleryii.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.galleryii.MainActivity;
import com.example.galleryii.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthActivity extends AppCompatActivity {

    EditText etLogin;
    EditText etPassword;
    Button btnAuth;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        etLogin = findViewById(R.id.et_login);
        etPassword = findViewById(R.id.et_password);
        btnAuth = findViewById(R.id.btn_auth);

        sp = getSharedPreferences(MainActivity.AUTH_PREFERENCES, Context.MODE_PRIVATE);

        String login = sp.getString(MainActivity.USER_LOGIN, null);

        etLogin.setText(login);

        btnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etLogin.getText().toString().trim().length() > 0) {
                    if (etPassword.getText().toString().trim().length() > 0) {
                        editor = sp.edit();
                        editor.putString(MainActivity.USER_LOGIN, etLogin.getText().toString());
                        editor.putString(MainActivity.USER_PASSWORD, etPassword.getText().toString());
                        editor.apply();
                        AuthTask task = new AuthTask();
                        task.execute();
                    } else {
                        Toast.makeText(AuthActivity.this, "Введите пароль", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AuthActivity.this, "Введите email", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    class AuthTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String login = sp.getString(MainActivity.USER_LOGIN, null);
            String password = sp.getString(MainActivity.USER_PASSWORD, null);
            String result = null;
            if (login != null && password != null) {
                String url = MainActivity.DEVELOP_URL+"/api/users/auth/";
                Log.d("auth_log", url);
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder().add("email", login).add("password", password).build();
                Log.d("auth", body.toString());
                Request request = new Request.Builder().url(url).post(body).build();
                try {
                    Response response = client.newCall(request).execute();
                    result = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d("auth_log", result);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                try {
                    JSONObject user = new JSONObject(s);
                    editor = sp.edit();
                    editor.putString(MainActivity.USER_ID, user.getString("id"));
                    editor.apply();
                    startActivity(new Intent(AuthActivity.this, MainActivity.class));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}