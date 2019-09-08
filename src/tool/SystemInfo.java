package tool;

import java.io.File;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

import com.sun.management.OperatingSystemMXBean;

import config.AppConfig;
import debug.Log;

public class SystemInfo implements Runnable {
	public static boolean ThreadNeedExit = false;
	public String hostname = new String();

	public static final int DISK_FREE_UP_LIMIT = 50;
	public static final int DISK_FREE_DOWN_LIMIT = 10;
	public static final int DISK_FREE_WATCH_STEP = 10;

	public static String getSystemDiskUsage() {
		String ret = new String();
		File[] disks = File.listRoots();
		for (File file : disks) {
			ret += "Path: " + file.getPath();
			ret += ", Total: " + file.getTotalSpace() / 1024 / 1024 + "M";
			ret += ", Free: " + file.getFreeSpace() / 1024 / 1024 + "M\n";
		}
		return ret;
	}

	public static boolean isSystemStroageLow(int percent) {
		boolean status = false;
		File[] disks = File.listRoots();
		int usage_status;
		for (File file : disks) {
			usage_status = (int) (file.getFreeSpace() * 100 / file.getTotalSpace());
			if (usage_status < percent) {
				status = true;
				break;
			}
		}

		return status;
	}

	public static String getSystemMemroy() {
		String ret = new String();
		OperatingSystemMXBean mem = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		ret += "Memory total：" + mem.getTotalPhysicalMemorySize() / 1024 / 1024 + "MB";
		ret += ", available：" + mem.getFreePhysicalMemorySize() / 1024 / 1024 + "MB\n";
		return ret;
	}

	@SuppressWarnings("unused")
	private static String runCommand(String CMD) {
		StringBuilder info = new StringBuilder();
		try {
			Process pos = Runtime.getRuntime().exec(CMD);
			pos.waitFor();
			InputStreamReader isr = new InputStreamReader(pos.getInputStream());
			LineNumberReader lnr = new LineNumberReader(isr);
			String line;
			while ((line = lnr.readLine()) != null) {
				info.append(line).append("\n");
			}
		} catch (Exception e) {
			info = new StringBuilder(e.toString());
		}
		return info.toString();
	}

	public static void main(String[] args) {
		System.out.println(runCommand("df -h"));
		System.out.println(runCommand("free -h"));
		System.out.println(runCommand("uptime"));

		System.out.println("Low disk space: " + isSystemStroageLow(DISK_FREE_DOWN_LIMIT));

		SystemInfo si = new SystemInfo();
		Thread t2 = new Thread(si);
		t2.start();

		while (true)
			;
	}

	public void run() {
		boolean disk_status;
		int disk_percentage = DISK_FREE_UP_LIMIT;
		Calendar calendar;
		String timestamp;
		String mailContent;

		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		while (ThreadNeedExit == false) {
			disk_status = isSystemStroageLow(disk_percentage);
			if (disk_percentage > DISK_FREE_DOWN_LIMIT) {
				if (disk_status == true) {
					Log.d("Disk is check and less than excpet sapace " + disk_percentage);
					calendar = Calendar.getInstance();
					timestamp = "" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "  "
							+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":"
							+ calendar.get(Calendar.SECOND) + "  ";
					mailContent = runCommand("df -h");
					mailContent += "=============================\n";
					mailContent += runCommand("free -h");
					mailContent += "=============================\n";
					mailContent += runCommand("uptime");
					mailContent += "=============================\n";
					SendEmail mail = new SendEmail(AppConfig.PROGRAM_NAME + "(" + hostname + ")",
							timestamp + mailContent, null);
					Thread t2 = new Thread(mail);
					t2.start();

					Log.d("Change disk_percentage to " + disk_percentage);
					disk_percentage -= DISK_FREE_WATCH_STEP;
				}
			}
			
			disk_status = isSystemStroageLow(disk_percentage+DISK_FREE_WATCH_STEP);
			if (disk_status == false) {
				disk_percentage = disk_percentage+DISK_FREE_WATCH_STEP;
				Log.d("Reset disk_percentage to " + disk_percentage);
			}
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("tick");
		}
	}
}
