import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Enumeration;

import config.AppConfig;
import debug.Log;
import tool.SendEmail;
import tool.SystemInfo;

public class IpaddrWatch {
	private static String Ipaddr = new String();
	private static final String AddrStorageFile = AppConfig.WORKING_DIR + "ipaddr";
	private static final boolean SendMailOnStartup = true;

	public static void main(String[] args) {
		String CurrentIpaddr;
		Calendar calendar;
		String timestamp;
		String hostname = new String();

		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (SendMailOnStartup) {
			// Do nothing
		} else {
			Ipaddr = readToString(AddrStorageFile);
		}

		SystemInfo si = new SystemInfo();
		Thread t1 = new Thread(si);
		t1.start();

		// TODO Auto-generated method stub
		while (true) {
			CurrentIpaddr = getCurrentUsedAddr();
			if ((CurrentIpaddr != null) && (CurrentIpaddr.compareTo(Ipaddr) != 0)) {
				Log.d("System ipaddr change from " + Ipaddr + " to " + CurrentIpaddr);
				Ipaddr = CurrentIpaddr;
				writeStringToFile(AddrStorageFile, Ipaddr);
				calendar = Calendar.getInstance();
				timestamp = "" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "  "
						+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":"
						+ calendar.get(Calendar.SECOND) + "  ";
				SendEmail mail = new SendEmail(AppConfig.PROGRAM_NAME + "(" + hostname + ")", timestamp + Ipaddr, null);
				Thread t2 = new Thread(mail);
				t2.start();
			}
			System.out.println(CurrentIpaddr);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static String getCurrentUsedAddr() {
		String ip = null;
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			ip = socket.getLocalAddress().getHostAddress();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (ip.compareTo("0.0.0.0") == 0) {
			return null;
		}
		return ip;
	}

	private static String getAllUsedAddr() {
		String ip = new String();
		Enumeration<NetworkInterface> enum_network;
		try {
			enum_network = NetworkInterface.getNetworkInterfaces();
			while (enum_network.hasMoreElements()) {
				NetworkInterface n = (NetworkInterface) enum_network.nextElement();
				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					InetAddress i = ee.nextElement();
					ip += i.getHostAddress();
				}
			}
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return ip;
	}

	public static String readToString(String fileName) {
		File file = new File(fileName);
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(filecontent);
			in.close();
		} catch (FileNotFoundException e) {
			Log.i(fileName + " is not exist");
			return new String();
		} catch (IOException e) {
			e.printStackTrace();
			Log.i(fileName + " read error");
			return new String();
		}
		return new String(filecontent);
	}

	public static boolean writeStringToFile(String fileName, String content) {
		File file = new File(fileName);
		if (file.exists()) {
			try {
				file.delete();
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		try {
			FileOutputStream out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.i(fileName + " write error");
			return false;
		}
		return true;
	}

}
