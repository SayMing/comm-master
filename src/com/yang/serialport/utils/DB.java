package com.yang.serialport.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yang.serialport.model.Beacon;
import com.yang.serialport.model.BeaconLog;
import com.yang.serialport.model.Msg;
import com.yang.serialport.model.WorkLineInfo;
import com.yang.serialport.model.WorkPath;
import com.yang.serialport.model.WorkTimeInfo;
import com.yang.serialport.utils.Constants.BeaconLogStatus;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;

/**
 *
 * @author Sayming-Hong
 * @date 2023-12-10
 */
public class DB {
    private Connection connection;
    private static final DB db = new DB();

    private static Logger logger;

    public DB() {
        try {
            // 加载SQLite驱动程序
            Class.forName("org.sqlite.JDBC");

            // 创建数据库连接
            connection = DriverManager
                    .getConnection("jdbc:sqlite:" + ConfigProperties.getDbDataPath());
            DB.logger = LoggerFactory.getLogger(DB.class);
            DB.logger.info("成功连接到SQLite数据库！");
            initDb();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DB getInstance() {
        return db;
    }

    /**
     * 记录信标数据
     * 
     * @param code       信标编号
     * @param sourceData
     * @param status
     * @return 大于0说明插入成功
     */
    public int insertBeaconLog(Integer code, String sourceData) {
        int currentTime = (int)DateUtil.currentSeconds();
        Integer lasterCode = lasterBeaconLog();
        logger.info("Latest beacon log code={} current code={} time={}", lasterCode, code, currentTime);
        // 与最后一条记录的信标编号不一致的话才入库，一致的话说明矿车一直在原地。
        if(lasterCode == null || !ObjectUtil.equal(code, lasterCode)) {
            String insertSQL = "INSERT INTO beaconLog (code, createTime, sourceData, status, beaconType) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                Beacon currentBeacon = selectBeaconByCode(code);
                preparedStatement.setInt(1, code);
                preparedStatement.setInt(2, currentTime);
                preparedStatement.setString(3, sourceData);
                preparedStatement.setInt(4, Constants.BeaconLogStatus.NOT_SEND);
                if(currentBeacon != null) {
                    preparedStatement.setInt(5, currentBeacon.getType());
                }
                int row = preparedStatement.executeUpdate();
                return row;
            } catch (Exception e) {
                logger.error("insertBeaconLog error:"+e.getMessage(), e);
            }
        }
        return 0;
    }
    
    public Integer lasterBeaconLog() {
        String querySQL = "SELECT * FROM beaconLog ORDER BY createTime DESC LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                Integer code = resultSet.getInt("code");
                return code;
            }

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    public boolean isExistedBeaconLog(Integer code, Integer createTime, String sourceData) {
        String querySQL = "SELECT COUNT(*) > 0 FROM beaconLog WHERE code = ? AND createTime = ? AND sourceData = ?;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.setInt(1, code);
            preparedStatement.setInt(2, createTime);
            preparedStatement.setString(3, sourceData);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean(1);
            }

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }
    
    /**
     * 指定信标日志更新等待响应
     * @param datas
     */
    public void updateSendBeaconLog(List<BeaconLog> datas, int requestId) {
        if(datas != null && !datas.isEmpty()) {
            try {
                // 将 List 转换为数组
                Long[] codeArray = new Long[datas.size()];
                int i = 0;
                for(; i<datas.size();i++) {
                    codeArray[i] = datas.get(i).getId();
                }
                String sql = "UPDATE beaconLog SET requestId=" + requestId +" WHERE id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    for (Long id : codeArray) {
                        preparedStatement.setLong(1, id);
                        preparedStatement.addBatch();
                    }
                    int[] rowsAffected = preparedStatement.executeBatch();
                    System.out.println(rowsAffected.length + " rows beacon log updated.");
                }
            }catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * 更新服务器接收信标成功数据
     * @param requestId
     */
    public void updateBeaconLogIsSend(Integer requestId) {
        try {
            // 将 List 转换为数组
            String sql = "UPDATE beaconLog SET status = " + BeaconLogStatus.SEND +
                    " WHERE requestId = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, requestId);
                preparedStatement.executeUpdate();
            }
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    /**
     * 获取未发送的信标数据
     * @return
     */
    public List<BeaconLog> selectNotSendBeaconLogList(){
        String querySQL = "SELECT * FROM BeaconLog WHERE status = " + BeaconLogStatus.NOT_SEND + " limit 50;";
        List<BeaconLog> dataBeaconLogs = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Integer code = resultSet.getInt("code");
                Integer createTime = resultSet.getInt("createTime");
                String sourceData = resultSet.getString("sourceData");
                Integer beaconType = resultSet.getInt("beaconType");
                Integer requestId = resultSet.getInt("requestId");
                BeaconLog beaconLog = new BeaconLog();
                beaconLog.setId(id);
                beaconLog.setCode(code);
                beaconLog.setSourceData(sourceData);
                beaconLog.setCreateTime(createTime);
                beaconLog.setBeaconType(beaconType);
                beaconLog.setRequestId(requestId);
                dataBeaconLogs.add(beaconLog);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return dataBeaconLogs;
    }
    
    /**
     * 获取当前卸矿次数
     * @param date 卸矿点时间 >= date
     * @return
     */
    public int countUnload(Date date) {
        if(date == null) {
            date = DateUtil.beginOfDay(new Date());
        }
        logger.info("count unload:{}", DateUtil.formatDateTime(date));
        String sqlString = "SELECT COUNT(*) AS count\r\n"
                + "FROM beaconLog\r\n"
                + "WHERE createTime >= "+date.getTime()/1000+" and beaconType = 2;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    // 记录通知信息数据
    public void insertMsg(Long id, String msg, Long createTime) {
        String insertSQL = "INSERT INTO msg (id, msg, createTime) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setString(2, msg);
            preparedStatement.setLong(3, createTime);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void saveMsg(Long id, String msg) {
        if(id == null) {
            return;
        }
        boolean insert = true;
        String searchSql = "SELECT * FROM msg where id=" + id;
        try (PreparedStatement preparedStatement = connection.prepareStatement(searchSql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                insert = false;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        
        String insertSQL;
        if(insert) {
            insertSQL = "INSERT INTO msg (id, msg, createTime) VALUES (?, ?, ?)";
        }else {
            insertSQL = "UPDATE msg set id=?, msg=?, createTime=? where id="+id;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setString(2, msg);
            preparedStatement.setLong(3, System.currentTimeMillis());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // 最多10条记录
        String delLimitSql = "DELETE FROM msg \r\n"
                + "WHERE id NOT IN (\r\n"
                + "    SELECT id FROM msg ORDER BY createTime desc LIMIT 10\r\n"
                + ");";
        try (PreparedStatement preparedStatement = connection.prepareStatement(delLimitSql)) {
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取最新的一条通知信息
     * 
     * @return
     */
    public Msg queryLatestMsg() {
        String querySQL = "SELECT * FROM msg ORDER BY createTime DESC LIMIT 1";
        Msg msgDto = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                long id = resultSet.getLong("id");
                String msg = resultSet.getString("msg");
                long createTime = resultSet.getLong("createTime");

                logger.info("Latest Msg: id=" + id + ", msg=" + msg + ", createTime=" + createTime);

                msgDto = new Msg();
                msgDto.setId(id);
                msgDto.setCreateTime(createTime);
                msgDto.setMsg(msg);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return msgDto;
    }

    /**
     * 获取上一条通知信息
     * 
     * @param currentId
     * @return
     */
    public Msg queryPrevMsg(Long currentId) {
        String querySQL = "SELECT * FROM msg WHERE id < ? ORDER BY id DESC LIMIT 1";
        Msg msgDto = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.setLong(1, currentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    String msg = resultSet.getString("msg");
                    long createTime = resultSet.getLong("createTime");

                    logger.info(
                            "Next Data: id=" + id + ", msg=" + msg + ", createTime=" + createTime);

                    msgDto = new Msg();
                    msgDto.setId(id);
                    msgDto.setCreateTime(createTime);
                    msgDto.setMsg(msg);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return msgDto;
    }

    /**
     * 获取下一条通知信息
     * 
     * @param connection
     * @param currentId
     * @return
     */
    public Msg queryNextMsg(Long currentId) {
        String querySQL = "SELECT * FROM msg WHERE id > ? ORDER BY id LIMIT 1";
        Msg msgDto = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.setLong(1, currentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    String msg = resultSet.getString("msg");
                    long createTime = resultSet.getLong("createTime");

                    logger.info(
                            "Next Data: id=" + id + ", msg=" + msg + ", createTime=" + createTime);

                    msgDto = new Msg();
                    msgDto.setId(id);
                    msgDto.setCreateTime(createTime);
                    msgDto.setMsg(msg);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return msgDto;
    }
    public int countNowMsg(Long currentId) {
        int count = 0;
        String sql = "select COUNT(1)+1 from msg where id < ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, currentId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
    public int countMsg() {
        int count = 0;
        String sql = "select COUNT(1) from msg";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
    
    /**
     * 获取上传批次号并自动更新下一次批次号，累加1
     * @return
     */
    public int getCurrentRequestIdAndAdd() {
        // 查询当前的 requestId 值
        int currentRequestId = getCurrentRequestId();

        // 将 requestId 累加1，回归到1，如果达到上限 255
        int newRequestId = (currentRequestId + 1) % 256;
        if (newRequestId == 0) {
            newRequestId = 1;
        }

        // 更新 beaconRequestId 表中的数据
        try {
            updateRequestId(newRequestId);
        }catch (Exception e) {
            logger.error("更新beaconRequestId异常：" + e.getMessage());
        }
        return newRequestId;
    }
    
    public int getCurrentRequestId() {
        String sql = "SELECT requestId FROM beaconRequestId";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt("requestId");
            } else {
                // 如果表中没有数据，则默认返回1
                return 1;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 1;
        }
    }
    
    public void updateRequestId(int newRequestId) throws SQLException {
        String sql = "UPDATE beaconRequestId SET requestId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, newRequestId);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * 插入运矿路线
     * @param workPath
     */
    public void addWorkPath(WorkPath workPath) {
        String insertSQL = "INSERT INTO msg (startBeacon, endBeacon, startName, endName, nextStartBeacon, nextEndBeacon, nextStartName, nextEndName) VALUES (?, ?, ?,?, ?, ?,?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setInt(1, workPath.getStartBeacon());
            preparedStatement.setInt(2, workPath.getEndBeacon());
            preparedStatement.setString(3, workPath.getStartName());
            preparedStatement.setString(4, workPath.getEndName());
            preparedStatement.setInt(5, workPath.getNextStartBeacon());
            preparedStatement.setInt(6, workPath.getNextEndBeacon());
            preparedStatement.setString(7, workPath.getNextStartName());
            preparedStatement.setString(8, workPath.getNextEndName());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 最后一条运矿路线记录
     * @return
     */
    public WorkPath lasterWorkPath() {
        String querySQL = "SELECT startBeacon, endBeacon, startName, endName, nextStartBeacon, nextEndBeacon, nextStartName, nextEndName FROM workPath LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                Integer startBeacon = resultSet.getInt("startBeacon");
                Integer endBeacon = resultSet.getInt("endBeacon");
                String startName = resultSet.getString("startName");
                String endName = resultSet.getString("startName");
                Integer nextStartBeacon = resultSet.getInt("nextStartBeacon");
                Integer nextEndBeacon = resultSet.getInt("nextEndBeacon");
                String nextStartName = resultSet.getString("nextStartName");
                String nextEndName = resultSet.getString("nextEndName");
                WorkPath workPath = new WorkPath();
                workPath.setStartBeacon(startBeacon);
                workPath.setEndBeacon(endBeacon);
                workPath.setStartName(startName);
                workPath.setEndName(endName);
                workPath.setNextStartBeacon(nextStartBeacon);
                workPath.setNextEndBeacon(nextEndBeacon);
                workPath.setNextStartName(nextStartName);
                workPath.setNextEndName(nextEndName);
                logger.info("Latest workPath={}", workPath);
                return workPath;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 保存信标信息数据，有数据则更新，无则新增
     * @param beaconCode
     * @param location
     * @param type
     */
    public void saveBeacon(Integer beaconCode, String location, Integer type) {
        Beacon beacon = selectBeaconByCode(beaconCode);
        String saveSql = null;
        if(beacon == null) {
            saveSql = "INSERT INTO beacon (code, location, type) VALUES (?, ?, ?)";
        }else {
            saveSql = "UPDATE beacon SET code = ?, location=?, type=? where code=" + beaconCode;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(saveSql)) {
            preparedStatement.setInt(1, beaconCode);
            preparedStatement.setString(2, location);
            preparedStatement.setInt(3, type);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Beacon selectBeaconByCode(Integer beaconCode) {
        String querySQL = "SELECT code, location, type FROM beacon where code ="+beaconCode+" LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                Integer code = resultSet.getInt("code");
                String location = resultSet.getString("location");
                Integer type = resultSet.getInt("type");
                Beacon beacon = new Beacon();
                beacon.setCode(code);
                beacon.setLocation(location);
                beacon.setType(type);
                return beacon;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    public void delBeaconAll() {
        String querySQL = "DELETE FROM beacon";
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.execute();
            logger.info("已删除信标数据");
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    public void delWorkTimeInfo() {
        String querySQL = "DELETE FROM workTimeInfo";
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    public WorkTimeInfo getWorkTimeInfo() {
        String querySQL = "SELECT id,trainTripNum,workTimeType,workTimeStart,workTimeEnd,driverName,driverCode FROM workTimeInfo";
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                WorkTimeInfo workTimeInfo = new WorkTimeInfo();
                workTimeInfo.setId(resultSet.getLong("id"));
                workTimeInfo.setTrainTripNum(resultSet.getInt("trainTripNum"));
                workTimeInfo.setWorkTimeType(resultSet.getInt("workTimeType"));
                workTimeInfo.setWorkTimeStart(resultSet.getString("workTimeStart"));
                workTimeInfo.setWorkTimeEnd(resultSet.getString("workTimeEnd"));
                workTimeInfo.setDriverName(resultSet.getString("driverName"));
                workTimeInfo.setDriverCode(resultSet.getString("driverCode"));
                return workTimeInfo;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    public void saveWorkTimeInfo(WorkTimeInfo workTimeInfo) {
        WorkTimeInfo workTimeInfoDb = getWorkTimeInfo();
        String saveSql = null;
        if(workTimeInfoDb == null) {
            workTimeInfo.setId(1L);
            saveSql = "INSERT INTO workTimeInfo (id,trainTripNum,workTimeType,workTimeStart,workTimeEnd,driverName,driverCode) VALUES (?, ?, ?, ?, ?, ?, ?)";
        }else {
            saveSql = "UPDATE workTimeInfo SET id=?,trainTripNum=?,workTimeType=?,workTimeStart=?,workTimeEnd=?,driverName=?,driverCode=? where id=" + workTimeInfoDb.getId();
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(saveSql)) {
            preparedStatement.setLong(1, workTimeInfo.getId());
            preparedStatement.setInt(2, workTimeInfo.getTrainTripNum());
            preparedStatement.setInt(3, workTimeInfo.getWorkTimeType());
            preparedStatement.setString(4, workTimeInfo.getWorkTimeStart());
            preparedStatement.setString(5, workTimeInfo.getWorkTimeEnd());
            preparedStatement.setString(6, workTimeInfo.getDriverName());
            preparedStatement.setString(7, workTimeInfo.getDriverCode());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("save work time info error:" + saveSql, e);
        }
    }
    
    public WorkLineInfo selectWorkLineInfo() {
        String querySQL = "SELECT id, currentBeaconCodeStart, currentBeaconCodeEnd, nextBeaconCodeStart, nextBeaconCodeEnd FROM workLineInfo";
        try (PreparedStatement preparedStatement = connection.prepareStatement(querySQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                WorkLineInfo workLineInfo = new WorkLineInfo();
                workLineInfo.setId(resultSet.getLong("id"));
                workLineInfo.setCurrentBeaconCodeStart(resultSet.getInt("currentBeaconCodeStart"));
                workLineInfo.setCurrentBeaconCodeEnd(resultSet.getInt("currentBeaconCodeEnd"));
                workLineInfo.setNextBeaconCodeStart(resultSet.getInt("nextBeaconCodeStart"));
                workLineInfo.setNextBeaconCodeEnd(resultSet.getInt("nextBeaconCodeEnd"));
                
                Beacon currentBeaconStart = DB.getInstance().selectBeaconByCode(workLineInfo.getCurrentBeaconCodeStart());
                if(currentBeaconStart != null) {
                    workLineInfo.setCurrentBeaconStartLocation(currentBeaconStart.getLocation());
                }
                Beacon currentBeaconEnd = DB.getInstance().selectBeaconByCode(workLineInfo.getCurrentBeaconCodeEnd());
                if(currentBeaconEnd != null) {
                    workLineInfo.setCurrentBeaconEndLocation(currentBeaconEnd.getLocation());
                }
                Beacon nextBeaconStart = DB.getInstance().selectBeaconByCode(workLineInfo.getNextBeaconCodeStart());
                if(nextBeaconStart != null) {
                    workLineInfo.setNextBeaconStartLocation(nextBeaconStart.getLocation());
                }
                Beacon nextBeaconEnd = DB.getInstance().selectBeaconByCode(workLineInfo.getNextBeaconCodeEnd());
                if(nextBeaconEnd != null) {
                    workLineInfo.setNextBeaconEndLocation(nextBeaconEnd.getLocation());
                }
                
                return workLineInfo;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    public void saveWorkLineInfo(WorkLineInfo lineInfo) {
        lineInfo.setId(1L);
        // 查询是否存在数据
        String selectQuery = "SELECT id FROM workLineInfo WHERE id = ?";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            selectStatement.setLong(1, lineInfo.getId());
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    // 记录存在，执行更新操作
                    updateWorkLineInfo(lineInfo);
                } else {
                    // 记录不存在，执行插入操作
                    insertWorkLineInfo(lineInfo);
                }
            }
        }catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void insertWorkLineInfo(WorkLineInfo workLineInfo) throws SQLException {
        String insertQuery = "INSERT INTO workLineInfo (id, currentBeaconCodeStart, currentBeaconCodeEnd, " +
                "nextBeaconCodeStart, nextBeaconCodeEnd) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setLong(1, workLineInfo.getId());
            insertStatement.setInt(2, workLineInfo.getCurrentBeaconCodeStart());
            insertStatement.setInt(3, workLineInfo.getCurrentBeaconCodeEnd());
            insertStatement.setInt(4, workLineInfo.getNextBeaconCodeStart());
            insertStatement.setInt(5, workLineInfo.getNextBeaconCodeEnd());

            insertStatement.executeUpdate();
        }
    }

    public void updateWorkLineInfo(WorkLineInfo workLineInfo) throws SQLException {
        String updateQuery = "UPDATE workLineInfo SET currentBeaconCodeStart=?, currentBeaconCodeEnd=?, " +
                "nextBeaconCodeStart=?, nextBeaconCodeEnd=? WHERE id=?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setInt(1, workLineInfo.getCurrentBeaconCodeStart());
            updateStatement.setInt(2, workLineInfo.getCurrentBeaconCodeEnd());
            updateStatement.setInt(3, workLineInfo.getNextBeaconCodeStart());
            updateStatement.setInt(4, workLineInfo.getNextBeaconCodeEnd());
            updateStatement.setLong(5, workLineInfo.getId());

            updateStatement.executeUpdate();
        }
    }
    
    public boolean isTableExists(String tableName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tableName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
    
    // 初始化数据库
    public void initDb() {
        try (Statement statement = connection.createStatement()) {
            if(!isTableExists("beacon")) {
                // 创建信标记录日志表
                String createTableSQL = "CREATE TABLE IF NOT EXISTS beacon ("
                        + "    code INTEGER," 
                        + "    location TEXT,"
                        + "    type INTEGER"+ ");";
                statement.executeUpdate(createTableSQL);
            }
            
            if(!isTableExists("beaconLog")) {
                // 创建信标记录日志表
                String createTableSQL = "CREATE TABLE IF NOT EXISTS beaconLog ("
                        + "    id INTEGER PRIMARY KEY," 
                        + "    code INTEGER," 
                        + "    createTime INTEGER,"
                        + "    sourceData TEXT,"
                        + "    requestId INTEGER," 
                        + "    status INTEGER DEFAULT (0)," 
                        + "    beaconType INTEGER" 
                        + ");";
                statement.executeUpdate(createTableSQL);
            }
            
            if(!isTableExists("msg")) {
                // 创建 通知信息表
                String createTableSQL = "CREATE TABLE IF NOT EXISTS msg (\r\n"
                        + "                    id INTEGER,\r\n" + "                    "
                                + "msg TEXT,\r\n"
                        + "                    createTime INTEGER\r\n" + "                );";
                statement.executeUpdate(createTableSQL);
            }
            
            if(!isTableExists("beaconRequestId")) {
                // 创建 信标上传id表
                String createTableSQL = "CREATE TABLE IF NOT EXISTS beaconRequestId ("
                        + "    requestId INTEGER DEFAULT (0));";
                statement.executeUpdate(createTableSQL);
                statement.executeUpdate("INSERT INTO beaconRequestId (requestId) VALUES (1);");
            }

            if(!isTableExists("workPath")) {
                // 创建 运矿路线表
                String createTableSQL = "CREATE TABLE workPath (\r\n"
                        + "                        startBeacon INTEGER,\r\n"
                        + "                        endBeacon INTEGER,\r\n"
                        + "                        startName TEXT,\r\n"
                        + "                        endName TEXT,\r\n"
                        + "                        nextStartBeacon INTEGER,\r\n"
                        + "                        nextEndBeacon INTEGER,\r\n"
                        + "                        nextStartName TEXT,\r\n"
                        + "                        nextEndName TEXT\r\n"
                        + "                    );";
                statement.executeUpdate(createTableSQL);
            }

            if(!isTableExists("workTimeInfo")) {
                // 创建 运矿信息表 
                String createTableSQL = "CREATE TABLE workTimeInfo (\r\n"
                        + "                        id INTEGER,\r\n"
                        + "                        trainTripNum INTEGER,\r\n"
                        + "                        workTimeType INTEGER,\r\n"
                        + "                        workTimeStart TEXT,\r\n"
                        + "                        workTimeEnd TEXT,\r\n"
                        + "                        driverName TEXT,\r\n"
                        + "                        driverCode TEXT\r\n"
                        + "                    );";
                statement.executeUpdate(createTableSQL);
            }
            if(!isTableExists("workLineInfo")) {
                // 创建 运矿路线表
                String createTableSQL = "CREATE TABLE workLineInfo (\r\n"
                        + "                        id INTEGER,\r\n"
                        + "                        currentBeaconCodeStart INTEGER,\r\n"
                        + "                        currentBeaconCodeEnd INTEGER,\r\n"
                        + "                        nextBeaconCodeStart INTEGER,\r\n"
                        + "                        nextBeaconCodeEnd INTEGER\r\n"
                        + "                    );";
                statement.executeUpdate(createTableSQL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        // 关闭数据库连接
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
