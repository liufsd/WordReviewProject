package com.coleman.ojm.core;

import java.util.Date;

import com.coleman.ojm.annotation.ValueObject;

class SLTypeChecker
{
    public static boolean isSimpleType(Class<?> type)
    {
        if (type.isPrimitive() || isWrappedPrimitive(type)
                || type.getName().equals(String.class.getName())
                || type.isEnum() || type.getName().equals(Date.class.getName()))
        {
            return true;
        }
        return false;
    }
    
    public static boolean isVOType(Class<?> type)
    {
        if (type.getAnnotation(ValueObject.class) != null)
        {
            return true;
        }
        return false;
    }
    
    private static boolean isWrappedPrimitive(Class<?> type)
    {
        if (type.getName().equals(Boolean.class.getName())
                || type.getName().equals(Byte.class.getName())
                || type.getName().equals(Character.class.getName())
                || type.getName().equals(Short.class.getName())
                || type.getName().equals(Integer.class.getName())
                || type.getName().equals(Long.class.getName())
                || type.getName().equals(Float.class.getName())
                || type.getName().equals(Double.class.getName()))
        {
            return true;
        }
        return false;
    }
}
