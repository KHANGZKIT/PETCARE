package com.example.petcareui.ui.staff;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.model.StaffAppointmentItem;
import com.example.petcareui.databinding.ActivityStaffAppointmentDetailsBinding;

import java.util.HashMap;
import java.util.Map;

/**
 * Màn hình S03: Chi tiết lịch hẹn (nhân viên) - Staff Appointment Details.
 * Bám sát thiết kế nguyên mẫu React S03StaffAppointmentDetails và DESIGN_SYSTEM_SPEC.md.
 * Hỗ trợ các trạng thái & hộp thoại:
 * - Chờ duyệt (default)
 * - Hộp thoại xác nhận (confirm-dialog)
 * - Hộp thoại từ chối (reject-dialog)
 * - Đã xác nhận (success / confirmed)
 * - Đã từ chối (rejected)
 * - Thất bại (failed)
 */
public class StaffAppointmentDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_REF = "extra_ref";
    public static final String EXTRA_STATE = "extra_state";

    private ActivityStaffAppointmentDetailsBinding binding;
    private final Map<String, StaffAppointmentItem> mockDataMap = new HashMap<>();
    private StaffAppointmentItem currentAppt;

    private String currentState = "default";
    private String selectedRejectReason = "Hết chỗ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffAppointmentDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initMockData();
        extractIntentData();
        bindAppointmentData();
        setupAppBar();
        setupStateToggles();
        setupDialogInteractions();
        setupStickyBarActions();

        applyState(currentState);
    }

    private void initMockData() {
        mockDataMap.put("PC-48213", new StaffAppointmentItem(
                "PC-48213", "Mochi", R.drawable.img_pet_mochi,
                "Pomeranian · 2 tuổi · Cái · 3,4 kg",
                "Nguyễn Lan", "+84 90 123 4567", "Khách hàng từ tháng 3/2026",
                "Tiêm phòng", "20 phút", "Thứ 2, 27/10/2026", "10:00",
                "PENDING", "250.000 ₫", "Gửi cách đây 2 giờ",
                "Mochi hơi mẩn nhẹ sau mũi tiêm lần trước."
        ));

        mockDataMap.put("PC-48230", new StaffAppointmentItem(
                "PC-48230", "Lựu", R.drawable.img_pet_luu,
                "Corgi · 6 tháng · Đực · 7,2 kg",
                "Phạm Hà", "+84 91 884 2310", "Khách hàng từ tháng 6/2026",
                "Khám tổng quát", "30 phút", "Thứ 2, 27/10/2026", "11:30",
                "PENDING", "180.000 ₫", "Gửi cách đây 5 giờ", null
        ));

        mockDataMap.put("PC-47980", new StaffAppointmentItem(
                "PC-47980", "Bông", R.drawable.img_pet_bong,
                "Anh lông ngắn · 4 tuổi · Đực · 5,1 kg",
                "Trần Minh", "+84 93 220 7781", "Khách hàng từ tháng 1/2025",
                "Cạo vôi & chăm sóc răng", "45 phút", "Thứ 2, 27/10/2026", "14:00",
                "CONFIRMED", "600.000 ₫", "Phòng khám đã xác nhận",
                "Nhờ bác sĩ xem lại răng hàm dưới bên trái."
        ));
    }

    private void extractIntentData() {
        String ref = getIntent().getStringExtra(EXTRA_REF);
        if (ref != null && mockDataMap.containsKey(ref)) {
            currentAppt = mockDataMap.get(ref);
        } else {
            currentAppt = mockDataMap.get("PC-48213");
        }

        if (getIntent().hasExtra(EXTRA_STATE)) {
            currentState = getIntent().getStringExtra(EXTRA_STATE);
        }
    }

    private void bindAppointmentData() {
        if (currentAppt == null) return;

        // 1. AppBar
        binding.tvApptRef.setText(currentAppt.getRef());
        binding.tvApptDate.setText(currentAppt.getDate());

        // 2. Status Badge & Request Time
        updateStatusBadge(currentAppt.getStatus());
        binding.tvRequestedTime.setText(currentAppt.getRequested());

        // 3. Customer Info
        binding.tvCustomerName.setText(currentAppt.getCustomer());
        binding.tvCustomerPhone.setText(currentAppt.getCustomerPhone());
        binding.tvCustomerSince.setText(currentAppt.getCustomerSince());

        String[] parts = currentAppt.getCustomer().split(" ");
        if (parts.length >= 2) {
            binding.tvCustomerInitials.setText(parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1));
        } else if (parts.length == 1 && !parts[0].isEmpty()) {
            binding.tvCustomerInitials.setText(parts[0].substring(0, 1));
        }

        binding.btnCallCustomer.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + currentAppt.getCustomerPhone().replaceAll("\\s+", "")));
            startActivity(callIntent);
        });

        // 4. Pet Info
        binding.ivPetPhoto.setImageResource(currentAppt.getPetImgRes());
        binding.tvPetName.setText(currentAppt.getPet());
        binding.tvPetBreed.setText(currentAppt.getBreed());

        // 5. Service & Schedule Info
        binding.tvServiceName.setText(currentAppt.getService());
        binding.tvDuration.setText(currentAppt.getDuration());
        binding.tvDate.setText(currentAppt.getDate());
        binding.tvTime.setText(currentAppt.getTime());
        binding.tvPrice.setText(currentAppt.getPrice());

        // 6. Owner Note
        if (currentAppt.getNote() != null && !currentAppt.getNote().trim().isEmpty()) {
            binding.blockOwnerNote.setVisibility(View.VISIBLE);
            binding.tvOwnerNote.setText("“" + currentAppt.getNote() + "”");
        } else {
            binding.blockOwnerNote.setVisibility(View.GONE);
        }

        // 7. Dialogs Text
        binding.tvConfirmDialogBody.setText(currentAppt.getCustomer() + " sẽ được thông báo: "
                + currentAppt.getPet() + " đã được xác nhận cho "
                + currentAppt.getService() + " ngày " + currentAppt.getDate()
                + " lúc " + currentAppt.getTime() + ".");

        binding.tvNoticeConfirmedBody.setText(currentAppt.getCustomer() + " được thông báo: "
                + currentAppt.getPet() + " có lịch " + currentAppt.getService()
                + " lúc " + currentAppt.getTime() + ".");

        binding.tvNoticeRejectedBody.setText(currentAppt.getCustomer()
                + " đã được thông báo. Khung giờ " + currentAppt.getTime() + " được mở lại.");

        binding.tvFloatingToastMessage.setText("Đã xác nhận · " + currentAppt.getCustomer() + " được thông báo");
    }

    private void updateStatusBadge(String status) {
        if ("CONFIRMED".equals(status)) {
            binding.tvStatusBadge.setText("• Đã xác nhận");
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_confirmed);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));
        } else if ("REJECTED".equals(status)) {
            binding.tvStatusBadge.setText("• Đã từ chối");
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_cancelled);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_destructive_ink));
        } else if ("COMPLETED".equals(status)) {
            binding.tvStatusBadge.setText("• Đã hoàn thành");
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_completed);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.brand_ink));
        } else {
            binding.tvStatusBadge.setText("• Chờ xác nhận");
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_warning_ink));
        }
    }

    private void setupAppBar() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnMoreMenu.setOnClickListener(v -> {
            String[] options = {"Sao chép mã " + currentAppt.getRef(), "Chia sẻ thông tin lịch hẹn", "Gửi tin nhắn SMS"};
            new AlertDialog.Builder(this)
                    .setTitle("Thao tác khác")
                    .setItems(options, (dialog, which) -> {
                        Toast.makeText(this, "Đã chọn: " + options[which], Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
    }

    private void setupStateToggles() {
        binding.chipStatePending.setOnClickListener(v -> applyState("default"));
        binding.chipStateConfirmDialog.setOnClickListener(v -> applyState("confirm-dialog"));
        binding.chipStateRejectDialog.setOnClickListener(v -> applyState("reject-dialog"));
        binding.chipStateConfirmed.setOnClickListener(v -> applyState("success"));
        binding.chipStateRejected.setOnClickListener(v -> applyState("rejected"));
        binding.chipStateFailed.setOnClickListener(v -> applyState("failed"));
    }

    private void applyState(String state) {
        currentState = state;

        // Cập nhật trạng thái hiển thị của các nút chuyển đổi
        updateStateChip(binding.chipStatePending, "default".equals(state));
        updateStateChip(binding.chipStateConfirmDialog, "confirm-dialog".equals(state));
        updateStateChip(binding.chipStateRejectDialog, "reject-dialog".equals(state));
        updateStateChip(binding.chipStateConfirmed, "success".equals(state));
        updateStateChip(binding.chipStateRejected, "rejected".equals(state));
        updateStateChip(binding.chipStateFailed, "failed".equals(state));

        // Ẩn các hộp thoại modal trước
        binding.layoutConfirmDialogOverlay.setVisibility(View.GONE);
        binding.layoutRejectSheetOverlay.setVisibility(View.GONE);
        binding.layoutFloatingToast.setVisibility(View.GONE);

        // Ẩn tất cả thông báo cảnh báo
        binding.noticePending.setVisibility(View.GONE);
        binding.noticeConfirmed.setVisibility(View.GONE);
        binding.noticeRejected.setVisibility(View.GONE);
        binding.noticeFailed.setVisibility(View.GONE);

        // Cấu hình theo từng trạng thái
        if ("confirm-dialog".equals(state)) {
            binding.layoutConfirmDialogOverlay.setVisibility(View.VISIBLE);
            binding.layoutActionsPending.setVisibility(View.VISIBLE);
            binding.layoutActionsConfirmed.setVisibility(View.GONE);
            binding.btnBackToList.setVisibility(View.GONE);
            updateStatusBadge("PENDING");
            resetTimelineToPending();
        } else if ("reject-dialog".equals(state)) {
            binding.layoutRejectSheetOverlay.setVisibility(View.VISIBLE);
            binding.layoutActionsPending.setVisibility(View.VISIBLE);
            binding.layoutActionsConfirmed.setVisibility(View.GONE);
            binding.btnBackToList.setVisibility(View.GONE);
            updateStatusBadge("PENDING");
            resetTimelineToPending();
        } else if ("success".equals(state)) {
            binding.noticeConfirmed.setVisibility(View.VISIBLE);
            binding.layoutFloatingToast.setVisibility(View.VISIBLE);
            binding.layoutActionsPending.setVisibility(View.GONE);
            binding.layoutActionsConfirmed.setVisibility(View.VISIBLE);
            binding.btnBackToList.setVisibility(View.GONE);
            updateStatusBadge("CONFIRMED");

            // Cập nhật Timeline
            binding.dotTimelineStep2.setBackgroundResource(R.drawable.bg_step_dot_active);
            binding.tvTimelineStep2Title.setText("Phòng khám đã xác nhận");
            binding.tvTimelineStep2Time.setText("Vừa xong");
            binding.dotTimelineStep3.setBackgroundResource(R.drawable.bg_step_dot_inactive);
            binding.tvTimelineStep3Title.setText("Ngày khám");
            binding.tvTimelineStep3Time.setText("27/10 · 10:00");
        } else if ("rejected".equals(state)) {
            binding.noticeRejected.setVisibility(View.VISIBLE);
            binding.layoutActionsPending.setVisibility(View.GONE);
            binding.layoutActionsConfirmed.setVisibility(View.GONE);
            binding.btnBackToList.setVisibility(View.VISIBLE);
            updateStatusBadge("REJECTED");

            // Cập nhật Timeline
            binding.dotTimelineStep2.setBackgroundResource(R.drawable.bg_step_dot_inactive);
            binding.tvTimelineStep2Title.setText("Chờ phòng khám xử lý");
            binding.tvTimelineStep2Time.setText("Đã xử lý");
            binding.dotTimelineStep3.setBackgroundResource(R.drawable.bg_danger_circle_soft);
            binding.tvTimelineStep3Title.setText("Phòng khám đã từ chối");
            binding.tvTimelineStep3Time.setText("Vừa xong");
        } else if ("failed".equals(state)) {
            binding.noticeFailed.setVisibility(View.VISIBLE);
            binding.layoutActionsPending.setVisibility(View.VISIBLE);
            binding.layoutActionsConfirmed.setVisibility(View.GONE);
            binding.btnBackToList.setVisibility(View.GONE);
            updateStatusBadge("PENDING");
            resetTimelineToPending();
        } else {
            // Default: Chờ duyệt
            binding.noticePending.setVisibility(View.VISIBLE);
            binding.layoutActionsPending.setVisibility(View.VISIBLE);
            binding.layoutActionsConfirmed.setVisibility(View.GONE);
            binding.btnBackToList.setVisibility(View.GONE);
            updateStatusBadge("PENDING");
            resetTimelineToPending();
        }
    }

    private void resetTimelineToPending() {
        binding.dotTimelineStep2.setBackgroundResource(R.drawable.bg_step_dot_inactive);
        binding.tvTimelineStep2Title.setText("Chờ phòng khám xử lý");
        binding.tvTimelineStep2Time.setText("Đang chờ");
        binding.dotTimelineStep3.setBackgroundResource(R.drawable.bg_step_dot_inactive);
        binding.tvTimelineStep3Title.setText("Ngày khám");
        binding.tvTimelineStep3Time.setText("27/10 · 10:00");
    }

    private void updateStateChip(TextView chip, boolean active) {
        if (active) {
            chip.setBackgroundResource(R.drawable.bg_chip_active);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }
    }

    private void setupStickyBarActions() {
        // Nút Xác nhận -> Mở Hộp thoại xác nhận
        binding.btnConfirm.setOnClickListener(v -> applyState("confirm-dialog"));

        // Nút Từ chối -> Mở Hộp thoại từ chối
        binding.btnReject.setOnClickListener(v -> applyState("reject-dialog"));

        // Nút Hoàn thành lịch hẹn (sau khi đã xác nhận)
        binding.btnComplete.setOnClickListener(v -> {
            Toast.makeText(this, "Đã hoàn thành lượt khám cho " + currentAppt.getPet(), Toast.LENGTH_SHORT).show();
            applyState("default");
            finish();
        });

        // Nút Không đến hẹn
        binding.btnNoShow.setOnClickListener(v -> {
            Toast.makeText(this, "Đã ghi nhận khách không đến hẹn", Toast.LENGTH_SHORT).show();
            applyState("default");
            finish();
        });

        // Nút Về danh sách lịch hẹn
        binding.btnBackToList.setOnClickListener(v -> finish());
    }

    private void setupDialogInteractions() {
        // 1. Confirm Modal Dialog
        binding.btnDialogConfirmAction.setOnClickListener(v -> {
            applyState("success");
        });

        binding.btnDialogConfirmDismiss.setOnClickListener(v -> {
            applyState("default");
        });

        binding.layoutConfirmDialogOverlay.setOnClickListener(v -> {
            applyState("default");
        });

        // 2. Reject Bottom Sheet
        binding.chipReason1.setOnClickListener(v -> selectRejectReason(binding.chipReason1, "Hết chỗ"));
        binding.chipReason2.setOnClickListener(v -> selectRejectReason(binding.chipReason2, "Khung giờ không phù hợp"));
        binding.chipReason3.setOnClickListener(v -> selectRejectReason(binding.chipReason3, "Chưa có dịch vụ này"));
        binding.chipReason4.setOnClickListener(v -> selectRejectReason(binding.chipReason4, "Phòng khám nghỉ"));

        binding.btnSheetRejectAction.setOnClickListener(v -> {
            Toast.makeText(this, "Đã từ chối với lý do: " + selectedRejectReason, Toast.LENGTH_SHORT).show();
            applyState("rejected");
        });

        binding.btnSheetRejectDismiss.setOnClickListener(v -> {
            applyState("default");
        });

        binding.layoutRejectSheetOverlay.setOnClickListener(v -> {
            applyState("default");
        });
    }

    private void selectRejectReason(TextView selectedChip, String reason) {
        selectedRejectReason = reason;

        TextView[] allChips = {binding.chipReason1, binding.chipReason2, binding.chipReason3, binding.chipReason4};
        for (TextView chip : allChips) {
            if (chip == selectedChip) {
                chip.setBackgroundResource(R.drawable.bg_chip_active);
                chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_inactive);
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            }
        }
    }
}
