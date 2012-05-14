package com.coleman.http.json.test;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.coleman.http.json.connection.SLRequest;
import com.coleman.http.json.connection.SLResponse;
import com.coleman.http.json.core.SLParser;
import com.coleman.http.json.core.SLWrapper;
import com.coleman.http.json.exception.FieldNotInitException;
import com.coleman.http.json.exception.UnknownTypeException;
import com.coleman.http.json.test.TestBean.InnerBean;

public class TestJSONTool {
	private static final String TAG = "TestJSONTool";

	public static String testWrap() {
		TestBean bean = new TestBean();
		try {
			Message msg = Message.obtain();
			// wrap to json object
			JSONObject json = SLWrapper.wrap(new SLRequest<TestBean>(bean));
			Log.i(TAG, "===coleman-debug-json:" + json.toString());
			return json.toString();

		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnknownTypeException e) {
			e.printStackTrace();
		} catch (FieldNotInitException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String testParse(String jsonStr) {
		try {
			JSONObject jsonObj = new JSONObject(new JSONTokener(jsonStr));
			// parse json string to bean
			SLResponse<TestBean> slobj = new SLResponse<TestBean>(
					TestBean.class);
			TestBean resp = slobj.getResponse();
			resp.setDate(null);
			resp.setAge(null);
			resp.setFee(null);
			resp.setList(null);
			resp.setStrs(null);
			resp.setBeans(null);
			slobj.setResponse(resp);
			SLParser.parse(slobj, jsonObj);
			return resp.toString();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (UnknownTypeException e) {
			e.printStackTrace();
		} catch (FieldNotInitException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
