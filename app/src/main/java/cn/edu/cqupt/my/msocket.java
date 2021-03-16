package cn.edu.cqupt.my;

import android.content.SharedPreferences;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class msocket extends Socket {

    private static final String host = "47.106.207.241";
    private static final int port = 3570;
    private static msocket socket = null;
    private static boolean flag = false;
    private static OutputStream mOutputStream = null;
    private static SharedPreferences sp;
    public static int commonKey;
    public static int publicKey;
    public static int KeyBlockBytes;

    private msocket() {
        super();
    }

    public static msocket getsocket(int a) {

        if (socket == null || a == 0) {
            try {
                socket = new msocket();
                SocketAddress socketAddress = new InetSocketAddress(host, port);
                socket.connect(socketAddress, 3000);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            try {
                socket.getOutputStream().write("ZYXX1226".getBytes());
                socket.getOutputStream().write("RSA11111".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            flag = true;
        }
        return socket;
    }
}
