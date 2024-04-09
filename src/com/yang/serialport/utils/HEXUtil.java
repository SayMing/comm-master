package com.yang.serialport.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 十六进制工具
 * 
 * @author Admin
 *
 */
public class HEXUtil {

    public static String HEXES = "0123456789ABCDEF";

    /**
     * 十六进制字符串转十进制，会先把小端转成大端格式再转换十进制，例如传入小端0100，会转成0001，输出结果1
     * 
     * @param content
     * @return
     */
    public static int covert(String content) {
        String lowContent = HEXUtil.toLowHex(content);
        int number = 0;
        String[] HighLetter = { "A", "B", "C", "D", "E", "F" };
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i <= 9; i++) {
            map.put(i + "", i);
        }
        for (int j = 10; j < HighLetter.length + 10; j++) {
            map.put(HighLetter[j - 10], j);
        }
        String[] str = new String[lowContent.length()];
        for (int i = 0; i < str.length; i++) {
            str[i] = lowContent.substring(i, i + 1);
        }
        for (int i = 0; i < str.length; i++) {
            number += map.get(str[i]) * Math.pow(16, str.length - 1 - i);
        }
        return number;
    }

    /**
     * 字节数组转十六进制字符串
     * 
     * @param hexs
     * @return
     */
    public static String covert2HexString(byte[] hexs) {
        final StringBuilder hex = new StringBuilder(2 * hexs.length);
        for (int i = 0; i < hexs.length; i++) {
            byte b = hexs[i];
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    /**
     * 字节数组转十六进制字符数组
     * 
     * @param hexs
     * @return
     */
    public static char[] covert2HexChar(byte[] hexs) {
        char[] data = new char[2 * hexs.length];
        for (int i = 0; i < hexs.length; i++) {
            byte b = hexs[i];
            data[i * 2] = HEXES.charAt((b & 0xF0) >> 4);
            data[i * 2 + 1] = HEXES.charAt((b & 0x0F));
        }
        return data;
    }
    
    /**
     * 十六进制字符串转字节数组
     * 
     * @param src
     * @return
     */
    public static byte[] hexString2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = (byte) Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

    /**
     * 十六进制字符串转小端格式
     * EC 5E => 5E EC
     * @param hex
     * @return
     */
    public static String toLowHex(String hex) {
        if(hex.length() == 2) return hex;
        StringBuffer sb = new StringBuffer();
        char[] chars = hex.toCharArray();
        int i = chars.length - 1;
        for(; i>=0; i--) {
            if(i % 2==0) {
                sb.append(chars[i]).append(chars[i+1]);
            }
        }
        return sb.toString();
    }
    
    /**
     * 整型转十六进制
     * @param i
     * @return
     */
    public static String int2Hex(int i) {
        String hex = Integer.toHexString(i);
        // 检查长度，如果不足两位，在前面补零
        if(hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        return hex.toUpperCase();
    }

    /**
     * 将十六进制转低位到高位排序。如：657AED6A >>> 6AED7A65
     * @param input
     * @return
     */
    public static String hex2LowSort(String input) {
        StringBuilder reversed = new StringBuilder();
        
        // 从后向前每两个字符读取，并添加到结果字符串
        for (int i = input.length() - 2; i >= 0; i -= 2) {
            reversed.append(input.substring(i, i + 2));
        }
        
        return reversed.toString();
    }
    /**
     * 整型转十六进制(小端形式)，如：00 01 >>> 01 00
     * @param i
     * @return
     */
    public static String int2HexSmallEnd(int i) {
        return hex2LowSort(int2Hex(i));
    }

    /**
     * 后补0
     * @param input
     * @param targetLength
     * @return
     */
    public static String padZero(String input, int targetLength) {
        if (input == null) {
            return null;
        }

        int currentLength = input.length();
        if (currentLength >= targetLength) {
            return input;
        }

        int numberOfZerosToPad = targetLength - currentLength;
        StringBuilder paddedString = new StringBuilder(input);
        for (int i = 0; i < numberOfZerosToPad; i++) {
            paddedString.append('0');
        }

        return paddedString.toString();
    }
    
    /**
     * 后补空格
     * @param input
     * @param targetLength 目标字节长度
     * @return
     */
    public static String padSpace(String input, int targetLength) {
        if (input == null) {
            return null;
        }
        StringBuilder paddedString = new StringBuilder(input);
        int temp = targetLength - (input.length() / 2);
        for(;temp > 0; temp--) {// 补空格
            paddedString.append("20");
        }
        return paddedString.toString();
    }

    public static String[] formatHexStrings(String data) {
        String regex = "(.{2})";
        return data.replaceAll(regex, "$1 ").split(" ");
    }
}
