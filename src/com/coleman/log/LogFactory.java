package com.coleman.log;

import com.coleman.log.jdk.JavaLogFactory;

public interface LogFactory {
	/**
	 * Get a log from the log pool, if not found created by key "default".
	 * 
	 * @return the log object.
	 */
	public Log getDefaultLog();

	/**
	 * Get a log from the log pool, if not found created by specified key.
	 * 
	 * @param key
	 *            to specified a log key.
	 * @return the log object.
	 */
	public Log getLog(String key);

	/**
	 * Get a log from the log pool, if not found created by specified
	 * parameters.
	 * 
	 * @param fileName
	 *            where to store.
	 * @param limitSize
	 *            max bytes size of the log file. 0 means not limit.
	 * @param fileCount
	 *            max log file count.
	 * @return the log object.
	 */
	public Log getDefaultLog(String fileName);

	/**
	 * Get a log from the log pool, if not found created by specified
	 * parameters.
	 * 
	 * @param key
	 *            to specified a log.
	 * @param fileName
	 *            where to store.
	 * @param limitSize
	 *            max bytes size of the log file. 0 means not limit.
	 * @param fileCount
	 *            max log file count.
	 * @return the log object.
	 */
	public Log getLog(String key, String fileName, int limitSize, int fileCount);

	public static class Creator {
		public static LogFactory getJavaLogFactory() {
			return new JavaLogFactory();
		}
	}
}
