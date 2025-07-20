package com.vhn.doan.presentation.category;

import com.vhn.doan.data.Category;
import com.vhn.doan.data.repository.CategoryRepository;
import com.vhn.doan.presentation.base.BasePresenter;

import java.util.List;

/**
 * Presenter xử lý logic cho màn hình danh sách danh mục
 */
public class CategoryPresenter extends BasePresenter<CategoryView> {

    private final CategoryRepository categoryRepository;
    private Object categoryListener;

    /**
     * Constructor
     * @param view Interface view cung cấp phương thức hiển thị
     * @param categoryRepository Repository cung cấp dữ liệu danh mục
     */
    public CategoryPresenter(CategoryView view, CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        attachView(view);
    }

    /**
     * Tải danh sách danh mục từ repository
     */
    public void loadCategories() {
        if (isViewAttached()) {
            view.showLoading();
        }

        categoryRepository.getAllCategories(new CategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (isViewAttached()) {
                    view.hideLoading();
                    if (categories != null && !categories.isEmpty()) {
                        view.displayCategories(categories);
                    } else {
                        view.showEmptyView();
                    }
                }
            }

            @Override
            public void onSingleCategoryLoaded(Category category) {
                // Không sử dụng trong trường hợp này
            }

            @Override
            public void onError(String errorMessage) {
                if (isViewAttached()) {
                    view.hideLoading();
                    view.showError(errorMessage);
                }
            }
        });
    }

    /**
     * Lắng nghe thay đổi từ danh sách danh mục real-time
     */
    public void startListeningToCategories() {
        categoryListener = categoryRepository.listenToCategories(new CategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (isViewAttached()) {
                    view.hideLoading();
                    if (categories != null && !categories.isEmpty()) {
                        view.displayCategories(categories);
                    } else {
                        view.showEmptyView();
                    }
                }
            }

            @Override
            public void onSingleCategoryLoaded(Category category) {
                // Không sử dụng trong trường hợp này
            }

            @Override
            public void onError(String errorMessage) {
                if (isViewAttached()) {
                    view.hideLoading();
                    view.showError(errorMessage);
                }
            }
        });
    }

    /**
     * Xử lý khi người dùng chọn một danh mục
     * @param category Danh mục được chọn
     */
    public void onCategorySelected(Category category) {
        if (isViewAttached()) {
            view.navigateToCategoryDetail(category);
        }
    }

    /**
     * Dọn dẹp tài nguyên khi presenter không còn được sử dụng
     */
    public void onDestroy() {
        if (categoryListener != null) {
            categoryRepository.removeListener(categoryListener);
            categoryListener = null;
        }
        detachView();
    }
}
