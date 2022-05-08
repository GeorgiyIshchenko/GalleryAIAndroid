package com.example.galleryii.data_classes;

import java.util.ArrayList;

public class Tag {

    public String name;
    public ArrayList<Photo> photos;

    public Tag(String name, ArrayList<Photo> photos) {
        this.name = name;
        this.photos = photos;
    }

    public Tag() {
        photos = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
    }
}
