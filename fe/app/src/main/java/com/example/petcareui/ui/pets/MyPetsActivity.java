package com.example.petcareui.ui.pets;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.DateItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.data.model.TimeSlotItem;
import com.example.petcareui.databinding.ActivityMyPetsBinding;
import com.example.petcareui.databinding.DialogPetDeleteConfirmBinding;
import com.example.petcareui.ui.appointments.MyAppointmentsActivity;
import com.example.petcareui.ui.booking.BookingReviewActivity;
import com.example.petcareui.ui.clinic.ClinicListActivity;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.List;

/**
 * Màn hình C08: Thú cưng của tôi & Chọn thú cưng (Bước 3/4) — Kiến trúc MVVM + RecyclerView.
 */
public class MyPetsActivity extends AppCompatActivity {

    public static final String EXTRA_CLINIC = "extra_clinic";
    public static final String EXTRA_SELECTED_SERVICE = "extra_selected_service";
    public static final String EXTRA_SELECTED_DATE = "extra_selected_date";
    public static final String EXTRA_SELECTED_TIME = "extra_selected_time";
    public static final String EXTRA_BOOKING_MODE = "extra_booking_mode";
    public static final String EXTRA_PRESELECTED_PET = "extra_preselected_pet";
    public static final String EXTRA_EDIT_FROM_REVIEW = "extra_edit_from_review";

    private ActivityMyPetsBinding binding;
    private MyPetsViewModel viewModel;
    private PetListAdapter petAdapter;

    private ClinicItem clinicItem;
    private ServiceItem serviceItem;
    private DateItem dateItem;
    private TimeSlotItem timeSlotItem;
    private PetItem preselectedPet;
    private boolean isBookingMode = true;
    private boolean isEditFromReview = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPetsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MyPetsViewModel.class);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.ds_home_bg));
        extractIntentData();
        UiMotionUtils.applyWindowInsets(
                binding.getRoot(),
                binding.layoutTopBar,
                isBookingMode ? binding.layoutStickyBottom : binding.bottomNavigation
        );

        setupRecyclerView();
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
            if (isEditFromReview) {
                binding.btnContinue.setText("Lưu thay đổi");
            }
        }

        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadMyPets(preselectedPet);
    }

    private void setupRecyclerView() {
        petAdapter = new PetListAdapter(isBookingMode, new PetListAdapter.OnPetActionListener() {
            @Override
            public void onPetSelect(int position, View itemView) {
                UiMotionUtils.animateSelectionPop(itemView);
                viewModel.selectPet(position);
            }

            @Override
            public void onPetOpenDetails(PetItem pet) {
                Intent intent = new Intent(MyPetsActivity.this, PetDetailsActivity.class);
                intent.putExtra(PetDetailsActivity.EXTRA_PET, pet);
                startActivity(intent);
            }

            @Override
            public void onPetEdit(PetItem pet) {
                Intent intent = new Intent(MyPetsActivity.this, AddPetActivity.class);
                intent.putExtra(AddPetActivity.EXTRA_EDIT_PET, pet);
                startActivity(intent);
            }

            @Override
            public void onPetHistory(PetItem pet) {
                Intent intent = new Intent(MyPetsActivity.this, PetDetailsActivity.class);
                intent.putExtra(PetDetailsActivity.EXTRA_PET, pet);
                intent.putExtra(PetDetailsActivity.EXTRA_SCROLL_TO_HISTORY, true);
                startActivity(intent);
            }

            @Override
            public void onPetDeleteRequest(PetItem pet) {
                showDeletePetDialog(pet);
            }
        });

        binding.rvPetsList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPetsList.setAdapter(petAdapter);
    }

    private void observeViewModel() {
        viewModel.getPets().observe(this, petList -> {
            if (petList == null || petList.isEmpty()) {
                showEmptyState();
                petAdapter.submitList(null, -1);
                updateBookingSummary(null);
            } else {
                showPetsList();
                binding.cardAddOtherPet.setVisibility(View.GONE);
                int idx = viewModel.getSelectedIndex().getValue() != null
                        ? viewModel.getSelectedIndex().getValue() : 0;
                petAdapter.submitList(petList, idx);
                updateBookingSummary(viewModel.getSelectedPet());
            }
        });

        viewModel.getSelectedIndex().observe(this, idx -> {
            int safeIdx = idx != null ? idx : -1;
            petAdapter.setSelectedIndex(safeIdx);
            updateBookingSummary(viewModel.getSelectedPet());
        });

        viewModel.getErrorMessage().observe(this, err -> {
            if (err != null && !err.isEmpty()) {
                Toast.makeText(this, err, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
    }

    private void updateBookingSummary(PetItem selected) {
        if (!isBookingMode) return;
        if (selected != null) {
            binding.tvSelectedPetName.setText(selected.getName());
            binding.btnContinue.setEnabled(true);
            binding.btnContinue.setAlpha(1.0f);
        } else {
            binding.tvSelectedPetName.setText("Chưa chọn");
            binding.btnContinue.setEnabled(false);
            binding.btnContinue.setAlpha(0.5f);
        }
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
        if (getIntent().hasExtra(EXTRA_PRESELECTED_PET)) {
            try {
                preselectedPet = (PetItem) getIntent().getSerializableExtra(EXTRA_PRESELECTED_PET);
            } catch (Exception ignored) {
            }
        }
        isBookingMode = getIntent().getBooleanExtra(EXTRA_BOOKING_MODE, true);
        isEditFromReview = getIntent().getBooleanExtra(EXTRA_EDIT_FROM_REVIEW, false);
    }

    private void setupTopBar() {
        binding.btnBack.setOnClickListener(v -> finish());

        View.OnClickListener addPetListener = v ->
                startActivity(new Intent(this, AddPetActivity.class));
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
            viewModel.deletePet(pet.getNumericId(), preselectedPet);
        });

        dialog.show();
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
            PetItem chosen = viewModel.getSelectedPet();
            if (chosen != null) {
                if (isEditFromReview) {
                    Intent result = new Intent();
                    result.putExtra(BookingReviewActivity.EXTRA_SELECTED_PET, chosen);
                    setResult(RESULT_OK, result);
                    finish();
                } else {
                    Intent intent = new Intent(this, BookingReviewActivity.class);
                    intent.putExtra(BookingReviewActivity.EXTRA_CLINIC, clinicItem);
                    intent.putExtra(BookingReviewActivity.EXTRA_SELECTED_SERVICE, serviceItem);
                    intent.putExtra(BookingReviewActivity.EXTRA_SELECTED_DATE, dateItem);
                    intent.putExtra(BookingReviewActivity.EXTRA_SELECTED_TIME, timeSlotItem);
                    intent.putExtra(BookingReviewActivity.EXTRA_SELECTED_PET, chosen);
                    startActivity(intent);
                }
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
