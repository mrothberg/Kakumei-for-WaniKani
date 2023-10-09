package com.mrothberg.kakumei.app.workers;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mrothberg.kakumei.R;
import com.mrothberg.kakumei.app.App;
import com.mrothberg.kakumei.app.activity.MainActivity;
import com.mrothberg.kakumei.client.WaniKaniApiV2;
import com.mrothberg.kakumei.client.WaniKaniServiceV2Builder;
import com.mrothberg.kakumei.managers.PrefManager;
import com.mrothberg.kakumei.wkamodels.StudyQueue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class NotificationWorker extends Worker {

    private static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_ID = "default";

    private final static String WORKER_NAME = "ReviewNotificationService";

    public NotificationWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams
    ){
        super(appContext, workerParams);
    }
    @NonNull
    @Override
    public Result doWork() {
        WaniKaniApiV2 waniKaniApi = new WaniKaniApiV2(new WaniKaniServiceV2Builder(PrefManager.getApiKey()));
        try {
            StudyQueue queue = waniKaniApi.getStudyQueue().get();
            int reviews = queue.reviews_available;
            int prevReview = PrefManager.getReviewsAtLastSync();
            if(reviews != 0 && (PrefManager.reminderNotificationEnabled() || reviews != prevReview)) {
                sendNotification(reviews);
            }
            PrefManager.setReviewsAtLastSync(reviews);

            return Result.success();
        } catch (InterruptedException e) {
            System.out.println(e);
            return Result.failure();
        } catch (ExecutionException e) {
            System.out.println(e);
            return Result.failure();
        }
    }

    private void sendNotification(int numReviews){

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), default_notification_channel_ID)
                .setSmallIcon(R.drawable.ic_burned)
                .setContentTitle("Time to review!")
                .setContentText(numReviews + " reviews in queue")
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_IMMUTABLE));

        NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Review Reminders", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.MAGENTA);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long []{ 100 , 200 , 300 , 400 , 500 , 400 , 300 , 200 , 400 });
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(0, mBuilder.build());
    }

    public static void startNotificationService(long interval) {
        LocalDateTime currentTime = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime targetTime = currentTime.plusHours(1).truncatedTo(ChronoUnit.HOURS);
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, interval, TimeUnit.MILLISECONDS, 5, TimeUnit.MINUTES).setInitialDelay(Duration.between(currentTime, targetTime)).build();
        WorkManager.getInstance(App.getContext()).enqueueUniquePeriodicWork(WORKER_NAME, ExistingPeriodicWorkPolicy.UPDATE, workRequest);
    }

    public static void stopNotificationService(){
        WorkManager.getInstance(App.getContext()).cancelUniqueWork(WORKER_NAME);
    }
}
