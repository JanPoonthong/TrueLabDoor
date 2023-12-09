package com.example.truelabdoor.util;

import com.cardlan.utils.ByteUtil;

public class QRJoint {

    static StringBuffer completeBuffer = new StringBuffer();
    /**
     * 获取完整的QRCode
     *
     * @param buffer
     * @param size
     * @return
     */
    public static String getCompleteQRCode(byte[] buffer, int size) {
        if (buffer == null || buffer.length <= 0) {
            return null;
        }
        if (size <= 0) {
            return null;
        }
        byte[] realData = new byte[size];
        System.arraycopy(buffer, 0, realData, 0, size);
        String bufString = ByteUtil.byteArrayToHexString(realData);
        completeBuffer.append(bufString);
        if (completeBuffer.length() <= 4) {
            return null;
        }
        if (!completeBuffer.toString().substring(completeBuffer.toString().length() - 4).equals("0D0A")) {
            return null;
        } else {
            String qrcode = completeBuffer.substring(0, completeBuffer.lastIndexOf("0D0A"));
            completeBuffer.delete(0, completeBuffer.length());
            return qrcode;
        }
    }

}
