package com.example.petcareui.utils;

import com.example.petcareui.R;

import java.util.Locale;

/**
 * Nguồn duy nhất để ánh xạ giá ước tính và biểu tượng của dịch vụ thú y,
 * đảm bảo nhất quán tuyệt đối giữa Chi tiết phòng khám, Chọn dịch vụ, Review và Lịch hẹn.
 */
public final class ServiceDisplayUtils {

    private ServiceDisplayUtils() {
    }

    public static String resolvePriceByName(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            return "250.000 ₫";
        }
        String lower = serviceName.toLowerCase(Locale.ROOT);
        if (lower.contains("cấp cứu") || lower.contains("24/7") || lower.contains("hồi sức")) {
            return "420.000 ₫";
        }
        if (lower.contains("răng") || lower.contains("vôi")) {
            return "600.000 ₫";
        }
        if (lower.contains("siêu âm") || lower.contains("xét nghiệm") || lower.contains("máu") || lower.contains("x-quang")) {
            return "350.000 ₫";
        }
        if (lower.contains("tắm") || lower.contains("spa") || lower.contains("cắt") || lower.contains("lông")) {
            return "320.000 ₫";
        }
        if (lower.contains("tiêm") || lower.contains("vắc") || lower.contains("vaccine")) {
            return "250.000 ₫";
        }
        if (lower.contains("khách sạn") || lower.contains("lưu chuồng")) {
            return "200.000 ₫";
        }
        if (lower.contains("tổng quát") || lower.contains("khám")) {
            return "180.000 ₫";
        }
        return "250.000 ₫";
    }

    public static int resolveIconByName(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            return R.drawable.ic_stethoscope;
        }
        String lower = serviceName.toLowerCase(Locale.ROOT);
        if (lower.contains("cấp cứu") || lower.contains("24/7") || lower.contains("hồi sức")) {
            return R.drawable.ic_heart_pulse;
        }
        if (lower.contains("răng") || lower.contains("vôi")) {
            return R.drawable.ic_sparkles;
        }
        if (lower.contains("siêu âm") || lower.contains("xét nghiệm") || lower.contains("máu") || lower.contains("x-quang")) {
            return R.drawable.ic_shield_check;
        }
        if (lower.contains("tắm") || lower.contains("spa") || lower.contains("cắt") || lower.contains("lông")) {
            return R.drawable.ic_scissors;
        }
        if (lower.contains("tiêm") || lower.contains("vắc") || lower.contains("vaccine")) {
            return R.drawable.ic_syringe;
        }
        if (lower.contains("khách sạn") || lower.contains("lưu chuồng")) {
            return R.drawable.ic_home;
        }
        return R.drawable.ic_stethoscope;
    }
}
