package com.coleman.http.json.bean;

import com.coleman.http.json.annotation.KeyName;
import com.coleman.http.json.annotation.ResponseObject;
@ResponseObject
public class WordlistResp
{
    @KeyName(abbr = "rc")
    private Integer resultCode;
    
    /**
     * 版本描述
     */
    @KeyName(abbr = "d")
    private String Description;
    
    /**
     * 服务端单词表目录树
     */
    @KeyName(abbr = "rf")
    private RFile rfile;
    
    public Integer getResultCode()
    {
        return resultCode;
    }
    
    public void setResultCode(Integer resultCode)
    {
        this.resultCode = resultCode;
    }
    
    public String getDescription()
    {
        return Description;
    }
    
    public void setDescription(String description)
    {
        Description = description;
    }
    
    public RFile getRfile()
    {
        return rfile;
    }
    
    public void setRfile(RFile rfile)
    {
        this.rfile = rfile;
    }
    
}
