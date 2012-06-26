package com.coleman.log.jdk;

import java.io.File;

import com.coleman.log.Log;
import com.coleman.log.LogFactory;
import com.coleman.log.LogPool;

public class JavaLogFactory implements LogFactory
{
    @Override
    public Log getDefaultLog()
    {
        String key = "default1";
        Log log = LogPool.getInstance().getLog(key);
        if (log == null)
        {
            log = new JavaLogImpl(key);
            LogPool.getInstance().addLog(key, log);
        }
        return log;
    }
    
    @Override
    public Log getLog(String key)
    {
        Log log = LogPool.getInstance().getLog(key);
        if (log == null)
        {
            log = new JavaLogImpl(key);
            LogPool.getInstance().addLog(key, log);
        }
        return log;
    }
    
    @Override
    public Log getDefaultLog(String fileName)
    {
        String key = "default2";
        key = (fileName != null && !fileName.equals("")) ? fileName.replace("/",
                "")
                .replace(".", "")
                : key;
        int limitSize = 1000 * 1024;
        int fileCount = 5;
        
        File file = new File(fileName);
        if (!file.getParentFile().exists())
        {
            file.getParentFile().mkdirs();
        }
        Log log = LogPool.getInstance().getLog(key);
        if (log == null)
        {
            log = new JavaLogImpl(key, fileName, limitSize, fileCount);
            LogPool.getInstance().addLog(key, log);
        }
        return log;
    }
    
    @Override
    public Log getLog(String key, String fileName, int limitSize, int fileCount)
    {
        File file = new File(fileName);
        if (!file.getParentFile().exists())
        {
            file.getParentFile().mkdirs();
        }
        Log log = LogPool.getInstance().getLog(key);
        if (log == null)
        {
            log = new JavaLogImpl(key, fileName, limitSize, fileCount);
            LogPool.getInstance().addLog(key, log);
        }
        return log;
    }
}
