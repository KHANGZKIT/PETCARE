package com.example.petcareui.ui.clinic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.databinding.ActivityClinicListBinding;
import com.example.petcareui.databinding.ItemClinicRowBinding;

import java.util.ArrayList;
import java.util.List;

public class ClinicListActivity extends AppCompatActivity {

    private ActivityClinicListBinding binding;
    private final List<ClinicItem> allClinics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClinicListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        setupListeners();
        showResults(allClinics);
    }

    private void initData() {
        allClinics.clear();
        allClinics.add(new ClinicItem(
                "cl-01",
                "Phòng khám Thú y An Nhiên",
                "42 Nguyễn Huệ, Quận 1, TP.HCM",
                4.8,
                214,
                "1,2 km",
                true,
                "08:00 – 20:00"
        ));
        allClinics.add(new ClinicItem(
                "cl-02",
                "Bệnh viện Thú y Gia Định",
                "128 Trần Não, Quận 2",
                4.6,
                158,
                "3,4 km",
                true,
                "07:30 – 21:00"
        ));
        allClinics.add(new ClinicItem(
                "cl-03",
                "Spa & Phòng khám Thú cưng Mimi",
                "9 Nguyễn Thị Thập, Quận 7",
                4.9,
                96,
                "5,1 km",
                false,
                "09:00 – 18:00"
        ));
        allClinics.add(new ClinicItem(
                "cl-04",
                "Phòng khám Thú cưng Bình Thạnh",
                "77 Điện Biên Phủ, Bình Thạnh",
                4.4,
                132,
                "6,8 km",
                true,
                "08:00 – 19:00"
        ));
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
            showLoading();
            new Handler(Looper.getMainLooper()).postDelayed(() -> showResults(allClinics), 800);
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
        TextView[] chips = new TextView[]{
                binding.chipAll,
                binding.chipOpen,
                binding.tvChipNearby,
                binding.chipEmergency
        };

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
