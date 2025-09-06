package com.vhn.doan.presentation.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.vhn.doan.R;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.SearchHistory;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.data.repository.SearchRepository;
import com.vhn.doan.data.repository.SearchRepositoryImpl;
import com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity;
import com.vhn.doan.presentation.video.SingleVideoPlayerActivity;
import com.vhn.doan.presentation.video.VideoActivity;
import com.vhn.doan.utils.FirebaseAuthHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị giao diện tìm kiếm, cho phép người dùng tìm kiếm bài viết và video
 */
public class SearchActivity extends AppCompatActivity implements SearchContract.View {
    private EditText etSearch;
    private ImageButton btnBack;
    private ImageButton btnSearch;
    private ViewFlipper searchViewFlipper;
    private ProgressBar progressSearch;
    private RecyclerView rvSearchHistory;
    private TextView tvClearAllHistory;
    private TextView tvSeeMoreHistory;
    private View layoutSearchHistoryHeader;
    private View layoutNoSearchHistory;
    private TabLayout tabLayoutSearch;
    private ViewPager2 viewPagerSearch;

    private SearchContract.Presenter mPresenter;
    private SearchHistoryAdapter historyAdapter;
    private List<SearchHistory> searchHistoryList = new ArrayList<>();
    private List<SearchHistory> displayedHistoryList = new ArrayList<>();
    private HealthTipSearchResultsFragment healthTipFragment;
    private VideoSearchResultsFragment videoFragment;
    private SearchResultsPagerAdapter pagerAdapter;

    private boolean isHistoryExpanded = false;
    private static final int INITIAL_HISTORY_LIMIT = 5;
    private static final int EXPANDED_HISTORY_LIMIT = 10;
    private static final int VIEW_SEARCH_HISTORY = 0;
    private static final int VIEW_SEARCH_RESULTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Khởi tạo các view
        initViews();

        // Khởi tạo presenter
        FirebaseAuthHelper authHelper = new FirebaseAuthHelper();
        SearchRepository searchRepository = new SearchRepositoryImpl();
        mPresenter = new SearchPresenter(searchRepository, authHelper);
        mPresenter.attachView(this);

        // Thiết lập adapter cho lịch sử tìm kiếm
        setupSearchHistoryAdapter();

        // Thiết lập các sự kiện
        setupEvents();

        // Thiết lập ViewPager và TabLayout cho kết quả tìm kiếm
        setupViewPagerAndTabs();

        // Tải lịch sử tìm kiếm
        mPresenter.loadSearchHistory();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        btnBack = findViewById(R.id.btn_back);
        btnSearch = findViewById(R.id.btn_search);
        searchViewFlipper = findViewById(R.id.search_view_flipper);
        progressSearch = findViewById(R.id.progress_search);
        rvSearchHistory = findViewById(R.id.rv_search_history);
        tvClearAllHistory = findViewById(R.id.tv_clear_all_history);
        tvSeeMoreHistory = findViewById(R.id.tv_see_more_history);
        layoutSearchHistoryHeader = findViewById(R.id.layout_search_history_header);
        layoutNoSearchHistory = findViewById(R.id.layout_no_search_history);
        tabLayoutSearch = findViewById(R.id.tab_layout_search);
        viewPagerSearch = findViewById(R.id.viewpager_search);
    }

    private void setupSearchHistoryAdapter() {
        rvSearchHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new SearchHistoryAdapter(this, searchHistoryList);
        rvSearchHistory.setAdapter(historyAdapter);

        // Thiết lập sự kiện click vào mục lịch sử tìm kiếm
        historyAdapter.setOnSearchHistoryClickListener(keyword -> {
            etSearch.setText(keyword);
            performSearch(keyword);
        });

        // Thiết lập sự kiện xóa mục lịch sử tìm kiếm
        historyAdapter.setOnSearchHistoryDeleteListener(searchHistory -> {
            mPresenter.deleteSearchHistory(searchHistory.getId());
        });
    }

    private void setupEvents() {
        // Sự kiện nút quay lại
        btnBack.setOnClickListener(v -> handleBackAction());

        // Sự kiện nút tìm kiếm
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            performSearch(keyword);
        });

        // Sự kiện nút xóa nội dung tìm kiếm
        ImageButton btnClear = findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            btnClear.setVisibility(View.GONE);
        });

        // Theo dõi thay đổi nội dung trong ô tìm kiếm
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hiển thị hoặc ẩn nút xóa dựa vào có nội dung hay không
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Không cần xử lý
            }
        });

        // Sự kiện ấn Enter trên bàn phím khi nhập tìm kiếm
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String keyword = etSearch.getText().toString().trim();
                performSearch(keyword);
                return true;
            }
            return false;
        });

        // Sự kiện xóa tất cả lịch sử tìm kiếm
        tvClearAllHistory.setOnClickListener(v -> {
            mPresenter.clearAllSearchHistory();
        });

        // Sự kiện xem thêm lịch sử tìm kiếm
        tvSeeMoreHistory.setOnClickListener(v -> {
            toggleSearchHistoryVisibility();
        });
    }

    private void toggleSearchHistoryVisibility() {
        isHistoryExpanded = !isHistoryExpanded;
        int limit = isHistoryExpanded ? EXPANDED_HISTORY_LIMIT : INITIAL_HISTORY_LIMIT;

        // Cập nhật danh sách lịch sử tìm kiếm hiển thị
        displayedHistoryList.clear();
        if (searchHistoryList.size() <= limit) {
            displayedHistoryList.addAll(searchHistoryList);
        } else {
            displayedHistoryList.addAll(searchHistoryList.subList(0, limit));
        }
        historyAdapter.updateDisplayedHistory(displayedHistoryList);

        // Cập nhật văn bản nút "Xem thêm"
        tvSeeMoreHistory.setText(isHistoryExpanded ? R.string.see_less_history : R.string.see_more_history);

        // Cuộn đến vị trí cuối của danh sách lịch sử tìm kiếm
        rvSearchHistory.smoothScrollToPosition(historyAdapter.getItemCount() - 1);
    }

    private void setupViewPagerAndTabs() {
        // Khởi tạo các fragment
        healthTipFragment = HealthTipSearchResultsFragment.newInstance();
        videoFragment = VideoSearchResultsFragment.newInstance();

        // Thiết lập sự kiện click vào video
        videoFragment.setVideoItemClickListener(video -> {
            // Mở màn hình phát video đơn lẻ với toàn bộ video object để đảm bảo trạng thái like được truyền
            Intent intent = SingleVideoPlayerActivity.createIntent(SearchActivity.this, video);
            startActivity(intent);
        });

        // Thiết lập ViewPager
        pagerAdapter = new SearchResultsPagerAdapter(this, healthTipFragment, videoFragment);
        viewPagerSearch.setAdapter(pagerAdapter);

        // Thiết lập TabLayout với ViewPager
        new TabLayoutMediator(tabLayoutSearch, viewPagerSearch, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.search_articles_tab);
                    break;
                case 1:
                    tab.setText(R.string.search_videos_tab);
                    break;
            }
        }).attach();
    }

    private void performSearch(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            // Ẩn bàn phím ảo khi thực hiện tìm kiếm
            hideKeyboard();

            // Thực hiện tìm kiếm
            mPresenter.search(keyword);
        } else {
            Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Phương thức ẩn bàn phím ảo
     */
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                    getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void showSearchHistory(List<SearchHistory> searchHistories) {
        searchHistoryList.clear();
        if (searchHistories != null) {
            searchHistoryList.addAll(searchHistories);
        }

        // Reset trạng thái hiển thị mở rộng
        isHistoryExpanded = false;

        // Cập nhật danh sách hiển thị với số lượng giới hạn ban đầu
        updateDisplayedHistory();

        // Hiển thị/ẩn nút "Xem thêm" nếu cần
        tvSeeMoreHistory.setVisibility(searchHistoryList.size() > INITIAL_HISTORY_LIMIT ? View.VISIBLE : View.GONE);
        tvSeeMoreHistory.setText(R.string.see_more_history);

        // Hiển thị giao diện phù hợp
        if (searchHistoryList.isEmpty()) {
            layoutNoSearchHistory.setVisibility(View.VISIBLE);
            rvSearchHistory.setVisibility(View.GONE);
            tvSeeMoreHistory.setVisibility(View.GONE);
        } else {
            layoutNoSearchHistory.setVisibility(View.GONE);
            rvSearchHistory.setVisibility(View.VISIBLE);
        }

        // Hiển thị layout lịch sử tìm kiếm
        searchViewFlipper.setDisplayedChild(VIEW_SEARCH_HISTORY);
    }

    /**
     * Cập nhật danh sách lịch sử tìm kiếm được hiển thị dựa trên trạng thái hiện tại
     */
    private void updateDisplayedHistory() {
        displayedHistoryList.clear();

        int limit = isHistoryExpanded ? EXPANDED_HISTORY_LIMIT : INITIAL_HISTORY_LIMIT;

        if (searchHistoryList.size() <= limit) {
            displayedHistoryList.addAll(searchHistoryList);
        } else {
            displayedHistoryList.addAll(searchHistoryList.subList(0, limit));
        }

        historyAdapter.updateDisplayedHistory(displayedHistoryList);
    }

    @Override
    public void showHealthTipResults(List<HealthTip> healthTips) {
        if (healthTipFragment != null) {
            healthTipFragment.updateResults(healthTips);

            // Chuyển đổi sang màn hình kết quả tìm kiếm
            searchViewFlipper.setDisplayedChild(VIEW_SEARCH_RESULTS);

            // Chọn tab bài viết
            viewPagerSearch.setCurrentItem(0);
        }
    }

    @Override
    public void showVideoResults(List<ShortVideo> videos) {
        if (videoFragment != null) {
            videoFragment.updateResults(videos);

            // Chuyển đổi sang màn hình kết quả tìm kiếm nếu chưa chuyển
            if (searchViewFlipper.getDisplayedChild() != VIEW_SEARCH_RESULTS) {
                searchViewFlipper.setDisplayedChild(VIEW_SEARCH_RESULTS);
            }
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading(boolean isLoading) {
        progressSearch.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showNoResults() {
        // Đảm bảo chuyển sang hiển thị màn hình kết quả tìm kiếm trước
        searchViewFlipper.setDisplayedChild(VIEW_SEARCH_RESULTS);

        // Kiểm tra xem có kết quả nào hay không dựa trên dữ liệu thực tế từ fragment
        if (healthTipFragment != null && videoFragment != null) {
            boolean hasNoHealthTipResults = healthTipFragment.isResultsEmpty();
            boolean hasNoVideoResults = videoFragment.isResultsEmpty();

            // Chọn tab phù hợp để hiển thị kết quả có sẵn (nếu có)
            if (!hasNoHealthTipResults && hasNoVideoResults) {
                viewPagerSearch.setCurrentItem(0); // Chuyển đến tab bài viết
            } else if (hasNoHealthTipResults && !hasNoVideoResults) {
                viewPagerSearch.setCurrentItem(1); // Chuyển đến tab video
            }
        }
    }

    /**
     * Xử lý hành động quay lại
     * - Nếu đang ở màn hình kết quả tìm kiếm: quay về màn hình lịch sử tìm kiếm
     * - Nếu đang ở màn hình lịch sử tìm kiếm: kết thúc activity
     */
    private void handleBackAction() {
        // Nếu đang hiển thị kết quả tìm kiếm
        if (searchViewFlipper.getDisplayedChild() == VIEW_SEARCH_RESULTS) {
            // Quay về màn hình lịch sử tìm kiếm
            searchViewFlipper.setDisplayedChild(VIEW_SEARCH_HISTORY);
            return;
        }

        // Ngược lại, kết thúc activity
        finish();
    }

    @Override
    public void onBackPressed() {
        handleBackAction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
        }
    }
}
