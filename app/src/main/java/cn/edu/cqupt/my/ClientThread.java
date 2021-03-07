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

public class ClientThread implements Runnable {
    private Socket mSocket=null;
    private BufferedReader mBufferedReader = null;
    private OutputStream mOutputStream = null;
    private Handler mHandler;
    public Handler revHandler;
    public String tag;
    public boolean flag=true;
    //public boolean isExit=false;
    public ClientThread(Handler handler) {
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
            new Thread() {
                @Override
                public void run() {
                    super.run();

                    while (flag) {
                        String str = null;
                        String content;
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
                            if (str.equals("SIA")) {
                                msg.obj = "SIA";
                                flag = false;
                            } else if (str.equals("HBA")) {
                                flag = false;
                                msg.obj = "HBA";
                            } else if (str.equals("SiA")) {
                                msg.obj = "SiA";
                            } else if (str.equals("BTA")) {
                                msg.obj = "BTA";
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
            }.start();
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
