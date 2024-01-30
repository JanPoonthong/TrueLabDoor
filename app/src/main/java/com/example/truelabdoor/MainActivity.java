package com.example.truelabdoor;
//import java.io.FileDescriptor;
//import android.content.Context;
//import android.hardware.usb.UsbManager;
import androidx.appcompat.app.AppCompatActivity;
import tw.com.prolific.driver.pl2303.PL2303Driver.BaudRate;
import tw.com.prolific.driver.pl2303.PL2303Driver.DataBits;
import tw.com.prolific.driver.pl2303.PL2303Driver.FlowControl;
import tw.com.prolific.driver.pl2303.PL2303Driver.Parity;
import tw.com.prolific.driver.pl2303.PL2303Driver.StopBits;

import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.widget.TextView;

import com.cardlan.twoshowinonescreen.CardLanSerialHelper;
// import com.cardlan.twoshowinonescreen.CardLanStandardBus;
import com.cardlan.utils.ByteUtil;
import com.example.truelabdoor.data.TerminalConsumeDataForSystem;
import com.example.truelabdoor.util.QRJoint;



public class MainActivity extends AppCompatActivity {

    //sget-object v2, Ltw/com/prolific/driver/pl2303/PL2303Driver$BaudRate;->B9600:Ltw/com/prolific/driver/pl2303/PL2303Driver$BaudRate;

    //sget-object v3, Ltw/com/prolific/driver/pl2303/PL2303Driver$DataBits;->D8:Ltw/com/prolific/driver/pl2303/PL2303Driver$DataBits;

    //sget-object v4, Ltw/com/prolific/driver/pl2303/PL2303Driver$StopBits;->S1:Ltw/com/prolific/driver/pl2303/PL2303Driver$StopBits;

    //sget-object v5, Ltw/com/prolific/driver/pl2303/PL2303Driver$Parity;->NONE:Ltw/com/prolific/driver/pl2303/PL2303Driver$Parity;

    //sget-object v6, Ltw/com/prolific/driver/pl2303/PL2303Driver$FlowControl;->OFF:Ltw/com/prolific/driver/pl2303/PL2303Driver$FlowControl;
    private final SerialParameters mSerialParameters = new SerialParameters(
            BaudRate.B9600,
            DataBits.D8,
            StopBits.S1,
            Parity.NONE,
            FlowControl.OFF);

    private final Serial_IO IO = new Serial_IO(this, mSerialParameters);

//    CardLanStandardBus mCardLanDevCtrl = new CardLanStandardBus();

//    private final FileDescriptor QrCodeFlag = mCardLanDevCtrl.callSerialOpen("/dev/ttyAMA4", 115200, 0);
    private TextView mTv_qc_result;
    TerminalConsumeDataForSystem terminal;

    protected boolean validateQrCode(String qrcode){
        return true;
    }

    protected void openDoor(){
        IO.openDoor();
        System.out.println("open");
       // String strWrite = "a";
        //mSerial.write(strWrite.getBytes(), strWrite.length());
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