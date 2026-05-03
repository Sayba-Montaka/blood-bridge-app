package com.example.bloodbankt;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID   = "chat_messages";
    private static final String CHANNEL_NAME = "New Messages";
    private static final int    NOTIF_ID     = 1001;

    // Call once at app start (in BaseActivity or Application class)
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Sound URI — use default notification sound
            Uri soundUri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH); // HIGH = heads-up + sound
            channel.setDescription("Incoming chat messages");
            channel.setSound(soundUri, audioAttributes);
            channel.enableVibration(true);
            channel.setShowBadge(true);  // badge on app icon

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    // Call this when a new message arrives for the user
    public static void showMessageNotification(Context context,
                                               String senderName,
                                               String messageText,
                                               String senderEmail,
                                               String senderImage) {
        // Tapping notification opens the Chat screen directly
        Intent intent = new Intent(context, Chat.class);
        intent.putExtra("email", senderEmail);
        intent.putExtra("name",  senderName);
        intent.putExtra("image", senderImage);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, senderEmail.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)   // create this icon (see step 5)
                .setContentTitle(senderName)
                .setContentText(messageText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[]{0, 300, 200, 300})
                .setNumber(1)                               // badge count
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        // Use senderEmail hashCode so each sender gets their own notification slot
        manager.notify(senderEmail.hashCode(), builder.build());
    }

    // Clear notification for a specific sender when user opens that chat
    public static void clearNotification(Context context, String senderEmail) {
        NotificationManagerCompat.from(context).cancel(senderEmail.hashCode());
    }
}