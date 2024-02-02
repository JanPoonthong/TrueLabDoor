package com.example.truelabdoor;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import tw.com.prolific.driver.pl2303.PL2303Driver;

public class Serial_IO {
    private PL2303Driver mSerial;
    private Context context;
    private SerialParameters mSerialParameters;
    private class ThreadTrig implements Runnable {

        @Override
        public void run() {
            // Implementation of the run method
            trig_syn();
        }
    }

    public Serial_IO(UsbManager usbManager, Context context, String permission) {
        mSerial = new PL2303Driver(usbManager, context, permission);
        this.context = context;
        mSerialParameters = new SerialParameters();
        setupDriver();
        enumerateDriver();
        openUsbSerial();
    }

    private void enumerateDriver() {
        Log.d("enumerateDriver()", "Attempting enumerate ...");

        if (mSerial.enumerate()) {
            Log.d("enumerateDriver()", "Enumerate successful");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("enumerateDriver()", "Enumerate failed, sleeping ...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void openUsbSerial() {
        Log.d("PL2303HXD_APLog", "Enter openUsbSerial");

        if (mSerial == null) {
            return;
        }

        if (mSerial.isConnected()) {
            Log.d("openUsbSerial", "openUsbSerial : isConnected");
        }

        if (!mSerial.InitByBaudRate(PL2303Driver.BaudRate.B9600, 0x2bc)) {
            if (!mSerial.PL2303Device_IsHasPermission()) {
                Toast.makeText(context, "cannot open, maybe no permission", Toast.LENGTH_SHORT).show();
            }

            if (mSerial.PL2303Device_IsHasPermission() && !mSerial.PL2303Device_IsSupportChip()) {
                Toast.makeText(context, "cannot open, maybe this chip has no support, please use PL2303HXD / RA / EA chip.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDriver() {
        Log.d("setupDriver()", "Setting up driver ...");

        try {
            int setup = mSerial.setup(
                    mSerialParameters.mBaudRate,
                    mSerialParameters.mDataBits,
                    mSerialParameters.mStopBits,
                    mSerialParameters.mParity,
                    mSerialParameters.mFlowControl);

            boolean initByBaudRate = mSerial.InitByBaudRate(mSerialParameters.mBaudRate);

            if (setup == 0 && initByBaudRate) {
                Log.d("setupDriver()", "Setup succeeded");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("setupDriver()", "Setup failed, sleeping ...");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void active() {
        if (mSerial != null) {
            mSerial.setDTR(true);
        }
    }

    public void idle() {
        if (mSerial != null) {
            mSerial.setDTR(false);
        }
    }

    public boolean isExitActive() {
        int[] ii = mSerial.PL2303HXD_GetCommModemStatus();
        return ii[1] == 1;
    }

    public String readcard() {
        String ret = "";
        byte[] rbuf = new byte[20];
        StringBuffer sbHex = new StringBuffer();

        int len = mSerial.read(rbuf);

        if (len < 0) {
            Log.d("PL2303HXD_APLog", "Fail to bulkTransfer(read data)");
            return "";
        }

        if (len > 0) {
            for (int j = 0; j < len; j++) {
                byte value = rbuf[j];

                if ((value >= 0x61 && value <= 0x66) || (value >= 0x41 && value <= 0x46) || (value >= 0x30 && value <= 0x39)) {
                    sbHex.append((char) value);
                }
            }

            if (sbHex.toString().length() >= 1) {
                ret = sbHex.toString();
            }
        }

        return ret;
    }

    public void trig() {
        String strWrite = "a";
        mSerial.write(strWrite.getBytes(), strWrite.length());
    }

    public void trig_syn() {
        for (int i = 0; i < 50; i++) {
            mSerial.setDTR(true);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mSerial.setDTR(false);
    }



    private class SerialParameters {
        PL2303Driver.BaudRate mBaudRate = PL2303Driver.BaudRate.B9600;
        PL2303Driver.DataBits mDataBits = PL2303Driver.DataBits.D8;
        PL2303Driver.StopBits mStopBits = PL2303Driver.StopBits.S1;
        PL2303Driver.Parity mParity = PL2303Driver.Parity.NONE;
        PL2303Driver.FlowControl mFlowControl = PL2303Driver.FlowControl.OFF;
    }
}
