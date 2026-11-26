package com.example.petcareui.ui.staff;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareui.R;
import com.example.petcareui.data.model.StaffSlotItem;
import com.example.petcareui.databinding.ItemStaffSlotRowBinding;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.ArrayList;
import java.util.List;

public class StaffSlotAdapter extends RecyclerView.Adapter<StaffSlotAdapter.StaffSlotViewHolder> {

    public interface OnSlotToggleListener {
        void onToggleSlot(StaffSlotItem slot, View actionView);
    }

    private final List<StaffSlotItem> items = new ArrayList<>();
    private final OnSlotToggleListener listener;

    public StaffSlotAdapter(OnSlotToggleListener listener) {
        this.listener = listener;
    }

    public void submitList(List<StaffSlotItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StaffSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStaffSlotRowBinding binding = ItemStaffSlotRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new StaffSlotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffSlotViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class StaffSlotViewHolder extends RecyclerView.ViewHolder {
        private final ItemStaffSlotRowBinding binding;

        StaffSlotViewHolder(ItemStaffSlotRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            UiMotionUtils.attachSpringTouch(binding.getRoot(), binding.btnToggleLock);
        }

        void bind(StaffSlotItem slot) {
            Context ctx = binding.getRoot().getContext();
            binding.tvSlotTime.setText(slot.getTime());
            binding.slotRowContainer.setBackgroundResource(R.drawable.bg_card_clinic);
            binding.btnToggleLock.setEnabled(true);
            binding.btnToggleLock.setAlpha(1.0f);

            if ("booked".equals(slot.getState())) {
                binding.tvSlotTime.setBackgroundResource(R.drawable.bg_shortcut_icon_cream);
                binding.tvSlotTime.setTextColor(ContextCompat.getColor(ctx, R.color.ds_text_primary));
                binding.tvSlotPrimaryText.setText((slot.getPet() != null ? slot.getPet() : "Thú cưng")
                        + " · " + (slot.getService() != null ? slot.getService() : "Lịch khám"));
                binding.tvSlotSecondaryText.setText(slot.getCustomer() != null ? slot.getCustomer() : "Đã có khách đặt");
                binding.tvSlotBookedBadge.setVisibility(View.VISIBLE);
                binding.btnToggleLock.setVisibility(View.GONE);
            } else if ("closed".equals(slot.getState())) {
                binding.tvSlotTime.setBackgroundResource(R.drawable.bg_shortcut_icon_cream);
                binding.tvSlotTime.setTextColor(ContextCompat.getColor(ctx, R.color.ds_text_secondary));
                binding.tvSlotPrimaryText.setText("Đã đóng");
                binding.tvSlotSecondaryText.setText("Ẩn khỏi ứng dụng của khách");
                binding.tvSlotBookedBadge.setVisibility(View.GONE);
                binding.btnToggleLock.setVisibility(View.VISIBLE);
                binding.ivLockIcon.setImageResource(R.drawable.ic_lock);
                binding.ivLockIcon.setImageTintList(ContextCompat.getColorStateList(ctx, R.color.ds_error));
            } else {
                binding.tvSlotTime.setBackgroundResource(R.drawable.bg_shortcut_icon_cream);
                binding.tvSlotTime.setTextColor(ContextCompat.getColor(ctx, R.color.ds_text_primary));
                binding.tvSlotPrimaryText.setText("Đang mở nhận đặt lịch");
                binding.tvSlotSecondaryText.setText("Khung 30 phút");
                binding.tvSlotBookedBadge.setVisibility(View.GONE);
                binding.btnToggleLock.setVisibility(View.VISIBLE);
                binding.ivLockIcon.setImageResource(R.drawable.ic_unlock);
                binding.ivLockIcon.setImageTintList(ContextCompat.getColorStateList(ctx, R.color.ds_primary));
            }

            binding.btnToggleLock.setOnClickListener(v -> {
                UiMotionUtils.animateSelectionPop(v);
                if (listener != null) {
                    listener.onToggleSlot(slot, v);
                }
            });
        }
    }
}
