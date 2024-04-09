package com.yang.serialport.model;

/**
 *
 * @author Sayming-Hong
 * @date 2023-12-27
 */
public class WorkTimeInfo {

    private Long id;
    
    private Integer trainTripNum;
    
    private Integer workTimeType;
    
    private String workTimeStart;
    
    private String workTimeEnd;
    
    private String driverName;
    
    private String driverCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTrainTripNum() {
        return trainTripNum;
    }

    public void setTrainTripNum(Integer trainTripNum) {
        this.trainTripNum = trainTripNum;
    }

    public Integer getWorkTimeType() {
        return workTimeType;
    }

    public void setWorkTimeType(Integer workTimeType) {
        this.workTimeType = workTimeType;
    }

    public String getWorkTimeStart() {
        return workTimeStart;
    }

    public void setWorkTimeStart(String workTimeStart) {
        this.workTimeStart = workTimeStart;
    }

    public String getWorkTimeEnd() {
        return workTimeEnd;
    }

    public void setWorkTimeEnd(String workTimeEnd) {
        this.workTimeEnd = workTimeEnd;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverCode() {
        return driverCode;
    }

    public void setDriverCode(String driverCode) {
        this.driverCode = driverCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WorkTimeInfo : {\"id\" : \"");
        builder.append(id);
        builder.append("\", trainTripNum\" : \"");
        builder.append(trainTripNum);
        builder.append("\", workTimeType\" : \"");
        builder.append(workTimeType);
        builder.append("\", workTimeStart\" : \"");
        builder.append(workTimeStart);
        builder.append("\", workTimeEnd\" : \"");
        builder.append(workTimeEnd);
        builder.append("\", driverName\" : \"");
        builder.append(driverName);
        builder.append("\", driverCode\" : \"");
        builder.append(driverCode);
        builder.append("}");
        return builder.toString();
    }

    
}
