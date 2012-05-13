
package com.coleman.http.json.bean;

import com.coleman.http.json.annotation.KeyName;
import com.coleman.http.json.annotation.RequestObject;

@RequestObject(path = "/ColemanServer/versionCheck")
public class VersionCheckReq {
    /**
     * 版本号
     */
    @KeyName(abbr = "vc")
    private Integer versionCode;

    /**
     * 版本类型, 预留字段
     */
    @KeyName(abbr = "vt")
    private String versionType;

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

}
