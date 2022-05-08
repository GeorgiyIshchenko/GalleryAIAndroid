package com.example.galleryii.data_classes;

public class Project {

    public int id;
    public String name;
    public Tag match;
    public Tag notMatch;
    public boolean isTrained;

    public Project() {
    }

    public Project(int id, String name, Tag match, Tag notMatch, boolean isTrained) {
        this.id = id;
        this.name = name;
        this.match = match;
        this.notMatch = notMatch;
        this.isTrained = isTrained;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tag getMatch() {
        return match;
    }

    public void setMatch(Tag match) {
        this.match = match;
    }

    public Tag getNotMatch() {
        return notMatch;
    }

    public void setNotMatch(Tag notMatch) {
        this.notMatch = notMatch;
    }

    public boolean isTrained() {
        return isTrained;
    }

    public void setTrained(boolean trained) {
        isTrained = trained;
    }
}
