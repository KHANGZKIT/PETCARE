package com.example.petcareui.data.dto.response;

import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.utils.PetAvatarUtils;

import java.io.Serializable;
import java.util.Locale;

public class PetResponse implements Serializable {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private String gender;
    private String age;
    private Double weight;
    private String notes;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSpecies() { return species; }
    public String getBreed() { return breed; }
    public String getGender() { return gender; }
    public String getAge() { return age; }
    public Double getWeight() { return weight; }
    public String getNotes() { return notes; }

    public PetItem toPetItem() {
        String raw = species != null ? species.trim() : "Chó";
        String lower = raw.toLowerCase(Locale.ROOT);
        String lowerName = name != null ? name.toLowerCase(Locale.ROOT) : "";

        String cleanSpecies;
        String resolvedBreed;

        if (lower.contains("mèo") || lower.contains("meo") || lower.contains("cat")
                || lowerName.contains("bông") || lowerName.contains("mimi") || lowerName.contains("miu")) {
            cleanSpecies = "Mèo";
            if (breed != null && !breed.trim().isEmpty()) {
                resolvedBreed = breed.trim();
            } else {
                resolvedBreed = lower.contains("anh") ? "Anh lông ngắn" : "Mèo cưng";
            }
        } else if (lower.startsWith("chó") || lower.startsWith("cho") || lower.contains("corgi")
                || lower.contains("shiba") || lower.contains("golden") || lower.contains("beagle")) {
            cleanSpecies = "Chó";
            if (breed != null && !breed.trim().isEmpty()) {
                resolvedBreed = breed.trim();
            } else if (!raw.equalsIgnoreCase("Chó") && !raw.equalsIgnoreCase("Cho")) {
                resolvedBreed = raw.replaceFirst("(?i)^chó\\s*", "").trim();
            } else {
                resolvedBreed = "Chó cưng";
            }
        } else {
            cleanSpecies = raw.isEmpty() ? "Khác" : raw;
            resolvedBreed = (breed != null && !breed.trim().isEmpty()) ? breed.trim() : "";
        }
        int avatar = PetAvatarUtils.resolvePetAvatar(id, name, cleanSpecies, resolvedBreed);

        // Ưu tiên dữ liệu thật từ API; fallback nhẹ nếu record cũ chưa điền
        String resolvedAge = (age != null && !age.trim().isEmpty()) ? age.trim() : "";
        String resolvedGender = (gender != null && !gender.trim().isEmpty()) ? gender.trim() : "";
        String resolvedWeight = "";
        if (weight != null && weight > 0) {
            if (weight == Math.floor(weight)) {
                resolvedWeight = String.format(Locale.US, "%.0f kg", weight);
            } else {
                resolvedWeight = String.format(Locale.US, "%.1f kg", weight);
            }
        }

        PetItem item = new PetItem(
                String.valueOf(id),
                name != null ? name : "Bé cưng",
                cleanSpecies,
                resolvedBreed,
                resolvedAge,
                "",
                resolvedGender,
                resolvedWeight,
                avatar
        );
        String displaySpecies = (resolvedBreed != null && !resolvedBreed.isEmpty() && !resolvedBreed.equalsIgnoreCase(cleanSpecies))
                ? cleanSpecies + " " + resolvedBreed
                : raw;
        item.setRawSpecies(displaySpecies);
        item.setWeightValue(weight);
        item.setNotes(notes != null ? notes.trim() : "");
        return item;
    }
}
