package com.vhn.doan.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class để format thời gian cho notification history
 * Hiển thị theo các định dạng: "Vừa xong", "5 phút trước", "Hôm nay lúc 14:30", etc.
 */
public class NotificationTimeUtils {

    /**
     * Format timestamp thành chuỗi thời gian tương đối
     *
     * @param timestamp Timestamp cần format (milliseconds)
     * @return Chuỗi thời gian đã format
     */
    public static String formatNotificationTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // Nếu timestamp trong tương lai (lỗi), return "Vừa xong"
        if (diff < 0) {
            return "Vừa xong";
        }

        // Dưới 1 phút
        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "Vừa xong";
        }

        // Dưới 1 giờ
        if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + " phút trước";
        }

        Calendar notifCal = Calendar.getInstance();
        notifCal.setTimeInMillis(timestamp);

        Calendar nowCal = Calendar.getInstance();
        nowCal.setTimeInMillis(now);

        // Kiểm tra cùng ngày
        boolean isSameDay = notifCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                           notifCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR);

        if (isSameDay) {
            // Hôm nay: "Hôm nay lúc 14:30"
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("vi", "VN"));
            return "Hôm nay lúc " + timeFormat.format(new Date(timestamp));
        }

        // Kiểm tra hôm qua
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTimeInMillis(now);
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        boolean isYesterday = notifCal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                             notifCal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR);

        if (isYesterday) {
            // Hôm qua: "Hôm qua lúc 14:30"
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("vi", "VN"));
            return "Hôm qua lúc " + timeFormat.format(new Date(timestamp));
        }

        // Trong tuần này (7 ngày gần nhất)
        if (diff < TimeUnit.DAYS.toMillis(7)) {
            SimpleDateFormat dayTimeFormat = new SimpleDateFormat("EEEE 'lúc' HH:mm", new Locale("vi", "VN"));
            return dayTimeFormat.format(new Date(timestamp));
        }

        // Cùng năm: "20 Thg 11 lúc 14:30"
        if (notifCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)) {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd 'Thg' MM 'lúc' HH:mm", new Locale("vi", "VN"));
            return dateTimeFormat.format(new Date(timestamp));
        }

        // Khác năm: "20/11/2024"
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
        return fullDateFormat.format(new Date(timestamp));
    }

    /**
     * Lấy section header cho grouping notifications
     *
     * @param timestamp Timestamp cần check
     * @return Section header: "Hôm nay", "Hôm qua", "Tuần này", "Tháng này", "Cũ hơn"
     */
    public static String getSectionHeader(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        Calendar notifCal = Calendar.getInstance();
        notifCal.setTimeInMillis(timestamp);

        Calendar nowCal = Calendar.getInstance();
        nowCal.setTimeInMillis(now);

        // Hôm nay
        boolean isSameDay = notifCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                           notifCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR);
        if (isSameDay) {
            return "Hôm nay";
        }

        // Hôm qua
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTimeInMillis(now);
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        boolean isYesterday = notifCal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                             notifCal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR);
        if (isYesterday) {
            return "Hôm qua";
        }

        // Tuần này (7 ngày)
        if (diff < TimeUnit.DAYS.toMillis(7)) {
            return "Tuần này";
        }

        // Tháng này (30 ngày)
        if (diff < TimeUnit.DAYS.toMillis(30)) {
            return "Tháng này";
        }

        // Cũ hơn
        return "Cũ hơn";
    }

    /**
     * Format timestamp thành "HH:mm" cho hiển thị trong list
     */
    public static String formatTime(long timestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("vi", "VN"));
        return timeFormat.format(new Date(timestamp));
    }

    /**
     * Format timestamp thành "dd/MM/yyyy HH:mm"
     */
    public static String formatFullDateTime(long timestamp) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));
        return dateTimeFormat.format(new Date(timestamp));
    }
}
