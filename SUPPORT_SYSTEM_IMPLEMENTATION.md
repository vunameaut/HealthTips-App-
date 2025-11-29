# Support Ticket System - Implementation Summary

## ✅ COMPLETED (Android App)

### 1. Models & Data Structure
- ✅ `SupportMessage.java` - Model for chat messages
- ✅ `SupportTicket.java` - Updated with `imageUrl` field
- ✅ `UserNotification.java` - Model for user notifications

### 2. Cloudinary Integration
- ✅ `CloudinaryHelper.java` - Added `uploadSupportImage()` method
- ✅ Images upload to `/support` folder

### 3. Report Issue with Image Attachment
- ✅ `ReportIssueActivity.java` - Full image attachment implementation
  - Image picker integration
  - Preview & remove functionality
  - Auto upload to Cloudinary before submit
  - imageUrl saved to Firebase `/issues/{issueId}`

- ✅ `activity_report_issue.xml` - UI with image attachment
  - `btnAttachImage`, `imagePreview`, `btnRemoveImage`

### 4. Support Tickets List Improvements
- ✅ `MySupportTicketsActivity.java` - Displays user's tickets
- ✅ `SupportTicketAdapter.java` - Shows tickets with:
  - Ticket ID, status, type
  - Subject & description preview
  - Image thumbnail (if available)
  - Admin response indicator
  - Created date

- ✅ `item_support_ticket.xml` - Improved ticket card with image thumbnail

### 5. Layouts & Resources
- ✅ `item_message_user.xml` - User message bubble (right-aligned, blue)
- ✅ `item_message_admin.xml` - Admin message bubble (left-aligned, gray)
- ✅ `bg_message_user.xml` - Blue rounded background
- ✅ `bg_message_admin.xml` - Gray bordered background
- ✅ All string resources for image attachment and chat

### 6. Existing Layouts
- ✅ `activity_ticket_detail.xml` - Already exists, shows ticket details

---

## 🔄 TO BE COMPLETED (Android App)

### Priority 1: Chat Adapter & Messages Display

**File to create: `MessageAdapter.java`**
Location: `app/src/main/java/com/vhn/doan/presentation/settings/support/adapter/`

Key features needed:
- ViewType for user vs admin messages
- Bind text, image, timestamp
- Load images with Glide
- Handle clicks on message images

### Priority 2: Ticket Detail with Chat

**File to create/update: `TicketDetailActivity.java`**
Location: `app/src/main/java/com/vhn/doan/presentation/settings/support/`

Key features needed:
- Load ticket details from Firebase `/issues/{ticketId}`
- Load messages from `/issues/{ticketId}/messages`
- Send new messages (text + optional image)
- Real-time listener for new messages
- Auto-scroll to bottom
- Image attachment in chat

**Update layout: `activity_ticket_detail.xml`**
- Replace NestedScrollView with RecyclerView for messages
- Add message input at bottom
- Add send button & image attach button

### Priority 3: Connect Ticket List to Detail

**Update: `SupportTicketAdapter.java`**
- Change `showTicketDetails()` to open `TicketDetailActivity`
- Pass ticket ID via Intent

**Update: `AndroidManifest.xml`**
- Register `TicketDetailActivity`

---

## 🔄 TO BE COMPLETED (Web Admin)

### Priority 1: Support Management Page

**File to create: `src/pages/support-management.tsx`**

Key features needed:
- Table view of all tickets from `/issues`
- Real-time Firebase sync
- Columns: ID, User, Subject, Status, Type, Date
- Filter by status (pending/in_progress/resolved)
- Search by keyword
- Click to view details

### Priority 2: Ticket Detail Modal/Page

**Component to create: `TicketDetailModal.tsx`**

Features needed:
- Show ticket details (subject, description, image, device info)
- Display all messages
- Send admin response
- Update ticket status
- Upload images in responses

### Priority 3: Firebase Integration

**Update: `src/lib/firebase-admin.ts`**
- Add methods to read `/issues`
- Add methods to send messages to `/issues/{id}/messages`
- Add methods to update ticket status

---

## 📊 FIREBASE DATA STRUCTURE

```javascript
/issues
  /{issueId}
    userId: "user123"
    userEmail: "user@example.com"
    issueType: "Báo cáo lỗi"
    subject: "App bị crash"
    description: "Mô tả chi tiết..."
    imageUrl: "https://cloudinary.../support/..."  // NEW
    status: "pending" | "in_progress" | "resolved"
    timestamp: 1234567890
    deviceManufacturer: "Samsung"
    deviceModel: "Galaxy S21"
    androidVersion: "12"

    /messages  // NEW
      /{messageId}
        text: "Tin nhắn..."
        imageUrl: "https://..." (optional)
        senderId: "user123" or "admin"
        senderType: "user" | "admin"
        senderName: "Tên người gửi"
        timestamp: 1234567890

/user_notifications
  /{userId}
    /{notificationId}
      title: "Admin đã phản hồi"
      message: "Ticket #123..."
      createdAt: 1234567890
      read: false
      issueId: "issue123"
```

---

## 🎯 NEXT STEPS

### For Testing Current Work:
1. Build APK with current changes
2. Test image attachment in Report Issue
3. Verify tickets list shows images
4. Check Firebase data structure

### For Completing Chat System:
1. Create `MessageAdapter.java`
2. Create/update `TicketDetailActivity.java`
3. Update `activity_ticket_detail.xml` for chat
4. Add activity to AndroidManifest
5. Test chat flow

### For Web Admin:
1. Create support-management page
2. Add Firebase integration
3. Create ticket detail component
4. Test admin responses

---

## 🐛 POTENTIAL ISSUES TO FIX

1. **Missing drawable:** `ic_send.xml` - Need to create send icon
2. **String resources:** May need to add more strings during implementation
3. **Permissions:** Ensure READ_EXTERNAL_STORAGE for image picker
4. **Firebase rules:** May need to update security rules for messages
5. **Error handling:** Add proper error handling for image upload failures

---

## 📝 CODE SNIPPETS NEEDED

See separate files:
- `MessageAdapter.java` - Chat messages adapter
- `TicketDetailActivity.java` - Ticket detail with chat
- `support-management.tsx` - Web admin page

Total estimated files to create/modify: ~8 files
Total estimated lines of code: ~1500-2000 lines
