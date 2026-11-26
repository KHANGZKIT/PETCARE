package com.example.petcareui.ui.appointments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.data.model.AppointmentItem;
import com.example.petcareui.databinding.ActivityMyAppointmentsBinding;
import com.example.petcareui.ui.clinic.ClinicListActivity;
import com.example.petcareui.ui.pets.MyPetsActivity;
import com.example.petcareui.utils.UiMotionUtils;

/**
 * Màn hình C12: Lịch hẹn của tôi (My Appointments) — Kiến trúc MVVM + RecyclerView.
 */
public class MyAppointmentsActivity extends AppCompatActivity {

    public static final String EXTRA_STATE = "extra_state";

    private ActivityMyAppointmentsBinding binding;
    private MyAppointmentsViewModel viewModel;
    private AppointmentAdapter primaryAdapter;
    private AppointmentAdapter pastPreviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyAppointmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MyAppointmentsViewModel.class);

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopHeader, binding.layoutBottomNav);

        setupRecyclerViews();
        setupTopBar();
        setupFilterChips();
        setupBottomNav();
        observeViewModel();

        if (savedInstanceState == null && getIntent().hasExtra(EXTRA_STATE)) {
            viewModel.setCurrentTab(getIntent().getStringExtra(EXTRA_STATE));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadAppointments();
    }

    private void setupRecyclerViews() {
        primaryAdapter = new AppointmentAdapter(this::openAppointmentDetail);
        pastPreviewAdapter = new AppointmentAdapter(this::openAppointmentDetail);

        binding.rvApptList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvApptList.setAdapter(primaryAdapter);

        binding.rvPastPreviewList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPastPreviewList.setAdapter(pastPreviewAdapter);
    }

    private void openAppointmentDetail(AppointmentItem item) {
        Intent intent = new Intent(this, AppointmentDetailsActivity.class);
        intent.putExtra(AppointmentDetailsActivity.EXTRA_APPT, item);
        intent.putExtra(AppointmentDetailsActivity.EXTRA_REF, item.getRef());
        startActivity(intent);
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, state -> {
            if (state == null) return;

            binding.tvHeaderSubtitle.setText(state.upcomingCount + " sắp tới · " + state.pastCount + " đã qua");
            binding.chipUpcoming.setText("Sắp tới · " + state.upcomingCount);
            binding.chipPast.setText("Đã qua · " + state.pastCount);

            updateChipStyle(binding.chipUpcoming, "upcoming".equals(state.activeTab));
            updateChipStyle(binding.chipPast, "past".equals(state.activeTab));
            updateChipStyle(binding.chipAll, "all".equals(state.activeTab));

            if (state.isEmpty) {
                binding.tvHeaderSubtitle.setVisibility(View.GONE);
                binding.rvApptList.setVisibility(View.GONE);
                binding.layoutPastSectionInUpcoming.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                primaryAdapter.submitList(null);
                pastPreviewAdapter.submitList(null);
                return;
            }

            binding.tvHeaderSubtitle.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.rvApptList.setVisibility(View.VISIBLE);

            primaryAdapter.submitList(state.primaryItems);

            if ("upcoming".equals(state.activeTab) && !state.pastPreviewItems.isEmpty()) {
                binding.layoutPastSectionInUpcoming.setVisibility(View.VISIBLE);
                pastPreviewAdapter.submitList(state.pastPreviewItems);
            } else {
                binding.layoutPastSectionInUpcoming.setVisibility(View.GONE);
                pastPreviewAdapter.submitList(null);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }

    private void setupTopBar() {
        binding.tvHeaderSubtitle.setText("0 sắp tới · 0 đã qua");

        binding.btnNewBooking.setOnClickListener(v ->
                startActivity(new Intent(this, ClinicListActivity.class)));

        binding.tvSeeAllPastAppts.setOnClickListener(v -> viewModel.setCurrentTab("past"));

        binding.btnEmptyFindClinic.setOnClickListener(v ->
                startActivity(new Intent(this, ClinicListActivity.class)));

        UiMotionUtils.attachSpringTouch(
                binding.btnNewBooking,
                binding.btnEmptyFindClinic,
                binding.chipUpcoming,
                binding.chipPast,
                binding.chipAll
        );
    }

    private void setupFilterChips() {
        binding.chipUpcoming.setText("Sắp tới · 0");
        binding.chipPast.setText("Đã qua · 0");
        binding.chipAll.setText("Tất cả");

        binding.chipUpcoming.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(binding.chipUpcoming);
            viewModel.setCurrentTab("upcoming");
        });
        binding.chipPast.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(binding.chipPast);
            viewModel.setCurrentTab("past");
        });
        binding.chipAll.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(binding.chipAll);
            viewModel.setCurrentTab("all");
        });
    }

    private void updateChipStyle(TextView chip, boolean active) {
        if (active) {
            chip.setBackgroundResource(R.drawable.bg_chip_active);
            chip.setTextColor(ContextCompat.getColor(this, R.color.ds_primary));
            UiMotionUtils.applyFont(chip, R.font.open_sans_semibold);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.ds_text_secondary));
            UiMotionUtils.applyFont(chip, R.font.open_sans_medium);
        }
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

        binding.navAppointments.setOnClickListener(v ->
                binding.scrollViewContent.smoothScrollTo(0, 0));

        binding.navPets.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyPetsActivity.class);
            intent.putExtra(MyPetsActivity.EXTRA_BOOKING_MODE, false);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }
}
