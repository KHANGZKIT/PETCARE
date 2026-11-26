package com.example.petcareui.ui.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareui.R;
import com.example.petcareui.data.model.NotificationItem;
import com.example.petcareui.databinding.ItemNotificationCardBinding;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem item);
    }

    private final List<NotificationItem> items = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<NotificationItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationCardBinding binding = ItemNotificationCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationCardBinding binding;

        NotificationViewHolder(ItemNotificationCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NotificationItem item) {
            Context ctx = binding.getRoot().getContext();

            binding.tvNotifTitle.setText(item.getTitle());
            binding.tvNotifBody.setText(item.getBody());
            binding.tvNotifTime.setText(item.getTime());

            if (item.isUnread()) {
                binding.layoutCardContainer.setBackgroundResource(R.drawable.bg_notification_card_unread);
                binding.viewUnreadDot.setVisibility(View.VISIBLE);
            } else {
                binding.layoutCardContainer.setBackgroundResource(R.drawable.bg_card_clinic);
                binding.viewUnreadDot.setVisibility(View.GONE);
            }

            applyTypeStyle(ctx, item.getType());

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(item);
                }
            });
        }

        private void applyTypeStyle(Context ctx, String type) {
            binding.layoutIconBox.setBackgroundResource(R.drawable.bg_shortcut_icon_cream);
            if ("confirmed".equalsIgnoreCase(type)) {
                binding.ivNotifIcon.setImageResource(R.drawable.ic_calendar_days);
                binding.ivNotifIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.ds_success));
            } else if ("pending".equalsIgnoreCase(type)) {
                binding.ivNotifIcon.setImageResource(R.drawable.ic_clock);
                binding.ivNotifIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.ds_warning));
            } else if ("reminder".equalsIgnoreCase(type)) {
                binding.ivNotifIcon.setImageResource(R.drawable.ic_bell);
                binding.ivNotifIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.ds_primary));
            } else if ("cancelled".equalsIgnoreCase(type)) {
                binding.ivNotifIcon.setImageResource(R.drawable.ic_calendar_x);
                binding.ivNotifIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.ds_text_secondary));
            } else if ("rejected".equalsIgnoreCase(type)) {
                binding.ivNotifIcon.setImageResource(R.drawable.ic_x_circle);
                binding.ivNotifIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.ds_error));
            } else {
                binding.ivNotifIcon.setImageResource(R.drawable.ic_bell);
                binding.ivNotifIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.ds_primary));
            }
        }
    }
}
