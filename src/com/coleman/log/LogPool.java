package com.coleman.log;

import java.util.HashMap;

public class LogPool {
	private HashMap<String, Log> map = new HashMap<String, Log>();

	private static LogPool pool = new LogPool();

	private LogPool() {
	}

	public static LogPool getInstance() {
		return pool;
	}

	public void clear() {
		map.clear();
	}

	public Log getLog(String key) {
		return map.get(key);
	}

	public void addLog(String key, Log log) {
		map.put(key, log);
	}
}
