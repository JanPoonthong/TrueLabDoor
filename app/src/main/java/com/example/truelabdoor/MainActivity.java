package com.example.truelabdoor;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.widget.TextView;

import com.cardlan.twoshowinonescreen.CardLanSerialHelper;
import com.cardlan.twoshowinonescreen.CardLanStandardBus;
import com.cardlan.utils.ByteUtil;
import com.example.truelabdoor.data.TerminalConsumeDataForSystem;
import com.example.truelabdoor.util.QRJoint;

import java.io.FileDescriptor;


public class MainActivity extends AppCompatActivity {
    CardLanStandardBus mCardLanDevCtrl = new CardLanStandardBus();
    private FileDescriptor QrCodeFlag = mCardLanDevCtrl.callSerialOpen("/dev/ttyAMA4", 115200, 0);
    private TextView mTv_qc_result;
    TerminalConsumeDataForSystem terminal;

    protected boolean validateQrCode(String qrcode){
        return true;
    }

    protected void openDoor(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv_qc_result = findViewById(R.id.tv_serial_result);
        terminal = new TerminalConsumeDataForSystem();

        CardLanSerialHelper serialTest = new CardLanSerialHelper("/dev/ttyAMA4", 115200, 0, 64);
        serialTest.start();
        System.out.println("Scan your QR code");
        serialTest.setCallBack((buffer, size) -> {
            System.out.println("Scanning");
            if (buffer == null || buffer.length <= 0) {
                return;
            }

            final String qrCode = QRJoint.getCompleteQRCode(buffer, size);
            String realQRCode = ByteUtil.hexStringToString(qrCode);

            System.out.println(realQRCode);

            Log.d("qrCode", "QrCode=" + qrCode);
            if (TextUtils.isEmpty(qrCode)) {
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ByteUtil.notNull(qrCode)) {

                        mTv_qc_result.setText(": " + qrCode);
                        if (validateQrCode(qrCode)) {
                            openDoor();
                        }
                        terminal.callProc();
                        System.out.println("Done");
                    }

                }
            });
        });
    }
}