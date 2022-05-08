package com.example.galleryii;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PhotoViewActivity extends AppCompatActivity {

    ImageView photoView;
    FloatingActionButton btnDelete, btnShare;
    TextView tvDescription, tvDate, tvTag;

    Intent intent;
    SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        photoView = findViewById(R.id.img_photo_view);
        btnDelete = findViewById(R.id.button_delete);
        btnShare = findViewById(R.id.button_share);
        tvDescription = findViewById(R.id.tv_description);
        tvDate = findViewById(R.id.tv_photo_date);
        tvTag = findViewById(R.id.tv_photo_name);

        intent = getIntent();
        String path = intent.getStringExtra("path");
        if (!path.equals("null")){
            Bitmap imageBitmap = BitmapFactory.decodeFile(path);
            photoView.setImageBitmap(imageBitmap);
        }
        else Picasso.with(this).load(MainActivity.DEVELOP_URL+intent.getStringExtra("url")).into(photoView);
        tvTag.setText(intent.getStringExtra("tag_name"));
        tvDate.setText(intent.getStringExtra("created_at"));

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeletePhoto task = new DeletePhoto();
                task.execute();
                Intent intent = new Intent(PhotoViewActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    class DeletePhoto extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                sp = getSharedPreferences(MainActivity.AUTH_PREFERENCES, Context.MODE_PRIVATE);
                String id = sp.getString(MainActivity.USER_ID, null);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(new URL(MainActivity.DEVELOP_URL+"/api/"+id+"/photos/"+intent.getIntExtra("id", 0)+"/delete")).delete().build();
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
            Toast.makeText(getBaseContext(), "Фото успешно удалено.", Toast.LENGTH_SHORT).show();
            Log.d("photo_delete", s);
            super.onPostExecute(s);
        }
    }

}