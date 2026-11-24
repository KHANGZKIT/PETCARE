package com.example.petcareui.ui.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.dto.response.AppointmentResponse;
import com.example.petcareui.data.model.AppointmentItem;
import com.example.petcareui.data.model.NotificationItem;
import com.example.petcareui.data.model.StaffAppointmentItem;
import com.example.petcareui.databinding.ActivityNotificationsBinding;
import com.example.petcareui.databinding.ItemNotificationCardBinding;
import com.example.petcareui.ui.appointments.AppointmentDetailsActivity;
import com.example.petcareui.ui.staff.StaffAppointmentDetailsActivity;
import com.example.petcareui.ui.staff.StaffDashboardActivity;
import com.example.petcareui.utils.TokenManager;
import com.example.petcareui.utils.UiMotionUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    public static final String EXTRA_STATE = "extra_state";

    private ActivityNotificationsBinding binding;
    private TokenManager tokenManager;
    private final List<NotificationItem> notifications = new ArrayList<>();
    private boolean isStaff = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutAppBar, null);
        tokenManager = TokenManager.getInstance(this);
        isStaff = "STAFF".equalsIgnoreCase(tokenManager.getUserRole());

        setupTopBar();
        loadLiveNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLiveNotifications();
    }

    private void setupTopBar() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.tvMarkAllRead.setOnClickListener(v -> {
            List<String> unreadIds = new ArrayList<>();
            for (NotificationItem item : notifications) {
                if (item.isUnread()) {
                    item.setUnread(false);
                    unreadIds.add(item.getId());
                }
            }
            if (!unreadIds.isEmpty()) {
                tokenManager.markNotificationsRead(unreadIds);
                renderNotificationList();
                Toast.makeText(this, "Đã đánh dấu đọc tất cả thông báo", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnEmptyBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, isStaff ? StaffDashboardActivity.class : MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadLiveNotifications() {
        Call<List<AppointmentResponse>> call = isStaff
                ? ApiClient.getAppointmentApi().getClinicAppointments(null)
                : ApiClient.getAppointmentApi().getMyAppointments();

        call.enqueue(new Callback<List<AppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<AppointmentResponse>> c, Response<List<AppointmentResponse>> response) {
                if (isFinishing() || isDestroyed()) return;
                notifications.clear();
                if (response.isSuccessful() && response.body() != null) {
                    if (isStaff) {
                        notifications.addAll(buildStaffNotifications(NotificationsActivity.this, response.body()));
                    } else {
                        notifications.addAll(buildCustomerNotifications(NotificationsActivity.this, response.body()));
                    }
                }
                renderState();
            }

            @Override
            public void onFailure(Call<List<AppointmentResponse>> c, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                notifications.clear();
                renderState();
            }
        });
    }

    private void renderState() {
        if (notifications.isEmpty()) {
            binding.tvSubtitle.setVisibility(View.GONE);
            binding.tvMarkAllRead.setVisibility(View.GONE);
            binding.layoutNotificationsContent.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutNotificationsContent.setVisibility(View.VISIBLE);
            renderNotificationList();
        }
    }

    private void renderNotificationList() {
        binding.layoutGroupToday.removeAllViews();
        binding.layoutGroupEarlier.removeAllViews();

        int unreadCount = 0;
        int todayCount = 0;
        int earlierCount = 0;

        for (NotificationItem item : notifications) {
            if (item.isUnread()) {
                unreadCount++;
            }

            View cardView = createNotificationCardView(item);
            if (item.isTodayGroup()) {
                binding.layoutGroupToday.addView(cardView);
                todayCount++;
            } else {
                binding.layoutGroupEarlier.addView(cardView);
                earlierCount++;
            }
        }

        binding.tvLabelToday.setVisibility(todayCount > 0 ? View.VISIBLE : View.GONE);
        binding.layoutGroupToday.setVisibility(todayCount > 0 ? View.VISIBLE : View.GONE);

        binding.tvLabelEarlier.setVisibility(earlierCount > 0 ? View.VISIBLE : View.GONE);
        binding.layoutGroupEarlier.setVisibility(earlierCount > 0 ? View.VISIBLE : View.GONE);

        if (unreadCount > 0) {
            binding.tvSubtitle.setVisibility(View.VISIBLE);
            binding.tvSubtitle.setText(unreadCount + " thông báo chưa đọc");
            binding.tvMarkAllRead.setVisibility(View.VISIBLE);
        } else {
            binding.tvSubtitle.setVisibility(View.GONE);
            binding.tvMarkAllRead.setVisibility(View.GONE);
        }
    }

    private View createNotificationCardView(NotificationItem item) {
        ItemNotificationCardBinding cardBinding = ItemNotificationCardBinding.inflate(getLayoutInflater());

        cardBinding.tvNotifTitle.setText(item.getTitle());
        cardBinding.tvNotifBody.setText(item.getBody());
        cardBinding.tvNotifTime.setText(item.getTime());

        if (item.isUnread()) {
            cardBinding.layoutCardContainer.setBackgroundResource(R.drawable.bg_notification_card_unread);
            cardBinding.viewUnreadDot.setVisibility(View.VISIBLE);
        } else {
            cardBinding.layoutCardContainer.setBackgroundResource(R.drawable.bg_card_clinic);
            cardBinding.viewUnreadDot.setVisibility(View.GONE);
        }

        applyNotificationTypeStyle(cardBinding, item.getType());

        cardBinding.getRoot().setOnClickListener(v -> {
            if (item.isUnread()) {
                item.setUnread(false);
                tokenManager.markNotificationRead(item.getId());
                renderNotificationList();
            }

            if (isStaff && item.getStaffAppt() != null) {
                Intent intent = new Intent(this, StaffAppointmentDetailsActivity.class);
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_REF, item.getStaffAppt().getRef());
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_APPT, item.getStaffAppt());
                startActivity(intent);
            } else if (item.getCustomerAppt() != null) {
                Intent intent = new Intent(this, AppointmentDetailsActivity.class);
                intent.putExtra(AppointmentDetailsActivity.EXTRA_APPT, item.getCustomerAppt());
                intent.putExtra(AppointmentDetailsActivity.EXTRA_REF, item.getCustomerAppt().getRef());
                startActivity(intent);
            }
        });

        return cardBinding.getRoot();
    }

    private void applyNotificationTypeStyle(ItemNotificationCardBinding cardBinding, String type) {
        cardBinding.layoutIconBox.setBackgroundResource(R.drawable.bg_shortcut_icon_cream);
        if ("confirmed".equalsIgnoreCase(type)) {
            cardBinding.ivNotifIcon.setImageResource(R.drawable.ic_calendar_days);
            cardBinding.ivNotifIcon.setColorFilter(ContextCompat.getColor(this, R.color.ds_success));
        } else if ("pending".equalsIgnoreCase(type)) {
            cardBinding.ivNotifIcon.setImageResource(R.drawable.ic_clock);
            cardBinding.ivNotifIcon.setColorFilter(ContextCompat.getColor(this, R.color.ds_warning));
        } else if ("reminder".equalsIgnoreCase(type)) {
            cardBinding.ivNotifIcon.setImageResource(R.drawable.ic_bell);
            cardBinding.ivNotifIcon.setColorFilter(ContextCompat.getColor(this, R.color.ds_primary));
        } else if ("cancelled".equalsIgnoreCase(type)) {
            cardBinding.ivNotifIcon.setImageResource(R.drawable.ic_calendar_x);
            cardBinding.ivNotifIcon.setColorFilter(ContextCompat.getColor(this, R.color.ds_text_secondary));
        } else if ("rejected".equalsIgnoreCase(type)) {
            cardBinding.ivNotifIcon.setImageResource(R.drawable.ic_x_circle);
            cardBinding.ivNotifIcon.setColorFilter(ContextCompat.getColor(this, R.color.ds_error));
        }
    }

    // =========================================================================
    // SHARED REAL NOTIFICATION BUILDERS & BELL BADGE CHECKERS
    // =========================================================================

    public static boolean hasUnreadCustomerNotifications(Context context, List<AppointmentResponse> list) {
        for (NotificationItem item : buildCustomerNotifications(context, list)) {
            if (item.isUnread()) return true;
        }
        return false;
    }

    public static boolean hasUnreadStaffNotifications(Context context, List<AppointmentResponse> list) {
        for (NotificationItem item : buildStaffNotifications(context, list)) {
            if (item.isUnread()) return true;
        }
        return false;
    }

    public static List<NotificationItem> buildCustomerNotifications(Context context, List<AppointmentResponse> rawList) {
        List<NotificationItem> result = new ArrayList<>();
        if (rawList == null || rawList.isEmpty()) return result;

        TokenManager tm = TokenManager.getInstance(context);
        List<AppointmentResponse> sorted = new ArrayList<>(rawList);
        sorted.sort(Comparator.comparing(NotificationsActivity::extractSortTimestamp, Comparator.reverseOrder()));

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        for (AppointmentResponse res : sorted) {
            Long id = res.getId() != null ? res.getId() : 0L;
            String status = res.getStatus() != null ? res.getStatus().toUpperCase(Locale.ROOT) : "PENDING";
            String petName = res.getPetName() != null ? res.getPetName() : "bé cưng";
            String clinicName = res.getClinicName() != null ? res.getClinicName() : "Phòng khám";
            String serviceName = res.getServiceName() != null ? res.getServiceName() : "khám thú y";
            String slotWhen = formatShortSlotWhen(res.getSlotDate(), res.getStartTime());
            AppointmentItem apptItem = res.toAppointmentItem();

            // 1. Nhắc lịch tự động nếu có lịch ĐÃ XÁC NHẬN vào hôm nay hoặc ngày mai
            if ("CONFIRMED".equals(status) && res.getSlotDate() != null) {
                try {
                    LocalDate slotLocalDate = LocalDate.parse(res.getSlotDate().substring(0, 10));
                    if (slotLocalDate.equals(today) || slotLocalDate.equals(tomorrow)) {
                        String remindId = "remind_" + id + "_" + slotLocalDate;
                        String dayWord = slotLocalDate.equals(today) ? "hôm nay" : "ngày mai";
                        String startShort = res.getStartTime() != null && res.getStartTime().length() >= 5
                                ? res.getStartTime().substring(0, 5) : "";
                        NotificationItem reminder = new NotificationItem(
                                remindId,
                                "reminder",
                                "info",
                                "Nhắc lịch khám sắp tới",
                                "Lịch " + serviceName.toLowerCase(Locale.ROOT) + " của " + petName + " tại " + clinicName
                                        + " diễn ra vào " + dayWord + (startShort.isEmpty() ? "" : " lúc " + startShort)
                                        + ". Bạn nhớ đưa bé đến sớm 10 phút nhé.",
                                "Hôm nay",
                                !tm.isNotificationRead(remindId),
                                true
                        );
                        reminder.setCustomerAppt(apptItem);
                        result.add(reminder);
                    }
                } catch (Exception ignored) {
                }
            }

            // 2. Thông báo trạng thái thực tế của lịch hẹn
            String notifId = "appt_" + id + "_" + status;
            String type;
            String tone;
            String title;
            String body;

            switch (status) {
                case "CONFIRMED":
                    type = "confirmed";
                    tone = "success";
                    title = "Lịch hẹn đã được xác nhận";
                    body = clinicName + " đã xác nhận lịch " + serviceName.toLowerCase(Locale.ROOT)
                            + " cho " + petName + " vào " + slotWhen + ".";
                    break;
                case "COMPLETED":
                    type = "confirmed";
                    tone = "success";
                    title = "Đã hoàn thành lượt khám";
                    body = "Buổi " + serviceName.toLowerCase(Locale.ROOT) + " của " + petName
                            + " tại " + clinicName + " đã hoàn tất và lưu vào hồ sơ.";
                    break;
                case "REJECTED":
                    type = "rejected";
                    tone = "danger";
                    title = "Yêu cầu đặt lịch bị từ chối";
                    body = clinicName + " chưa thể tiếp nhận lịch " + serviceName.toLowerCase(Locale.ROOT)
                            + " của " + petName + " vào " + slotWhen + ". Bạn vui lòng chọn giờ khác nhé.";
                    break;
                case "CANCELLED":
                    type = "cancelled";
                    tone = "neutral";
                    title = "Lịch hẹn đã hủy";
                    body = "Lịch " + serviceName.toLowerCase(Locale.ROOT) + " của " + petName
                            + " tại " + clinicName + " (" + slotWhen + ") đã được hủy.";
                    break;
                case "NO_SHOW":
                    type = "cancelled";
                    tone = "neutral";
                    title = "Lỡ lịch hẹn khám";
                    body = "Lịch " + serviceName.toLowerCase(Locale.ROOT) + " của " + petName
                            + " tại " + clinicName + " (" + slotWhen + ") được ghi nhận vắng mặt.";
                    break;
                default:
                    type = "pending";
                    tone = "warning";
                    title = "Đang chờ phòng khám xác nhận";
                    body = clinicName + " đã nhận yêu cầu " + serviceName.toLowerCase(Locale.ROOT)
                            + " cho " + petName + " vào " + slotWhen + ".";
                    break;
            }

            String rawTs = res.getUpdatedAt() != null && !res.getUpdatedAt().isEmpty()
                    ? res.getUpdatedAt() : res.getCreatedAt();
            TimeLabel timeLabel = formatNotifTime(rawTs, res.getSlotDate());

            NotificationItem item = new NotificationItem(
                    notifId,
                    type,
                    tone,
                    title,
                    body,
                    timeLabel.label,
                    !tm.isNotificationRead(notifId),
                    timeLabel.isToday
            );
            item.setCustomerAppt(apptItem);
            result.add(item);
        }

        return result;
    }

    public static List<NotificationItem> buildStaffNotifications(Context context, List<AppointmentResponse> rawList) {
        List<NotificationItem> result = new ArrayList<>();
        if (rawList == null || rawList.isEmpty()) return result;

        TokenManager tm = TokenManager.getInstance(context);
        List<AppointmentResponse> sorted = new ArrayList<>(rawList);
        sorted.sort(Comparator.comparing(NotificationsActivity::extractSortTimestamp, Comparator.reverseOrder()));

        for (AppointmentResponse res : sorted) {
            Long id = res.getId() != null ? res.getId() : 0L;
            String status = res.getStatus() != null ? res.getStatus().toUpperCase(Locale.ROOT) : "PENDING";
            String customer = res.getCustomerName() != null && !res.getCustomerName().isEmpty()
                    ? res.getCustomerName() : "Khách hàng";
            String petName = res.getPetName() != null ? res.getPetName() : "thú cưng";
            String serviceName = res.getServiceName() != null ? res.getServiceName() : "khám thú y";
            String slotWhen = formatShortSlotWhen(res.getSlotDate(), res.getStartTime());
            StaffAppointmentItem staffItem = res.toStaffAppointmentItem();

            String notifId = "staff_appt_" + id + "_" + status;
            String type;
            String tone;
            String title;
            String body;

            switch (status) {
                case "PENDING":
                    type = "pending";
                    tone = "warning";
                    title = "Yêu cầu đặt lịch mới cần duyệt";
                    body = customer + " vừa đặt lịch " + serviceName.toLowerCase(Locale.ROOT)
                            + " cho bé " + petName + " vào " + slotWhen + ".";
                    break;
                case "CANCELLED":
                    type = "cancelled";
                    tone = "neutral";
                    title = "Khách hàng đã hủy lịch hẹn";
                    body = customer + " đã hủy lịch " + serviceName.toLowerCase(Locale.ROOT)
                            + " của bé " + petName + " (" + slotWhen + "). Khung giờ đã mở lại.";
                    break;
                case "CONFIRMED":
                    type = "confirmed";
                    tone = "success";
                    title = "Đã xác nhận lịch khám";
                    body = "Lịch " + serviceName.toLowerCase(Locale.ROOT) + " của bé " + petName
                            + " (" + customer + ") vào " + slotWhen + " đã được xác nhận.";
                    break;
                case "COMPLETED":
                    type = "confirmed";
                    tone = "success";
                    title = "Đã hoàn thành ca khám";
                    body = "Ca khám " + serviceName.toLowerCase(Locale.ROOT) + " cho bé " + petName
                            + " (" + customer + ") đã hoàn tất.";
                    break;
                default:
                    type = "rejected";
                    tone = "danger";
                    title = "Cập nhật lịch hẹn";
                    body = "Lịch khám của bé " + petName + " (" + customer + ") vào " + slotWhen + " đã cập nhật.";
                    break;
            }

            String rawTs = res.getUpdatedAt() != null && !res.getUpdatedAt().isEmpty()
                    ? res.getUpdatedAt() : res.getCreatedAt();
            TimeLabel timeLabel = formatNotifTime(rawTs, res.getSlotDate());

            NotificationItem item = new NotificationItem(
                    notifId,
                    type,
                    tone,
                    title,
                    body,
                    timeLabel.label,
                    !tm.isNotificationRead(notifId),
                    timeLabel.isToday
            );
            item.setStaffAppt(staffItem);
            result.add(item);
        }

        return result;
    }

    private static String extractSortTimestamp(AppointmentResponse r) {
        if (r.getUpdatedAt() != null && !r.getUpdatedAt().isEmpty()) return r.getUpdatedAt();
        if (r.getCreatedAt() != null && !r.getCreatedAt().isEmpty()) return r.getCreatedAt();
        return (r.getSlotDate() != null ? r.getSlotDate() : "") + "T" + (r.getStartTime() != null ? r.getStartTime() : "");
    }

    private static String formatShortSlotWhen(String slotDate, String startTime) {
        String datePart = slotDate != null ? slotDate : "";
        try {
            if (slotDate != null && slotDate.length() >= 10) {
                LocalDate d = LocalDate.parse(slotDate.substring(0, 10));
                String dow;
                switch (d.getDayOfWeek()) {
                    case MONDAY: dow = "Thứ 2"; break;
                    case TUESDAY: dow = "Thứ 3"; break;
                    case WEDNESDAY: dow = "Thứ 4"; break;
                    case THURSDAY: dow = "Thứ 5"; break;
                    case FRIDAY: dow = "Thứ 6"; break;
                    case SATURDAY: dow = "Thứ 7"; break;
                    default: dow = "CN"; break;
                }
                datePart = String.format(Locale.US, "%s, %02d/%02d", dow, d.getDayOfMonth(), d.getMonthValue());
            }
        } catch (Exception ignored) {
        }
        String timeShort = (startTime != null && startTime.length() >= 5) ? startTime.substring(0, 5) : (startTime != null ? startTime : "");
        if (!datePart.isEmpty() && !timeShort.isEmpty()) {
            return datePart + " lúc " + timeShort;
        }
        return !datePart.isEmpty() ? datePart : timeShort;
    }

    private static class TimeLabel {
        final String label;
        final boolean isToday;

        TimeLabel(String label, boolean isToday) {
            this.label = label;
            this.isToday = isToday;
        }
    }

    private static TimeLabel formatNotifTime(String isoTs, String fallbackSlotDate) {
        LocalDate today = LocalDate.now();
        if (isoTs != null && !isoTs.trim().isEmpty()) {
            try {
                ZonedDateTime zdt = OffsetDateTime.parse(isoTs.trim()).atZoneSameInstant(ZoneId.systemDefault());
                LocalDate d = zdt.toLocalDate();
                String hm = zdt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.US));
                if (d.equals(today)) {
                    return new TimeLabel("Hôm nay · " + hm, true);
                }
                if (d.equals(today.minusDays(1))) {
                    return new TimeLabel("Hôm qua · " + hm, false);
                }
                return new TimeLabel(zdt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm", Locale.US)), false);
            } catch (Exception ignored) {
            }
        }
        if (fallbackSlotDate != null && fallbackSlotDate.length() >= 10) {
            try {
                LocalDate d = LocalDate.parse(fallbackSlotDate.substring(0, 10));
                if (d.equals(today)) {
                    return new TimeLabel("Hôm nay", true);
                }
                return new TimeLabel(String.format(Locale.US, "%02d/%02d/%d", d.getDayOfMonth(), d.getMonthValue(), d.getYear()), false);
            } catch (Exception ignored) {
            }
        }
        return new TimeLabel("Gần đây", false);
    }
}
