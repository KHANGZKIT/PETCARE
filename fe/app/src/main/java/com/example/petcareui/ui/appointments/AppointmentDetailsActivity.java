package com.example.petcareui.ui.appointments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.dto.response.AppointmentResponse;
import com.example.petcareui.data.dto.response.ClinicResponse;
import com.example.petcareui.data.dto.response.PetResponse;
import com.example.petcareui.data.model.AppointmentItem;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.databinding.ActivityAppointmentDetailsBinding;
import com.example.petcareui.ui.clinic.ClinicDetailsActivity;
import com.example.petcareui.ui.clinic.ClinicListActivity;
import com.example.petcareui.ui.pets.MyPetsActivity;
import com.example.petcareui.ui.pets.PetDetailsActivity;
import com.example.petcareui.utils.UiMotionUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_REF = "extra_ref";
    public static final String EXTRA_APPT = "extra_appt";
    public static final String EXTRA_STATE = "extra_state";

    private ActivityAppointmentDetailsBinding binding;

    private AppointmentItem appt;
    private ClinicItem resolvedClinicItem;
    private PetItem resolvedPetItem;
    private String currentState = "";
    private String previousStateBeforeDialog = "pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopBar, null);

        setupStateTabs();
        setupInteractiveActions();
        extractIntentData();
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

        if (appt != null) {
            resolveStateFromAppt();
            bindAppointmentToUi();
            enrichClinicAndPetDetails();
            // Refresh latest status & fields from backend if id is known
            Long apptId = appt.getId();
            if (apptId != null && apptId > 0) {
                fetchAppointmentFromServer(apptId, false);
            }
        } else {
            Long parsedId = parseIdFromRef(getIntent().getStringExtra(EXTRA_REF));
            if (parsedId != null && parsedId > 0) {
                fetchAppointmentFromServer(parsedId, true);
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin lịch hẹn", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private Long parseIdFromRef(String ref) {
        if (ref == null || ref.trim().isEmpty()) return null;
        try {
            String s = ref.trim();
            if (s.toUpperCase(Locale.ROOT).startsWith("PC-")) {
                s = s.substring(3).trim();
            }
            return Long.parseLong(s);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void fetchAppointmentFromServer(Long apptId, boolean finishOnFail) {
        ApiClient.getAppointmentApi().getAppointmentById(apptId).enqueue(new Callback<AppointmentResponse>() {
            @Override
            public void onResponse(Call<AppointmentResponse> call, Response<AppointmentResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    appt = response.body().toAppointmentItem();
                    currentState = "";
                    resolveStateFromAppt();
                    bindAppointmentToUi();
                    enrichClinicAndPetDetails();
                } else if (finishOnFail) {
                    Toast.makeText(AppointmentDetailsActivity.this, "Không tải được chi tiết lịch hẹn", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<AppointmentResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (finishOnFail) {
                    Toast.makeText(AppointmentDetailsActivity.this, "Lỗi kết nối máy chủ khi tải lịch hẹn", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void resolveStateFromAppt() {
        if (appt == null) return;
        if (currentState == null || currentState.isEmpty()) {
            if ("CONFIRMED".equalsIgnoreCase(appt.getStatus())) {
                currentState = "confirmed";
            } else if ("COMPLETED".equalsIgnoreCase(appt.getStatus())) {
                currentState = "completed";
            } else if ("NO_SHOW".equalsIgnoreCase(appt.getStatus())) {
                currentState = "no_show";
            } else if ("CANCELLED".equalsIgnoreCase(appt.getStatus()) || "REJECTED".equalsIgnoreCase(appt.getStatus())) {
                currentState = "cancelled";
            } else {
                currentState = "pending";
            }
        }
    }

    private void bindAppointmentToUi() {
        if (appt == null) return;

        binding.tvClinicNameTop.setText(appt.getClinic());
        binding.btnBack.setOnClickListener(v -> finish());

        String reqText = appt.getRequested();
        binding.tvRequestedTime.setText(reqText != null && !reqText.isEmpty() ? reqText : "Đã ghi nhận");

        // Thẻ Phòng khám
        binding.tvClinicName.setText(appt.getClinic());
        String addrDisplay = (appt.getClinicAddress() != null && !appt.getClinicAddress().trim().isEmpty())
                ? appt.getClinicAddress().trim()
                : (appt.getClinicArea() != null ? appt.getClinicArea().trim() : "Hà Nội");
        binding.tvClinicArea.setText(addrDisplay);

        // Dịch vụ & Thời lượng thật
        binding.tvServiceName.setText(appt.getService());
        binding.tvServicePrice.setText(appt.getPrice());
        int dur = appt.getDurationMinutes() != null && appt.getDurationMinutes() > 0 ? appt.getDurationMinutes() : 30;
        binding.tvServiceDuration.setText(dur + " phút");
        binding.tvPetName.setText(appt.getPet());

        // Thú cưng
        int avatarRes = resolveUnifiedPetAvatar(appt.getPet(), appt.getPetSpecies(), appt.getPetImgRes());
        binding.ivPetAvatar.setImageResource(avatarRes);
        if (avatarRes == R.drawable.img_pet_bong) {
            binding.ivPetAvatar.setPadding(0, 0, 0, 0);
            binding.ivPetAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            int pad = Math.round(4 * getResources().getDisplayMetrics().density);
            binding.ivPetAvatar.setPadding(pad, pad, pad, pad);
            binding.ivPetAvatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        binding.tvCardPetName.setText(appt.getPet());
        binding.tvPetDetails.setText(buildPetSubtitle(appt.getPetSpecies(), appt.getPetBreed(), appt.getPetAge(), appt.getPetGender()));

        // Ngày & Giờ khám
        binding.tvDate.setText(formatNaturalDate(appt.getDate()));
        binding.tvTime.setText(appt.getTime());

        // Ghi chú của khách hàng (nếu có)
        if (appt.getNote() != null && !appt.getNote().trim().isEmpty()) {
            binding.blockCustomerNote.setVisibility(View.VISIBLE);
            binding.tvCustomerNote.setText("“" + appt.getNote().trim() + "”");
        } else {
            binding.blockCustomerNote.setVisibility(View.GONE);
        }

        // Timeline Step 1 (thời gian tạo thực tế từ createdAt)
        binding.tvTimelineStep1Time.setText(formatCreatedTimestamp(appt.getCreatedAt(), appt.getDate()));

        // Hộp thoại hủy
        binding.tvDialogBody.setText(appt.getClinic() + " sẽ được thông báo ngay và khung giờ " + appt.getTime() + " được mở lại.");

        applyState(currentState);
    }

    private String buildPetSubtitle(String species, String breed, String age, String gender) {
        StringBuilder sb = new StringBuilder();
        if (breed != null && !breed.trim().isEmpty()) {
            sb.append(breed.trim());
        } else if (species != null && !species.trim().isEmpty()) {
            sb.append(species.trim());
        }
        if (age != null && !age.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(age.trim());
        }
        if (gender != null && !gender.trim().isEmpty()) {
            String cleanSex = gender.replace("♂", "").replace("♀", "").trim();
            if (!cleanSex.isEmpty()) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append(cleanSex);
            }
        }
        return sb.length() > 0 ? sb.toString() : "Xem hồ sơ thú cưng";
    }

    private void enrichClinicAndPetDetails() {
        if (appt == null) return;

        // 1. Tải thông tin thật của phòng khám (SĐT, địa chỉ, tọa độ bản đồ)
        Long clinicId = appt.getClinicId();
        if (clinicId != null && clinicId > 0) {
            ApiClient.getClinicApi().getClinicById(clinicId).enqueue(new Callback<ClinicResponse>() {
                @Override
                public void onResponse(Call<ClinicResponse> call, Response<ClinicResponse> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        ClinicResponse clinicRes = response.body();
                        resolvedClinicItem = clinicRes.toClinicItem();
                        appt.setClinicPhone(clinicRes.getPhone());
                        appt.setClinicAddress(clinicRes.getAddress());
                        appt.setClinicLatitude(clinicRes.getLatitude());
                        appt.setClinicLongitude(clinicRes.getLongitude());
                        if (clinicRes.getAddress() != null && !clinicRes.getAddress().trim().isEmpty()) {
                            binding.tvClinicArea.setText(clinicRes.getAddress().trim());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ClinicResponse> call, Throwable t) {
                }
            });
        } else {
            // Nếu mở từ thẻ cũ chưa gắn clinicId, tra cứu theo tên phòng khám
            ApiClient.getClinicApi().getAllClinics(null).enqueue(new Callback<List<ClinicResponse>>() {
                @Override
                public void onResponse(Call<List<ClinicResponse>> call, Response<List<ClinicResponse>> response) {
                    if (isFinishing() || isDestroyed() || response.body() == null) return;
                    String targetName = appt.getClinic() != null ? appt.getClinic().trim() : "";
                    for (ClinicResponse c : response.body()) {
                        if (c.getName() != null && c.getName().trim().equalsIgnoreCase(targetName)) {
                            resolvedClinicItem = c.toClinicItem();
                            appt.setClinicId(c.getId());
                            appt.setClinicPhone(c.getPhone());
                            appt.setClinicAddress(c.getAddress());
                            appt.setClinicLatitude(c.getLatitude());
                            appt.setClinicLongitude(c.getLongitude());
                            if (c.getAddress() != null && !c.getAddress().trim().isEmpty()) {
                                binding.tvClinicArea.setText(c.getAddress().trim());
                            }
                            break;
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<ClinicResponse>> call, Throwable t) {
                }
            });
        }

        // 2. Tải đúng hồ sơ thú cưng của lịch hẹn để hiển thị phụ đề & mở thẳng PetDetailsActivity
        ApiClient.getPetApi().getMyPets().enqueue(new Callback<List<PetResponse>>() {
            @Override
            public void onResponse(Call<List<PetResponse>> call, Response<List<PetResponse>> response) {
                if (isFinishing() || isDestroyed() || response.body() == null) return;
                Long targetPetId = appt.getPetId();
                String targetPetName = appt.getPet() != null ? appt.getPet().trim() : "";
                for (PetResponse p : response.body()) {
                    boolean idMatch = targetPetId != null && targetPetId.equals(p.getId());
                    boolean nameMatch = targetPetId == null && p.getName() != null && p.getName().trim().equalsIgnoreCase(targetPetName);
                    if (idMatch || nameMatch) {
                        resolvedPetItem = p.toPetItem();
                        appt.setPetId(p.getId());
                        appt.setPetSpecies(p.getSpecies());
                        appt.setPetBreed(p.getBreed());
                        appt.setPetAge(p.getAge());
                        appt.setPetGender(p.getGender());
                        binding.tvPetDetails.setText(buildPetSubtitle(
                                p.getSpecies(),
                                resolvedPetItem.getBreed(),
                                resolvedPetItem.getAge(),
                                resolvedPetItem.getSex()
                        ));
                        break;
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PetResponse>> call, Throwable t) {
            }
        });
    }

    private int resolveUnifiedPetAvatar(String petName, String petSpecies, int currentRes) {
        if (currentRes == R.drawable.img_pet_bong || currentRes == R.drawable.mascot_corgi_running || currentRes == R.drawable.mascot_corgi_wave) {
            return currentRes;
        }
        String lowerName = petName != null ? petName.toLowerCase(Locale.ROOT) : "";
        String lowerSpec = petSpecies != null ? petSpecies.toLowerCase(Locale.ROOT) : "";
        if (lowerSpec.contains("mèo") || lowerName.contains("mèo") || lowerName.contains("bông") || lowerName.contains("mimi") || lowerName.contains("miu")) {
            return R.drawable.img_pet_bong;
        }
        return R.drawable.mascot_corgi_running;
    }

    private String formatCreatedTimestamp(String isoCreatedAt, String fallbackDate) {
        if (isoCreatedAt != null && !isoCreatedAt.trim().isEmpty()) {
            try {
                ZonedDateTime zdt = OffsetDateTime.parse(isoCreatedAt.trim()).atZoneSameInstant(ZoneId.systemDefault());
                return zdt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm", Locale.US));
            } catch (Exception ignored) {
            }
        }
        return formatNaturalDate(fallbackDate);
    }

    private String formatNaturalDate(String rawDate) {
        if (rawDate == null) return "";
        String s = rawDate.trim();
        try {
            if (s.length() >= 10 && s.charAt(4) == '-' && s.charAt(7) == '-') {
                LocalDate d = LocalDate.parse(s.substring(0, 10));
                String dow;
                switch (d.getDayOfWeek()) {
                    case MONDAY: dow = "T2"; break;
                    case TUESDAY: dow = "T3"; break;
                    case WEDNESDAY: dow = "T4"; break;
                    case THURSDAY: dow = "T5"; break;
                    case FRIDAY: dow = "T6"; break;
                    case SATURDAY: dow = "T7"; break;
                    default: dow = "CN"; break;
                }
                return String.format(Locale.US, "%s, %02d/%02d/%d", dow, d.getDayOfMonth(), d.getMonthValue(), d.getYear());
            }
        } catch (Exception ignored) {
        }
        return s;
    }

    private void setupStateTabs() {
        if (binding.scrollStateTabs != null) {
            binding.scrollStateTabs.setVisibility(View.GONE);
        }
    }

    public void applyState(String state) {
        if (!"cancel-dialog".equals(state)) {
            previousStateBeforeDialog = state;
        }
        currentState = state;

        updateTabChip(binding.tabStatePending, "pending".equals(state));
        updateTabChip(binding.tabStateConfirmed, "confirmed".equals(state));
        updateTabChip(binding.tabStateCompleted, "completed".equals(state));
        updateTabChip(binding.tabStateDialog, "cancel-dialog".equals(state));
        updateTabChip(binding.tabStateCancelled, "cancelled".equals(state));

        if ("cancel-dialog".equals(state)) {
            binding.layoutCancelDialogOverlay.setVisibility(View.VISIBLE);
            return;
        } else {
            binding.layoutCancelDialogOverlay.setVisibility(View.GONE);
        }

        if ("no_show".equals(state)) {
            binding.layoutNoticeBox.setBackgroundResource(R.drawable.bg_notice_danger);
            binding.ivNoticeIcon.setImageResource(R.drawable.ic_alert_circle);
            binding.ivNoticeIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_destructive));
            binding.tvNoticeTitle.setText("Không đến theo lịch hẹn");
            binding.tvNoticeTitle.setTextColor(ContextCompat.getColor(this, R.color.status_destructive_ink));
            binding.tvNoticeBody.setText("Phòng khám đã ghi nhận vắng mặt ở khung giờ này. Bạn có thể đặt lịch mới bất cứ lúc nào.");
            binding.tvNoticeBody.setTextColor(ContextCompat.getColor(this, R.color.status_destructive_ink));

            binding.viewTimelineStep2Dot.setBackgroundResource(R.drawable.bg_pin_circle);
            binding.tvTimelineStep2Title.setText("Ghi nhận không đến hẹn");
            binding.tvTimelineStep2Time.setText("Đã cập nhật");

            binding.layoutCancelPolicyNotice.setVisibility(View.GONE);
            binding.btnCancelBooking.setVisibility(View.GONE);
            binding.btnBackToListOutline.setVisibility(View.GONE);
            binding.layoutCancelledActions.setVisibility(View.VISIBLE);
            return;
        }

        if ("cancelled".equals(state)) {
            boolean isRejected = appt != null && "REJECTED".equalsIgnoreCase(appt.getStatus());
            binding.layoutNoticeBox.setBackgroundResource(R.drawable.bg_notice_danger);
            binding.ivNoticeIcon.setImageResource(R.drawable.ic_alert_circle);
            binding.ivNoticeIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_destructive));
            binding.tvNoticeTitle.setText(isRejected ? "Phòng khám đã từ chối lịch hẹn" : "Đã hủy lịch hẹn");
            binding.tvNoticeTitle.setTextColor(ContextCompat.getColor(this, R.color.status_destructive_ink));
            binding.tvNoticeBody.setText(isRejected
                    ? "Khung giờ này hiện không thể tiếp nhận. Bạn vui lòng đặt khung giờ khác nhé."
                    : "Khung giờ đã được mở lại. Bạn có thể đặt lại bất cứ lúc nào.");
            binding.tvNoticeBody.setTextColor(ContextCompat.getColor(this, R.color.status_destructive_ink));

            binding.viewTimelineStep2Dot.setBackgroundResource(R.drawable.bg_pin_circle);
            binding.tvTimelineStep2Title.setText(isRejected ? "Phòng khám từ chối yêu cầu" : "Đã hủy lịch hẹn");
            binding.tvTimelineStep2Time.setText("Đã cập nhật");

            binding.layoutCancelPolicyNotice.setVisibility(View.GONE);
            binding.btnCancelBooking.setVisibility(View.GONE);
            binding.btnBackToListOutline.setVisibility(View.GONE);
            binding.layoutCancelledActions.setVisibility(View.VISIBLE);
            return;
        }

        binding.layoutCancelledActions.setVisibility(View.GONE);

        if ("completed".equals(state)) {
            binding.layoutNoticeBox.setBackgroundResource(R.drawable.bg_notice_success);
            binding.ivNoticeIcon.setImageResource(R.drawable.ic_check_circle);
            binding.ivNoticeIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_success_ink));
            binding.tvNoticeTitle.setText("Đã hoàn thành lượt khám");
            binding.tvNoticeTitle.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));
            binding.tvNoticeBody.setText("Cảm ơn bạn đã đến. Lần khám đã được lưu vào hồ sơ sức khỏe của bé.");
            binding.tvNoticeBody.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));

            binding.viewTimelineStep2Dot.setBackgroundResource(R.drawable.bg_pin_circle);
            binding.tvTimelineStep2Title.setText("Đã hoàn thành lượt khám");
            binding.tvTimelineStep2Time.setText("Hoàn tất");

            binding.layoutCancelPolicyNotice.setVisibility(View.GONE);
            binding.btnCancelBooking.setVisibility(View.GONE);
            binding.btnBackToListOutline.setVisibility(View.VISIBLE);
            return;
        }

        if ("confirmed".equals(state)) {
            binding.layoutNoticeBox.setBackgroundResource(R.drawable.bg_notice_success);
            binding.ivNoticeIcon.setImageResource(R.drawable.ic_check_circle);
            binding.ivNoticeIcon.setColorFilter(ContextCompat.getColor(this, R.color.ds_success));
            binding.tvNoticeTitle.setText("Phòng khám đã xác nhận");
            binding.tvNoticeTitle.setTextColor(ContextCompat.getColor(this, R.color.ds_success));
            binding.tvNoticeBody.setText("Bạn nhớ đưa bé đến sớm khoảng 10 phút trước giờ hẹn nhé.");
            binding.tvNoticeBody.setTextColor(ContextCompat.getColor(this, R.color.ds_text_primary));

            binding.viewTimelineStep2Dot.setBackgroundResource(R.drawable.bg_pin_circle);
            binding.tvTimelineStep2Title.setText("Phòng khám đã xác nhận");
            binding.tvTimelineStep2Time.setText("Đã duyệt");

            binding.layoutCancelPolicyNotice.setVisibility(View.VISIBLE);
            binding.btnCancelBooking.setVisibility(View.VISIBLE);
            binding.btnBackToListOutline.setVisibility(View.GONE);
            return;
        }

        // Mặc định: Chờ xác nhận (Pending)
        binding.layoutNoticeBox.setBackgroundResource(R.drawable.bg_notice_warning);
        binding.ivNoticeIcon.setImageResource(R.drawable.ic_alert_triangle);
        binding.ivNoticeIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_warning));
        binding.tvNoticeTitle.setText("Đang chờ phòng khám xác nhận");
        binding.tvNoticeTitle.setTextColor(ContextCompat.getColor(this, R.color.status_warning_ink));
        binding.tvNoticeBody.setText("Bạn có thể hủy miễn phí trước khi phòng khám phản hồi.");
        binding.tvNoticeBody.setTextColor(ContextCompat.getColor(this, R.color.status_warning_ink));

        binding.viewTimelineStep2Dot.setBackgroundResource(R.drawable.bg_step_dot_inactive);
        binding.tvTimelineStep2Title.setText("Chờ phòng khám phản hồi");
        binding.tvTimelineStep2Time.setText("Đang chờ");

        binding.layoutCancelPolicyNotice.setVisibility(View.VISIBLE);
        binding.btnCancelBooking.setVisibility(View.VISIBLE);
        binding.btnBackToListOutline.setVisibility(View.GONE);
    }

    private void updateTabChip(TextView tab, boolean active) {
        if (active) {
            tab.setBackgroundResource(R.drawable.bg_chip_active);
            tab.setTextColor(ContextCompat.getColor(this, R.color.ds_primary));
        } else {
            tab.setBackgroundResource(R.drawable.bg_chip_inactive);
            tab.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }
    }

    private void setupInteractiveActions() {
        // Bấm vào thông tin phòng khám -> Mở trang Chi tiết phòng khám thật
        binding.rowClinicHeader.setOnClickListener(v -> {
            if (resolvedClinicItem != null) {
                Intent intent = new Intent(this, ClinicDetailsActivity.class);
                intent.putExtra(ClinicDetailsActivity.EXTRA_CLINIC, resolvedClinicItem);
                startActivity(intent);
            } else if (appt != null) {
                ClinicItem fallbackClinic = new ClinicItem(
                        appt.getClinicId() != null ? String.valueOf(appt.getClinicId()) : "1",
                        appt.getClinic(),
                        appt.getClinicAddress() != null ? appt.getClinicAddress() : appt.getClinicArea(),
                        4.8,
                        150,
                        "",
                        true,
                        "08:00 – 20:00",
                        appt.getClinicPhone() != null ? appt.getClinicPhone() : "",
                        "Phòng khám thú y tiêu chuẩn chất lượng cao tại Hà Nội.",
                        "Hỗ trợ trong ngày",
                        "Tiếng Việt"
                );
                Intent intent = new Intent(this, ClinicDetailsActivity.class);
                intent.putExtra(ClinicDetailsActivity.EXTRA_CLINIC, fallbackClinic);
                startActivity(intent);
            }
        });

        // 1. Chỉ đường: Mở Google Maps trực tiếp đến địa chỉ / tọa độ phòng khám thật
        binding.btnDirections.setOnClickListener(v -> {
            if (appt == null) return;
            String addr = (appt.getClinicAddress() != null && !appt.getClinicAddress().trim().isEmpty())
                    ? appt.getClinicAddress().trim()
                    : (appt.getClinicArea() != null ? appt.getClinicArea().trim() : "Hà Nội");
            String query = appt.getClinic() + ", " + addr;
            Double lat = appt.getClinicLatitude();
            Double lng = appt.getClinicLongitude();
            Uri mapUri = (lat != null && lng != null)
                    ? Uri.parse("geo:" + lat + "," + lng + "?q=" + Uri.encode(query))
                    : Uri.parse("geo:0,0?q=" + Uri.encode(query));
            try {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Địa chỉ: " + query, Toast.LENGTH_LONG).show();
            }
        });

        // 2. Gọi phòng khám: Quay đúng số điện thoại của phòng khám trong lịch hẹn
        binding.btnCallClinic.setOnClickListener(v -> {
            String phone = appt != null ? appt.getClinicPhone() : null;
            if (phone != null && !phone.trim().isEmpty()) {
                dialPhoneNumber(phone);
            } else if (appt != null && appt.getClinicId() != null) {
                ApiClient.getClinicApi().getClinicById(appt.getClinicId()).enqueue(new Callback<ClinicResponse>() {
                    @Override
                    public void onResponse(Call<ClinicResponse> call, Response<ClinicResponse> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getPhone() != null) {
                            appt.setClinicPhone(response.body().getPhone());
                            dialPhoneNumber(response.body().getPhone());
                        } else {
                            Toast.makeText(AppointmentDetailsActivity.this, "Chưa có số điện thoại phòng khám", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ClinicResponse> call, Throwable t) {
                        Toast.makeText(AppointmentDetailsActivity.this, "Không tải được số điện thoại phòng khám", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Chưa có số điện thoại phòng khám", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Mở hồ sơ bé: Đi thẳng vào đúng hồ sơ thú cưng của lịch hẹn này
        View.OnClickListener openPetListener = v -> openExactPetProfile();
        binding.btnOpenPetProfile.setOnClickListener(openPetListener);
        binding.rowPetProfile.setOnClickListener(openPetListener);

        // Nút Hủy lịch hẹn
        binding.btnCancelBooking.setOnClickListener(v -> applyState("cancel-dialog"));

        binding.btnConfirmCancelBooking.setOnClickListener(v -> {
            Long apptId = appt != null ? appt.getId() : null;
            if (apptId != null && apptId > 0) {
                binding.btnConfirmCancelBooking.setEnabled(false);
                ApiClient.getAppointmentApi().cancelAppointment(apptId).enqueue(new Callback<AppointmentResponse>() {
                    @Override
                    public void onResponse(Call<AppointmentResponse> call, Response<AppointmentResponse> response) {
                        binding.btnConfirmCancelBooking.setEnabled(true);
                        if (response.isSuccessful()) {
                            if (appt != null) appt.setStatus("CANCELLED");
                            applyState("cancelled");
                            Toast.makeText(AppointmentDetailsActivity.this, "Đã hủy lịch hẹn thành công!", Toast.LENGTH_SHORT).show();
                        } else {
                            applyState(previousStateBeforeDialog);
                            Toast.makeText(AppointmentDetailsActivity.this, "Không thể hủy lịch lúc này", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AppointmentResponse> call, Throwable t) {
                        binding.btnConfirmCancelBooking.setEnabled(true);
                        applyState(previousStateBeforeDialog);
                        Toast.makeText(AppointmentDetailsActivity.this, "Lỗi kết nối máy chủ khi hủy lịch hẹn.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(AppointmentDetailsActivity.this, "Không thể hủy lịch hẹn lúc này.", Toast.LENGTH_SHORT).show();
                applyState(previousStateBeforeDialog);
            }
        });

        binding.btnKeepBooking.setOnClickListener(v -> applyState(previousStateBeforeDialog));
        binding.layoutCancelDialogOverlay.setOnClickListener(v -> applyState(previousStateBeforeDialog));

        binding.btnBookAnother.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnBackToListGhost.setOnClickListener(v -> finish());
        binding.btnBackToListOutline.setOnClickListener(v -> finish());
    }

    private void dialPhoneNumber(String rawPhone) {
        String cleanPhone = rawPhone.replaceAll("\\s+", "");
        try {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + cleanPhone));
            startActivity(dialIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Hotline phòng khám: " + rawPhone, Toast.LENGTH_SHORT).show();
        }
    }

    private void openExactPetProfile() {
        if (resolvedPetItem != null) {
            Intent intent = new Intent(this, PetDetailsActivity.class);
            intent.putExtra(PetDetailsActivity.EXTRA_PET, resolvedPetItem);
            startActivity(intent);
            return;
        }
        if (appt != null && appt.getPetId() != null) {
            int avatarRes = resolveUnifiedPetAvatar(appt.getPet(), appt.getPetSpecies(), appt.getPetImgRes());
            PetItem tempPet = new PetItem(
                    String.valueOf(appt.getPetId()),
                    appt.getPet() != null ? appt.getPet() : "Bé cưng",
                    appt.getPetSpecies() != null ? appt.getPetSpecies() : "Thú cưng",
                    appt.getPetBreed() != null ? appt.getPetBreed() : "",
                    appt.getPetAge() != null ? appt.getPetAge() : "",
                    "",
                    appt.getPetGender() != null ? appt.getPetGender() : "",
                    "",
                    avatarRes
            );
            Intent intent = new Intent(this, PetDetailsActivity.class);
            intent.putExtra(PetDetailsActivity.EXTRA_PET, tempPet);
            startActivity(intent);
            return;
        }
        // Fallback cuối nếu không có petId: mở danh sách thú cưng
        Intent intent = new Intent(this, MyPetsActivity.class);
        intent.putExtra(MyPetsActivity.EXTRA_BOOKING_MODE, false);
        startActivity(intent);
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
