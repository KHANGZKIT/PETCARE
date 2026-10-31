package com.example.petcareui.ui.pets;

import android.os.Bundle;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình C09: Thêm thú cưng mới (Add Pet).
 * Bám sát DESIGN_SYSTEM_SPEC (C09) và kết nối trực tiếp với backend /api/v1/pets.
 */
public class AddPetActivity extends AppCompatActivity {

    private ActivityAddPetBinding binding;
    private String selectedSpecies = "Chó";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupTopBar();
        setupSpeciesChips();
        setupSaveButton();
    }

    private void setupTopBar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupSpeciesChips() {
        binding.chipDog.setOnClickListener(v -> {
            selectedSpecies = "Chó";
            updateChipUI(binding.chipDog);
            binding.layoutOtherSpecies.setVisibility(View.GONE);
        });

        binding.chipCat.setOnClickListener(v -> {
            selectedSpecies = "Mèo";
            updateChipUI(binding.chipCat);
            binding.layoutOtherSpecies.setVisibility(View.GONE);
        });

        binding.chipOther.setOnClickListener(v -> {
            selectedSpecies = "";
            updateChipUI(binding.chipOther);
            binding.layoutOtherSpecies.setVisibility(View.VISIBLE);
            binding.etOtherSpecies.requestFocus();
        });
    }

    private void updateChipUI(TextView activeChip) {
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

            hideError();
            setLoading(true);

            PetRequest request = new PetRequest(name, species);
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
            binding.btnSavePet.setText("Thêm thú cưng");
            binding.pbLoading.setVisibility(View.GONE);
        }
    }
}
