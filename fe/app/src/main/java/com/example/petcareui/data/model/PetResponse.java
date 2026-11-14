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
        String raw = species != null ? species.trim() : "Chó";
        String lower = raw.toLowerCase();
        String lowerName = name != null ? name.toLowerCase() : "";

        String cleanSpecies;
        String breed;
        int avatar;

        if (lower.contains("mèo") || lower.contains("meo") || lower.contains("cat") || lowerName.contains("bông") || lowerName.contains("mimi")) {
            cleanSpecies = "Mèo";
            breed = lower.contains("anh") ? "Anh lông ngắn" : "Mèo cưng";
            avatar = R.drawable.img_pet_bong;
        } else if (lower.contains("shiba") || lowerName.contains("kiki")) {
            cleanSpecies = "Chó";
            breed = "Shiba Inu";
            avatar = R.drawable.ic_avatar_shiba;
        } else if (lower.contains("golden") || lowerName.contains("lucky")) {
            cleanSpecies = "Chó";
            breed = "Golden Retriever";
            avatar = R.drawable.ic_avatar_golden;
        } else if (lower.contains("beagle") || lowerName.contains("max")) {
            cleanSpecies = "Chó";
            breed = "Beagle";
            avatar = R.drawable.ic_avatar_corgi;
        } else if (lower.contains("corgi") || lowerName.contains("lựu")) {
            cleanSpecies = "Chó";
            breed = "Pembroke Corgi";
            avatar = R.drawable.ic_avatar_corgi;
        } else {
            cleanSpecies = "Chó";
            String[] defaultBreeds = new String[]{"Pomeranian", "Poodle Tiny", "Corgi", "Shiba Inu"};
            int[] defaultAvatars = new int[]{
                    R.drawable.img_pet_mochi,
                    R.drawable.ic_avatar_shiba,
                    R.drawable.ic_avatar_golden,
                    R.drawable.ic_avatar_corgi
            };
            int idx = (int) (Math.abs(id != null ? id : 0L) % defaultBreeds.length);
            breed = (!raw.equalsIgnoreCase("Chó") && !raw.equalsIgnoreCase("Cho")) ? raw : defaultBreeds[idx];
            avatar = defaultAvatars[idx];
        }

        String[] sampleAges = new String[]{"2 tuổi", "1,5 tuổi", "8 tháng", "3 tuổi", "1 tuổi"};
        String[] sampleSexes = new String[]{"♂ Đực", "♀ Cái", "♂ Đực", "♀ Cái", "♂ Đực"};
        String[] sampleWeights = new String[]{"4,2 kg", "5,8 kg", "8,5 kg", "3,6 kg", "6,4 kg"};
        int seed = (int) (Math.abs(id != null ? id : 1L) % sampleAges.length);

        return new PetItem(
                String.valueOf(id),
                name != null ? name : "Bé cưng",
                cleanSpecies,
                breed,
                sampleAges[seed],
                "12/03/2024",
                sampleSexes[seed],
                sampleWeights[seed],
                avatar
        );
    }
}
