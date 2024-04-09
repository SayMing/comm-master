package com.yang.serialport.utils;

import java.nio.ByteBuffer;
import java.util.Locale;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.HexUtil;

/**
 * Byte转换工具
 * 
 * @author yangle
 */
public class ByteUtils {

    /**
     * 讲十六进制 转utf8字符串
     * @param hexString
     * @return
     */
    public static String hexStringToUTF8(String hexString) {
        // 将十六进制字符串转换为字节数组
        byte[] bytes = hexStringToByteArray(hexString);

        // 将字节数组转换为 UTF-8 字符串
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] byteArray = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }

        return byteArray;
    }
    
	/**
	 * 十六进制字符串转byte[]
	 * 
	 * @param hex
	 *            十六进制字符串
	 * @return byte[]
	 */
	public static byte[] hexStr2Byte(String hex) {
		if (hex == null) {
			return new byte[] {};
		}

		// 奇数位补0
		if (hex.length() % 2 != 0) {
			hex = "0" + hex;
		}

		int length = hex.length();
		ByteBuffer buffer = ByteBuffer.allocate(length / 2);
		for (int i = 0; i < length; i++) {
			String hexStr = hex.charAt(i) + "";
			i++;
			hexStr += hex.charAt(i);
			byte b = (byte) Integer.parseInt(hexStr, 16);
			buffer.put(b);
		}
		return buffer.array();
	}

	/**
	 * byte[]转十六进制字符串
	 * 
	 * @param array
	 *            byte[]
	 * @return 十六进制字符串
	 */
	public static String byteArrayToHexString(byte[] array) {
		if (array == null) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			buffer.append(byteToHex(array[i]));
		}
		return buffer.toString();
	}

	/**
	 * byte转十六进制字符
	 * 
	 * @param b
	 *            byte
	 * @return 十六进制字符
	 */
	public static String byteToHex(byte b) {
		String hex = Integer.toHexString(b & 0xFF);
		if (hex.length() == 1) {
			hex = '0' + hex;
		}
		return hex.toUpperCase(Locale.getDefault());
	}
	
	/**
	 * 计算给定十六进制数组之和
	 * @param hexs
	 * @return
	 */
	public static String hexSum(String[] hexs) {
	    int sum = 0;
	    for(String hex : hexs) {
	        if(hex != null && !hex.isEmpty()) {
	            sum += Integer.parseInt(hex, 16);
	        }
	    }
	    return Integer.toHexString(sum);
	}
	
	/**
	 * 获取十六进制低二位
	 * @param hex
	 * @return
	 */
	public static String hex2Low(String hex) {
	    int length = hex.length();
	    if(length > 2) {
	        return hex.substring(length - 2, length).toUpperCase();
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
	 * 整型转十六进制(小端形式)，如：00 01 >>> 01 00
	 * @param i
	 * @return
	 */
	public static String int2HexSmallEnd(int i) {
	    return ByteUtils.hex2LowSort(ByteUtils.int2Hex(i));
	}
	
	public static String formatHexString(String data) {
	    String regex = "(.{2})";
	    return data.replaceAll(regex, "$1 ");
	}
	
	public static String[] formatHexStrings(String data) {
        String regex = "(.{2})";
        return data.replaceAll(regex, "$1 ").split(" ");
	}
	
	public static void main(String[] args) {
	    Long[] times = new Long[] {
	            1703505836L,
	            1703505896L
	    };
        for (Long time : times) {
            System.out.println(ByteUtils.formatHexString(ByteUtils.hex2LowSort(time.toHexString(time))));
        }
        System.out.println(HexUtil.decodeHexStr("3123ced2cac7cffbcfa2313233", CharsetUtil.CHARSET_GBK));
        
        String driverNameHexStr = hex2LowSort("20202020FDC8C5D5");
        System.out.println(HexUtil.decodeHexStr(driverNameHexStr, CharsetUtil.CHARSET_GBK).trim());
        
        String driverNoHexStr = hex2LowSort("64000000");
        System.out.println(HexUtil.hexToInt(driverNoHexStr));
        
        String beaconLocationStr = hex2LowSort("202020202020202020202041F3BFB0D7");
        System.out.println(HexUtil.decodeHexStr(beaconLocationStr, CharsetUtil.CHARSET_GBK).trim());
        
    }
}
