package com.example.galleryii;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.galleryii.data_classes.Photo;
import com.example.galleryii.data_set_creation.DataSetCreationActivity;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PredictionActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_CODE = 501;

    Button btnLoadPhotos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_photos);
        btnLoadPhotos = findViewById(R.id.btn_load_photos);
        btnLoadPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Выберите изображения (не менее 20)"), PICK_IMAGES_CODE);
            }
        });
    }

    class SendPhotos extends AsyncTask<String, String, String> {

        private final ArrayList<Photo> taskPhotos;

        public SendPhotos(ArrayList<Photo> photos) {
            this.taskPhotos = photos;
        }

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences sp = getSharedPreferences(MainActivity.AUTH_PREFERENCES, Context.MODE_PRIVATE);
            int tagId = sp.getInt(MainActivity.CURRENT_TAG, -1);
            String token = sp.getString(MainActivity.USER_TOKEN, "");
            String url = MainActivity.DEVELOP_URL + "/api/post_photo_prediction";
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(180, TimeUnit.SECONDS).readTimeout(180, TimeUnit.SECONDS).build();
            for (Photo photo : taskPhotos) {
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("token", token)
                        .addFormDataPart("image", photo.getFile().getName(), RequestBody.create(MediaType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), photo.getFile()))
                        .addFormDataPart("tag", String.valueOf(tagId))
                        .addFormDataPart("device_path", photo.getFile().getAbsolutePath())
                        .addFormDataPart("device_uri", photo.getUri().getPath())
                        .build();
                Log.d("load_photo_body", body.contentType().toString());
                Request request = new Request.Builder().url(url).post(body).build();
                try {
                    client.newCall(request).execute();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            StartPrediction startPrediction = new StartPrediction();
            startPrediction.execute();
        }
    }

    class StartPrediction extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences sp = getSharedPreferences(MainActivity.AUTH_PREFERENCES, Context.MODE_PRIVATE);
            int tagId = sp.getInt(MainActivity.CURRENT_TAG, -1);
            OkHttpClient client = new OkHttpClient();
            String url = MainActivity.DEVELOP_URL + "/api/start_prediction_project/" + tagId;
            Log.d("request", url);
            try {
                Request request = new Request.Builder().url(new URL(url)).build();
                client.newCall(request).execute();
                Response response = client.newCall(request).execute();
                String s = response.body().string();
                Log.d("request", s);
                return s;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            startActivity(new Intent(PredictionActivity.this, MainActivity.class));
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<Photo> photos = new ArrayList<>();
                assert data != null;
                if (data.getClipData() != null){
                    int size = data.getClipData().getItemCount();
                    for (int i = 0; i < size; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        File file = null;
                        try {
                            file = FileUtil.from(this,imageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("file", "File...:::: uti - "+file .getPath()+" file -" + file + " : " + file .exists());
                        Photo photo = new Photo();
                        photo.uri = imageUri;
                        try{
                            photo.file = DataSetCreationActivity.scaleFile(file, 512);
                        }
                        catch (Exception e){
                            photo.file = file;
                            e.printStackTrace();
                        }
                        photo.file = file;
                        photos.add(photo);
                    }
                }
                else{
                    Uri imageUri = data.getData();
                    File file = null;
                    try {
                        file = FileUtil.from(this,imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Photo photo = new Photo();
                    photo.uri = imageUri;
                    try{
                        assert file != null;
                        photo.file = DataSetCreationActivity.scaleFile(file, 512);
                    }
                    catch (Exception e){
                        photo.file = file;
                        e.printStackTrace();
                    }
                    photo.file = file;
                    photos.add(photo);
                }
                SendPhotos sendPhotos = new SendPhotos(photos);
                sendPhotos.execute();
            }
        }
    }

}