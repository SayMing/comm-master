package com.yang.serialport.model;

/**
 *
 * @author Sayming-Hong
 * @date 2023-12-27
 */
public class Beacon {

    /**
     * 信标编号
     */
    private Integer code;
    
    private String location;
    
    /**
     * 类型，1=装矿点 2=卸矿点 3=其他
     */
    private Integer type;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Beacon : {\"code\" : \"");
        builder.append(code);
        builder.append("\", location\" : \"");
        builder.append(location);
        builder.append("\", type\" : \"");
        builder.append(type);
        builder.append("}");
        return builder.toString();
    }
    
}
