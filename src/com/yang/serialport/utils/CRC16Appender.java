package com.yang.serialport.utils;

/**
 *
 * @author Sayming-Hong
 * @date 2023-12-14
 */
public class CRC16Appender {
    
    /**
     * 追加CRC16 校验码
     * @param sourceData
     * @return
     */
    public static byte[] to(byte[] sourceData) {
     // 计算 Modbus CRC16 校验码
        int modbusCrc16 = calculateModbusCRC16(sourceData);

        // 获取 Modbus CRC16 校验码的高位和低位
        byte crcLow = (byte) (modbusCrc16 & 0xFF);          // 低位
        byte crcHigh = (byte) ((modbusCrc16 >> 8) & 0xFF); // 高位

        // 源数据后追加 Modbus CRC16 校验码
        byte[] dataWithCRC = new byte[sourceData.length + 2];
        System.arraycopy(sourceData, 0, dataWithCRC, 0, sourceData.length);
        dataWithCRC[sourceData.length] = crcLow;
        dataWithCRC[sourceData.length + 1] = crcHigh;
        
        return dataWithCRC;
    }
    
    public static int calculateModbusCRC16(byte[] data) {
        int crc = 0xFFFF; // 初始值
        int polynomial = 0xA001; // Modbus CRC16 多项式

        for (byte b : data) {
            crc ^= (b & 0xFF); // 保证 byte 转为无符号整数
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >> 1) ^ polynomial;
                } else {
                    crc >>= 1;
                }
            }
        }

        return crc & 0xFFFF;
    }
}
