package com.example.truelabdoor;


import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;



import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
//import android.os.Build;
//import android.widget.Toast;

import android.hardware.usb.UsbManager;

import androidx.appcompat.app.AppCompatActivity;


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

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {



//    CardLanStandardBus mCardLanDevCtrl = new CardLanStandardBus();

//    private final FileDescriptor QrCodeFlag = mCardLanDevCtrl.callSerialOpen("/dev/ttyAMA4", 115200, 0);
    private TextView mTv_qc_result;
    TerminalConsumeDataForSystem terminal;

    protected boolean validateQrCode(String qrcode){
        return true;
    }

    protected void openDoor(){
//        openUsbSerial();
        //IO.trig();
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
/*private void requestUsbPermission() {
    UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(Constants.ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
    IntentFilter filter = new IntentFilter(Constants.ACTION_USB_PERMISSION);

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
                        //connect();
                        // Permission granted, you can now access the USB device
                        // Perform your USB-related operations here
                    }
                }
            }
        }
    }
};*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(Constants.ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        IntentFilter filter = new IntentFilter(Constants.ACTION_USB_PERMISSION);
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDevice device = driver.getDevice();
        UsbDeviceConnection connection = manager.openDevice(device);
        if (connection == null) {
            manager.requestPermission(device, permissionIntent);
            return;
        }
        UsbSerialPort port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        try {
            port.open(connection);
            port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
//        UsbDevice device = deviceList.get("/dev/bus/usb/001/004");
//        assert device != null;
//        System.out.println(device.getDeviceProtocol());
        //UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        //List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        //if (availableDrivers.isEmpty()) {
          //  return;
        //}
/*        requestUsbPermission();*/


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
                        try {
                            port.write("a".getBytes(), 1000);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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
    }
}