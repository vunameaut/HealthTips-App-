# ✅ Integration Checklist - Reminder Fix

## 📋 Checklist tích hợp Permission Checker vào UI

Để hoàn thiện fix notification, cần integrate `ReminderPermissionChecker` vào UI tại các điểm sau:

---

## 1. ✅ ReminderDialog.java (Khi tạo reminder mới)

**File:** `app/src/main/java/com/vhn/doan/presentation/reminder/ReminderDialog.java`

**Thêm vào method save/create reminder:**

```java
import com.vhn.doan.utils.ReminderPermissionChecker;

private void saveReminder() {
    // Trước khi save reminder, check permissions
    if (!ReminderPermissionChecker.checkAllPermissions(getActivity())) {
        // Show dialog explain và request permissions
        new AlertDialog.Builder(getActivity())
            .setTitle("Permissions cần thiết")
            .setMessage(ReminderPermissionChecker.getPermissionExplanationMessage(getActivity()))
            .setPositiveButton("Cài đặt", (dialog, which) -> {
                ReminderPermissionChecker.requestAllNecessaryPermissions(getActivity());
            })
            .setNegativeButton("Bỏ qua", (dialog, which) -> {
                // Vẫn cho phép tạo reminder nhưng cảnh báo
                Toast.makeText(getActivity(),
                    "Thông báo có thể không hoạt động đúng giờ",
                    Toast.LENGTH_LONG).show();
                proceedToSaveReminder();
            })
            .show();
        return;
    }

    // Nếu có đủ permissions, proceed normally
    proceedToSaveReminder();
}

private void proceedToSaveReminder() {
    // Existing save logic here...
    Reminder reminder = new Reminder(...);

    // Schedule với ReminderScheduler
    ReminderScheduler scheduler = ReminderScheduler.getInstance(getContext());
    scheduler.scheduleReminder(reminder);
}
```

---

## 2. ✅ NotificationSettingsActivity.java

**File:** `app/src/main/java/com/vhn/doan/presentation/settings/content/NotificationSettingsActivity.java`

**Thêm section mới:**

```java
import com.vhn.doan.utils.ReminderPermissionChecker;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Thêm section Permission Status
    setupPermissionStatusSection();
}

private void setupPermissionStatusSection() {
    // Battery Optimization Status
    TextView tvBatteryStatus = findViewById(R.id.tv_battery_optimization_status);
    Button btnBatterySettings = findViewById(R.id.btn_battery_settings);

    boolean batteryOptimized = !ReminderPermissionChecker.isBatteryOptimizationDisabled(this);
    tvBatteryStatus.setText(batteryOptimized ?
        "❌ Đang bị tối ưu hóa pin" : "✅ Đã tắt tối ưu hóa pin");

    btnBatterySettings.setOnClickListener(v -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ReminderPermissionChecker.requestBatteryOptimizationExemption(this);
        }
    });

    // Exact Alarm Permission Status (Android 12+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        TextView tvAlarmStatus = findViewById(R.id.tv_exact_alarm_status);
        Button btnAlarmSettings = findViewById(R.id.btn_alarm_settings);

        boolean canSchedule = ReminderPermissionChecker.canScheduleExactAlarms(this);
        tvAlarmStatus.setText(canSchedule ?
            "✅ Có quyền đặt báo thức" : "❌ Chưa có quyền đặt báo thức");

        btnAlarmSettings.setOnClickListener(v -> {
            ReminderPermissionChecker.requestExactAlarmPermission(this);
        });
    }

    // Check All Button
    Button btnCheckAll = findViewById(R.id.btn_check_all_permissions);
    btnCheckAll.setOnClickListener(v -> {
        if (ReminderPermissionChecker.checkAllPermissions(this)) {
            new AlertDialog.Builder(this)
                .setTitle("✅ Hoàn tất")
                .setMessage("Tất cả permissions đã được cấp. Thông báo sẽ hoạt động đúng giờ.")
                .setPositiveButton("OK", null)
                .show();
        } else {
            ReminderPermissionChecker.requestAllNecessaryPermissions(this);
        }
    });
}

@Override
protected void onResume() {
    super.onResume();
    // Refresh status khi user quay lại từ settings
    setupPermissionStatusSection();
}
```

**Layout XML cần thêm:**

```xml
<!-- res/layout/activity_notification_settings.xml -->

<!-- Battery Optimization Section -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@drawable/card_background">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Tối ưu hóa pin"
        android:textSize="16sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/tv_battery_optimization_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Đang kiểm tra..." />

    <Button
        android:id="@+id/btn_battery_settings"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Cài đặt" />
</LinearLayout>

<!-- Exact Alarm Section (Android 12+) -->
<LinearLayout
    android:id="@+id/exact_alarm_section"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp"
    android:layout_marginTop="8dp"
    android:background="@drawable/card_background">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Quyền đặt báo thức chính xác"
        android:textSize="16sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/tv_exact_alarm_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Đang kiểm tra..." />

    <Button
        android:id="@+id/btn_alarm_settings"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Cài đặt" />
</LinearLayout>

<!-- Check All Button -->
<Button
    android:id="@+id/btn_check_all_permissions"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    android:text="Kiểm tra tất cả permissions"
    android:backgroundTint="@color/primary" />
```

---

## 3. ✅ HomeActivity.java (First Launch)

**File:** `app/src/main/java/com/vhn/doan/presentation/home/HomeActivity.java`

**Thêm vào onCreate hoặc onResume:**

```java
import com.vhn.doan.utils.ReminderPermissionChecker;
import android.content.SharedPreferences;

private static final String PREF_PERMISSIONS_REQUESTED = "permissions_requested";

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Check nếu chưa bao giờ request permissions
    SharedPreferences prefs = getPreferences(MODE_PRIVATE);
    boolean hasRequestedBefore = prefs.getBoolean(PREF_PERMISSIONS_REQUESTED, false);

    if (!hasRequestedBefore && !ReminderPermissionChecker.checkAllPermissions(this)) {
        // First time - show onboarding dialog
        showPermissionOnboardingDialog();
        prefs.edit().putBoolean(PREF_PERMISSIONS_REQUESTED, true).apply();
    }
}

private void showPermissionOnboardingDialog() {
    new AlertDialog.Builder(this)
        .setTitle("🔔 Cài đặt thông báo")
        .setMessage("Để nhận thông báo nhắc nhở sức khỏe đúng giờ, vui lòng cấp các quyền sau:\n\n" +
            "• Quyền đặt báo thức chính xác\n" +
            "• Tắt tối ưu hóa pin\n\n" +
            "Bạn có thể thay đổi trong Settings bất kỳ lúc nào.")
        .setPositiveButton("Cài đặt ngay", (dialog, which) -> {
            ReminderPermissionChecker.requestAllNecessaryPermissions(this);
        })
        .setNegativeButton("Để sau", null)
        .show();
}
```

---

## 4. ✅ ReminderFragment.java (Reminder List)

**File:** `app/src/main/java/com/vhn/doan/presentation/reminder/ReminderFragment.java`

**Thêm warning banner nếu thiếu permissions:**

```java
import com.vhn.doan.utils.ReminderPermissionChecker;

@Override
public void onResume() {
    super.onResume();
    checkAndShowPermissionWarning();
}

private void checkAndShowPermissionWarning() {
    View warningBanner = getView().findViewById(R.id.permission_warning_banner);

    if (!ReminderPermissionChecker.checkAllPermissions(getActivity())) {
        warningBanner.setVisibility(View.VISIBLE);
        warningBanner.setOnClickListener(v -> {
            ReminderPermissionChecker.requestAllNecessaryPermissions(getActivity());
        });
    } else {
        warningBanner.setVisibility(View.GONE);
    }
}
```

**Layout warning banner:**

```xml
<!-- res/layout/fragment_reminder.xml -->
<LinearLayout
    android:id="@+id/permission_warning_banner"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="12dp"
    android:background="#FFFFCC00"
    android:visibility="gone">

    <TextView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="⚠️ Cần cấp quyền để nhận thông báo đúng giờ"
        android:textColor="#000000" />

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Cài đặt"
        android:textColor="#000000"
        style="?attr/borderlessButtonStyle" />
</LinearLayout>
```

---

## 5. ✅ Build và Test

### 5.1. Build project
```bash
./gradlew clean
./gradlew assembleDebug
```

### 5.2. Install và test
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 5.3. Test scenarios

**Test 1: First launch**
- [ ] Mở app lần đầu
- [ ] Nên thấy onboarding dialog về permissions
- [ ] Click "Cài đặt ngay"
- [ ] Verify dialogs xuất hiện theo sequence

**Test 2: Create reminder**
- [ ] Tạo reminder mới
- [ ] Nếu thiếu permission, nên thấy dialog
- [ ] Grant permissions
- [ ] Verify reminder được tạo thành công

**Test 3: Settings screen**
- [ ] Vào Notification Settings
- [ ] Verify status hiển thị đúng
- [ ] Click buttons để request permissions
- [ ] Verify redirect đến settings

**Test 4: Background notification**
- [ ] Tạo reminder 2 phút sau
- [ ] Grant all permissions
- [ ] Thoát app
- [ ] Verify notification xuất hiện đúng giờ

**Test 5: Warning banner**
- [ ] Revoke battery optimization exemption
- [ ] Vào Reminder tab
- [ ] Verify warning banner xuất hiện
- [ ] Click banner
- [ ] Verify redirect đến settings

---

## 🔍 Verification

Sau khi integrate, verify bằng logs:

```bash
# Check permission status
adb logcat | grep "ReminderPermissionChecker"

# Check scheduler
adb logcat | grep "ReminderScheduler"

# Check AlarmManager
adb logcat | grep "AlarmManager"

# Kiểm tra service đang chạy
adb shell dumpsys activity services | grep ReminderForegroundService
```

---

## 📱 Testing với ADB

```bash
# Force Doze mode
adb shell dumpsys deviceidle force-idle

# Check reminder có trigger không
adb logcat | grep "ReminderWorker\|ReminderBroadcastReceiver"

# Exit Doze mode
adb shell dumpsys deviceidle unforce

# Simulate reboot (test BootReceiver)
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
```

---

## ✅ Final Checklist

- [ ] ReminderPermissionChecker được gọi trong ReminderDialog
- [ ] NotificationSettingsActivity có section permission status
- [ ] HomeActivity show onboarding dialog (first launch)
- [ ] ReminderFragment có warning banner
- [ ] Build thành công không có errors
- [ ] Test background notifications hoạt động
- [ ] Test Doze mode
- [ ] Test sau reboot
- [ ] Logs không có errors
- [ ] UI responsive và clear

---

## 🎯 Expected Results

Sau khi hoàn tất integration:

✅ **User Experience:**
- User được hướng dẫn rõ ràng về permissions cần thiết
- Dialog giải thích tại sao cần permissions
- Easy access đến settings để cấp permissions
- Warning visible nếu thiếu permissions

✅ **Technical:**
- Notifications hoạt động đúng giờ khi app ở background
- Không bị defer trong Doze mode
- Auto-reschedule sau reboot
- Reliable delivery

✅ **Monitoring:**
- Logs rõ ràng để debug
- Status visible trong UI
- Easy troubleshooting
