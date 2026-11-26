package com.example.petcareui.ui.pets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareui.R;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.databinding.ItemPetCardBinding;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.ArrayList;
import java.util.List;

public class PetListAdapter extends RecyclerView.Adapter<PetListAdapter.PetViewHolder> {

    public interface OnPetActionListener {
        void onPetSelect(int position, View itemView);
        void onPetOpenDetails(PetItem pet);
        void onPetEdit(PetItem pet);
        void onPetHistory(PetItem pet);
        void onPetDeleteRequest(PetItem pet);
    }

    private final List<PetItem> items = new ArrayList<>();
    private final boolean bookingMode;
    private final OnPetActionListener listener;
    private int selectedIndex = 0;

    public PetListAdapter(boolean bookingMode, OnPetActionListener listener) {
        this.bookingMode = bookingMode;
        this.listener = listener;
    }

    public void submitList(List<PetItem> newItems, int newSelectedIndex) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        this.selectedIndex = newSelectedIndex;
        notifyDataSetChanged();
    }

    public void setSelectedIndex(int newSelectedIndex) {
        int previous = this.selectedIndex;
        this.selectedIndex = newSelectedIndex;
        if (previous >= 0 && previous < items.size()) {
            notifyItemChanged(previous);
        }
        if (newSelectedIndex >= 0 && newSelectedIndex < items.size()) {
            notifyItemChanged(newSelectedIndex);
        }
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPetCardBinding binding = ItemPetCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class PetViewHolder extends RecyclerView.ViewHolder {
        private final ItemPetCardBinding binding;

        PetViewHolder(ItemPetCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            UiMotionUtils.attachSpringTouch(binding.getRoot(), binding.btnEditPet, binding.btnHistoryPet);
        }

        void bind(PetItem pet, int position) {
            binding.ivPetAvatar.setImageResource(pet.getImageResId());

            binding.tvPetName.setText(pet.getName());

            String realSpecies = pet.getRawSpecies();
            binding.tvPetSpecies.setText(realSpecies != null && !realSpecies.isEmpty() ? realSpecies : "Thú cưng");

            String age = pet.getAge() != null ? pet.getAge().trim() : "";
            String sex = pet.getSex() != null ? pet.getSex().replace("♂", "").replace("♀", "").trim() : "";
            if (!age.isEmpty() && !sex.isEmpty()) {
                binding.tvPetHealthBadge.setText(age + " · " + sex);
            } else if (!age.isEmpty()) {
                binding.tvPetHealthBadge.setText(age);
            } else {
                binding.tvPetHealthBadge.setText(sex);
            }

            if (bookingMode) {
                binding.layoutPetActions.setVisibility(View.GONE);
                binding.ivPetCheck.setVisibility(View.VISIBLE);
                boolean isSelected = (position == selectedIndex);
                if (isSelected) {
                    binding.layoutPetCard.setBackgroundResource(R.drawable.bg_pet_card_selected_minimal);
                    binding.ivPetCheck.setImageResource(R.drawable.bg_radio_selected);
                } else {
                    binding.layoutPetCard.setBackgroundResource(R.drawable.bg_home_card_borderless);
                    binding.ivPetCheck.setImageResource(R.drawable.bg_radio_unselected);
                }
                binding.getRoot().setOnClickListener(v -> {
                    int adapterPos = getBindingAdapterPosition();
                    if (adapterPos != RecyclerView.NO_POSITION && listener != null) {
                        listener.onPetSelect(adapterPos, binding.getRoot());
                    }
                });
            } else {
                binding.layoutPetCard.setBackgroundResource(R.drawable.bg_home_card_borderless);
                binding.layoutPetActions.setVisibility(View.VISIBLE);
                binding.ivPetCheck.setVisibility(View.GONE);

                binding.btnEditPet.setOnClickListener(v -> {
                    if (listener != null) listener.onPetEdit(pet);
                });
                binding.btnHistoryPet.setOnClickListener(v -> {
                    if (listener != null) listener.onPetHistory(pet);
                });
                binding.getRoot().setOnClickListener(v -> {
                    if (listener != null) listener.onPetOpenDetails(pet);
                });
            }

            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onPetDeleteRequest(pet);
                    return true;
                }
                return false;
            });
        }
    }
}
