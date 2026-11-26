package com.example.petcareui.ui.appointments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareui.R;
import com.example.petcareui.data.model.AppointmentItem;
import com.example.petcareui.databinding.ItemAppointmentCardBinding;
import com.example.petcareui.utils.UiMotionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    public interface OnAppointmentClickListener {
        void onAppointmentClick(AppointmentItem item);
    }

    private final List<AppointmentItem> items = new ArrayList<>();
    private final OnAppointmentClickListener listener;

    public AppointmentAdapter(OnAppointmentClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AppointmentItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppointmentCardBinding binding = ItemAppointmentCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AppointmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemAppointmentCardBinding binding;

        AppointmentViewHolder(ItemAppointmentCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            UiMotionUtils.attachSpringTouch(binding.getRoot());
        }

        void bind(AppointmentItem item) {
            Context ctx = binding.getRoot().getContext();
            applyBadgeStatus(ctx, binding.tvApptStatus, item.getStatus());

            int avatarRes = item.getPetImgRes();
            binding.ivPetImage.setImageResource(avatarRes);
            if (avatarRes == R.drawable.img_pet_bong) {
                binding.ivPetImage.setPadding(0, 0, 0, 0);
                binding.ivPetImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                int pad = Math.round(4 * ctx.getResources().getDisplayMetrics().density);
                binding.ivPetImage.setPadding(pad, pad, pad, pad);
                binding.ivPetImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            binding.tvServiceTitle.setText(item.getService());
            binding.tvPetAndClinic.setText(item.getPet() + " · " + formatShortClinicName(item.getClinic()));
            binding.tvApptDate.setText(formatNaturalDateTime(item.getDate(), item.getTime()));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAppointmentClick(item);
                }
            });
        }
    }

    private static String formatShortClinicName(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "Phòng khám thú y";
        String s = raw.trim();
        if (s.toLowerCase(Locale.ROOT).startsWith("phòng khám thú y ")) {
            return "Thú y " + s.substring("Phòng khám Thú y ".length()).trim();
        }
        return s;
    }

    private static String formatNaturalDateTime(String rawDate, String rawTime) {
        String datePart = rawDate != null ? rawDate.trim() : "";
        try {
            if (datePart.length() >= 10 && datePart.charAt(4) == '-' && datePart.charAt(7) == '-') {
                LocalDate parsed = LocalDate.parse(datePart.substring(0, 10));
                String dow;
                switch (parsed.getDayOfWeek()) {
                    case MONDAY: dow = "T2"; break;
                    case TUESDAY: dow = "T3"; break;
                    case WEDNESDAY: dow = "T4"; break;
                    case THURSDAY: dow = "T5"; break;
                    case FRIDAY: dow = "T6"; break;
                    case SATURDAY: dow = "T7"; break;
                    default: dow = "CN"; break;
                }
                datePart = String.format(Locale.US, "%s, %02d/%02d",
                        dow, parsed.getDayOfMonth(), parsed.getMonthValue());
            }
        } catch (Exception ignored) {
        }

        String timePart = rawTime != null ? rawTime.trim() : "";
        if (timePart.length() > 5 && timePart.charAt(2) == ':') {
            timePart = timePart.substring(0, 5);
        }

        if (!datePart.isEmpty() && !timePart.isEmpty()) {
            return datePart + " · " + timePart;
        }
        return !datePart.isEmpty() ? datePart : timePart;
    }

    private static void applyBadgeStatus(Context ctx, TextView tvBadge, String status) {
        if ("CONFIRMED".equalsIgnoreCase(status)) {
            tvBadge.setText("● Đã xác nhận");
            tvBadge.setTextColor(ContextCompat.getColor(ctx, R.color.ds_success));
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            tvBadge.setText("● Đã hoàn thành");
            tvBadge.setTextColor(ContextCompat.getColor(ctx, R.color.ds_success));
        } else if ("NO_SHOW".equalsIgnoreCase(status)) {
            tvBadge.setText("● Không đến");
            tvBadge.setTextColor(ContextCompat.getColor(ctx, R.color.status_info));
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            tvBadge.setText("● Đã hủy");
            tvBadge.setTextColor(ContextCompat.getColor(ctx, R.color.ds_text_secondary));
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            tvBadge.setText("● Đã từ chối");
            tvBadge.setTextColor(ContextCompat.getColor(ctx, R.color.ds_error));
        } else {
            tvBadge.setText("● Chờ xác nhận");
            tvBadge.setTextColor(ContextCompat.getColor(ctx, R.color.ds_warning));
        }
    }
}
