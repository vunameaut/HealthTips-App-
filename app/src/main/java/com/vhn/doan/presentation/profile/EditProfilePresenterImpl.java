package com.vhn.doan.presentation.profile;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vhn.doan.data.User;
import com.vhn.doan.data.repository.UserRepository;
import com.vhn.doan.utils.CloudinaryHelper;

/**
 * Triển khai EditProfilePresenter để xử lý logic cho màn hình chỉnh sửa profile
 */
public class EditProfilePresenterImpl extends EditProfileContract.Presenter {
    private static final String TAG = "EditProfilePresenter";

    private final UserRepository userRepository;
    private final FirebaseAuth firebaseAuth;
    private Context context;

    /**
     * Constructor với dependency injection
     * @param userRepository repository để tương tác với dữ liệu người dùng
     * @param firebaseAuth FirebaseAuth để lấy thông tin người dùng hiện tại
     */
    public EditProfilePresenterImpl(UserRepository userRepository, FirebaseAuth firebaseAuth) {
        this.userRepository = userRepository;
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public void attachView(EditProfileContract.View view) {
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

                // Hiển thị avatar nếu có
                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                    getView().displayUserAvatar(user.getPhotoUrl());
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

                        // Cập nhật URL avatar vào database
                        userRepository.updateUserAvatar(currentUser.getUid(), imageUrl,
                                new UserRepository.UserOperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        if (!isViewAttached()) return;

                                        getView().showLoading(false);
                                        getView().displayUserAvatar(imageUrl);
                                        getView().showAvatarUploadProgress(100);
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
                    public void onUploadError(String errorMessage) {
                        if (!isViewAttached()) return;

                        getView().showLoading(false);
                        getView().showError("Lỗi khi tải ảnh lên: " + errorMessage);
                    }
                });
    }

    @Override
    public void updateUserProfile(String displayName, String phoneNumber) {
        if (!isViewAttached()) return;

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            getView().showError("Không thể cập nhật thông tin. Vui lòng đăng nhập lại.");
            return;
        }

        // Validate dữ liệu đầu vào
        if (displayName == null || displayName.trim().isEmpty()) {
            getView().showError("Tên hiển thị không được để trống");
            return;
        }

        getView().showLoading(true);
        // Truyền null cho bio vì đã loại bỏ trường này
        userRepository.updateUserProfile(currentUser.getUid(), displayName, null, phoneNumber,
                new UserRepository.UserOperationCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isViewAttached()) return;

                        getView().showLoading(false);
                        getView().onProfileUpdateSuccess();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (!isViewAttached()) return;

                        getView().showLoading(false);
                        getView().showError("Lỗi khi cập nhật thông tin: " + errorMessage);
                    }
                });
    }
}
