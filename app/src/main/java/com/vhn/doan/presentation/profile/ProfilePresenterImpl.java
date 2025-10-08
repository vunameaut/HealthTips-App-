package com.vhn.doan.presentation.profile;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.data.User;
import com.vhn.doan.data.repository.UserRepository;
import com.vhn.doan.presentation.profile.ProfileContract.AvatarCallback;
import com.vhn.doan.utils.AvatarUtils;
import com.vhn.doan.utils.CloudinaryHelper;

/**
 * Triển khai của ProfilePresenter theo kiến trúc MVP
 * Xử lý logic cho màn hình profile người dùng
 */
public class ProfilePresenterImpl extends ProfileContract.Presenter {
    private static final String TAG = "ProfilePresenter";

    private final UserRepository userRepository;
    private final FirebaseAuth firebaseAuth;
    private Context context;

    /**
     * Constructor với dependency injection
     * @param userRepository Repository để tương tác với dữ liệu người dùng
     * @param firebaseAuth FirebaseAuth để xử lý đăng xuất
     */
    public ProfilePresenterImpl(UserRepository userRepository, FirebaseAuth firebaseAuth) {
        this.userRepository = userRepository;
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public void attachView(ProfileContract.View view) {
        super.attachView(view);
    }

    /**
     * Thiết lập context cho presenter
     * @param context Context của ứng dụng
     */
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void start() {
        loadUserProfile();
    }

    @Override
    public void loadUserProfile() {
        if (!isViewAttached()) return;

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            getView().showError("Không thể tải thông tin người dùng. Vui lòng đăng nhập lại.");
            return;
        }

        getView().showLoading(true);
        userRepository.getUserByUid(currentUser.getUid(), new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (!isViewAttached()) return;

                getView().showLoading(false);
                getView().displayUserProfile(user);

                // Kiểm tra và hiển thị avatar
                String avatarUrl = user.getPhotoUrl();
                if (avatarUrl == null || avatarUrl.isEmpty()) {
                    // Người dùng chưa có avatar, tạo ngẫu nhiên một avatar từ Cloudinary
                    avatarUrl = AvatarUtils.getRandomAvatarUrl();

                    // Lưu avatar ngẫu nhiên vào cơ sở dữ liệu
                    final String randomAvatarUrl = avatarUrl;
                    userRepository.updateUserAvatar(currentUser.getUid(), randomAvatarUrl,
                            new UserRepository.UserOperationCallback() {
                                @Override
                                public void onSuccess() {
                                    if (isViewAttached()) {
                                        getView().displayUserAvatar(randomAvatarUrl, true);
                                    }
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    // Ngay cả khi lưu thất bại, vẫn hiển thị avatar ngẫu nhiên
                                    if (isViewAttached()) {
                                        getView().displayUserAvatar(randomAvatarUrl, true);
                                    }
                                }
                            });
                } else {
                    // Người dùng đã có avatar, hiển thị
                    boolean isDefaultAvatar = AvatarUtils.isDefaultAvatar(avatarUrl);
                    getView().displayUserAvatar(avatarUrl, isDefaultAvatar);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (!isViewAttached()) return;

                getView().showLoading(false);
                getView().showError("Lỗi: " + errorMessage);
            }
        });
    }

    @Override
    public void logout() {
        if (!isViewAttached()) return;

        firebaseAuth.signOut();
        getView().onLogoutSuccess();
    }

    @Override
    public void refreshUserProfile() {
        loadUserProfile();
    }

    @Override
    public void updateUserAvatar(Uri imageUri) {
        if (!isViewAttached() || imageUri == null || context == null) return;

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            getView().showError("Không thể cập nhật avatar. Vui lòng đăng nhập lại.");
            return;
        }

        getView().showLoading(true);
        CloudinaryHelper.uploadUserAvatar(context, imageUri, currentUser.getUid(),
                new CloudinaryHelper.CloudinaryUploadCallback() {
                    @Override
                    public void onUploadStart() {
                        if (!isViewAttached()) return;
                        getView().showAvatarUploadProgress(0);
                    }

                    @Override
                    public void onUploadProgress(int progress) {
                        if (!isViewAttached()) return;
                        getView().showAvatarUploadProgress(progress);
                    }

                    @Override
                    public void onUploadSuccess(String imageUrl) {
                        if (!isViewAttached()) return;
                        updateUserAvatar(imageUrl);
                    }

                    @Override
                    public void onUploadError(String errorMessage) {
                        if (!isViewAttached()) return;
                        getView().showLoading(false);
                        getView().showError("Lỗi khi tải ảnh lên: " + errorMessage);
                    }
                });
    }

    // Phương thức hỗ trợ để cập nhật URL avatar trong database
    private void updateUserAvatar(String avatarUrl) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || !isViewAttached()) return;

        userRepository.updateUserAvatar(currentUser.getUid(), avatarUrl,
                new UserRepository.UserOperationCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isViewAttached()) return;

                        getView().showLoading(false);
                        getView().displayUserAvatar(avatarUrl, AvatarUtils.isDefaultAvatar(avatarUrl));
                        getView().showProfileUpdateResult(true, "Đã cập nhật ảnh đại diện.");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (!isViewAttached()) return;

                        getView().showLoading(false);
                        getView().showError("Lỗi khi cập nhật avatar: " + errorMessage);
                    }
                });
    }

    @Override
    public void updateUserProfile(String displayName, String bio, String phoneNumber) {
        if (!isViewAttached()) return;

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            getView().showError("Không thể cập nhật thông tin. Vui lòng đăng nhập lại.");
            return;
        }

        getView().showLoading(true);
        userRepository.updateUserProfile(currentUser.getUid(), displayName, bio, phoneNumber,
                new UserRepository.UserOperationCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isViewAttached()) return;

                        getView().showLoading(false);
                        getView().showProfileUpdateResult(true, "Đã cập nhật thông tin cá nhân.");
                        refreshUserProfile();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (!isViewAttached()) return;

                        getView().showLoading(false);
                        getView().showError("Lỗi khi cập nhật thông tin: " + errorMessage);
                    }
                });
    }

    @Override
    public void viewFullAvatar() {
        // Phương thức này có thể được triển khai sau
        // để hiển thị avatar ở kích thước đầy đủ
        Log.d(TAG, "Chức năng xem avatar đầy đủ chưa được triển khai");
    }

    @Override
    public void getCurrentUserAvatar(AvatarCallback callback) {
        if (callback == null) return;

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onAvatarLoaded(null);
            return;
        }

        userRepository.getUserByUid(currentUser.getUid(), new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getPhotoUrl() != null) {
                    callback.onAvatarLoaded(user.getPhotoUrl());
                } else if (currentUser.getPhotoUrl() != null) {
                    // Fallback sử dụng URL từ FirebaseAuth nếu không có trong database
                    callback.onAvatarLoaded(currentUser.getPhotoUrl().toString());
                } else {
                    // Không có avatar nào được tìm thấy
                    callback.onAvatarLoaded(null);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Lỗi khi lấy thông tin avatar: " + errorMessage);
                // Fallback sử dụng URL từ FirebaseAuth
                if (currentUser.getPhotoUrl() != null) {
                    callback.onAvatarLoaded(currentUser.getPhotoUrl().toString());
                } else {
                    callback.onAvatarLoaded(null);
                }
            }
        });
    }
}
