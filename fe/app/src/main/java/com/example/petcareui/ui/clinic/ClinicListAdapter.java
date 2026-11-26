package com.example.petcareui.ui.clinic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareui.R;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.databinding.ItemClinicRowBinding;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClinicListAdapter extends RecyclerView.Adapter<ClinicListAdapter.ClinicViewHolder> {

    public interface OnClinicClickListener {
        void onClinicClick(ClinicItem clinic);
    }

    private final List<ClinicItem> items = new ArrayList<>();
    private final OnClinicClickListener listener;

    public ClinicListAdapter(OnClinicClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ClinicItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClinicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemClinicRowBinding binding = ItemClinicRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ClinicViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ClinicViewHolder holder, int position) {
        holder.bind(items.get(position), position == items.size() - 1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ClinicViewHolder extends RecyclerView.ViewHolder {
        private final ItemClinicRowBinding binding;

        ClinicViewHolder(ItemClinicRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            UiMotionUtils.attachSpringTouch(binding.getRoot());
        }

        void bind(ClinicItem clinic, boolean isLast) {
            Context ctx = binding.getRoot().getContext();
            binding.tvClinicName.setText(formatShortClinicName(clinic.getName()));
            binding.tvClinicAddress.setText(clinic.getAddress());
            binding.tvClinicRating.setText(String.valueOf(clinic.getRating()));
            binding.tvClinicMeta.setText("(" + clinic.getReviews() + ") · " + clinic.getDistance());
            binding.ivClinicImage.setImageResource(clinic.getImageResId());
            binding.ivClinicImage.setColorFilter(null);

            if (clinic.isOpen()) {
                binding.tvClinicStatusBadge.setText("● Đang mở cửa");
                binding.tvClinicStatusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.ds_success));
            } else {
                binding.tvClinicStatusBadge.setText("● Đã đóng cửa");
                binding.tvClinicStatusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.ds_text_secondary));
            }

            binding.tvClinicHours.setText("· " + formatShortHours(clinic.getHours()));
            binding.dividerClinicRow.setVisibility(isLast ? View.GONE : View.VISIBLE);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClinicClick(clinic);
                }
            });
        }
    }

    private static String formatShortClinicName(String raw) {
        if (raw == null) return "Phòng khám thú y";
        String s = raw.trim();
        if (s.toLowerCase(Locale.ROOT).startsWith("phòng khám thú y ")) {
            return "Thú y " + s.substring("Phòng khám Thú y ".length()).trim();
        }
        return s;
    }

    private static String formatShortHours(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "08:00 - 20:00";
        String s = raw.trim();
        if (s.contains("24/7")) {
            return "24/7 Cấp cứu";
        }
        int parenIdx = s.indexOf('(');
        if (parenIdx > 0) {
            s = s.substring(0, parenIdx).trim();
        }
        int ampIdx = s.indexOf('&');
        if (ampIdx > 0) {
            s = s.substring(0, ampIdx).trim();
        }
        return s;
    }
}
