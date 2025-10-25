package com.vhn.doan.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

/**
 * Helper class để debug Firebase
 */
public class FirebaseDebugHelper {

    private static final String TAG = "FirebaseDebugHelper";

    /**
     * Log thông tin user hiện tại
     */
    public static void logCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "=== CURRENT USER INFO ===");
            Log.d(TAG, "User ID: " + user.getUid());
            Log.d(TAG, "Email: " + user.getEmail());
            Log.d(TAG, "Display Name: " + user.getDisplayName());
            Log.d(TAG, "========================");
        } else {
            Log.w(TAG, "No user logged in!");
        }
    }

    /**
     * Đọc tất cả support tickets từ Firebase
     */
    public static void debugAllSupportTickets() {
        Log.d(TAG, "=== DEBUGGING ALL SUPPORT TICKETS ===");

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("support_tickets");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "DataSnapshot exists: " + dataSnapshot.exists());
                Log.d(TAG, "Children count: " + dataSnapshot.getChildrenCount());

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "--- Ticket: " + snapshot.getKey() + " ---");

                        // Log từng field
                        for (DataSnapshot field : snapshot.getChildren()) {
                            Log.d(TAG, "  " + field.getKey() + ": " + field.getValue());
                        }
                    }
                } else {
                    Log.w(TAG, "No support tickets found in database!");
                }

                Log.d(TAG, "=================================");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error reading support tickets: " + error.getMessage());
            }
        });
    }

    /**
     * Đọc support tickets của user hiện tại
     */
    public static void debugUserSupportTickets() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "Cannot debug user tickets - no user logged in");
            return;
        }

        String userId = user.getUid();
        Log.d(TAG, "=== DEBUGGING USER SUPPORT TICKETS ===");
        Log.d(TAG, "User ID: " + userId);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("support_tickets");

        // Query theo userId
        ref.orderByChild("userId")
           .equalTo(userId)
           .addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   Log.d(TAG, "Query result - exists: " + dataSnapshot.exists());
                   Log.d(TAG, "Query result - count: " + dataSnapshot.getChildrenCount());

                   if (dataSnapshot.exists()) {
                       for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                           Log.d(TAG, "--- User Ticket: " + snapshot.getKey() + " ---");

                           for (DataSnapshot field : snapshot.getChildren()) {
                               Log.d(TAG, "  " + field.getKey() + ": " + field.getValue());
                           }
                       }
                   } else {
                       Log.w(TAG, "No tickets found for user: " + userId);
                   }

                   Log.d(TAG, "====================================");
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {
                   Log.e(TAG, "Error querying user tickets: " + error.getMessage());
               }
           });
    }
}

