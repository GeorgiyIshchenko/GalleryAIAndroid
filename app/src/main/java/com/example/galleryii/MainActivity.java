package com.example.galleryii;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.galleryii.adapters.TagsAdapter;
import com.example.galleryii.auth.AuthActivity;
import com.example.galleryii.data_classes.Photo;
import com.example.galleryii.data_classes.Project;
import com.example.galleryii.data_classes.Tag;
import com.example.galleryii.data_set_creation.DataSetCreationActivity;
import com.example.galleryii.data_set_creation.SinglePhotoCreationActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1001;
    private static final int IMAGE_CAPTURE_CODE = 1002;
    private static final int IMAGE_PICK_CODE = 1003;

    public static final String AUTH_PREFERENCES = "auth";
    public static final String USER_LOGIN = "login";
    public static final String USER_PASSWORD = "password";
    public static final String USER_TOKEN = "token";
    public static final String USER_ID = "user_id";
    public static final String CURRENT_TAG = "current_tag";
    public static final String IP_VAR = "ip_var";

    public static String DEVELOP_URL;

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Uri image_uri;
    File photo;

    //FloatingActionButton buttonAdd;
    ExtendedFloatingActionButton buttonGallery;
    FloatingActionButton buttonExit;
    ExtendedFloatingActionButton buttonLoad;
    Spinner projectDropdown;
    ImageButton reloadBtn;

    ArrayList<Tag> tags;
    TagsAdapter adapter;
    RecyclerView recyclerView;

    ArrayList<Project> projects;
    Project currentProj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);
        String token = sp.getString(USER_TOKEN, "");
        Log.d("USER TOKEN", "TOKEN: "+token);
        if (token.length() == 0) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        }

        //buttonAdd = findViewById(R.id.button_photo_add);
        buttonGallery = findViewById(R.id.button_train);
        buttonExit = findViewById(R.id.button_exit);
        buttonLoad = findViewById(R.id.button_predict);
        recyclerView = findViewById(R.id.rv_tags);

        // ActionBar
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.actionbar_dropdown);
        //getSupportActionBar().setElevation(0);
        View view = getSupportActionBar().getCustomView();
        projectDropdown = view.findViewById(R.id.project_spinner);
        reloadBtn = view.findViewById(R.id.reload_btn);
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetProjectsList getProjectsList = new GetProjectsList();
                getProjectsList.execute();
            }
        });

        GetProjectsList getProjectsList = new GetProjectsList();
        getProjectsList.execute();

        CheckPermissions();

        /*buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });*/
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentProj.isTrained) {
                    startActivity(new Intent(MainActivity.this, PredictionActivity.class));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Перед предсказанием обучите нейросеть", Toast.LENGTH_LONG).show();
                }
            }
        });
        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DataSetCreationActivity.class));
                finish();
            }
        });
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Выход");  // заголовок
                builder.setMessage("Вы\bуверены\bчто\bхотите\bвыйти?"); // сообщение
                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(USER_TOKEN, null);
                        editor.apply();
                        startActivity(new Intent(MainActivity.this, AuthActivity.class));
                        finish();
                    }
                });
                builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                builder.setCancelable(true);
                builder.create();
                builder.show();
            }
        });
        AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                editor = sp.edit();
                editor.putInt(CURRENT_TAG, projects.get(i).id);
                editor.apply();

                currentProj = projects.get(i);

                GetPhotoList getPhotoList = new GetPhotoList();
                getPhotoList.execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        projectDropdown.setOnItemSelectedListener(onItemSelectedListener);
    }

    public void reloadPhotos(){
        GetPhotoList getPhotoList = new GetPhotoList();
        getPhotoList.execute();
    }

    class GetProjectsList extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();
                String token = sp.getString(USER_TOKEN, "");
                String url = DEVELOP_URL + "/api/projects";
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).
                        addFormDataPart("token", token).build();
                Request request = new Request.Builder().url(new URL(url)).post(body).build();
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
            try {
                JSONArray jsonArray = new JSONArray(s);
                projects = new ArrayList<>();
                int JSONArraySize = jsonArray.length();
                String[] dropdownItems = new String[JSONArraySize];

                Project select_projects = new Project();
                select_projects.id = -1;
                select_projects.name = "Выберете проект";
                projects.add(select_projects);
                dropdownItems[0] = select_projects.name;

                for (int i = 1; i < JSONArraySize + 1; i++) {
                    JSONObject projectJSON = jsonArray.getJSONObject(i);
                    Project project = new Project();
                    project.id = projectJSON.getInt("id");
                    project.name = projectJSON.getString("name");
                    if (project.name.length() > 0)
                        project.name = project.name.substring(0, 1).toUpperCase() + project.name.substring(1);
                    project.isTrained = projectJSON.getBoolean("is_trained");
                    projects.add(project);
                    dropdownItems[i] = project.name;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_item, dropdownItems);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                projectDropdown.setAdapter(adapter);

                if (projects.size() != 0) {
                    currentProj = projects.get(0);
                    editor = sp.edit();
                    editor.putInt(CURRENT_TAG, projects.get(0).id);
                    editor.apply();
                }

                GetPhotoList getPhotoList = new GetPhotoList();
                getPhotoList.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }

    public class GetPhotoList extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                String token = sp.getString(USER_TOKEN, "");
                int tagId = sp.getInt(CURRENT_TAG, -1);
                String url = DEVELOP_URL + "/api/projects/" + String.valueOf(tagId);
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("token", token).build();
                Request request = new Request.Builder().url(new URL(url)).post(body).build();
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
            assert (s != null);
            try {
                tags = new ArrayList<>();
                JSONObject project = new JSONObject(s);
                JSONArray photosJSON = project.getJSONArray("photos");
                Log.d("request", photosJSON.toString());
                int size = photosJSON.length();
                Tag match = new Tag();
                match.name = "Predicted:\bMatch";
                ArrayList<Photo> matchList = new ArrayList<>();
                Tag notMatch = new Tag();
                notMatch.name = "Predicted:\bDon't\bmatch";
                ArrayList<Photo> notMatchList = new ArrayList<>();
                Tag matchTrained = new Tag();
                matchTrained.name = "Trained\bon:\bMatch";
                ArrayList<Photo> matchListTrained = new ArrayList<>();
                Tag notMatchTrained = new Tag();
                notMatchTrained.name = "Trained\bon:\bdon't\bmatch";
                ArrayList<Photo> notMatchListTrained = new ArrayList<>();
                for (int j = 0; j < size; j++) {
                    try {
                        JSONObject photoJSON = photosJSON.getJSONObject(j);
                        Photo photo = new Photo();
                        photo.id = photoJSON.getInt("id");
                        photo.url = photoJSON.getString("image");
                        photo.full_image_url = photoJSON.getString("full_image");
                        photo.match = photoJSON.getBoolean("match");
                        photo.isAiTag = photoJSON.getBoolean("is_ai_tag");
                        if (photo.isAiTag)
                            photo.score = photoJSON.getInt("score");
                        photo.createdAt = photoJSON.getString("created_at");
                        try {
                            photo.devicePath = photoJSON.getString("device_path");
                            photo.uri = Uri.fromFile(new File(photoJSON.getString("device_uri")));
                            Log.d("photo_uri", photo.uri.getPath()+" "+photoJSON.getString("device_path"));
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        if (photo.match && photo.isAiTag) matchList.add(photo);
                        else if (photo.match && !photo.isAiTag) matchListTrained.add(photo);
                        else if (!photo.match && photo.isAiTag) notMatchList.add(photo);
                        else notMatchListTrained.add(photo);
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                Log.d("request", matchList.size()+" "+notMatchList.size()+" "+matchListTrained.size()+" "+notMatchListTrained.size());
                match.photos = matchList;
                notMatch.photos = notMatchList;
                matchTrained.photos = matchListTrained;
                notMatchTrained.photos = notMatchListTrained;
                tags.add(match);
                tags.add(notMatch);
                tags.add(matchTrained);
                tags.add(notMatchTrained);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                adapter = new TagsAdapter(tags, MainActivity.this, MainActivity.this);
                recyclerView.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }
    }

    private void CheckPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_CODE);
            }
        }
    }


    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int id_uri = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(id_uri);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("single_photo", image_uri.getPath());
        if (resultCode == RESULT_OK && requestCode == IMAGE_CAPTURE_CODE) {
            Log.d("request", getRealPathFromURI(image_uri));
            photo = new File(getRealPathFromURI(image_uri));
            Intent intent = new Intent(MainActivity.this, SinglePhotoCreationActivity.class);
            intent.putExtra("fileUri", photo.getAbsolutePath());
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}