package com.example.galleryii;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PhotoViewActivity extends AppCompatActivity {

    ImageView photoView;
    ExtendedFloatingActionButton btnDelete, btnShare;
    TextView tvDate, tvTag, tvScore;

    Intent intent;
    SharedPreferences sp;
    String photoId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        photoView = findViewById(R.id.img_photo_view);
        btnDelete = findViewById(R.id.button_delete);
        btnShare = findViewById(R.id.button_share);
        tvDate = findViewById(R.id.tv_photo_date);
        //tvTag = findViewById(R.id.tv_photo_name);
        tvScore = findViewById(R.id.tv_photo_score);

        intent = getIntent();
        photoId = intent.getStringExtra("photo_id");
        String path = intent.getStringExtra("path");
        if (!path.equals("null")) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(path);
            if (imageBitmap != null) photoView.setImageBitmap(imageBitmap);
            else
                Picasso.with(this).load(MainActivity.DEVELOP_URL + intent.getStringExtra("url")).into(photoView);
        } else
            Picasso.with(this).load(MainActivity.DEVELOP_URL + intent.getStringExtra("url")).into(photoView);
        //tvTag.setText(intent.getStringExtra("tag_name"));
        String date = "Загружено:\b" + intent.getStringExtra("created_at");
        tvDate.setText(date);
        String score;
        if (intent.getBooleanExtra("match", true)) {
            score = String.valueOf(intent.getIntExtra("score", 0)) + "%:\bMatch";
        } else {
            score = String.valueOf(intent.getIntExtra("score", 0)) + "%:\bDon't match";
        }
        tvScore.setText(score);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeletePhoto task = new DeletePhoto();
                task.execute();
                Log.d("File delete", path);
                String photo_uri = intent.getStringExtra("uri");
                Log.d("File delete uri", photo_uri);
                if (!photo_uri.equals("/null")){
                    File file = new File(photo_uri);
                    file.delete();
                    Log.d("File delete uri", file.getAbsolutePath());
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(photo_uri))));
                }
                if (!path.equals("null")) {
                    File file = new File(path);
                    file.delete();
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
                }
            }
        });
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent share = new Intent(Intent.ACTION_SEND);
                Bitmap icon = BitmapFactory.decodeFile(path);
                if (icon != null) {
                    share.setType("image/jpeg");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
                    try {
                        f.createNewFile();
                        FileOutputStream fo = new FileOutputStream(f);
                        fo.write(bytes.toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
                } else {
                    share.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra("url"));
                }
                startActivity(Intent.createChooser(share, "Поделиться изображением"));
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
                String url = MainActivity.DEVELOP_URL + "/api/" + id + "/photos/" + photoId + "/delete";
                Request request = new Request.Builder().url(new URL(url)).delete().build();
                Log.d("delete", url);
                Response response = client.newCall(request).execute();
                String s = response.body().string();
                Log.d("delete", s);
                return s;
            } catch (IOException e) {
                Log.d("delete", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getBaseContext(), "Фото успешно удалено.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PhotoViewActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

}