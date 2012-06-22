
package com.coleman.ojm.bean;

import com.coleman.ojm.annotation.KeyName;
import com.coleman.ojm.annotation.ResponseObject;

@ResponseObject
public class VersionCheckResp implements BasicResponse {
    @KeyName(abbr = "rc")
    private Integer resultCode;

    /**
     * 新版本号
     */
    @KeyName(abbr = "vc")
    private Integer newVersionCode;

    /**
     * 版本描述
     */
    @KeyName(abbr = "d")
    private String Description;

    /**
     * 下载文件名
     */
    @KeyName(abbr = "u")
    private String downloadFileName;

    /**
     * 第三方下载地址， 如果不存在，会从自有服务器下载
     */
    @KeyName(abbr = "tdl")
    private String thirdpartDownloadUrl;

    public Integer getNewVersionCode() {
        return newVersionCode;
    }

    public void setNewVersionCode(Integer newVersionCode) {
        this.newVersionCode = newVersionCode;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDownloadFileName() {
        return downloadFileName;
    }

    public void setDownloadFile(String downloadFileName) {
        this.downloadFileName = downloadFileName;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setThirdpartDownloadUrl(String thirdpartDownloadUrl) {
        this.thirdpartDownloadUrl = thirdpartDownloadUrl;
    }

    public String getThirdpartDownloadUrl() {
        return thirdpartDownloadUrl;
    }

}
