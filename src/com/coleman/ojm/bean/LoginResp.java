
package com.coleman.ojm.bean;

import com.coleman.ojm.annotation.KeyName;
import com.coleman.ojm.annotation.ResponseObject;

@ResponseObject
public class LoginResp implements BasicResponse{
    @KeyName(abbr = "rc")
    private Integer resultCode;

    /**
     * 版本描述
     */
    @KeyName(abbr = "d")
    private String Description;

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

}
