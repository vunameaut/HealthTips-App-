package com.vhn.doan.presentation.reminder;

import com.vhn.doan.data.Reminder;
import com.vhn.doan.data.repository.ReminderRepository;
import com.vhn.doan.presentation.base.BasePresenter;
import com.vhn.doan.services.ReminderService;
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

                    // CẬP NHẬT SỐ LƯỢNG NHẮC NHỞ ĐANG HOẠT ĐỘNG
                    updateActiveReminderCount();

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

    /**
     * Cập nhật số lượng nhắc nhở đang hoạt động
     */
    private void updateActiveReminderCount() {
        if (!isViewAttached()) return;

        int activeCount = 0;
        for (Reminder reminder : allReminders) {
            if (reminder.isActive()) {
                activeCount++;
            }
        }

        // Cập nhật UI hiển thị số lượng nhắc nhở đang hoạt động
        view.updateActiveReminderCount(activeCount);

        android.util.Log.d("ReminderPresenter", "📊 Số nhắc nhở đang hoạt động: " + activeCount + "/" + allReminders.size());
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

                        // ✅ THÊM: Lên lịch thông báo cho nhắc nhở mới
                        scheduleReminderNotification(reminder);
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

                        // ✅ THÊM: Cập nhật lịch thông báo cho nhắc nhở đã chỉnh sửa
                        scheduleReminderNotification(reminder);
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

                    // ✅ THÊM: Cập nhật lịch thông báo khi bật/tắt
                    scheduleReminderNotification(reminder);
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

    /**
     * ✅ THÊM: Method để lên lịch thông báo cho nhắc nhở
     */
    private void scheduleReminderNotification(Reminder reminder) {
        if (!isViewAttached() || reminder == null) {
            return;
        }

        // Lấy context từ view để tạo ReminderService
        android.content.Context context = getContextFromView();
        if (context == null) {
            android.util.Log.w("ReminderPresenter", "Không thể lấy context để schedule reminder");
            return;
        }

        try {
            ReminderService reminderService = new ReminderService(context);

            if (reminder.isActive()) {
                // Lên lịch thông báo nếu reminder đang bật
                reminderService.scheduleReminder(reminder);
                android.util.Log.d("ReminderPresenter", "✅ Đã lên lịch thông báo cho reminder: " + reminder.getTitle());
            } else {
                // Hủy thông báo nếu reminder bị tắt
                reminderService.cancelReminder(reminder.getId());
                android.util.Log.d("ReminderPresenter", "❌ Đã hủy thông báo cho reminder: " + reminder.getTitle());
            }
        } catch (Exception e) {
            android.util.Log.e("ReminderPresenter", "Lỗi khi schedule reminder: " + e.getMessage());
            if (isViewAttached()) {
                view.showError("Lỗi khi thiết lập thông báo: " + e.getMessage());
            }
        }
    }

    /**
     * ✅ THÊM: Lấy context từ view (giả sử view là Fragment hoặc Activity)
     */
    private android.content.Context getContextFromView() {
        if (!isViewAttached()) {
            return null;
        }

        // Thử cast view thành các loại có thể có context
        if (view instanceof androidx.fragment.app.Fragment) {
            return ((androidx.fragment.app.Fragment) view).getContext();
        } else if (view instanceof android.app.Activity) {
            return (android.app.Activity) view;
        } else if (view instanceof ReminderFragment) {
            return ((ReminderFragment) view).getContext();
        }

        // Nếu không thể lấy context trực tiếp, thử cách khác
        return null;
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
     * Sắp xếp danh sách nhắc nhở theo tiêu chí
     */
    public void sortReminders(String sortType) {
        if (allReminders == null || allReminders.isEmpty()) {
            return;
        }

        List<Reminder> sortedList = new ArrayList<>(filteredReminders);

        switch (sortType) {
            case "created_desc":
                sortedList.sort((r1, r2) -> {
                    Long time1 = r1.getCreatedAt();
                    Long time2 = r2.getCreatedAt();
                    if (time1 == null && time2 == null) return 0;
                    if (time1 == null) return 1;
                    if (time2 == null) return -1;
                    return time2.compareTo(time1);
                });
                break;
            case "created_asc":
                sortedList.sort((r1, r2) -> {
                    Long time1 = r1.getCreatedAt();
                    Long time2 = r2.getCreatedAt();
                    if (time1 == null && time2 == null) return 0;
                    if (time1 == null) return 1;
                    if (time2 == null) return -1;
                    return time1.compareTo(time2);
                });
                break;
            case "datetime_asc":
                sortedList.sort((r1, r2) -> {
                    Long time1 = r1.getReminderTime();
                    Long time2 = r2.getReminderTime();
                    if (time1 == null && time2 == null) return 0;
                    if (time1 == null) return 1;
                    if (time2 == null) return -1;
                    return time1.compareTo(time2);
                });
                break;
            case "datetime_desc":
                sortedList.sort((r1, r2) -> {
                    Long time1 = r1.getReminderTime();
                    Long time2 = r2.getReminderTime();
                    if (time1 == null && time2 == null) return 0;
                    if (time1 == null) return 1;
                    if (time2 == null) return -1;
                    return time2.compareTo(time1);
                });
                break;
            case "name_asc":
                sortedList.sort((r1, r2) -> {
                    String title1 = r1.getTitle() != null ? r1.getTitle() : "";
                    String title2 = r2.getTitle() != null ? r2.getTitle() : "";
                    return title1.compareToIgnoreCase(title2);
                });
                break;
            case "name_desc":
                sortedList.sort((r1, r2) -> {
                    String title1 = r1.getTitle() != null ? r1.getTitle() : "";
                    String title2 = r2.getTitle() != null ? r2.getTitle() : "";
                    return title2.compareToIgnoreCase(title1);
                });
                break;
            case "active_first":
                sortedList.sort((r1, r2) -> {
                    boolean active1 = r1.isActive();
                    boolean active2 = r2.isActive();
                    if (active1 && !active2) return -1;
                    if (!active1 && active2) return 1;
                    return 0;
                });
                break;
            default:
                // Default sort by created time desc
                sortedList.sort((r1, r2) -> {
                    Long time1 = r1.getCreatedAt();
                    Long time2 = r2.getCreatedAt();
                    if (time1 == null && time2 == null) return 0;
                    if (time1 == null) return 1;
                    if (time2 == null) return -1;
                    return time2.compareTo(time1);
                });
                break;
        }

        this.filteredReminders = sortedList;

        // Cập nhật view thay vì gọi updateView()
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
     * Xuất danh sách nhắc nhở
     */
    public void exportReminders() {
        if (getView() == null) return;

        try {
            if (allReminders == null || allReminders.isEmpty()) {
                getView().showError("Không có dữ liệu nhắc nhở để xuất");
                return;
            }

            // Tạo nội dung xuất
            StringBuilder exportContent = new StringBuilder();
            exportContent.append("DANH SÁCH NHẮC NHỞ SỨC KHỎE\n");
            exportContent.append("============================\n\n");

            for (int i = 0; i < allReminders.size(); i++) {
                Reminder reminder = allReminders.get(i);
                exportContent.append(String.format("%d. %s\n", i + 1,
                        reminder.getTitle() != null ? reminder.getTitle() : "Không có tiêu đề"));

                if (reminder.getDescription() != null && !reminder.getDescription().trim().isEmpty()) {
                    exportContent.append("   Mô tả: ").append(reminder.getDescription()).append("\n");
                }

                if (reminder.getReminderTime() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                    exportContent.append("   Thời gian: ").append(sdf.format(new java.util.Date(reminder.getReminderTime()))).append("\n");
                }

                String repeatText = getRepeatTypeText(reminder.getRepeatType());
                exportContent.append("   Lặp lại: ").append(repeatText).append("\n");
                exportContent.append("   Trạng thái: ").append(reminder.isActive() ? "Đang hoạt động" : "Tạm dừng").append("\n");
                exportContent.append("\n");
            }

            exportContent.append("Tổng số nhắc nhở: ").append(allReminders.size()).append("\n");
            int activeCount = 0;
            for (Reminder r : allReminders) {
                if (r.isActive()) activeCount++;
            }
            exportContent.append("Đang hoạt động: ").append(activeCount).append("\n");
            exportContent.append("Tạm dừng: ").append(allReminders.size() - activeCount).append("\n");

            // Gọi View để xử lý việc xuất file
            getView().showSuccess("Đã tạo nội dung xuất thành công");

            // Có thể thêm logic để lưu file vào External Storage hoặc chia sẻ

        } catch (Exception e) {
            android.util.Log.e("ReminderPresenter", "Error exporting reminders: " + e.getMessage());
            if (getView() != null) {
                getView().showError("Lỗi khi xuất danh sách: " + e.getMessage());
            }
        }
    }

    /**
     * Helper method để convert repeat type int thành text
     */
    private String getRepeatTypeText(int repeatType) {
        switch (repeatType) {
            case Reminder.RepeatType.NO_REPEAT:
                return "Không lặp lại";
            case Reminder.RepeatType.DAILY:
                return "Hàng ngày";
            case Reminder.RepeatType.WEEKLY:
                return "Hàng tuần";
            case Reminder.RepeatType.MONTHLY:
                return "Hàng tháng";
            default:
                return "Tùy chỉnh";
        }
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

        // CẬP NHẬT SỐ LƯỢNG NHẮC NHỞ ĐANG HOẠT ĐỘNG SAU KHI THÊM MỚI
        updateActiveReminderCount();

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

        // CẬP NHẬT SỐ LƯỢNG NHẮC NHỞ ĐANG HOẠT ĐỘNG SAU KHI CẬP NHẬT
        updateActiveReminderCount();

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

        // CẬP NHẬT SỐ LƯỢNG NHẮC NHỞ ĐANG HOẠT ĐỘNG SAU KHI XÓA
        updateActiveReminderCount();

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
