package ch.ethz.inf.vs.a3.udpclient;

import android.content.SharedPreferences;
import android.util.Log;

public final class NetworkConsts implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = "###networkconsts";
    /**
     * UDP Port
     */
    public static int UDP_PORT = 4446;

    /**
     * Address of the chat server
     *
     * This address is for the emulator.
     */
    public static String SERVER_ADDRESS = "10.0.2.2";

    /**
     * Size of UDP payload in bytes
     */
    public static final int PAYLOAD_SIZE = 1024;

    /**
     * Time to wait for a message in ms
     */
    public static final int SOCKET_TIMEOUT = 100;

    /**
     * Number of times trying after timeout
     */
    public static final int RETRY_COUNT = 5;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SERVER_ADDRESS = sharedPreferences.getString("address", "127.0.0.1");
        UDP_PORT = Integer.parseInt(sharedPreferences.getString("port", "4446"));
        Log.d(LOG_TAG, "prefs changed, addr: " + SERVER_ADDRESS + ", port: " + UDP_PORT);
    }
}
