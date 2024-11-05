package com.yang.serialport.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yang.serialport.model.Beacon;
import com.yang.serialport.model.BeaconLog;
import com.yang.serialport.model.Msg;
import com.yang.serialport.model.WorkLineInfo;
import com.yang.serialport.model.WorkTimeInfo;
import com.yang.serialport.ui.CarMainFrame;
import com.yang.serialport.utils.ByteUtils;
import com.yang.serialport.utils.CRC16;
import com.yang.serialport.utils.CRC16Appender;
import com.yang.serialport.utils.ConfigProperties;
import com.yang.serialport.utils.Constants;
import com.yang.serialport.utils.DB;
import com.yang.serialport.utils.ExpandSplitIter;
import com.yang.serialport.utils.HEXUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 *
 * @author Sayming-Hong
 * @date 2023-12-08
 */
public class SerialPortHandler implements SerialPortEventListener{
    private static Logger logger = LoggerFactory.getLogger(SerialPortHandler.class);
    // 串口对象
    public SerialPort mSerialport = null;
    private CarMainFrame carMainFrame;
    private final String REG = "55AA([0-9]{2}|0A)";
    private final String NO_WORK_LINE = "无调度";
    private long lastMsgTime = 0L;
    private long healthCheckTime = 5000L;
    
    private InputStream inputStream;
//    private OutputStream outputStream;
    
    public SerialPortHandler(CarMainFrame carMainFrame) {
        this.carMainFrame = carMainFrame;
        
        CronUtil.schedule("*/5 * * * * *", new Task() {
            @Override
            public void execute() {
                try {
                    updateStatus();
                }catch (Exception e) {
                    logger.error("update status error:"+e.getMessage(), e);
                }
            }
        });

        CronUtil.schedule("*/1 * * * * *", new Task() {
            @Override
            public void execute() {
                try {
                    carMainFrame.nowTime();
                    healthCheck();
                }catch (Exception e) {
                    logger.error("update status error:"+e.getMessage(), e);
                }
            }
        });
        
        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    public void healthCheck() {
        if(lastMsgTime + healthCheckTime < System.currentTimeMillis()) {
            restartSerialPort();
            lastMsgTime = System.currentTimeMillis();
        }
    }
    
    Thread thread = null;
    /**
     * 重连串口
     */
    public void restartSerialPort() {
        try {
            if(mSerialport != null) {
                mSerialport.close();
                mSerialport = null;
                logger.info("serial port close.");
            }
        }catch (Exception e) {
            logger.error("restart serial port close error.", e);
        }
        
        if(thread == null || !thread.isAlive()) {
            thread = new Thread(()->{
                boolean restart = true;
                while (restart) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                    }
                    String error = openSerialPort();
                    logger.info("do restart port {} error msg:{}", thread.toString(), error);
                    if(error == null) {
                        restart = false;
                        sendBeaconInfoDataInit();
                    }
                }
            });
            thread.start();
        }
        
    }
    
    /***
     * 关闭串口
     */
    public void close() {
        CronUtil.stop();
        try {
            if(mSerialport != null) {
                mSerialport.removeEventListener();
                mSerialport.close();
                mSerialport = null;
            }
        }catch (Exception e) {
            logger.error("close serial port error.", e);
        }
    }
    
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        logger.info("serialPortEvent:{} mSerialport isCD:{}, isCTS:{}, isDSR:{}, isDTR:{}, isRI:{}, isRTS:{}, ", 
                serialPortEvent.getEventType(),
                mSerialport.isCD(), mSerialport.isCTS(), mSerialport.isDSR(), mSerialport.isDTR(), mSerialport.isRI(), mSerialport.isRTS());
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            lastMsgTime = System.currentTimeMillis();
            try {
                int availableBytes = inputStream.available();
                byte[] buffer = new byte[availableBytes];
                inputStream.read(buffer);
                
                // 以十六进制的形式接收数据
                String dataHexAll = ByteUtils.byteArrayToHexString(buffer);
                List<String> dataHexList = ExpandSplitIter.split(dataHexAll, REG, 100, true, true, true);
                dataHexList.forEach(dataHex->{
                    String[] dataHexStrs = ByteUtils.formatHexStrings(dataHex);
                    logger.info("receive dataHexStr:{}", StrUtil.concat(true, dataHexStrs));
                    
                    // 检查CRC16
                    String sumCrc16 = CRC16.signToHex(StrUtil.concat(true, ArrayUtil.sub(dataHexStrs, 0, dataHexStrs.length-2)));
                    String requestCrc16 = StrUtil.concat(true, ArrayUtil.sub(dataHexStrs, dataHexStrs.length-2, dataHexStrs.length));
                    if(!StrUtil.equals(sumCrc16, requestCrc16)){
                        logger.error("check crc16 failed. sumCrc16:{} requestCrc16:{}", sumCrc16, requestCrc16);
                        return;
                    }
                    
                    // 解析 
                    String dataHexStr_3 = dataHexStrs[2];
                    
                    // 无线模块发送信标编号给车载机（相同的编号只记录1次，无线模块会发送多次）
                    // 55  AA  01  IDL  IDH  CRC16L  CRC16H  （共7字节）
                    // 55  AA  01  EF  03  54  25
                    if(StrUtil.equals("01", dataHexStr_3)) {
                        // 小端转换大端0100 >>> 0001
                        String obuCodeHex = dataHexStrs[4] + dataHexStrs[3];
                        int obuCode = HexUtil.hexToInt(obuCodeHex);
                        DB.getInstance().insertBeaconLog(obuCode, StrUtil.concat(true, dataHexStrs));
                        
                        Constants.CURRENT_BEACON.code = obuCode;
                        Constants.CURRENT_BEACON.time = System.currentTimeMillis();
                        updateStatus();
                    }else if(StrUtil.equals("02", dataHexStr_3)) {// 无线模块向车载机索取信标编号信息
                        //55  AA  02  00  00  CRC16L  CRC16H  （共7字节
                        // 需要向无线模块传送数据
                        List<BeaconLog> allNotSendData = DB.getInstance().selectNotSendBeaconLogList();
                        if(!allNotSendData.isEmpty()) {
                            // 分中情况，已经生成过index的，和未生成过index的。
                            Map<Integer, List<BeaconLog>> groupedData = allNotSendData.stream()
                                    .collect(Collectors.groupingBy(BeaconLog :: getRequestId));

                            // 打印分组后的数据
                            groupedData.forEach((requestId, group) -> {
                                // 分批上传
                                List<List<BeaconLog>> groups = CollUtil.split(group, 10);
                                groups.forEach(notSendDatas -> {
                                    // 当前上传数据批次号
                                    int currentIndex = requestId == 0 ? DB.getInstance().getCurrentRequestIdAndAdd() : requestId;
                                    sendBeaconLog(notSendDatas, currentIndex);
                                    DB.getInstance().updateSendBeaconLog(notSendDatas, currentIndex);
                                    try {
                                        Thread.sleep(266L);
                                    } catch (InterruptedException e) {
                                    }
                                });
                            });
                        }else {
                            sendEmpty();
                        }
                    }else if(StrUtil.equals("04", dataHexStr_3)) {
                        // 接收服务器响应 收到信标成功
                        receiveBeaconLogResult04(dataHexStrs);
                    }else if(StrUtil.equals("05", dataHexStr_3)) {
                        // 接收运矿信息
                        receiveWorkTimeInfoData05(dataHexStrs);
                    }else if(StrUtil.equals("06", dataHexStr_3)) {
                        // 接收运矿路线
                        receiveWorkLineInfoData06(dataHexStrs);
                    }else if(StrUtil.equals("07", dataHexStr_3)) {
                        // 接收通知信息
                        receiveNotifyData07(dataHexStrs);
                    }else if(StrUtil.equals("08", dataHexStr_3)) {
                        // 接收信标信息数据
                        receiveBeaconInfoData08(dataHexStrs);
                    }else if(StrUtil.equals("0A", dataHexStr_3)) {
                        // 小端转换大端0100 >>> 0001
                        String carCodeHexStr = dataHexStrs[4] + dataHexStrs[3];
                        int obuCode = HexUtil.hexToInt(carCodeHexStr);
                        if(ConfigProperties.getObuCode().intValue() == obuCode) {
                            receiveCleanBeaconInfo(dataHexStrs);
                        }
                    }
                    carMainFrame.tips();
                });
                
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                restartSerialPort();
            }
        }else if (serialPortEvent.getEventType() == SerialPortEvent.BI) { // 10.通讯中断
            logger.error("***通讯中断***");
            restartSerialPort();
        }
    }
    
    /**
     * 当车载机接收不到卸矿点的无线信标编号时，状态显示去装矿；
        当车载机收到装矿点的无线信标编号时，状态显示到达装矿点；
        当接收到装矿点的无线信标编号与去的不一致时，提示装矿点错误；
        当车载机接收不到装矿点的无线信标编号时，状态显示去卸矿；
        当车载机接收到卸矿点的无线信标编号时，状态显示到达卸矿点。
        辆到达卸矿点后，无线模块会向车载机要无线信标的数据，按照第2条协议。
        当车载机按第3条协议上报无线信标数据后，车载机会收到第4、5、6条协议的数据。
        本次运矿路线的显示的更新在车载机接收不到卸矿点的无线信标时才更新，否则显示表一次的到达卸矿点。
        接收不到无线信标编号的判定：连续5秒接收不到第1条协议的数据。
     * @param beaconLogInsert 是否新增信标日志过
     */
    public void updateStatus() {
        Beacon currentBeacon = DB.getInstance().selectBeaconByCode(Constants.CURRENT_BEACON.code);
        carMainFrame.setStatusLabel("");
        if(currentBeacon != null) {
            WorkLineInfo workLineInfo = DB.getInstance().selectWorkLineInfo();
            boolean offline = System.currentTimeMillis() - Constants.CURRENT_BEACON.time > 5000L;
            if(offline) {// 接收不到无线信标编号
                if(currentBeacon.getType() == Constants.BeaconType.INSTALL) {
                    // 当车载机接收不到装矿点的无线信标编号时，状态显示去卸矿；
                    carMainFrame.setStatusLabel(Constants.CarStatus.GO_TO_UNLOAD);
                }else if(currentBeacon.getType() == Constants.BeaconType.UNLOAD) {
                    // 当车载机接收不到卸矿点的无线信标编号时，状态显示去装矿；
                    carMainFrame.setStatusLabel(Constants.CarStatus.GO_TO_INSTALL);
                }
            }else {
                // 当车载机收到装矿点的无线信标编号时，状态显示到达装矿点；
                if(currentBeacon.getType() == Constants.BeaconType.INSTALL) { // 装矿点
                    carMainFrame.setStatusLabel(Constants.CarStatus.IN_INSTALL);
                    // 当接收到装矿点的无线信标编号与去的不一致时，提示装矿点错误；
                    if(workLineInfo != null && workLineInfo.getCurrentBeaconCodeStart() != null 
                            && workLineInfo.getCurrentBeaconCodeStart() != 0
                            && !ObjectUtil.equal(currentBeacon.getCode(), workLineInfo.getCurrentBeaconCodeStart())) {
                        carMainFrame.setStatusLabel(Constants.CarStatus.ERROR_INSTALL);
                    }
                }else if(currentBeacon.getType() == Constants.BeaconType.UNLOAD) { // 卸矿点
                    // 当车载机接收到卸矿点的无线信标编号时，状态显示到达卸矿点。
                    carMainFrame.setStatusLabel(Constants.CarStatus.IN_UNLOAD);
//                    updateCarSum();
                }
            }
        }
    }
    
//    /**
//     * 更新卸矿次数
//     */
//    public void updateCarSum() {
//        int carSum = getCarSum();
//        carMainFrame.setCarSumLabel(Integer.toString(carSum) + "车");
////        WorkTimeInfo workTimeInfo = DB.getInstance().getWorkTimeInfo();
////        if(workTimeInfo != null) {
////            if(carSum >= workTimeInfo.getTrainTripNum()) {
////                carMainFrame.cleanNextLineStartLabel();
////            }else {
////                carMainFrame.updateNextLineStartLabel();
////            }
////        }else {
////            carMainFrame.cleanNextLineStartLabel();
////        }
//    }
//    /**
//     * 获取卸矿次数
//     * @return
//     */
//    public int getCarSum() {
//        WorkTimeInfo workTimeInfo = DB.getInstance().getWorkTimeInfo();
//        return workTimeInfo.getTrainTripNum();
////        Date date;
////        if(workTimeInfo == null) {
////            date = CarMainFrame.RUN_DATE;
////        }else {
////            date = DateUtil.parse(DateUtil.today() + " " + workTimeInfo.getWorkTimeStart() + ":00");
////        }
////        return DB.getInstance().countUnload(date);
//    }
    
    /**
     * 发送空包
     */
    public void sendEmpty() {
        StringBuilder sendBodyBuilder = new StringBuilder();
        sendBodyBuilder.append("55AA03")
        .append(ByteUtils.int2HexSmallEnd(0))
        .append(ByteUtils.hex2LowSort(ConfigProperties.getObuCodeHex()))
        .append(ByteUtils.int2HexSmallEnd(0));
        SerialPortManager.sendToPort(carMainFrame, mSerialport, CRC16Appender.to(ByteUtils.hexStr2Byte(sendBodyBuilder.toString())));
    }
    
    /**
     * 车载机发送信标编号信息给无线模块  
     * 55  AA  03  XH  IDL IDH  NUM  XX … XX  CRC16L  CRC16H   （共 NUM*6+9个字节）  
     * XH表示数据序号，车载机每次上报相同数据时，用同一个序号，防止服务器重复记录；  
     * NUM表示信标个数，后面依次排列信标信息，每个信标信息6个字节，前两字为信标编号，后4字节为UNIX时间。  
     * 该信息的XH后面两个00 00修改为车载机编号后，重新校验，其他数据不变，直接转发给服务器，服务器收到应答车载机。  
     */
    public void sendBeaconLog(List<BeaconLog> notSendDatas, int requestId) {
        logger.info("not send datas:{} requestId:{} obu code:{}", JSONUtil.toJsonStr(notSendDatas), requestId, ConfigProperties.getObuCodeHex());
        StringBuilder sendBodyBuilder = new StringBuilder();
        sendBodyBuilder.append("55AA03")
        .append(ByteUtils.int2HexSmallEnd(requestId))
        .append(ByteUtils.hex2LowSort(ConfigProperties.getObuCodeHex()))
        .append(ByteUtils.int2HexSmallEnd(notSendDatas.size()));
        
        int i = 0;
        BeaconLog item;
        for (; i < notSendDatas.size(); i++) {
            item = notSendDatas.get(i);
            sendBodyBuilder.append(ByteUtils.int2HexSmallEnd(item.getCode()))
            .append(ByteUtils.int2HexSmallEnd(item.getCreateTime()));
        }
        
        SerialPortManager.sendToPort(carMainFrame, mSerialport, CRC16Appender.to(ByteUtils.hexStr2Byte(sendBodyBuilder.toString())));
    }

    /**
     * 打开串口
     * @param commName 串口名称
     */
    public String openSerialPort() {
        String errorMsg = null;
        String commName = ConfigProperties.getCommPort();
        if(StrUtil.isBlank(commName)) {
            commName = "COM2";
        }
        logger.info("系统链接串口号：" + commName);
        // 检查串口名称是否获取正确
        if (commName == null || commName.equals("")) {
            logger.error("没有搜索到有效串口！");
            errorMsg = "没有搜索到有效串口！";
        } else {
            try {
                mSerialport = SerialPortManager.openPort(commName, 115200);
                if(mSerialport == null) {
                    logger.error("启动串口号[{}]失败。", commName);
                    errorMsg = "启动串口号["+commName+"]失败。";
                }else {
                    mSerialport.addEventListener(this);
                    mSerialport.notifyOnDataAvailable(true);
                    mSerialport.notifyOnBreakInterrupt(true);
                    inputStream = mSerialport.getInputStream();
                }
            } catch (TooManyListenersException | PortInUseException e) {
                logger.error("串口已被占用【{}】！", commName);
                errorMsg = "串口已被占用【"+commName+"】";
            } catch (Exception e) {
                logger.error("串口【{}】未找到！", commName);
                errorMsg = "串口【" + commName + "】未找到！";
            }
        }
        return errorMsg;
    }
    
    /**
     * 接收信标信息数据
     * 55   AA  08  IDL IDH  NUM  XX XX … XX CRC16L  CRC16H  （共NUM*19 + 8字节）
 NUM表示本次更新无线信标的个数；
每个无线信标编号用19个字节表示，前2个字节为无线信标编号（低字节在前）；后一个字节为信标的属性，1对应为装矿点，2对应为卸矿点，3对应为其他位置；后面16个字节对应无线信标编号对应的安装位置的汉字编码，最多可以8个汉字，或者16个字符，不足的用空格填充。
下行发送的时候，无线信标编号从小到大排列，依次发送，单包数据不超过110字节，超过了就分包多次发送。对于存储的无线信标编号就更新进去，对于之前没有存储的，就插入进去。 
     */
    public void receiveBeaconInfoData08(String[] dataHexStrs) {
        String obuCode = dataHexStrs[3] + dataHexStrs[4];
        String localObuCode = ByteUtils.hex2LowSort(ConfigProperties.getObuCodeHex());
        if(!StrUtil.equals(localObuCode, obuCode)) {
            logger.error("receiveBeaconInfoData08 error: receive obucode={} not equals local obucode={}", obuCode, localObuCode);
            return;
        }
        
        for(int i=6; i<dataHexStrs.length-2;) {
            try {
                Integer code = HEXUtil.covert(dataHexStrs[i++] + dataHexStrs[i++]);
                Integer type = HEXUtil.covert(dataHexStrs[i++]);
                String location = HexUtil.decodeHexStr(HEXUtil.hex2LowSort(StrUtil.concat(true, Arrays.copyOfRange(dataHexStrs, i, i+16))), CharsetUtil.CHARSET_GBK);
                i= i+ 16;
                logger.info("receive beacon info code:{} type:{} location:{}", code, type, location);
                DB.getInstance().saveBeacon(code, location.trim(), type);
            }catch (Exception e) {
                logger.error("receiveBeaconInfoData08 error:"+e.getMessage(), e);
            }
        }
        
        // 回调同步信标列表结果
        sendBeaconInfoDataResult09(dataHexStrs);
        
    }
    
    /**
     * 发送同步信标列表结果
     * @param dataHexStrs
     */
    public void sendBeaconInfoDataResult09(String[] dataHexStrs) {
        String checkSign = dataHexStrs[dataHexStrs.length-2]+dataHexStrs[dataHexStrs.length-1];
        //55 AA 09 IDL IDH  SUM  CRC16-XBL  CRC16-XBH  XX CRC16L  CRC16H   （共10个字节）
        StringBuilder sendBodyBuilder = new StringBuilder();
        sendBodyBuilder.append("55AA09")
        .append(ByteUtils.hex2LowSort(ConfigProperties.getObuCodeHex()))
        .append(dataHexStrs[5]).append(checkSign);
        logger.info("send sync beacon list result body:{}", sendBodyBuilder.toString());
        
        SerialPortManager.sendToPort(carMainFrame, mSerialport, CRC16Appender.to(ByteUtils.hexStr2Byte(sendBodyBuilder.toString())));
    }

    /**
     * 发送心跳包
     */
    public void sendHealthCheck() {
        StringBuilder sendBodyBuilder = new StringBuilder();
        sendBodyBuilder.append("55AABB");
        SerialPortManager.sendToPort(carMainFrame, mSerialport, CRC16Appender.to(ByteUtils.hexStr2Byte(sendBodyBuilder.toString())));
    }
    
    /**
     * 发送初始化信标信息
     */
    public void sendBeaconInfoDataInit() {
        //55 AA 09 IDL IDH  SUM  CRC16-XBL  CRC16-XBH  XX CRC16L  CRC16H   （共10个字节）
        StringBuilder sendBodyBuilder = new StringBuilder();
        sendBodyBuilder.append("55AA09")
        .append(ByteUtils.hex2LowSort(ConfigProperties.getObuCodeHex()))
        .append("000000");
        logger.info("send beacon info data init:{}", sendBodyBuilder.toString());
        
        SerialPortManager.sendToPort(carMainFrame, mSerialport, CRC16Appender.to(ByteUtils.hexStr2Byte(sendBodyBuilder.toString())));
    }
    
    /**
     * 服务器响应接收信标成功
     * 55   AA  04  XH  IDL IDH  CRC16L  CRC16H  （共8字节）
      IDL IDH 为车载机编号的低字节和高字节。
     * @param dataHexStrs
     */
    public void receiveBeaconLogResult04(String[] dataHexStrs) {
        Integer index = HEXUtil.covert(dataHexStrs[3]);
        DB.getInstance().updateBeaconLogIsSend(index);
        logger.info("receiveBeaconLogResult04 set send status over:{}", index);
    }
    
    /**
     * 服务器发送运矿信息给车载机数据格式
        55   AA  05  IDL IDH  X0 X1 … X17 CRC16L  CRC16H  （共25字节）
          IDL IDH 为车载机编号的低字节和高字节；
          X0表示本班运矿次数（每个班最多不会操作255次，所以一个字节表示）；
        X1表示班次，1对应早班；2对应中班；3对应晚班；
        X2对应本班开始时间的小时数，X3对应本班开始时间的分钟数；
        X4对应本班结束时间的小时数，X5对应本班结束时间的分钟数；
        （工作时间车载机自动累计计数，不用服务器统计）
        X6~X13 共8个字节对应驾驶员的姓名的汉字编码，最多4个字，不足用空格填充；
        X14~X17对应驾驶员工号的数字，小端格式，低字节在前。
        注意：当车载机接收到上报的无线信标信息后，发送该信息到对应IP，该信息由服务器发起，无线模块直接转发给车载机，车载机负责校验和执行。服务器可以间隔1秒连发3次，确保数据成功。
     */
    public void receiveWorkTimeInfoData05(String[] dataHexStrs) {
        try {
            int index = 5;
            Integer trainTripNum = HEXUtil.covert(dataHexStrs[index++]);
            Integer workTimeType = HEXUtil.covert(dataHexStrs[index++]);
            String workTimeStart = StrUtil.fillBefore(Integer.toString(HEXUtil.covert(dataHexStrs[index++])), '0', 2) + ":" + StrUtil.fillBefore(Integer.toString(HEXUtil.covert(dataHexStrs[index++])), '0', 2);
            String workTimeEnd = StrUtil.fillBefore(Integer.toString(HEXUtil.covert(dataHexStrs[index++])), '0', 2) + ":" + StrUtil.fillBefore(Integer.toString(HEXUtil.covert(dataHexStrs[index++])), '0', 2);
            String driverName = HexUtil.decodeHexStr(HEXUtil.hex2LowSort(StrUtil.concat(true, Arrays.copyOfRange(dataHexStrs, index, index+8))), CharsetUtil.CHARSET_GBK);
            if(driverName != null) {
                driverName = driverName.trim();
            }
            index+=8;
            String driverCode = Integer.toString(HEXUtil.covert(StrUtil.concat(true, Arrays.copyOfRange(dataHexStrs, index, index+4))));
            if(StrUtil.equals("0", driverCode)) {
                driverCode = "";
            }
            WorkTimeInfo workTimeInfo = new WorkTimeInfo();
            workTimeInfo.setDriverCode(driverCode);
            workTimeInfo.setDriverName(driverName);
            workTimeInfo.setId(1L);
            workTimeInfo.setTrainTripNum(trainTripNum);
            workTimeInfo.setWorkTimeEnd(workTimeEnd);
            workTimeInfo.setWorkTimeStart(workTimeStart);
            workTimeInfo.setWorkTimeType(workTimeType);
            DB.getInstance().saveWorkTimeInfo(workTimeInfo);
            logger.info("receiveWorkTimeInfoData05 save workTimeInfo over:{}", workTimeInfo);
            
            String classeString = "";
            if(workTimeType == 1) { // 1对应早班；2对应中班；3对应晚班；
                classeString = "早班("+workTimeStart+" - " + workTimeEnd +")";
            }else if(workTimeType == 2) {
                classeString = "中班("+workTimeStart+" - " + workTimeEnd +")";
            }else if(workTimeType == 3) {
                classeString = "晚班("+workTimeStart+" - " + workTimeEnd +")";
            }else {
                classeString = "";
            }
            
            carMainFrame.setDriverNameLabel(driverName);
            carMainFrame.setClassesLabel(classeString);
            carMainFrame.setJobNumberLabel(driverCode);
            carMainFrame.setCarSumLabel(Integer.toString(trainTripNum) + "车");
//            updateCarSum();
        }catch (Exception e) {
            logger.error("receiveWorkTimeInfoData05 error:"+e.getMessage(), e);
        }
    }
    
    /**
     * 服务器发送运矿路线给车载机数据格式
        55   AA  06  IDL IDH  X0 X1 … X7 CRC16L  CRC16H  （共15字节）
        IDL IDH 为车载机编号的低字节和高字节；
        X0 X1表示本次运矿路线的装矿点无线信标编号；
        X2 X3 表示本次运矿路线的卸矿点无线信标编号；
        X4 X5表示下次运矿路线的装矿点无线信标编号；
        X6 X7 表示下次运矿路线的卸矿点无线信标编号；
        注意：无线信标编号都采用小端格式，低字节在前；
        当车载机接收到上报的无线信标信息后，发送该信息到对应IP，该信息由服务器发起，无线模块直接转发给车载机，车载机负责校验和执行。
        第5条、第6条的协议的数据可以依次发送。服务器可以间隔1秒连发3次，确保数据成功。
        当车载机接收不到卸矿点的无线信标编号时，状态显示去装矿；
        当车载机收到装矿点的无线信标编号时，状态显示到达装矿点；
        当接收到装矿点的无线信标编号与去的不一致时，提示装矿点错误；
        当车载机接收不到装矿点的无线信标编号时，状态显示去卸矿；
        当车载机接收到卸矿点的无线信标编号时，状态显示到达卸矿点。
        辆到达卸矿点后，无线模块会向车载机要无线信标的数据，按照第2条协议。
        当车载机按第3条协议上报无线信标数据后，车载机会收到第4、5、6条协议的数据。
        本次运矿路线的显示的更新在车载机接收不到卸矿点的无线信标时才更新，否则显示表一次的到达卸矿点。
        接收不到无线信标编号的判定：连续5秒接收不到第1条协议的数据。
     * @param dataHexStrs
     */
    public void receiveWorkLineInfoData06(String[] dataHexStrs) {
        try {
            int index = 5;
            Integer currentBeaconCodeStart = HEXUtil.covert(dataHexStrs[index++]+dataHexStrs[index++]);
            Integer currentBeaconCodeEnd = HEXUtil.covert(dataHexStrs[index++]+dataHexStrs[index++]);
            Integer nextBeaconCodeStart = HEXUtil.covert(dataHexStrs[index++]+dataHexStrs[index++]);
            Integer nextBeaconCodeEnd = HEXUtil.covert(dataHexStrs[index++]+dataHexStrs[index++]);
            WorkLineInfo workLineInfo = new WorkLineInfo();
            workLineInfo.setCurrentBeaconCodeStart(currentBeaconCodeStart);
            workLineInfo.setCurrentBeaconCodeEnd(currentBeaconCodeEnd);
            workLineInfo.setNextBeaconCodeStart(nextBeaconCodeStart);
            workLineInfo.setNextBeaconCodeEnd(nextBeaconCodeEnd);
            
            DB.getInstance().saveWorkLineInfo(workLineInfo);
            logger.info("receiveWorkLineInfoData06 save workLineInfo over:{}", workLineInfo);
            
            Beacon currentBeaconStart = DB.getInstance().selectBeaconByCode(currentBeaconCodeStart);
            if(currentBeaconStart != null) {
                workLineInfo.setCurrentBeaconStartLocation(currentBeaconStart.getLocation());
            }else {
                workLineInfo.setCurrentBeaconStartLocation(NO_WORK_LINE);
            }
            
            Beacon currentBeaconEnd = DB.getInstance().selectBeaconByCode(currentBeaconCodeEnd);
            if(currentBeaconEnd != null) {
                workLineInfo.setCurrentBeaconEndLocation(currentBeaconEnd.getLocation());
            }else {
                workLineInfo.setCurrentBeaconEndLocation(NO_WORK_LINE);
            }
            
            Beacon nextBeaconStart = DB.getInstance().selectBeaconByCode(nextBeaconCodeStart);
            if(nextBeaconStart != null) {
                workLineInfo.setNextBeaconStartLocation(nextBeaconStart.getLocation());
            }else {
                workLineInfo.setNextBeaconStartLocation(NO_WORK_LINE);
            }
            
            Beacon nextBeaconEnd = DB.getInstance().selectBeaconByCode(nextBeaconCodeEnd);
            if(nextBeaconEnd != null) {
                workLineInfo.setNextBeaconEndLocation(nextBeaconEnd.getLocation());
            }else {
                workLineInfo.setNextBeaconEndLocation(NO_WORK_LINE);
            }
            
            carMainFrame.setWorkLineInfo(workLineInfo);
        }catch (Exception e) {
            logger.error("receiveWorkLineInfoData06 error:"+ e.getMessage(), e);
        }
    }
    
    /**
     * 服务器发送通知信息给车载机数据格式
        55   AA  07  IDL IDH  X0 X1 … Xn CRC16L  CRC16H  （共  字节）
        X0…Xn 对应通知信息显示的汉字编号。中间传输为透传，即服务器发送什么，就直接转给车载机。
     * @param dataHexStrs
     */
    public void receiveNotifyData07(String[] dataHexStrs) {
        int index = 5;
        String body = StrUtil.concat(true, Arrays.copyOfRange(dataHexStrs, index, dataHexStrs.length-2));
        body = HexUtil.decodeHexStr(body, CharsetUtil.CHARSET_GBK);
        logger.info("receiveNotifyData07 body:{}", body);
        if(StrUtil.isBlank(body)) {
            return;
        }
        String[] bodys = body.split("#", 2);
        if(bodys.length == 2) {
            Long id = Long.parseLong(bodys[0]);
            DB.getInstance().saveMsg(id, bodys[1]);
            Msg msg = DB.getInstance().queryLatestMsg();
            carMainFrame.setMsgText(msg);
        }
    }
    
    /**
     * 清除车载机里面存储的无线信标编号信息数据（服务器——>车载机）
        55  AA  0A  IDL IDH  CRC16L  CRC16H   （共7个字节）
     * @param dataHexStrs
     */
    public void receiveCleanBeaconInfo(String[] dataHexStrs) {
        logger.info("清除车载机里面存储的无线信标编号信息数据");
        DB.getInstance().delBeaconAll();
    }
    
}
