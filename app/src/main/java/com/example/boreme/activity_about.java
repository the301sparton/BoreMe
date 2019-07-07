package com.example.boreme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import de.hdodenhof.circleimageview.CircleImageView;

public class activity_about extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about);
        CircleImageView git,lin,gplus,gplus_t,lin_t;
        git = findViewById(R.id.git);
        lin = findViewById(R.id.lin);

        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri uri = Uri.parse("https://www.github.com/the301sparton");
                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().addDefaultShareMenuItem().enableUrlBarHiding().setCloseButtonIcon(BitmapFactory.decodeResource(
                        getResources(), R.drawable.ic_chevron_right_black_24dp)).setToolbarColor(Color.parseColor("#000000")).build();

                CustomTabActivityHelper.openCustomTab(activity_about.this, customTabsIntent, uri,
                        new CustomTabActivityHelper.CustomTabFallback() {
                            @Override
                            public void openUri(Activity activity, Uri uri) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                activity.startActivity(intent);
                            }
                        });

            }
        });




        lin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.linkedin.com/in/chaitanya-deshpande-6bb71b153");
                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().addDefaultShareMenuItem().enableUrlBarHiding().setCloseButtonIcon(BitmapFactory.decodeResource(
                        getResources(), R.drawable.ic_chevron_right_black_24dp)).setToolbarColor(Color.parseColor("#000000")).build();

                CustomTabActivityHelper.openCustomTab(activity_about.this, customTabsIntent, uri,
                        new CustomTabActivityHelper.CustomTabFallback() {
                            @Override
                            public void openUri(Activity activity, Uri uri) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                activity.startActivity(intent);
                            }
                        });
            }
        });

    }
}
