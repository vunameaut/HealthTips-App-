package com.vhn.doan.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Lớp tiện ích để quản lý avatar người dùng
 */
public class AvatarUtils {

    // Danh sách các URL avatar mặc định từ Cloudinary
    private static final List<String> DEFAULT_AVATARS = Arrays.asList(
            "https://res.cloudinary.com/dazo6ypwt/image/upload/v1737556504/cld-sample.jpg",
            "https://res.cloudinary.com/dazo6ypwt/image/upload/v1737556503/samples/upscale-face-1.jpg",
            "https://res.cloudinary.com/dazo6ypwt/image/upload/v1737556503/samples/woman-on-a-football-field.jpg",
            "https://res.cloudinary.com/dazo6ypwt/image/upload/v1737556502/samples/look-up.jpg",
            "https://res.cloudinary.com/dazo6ypwt/image/upload/v1737556502/samples/man-on-a-escalator.jpg",
            "https://res.cloudinary.com/dazo6ypwt/image/upload/v1737556502/samples/man-on-a-street.jpg",
            "https://res.cloudinary.com/dazo6ypwt/image/upload/v1737556501/samples/smile.jpg",
            "https://res.cloudinary.com/dazo6ypwt/image/upload/v1737556502/samples/man-portrait.jpg"
    );

    /**
     * Trả về URL avatar ngẫu nhiên từ danh sách mặc định
     * @return URL của avatar ngẫu nhiên
     */
    public static String getRandomAvatarUrl() {
        Random random = new Random();
        int index = random.nextInt(DEFAULT_AVATARS.size());
        return DEFAULT_AVATARS.get(index);
    }

    /**
     * Kiểm tra xem URL avatar đã cho có phải là avatar mặc định hay không
     * @param avatarUrl URL avatar cần kiểm tra
     * @return true nếu là avatar mặc định, false nếu ngược lại
     */
    public static boolean isDefaultAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return true;
        }

        for (String defaultAvatar : DEFAULT_AVATARS) {
            if (defaultAvatar.equals(avatarUrl)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tạo đường dẫn trên Cloudinary cho avatar người dùng
     * @param userId ID của người dùng
     * @return URL đường dẫn trên Cloudinary
     */
    public static String createCloudinaryAvatarPath(String userId) {
        return "users/" + userId + "/avatar_" + System.currentTimeMillis();
    }
}
