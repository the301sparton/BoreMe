
//Service Monitors if user has new message and also sends a notification
//Responsible to monitor the database



package com.example.boreme;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;



public class BoundService extends Service {
    private static String LOG_TAG = "BoundService";
    private IBinder mBinder = new MyBinder();
    FirebaseDatabase database;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "in onCreate");
        database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = database.getReference("messages").child(currentUser.getUid());

        //Add database event listener
        ref.addValueEventListener(new ValueEventListener() {
            int i=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Intent intent = new Intent(getApplicationContext(), Activity_home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                //Build notification base
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "NewBoreMe")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("New Message")
                        .setContentText("Message is encrypted")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setGroup("newMsgGroup");


                //if device has a relatively new android version (Oreo Plus) create a notification channel
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence name = "BoreMe Channel";
                    String description = "Notification on new messages";
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel channel = new NotificationChannel("NewBoreMe", name, importance);
                    channel.setDescription(description);
                    // Register the channel with the system; you can't change the importance
                    // or other notification behaviors after this
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    if (notificationManager != null) {
                        notificationManager.createNotificationChannel(channel);
                    }
                }


                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                // notificationId is a unique int for each notification that you must define
                //Show notification if app is not open
                if(i!=0 && !isAppOnForeground(getApplicationContext())){
                    notificationManager.notify(i, builder.build());
                }

                i++;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "in onDestroy");
    }

    public class MyBinder extends Binder {
        BoundService getService() {
            return BoundService.this;
        }
    }

    //function responsible to check if app is in foreground (is open and on users screen)
    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = "com.example.boreme";
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                //                Log.e("app",appPackageName);
                return true;
            }
        }
        return false;
    }
}