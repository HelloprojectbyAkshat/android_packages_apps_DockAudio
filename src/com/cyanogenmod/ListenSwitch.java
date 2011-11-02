package com.cyanogenmod.dockaudio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.UEventObserver;
import android.util.Log;

public class ListenSwitch extends Service {
    private static final String LOG_TAG = "DockAudio";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mUEventObserver.startObserving("DEVPATH=/devices/virtual/switch/emuconn");
        Log.i(LOG_TAG, "Dock Audio service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "Dock Audio service stopped");
    }


    /*
     * Listens for uevent messages from the kernel
     */
    private final UEventObserver mUEventObserver = new UEventObserver() {
        @Override
        public void onUEvent(UEventObserver.UEvent event) {
            Log.i(LOG_TAG, "DockAudio UEVENT: " + event.toString());

            int state = 0;
            try {
                state = Integer.parseInt(event.get("SWITCH_STATE"));
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "Error parsing switch state!");
            }

            Intent intent = new Intent();
            if (0 == state) { //No Device
                intent.setAction("com.cyanogenmod.dockaudio.DISABLE_AUDIO");
            } if (1 == state || 2 == state) { // Mono out or Stereo out
                intent.setAction("com.cyanogenmod.dockaudio.ENABLE_ANALOG_AUDIO");
            } else { //if ("SPDIF audio out".equals(name)) {
                intent.setAction("com.cyanogenmod.dockaudio.ENABLE_DIGITAL_AUDIO");
            }
            sendBroadcast(intent);
            Log.i(LOG_TAG, "Broadcasted intent for state " + state);
        }
    };
}
