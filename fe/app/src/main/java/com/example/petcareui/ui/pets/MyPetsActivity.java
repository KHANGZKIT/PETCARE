package com.example.petcareui.ui.pets;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.DateItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.data.model.PetResponse;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.data.model.TimeSlotItem;
import com.example.petcareui.databinding.ActivityMyPetsBinding;
import com.example.petcareui.databinding.ItemPetSelectableBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình C08: Thú cưng của tôi (My Pets) - Bước 3/4 của luồng đặt lịch & Tab chính "Thú cưng".
 * Bám sát nguyên mẫu React C08MyPets và kết nối Backend REST API /api/v1/pets.
 */
public class MyPetsActivity extends AppCompatActivity {

    public static final String EXTRA_CLINIC = "extra_clinic";
    public static final String EXTRA_SELECTED_SERVICE = "extra_selected_service";
    public static final String EXTRA_SELECTED_DATE = "extra_selected_date";
    public static final String EXTRA_SELECTED_TIME = "extra_selected_time";
    public static final String EXTRA_BOOKING_MODE = "extra_booking_mode";

    private ActivityMyPetsBinding binding;

    private ClinicItem clinicItem;
    private ServiceItem serviceItem;
    private DateItem dateItem;
    private TimeSlotItem timeSlotItem;
    private boolean isBookingMode = true;

    private final List<PetItem> pets = new ArrayList<>();
    private final List<ItemPetSelectableBinding> petBindings = new ArrayList<>();
    private int selectedPetIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPetsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractIntentData();
        setupTopBar();
        setupEmptyStateActions();
        setupStickyBottomBar();
        setupBottomNav();
        loadPetsFromApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPetsFromApi();
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_CLINIC)) {
            try {
                clinicItem = (ClinicItem) getIntent().getSerializableExtra(EXTRA_CLINIC);
            } catch (Exception ignored) {
            }
        }
        if (getIntent().hasExtra(EXTRA_SELECTED_SERVICE)) {
            try {
                serviceItem = (ServiceItem) getIntent().getSerializableExtra(EXTRA_SELECTED_SERVICE);
            } catch (Exception ignored) {
            }
        }
        if (getIntent().hasExtra(EXTRA_SELECTED_DATE)) {
            try {
                dateItem = (DateItem) getIntent().getSerializableExtra(EXTRA_SELECTED_DATE);
            } catch (Exception ignored) {
            }
        }
        if (getIntent().hasExtra(EXTRA_SELECTED_TIME)) {
            try {
                timeSlotItem = (TimeSlotItem) getIntent().getSerializableExtra(EXTRA_SELECTED_TIME);
            } catch (Exception ignored) {
            }
        }
        isBookingMode = getIntent().getBooleanExtra(EXTRA_BOOKING_MODE, true);
    }

    private void setupTopBar() {
        binding.btnBack.setOnClickListener(v -> finish());

        View.OnClickListener addPetListener = v -> {
            Intent intent = new Intent(this, AddPetActivity.class);
            startActivity(intent);
        };
        binding.btnAddPetTop.setOnClickListener(addPetListener);
        binding.cardAddOtherPet.setOnClickListener(addPetListener);
        binding.btnAddFirstPet.setOnClickListener(addPetListener);

        if (!isBookingMode) {
            binding.tvSubtitleBooking.setVisibility(View.GONE);
        }
    }

    private void loadPetsFromApi() {
        ApiClient.getPetApi().getMyPets().enqueue(new Callback<List<PetResponse>>() {
            @Override
            public void onResponse(Call<List<PetResponse>> call, Response<List<PetResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PetResponse> body = response.body();
                    pets.clear();
                    for (PetResponse res : body) {
                        pets.add(res.toPetItem());
                    }

                    if (pets.isEmpty()) {
                        showEmptyState();
                    } else {
                        showPetsList();
                        if (selectedPetIndex >= pets.size()) {
                            selectedPetIndex = 0;
                        }
                        renderPets();
                    }
                } else {
                    if (pets.isEmpty()) {
                        setupFallbackPets();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PetResponse>> call, Throwable t) {
                if (pets.isEmpty()) {
                    setupFallbackPets();
                }
            }
        });
    }

    private void setupFallbackPets() {
        pets.clear();
        pets.add(new PetItem("1", "Mochi", "Chó", "Pomeranian", "2 tuổi", "12/03/2024", "Cái", "3,4 kg", R.drawable.img_pet_mochi));
        pets.add(new PetItem("2", "Bông", "Mèo", "Anh lông ngắn", "4 tuổi", "02/07/2022", "Đực", "5,1 kg", R.drawable.img_pet_bong));
        pets.add(new PetItem("3", "Lựu", "Chó", "Corgi", "6 tháng", "18/04/2026", "Đực", "7,2 kg", R.drawable.img_pet_luu));
        selectedPetIndex = 0;
        showPetsList();
        renderPets();
    }

    private void renderPets() {
        petBindings.clear();
        binding.containerPetsList.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < pets.size(); i++) {
            final int index = i;
            PetItem pet = pets.get(i);
            ItemPetSelectableBinding itemBinding = ItemPetSelectableBinding.inflate(inflater, binding.containerPetsList, false);

            itemBinding.ivPetAvatar.setImageResource(pet.getImageResId());
            itemBinding.tvPetName.setText(pet.getName());
            itemBinding.tvPetBreed.setText(pet.getSpecies() + " · " + pet.getBreed());
            itemBinding.tvPetAge.setText(pet.getAge());
            itemBinding.tvPetSex.setText(pet.getSex());
            itemBinding.tvPetWeight.setText(pet.getWeight());

            itemBinding.getRoot().setOnClickListener(v -> selectPet(index));

            // Nhấn giữ để xóa thú cưng
            itemBinding.getRoot().setOnLongClickListener(v -> {
                showDeletePetDialog(pet);
                return true;
            });

            petBindings.add(itemBinding);
            binding.containerPetsList.addView(itemBinding.getRoot());
        }

        updatePetSelectionUI();
    }

    private void selectPet(int index) {
        selectedPetIndex = index;
        updatePetSelectionUI();
    }

    private void showDeletePetDialog(PetItem pet) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa thú cưng")
                .setMessage("Bạn có chắc chắn muốn xóa bé " + pet.getName() + " khỏi danh sách?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Long numericId = pet.getNumericId();
                    if (numericId != null) {
                        ApiClient.getPetApi().deletePet(numericId).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(MyPetsActivity.this, "Đã xóa bé " + pet.getName(), Toast.LENGTH_SHORT).show();
                                    loadPetsFromApi();
                                } else {
                                    Toast.makeText(MyPetsActivity.this, "Không thể xóa bé lúc này", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(MyPetsActivity.this, "Lỗi kết nối khi xóa thú cưng", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updatePetSelectionUI() {
        for (int i = 0; i < petBindings.size(); i++) {
            ItemPetSelectableBinding b = petBindings.get(i);
            boolean isSelected = (i == selectedPetIndex);

            if (isSelected) {
                b.layoutPetCard.setBackgroundResource(R.drawable.bg_service_card_selected);
                b.ivPetCheck.setImageResource(R.drawable.bg_radio_selected);
            } else {
                b.layoutPetCard.setBackgroundResource(R.drawable.bg_service_card_default);
                b.ivPetCheck.setImageResource(R.drawable.bg_radio_unselected);
            }
        }

        if (selectedPetIndex >= 0 && selectedPetIndex < pets.size()) {
            PetItem selected = pets.get(selectedPetIndex);
            binding.tvSelectedPetName.setText(selected.getName());
            binding.btnContinue.setEnabled(true);
            binding.btnContinue.setAlpha(1.0f);
        } else {
            binding.tvSelectedPetName.setText("Chưa chọn");
            binding.btnContinue.setEnabled(false);
            binding.btnContinue.setAlpha(0.5f);
        }
    }

    public void showEmptyState() {
        binding.layoutPetsContent.setVisibility(View.GONE);
        binding.layoutPetsEmpty.setVisibility(View.VISIBLE);
        binding.layoutStickyBottom.setVisibility(View.GONE);
        binding.tvSubtitleBooking.setVisibility(View.GONE);
    }

    public void showPetsList() {
        binding.layoutPetsContent.setVisibility(View.VISIBLE);
        binding.layoutPetsEmpty.setVisibility(View.GONE);
        binding.layoutStickyBottom.setVisibility(View.VISIBLE);
        if (isBookingMode) {
            binding.tvSubtitleBooking.setVisibility(View.VISIBLE);
        }
    }

    private void setupEmptyStateActions() {
        binding.btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void setupStickyBottomBar() {
        binding.btnContinue.setOnClickListener(v -> {
            if (selectedPetIndex >= 0 && selectedPetIndex < pets.size()) {
                PetItem chosen = pets.get(selectedPetIndex);
                Intent intent = new Intent(this, com.example.petcareui.ui.booking.BookingReviewActivity.class);
                intent.putExtra(com.example.petcareui.ui.booking.BookingReviewActivity.EXTRA_CLINIC, clinicItem);
                intent.putExtra(com.example.petcareui.ui.booking.BookingReviewActivity.EXTRA_SELECTED_SERVICE, serviceItem);
                intent.putExtra(com.example.petcareui.ui.booking.BookingReviewActivity.EXTRA_SELECTED_DATE, dateItem);
                intent.putExtra(com.example.petcareui.ui.booking.BookingReviewActivity.EXTRA_SELECTED_TIME, timeSlotItem);
                intent.putExtra(com.example.petcareui.ui.booking.BookingReviewActivity.EXTRA_SELECTED_PET, chosen);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Vui lòng chọn một bé để tiếp tục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNav() {
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        binding.navAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.petcareui.ui.appointments.MyAppointmentsActivity.class);
            startActivity(intent);
        });

        binding.navPets.setOnClickListener(v -> {
            // Đang ở tab Thú cưng
        });

        binding.navProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Chuyển sang Tài khoản (C15)", Toast.LENGTH_SHORT).show();
        });
    }
}
