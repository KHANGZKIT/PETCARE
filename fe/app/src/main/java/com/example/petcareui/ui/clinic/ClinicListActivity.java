package com.example.petcareui.ui.clinic;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.ClinicResponse;
import com.example.petcareui.databinding.ActivityClinicListBinding;
import com.example.petcareui.databinding.ItemClinicRowBinding;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClinicListActivity extends AppCompatActivity {

    private ActivityClinicListBinding binding;
    private final List<ClinicItem> allClinics = new ArrayList<>();

    private static final int[] CLINIC_THUMBNAILS = new int[]{
            R.drawable.img_clinic_cover,
            R.drawable.ic_clinic_illustration_hospital,
            R.drawable.ic_clinic_illustration_spa
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClinicListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopBar, binding.layoutResultsBody);

        setupListeners();
        loadClinicsFromApi();
    }

    private void loadClinicsFromApi() {
        showLoading();
        ApiClient.getClinicApi().getAllClinics(null).enqueue(new Callback<List<ClinicResponse>>() {
            @Override
            public void onResponse(Call<List<ClinicResponse>> call, Response<List<ClinicResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allClinics.clear();
                    for (ClinicResponse res : response.body()) {
                        allClinics.add(res.toClinicItem());
                    }
                    if (allClinics.isEmpty()) {
                        showEmpty();
                    } else {
                        showResults(allClinics);
                    }
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<List<ClinicResponse>> call, Throwable t) {
                showError();
            }
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Mở bộ lọc phòng khám nâng cao", Toast.LENGTH_SHORT).show();
        });

        binding.tvSortOption.setOnClickListener(v -> {
            Toast.makeText(this, "Chọn cách sắp xếp (Gần nhất / Đánh giá cao)", Toast.LENGTH_SHORT).show();
        });

        binding.btnLoadMore.setOnClickListener(v -> {
            Toast.makeText(this, "Đang tải thêm phòng khám...", Toast.LENGTH_SHORT).show();
        });

        binding.btnRetry.setOnClickListener(v -> {
            loadClinicsFromApi();
        });

        binding.btnClearFilter.setOnClickListener(v -> {
            resetFilterChips();
            showResults(allClinics);
        });

        binding.tvChangeLocation.setOnClickListener(v -> {
            Toast.makeText(this, "Mở bản đồ chọn khu vực khác", Toast.LENGTH_SHORT).show();
        });

        UiMotionUtils.attachSpringTouch(
                binding.btnBack,
                binding.btnFilter,
                binding.btnLoadMore,
                binding.btnClearFilter,
                binding.btnRetry,
                binding.chipAll,
                binding.chipOpen,
                binding.chipNearby,
                binding.chipEmergency
        );

        setupFilterChips();
    }

    private void setupFilterChips() {
        binding.chipAll.setOnClickListener(v -> {
            setActiveChip(binding.chipAll);
            showResults(allClinics);
        });

        binding.chipOpen.setOnClickListener(v -> {
            setActiveChip(binding.chipOpen);
            List<ClinicItem> openList = new ArrayList<>();
            for (ClinicItem c : allClinics) {
                if (c.isOpen()) openList.add(c);
            }
            showResults(openList);
        });

        binding.chipNearby.setOnClickListener(v -> {
            setActiveChip(binding.chipNearby);
            showResults(allClinics);
        });

        binding.chipEmergency.setOnClickListener(v -> {
            setActiveChip(binding.chipEmergency);
            showEmpty();
        });
    }

    private void setActiveChip(View activeView) {
        UiMotionUtils.animateSelectionPop(activeView);
        View[] chipContainers = new View[]{
                binding.chipAll,
                binding.chipOpen,
                binding.chipNearby,
                binding.chipEmergency
        };

        for (View c : chipContainers) {
            boolean isActive = (c == activeView);
            c.setBackgroundResource(isActive ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);

            if (c instanceof TextView) {
                ((TextView) c).setTextColor(ContextCompat.getColor(this, isActive ? R.color.white : R.color.text_muted));
            } else if (c == binding.chipNearby) {
                binding.tvChipNearby.setTextColor(ContextCompat.getColor(this, isActive ? R.color.white : R.color.text_muted));
            }
        }
    }

    private void resetFilterChips() {
        setActiveChip(binding.chipAll);
    }

    private void showResults(List<ClinicItem> list) {
        binding.layoutResultsState.setVisibility(View.VISIBLE);
        binding.layoutLoadingState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);

        binding.tvClinicCount.setText(list.size() + " phòng khám");

        binding.containerClinicsList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < list.size(); i++) {
            ClinicItem clinic = list.get(i);
            ItemClinicRowBinding rowBinding = ItemClinicRowBinding.inflate(inflater, binding.containerClinicsList, false);

            // Gán ảnh đại diện phòng khám đa dạng (Ảnh thật + Minh họa bệnh viện/spa)
            int thumbRes = CLINIC_THUMBNAILS[i % CLINIC_THUMBNAILS.length];
            rowBinding.ivClinicImage.setImageResource(thumbRes);

            rowBinding.tvClinicName.setText(clinic.getName());
            rowBinding.tvClinicAddress.setText(clinic.getAddress());
            rowBinding.tvClinicRating.setText(String.valueOf(clinic.getRating()));
            rowBinding.tvClinicMeta.setText(" · " + clinic.getReviews() + " đánh giá · " + clinic.getDistance());

            if (clinic.isOpen()) {
                rowBinding.tvClinicStatusBadge.setText("● ĐANG MỞ CỬA");
                rowBinding.tvClinicStatusBadge.setBackgroundResource(R.drawable.bg_badge_open);
                rowBinding.tvClinicStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));
            } else {
                rowBinding.tvClinicStatusBadge.setText("ĐÃ ĐÓNG CỬA");
                rowBinding.tvClinicStatusBadge.setBackgroundResource(R.drawable.bg_badge_closed);
                rowBinding.tvClinicStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            }

            rowBinding.tvClinicHours.setText(clinic.getHours());

            UiMotionUtils.attachSpringTouch(rowBinding.getRoot());

            rowBinding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, ClinicDetailsActivity.class);
                intent.putExtra(ClinicDetailsActivity.EXTRA_CLINIC, clinic);
                startActivity(intent);
            });

            binding.containerClinicsList.addView(rowBinding.getRoot());
        }

        // Hiệu ứng đổ danh sách tuần tự (Staggered Animation)
        UiMotionUtils.animateStaggeredChildren(binding.containerClinicsList, 30);
    }

    public void showLoading() {
        binding.layoutResultsState.setVisibility(View.GONE);
        binding.layoutLoadingState.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);
    }

    public void showEmpty() {
        binding.layoutResultsState.setVisibility(View.GONE);
        binding.layoutLoadingState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
        binding.layoutErrorState.setVisibility(View.GONE);
    }

    public void showError() {
        binding.layoutResultsState.setVisibility(View.GONE);
        binding.layoutLoadingState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.VISIBLE);
    }
}
