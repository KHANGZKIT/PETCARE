package com.example.petcareui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.dto.response.AppointmentResponse;
import com.example.petcareui.data.dto.response.ClinicResponse;
import com.example.petcareui.data.dto.response.PetResponse;
import com.example.petcareui.data.dto.response.UserProfileResponse;
import com.example.petcareui.data.model.AppointmentItem;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.databinding.ActivityMainBinding;
import com.example.petcareui.databinding.ItemHomeClinicCardBinding;
import com.example.petcareui.databinding.ItemHomePetCardBinding;
import com.example.petcareui.ui.appointments.AppointmentDetailsActivity;
import com.example.petcareui.ui.appointments.MyAppointmentsActivity;
import com.example.petcareui.ui.auth.LoginActivity;
import com.example.petcareui.ui.clinic.ClinicDetailsActivity;
import com.example.petcareui.ui.clinic.ClinicListActivity;
import com.example.petcareui.ui.notifications.NotificationsActivity;
import com.example.petcareui.ui.pets.AddPetActivity;
import com.example.petcareui.ui.pets.MyPetsActivity;
import com.example.petcareui.ui.pets.PetDetailsActivity;
import com.example.petcareui.ui.profile.ProfileActivity;
import com.example.petcareui.ui.staff.StaffDashboardActivity;
import com.example.petcareui.utils.TokenManager;
import com.example.petcareui.utils.UiMotionUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_HOME_CLINICS = 3;

    private ActivityMainBinding binding;
    private TokenManager tokenManager;
    private boolean authRedirected = false;
    private boolean isFirstResume = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.ds_home_bg));
        UiMotionUtils.applyWindowInsets(binding.main, binding.layoutHomeBody, binding.bottomNavigation);

        tokenManager = TokenManager.getInstance(this);
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        displayUserInfo();
        setupListeners();
        setupMotionEffects();

        refreshUserProfile(false);
        loadAllHomeSections();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tokenManager == null || !tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }
        displayUserInfo();
        if (isFirstResume) {
            isFirstResume = false;
            return;
        }
        loadUpcomingAppointment();
        loadMyPets();
    }

    private void loadAllHomeSections() {
        loadUpcomingAppointment();
        loadMyPets();
        loadClinics();
    }

    private void setupMotionEffects() {
        UiMotionUtils.animateEntranceSections(
                binding.sectionHeader,
                binding.bannerBooking,
                binding.sectionUpcoming,
                binding.sectionMyPets,
                binding.sectionClinics
        );

        UiMotionUtils.attachSpringTouch(
                binding.btnNotification,
                binding.btnAvatar,
                binding.bannerBooking,
                binding.btnBookNow,
                binding.cardUpcomingAppointment
        );
    }

    private void displayUserInfo() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) {
            binding.tvGreeting.setText("Chào buổi sáng,");
        } else if (hour < 18) {
            binding.tvGreeting.setText("Chào buổi chiều,");
        } else {
            binding.tvGreeting.setText("Chào buổi tối,");
        }

        String name = tokenManager.getUserName();
        if (name != null && !name.trim().isEmpty()) {
            binding.tvWelcomeName.setText(name.trim());

            String[] parts = name.trim().split("\\s+");
            StringBuilder initials = new StringBuilder();
            if (parts.length > 0 && !parts[0].isEmpty()) {
                initials.append(parts[0].charAt(0));
            }
            if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                initials.append(parts[parts.length - 1].charAt(0));
            }
            binding.tvAvatarInitials.setText(initials.toString().toUpperCase(Locale.ROOT));
        } else {
            binding.tvWelcomeName.setText("Khách hàng");
            binding.tvAvatarInitials.setText("KH");
        }
    }

    private void setupListeners() {
        // Header actions
        binding.btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        binding.btnAvatar.setOnClickListener(v -> openProfileScreen());

        // Hero Booking CTA (Push flow -> ClinicListActivity in booking/browse mode)
        View.OnClickListener openBookingFlow = v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            intent.putExtra(ClinicListActivity.EXTRA_TAB_MODE, false);
            startActivity(intent);
        };
        binding.bannerBooking.setOnClickListener(openBookingFlow);
        binding.btnBookNow.setOnClickListener(openBookingFlow);

        // Section links & retry actions
        binding.tvSeeAllAppointments.setOnClickListener(v ->
                startActivity(new Intent(this, MyAppointmentsActivity.class)));
        binding.btnRetryUpcoming.setOnClickListener(v -> loadUpcomingAppointment());

        View.OnClickListener addPetListener = v ->
                startActivity(new Intent(this, AddPetActivity.class));
        binding.btnAddPetHeader.setOnClickListener(addPetListener);
        binding.btnEmptyAddPet.setOnClickListener(addPetListener);
        binding.btnRetryPets.setOnClickListener(v -> loadMyPets());

        binding.tvSeeAllClinics.setOnClickListener(openBookingFlow);
        binding.btnRetryClinics.setOnClickListener(v -> loadClinics());

        // Bottom Navigation (Phương án B: Trang chủ · Phòng khám · Lịch hẹn · Thú cưng)
        binding.navHome.setOnClickListener(v ->
                binding.scrollContent.smoothScrollTo(0, 0));

        binding.navClinics.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            intent.putExtra(ClinicListActivity.EXTRA_TAB_MODE, true);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        binding.navAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyAppointmentsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        binding.navPets.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyPetsActivity.class);
            intent.putExtra(MyPetsActivity.EXTRA_BOOKING_MODE, false);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void refreshUserProfile(boolean showDialogAfterLoad) {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            goToLogin();
            return;
        }

        ApiClient.getAuthApi().getCurrentUser("Bearer " + token)
                .enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(Call<UserProfileResponse> call,
                                           Response<UserProfileResponse> response) {
                        if (isFinishing() || isDestroyed()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            UserProfileResponse profile = response.body();
                            tokenManager.saveAuthData(
                                    token,
                                    profile.getId(),
                                    profile.getFullName(),
                                    profile.getEmail(),
                                    profile.getRole(),
                                    profile.getClinicId()
                            );
                            tokenManager.saveUserPhone(profile.getPhone());
                            displayUserInfo();
                            if (showDialogAfterLoad) {
                                showAccountDialog();
                            }
                        } else if (response.code() == 401 || response.code() == 403) {
                            handleUnauthorizedOnce();
                        } else if (showDialogAfterLoad) {
                            showProfileErrorDialog("Không thể tải thông tin tài khoản. Vui lòng thử lại.", true);
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        if (showDialogAfterLoad) {
                            showProfileErrorDialog("Lỗi kết nối mạng. Vui lòng kiểm tra kết nối và tải lại.", true);
                        }
                    }
                });
    }

    private void loadUpcomingAppointment() {
        binding.layoutUpcomingLoading.setVisibility(View.VISIBLE);
        binding.cardUpcomingAppointment.setVisibility(View.GONE);
        binding.layoutUpcomingEmpty.setVisibility(View.GONE);
        binding.layoutUpcomingError.setVisibility(View.GONE);

        ApiClient.getAppointmentApi().getMyAppointments()
                .enqueue(new Callback<List<AppointmentResponse>>() {
                    @Override
                    public void onResponse(Call<List<AppointmentResponse>> call,
                                           Response<List<AppointmentResponse>> response) {
                        if (isFinishing() || isDestroyed()) return;
                        binding.layoutUpcomingLoading.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            AppointmentResponse nearest = pickNearestUpcoming(response.body());
                            if (nearest != null) {
                                bindUpcomingCard(nearest);
                                binding.cardUpcomingAppointment.setVisibility(View.VISIBLE);
                            } else {
                                binding.layoutUpcomingEmpty.setVisibility(View.VISIBLE);
                            }
                        } else if (response.code() == 401 || response.code() == 403) {
                            handleUnauthorizedOnce();
                        } else {
                            binding.layoutUpcomingError.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        binding.layoutUpcomingLoading.setVisibility(View.GONE);
                        binding.layoutUpcomingError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private AppointmentResponse pickNearestUpcoming(List<AppointmentResponse> all) {
        List<AppointmentResponse> active = new ArrayList<>();
        String todayIso = LocalDate.now().toString();

        for (AppointmentResponse res : all) {
            String status = res.getStatus();
            boolean isActive = "PENDING".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status);
            if (!isActive) continue;
            if (res.getSlotDate() != null && res.getSlotDate().compareTo(todayIso) < 0) {
                continue;
            }
            active.add(res);
        }

        if (active.isEmpty()) {
            // Nếu tất cả lịch PENDING/CONFIRMED đều chưa cập nhật ngày mới, vẫn hiển thị lịch chờ xử lý gần nhất
            for (AppointmentResponse res : all) {
                String status = res.getStatus();
                if ("PENDING".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status)) {
                    active.add(res);
                }
            }
        }

        if (active.isEmpty()) return null;

        active.sort(Comparator
                .comparing((AppointmentResponse r) -> r.getSlotDate() != null ? r.getSlotDate() : "9999-99-99")
                .thenComparing(r -> r.getStartTime() != null ? r.getStartTime() : "99:99"));
        return active.get(0);
    }

    private void bindUpcomingCard(AppointmentResponse res) {
        AppointmentItem item = res.toAppointmentItem();

        binding.ivUpcomingPetAvatar.setImageResource(item.getPetImgRes());
        binding.tvUpcomingService.setText(res.getServiceName() != null ? res.getServiceName() : item.getService());

        String petPart = res.getPetName() != null && !res.getPetName().isEmpty() ? res.getPetName() : item.getPet();
        String clinicPart = res.getClinicName() != null && !res.getClinicName().isEmpty() ? res.getClinicName() : item.getClinic();
        binding.tvUpcomingPetClinic.setText(petPart + " · " + clinicPart);

        binding.tvUpcomingDateTime.setText(formatReadableDateTime(res.getSlotDate(), res.getStartTime(), res.getEndTime()));

        if ("CONFIRMED".equalsIgnoreCase(res.getStatus())) {
            binding.viewUpcomingAccent.setBackgroundColor(ContextCompat.getColor(this, R.color.ds_success));
            binding.badgeUpcomingStatus.setBackgroundResource(R.drawable.bg_badge_status_success_flat);
            binding.ivUpcomingStatusIcon.setImageResource(R.drawable.ic_check_circle);
            binding.ivUpcomingStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_success_ink));
            binding.tvUpcomingStatus.setText("Đã xác nhận");
            binding.tvUpcomingStatus.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));
        } else {
            binding.viewUpcomingAccent.setBackgroundColor(ContextCompat.getColor(this, R.color.ds_warning));
            binding.badgeUpcomingStatus.setBackgroundResource(R.drawable.bg_badge_status_warning_flat);
            binding.ivUpcomingStatusIcon.setImageResource(R.drawable.ic_clock);
            binding.ivUpcomingStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_warning_ink));
            binding.tvUpcomingStatus.setText("Chờ xác nhận");
            binding.tvUpcomingStatus.setTextColor(ContextCompat.getColor(this, R.color.status_warning_ink));
        }

        View.OnClickListener detailClick = v -> {
            Intent intent = new Intent(this, AppointmentDetailsActivity.class);
            intent.putExtra(AppointmentDetailsActivity.EXTRA_APPT, item);
            intent.putExtra(AppointmentDetailsActivity.EXTRA_REF, item.getRef());
            startActivity(intent);
        };
        binding.cardUpcomingAppointment.setOnClickListener(detailClick);
        binding.btnUpcomingDetail.setOnClickListener(detailClick);
    }

    private String formatReadableDateTime(String slotDate, String startTime, String endTime) {
        String dateDisplay = slotDate != null ? slotDate : "";
        try {
            if (slotDate != null && slotDate.length() >= 10) {
                LocalDate parsed = LocalDate.parse(slotDate.substring(0, 10));
                String dow;
                switch (parsed.getDayOfWeek()) {
                    case MONDAY: dow = "Thứ 2"; break;
                    case TUESDAY: dow = "Thứ 3"; break;
                    case WEDNESDAY: dow = "Thứ 4"; break;
                    case THURSDAY: dow = "Thứ 5"; break;
                    case FRIDAY: dow = "Thứ 6"; break;
                    case SATURDAY: dow = "Thứ 7"; break;
                    default: dow = "CN"; break;
                }
                dateDisplay = String.format(Locale.US, "%s, %02d/%02d/%d",
                        dow, parsed.getDayOfMonth(), parsed.getMonthValue(), parsed.getYear());
            }
        } catch (Exception ignored) {
        }

        String startShort = (startTime != null && startTime.length() >= 5) ? startTime.substring(0, 5) : (startTime != null ? startTime : "");
        String endShort = (endTime != null && endTime.length() >= 5) ? endTime.substring(0, 5) : "";

        if (!startShort.isEmpty() && !endShort.isEmpty()) {
            return dateDisplay + " · " + startShort + " – " + endShort;
        } else if (!startShort.isEmpty()) {
            return dateDisplay + " · " + startShort;
        }
        return dateDisplay;
    }

    private void loadMyPets() {
        binding.layoutPetsLoading.setVisibility(View.VISIBLE);
        binding.scrollHomePets.setVisibility(View.GONE);
        binding.layoutPetsEmpty.setVisibility(View.GONE);
        binding.layoutPetsError.setVisibility(View.GONE);

        ApiClient.getPetApi().getMyPets().enqueue(new Callback<List<PetResponse>>() {
            @Override
            public void onResponse(Call<List<PetResponse>> call, Response<List<PetResponse>> response) {
                if (isFinishing() || isDestroyed()) return;
                binding.layoutPetsLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<PetResponse> pets = response.body();
                    if (pets.isEmpty()) {
                        binding.layoutPetsEmpty.setVisibility(View.VISIBLE);
                    } else {
                        renderPetsRail(pets);
                        binding.scrollHomePets.setVisibility(View.VISIBLE);
                    }
                } else if (response.code() == 401 || response.code() == 403) {
                    handleUnauthorizedOnce();
                } else {
                    binding.layoutPetsError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<PetResponse>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                binding.layoutPetsLoading.setVisibility(View.GONE);
                binding.layoutPetsError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void renderPetsRail(List<PetResponse> pets) {
        binding.containerHomePets.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (PetResponse res : pets) {
            PetItem mapped = res.toPetItem();
            ItemHomePetCardBinding card = ItemHomePetCardBinding.inflate(inflater, binding.containerHomePets, false);

            card.ivHomePetAvatar.setImageResource(mapped.getImageResId());
            card.tvHomePetName.setText(res.getName() != null && !res.getName().trim().isEmpty() ? res.getName().trim() : "Bé cưng");

            String rawSpecies = res.getSpecies() != null && !res.getSpecies().trim().isEmpty() ? res.getSpecies().trim() : "Thú cưng";
            card.tvHomePetSpecies.setText(rawSpecies);

            String lowerSpec = rawSpecies.toLowerCase(Locale.ROOT);
            if (lowerSpec.contains("mèo") || lowerSpec.contains("meo") || lowerSpec.contains("cat")) {
                card.tvHomePetSpecies.setBackgroundResource(R.drawable.bg_pill_mint);
                card.tvHomePetSpecies.setTextColor(ContextCompat.getColor(this, R.color.ds_mint_ink));
            } else {
                card.tvHomePetSpecies.setBackgroundResource(R.drawable.bg_pill_peach);
                card.tvHomePetSpecies.setTextColor(ContextCompat.getColor(this, R.color.ds_peach_ink));
            }

            UiMotionUtils.attachSpringTouch(card.getRoot());
            card.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, PetDetailsActivity.class);
                intent.putExtra(PetDetailsActivity.EXTRA_PET, mapped);
                startActivity(intent);
            });

            binding.containerHomePets.addView(card.getRoot());
        }

        // Thẻ cuối cuộn ngang: Thêm thú cưng nhanh
        binding.containerHomePets.addView(createTrailingAddPetCard());
    }

    private View createTrailingAddPetCard() {
        float density = getResources().getDisplayMetrics().density;
        int heightPx = Math.round(80 * density);
        int iconBoxPx = Math.round(44 * density);
        int iconPx = Math.round(18 * density);

        LinearLayout addCard = new LinearLayout(this);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, heightPx);
        cardLp.topMargin = Math.round(2 * density);
        cardLp.bottomMargin = Math.round(4 * density);
        addCard.setLayoutParams(cardLp);
        addCard.setOrientation(LinearLayout.HORIZONTAL);
        addCard.setGravity(Gravity.CENTER_VERTICAL);
        addCard.setBackgroundResource(R.drawable.bg_add_pet_pill_borderless);
        addCard.setClickable(true);
        addCard.setFocusable(true);
        addCard.setPadding(
                Math.round(14 * density),
                Math.round(12 * density),
                Math.round(18 * density),
                Math.round(12 * density)
        );

        LinearLayout circle = new LinearLayout(this);
        LinearLayout.LayoutParams circleLp = new LinearLayout.LayoutParams(iconBoxPx, iconBoxPx);
        circle.setLayoutParams(circleLp);
        circle.setGravity(Gravity.CENTER);
        circle.setBackgroundResource(R.drawable.bg_home_icon_btn_white);

        ImageView plusIcon = new ImageView(this);
        plusIcon.setLayoutParams(new LinearLayout.LayoutParams(iconPx, iconPx));
        plusIcon.setImageResource(R.drawable.ic_plus);
        plusIcon.setColorFilter(ContextCompat.getColor(this, R.color.ds_primary_dark));
        circle.addView(plusIcon);

        TextView label = new TextView(this);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelLp.setMarginStart(Math.round(10 * density));
        label.setLayoutParams(labelLp);
        label.setText("Thêm bé mới");
        label.setTextSize(12.5f);
        label.setIncludeFontPadding(false);
        label.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.open_sans_medium));
        label.setTextColor(ContextCompat.getColor(this, R.color.ds_primary_dark));

        addCard.addView(circle);
        addCard.addView(label);

        UiMotionUtils.attachSpringTouch(addCard);
        addCard.setOnClickListener(v -> startActivity(new Intent(this, AddPetActivity.class)));
        return addCard;
    }

    private void loadClinics() {
        binding.layoutClinicsLoading.setVisibility(View.VISIBLE);
        binding.containerHomeClinics.setVisibility(View.GONE);
        binding.layoutClinicsEmpty.setVisibility(View.GONE);
        binding.layoutClinicsError.setVisibility(View.GONE);

        ApiClient.getClinicApi().getAllClinics(null).enqueue(new Callback<List<ClinicResponse>>() {
            @Override
            public void onResponse(Call<List<ClinicResponse>> call, Response<List<ClinicResponse>> response) {
                if (isFinishing() || isDestroyed()) return;
                binding.layoutClinicsLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<ClinicResponse> clinics = response.body();
                    if (clinics.isEmpty()) {
                        binding.layoutClinicsEmpty.setVisibility(View.VISIBLE);
                    } else {
                        renderClinicsList(clinics);
                        binding.containerHomeClinics.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.layoutClinicsError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<ClinicResponse>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                binding.layoutClinicsLoading.setVisibility(View.GONE);
                binding.layoutClinicsError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void renderClinicsList(List<ClinicResponse> clinics) {
        binding.containerHomeClinics.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        int count = Math.min(clinics.size(), MAX_HOME_CLINICS);
        for (int i = 0; i < count; i++) {
            ClinicResponse res = clinics.get(i);
            ClinicItem clinicItem = res.toClinicItem();

            ItemHomeClinicCardBinding card = ItemHomeClinicCardBinding.inflate(inflater, binding.containerHomeClinics, false);
            card.ivHomeClinicImage.setImageResource(clinicItem.getImageResId());
            card.tvHomeClinicName.setText(res.getName() != null ? res.getName() : "Phòng khám thú y");

            String addr = res.getAddress();
            if (addr == null || addr.trim().isEmpty()) {
                addr = res.getArea() != null ? res.getArea() : "Chưa cập nhật địa chỉ";
            }
            card.tvHomeClinicAddress.setText(addr);

            if (res.isActive()) {
                card.tvHomeClinicStatus.setText("● Đang hoạt động");
                card.tvHomeClinicStatus.setBackgroundResource(R.drawable.bg_badge_status_success_flat);
                card.tvHomeClinicStatus.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));
            } else {
                card.tvHomeClinicStatus.setText("● Tạm ngưng");
                card.tvHomeClinicStatus.setBackgroundResource(R.drawable.bg_badge_status_neutral_flat);
                card.tvHomeClinicStatus.setTextColor(ContextCompat.getColor(this, R.color.status_neutral_ink));
            }

            if (res.getWorkingHourNote() != null && !res.getWorkingHourNote().trim().isEmpty()) {
                card.tvHomeClinicHours.setText(res.getWorkingHourNote().trim());
                card.layoutHomeClinicHours.setVisibility(View.VISIBLE);
            } else {
                card.layoutHomeClinicHours.setVisibility(View.GONE);
            }

            UiMotionUtils.attachSpringTouch(card.getRoot());
            card.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, ClinicDetailsActivity.class);
                intent.putExtra(ClinicDetailsActivity.EXTRA_CLINIC, clinicItem);
                startActivity(intent);
            });

            binding.containerHomeClinics.addView(card.getRoot());
        }
    }

    private void handleUnauthorizedOnce() {
        if (authRedirected) return;
        authRedirected = true;
        Toast.makeText(this,
                "Phiên đăng nhập không hợp lệ hoặc đã hết hạn, vui lòng đăng nhập lại",
                Toast.LENGTH_LONG).show();
        tokenManager.clear();
        goToLogin();
    }

    private void openProfileScreen() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void showProfileErrorDialog(String message, boolean allowRetry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage(message)
                .setNegativeButton("Đóng", null);
        if (allowRetry) {
            builder.setPositiveButton("Tải lại", (dialog, which) -> refreshUserProfile(true));
        }
        builder.show();
    }

    private void showAccountDialog() {
        String phone = tokenManager.getUserPhone();
        String phoneDisplay = (phone != null && !phone.isEmpty()) ? phone : "Chưa cập nhật";
        String role = tokenManager.getUserRole();
        String roleDisplay = "STAFF".equalsIgnoreCase(role) ? "Nhân viên phòng khám" : "Khách hàng";

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Thông tin tài khoản")
                .setMessage("Họ và tên: " + tokenManager.getUserName()
                        + "\nEmail: " + tokenManager.getUserEmail()
                        + "\nSố điện thoại: " + phoneDisplay
                        + "\nVai trò: " + roleDisplay)
                .setPositiveButton("Đóng", null)
                .setNegativeButton("Đăng xuất", (dialog, which) -> {
                    tokenManager.clear();
                    goToLogin();
                });

        if ("STAFF".equalsIgnoreCase(role)) {
            builder.setNeutralButton("Bảng điều khiển nhân viên", (dialog, which) -> {
                Intent intent = new Intent(this, StaffDashboardActivity.class);
                startActivity(intent);
            });
        }

        builder.show();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
