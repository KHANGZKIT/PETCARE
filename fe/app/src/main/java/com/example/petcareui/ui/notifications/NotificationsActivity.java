package com.example.petcareui.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.data.model.NotificationItem;
import com.example.petcareui.databinding.ActivityNotificationsBinding;
import com.example.petcareui.databinding.ItemNotificationCardBinding;
import com.example.petcareui.ui.appointments.AppointmentDetailsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình C14: Thông báo (Notifications).
 * Bám sát nguyên mẫu React C14Notifications và Design System Spec.
 * Hỗ trợ nhóm theo ngày ("Hôm nay", "Trước đó"), chỉ báo chưa đọc,
 * thao tác "Đọc tất cả" và chuyển đổi trạng thái Rỗng (Empty State).
 */
public class NotificationsActivity extends AppCompatActivity {

    public static final String EXTRA_STATE = "extra_state"; // "default", "empty"

    private ActivityNotificationsBinding binding;
    private final List<NotificationItem> notifications = new ArrayList<>();
    private String currentState = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractIntentData();
        initMockData();
        setupTopBar();
        setupStateToggles();
        applyState(currentState);
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_STATE)) {
            currentState = getIntent().getStringExtra(EXTRA_STATE);
        }
    }

    private void initMockData() {
        // Dữ liệu chuẩn theo data.js (NOTIFICATIONS)
        notifications.clear();
        notifications.add(new NotificationItem(
                "n1", "confirmed", "success",
                "Lịch hẹn đã được xác nhận",
                "Bệnh viện Thú y Gia Định đã xác nhận lịch cạo vôi răng cho Bông vào Thứ 4, 29/10 lúc 14:00.",
                "Hôm nay · 08:12",
                true
        ));

        notifications.add(new NotificationItem(
                "n2", "pending", "warning",
                "Đang chờ phòng khám xác nhận",
                "Phòng khám An Nhiên chưa xác nhận lịch tiêm phòng của Mochi vào Thứ 2, 27/10 lúc 10:00.",
                "Hôm nay · 07:40",
                true
        ));

        notifications.add(new NotificationItem(
                "n3", "reminder", "info",
                "Nhắc lịch khám",
                "Lịch tiêm phòng của Mochi còn 2 ngày nữa. Bạn nhớ đến sớm 10 phút nhé.",
                "Hôm qua · 18:05",
                false
        ));

        notifications.add(new NotificationItem(
                "n4", "cancelled", "neutral",
                "Lịch hẹn đã bị hủy",
                "Buổi tư vấn da & dị ứng tại Bệnh viện Thú y Gia Định đã được hủy.",
                "Thứ 3, 09/09 · 09:20",
                false
        ));

        notifications.add(new NotificationItem(
                "n5", "rejected", "danger",
                "Yêu cầu bị từ chối",
                "Spa & Phòng khám Mimi chưa nhận được lịch tiêm phòng của Mochi vào 18/08. Bạn hãy chọn giờ khác nhé.",
                "Thứ 2, 18/08 · 11:02",
                false
        ));
    }

    private void setupTopBar() {
        binding.btnBack.setOnClickListener(v -> finish());

        // Thao tác Đọc tất cả
        binding.tvMarkAllRead.setOnClickListener(v -> {
            boolean hadUnread = false;
            for (NotificationItem item : notifications) {
                if (item.isUnread()) {
                    item.setUnread(false);
                    hadUnread = true;
                }
            }
            if (hadUnread) {
                renderNotificationList();
                Toast.makeText(this, "Đã đánh dấu đọc tất cả thông báo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tất cả thông báo đã được đọc", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút Về trang chủ trong Trạng thái rỗng
        binding.btnEmptyBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void setupStateToggles() {
        binding.chipStateDefault.setOnClickListener(v -> applyState("default"));
        binding.chipStateEmpty.setOnClickListener(v -> applyState("empty"));
    }

    private void applyState(String state) {
        currentState = state;

        boolean isDefault = "default".equals(state);
        updateToggleChip(binding.chipStateDefault, isDefault);
        updateToggleChip(binding.chipStateEmpty, !isDefault);

        if (!isDefault) {
            // Trạng thái Rỗng
            binding.tvSubtitle.setVisibility(View.GONE);
            binding.layoutNotificationsContent.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            // Trạng thái Mặc định
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutNotificationsContent.setVisibility(View.VISIBLE);
            renderNotificationList();
        }
    }

    private void updateToggleChip(TextView chip, boolean active) {
        if (active) {
            chip.setBackgroundResource(R.drawable.bg_chip_active);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }
    }

    private void renderNotificationList() {
        binding.layoutGroupToday.removeAllViews();
        binding.layoutGroupEarlier.removeAllViews();

        int unreadCount = 0;

        for (NotificationItem item : notifications) {
            if (item.isUnread()) {
                unreadCount++;
            }

            View cardView = createNotificationCardView(item);

            if (item.getTime() != null && item.getTime().startsWith("Hôm nay")) {
                binding.layoutGroupToday.addView(cardView);
            } else {
                binding.layoutGroupEarlier.addView(cardView);
            }
        }

        // Cập nhật phụ đề số lượng chưa đọc
        if (unreadCount > 0) {
            binding.tvSubtitle.setVisibility(View.VISIBLE);
            binding.tvSubtitle.setText(unreadCount + " thông báo chưa đọc");
        } else {
            binding.tvSubtitle.setVisibility(View.GONE);
        }
    }

    private View createNotificationCardView(NotificationItem item) {
        ItemNotificationCardBinding cardBinding = ItemNotificationCardBinding.inflate(getLayoutInflater());

        cardBinding.tvNotifTitle.setText(item.getTitle());
        cardBinding.tvNotifBody.setText(item.getBody());
        cardBinding.tvNotifTime.setText(item.getTime());

        // Viền thẻ & chấm chưa đọc
        if (item.isUnread()) {
            cardBinding.layoutCardContainer.setBackgroundResource(R.drawable.bg_notification_card_unread);
            cardBinding.viewUnreadDot.setVisibility(View.VISIBLE);
        } else {
            cardBinding.layoutCardContainer.setBackgroundResource(R.drawable.bg_card_clinic);
            cardBinding.viewUnreadDot.setVisibility(View.GONE);
        }

        // Icon và màu sắc phù hợp từng loại thông báo
        applyNotificationTypeStyle(cardBinding, item.getType());

        // Chạm vào thẻ -> Đánh dấu đã đọc và mở chi tiết lịch hẹn C13 tương ứng
        cardBinding.getRoot().setOnClickListener(v -> {
            if (item.isUnread()) {
                item.setUnread(false);
                renderNotificationList();
            }

            // Chuyển sang C13 Chi tiết lịch hẹn
            Intent intent = new Intent(this, AppointmentDetailsActivity.class);
            if ("confirmed".equals(item.getType())) {
                intent.putExtra(AppointmentDetailsActivity.EXTRA_REF, "PC-47980");
                intent.putExtra(AppointmentDetailsActivity.EXTRA_STATE, "confirmed");
            } else if ("pending".equals(item.getType())) {
                intent.putExtra(AppointmentDetailsActivity.EXTRA_REF, "PC-48213");
                intent.putExtra(AppointmentDetailsActivity.EXTRA_STATE, "pending");
            } else if ("cancelled".equals(item.getType())) {
                intent.putExtra(AppointmentDetailsActivity.EXTRA_REF, "PC-45877");
                intent.putExtra(AppointmentDetailsActivity.EXTRA_STATE, "cancelled");
            } else {
                intent.putExtra(AppointmentDetailsActivity.EXTRA_REF, "PC-48213");
            }
            startActivity(intent);
        });

        return cardBinding.getRoot();
    }

    private void applyNotificationTypeStyle(ItemNotificationCardBinding cardBinding, String type) {
        if ("confirmed".equalsIgnoreCase(type)) {
            cardBinding.layoutIconBox.setBackgroundResource(R.drawable.bg_badge_confirmed);
            cardBinding.ivNotifIcon.setImageResource(R.drawable.ic_calendar_days);
            cardBinding.ivNotifIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_success_ink));
        } else if ("pending".equalsIgnoreCase(type)) {
            cardBinding.layoutIconBox.setBackgroundResource(R.drawable.bg_notice_warning);
            cardBinding.ivNotifIcon.setImageResource(R.drawable.ic_clock);
            cardBinding.ivNotifIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_warning_ink));
        } else if ("reminder".equalsIgnoreCase(type)) {
            cardBinding.layoutIconBox.setBackgroundResource(R.drawable.bg_badge_no_show);
            cardBinding.ivNotifIcon.setImageResource(R.drawable.ic_bell_off);
            cardBinding.ivNotifIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_info_ink));
        } else if ("cancelled".equalsIgnoreCase(type)) {
            cardBinding.layoutIconBox.setBackgroundResource(R.drawable.bg_pill_ref_code);
            cardBinding.ivNotifIcon.setImageResource(R.drawable.ic_calendar_x);
            cardBinding.ivNotifIcon.setColorFilter(ContextCompat.getColor(this, R.color.text_muted));
        } else if ("rejected".equalsIgnoreCase(type)) {
            cardBinding.layoutIconBox.setBackgroundResource(R.drawable.bg_notice_danger);
            cardBinding.ivNotifIcon.setImageResource(R.drawable.ic_x_circle);
            cardBinding.ivNotifIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_destructive_ink));
        }
    }
}
