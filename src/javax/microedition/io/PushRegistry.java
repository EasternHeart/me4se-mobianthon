package javax.microedition.io;

import java.io.IOException;

/**
 * @API MIDP-2.0 
 */
public class PushRegistry {

	private PushRegistry() {
	}

	/**
	 * @API MIDP-2.0
	 * @ME4SE UNIMPLEMENTED 
	 */
	public static void registerConnection(String connection, String midlet, String filter) throws ClassNotFoundException, IOException {
		System.out.println("PushRegistry.registerConnection() called with no effect!");
	}

	/**
	 * @API MIDP-2.0
	 * @ME4SE UNIMPLEMENTED 
	 */
	public static boolean unregisterConnection(String connection) {
		System.out.println("PushRegistry.unregisterConnection() called with no effect!");
		return false;
	}

	/**
	 * @API MIDP-2.0
	 * @ME4SE UNIMPLEMENTED 
	 */
	public static String[] listConnections(boolean available) {
		System.out.println("PushRegistry.listConnections() called with no effect!");
		return null;
	}

	/**
	 * @API MIDP-2.0
	 * @ME4SE UNIMPLEMENTED 
	 */
	public static String getMIDlet(String connection) {
		System.out.println("PushRegistry.getMIDlet() called with no effect!");
		return null;
	}

	/**
	 * @API MIDP-2.0
	 * @ME4SE UNIMPLEMENTED 
	 */
	public static String getFilter(String connection) {
		System.out.println("PushRegistry.getFilter() called with no effect!");
		return null;
	}

	/**
	 * @API MIDP-2.0
	 * @ME4SE UNIMPLEMENTED 
	 */
	public static long registerAlarm(String midlet, long time) throws ClassNotFoundException, ConnectionNotFoundException {
		System.out.println("PushRegistry.registerAlarm() called with no effect!");
		return 0L;
	}
}
