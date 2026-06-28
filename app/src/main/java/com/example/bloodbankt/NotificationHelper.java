package com.example.bloodbankt;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {

    // ✅ Changed to v2 to force fresh channel registration
    private static final String CHANNEL_ID   = "chat_messages_v2";
    private static final String CHANNEL_NAME = "New Messages";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri soundUri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Incoming chat messages");
            channel.setSound(soundUri, audioAttributes);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public static void showMessageNotification(Context context,
                                               String senderName,
                                               String messageText,
                                               String senderEmail,
                                               String senderImage) {
        // ✅ Log so you can see in Logcat whether this is even being called
        Log.d("NOTIF_HELPER", "Attempting notification for: " + senderEmail);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("NOTIF_HELPER", "POST_NOTIFICATIONS permission not granted — notification blocked");
                return;
            }
        }

        Intent intent = new Intent(context, Chat.class);
        intent.putExtra("email", senderEmail);
        intent.putExtra("name",  senderName);
        intent.putExtra("image", senderImage);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, senderEmail.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                // ✅ Use launcher icon — safe fallback that always exists
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(senderName)
                .setContentText(messageText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[]{0, 300, 200, 300})
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(senderEmail.hashCode(), builder.build());
        Log.d("NOTIF_HELPER", "Notification fired for: " + senderName);
    }

    public static void clearNotification(Context context, String senderEmail) {
        NotificationManagerCompat.from(context).cancel(senderEmail.hashCode());
    }
}