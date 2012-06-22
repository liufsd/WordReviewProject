
package com.coleman.ojm.bean;

import com.coleman.ojm.annotation.KeyName;
import com.coleman.ojm.annotation.RequestObject;

@RequestObject(path = "/ColemanServer/login")
public class LoginReq {
    @KeyName(abbr = "un")
    private String userName;

    @KeyName(abbr = "pw")
    private String password;

    @KeyName(abbr = "md")
    private String model;

    @KeyName(abbr = "pn")
    private String phoneNumber;

    @KeyName(abbr = "it")
    private Long installedTime;

    @KeyName(abbr = "st")
    private Integer startTimes;

    @KeyName(abbr = "cl")
    private String currentLevel;

    /**
     * device id
     */
    @KeyName(abbr = "im")
    private String IMEI;

    /**
     * 2.1
     */
    @KeyName(abbr = "vs")
    private String version;

    /**
     * generic
     */
    @KeyName(abbr = "dv")
    private String device;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getInstalledTime() {
        return installedTime;
    }

    public void setInstalledTime(Long installedTime) {
        this.installedTime = installedTime;
    }

    public Integer getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(Integer startTimes) {
        this.startTimes = startTimes;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(String currentLevel) {
        this.currentLevel = currentLevel;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String iMEI) {
        IMEI = iMEI;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

}
