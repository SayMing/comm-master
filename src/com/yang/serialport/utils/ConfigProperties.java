package com.yang.serialport.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import cn.hutool.core.util.StrUtil;

/**
 *
 * @author Sayming-Hong
 * @date 2023-12-10
 */
public class ConfigProperties {
    
    private static Properties properties;
    /**
     * 配置文件路径
     */
    private static String FILE_PATH = ".\\config.properties";
    
    private static String OBU_CODE_HEX;

    static {
        properties = new Properties();
        
        // 读取配置文件
        try (InputStream input = new FileInputStream(FILE_PATH)) {
            // 加载配置文件
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getCommPort() {
        String value = properties.getProperty("comm.name");
        System.out.println("getCommPort --> " + value);
        return value;
    }
    
    public static String getDbDataPath() {
        String value = properties.getProperty("db.data.path");
        System.out.println("getDbDataPath --> " + value);
        return value;
    }
    
    /**
     * 车载机编号
     * @return
     */
    public static Integer getObuCode() {
        String value = properties.getProperty("obu.code");
        System.out.println("getObuCode --> " + value);
        return Integer.parseInt(value);
    }
    
    /**
     * 车载机编号，十六进制，完整4位长度编号。如十进制编号1，返回十六进制为0001
     * @return
     */
    public static String getObuCodeHex() {
        if(OBU_CODE_HEX == null) {
            OBU_CODE_HEX = StrUtil.fillBefore(ByteUtils.int2Hex(getObuCode()), '0', 4);
        }
        return OBU_CODE_HEX;
    }
}
