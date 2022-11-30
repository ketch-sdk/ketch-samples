package com.ketch.sample.webview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ORG_CODE = "<organization code>";
    private static final String PROPERTY = "<property>";
    private static final String ADVERTISING_ID_CODE = "<advertising field code>";

    private KetchSharedPreferences sharedPreferences;

    private KetchWebView ketchWebView;
    private View mainLayout;
    private Button button;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        sharedPreferences = new KetchSharedPreferences(getApplicationContext());

        ketchWebView = findViewById(R.id.ketchWebView);
        mainLayout = findViewById(R.id.mainLayout);
        button = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);

        ketchWebView.setListener(new KetchWebView.KetchListener() {
            @Override
            public void onCCPAUpdate(String ccpaString) {
                sharedPreferences.saveUSPrivacyString(ccpaString);
            }

            @Override
            public void onTCFUpdate(String tcfString) {
                sharedPreferences.saveTCFTCString(tcfString);
            }

            @Override
            public void onClose() {
                mainLayout.setVisibility(View.VISIBLE);
            }
        });

        button.setOnClickListener(v -> {
            ketchWebView.show();
            mainLayout.setVisibility(View.GONE);
        });

        loadAdvertisingId(advertisingId -> {
            if (advertisingId != null) {
                button.setVisibility(View.VISIBLE);

                // identities to be passed to the WebView displaying the Ketch Preference Center
                List<Identity> identities = new ArrayList<>();
                identities.add(new Identity(ADVERTISING_ID_CODE, advertisingId));

                ketchWebView.init(ORG_CODE, PROPERTY, identities);
            } else {
                button.setVisibility(View.GONE);
            }
        });
    }

    private interface AdvertisingIdCallback {
        void onAdvertisingIdResult(@Nullable String advertisingId);
    }

    private void loadAdvertisingId(AdvertisingIdCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            String id = null;
            try {
                id = AdvertisingIdClient.getAdvertisingIdInfo(MainActivity.this).getId();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                handler.post(() -> {
                    Toast.makeText(MainActivity.this,
                            R.string.cannot_get_advertising_id_toast,
                            Toast.LENGTH_LONG
                    ).show();
                });
            } finally {
                final String advertisingId = id;
                handler.post(() -> {
                    callback.onAdvertisingIdResult(advertisingId);
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }
}

