package com.example.truelabdoor;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.cardlan.twoshowinonescreen.CardLanSerialHelper;
import com.cardlan.utils.ByteUtil;
import com.example.truelabdoor.data.TerminalConsumeDataForSystem;
import com.example.truelabdoor.util.QRJoint;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private final BroadcastReceiver broadcastReceiver;
    TerminalConsumeDataForSystem terminal;
    private Boolean portConnected = false;
    private UsbPermission usbPermission = UsbPermission.Unknown;
    private TextView mTv_qc_result;

    public MainActivity() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Constants.INTENT_ACTION_GRANT_USB.equals(intent.getAction())) {
                    usbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                            ? UsbPermission.Granted : UsbPermission.Denied;
                    connect();
                }
            }
        };
    }

    protected boolean validateQrCode(String qrcode) {
        return true;
    }

    protected void openDoor() {
        System.out.println("open");
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(broadcastReceiver, new IntentFilter(Constants.INTENT_ACTION_GRANT_USB), Context.RECEIVER_NOT_EXPORTED);
        }

    }

    @Override
    public void onPause() {
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    public void connect() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        UsbSerialProber usbCustomProber = CustomProber.getCustomProber();
        UsbSerialDriver driver = null;
        for (UsbDevice device : manager.getDeviceList().values()) {
            driver = usbCustomProber.probeDevice(device);
            if (driver != null) {
                System.out.println(device);
                System.out.println(driver);
                break;
            }
        }

        assert driver != null;

        UsbDevice device = driver.getDevice();
        UsbDeviceConnection connection = manager.openDevice(device);
        if (connection == null && usbPermission == UsbPermission.Unknown && !manager.hasPermission(driver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_MUTABLE : 0;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(Constants.INTENT_ACTION_GRANT_USB), flags);
            manager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        System.out.println(connection);
        UsbSerialPort port = driver.getPorts().get(0);
        System.out.println(port);
        try {
            port.open(connection);
            port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            if (portConnected) {
                port.write("a".getBytes(), 0);
            }
            portConnected = true;

            System.out.println("portConnected");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        connect();

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
                    if (validateQrCode(qrCode) && portConnected) {
                        connect();
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

    private enum UsbPermission {Unknown, Requested, Granted, Denied}

}
