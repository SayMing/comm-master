package com.yang.serialport.model;

/**
 *
 * @author Sayming-Hong
 * @date 2023-12-10
 */
public class Msg {

    private Long id;

    private String msg;

    private Long createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg : {\"id\" : \"");
        builder.append(id);
        builder.append("\", msg\" : \"");
        builder.append(msg);
        builder.append("\", createTime\" : \"");
        builder.append(createTime);
        builder.append("}");
        return builder.toString();
    }

}
