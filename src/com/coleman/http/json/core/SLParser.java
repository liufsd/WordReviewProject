package com.coleman.http.json.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.coleman.http.json.annotation.KeyName;
import com.coleman.http.json.connection.SLResponse;
import com.coleman.http.json.exception.FieldNotInitException;
import com.coleman.http.json.exception.UnknownTypeException;
import com.coleman.util.Log;

public class SLParser
{
    private static final String TAG = "SLParser";
    
    /**
     * Parse the JSONObject to the java object.
     * 
     * @param jsonRespObj
     * 
     * @param jsonRespObj
     * @return
     * @throws JSONException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws UnknownTypeException
     * @throws FieldNotInitException
     * @throws ClassNotFoundException
     */
    public static void parse(SLResponse<?> slRespObj, JSONObject jsonRespObj)
            throws JSONException, IllegalArgumentException,
            IllegalAccessException, InstantiationException,
            UnknownTypeException, FieldNotInitException, ClassNotFoundException
    {
        JSONParser.parse(slRespObj.getResponse(), jsonRespObj);
        slRespObj.notifyLoaded();
    }
    
    private static class JSONParser
    {
        
        public static void parse(Object obj, JSONObject json)
                throws JSONException, IllegalArgumentException,
                IllegalAccessException, InstantiationException,
                UnknownTypeException, FieldNotInitException,
                ClassNotFoundException
        {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields)
            {
                field.setAccessible(true);
                Class<?> type = field.getType();
                KeyName kn = field.getAnnotation(KeyName.class);
                if (!json.has(kn.abbr()) || json.get(kn.abbr()) == null)
                {
                    Log.w(TAG, "Field Not Returned: "
                            + obj.getClass().getName() + "." + field.getName());
                    makeArrayNotNull(type, field, obj);
                    continue;
                }
                Object subJson = json.get(kn.abbr());
                Log.v(TAG,
                        obj.getClass().getSimpleName() + "." + field.getName()
                                + ": " + subJson);
                if (SLTypeChecker.isSimpleType(type))
                {
                    if (field.getType().getName().equals(Date.class.getName()))
                    {
                        try
                        {
                            field.set(obj,
                                    SLWrapper.format.parse(subJson.toString()));
                        }
                        catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        //                        field.set(obj, subJson);
                        field.set(obj,
                                makeTypeSafeValue(type, subJson.toString()));
                    }
                }
                else if (SLTypeChecker.isVOType(type))
                {
                    Object subObj = type.newInstance();
                    parse(subObj, (JSONObject) subJson);
                    field.set(obj, subObj);
                }
                else if (type.isArray())
                {
                    Log.d(TAG, "Component type: " + type.getComponentType());
                    Object arr = parseJSONArrayToArray(type.getComponentType(),
                            (JSONArray) subJson);
                    if (arr == null && kn.need())
                    {
                        Log.w(TAG,
                                "Field Not Returned: "
                                        + obj.getClass().getName() + "."
                                        + field.getName());
                        continue;
                    }
                    field.set(obj, arr);
                }
                else if (type.getName().equals(List.class.getName()))
                {
                    Log.d(TAG, "Generic type: " + kn.genericType());
                    List<?> list = parseJSONArrayToList(kn.genericType(),
                            (JSONArray) subJson);
                    if (list == null && kn.need())
                    {
                        Log.w(TAG,
                                "Field Not Returned: "
                                        + obj.getClass().getName() + "."
                                        + field.getName());
                        continue;
                    }
                    field.set(obj, list);
                }
                else
                {
                    throw new UnknownTypeException(type.getName());
                }
            }
        }
        
        private static final Object makeTypeSafeValue(Class<?> type,
                String value) throws NumberFormatException,
                IllegalArgumentException
        {
            if (int.class == type || Integer.class == type)
            {
                return Integer.parseInt(value);
            }
            else if (long.class == type || Long.class == type)
            {
                return Long.parseLong(value);
            }
            else if (short.class == type || Short.class == type)
            {
                return Short.parseShort(value);
            }
            else if (char.class == type || Character.class == type)
            {
                return value.charAt(0);
            }
            else if (byte.class == type || Byte.class == type)
            {
                return Byte.valueOf(value);
            }
            else if (float.class == type || Float.class == type)
            {
                return Float.parseFloat(value);
            }
            else if (double.class == type || Double.class == type)
            {
                return Double.parseDouble(value);
            }
            else if (boolean.class == type || Boolean.class == type)
            {
                return Boolean.valueOf(value);
            }
            else if (Date.class == type)
            {
                return new Date(value);
            }
            else
            {
                return value;
            }
        }
        
        /**
         * This method is add to make sure that the array and the list is
         * initialized.
         * <p>
         * Invoke this method is not necessary, actually the business logic
         * should do the null check.
         * 
         * @param type
         * @param field
         * @param obj
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         */
        private static void makeArrayNotNull(Class<?> type, Field field,
                Object obj) throws IllegalArgumentException,
                IllegalAccessException
        {
            if (type.isArray())
            {
                Object arr = Array.newInstance(type.getComponentType(), 0);
                field.set(obj, arr);
            }
            else if (type.getName().equals(List.class.getName()))
            {
                List<Object> list = new ArrayList<Object>();
                field.set(obj, list);
            }
        }
        
        private static Object parseJSONArrayToArray(Class<?> type,
                JSONArray jsonArr) throws ArrayIndexOutOfBoundsException,
                IllegalArgumentException, JSONException,
                IllegalAccessException, InstantiationException,
                UnknownTypeException, FieldNotInitException,
                ClassNotFoundException
        {
            int len = jsonArr == null ? 0 : jsonArr.length();
            Object arr = Array.newInstance(type, len);
            for (int i = 0; i < len; i++)
            {
                if (SLTypeChecker.isSimpleType(type))
                {
                    Array.set(arr, i, jsonArr.get(i));
                    Log.i(TAG,
                            "===coleman-debug-jsonArr.get(i): "
                                    + jsonArr.get(i));
                }
                else if (SLTypeChecker.isVOType(type))
                {
                    Object object = type.newInstance();
                    parse(object, (JSONObject) jsonArr.get(i));
                    Array.set(arr, i, object);
                }
                else
                {
                    throw new UnknownTypeException(type.getName());
                }
            }
            return arr;
        }
        
        private static List<?> parseJSONArrayToList(Class<?> type,
                JSONArray jsonArr) throws IllegalArgumentException,
                JSONException, IllegalAccessException, InstantiationException,
                UnknownTypeException, FieldNotInitException,
                ClassNotFoundException
        {
            int len = jsonArr == null ? 0 : jsonArr.length();
            List<Object> list = new ArrayList<Object>();
            for (int i = 0; i < len; i++)
            {
                if (SLTypeChecker.isSimpleType(type))
                {
                    list.add(jsonArr.get(i));
                }
                else if (SLTypeChecker.isVOType(type))
                {
                    Object object = type.newInstance();
                    parse(object, (JSONObject) jsonArr.get(i));
                    list.add(object);
                }
                else
                {
                    throw new UnknownTypeException(type.getName());
                }
            }
            return list;
        }
        
    }
    
}
