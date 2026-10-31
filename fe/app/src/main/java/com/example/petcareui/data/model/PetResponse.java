package com.example.petcareui.data.model;

import com.example.petcareui.R;

import java.io.Serializable;

public class PetResponse implements Serializable {
    private Long id;
    private String name;
    private String species;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        return species;
    }

    public PetItem toPetItem() {
        boolean isCat = species != null && (species.toLowerCase().contains("mèo") || species.toLowerCase().contains("cat"));
        int avatar = isCat ? R.drawable.img_pet_bong : R.drawable.img_pet_mochi;
        String displaySpecies = species != null ? species : "Thú cưng";
        return new PetItem(
                String.valueOf(id),
                name,
                displaySpecies,
                displaySpecies,
                "—",
                "—",
                "—",
                "—",
                avatar
        );
    }
}
