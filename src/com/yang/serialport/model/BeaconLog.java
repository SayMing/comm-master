package com.yang.serialport.model;

/**
 *
 * @author Sayming-Hong
 * @date 2023-12-12
 */
public class BeaconLog {

    private Long id;

    /**
     * 信标编号
     */
    private Integer code;
    
    /**
     * 创建时间
     */
    private Integer createTime;
    
    /**
     * 源数据
     */
    private String sourceData;
    
    /**
     * 状态 {@link com.yang.serialport.utils.Constants.BeaconLogStatus} 
     */
    private Integer status;
    
    /**
     * 上传批次号
     */
    private Integer requestId;
    
    /**
     * 信标类型， @See Constants.BeaconType
     */
    private Integer beaconType;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public String getSourceData() {
        return sourceData;
    }

    public void setSourceData(String sourceData) {
        this.sourceData = sourceData;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getBeaconType() {
        return beaconType;
    }

    public void setBeaconType(Integer beaconType) {
        this.beaconType = beaconType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BeaconLog : {\"id\" : \"");
        builder.append(id);
        builder.append("\", code\" : \"");
        builder.append(code);
        builder.append("\", createTime\" : \"");
        builder.append(createTime);
        builder.append("\", sourceData\" : \"");
        builder.append(sourceData);
        builder.append("\", status\" : \"");
        builder.append(status);
        builder.append("\", requestId\" : \"");
        builder.append(requestId);
        builder.append("\", beaconType\" : \"");
        builder.append(beaconType);
        builder.append("}");
        return builder.toString();
    }
    
}
