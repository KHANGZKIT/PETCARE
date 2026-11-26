package com.example.petcareui.utils;

import com.example.petcareui.R;

import java.util.Locale;

public final class PetAvatarUtils {

    private static final int[] DOG_AVATARS = new int[]{
            R.drawable.img_pet_mochi,
            R.drawable.img_pet_corgi,
            R.drawable.img_pet_golden,
            R.drawable.img_pet_luu
    };

    private PetAvatarUtils() {
    }

    public static int resolvePetAvatar(Long petId, String petName, String species, String breed) {
        String combined = ((species != null ? species : "") + " "
                + (breed != null ? breed : "") + " "
                + (petName != null ? petName : "")).toLowerCase(Locale.ROOT);

        if (combined.contains("mèo") || combined.contains("meo") || combined.contains("cat")
                || combined.contains("anh lông ngắn") || combined.contains("british")
                || combined.contains("scottish") || combined.contains("bông")
                || combined.contains("mimi") || combined.contains("miu")) {
            if (combined.contains("anh") || combined.contains("british")
                    || combined.contains("scottish") || combined.contains("miu")) {
                return R.drawable.img_pet_cat_british;
            }
            if (combined.contains("mướp") || combined.contains("tabby") || combined.contains("bông")) {
                return R.drawable.img_pet_bong;
            }
            return (petId != null && petId % 2 == 0)
                    ? R.drawable.img_pet_cat_british
                    : R.drawable.img_pet_bong;
        }

        if (combined.contains("thỏ") || combined.contains("rabbit")
                || combined.contains("hamster") || combined.contains("chuột")
                || combined.contains("chim") || combined.contains("bò sát")
                || combined.contains("khác")) {
            return R.drawable.img_pet_other;
        }

        if (combined.contains("pomeranian") || combined.contains("phốc") || combined.contains("phoc")
                || combined.contains("mochi")) {
            return R.drawable.img_pet_mochi;
        }
        if (combined.contains("corgi")) {
            return R.drawable.img_pet_corgi;
        }
        if (combined.contains("golden") || combined.contains("labrador")
                || combined.contains("husky") || combined.contains("samoyed")
                || combined.contains("alaska")) {
            return R.drawable.img_pet_golden;
        }
        if (combined.contains("shiba") || combined.contains("poodle")
                || combined.contains("beagle") || combined.contains("pug")
                || combined.contains("lưu") || combined.contains("luu")
                || combined.contains("lulu")) {
            return R.drawable.img_pet_luu;
        }

        long seed = petId != null ? Math.abs(petId) : Math.abs((long) combined.hashCode());
        return DOG_AVATARS[(int) (seed % DOG_AVATARS.length)];
    }
}
