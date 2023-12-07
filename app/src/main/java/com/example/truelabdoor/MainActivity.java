package com.example.truelabdoor;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cardlan.twoshowinonescreen.CardLanSerialHelper;
import com.cardlan.twoshowinonescreen.CardLanSpiHelper;
import com.cardlan.twoshowinonescreen.CardLanStandardBus;
import com.cardlan.utils.ByteUtil;
import com.example.truelabdoor.data.TerminalConsumeDataForSystem;
import com.example.truelabdoor.util.QRJoint;

import java.io.FileDescriptor;


public class MainActivity extends AppCompatActivity {
    String QRCodeTailSplit = "\r\n";
    CardLanStandardBus mCardLanDevCtrl = new CardLanStandardBus();
    private FileDescriptor QrCodeFlag = mCardLanDevCtrl.callSerialOpen("/dev/ttyAMA4", 115200, 0);
    private TextView mTv_qc_result;
    TerminalConsumeDataForSystem terminal;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv_qc_result = findViewById(R.id.tv_serial_result);
        terminal = new TerminalConsumeDataForSystem();

        CardLanSerialHelper serialTest = new CardLanSerialHelper("/dev/ttyAMA4", 115200, 0, 64);
        serialTest.start();
        serialTest.setCallBack((buffer, size) -> {
            if (buffer == null || buffer.length <= 0) {
                return;
            }

            final String qrCode = QRJoint.getCompleteQRCode(buffer, size);
            String realQRCode = ByteUtil.hexStringToString(qrCode);

            Log.d("qrCode", "QrCode=" + qrCode);
            if (TextUtils.isEmpty(qrCode)) {
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ByteUtil.notNull(qrCode)) {

                        mTv_qc_result.setText(": " + qrCode);
                        terminal.callProc();
                    }

                }
            });
        });
    }
}