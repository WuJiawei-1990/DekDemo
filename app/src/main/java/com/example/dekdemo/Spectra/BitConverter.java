package com.example.dekdemo.Spectra;

/**
 * Created by ChenGang on 2018/12/3.
 */

public class BitConverter {
    public static short ToInt16(byte[] bytes, int offset) {
        short result = (short) ((int)bytes[offset]&0xff);
        result |= ((int)bytes[offset+1]&0xff) << 8;
        return (short) (result & 0xffff);
    }

    public static int ToUInt16(byte[] bytes, int offset) {
        int result = (int)bytes[offset]&0xff;
        result |= ((int)bytes[offset+1]&0xff) << 8;
        return result & 0xffff;
    }

    public static int ToInt32(byte[] bytes, int offset) {
        int result = (int)bytes[offset]&0xff;
        result |= ((int)bytes[offset+1]&0xff) << 8;
        result |= ((int)bytes[offset+2]&0xff) << 16;
        result |= ((int)bytes[offset+3]&0xff) << 24;
        return result;
    }

    public static long ToUInt32(byte[] bytes, int offset) {
        long result = (int)bytes[offset]&0xff;
        result |= ((int)bytes[offset+1]&0xff) << 8;
        result |= ((int)bytes[offset+2]&0xff) << 16;
        result |= ((int)bytes[offset+3]&0xff) << 24;
        return result & 0xFFFFFFFFL;
    }

    public static long ToInt64(byte[] bytes,int offset) {
        long values = 0;
        for (int i = 0; i < 8; i++) {
            values |= (long)(bytes[offset+i] & 0xff) << (i*8);
        }
        return values;
    }

    public static float ToFloat(byte[] bytes, int index) {
        return Float.intBitsToFloat(ToInt32(bytes, index));
    }

    public static byte[] GetBytes(short value) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (value & 0xff);
        bytes[1] = (byte) ((value & 0xff00) >> 8);
        return bytes;
    }

    public static byte[] GetBytes(int value) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((value)&0xff); //最低位
        bytes[1] = (byte) ((value >> 8)&0xff);
        bytes[2] = (byte) ((value >> 16)&0xff);
        bytes[3] = (byte) ((value >>> 24)); //最高位，无符号右移
        return bytes;
    }

    public static byte[] GetBytes(long values) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i]=(byte)((values>>(i*8))&0xff);
        }
        return bytes;
    }

    public static byte[] GetBytes(float value) {
        return GetBytes(Float.floatToIntBits(value));
    }

    public static byte[] GetBytes(double value) {
        long temp = Double.doubleToLongBits(value);
        return GetBytes(temp);
    }
}
