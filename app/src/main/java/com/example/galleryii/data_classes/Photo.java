package com.example.galleryii.data_classes;

import android.net.Uri;

import java.io.File;

public class Photo {

    public long id;
    public String url;
    public String full_image_url;
    public String description;
    public String createdAt;
    public String tagName;
    public boolean match;
    public boolean isAiTag;
    public int score;
    public File file;
    public String devicePath;
    public Uri uri;

    public Photo(long id, String url, String full_image_url, String description, String createdAt, String tagName, int score, boolean match, boolean isAiTag, File file, Uri uri, String devicePath) {
        this.id = id;
        this.url = url;
        this.full_image_url = full_image_url;
        this.description = description;
        this.createdAt = createdAt;
        this.score = score;
        this.match = match;
        this.isAiTag = isAiTag;
        this.tagName = tagName;
        this.file = file;
        this.uri = uri;
        this.devicePath = devicePath;
    }

    public Photo() {
    }

    public String getDevicePath() {
        return devicePath;
    }

    public void setDevicePath(String devicePath) {
        this.devicePath = devicePath;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getFull_image_url() {
        return full_image_url;
    }

    public void setFull_image_url(String full_image_url) {
        this.full_image_url = full_image_url;
    }

    public boolean isAiTag() {
        return isAiTag;
    }

    public void setAiTag(boolean aiTag) {
        isAiTag = aiTag;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
