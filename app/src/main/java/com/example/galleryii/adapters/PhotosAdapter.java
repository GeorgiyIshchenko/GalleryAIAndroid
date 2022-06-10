package com.example.galleryii.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryii.MainActivity;
import com.example.galleryii.PhotoViewActivity;
import com.example.galleryii.R;
import com.example.galleryii.data_classes.Photo;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {

    ArrayList<Photo> photos;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView tvScore;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.img_photo_item);
            tvScore = (TextView) view.findViewById(R.id.img_photo_score);
        }

        public TextView getTvScore() {
            return tvScore;
        }

        public ImageView getImageView() {
            return imageView;
        }
    }

    public PhotosAdapter(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_view, parent, false);
        return new PhotosAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PhotosAdapter.ViewHolder holder, int position) {
        Photo photo = photos.get(position);
        Context context = holder.getImageView().getContext();
        if (!photo.devicePath.equals("null")) {
            try {
                Log.d("device_path", photo.devicePath);
                Bitmap bitmapImage = BitmapFactory.decodeFile(photo.getDevicePath());
                bitmapImage = Bitmap.createScaledBitmap(bitmapImage, 300, 320, false);
                holder.imageView.setImageBitmap(bitmapImage);
            } catch (NullPointerException e) {
                Picasso.with(holder.imageView.getContext()).load(MainActivity.DEVELOP_URL + photo.url).into(holder.getImageView());
                e.printStackTrace();
            }
        } else
            Picasso.with(holder.imageView.getContext()).load(MainActivity.DEVELOP_URL + photo.url).into(holder.getImageView());
        String score = photo.score + "%";
        holder.tvScore.setText(score);
        if (!photo.isAiTag) holder.tvScore.setVisibility(View.INVISIBLE);
        holder.getImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.getImageView().getContext(), PhotoViewActivity.class);
                intent.putExtra("photo_id", String.valueOf(photo.id));
                intent.putExtra("url", photo.full_image_url);
                intent.putExtra("score", photo.score);
                intent.putExtra("tag_name", photo.tagName);
                intent.putExtra("match", photo.match);
                intent.putExtra("created_at", photo.createdAt);
                intent.putExtra("path", photo.devicePath);
                intent.putExtra("uri", photo.getUri().getPath());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }


}
