package com.example.truelabdoor;
//import java.io.FileDescriptor;
//import android.content.Context;
//import android.hardware.usb.UsbManager;
import static tw.com.prolific.driver.pl2303.PL2303Driver.BaudRate.B9600;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.widget.Toast;

import android.hardware.usb.UsbManager;
import androidx.appcompat.app.AppCompatActivity;

import tw.com.prolific.driver.pl2303.PL2303Driver;
import tw.com.prolific.driver.pl2303.PL2303Driver.BaudRate;
import tw.com.prolific.driver.pl2303.PL2303Driver.DataBits;
import tw.com.prolific.driver.pl2303.PL2303Driver.FlowControl;
import tw.com.prolific.driver.pl2303.PL2303Driver.Parity;
import tw.com.prolific.driver.pl2303.PL2303Driver.StopBits;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.widget.TextView;

import com.cardlan.twoshowinonescreen.CardLanSerialHelper;
// import com.cardlan.twoshowinonescreen.CardLanStandardBus;
import com.cardlan.utils.ByteUtil;
import com.example.truelabdoor.data.TerminalConsumeDataForSystem;
import com.example.truelabdoor.util.QRJoint;

import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity {
        Serial_IO IO;


    //sget-object v2, Ltw/com/prolific/driver/pl2303/PL2303Driver$BaudRate;->B9600:Ltw/com/prolific/driver/pl2303/PL2303Driver$BaudRate;

    //sget-object v3, Ltw/com/prolific/driver/pl2303/PL2303Driver$DataBits;->D8:Ltw/com/prolific/driver/pl2303/PL2303Driver$DataBits;

    //sget-object v4, Ltw/com/prolific/driver/pl2303/PL2303Driver$StopBits;->S1:Ltw/com/prolific/driver/pl2303/PL2303Driver$StopBits;

    //sget-object v5, Ltw/com/prolific/driver/pl2303/PL2303Driver$Parity;->NONE:Ltw/com/prolific/driver/pl2303/PL2303Driver$Parity;

    //sget-object v6, Ltw/com/prolific/driver/pl2303/PL2303Driver$FlowControl;->OFF:Ltw/com/prolific/driver/pl2303/PL2303Driver$FlowControl;

//    CardLanStandardBus mCardLanDevCtrl = new CardLanStandardBus();

//    private final FileDescriptor QrCodeFlag = mCardLanDevCtrl.callSerialOpen("/dev/ttyAMA4", 115200, 0);
    private TextView mTv_qc_result;
    TerminalConsumeDataForSystem terminal;

    protected boolean validateQrCode(String qrcode){
        return true;
    }

    protected void openDoor(){
//        openUsbSerial();
        IO.trig();
        System.out.println("open");
       // String strWrite = "a";
        //mSerial.write(strWrite.getBytes(), strWrite.length());
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(Constants.ACTION_USB_PERMISSION), 0);
//        IntentFilter filter = new IntentFilter(Constants.ACTION_USB_PERMISSION);
//    }
private void requestUsbPermission() {
    UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(Constants.ACTION_USB_PERMISSION), 0);
    IntentFilter filter = new IntentFilter(Constants.ACTION_USB_PERMISSION);
    registerReceiver(usbReceiver, filter);

    // Get a list of connected USB devices
    HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

    while (deviceIterator.hasNext()) {
        UsbDevice device = deviceIterator.next();
        usbManager.requestPermission(device, permissionIntent);
    }
}
private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Constants.ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {
                        IO = new Serial_IO(getApplicationContext());
                        // Permission granted, you can now access the USB device
                        // Perform your USB-related operations here
                    }
                } else {
                    // Permission denied
                    // Handle accordingly
                }
            }
        }
    }
};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
//        UsbDevice device = deviceList.get("/dev/bus/usb/001/004");
//        assert device != null;
//        System.out.println(device.getDeviceProtocol());
        requestUsbPermission();


//        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
//        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
//        System.out.println("Device List");
//        while(deviceIterator.hasNext()){
//            UsbDevice device = deviceIterator.next();
//            System.out.println("device");
//            System.out.println(device);
//            // your code
//        }
//        UsbDevice device = deviceList.get("deviceName");
        setContentView(R.layout.activity_main);
        mTv_qc_result = findViewById(R.id.tv_serial_result);
        terminal = new TerminalConsumeDataForSystem();

        CardLanSerialHelper serialTest = new CardLanSerialHelper("/dev/ttyAMA4", 115200, 0, 64);
        serialTest.start();
        System.out.println("Scan your QR code");
        serialTest.setCallBack((buffer, size) -> {
            System.out.println("Scanning");
            if (buffer == null || buffer.length == 0) {
                return;
            }

            final String qrCode = QRJoint.getCompleteQRCode(buffer, size);
            String realQRCode = ByteUtil.hexStringToString(qrCode);

            System.out.println(realQRCode);

            Log.d("qrCode", "QrCode=" + qrCode);
            if (TextUtils.isEmpty(qrCode)) return;


            runOnUiThread(() -> {
                if (ByteUtil.notNull(qrCode)) {
                    String qrFormat = ": " + qrCode;
                    mTv_qc_result.setText(qrFormat);
                    if (validateQrCode(qrCode)) {
                        openDoor(IO);
                    }
                    terminal.callProc();
                    System.out.println("Done");
                }

            });
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
    }
}
