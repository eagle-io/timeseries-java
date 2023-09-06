package io.eagle.util;


public class BitBuddy {

    public static final int LEFT_MASK = 0xffff0000;
    public static final int RIGHT_MASK = 0x0000ffff;

    public static int getLeft(int value) {
        return value >>> 16;
    }

    public static int getRight(int value) {
        return value & RIGHT_MASK;
    }

    public static boolean getBit(int value, int index) {
        return (value & 1 << index) != 0;
    }

    public static int setLeft(int value, int left) {
        return (left << 16) | (value & RIGHT_MASK);
    }

    public static int setRight(int value, int right) {
        return (value & LEFT_MASK) | right;
    }

    public static int setBoth(int value, int left, int right) {
        return setRight(setLeft(value, left), right);
    }

    public static int setBit(int value, int index) {
        return value |= 1 << index;
    }

    public static int clearBit(int value, int index) {
        return value &= ~(1 << index);
    }

    public static String toString(int value) {
        String bitString = String.format("%1$32s", Integer.toBinaryString(value)).replace(" ", "0");
        String str = "";

        for (int i = 0; i < 32; i += 8) {
            str += bitString.substring(i, i + 8) + " ";
        }

        return str;
    }

}
