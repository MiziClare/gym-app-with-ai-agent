package com.example.entity;

import java.io.Serializable;

/**
 * 器材预约表
 */
public class Eqreserve implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer equipmentId;
    private Integer userId;
    private String start;
    private String end;
    private String status;

    private String equipmentImg;
    private String equipmentName;
    private String userName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEquipmentImg() {
        return equipmentImg;
    }

    public void setEquipmentImg(String equipmentImg) {
        this.equipmentImg = equipmentImg;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}