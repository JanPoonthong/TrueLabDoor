package com.example.truelabdoor;

import tw.com.prolific.driver.pl2303.PL2303Driver.BaudRate;
import tw.com.prolific.driver.pl2303.PL2303Driver.DataBits;
import tw.com.prolific.driver.pl2303.PL2303Driver.FlowControl;
import tw.com.prolific.driver.pl2303.PL2303Driver.Parity;
import tw.com.prolific.driver.pl2303.PL2303Driver.StopBits;
public class SerialParameters {
    public final BaudRate mBaudRate;
    public final DataBits mDataBits;
    public final StopBits mStopBits;
    public final Parity mParity;
    public final FlowControl mFlowControl;
    public SerialParameters(
            BaudRate baudRate,
            DataBits dataBits,
            StopBits stopBits,
            Parity parity,
            FlowControl flowControl) {
        mBaudRate = baudRate;
        mDataBits = dataBits;
        mStopBits = stopBits;
        mParity = parity;
        mFlowControl = flowControl;
    }
}
