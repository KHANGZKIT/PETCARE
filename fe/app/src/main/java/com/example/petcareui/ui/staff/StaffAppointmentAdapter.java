package com.example.petcareui.ui.staff;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareui.R;
import com.example.petcareui.data.model.StaffAppointmentItem;
import com.example.petcareui.databinding.ItemStaffApptCardBinding;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.ArrayList;
import java.util.List;

public class StaffAppointmentAdapter extends RecyclerView.Adapter<StaffAppointmentAdapter.StaffApptViewHolder> {

    public interface OnStaffAppointmentClickListener {
        void onAppointmentClick(StaffAppointmentItem item);
    }

    private final List<StaffAppointmentItem> items = new ArrayList<>();
    private final OnStaffAppointmentClickListener listener;

    public StaffAppointmentAdapter(OnStaffAppointmentClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<StaffAppointmentItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StaffApptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStaffApptCardBinding binding = ItemStaffApptCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new StaffApptViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffApptViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class StaffApptViewHolder extends RecyclerView.ViewHolder {
        private final ItemStaffApptCardBinding binding;

        StaffApptViewHolder(ItemStaffApptCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            UiMotionUtils.attachSpringTouch(binding.getRoot());
        }

        void bind(StaffAppointmentItem item) {
            Context ctx = binding.getRoot().getContext();

            binding.ivStaffPetAvatar.setImageResource(item.getPetImgRes());
            binding.tvStaffServiceTitle.setText(item.getService());
            binding.tvStaffCustomerPet.setText(item.getCustomer() + " · " + item.getPet());
            binding.tvStaffDate.setText(StaffClinicHelper.formatNaturalDateTime(item.getDate(), item.getTime()));
            binding.tvStaffTime.setText(item.getTime());
            binding.tvStaffDuration.setText("· " + item.getDuration());

            binding.cardContainer.setBackgroundResource(R.drawable.bg_home_card_borderless);
            binding.tvStaffApptStatus.setBackground(null);

            if ("PENDING".equalsIgnoreCase(item.getStatus())) {
                binding.tvStaffApptStatus.setText("● Chờ xác nhận");
                binding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(ctx, R.color.ds_warning));
            } else if ("CONFIRMED".equalsIgnoreCase(item.getStatus())) {
                binding.tvStaffApptStatus.setText("● Đã xác nhận");
                binding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(ctx, R.color.ds_success));
            } else if ("COMPLETED".equalsIgnoreCase(item.getStatus())) {
                binding.tvStaffApptStatus.setText("● Đã hoàn thành");
                binding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(ctx, R.color.ds_primary));
            } else if ("NO_SHOW".equalsIgnoreCase(item.getStatus())) {
                binding.tvStaffApptStatus.setText("● Không đến");
                binding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(ctx, R.color.ds_text_secondary));
            } else {
                binding.tvStaffApptStatus.setText("● Đã hủy / Từ chối");
                binding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(ctx, R.color.ds_error));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAppointmentClick(item);
                }
            });
        }
    }
}
