# Project_Analysis_Details.md

HealthTips - Ứng dụng Mẹo chăm sóc sức khỏe hàng ngày

### 1. Tổng quan dự án

* Platform: Android (Java)
* Min SDK: 24 (Android 7.0)
* Target SDK: 34
* Architecture: MVP (Model-View-Presenter)
* **Database: Firebase Realtime Database (Chính) + Firebase Firestore (Tùy chọn)**
* Authentication: Firebase Auth
* Storage: Firebase Storage
* Notifications: Firebase Cloud Messaging (FCM)
* Analytics: Firebase Analytics
* Crash Reporting: Firebase Crashlytics
* UI: Material Design 3 + XML Layouts
* Async: AsyncTask, CompletableFuture, RxJava (tùy chọn)
* Dependency Injection: Dagger 2
* Multi-language: Android Localization (strings.xml)

### 2. Cấu trúc thư mục (MVP Pattern)
app/
├── data/
│   ├── firebase/
│   │   ├── FirebaseManager.java
│   │   ├── AuthManager.java
│   │   ├── FirestoreManager.java
│   │   └── StorageManager.java
│   ├── models/
│   │   ├── User.java
│   │   ├── HealthTip.java
│   │   ├── Category.java
│   │   ├── Favorite.java
│   │   ├── Reminder.java
│   │   └── ChatMessage.java
│   ├── repositories/
│   │   ├── UserRepository.java
│   │   ├── HealthTipRepository.java
│   │   ├── CategoryRepository.java
│   │   └── FavoriteRepository.java
│   └── local/
│       ├── SharedPreferencesManager.java
│       └── CacheManager.java
├── presentation/
│   ├── activities/
│   │   ├── MainActivity.java
│   │   ├── LoginActivity.java
│   │   ├── SplashActivity.java
│   │   └── AdminActivity.java
│   ├── fragments/
│   │   ├── HomeFragment.java
│   │   ├── CategoryFragment.java
│   │   ├── FavoriteFragment.java
│   │   ├── ReminderFragment.java
│   │   └── ProfileFragment.java
│   ├── presenters/
│   │   ├── HomePresenter.java
│   │   ├── CategoryPresenter.java
│   │   ├── AuthPresenter.java
    public String getLocalizedName(String language) {
        return nameTranslations.getOrDefault(language, name);
    }

    // ... other methods
}

// Favorite Model
public class Favorite {
    private String id;
    private String userId;
    private String tipId;
    private long createdAt;

    // Constructors, getters, setters
}

// Reminder Model
public class Reminder {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String time; // "HH:mm"
    private ReminderFrequency frequency;
    private boolean isActive;
    private List<String> categories;
    private long createdAt;
    private Map<String, String> titleTranslations;
    private Map<String, String> descriptionTranslations;

    // Constructors, getters, setters
}

// Chat Message Model
public class ChatMessage {
    private String id;
    private String sessionId;
    private String message;
    private boolean isFromUser;
    private long timestamp;
    private List<String> relatedTips;

    // Constructors, getters, setters
}

// Enums
public enum ReminderFrequency {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly");

    private final String value;

    ReminderFrequency(String value) {
        this.value = value;
    }

    public String getValue() { return value; }
}

public enum NotificationType {
    REMINDER, CHAT, SYSTEM
}
4. Firebase Configuration
4.1 Firebase Manager
Java

public class FirebaseManager {
    private static FirebaseManager instance;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseDatabase realtimeDb;

    private FirebaseManager() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        realtimeDb = FirebaseDatabase.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public FirebaseFirestore getFirestore() { return firestore; }
    public FirebaseAuth getAuth() { return auth; }
    public FirebaseStorage getStorage() { return storage; }
    public FirebaseDatabase getRealtimeDb() { return realtimeDb; }
}
4.2 Realtime Database Structure
Dữ liệu sẽ được tổ chức dưới dạng cây JSON trong Firebase Realtime Database như sau:

JSON

{
  "healthtips_app": {
    "users": {
      "{userId}": {
        "profile": {
          "email": "user@example.com",
          "username": "tên_người_dùng",
          "fullName": "Tên đầy đủ",
          "phone": "0123456789",
          "createdAt": 1678886400000,
          "isActive": true,
          "isAdmin": false,
          "preferredLanguage": "vi",
          "fcmToken": "token_device"
        },
        "favorites": {
          "{favoriteId}": {
            "tipId": "tip_id_của_mẹo_yêu_thích",
            "createdAt": 1678886500000
          }
        },
        "reminders": {
          "{reminderId}": {
            "title": "Nhắc nhở uống nước",
            "description": "Hãy uống đủ nước mỗi giờ!",
            "time": "10:00",
            "frequency": "DAILY",
            "isActive": true,
            "categories": ["cat001"],
            "createdAt": 1678886600000
          }
        }
      }
    },
    "categories": {
      "{categoryId}": {
        "name": "Dinh dưỡng",
        "description": "Các mẹo về dinh dưỡng và chế độ ăn uống",
        "iconUrl": "url_to_icon_image",
        "color": "#HEX_COLOR",
        "createdAt": 1721195400000,
        "isActive": true,
        "nameTranslations": {"vi": "Dinh dưỡng", "en": "Nutrition"},
        "descriptionTranslations": {"vi": "Mô tả tiếng Việt", "en": "English description"}
      }
    },
    "health_tips": {
      "{tipId}": {
        "title": "10 mẹo ăn uống lành mạnh",
        "content": "Nội dung chi tiết...",
        "shortDescription": "Tóm tắt ngắn gọn...",
        "categoryId": "cat001",
        "tags": ["dinhduong", "suckhoe"],
        "createdBy": "admin_user_id",
        "createdAt": 1721195800000,
        "updatedAt": 1721195900000,
        "isActive": true,
        "viewCount": 150,
        "likeCount": 25,
        "imageUrl": "url_to_tip_image",
        "titleTranslations": {"vi": "Tiêu đề tiếng Việt", "en": "English title"},
        "contentTranslations": {"vi": "Nội dung tiếng Việt", "en": "English content"}
      }
    },
    "chat-sessions": {
      "{sessionId}": {
        "userId": "user_id",
        "createdAt": 1678887000000,
        "messages": {
          "{messageId}": {
            "message": "Xin chào!",
            "isFromUser": true,
            "timestamp": 1678887005000,
            "relatedTips": ["tip001"]
          }
        }
      }
    },
    "app-settings": {
      "localization": {
        "version": "1.0",
        "lastUpdated": 1678887100000,
        "strings": {
          "vi": {
            "app_name": "Mẹo Sức Khỏe"
          },
          "en": {
            "app_name": "HealthTips"
          }
        }
      }
    }
  }
}
5. MVP Architecture Implementation
5.1 Base Classes
Java

// BaseView interface
public interface BaseView {
    void showLoading();
    void hideLoading();
    void showError(String message);
    void showSuccess(String message);
    Context getContext();
}

// BasePresenter abstract class
public abstract class BasePresenter<V extends BaseView> {
    protected V view;
    protected CompositeDisposable disposables;

    public BasePresenter() {
        disposables = new CompositeDisposable();
    }

    public void attachView(V view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        if (disposables != null) {
            disposables.clear();
        }
    }

    protected boolean isViewAttached() {
        return view != null;
    }
}

// Example: HomeView interface
public interface HomeView extends BaseView {
    void showCategories(List<Category> categories);
    void showPopularTips(List<HealthTip> tips);
    void navigateToCategory(String categoryId);
    void navigateToTip(String tipId);
}

// Example: HomePresenter
public class HomePresenter extends BasePresenter<HomeView> {
    private CategoryRepository categoryRepository;
    private HealthTipRepository tipRepository;

    @Inject
    public HomePresenter(CategoryRepository categoryRepository,
                        HealthTipRepository tipRepository) {
        this.categoryRepository = categoryRepository;
        this.tipRepository = tipRepository;
    }

    public void loadHomeData() {
        if (!isViewAttached()) return;

        view.showLoading();

        // Load categories
        categoryRepository.getAllCategories(new DataCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (isViewAttached()) {
                    view.showCategories(categories);
                }
            }

            @Override
            public void onError(String error) {
                if (isViewAttached()) {
                    view.showError(error);
                }
            }
        });

        // Load popular tips
        tipRepository.getPopularTips(10, new DataCallback<List<HealthTip>>() {
            @Override
            public void onSuccess(List<HealthTip> tips) {
                if (isViewAttached()) {
                    view.showPopularTips(tips);
                    view.hideLoading();
                }
            }

            @Override
            public void onError(String error) {
                if (isViewAttached()) {
                    view.showError(error);
                    view.hideLoading();
                }
            }
        });
    }
}
5.2 Repository Pattern
Java

public interface HealthTipRepository {
    void getAllTips(DataCallback<List<HealthTip>> callback);
    void getTipsByCategory(String categoryId, DataCallback<List<HealthTip>> callback);
    void searchTips(String query, DataCallback<List<HealthTip>> callback);
    void getTipById(String id, DataCallback<HealthTip> callback);
    void addTip(HealthTip tip, DataCallback<Void> callback);
    void updateTip(HealthTip tip, DataCallback<Void> callback);
    void deleteTip(String id, DataCallback<Void> callback);
    void getPopularTips(int limit, DataCallback<List<HealthTip>> callback);
}

public class HealthTipRepositoryImpl implements HealthTipRepository {
    private FirebaseFirestore firestore;

    private static final String COLLECTION_TIPS = "tips";

    @Inject
    public HealthTipRepositoryImpl(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void getAllTips(DataCallback<List<HealthTip>> callback) {
        firestore.collection(COLLECTION_TIPS)
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HealthTip> tips = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        HealthTip tip = doc.toObject(HealthTip.class);
                        if (tip != null) {
                            tip.setId(doc.getId());
                            tips.add(tip);
                        }
                    }
                    callback.onSuccess(tips);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Implement other methods...
}

// Callback interface
public interface DataCallback<T> {
    void onSuccess(T data);
    void onError(String error);
}
6. Đa ngôn ngữ (Internationalization)
6.1 Locale Helper
Java

public class LocaleHelper {
    private static final String SELECTED_LANGUAGE = "selected_language";

    public static void setLocale(Context context, String language) {
        persist(context, language);
        updateResources(context, language);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }
}
6.2 Cấu trúc tài nguyên đa ngôn ngữ
res/
├── values/                 # Default (English)
│   ├── strings.xml
│   ├── colors.xml
│   └── dimens.xml
├── values-vi/             # Vietnamese
│   └── strings.xml
├── values-en/             # English
│   └── strings.xml
├── values-zh/             # Chinese
│   └── strings.xml
├── values-ja/             # Japanese
│   └── strings.xml
└── values-ko/             # Korean
    └── strings.xml
6.3 Strings.xml Examples
XML

<resources>
    <string name="app_name">HealthTips</string>
    <string name="home">Home</string>
    <string name="categories">Categories</string>
    <string name="favorites">Favorites</string>
    <string name="reminders">Reminders</string>
    <string name="profile">Profile</string>
    <string name="search_hint">Search health tips...</string>
    <string name="no_internet">No internet connection</string>
    <string name="loading">Loading...</string>
    <string name="error_occurred">An error occurred</string>
    <string name="welcome_message">Welcome to HealthTips!</string>
    <string name="daily_health_tip">Daily Health Tip</string>
    <string name="popular_tips">Popular Tips</string>
    <string name="add_to_favorites">Add to Favorites</string>
    <string name="remove_from_favorites">Remove from Favorites</string>
    <string name="share_tip">Share Tip</string>
    <string name="set_reminder">Set Reminder</string>
    <string name="language_settings">Language Settings</string>
    <string name="select_language">Select Language</string>
    <string name="vietnamese">Tiếng Việt</string>
    <string name="english">English</string>
    <string name="chinese">中文</string>
    <string name="japanese">日本語</string>
    <string name="korean">한국어</string>
</resources>

<resources>
    <string name="app_name">Mẹo Sức Khỏe</string>
    <string name="home">Trang chủ</string>
    <string name="categories">Chủ đề</string>
    <string name="favorites">Yêu thích</string>
    <string name="reminders">Nhắc nhở</string>
    <string name="profile">Cá nhân</string>
    <string name="search_hint">Tìm kiếm mẹo sức khỏe...</string>
    <string name="no_internet">Không có kết nối internet</string>
    <string name="loading">Đang tải...</string>
    <string name="error_occurred">Đã xảy ra lỗi</string>
    <string name="welcome_message">Chào mừng đến với Mẹo Sức Khỏe!</string>
    <string name="daily_health_tip">Mẹo Sức Khỏe Hàng Ngày</string>
    <string name="popular_tips">Mẹo Phổ Biến</string>
    <string name="add_to_favorites">Thêm vào yêu thích</string>
    <string name="remove_from_favorites">Xóa khỏi yêu thích</string>
    <string name="share_tip">Chia sẻ mẹo</string>
    <string name="set_reminder">Đặt nhắc nhở</string>
    <string name="language_settings">Cài đặt ngôn ngữ</string>
    <string name="select_language">Chọn ngôn ngữ</string>
    <string name="vietnamese">Tiếng Việt</string>
    <string name="english">English</string>
    <string name="chinese">中文</string>
    <string name="japanese">日本語</string>
    <string name="korean">한국어</string>
</resources>
7. Key Activities và Fragments
7.1 MainActivity
Java

public class MainActivity extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply language setting
        String language = LocaleHelper.getLanguage(this);
        LocaleHelper.setLocale(this, language);

        setContentView(R.layout.activity_main);

        initViews();
        setupBottomNavigation();
        setupDrawerNavigation();
        loadHomeFragment();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNav = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_categories) {
                fragment = new CategoryFragment();
            } else if (itemId == R.id.nav_favorites) {
                fragment = new FavoriteFragment();
            } else if (itemId == R.id.nav_reminders) {
                fragment = new ReminderFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
                return true;
            }
            return false;
        });
    }

    // Other methods...
}
7.2 HomeFragment với MVP
Java

public class HomeFragment extends Fragment implements HomeView {
    private HomePresenter presenter;
    private RecyclerView categoryRecyclerView;
    private RecyclerView tipsRecyclerView;
    private ProgressBar progressBar;
    private CategoryAdapter categoryAdapter;
    private HealthTipAdapter tipAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerViews();

        // Inject presenter
        presenter = new HomePresenter(
            new CategoryRepositoryImpl(FirebaseManager.getInstance().getFirestore()),
            new HealthTipRepositoryImpl(FirebaseManager.getInstance().getFirestore())
        );

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);
        presenter.loadHomeData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    // HomeView implementation
    @Override
    public void showCategories(List<Category> categories) {
        categoryAdapter.updateData(categories);
    }

    @Override
    public void showPopularTips(List<HealthTip> tips) {
        tipAdapter.updateData(tips);
    }

    @Override
    public void navigateToCategory(String categoryId) {
        // Navigate to category detail
        Intent intent = new Intent(getContext(), CategoryDetailActivity.class);
        intent.putExtra("categoryId", categoryId);
        startActivity(intent);
    }

    @Override
    public void navigateToTip(String tipId) {
        // Navigate to tip detail
        Intent intent = new Intent(getContext(), TipDetailActivity.class);
        intent.putExtra("tipId", tipId);
        startActivity(intent);
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Other methods...
}
8. Notification System với Firebase
8.1 Firebase Cloud Messaging
Java

public class FCMService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Handle FCM messages
        if (remoteMessage.getData().size() > 0) {
            handleDataMessage(remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            handleNotificationMessage(remoteMessage.getNotification());
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        String type = data.get("type");
        String tipId = data.get("tipId");

        if ("daily_tip".equals(type) && tipId != null) {
            showDailyTipNotification(tipId);
        }
    }

    private void showDailyTipNotification(String tipId) {
        // Create notification for daily tip
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "health_tips")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.daily_health_tip))
                .setContentText(getString(R.string.tap_to_read))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent intent = new Intent(this, TipDetailActivity.class);
        intent.putExtra("tipId", tipId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Send token to server
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        // Update user's FCM token in Firestore
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .update("fcmToken", token);
        }
    }
}
8.2 Reminder Service
Java

public class ReminderService extends Service {
    private static final String TAG = "ReminderService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("SHOW_REMINDER".equals(action)) {
                String reminderId = intent.getStringExtra("reminderId");
                showReminderNotification(reminderId);
            }
        }
        return START_NOT_STICKY;
    }

    private void showReminderNotification(String reminderId) {
        // Get reminder from database and show notification
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(getCurrentUserId())
                .collection("reminders")
                .document(reminderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Reminder reminder = documentSnapshot.toObject(Reminder.class);
                        if (reminder != null && reminder.isActive()) {
                            createReminderNotification(reminder);
                        }
                    }
                });
    }

    private void createReminderNotification(Reminder reminder) {
        String language = LocaleHelper.getLanguage(this);
        String title = reminder.getLocalizedTitle(language);
        String description = reminder.getLocalizedDescription(language);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "reminders")
                .setSmallIcon(R.drawable.ic_reminder)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(reminder.getId().hashCode(), builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : "";
    }
}
9. Chat Bot System
9.1 Simple Chat Bot Service
Java

public class ChatBotService {
    private static ChatBotService instance;
    private HealthTipRepository tipRepository;
    private Map<String, List<String>> keywordMap;

    private ChatBotService() {
        tipRepository = new HealthTipRepositoryImpl(FirebaseManager.getInstance().getFirestore());
        initializeKeywordMap();
    }

    public static synchronized ChatBotService getInstance() {
        if (instance == null) {
            instance = new ChatBotService();
        }
        return instance;
    }

    private void initializeKeywordMap() {
        keywordMap = new HashMap<>();
        keywordMap.put("tim", Arrays.asList("heart", "cardiac", "cardiovascular"));
        keywordMap.put("huyết áp", Arrays.asList("blood pressure", "hypertension"));
        keywordMap.put("tiểu đường", Arrays.asList("diabetes", "blood sugar"));
        keywordMap.put("giảm cân", Arrays.asList("weight loss", "diet", "fitness"));
        keywordMap.put("tập thể dục", Arrays.asList("exercise", "workout", "fitness"));
        keywordMap.put("ăn uống", Arrays.asList("nutrition", "food", "diet"));
        keywordMap.put("ngủ", Arrays.asList("sleep", "insomnia", "rest"));
        keywordMap.put("stress", Arrays.asList("anxiety", "mental health", "relaxation"));
    }

    public void processMessage(String message, String sessionId, DataCallback<ChatMessage> callback) {
        // Analyze message and find relevant tips
        List<String> keywords = extractKeywords(message.toLowerCase());

        if (keywords.isEmpty()) {
            // Default response
            ChatMessage response = new ChatMessage();
            response.setId(generateId());
            response.setSessionId(sessionId);
            response.setMessage("Xin chào! Tôi có thể giúp bạn tìm kiếm các mẹo sức khỏe. Hãy hỏi tôi về tim mạch, huyết áp, tiểu đường, giảm cân, tập thể dục, ăn uống, ngủ nghỉ hoặc stress.");
            response.setFromUser(false);
            response.setTimestamp(System.currentTimeMillis());
            callback.onSuccess(response);
            return;
        }

        // Search for relevant tips
        searchRelevantTips(keywords, new DataCallback<List<HealthTip>>() {
            @Override
            public void onSuccess(List<HealthTip> tips) {
                ChatMessage response = generateResponse(tips, sessionId);
                callback.onSuccess(response);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private List<String> extractKeywords(String message) {
        List<String> foundKeywords = new ArrayList<>();
        for (String keyword : keywordMap.keySet()) {
            if (message.contains(keyword)) {
                foundKeywords.add(keyword);
            }
        }
        return foundKeywords;
    }

    private void searchRelevantTips(List<String> keywords, DataCallback<List<HealthTip>> callback) {
        // Search tips based on keywords
        tipRepository.searchTips(String.join(" ", keywords), callback);
    }

    private ChatMessage generateResponse(List<HealthTip> tips, String sessionId) {
        ChatMessage response = new ChatMessage();
        response.setId(generateId());
        response.setSessionId(sessionId);
        response.setFromUser(false);
        response.setTimestamp(System.currentTimeMillis());

        if (tips.isEmpty()) {
            response.setMessage("Xin lỗi, tôi không tìm thấy mẹo nào phù hợp với câu hỏi của bạn. Bạn có thể thử hỏi về các chủ đề khác không?");
        } else {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Tôi đã tìm thấy ").append(tips.size()).append(" mẹo sức khỏe liên quan:\n\n");

            List<String> tipIds = new ArrayList<>();
            for (int i = 0; i < Math.min(tips.size(), 3); i++) {
                HealthTip tip = tips.get(i);
                messageBuilder.append("• ").append(tip.getTitle()).append("\n");
                messageBuilder.append("  ").append(tip.getShortDescription()).append("\n\n");
                tipIds.add(tip.getId());
            }

            messageBuilder.append("Bạn có muốn xem chi tiết các mẹo này không?");
            response.setMessage(messageBuilder.toString());
            response.setRelatedTips(tipIds);
        }

        return response;
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
10. Permissions và Manifest
10.1 AndroidManifest.xml
XML

<manifest xmlns:android="[http://schemas.android.com/apk/res/android](http://schemas.android.com/apk/res/android)"
    package="com.healthtips.app">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

    <application
        android:name=".HealthTipsApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.HealthTips">

        <activity
            android:name=".presentation.activities.SplashActivity"
            android:theme="@style/SplashTheme"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".presentation.activities.MainActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <activity
            android:name=".presentation.activities.LoginActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <activity
            android:name=".presentation.activities.TipDetailActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <activity
            android:name=".presentation.activities.CategoryDetailActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <activity
            android:name=".presentation.activities.AdminActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <service
            android:name=".services.FCMService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

        <service
            android:name=".services.ReminderService"
            android:exported="false" />

        <receiver
            android:name=".receivers.BootReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>

        <receiver
            android:name=".receivers.AlarmReceiver"
            android:exported="false" />

        <meta-data
            android:name="com.google.firebase.messaging.default_notification_icon"
            android:resource="@drawable/ic_notification" />

        <meta-data
            android:name="com.google.firebase.messaging.default_notification_color"
            android:resource="@color/colorAccent" />

        <meta-data
            android:name="com.google.firebase.messaging.default_notification_channel_id"
            android:value="health_tips" />

    </application>
</manifest>
11. Build Configuration
11.1 build.gradle (Module: app)
Gradle

plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services'
    id 'com.google.firebase.crashlytics'
}

android {
    compileSdk 34

    defaultConfig {
        applicationId "com.healthtips.app"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"

        // Multi-language support
        resConfigs "vi", "en", "zh", "ja", "ko"
    }

    buildTypes {
        debug {
            debuggable true
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }

        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.debug
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding true
    }
}

dependencies {
    // Core Android
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'

    // Navigation
    implementation 'androidx.navigation:navigation-fragment:2.7.6'
    implementation 'androidx.navigation:navigation-ui:2.7.6'

    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-analytics'
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    implementation 'com.google.firebase:firebase-database'
    implementation 'com.google.firebase:firebase-storage'
    implementation 'com.google.firebase:firebase-messaging'
    implementation 'com.google.firebase:firebase-crashlytics'

    // Image loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'

    // Date/Time
    implementation 'com.jakewharton.threetenabp:threetenabp:1.4.6'

    // Dependency Injection
    implementation 'com.google.dagger:dagger:2.48'
    annotationProcessor 'com.google.dagger:dagger-compiler:2.48'

    // RxJava (Optional)
    implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'

    // Networking (if needed)
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

    // Utils
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'commons-validator:commons-validator:1.7'

    // Testing
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:5.7.0'
    testImplementation 'androidx.arch.core:core-testing:2.2.0'

    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.test.espresso:espresso-intents:3.5.1'
    androidTestImplementation 'androidx.test:runner:1.5.2'
    androidTestImplementation 'androidx.test:rules:1.5.0'
}
12. Application Class
Java

public class HealthTipsApplication extends Application {
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize ThreeTen
        AndroidThreeTen.init(this);

        // Initialize Dagger
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        // Create notification channels
        createNotificationChannels();

        // Set up crash reporting
        setupCrashReporting();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            // Health tips channel
            NotificationChannel healthTipsChannel = new NotificationChannel(
                    "health_tips",
                    getString(R.string.health_tips_channel),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            healthTipsChannel.setDescription(getString(R.string.health_tips_channel_description));
            notificationManager.createNotificationChannel(healthTipsChannel);

            // Reminders channel
            NotificationChannel remindersChannel = new NotificationChannel(
                    "reminders",
                    getString(R.string.reminders_channel),
                    NotificationManager.IMPORTANCE_HIGH
            );
            remindersChannel.setDescription(getString(R.string.reminders_channel_description));
            notificationManager.createNotificationChannel(remindersChannel);
        }
    }

    private void setupCrashReporting() {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCrashlyticsCollectionEnabled(true);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
13. Dependency Injection với Dagger 2
13.1 App Module
Java

@Module
public class AppModule {
    private final Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return application;
    }

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Singleton
    public FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseFirestore provideFirebaseFirestore() {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseStorage provideFirebaseStorage() {
        return FirebaseStorage.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseDatabase provideFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }
}
13.2 App Component
Java

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(HealthTipsApplication application);
    void inject(MainActivity activity);
    void inject(LoginActivity activity);
    void inject(HomeFragment fragment);
    void inject(CategoryFragment fragment);
    void inject(FavoriteFragment fragment);
    void inject(ReminderFragment fragment);
    void inject(ProfileFragment fragment);

    // Providers
    Context context();
    SharedPreferences sharedPreferences();
    FirebaseAuth firebaseAuth();
    FirebaseFirestore firebaseFirestore();
    FirebaseStorage firebaseStorage();
    FirebaseDatabase firebaseDatabase();
}
14. Testing Strategy
14.1 Unit Tests
Java

@RunWith(JUnit4.class)
public class HealthTipRepositoryTest {

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockCollection;

    @Mock
    private Task<QuerySnapshot> mockTask;

    private HealthTipRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        repository = new HealthTipRepositoryImpl(mockFirestore);
    }

    @Test
    public void testGetAllTips_Success() {
        // Arrange
        List<HealthTip> expectedTips = Arrays.asList(
            new HealthTip("1", "Tip 1", "Content 1"),
            new HealthTip("2", "Tip 2", "Content 2")
        );

        when(mockFirestore.collection("tips")).thenReturn(mockCollection);
        when(mockCollection.whereEqualTo("isActive", true)).thenReturn(mockCollection);
        when(mockCollection.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(mockCollection);
        when(mockCollection.get()).thenReturn(mockTask);

        // Act & Assert
        repository.getAllTips(new DataCallback<List<HealthTip>>() {
            @Override
            public void onSuccess(List<HealthTip> tips) {
                assertEquals(2, tips.size());
                assertEquals("Tip 1", tips.get(0).getTitle());
            }

            @Override
            public void onError(String error) {
                fail("Should not fail");
            }
        });
    }

    @Test
    public void testSearchTips_EmptyQuery() {
        // Test search with empty query
        repository.searchTips("", new DataCallback<List<HealthTip>>() {
            @Override
            public void onSuccess(List<HealthTip> tips) {
                assertTrue(tips.isEmpty());
            }

            @Override
            public void onError(String error) {
                fail("Should not fail");
            }
        });
    }
}
14.2 Presenter Tests
Java

@RunWith(JUnit4.class)
public class HomePresenterTest {

    @Mock
    private HomeView mockView;

    @Mock
    private CategoryRepository mockCategoryRepository;

    @Mock
    private HealthTipRepository mockTipRepository;

    private HomePresenter presenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        presenter = new HomePresenter(mockCategoryRepository, mockTipRepository);
        presenter.attachView(mockView);
    }

    @After
    public void tearDown() {
        presenter.detachView();
    }

    @Test
    public void testLoadHomeData_Success() {
        // Arrange
        List<Category> categories = Arrays.asList(new Category("1", "Category 1"));
        List<HealthTip> tips = Arrays.asList(new HealthTip("1", "Tip 1", "Content 1"));

        // Act
        presenter.loadHomeData();

        // Verify loading is shown
        verify(mockView).showLoading();

        // Simulate successful category loading
        ArgumentCaptor<DataCallback<List<Category>>> categoryCallbackCaptor =
            ArgumentCaptor.forClass(DataCallback.class);
        verify(mockCategoryRepository).getAllCategories(categoryCallbackCaptor.capture());
        categoryCallbackCaptor.getValue().onSuccess(categories);

        // Simulate successful tips loading
        ArgumentCaptor<DataCallback<List<HealthTip>>> tipCallbackCaptor =
            ArgumentCaptor.forClass(DataCallback.class);
        verify(mockTipRepository).getPopularTips(eq(10), tipCallbackCaptor.capture());
        tipCallbackCaptor.getValue().onSuccess(tips);

        // Verify
        verify(mockView).showCategories(categories);
        verify(mockView).showPopularTips(tips);
        verify(mockView).hideLoading();
    }

    @Test
    public void testLoadHomeData_Error() {
        // Arrange
        String errorMessage = "Network error";

        // Act
        presenter.loadHomeData();

        // Simulate error in category loading
        ArgumentCaptor<DataCallback<List<Category>>> callbackCaptor =
            ArgumentCaptor.forClass(DataCallback.class);
        verify(mockCategoryRepository).getAllCategories(callbackCaptor.capture());
        callbackCaptor.getValue().onError(errorMessage);

        // Verify
        verify(mockView).showError(errorMessage);
    }
}
15. Implementation Priority
Phase 1 (MVP - 2 tuần):

Cơ sở hạ tầng: Firebase setup, Database structure, Authentication

Core UI: MainActivity, HomeFragment, CategoryFragment với MVP pattern

Cơ bản: CRUD operations cho tips và categories

Đa ngôn ngữ: Cài đặt cơ bản cho Vietnamese và English

Search: Tìm kiếm cơ bản trong tips

Phase 2 (Tính năng chính - 3 tuần):

Authentication: Login/Register system hoàn chỉnh

Favorite System: Thêm/xóa yêu thích

Reminder System: Tạo và quản lý nhắc nhở

Notification: FCM integration và local notifications

Admin Functions: Quản lý tips và categories

Đa ngôn ngữ: Thêm Chinese, Japanese, Korean

Phase 3 (Tính năng nâng cao - 2 tuần):

Chat Bot: Simple AI chat với keyword matching

Advanced Search: Filter, sort, search history

Statistics: User engagement analytics

Offline Support: Cache dữ liệu quan trọng

Performance: Optimize loading và memory usage

Phase 4 (Polish - 1 tuần):

UI/UX: Hoàn thiện giao diện, animations

Testing: Unit tests, integration tests

Bug Fixes: Sửa lỗi và tối ưu hiệu suất

Documentation: Hoàn thiện tài liệu và comments

16. Security & Best Practices
16.1 Firebase Security Rules
Đoạn mã

rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      // User's favorites
      match /favorites/{favoriteId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }

      // User's reminders
      match /reminders/{reminderId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }

    // Tips are readable by authenticated users
    match /tips/{tipId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true);
    }

    // Categories are readable by authenticated users
    match /categories/{categoryId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true);
    }
  }
}
Lưu ý: Phần Firestore Security Rules ở trên chỉ mang tính chất minh họa cho cấu trúc Firestore ban đầu. Vì bạn ưu tiên Realtime Database, bạn sẽ cần định nghĩa Realtime Database Rules phù hợp thay thế cho Firestore Rules.

16.2 ProGuard Rules
# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# Model classes
-keep class com.healthtips.app.data.models.** { *; }

# Dagger
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.internal.Binding
-keep class * extends dagger.internal.ModuleAdapter
-keep class * extends dagger.internal.StaticInjection
17. Monitoring & Analytics
17.1 Firebase Analytics Events
Java

public class AnalyticsHelper {
    private FirebaseAnalytics analytics;

    public AnalyticsHelper(Context context) {
        analytics = FirebaseAnalytics.getInstance(context);
    }

    public void logTipViewed(String tipId, String categoryId) {
        Bundle bundle = new Bundle();
        bundle.putString("tip_id", tipId);
        bundle.putString("category_id", categoryId);
        analytics.logEvent("tip_viewed", bundle);
    }

    public void logSearchPerformed(String query, int resultsCount) {
        Bundle bundle = new Bundle();
        bundle.putString("search_query", query);
        bundle.putInt("results_count", resultsCount);
        analytics.logEvent("search_performed", bundle);
    }

    public void logFavoriteAdded(String tipId) {
        Bundle bundle = new Bundle();
        bundle.putString("tip_id", tipId);
        analytics.logEvent("favorite_added", bundle);
    }

    public void logLanguageChanged(String fromLanguage, String toLanguage) {
        Bundle bundle = new Bundle();
        bundle.putString("from_language", fromLanguage);
        bundle.putString("to_language", toLanguage);
        analytics.logEvent("language_changed", bundle);
    }
}

---

```markdown
# Instructions cho GitHub Copilot - Dự án HealthTips App (Android)

Dự án này là ứng dụng mẹo sức khỏe trên Android, được phát triển bằng Java và tuân thủ kiến trúc MVP (Model-View-Presenter).

## Hướng dẫn chung:
0.  **Ngôn ngữ phản hồi:**
    * **Luôn viết phản hồi / mô tả bằng tiếng Việt.**
1.  **Ngôn ngữ và Kiến trúc:**
    * Sử dụng **Java** làm ngôn ngữ lập trình chính.
    * Tuân thủ chặt chẽ kiến trúc **MVP (Model-View-Presenter)** cho tất cả các màn hình và luồng logic.

2.  **Cấu trúc thư mục:**
    * Tổ chức code theo các thư mục sau:
        * `data/`: Chứa các lớp liên quan đến truy cập và quản lý dữ liệu (models, repositories, data sources, etc.).
        * `presentation/`: Chứa các lớp UI (Activities, Fragments), các Presenters và View interfaces.
        * `services/`: Chứa các Service (ví dụ: background services, reminder services).
        * `di/`: Chứa các module và component của Dagger 2 cho Dependency Injection.
        * `receivers/`: Chứa các Broadcast Receivers.
        * `utils/`: Chứa các lớp tiện ích chung, helpers, constants.

3.  **Tích hợp Firebase:**
    * Firebase đã được tích hợp đầy đủ và sẽ được sử dụng cho các chức năng sau:
        * **Firebase Authentication:** Quản lý đăng nhập/đăng ký người dùng.
        * **Firebase Realtime Database:** **Đây là cơ sở dữ liệu chính được sử dụng để lưu trữ dữ liệu cấu trúc (ví dụ: thông tin mẹo sức khỏe, danh mục, hồ sơ người dùng) và quản lý dữ liệu thời gian thực.**
        * **Firebase Storage:** Lưu trữ các file (ví dụ: hình ảnh cho mẹo sức khỏe).
        * **Firebase Cloud Messaging (FCM):** Gửi thông báo đẩy cho người dùng.
        * **Cloud Firestore:** **Không phải là cơ sở dữ liệu chính cho dữ liệu cấu trúc hiện tại.** Có thể được sử dụng tùy chọn cho các tính năng đặc thù trong tương lai nếu cần.

4.  **Dependency Injection:**
    * Sử dụng **Dagger 2** cho toàn bộ hệ thống Dependency Injection.
    * Đảm bảo các module và component được cấu hình đúng đắn để cung cấp các dependencies cần thiết cho Presenters, Repositories, Services, v.v.

5.  **Giao diện người dùng (UI):**
    * Giao diện được xây dựng bằng **XML**.
    * Tuân thủ nghiêm ngặt **Material Design 3** để đảm bảo tính nhất quán và trải nghiệm người dùng hiện đại.

6.  **Quy ước đặt tên:**
    * Luôn tuân thủ quy ước đặt tên rõ ràng và nhất quán:
        * Adapter: `[Tên]Adapter.java` (ví dụ: `HealthTipAdapter.java`).
        * Presenter: `[Tên]Presenter.java` (ví dụ: `HomePresenter.java`, `LoginPresenter.java`).
        * Service: `[Tên]Service.java` (ví dụ: `ReminderService.java`, `NotificationService.java`).
        * Activity/Fragment: `[Tên]Activity.java`, `[Tên]Fragment.java`.
        * View Interface: `[Tên]Contract.java` (chứa `View` và `Presenter` interfaces lồng vào nhau) hoặc `[Tên]View.java`.
        * Model: `[Tên]Model.java` hoặc chỉ `[Tên].java` (ví dụ: `HealthTip.java`).
        * Repository: `[Tên]Repository.java`.

7.  **Kế thừa Presenter và View:**
    * Tất cả các Presenter phải kế thừa từ một lớp `BasePresenter` chung.
    * Tất cả các View interfaces (hoặc các lớp Activity/Fragment implement View interface) phải implement một interface `BaseView` chung.

8.  **Tuân thủ thiết kế:**
    * Luôn sinh code tuân thủ chặt chẽ **file thiết kế phân tích đã được cung cấp**, bao gồm cả việc chia module và chức năng đã định rõ.

9.  **Phong cách và Giao diện người dùng:**
    * **Ứng dụng sẽ hỗ trợ hai chế độ giao diện:** Chế độ tối (Dark Mode) và Chế độ sáng (Light Mode).
    * **Chế độ tối (Dark Mode - Lấy cảm hứng từ `cu_black.png`):**
        * **Gam màu chủ đạo:**
            * **Nền (Background):** Các tông màu đen xám đậm (ví dụ: `#1A1A1D` hoặc `#212124`) để tạo cảm giác hiện đại, sang trọng và dịu mắt khi sử dụng trong điều kiện thiếu sáng.
            * **Chữ (Text):** Trắng tinh khiết (`#FFFFFF`) hoặc trắng xám nhạt (`#F0F0F0`) để đảm bảo độ tương phản cao và dễ đọc trên nền tối.
        * **Màu sắc các nút (Buttons):**
            * **Nút chính (Primary Buttons):** Sử dụng gradient chuyển màu từ xanh lá cây đậm (ví dụ: `#4CAF50` hoặc `#2E7D32`) sang vàng chanh (`#C0CA33` hoặc xanh lá cây nhạt hơn `#8BC34A`). Chữ trên nút phải là màu trắng (`#FFFFFF`).
            * **Nút phụ/Hành động khác (Secondary Buttons):** Sử dụng gradient chuyển màu từ cam tươi (`#FF9800` hoặc `#F57C00`) sang đỏ cam (`#FF5722` hoặc đỏ gạch `#D32F2F`). Chữ trên nút nên là màu trắng (`#FFFFFF`).
        * **Các thành phần UI khác:**
            * **Đường viền/phân cách:** Nên là màu xám nhạt (`#424242`) hoặc xám đậm hơn một chút so với nền để tạo sự phân tách tinh tế.
            * **Biểu tượng (Icons):** Màu trắng (`#FFFFFF`) hoặc các màu tương đồng với gam màu của các nút để duy trì sự nhất quán.
            * **Điểm nhấn/Highlight:** Có thể sử dụng các màu xanh dương sáng từ logo `cu_black.png` (ví dụ: `#00BFFF` hoặc `#1E90FF`) cho các yếu tố tương tác nhỏ, đường viền hoặc trạng thái được chọn.
    * **Chế độ sáng (Light Mode - Lấy cảm hứng từ `cu_night.png`):**
        * **Gam màu chủ đạo:**
            * **Nền (Background):** Các tông màu xanh dương nhạt hoặc trắng sáng (ví dụ: `#E0F2F7` hoặc `#FFFFFF`), tạo cảm giác tươi mới và dễ chịu.
            * **Chữ (Text):** Xám đậm (ví dụ: `#212124` hoặc `#424242`) hoặc đen (`#000000`) để đảm bảo độ tương phản trên nền sáng.
        * **Màu sắc các nút (Buttons):**
            * **Nút chính (Primary Buttons):** Sử dụng các tông màu xanh dương (ví dụ: `#2196F3` hoặc `#1976D2`) hoặc xanh lá cây (ví dụ: `#4CAF50`) tương tự như logo `cu_night.png`. Chữ trên nút phải là màu trắng (`#FFFFFF`).
            * **Nút phụ/Hành động khác (Secondary Buttons):** Sử dụng màu sắc tương phản nhưng vẫn hài hòa, ví dụ: màu xám đậm (`#616161`) hoặc cam nhạt (`#FFB74D`). Chữ trên nút nên là màu trắng (`#FFFFFF`) hoặc đen (`#000000`).
        * **Các thành phần UI khác:**
            * **Đường viền/phân cách:** Nên là màu xám nhạt (`#BDBDBD`) hoặc xanh nhạt (`#81D4FA`).
            * **Biểu tượng (Icons):** Màu đen (`#000000`), xanh lam đậm (`#1976D2`), hoặc các màu xanh từ logo `cu_night.png` để phù hợp với nền sáng.
            * **Điểm nhấn/Highlight:** Các màu cam/vàng từ bóng đèn trên logo `cu_night.png` (ví dụ: `#FFC107` hoặc `#FFEB3B`) có thể được sử dụng để làm nổi bật các yếu tố quan trọng hoặc trạng thái.
    * **Hiệu ứng chung:** Ưu tiên sử dụng gradient cho các nút để tạo chiều sâu và điểm nhấn trong cả hai chế độ. Đảm bảo tính nhất quán về hình dạng, khoảng cách và kiểu chữ giữa hai chế độ để trải nghiệm người dùng không bị gián đoạn.

10. **Tránh trùng lặp File và Chức năng:**
    * **Trước khi đề xuất hoặc tạo bất kỳ file mới nào (Activity, Fragment, Presenter, Model, Service, v.v.) hoặc triển khai một chức năng mới, Copilot phải kiểm tra kỹ lưỡng toàn bộ cấu trúc dự án hiện có.**
    * **Nếu phát hiện đã tồn tại một file hoặc một phần code thực hiện chức năng tương tự hoặc cùng tên trong dự án (dựa trên tên file, quy ước đặt tên, hoặc logic đã có), Copilot phải thông báo và KHÔNG tạo ra bản sao.**
    * **Thay vào đó, Copilot sẽ:**
        * **Đề xuất tích hợp hoặc mở rộng chức năng hiện có** vào file hoặc module đã tồn tại.
        * **Chỉ tạo file mới khi không có bất kỳ file hoặc chức năng tương tự nào tồn tại** và nó thực sự cần thiết cho một module hoặc tính năng mới hoàn toàn.
        * **Khi tiếp tục một chức năng đã được bắt đầu ở bước trước, Copilot phải tiếp tục làm việc trên các file đã được tạo ra cho chức năng đó, không tạo lại chúng ở thư mục khác.**
    * **Luôn ưu tiên việc tái sử dụng và mở rộng code hiện có.**

11. **Quản lý tài nguyên:**
    * **Tập trung tài nguyên vào các file chung:**
        * **Strings:** Tất cả chuỗi văn bản phải được đặt trong file `strings.xml` chung, không tạo file strings riêng cho từng tính năng.
        * **Colors:** Tất cả định nghĩa màu sắc phải được đặt trong file `colors.xml` chung, không tạo file colors riêng biệt.
        * **Styles/Themes:** Tất cả styles và themes phải được đặt trong file `styles.xml` hoặc `themes.xml` chung, không tạo file styles riêng.
        * **Dimensions:** Tất cả kích thước phải được đặt trong file `dimens.xml` chung.
    * **Đặt tên tài nguyên:**
        * Đặt tên theo cấu trúc `[feature]_[type]_[description]` (ví dụ: `category_title`, `home_description`, `auth_button_text`)
        * Đảm bảo tên mô tả đúng mục đích sử dụng và dễ hiểu
    * **Tránh trùng lặp tài nguyên:** Kiểm tra kỹ trước khi thêm tài nguyên mới, tái sử dụng tài nguyên hiện có nếu phù hợp.
    * **Tài nguyên hình ảnh:**
        * Vector Drawables (XML) được ưu tiên hơn bitmap cho biểu tượng và đồ họa đơn giản
        * Bitmap (PNG, JPEG) chỉ sử dụng cho hình ảnh phức tạp không thể biểu diễn bằng vector

12. **Chi tiết Phân tích Dự án:**
    * **Để có cái nhìn toàn diện và chi tiết về cấu trúc dự án, các thành phần công nghệ, mô hình dữ liệu, quy tắc bảo mật, và lộ trình phát triển, Copilot HÃY THAM KHẢO file `Project_Analysis_Details.md` được cung cấp trong cùng thư mục dự án.**
    * **File này chứa thông tin chi tiết về:**
        * Tổng quan dự án (SDK, Architecture, Database, Authentication, v.v.).
        * Cấu trúc thư mục chi tiết.
        * Định nghĩa các Data Models (User, HealthTip, Category, Reminder, ChatMessage, Enums).
        * Cấu hình Firebase (FirebaseManager, Realtime Database Structure).
        * Chi tiết triển khai kiến trúc MVP (Base Classes, Repository Pattern).
        * Triển khai đa ngôn ngữ (LocaleHelper, cấu trúc resource, ví dụ strings.xml).
        * Các Activity và Fragment chính.
        * Hệ thống thông báo (FCMService, ReminderService).
        * Hệ thống Chat Bot.
        * Permissions và cấu hình AndroidManifest.xml.
        * Cấu hình Build (build.gradle).
        * Class Application.
        * Cấu hình Dependency Injection với Dagger 2.
        * Chiến lược kiểm thử (Unit Tests, Presenter Tests).
        * Ưu tiên triển khai tính năng theo từng Phase.
        * Các quy tắc bảo mật Firebase (Realtime Database Rules) và ProGuard.
        * Monitoring & Analytics (Firebase Analytics Events).

13. **Hướng dẫn theo ngữ cảnh Chức năng:**
    * **Copilot PHẢI chủ động đọc và phân tích file `Project_Analysis_Details.md` dựa trên chức năng hoặc nhiệm vụ mà người dùng đang thực hiện.**
    * **Khi người dùng yêu cầu thực hiện một tác vụ hoặc đang làm việc trong một file cụ thể, Copilot cần:**
        * **Xác định chức năng liên quan:** Ví dụ, nếu người dùng đang chỉnh sửa `HomeFragment.java` hoặc yêu cầu "tải dữ liệu trang chủ", Copilot phải hiểu rằng đây là một phần của "Home Fragment với MVP" và "Phase 1 (MVP)".
        * **Truy xuất thông tin liên quan:** Tìm kiếm các phần trong `Project_Analysis_Details.md` mô tả chi tiết về chức năng đó, bao gồm:
            * **Mô hình dữ liệu (Models):** Các Model liên quan (ví dụ: `HealthTip`, `Category`).
            * **Giao diện (Views):** Giao diện View tương ứng (ví dụ: `HomeView`).
            * **Presenter:** Presenter liên quan (ví dụ: `HomePresenter`).
            * **Repository:** Các Repository được sử dụng (ví dụ: `HealthTipRepository`, `CategoryRepository`).
            * **Cấu trúc Firebase:** Các Collection hoặc cấu trúc dữ liệu Firebase liên quan.
            * **Quy ước đặt tên:** Tên file và thư mục chính xác theo quy ước.
            * **Ưu tiên triển khai (Implementation Priority):** Xác định giai đoạn hiện tại của chức năng để gợi ý các bước tiếp theo phù hợp với lộ trình dự án.
        * **Đề xuất và Hỗ trợ thông minh:** Dựa trên thông tin đã truy xuất, Copilot sẽ đưa ra các gợi ý code, giải thích, hoặc các bước tiếp theo một cách chính xác và phù hợp với thiết kế tổng thể của dự án.
        * **Nếu không rõ ngữ cảnh:** Nếu Copilot không thể xác định rõ chức năng hiện tại hoặc các thông tin liên quan trong `Project_Analysis_Details.md`, nó sẽ hỏi người dùng để làm rõ.

---

## Những điều cần lưu ý khi sinh code:

* **Tính mô đun:** Tập trung vào việc tạo ra các thành phần nhỏ, có trách nhiệm duy nhất.
* **Tính khả dụng lại:** Viết code có thể tái sử dụng.
* **Xử lý lỗi:** Bao gồm các cơ chế xử lý lỗi phù hợp (ví dụ: try-catch, kiểm tra null).
* **Phản hồi UI:** Đảm bảo có phản hồi phù hợp cho người dùng khi thực hiện các thao tác (ví dụ: loading states, thông báo lỗi/thành công).
* **Bảo mật:** Lưu ý các vấn đề bảo mật cơ bản khi làm việc với dữ liệu người dùng và Firebase.
* **Hiệu suất:** Cân nhắc hiệu suất, đặc biệt là khi làm việc với dữ liệu lớn hoặc các thao tác UI phức tạp.

---

**Lưu ý quan trọng cho việc phát triển (Tham khảo thêm từ Project_Analysis_Details.md):**

* Luôn sử dụng Java best practices và tuân thủ coding conventions.
* Implement proper error handling cho tất cả Firebase operations.
* Test trên nhiều thiết bị và API levels khác nhau.
* Optimize cho performance - đặc biệt là loading times và memory usage.
* Follow Material Design guidelines cho UI/UX consistency.
* Implement proper lifecycle management cho Activities và Fragments.
* Use proper logging với different levels (DEBUG, INFO, WARNING, ERROR).
* Secure sensitive data và không hardcode API keys trong source code.
* Implement offline capabilities cho user experience tốt hơn.
* Regular backup Firebase data và có disaster recovery plan.

**Đối với đa ngôn ngữ (Tham khảo thêm từ Project_Analysis_Details.md):**

* Luôn sử dụng string resources thay vì hardcode text.
* Test UI layout với các ngôn ngữ có text dài (German) và ngắn (Chinese).
* Implement proper RTL support cho các ngôn ngữ như Arabic (nếu cần).
* Use appropriate fonts cho các ngôn ngữ khác nhau.






nếu được thì tạo file md luôn đi không thì hãy để chung vô 1 ô để tôi coppy cho dễ



Hiện tiến trình tư duy
Tuyệt vời, tôi hiểu rồi! Bạn muốn có cả hai file Markdown (đã được sửa lỗi ký tự đặc biệt và cập nhật thông tin Realtime Database) được cung cấp trong một khối văn bản duy nhất để dễ dàng sao chép.

Đây là nội dung của hai file, được đặt chung trong một khối mã Markdown lớn:

Markdown

# Project_Analysis_Details.md

HealthTips - Ứng dụng Mẹo chăm sóc sức khỏe hàng ngày

### 1. Tổng quan dự án

* Platform: Android (Java)
* Min SDK: 24 (Android 7.0)
* Target SDK: 34
* Architecture: MVP (Model-View-Presenter)
* **Database: Firebase Realtime Database (Chính) + Firebase Firestore (Tùy chọn)**
* Authentication: Firebase Auth
* Storage: Firebase Storage
* Notifications: Firebase Cloud Messaging (FCM)
* Analytics: Firebase Analytics
* Crash Reporting: Firebase Crashlytics
* UI: Material Design 3 + XML Layouts
* Async: AsyncTask, CompletableFuture, RxJava (tùy chọn)
* Dependency Injection: Dagger 2
* Multi-language: Android Localization (strings.xml)

### 2. Cấu trúc thư mục (MVP Pattern)

```
app/
├── data/
│   ├── firebase/
│   │   ├── FirebaseManager.java
│   │   ├── AuthManager.java
│   │   ├── FirestoreManager.java
│   │   └── StorageManager.java
│   ├── models/
│   │   ├── User.java
│   │   ├── HealthTip.java
│   │   ├── Category.java
│   │   ├── Favorite.java
│   │   ├── Reminder.java
│   │   └── ChatMessage.java
│   ├── repositories/
│   │   ├── UserRepository.java
│   │   ├── HealthTipRepository.java
│   │   ├── CategoryRepository.java
│   │   └── FavoriteRepository.java
│   └── local/
│       ├── SharedPreferencesManager.java
│       └── CacheManager.java
├── presentation/
│   ├── activities/
│   │   ├── MainActivity.java
│   │   ├── LoginActivity.java
│   │   ├── SplashActivity.java
│   │   └── AdminActivity.java
│   ├── fragments/
│   │   ├── HomeFragment.java
│   │   ├── CategoryFragment.java
│   │   ├── FavoriteFragment.java
│   │   ├── ReminderFragment.java
│   │   └── ProfileFragment.java
│   ├── presenters/
│   │   ├── HomePresenter.java
│   │   ├── CategoryPresenter.java
│   │   ├── AuthPresenter.java
│   │   └── BasePresenter.java
│   ├── views/
│   │   ├── HomeView.java
│   │   ├── CategoryView.java
│   │   ├── AuthView.java
│   │   └── BaseView.java
│   ├── adapters/
│   │   ├── CategoryAdapter.java
│   │   ├── HealthTipAdapter.java
│   │   ├── ChatAdapter.java
│   │   └── ReminderAdapter.java
│   └── dialogs/
│       ├── ReminderDialog.java
│       └── LanguageDialog.java
├── services/
│   ├── NotificationService.java
│   ├── ReminderService.java
│   └── ChatBotService.java
├── utils/
│   ├── Constants.java
│   ├── Utils.java
│   ├── DateUtils.java
│   ├── LocaleHelper.java
│   └── ValidationUtils.java
├── di/
│   ├── AppModule.java
│   ├── NetworkModule.java
│   └── AppComponent.java
└── receivers/
    ├── BootReceiver.java
    └── AlarmReceiver.java
```

### 3. Data Models (Java Classes)

#### 3.1 Core Models

```java
// User Model
public class User {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private long createdAt;
    private boolean isActive;
    private boolean isAdmin;
    private String preferredLanguage;

    // Constructors
    public User() {}

    public User(String id, String username, String email, String fullName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
        this.isAdmin = false;
        this.preferredLanguage = "vi"; // default Vietnamese
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // ... other getters and setters
}

// Health Tip Model
public class HealthTip {
    private String id;
    private String title;
    private String content;
    private String shortDescription;
    private String categoryId;
    private List<String> tags;
    private String createdBy;
    private long createdAt;
    private long updatedAt;
    private boolean isActive;
    private int viewCount;
    private Map<String, String> titleTranslations; // {"vi": "Tiêu đề", "en": "Title"}
    private Map<String, String> contentTranslations;

    // Constructors
    public HealthTip() {
        this.tags = new ArrayList<>();
        this.titleTranslations = new HashMap<>();
        this.contentTranslations = new HashMap<>();
    }

    // Getters and Setters
    public String getLocalizedTitle(String language) {
        return titleTranslations.getOrDefault(language, title);
    }

    public String getLocalizedContent(String language) {
        return contentTranslations.getOrDefault(language, content);
    }

    // ... other methods
}

// Category Model
public class Category {
    private String id;
    private String name;
    private String description;
    private String icon;
    private String color;
    private long createdAt;
    private boolean isActive;
    private Map<String, String> nameTranslations;
    private Map<String, String> descriptionTranslations;

    // Constructors and methods
    public Category() {
        this.nameTranslations = new HashMap<>();
        this.descriptionTranslations = new HashMap<>();
    }

    public String getLocalizedName(String language) {
        return nameTranslations.getOrDefault(language, name);
    }

    // ... other methods
}

// Favorite Model
public class Favorite {
    private String id;
    private String userId;
    private String tipId;
    private long createdAt;

    // Constructors, getters, setters
}

// Reminder Model
public class Reminder {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String time; // "HH:mm"
    private ReminderFrequency frequency;
    private boolean isActive;
    private List<String> categories;
    private long createdAt;
    private Map<String, String> titleTranslations;
    private Map<String, String> descriptionTranslations;

    // Constructors, getters, setters
}

// Chat Message Model
public class ChatMessage {
    private String id;
    private String sessionId;
    private String message;
    private boolean isFromUser;
    private long timestamp;
    private List<String> relatedTips;

    // Constructors, getters, setters
}

// Enums
public enum ReminderFrequency {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly");

    private final String value;

    ReminderFrequency(String value) {
        this.value = value;
    }

    public String getValue() { return value; }
}

public enum NotificationType {
    REMINDER, CHAT, SYSTEM
}
```

### 4. Firebase Configuration

#### 4.1 Firebase Manager

```java
public class FirebaseManager {
    private static FirebaseManager instance;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseDatabase realtimeDb;

    private FirebaseManager() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        realtimeDb = FirebaseDatabase.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public FirebaseFirestore getFirestore() { return firestore; }
    public FirebaseAuth getAuth() { return auth; }
    public FirebaseStorage getStorage() { return storage; }
    public FirebaseDatabase getRealtimeDb() { return realtimeDb; }
}
```

#### 4.2 Realtime Database Structure

Dữ liệu sẽ được tổ chức dưới dạng cây JSON trong Firebase Realtime Database như sau:

```json
{
  "healthtips_app": {
    "users": {
      "{userId}": {
        "profile": {
          "email": "user@example.com",
          "username": "tên_người_dùng",
          "fullName": "Tên đầy đủ",
          "phone": "0123456789",
          "createdAt": 1678886400000,
          "isActive": true,
          "isAdmin": false,
          "preferredLanguage": "vi",
          "fcmToken": "token_device"
        },
        "favorites": {
          "{favoriteId}": {
            "tipId": "tip_id_của_mẹo_yêu_thích",
            "createdAt": 1678886500000
          }
        },
        "reminders": {
          "{reminderId}": {
            "title": "Nhắc nhở uống nước",
            "description": "Hãy uống đủ nước mỗi giờ!",
            "time": "10:00",
            "frequency": "DAILY",
            "isActive": true,
            "categories": ["cat001"],
            "createdAt": 1678886600000
          }
        }
      }
    },
    "categories": {
      "{categoryId}": {
        "name": "Dinh dưỡng",
        "description": "Các mẹo về dinh dưỡng và chế độ ăn uống",
        "iconUrl": "url_to_icon_image",
        "color": "#HEX_COLOR",
        "createdAt": 1721195400000,
        "isActive": true,
        "nameTranslations": {"vi": "Dinh dưỡng", "en": "Nutrition"},
        "descriptionTranslations": {"vi": "Mô tả tiếng Việt", "en": "English description"}
      }
    },
    "health_tips": {
      "{tipId}": {
        "title": "10 mẹo ăn uống lành mạnh",
        "content": "Nội dung chi tiết...",
        "shortDescription": "Tóm tắt ngắn gọn...",
        "categoryId": "cat001",
        "tags": ["dinhduong", "suckhoe"],
        "createdBy": "admin_user_id",
        "createdAt": 1721195800000,
        "updatedAt": 1721195900000,
        "isActive": true,
        "viewCount": 150,
        "likeCount": 25,
        "imageUrl": "url_to_tip_image",
        "titleTranslations": {"vi": "Tiêu đề tiếng Việt", "en": "English title"},
        "contentTranslations": {"vi": "Nội dung tiếng Việt", "en": "English content"}
      }
    },
    "chat-sessions": {
      "{sessionId}": {
        "userId": "user_id",
        "createdAt": 1678887000000,
        "messages": {
          "{messageId}": {
            "message": "Xin chào!",
            "isFromUser": true,
            "timestamp": 1678887005000,
            "relatedTips": ["tip001"]
          }
        }
      }
    },
    "app-settings": {
      "localization": {
        "version": "1.0",
        "lastUpdated": 1678887100000,
        "strings": {
          "vi": {
            "app_name": "Mẹo Sức Khỏe"
          },
          "en": {
            "app_name": "HealthTips"
          }
        }
      }
    }
  }
}
```

### 5. MVP Architecture Implementation

#### 5.1 Base Classes

```java
// BaseView interface
public interface BaseView {
    void showLoading();
    void hideLoading();
    void showError(String message);
    void showSuccess(String message);
    Context getContext();
}

// BasePresenter abstract class
public abstract class BasePresenter<V extends BaseView> {
    protected V view;
    protected CompositeDisposable disposables;

    public BasePresenter() {
        disposables = new CompositeDisposable();
    }

    public void attachView(V view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        if (disposables != null) {
            disposables.clear();
        }
    }

    protected boolean isViewAttached() {
        return view != null;
    }
}

// Example: HomeView interface
public interface HomeView extends BaseView {
    void showCategories(List<Category> categories);
    void showPopularTips(List<HealthTip> tips);
    void navigateToCategory(String categoryId);
    void navigateToTip(String tipId);
}

// Example: HomePresenter
public class HomePresenter extends BasePresenter<HomeView> {
    private CategoryRepository categoryRepository;
    private HealthTipRepository tipRepository;

    @Inject
    public HomePresenter(CategoryRepository categoryRepository,
                        HealthTipRepository tipRepository) {
        this.categoryRepository = categoryRepository;
        this.tipRepository = tipRepository;
    }

    public void loadHomeData() {
        if (!isViewAttached()) return;

        view.showLoading();

        // Load categories
        categoryRepository.getAllCategories(new DataCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (isViewAttached()) {
                    view.showCategories(categories);
                }
            }

            @Override
            public void onError(String error) {
                if (isViewAttached()) {
                    view.showError(error);
                }
            }
        });

        // Load popular tips
        tipRepository.getPopularTips(10, new DataCallback<List<HealthTip>>() {
            @Override
            public void onSuccess(List<HealthTip> tips) {
                if (isViewAttached()) {
                    view.showPopularTips(tips);
                    view.hideLoading();
                }
            }

            @Override
            public void onError(String error) {
                if (isViewAttached()) {
                    view.showError(error);
                    view.hideLoading();
                }
            }
        });
    }
}
```

#### 5.2 Repository Pattern

```java
public interface HealthTipRepository {
    void getAllTips(DataCallback<List<HealthTip>> callback);
    void getTipsByCategory(String categoryId, DataCallback<List<HealthTip>> callback);
    void searchTips(String query, DataCallback<List<HealthTip>> callback);
    void getTipById(String id, DataCallback<HealthTip> callback);
    void addTip(HealthTip tip, DataCallback<Void> callback);
    void updateTip(HealthTip tip, DataCallback<Void> callback);
    void deleteTip(String id, DataCallback<Void> callback);
    void getPopularTips(int limit, DataCallback<List<HealthTip>> callback);
}

public class HealthTipRepositoryImpl implements HealthTipRepository {
    private FirebaseFirestore firestore;

    private static final String COLLECTION_TIPS = "tips";

    @Inject
    public HealthTipRepositoryImpl(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void getAllTips(DataCallback<List<HealthTip>> callback) {
        firestore.collection(COLLECTION_TIPS)
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HealthTip> tips = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        HealthTip tip = doc.toObject(HealthTip.class);
                        if (tip != null) {
                            tip.setId(doc.getId());
                            tips.add(tip);
                        }
                    }
                    callback.onSuccess(tips);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Implement other methods...
}

// Callback interface
public interface DataCallback<T> {
    void onSuccess(T data);
    void onError(String error);
}
```

### 6. Đa ngôn ngữ (Internationalization)

#### 6.1 Locale Helper

```java
public class LocaleHelper {
    private static final String SELECTED_LANGUAGE = "selected_language";

    public static void setLocale(Context context, String language) {
        persist(context, language);
        updateResources(context, language);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }
}
```

#### 6.2 Cấu trúc tài nguyên đa ngôn ngữ

```
res/
├── values/                 # Default (English)
│   ├── strings.xml
│   ├── colors.xml
│   └── dimens.xml
├── values-vi/             # Vietnamese
│   └── strings.xml
├── values-en/             # English
│   └── strings.xml
├── values-zh/             # Chinese
│   └── strings.xml
├── values-ja/             # Japanese
│   └── strings.xml
└── values-ko/             # Korean
    └── strings.xml
```

#### 6.3 Strings.xml Examples

```xml
<resources>
    <string name="app_name">HealthTips</string>
    <string name="home">Home</string>
    <string name="categories">Categories</string>
    <string name="favorites">Favorites</string>
    <string name="reminders">Reminders</string>
    <string name="profile">Profile</string>
    <string name="search_hint">Search health tips...</string>
    <string name="no_internet">No internet connection</string>
    <string name="loading">Loading...</string>
    <string name="error_occurred">An error occurred</string>
    <string name="welcome_message">Welcome to HealthTips!</string>
    <string name="daily_health_tip">Daily Health Tip</string>
    <string name="popular_tips">Popular Tips</string>
    <string name="add_to_favorites">Add to Favorites</string>
    <string name="remove_from_favorites">Remove from Favorites</string>
    <string name="share_tip">Share Tip</string>
    <string name="set_reminder">Set Reminder</string>
    <string name="language_settings">Language Settings</string>
    <string name="select_language">Select Language</string>
    <string name="vietnamese">Tiếng Việt</string>
    <string name="english">English</string>
    <string name="chinese">中文</string>
    <string name="japanese">日本語</string>
    <string name="korean">한국어</string>
</resources>

<resources>
    <string name="app_name">Mẹo Sức Khỏe</string>
    <string name="home">Trang chủ</string>
    <string name="categories">Chủ đề</string>
    <string name="favorites">Yêu thích</string>
    <string name="reminders">Nhắc nhở</string>
    <string name="profile">Cá nhân</string>
    <string name="search_hint">Tìm kiếm mẹo sức khỏe...</string>
    <string name="no_internet">Không có kết nối internet</string>
    <string name="loading">Đang tải...</string>
    <string name="error_occurred">Đã xảy ra lỗi</string>
    <string name="welcome_message">Chào mừng đến với Mẹo Sức Khỏe!</string>
    <string name="daily_health_tip">Mẹo Sức Khỏe Hàng Ngày</string>
    <string name="popular_tips">Mẹo Phổ Biến</string>
    <string name="add_to_favorites">Thêm vào yêu thích</string>
    <string name="remove_from_favorites">Xóa khỏi yêu thích</string>
    <string name="share_tip">Chia sẻ mẹo</string>
    <string name="set_reminder">Đặt nhắc nhở</string>
    <string name="language_settings">Cài đặt ngôn ngữ</string>
    <string name="select_language">Chọn ngôn ngữ</string>
    <string name="vietnamese">Tiếng Việt</string>
    <string name="english">English</string>
    <string name="chinese">中文</string>
    <string name="japanese">日本語</string>
    <string name="korean">한국어</string>
</resources>
```

### 7. Key Activities và Fragments

#### 7.1 MainActivity

```java
public class MainActivity extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply language setting
        String language = LocaleHelper.getLanguage(this);
        LocaleHelper.setLocale(this, language);

        setContentView(R.layout.activity_main);

        initViews();
        setupBottomNavigation();
        setupDrawerNavigation();
        loadHomeFragment();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNav = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_categories) {
                fragment = new CategoryFragment();
            } else if (itemId == R.id.nav_favorites) {
                fragment = new FavoriteFragment();
            } else if (itemId == R.id.nav_reminders) {
                fragment = new ReminderFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
                return true;
            }
            return false;
        });
    }

    // Other methods...
}
```

#### 7.2 HomeFragment với MVP

```java
public class HomeFragment extends Fragment implements HomeView {
    private HomePresenter presenter;
    private RecyclerView categoryRecyclerView;
    private RecyclerView tipsRecyclerView;
    private ProgressBar progressBar;
    private CategoryAdapter categoryAdapter;
    private HealthTipAdapter tipAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerViews();

        // Inject presenter
        presenter = new HomePresenter(
            new CategoryRepositoryImpl(FirebaseManager.getInstance().getFirestore()),
            new HealthTipRepositoryImpl(FirebaseManager.getInstance().getFirestore())
        );

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);
        presenter.loadHomeData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    // HomeView implementation
    @Override
    public void showCategories(List<Category> categories) {
        categoryAdapter.updateData(categories);
    }

    @Override
    public void showPopularTips(List<HealthTip> tips) {
        tipAdapter.updateData(tips);
    }

    @Override
    public void navigateToCategory(String categoryId) {
        // Navigate to category detail
        Intent intent = new Intent(getContext(), CategoryDetailActivity.class);
        intent.putExtra("categoryId", categoryId);
        startActivity(intent);
    }

    @Override
    public void navigateToTip(String tipId) {
        // Navigate to tip detail
        Intent intent = new Intent(getContext(), TipDetailActivity.class);
        intent.putExtra("tipId", tipId);
        startActivity(intent);
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Other methods...
}
```

### 8. Notification System với Firebase

#### 8.1 Firebase Cloud Messaging

```java
public class FCMService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Handle FCM messages
        if (remoteMessage.getData().size() > 0) {
            handleDataMessage(remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            handleNotificationMessage(remoteMessage.getNotification());
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        String type = data.get("type");
        String tipId = data.get("tipId");

        if ("daily_tip".equals(type) && tipId != null) {
            showDailyTipNotification(tipId);
        }
    }

    private void showDailyTipNotification(String tipId) {
        // Create notification for daily tip
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "health_tips")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.daily_health_tip))
                .setContentText(getString(R.string.tap_to_read))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent intent = new Intent(this, TipDetailActivity.class);
        intent.putExtra("tipId", tipId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Send token to server
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        // Update user's FCM token in Firestore
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .update("fcmToken", token);
        }
    }
}
```

#### 8.2 Reminder Service

```java
public class ReminderService extends Service {
    private static final String TAG = "ReminderService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("SHOW_REMINDER".equals(action)) {
                String reminderId = intent.getStringExtra("reminderId");
                showReminderNotification(reminderId);
            }
        }
        return START_NOT_STICKY;
    }

    private void showReminderNotification(String reminderId) {
        // Get reminder from database and show notification
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(getCurrentUserId())
                .collection("reminders")
                .document(reminderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Reminder reminder = documentSnapshot.toObject(Reminder.class);
                        if (reminder != null && reminder.isActive()) {
                            createReminderNotification(reminder);
                        }
                    }
                });
    }

    private void createReminderNotification(Reminder reminder) {
        String language = LocaleHelper.getLanguage(this);
        String title = reminder.getLocalizedTitle(language);
        String description = reminder.getLocalizedDescription(language);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "reminders")
                .setSmallIcon(R.drawable.ic_reminder)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(reminder.getId().hashCode(), builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : "";
    }
}
```

### 9. Chat Bot System

#### 9.1 Simple Chat Bot Service

```java
public class ChatBotService {
    private static ChatBotService instance;
    private HealthTipRepository tipRepository;
    private Map<String, List<String>> keywordMap;

    private ChatBotService() {
        tipRepository = new HealthTipRepositoryImpl(FirebaseManager.getInstance().getFirestore());
        initializeKeywordMap();
    }

    public static synchronized ChatBotService getInstance() {
        if (instance == null) {
            instance = new ChatBotService();
        }
        return instance;
    }

    private void initializeKeywordMap() {
        keywordMap = new HashMap<>();
        keywordMap.put("tim", Arrays.asList("heart", "cardiac", "cardiovascular"));
        keywordMap.put("huyết áp", Arrays.asList("blood pressure", "hypertension"));
        keywordMap.put("tiểu đường", Arrays.asList("diabetes", "blood sugar"));
        keywordMap.put("giảm cân", Arrays.asList("weight loss", "diet", "fitness"));
        keywordMap.put("tập thể dục", Arrays.asList("exercise", "workout", "fitness"));
        keywordMap.put("ăn uống", Arrays.asList("nutrition", "food", "diet"));
        keywordMap.put("ngủ", Arrays.asList("sleep", "insomnia", "rest"));
        keywordMap.put("stress", Arrays.asList("anxiety", "mental health", "relaxation"));
    }

    public void processMessage(String message, String sessionId, DataCallback<ChatMessage> callback) {
        // Analyze message and find relevant tips
        List<String> keywords = extractKeywords(message.toLowerCase());

        if (keywords.isEmpty()) {
            // Default response
            ChatMessage response = new ChatMessage();
            response.setId(generateId());
            response.setSessionId(sessionId);
            response.setMessage("Xin chào! Tôi có thể giúp bạn tìm kiếm các mẹo sức khỏe. Hãy hỏi tôi về tim mạch, huyết áp, tiểu đường, giảm cân, tập thể dục, ăn uống, ngủ nghỉ hoặc stress.");
            response.setFromUser(false);
            response.setTimestamp(System.currentTimeMillis());
            callback.onSuccess(response);
            return;
        }

        // Search for relevant tips
        searchRelevantTips(keywords, new DataCallback<List<HealthTip>>() {
            @Override
            public void onSuccess(List<HealthTip> tips) {
                ChatMessage response = generateResponse(tips, sessionId);
                callback.onSuccess(response);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private List<String> extractKeywords(String message) {
        List<String> foundKeywords = new ArrayList<>();
        for (String keyword : keywordMap.keySet()) {
            if (message.contains(keyword)) {
                foundKeywords.add(keyword);
            }
        }
        return foundKeywords;
    }

    private void searchRelevantTips(List<String> keywords, DataCallback<List<HealthTip>> callback) {
        // Search tips based on keywords
        tipRepository.searchTips(String.join(" ", keywords), callback);
    }

    private ChatMessage generateResponse(List<HealthTip> tips, String sessionId) {
        ChatMessage response = new ChatMessage();
        response.setId(generateId());
        response.setSessionId(sessionId);
        response.setFromUser(false);
        response.setTimestamp(System.currentTimeMillis());

        if (tips.isEmpty()) {
            response.setMessage("Xin lỗi, tôi không tìm thấy mẹo nào phù hợp với câu hỏi của bạn. Bạn có thể thử hỏi về các chủ đề khác không?");
        } else {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Tôi đã tìm thấy ").append(tips.size()).append(" mẹo sức khỏe liên quan:\n\n");

            List<String> tipIds = new ArrayList<>();
            for (int i = 0; i < Math.min(tips.size(), 3); i++) {
                HealthTip tip = tips.get(i);
                messageBuilder.append("• ").append(tip.getTitle()).append("\n");
                messageBuilder.append("  ").append(tip.getShortDescription()).append("\n\n");
                tipIds.add(tip.getId());
            }

            messageBuilder.append("Bạn có muốn xem chi tiết các mẹo này không?");
            response.setMessage(messageBuilder.toString());
            response.setRelatedTips(tipIds);
        }

        return response;
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
```

### 10. Permissions và Manifest

#### 10.1 AndroidManifest.xml

```xml
<manifest xmlns:android="[http://schemas.android.com/apk/res/android](http://schemas.android.com/apk/res/android)"
    package="com.healthtips.app">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

    <application
        android:name=".HealthTipsApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.HealthTips">

        <activity
            android:name=".presentation.activities.SplashActivity"
            android:theme="@style/SplashTheme"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".presentation.activities.MainActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <activity
            android:name=".presentation.activities.LoginActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <activity
            android:name=".presentation.activities.TipDetailActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <activity
            android:name=".presentation.activities.CategoryDetailActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <activity
            android:name=".presentation.activities.AdminActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <service
            android:name=".services.FCMService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

        <service
            android:name=".services.ReminderService"
            android:exported="false" />

        <receiver
            android:name=".receivers.BootReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>

        <receiver
            android:name=".receivers.AlarmReceiver"
            android:exported="false" />

        <meta-data
            android:name="com.google.firebase.messaging.default_notification_icon"
            android:resource="@drawable/ic_notification" />

        <meta-data
            android:name="com.google.firebase.messaging.default_notification_color"
            android:resource="@color/colorAccent" />

        <meta-data
            android:name="com.google.firebase.messaging.default_notification_channel_id"
            android:value="health_tips" />

    </application>
</manifest>
```

### 11. Build Configuration

#### 11.1 build.gradle (Module: app)

```gradle
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services'
    id 'com.google.firebase.crashlytics'
}

android {
    compileSdk 34

    defaultConfig {
        applicationId "com.healthtips.app"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"

        // Multi-language support
        resConfigs "vi", "en", "zh", "ja", "ko"
    }

    buildTypes {
        debug {
            debuggable true
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }

        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.debug
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding true
    }
}

dependencies {
    // Core Android
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'

    // Navigation
    implementation 'androidx.navigation:navigation-fragment:2.7.6'
    implementation 'androidx.navigation:navigation-ui:2.7.6'

    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-analytics'
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    implementation 'com.google.firebase:firebase-database'
    implementation 'com.google.firebase:firebase-storage'
    implementation 'com.google.firebase:firebase-messaging'
    implementation 'com.google.firebase:firebase-crashlytics'

    // Image loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'

    // Date/Time
    implementation 'com.jakewharton.threetenabp:threetenabp:1.4.6'

    // Dependency Injection
    implementation 'com.google.dagger:dagger:2.48'
    annotationProcessor 'com.google.dagger:dagger-compiler:2.48'

    // RxJava (Optional)
    implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'

    // Networking (if needed)
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

    // Utils
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'commons-validator:commons-validator:1.7'

    // Testing
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:5.7.0'
    testImplementation 'androidx.arch.core:core-testing:2.2.0'

    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.test.espresso:espresso-intents:3.5.1'
    androidTestImplementation 'androidx.test:runner:1.5.2'
    androidTestImplementation 'androidx.test:rules:1.5.0'
}
```

### 12. Application Class

```java
public class HealthTipsApplication extends Application {
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize ThreeTen
        AndroidThreeTen.init(this);

        // Initialize Dagger
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        // Create notification channels
        createNotificationChannels();

        // Set up crash reporting
        setupCrashReporting();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            // Health tips channel
            NotificationChannel healthTipsChannel = new NotificationChannel(
                    "health_tips",
                    getString(R.string.health_tips_channel),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            healthTipsChannel.setDescription(getString(R.string.health_tips_channel_description));
            notificationManager.createNotificationChannel(healthTipsChannel);

            // Reminders channel
            NotificationChannel remindersChannel = new NotificationChannel(
                    "reminders",
                    getString(R.string.reminders_channel),
                    NotificationManager.IMPORTANCE_HIGH
            );
            remindersChannel.setDescription(getString(R.string.reminders_channel_description));
            notificationManager.createNotificationChannel(remindersChannel);
        }
    }

    private void setupCrashReporting() {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCrashlyticsCollectionEnabled(true);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
```

### 13. Dependency Injection với Dagger 2

#### 13.1 App Module

```java
@Module
public class AppModule {
    private final Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return application;
    }

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Singleton
    public FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseFirestore provideFirebaseFirestore() {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseStorage provideFirebaseStorage() {
        return FirebaseStorage.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseDatabase provideFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }
}
```

#### 13.2 App Component

```java
@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(HealthTipsApplication application);
    void inject(MainActivity activity);
    void inject(LoginActivity activity);
    void inject(HomeFragment fragment);
    void inject(CategoryFragment fragment);
    void inject(FavoriteFragment fragment);
    void inject(ReminderFragment fragment);
    void inject(ProfileFragment fragment);

    // Providers
    Context context();
    SharedPreferences sharedPreferences();
    FirebaseAuth firebaseAuth();
    FirebaseFirestore firebaseFirestore();
    FirebaseStorage firebaseStorage();
    FirebaseDatabase firebaseDatabase();
}
```

### 14. Testing Strategy

#### 14.1 Unit Tests

```java
@RunWith(JUnit4.class)
public class HealthTipRepositoryTest {

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockCollection;

    @Mock
    private Task<QuerySnapshot> mockTask;

    private HealthTipRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        repository = new HealthTipRepositoryImpl(mockFirestore);
    }

    @Test
    public void testGetAllTips_Success() {
        // Arrange
        List<HealthTip> expectedTips = Arrays.asList(
            new HealthTip("1", "Tip 1", "Content 1"),
            new HealthTip("2", "Tip 2", "Content 2")
        );

        when(mockFirestore.collection("tips")).thenReturn(mockCollection);
        when(mockCollection.whereEqualTo("isActive", true)).thenReturn(mockCollection);
        when(mockCollection.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(mockCollection);
        when(mockCollection.get()).thenReturn(mockTask);

        // Act & Assert
        repository.getAllTips(new DataCallback<List<HealthTip>>() {
            @Override
            public void onSuccess(List<HealthTip> tips) {
                assertEquals(2, tips.size());
                assertEquals("Tip 1", tips.get(0).getTitle());
            }

            @Override
            public void onError(String error) {
                fail("Should not fail");
            }
        });
    }

    @Test
    public void testSearchTips_EmptyQuery() {
        // Test search with empty query
        repository.searchTips("", new DataCallback<List<HealthTip>>() {
            @Override
            public void onSuccess(List<HealthTip> tips) {
                assertTrue(tips.isEmpty());
            }

            @Override
            public void onError(String error) {
                fail("Should not fail");
            }
        });
    }
}
```

#### 14.2 Presenter Tests

```java
@RunWith(JUnit4.class)
public class HomePresenterTest {

    @Mock
    private HomeView mockView;

    @Mock
    private CategoryRepository mockCategoryRepository;

    @Mock
    private HealthTipRepository mockTipRepository;

    private HomePresenter presenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        presenter = new HomePresenter(mockCategoryRepository, mockTipRepository);
        presenter.attachView(mockView);
    }

    @After
    public void tearDown() {
        presenter.detachView();
    }

    @Test
    public void testLoadHomeData_Success() {
        // Arrange
        List<Category> categories = Arrays.asList(new Category("1", "Category 1"));
        List<HealthTip> tips = Arrays.asList(new HealthTip("1", "Tip 1", "Content 1"));

        // Act
        presenter.loadHomeData();

        // Verify loading is shown
        verify(mockView).showLoading();

        // Simulate successful category loading
        ArgumentCaptor<DataCallback<List<Category>>> categoryCallbackCaptor =
            ArgumentCaptor.forClass(DataCallback.class);
        verify(mockCategoryRepository).getAllCategories(categoryCallbackCaptor.capture());
        categoryCallbackCaptor.getValue().onSuccess(categories);

        // Simulate successful tips loading
        ArgumentCaptor<DataCallback<List<HealthTip>>> tipCallbackCaptor =
            ArgumentCaptor.forClass(DataCallback.class);
        verify(mockTipRepository).getPopularTips(eq(10), tipCallbackCaptor.capture());
        tipCallbackCaptor.getValue().onSuccess(tips);

        // Verify
        verify(mockView).showCategories(categories);
        verify(mockView).showPopularTips(tips);
        verify(mockView).hideLoading();
    }

    @Test
    public void testLoadHomeData_Error() {
        // Arrange
        String errorMessage = "Network error";

        // Act
        presenter.loadHomeData();

        // Simulate error in category loading
        ArgumentCaptor<DataCallback<List<Category>>> callbackCaptor =
            ArgumentCaptor.forClass(DataCallback.class);
        verify(mockCategoryRepository).getAllCategories(callbackCaptor.capture());
        callbackCaptor.getValue().onError(errorMessage);

        // Verify
        verify(mockView).showError(errorMessage);
    }
}
```

### 15. Implementation Priority

Phase 1 (MVP - 2 tuần):

1.  Cơ sở hạ tầng: Firebase setup, Database structure, Authentication
2.  Core UI: MainActivity, HomeFragment, CategoryFragment với MVP pattern
3.  Cơ bản: CRUD operations cho tips và categories
4.  Đa ngôn ngữ: Cài đặt cơ bản cho Vietnamese và English
5.  Search: Tìm kiếm cơ bản trong tips

Phase 2 (Tính năng chính - 3 tuần):

1.  Authentication: Login/Register system hoàn chỉnh
2.  Favorite System: Thêm/xóa yêu thích
3.  Reminder System: Tạo và quản lý nhắc nhở
4.  Notification: FCM integration và local notifications
5.  Admin Functions: Quản lý tips và categories
6.  Đa ngôn ngữ: Thêm Chinese, Japanese, Korean

Phase 3 (Tính năng nâng cao - 2 tuần):

1.  Chat Bot: Simple AI chat với keyword matching
2.  Advanced Search: Filter, sort, search history
3.  Statistics: User engagement analytics
4.  Offline Support: Cache dữ liệu quan trọng
5.  Performance: Optimize loading và memory usage

Phase 4 (Polish - 1 tuần):

1.  UI/UX: Hoàn thiện giao diện, animations
2.  Testing: Unit tests, integration tests
3.  Bug Fixes: Sửa lỗi và tối ưu hiệu suất
4.  Documentation: Hoàn thiện tài liệu và comments

### 16. Security & Best Practices

#### 16.1 Firebase Security Rules

```firestore-rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      // User's favorites
      match /favorites/{favoriteId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }

      // User's reminders
      match /reminders/{reminderId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }

    // Tips are readable by authenticated users
    match /tips/{tipId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true);
    }

    // Categories are readable by authenticated users
    match /categories/{categoryId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true);
    }
  }
}
```
*Lưu ý: Phần `Firestore Security Rules` ở trên chỉ mang tính chất minh họa cho cấu trúc Firestore ban đầu. Vì bạn ưu tiên Realtime Database, bạn sẽ cần định nghĩa `Realtime Database Rules` phù hợp thay thế cho Firestore Rules.*

#### 16.2 ProGuard Rules

```
# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# Model classes
-keep class com.healthtips.app.data.models.** { *; }

# Dagger
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.internal.Binding
-keep class * extends dagger.internal.ModuleAdapter
-keep class * extends dagger.internal.StaticInjection
```

### 17. Monitoring & Analytics

#### 17.1 Firebase Analytics Events

```java
public class AnalyticsHelper {
    private FirebaseAnalytics analytics;

    public AnalyticsHelper(Context context) {
        analytics = FirebaseAnalytics.getInstance(context);
    }

    public void logTipViewed(String tipId, String categoryId) {
        Bundle bundle = new Bundle();
        bundle.putString("tip_id", tipId);
        bundle.putString("category_id", categoryId);
        analytics.logEvent("tip_viewed", bundle);
    }

    public void logSearchPerformed(String query, int resultsCount) {
        Bundle bundle = new Bundle();
        bundle.putString("search_query", query);
        bundle.putInt("results_count", resultsCount);
        analytics.logEvent("search_performed", bundle);
    }

    public void logFavoriteAdded(String tipId) {
        Bundle bundle = new Bundle();
        bundle.putString("tip_id", tipId);
        analytics.logEvent("favorite_added", bundle);
    }

    public void logLanguageChanged(String fromLanguage, String toLanguage) {
        Bundle bundle = new Bundle();
        bundle.putString("from_language", fromLanguage);
        bundle.putString("to_language", toLanguage);
        analytics.logEvent("language_changed", bundle);
    }
}
```

***

# Instructions cho GitHub Copilot - Dự án HealthTips App (Android)

Dự án này là ứng dụng mẹo sức khỏe trên Android, được phát triển bằng Java và tuân thủ kiến trúc MVP (Model-View-Presenter).

## Hướng dẫn chung:
0.  **Ngôn ngữ phản hồi:**
    * **Luôn viết phản hồi / mô tả bằng tiếng Việt.**
1.  **Ngôn ngữ và Kiến trúc:**
    * Sử dụng **Java** làm ngôn ngữ lập trình chính.
    * Tuân thủ chặt chẽ kiến trúc **MVP (Model-View-Presenter)** cho tất cả các màn hình và luồng logic.

2.  **Cấu trúc thư mục:**
    * Tổ chức code theo các thư mục sau:
        * `data/`: Chứa các lớp liên quan đến truy cập và quản lý dữ liệu (models, repositories, data sources, etc.).
        * `presentation/`: Chứa các lớp UI (Activities, Fragments), các Presenters và View interfaces.
        * `services/`: Chứa các Service (ví dụ: background services, reminder services).
        * `di/`: Chứa các module và component của Dagger 2 cho Dependency Injection.
        * `receivers/`: Chứa các Broadcast Receivers.
        * `utils/`: Chứa các lớp tiện ích chung, helpers, constants.

3.  **Tích hợp Firebase:**
    * Firebase đã được tích hợp đầy đủ và sẽ được sử dụng cho các chức năng sau:
        * **Firebase Authentication:** Quản lý đăng nhập/đăng ký người dùng.
        * **Firebase Realtime Database:** **Đây là cơ sở dữ liệu chính được sử dụng để lưu trữ dữ liệu cấu trúc (ví dụ: thông tin mẹo sức khỏe, danh mục, hồ sơ người dùng) và quản lý dữ liệu thời gian thực.**
        * **Firebase Storage:** Lưu trữ các file (ví dụ: hình ảnh cho mẹo sức khỏe).
        * **Firebase Cloud Messaging (FCM):** Gửi thông báo đẩy cho người dùng.
        * **Cloud Firestore:** **Không phải là cơ sở dữ liệu chính cho dữ liệu cấu trúc hiện tại.** Có thể được sử dụng tùy chọn cho các tính năng đặc thù trong tương lai nếu cần.

4.  **Dependency Injection:**
    * Sử dụng **Dagger 2** cho toàn bộ hệ thống Dependency Injection.
    * Đảm bảo các module và component được cấu hình đúng đắn để cung cấp các dependencies cần thiết cho Presenters, Repositories, Services, v.v.

5.  **Giao diện người dùng (UI):**
    * Giao diện được xây dựng bằng **XML**.
    * Tuân thủ nghiêm ngặt **Material Design 3** để đảm bảo tính nhất quán và trải nghiệm người dùng hiện đại.

6.  **Quy ước đặt tên:**
    * Luôn tuân thủ quy ước đặt tên rõ ràng và nhất quán:
        * Adapter: `[Tên]Adapter.java` (ví dụ: `HealthTipAdapter.java`).
        * Presenter: `[Tên]Presenter.java` (ví dụ: `HomePresenter.java`, `LoginPresenter.java`).
        * Service: `[Tên]Service.java` (ví dụ: `ReminderService.java`, `NotificationService.java`).
        * Activity/Fragment: `[Tên]Activity.java`, `[Tên]Fragment.java`.
        * View Interface: `[Tên]Contract.java` (chứa `View` và `Presenter` interfaces lồng vào nhau) hoặc `[Tên]View.java`.
        * Model: `[Tên]Model.java` hoặc chỉ `[Tên].java` (ví dụ: `HealthTip.java`).
        * Repository: `[Tên]Repository.java`.

7.  **Kế thừa Presenter và View:**
    * Tất cả các Presenter phải kế thừa từ một lớp `BasePresenter` chung.
    * Tất cả các View interfaces (hoặc các lớp Activity/Fragment implement View interface) phải implement một interface `BaseView` chung.

8.  **Tuân thủ thiết kế:**
    * Luôn sinh code tuân thủ chặt chẽ **file thiết kế phân tích đã được cung cấp**, bao gồm cả việc chia module và chức năng đã định rõ.

9.  **Phong cách và Giao diện người dùng:**
    * **Ứng dụng sẽ hỗ trợ hai chế độ giao diện:** Chế độ tối (Dark Mode) và Chế độ sáng (Light Mode).
    * **Chế độ tối (Dark Mode - Lấy cảm hứng từ `cu_black.png`):**
        * **Gam màu chủ đạo:**
            * **Nền (Background):** Các tông màu đen xám đậm (ví dụ: `#1A1A1D` hoặc `#212124`) để tạo cảm giác hiện đại, sang trọng và dịu mắt khi sử dụng trong điều kiện thiếu sáng.
            * **Chữ (Text):** Trắng tinh khiết (`#FFFFFF`) hoặc trắng xám nhạt (`#F0F0F0`) để đảm bảo độ tương phản cao và dễ đọc trên nền tối.
        * **Màu sắc các nút (Buttons):**
            * **Nút chính (Primary Buttons):** Sử dụng gradient chuyển màu từ xanh lá cây đậm (ví dụ: `#4CAF50` hoặc `#2E7D32`) sang vàng chanh (`#C0CA33` hoặc xanh lá cây nhạt hơn `#8BC34A`). Chữ trên nút phải là màu trắng (`#FFFFFF`).
            * **Nút phụ/Hành động khác (Secondary Buttons):** Sử dụng gradient chuyển màu từ cam tươi (`#FF9800` hoặc `#F57C00`) sang đỏ cam (`#FF5722` hoặc đỏ gạch `#D32F2F`). Chữ trên nút nên là màu trắng (`#FFFFFF`).
        * **Các thành phần UI khác:**
            * **Đường viền/phân cách:** Nên là màu xám nhạt (`#424242`) hoặc xám đậm hơn một chút so với nền để tạo sự phân tách tinh tế.
            * **Biểu tượng (Icons):** Màu trắng (`#FFFFFF`) hoặc các màu tương đồng với gam màu của các nút để duy trì sự nhất quán.
            * **Điểm nhấn/Highlight:** Có thể sử dụng các màu xanh dương sáng từ logo `cu_black.png` (ví dụ: `#00BFFF` hoặc `#1E90FF`) cho các yếu tố tương tác nhỏ, đường viền hoặc trạng thái được chọn.
    * **Chế độ sáng (Light Mode - Lấy cảm hứng từ `cu_night.png`):**
        * **Gam màu chủ đạo:**
            * **Nền (Background):** Các tông màu xanh dương nhạt hoặc trắng sáng (ví dụ: `#E0F2F7` hoặc `#FFFFFF`), tạo cảm giác tươi mới và dễ chịu.
            * **Chữ (Text):** Xám đậm (ví dụ: `#212124` hoặc `#424242`) hoặc đen (`#000000`) để đảm bảo độ tương phản trên nền sáng.
        * **Màu sắc các nút (Buttons):**
            * **Nút chính (Primary Buttons):** Sử dụng các tông màu xanh dương (ví dụ: `#2196F3` hoặc `#1976D2`) hoặc xanh lá cây (ví dụ: `#4CAF50`) tương tự như logo `cu_night.png`. Chữ trên nút phải là màu trắng (`#FFFFFF`).
            * **Nút phụ/Hành động khác (Secondary Buttons):** Sử dụng màu sắc tương phản nhưng vẫn hài hòa, ví dụ: màu xám đậm (`#616161`) hoặc cam nhạt (`#FFB74D`). Chữ trên nút nên là màu trắng (`#FFFFFF`) hoặc đen (`#000000`).
        * **Các thành phần UI khác:**
            * **Đường viền/phân cách:** Nên là màu xám nhạt (`#BDBDBD`) hoặc xanh nhạt (`#81D4FA`).
            * **Biểu tượng (Icons):** Màu đen (`#000000`), xanh lam đậm (`#1976D2`), hoặc các màu xanh từ logo `cu_night.png` để phù hợp với nền sáng.
            * **Điểm nhấn/Highlight:** Các màu cam/vàng từ bóng đèn trên logo `cu_night.png` (ví dụ: `#FFC107` hoặc `#FFEB3B`) có thể được sử dụng để làm nổi bật các yếu tố quan trọng hoặc trạng thái.
    * **Hiệu ứng chung:** Ưu tiên sử dụng gradient cho các nút để tạo chiều sâu và điểm nhấn trong cả hai chế độ. Đảm bảo tính nhất quán về hình dạng, khoảng cách và kiểu chữ giữa hai chế độ để trải nghiệm người dùng không bị gián đoạn.

10. **Tránh trùng lặp File và Chức năng:**
    * **Trước khi đề xuất hoặc tạo bất kỳ file mới nào (Activity, Fragment, Presenter, Model, Service, v.v.) hoặc triển khai một chức năng mới, Copilot phải kiểm tra kỹ lưỡng toàn bộ cấu trúc dự án hiện có.**
    * **Nếu phát hiện đã tồn tại một file hoặc một phần code thực hiện chức năng tương tự hoặc cùng tên trong dự án (dựa trên tên file, quy ước đặt tên, hoặc logic đã có), Copilot phải thông báo và KHÔNG tạo ra bản sao.**
    * **Thay vào đó, Copilot sẽ:**
        * **Đề xuất tích hợp hoặc mở rộng chức năng hiện có** vào file hoặc module đã tồn tại.
        * **Chỉ tạo file mới khi không có bất kỳ file hoặc chức năng tương tự nào tồn tại** và nó thực sự cần thiết cho một module hoặc tính năng mới hoàn toàn.
        * **Khi tiếp tục một chức năng đã được bắt đầu ở bước trước, Copilot phải tiếp tục làm việc trên các file đã được tạo ra cho chức năng đó, không tạo lại chúng ở thư mục khác.**
    * **Luôn ưu tiên việc tái sử dụng và mở rộng code hiện có.**

11. **Quản lý tài nguyên:**
    * **Tập trung tài nguyên vào các file chung:**
        * **Strings:** Tất cả chuỗi văn bản phải được đặt trong file `strings.xml` chung, không tạo file strings riêng cho từng tính năng.
        * **Colors:** Tất cả định nghĩa màu sắc phải được đặt trong file `colors.xml` chung, không tạo file colors riêng biệt.
        * **Styles/Themes:** Tất cả styles và themes phải được đặt trong file `styles.xml` hoặc `themes.xml` chung, không tạo file styles riêng.
        * **Dimensions:** Tất cả kích thước phải được đặt trong file `dimens.xml` chung.
    * **Đặt tên tài nguyên:**
        * Đặt tên theo cấu trúc `[feature]_[type]_[description]` (ví dụ: `category_title`, `home_description`, `auth_button_text`)
        * Đảm bảo tên mô tả đúng mục đích sử dụng và dễ hiểu
    * **Tránh trùng lặp tài nguyên:** Kiểm tra kỹ trước khi thêm tài nguyên mới, tái sử dụng tài nguyên hiện có nếu phù hợp.
    * **Tài nguyên hình ảnh:**
        * Vector Drawables (XML) được ưu tiên hơn bitmap cho biểu tượng và đồ họa đơn giản
        * Bitmap (PNG, JPEG) chỉ sử dụng cho hình ảnh phức tạp không thể biểu diễn bằng vector

12. **Chi tiết Phân tích Dự án:**
    * **Để có cái nhìn toàn diện và chi tiết về cấu trúc dự án, các thành phần công nghệ, mô hình dữ liệu, quy tắc bảo mật, và lộ trình phát triển, Copilot HÃY THAM KHẢO file `Project_Analysis_Details.md` được cung cấp trong cùng thư mục dự án.**
    * **File này chứa thông tin chi tiết về:**
        * Tổng quan dự án (SDK, Architecture, Database, Authentication, v.v.).
        * Cấu trúc thư mục chi tiết.
        * Định nghĩa các Data Models (User, HealthTip, Category, Reminder, ChatMessage, Enums).
        * Cấu hình Firebase (FirebaseManager, Realtime Database Structure).
        * Chi tiết triển khai kiến trúc MVP (Base Classes, Repository Pattern).
        * Triển khai đa ngôn ngữ (LocaleHelper, cấu trúc resource, ví dụ strings.xml).
        * Các Activity và Fragment chính.
        * Hệ thống thông báo (FCMService, ReminderService).
        * Hệ thống Chat Bot.
        * Permissions và cấu hình AndroidManifest.xml.
        * Cấu hình Build (build.gradle).
        * Class Application.
        * Cấu hình Dependency Injection với Dagger 2.
        * Chiến lược kiểm thử (Unit Tests, Presenter Tests).
        * Ưu tiên triển khai tính năng theo từng Phase.
        * Các quy tắc bảo mật Firebase (Realtime Database Rules) và ProGuard.
        * Monitoring & Analytics (Firebase Analytics Events).

13. **Hướng dẫn theo ngữ cảnh Chức năng:**
    * **Copilot PHẢI chủ động đọc và phân tích file `Project_Analysis_Details.md` dựa trên chức năng hoặc nhiệm vụ mà người dùng đang thực hiện.**
    * **Khi người dùng yêu cầu thực hiện một tác vụ hoặc đang làm việc trong một file cụ thể, Copilot cần:**
        * **Xác định chức năng liên quan:** Ví dụ, nếu người dùng đang chỉnh sửa `HomeFragment.java` hoặc yêu cầu "tải dữ liệu trang chủ", Copilot phải hiểu rằng đây là một phần của "Home Fragment với MVP" và "Phase 1 (MVP)".
        * **Truy xuất thông tin liên quan:** Tìm kiếm các phần trong `Project_Analysis_Details.md` mô tả chi tiết về chức năng đó, bao gồm:
            * **Mô hình dữ liệu (Models):** Các Model liên quan (ví dụ: `HealthTip`, `Category`).
            * **Giao diện (Views):** Giao diện View tương ứng (ví dụ: `HomeView`).
            * **Presenter:** Presenter liên quan (ví dụ: `HomePresenter`).
            * **Repository:** Các Repository được sử dụng (ví dụ: `HealthTipRepository`, `CategoryRepository`).
            * **Cấu trúc Firebase:** Các Collection hoặc cấu trúc dữ liệu Firebase liên quan.
            * **Quy ước đặt tên:** Tên file và thư mục chính xác theo quy ước.
            * **Ưu tiên triển khai (Implementation Priority):** Xác định giai đoạn hiện tại của chức năng để gợi ý các bước tiếp theo phù hợp với lộ trình dự án.
        * **Đề xuất và Hỗ trợ thông minh:** Dựa trên thông tin đã truy xuất, Copilot sẽ đưa ra các gợi ý code, giải thích, hoặc các bước tiếp theo một cách chính xác và phù hợp với thiết kế tổng thể của dự án.
        * **Nếu không rõ ngữ cảnh:** Nếu Copilot không thể xác định rõ chức năng hiện tại hoặc các thông tin liên quan trong `Project_Analysis_Details.md`, nó sẽ hỏi người dùng để làm rõ.

---

## Những điều cần lưu ý khi sinh code:

* **Tính mô đun:** Tập trung vào việc tạo ra các thành phần nhỏ, có trách nhiệm duy nhất.
* **Tính khả dụng lại:** Viết code có thể tái sử dụng.
* **Xử lý lỗi:** Bao gồm các cơ chế xử lý lỗi phù hợp (ví dụ: try-catch, kiểm tra null).
* **Phản hồi UI:** Đảm bảo có phản hồi phù hợp cho người dùng khi thực hiện các thao tác (ví dụ: loading states, thông báo lỗi/thành công).
* **Bảo mật:** Lưu ý các vấn đề bảo mật cơ bản khi làm việc với dữ liệu người dùng và Firebase.
* **Hiệu suất:** Cân nhắc hiệu suất, đặc biệt là khi làm việc với dữ liệu lớn hoặc các thao tác UI phức tạp.

---

**Lưu ý quan trọng cho việc phát triển (Tham khảo thêm từ Project_Analysis_Details.md):**

* Luôn sử dụng Java best practices và tuân thủ coding conventions.
* Implement proper error handling cho tất cả Firebase operations.
* Test trên nhiều thiết bị và API levels khác nhau.
* Optimize cho performance - đặc biệt là loading times và memory usage.
* Follow Material Design guidelines cho UI/UX consistency.
* Implement proper lifecycle management cho Activities và Fragments.
* Use proper logging với different levels (DEBUG, INFO, WARNING, ERROR).
* Secure sensitive data và không hardcode API keys trong source code.
* Implement offline capabilities cho user experience tốt hơn.
* Regular backup Firebase data và có disaster recovery plan.

**Đối với đa ngôn ngữ (Tham khảo thêm từ Project_Analysis_Details.md):**

* Luôn sử dụng string resources thay vì hardcode text.
* Test UI layout với các ngôn ngữ có text dài (German) và ngắn (Chinese).
* Implement proper RTL support cho các ngôn ngữ như Arabic (nếu cần).
* Use appropriate fonts cho các ngôn ngữ khác nhau.