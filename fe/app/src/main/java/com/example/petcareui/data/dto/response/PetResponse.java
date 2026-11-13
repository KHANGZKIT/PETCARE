package com.example.petcareui.data.dto.response;

import com.example.petcareui.R;
import com.example.petcareui.data.model.PetItem;

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
        String rawSpecies = species != null ? species.trim() : "Thú cưng";

        String mainSpecies = isCat ? "Mèo" : (rawSpecies.toLowerCase().contains("chó") || rawSpecies.toLowerCase().contains("dog") ? "Chó" : rawSpecies);
        String breed = rawSpecies;

        if (rawSpecies.toLowerCase().startsWith("mèo ") && rawSpecies.length() > 4) {
            breed = rawSpecies.substring(4).trim();
            mainSpecies = "Mèo";
        } else if (rawSpecies.toLowerCase().startsWith("chó ") && rawSpecies.length() > 4) {
            breed = rawSpecies.substring(4).trim();
            mainSpecies = "Chó";
        }

        return new PetItem(
                String.valueOf(id),
                name,
                mainSpecies,
                breed,
                "—",
                "—",
                "—",
                "—",
                avatar
        );
    }
}

