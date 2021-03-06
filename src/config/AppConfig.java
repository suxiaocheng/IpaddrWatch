package config;

import java.io.File;

public class AppConfig {
	public static final int RETRY_TIMES = 100;
	public static final int CONNECTION_TIMEOUT = 30000;
	public static final boolean DEBUG = false;
	public static final String WORKING_DIR = File.separator + "etc" + File.separator + "IpAddr" + File.separator;
	public static final String LOG_DIR = File.separator + "var" + File.separator + "log" + File.separator + "IpAddr"
			+ File.separator;
	public static final String PROGRAM_NAME = System.getProperty("sun.java.command");
}
