package com.example.petcareui.ui.pets;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.DateItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.data.dto.response.PetResponse;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.data.model.TimeSlotItem;
import com.example.petcareui.databinding.ActivityMyPetsBinding;
import com.example.petcareui.databinding.DialogPetDeleteConfirmBinding;
import com.example.petcareui.databinding.DialogPetOptionsBinding;
import com.example.petcareui.databinding.ItemPetSelectableBinding;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình C08: Thú cưng của tôi (My Pets) - Bước 3/4 của luồng đặt lịch & Tab chính "Thú cưng".
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

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopBar, binding.bottomNavigation);

        extractIntentData();
        setupTopBar();
        setupEmptyStateActions();
        setupStickyBottomBar();
        setupBottomNav();

        // Ẩn/hiện ngay thanh Đặt lịch và thanh Điều hướng đáy theo chế độ
        if (!isBookingMode) {
            binding.layoutStickyBottom.setVisibility(View.GONE);
            binding.tvSubtitleBooking.setVisibility(View.GONE);
            binding.bottomNavigation.setVisibility(View.VISIBLE);
        } else {
            binding.layoutStickyBottom.setVisibility(View.VISIBLE);
            binding.tvSubtitleBooking.setVisibility(View.VISIBLE);
            binding.bottomNavigation.setVisibility(View.GONE);
        }

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

        UiMotionUtils.attachSpringTouch(
                binding.btnBack,
                binding.btnAddPetTop,
                binding.cardAddOtherPet,
                binding.btnAddFirstPet,
                binding.btnContinue
        );

        if (!isBookingMode) {
            binding.tvSubtitleBooking.setText("Sổ sức khỏe & hồ sơ thú cưng của bạn");
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
                        showEmptyState();
                    }
                    Toast.makeText(MyPetsActivity.this, "Không thể tải danh sách thú cưng (Mã: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PetResponse>> call, Throwable t) {
                if (pets.isEmpty()) {
                    showEmptyState();
                }
                Toast.makeText(MyPetsActivity.this, "Lỗi kết nối máy chủ khi tải thú cưng.", Toast.LENGTH_SHORT).show();
            }
        });
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

            // Xử lý hiển thị loài và giống sạch sẽ, tránh lặp chữ
            if (pet.getSpecies() != null && pet.getBreed() != null
                    && !pet.getSpecies().trim().equalsIgnoreCase(pet.getBreed().trim())) {
                itemBinding.tvPetBreed.setText(pet.getSpecies() + " · " + pet.getBreed());
            } else if (pet.getSpecies() != null) {
                itemBinding.tvPetBreed.setText(pet.getSpecies());
            } else {
                itemBinding.tvPetBreed.setText("Thú cưng");
            }

            // Badge loài: 🐾 Mèo / 🐾 Chó
            String sp = pet.getSpecies() != null ? pet.getSpecies() : "Thú cưng";
            itemBinding.tvPetSpeciesBadge.setText("🐾 " + sp);

            bindBadgeOrHide(itemBinding.tvPetAge, pet.getAge());
            bindBadgeOrHide(itemBinding.tvPetSex, pet.getSex());
            bindBadgeOrHide(itemBinding.tvPetWeight, pet.getWeight());

            UiMotionUtils.attachSpringTouch(itemBinding.getRoot());

            if (isBookingMode) {
                itemBinding.btnDeletePetDirect.setVisibility(View.GONE);
                itemBinding.ivPetCheck.setVisibility(View.VISIBLE);
                itemBinding.getRoot().setOnClickListener(v -> selectPet(index));
            } else {
                itemBinding.btnDeletePetDirect.setVisibility(View.VISIBLE);
                itemBinding.ivPetCheck.setVisibility(View.GONE);
                itemBinding.btnDeletePetDirect.setOnClickListener(v -> showDeletePetDialog(pet));
                itemBinding.getRoot().setOnClickListener(v -> {
                    Intent intent = new Intent(this, com.example.petcareui.ui.clinic.ClinicListActivity.class);
                    startActivity(intent);
                });
            }
            itemBinding.getRoot().setOnLongClickListener(v -> {
                showDeletePetDialog(pet);
                return true;
            });

            petBindings.add(itemBinding);
            binding.containerPetsList.addView(itemBinding.getRoot());
        }

        updatePetSelectionUI();
        UiMotionUtils.animateStaggeredChildren(binding.containerPetsList, 25);
    }

    private void bindBadgeOrHide(android.widget.TextView badge, String value) {
        if (value == null || value.trim().isEmpty() || "—".equals(value.trim()) || "-".equals(value.trim())) {
            badge.setVisibility(View.GONE);
        } else {
            badge.setVisibility(View.VISIBLE);
            badge.setText(value);
        }
    }

    private void selectPet(int index) {
        selectedPetIndex = index;
        if (index >= 0 && index < petBindings.size()) {
            UiMotionUtils.animateSelectionPop(petBindings.get(index).getRoot());
        }
        updatePetSelectionUI();
    }

    private void showDeletePetDialog(PetItem pet) {
        Dialog dialog = new Dialog(this);
        DialogPetDeleteConfirmBinding dialogBinding = DialogPetDeleteConfirmBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialogBinding.tvDeleteTitle.setText("Xóa bé " + pet.getName() + "?");
        dialogBinding.tvDeleteMessage.setText("Bạn có chắc chắn muốn xóa bé " + pet.getName() + " khỏi danh sách hồ sơ? Thao tác này không thể hoàn tác.");

        dialogBinding.btnCancelDelete.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnConfirmDelete.setOnClickListener(v -> {
            dialog.dismiss();
            Long numericId = pet.getNumericId();
            if (numericId != null) {
                ApiClient.getPetApi().deletePet(numericId).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MyPetsActivity.this, "Đã xóa bé " + pet.getName() + " thành công", Toast.LENGTH_SHORT).show();
                            loadPetsFromApi();
                        } else {
                            Toast.makeText(MyPetsActivity.this, "Không thể xóa bé lúc này (Mã: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(MyPetsActivity.this, "Lỗi kết nối khi xóa thú cưng", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    private void updatePetSelectionUI() {
        for (int i = 0; i < petBindings.size(); i++) {
            ItemPetSelectableBinding b = petBindings.get(i);
            if (!isBookingMode) {
                b.layoutPetCard.setBackgroundResource(R.drawable.bg_service_card_default);
                b.btnDeletePetDirect.setVisibility(View.VISIBLE);
                b.ivPetCheck.setVisibility(View.GONE);
            } else {
                b.btnDeletePetDirect.setVisibility(View.GONE);
                b.ivPetCheck.setVisibility(View.VISIBLE);
                boolean isSelected = (i == selectedPetIndex);
                if (isSelected) {
                    b.layoutPetCard.setBackgroundResource(R.drawable.bg_service_card_selected);
                    b.ivPetCheck.setImageResource(R.drawable.bg_radio_selected);
                } else {
                    b.layoutPetCard.setBackgroundResource(R.drawable.bg_service_card_default);
                    b.ivPetCheck.setImageResource(R.drawable.bg_radio_unselected);
                }
            }
        }

        if (isBookingMode) {
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
    }

    public void showEmptyState() {
        binding.layoutPetsContent.setVisibility(View.GONE);
        binding.layoutPetsEmpty.setVisibility(View.VISIBLE);
        binding.layoutStickyBottom.setVisibility(View.GONE);
        binding.tvSubtitleBooking.setVisibility(View.GONE);
        binding.bottomNavigation.setVisibility(isBookingMode ? View.GONE : View.VISIBLE);
    }

    public void showPetsList() {
        binding.layoutPetsContent.setVisibility(View.VISIBLE);
        binding.layoutPetsEmpty.setVisibility(View.GONE);
        if (isBookingMode) {
            binding.layoutStickyBottom.setVisibility(View.VISIBLE);
            binding.tvSubtitleBooking.setVisibility(View.VISIBLE);
            binding.bottomNavigation.setVisibility(View.GONE);
        } else {
            binding.layoutStickyBottom.setVisibility(View.GONE);
            binding.tvSubtitleBooking.setVisibility(View.GONE);
            binding.bottomNavigation.setVisibility(View.VISIBLE);
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
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
            finish();
        });

        binding.navAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.petcareui.ui.appointments.MyAppointmentsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });

        binding.navPets.setOnClickListener(v -> {
            binding.scrollViewContent.smoothScrollTo(0, 0);
        });

        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.petcareui.ui.profile.ProfileActivity.class);
            startActivity(intent);
        });
    }
}
