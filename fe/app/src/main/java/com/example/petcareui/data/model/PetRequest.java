package com.example.petcareui.data.model;

public class PetRequest {
    private final String name;
    private final String species;

    public PetRequest(String name, String species) {
        this.name = name;
        this.species = species;
    }

    public String getName() { return name; }
    public String getSpecies() { return species; }
}
