package com.example.galleryii.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryii.MainActivity;
import com.example.galleryii.PhotoViewActivity;
import com.example.galleryii.R;
import com.example.galleryii.data_classes.Photo;
import com.example.galleryii.data_classes.Tag;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder> {

    private ArrayList<Tag> tags;
    private Context context;
    private MainActivity mainActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tagNameTV;
        private final RecyclerView photosRV;
        private final ImageButton btnDelete;

        public ViewHolder(View view) {
            super(view);
            tagNameTV = (TextView) view.findViewById(R.id.tv_tag_name);
            photosRV = (RecyclerView) view.findViewById(R.id.rv_photos);
            btnDelete = (ImageButton) view.findViewById(R.id.btn_delete_tag);
        }

        public TextView getTagNameTV() {
            return tagNameTV;
        }

        public RecyclerView getPhotosRV() {
            return photosRV;
        }

        public ImageButton getBtnDelete() {
            return btnDelete;
        }
    }

    public TagsAdapter(ArrayList<Tag> tags, Context context, MainActivity mainActivity) {
        this.tags = tags;
        this.context = context;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.getTagNameTV().setText(tags.get(position).name);
        PhotosAdapter adapter = new PhotosAdapter(tags.get(position).photos);
        holder.getBtnDelete().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeletePhoto deletePhoto = new DeletePhoto(position, holder.itemView);
                deletePhoto.execute();
            }
        });
        holder.getPhotosRV().setLayoutManager(new GridLayoutManager(holder.getPhotosRV().getContext(), 3, GridLayoutManager.VERTICAL, false));
        holder.getPhotosRV().setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    class DeletePhoto extends AsyncTask<String, String, String> {

        int position;
        View view;

        public DeletePhoto(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                SharedPreferences sp = context.getSharedPreferences(MainActivity.AUTH_PREFERENCES, Context.MODE_PRIVATE);
                String id = sp.getString(MainActivity.USER_ID, null);
                OkHttpClient client = new OkHttpClient();
                for (Photo photo: tags.get(position).photos){
                    String url = MainActivity.DEVELOP_URL + "/api/photos/" + photo.id + "/delete";
                    Request request = new Request.Builder().url(new URL(url)).get().build();
                    Log.d("delete", url);
                    Response response = client.newCall(request).execute();
                    String s = response.body().string();
                    Log.d("delete", s);
                    if (!photo.uri.getPath().equals("/null")){
                        File file = new File(photo.uri.getPath());
                        file.delete();
                        Log.d("File delete uri", file.getAbsolutePath());
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(photo.uri.getPath()))));
                    }
                    if (!photo.devicePath.equals("null")) {
                        File file = new File(photo.devicePath);
                        file.delete();
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(photo.devicePath))));
                    }
                }
            } catch (IOException e) {
                Log.d("delete", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ((MainActivity)view.getContext()).reloadPhotos();
        }
    }
}
