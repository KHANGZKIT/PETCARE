package com.example.petcareui.ui.pets;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.model.PetRequest;
import com.example.petcareui.data.model.PetResponse;
import com.example.petcareui.databinding.ActivityAddPetBinding;
import com.example.petcareui.utils.UiMotionUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình C09: Thêm thú cưng mới (Add Pet).
 * Kết hợp thẻ xem trước Hồ sơ thú cưng trực quan & hiệu ứng chuyển động mượt mà.
 */
public class AddPetActivity extends AppCompatActivity {

    private ActivityAddPetBinding binding;
    private String selectedSpecies = "Chó";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopBar, binding.layoutAddPetRoot);

        setupTopBar();
        setupSpeciesChips();
        setupLivePreview();
        setupSaveButton();

        UiMotionUtils.animateEntranceSections(binding.cardHeroPreview, binding.layoutFormSection);
        UiMotionUtils.attachSpringTouch(
                binding.btnBack,
                binding.chipDog,
                binding.chipCat,
                binding.chipOther,
                binding.btnSavePet
        );
    }

    private void setupTopBar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupLivePreview() {
        binding.etPetName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s != null ? s.toString().trim() : "";
                if (input.isEmpty()) {
                    binding.tvPreviewTitle.setText("Hồ sơ bé cưng");
                } else {
                    binding.tvPreviewTitle.setText("Hồ sơ của " + input);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupSpeciesChips() {
        binding.chipDog.setOnClickListener(v -> {
            selectedSpecies = "Chó";
            updateChipUI(binding.chipDog);
            binding.ivPetPreviewAvatar.setImageResource(R.drawable.ic_avatar_shiba);
            UiMotionUtils.animateSelectionPop(binding.ivPetPreviewAvatar);
            binding.layoutOtherSpecies.setVisibility(View.GONE);
        });

        binding.chipCat.setOnClickListener(v -> {
            selectedSpecies = "Mèo";
            updateChipUI(binding.chipCat);
            binding.ivPetPreviewAvatar.setImageResource(R.drawable.img_pet_bong);
            UiMotionUtils.animateSelectionPop(binding.ivPetPreviewAvatar);
            binding.layoutOtherSpecies.setVisibility(View.GONE);
        });

        binding.chipOther.setOnClickListener(v -> {
            selectedSpecies = "";
            updateChipUI(binding.chipOther);
            binding.ivPetPreviewAvatar.setImageResource(R.drawable.ic_avatar_corgi);
            UiMotionUtils.animateSelectionPop(binding.ivPetPreviewAvatar);
            binding.layoutOtherSpecies.setVisibility(View.VISIBLE);
            binding.etOtherSpecies.requestFocus();
        });
    }

    private void updateChipUI(TextView activeChip) {
        UiMotionUtils.animateSelectionPop(activeChip);
        TextView[] chips = new TextView[]{binding.chipDog, binding.chipCat, binding.chipOther};
        for (TextView chip : chips) {
            boolean isActive = (chip == activeChip);
            chip.setBackgroundResource(isActive ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, isActive ? R.color.white : R.color.text_muted));
        }
    }

    private void setupSaveButton() {
        binding.btnSavePet.setOnClickListener(v -> {
            String name = binding.etPetName.getText().toString().trim();
            if (name.isEmpty()) {
                showError("Vui lòng nhập tên thú cưng của bạn");
                binding.etPetName.requestFocus();
                return;
            }

            String species = selectedSpecies;
            if (species.isEmpty()) {
                species = binding.etOtherSpecies.getText().toString().trim();
                if (species.isEmpty()) {
                    showError("Vui lòng nhập loài thú cưng (Chó, Mèo, Thỏ...)");
                    binding.etOtherSpecies.requestFocus();
                    return;
                }
            }

            String breed = binding.etPetBreed.getText().toString().trim();
            String combinedSpecies = breed.isEmpty() ? species : (species + " " + breed);

            hideError();
            setLoading(true);

            PetRequest request = new PetRequest(name, combinedSpecies);
            ApiClient.getPetApi().addPet(request).enqueue(new Callback<PetResponse>() {
                @Override
                public void onResponse(Call<PetResponse> call, Response<PetResponse> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(AddPetActivity.this, "Đã thêm bé " + response.body().getName() + " thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        showError("Không thể thêm thú cưng. Vui lòng kiểm tra lại thông tin hoặc đăng nhập lại.");
                    }
                }

                @Override
                public void onFailure(Call<PetResponse> call, Throwable t) {
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
            binding.btnSavePet.setText("Lưu hồ sơ thú cưng");
            binding.pbLoading.setVisibility(View.GONE);
        }
    }
}
