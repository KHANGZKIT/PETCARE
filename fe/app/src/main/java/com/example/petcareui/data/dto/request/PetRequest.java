package com.example.petcareui.data.dto.request;

public class PetRequest {
    private final String name;
    private final String species;
    private final String breed;
    private final String gender;
    private final String age;
    private final Double weight;
    private final String notes;

    public PetRequest(String name, String species) {
        this(name, species, null, null, null, null, null);
    }

    public PetRequest(String name, String species, String breed, String gender, String age, Double weight, String notes) {
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.notes = notes;
    }

    public String getName() { return name; }
    public String getSpecies() { return species; }
    public String getBreed() { return breed; }
    public String getGender() { return gender; }
    public String getAge() { return age; }
    public Double getWeight() { return weight; }
    public String getNotes() { return notes; }
}
