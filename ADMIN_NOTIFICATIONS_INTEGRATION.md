# Admin Notifications Integration - Mobile App

## ✅ Đã hoàn thành

### 1. **AdminNotificationSender Utility Class**
**File:** `app/src/main/java/com/vhn/doan/utils/AdminNotificationSender.java`

**Chức năng:**
- Gửi notifications từ mobile app đến web admin
- Sử dụng OkHttp để call REST API
- Async callbacks để không block UI thread
- Tự động lấy thông tin user từ FirebaseAuth
- Tự động lấy device info (manufacturer, model, OS version)

**Methods:**
```java
// Gửi user report (spam, inappropriate, abuse, bug, other)
public void sendUserReport(
    String reportType,
    String reason,
    String description,
    String contentId,
    String contentType,
    NotificationCallback callback
)

// Report content (post/video)
public void reportContent(
    String contentId,
    String contentType,
    String reportType,
    String reason,
    String description,
    NotificationCallback callback
)

// Report bug
public void reportBug(
    String description,
    String steps,
    NotificationCallback callback
)

// Send feedback
public void sendFeedback(
    String feedbackText,
    int rating,
    NotificationCallback callback
)
```

### 2. **ReportIssueActivity Integration**
**File:** `app/src/main/java/com/vhn/doan/presentation/settings/support/ReportIssueActivity.java`

**Changes:**
- ✅ Imported `AdminNotificationSender`
- ✅ Initialized `AdminNotificationSender` in `onCreate()`
- ✅ Added `sendToAdminPanel()` method
- ✅ Added `mapIssueTypeToReportType()` helper
- ✅ Modified `submitReport()` to send to both Firebase AND web admin

**Flow:**
1. User fills report form
2. Submit to Firebase Database (existing - backup)
3. Also send to Web Admin API (new)
4. Admin sees notification real-time on web

### 3. **API Endpoint**
**URL:** `https://healthtips-admin-4nqwzfhay-vunams-projects-d3582d4f.vercel.app/api/admin-notifications/user-report`

**Request Body:**
```json
{
  "userId": "firebase_user_id",
  "userName": "User Name",
  "reportType": "spam|inappropriate|abuse|bug|other",
  "reason": "Short reason",
  "description": "Detailed description",
  "contentId": "post_id or video_id (optional)",
  "contentType": "post|video (optional)",
  "additionalData": {
    "device": "Samsung Galaxy S21",
    "osVersion": "Android 13",
    "apiLevel": 33
  }
}
```

**Response:**
```json
{
  "success": true,
  "notificationId": "firebase_notification_id",
  "message": "Báo cáo đã được gửi đến admin"
}
```

## 🎯 Cách hoạt động

### Current Flow (ReportIssueActivity)
```
User fills form
    ↓
Tap "Submit" button
    ↓
Save to Firebase /issues (existing)
    ↓
Send to Web Admin API (new)
    ↓
Admin sees notification on web dashboard
    ↓
Show success message to user
```

### Report Types Mapping
| Issue Type (App)     | Report Type (API) | Priority |
|---------------------|-------------------|----------|
| Spam                | spam              | medium   |
| Inappropriate       | inappropriate     | high     |
| Harassment/Violence | abuse             | high     |
| Misleading          | content           | medium   |
| Other               | other             | low      |

## 🔧 Cách test

### Test trên Emulator/Device:

1. **Mở app** → Login
2. **Vào Settings** → Support → Report Issue
3. **Fill form:**
   - Issue Type: Chọn "Spam"
   - Subject: "Test report from app"
   - Description: "Testing admin notifications"
4. **Tap Submit**
5. **Check Web Admin:**
   - URL: https://healthtips-admin-4nqwzfhay-vunams-projects-d3582d4f.vercel.app
   - Login → Click bell icon (top right)
   - Sẽ thấy notification mới với title: "Báo cáo từ [User Name]"

## 📋 TODO - Tính năng cần thêm

### 1. Report Button trên Health Tip Detail
**File cần sửa:** `HealthTipDetailActivity.java`

**Thêm:**
```java
// Add menu option for report
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_health_tip_detail, menu);
    return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_report) {
        showReportDialog();
        return true;
    }
    return super.onOptionsItemSelected(item);
}

private void showReportDialog() {
    String[] reportOptions = {
        "Spam",
        "Nội dung không phù hợp",
        "Thông tin sai lệch",
        "Khác"
    };

    new AlertDialog.Builder(this)
        .setTitle("Báo cáo bài viết")
        .setItems(reportOptions, (dialog, which) -> {
            String reportType = which == 0 ? "spam" :
                              which == 1 ? "inappropriate" :
                              which == 2 ? "content" : "other";
            String reason = reportOptions[which];

            AdminNotificationSender sender = new AdminNotificationSender(this);
            sender.reportContent(
                healthTipId,
                "post",
                reportType,
                reason,
                "",
                new AdminNotificationSender.NotificationCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() ->
                            Toast.makeText(HealthTipDetailActivity.this,
                                "Đã gửi báo cáo", Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() ->
                            Toast.makeText(HealthTipDetailActivity.this,
                                "Không thể gửi báo cáo", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            );
        })
        .setNegativeButton("Hủy", null)
        .show();
}
```

### 2. Report Button trên Video Player
**File cần sửa:** `VideoActivity.java` hoặc `SingleVideoPlayerActivity.java`

Tương tự như trên, nhưng với:
- `contentType = "video"`
- `contentId = videoId`

### 3. Feedback Screen
**File mới:** `FeedbackActivity.java`

```java
public class FeedbackActivity extends AppCompatActivity {
    private EditText etFeedback;
    private RatingBar ratingBar;
    private Button btnSubmit;

    private void submitFeedback() {
        String feedback = etFeedback.getText().toString();
        int rating = (int) ratingBar.getRating();

        AdminNotificationSender sender = new AdminNotificationSender(this);
        sender.sendFeedback(feedback, rating, new NotificationCallback() {
            // Handle success/failure
        });
    }
}
```

## 🔐 Security Notes

- ✅ User authentication required (FirebaseAuth)
- ✅ Firebase ID Token sent in Authorization header (Bearer token)
- ✅ HTTPS only
- ✅ Server-side validation on API
- ✅ Rate limiting on web admin (if needed)

### Authentication Flow
```
1. User logs in with FirebaseAuth
2. App gets Firebase ID token: currentUser.getIdToken(true)
3. Token added to request header: "Authorization: Bearer {idToken}"
4. Server validates token with Firebase Admin SDK
5. Request processed if token is valid
```

## 📱 Dependencies

All required dependencies already included:
- ✅ OkHttp 4.12.0
- ✅ Firebase Auth
- ✅ Gson 2.10.1

No additional dependencies needed!

## 🎉 Kết quả

**User experience:**
1. User reports issue → Sees success message
2. Report saved to Firebase (backup)
3. Report sent to web admin (notification)

**Admin experience:**
1. Gets real-time notification on web
2. Sees badge count on bell icon
3. Can view details, mark as read, resolve
4. Can navigate to reported content directly

**Integration complete!** ✨

---

## 📸 Screenshots Preview

**App Side:**
```
Settings → Support → Report Issue
[Issue Type Dropdown ▼]
[Subject Input      ]
[Description Input  ]
[Device Info Display]
[Submit Button      ]
```

**Web Admin Side:**
```
Header: [🔔 7] ← Badge shows unread count
Click bell → Admin Notifications page
Shows: "Báo cáo từ Nguyễn Văn A"
Type: USER_REPORT (red)
Priority: HIGH
```

---

## 🐛 Fixed Issues

### 401 Unauthorized Error (Fixed: 2025-11-28)
**Problem:** API requests were failing with "Admin notification failed: 401"

**Root Cause:** Requests were not including Firebase authentication token

**Solution:**
- Added `currentUser.getIdToken(true)` to get Firebase ID token
- Added token to request headers: `Authorization: Bearer {idToken}`
- Server now validates token before processing request

**Changes Made:**
```java
// Before - No authentication
Request request = new Request.Builder()
    .url(ADMIN_API_BASE_URL + "/admin-notifications/user-report")
    .post(body)
    .build();

// After - With Firebase ID token
currentUser.getIdToken(true).addOnCompleteListener(task -> {
    String idToken = task.getResult().getToken();
    Request request = new Request.Builder()
        .url(ADMIN_API_BASE_URL + "/admin-notifications/user-report")
        .addHeader("Authorization", "Bearer " + idToken)
        .addHeader("Content-Type", "application/json")
        .post(body)
        .build();
});
```

---

**Created:** 2025-11-28
**Updated:** 2025-11-28 (Fixed 401 authentication issue)
**Author:** Claude Code Integration
**Status:** ✅ COMPLETED & TESTED
