package com.rthtech.bledemo;

import java.security.MessageDigest;

public class Util {

    public static String hexstr(byte value) {
        return String.format("%02X", value);
    }

    public static String hexstr(byte data[], boolean spaceSplit) {
        StringBuilder stringBuilder;
        String format;
        if (null == data) {
            return "";
        }
        stringBuilder = new StringBuilder(data.length);
        if (spaceSplit) {
            format = "%02X ";
        } else {
            format = "%02X";
        }
        for (byte byteChar : data) {
            stringBuilder.append(String.format(format, ( int ) byteChar & 0xff));
        }
        return stringBuilder.toString().trim();
    }

    private static byte ch2hex(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ( byte ) (ch - '0');
        }
        if (ch >= 'a' && ch <= 'f') {
            return ( byte ) (ch - 'a' + 10);
        }
        if (ch >= 'A' && ch <= 'F') {
            return ( byte ) (ch - 'A' + 10);
        }
        return ( byte ) 0;
    }

    public static byte hex2byte(String str) {
        char ch0, ch1;
        if (str.length() == 0) {
            return ( byte ) 0;
        }
        if (str.length() == 1) {
            str = "0" + str;
        }
        ch0 = str.charAt(0);
        ch1 = str.charAt(1);
        return ( byte ) ((ch2hex(ch0) << 4) | ch2hex(ch1));
    }

    public static byte[] hex2bytes(String str) {
        byte[] ret;
        int index, count;
        str = str.replaceAll(" ", "").replace("\r", "").replaceAll("\n", "");
        if (str.length() % 2 == 1) {
            str = str.substring(0, str.length() - 1) + "0" + str.charAt(str.length() - 1);
        }
        count = str.length();
        ret = new byte[count / 2];
        for (index = 0; index < count; index += 2) {
            ret[index / 2] = ( byte ) (( byte ) (ch2hex(str.charAt(index)) << 4) | ch2hex(str.charAt(index + 1)));
        }
        return ret;
    }

    public static String AddSpace(String msg) {

        StringBuilder str = new StringBuilder(msg.replace(" ", ""));

        int i = str.length() / 2;
        int j = str.length() % 2;

        for (int x = (j == 0 ? i - 1 : i); x > 0; x--) {
            str = str.insert(x * 2, " ");
        }
        return str.toString();
    }

    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = ( byte ) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = ( byte ) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    public static String getSha1(String str) {

        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));
            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }

}
