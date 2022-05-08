package com.example.galleryii.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryii.R;
import com.example.galleryii.data_classes.Tag;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder> {

    private ArrayList<Tag> tags;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView tagNameTV;
        private final RecyclerView photosRV;

        public ViewHolder(View view){
            super(view);
            tagNameTV = (TextView) view.findViewById(R.id.tv_tag_name);
            photosRV = (RecyclerView) view.findViewById(R.id.rv_photos);
        }

        public TextView getTagNameTV() {
            return tagNameTV;
        }

        public RecyclerView getPhotosRV() {
            return photosRV;
        }
    }

    public TagsAdapter(ArrayList<Tag> tags){
        this.tags = tags;
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
        holder.getPhotosRV().setLayoutManager(new GridLayoutManager(holder.getPhotosRV().getContext(), 3, GridLayoutManager.VERTICAL, false));
        holder.getPhotosRV().setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }
}
