package com.rthtech.bledemo;

import java.security.MessageDigest;

public class Util {
    private static String sExternalLogFilePath = "";
    public final static String DATA_DEVICE_ADDR = "data_device_addr";
    public final static String DATA_RESULT = "data_result";
    public final static String DATA_TITLE = "data_title";
    public final static String DATA_BOOL_LABEL = "data_bool_label";
    public final static String DATA_BOOL = "data_bool";
    public final static String DATA_BYTE1_LABEL = "data_byte1_label";
    public final static String DATA_BYTE1 = "data_byte1";
    public final static String DATA_BYTE2_LABEL = "data_byte2_label";
    public final static String DATA_BYTE2 = "data_byte2";
    public final static String DATA_BYTES1_LABEL = "data_bytes1_label";
    public final static String DATA_BYTES1 = "data_bytes1";
    public final static String DATA_CODE = "data_code";
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

    static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (byte aSrc : src) {
            buffer[0] = Character.forDigit((aSrc >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(aSrc & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
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

    private static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (byte b : bs) {
            bit = (b & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = b & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }


    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = ( byte ) (n & 0xff);
        }
        return new String(bytes);
    }


    private static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        StringBuffer sb;
        while (strLen < strLength) {
            sb = new StringBuffer();
            sb.append("0").append(str);
            str = sb.toString();
            strLen = str.length();
        }
        return str;
    }


    public static String hexAddSum(String number) {
        int m = 0;
        String sum;
        for (int i = 0; i < number.length() / 2; i++) {
            m = m + Integer.parseInt(number.substring(i * 2, i * 2 + 2), 16);
        }
        sum = addZeroForNum(Integer.toHexString(m), 2);
        if (sum.length() > 2) {
            sum = sum.substring(sum.length() - 2, sum.length());
        }
        return sum;
    }


    public static String crc16(String gprsstr) {
        try {
            int crc;
            int strlength, r;
            byte sbit;
            int tc;
            strlength = gprsstr.length();
            byte[] data = gprsstr.getBytes();
            crc = 0x0000FFFF;
            for (int i = 0; i < strlength; i++) {
                tc = crc >>> 8;
                crc = tc ^ data[i];
                for (r = 0; r < 8; r++) {
                    sbit = ( byte ) (crc & 0x00000001);
                    crc >>>= 1;
                    if (sbit != 0)
                        crc ^= 0x0000A001;
                }
            }
            return Integer.toHexString(crc);
        } catch (Exception ex) {
            return "";
        }
    }

    public static String Make_CRC(byte[] data) {
        byte[] buf = new byte[data.length];
        System.arraycopy(data, 0, buf, 0, data.length);
        int len = buf.length;
        int crc = 0xFFFF;
        for (byte aBuf : buf) {
            if (aBuf < 0) {
                crc ^= ( int ) aBuf + 256;

            } else {
                crc ^= ( int ) aBuf;
            }
            for (int i = 8; i != 0; i--) {
                if ((crc & 0x0001) != 0) {
                    crc >>= 1;
                    crc ^= 0xA001;
                } else

                    crc >>= 1;
            }
        }
        String c = Integer.toHexString(crc);
        if (c.length() == 4) {
            c = c.substring(2, 4) + c.substring(0, 2);
        } else if (c.length() == 3) {
            c = "0" + c;
            c = c.substring(2, 4) + c.substring(0, 2);
        } else if (c.length() == 2) {
            c = "0" + c.substring(1, 2) + "0" + c.substring(0, 1);
        }
        return c;
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

    public static String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        for (int i = 0; i < hex.length() - 1; i += 2) {

            // grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            // convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            // convert the decimal to character
            sb.append(( char ) decimal);

            StringBuilder append = temp.append(decimal);
        }
        // System.out.println(sb.toString());
        return sb.toString();
    }

}
