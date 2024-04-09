package com.yang.serialport.model;

/**
 * CREATE TABLE workPath (
    startBeacon INTEGER,
    endBeacon INTEGER,
    startName TEXT,
    endName TEXT,
    nextStartBeacon INTEGER,
    nextEndBeacon INTEGER,
    nextStartName TEXT,
    nextEndName TEXT
);

 * @author Sayming-Hong
 * @date 2023-12-15
 */
public class WorkPath {

    private Integer startBeacon;
    private Integer endBeacon;
    
    private String startName;
    private String endName;

    private Integer nextStartBeacon;
    private Integer nextEndBeacon;
    private String nextStartName;
    private String nextEndName;
    public Integer getStartBeacon() {
        return startBeacon;
    }
    public void setStartBeacon(Integer startBeacon) {
        this.startBeacon = startBeacon;
    }
    public Integer getEndBeacon() {
        return endBeacon;
    }
    public void setEndBeacon(Integer endBeacon) {
        this.endBeacon = endBeacon;
    }
    public String getStartName() {
        return startName;
    }
    public void setStartName(String startName) {
        this.startName = startName;
    }
    public String getEndName() {
        return endName;
    }
    public void setEndName(String endName) {
        this.endName = endName;
    }
    public Integer getNextStartBeacon() {
        return nextStartBeacon;
    }
    public void setNextStartBeacon(Integer nextStartBeacon) {
        this.nextStartBeacon = nextStartBeacon;
    }
    public Integer getNextEndBeacon() {
        return nextEndBeacon;
    }
    public void setNextEndBeacon(Integer nextEndBeacon) {
        this.nextEndBeacon = nextEndBeacon;
    }
    public String getNextStartName() {
        return nextStartName;
    }
    public void setNextStartName(String nextStartName) {
        this.nextStartName = nextStartName;
    }
    public String getNextEndName() {
        return nextEndName;
    }
    public void setNextEndName(String nextEndName) {
        this.nextEndName = nextEndName;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WorkPath : {\"startBeacon\" : \"");
        builder.append(startBeacon);
        builder.append("\", endBeacon\" : \"");
        builder.append(endBeacon);
        builder.append("\", startName\" : \"");
        builder.append(startName);
        builder.append("\", endName\" : \"");
        builder.append(endName);
        builder.append("\", nextStartBeacon\" : \"");
        builder.append(nextStartBeacon);
        builder.append("\", nextEndBeacon\" : \"");
        builder.append(nextEndBeacon);
        builder.append("\", nextStartName\" : \"");
        builder.append(nextStartName);
        builder.append("\", nextEndName\" : \"");
        builder.append(nextEndName);
        builder.append("}");
        return builder.toString();
    }
    
}
