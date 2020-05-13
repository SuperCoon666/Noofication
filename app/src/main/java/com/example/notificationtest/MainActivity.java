package com.example.notificationtest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.atomic.AtomicInteger;

import static com.example.notificationtest.MyBroadcastReceiver.ACTION_SNOOZE;
import static com.example.notificationtest.MyBroadcastReceiver.EXTRA_NOTIFICATION_ID;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "CHANNEL_ID";
    private static final String TAG = "main";
    Button bStart;
    public boolean klick=false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bStart = (Button) findViewById(R.id.bStart);
        final LongOperation lo = new LongOperation(this);

        if(klick==false)
        {
            bStart.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    createNotificationChannel();
                    Log.e(TAG, "onCreate");

                    lo.execute("Дмитрий Сергеевич", "Поставье пожалуста", "баллы за четвёртый модуль");
                    klick = true;

                }
            });

        }
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private class LongOperation extends AsyncTask<String, String, String>
    {

        private static final String TAG = "longoperation";
        private Context ctx;
        private AtomicInteger notificationId = new AtomicInteger(0);

        LongOperation(Context ctx)
        {
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params)
        {
            for (String s : params) {
                Log.e(TAG, s);

                publishProgress(s);

                for (int i = 0; i < 5; i++) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                }
            }
            return "Executed";
        }

        @Override
        protected void onProgressUpdate(String... values)
        {
            for (String title: values)
            {
                sendNotification(title, notificationId.incrementAndGet());
            }
        }

        void sendNotification(String title, int notificationId)
        {

            Intent snoozeIntent = new Intent(ctx, MyBroadcastReceiver.class);
            snoozeIntent.setAction(ACTION_SNOOZE);
            snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);

            Log.e(TAG, snoozeIntent.getExtras().toString());
            PendingIntent snoozePendingIntent =
                    PendingIntent.getBroadcast(ctx, notificationId, snoozeIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(String.format(title, notificationId))
                    .setContentText("Пятый я почти доделал")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(false)
                    .addAction(R.drawable.ic_launcher_foreground, ctx.getString(R.string.snooze),
                            snoozePendingIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
