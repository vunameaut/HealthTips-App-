package com.vhn.doan.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.SearchHistory;
import com.vhn.doan.data.ShortVideo;
import com.vhn.doan.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation của SearchRepository để thực hiện các thao tác tìm kiếm qua Firebase
 */
public class SearchRepositoryImpl implements SearchRepository {
    private final DatabaseReference mDatabase;

    public SearchRepositoryImpl() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void searchHealthTips(String keyword, RepositoryCallback<List<HealthTip>> callback) {
        // Chuyển keyword về lowercase để tìm kiếm không phân biệt hoa thường
        keyword = keyword.toLowerCase();
        final String searchKeyword = keyword;

        // Tìm kiếm trong tất cả bài viết
        Query query = mDatabase.child("healthTips");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<HealthTip> results = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthTip healthTip = snapshot.getValue(HealthTip.class);
                    if (healthTip != null) {
                        // Tìm kiếm trong title, content hoặc categoryName
                        boolean matchesTitle = healthTip.getTitle() != null &&
                                healthTip.getTitle().toLowerCase().contains(searchKeyword);
                        boolean matchesContent = healthTip.getContent() != null &&
                                healthTip.getContent().toLowerCase().contains(searchKeyword);
                        boolean matchesCategory = healthTip.getCategoryName() != null &&
                                healthTip.getCategoryName().toLowerCase().contains(searchKeyword);

                        if (matchesTitle || matchesContent || matchesCategory) {
                            results.add(healthTip);
                        }
                    }
                }

                callback.onSuccess(results);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void searchVideos(String keyword, RepositoryCallback<List<ShortVideo>> callback) {
        // Chuyển keyword về lowercase để tìm kiếm không phân biệt hoa thường
        keyword = keyword.toLowerCase();
        final String searchKeyword = keyword;

        // Tìm kiếm trong tất cả video
        Query query = mDatabase.child("videos");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ShortVideo> results = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ShortVideo video = snapshot.getValue(ShortVideo.class);
                    if (video != null) {
                        // Tìm kiếm trong title, caption, tags
                        boolean matchesTitle = video.getTitle() != null &&
                                video.getTitle().toLowerCase().contains(searchKeyword);
                        boolean matchesCaption = video.getCaption() != null &&
                                video.getCaption().toLowerCase().contains(searchKeyword);
                        boolean matchesTags = false;

                        // Tìm trong tags
                        Map<String, Boolean> tags = video.getTags();
                        if (tags != null) {
                            for (String tag : tags.keySet()) {
                                if (tag.toLowerCase().contains(searchKeyword)) {
                                    matchesTags = true;
                                    break;
                                }
                            }
                        }

                        if (matchesTitle || matchesCaption || matchesTags) {
                            results.add(video);
                        }
                    }
                }

                callback.onSuccess(results);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void saveSearchHistory(String keyword, String userId, RepositoryCallback<Boolean> callback) {
        if (userId == null || userId.isEmpty() || keyword == null || keyword.trim().isEmpty()) {
            callback.onError("Thiếu thông tin người dùng hoặc từ khóa tìm kiếm");
            return;
        }

        final String trimmedKeyword = keyword.trim();

        // Kiểm tra xem từ khóa đã tồn tại trong lịch sử chưa
        mDatabase.child("searchHistories").child(userId)
                .orderByChild("keyword")
                .equalTo(trimmedKeyword)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Từ khóa đã tồn tại, xóa phiên bản cũ và tạo mới để đưa lên đầu danh sách
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String historyId = snapshot.getKey();
                                if (historyId != null) {
                                    // Xóa mục cũ
                                    mDatabase.child("searchHistories").child(userId).child(historyId)
                                            .removeValue()
                                            .addOnSuccessListener(aVoid -> {
                                                // Sau khi xóa thành công, tạo mới để đảm bảo nó ở vị trí đầu tiên
                                                createNewSearchHistory(trimmedKeyword, userId, callback);
                                            })
                                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                                    return; // Thoát sau khi xử lý mục đầu tiên tìm thấy
                                }
                            }
                        } else {
                            // Từ khóa chưa tồn tại, thêm mới
                            createNewSearchHistory(trimmedKeyword, userId, callback);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });

        // Lưu từ khóa cho đề xuất
        saveKeywordForSuggestion(trimmedKeyword, userId);
    }

    /**
     * Helper method để tạo mục lịch sử tìm kiếm mới
     */
    private void createNewSearchHistory(String keyword, String userId, RepositoryCallback<Boolean> callback) {
        DatabaseReference searchHistoryRef = mDatabase.child("searchHistories").child(userId).push();
        String historyId = searchHistoryRef.getKey();

        // Tạo đối tượng SearchHistory với timestamp hiện tại
        SearchHistory searchHistory = new SearchHistory(historyId, userId, keyword);

        searchHistoryRef.setValue(searchHistory)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getSearchHistory(String userId, int limit, RepositoryCallback<List<SearchHistory>> callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("Thiếu thông tin người dùng");
            return;
        }

        // Truy vấn lịch sử tìm kiếm của người dùng, sắp xếp theo timestamp giảm dần (mới nhất lên đầu)
        Query query = mDatabase.child("searchHistories").child(userId)
                .orderByChild("timestamp")
                .limitToLast(limit);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<SearchHistory> historyList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    SearchHistory history = snapshot.getValue(SearchHistory.class);
                    if (history != null) {
                        historyList.add(0, history); // Thêm vào đầu danh sách để duy trì thứ tự mới nhất lên đầu
                    }
                }

                callback.onSuccess(historyList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    @Override
    public void deleteSearchHistory(String searchHistoryId, String userId, RepositoryCallback<Boolean> callback) {
        if (userId == null || userId.isEmpty() || searchHistoryId == null || searchHistoryId.isEmpty()) {
            callback.onError("Thiếu thông tin người dùng hoặc ID lịch sử tìm kiếm");
            return;
        }

        mDatabase.child("searchHistories").child(userId).child(searchHistoryId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void clearAllSearchHistory(String userId, RepositoryCallback<Boolean> callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("Thiếu thông tin người dùng");
            return;
        }

        mDatabase.child("searchHistories").child(userId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void saveKeywordForSuggestion(String keyword, String userId) {
        if (userId == null || userId.isEmpty() || keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        // Lưu từ khóa vào danh sách đề xuất của người dùng
        DatabaseReference userPreferencesRef = mDatabase.child("userPreferences").child(userId).child("keywords");

        // Thêm hoặc cập nhật từ khóa với timestamp hiện tại
        Map<String, Object> updates = new HashMap<>();
        updates.put(keyword.trim().toLowerCase(), System.currentTimeMillis());

        userPreferencesRef.updateChildren(updates);
    }
}
