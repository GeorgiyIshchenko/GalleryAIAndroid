package com.example.galleryii.data_set_creation;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUriExposedException;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.galleryii.FileUtil;
import com.example.galleryii.MainActivity;
import com.example.galleryii.R;
import com.example.galleryii.data_classes.Photo;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DataSetCreationActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_CODE = 501;

    boolean match;
    boolean finish;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_set_creation);
        getSupportFragmentManager().beginTransaction().replace(R.id.data_set_creation, new MatchPhotoCreationFragment()).commit();
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
            String url = MainActivity.DEVELOP_URL + "/api/photos/post/train";
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(180, TimeUnit.SECONDS).readTimeout(180, TimeUnit.SECONDS).build();
            for (Photo photo : taskPhotos) {
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("image", photo.getFile().getName(), RequestBody.create(MediaType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), photo.getFile()))
                        .addFormDataPart("match", String.valueOf(photo.isMatch()))
                        .addFormDataPart("is_ai_tag", "false")
                        .addFormDataPart("tag", String.valueOf(tagId))
                        .addFormDataPart("device_path", photo.getFile().getAbsolutePath())
                        .addFormDataPart("device_uri", photo.getUri().getPath())
                        .build();
                Log.d("photo_uri", photo.uri.getPath());
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
            if (finish){
                StartTrain startTrain = new StartTrain();
                startTrain.execute();
            }
        }
    }

    class StartTrain extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences sp = getSharedPreferences(MainActivity.AUTH_PREFERENCES, Context.MODE_PRIVATE);
            int tagId = sp.getInt(MainActivity.CURRENT_TAG, -1);
            String user_id = sp.getString(MainActivity.USER_ID, null);
            OkHttpClient client = new OkHttpClient();
            String url = MainActivity.DEVELOP_URL + "/api/"+user_id+"/tags/" + tagId + "/train/";
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
            context.startActivity(new Intent(context, MainActivity.class));
        }
    }

    public void pickImagesIntent(Boolean match, Context context, boolean finish) {
        this.match = match;
        this.context = context;
        this.finish = finish;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Выберите изображения (не менее 20)"), PICK_IMAGES_CODE);
    }

    static public File scaleFile(File file, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        bitmap = Bitmap.createScaledBitmap(bitmap, width, Integer.valueOf(bitmap.getHeight()/bitmap.getWidth()*width), false);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<Photo> photos = new ArrayList<>();
                assert data != null;
                if (data.getClipData() != null) {
                    int size = data.getClipData().getItemCount();
                    for (int i = 0; i < size; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        File file = null;
                        try {
                            file = FileUtil.from(this, imageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("file", "File...:::: uti - " + file.getPath() + " file -" + file + " : " + file.exists());
                        Photo photo = new Photo();
                        photo.match = this.match;
                        photo.uri = imageUri;
                        try {
                            photo.file = file;
                        } catch (Exception e) {
                            photo.file = file;
                            e.printStackTrace();
                        }
                        photos.add(photo);
                    }
                } else {
                    Uri imageUri = data.getData();
                    File file = null;
                    try {
                        file = FileUtil.from(this, imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Photo photo = new Photo();
                    photo.match = this.match;
                    photo.uri = imageUri;
                    try {
                        assert file != null;
                        photo.file = file;
                    } catch (Exception e) {
                        photo.file = file;
                        e.printStackTrace();
                    }
                    photos.add(photo);
                }
                SendPhotos sendPhotos = new SendPhotos(photos);
                sendPhotos.execute();
            }
        }
    }
}