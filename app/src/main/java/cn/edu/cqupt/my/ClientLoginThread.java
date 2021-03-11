package cn.edu.cqupt.my;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ClientLoginThread implements Runnable {
    private Socket mSocket=null;
    private BufferedReader mBufferedReader = null;
    private OutputStream mOutputStream = null;
    private Handler mHandler;
    public Handler revHandler;
    public String tag;
    public boolean flag=true;
    //public boolean isExit=false;
    public ClientLoginThread(Handler handler) {
        mHandler = handler;
    }
    @Override
    public void run() {
        try {
            mSocket = msocket.getsocket(1);
            Log.d("a", "connect success");
            if (mSocket == null) {
                Message mess = new Message();
                mess.obj = "TC";
                mHandler.sendMessage(mess);
                flag=false;
            }
                else {
                mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                mOutputStream = mSocket.getOutputStream();
                flag = true;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                        String str = null;
                        String content = "";
                        while (flag) {
                            if (!mSocket.isClosed()) {
                                Message msg = new Message();
                                try {
                                    content = mBufferedReader.readLine();
                                    if (content != null) {
                                        str = content.substring(0, 3);
                                    } else {
                                        str = "ER";
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (str == null) {
                                    msg.obj = "ER";
                                    mHandler.sendMessage(msg);
                                    continue;
                                }
                                if(str.contains("RSA"))
                                {
                                    String[] strings=content.substring(108,620).split("_",4);
                                    msocket.commonKey=Integer.parseInt(strings[0]);
                                    msocket.publicKey=Integer.parseInt(strings[1]);
                                    msocket.KeyBlockBytes=Integer.parseInt(strings[2]);
                                    Log.d("RSAKey",msocket.commonKey+"|"+msocket.publicKey+"|"+msocket.KeyBlockBytes);
                                }
                                if (str.contains("SI")) {
                                    msg.obj = "SI";
                                    flag = false;
                                } else if (str.equals("HB")) {
                                    flag = false;
                                    msg.obj = "HB";
                                } else if (str.contains("Si")) {
                                    msg.obj = "Si";
                                } else if (str.equals("BT")) {
                                    msg.obj = "BT";
                                } else {
                                    msg.obj = "ER";
                                }
                                mHandler.sendMessage(msg);
                            } else {
                                Message msg1 = new Message();
                                msg1.obj = "ER";
                                mHandler.sendMessage(msg1);
                            }
                        }
                    }
            }).start();
            Looper.prepare();
            revHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (!mSocket.isClosed()) {
                        if (msg.what == 1) {
                            tag = msg.getData().getString("Checkcode");
                            try {
                                mOutputStream.write(Struct.getBuf(msg.getData().getString("Checkcode"), msg.getData().getString("ID"), msg.getData().getString("PWD"), msg.getData().getString("TalkToid"), msg.getData().getString("REPWD"), msg.getData().getString("data"), null));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            mSocket = msocket.getsocket(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //if(msg.arg2==10)
                    // isExit=true;
                }
            };
            Looper.loop();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("a","");
        }

    }
}
