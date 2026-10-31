package com.example.petcareui.data.model;

import java.io.Serializable;

public class PetItem implements Serializable {
    private String id;
    private String name;
    private String species;
    private String breed;
    private String age;
    private String dob;
    private String sex;
    private String weight;
    private int imageResId;
    private boolean selected;

    public PetItem(String id, String name, String species, String breed, String age, String dob, String sex, String weight, int imageResId) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
        this.dob = dob;
        this.sex = sex;
        this.weight = weight;
        this.imageResId = imageResId;
        this.selected = false;
    }

    public String getId() {
        return id;
    }

    public Long getNumericId() {
        try {
            return id != null ? Long.parseLong(id) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        return species;
    }

    public String getBreed() {
        return breed;
    }

    public String getAge() {
        return age;
    }

    public String getDob() {
        return dob;
    }

    public String getSex() {
        return sex;
    }

    public String getWeight() {
        return weight;
    }

    public int getImageResId() {
        return imageResId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
