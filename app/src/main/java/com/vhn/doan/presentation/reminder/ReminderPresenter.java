package com.vhn.doan.presentation.reminder;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.presentation.base.BasePresenter;
import com.vhn.doan.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Presenter cho chức năng Reminder theo kiến trúc MVP
 */
public class ReminderPresenter extends BasePresenter<ReminderContract.View> implements ReminderContract.Presenter {

    private ReminderRepository reminderRepository;
    private UserSessionManager userSessionManager;

    private List<Reminder> allReminders;
    private List<Reminder> filteredReminders;
    private boolean isLoading = false;
    private String currentSearchQuery = "";
    private boolean showActiveOnly = false;

    @Inject
    public ReminderPresenter(ReminderRepository reminderRepository, UserSessionManager userSessionManager) {
        this.reminderRepository = reminderRepository;
        this.userSessionManager = userSessionManager;
        this.allReminders = new ArrayList<>();
        this.filteredReminders = new ArrayList<>();
    }

    @Override
    public void attachView(ReminderContract.View view) {
        super.attachView(view);
    }

    @Override
    public void start() {
        loadReminders();
    }

    @Override
    public void loadReminders() {
        if (isLoading) {
            return;
        }

        if (!isViewAttached()) {
            return;
        }

        String userId = userSessionManager.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            view.showError("Vui lòng đăng nhập để xem nhắc nhở");
            return;
        }

        isLoading = true;
        view.showLoading();

        reminderRepository.getUserReminders(userId, new ReminderRepository.RepositoryCallback<List<Reminder>>() {
            @Override
            public void onSuccess(List<Reminder> reminders) {
                isLoading = false;
                if (isViewAttached()) {
                    view.hideLoading();
                    allReminders.clear();
                    allReminders.addAll(reminders);

                    applyFiltersAndSearch();

                    if (filteredReminders.isEmpty()) {
                        view.showEmptyState();
                    } else {
                        view.hideEmptyState();
                        view.showReminders(filteredReminders);
                    }
                }
            }

            @Override
            public void onError(String error) {
                isLoading = false;
                if (isViewAttached()) {
                    view.hideLoading();
                    view.showError("Lỗi khi tải nhắc nhở: " + error);
                }
            }
        });
    }

    @Override
    public void createReminder() {
        if (isViewAttached()) {
            view.showReminderDialog(null);
        }
    }

    @Override
    public void editReminder(Reminder reminder) {
        if (isViewAttached() && reminder != null) {
            view.showReminderDialog(reminder);
        }
    }

    @Override
    public void deleteReminder(Reminder reminder) {
        if (isViewAttached() && reminder != null) {
            view.showDeleteConfirmDialog(reminder);
        }
    }

    @Override
    public void saveReminder(Reminder reminder) {
        if (!isViewAttached() || reminder == null) {
            return;
        }

        String userId = userSessionManager.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            view.showError("Vui lòng đăng nhập để lưu nhắc nhở");
            return;
        }

        reminder.setUserId(userId);
        view.showLoading();

        if (reminder.getId() == null || reminder.getId().isEmpty()) {
            // Tạo mới
            reminderRepository.addReminder(reminder, new ReminderRepository.RepositoryCallback<String>() {
                @Override
                public void onSuccess(String reminderId) {
                    if (isViewAttached()) {
                        view.hideLoading();
                        view.showSuccess("Tạo nhắc nhở thành công");
                        reminder.setId(reminderId);
                        addReminderToList(reminder);
                    }
                }

                @Override
                public void onError(String error) {
                    if (isViewAttached()) {
                        view.hideLoading();
                        view.showError("Lỗi khi tạo nhắc nhở: " + error);
                    }
                }
            });
        } else {
            // Cập nhật
            reminderRepository.updateReminder(reminder, new ReminderRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    if (isViewAttached()) {
                        view.hideLoading();
                        view.showSuccess("Cập nhật nhắc nhở thành công");
                        updateReminderInList(reminder);
                    }
                }

                @Override
                public void onError(String error) {
                    if (isViewAttached()) {
                        view.hideLoading();
                        view.showError("Lỗi khi cập nhật nhắc nhở: " + error);
                    }
                }
            });
        }
    }

    @Override
    public void toggleReminder(Reminder reminder) {
        if (!isViewAttached() || reminder == null) {
            return;
        }

        boolean newStatus = !reminder.isActive();

        reminderRepository.toggleReminder(reminder.getId(), newStatus, new ReminderRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isViewAttached()) {
                    reminder.setActive(newStatus);
                    updateReminderInList(reminder);

                    String message = newStatus ? "Đã bật nhắc nhở" : "Đã tắt nhắc nhở";
                    view.showSuccess(message);
                }
            }

            @Override
            public void onError(String error) {
                if (isViewAttached()) {
                    view.showError("Lỗi khi thay đổi trạng thái nhắc nhở: " + error);
                }
            }
        });
    }

    @Override
    public void refreshReminders() {
        loadReminders();
    }

    @Override
    public void searchReminders(String query) {
        currentSearchQuery = query != null ? query.trim() : "";
        applyFiltersAndSearch();

        if (isViewAttached()) {
            if (filteredReminders.isEmpty() && !allReminders.isEmpty()) {
                view.showEmptyState();
            } else {
                view.hideEmptyState();
                view.showReminders(filteredReminders);
            }
        }
    }

    @Override
    public void filterReminders(boolean activeOnly) {
        showActiveOnly = activeOnly;
        applyFiltersAndSearch();

        if (isViewAttached()) {
            if (filteredReminders.isEmpty() && !allReminders.isEmpty()) {
                view.showEmptyState();
            } else {
                view.hideEmptyState();
                view.showReminders(filteredReminders);
            }
        }
    }

    /**
     * Xóa nhắc nhở sau khi xác nhận
     */
    public void confirmDeleteReminder(Reminder reminder) {
        if (!isViewAttached() || reminder == null) {
            return;
        }

        view.showLoading();

        reminderRepository.deleteReminder(reminder.getId(), new ReminderRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isViewAttached()) {
                    view.hideLoading();
                    view.showSuccess("Xóa nhắc nhở thành công");
                    removeReminderFromList(reminder);
                }
            }

            @Override
            public void onError(String error) {
                if (isViewAttached()) {
                    view.hideLoading();
                    view.showError("Lỗi khi xóa nhắc nhở: " + error);
                }
            }
        });
    }

    /**
     * Áp dụng bộ lọc và tìm kiếm
     */
    private void applyFiltersAndSearch() {
        filteredReminders.clear();

        for (Reminder reminder : allReminders) {
            boolean matchesSearch = true;
            boolean matchesFilter = true;

            // Kiểm tra tìm kiếm
            if (!currentSearchQuery.isEmpty()) {
                String title = reminder.getTitle() != null ? reminder.getTitle().toLowerCase() : "";
                String description = reminder.getDescription() != null ? reminder.getDescription().toLowerCase() : "";
                String query = currentSearchQuery.toLowerCase();

                matchesSearch = title.contains(query) || description.contains(query);
            }

            // Kiểm tra bộ lọc
            if (showActiveOnly) {
                matchesFilter = reminder.isActive();
            }

            if (matchesSearch && matchesFilter) {
                filteredReminders.add(reminder);
            }
        }
    }

    /**
     * Thêm reminder mới vào danh sách
     */
    private void addReminderToList(Reminder reminder) {
        allReminders.add(0, reminder); // Thêm vào đầu danh sách
        applyFiltersAndSearch();

        if (isViewAttached()) {
            if (filteredReminders.isEmpty()) {
                view.showEmptyState();
            } else {
                view.hideEmptyState();
                view.addReminderItem(reminder);
            }
        }
    }

    /**
     * Cập nhật reminder trong danh sách
     */
    private void updateReminderInList(Reminder updatedReminder) {
        for (int i = 0; i < allReminders.size(); i++) {
            if (allReminders.get(i).getId().equals(updatedReminder.getId())) {
                allReminders.set(i, updatedReminder);
                break;
            }
        }

        applyFiltersAndSearch();

        if (isViewAttached()) {
            view.updateReminderItem(updatedReminder);
        }
    }

    /**
     * Xóa reminder khỏi danh sách
     */
    private void removeReminderFromList(Reminder reminder) {
        allReminders.removeIf(r -> r.getId().equals(reminder.getId()));
        filteredReminders.removeIf(r -> r.getId().equals(reminder.getId()));

        if (isViewAttached()) {
            view.removeReminderItem(reminder);

            if (filteredReminders.isEmpty()) {
                view.showEmptyState();
            }
        }
    }

    /**
     * Lấy số lượng nhắc nhở đang hoạt động
     */
    public int getActiveReminderCount() {
        int count = 0;
        for (Reminder reminder : allReminders) {
            if (reminder.isActive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Lấy số lượng nhắc nhở tổng cộng
     */
    public int getTotalReminderCount() {
        return allReminders.size();
    }
}
