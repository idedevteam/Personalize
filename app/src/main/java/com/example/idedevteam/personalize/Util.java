package com.example.idedevteam.personalize;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Created by neo on 24/01/18.
 */

public class Util {

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static Date bytesToDate(byte[] dateBytes) {
        String hexDate = bytesToHex(dateBytes);
        int secs = Integer.parseInt(hexDate, 16);
        byte[] bytes = ByteBuffer.allocate(4).putInt(secs).array();
        int dis = ByteBuffer.wrap(bytes).getInt();
        long l = (long) dis*1000;
        return new Date(l);
    }

    public static String getformattedDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hex = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hex[j * 2] = hexArray[v >>> 4];
            hex[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hex);
    }

    public static byte[] trimZeroPadding(byte[] bytes) {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0) {
            --i;
        }
        return Arrays.copyOf(bytes, i + 1);
    }

    public static String bytesToString(byte[] bytes) {
        String hex = bytesToHex(bytes);
        StringBuilder output = new StringBuilder();
        for (int i=0; i < hex.length(); i+=2) {
            String str = hex.substring(i, i+2);
            output.append((char)Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    public static String asciiToHex(String asciiValue)
    {
        char[] chars = asciiValue.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++)
        {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }

//    public static int getWriteIndex(ArrayList<MedrecDinamikData> mddArray) {
    public static int getWriteIndex(ArrayList<Date> timestamp) {
        int arraysize = timestamp.size();
        ArrayList<Date> dates = new ArrayList<>();
        for (int i=0; i < arraysize; i++) {
            dates.add(timestamp.get(i));
        }
        if (dates.isEmpty()) {
            return 0;
        } else {
            Date mostRecent = Collections.max(dates);
            int maxIdx = dates.indexOf(mostRecent);
            return (maxIdx+1) % 5;
        }
    }

    public static String getFormattedDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy");
        return sdf.format(date);
    }

    public static byte[] intToBytes(int integer) {
        return ByteBuffer.allocate(4).putInt(integer).array();
    }

    public static byte[] intToShortToBytes(int integer) {
        short shortint = (short) integer;
        return ByteBuffer.allocate(2).putShort(shortint).array();
    }

    public static byte[] dateToBytes(Date date) {
        long millis = date.getTime();
        int secs = (int) (millis/1000);
        return ByteBuffer.allocate(4).putInt(secs).array();
    }

    public static Date getCurrentDate() {
        int dateInSecs = (int) (System.currentTimeMillis()/1000);
        byte[] bytes = ByteBuffer.allocate(4).putInt(dateInSecs).array();
        int dis = ByteBuffer.wrap(bytes).getInt();
        long l = (long) dis*1000;
        return new Date(l);
    }

    public static Date getTimeMilisDate(String tanggal) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(tanggal);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int dateInSecs = (int) (date.getTime()/1000);
        byte[] bytes = ByteBuffer.allocate(4).putInt(dateInSecs).array();
        int dis = ByteBuffer.wrap(bytes).getInt();
        long l = (long) dis*1000;
        return new Date(l);
    }

    public static String padVariableText(String str, int totalLength) {
        String hex = bytesToHex(stringToBytes(str));
        int padsCharNum = totalLength - str.length();
        for (int i=0; i<padsCharNum; i++) {
            hex += "00";
        }
        return hex;
    }

    public static byte[] stringToBytes(String string) {
        return string.getBytes(Charset.forName("UTF-8"));
    }

    public static String stringToHex(String string) {
        return bytesToHex(stringToBytes(string));
    }

    public static String intToHex(int i) {
        String hexString = Integer.toHexString(i);
        String hex = "";
        int zeroNum = 8 - hexString.length();
        for (int j = 0; j < zeroNum; j++) {
            hex += "0";
        }
        hex += hexString;
        return hex;
    }

    public static byte[] floatToBytes(float floatNum) {
        return ByteBuffer.allocate(4).putFloat(floatNum).array();
    }

    public static float bytesToFloat(byte[] floatBytes) {
        return ByteBuffer.wrap(floatBytes).getFloat();
    }

    public static String intToHex2(int i) {
        String hexString = Integer.toHexString(i);
        String hex = "";
        int zeroNum = 4 - hexString.length();
        for (int j = 0; j < zeroNum; j++) {
            hex += "0";
        }
        hex += hexString;
        return hex;
    }

    public static String intToHex3(int i) {
        String hexString = Integer.toHexString(i);
        String hex = "";
        int zeroNum = 6 - hexString.length();
        for (int j = 0; j < zeroNum; j++) {
            hex += "0";
        }
        hex += hexString;
        return hex;
    }

    public static String intToHex4(int i) {
        String hexString = Integer.toHexString(i);
        String hex = "";
        int zeroNum = 2 - hexString.length();
        for (int j = 0; j < zeroNum; j++) {
            hex += "0";
        }
        hex += hexString;
        return hex;
    }

}
