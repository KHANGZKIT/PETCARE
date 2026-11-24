package com.example.petcareui.ui.pets;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.api.ApiErrorParser;
import com.example.petcareui.data.dto.request.PetRequest;
import com.example.petcareui.data.dto.response.PetResponse;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.databinding.ActivityAddPetBinding;
import com.example.petcareui.databinding.DialogPetDeleteConfirmBinding;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPetActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_PET = "extra_edit_pet";

    private ActivityAddPetBinding binding;
    private String selectedSpecies = "Chó";
    private String selectedGender = "Đực";
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
        setupGenderPills();
        populateEditStateIfNeeded();
        setupSaveButton();

        UiMotionUtils.animateEntranceSections(binding.sectionUploadAvatar, binding.layoutFormSection);
        UiMotionUtils.attachSpringTouch(
                binding.btnBack,
                binding.btnUploadAvatar,
                binding.chipDog,
                binding.chipCat,
                binding.chipOther,
                binding.chipMale,
                binding.chipFemale,
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

    private void setupGenderPills() {
        binding.chipMale.setOnClickListener(v -> {
            selectedGender = "Đực";
            applyActiveGenderPill(binding.chipMale);
        });

        binding.chipFemale.setOnClickListener(v -> {
            selectedGender = "Cái";
            applyActiveGenderPill(binding.chipFemale);
        });
    }

    private void applyActiveSpeciesPill(LinearLayout activePill) {
        UiMotionUtils.animateSelectionPop(activePill);

        stylePill(binding.chipDog, binding.ivChipDog, binding.tvChipDog, activePill == binding.chipDog);
        stylePill(binding.chipCat, binding.ivChipCat, binding.tvChipCat, activePill == binding.chipCat);
        stylePill(binding.chipOther, binding.ivChipOther, binding.tvChipOther, activePill == binding.chipOther);
    }

    private void applyActiveGenderPill(LinearLayout activePill) {
        UiMotionUtils.animateSelectionPop(activePill);

        stylePill(binding.chipMale, null, binding.tvChipMale, activePill == binding.chipMale);
        stylePill(binding.chipFemale, null, binding.tvChipFemale, activePill == binding.chipFemale);
    }

    private void stylePill(LinearLayout pill, ImageView icon, TextView label, boolean isSelected) {
        if (isSelected) {
            pill.setBackgroundResource(R.drawable.bg_species_pill_selected);
            int activeColor = ContextCompat.getColor(this, R.color.ds_primary);
            if (icon != null) icon.setColorFilter(activeColor);
            label.setTextColor(activeColor);
            label.setTypeface(ResourcesCompat.getFont(this, R.font.open_sans_semibold));
        } else {
            pill.setBackgroundResource(R.drawable.bg_species_pill_unselected);
            int inactiveColor = ContextCompat.getColor(this, R.color.ds_text_secondary);
            if (icon != null) icon.setColorFilter(inactiveColor);
            label.setTextColor(inactiveColor);
            label.setTypeface(ResourcesCompat.getFont(this, R.font.open_sans_medium));
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

        // Loài & Giống
        String raw = editingPet.getRawSpecies() != null ? editingPet.getRawSpecies().trim() : "";
        String lower = raw.toLowerCase(Locale.ROOT);
        String breedVal = editingPet.getBreed() != null ? editingPet.getBreed().trim() : "";

        if (lower.startsWith("mèo") || lower.startsWith("meo")) {
            selectedSpecies = "Mèo";
            applyActiveSpeciesPill(binding.chipCat);
            if (breedVal.isEmpty() || breedVal.equalsIgnoreCase("Mèo cưng")) {
                String rest = raw.length() > 3 ? raw.substring(3).replaceFirst("^[\\s·\\-,:]+", "").trim() : "";
                breedVal = rest;
            }
        } else if (lower.startsWith("chó") || lower.startsWith("cho")) {
            selectedSpecies = "Chó";
            applyActiveSpeciesPill(binding.chipDog);
            if (breedVal.isEmpty() || breedVal.equalsIgnoreCase("Chó cưng")) {
                String rest = raw.length() > 3 ? raw.substring(3).replaceFirst("^[\\s·\\-,:]+", "").trim() : "";
                breedVal = rest;
            }
        } else {
            selectedSpecies = "Khác";
            applyActiveSpeciesPill(binding.chipOther);
            if (breedVal.isEmpty() && !raw.equalsIgnoreCase("Khác")) {
                breedVal = raw;
            }
        }
        binding.etPetBreed.setText(breedVal);

        // Giới tính
        String sex = editingPet.getSex() != null ? editingPet.getSex().replace("♂", "").replace("♀", "").trim() : "";
        if (sex.equalsIgnoreCase("Cái")) {
            selectedGender = "Cái";
            applyActiveGenderPill(binding.chipFemale);
        } else {
            selectedGender = "Đực";
            applyActiveGenderPill(binding.chipMale);
        }

        // Tuổi
        if (editingPet.getAge() != null && !editingPet.getAge().trim().isEmpty()) {
            binding.etPetAge.setText(editingPet.getAge().trim());
        }

        // Cân nặng (kg)
        if (editingPet.getWeightValue() != null && editingPet.getWeightValue() > 0) {
            double w = editingPet.getWeightValue();
            binding.etPetWeight.setText(w == Math.floor(w)
                    ? String.format(Locale.US, "%.0f", w)
                    : String.format(Locale.US, "%.1f", w));
        } else if (editingPet.getWeight() != null && !editingPet.getWeight().trim().isEmpty()) {
            String numeric = editingPet.getWeight().replace("kg", "").replace(",", ".").trim();
            binding.etPetWeight.setText(numeric);
        }

        // Ghi chú
        if (editingPet.getNotes() != null && !editingPet.getNotes().trim().isEmpty()) {
            binding.etPetNotes.setText(editingPet.getNotes().trim());
        }

        // Hiện nút Xóa hồ sơ rõ ràng khi mở màn Chỉnh sửa
        binding.btnDeletePet.setVisibility(View.VISIBLE);
        UiMotionUtils.attachSpringTouch(binding.btnDeletePet);
        binding.btnDeletePet.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void showDeleteConfirmDialog() {
        if (editingPet == null) return;
        Long editId = editingPet.getNumericId();
        if (editId == null) return;

        Dialog dialog = new Dialog(this);
        DialogPetDeleteConfirmBinding dialogBinding = DialogPetDeleteConfirmBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        String petName = editingPet.getName() != null && !editingPet.getName().trim().isEmpty()
                ? editingPet.getName().trim() : "thú cưng";
        dialogBinding.tvDeleteTitle.setText("Xóa bé " + petName + "?");
        dialogBinding.tvDeleteMessage.setText("Bạn có chắc chắn muốn xóa bé " + petName + " khỏi danh sách hồ sơ? Thao tác này không thể hoàn tác.");

        dialogBinding.btnCancelDelete.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnConfirmDelete.setOnClickListener(v -> {
            dialog.dismiss();
            hideError();
            setLoading(true);
            ApiClient.getPetApi().deletePet(editId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (isFinishing() || isDestroyed()) return;
                    setLoading(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(AddPetActivity.this, "Đã xóa hồ sơ bé " + petName, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        showError(ApiErrorParser.getMessage(response, "Không thể xóa hồ sơ thú cưng lúc này."));
                        binding.layoutErrorBanner.post(() -> binding.layoutErrorBanner.getParent().requestChildFocus(
                                binding.layoutErrorBanner, binding.layoutErrorBanner));
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    setLoading(false);
                    showError("Lỗi kết nối máy chủ khi xóa hồ sơ thú cưng.");
                }
            });
        });

        dialog.show();
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

            String breed = binding.etPetBreed.getText() != null
                    ? binding.etPetBreed.getText().toString().replaceAll("\\s+", " ").trim() : "";
            String age = binding.etPetAge.getText() != null
                    ? binding.etPetAge.getText().toString().trim() : "";
            String weightStr = binding.etPetWeight.getText() != null
                    ? binding.etPetWeight.getText().toString().replace(",", ".").trim() : "";
            String notes = binding.etPetNotes.getText() != null
                    ? binding.etPetNotes.getText().toString().trim() : "";

            Double weight = null;
            if (!weightStr.isEmpty()) {
                try {
                    weight = Double.parseDouble(weightStr);
                    if (weight <= 0 || weight > 200) {
                        showError("Cân nặng phải nằm trong khoảng 0.1 – 200 kg.");
                        binding.etPetWeight.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    showError("Cân nặng không hợp lệ. Vui lòng nhập số (VD: 4.2).");
                    binding.etPetWeight.requestFocus();
                    return;
                }
            }

            hideError();
            setLoading(true);

            PetRequest request = new PetRequest(
                    name,
                    selectedSpecies,
                    breed.isEmpty() ? null : breed,
                    selectedGender,
                    age.isEmpty() ? null : age,
                    weight,
                    notes.isEmpty() ? null : notes
            );
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
