package com.yang.serialport.model;

/**
 *
 * @author Sayming-Hong
 * @date 2023-12-27
 */
public class WorkLineInfo {

    private Long id;
    
    private Integer currentBeaconCodeStart;
    
    private String currentBeaconStartLocation;
    
    private Integer currentBeaconCodeEnd;
    
    private String currentBeaconEndLocation;
    
    private Integer nextBeaconCodeStart;
    
    private String nextBeaconStartLocation;
    
    private Integer nextBeaconCodeEnd;
    
    private String nextBeaconEndLocation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCurrentBeaconCodeStart() {
        return currentBeaconCodeStart;
    }

    public void setCurrentBeaconCodeStart(Integer currentBeaconCodeStart) {
        this.currentBeaconCodeStart = currentBeaconCodeStart;
    }

    public String getCurrentBeaconStartLocation() {
        return currentBeaconStartLocation;
    }

    public void setCurrentBeaconStartLocation(String currentBeaconStartLocation) {
        this.currentBeaconStartLocation = currentBeaconStartLocation;
    }

    public Integer getCurrentBeaconCodeEnd() {
        return currentBeaconCodeEnd;
    }

    public void setCurrentBeaconCodeEnd(Integer currentBeaconCodeEnd) {
        this.currentBeaconCodeEnd = currentBeaconCodeEnd;
    }

    public String getCurrentBeaconEndLocation() {
        return currentBeaconEndLocation;
    }

    public void setCurrentBeaconEndLocation(String currentBeaconEndLocation) {
        this.currentBeaconEndLocation = currentBeaconEndLocation;
    }

    public Integer getNextBeaconCodeStart() {
        return nextBeaconCodeStart;
    }

    public void setNextBeaconCodeStart(Integer nextBeaconCodeStart) {
        this.nextBeaconCodeStart = nextBeaconCodeStart;
    }

    public String getNextBeaconStartLocation() {
        return nextBeaconStartLocation;
    }

    public void setNextBeaconStartLocation(String nextBeaconStartLocation) {
        this.nextBeaconStartLocation = nextBeaconStartLocation;
    }

    public Integer getNextBeaconCodeEnd() {
        return nextBeaconCodeEnd;
    }

    public void setNextBeaconCodeEnd(Integer nextBeaconCodeEnd) {
        this.nextBeaconCodeEnd = nextBeaconCodeEnd;
    }

    public String getNextBeaconEndLocation() {
        return nextBeaconEndLocation;
    }

    public void setNextBeaconEndLocation(String nextBeaconEndLocation) {
        this.nextBeaconEndLocation = nextBeaconEndLocation;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WorkLineInfo : {\"id\" : \"");
        builder.append(id);
        builder.append("\", currentBeaconCodeStart\" : \"");
        builder.append(currentBeaconCodeStart);
        builder.append("\", currentBeaconStartLocation\" : \"");
        builder.append(currentBeaconStartLocation);
        builder.append("\", currentBeaconCodeEnd\" : \"");
        builder.append(currentBeaconCodeEnd);
        builder.append("\", currentBeaconEndLocation\" : \"");
        builder.append(currentBeaconEndLocation);
        builder.append("\", nextBeaconCodeStart\" : \"");
        builder.append(nextBeaconCodeStart);
        builder.append("\", nextBeaconStartLocation\" : \"");
        builder.append(nextBeaconStartLocation);
        builder.append("\", nextBeaconCodeEnd\" : \"");
        builder.append(nextBeaconCodeEnd);
        builder.append("\", nextBeaconEndLocation\" : \"");
        builder.append(nextBeaconEndLocation);
        builder.append("}");
        return builder.toString();
    }

}
