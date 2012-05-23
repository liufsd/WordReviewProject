package com.coleman.http.json.bean;

import com.coleman.http.json.annotation.KeyName;
import com.coleman.http.json.annotation.ValueObject;

@ValueObject
public class RFile
{
    @KeyName(abbr = "ir")
    private Boolean isRoot;
    
    @KeyName(abbr = "if")
    private Boolean isFolder;
    
    @KeyName(abbr = "c")
    private RFile[] chirldren;
    
    @KeyName(abbr = "p")
    private RFile parent;
    
    @KeyName(abbr = "n")
    private String name;
    
    @KeyName(abbr = "i")
    private String id;
    
    @KeyName(abbr = "u")
    private String url;
    
    public Boolean getIsRoot()
    {
        return isRoot;
    }
    
    public void setIsRoot(Boolean isRoot)
    {
        this.isRoot = isRoot;
    }
    
    public Boolean getIsFolder()
    {
        return isFolder;
    }
    
    public void setIsFolder(Boolean isFolder)
    {
        this.isFolder = isFolder;
    }
    
    public RFile[] getChirldren()
    {
        return chirldren;
    }
    
    public void setChirldren(RFile[] chirldren)
    {
        this.chirldren = chirldren;
    }
    
    public RFile getParent()
    {
        return parent;
    }
    
    public void setParent(RFile parent)
    {
        this.parent = parent;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getId()
    {
        return id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public String getUrl()
    {
        return url;
    }
    
    public void setUrl(String url)
    {
        this.url = url;
    }
    
}
