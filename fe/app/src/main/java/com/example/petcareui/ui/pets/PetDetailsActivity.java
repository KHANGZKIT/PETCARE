package com.example.petcareui.ui.pets;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.dto.response.AppointmentResponse;
import com.example.petcareui.data.dto.response.PetResponse;
import com.example.petcareui.data.model.AppointmentItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.databinding.ActivityPetDetailsBinding;
import com.example.petcareui.databinding.ItemPetHistoryRowBinding;
import com.example.petcareui.ui.appointments.AppointmentDetailsActivity;
import com.example.petcareui.ui.appointments.MyAppointmentsActivity;
import com.example.petcareui.ui.clinic.ClinicListActivity;
import com.example.petcareui.utils.UiMotionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PetDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PET = "extra_pet";
    public static final String EXTRA_SCROLL_TO_HISTORY = "extra_scroll_to_history";

    private ActivityPetDetailsBinding binding;
    private PetItem pet;
    private boolean scrollToHistoryOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPetDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.ds_home_bg));
        UiMotionUtils.applyWindowInsets(binding.layoutPetDetailsRoot, binding.layoutTopBar, binding.layoutBottomCta);

        extractIntentData();
        if (pet == null) {
            finish();
            return;
        }

        setupListeners();
        bindPetInfo();
        loadPetHistoryFromApi();

        UiMotionUtils.animateEntranceSections(
                binding.sectionHeroAvatar,
                binding.cardBasicInfo,
                binding.cardHealthHistory
        );
        UiMotionUtils.attachSpringTouch(
                binding.btnBack,
                binding.btnEditPetTop,
                binding.btnBookForPet
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPetFromApi();
        loadPetHistoryFromApi();
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_PET)) {
            try {
                pet = (PetItem) getIntent().getSerializableExtra(EXTRA_PET);
            } catch (Exception ignored) {
            }
        }
        scrollToHistoryOnce = getIntent().getBooleanExtra(EXTRA_SCROLL_TO_HISTORY, false);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnEditPetTop.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPetActivity.class);
            intent.putExtra(AddPetActivity.EXTRA_EDIT_PET, pet);
            startActivity(intent);
        });

        binding.btnSeeAllHistory.setOnClickListener(v ->
                startActivity(new Intent(this, MyAppointmentsActivity.class)));

        binding.btnBookForPet.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            intent.putExtra(ClinicListActivity.EXTRA_TAB_MODE, false);
            intent.putExtra(ClinicListActivity.EXTRA_PRESELECTED_PET, pet);
            startActivity(intent);
        });
    }

    private void bindPetInfo() {
        if (pet == null) return;

        String petName = pet.getName() != null && !pet.getName().trim().isEmpty()
                ? pet.getName().trim() : "Bé cưng";
        String rawSpecies = pet.getRawSpecies() != null && !pet.getRawSpecies().trim().isEmpty()
                ? pet.getRawSpecies().trim() : "Thú cưng";

        binding.ivPetHeroAvatar.setImageResource(pet.getImageResId());

        binding.tvPetHeroName.setText(petName);

        String age = pet.getAge() != null ? pet.getAge().trim() : "";
        String sex = pet.getSex() != null ? pet.getSex().replace("♂", "").replace("♀", "").trim() : "";
        StringBuilder heroSub = new StringBuilder(rawSpecies);
        if (!age.isEmpty()) heroSub.append(" · ").append(age);
        if (!sex.isEmpty()) heroSub.append(" · ").append(sex);
        binding.tvPetHeroSpecies.setText(heroSub.toString());

        binding.tvBookForPetText.setText("Đặt lịch khám cho " + petName);

        // Card 1: Thông tin cơ bản (chỉ hiển thị dữ liệu thật từ API, ẩn dòng nếu rỗng/null)
        binding.tvBasicNameValue.setText(petName);

        String baseSpecies = extractBaseSpecies(rawSpecies);
        String breedVal = pet.getBreed() != null && !pet.getBreed().trim().isEmpty()
                ? pet.getBreed().trim()
                : extractBreedOrNote(rawSpecies, baseSpecies);

        binding.tvBasicSpeciesValue.setText(baseSpecies);

        if (breedVal != null && !breedVal.isEmpty()) {
            binding.tvBasicBreedNoteValue.setText(breedVal);
            binding.dividerBasicBreedNote.setVisibility(View.VISIBLE);
            binding.rowBasicBreedNote.setVisibility(View.VISIBLE);
        } else {
            binding.dividerBasicBreedNote.setVisibility(View.GONE);
            binding.rowBasicBreedNote.setVisibility(View.GONE);
        }

        if (!sex.isEmpty()) {
            binding.tvBasicGenderValue.setText(sex);
            binding.dividerBasicGender.setVisibility(View.VISIBLE);
            binding.rowBasicGender.setVisibility(View.VISIBLE);
        } else {
            binding.dividerBasicGender.setVisibility(View.GONE);
            binding.rowBasicGender.setVisibility(View.GONE);
        }

        if (!age.isEmpty()) {
            binding.tvBasicAgeValue.setText(age);
            binding.dividerBasicAge.setVisibility(View.VISIBLE);
            binding.rowBasicAge.setVisibility(View.VISIBLE);
        } else {
            binding.dividerBasicAge.setVisibility(View.GONE);
            binding.rowBasicAge.setVisibility(View.GONE);
        }

        String weightText = pet.getWeight() != null ? pet.getWeight().trim() : "";
        if (!weightText.isEmpty()) {
            binding.tvBasicWeightValue.setText(weightText);
            binding.dividerBasicWeight.setVisibility(View.VISIBLE);
            binding.rowBasicWeight.setVisibility(View.VISIBLE);
        } else {
            binding.dividerBasicWeight.setVisibility(View.GONE);
            binding.rowBasicWeight.setVisibility(View.GONE);
        }

        String notesText = pet.getNotes() != null ? pet.getNotes().trim() : "";
        if (!notesText.isEmpty()) {
            binding.tvBasicNotesValue.setText(notesText);
            binding.dividerBasicNotes.setVisibility(View.VISIBLE);
            binding.rowBasicNotes.setVisibility(View.VISIBLE);
        } else {
            binding.dividerBasicNotes.setVisibility(View.GONE);
            binding.rowBasicNotes.setVisibility(View.GONE);
        }
    }

    private String extractBaseSpecies(String raw) {
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.startsWith("chó")) return "Chó";
        if (lower.startsWith("mèo")) return "Mèo";
        return raw;
    }

    private String extractBreedOrNote(String raw, String baseSpecies) {
        if (raw.equalsIgnoreCase(baseSpecies)) {
            return null;
        }
        if (raw.length() > baseSpecies.length() && raw.regionMatches(true, 0, baseSpecies, 0, baseSpecies.length())) {
            String rest = raw.substring(baseSpecies.length()).replaceFirst("^[\\s·\\-,:]+", "").trim();
            return rest.isEmpty() ? null : rest;
        }
        return null;
    }

    private void refreshPetFromApi() {
        Long targetId = pet != null ? pet.getNumericId() : null;
        if (targetId == null) return;

        ApiClient.getPetApi().getMyPets().enqueue(new Callback<List<PetResponse>>() {
            @Override
            public void onResponse(Call<List<PetResponse>> call, Response<List<PetResponse>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    boolean found = false;
                    for (PetResponse res : response.body()) {
                        if (targetId.equals(res.getId())) {
                            pet = res.toPetItem();
                            bindPetInfo();
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PetResponse>> call, Throwable t) {
            }
        });
    }

    private void loadPetHistoryFromApi() {
        binding.pbHistoryLoading.setVisibility(View.VISIBLE);
        binding.containerPetHistory.setVisibility(View.GONE);
        binding.tvHistoryEmpty.setVisibility(View.GONE);

        Long targetPetId = pet != null ? pet.getNumericId() : null;
        String targetName = pet != null && pet.getName() != null ? pet.getName().trim() : "";

        ApiClient.getAppointmentApi().getMyAppointments().enqueue(new Callback<List<AppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                if (isFinishing() || isDestroyed()) return;
                binding.pbHistoryLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<AppointmentResponse> matched = new ArrayList<>();
                    for (AppointmentResponse res : response.body()) {
                        boolean idMatches = targetPetId != null && targetPetId.equals(res.getPetId());
                        boolean nameMatches = targetPetId == null && !targetName.isEmpty()
                                && res.getPetName() != null && targetName.equalsIgnoreCase(res.getPetName().trim());
                        if (idMatches || nameMatches) {
                            matched.add(res);
                        }
                    }

                    matched.sort(Comparator
                            .comparing((AppointmentResponse r) -> r.getSlotDate() != null ? r.getSlotDate() : "", Comparator.reverseOrder())
                            .thenComparing(r -> r.getStartTime() != null ? r.getStartTime() : "", Comparator.reverseOrder()));

                    if (matched.isEmpty()) {
                        binding.tvHistoryEmpty.setVisibility(View.VISIBLE);
                    } else {
                        renderHistoryList(matched);
                        binding.containerPetHistory.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.tvHistoryEmpty.setText("Không thể tải lịch sử khám lúc này.");
                    binding.tvHistoryEmpty.setVisibility(View.VISIBLE);
                }

                if (scrollToHistoryOnce) {
                    scrollToHistoryOnce = false;
                    binding.scrollPetDetails.post(() ->
                            binding.scrollPetDetails.smoothScrollTo(0, binding.cardHealthHistory.getTop()));
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                binding.pbHistoryLoading.setVisibility(View.GONE);
                binding.tvHistoryEmpty.setText("Không thể kết nối máy chủ để tải lịch sử khám.");
                binding.tvHistoryEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void renderHistoryList(List<AppointmentResponse> list) {
        binding.containerPetHistory.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < list.size(); i++) {
            AppointmentResponse res = list.get(i);
            AppointmentItem apptItem = res.toAppointmentItem();

            ItemPetHistoryRowBinding row = ItemPetHistoryRowBinding.inflate(inflater, binding.containerPetHistory, false);
            row.tvHistoryServiceName.setText(res.getServiceName() != null ? res.getServiceName() : apptItem.getService());
            row.tvHistoryDate.setText(formatHistoryDate(res.getSlotDate(), res.getStartTime(), res.getClinicName()));
            applyHistoryStatusChip(row, res.getStatus());

            row.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, AppointmentDetailsActivity.class);
                intent.putExtra(AppointmentDetailsActivity.EXTRA_APPT, apptItem);
                intent.putExtra(AppointmentDetailsActivity.EXTRA_REF, apptItem.getRef());
                startActivity(intent);
            });

            binding.containerPetHistory.addView(row.getRoot());

            if (i < list.size() - 1) {
                View divider = new View(this);
                divider.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        Math.max(1, Math.round(getResources().getDisplayMetrics().density))
                ));
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.ds_divider_hairline));
                binding.containerPetHistory.addView(divider);
            }
        }
    }

    private String formatHistoryDate(String slotDate, String startTime, String clinicName) {
        String datePart = slotDate != null ? slotDate : "";
        try {
            if (slotDate != null && slotDate.length() >= 10) {
                LocalDate d = LocalDate.parse(slotDate.substring(0, 10));
                datePart = String.format(Locale.US, "%02d/%02d/%d", d.getDayOfMonth(), d.getMonthValue(), d.getYear());
            }
        } catch (Exception ignored) {
        }
        String timePart = (startTime != null && startTime.length() >= 5) ? startTime.substring(0, 5) : "";
        StringBuilder sb = new StringBuilder(datePart);
        if (!timePart.isEmpty()) {
            sb.append(" · ").append(timePart);
        }
        if (clinicName != null && !clinicName.trim().isEmpty()) {
            String shortClinic = clinicName.trim();
            if (shortClinic.toLowerCase(Locale.ROOT).startsWith("phòng khám thú y ")) {
                shortClinic = "Thú y " + shortClinic.substring("Phòng khám Thú y ".length()).trim();
            }
            sb.append(" · ").append(shortClinic);
        }
        return sb.toString();
    }

    private void applyHistoryStatusChip(ItemPetHistoryRowBinding row, String status) {
        if ("COMPLETED".equalsIgnoreCase(status)) {
            row.tvHistoryStatusChip.setText("● Hoàn thành");
            row.tvHistoryStatusChip.setTextColor(ContextCompat.getColor(this, R.color.ds_success));
        } else if ("CONFIRMED".equalsIgnoreCase(status)) {
            row.tvHistoryStatusChip.setText("● Đã xác nhận");
            row.tvHistoryStatusChip.setTextColor(ContextCompat.getColor(this, R.color.ds_success));
        } else if ("PENDING".equalsIgnoreCase(status)) {
            row.tvHistoryStatusChip.setText("● Chờ duyệt");
            row.tvHistoryStatusChip.setTextColor(ContextCompat.getColor(this, R.color.ds_warning));
        } else {
            row.tvHistoryStatusChip.setText("● Đã hủy");
            row.tvHistoryStatusChip.setTextColor(ContextCompat.getColor(this, R.color.ds_text_secondary));
        }
    }
}
