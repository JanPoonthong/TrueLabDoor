package com.example.truelabdoor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.widget.TextView;
import tw.com.prolific.driver.pl2303.PL2303Driver;

import com.cardlan.twoshowinonescreen.CardLanSerialHelper;
import com.cardlan.twoshowinonescreen.CardLanStandardBus;
import com.cardlan.utils.ByteUtil;
import com.example.truelabdoor.data.TerminalConsumeDataForSystem;
import com.example.truelabdoor.util.QRJoint;

//import java.io.FileDescriptor;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    PL2303Driver mSerial;
    private final PL2303Driver.BaudRate mBaudrate = PL2303Driver.BaudRate.B9600;
    private final PL2303Driver.DataBits mDataBits = PL2303Driver.DataBits.D8;
    private final PL2303Driver.Parity mParity = PL2303Driver.Parity.NONE;
    private final PL2303Driver.StopBits mStopBits = PL2303Driver.StopBits.S1;
    private final PL2303Driver.FlowControl mFlowControl = PL2303Driver.FlowControl.OFF;
    String TAG = "PL2303HXD_APLog";

    private static final String ACTION_USB_PERMISSION = "com.example.truelabdoor.USB_PERMISSION";

//    CardLanStandardBus mCardLanDevCtrl = new CardLanStandardBus();

//    private final FileDescriptor QrCodeFlag = mCardLanDevCtrl.callSerialOpen("/dev/ttyAMA4", 115200, 0);
    private TextView mTv_qc_result;
    TerminalConsumeDataForSystem terminal;
    private void openUsbSerial() {
        Log.d(TAG, "Enter  openUsbSerial");

        if(mSerial==null) {

            Log.d(TAG, "No mSerial");
            return;

        }


        if (mSerial.isConnected()) {
//            if (SHOW_DEBUG) {
//                Log.d(TAG, "openUsbSerial : isConnected ");
//            }
//            mBaudrate = PL2303Driver.BaudRate.B9600;
//            String str = PL2303HXD_BaudRate_spinner.getSelectedItem().toString();
//            int baudRate= Integer.parseInt(str);
//            switch (baudRate) {
//                case 9600:
//                    mBaudrate = PL2303Driver.BaudRate.B9600;
//                    break;
//                case 19200:
//                    mBaudrate =PL2303Driver.BaudRate.B19200;
//                    break;
//                case 115200:
//                    mBaudrate =PL2303Driver.BaudRate.B115200;
//                    break;
//                default:
//                    mBaudrate =PL2303Driver.BaudRate.B9600;
//                    break;
//            }
//            Log.d(TAG, "baudRate:"+baudRate);baudRate
            // if (!mSerial.InitByBaudRate(mBaudrate)) {
            if (!mSerial.InitByBaudRate(mBaudrate,700)) {
/*
                if(!mSerial.PL2303Device_IsHasPermission()) {
                   Toast.makeText(this, "cannot open, maybe no permission", Toast.LENGTH_SHORT).show();
                }
*/

                if(mSerial.PL2303Device_IsHasPermission() && (!mSerial.PL2303Device_IsSupportChip())) {
//                    Toast.makeText(this, "cannot open, maybe this chip has no support, please use PL2303HXD / RA / EA chip.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "cannot open, maybe this chip has no support, please use PL2303HXD / RA / EA chip.");
                }
            } else {

//                Toast.makeText(this, "connected : OK" , Toast.LENGTH_SHORT).show();
                Log.d(TAG, "connected : OK");
                Log.d(TAG, "Exit  openUsbSerial");


            }
        }//isConnected
        else {
//            Toast.makeText(this, "Connected failed, Please plug in PL2303 cable again!" , Toast.LENGTH_SHORT).show();
            Log.d(TAG, "connected failed, Please plug in PL2303 cable again!");


        }
    }
    protected boolean validateQrCode(String qrcode){
        return true;
    }

    protected void openDoor(){
        openUsbSerial();
        if(null==mSerial)
            return;

        if(!mSerial.isConnected())
            return;
        System.out.println("open");
        String strWrite = "a";
        mSerial.write(strWrite.getBytes(), strWrite.length());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSerial = new PL2303Driver((UsbManager) getSystemService(Context.USB_SERVICE),
                this, ACTION_USB_PERMISSION);
        try {
            mSerial.setup(mBaudrate, mDataBits, mStopBits, mParity, mFlowControl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
                        openDoor();
                    }
                    terminal.callProc();
                    System.out.println("Done");
                }

            });
        });
    }
}