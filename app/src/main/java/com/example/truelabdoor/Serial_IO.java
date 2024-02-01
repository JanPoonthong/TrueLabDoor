package com.example.truelabdoor;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.IOException;

import tw.com.prolific.driver.pl2303.PL2303Driver;

public class Serial_IO implements Runnable {
    private static final String USB_PERMISSION = "com.example.truelabdoor.USB_PERMISSION";
    private static final int READ_BUFFER_SIZE = 2048;
    private static final long ENUMERATE_PAUSE = 1000L;
    private static final long SETUP_PAUSE = 2000L;
    private static final long READ_PAUSE = 10L;

    private final Context mContext;
    private final SerialParameters mSerialParameters;
    private PL2303Driver mDriver;
    private final byte[] mReadBuffer = new byte[READ_BUFFER_SIZE];
    private final StringBuilder mLineBuilder = new StringBuilder();
    private boolean mRunning = true;
    private Thread mThread;

    public Serial_IO(Context context, SerialParameters serialParameters) {
        mContext = context;
        mSerialParameters = serialParameters;
        (mThread = new Thread(this)).start();
    }

    @Override
    public void run() {
        mDriver = new PL2303Driver(
                (UsbManager) mContext.getSystemService(Context.USB_SERVICE),
                mContext,
                USB_PERMISSION);
        boolean doneSetup = false;
        try {
            while (mRunning) {
                if (!mDriver.isConnected()) {
                    doneSetup = false;
                    enumerateDriver();
                } else if (mDriver.isConnected() && !doneSetup) {
                    doneSetup = setupDriver();
                } else {
                    readFromDriver();
                }
            }
        } catch (InterruptedException e) {
            // Interruption is an expected way to terminate
            Log.i(Constants.LOG_TAG, "Interrupted data read thread, terminating");
        }
    }
    private void enumerateDriver() throws InterruptedException {
        if (!mDriver.enumerate()) {
//            System.out.println(mDriver.enumerate());
            Thread.sleep(ENUMERATE_PAUSE);
        }
    }

    private boolean setupDriver() throws InterruptedException {
        Log.i(Constants.LOG_TAG, "Setting up driver ...");
        System.out.println("Setting up driver ...");
        boolean success;
        try {
            int setup = mDriver.setup(
                    mSerialParameters.mBaudRate,
                    mSerialParameters.mDataBits,
                    mSerialParameters.mStopBits,
                    mSerialParameters.mParity,
                    mSerialParameters.mFlowControl);
            boolean initByBaudRate = mDriver.InitByBaudRate(
                    mSerialParameters.mBaudRate);
            success = setup == 0 && initByBaudRate;
        } catch (IOException e) {
            success = false;
            Log.e(Constants.LOG_TAG, e.toString());
        }
        if (success) {
            Log.i(Constants.LOG_TAG, "Setup succeeded");
            return true;
        } else {
            Log.i(Constants.LOG_TAG, "Setup failed, sleeping ...");
            Thread.sleep(SETUP_PAUSE);
            return false;
        }
    }

    private void readFromDriver() throws InterruptedException {
        for (int n; (n = mDriver.read(mReadBuffer)) > 0; ) {
            receivedData(n);
        }
        Thread.sleep(READ_PAUSE);
    }

    private boolean isLineSeparator(char c) {
        return c == '\r' || c == '\n';
    }

    private void receivedData(int n) {
        for (int i = 0; i < n; i++) {
            char c = (char) mReadBuffer[i];
            if (isLineSeparator(c)) {
                if (mLineBuilder.length() > 0) {
                    mLineBuilder.setLength(0);
                }
            } else {
                mLineBuilder.append(c);
            }
        }
    }
    public void openDoor() {
        String strWrite = "a";
        byte[] bytesToWrite = strWrite.getBytes();
        int numBytes = strWrite.length();
        mDriver.write(bytesToWrite, numBytes);
    }

}
