# Chuẩn bị dữ liệu cho tính năng Video Short cá nhân hóa

## 1. Mô hình dữ liệu

```java
package com.vhn.doan.data.models;

import com.google.firebase.database.PropertyName;
import java.util.List;

public class HealthTipVideo {
    @PropertyName("id")
    private String id;

    @PropertyName("title")
    private String title;

    @PropertyName("description")
    private String description;

    @PropertyName("videoUrl")
    private String videoUrl;

    @PropertyName("thumbnailUrl")
    private String thumbnailUrl;

    @PropertyName("categoryId")
    private String categoryId;

    @PropertyName("tags")
    private List<String> tags;

    @PropertyName("viewCount")
    private int viewCount;

    @PropertyName("likeCount")
    private int likeCount;

    @PropertyName("duration")
    private int duration; // thời lượng video tính bằng giây

    @PropertyName("publishDate")
    private long publishDate;

    @PropertyName("authorId")
    private String authorId;

    // Constructor và các getter/setter
}
```

## 2. Cấu trúc Firebase Realtime Database

```
healthtips-app/
├── videos/
│   ├── video1_id/
│   │   ├── id: "video1_id"
│   │   ├── title: "5 cách uống nước đúng cách"
│   │   ├── description: "Hãy học cách..."
│   │   ├── videoUrl: "https://storage.../videos/video1.mp4"
│   │   ├── thumbnailUrl: "https://storage.../thumbnails/thumb1.jpg"
│   │   ├── categoryId: "category1_id"
│   │   ├── tags: ["uống nước", "thủy phân", "sức khỏe"]
│   │   ├── viewCount: 1200
│   │   ├── likeCount: 350
│   │   ├── duration: 45
│   │   ├── publishDate: 1654321234567
│   │   └── authorId: "user1_id"
│
├── user_likes/
│   ├── user1_id/
│   │   ├── video1_id: true
│   │   └── ...
│
├── user_views/
│   ├── user1_id/
│   │   ├── video1_id: 1654987654321 (timestamp xem)
│   │   └── ...
│
├── user_searches/
│   ├── user1_id/
│   │   ├── "uống nước": 1654987654321
│   │   └── ...
│
├── user_ai_interactions/
│   ├── user1_id/
│   │   ├── interaction1_id: {
│   │   │   text: "Làm thế nào để hạ huyết áp?",
│   │   │   extracted_keywords: ["huyết áp", "tim mạch"]
│   │   │   timestamp: 1654987654321
│   │   └── ...
│
├── video_categories/
│   ├── category1_id/
│   │   ├── name: "Sức khỏe tim mạch"
│   │   ├── videos/
│   │   │   ├── video1_id: true
│   │   │   └── ...
│
└── video_tags/
    ├── "uống nước"/
    │   ├── video1_id: true
    │   └── ...
```

## 3. Chuẩn bị dữ liệu test

### Bước 1: Chuẩn bị video và metadata
- Thu thập 10-15 video ngắn (15-60 giây) về mẹo sức khỏe
- Phân loại thành 5-7 chủ đề chính (ví dụ: dinh dưỡng, thể dục, sức khỏe tinh thần...)
- Chuẩn bị metadata đầy đủ cho mỗi video: tiêu đề, mô tả, tags (5-10 tags/video)

### Bước 2: Tải lên Firebase
1. **Tải video lên Firebase Storage**:
   ```
   /videos/video1.mp4
   /videos/video2.mp4
   /thumbnails/thumb1.jpg
   /thumbnails/thumb2.jpg
   ```

2. **Tạo script tự động để tạo dữ liệu test** (có thể viết bằng Node.js hoặc dùng Firebase Admin SDK):

```javascript
const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://your-project-id.firebaseio.com'
});

const db = admin.database();

// Tạo dữ liệu video
async function createTestData() {
  // 1. Tạo danh mục
  const categories = [
    {id: 'cat1', name: 'Dinh dưỡng'},
    {id: 'cat2', name: 'Thể dục'},
    {id: 'cat3', name: 'Sức khỏe tinh thần'}
  ];
  
  // 2. Tạo dữ liệu video
  const videos = [
    {
      id: 'vid1',
      title: '5 thức uống tăng cường miễn dịch',
      description: 'Các loại nước ép và sinh tố giúp tăng cường sức đề kháng...',
      videoUrl: 'https://storage.../videos/vid1.mp4',
      thumbnailUrl: 'https://storage.../thumbnails/thumb1.jpg',
      categoryId: 'cat1',
      tags: ['miễn dịch', 'nước ép', 'vitamin c', 'dinh dưỡng'],
      viewCount: Math.floor(Math.random() * 1000),
      likeCount: Math.floor(Math.random() * 300),
      duration: 45,
      publishDate: Date.now() - Math.floor(Math.random() * 30 * 24 * 60 * 60 * 1000),
      authorId: 'admin'
    },
    // Thêm 9-14 video khác
  ];
  
  // 3. Tạo dữ liệu giả về lịch sử xem, like, tìm kiếm
  const testUsers = ['user1', 'user2', 'user3'];
  
  // Upload tất cả lên Firebase
  const updates = {};
  
  // Upload video data
  videos.forEach(video => {
    updates[`videos/${video.id}`] = video;
    
    // Thêm vào video_categories
    updates[`video_categories/${video.categoryId}/videos/${video.id}`] = true;
    
    // Thêm vào video_tags
    video.tags.forEach(tag => {
      updates[`video_tags/${tag}/${video.id}`] = true;
    });
  });
  
  // Tạo dữ liệu người dùng test
  testUsers.forEach(userId => {
    // Mỗi user like một số video ngẫu nhiên
    const likedVideos = videos
      .filter(() => Math.random() > 0.7)
      .forEach(video => {
        updates[`user_likes/${userId}/${video.id}`] = true;
      });
      
    // Mỗi user xem một số video ngẫu nhiên
    videos
      .filter(() => Math.random() > 0.5)
      .forEach(video => {
        updates[`user_views/${userId}/${video.id}`] = Date.now() - Math.floor(Math.random() * 7 * 24 * 60 * 60 * 1000);
      });
      
    // Mỗi user tìm kiếm một số từ khóa
    const searchKeywords = ['vitamin', 'ngủ ngon', 'huyết áp', 'đau lưng', 'tập thể dục'];
    searchKeywords
      .filter(() => Math.random() > 0.6)
      .forEach(keyword => {
        updates[`user_searches/${userId}/${keyword}`] = Date.now() - Math.floor(Math.random() * 10 * 24 * 60 * 60 * 1000);
      });
  });
  
  // Thực hiện update
  return db.ref().update(updates);
}

createTestData()
  .then(() => console.log('Đã tạo dữ liệu test thành công'))
  .catch(err => console.error('Lỗi:', err));
```

> **Lưu ý quan trọng**: Dữ liệu test này sẽ được thêm vào file `firebase_test_data.json` để import vào Firebase. Các video và thumbnail thực tế sẽ được upload vào Firebase Storage theo cấu trúc thư mục phân theo danh mục như sau:
> 
> ```
> /videos/
> ├── giamcan/    # Video về giảm cân
> ├── tapluyen/   # Video về tập luyện
> ├── dinhduong/  # Video về dinh dưỡng
> ├── suckhoe/    # Video về sức khỏe chung
> 
> /thumbnails/
> ├── giamcan/    # Thumbnail cho video giảm cân
> ├── tapluyen/   # Thumbnail cho video tập luyện
> ├── dinhduong/  # Thumbnail cho video dinh dưỡng
> ├── suckhoe/    # Thumbnail cho video sức khỏe chung
> ```
> 
> Hiện tại, chúng ta sử dụng 4 danh mục chính này, nhưng trong tương lai có thể bổ sung thêm các danh mục khác. Khi thực hiện upload video và thumbnail, cần đặt tên file theo {videoId} và đưa vào đúng thư mục danh mục.

## 4. Thuật toán đề xuất cá nhân hóa

Để cá nhân hóa đề xuất video, bạn cần triển khai một `VideoRecommendationEngine`:

```java
package com.vhn.doan.data.recommendation;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.vhn.doan.data.models.HealthTipVideo;
import com.vhn.doan.data.repositories.HealthTipVideoRepository;

import javax.inject.Inject;
import java.util.*;

public class VideoRecommendationEngine {
    private final FirebaseDatabase database;
    private final HealthTipVideoRepository videoRepository;
    private final String currentUserId;

    @Inject
    public VideoRecommendationEngine(FirebaseDatabase database, HealthTipVideoRepository videoRepository) {
        this.database = database;
        this.videoRepository = videoRepository;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Task<List<HealthTipVideo>> getRecommendedVideos(int limit) {
        return Tasks.call(() -> {
            Map<String, Double> userPreferences = new HashMap<>();
            
            // 1. Lấy sở thích người dùng từ /users/{userId}/preferences
            Task<DataSnapshot> preferencesTask = database.getReference("users")
                .child(currentUserId)
                .child("preferences")
                .get();
                
            // 2. Lấy chủ đề AI từ /user_topics/{userId}
            Task<DataSnapshot> topicsTask = database.getReference("user_topics")
                .child(currentUserId)
                .get();
                
            // 3. Lấy danh sách video đã like
            Task<DataSnapshot> likesTask = database.getReference("user_likes")
                .child(currentUserId)
                .get();
                
            // 4. Lấy danh sách video đã xem
            Task<DataSnapshot> viewsTask = database.getReference("user_views")
                .child(currentUserId)
                .get();
            
            // Đợi tất cả các Task hoàn thành
            Tasks.await(Tasks.whenAll(preferencesTask, topicsTask, likesTask, viewsTask));
            
            // Xử lý sở thích người dùng
            DataSnapshot preferencesSnapshot = Tasks.await(preferencesTask);
            if (preferencesSnapshot.exists()) {
                for (DataSnapshot tagSnapshot : preferencesSnapshot.getChildren()) {
                    userPreferences.put(tagSnapshot.getKey(), tagSnapshot.getValue(Double.class));
                }
            }
            
            // Xử lý chủ đề AI và ánh xạ sang tag
            DataSnapshot topicsSnapshot = Tasks.await(topicsTask);
            if (topicsSnapshot.exists()) {
                for (DataSnapshot topicSnapshot : topicsSnapshot.getChildren()) {
                    String topic = topicSnapshot.getKey();
                    String mappedTag = mapTopicToTag(topic);
                    
                    // Nếu đã có tag này trong preferences, tăng điểm ưu tiên
                    if (userPreferences.containsKey(mappedTag)) {
                        userPreferences.put(mappedTag, userPreferences.get(mappedTag) + 0.5);
                    } else {
                        userPreferences.put(mappedTag, 1.0);
                    }
                }
            }
            
            // Nếu là người dùng mới (không có preferences)
            boolean isNewUser = userPreferences.isEmpty();
            
            // Lấy danh sách video
            List<HealthTipVideo> allVideos;
            if (isNewUser) {
                // Lấy video trending cho người dùng mới
                allVideos = Tasks.await(videoRepository.getTrendingVideos("vietnam", limit * 2));
            } else {
                // Lấy tất cả video để lọc và sắp xếp theo sở thích
                allVideos = Tasks.await(videoRepository.getAllVideos());
                
                // Tính điểm phù hợp cho mỗi video dựa trên tag và sở thích người dùng
                final Map<String, Double> finalUserPreferences = userPreferences;
                
                Collections.sort(allVideos, (v1, v2) -> {
                    double score1 = calculateVideoScore(v1, finalUserPreferences);
                    double score2 = calculateVideoScore(v2, finalUserPreferences);
                    
                    // Ưu tiên video mới hơn nếu điểm gần bằng nhau
                    if (Math.abs(score1 - score2) < 0.2) {
                        return Long.compare(v2.getPublishDate(), v1.getPublishDate());
                    }
                    
                    return Double.compare(score2, score1);
                });
            }
            
            // Giới hạn số lượng video trả về
            return allVideos.size() <= limit ? allVideos : allVideos.subList(0, limit);
        });
    }
    
    // Ánh xạ từ topic sang tag
    private String mapTopicToTag(String topic) {
        Map<String, String> topicTagMap = new HashMap<>();
        topicTagMap.put("sứckhỏetổngquát", "suckhoe");
        topicTagMap.put("tim mạch", "timmach");
        topicTagMap.put("dinh dưỡng", "dinhduong");
        // Thêm các ánh xạ khác...
        
        return topicTagMap.getOrDefault(topic, topic.toLowerCase().replaceAll("\\s+", ""));
    }
    
    // Tính điểm phù hợp của video với sở thích người dùng
    private double calculateVideoScore(HealthTipVideo video, Map<String, Double> userPreferences) {
        double score = 0.0;
        
        // Cộng điểm dựa trên tag khớp với sở thích
        for (String tag : video.getTags()) {
            if (userPreferences.containsKey(tag)) {
                score += userPreferences.get(tag);
            }
        }
        
        // Thêm điểm dựa trên độ phổ biến (view, like)
        score += (Math.log10(video.getViewCount() + 10) * 0.1);
        score += (Math.log10(video.getLikeCount() + 1) * 0.2);
        
        // Ưu tiên nội dung mới (trong vòng 7 ngày)
        long daysSincePublish = (System.currentTimeMillis() - video.getPublishDate()) / (1000 * 60 * 60 * 24);
        if (daysSincePublish <= 7) {
            score += (1.0 - (daysSincePublish / 7.0)) * 0.5;
        }
        
        return score;
    }
}
```

## 5. Triển khai UI cho Short Video kiểu TikTok/Reels

### 5.1. ShortVideoFragment

```java
public class ShortVideoFragment extends Fragment {
    private ViewPager2 viewPager;
    private ShortVideoAdapter adapter;
    private VideoRecommendationEngine recommendationEngine;
    private SimpleExoPlayer currentPlayer;
    private int currentPlayingPosition = -1;
    
    @Inject
    HealthTipVideoRepository videoRepository;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Thiết lập chế độ immersive full screen để giống TikTok/Reels
        requireActivity().getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        View view = inflater.inflate(R.layout.fragment_short_video, container, false);
        
        viewPager = view.findViewById(R.id.viewPager);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        
        // Thêm hiệu ứng transform page để tạo trải nghiệm giống TikTok
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        
        // Thiết lập adapter và load video
        adapter = new ShortVideoAdapter(getContext());
        viewPager.setAdapter(adapter);
        
        // Đặt offscreen page limit để preload trước video kế tiếp
        viewPager.setOffscreenPageLimit(1);
        
        // Bắt sự kiện khi người dùng lướt video
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                
                // Dừng video trước đó nếu có
                if (currentPlayingPosition != -1 && currentPlayingPosition != position) {
                    ShortVideoAdapter.VideoPlayerViewHolder previousHolder = 
                        (ShortVideoAdapter.VideoPlayerViewHolder) viewPager
                            .findViewHolderForAdapterPosition(currentPlayingPosition);
                    if (previousHolder != null) {
                        previousHolder.pauseVideo();
                    }
                }
                
                // Phát video mới
                ShortVideoAdapter.VideoPlayerViewHolder currentHolder = 
                    (ShortVideoAdapter.VideoPlayerViewHolder) viewPager
                        .findViewHolderForAdapterPosition(position);
                if (currentHolder != null) {
                    currentHolder.playVideo();
                }
                
                currentPlayingPosition = position;
                
                // Ghi nhận lượt xem video
                trackVideoView(adapter.getVideoAt(position));
            }
        });
        
        // Sử dụng recommendation engine để lấy video
        recommendationEngine = new VideoRecommendationEngine(FirebaseDatabase.getInstance(), videoRepository);
        loadVideos();
        
        return view;
    }
    
    private void loadVideos() {
        recommendationEngine.getRecommendedVideos(20)
            .addOnSuccessListener(videos -> {
                adapter.setVideos(videos);
                
                // Auto-play video đầu tiên khi load xong
                viewPager.post(() -> {
                    ShortVideoAdapter.VideoPlayerViewHolder firstHolder = 
                        (ShortVideoAdapter.VideoPlayerViewHolder) viewPager
                            .findViewHolderForAdapterPosition(0);
                    if (firstHolder != null) {
                        firstHolder.playVideo();
                        currentPlayingPosition = 0;
                    }
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Không thể tải video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void trackVideoView(HealthTipVideo video) {
        // Ghi nhận lượt xem vào Firebase
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference("user_views")
                .child(userId)
                .child(video.getId())
                .setValue(System.currentTimeMillis());
                
            // Tăng view count
            DatabaseReference videoRef = FirebaseDatabase.getInstance()
                .getReference("videos")
                .child(video.getId())
                .child("viewCount");
            videoRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Integer currentValue = mutableData.getValue(Integer.class);
                    if (currentValue == null) {
                        mutableData.setValue(1);
                    } else {
                        mutableData.setValue(currentValue + 1);
                    }
                    return Transaction.success(mutableData);
                }
                
                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {}
            });
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Tạm dừng video hiện tại khi rời khỏi màn hình
        if (currentPlayingPosition != -1) {
            ShortVideoAdapter.VideoPlayerViewHolder holder = 
                (ShortVideoAdapter.VideoPlayerViewHolder) viewPager
                    .findViewHolderForAdapterPosition(currentPlayingPosition);
            if (holder != null) {
                holder.pauseVideo();
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Tiếp tục phát video hiện tại khi quay lại màn hình
        if (currentPlayingPosition != -1) {
            ShortVideoAdapter.VideoPlayerViewHolder holder = 
                (ShortVideoAdapter.VideoPlayerViewHolder) viewPager
                    .findViewHolderForAdapterPosition(currentPlayingPosition);
            if (holder != null) {
                holder.playVideo();
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Thoát chế độ full screen khi rời fragment
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
```

### 5.2. ShortVideoAdapter và VideoPlayerViewHolder

```java
public class ShortVideoAdapter extends RecyclerView.Adapter<ShortVideoAdapter.VideoPlayerViewHolder> {
    private List<HealthTipVideo> videos = new ArrayList<>();
    private Context context;
    private Map<Integer, SimpleExoPlayer> playerCache = new HashMap<>();
    
    public ShortVideoAdapter(Context context) {
        this.context = context;
    }
    
    public void setVideos(List<HealthTipVideo> videos) {
        this.videos = videos;
        notifyDataSetChanged();
    }
    
    public HealthTipVideo getVideoAt(int position) {
        if (position >= 0 && position < videos.size()) {
            return videos.get(position);
        }
        return null;
    }
    
    @NonNull
    @Override
    public VideoPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_short_video, parent, false);
        return new VideoPlayerViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull VideoPlayerViewHolder holder, int position) {
        holder.bind(videos.get(position), position);
    }
    
    @Override
    public int getItemCount() {
        return videos.size();
    }
    
    @Override
    public void onViewRecycled(@NonNull VideoPlayerViewHolder holder) {
        super.onViewRecycled(holder);
        int position = holder.getBindingAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            if (playerCache.containsKey(position)) {
                SimpleExoPlayer player = playerCache.get(position);
                if (player != null) {
                    player.release();
                    playerCache.remove(position);
                }
            }
        }
    }
    
    class VideoPlayerViewHolder extends RecyclerView.ViewHolder {
        PlayerView playerView;
        SimpleExoPlayer player;
        ImageView thumbnailView;
        TextView titleTextView, captionTextView;
        ImageView likeButton, shareButton, commentButton;
        TextView likeCountTextView, commentCountTextView;
        ProgressBar loadingIndicator;
        ShimmerFrameLayout shimmerLayout;
        int position;
        boolean isPlaying = false;
        
        public VideoPlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.player_view);
            thumbnailView = itemView.findViewById(R.id.image_thumbnail);
            titleTextView = itemView.findViewById(R.id.text_title);
            captionTextView = itemView.findViewById(R.id.text_caption);
            likeButton = itemView.findViewById(R.id.button_like);
            shareButton = itemView.findViewById(R.id.button_share);
            commentButton = itemView.findViewById(R.id.button_comment);
            likeCountTextView = itemView.findViewById(R.id.text_like_count);
            commentCountTextView = itemView.findViewById(R.id.text_comment_count);
            loadingIndicator = itemView.findViewById(R.id.loading_indicator);
            shimmerLayout = itemView.findViewById(R.id.shimmer_layout);
        }
        
        public void bind(HealthTipVideo video, int pos) {
            this.position = pos;
            titleTextView.setText(video.getTitle());
            captionTextView.setText(video.getDescription());
            likeCountTextView.setText(formatCount(video.getLikeCount()));
            
            // Hiển thị thumbnail trước khi video được phát
            Glide.with(context)
                .load(video.getThumbnailUrl())
                .placeholder(R.drawable.placeholder_video)
                .into(thumbnailView);
                
            // Hiệu ứng shimmer cho loading state
            shimmerLayout.startShimmer();
            
            // Chuẩn bị ExoPlayer nhưng chưa phát
            preparePlayer(video.getVideoUrl());
            
            // Hiển thị nút like đã chọn nếu người dùng đã like video này
            checkIfVideoLiked(video.getId());
            
            // Thiết lập các sự kiện like, share, comment
            setupEventListeners(video);
        }
        
        private void preparePlayer(String videoUrl) {
            if (player == null) {
                player = new SimpleExoPlayer.Builder(context).build();
                playerView.setPlayer(player);
                player.setVolume(1.0f);
                player.setRepeatMode(Player.REPEAT_MODE_ONE); // Loop video
                
                // Cache player để quản lý tốt hơn
                playerCache.put(position, player);
                
                // Hiển thị loading indicator
                loadingIndicator.setVisibility(View.VISIBLE);
                
                // Media source từ URL
                MediaSource mediaSource = new ProgressiveMediaSource.Factory(
                    new DefaultHttpDataSourceFactory("exoplayer-app"))
                    .createMediaSource(Uri.parse(videoUrl));
                
                player.setMediaSource(mediaSource);
                player.prepare();
                
                player.addListener(new Player.EventListener() {
                    @Override
                    public void onPlaybackStateChanged(int state) {
                        if (state == Player.STATE_READY) {
                            // Khi video đã sẵn sàng để phát
                            loadingIndicator.setVisibility(View.GONE);
                            thumbnailView.setVisibility(View.GONE);
                            shimmerLayout.stopShimmer();
                            shimmerLayout.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
        
        public void playVideo() {
            if (player != null && !isPlaying) {
                player.setPlayWhenReady(true);
                isPlaying = true;
                
                // Hiệu ứng fade-in cho PlayerView
                playerView.animate().alpha(1f).setDuration(300).start();
                
                // Hiệu ứng nhỏ dần cho thumbnail
                if (thumbnailView.getVisibility() == View.VISIBLE) {
                    thumbnailView.animate().alpha(0f).setDuration(300)
                        .withEndAction(() -> thumbnailView.setVisibility(View.GONE))
                        .start();
                }
            }
        }
        
        public void pauseVideo() {
            if (player != null && isPlaying) {
                player.setPlayWhenReady(false);
                isPlaying = false;
            }
        }
        
        private void checkIfVideoLiked(String videoId) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase.getInstance().getReference("user_likes")
                    .child(userId)
                    .child(videoId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                likeButton.setImageResource(R.drawable.ic_like_filled);
                            } else {
                                likeButton.setImageResource(R.drawable.ic_like_outline);
                            }
                        }
                        
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
            }
        }
        
        private void setupEventListeners(HealthTipVideo video) {
            // Xử lý sự kiện double-tap để like (giống TikTok)
            playerView.setOnTouchListener(new DoubleTapGestureListener(context) {
                @Override
                public void onDoubleTap(MotionEvent e) {
                    toggleLike(video.getId());
                    showLikeAnimation(e.getX(), e.getY());
                }
            });
            
            // Nút like
            likeButton.setOnClickListener(v -> toggleLike(video.getId()));
            
            // Nút chia sẻ
            shareButton.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Xem video sức khỏe hay này: " + 
                                     video.getTitle() + " - " + video.getVideoUrl());
                context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
            });
            
            // Nút bình luận
            commentButton.setOnClickListener(v -> {
                CommentBottomSheet commentSheet = CommentBottomSheet.newInstance(video.getId());
                commentSheet.show(((FragmentActivity) context).getSupportFragmentManager(), "comments");
            });
        }
        
        private void toggleLike(String videoId) {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(context, "Đăng nhập để thích video", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userLikeRef = FirebaseDatabase.getInstance().getReference("user_likes")
                .child(userId)
                .child(videoId);
                
            userLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isLiked = snapshot.exists();
                    
                    if (isLiked) {
                        // Unlike
                        userLikeRef.removeValue();
                        likeButton.setImageResource(R.drawable.ic_like_outline);
                        updateLikeCount(videoId, -1);
                    } else {
                        // Like
                        userLikeRef.setValue(true);
                        likeButton.setImageResource(R.drawable.ic_like_filled);
                        
                        // Hiệu ứng scale cho nút like
                        likeButton.animate()
                            .scaleX(1.5f).scaleY(1.5f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                likeButton.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(100).start();
                            }).start();
                            
                        updateLikeCount(videoId, 1);
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        
        private void updateLikeCount(String videoId, int delta) {
            DatabaseReference videoRef = FirebaseDatabase.getInstance()
                .getReference("videos")
                .child(videoId)
                .child("likeCount");
                
            videoRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Integer currentValue = mutableData.getValue(Integer.class);
                    if (currentValue == null) {
                        mutableData.setValue(Math.max(0, delta));
                    } else {
                        mutableData.setValue(Math.max(0, currentValue + delta));
                    }
                    return Transaction.success(mutableData);
                }
                
                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                    if (committed) {
                        Integer newLikeCount = dataSnapshot.getValue(Integer.class);
                        if (newLikeCount != null) {
                            likeCountTextView.setText(formatCount(newLikeCount));
                        }
                    }
                }
            });
        }
        
        private void showLikeAnimation(float x, float y) {
            // Tạo hiệu ứng trái tim bay lên khi double-tap (như TikTok)
            ImageView heartView = new ImageView(context);
            heartView.setImageResource(R.drawable.ic_heart_animation);
            
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            
            // Đặt vị trí trái tim tại điểm nhấn
            params.leftMargin = (int) x - 50;
            params.topMargin = (int) y - 50;
            
            ((FrameLayout) itemView).addView(heartView, params);
            
            // Hiệu ứng bay lên và mờ dần
            heartView.setScaleX(0.1f);
            heartView.setScaleY(0.1f);
            heartView.setAlpha(0.8f);
            
            heartView.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .alpha(0f)
                .translationYBy(-200f)
                .setDuration(1000)
                .withEndAction(() -> ((FrameLayout) itemView).removeView(heartView))
                .start();
        }
        
        private String formatCount(int count) {
            if (count < 1000) return String.valueOf(count);
            if (count < 1000000) return (count / 1000) + "K";
            return (count / 1000000) + "M";
        }
    }
    
    // Double-tap gesture listener class
    public static abstract class DoubleTapGestureListener implements View.OnTouchListener {
        private final GestureDetector gestureDetector;
        
        public DoubleTapGestureListener(Context context) {
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }
                
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    onSingleTap(e);
                    return super.onSingleTapConfirmed(e);
                }
                
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    DoubleTapGestureListener.this.onDoubleTap(e);
                    return super.onDoubleTap(e);
                }
            });
        }
        
        public void onSingleTap(MotionEvent e) {
            // Mặc định không làm gì, class con có thể override
        }
        
        public abstract void onDoubleTap(MotionEvent e);
        
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }
    }
    
    // Page transformer để tạo hiệu ứng chuyển trang
    public static class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;
        
        @Override
        public void transformPage(@NonNull View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();
            
            if (position < -1) { // [-Infinity,-1)
                // Trang này nằm ngoài màn hình bên trái
                view.setAlpha(0f);
            } else if (position <= 1) { // [-1,1]
                // Trang đang di chuyển
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float verticalMargin = pageHeight * (1 - scaleFactor) / 2;
                float horizontalMargin = pageWidth * (1 - scaleFactor) / 2;
                
                if (position < 0) {
                    view.setTranslationX(horizontalMargin - verticalMargin / 2);
                } else {
                    view.setTranslationX(-horizontalMargin + verticalMargin / 2);
                }
                
                // Scale trang
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
                
                // Làm mờ trang
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else { // (1,+Infinity]
                // Trang này nằm ngoài màn hình bên phải
                view.setAlpha(0f);
            }
        }
    }
}
```

### 5.3. Thiết kế giao diện (item_short_video.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Màn hình đầy đủ với tỷ lệ 9:16 như TikTok -->
    <com.google.android.exoplayer2.ui.PlayerView
        android:id="@+id/player_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:alpha="0"
        app:resize_mode="zoom"
        app:use_controller="false" />

    <!-- Thumbnail hiển thị trước khi video load -->
    <ImageView
        android:id="@+id/image_thumbnail"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop" />

    <!-- Shimmer effect khi loading -->
    <com.facebook.shimmer.ShimmerFrameLayout
        android:id="@+id/shimmer_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <View
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="#80000000" />
    </com.facebook.shimmer.ShimmerFrameLayout>

    <!-- Loading indicator -->
    <ProgressBar
        android:id="@+id/loading_indicator"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:layout_gravity="center"
        android:visibility="gone" />

    <!-- Overlay nội dung - nằm dưới cùng như TikTok -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        android:orientation="horizontal"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:paddingBottom="24dp">

        <!-- Vùng thông tin video (bên trái) -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">

            <TextView
                android:id="@+id/text_title"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="@android:color/white"
                android:textSize="16sp"
                android:textStyle="bold"
                android:shadowColor="#80000000"
                android:shadowDx="1"
                android:shadowDy="1"
                android:shadowRadius="3"
                tools:text="5 cách uống nước đúng cách" />

            <TextView
                android:id="@+id/text_caption"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:textColor="@android:color/white"
                android:textSize="14sp"
                android:maxLines="2"
                android:ellipsize="end"
                android:shadowColor="#80000000"
                android:shadowDx="1"
                android:shadowDy="1"
                android:shadowRadius="2"
                tools:text="Hãy học cách uống nước đúng để cơ thể khỏe mạnh hơn..." />
        </LinearLayout>

        <!-- Vùng các nút tương tác (bên phải) -->
        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="center">

            <!-- Nút like -->
            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="center"
                android:layout_marginBottom="16dp">

                <ImageView
                    android:id="@+id/button_like"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:padding="8dp"
                    android:src="@drawable/ic_like_outline"
                    android:background="?attr/selectableItemBackgroundBorderless" />

                <TextView
                    android:id="@+id/text_like_count"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:textColor="@android:color/white"
                    android:textSize="12sp"
                    android:shadowColor="#80000000"
                    android:shadowDx="1"
                    android:shadowDy="1"
                    android:shadowRadius="2"
                    tools:text="1.2K" />
            </LinearLayout>

            <!-- Nút comment -->
            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="center"
                android:layout_marginBottom="16dp">

                <ImageView
                    android:id="@+id/button_comment"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:padding="8dp"
                    android:src="@drawable/ic_comment"
                    android:background="?attr/selectableItemBackgroundBorderless" />

                <TextView
                    android:id="@+id/text_comment_count"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:textColor="@android:color/white"
                    android:textSize="12sp"
                    android:shadowColor="#80000000"
                    android:shadowDx="1"
                    android:shadowDy="1"
                    android:shadowRadius="2"
                    tools:text="156" />
            </LinearLayout>

            <!-- Nút share -->
            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="center">

                <ImageView
                    android:id="@+id/button_share"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:padding="8dp"
                    android:src="@drawable/ic_share"
                    android:background="?attr/selectableItemBackgroundBorderless" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:textColor="@android:color/white"
                    android:textSize="12sp"
                    android:text="Chia sẻ"
                    android:shadowColor="#80000000"
                    android:shadowDx="1"
                    android:shadowDy="1"
                    android:shadowRadius="2" />
            </LinearLayout>
        </LinearLayout>
    </LinearLayout>
</FrameLayout>
```

### 5.4. CommentBottomSheet (Comment như TikTok)

```java
public class CommentBottomSheet extends BottomSheetDialogFragment {
    private String videoId;
    private RecyclerView commentRecyclerView;
    private EditText commentInput;
    private ImageButton sendButton;
    private CommentAdapter commentAdapter;
    
    public static CommentBottomSheet newInstance(String videoId) {
        CommentBottomSheet fragment = new CommentBottomSheet();
        Bundle args = new Bundle();
        args.putString("videoId", videoId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoId = getArguments().getString("videoId");
        }
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_comments, container, false);
        
        // Thiết lập UI
        commentRecyclerView = view.findViewById(R.id.recycler_comments);
        commentInput = view.findViewById(R.id.edit_comment);
        sendButton = view.findViewById(R.id.button_send);
        
        // Thiết lập RecyclerView
        commentAdapter = new CommentAdapter();
        commentRecyclerView.setAdapter(commentAdapter);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Load comments
        loadComments();
        
        // Sự kiện gửi comment
        sendButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                postComment(commentText);
            }
        });
        
        return view;
    }
    
    private void loadComments() {
        // Lấy comments từ Firebase
        FirebaseDatabase.getInstance().getReference("video_comments")
            .child(videoId)
            .orderByChild("timestamp")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Comment> comments = new ArrayList<>();
                    for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                        Comment comment = commentSnapshot.getValue(Comment.class);
                        if (comment != null) {
                            comments.add(comment);
                        }
                    }
                    commentAdapter.setComments(comments);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }
    
    private void postComment(String text) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (userName == null) userName = "Người dùng";
        
        String commentId = FirebaseDatabase.getInstance().getReference("video_comments")
            .child(videoId).push().getKey();
            
        if (commentId != null) {
            Comment comment = new Comment(
                commentId,
                userId,
                userName,
                text,
                System.currentTimeMillis()
            );
            
            FirebaseDatabase.getInstance().getReference("video_comments")
                .child(videoId)
                .child(commentId)
                .setValue(comment)
                .addOnSuccessListener(aVoid -> {
                    commentInput.setText("");
                    // Cập nhật số lượng comment
                    updateCommentCount();
                });
        }
    }
    
    private void updateCommentCount() {
        FirebaseDatabase.getInstance().getReference("video_comments")
            .child(videoId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int commentCount = (int) snapshot.getChildrenCount();
                    
                    // Cập nhật số lượng comment trên video
                    FirebaseDatabase.getInstance().getReference("videos")
                        .child(videoId)
                        .child("commentCount")
                        .setValue(commentCount);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }
}
```