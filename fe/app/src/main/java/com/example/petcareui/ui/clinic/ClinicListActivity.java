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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClinicListActivity extends AppCompatActivity {

    private ActivityClinicListBinding binding;
    private final List<ClinicItem> allClinics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClinicListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        // Nút quay lại trên App Bar
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút lọc nâng cao
        binding.btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Mở bộ lọc phòng khám nâng cao", Toast.LENGTH_SHORT).show();
        });

        // Sắp xếp
        binding.tvSortOption.setOnClickListener(v -> {
            Toast.makeText(this, "Chọn cách sắp xếp (Gần nhất / Đánh giá cao)", Toast.LENGTH_SHORT).show();
        });

        // Nút Xem thêm
        binding.btnLoadMore.setOnClickListener(v -> {
            Toast.makeText(this, "Đang tải thêm phòng khám...", Toast.LENGTH_SHORT).show();
        });

        // Thử lại trong Error State
        binding.btnRetry.setOnClickListener(v -> {
            loadClinicsFromApi();
        });

        // Bỏ bộ lọc trong Empty State
        binding.btnClearFilter.setOnClickListener(v -> {
            resetFilterChips();
            showResults(allClinics);
        });

        binding.tvChangeLocation.setOnClickListener(v -> {
            Toast.makeText(this, "Mở bản đồ chọn khu vực khác", Toast.LENGTH_SHORT).show();
        });

        // Xử lý các Chip bộ lọc
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
            // Giả lập trạng thái Không tìm thấy kết quả (Empty State) theo thiết kế
            showEmpty();
        });
    }

    private void setActiveChip(View activeView) {
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

    /**
     * Trạng thái 1: Có kết quả danh sách phòng khám (Results)
     */
    private void showResults(List<ClinicItem> list) {
        binding.layoutResultsState.setVisibility(View.VISIBLE);
        binding.layoutLoadingState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);

        binding.tvClinicCount.setText(list.size() + " phòng khám");

        binding.containerClinicsList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (ClinicItem clinic : list) {
            ItemClinicRowBinding rowBinding = ItemClinicRowBinding.inflate(inflater, binding.containerClinicsList, false);

            rowBinding.tvClinicName.setText(clinic.getName());
            rowBinding.tvClinicAddress.setText(clinic.getAddress());
            rowBinding.tvClinicRating.setText(String.valueOf(clinic.getRating()));
            rowBinding.tvClinicMeta.setText(" · " + clinic.getReviews() + " đánh giá · " + clinic.getDistance());

            // Badge Trạng thái mở cửa
            if (clinic.isOpen()) {
                rowBinding.tvClinicStatusBadge.setText("ĐANG MỞ CỬA");
                rowBinding.tvClinicStatusBadge.setBackgroundResource(R.drawable.bg_badge_open);
                rowBinding.tvClinicStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));
            } else {
                rowBinding.tvClinicStatusBadge.setText("ĐÃ ĐÓNG CỬA");
                rowBinding.tvClinicStatusBadge.setBackgroundResource(R.drawable.bg_badge_closed);
                rowBinding.tvClinicStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            }

            rowBinding.tvClinicHours.setText(clinic.getHours());

            // Click vào phòng khám -> Chuyển sang C05 Chi tiết phòng khám
            rowBinding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, ClinicDetailsActivity.class);
                intent.putExtra(ClinicDetailsActivity.EXTRA_CLINIC, clinic);
                startActivity(intent);
            });

            binding.containerClinicsList.addView(rowBinding.getRoot());
        }
    }

    /**
     * Trạng thái 2: Đang tải dữ liệu (Loading Skeleton)
     */
    public void showLoading() {
        binding.layoutResultsState.setVisibility(View.GONE);
        binding.layoutLoadingState.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);
    }

    /**
     * Trạng thái 3: Không tìm thấy kết quả (Empty State)
     */
    public void showEmpty() {
        binding.layoutResultsState.setVisibility(View.GONE);
        binding.layoutLoadingState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
        binding.layoutErrorState.setVisibility(View.GONE);
    }

    /**
     * Trạng thái 4: Lỗi kết nối / Lỗi tải danh sách (Error State)
     */
    public void showError() {
        binding.layoutResultsState.setVisibility(View.GONE);
        binding.layoutLoadingState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.VISIBLE);
    }
}
