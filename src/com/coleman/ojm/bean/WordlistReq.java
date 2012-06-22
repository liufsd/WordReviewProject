package com.coleman.ojm.bean;

import com.coleman.ojm.annotation.KeyName;
import com.coleman.ojm.annotation.RequestObject;
@RequestObject(path = "/ColemanServer/getWordlist")
public class WordlistReq
{
    
    /**
     * It's not used now.
     */
    @KeyName(abbr = "t")
    private Integer type;
    
    public void setType(Integer type)
    {
        this.type = type;
    }
    
    public Integer getType()
    {
        return type;
    }
    
}
