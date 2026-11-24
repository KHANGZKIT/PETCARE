package com.example.petcareui.data.dto.response;

import com.example.petcareui.R;
import com.example.petcareui.data.model.AppointmentItem;
import com.example.petcareui.data.model.StaffAppointmentItem;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AppointmentResponse implements Serializable {
    private Long id;
    private String status;
    private String notes;
    private String createdAt;
    private String updatedAt;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long petId;
    private String petName;
    private String petSpecies;
    private String petBreed;
    private String petGender;
    private String petAge;
    private Long clinicId;
    private String clinicName;
    private String clinicAddress;
    private String clinicArea;
    private String clinicPhone;
    private Double clinicLatitude;
    private Double clinicLongitude;
    private Long serviceId;
    private String serviceName;
    private Integer durationMinutes;
    private Long slotId;
    private String slotDate;
    private String startTime;
    private String endTime;

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public Long getPetId() { return petId; }
    public String getPetName() { return petName; }
    public String getPetSpecies() { return petSpecies; }
    public String getPetBreed() { return petBreed; }
    public String getPetGender() { return petGender; }
    public String getPetAge() { return petAge; }
    public Long getClinicId() { return clinicId; }
    public String getClinicName() { return clinicName; }
    public String getClinicAddress() { return clinicAddress; }
    public String getClinicArea() { return clinicArea; }
    public String getClinicPhone() { return clinicPhone; }
    public Double getClinicLatitude() { return clinicLatitude; }
    public Double getClinicLongitude() { return clinicLongitude; }
    public Long getServiceId() { return serviceId; }
    public String getServiceName() { return serviceName; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public Long getSlotId() { return slotId; }
    public String getSlotDate() { return slotDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    private int resolvePetAvatar() {
        String lowerSpec = petSpecies != null ? petSpecies.toLowerCase(Locale.ROOT) : "";
        String lowerName = petName != null ? petName.toLowerCase(Locale.ROOT) : "";
        if (lowerSpec.contains("mèo") || lowerSpec.contains("cat") || lowerName.contains("bông") || lowerName.contains("mimi") || lowerName.contains("miu")) {
            return R.drawable.img_pet_bong;
        }
        return R.drawable.mascot_corgi_running;
    }

    public static String formatRelativeTime(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.trim().isEmpty()) {
            return "";
        }
        try {
            ZonedDateTime target = OffsetDateTime.parse(isoTimestamp.trim()).atZoneSameInstant(ZoneId.systemDefault());
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            long minutes = Math.max(0, Duration.between(target, now).toMinutes());
            if (minutes < 1) return "Vừa đặt";
            if (minutes < 60) return minutes + " phút trước";
            long hours = minutes / 60;
            if (hours < 24 && target.toLocalDate().equals(now.toLocalDate())) {
                return hours + " giờ trước";
            }
            if (target.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
                return "Hôm qua · " + target.format(DateTimeFormatter.ofPattern("HH:mm", Locale.US));
            }
            return target.format(DateTimeFormatter.ofPattern("dd/MM · HH:mm", Locale.US));
        } catch (Exception ignored) {
            return "";
        }
    }

    public AppointmentItem toAppointmentItem() {
        int avatar = resolvePetAvatar();
        String formattedRef = String.format(Locale.US, "PC-%04d", id != null ? id : 1L);
        String formattedDate = slotDate != null ? slotDate : "";
        String formattedTime = startTime != null ? (startTime.length() >= 5 ? startTime.substring(0, 5) : startTime) : "";
        String areaOrAddr = (clinicArea != null && !clinicArea.trim().isEmpty())
                ? clinicArea.trim()
                : (clinicAddress != null ? clinicAddress : "Hà Nội");
        String relRequested = formatRelativeTime(createdAt);

        AppointmentItem item = new AppointmentItem(
                id,
                formattedRef,
                clinicName != null ? clinicName : "Phòng khám thú y",
                areaOrAddr,
                petName != null ? petName : "Bé cưng",
                avatar,
                serviceName != null ? serviceName : "Dịch vụ khám",
                formattedDate,
                formattedTime,
                status != null ? status : "PENDING",
                resolvePriceByServiceName(serviceName),
                !relRequested.isEmpty() ? relRequested : "Đã ghi nhận",
                notes != null ? notes.trim() : ""
        );
        item.setClinicId(clinicId);
        item.setClinicAddress(clinicAddress);
        item.setClinicPhone(clinicPhone);
        item.setClinicLatitude(clinicLatitude);
        item.setClinicLongitude(clinicLongitude);
        item.setPetId(petId);
        item.setPetSpecies(petSpecies);
        item.setPetBreed(petBreed);
        item.setPetGender(petGender);
        item.setPetAge(petAge);
        item.setDurationMinutes(durationMinutes);
        item.setCreatedAt(createdAt);
        item.setUpdatedAt(updatedAt);
        return item;
    }

    private String resolvePriceByServiceName(String name) {
        if (name == null) return "250.000 ₫";
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.contains("tổng quát")) return "180.000 ₫";
        if (lower.contains("tiêm") || lower.contains("vắc") || lower.contains("vaccine")) return "250.000 ₫";
        if (lower.contains("răng") || lower.contains("vôi")) return "600.000 ₫";
        if (lower.contains("tắm") || lower.contains("spa") || lower.contains("cắt")) return "320.000 ₫";
        return "250.000 ₫";
    }

    public StaffAppointmentItem toStaffAppointmentItem() {
        int avatar = resolvePetAvatar();
        String formattedRef = String.format(Locale.US, "PC-%04d", id != null ? id : 1L);
        String formattedDate = slotDate != null ? slotDate : "";
        String formattedTime = startTime != null ? (startTime.length() >= 5 ? startTime.substring(0, 5) : startTime) : "";
        String durationStr = (durationMinutes != null ? durationMinutes : 30) + " phút";

        StringBuilder petMeta = new StringBuilder();
        if (petSpecies != null && !petSpecies.trim().isEmpty()) petMeta.append(petSpecies.trim());
        if (petBreed != null && !petBreed.trim().isEmpty()) {
            if (petMeta.length() > 0) petMeta.append(" · ");
            petMeta.append(petBreed.trim());
        }
        if (petAge != null && !petAge.trim().isEmpty()) {
            if (petMeta.length() > 0) petMeta.append(" · ");
            petMeta.append(petAge.trim());
        }
        if (petGender != null && !petGender.trim().isEmpty()) {
            if (petMeta.length() > 0) petMeta.append(" · ");
            petMeta.append(petGender.trim());
        }

        String relRequested = formatRelativeTime(createdAt);
        StaffAppointmentItem item = new StaffAppointmentItem(
                formattedRef,
                petName != null ? petName : "Bé cưng",
                avatar,
                petMeta.length() > 0 ? petMeta.toString() : "Thú cưng",
                customerName != null && !customerName.isEmpty() ? customerName : "Khách hàng",
                customerPhone != null && !customerPhone.isEmpty() ? customerPhone : "",
                "Khách hàng",
                serviceName != null ? serviceName : "Khám tổng quát",
                durationStr,
                formattedDate,
                formattedTime,
                status != null ? status : "PENDING",
                resolvePriceByServiceName(serviceName),
                !relRequested.isEmpty() ? relRequested : "Hôm nay",
                notes != null ? notes.trim() : ""
        );
        item.setId(id);
        return item;
    }
}
