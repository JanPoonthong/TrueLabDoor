package com.example.truelabdoor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

import java.util.Objects;

import tw.com.prolific.driver.pl2303.PL2303Driver;

public class StartBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Objects.equals(action, Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
//        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
//            Intent i = new Intent(context, MainActivity.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(i);
//        }
    }
}