
package com.coleman.ojm.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.coleman.log.Log;
import com.coleman.ojm.annotation.KeyName;
import com.coleman.ojm.exception.FieldNotInitException;
import com.coleman.ojm.exception.UnknownTypeException;
import com.coleman.ojm.http.SLRequest;
import com.coleman.util.Config;

public class SLWrapper {
    private static final String TAG = "SLWrapper";

    private static Log Log = Config.getLog();

    public static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

    public static JSONObject wrap(SLRequest<?> slRequest) throws IllegalArgumentException,
            IllegalAccessException, JSONException, UnknownTypeException, FieldNotInitException {
        Object javaObj = slRequest.getRequest();
        return JSONWrapper.wrap(javaObj);
    }

    private static class JSONWrapper {
        public static JSONObject wrap(Object javaObj) throws IllegalArgumentException,
                IllegalAccessException, JSONException, UnknownTypeException, FieldNotInitException {

            JSONObject jsonObj = new JSONObject();
            Field[] fields = javaObj.getClass().getDeclaredFields();
            for (Field field : fields) {
                KeyName kn = field.getAnnotation(KeyName.class);
                field.setAccessible(true);
                Class<?> type = field.getType();
                if (kn == null) {
                    throw new IllegalArgumentException(field.getName() + " has no abbr");
                }
                if (!isFieldInit(field, javaObj)) {
                    Log.w(TAG,
                            "Field Not init: " + javaObj.getClass().getName() + "."
                                    + field.getName());
                    continue;
                }
                Log.v(TAG, javaObj.getClass().getSimpleName() + "." + field.getName() + ": "
                        + field.get(javaObj));
                if (SLTypeChecker.isSimpleType(type)) {
                    if (type.getName().equals(Date.class.getName())) {
                        jsonObj.put(kn.abbr(), format.format(field.get(javaObj)));
                    } else {
                        jsonObj.put(kn.abbr(), field.get(javaObj));
                    }
                } else if (SLTypeChecker.isVOType(type)) {
                    JSONObject jobj = wrap(field.get(javaObj));
                    jsonObj.put(kn.abbr(), jobj);
                } else if (type.isArray()) {
                    Object arr = field.get(javaObj);
                    JSONArray jsonArray = convertArrayToJSONArray(arr);
                    if (jsonArray == null) {
                        Log.w(TAG,
                                "Field Not init: " + javaObj.getClass().getName() + "."
                                        + field.getName());
                        continue;
                    }
                    jsonObj.put(kn.abbr(), jsonArray);
                } else if (type.getName().equals(List.class.getName())) {
                    List<?> list = (List<?>) field.get(javaObj);
                    JSONArray jsonArray = convertListToJSONArray(list);
                    if (jsonArray == null) {
                        Log.w(TAG,
                                "Field Not init: " + javaObj.getClass().getName() + "."
                                        + field.getName());
                        continue;
                    }
                    jsonObj.put(kn.abbr(), jsonArray);
                } else {
                    throw new UnknownTypeException(type.getName());
                }
            }

            return jsonObj;
        }

        private static boolean isFieldInit(Field field, Object reqBean)
                throws IllegalArgumentException, IllegalAccessException {
            if (field.get(reqBean) == null) {
                return false;
            }
            if (field.getType().getName().equals(byte.class.getName())
                    && field.getByte(reqBean) == Byte.MIN_VALUE) {
                return false;
            }
            if (field.getType().getName().equals(short.class.getName())
                    && field.getShort(reqBean) == Short.MIN_VALUE) {
                return false;
            }
            if (field.getType().getName().equals(int.class.getName())
                    && field.getInt(reqBean) == Integer.MIN_VALUE) {
                return false;
            }
            if (field.getType().getName().equals(long.class.getName())
                    && field.getLong(reqBean) == Long.MIN_VALUE) {
                return false;
            }
            if (field.getType().getName().equals(float.class.getName())
                    && field.getFloat(reqBean) == Float.MIN_VALUE) {
                return false;
            }
            if (field.getType().getName().equals(double.class.getName())
                    && field.getDouble(reqBean) == Double.MIN_VALUE) {
                return false;
            }
            return true;
        }

        private static JSONArray convertArrayToJSONArray(Object objs)
                throws IllegalArgumentException, IllegalAccessException, JSONException,
                UnknownTypeException, FieldNotInitException {
            JSONArray jsonArr = new JSONArray();
            if (objs == null || Array.getLength(objs) == 0) {
                return null;
            }
            for (int i = 0; i < Array.getLength(objs); i++) {
                Object object = Array.get(objs, i);
                if (SLTypeChecker.isSimpleType(object.getClass())) {
                    jsonArr.put(object);
                } else if (SLTypeChecker.isVOType(object.getClass())) {
                    JSONObject json = wrap(object);
                    jsonArr.put(json);
                } else {
                    throw new UnknownTypeException(object.getClass().getName());
                }
            }
            return jsonArr;
        }

        private static JSONArray convertListToJSONArray(List<?> list)
                throws IllegalArgumentException, IllegalAccessException, JSONException,
                UnknownTypeException, FieldNotInitException {
            JSONArray jsonArr = new JSONArray();
            if (list == null || list.size() == 0) {
                return null;
            }
            for (Object object : list) {
                if (SLTypeChecker.isSimpleType(object.getClass())) {
                    jsonArr.put(object);
                } else if (SLTypeChecker.isVOType(object.getClass())) {
                    JSONObject json = wrap(object);
                    jsonArr.put(json);
                } else {
                    throw new UnknownTypeException(object.getClass().getName());
                }
            }
            return jsonArr;
        }
    }

}
