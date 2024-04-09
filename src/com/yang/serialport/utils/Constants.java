package com.yang.serialport.utils;

import com.yang.serialport.model.CurrentBeacon;

/**
 *
 * @author Sayming-Hong
 * @date 2023-12-08
 */
public class Constants {
    public static CurrentBeacon CURRENT_BEACON = new CurrentBeacon();
    
    public static boolean isDebug() {
        return true;
    }
    
    /**
     * 矿车状态
     *
     * @author Sayming-Hong
     * @date 2023-12-28
     */
    public static class CarStatus{
        /**
         * 去装矿
         */
        public static final String GO_TO_INSTALL = "去装矿";
        /**
         * 到达装矿点
         */
        public static final String IN_INSTALL = "到达装矿点";
        /**
         * 装矿点错误
         */
        public static final String ERROR_INSTALL = "装矿点错误";
        /**
         * 去卸矿
         */
        public static final String GO_TO_UNLOAD = "去卸矿";
        /**
         * 到达卸矿点
         */
        public static final String IN_UNLOAD = "到达卸矿点";
    }

    /**
     * 信标类型
     *
     * @author Sayming-Hong
     * @date 2023-12-27
     */
    public static class BeaconType{
        /**
         * 装矿溜井
         */
        public static final Integer INSTALL = 1;
        /**
         * 卸矿溜井
         */
        public static final Integer UNLOAD = 2;
        /**
         * 途径点
         */
        public static final Integer ROAD = 3;
    }
    
    public static class BeaconLogStatus{
        /**
         * 未发送
         */
        public static Integer NOT_SEND = 0;
        
        /**
         * 已发送
         */
        public static Integer SEND = 1;
        
        /**
         * 等待响应
         */
        public static Integer WAIT_RESPONSE = 2;
    }
}
