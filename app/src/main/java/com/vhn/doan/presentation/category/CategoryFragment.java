package com.vhn.doan.presentation.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.Category;
import com.vhn.doan.data.repository.CategoryRepository;
import com.vhn.doan.data.repository.CategoryRepositoryImpl;
import com.vhn.doan.utils.Constants;

import java.util.List;

/**
 * Fragment hiển thị tất cả các chủ đề sức khỏe
 */
public class CategoryFragment extends Fragment implements CategoryView, CategoryAdapter.OnCategoryClickListener {

    // Views
    private RecyclerView recyclerViewCategories;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private TextView textViewError;

    // Adapter và Presenter
    private CategoryAdapter adapter;
    private CategoryPresenter presenter;

    public CategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Tạo instance mới của CategoryFragment
     */
    public static CategoryFragment newInstance() {
        return new CategoryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout cho fragment
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các views
        initViews(view);

        // Khởi tạo adapter
        setupRecyclerView();

        // Khởi tạo presenter
        initPresenter();

        // Tải dữ liệu
        loadData();
    }

    /**
     * Khởi tạo các views trong fragment
     */
    private void initViews(View view) {
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        textViewError = view.findViewById(R.id.textViewError);
    }

    /**
     * Thiết lập RecyclerView và Adapter
     */
    private void setupRecyclerView() {
        // Sử dụng GridLayoutManager với 2 cột
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerViewCategories.setLayoutManager(layoutManager);

        // Khởi tạo adapter và gắn vào recyclerView
        adapter = new CategoryAdapter(requireContext(), this);
        recyclerViewCategories.setAdapter(adapter);
    }

    /**
     * Khởi tạo presenter
     */
    private void initPresenter() {
        CategoryRepository categoryRepository = new CategoryRepositoryImpl();
        presenter = new CategoryPresenter(this, categoryRepository);
    }

    /**
     * Tải dữ liệu danh mục
     */
    private void loadData() {
        // Hiển thị loading và tải dữ liệu
        presenter.loadCategories();
        // Đăng ký lắng nghe thay đổi dữ liệu realtime
        presenter.startListeningToCategories();
    }

    @Override
    public void displayCategories(List<Category> categories) {
        if (isAdded()) {
            recyclerViewCategories.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            textViewError.setVisibility(View.GONE);

            // Cập nhật adapter với dữ liệu mới
            adapter.setCategories(categories);
        }
    }

    @Override
    public void showEmptyView() {
        if (isAdded()) {
            recyclerViewCategories.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            textViewError.setVisibility(View.GONE);
        }
    }

    /**
     * Phương thức cập nhật trạng thái loading từ BaseView
     * @param loading true để hiển thị loading, false để ẩn
     */
    @Override
    public void showLoading(boolean loading) {
        if (isAdded()) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showLoading() {
        showLoading(true);
    }

    @Override
    public void hideLoading() {
        showLoading(false);
    }

    @Override
    public void showError(String message) {
        if (isAdded()) {
            recyclerViewCategories.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
            textViewError.setVisibility(View.VISIBLE);
            textViewError.setText(message);
        }
    }

    @Override
    public void showMessage(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void navigateToCategoryDetail(Category category) {
        if (isAdded()) {
            // Tạo bundle với thông tin danh mục
            Bundle args = new Bundle();
            args.putString(Constants.INTENT_CATEGORY_ID, category.getId());

            // TODO: Thực hiện điều hướng đến màn hình chi tiết danh mục
            // Navigation Component hoặc Fragment Transaction có thể được sử dụng ở đây

            // Ví dụ sử dụng Navigation Component:
            // Navigation.findNavController(requireView())
            //         .navigate(R.id.action_categoryFragment_to_categoryDetailFragment, args);

            // Tạm thời hiển thị thông báo
            showMessage("Đã chọn danh mục: " + category.getName());
        }
    }

    @Override
    public void onCategoryClick(Category category) {
        // Chuyển sự kiện click đến presenter
        presenter.onCategorySelected(category);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dọn dẹp resources
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}
