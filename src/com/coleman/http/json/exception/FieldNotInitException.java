package com.coleman.http.json.exception;

public class FieldNotInitException extends Exception
{
    
    private static final long serialVersionUID = -2455184065134356677L;
    
    public FieldNotInitException(boolean isRemote, String className,
            String filedName)
    {
        super((isRemote ? "Remote " : "Local ")
                + "filed need to be initialized: " + className + "."
                + filedName);
    }
}
