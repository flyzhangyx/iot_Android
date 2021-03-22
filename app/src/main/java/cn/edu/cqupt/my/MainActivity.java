package cn.edu.cqupt.my;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.litepal.LitePal;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity {
    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }
    RadioGroup mRgBottomMenu;
    public static boolean network;
    //数组 存储Fragment
    Fragment[] mFragments;
    //当前Fragent的下标
    public static String version="002";
    private Handler qHandler,udpHandler;
    private TextView name;
    private View Drawtop;
    private boolean isExit;
    private NavigationView navigationViewq;
    private static DrawerLayout drawerLayoutq;
    private static TalkThread qClientThread;
    private static UdpSocket udpthred;
    private Bundle bundle;
    private Bundle bundle1;
    private int currentIndex;
    private static int flag;
    private String j="";
    private  static String ID;
    private  static String PWD;
    public static Context ctx;
    private final Timer timer = new Timer();
    private TimerTask task;
    private  static SharedPreferences sp;
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkchanggereceiver;
//    private ImageView imageView;
    private static boolean flag_up_pic=false;
    private static WebView user_head_webview;
    private TextView useridtext;
    private ImageView DrawBackImg;
    private InputStream objectimg;
    public static boolean update_pic=false;
    //private Get_User_pic_task taskpic;
    private boolean pic_yes=false;
    private boolean pic_f=false;
    private updateDialog updateDialog;
    //private  View.OnClickListener onClickListener;
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        timer.cancel();
        ActivityCollector.deleteActivity(this);
        //UserContext.ClearContact();
        LitePal.deleteAll(Contact.class,"UserID==?",ID);
        LitePal.deleteAll(IoTdevice.class,"User_ID==?",ID);
        isExit=false;
        unregisterReceiver(networkchanggereceiver);
    }

        /*************************************/
        class  NetworkChangeReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(@NonNull Context context,Intent intent){
                if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
                {

                }
                /*ConnectivityManager connectionManager= (ConnectivityManager) getSystemService (Context.NETWORK_STATS_SERVICE);
                NetworkInfo networkInfo=connectionManager.getActiveNetworkInfo();
                if(networkInfo!=null ){
                    if(networkInfo.getExtraInfo()!=null)
                   network=true;
                }else
                {
                    network=false;
                }*/
                network = false;
                Home.Netset(network);
            }
        }
        /*****************************************************/
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            UserContext.UsercontactRefresh();
            Bundle s=new Bundle();
            s.putString("Checkcode","RME");
            s.putString("data","");
            s.putString("TalkToid","");
            MainActivity.sendMessage(s);
            super.handleMessage(msg);
        }
    };
    @LayoutRes
    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_main;
    }
    @Override
    protected void initFindViewById() {
        mRgBottomMenu =  findViewById(R.id.rg_bottom_menu);
    }
    @Override
    protected void initViews() {
        //将Fragment加入数组
        isExit = true;
        ctx = MainActivity.this;
        // Toast.makeText(MainActivity.this,LoginActivity.GetLoginActivvitycontext().toString(),Toast.LENGTH_SHORT).show();
        sp = MainActivity.this.getSharedPreferences("userconfig", MODE_PRIVATE);
        LitePal.getDatabase();
        flag = 0;

        PWD = getSharedPreferences("userconfig", MODE_PRIVATE).getString("PWD", "");
        ID = getSharedPreferences("userconfig", MODE_PRIVATE).getString("ID", "");
        ActivityCollector.addActivity(this);
        ///********************************************************
        navigationViewq = findViewById(R.id.design_navigation_viewq);
        drawerLayoutq = findViewById(R.id.drawlayout_main);
        Drawtop = navigationViewq.getHeaderView(0);
        user_head_webview=Drawtop.findViewById(R.id.user_head);
        useridtext=Drawtop.findViewById(R.id.drawer_header_userName);
        //imageView = Drawtop.findViewById(R.id.drawer_header_userHead);
        name = Drawtop.findViewById(R.id.drawer_header_userName);
        name.setText(MainActivity.GetID());
        /********/
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkchanggereceiver = new NetworkChangeReceiver();
        registerReceiver(networkchanggereceiver, intentFilter);
        /*******/
        initWebView();
        user_head_webview.setWebViewClient(new WebViewClient(){

        });


        String postdata="user="+LoginConfig.getID(MainActivity.ctx)+"&"+"password="+LoginConfig.getPWD(MainActivity.ctx);
        byte[] postbyte=postdata.getBytes();
        user_head_webview.postUrl("http://uubang.flyzhangyx.com/user_head.php",postbyte);
        useridtext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle s=new Bundle();
                s.putString("DestUrl","http://uubang.flyzhangyx.com/upload_user_head.php");
                s.putString("POST","POST");
                WebviewActivity.Actionstart(MainActivity.this,s);
            }
        });
        /************************************************/

        /*******************************************/
        navigationViewq.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int index = menuItem.getItemId();
                if (index == R.id.drawer_menu_CZZ) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    //intent.setData(Uri.parse("http://qm.qq.com/cgi-bin/qm/qr?k=gsLtROoZae2Uo0gvUWNTVtqJ8kEGuJ_y"));
                    intent.setData(Uri.parse("http://flyzhangyx.com"));
                    startActivity(intent);
                } else if (index == R.id.drawer_menu_FWGW) {
                    Bundle a = new Bundle();
                    a.putString("DestUrl", "http://flyzhangyx.com/gw.html");
                    WebviewActivity.Actionstart(MainActivity.this, a);
                } else if (index == R.id.drawer_menu_QFK) {
                    Bundle a = new Bundle();
                    a.putString("DestUrl", "https://jinshuju.net/f/DGMYkQ");
                    WebviewActivity.Actionstart(MainActivity.this, a);
                } else if (index == R.id.drawer_menu_update) {
                    CheckAppUpdate();
                } else if (index == R.id.drawer_menu_logout) {
                    final QMUIDialog.CheckBoxMessageDialogBuilder logoutDlg = new QMUIDialog.CheckBoxMessageDialogBuilder(ctx);
                    logoutDlg.setChecked(true)
                            .setMessage("清除此账号本地数据")
                            .setTitle("注销登录")
                            .setCancelable(false)
                            .addAction("取消", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            }).addAction("确认", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            Bundle s = new Bundle();
                            s.putString("TalkToid", " ");
                            s.putString("Checkcode", "STO");
                            s.putString("data", " ");
                            s.putString("ID", "00000000000");
                            s.putString("PWD", "000000");
                            s.putString("REPWD", "NULL");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 500);
                            if(logoutDlg.isChecked())
                            {
                                Smartlife.ClearContact();
                                Home.ClearNmsg();
                                LoginActivity.autoSignclear();
                            }
                            MainActivity.sendMessage(s);
                            dialog.dismiss();
                            // IoTcmdActivity.recclear();

                        }
                    }).show();
                }
                return true;
            }
        });
        ///**********************************************************
        mFragments = new Fragment[]{
                //主页、新闻、图片、视频、个人
                new Home(),
                new UserContext(),
                new Person()
        };

        UserContext.UsercontactRefresh();
        //Home.RefreshUnreadMsg();
        Bundle sd = new Bundle();
        sd.putString("TalkToid", "");
        sd.putString("Checkcode", "RME");
        sd.putString("data", "");
        MainActivity.sendMessage(sd);

        //******************************************************
        /**timer**/

        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
                /*******获取头像********/
                if(update_pic){
                    String postdata="user="+LoginConfig.getID(MainActivity.ctx)+"&"+"password="+LoginConfig.getPWD(MainActivity.ctx);
                    byte[] postbyte=postdata.getBytes();
                    user_head_webview.postUrl("http://uubang.flyzhangyx.com/user_head.php",postbyte);
                    update_pic=false;
                }
                /**********************/
            }
        };
        timer.schedule(task, 5000, 10000);
        /****************************************************处理子线程发过来的TCP事物**************************************************/
        bundle = new Bundle();
        bundle1 = new Bundle();
        qHandler = new Handler() {
            @Override
            public void handleMessage(Message Loginmsg) {
                bundle = Loginmsg.getData();
                if (Loginmsg.obj.equals("TAS")) {
                    bundle.putString("Checkcode", "TAS");
                    if (flag == 1) {
                        TalkActivity.recvMessage(bundle);
                    }
                    //UserContext.AddNewContext(bundle.getString("TalkToid"));
                } else if (Loginmsg.obj.equals("TAN")) {
                    bundle.putString("Checkcode", "TAN");
                    if (flag == 1) {
                        TalkActivity.recvMessage(bundle);
                    }
                    //UserContext.AddNewContext(bundle.getString("TalkToid"));
                } else if (Loginmsg.obj.equals("ADS")) {
                    Contact context = new Contact();
                    context.SetContact(ID, bundle.getString("TalkToid"));
                    context.save();
                    UserContext.AddNewContext(bundle.getString("TalkToid"));

                } else if (Loginmsg.obj.equals("ADN") || Loginmsg.obj.equals("ADD")) {
                    Toast.makeText(MainActivity.this, "请求发送成功", Toast.LENGTH_SHORT).show();
                } else if (Loginmsg.obj.equals("ADT")) {
                    /*NewMsgUnread s=new NewMsgUnread();
                    s.setUnreadMsg("RCO",MainActivity.GetID(),bundle.getString("TalkToid"),0);
                    s.save();*/
                    Home.AddNewUnreadMsg(bundle.getString("TalkToid"), 0, "RCO");

                } else if (Loginmsg.obj.equals("Taa")) {
                    bundle.putString("Checkcode", "Taa");
                    TalkActivity.recvMessage(bundle);
                } else if (Loginmsg.obj.equals("RCO")) {
                    Contact s = new Contact();
                    s.SetContact(ID, bundle.getString("TalkToid"));
                    s.save();
                    //UserContext.AddNewContext(Loginmsg.getData().getString("TalkToid"));
                } else if (Loginmsg.obj.equals("OUT")) {
                    IoTdevice s = new IoTdevice();
                    s.SetContact(ID, bundle.getString("Talktoid"), "NUL", "NUL", 2);
                    if (LitePal.where("Talk_ToId=?", bundle.getString("Talktoid")).find(IoTdevice.class).size() == 0)
                        s.save();
                    else
                        s.updateAll("User_ID = ? and Talk_ToId = ?", ID, bundle.getString("Talktoid"));
                    //Smartlife.upiot(bundle.getString("TalkToid"),0);
                    // Toast.makeText(MainActivity.this,"???",Toast.LENGTH_SHORT).show();

                } else if (Loginmsg.obj.equals("ONL")) {
                    IoTdevice s = new IoTdevice();
                    s.SetContact(ID, bundle.getString("Talktoid"), bundle.getString("temp"), bundle.getString("water"), 1);
                    if (LitePal.where("Talk_ToId=?", bundle.getString("Talktoid")).find(IoTdevice.class).size() == 0)
                        s.save();
                    else
                        s.updateAll("User_ID = ? and Talk_ToId = ?", ID, bundle.getString("Talktoid"));
                    //Smartlife.upiot(bundle.getString("TalkToid"),1);
                } else if (Loginmsg.obj.equals("TAI")) {

                    IoTdevice s = new IoTdevice();
                    s.SetContact(ID, bundle.getString("Talktoid"), bundle.getString("temp"), bundle.getString("water"), 1);
                    if (LitePal.where("Talk_ToId=?", bundle.getString("Talktoid")).find(IoTdevice.class).size() == 0)
                        s.save();
                    else
                        s.updateAll("User_ID = ? and Talk_ToId = ?", ID, bundle.getString("Talktoid"));
                    //Smartlife.upiot(bundle.getString("TalkToid"),1);
                    Toast.makeText(MainActivity.this, bundle.getString("Talktoid") + "状态更新，请尝试刷新", Toast.LENGTH_SHORT).show();
                } else if (Loginmsg.obj.equals("NME")) {
                    UserMessage message = new UserMessage();
                    message.setUser(MainActivity.GetID(), bundle.getString("TalkToid"), bundle.getString("data"), Msg.Type_recv, Calendar.getInstance().get(Calendar.YEAR) + Calendar.getInstance().get(Calendar.YEAR) + Calendar.getInstance().get(Calendar.MONTH) + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + Calendar.getInstance().get(Calendar.MINUTE) + Calendar.getInstance().get(Calendar.SECOND) + "");
                    message.save();

                    Home.AddNewUnreadMsg(bundle.getString("TalkToid"), 0, "MSG");

                } else if (Loginmsg.obj.equals("NCA")) {

                    Home.AddNewUnreadMsg(bundle.getString("TalkToid"), 0, "RCO");
                  /*  Contact s=new Contact();
                    s.SetContact(ID,Loginmsg.getData().getString("TalkToid"));
                    UserContext.AddNewContext(Loginmsg.getData().getString("TalkToid"));*/
                } else if (Loginmsg.obj.equals("STO")) {
                    LoginActivity.autoSignclear();
                    Bundle bundleq = new Bundle();
                    sendMessageudp(bundleq, 2);
                    MainActivity.this.finish();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    //Home.ClearNmsg();
                    MainActivity.this.startActivity(intent);
                } else if (Loginmsg.obj.equals("Add")) {
                    //bundle.putString("Checkcode","Add");
                    Toast.makeText(MainActivity.this, "用户不存在", Toast.LENGTH_SHORT).show();
                } else if (Loginmsg.obj.equals("ADD")) {
                    //bundle.putString("Checkcode","Add");
                    Toast.makeText(MainActivity.this, "对方暂不在线，请求发送成功", Toast.LENGTH_SHORT).show();
                } else if (Loginmsg.obj.equals("TAT")) {
                    UserMessage message = new UserMessage();
                    message.setUser(MainActivity.GetID(), bundle.getString("TalkToid"), bundle.getString("data"), Msg.Type_recv, Calendar.getInstance().get(Calendar.YEAR) + Calendar.getInstance().get(Calendar.YEAR) + Calendar.getInstance().get(Calendar.MONTH) + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + Calendar.getInstance().get(Calendar.MINUTE) + Calendar.getInstance().get(Calendar.SECOND) + "");
                    message.save();
                    Home.AddNewUnreadMsg(bundle.getString("TalkToid"), 0, "MSG");
                    if (flag == 1) {
                        bundle.putString("Checkcode", "TAT");
                        TalkActivity.recvMessage(bundle);
                    }
                } else if (Loginmsg.obj.equals("TAi")) {
                    Toast.makeText(MainActivity.this, "下发成功", Toast.LENGTH_SHORT).show();
                } else if (Loginmsg.obj.equals("TNI")) {
                    Toast.makeText(MainActivity.this, "设备连接丢失，请刷新", Toast.LENGTH_SHORT).show();
                    Smartlife.UsercontactRefresh();
                    //Smartlife.AddNewContext(bundle.getString("TalkToid"),0);
                } else if (Loginmsg.obj.equals("PNG")) {
                    Toast.makeText(MainActivity.this, "PNG", Toast.LENGTH_SHORT).show();
                    bundle1.putString("TalkToid", getIntent().getExtras().getString("12345678900"));
                    bundle1.putString("data", "");
                    bundle1.putString("Checkcode", "CTU");
                    MainActivity.sendMessage(bundle1);
                } else if (Loginmsg.obj.equals("ERR")) {
                    // Toast.makeText(MainActivity.this,bundle.getString("data")+"vv",Toast.LENGTH_SHORT).show();
                   // Toast.makeText(MainActivity.this, "网络连接超时，请尝试刷新", Toast.LENGTH_SHORT).show();
                } else if (Loginmsg.obj.equals("BTT")) {
                    // Toast.makeText(MainActivity.this,bundle.getString("data")+"vv",Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MainActivity.this,"网络连接超时",Toast.LENGTH_SHORT).show();
                }
                else if (Loginmsg.obj.equals("UPD")) {
                    showupdDialog();
                }
            }
        };

        qClientThread = new TalkThread(qHandler);
        new Thread(qClientThread).start();
        /***************************************************TCP线程***********************************************/


        /**************************************************处理子线程发过来的UDP事物*********************************************/
       // udpHandler = new Handler() {
//            @Override
//            public void handleMessage(Message Loginmsg) {

//                if (Loginmsg.obj.equals("PNG")) {
//                    //Toast.makeText(MainActivity.this,"PNG:"+j+"_"+Loginmsg.getData().getString("info").getBytes()[0]+"/"+Loginmsg.getData().getString("info").getBytes()[1],Toast.LENGTH_SHORT).show();
//                    //UserContext.AddNewContext(bundle.getString("TalkToid"));
//                    j = j + Loginmsg.getData().getString("info").getBytes()[0];
//                   // Toast.makeText(MainActivity.this, j, Toast.LENGTH_SHORT).show();
//                    if (j.equals("123456789101112131415161718")) {
//                        j = "";
//                    }
//                } else if (Loginmsg.obj.equals("CON")) {
//                   // Toast.makeText(MainActivity.this, "UDP_CON", Toast.LENGTH_SHORT).show();
//                }else if (Loginmsg.obj.equals("UPD")) {
//                        Toast.makeText(MainActivity.this, "应用有新版本", Toast.LENGTH_SHORT).show();
//                        showupdDialog();
//                } else {
//                   // Toast.makeText(MainActivity.this, "bug", Toast.LENGTH_SHORT).show();
//                }
           // }
       // };
        //udpthred = new UdpSocket(udpHandler);
        //new Thread(udpthred).start();
        /****************************************************UDP**************************************************/

        /******************************************************/
        //开启事务
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //设置为默认界面 MainHomeFragment
        ft.add(R.id.main_content, mFragments[0]).commit();
        //RadioGroup选中事件监听 改变fragment
        mRgBottomMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_home:
                        setIndexSelected(0);
                        break;
                    case R.id.rb_news:
                        setIndexSelected(1);
                        break;
                    case R.id.rb_person:
                        setIndexSelected(2);
                        break;
                }
            }
        });
//        Intent intent = new Intent(MainActivity.this, Welcome2.class);
//        MainActivity.this.startActivity(intent);
    }


    /**************************使用到的自定义类函数***************************/
    public static  void Reconnect_a()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Bundle bundle=new Bundle();
                if (!LoginConfig.getID(ctx).equals("")) {
                    Message sendLoginmsg = new Message();
                    try {
                        sendLoginmsg.what = 1;
                        String namestring=LoginConfig.getID(ctx);
                        String passwordstring=LoginConfig.getPWD(ctx);
                        // mEtusername.setText(namestring);
                        bundle.putString("ID", namestring);
                        bundle.putString("Checkcode", "SI");
                        bundle.putString("TalkToid", "0000");
                        bundle.putString("data","NULL");
                        bundle.putString("PWD", passwordstring);
                        bundle.putString("REPWD", "NULL");
                        sendLoginmsg.setData(bundle);
                        qClientThread.revHandler.sendMessage(sendLoginmsg);
                        //Toast.makeText(LoginActivity.this,"1", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 100);
    }
    //设置Fragment页面
    private void setIndexSelected(int index) {
        if (currentIndex == index) {
            return;
        }
        //开启事务
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //隐藏当前Fragment
        ft.hide(mFragments[currentIndex]);
        //判断Fragment是否已经添加
        if (!mFragments[index].isAdded()) {
            ft.add(R.id.main_content,mFragments[index]).show(mFragments[index]);
        }else {
            //显示新的Fragment
            ft.show(mFragments[index]);
        }
        ft.commit();
        currentIndex = index;
    }
    /*******************************************TCP**发送信息给子线程****************************************/
    public static void sendMessage(final Bundle a1)
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Message sendLoginmsg = new Message();
                try {
                    Bundle bundle1=new Bundle();
                    sendLoginmsg.what = 1;
                    // mEtusername.setText(namestring);
                    bundle1.putString("ID",ID);
                    bundle1.putString("Checkcode",a1.getString("Checkcode"));
                    bundle1.putString("TalkToid",a1.getString("TalkToid"));
                    bundle1.putString("data",a1.getString("data"));
                    bundle1.putString("PWD", PWD);
                    bundle1.putString("REPWD","NULL");
                    sendLoginmsg.setData(bundle1);
                    qClientThread.revHandler.sendMessage(sendLoginmsg);
                    //Toast.makeText(LoginActivity.this,"1", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0);
    }
    /******************************************************************************************************************/
    /*********************************************UDP***发送信息给子线程***********************************************/
    public static void sendMessageudp(final Bundle a1, final int j)
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Message sendLoginmsg = new Message();
                try {
                    Bundle bundle1=new Bundle();
                    sendLoginmsg.what = j;
                    bundle1.putString("ID",ID);
                    bundle1.putString("Checkcode",a1.getString("Checkcode"));
                    bundle1.putString("TalkToid",a1.getString("TalkToid"));
                    bundle1.putString("data",a1.getString("data"));
                    bundle1.putString("PWD", PWD);
                    bundle1.putString("REPWD","NULL");
                    sendLoginmsg.setData(bundle1);
                    udpthred.recHandler.sendMessage(sendLoginmsg);
                    //Toast.makeText(LoginActivity.this,"1", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0);
    }
    /***********************************************************************************************/
    public  static  String GetID()
    {
        return ID;
    }
    public static void Isalive(int a)
    {
        flag=a;
    }
    public static void opencebianlan()
    {
        drawerLayoutq.openDrawer(GravityCompat.START);
         if(!flag_up_pic) {
             flag_up_pic=true;
             new Handler().postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     String postdata = "user=" + LoginConfig.getID(MainActivity.ctx) + "&" + "password=" + LoginConfig.getPWD(MainActivity.ctx);
                     byte[] postbyte = postdata.getBytes();
                     user_head_webview.postUrl("http://uubang.flyzhangyx.com/user_head.php", postbyte);
                     flag_up_pic=false;
                 }
             }, 20000);//dakai cebianlan gengxin touxiang
         }

    }
    public void initWebView() {
        WebSettings settings = user_head_webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setUseWideViewPort(true);
        //自适应屏幕
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
    }
    public void showupdDialog() {
        /*updateDialog = new updateDialog(this,R.layout.activity_main,onClickListener);
        updateDialog.show();*/
        new QMUIDialog.MessageDialogBuilder(ctx)
                .setTitle("应用有新版本")
                .setMessage("****************************************")
                .addAction("稍后手动更新", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("立即下载更新", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        Uri uri = Uri.parse("http://flyzhangyx.com/uubang.apk");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                }).show();
    }

    private void CheckAppUpdate()
    {
        Bundle sd1 = new Bundle();
        sd1.putString("TalkToid", "");
        sd1.putString("Checkcode", "UPD");
        sd1.putString("data", "");
        MainActivity.sendMessage(sd1);
    }
}
