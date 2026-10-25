package com.example.petcareui.ui.appointments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.model.AppointmentItem;
import com.example.petcareui.databinding.ActivityAppointmentDetailsBinding;
import com.example.petcareui.ui.clinic.ClinicDetailsActivity;
import com.example.petcareui.ui.clinic.ClinicListActivity;
import com.example.petcareui.ui.pets.MyPetsActivity;

/**
 * Màn hình C13: Chi tiết lịch hẹn (Appointment Details).
 * Bám sát nguyên mẫu React C13AppointmentDetails và Design System Spec.
 * Hỗ trợ trọn vẹn 5 phần / 5 trạng thái theo thiết kế:
 * 1. Chờ xác nhận (Pending)
 * 2. Đã xác nhận (Confirmed)
 * 3. Đã hoàn thành (Completed)
 * 4. Hộp thoại xác nhận hủy lịch (Cancel Dialog)
 * 5. Đã hủy (Cancelled kèm thanh Hoàn tác & Đặt lịch khác)
 */
public class AppointmentDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_REF = "extra_ref";
    public static final String EXTRA_APPT = "extra_appt";
    public static final String EXTRA_STATE = "extra_state";

    private ActivityAppointmentDetailsBinding binding;

    private AppointmentItem appt;
    private String currentState = "pending"; // pending, confirmed, completed, cancel-dialog, cancelled
    private String previousStateBeforeDialog = "pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractIntentData();
        setupTopBar();
        populateStaticData();
        setupStateTabs();
        setupInteractiveActions();
        applyState(currentState);
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_APPT)) {
            try {
                appt = (AppointmentItem) getIntent().getSerializableExtra(EXTRA_APPT);
            } catch (Exception ignored) {
            }
        }

        if (getIntent().hasExtra(EXTRA_STATE)) {
            currentState = getIntent().getStringExtra(EXTRA_STATE);
        }

        // Dữ liệu mặc định chuẩn PC-48213 (SELECTED, STAFF_CLINIC, PETS[0], SERVICES[1])
        if (appt == null) {
            String ref = getIntent().getStringExtra(EXTRA_REF);
            if (ref == null || ref.isEmpty()) {
                ref = "PC-48213";
            }
            appt = new AppointmentItem(
                    ref,
                    "Phòng khám Thú y An Nhiên",
                    "Quận 1",
                    "Mochi",
                    R.drawable.img_pet_mochi,
                    "Tiêm phòng",
                    "Thứ 2, 27/10/2026",
                    "10:00",
                    "PENDING",
                    "250.000 ₫",
                    "Gửi cách đây 2 giờ",
                    "Mochi hơi mẩn nhẹ sau mũi tiêm lần trước."
            );
        }

        if (currentState == null || currentState.isEmpty()) {
            if ("CONFIRMED".equalsIgnoreCase(appt.getStatus())) {
                currentState = "confirmed";
            } else if ("COMPLETED".equalsIgnoreCase(appt.getStatus())) {
                currentState = "completed";
            } else if ("CANCELLED".equalsIgnoreCase(appt.getStatus())) {
                currentState = "cancelled";
            } else {
                currentState = "pending";
            }
        }
    }

    private void setupTopBar() {
        binding.tvApptRefTop.setText(appt.getRef());
        binding.tvClinicNameTop.setText(appt.getClinic());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void populateStaticData() {
        // Hàng 1
        binding.tvRequestedTime.setText(appt.getRequested() != null ? appt.getRequested() : "Gửi cách đây 2 giờ");

        // Thẻ phòng khám
        binding.tvClinicName.setText(appt.getClinic());
        binding.tvClinicArea.setText(appt.getClinicArea() + " · TP.HCM");

        // Thẻ Dịch vụ & Chi phí
        binding.tvServiceName.setText(appt.getService());
        binding.tvServicePrice.setText(appt.getPrice());
        binding.tvServiceDuration.setText("45 phút");
        binding.tvPetName.setText(appt.getPet());

        // Thẻ Thú cưng
        binding.ivPetAvatar.setImageResource(appt.getPetImgRes());
        binding.tvCardPetName.setText(appt.getPet());
        if ("Mochi".equalsIgnoreCase(appt.getPet())) {
            binding.tvPetDetails.setText("Pomeranian · 2 tuổi · Cái");
        } else if ("Bông".equalsIgnoreCase(appt.getPet())) {
            binding.tvPetDetails.setText("Mèo Anh lông ngắn · 3 tuổi · Đực");
        } else {
            binding.tvPetDetails.setText("Golden Retriever · 1 tuổi · Đực");
        }

        // Thẻ Ngày, Giờ & Mã hẹn
        binding.tvDate.setText(appt.getDate());
        binding.tvTime.setText(appt.getTime());
        binding.tvRefCode.setText(appt.getRef());

        // Thẻ Timeline Step 1
        binding.tvTimelineStep1Time.setText(appt.getDate() + " · 07:40");

        // Thân hộp thoại hủy
        binding.tvDialogBody.setText(appt.getClinic() + " sẽ được thông báo ngay và khung giờ " + appt.getTime() + " được mở lại. Hành động này không thể hoàn tác.");
    }

    private void setupStateTabs() {
        binding.tabStatePending.setOnClickListener(v -> applyState("pending"));
        binding.tabStateConfirmed.setOnClickListener(v -> applyState("confirmed"));
        binding.tabStateCompleted.setOnClickListener(v -> applyState("completed"));
        binding.tabStateDialog.setOnClickListener(v -> applyState("cancel-dialog"));
        binding.tabStateCancelled.setOnClickListener(v -> applyState("cancelled"));
    }

    /**
     * Cập nhật toàn bộ giao diện theo 5 trạng thái thiết kế
     */
    public void applyState(String state) {
        if (!"cancel-dialog".equals(state)) {
            previousStateBeforeDialog = state;
        }
        currentState = state;

        // Cập nhật chip style cho 5 tab trên cùng
        updateTabChip(binding.tabStatePending, "pending".equals(state));
        updateTabChip(binding.tabStateConfirmed, "confirmed".equals(state));
        updateTabChip(binding.tabStateCompleted, "completed".equals(state));
        updateTabChip(binding.tabStateDialog, "cancel-dialog".equals(state));
        updateTabChip(binding.tabStateCancelled, "cancelled".equals(state));

        // 1. Quản lý hiển thị Dialog hủy
        if ("cancel-dialog".equals(state)) {
            binding.layoutCancelDialogOverlay.setVisibility(View.VISIBLE);
            return;
        } else {
            binding.layoutCancelDialogOverlay.setVisibility(View.GONE);
        }

        // 2. Trạng thái ĐÃ HỦY (Cancelled)
        if ("cancelled".equals(state)) {
            // Huy hiệu
            binding.tvStatusBadge.setText("• Đã hủy");
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_cancelled);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_neutral_ink));

            // Banner Notice
            binding.layoutNoticeBox.setBackgroundResource(R.drawable.bg_notice_danger);
            binding.ivNoticeIcon.setImageResource(R.drawable.ic_alert_circle);
            binding.ivNoticeIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_destructive));
            binding.tvNoticeTitle.setText("Đã hủy lịch hẹn");
            binding.tvNoticeTitle.setTextColor(ContextCompat.getColor(this, R.color.status_destructive_ink));
            binding.tvNoticeBody.setText("Phòng khám đã mở lại khung giờ và các bên đã được thông báo. Bạn có thể đặt lại bất cứ lúc nào.");
            binding.tvNoticeBody.setTextColor(ContextCompat.getColor(this, R.color.status_destructive_ink));

            // Timeline Step 2
            binding.viewTimelineStep2Dot.setBackgroundResource(R.drawable.bg_pin_circle);
            binding.tvTimelineStep2Title.setText("Bạn đã hủy lịch hẹn");
            binding.tvTimelineStep2Time.setText("Hôm nay");

            // Ẩn notice chính sách đổi lịch
            binding.layoutCancelPolicyNotice.setVisibility(View.GONE);

            // Nút Sticky Bottom
            binding.btnCancelBooking.setVisibility(View.GONE);
            binding.btnBackToListOutline.setVisibility(View.GONE);
            binding.layoutCancelledActions.setVisibility(View.VISIBLE);

            // Hiện Floating Undo Snackbar
            binding.layoutUndoSnackbar.setVisibility(View.VISIBLE);
            return;
        }

        // Ẩn Floating Undo Snackbar với các trạng thái còn lại
        binding.layoutUndoSnackbar.setVisibility(View.GONE);
        binding.layoutCancelledActions.setVisibility(View.GONE);

        // 3. Trạng thái ĐÃ HOÀN THÀNH (Completed)
        if ("completed".equals(state)) {
            // Huy hiệu
            binding.tvStatusBadge.setText("• Đã hoàn thành");
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_confirmed);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));

            // Banner Notice
            binding.layoutNoticeBox.setBackgroundResource(R.drawable.bg_notice_success);
            binding.ivNoticeIcon.setImageResource(R.drawable.ic_check_circle);
            binding.ivNoticeIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_success_ink));
            binding.tvNoticeTitle.setText("Đã hoàn thành lượt khám");
            binding.tvNoticeTitle.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));
            binding.tvNoticeBody.setText("Cảm ơn bạn đã đến. Phòng khám đã lưu lần khám vào hồ sơ của bé.");
            binding.tvNoticeBody.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));

            // Timeline Step 2
            binding.viewTimelineStep2Dot.setBackgroundResource(R.drawable.bg_pin_circle);
            binding.tvTimelineStep2Title.setText("Đã hoàn thành lượt khám");
            binding.tvTimelineStep2Time.setText("Hôm nay");

            // Ẩn notice chính sách đổi lịch
            binding.layoutCancelPolicyNotice.setVisibility(View.GONE);

            // Nút Sticky Bottom
            binding.btnCancelBooking.setVisibility(View.GONE);
            binding.btnBackToListOutline.setVisibility(View.VISIBLE);
            return;
        }

        // 4. Trạng thái ĐÃ XÁC NHẬN (Confirmed)
        if ("confirmed".equals(state)) {
            // Huy hiệu
            binding.tvStatusBadge.setText("• Đã xác nhận");
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_confirmed);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));

            // Banner Notice
            binding.layoutNoticeBox.setBackgroundResource(R.drawable.bg_notice_brand);
            binding.ivNoticeIcon.setImageResource(R.drawable.ic_info);
            binding.ivNoticeIcon.setColorFilter(ContextCompat.getColor(this, R.color.brand_primary));
            binding.tvNoticeTitle.setText("Phòng khám đã xác nhận");
            binding.tvNoticeTitle.setTextColor(ContextCompat.getColor(this, R.color.brand_ink));
            binding.tvNoticeBody.setText("Đến sớm 10 phút và mang theo hồ sơ cũ (nếu có).");
            binding.tvNoticeBody.setTextColor(ContextCompat.getColor(this, R.color.brand_ink));

            // Timeline Step 2
            binding.viewTimelineStep2Dot.setBackgroundResource(R.drawable.bg_pin_circle);
            binding.tvTimelineStep2Title.setText("Phòng khám đã xác nhận");
            binding.tvTimelineStep2Time.setText("Hôm nay");

            // Hiện notice chính sách đổi lịch
            binding.layoutCancelPolicyNotice.setVisibility(View.VISIBLE);

            // Nút Sticky Bottom (cho phép hủy)
            binding.btnCancelBooking.setVisibility(View.VISIBLE);
            binding.btnBackToListOutline.setVisibility(View.GONE);
            return;
        }

        // 5. Trạng thái CHỜ XÁC NHẬN (Pending - Mặc định)
        binding.tvStatusBadge.setText("• Chờ xác nhận");
        binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending);
        binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_warning_ink));

        // Banner Notice
        binding.layoutNoticeBox.setBackgroundResource(R.drawable.bg_notice_warning);
        binding.ivNoticeIcon.setImageResource(R.drawable.ic_alert_triangle);
        binding.ivNoticeIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_warning));
        binding.tvNoticeTitle.setText("Đang chờ phòng khám xác nhận");
        binding.tvNoticeTitle.setTextColor(ContextCompat.getColor(this, R.color.status_warning_ink));
        binding.tvNoticeBody.setText("Yêu cầu này chưa được xác nhận. Bạn có thể hủy miễn phí trước khi phòng khám phản hồi.");
        binding.tvNoticeBody.setTextColor(ContextCompat.getColor(this, R.color.status_warning_ink));

        // Timeline Step 2
        binding.viewTimelineStep2Dot.setBackgroundResource(R.drawable.bg_step_dot_inactive);
        binding.tvTimelineStep2Title.setText("Chờ phòng khám phản hồi");
        binding.tvTimelineStep2Time.setText("Đang chờ");

        // Hiện notice chính sách đổi lịch
        binding.layoutCancelPolicyNotice.setVisibility(View.VISIBLE);

        // Nút Sticky Bottom (cho phép hủy)
        binding.btnCancelBooking.setVisibility(View.VISIBLE);
        binding.btnBackToListOutline.setVisibility(View.GONE);
    }

    private void updateTabChip(TextView tab, boolean active) {
        if (active) {
            tab.setBackgroundResource(R.drawable.bg_chip_active);
            tab.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            tab.setBackgroundResource(R.drawable.bg_chip_inactive);
            tab.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }
    }

    private void setupInteractiveActions() {
        // Nút Chỉ đường -> Mở màn hình chi tiết phòng khám C05
        binding.btnDirections.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClinicDetailsActivity.class);
            startActivity(intent);
        });

        // Nút Gọi phòng khám -> Gọi hotline
        binding.btnCallClinic.setOnClickListener(v -> {
            try {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:02838221140"));
                startActivity(dialIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Hotline phòng khám: 028 3822 1140", Toast.LENGTH_SHORT).show();
            }
        });

        // Mở hồ sơ thú cưng
        binding.btnOpenPetProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyPetsActivity.class);
            intent.putExtra(MyPetsActivity.EXTRA_BOOKING_MODE, false);
            startActivity(intent);
        });

        // Nút Hủy lịch hẹn ở Sticky Bar -> Mở hộp thoại hủy
        binding.btnCancelBooking.setOnClickListener(v -> applyState("cancel-dialog"));

        // Trong Hộp thoại hủy: Nút Đồng ý hủy lịch
        binding.btnConfirmCancelBooking.setOnClickListener(v -> applyState("cancelled"));

        // Trong Hộp thoại hủy: Nút Giữ lịch hẹn
        binding.btnKeepBooking.setOnClickListener(v -> applyState(previousStateBeforeDialog));

        // Chạm ra ngoài overlay của hộp thoại -> Đóng hộp thoại
        binding.layoutCancelDialogOverlay.setOnClickListener(v -> applyState(previousStateBeforeDialog));

        // Nút Hoàn tác trên Floating Snackbar
        binding.tvUndoCancel.setOnClickListener(v -> {
            applyState("pending");
            Toast.makeText(this, "Đã hoàn tác hủy lịch hẹn thành công!", Toast.LENGTH_SHORT).show();
        });

        // Nút Đặt lịch khác khi đã hủy -> Chuyển sang C04
        binding.btnBookAnother.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            startActivity(intent);
            finish();
        });

        // Nút Về danh sách lịch hẹn
        binding.btnBackToListGhost.setOnClickListener(v -> finish());
        binding.btnBackToListOutline.setOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        if ("cancel-dialog".equals(currentState)) {
            applyState(previousStateBeforeDialog);
        } else {
            super.onBackPressed();
        }
    }
}
