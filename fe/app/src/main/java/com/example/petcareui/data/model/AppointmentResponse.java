package com.example.petcareui.data.model;

import com.example.petcareui.R;

import java.io.Serializable;

public class AppointmentResponse implements Serializable {
    private Long id;
    private String status;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long petId;
    private String petName;
    private String petSpecies;
    private Long clinicId;
    private String clinicName;
    private String clinicAddress;
    private Long serviceId;
    private String serviceName;
    private Integer durationMinutes;
    private Long slotId;
    private String slotDate;
    private String startTime;
    private String endTime;

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public Long getPetId() { return petId; }
    public String getPetName() { return petName; }
    public String getPetSpecies() { return petSpecies; }
    public Long getClinicId() { return clinicId; }
    public String getClinicName() { return clinicName; }
    public String getClinicAddress() { return clinicAddress; }
    public Long getServiceId() { return serviceId; }
    public String getServiceName() { return serviceName; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public Long getSlotId() { return slotId; }
    public String getSlotDate() { return slotDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    public AppointmentItem toAppointmentItem() {
        boolean isCat = petSpecies != null && (petSpecies.toLowerCase().contains("mèo") || petSpecies.toLowerCase().contains("cat"));
        int avatar = isCat ? R.drawable.img_pet_bong : R.drawable.img_pet_mochi;
        String formattedRef = String.format("PC-%04d", id != null ? id : 1L);
        String formattedDate = slotDate != null ? slotDate : "";
        String formattedTime = startTime != null ? (startTime.length() >= 5 ? startTime.substring(0, 5) : startTime) : "";
        return new AppointmentItem(
                id,
                formattedRef,
                clinicName != null ? clinicName : "Phòng khám thú y",
                clinicAddress != null ? clinicAddress : "TP.HCM",
                petName != null ? petName : "Bé cưng",
                avatar,
                serviceName != null ? serviceName : "Dịch vụ khám",
                formattedDate,
                formattedTime,
                status != null ? status : "PENDING",
                "Liên hệ",
                "Vừa đặt",
                ""
        );
    }

    public StaffAppointmentItem toStaffAppointmentItem() {
        boolean isCat = petSpecies != null && (petSpecies.toLowerCase().contains("mèo") || petSpecies.toLowerCase().contains("cat"));
        int avatar = isCat ? R.drawable.img_pet_bong : R.drawable.img_pet_mochi;
        String formattedRef = String.format("PC-%04d", id != null ? id : 1L);
        String formattedDate = slotDate != null ? slotDate : "";
        String formattedTime = startTime != null ? (startTime.length() >= 5 ? startTime.substring(0, 5) : startTime) : "";
        String durationStr = (durationMinutes != null ? durationMinutes : 30) + " phút";
        StaffAppointmentItem item = new StaffAppointmentItem(
                formattedRef,
                petName != null ? petName : "Bé cưng",
                avatar,
                petSpecies != null ? petSpecies : "Thú cưng",
                customerName != null && !customerName.isEmpty() ? customerName : "Khách hàng",
                customerPhone != null && !customerPhone.isEmpty() ? customerPhone : "+84 90 123 4567",
                "Khách hàng",
                serviceName != null ? serviceName : "Khám tổng quát",
                durationStr,
                formattedDate,
                formattedTime,
                status != null ? status : "PENDING",
                "250.000 ₫",
                "Hôm nay",
                ""
        );
        item.setId(id);
        return item;
    }
}
