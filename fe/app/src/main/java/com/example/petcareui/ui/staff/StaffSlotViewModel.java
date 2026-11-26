package com.example.petcareui.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petcareui.data.dto.request.CreateSlotRequest;
import com.example.petcareui.data.dto.response.ClinicSlotResponse;
import com.example.petcareui.data.model.StaffSlotItem;
import com.example.petcareui.data.repository.RepositoryCallback;
import com.example.petcareui.data.repository.StaffSlotRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StaffSlotViewModel extends ViewModel {

    public static class SlotCreatedResult {
        public final String formattedStart;
        public final String formattedEnd;

        public SlotCreatedResult(String formattedStart, String formattedEnd) {
            this.formattedStart = formattedStart;
            this.formattedEnd = formattedEnd;
        }
    }

    private final StaffSlotRepository repository = new StaffSlotRepository();
    private final List<StaffSlotItem> currentSlots = new ArrayList<>();

    private final MutableLiveData<List<StaffSlotItem>> slots = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Integer> openSlotCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> unauthorizedEvent = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<SlotCreatedResult> slotCreatedEvent = new MutableLiveData<>();

    private String lastLoadedDateIso = "";

    public LiveData<List<StaffSlotItem>> getSlots() {
        return slots;
    }

    public LiveData<Integer> getOpenSlotCount() {
        return openSlotCount;
    }

    public LiveData<Boolean> getUnauthorizedEvent() {
        return unauthorizedEvent;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public LiveData<SlotCreatedResult> getSlotCreatedEvent() {
        return slotCreatedEvent;
    }

    public void clearToast() {
        toastMessage.setValue(null);
    }

    public void clearSlotCreatedEvent() {
        slotCreatedEvent.setValue(null);
    }

    public void loadSlots(String isoDate) {
        this.lastLoadedDateIso = isoDate;
        repository.getStaffSlots(isoDate, new RepositoryCallback<List<ClinicSlotResponse>>() {
            @Override
            public void onSuccess(List<ClinicSlotResponse> data) {
                currentSlots.clear();
                for (ClinicSlotResponse res : data) {
                    currentSlots.add(res.toStaffSlotItem());
                }
                publishSlots();
            }

            @Override
            public void onError(int httpCode, String message) {
                if (httpCode == 401 || httpCode == 403) {
                    unauthorizedEvent.setValue(true);
                    return;
                }
                currentSlots.clear();
                publishSlots();
                toastMessage.setValue(message);
            }
        });
    }

    public void createSlot(String isoDate, String startTime, String endTime) {
        CreateSlotRequest request = new CreateSlotRequest(isoDate, startTime, endTime);
        repository.createSlot(request, new RepositoryCallback<ClinicSlotResponse>() {
            @Override
            public void onSuccess(ClinicSlotResponse created) {
                String formattedStart = created.getFormattedStartTime();
                String rawEnd = created.getEndTime();
                String formattedEnd = (rawEnd != null && rawEnd.length() >= 5)
                        ? rawEnd.substring(0, 5)
                        : (rawEnd != null ? rawEnd : endTime);
                slotCreatedEvent.setValue(new SlotCreatedResult(formattedStart, formattedEnd));
                toastMessage.setValue("Đã tạo khung giờ thành công!");
                loadSlots(isoDate);
            }

            @Override
            public void onError(int httpCode, String message) {
                if (httpCode == 401 || httpCode == 403) {
                    unauthorizedEvent.setValue(true);
                    return;
                }
                toastMessage.setValue(message);
            }
        });
    }

    public void toggleSlotStatus(StaffSlotItem slot, Runnable onFinished) {
        Long slotId = slot != null ? slot.getId() : null;
        if (slotId == null) {
            toastMessage.setValue("Không xác định được mã khung giờ");
            if (onFinished != null) onFinished.run();
            return;
        }
        final boolean closing = "open".equals(slot.getState());
        final String targetStatus = closing ? "CLOSED" : "AVAILABLE";

        repository.updateSlotStatus(slotId, targetStatus, new RepositoryCallback<ClinicSlotResponse>() {
            @Override
            public void onSuccess(ClinicSlotResponse response) {
                if (onFinished != null) onFinished.run();
                String newState = response.toStaffSlotItem().getState();
                for (StaffSlotItem item : currentSlots) {
                    if (slotId.equals(item.getId())) {
                        item.setState(newState);
                        break;
                    }
                }
                publishSlots();
                toastMessage.setValue(closing
                        ? "Đã đóng khung giờ " + slot.getTime()
                        : "Đã mở lại khung giờ " + slot.getTime());
            }

            @Override
            public void onError(int httpCode, String message) {
                if (onFinished != null) onFinished.run();
                if (httpCode == 401 || httpCode == 403) {
                    unauthorizedEvent.setValue(true);
                    return;
                }
                toastMessage.setValue(message);
                if (httpCode == 409 && !lastLoadedDateIso.isEmpty()) {
                    loadSlots(lastLoadedDateIso);
                }
            }
        });
    }

    private void publishSlots() {
        int openCount = 0;
        for (StaffSlotItem item : currentSlots) {
            if ("open".equals(item.getState())) {
                openCount++;
            }
        }
        openSlotCount.setValue(openCount);
        slots.setValue(new ArrayList<>(currentSlots));
    }
}
