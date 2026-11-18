package com.example.petcareui.ui.pets;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.dto.request.PetRequest;
import com.example.petcareui.data.dto.response.PetResponse;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.databinding.ActivityAddPetBinding;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPetActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_PET = "extra_edit_pet";

    private ActivityAddPetBinding binding;
    private String selectedSpecies = "Chó";
    private PetItem editingPet = null;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onAvatarPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.ds_home_bg));
        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopBar, binding.layoutAddPetRoot);

        extractEditIntent();
        setupNavigationAndAvatar();
        setupSpeciesPills();
        populateEditStateIfNeeded();
        setupSaveButton();

        UiMotionUtils.animateEntranceSections(binding.sectionUploadAvatar, binding.layoutFormSection);
        UiMotionUtils.attachSpringTouch(
                binding.btnBack,
                binding.btnUploadAvatar,
                binding.chipDog,
                binding.chipCat,
                binding.chipOther,
                binding.btnSavePet
        );
    }

    private void extractEditIntent() {
        if (getIntent().hasExtra(EXTRA_EDIT_PET)) {
            try {
                editingPet = (PetItem) getIntent().getSerializableExtra(EXTRA_EDIT_PET);
            } catch (Exception ignored) {
            }
        }
    }

    private void setupNavigationAndAvatar() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnUploadAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void onAvatarPicked(Uri uri) {
        if (uri == null) return;
        binding.layoutCameraPlaceholder.setVisibility(View.GONE);
        binding.ivSelectedAvatarPreview.setVisibility(View.VISIBLE);
        binding.ivSelectedAvatarPreview.setImageURI(uri);
        binding.tvUploadAvatarLabel.setText("Chạm để đổi ảnh khác");
        UiMotionUtils.animateSelectionPop(binding.btnUploadAvatar);
    }

    private void setupSpeciesPills() {
        binding.chipDog.setOnClickListener(v -> {
            selectedSpecies = "Chó";
            applyActiveSpeciesPill(binding.chipDog);
        });

        binding.chipCat.setOnClickListener(v -> {
            selectedSpecies = "Mèo";
            applyActiveSpeciesPill(binding.chipCat);
        });

        binding.chipOther.setOnClickListener(v -> {
            selectedSpecies = "Khác";
            applyActiveSpeciesPill(binding.chipOther);
        });
    }

    private void applyActiveSpeciesPill(LinearLayout activePill) {
        UiMotionUtils.animateSelectionPop(activePill);

        stylePill(binding.chipDog, binding.ivChipDog, binding.tvChipDog, activePill == binding.chipDog);
        stylePill(binding.chipCat, binding.ivChipCat, binding.tvChipCat, activePill == binding.chipCat);
        stylePill(binding.chipOther, binding.ivChipOther, binding.tvChipOther, activePill == binding.chipOther);
    }

    private void stylePill(LinearLayout pill, ImageView icon, TextView label, boolean isSelected) {
        if (isSelected) {
            pill.setBackgroundResource(R.drawable.bg_species_pill_selected);
            int activeColor = ContextCompat.getColor(this, R.color.ds_primary_dark);
            icon.setColorFilter(activeColor);
            label.setTextColor(activeColor);
            label.setTypeface(ResourcesCompat.getFont(this, R.font.plus_jakarta_sans_semibold));
        } else {
            pill.setBackgroundResource(R.drawable.bg_species_pill_unselected);
            int inactiveColor = ContextCompat.getColor(this, R.color.ds_text_secondary);
            icon.setColorFilter(inactiveColor);
            label.setTextColor(inactiveColor);
            label.setTypeface(ResourcesCompat.getFont(this, R.font.plus_jakarta_sans_medium));
        }
    }

    private void populateEditStateIfNeeded() {
        if (editingPet == null) return;

        binding.tvAddPetScreenTitle.setText("Chỉnh sửa thú cưng");
        binding.btnSavePet.setText("Lưu thay đổi");

        if (editingPet.getImageResId() != 0) {
            binding.layoutCameraPlaceholder.setVisibility(View.GONE);
            binding.ivSelectedAvatarPreview.setVisibility(View.VISIBLE);
            binding.ivSelectedAvatarPreview.setImageResource(editingPet.getImageResId());
        }

        if (editingPet.getName() != null) {
            binding.etPetName.setText(editingPet.getName());
            binding.etPetName.setSelection(binding.etPetName.getText().length());
        }

        String raw = editingPet.getRawSpecies() != null ? editingPet.getRawSpecies().trim() : "";
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.startsWith("mèo") || lower.startsWith("meo")) {
            selectedSpecies = "Mèo";
            applyActiveSpeciesPill(binding.chipCat);
            String rest = raw.length() > 3 ? raw.substring(3).replaceFirst("^[\\s·\\-,:]+", "").trim() : "";
            binding.etPetBreed.setText(rest);
        } else if (lower.startsWith("chó") || lower.startsWith("cho")) {
            selectedSpecies = "Chó";
            applyActiveSpeciesPill(binding.chipDog);
            String rest = raw.length() > 3 ? raw.substring(3).replaceFirst("^[\\s·\\-,:]+", "").trim() : "";
            binding.etPetBreed.setText(rest);
        } else {
            selectedSpecies = "Khác";
            applyActiveSpeciesPill(binding.chipOther);
            if (!raw.equalsIgnoreCase("Khác")) {
                binding.etPetBreed.setText(raw);
            }
        }
    }

    private void setupSaveButton() {
        binding.btnSavePet.setOnClickListener(v -> {
            String name = binding.etPetName.getText() != null
                    ? binding.etPetName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                showError("Vui lòng nhập tên thú cưng của bạn.");
                binding.etPetName.requestFocus();
                return;
            }

            String breedOrNote = binding.etPetBreed.getText() != null
                    ? binding.etPetBreed.getText().toString().replaceAll("\\s+", " ").trim() : "";

            String combinedSpecies;
            if ("Khác".equals(selectedSpecies)) {
                combinedSpecies = breedOrNote.isEmpty() ? "Khác" : breedOrNote;
            } else {
                combinedSpecies = breedOrNote.isEmpty() ? selectedSpecies : (selectedSpecies + " " + breedOrNote);
            }

            if (combinedSpecies.length() > 50) {
                combinedSpecies = combinedSpecies.substring(0, 50).trim();
            }

            hideError();
            setLoading(true);

            PetRequest request = new PetRequest(name, combinedSpecies);
            Long editId = editingPet != null ? editingPet.getNumericId() : null;

            Call<PetResponse> apiCall = (editId != null)
                    ? ApiClient.getPetApi().updatePet(editId, request)
                    : ApiClient.getPetApi().addPet(request);

            apiCall.enqueue(new Callback<PetResponse>() {
                @Override
                public void onResponse(Call<PetResponse> call, Response<PetResponse> response) {
                    if (isFinishing() || isDestroyed()) return;
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        showError("Không thể lưu thông tin thú cưng. Vui lòng kiểm tra lại dữ liệu.");
                    }
                }

                @Override
                public void onFailure(Call<PetResponse> call, Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    setLoading(false);
                    showError("Lỗi kết nối máy chủ. Vui lòng thử lại sau.");
                }
            });
        });
    }

    private void showError(String message) {
        binding.layoutErrorBanner.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message);
    }

    private void hideError() {
        binding.layoutErrorBanner.setVisibility(View.GONE);
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.btnSavePet.setEnabled(false);
            binding.btnSavePet.setText("");
            binding.pbLoading.setVisibility(View.VISIBLE);
        } else {
            binding.btnSavePet.setEnabled(true);
            binding.btnSavePet.setText(editingPet != null ? "Lưu thay đổi" : "Lưu thú cưng");
            binding.pbLoading.setVisibility(View.GONE);
        }
    }
}
