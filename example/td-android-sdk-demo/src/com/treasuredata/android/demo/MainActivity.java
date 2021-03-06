package com.treasuredata.android.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;
import com.treasuredata.android.TDCallback;
import com.treasuredata.android.TreasureData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TreasureData.initializeApiEndpoint("https://mobile-ybi.jp-east.idcfcloud.com");
        // TreasureData.initializeDefaultApiKey("your_default_api_key");
        TreasureData.enableLogging();
        TreasureData.initializeEncryptionKey("hello world");
        // TreasureData.disableEventCompression();

        TreasureData.initializeSharedInstance(this, "your_default_api_key");
        TreasureData.sharedInstance().setDebugMode(false);
        TreasureData.sharedInstance().enableAutoAppendUniqId();
        TreasureData.sharedInstance().enableAutoAppendModelInformation();
        TreasureData.sharedInstance().setDefaultDatabase("testdb");
        TreasureData.sharedInstance().startSession("demotbl");
        // TreasureData.sharedInstance().disableAutoRetryUploading();

        if (TreasureData.sharedInstance().isFirstRun(this)) {
            TreasureData.sharedInstance().addEventWithCallback("demotbl", "first_run", true, new TDCallback() {
                @Override
                public void onSuccess() {
                    TreasureData.sharedInstance().uploadEventsWithCallback(new TDCallback() {
                        @Override
                        public void onSuccess() {
                            TreasureData.sharedInstance().clearFirstRun(MainActivity.this);
                        }

                        @Override
                        public void onError(String errorCode, Exception e) {
                            Log.w(TAG, "TreasureData.uploadEvent:onError errorCode=" + errorCode + ", ex=" + e);
                        }
                    });
                }

                @Override
                public void onError(String errorCode, Exception e) {
                    Log.w(TAG, "TreasureData.addEvent:onError errorCode=" + errorCode + ", ex=" + e);
                }
            });
        }

        // For default callback, optional.
        TreasureData.sharedInstance().setAddEventCallBack(addEventCallback);
        // td.setUploadEventsCallBack(uploadEventsCallback);

        List<Pair<Integer, String>> targets = Arrays.asList(
                new Pair<Integer, String>(R.id.navi_help, "navi_help"),
                new Pair<Integer, String>(R.id.navi_news, "navi_news"),
                new Pair<Integer, String>(R.id.navi_play, "navi_play")
        );

        for (Pair<Integer, String> target : targets) {
            int id = target.first;
            final String label = target.second;
            View v = findViewById(id);
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Map event = new HashMap<String, Object>();
                    event.put("label", label);
                    event.put("id", v.getId());
                    event.put("left", v.getLeft());
                    event.put("right", v.getRight());
                    event.put("top", v.getTop());
                    event.put("bottom", v.getBottom());

                    addEventCallback.eventName = label;
                    TreasureData.sharedInstance().addEventWithCallback("demotbl", event, addEventCallback);
                }
            });
        }

        findViewById(R.id.image).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                addEventCallback.eventName = "image";
                // Use default callback
                TreasureData.sharedInstance().addEvent("demotbl", "image", ev.toString());
                return false;
            }
        });

        findViewById(R.id.upload).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                TreasureData.sharedInstance().uploadEventsWithCallback(uploadEventsCallback);
                return false;
            }
        });
    }

    /*
     * These are only for callback, Optional.
     */
    class AddEventCallback implements TDCallback {
        String eventName;

        @Override
        public void onSuccess() {
            String message = "TreasureData.addEvent:onSuccess[" + eventName + "]";
            Log.d(TAG, message);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String errorCode, Exception e) {
            String message = "TreasureData.addEvent:onError[" + eventName + ": errorCode=" + errorCode + ", ex=" + e + "]";
            Log.d(TAG, message);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    class UploadEventsCallback implements TDCallback {
        @Override
        public void onSuccess() {
            String message = "TreasureData.uploadEvents:onSuccess";
            Log.d(TAG, message);
        }

        @Override
        public void onError(String errorCode, Exception e) {
            String message = "TreasureData.uploadEvents:onError[" + errorCode + ": errorCode=" + errorCode + ", ex=" + e + "]";
            Log.d(TAG, message);
        }
    }

    private AddEventCallback addEventCallback = new AddEventCallback();

    private UploadEventsCallback uploadEventsCallback = new UploadEventsCallback();

    @Override
    protected void onDestroy() {
        TreasureData.sharedInstance().endSession("demotbl");
        TreasureData.sharedInstance().uploadEvents();
        super.onDestroy();
    }
}
