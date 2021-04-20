package cn.edu.cqupt.my;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUILoadingView;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopups;
import com.qmuiteam.qmui.widget.popup.QMUIQuickAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.edu.cqupt.my.base.BaseFragmentActivity;

public class iotLaunch extends BaseFragmentActivity {
    private static String ID;
    private static String PWD;
    private static TalkThread qClientThread;
    private Handler qHandler;
    private Bundle bundle;
    public static ArrayList<DevStore> DevStoreList = new ArrayList<>();
    private QMUIGroupListView mGroupListView;
    private QMUIPopup mNormalPopup;
    QMUITopBarLayout mTopBar;
    private Timer timer;
    public static String Version = "003";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_launch);
        initTopBar();
        PWD = getSharedPreferences("userconfig", MODE_PRIVATE).getString("PWD", "");
        ID = getSharedPreferences("userconfig", MODE_PRIVATE).getString("ID", "");
        mGroupListView = findViewById(R.id.groupListView);
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Refresh();
            }
        };
        timer.schedule(task, 0,5000);
        qHandler = new Handler() {
            @Override
            public void handleMessage(Message Loginmsg) {
                bundle = Loginmsg.getData();
                Bundle tmp = new Bundle();
                if (Loginmsg.obj.equals("RCI")) {
                    tmp.putString("DevOpenId", bundle.getString("DevOpenId"));
                    tmp.putInt("DevClass", Integer.parseInt(bundle.getString("DevClass")));
                    tmp.putString("DevData", "请刷新");
                    tmp.putString("DevKeyId",bundle.getString("DevKeyId"));
                    addIotDev(tmp);
                    if(bundle.getString("DevOpenId").trim().equals("2609204"))
                    Toast.makeText(getContext(),bundle.getString("DevOpenId")+"|"+bundle.getString("DevKeyId"),Toast.LENGTH_SHORT).show();
                } else if (Loginmsg.obj.equals("IOC")) {
                    Toast.makeText(iotLaunch.this, "添加成功", Toast.LENGTH_SHORT).show();
                }else if (Loginmsg.obj.equals("DSC")) {
                    Toast.makeText(iotLaunch.this, "添加成功", Toast.LENGTH_SHORT).show();
                } else if (Loginmsg.obj.equals("STO")) {
                    LoginActivity.autoSignclear();
                    iotLaunch.this.finish();
                    Intent intent = new Intent(iotLaunch.this, LoginActivity.class);
                    iotLaunch.this.startActivity(intent);
                } else if (Loginmsg.obj.equals("IOT")) {
                    tmp.putString("DevClass", bundle.getString("DevClass"));
                    tmp.putString("DevStatus", bundle.getString("DevStatus"));
                    tmp.putString("DevContent", bundle.getString("DevContent"));
                    tmp.putString("DevContentUpdateTime", bundle.getString("DevContentUpdateTime"));
                    tmp.putString("DevOpenId", bundle.getString("DevOpenId"));
                    updateIotDev(tmp);
                } else if (Loginmsg.obj.equals("ERR")) {
                } else if (Loginmsg.obj.equals("BTT")) {
                } else if (Loginmsg.obj.equals("UPD")) {
                    showupdDialog();
                }
            }
        };

        qClientThread = new TalkThread(qHandler);
        new Thread(qClientThread).start();


        Bundle tmp = new Bundle();
        tmp.putString("TalkToid", "DevOpenId");
        tmp.putString("data", ID);
        tmp.putString("Checkcode", "RCO");
        iotLaunch.sendMessage(tmp);
        tmp.putString("Checkcode", "UPD");
        iotLaunch.sendMessage(tmp);

    }

    private void initTopBar() {
        mTopBar = findViewById(R.id.topbar);
        mTopBar.addRightImageButton(R.drawable.icon_quick_action_share, R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QMUIPopups.quickAction(getContext(),
                        QMUIDisplayHelper.dp2px(getContext(), 56),
                        QMUIDisplayHelper.dp2px(getContext(), 56))
                        .shadow(true)
                        .skinManager(QMUISkinManager.defaultInstance(getContext()))
                        .edgeProtection(QMUIDisplayHelper.dp2px(getContext(), 20))
                        .addAction(new QMUIQuickAction.Action().icon(R.drawable.refresh_iot).text("刷新数据").onClick(
                                (quickAction, action, position) -> {
                                    quickAction.dismiss();
                                    Bundle tmp = new Bundle();
                                    tmp.putString("TalkToid", "DevOpenId");
                                    tmp.putString("data", ID);
                                    tmp.putString("Checkcode", "RCO");
                                    iotLaunch.sendMessage(tmp);
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    for (DevStore d : DevStoreList) {
                                        tmp.putString("TalkToid", d.OpenId);
                                        tmp.putString("data", ID);
                                        tmp.putString("Checkcode", "IOT");
                                        iotLaunch.sendMessage(tmp);
                                    }
                                }
                        ))
                        .addAction(new QMUIQuickAction.Action().icon(R.drawable.add_iot).text("添加设备").onClick(
                                (quickAction, action, position) -> {
                                    quickAction.dismiss();
                                    final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getContext());
                                    builder.setTitle("设备添加")
                                            .setPlaceholder("请在此输入设备ID")
                                            .setInputType(InputType.TYPE_CLASS_TEXT)
                                            .addAction("取消", (dialog, index) -> dialog.dismiss())
                                            .addAction("确定", (dialog, index) -> {
                                                String ID = builder.getEditText().getText().toString().trim();
                                                if (!ID.equals("") && ID != null) {
                                                    boolean flag = false;
                                                    if (ID.length() < 10 && ID.length() >6) {
                                                        for (DevStore d : DevStoreList) {
                                                            if (ID.equals(d.OpenId.trim())) {
                                                                flag = true;
                                                                break;
                                                            }
                                                        }
                                                        if (!flag) {
                                                            Bundle tmp = new Bundle();
                                                            tmp.putString("TalkToid", ID);
                                                            tmp.putString("data", ID);
                                                            tmp.putString("Checkcode", "IOC");
                                                            iotLaunch.sendMessage(tmp);
                                                        } else {
                                                            Toast.makeText(getContext(), "设备:" + ID + " 已经存在", Toast.LENGTH_SHORT).show();
                                                        }
                                                        dialog.dismiss();
                                                    } else {
                                                        Toast.makeText(getContext(), "请填入合法设备ID", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toast.makeText(getContext(), "请填入合法设备ID", Toast.LENGTH_SHORT).show();
                                                }
                                            }).show();
                                }
                        ))
                        .addAction(new QMUIQuickAction.Action().icon(R.drawable.sce_iot).text("智能场景").onClick(
                                (quickAction, action, position) -> {
                                    quickAction.dismiss();
                                    Intent intent = new Intent(getContext(), iotSceCreateActivity.class);
                                    getContext().startActivity(intent);
                                }
                        ))
                        .show(v);
            }
        });
        mTopBar.addLeftImageButton(R.drawable.qmui_icon_topbar_back, R.id.exit).setOnClickListener(v -> {

            logout();
        });

        mTopBar.setTitle("");
    }

    private void addIotDev(final Bundle tmp) {
        for (DevStore d : DevStoreList) {
            if (d.OpenId != null && d.OpenId.equals(tmp.getString("DevOpenId")))
                return;
        }

        int DevClass = tmp.getInt("DevClass");
        String DevOpenId = tmp.getString("DevOpenId");
        String Data = tmp.getString("DevData");
        String keyId = tmp.getString("DevKeyId");

        DevStore devStore = new DevStore(DevClass, DevOpenId,Integer.parseInt(keyId));

        QMUILoadingView loadingView = new QMUILoadingView(iotLaunch.this);
        View.OnClickListener onClickListener = v -> {
            if (v instanceof QMUICommonListItemView) {
                CharSequence text = ((QMUICommonListItemView) v).getText();
                if (text.toString().contains("更新时间")) {
                    loadingView.start();
                    Bundle bundle = new Bundle();
                    bundle.putString("TalkToid", DevOpenId);
                    bundle.putString("data", ID);
                    bundle.putString("Checkcode", "IOT");
                    iotLaunch.sendMessage(bundle);
                } else {
                    //Toast.makeText(iotLaunch.this, text + " is Clicked", Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    bundle.putString("TalkToid", DevOpenId);
                    bundle.putString("data", ID);
                    bundle.putString("Checkcode", "RCO");
                    iotLaunch.sendMessage(bundle);
                }

            }
        };//默认文字在左边   自定义加载框按钮

        QMUIGroupListView.Section tt = QMUIGroupListView.newSection(iotLaunch.this);
        tt.setTitle("设备ID : " + DevOpenId + "|" + keyId);

        QMUICommonListItemView DevUpdateTime = mGroupListView.createItemView("更新时间:");
        DevUpdateTime.setDetailText("点击刷新");
        tt.addItemView(DevUpdateTime, onClickListener);

        QMUICommonListItemView DevName = mGroupListView.createItemView("设备名称:");
        DevName.setDetailText("测试设备：" + DevClass);
        tt.addItemView(DevName, onClickListener);

        QMUICommonListItemView OnlineInfo = mGroupListView.createItemView("在线信息:");
        //OnlineInfo.addAccessoryCustomView(loadingView);
        //OnlineInfo.setDetailText("在右方的详细信息");//默认文字在左边   描述文字在右边
        tt.addItemView(OnlineInfo, onClickListener);


        QMUICommonListItemView DevData = mGroupListView.createItemView("测试数据:");
        DevData.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);//默认文字在左边   右侧更多按钮
        DevData.setOnClickListener(v -> {
            List<String> data = new ArrayList<>();
            for (int key : devStore.DevData.keySet()) {
                if (key != 0)
                    data.add("数据类型: " + key + "\t内容: " + devStore.DevData.get(key));
            }
            if (data.size() == 0) {
                data.add("暂无数据");
            }
            ArrayAdapter adapter = new ArrayAdapter<>(getContext(), R.layout.simple_list_item, data);
            AdapterView.OnItemClickListener onItemClickListener = (adapterView, view, i, l) -> {
                if (mNormalPopup != null) {
                    mNormalPopup.dismiss();
                }
            };
            mNormalPopup = QMUIPopups.listPopup(getContext(),
                    QMUIDisplayHelper.dp2px(getContext(), 250),
                    QMUIDisplayHelper.dp2px(getContext(), 250),
                    adapter,
                    onItemClickListener)
                    .animStyle(QMUIPopup.ANIM_GROW_FROM_RIGHT)
                    .preferredDirection(QMUIPopup.DIRECTION_BOTTOM)
                    .shadow(true)
                    .offsetYIfTop(QMUIDisplayHelper.dp2px(getContext(), 5))
                    //.skinManager(QMUISkinManager.defaultInstance(getContext()))
                    .bgColor(R.color.app_color_blue_disabled)
                    .onDismiss(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            //Toast.makeText(getContext(), "onDismiss", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show(v);
        });
        tt.addItemView(DevData, null);
        devStore.ItemViewList.add(DevData);

        QMUICommonListItemView DevSwitch = mGroupListView.createItemView("设备开关:");
        DevSwitch.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_SWITCH);
        DevSwitch.getSwitch().setOnCheckedChangeListener((buttonView, isChecked) -> {
                IotCmd(0+"_"+(isChecked==true?1:0)+"_",DevOpenId.trim());
                devStore.isDataSetChecked = true;
        });//默认文字在左边   右侧选择按钮
        tt.addItemView(DevSwitch, null);

        tt.addTo(mGroupListView);

        Collections.addAll(devStore.ItemViewList,DevUpdateTime, DevName, DevSwitch, OnlineInfo);
        DevStoreList.add(devStore);
    }

    private Context getContext() {
        return iotLaunch.this;
    }


    private void updateIotDev(final Bundle tmp) {
        for (DevStore d : DevStoreList) {
            if (d.OpenId != null && d.OpenId.equals(tmp.getString("DevOpenId"))) {
                for (QMUICommonListItemView q1 : d.ItemViewList) {
                    if (q1.getText().equals("在线信息:")&&tmp.getString("DevClass").equals("0")) {
                        q1.setDetailText(tmp.getString("DevStatus"));
                    }
                    if(q1.getText().equals("设备开关:")&&tmp.getString("DevClass").equals("0"))
                    {
                        if(!d.isDataSetChecked)
                        {
                            q1.getSwitch().setChecked(tmp.getString("DevStatus").equals("1"));
                        }
                        d.isDataSetChecked = false;
                    }
                    if(q1.getText().equals("更新时间:"))
                    {
                        q1.setDetailText(tmp.getString("DevContentUpdateTime"));
                    }
                }
                if(tmp.getString("DevContent").equals("status"))
                    d.DevData.put(Integer.parseInt(tmp.getString("DevClass")), tmp.getString("DevStatus"));
                else
                    d.DevData.put(Integer.parseInt(tmp.getString("DevClass")), tmp.getString("DevContent"));
            }
        }
    }

    /*******************************************TCP**发送信息给子线程****************************************/
    public static void sendMessage(final Bundle a1) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Message sendLoginmsg = new Message();
                try {
                    Bundle bundle1 = new Bundle();
                    sendLoginmsg.what = 1;
                    // mEtusername.setText(namestring);
                    bundle1.putString("ID", ID);
                    bundle1.putString("Checkcode", a1.getString("Checkcode"));
                    bundle1.putString("TalkToid", a1.getString("TalkToid"));
                    bundle1.putString("data", a1.getString("data"));
                    bundle1.putString("PWD", PWD);
                    bundle1.putString("REPWD", "NULL");
                    sendLoginmsg.setData(bundle1);
                    qClientThread.revHandler.sendMessage(sendLoginmsg);
                    //Toast.makeText(LoginActivity.this,"1", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0);
    }

    public void showupdDialog() {
        /*updateDialog = new updateDialog(this,R.layout.activity_main,onClickListener);
        updateDialog.show();*/
        new QMUIDialog.MessageDialogBuilder(iotLaunch.this)
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

    class DevStore {
        public boolean isDataSetChecked = false;
        public final int KeyId;
        public final int DevClass;
        public final String OpenId;
        public ArrayList<QMUICommonListItemView> ItemViewList = new ArrayList<>();
        public HashMap<Integer, String> DevData = new HashMap<>();

        DevStore(int DevClass, String OpenId ,int keyId) {
            this.DevClass = DevClass;
            this.OpenId = OpenId;
            this.KeyId = keyId;
        }

    }

    private void logout() {
        QMUIDialog.CheckBoxMessageDialogBuilder logoutDlg = new QMUIDialog.CheckBoxMessageDialogBuilder(iotLaunch.this);
        logoutDlg.setChecked(true)
                .setMessage("清除此账号本地数据")
                .setTitle("注销登录")
                .setCancelable(false)
                .addAction("取消", (dialog, index) -> dialog.dismiss()).addAction("确认", new QMUIDialogAction.ActionListener() {
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
                if (logoutDlg.isChecked()) {
                    LoginActivity.autoSignclear();
                }
                iotLaunch.sendMessage(s);
                dialog.dismiss();
            }
        }).show();
    }

    private void Refresh() {
        Bundle tmp = new Bundle();
        tmp.putString("TalkToid", "DevOpenId");
        tmp.putString("data", ID);
        tmp.putString("Checkcode", "RCO");
        SendMessageNoThread(tmp);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (DevStore d : DevStoreList) {
            tmp.putString("TalkToid", d.OpenId);
            tmp.putString("data", ID);
            tmp.putString("Checkcode", "IOT");
            SendMessageNoThread(tmp);
        }
    }

    private void SendMessageNoThread(final Bundle a1)
    {
        Message sendLoginmsg = new Message();
        try {
            Bundle bundle1 = new Bundle();
            sendLoginmsg.what = 1;
            // mEtusername.setText(namestring);
            bundle1.putString("ID", ID);
            bundle1.putString("Checkcode", a1.getString("Checkcode"));
            bundle1.putString("TalkToid", a1.getString("TalkToid"));
            bundle1.putString("data", a1.getString("data"));
            bundle1.putString("PWD", PWD);
            bundle1.putString("REPWD", "NULL");
            sendLoginmsg.setData(bundle1);
            qClientThread.revHandler.sendMessage(sendLoginmsg);
            //Toast.makeText(LoginActivity.this,"1", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void IotCmd(String Cmd,String IotId)
    {
        new Handler().postDelayed(() -> {
            Message sendLoginmsg = new Message();
            try {
                Bundle bundle1 = new Bundle();
                sendLoginmsg.what = 1;
                bundle1.putString("ID", ID);
                bundle1.putString("Checkcode", "ICM");
                bundle1.putString("TalkToid", IotId);
                bundle1.putString("data", Cmd);
                bundle1.putString("PWD", PWD);
                bundle1.putString("REPWD", "NULL");
                sendLoginmsg.setData(bundle1);
                qClientThread.revHandler.sendMessage(sendLoginmsg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0);
    }
}

/*QMUICommonListItemView normalItem = mGroupListView.createItemView("Item 1");
        normalItem.setOrientation(QMUICommonListItemView.VERTICAL); //默认文字在左边

        QMUICommonListItemView itemWithDetail = mGroupListView.createItemView("Item 2");
        itemWithDetail.setDetailText("在右方的详细信息");//默认文字在左边   描述文字在右边

        QMUICommonListItemView itemWithDetailBelow = mGroupListView.createItemView("Item 3");
        itemWithDetailBelow.setOrientation(QMUICommonListItemView.VERTICAL);
        itemWithDetailBelow.setDetailText("在标题下方的详细信息");//默认文字在左边   描述文字在标题下边

        QMUICommonListItemView itemWithChevron = mGroupListView.createItemView("Item 4");
        itemWithChevron.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);//默认文字在左边   右侧更多按钮


        QMUICommonListItemView itemWithSwitch = mGroupListView.createItemView("Item 5");
        itemWithSwitch.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_SWITCH);
        itemWithSwitch.getSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(iotLaunch.this, "checked = " + isChecked, Toast.LENGTH_SHORT).show();
            }
        });//默认文字在左边   右侧选择按钮

        QMUICommonListItemView itemWithCustom = mGroupListView.createItemView("Item 6");
        itemWithCustom.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
        QMUILoadingView loadingView = new QMUILoadingView(iotLaunch.this);
        itemWithCustom.addAccessoryCustomView(loadingView);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof QMUICommonListItemView) {
                    CharSequence text = ((QMUICommonListItemView) v).getText();
                    Toast.makeText(iotLaunch.this, text + " is Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        };//默认文字在左边   自定义加载框按钮

        QMUIGroupListView.newSection(iotLaunch.this)
                .setTitle("Section 1: 默认提供的样式")
                .setDescription("Section 1 的描述")
                .addItemView(normalItem, onClickListener)
                .addItemView(itemWithDetail, onClickListener)
                .addItemView(itemWithDetailBelow, onClickListener)
                .addItemView(itemWithChevron, onClickListener)
                .addItemView(itemWithSwitch, onClickListener)
                .addTo(mGroupListView);

        QMUIGroupListView.newSection(iotLaunch.this)
                .setTitle("Section 2: 自定义右侧 View")
                .addItemView(itemWithCustom, onClickListener)
                .addTo(mGroupListView);*/