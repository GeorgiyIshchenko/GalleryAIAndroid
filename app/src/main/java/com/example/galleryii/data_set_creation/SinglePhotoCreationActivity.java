package com.example.galleryii.data_set_creation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.galleryii.MainActivity;
import com.example.galleryii.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SinglePhotoCreationActivity extends AppCompatActivity {

    File photo;

    AppCompatButton btnMatch, btnNotMatch;
    ImageView imageView;

    boolean match;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_photo_creation);
        imageView = findViewById(R.id.img_photo_single_creation);
        btnMatch = findViewById(R.id.btn_match);
        btnNotMatch = findViewById(R.id.btn_not_match);

        Intent intent = getIntent();
        String photoPath = intent.getStringExtra("fileUri");
        photo = new File(photoPath);
        imageView.setImageBitmap(BitmapFactory.decodeFile(photoPath));

        btnMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                match = true;
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.execute();
            }
        });

        btnNotMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                match = false;
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.execute();
            }
        });

    }

    class SendPhoto extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                SharedPreferences sp = getSharedPreferences(MainActivity.AUTH_PREFERENCES, Context.MODE_PRIVATE);
                int tagId = sp.getInt(MainActivity.CURRENT_TAG, -1);
                String url = MainActivity.DEVELOP_URL+"/api/photos/post/";
                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(180, TimeUnit.SECONDS).readTimeout(180, TimeUnit.SECONDS).build();
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("image", photo.getName(), RequestBody.create(MediaType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), photo))
                        .addFormDataPart("match", String.valueOf(match))
                        .addFormDataPart("is_ai_tag", "false")
                        .addFormDataPart("tag", String.valueOf(tagId))
                        .addFormDataPart("device_path", photo.getAbsolutePath())
                        .build();
                Request request = new Request.Builder().url(url).post(body).build();
                Response response = client.newCall(request).execute();
                String s = response.body().string();
                Log.d("request", s);
                return s;
            } catch (IOException e) {
                Log.d("request", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("post_single_photo", s);
            startActivity(new Intent(SinglePhotoCreationActivity.this, MainActivity.class));
            finish();
        }
    }
}