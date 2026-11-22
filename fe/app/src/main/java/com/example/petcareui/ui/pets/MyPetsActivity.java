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
import com.example.petcareui.data.dto.response.PetResponse;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.DateItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.data.model.TimeSlotItem;
import com.example.petcareui.databinding.ActivityMyPetsBinding;
import com.example.petcareui.databinding.DialogPetDeleteConfirmBinding;
import com.example.petcareui.databinding.ItemPetCardBinding;
import com.example.petcareui.ui.appointments.MyAppointmentsActivity;
import com.example.petcareui.ui.booking.BookingReviewActivity;
import com.example.petcareui.ui.clinic.ClinicListActivity;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private final List<ItemPetCardBinding> petBindings = new ArrayList<>();
    private int selectedPetIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPetsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.ds_home_bg));
        extractIntentData();
        UiMotionUtils.applyWindowInsets(
                binding.getRoot(),
                binding.layoutTopBar,
                isBookingMode ? binding.layoutStickyBottom : binding.bottomNavigation
        );

        setupTopBar();
        setupEmptyStateActions();
        setupStickyBottomBar();
        setupBottomNav();

        if (!isBookingMode) {
            binding.btnBack.setVisibility(View.GONE);
            binding.tvPetsScreenTitle.setText("Thú cưng của tôi");
            binding.layoutStickyBottom.setVisibility(View.GONE);
            binding.tvSubtitleBooking.setVisibility(View.GONE);
            binding.bottomNavigation.setVisibility(View.VISIBLE);
        } else {
            binding.btnBack.setVisibility(View.VISIBLE);
            binding.tvPetsScreenTitle.setText("Chọn thú cưng");
            binding.tvSubtitleBooking.setText("Bước 3/4");
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
    }

    private void loadPetsFromApi() {
        ApiClient.getPetApi().getMyPets().enqueue(new Callback<List<PetResponse>>() {
            @Override
            public void onResponse(Call<List<PetResponse>> call, Response<List<PetResponse>> response) {
                if (isFinishing() || isDestroyed()) return;

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
                }
            }

            @Override
            public void onFailure(Call<List<PetResponse>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (pets.isEmpty()) {
                    showEmptyState();
                }
            }
        });
    }

    private void renderPets() {
        petBindings.clear();
        binding.containerPetsList.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        binding.cardAddOtherPet.setVisibility(pets.isEmpty() ? View.VISIBLE : View.GONE);

        for (int i = 0; i < pets.size(); i++) {
            final int index = i;
            PetItem pet = pets.get(i);
            ItemPetCardBinding itemBinding = ItemPetCardBinding.inflate(inflater, binding.containerPetsList, false);

            int avatarRes = pet.getImageResId();
            itemBinding.ivPetAvatar.setImageResource(avatarRes);
            if (avatarRes == R.drawable.img_pet_bong) {
                itemBinding.ivPetAvatar.setPadding(0, 0, 0, 0);
                itemBinding.ivPetAvatar.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            } else {
                int pad = Math.round(4 * getResources().getDisplayMetrics().density);
                itemBinding.ivPetAvatar.setPadding(pad, pad, pad, pad);
                itemBinding.ivPetAvatar.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            }

            itemBinding.tvPetName.setText(pet.getName());

            String realSpecies = pet.getRawSpecies();
            itemBinding.tvPetSpecies.setText(realSpecies != null && !realSpecies.isEmpty() ? realSpecies : "Thú cưng");

            String age = pet.getAge() != null ? pet.getAge().trim() : "";
            String sex = pet.getSex() != null ? pet.getSex().replace("♂", "").replace("♀", "").trim() : "";
            if (!age.isEmpty() && !sex.isEmpty()) {
                itemBinding.tvPetHealthBadge.setText(age + " · " + sex);
            } else if (!age.isEmpty()) {
                itemBinding.tvPetHealthBadge.setText(age);
            } else {
                itemBinding.tvPetHealthBadge.setText(sex);
            }

            UiMotionUtils.attachSpringTouch(itemBinding.getRoot(), itemBinding.btnEditPet, itemBinding.btnHistoryPet);

            if (isBookingMode) {
                itemBinding.layoutPetActions.setVisibility(View.GONE);
                itemBinding.ivPetCheck.setVisibility(View.VISIBLE);
                itemBinding.getRoot().setOnClickListener(v -> selectPet(index));
            } else {
                itemBinding.layoutPetActions.setVisibility(View.VISIBLE);
                itemBinding.ivPetCheck.setVisibility(View.GONE);

                itemBinding.btnEditPet.setOnClickListener(v -> {
                    Intent intent = new Intent(this, AddPetActivity.class);
                    intent.putExtra(AddPetActivity.EXTRA_EDIT_PET, pet);
                    startActivity(intent);
                });

                itemBinding.btnHistoryPet.setOnClickListener(v -> {
                    Intent intent = new Intent(this, PetDetailsActivity.class);
                    intent.putExtra(PetDetailsActivity.EXTRA_PET, pet);
                    intent.putExtra(PetDetailsActivity.EXTRA_SCROLL_TO_HISTORY, true);
                    startActivity(intent);
                });

                itemBinding.getRoot().setOnClickListener(v -> {
                    Intent intent = new Intent(this, PetDetailsActivity.class);
                    intent.putExtra(PetDetailsActivity.EXTRA_PET, pet);
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
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful()) {
                            loadPetsFromApi();
                        } else {
                            Toast.makeText(MyPetsActivity.this, "Không thể xóa bé lúc này", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(MyPetsActivity.this, "Lỗi kết nối khi xóa thú cưng", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    private void updatePetSelectionUI() {
        for (int i = 0; i < petBindings.size(); i++) {
            ItemPetCardBinding b = petBindings.get(i);
            if (!isBookingMode) {
                b.layoutPetCard.setBackgroundResource(R.drawable.bg_home_card_borderless);
                b.layoutPetActions.setVisibility(View.VISIBLE);
                b.ivPetCheck.setVisibility(View.GONE);
            } else {
                b.layoutPetActions.setVisibility(View.GONE);
                b.ivPetCheck.setVisibility(View.VISIBLE);
                boolean isSelected = (i == selectedPetIndex);
                if (isSelected) {
                    b.layoutPetCard.setBackgroundResource(R.drawable.bg_pet_card_selected_minimal);
                    b.ivPetCheck.setImageResource(R.drawable.bg_radio_selected);
                } else {
                    b.layoutPetCard.setBackgroundResource(R.drawable.bg_home_card_borderless);
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
                Intent intent = new Intent(this, BookingReviewActivity.class);
                intent.putExtra(BookingReviewActivity.EXTRA_CLINIC, clinicItem);
                intent.putExtra(BookingReviewActivity.EXTRA_SELECTED_SERVICE, serviceItem);
                intent.putExtra(BookingReviewActivity.EXTRA_SELECTED_DATE, dateItem);
                intent.putExtra(BookingReviewActivity.EXTRA_SELECTED_TIME, timeSlotItem);
                intent.putExtra(BookingReviewActivity.EXTRA_SELECTED_PET, chosen);
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
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navClinics.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            intent.putExtra(ClinicListActivity.EXTRA_TAB_MODE, true);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyAppointmentsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navPets.setOnClickListener(v ->
                binding.scrollViewContent.smoothScrollTo(0, 0));
    }
}
