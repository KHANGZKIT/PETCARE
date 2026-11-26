package com.example.petcareui.ui.pets;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petcareui.data.dto.response.PetResponse;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.data.repository.PetRepository;
import com.example.petcareui.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyPetsViewModel extends ViewModel {

    private final PetRepository petRepository = new PetRepository();

    private final MutableLiveData<List<PetItem>> pets = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Integer> selectedIndex = new MutableLiveData<>(0);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private boolean appliedPreselectedPet = false;

    public LiveData<List<PetItem>> getPets() {
        return pets;
    }

    public LiveData<Integer> getSelectedIndex() {
        return selectedIndex;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public void selectPet(int index) {
        List<PetItem> current = pets.getValue();
        if (current != null && index >= 0 && index < current.size()) {
            selectedIndex.setValue(index);
        }
    }

    public PetItem getSelectedPet() {
        List<PetItem> current = pets.getValue();
        Integer idx = selectedIndex.getValue();
        if (current != null && idx != null && idx >= 0 && idx < current.size()) {
            return current.get(idx);
        }
        return null;
    }

    public void loadMyPets(PetItem preselectedPet) {
        petRepository.getMyPets(new RepositoryCallback<List<PetResponse>>() {
            @Override
            public void onSuccess(List<PetResponse> data) {
                List<PetItem> mapped = new ArrayList<>();
                for (PetResponse res : data) {
                    mapped.add(res.toPetItem());
                }

                int nextIndex = selectedIndex.getValue() != null ? selectedIndex.getValue() : 0;
                if (!mapped.isEmpty()) {
                    if (!appliedPreselectedPet && preselectedPet != null) {
                        Long preId = preselectedPet.getNumericId();
                        String preName = preselectedPet.getName();
                        for (int i = 0; i < mapped.size(); i++) {
                            PetItem item = mapped.get(i);
                            boolean idMatch = preId != null && preId.equals(item.getNumericId());
                            boolean nameMatch = preId == null && preName != null
                                    && preName.equalsIgnoreCase(item.getName());
                            if (idMatch || nameMatch) {
                                nextIndex = i;
                                break;
                            }
                        }
                        appliedPreselectedPet = true;
                    }
                    if (nextIndex < 0 || nextIndex >= mapped.size()) {
                        nextIndex = 0;
                    }
                } else {
                    nextIndex = -1;
                }

                selectedIndex.setValue(nextIndex);
                pets.setValue(mapped);
            }

            @Override
            public void onError(int httpCode, String message) {
                List<PetItem> current = pets.getValue();
                if (current == null || current.isEmpty()) {
                    pets.setValue(Collections.emptyList());
                }
                errorMessage.setValue(message);
            }
        });
    }

    public void deletePet(Long petId, PetItem preselectedPet) {
        if (petId == null) return;
        petRepository.deletePet(petId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadMyPets(preselectedPet);
            }

            @Override
            public void onError(int httpCode, String message) {
                errorMessage.setValue(message);
            }
        });
    }
}
