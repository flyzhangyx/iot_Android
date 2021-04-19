package cn.edu.cqupt.my;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.edu.cqupt.my.base.BaseFragmentActivity;

public class iotSceCreateActivity extends BaseFragmentActivity {
    private QMUIGroupListView mGroupListView;
    private QMUITopBarLayout mTopBar;
    private int condIndex = 0;
    private int cmdIndex = 0;
    private boolean isCondComplete = false;

    class Trigger {
        public String DevId;
        public String DevClass;
        public String TriggerData;

        Trigger(String devId, String devClass, String triggerData) {
            DevId = devId;
            DevClass = devClass;
            TriggerData = triggerData;
        }
    }

    class Cond {
        public int index;
        public String DevId;
        public String DevClass;
        public String CondData;

        Cond(int index, String devId, String devClass, String condData) {
            DevId = devId;
            DevClass = devClass;
            CondData = condData;
        }
    }

    class Cmd {
        public String DevId;
        public String DevClass;
        public String CmdData;

        Cmd(String devId, String devClass, String cmdData) {
            DevId = devId;
            DevClass = devClass;
            CmdData = cmdData;
        }
    }

    private Trigger trigger;
    private ArrayList<Cond> condList;
    private ArrayList<Cmd> cmdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_sce_create);
        initTopBar();
        mGroupListView = findViewById(R.id.groupListView1);
        condList = new ArrayList<>();
        cmdList = new ArrayList<>();
        trigger = new Trigger("", "", "");

        /****************************************************************************************/
        QMUICommonListItemView devChoose = mGroupListView.createItemView("选择触发设备:");
        devChoose.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        int i = 0;
        ArrayList<String> DevList = new ArrayList<>();
        for (iotLaunch.DevStore a : iotLaunch.DevStoreList) {
            DevList.add(a.OpenId);
        }
        devChoose.setOnClickListener(v -> {
            showDevList(true, true, "选择触发设备",
                    DevList.size(), true, DevList, devChoose);
        });

        QMUICommonListItemView devClassChoose = mGroupListView.createItemView("选择触发数据类型:");
        devClassChoose.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        devClassChoose.setOnClickListener(v -> {
            if (trigger.DevId.equals("")) {
                Toast.makeText(getContext(), "请先选择设备", Toast.LENGTH_SHORT).show();
            } else {
                iotLaunch.DevStore dev = null;
                for (iotLaunch.DevStore a : iotLaunch.DevStoreList) {
                    if (a.OpenId.equals(trigger.DevId)) {
                        dev = a;
                        break;
                    }
                }
                Set<Integer> key = dev.DevData.keySet();
                ArrayList<String> DevClass = new ArrayList<>();
                for (int Key : key) {
                    DevClass.add("类型：[" + Key + "]|数据：[" + dev.DevData.get(Key) + "]");
                }
                showDevClass(true, true, "选择触发数据类型",
                        DevClass.size(), true, DevClass, devClassChoose);
            }
        });

        QMUICommonListItemView devDataTrigger = mGroupListView.createItemView("填写数据触发条件:");
        devDataTrigger.setOrientation(QMUICommonListItemView.HORIZONTAL);
        devDataTrigger.setOnClickListener(v -> {
            final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getContext());
            builder.setTitle("填写触发条件")
                    .setPlaceholder("[判断符][数据]例:{>50}")
                    .setInputType(InputType.TYPE_CLASS_TEXT)
                    .addAction("取消", (dialog, index) -> dialog.dismiss())
                    .addAction("确定", (dialog, index) -> {
                        String CmdData = builder.getEditText().getText().toString().trim();
                        if (!CmdData.equals("") && CmdData != null && (CmdData.contains(">") || CmdData.contains("=") || CmdData.contains("<"))) {
                            trigger.TriggerData = CmdData;
                            devDataTrigger.setDetailText(CmdData);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "请填入合法触发条件", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        });

        QMUIGroupListView.newSection(getContext())
                .setTitle("触发条件:当某设备发生什么变化时")
                .addItemView(devChoose, null)
                .addItemView(devClassChoose, null)
                .addItemView(devDataTrigger, null)
                .addTo(mGroupListView);

        /********************************************************************/
        QMUICommonListItemView CondAdd = mGroupListView.createItemView("添加");
        CondAdd.setOnClickListener(v -> {
            if (!isCondComplete) {
                final String[] strs = new String[]{"添加[中间条件]", "添加[直接指令]"};
                new QMUIDialog.MenuDialogBuilder(getContext())
                        .addItems(strs, (dialog, index) -> {
                            if (index == 0) {
                                AddCond();
                            } else {
                                isCondComplete = true;
                                AddCmd();
                            }
                            dialog.dismiss();
                        }).create().show();
            } else {
                final String[] strs = new String[]{"添加[直接指令]"};
                new QMUIDialog.MenuDialogBuilder(getContext())
                        .addItems(strs, (dialog, index) -> {
                            AddCmd();
                            dialog.dismiss();
                        }).create().show();
            }
        });
        /**********************************************************************/
        QMUIGroupListView.newSection(getContext())
                .setTitle("添加中间条件或者直接添加执行指令")
                .addItemView(CondAdd, null)
                .addTo(mGroupListView);
    }

    private void AddCond() {
        /****************************************************************************************/
        int Index = condIndex;
        condIndex++;
        Cond cond = new Cond(Index, "", "", "");
        QMUICommonListItemView devChoose = mGroupListView.createItemView("选择条件设备:");
        devChoose.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        ArrayList<String> DevList = new ArrayList<>();
        for (iotLaunch.DevStore a : iotLaunch.DevStoreList) {
            DevList.add(a.OpenId);
        }
        devChoose.setOnClickListener(v -> {
            showDevListCond(true, true, "选择条件设备",
                    DevList.size(), true, DevList, devChoose, cond);
        });

        QMUICommonListItemView devClassChoose = mGroupListView.createItemView("选择条件数据类型:");
        devClassChoose.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        devClassChoose.setOnClickListener(v -> {
            if (cond.DevId.equals("")) {
                Toast.makeText(getContext(), "请先选择设备", Toast.LENGTH_SHORT).show();
            } else {
                iotLaunch.DevStore dev = null;
                for (iotLaunch.DevStore a : iotLaunch.DevStoreList) {
                    if (a.OpenId.equals(cond.DevId)) {
                        dev = a;
                        break;
                    }
                }
                Set<Integer> key = dev.DevData.keySet();
                ArrayList<String> DevClass = new ArrayList<>();
                for (int Key : key) {
                    DevClass.add("类型：[" + Key + "]|数据：[" + dev.DevData.get(Key) + "]");
                }
                showDevClassCond(true, true, "选择条件数据类型",
                        DevClass.size(), true, DevClass, devClassChoose, cond);
            }
        });

        QMUICommonListItemView devDataTrigger = mGroupListView.createItemView("填写中间条件:");
        devDataTrigger.setOrientation(QMUICommonListItemView.HORIZONTAL);
        devDataTrigger.setOnClickListener(v -> {
            final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getContext());
            builder.setTitle("填写中间条件")
                    .setPlaceholder("[判断符][数据]例:{>50}")
                    .setInputType(InputType.TYPE_CLASS_TEXT)
                    .addAction("取消", (dialog, index) -> dialog.dismiss())
                    .addAction("确定", (dialog, index) -> {
                        String CmdData = builder.getEditText().getText().toString().trim();
                        if (!CmdData.equals("") && CmdData != null && (CmdData.contains(">") || CmdData.contains("=") || CmdData.contains("<"))) {
                            cond.CondData = CmdData;
                            devDataTrigger.setDetailText(CmdData);
                            dialog.dismiss();
                            if(condList.size()>Index)
                            {
                                condList.set(Index,cond);
                            }
                           else
                            {
                                condList.add(cond);
                            }
                        } else {
                            Toast.makeText(getContext(), "请填入合法中间条件", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        });

        QMUIGroupListView.newSection(getContext())
                .setTitle("中间条件:当触发条件满足时需经过的中间条件")
                .addItemView(devChoose, null)
                .addItemView(devClassChoose, null)
                .addItemView(devDataTrigger, null)
                .addTo(mGroupListView);
    }

    private void AddCmd() {
        /****************************************************************************************/
        int Index = cmdIndex;
        cmdIndex++;
        Cmd cmd = new Cmd("", "", "");
        QMUICommonListItemView devChoose = mGroupListView.createItemView("选择执行设备:");
        devChoose.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        ArrayList<String> DevList = new ArrayList<>();
        for (iotLaunch.DevStore a : iotLaunch.DevStoreList) {
            DevList.add(a.OpenId);
        }
        devChoose.setOnClickListener(v -> {
            showDevListCmd(true, true, "选择执行设备",
                    DevList.size(), true, DevList, devChoose, cmd);
        });

        QMUICommonListItemView devClassChoose = mGroupListView.createItemView("选择执行设备类型:");
        devClassChoose.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        devClassChoose.setOnClickListener(v -> {
            if (cmd.DevId.equals("")) {
                Toast.makeText(getContext(), "请先选择设备", Toast.LENGTH_SHORT).show();
            } else {
                iotLaunch.DevStore dev = null;
                for (iotLaunch.DevStore a : iotLaunch.DevStoreList) {
                    if (a.OpenId.equals(cmd.DevId)) {
                        dev = a;
                        break;
                    }
                }
                Set<Integer> key = dev.DevData.keySet();
                ArrayList<String> DevClass = new ArrayList<>();
                for (int Key : key) {
                    DevClass.add("类型：[" + Key + "]|数据：[" + dev.DevData.get(Key) + "]");
                }
                showDevClassCmd(true, true, "选择条件数据类型",
                        DevClass.size(), true, DevClass, devClassChoose, cmd);
            }
        });

        QMUICommonListItemView devDataTrigger = mGroupListView.createItemView("填写执行数据:");
        devDataTrigger.setOrientation(QMUICommonListItemView.HORIZONTAL);
        devDataTrigger.setOnClickListener(v -> {
            final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getContext());
            builder.setTitle("填写执行数据")
                    .setPlaceholder("[数据]例:{50}")
                    .setInputType(InputType.TYPE_CLASS_TEXT)
                    .addAction("取消", (dialog, index) -> dialog.dismiss())
                    .addAction("确定", (dialog, index) -> {
                        String CmdData = builder.getEditText().getText().toString().trim();
                        if (!CmdData.equals("") && CmdData != null ) {
                            cmd.CmdData = CmdData;
                            devDataTrigger.setDetailText(CmdData);
                            dialog.dismiss();
                            if(cmdList.size()>Index)
                            {
                                cmdList.set(Index,cmd);
                            }
                            else
                            {
                                cmdList.add(cmd);
                            }
                            Toast.makeText(getContext(),trigger.DevId+"_"+trigger.DevClass+"_"+trigger.TriggerData+"_",Toast.LENGTH_LONG);
                        } else {
                            Toast.makeText(getContext(), "请填入合法执行数据", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        });

        QMUIGroupListView.newSection(getContext())
                .setTitle("执行指令:当上述条件满足时执行的指令")
                .addItemView(devChoose, null)
                .addItemView(devClassChoose, null)
                .addItemView(devDataTrigger, null)
                .addTo(mGroupListView);
    }

    private Context getContext() {
        return iotSceCreateActivity.this;
    }

    private void initTopBar() {
        mTopBar = findViewById(R.id.topbar1);
        mTopBar.addLeftImageButton(R.drawable.qmui_icon_topbar_back, R.id.exit).setOnClickListener(v -> {
            popBackStack();
        });
        mTopBar.addRightImageButton(R.drawable.complete_sce_white_iot, R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cmd = new String();
                int triggerDevKeyId = 0;
                for(iotLaunch.DevStore d:iotLaunch.DevStoreList)
                {
                    if (d.OpenId.trim().equals(trigger.DevId.trim()))
                    {
                        triggerDevKeyId = d.KeyId;
                        break;
                    }
                }
                Toast.makeText(getContext(),trigger.DevId+"|"+triggerDevKeyId,Toast.LENGTH_SHORT).show();
                cmd = cmd + triggerDevKeyId+"-"+trigger.DevClass+"-"+trigger.TriggerData+"-";
                for(Cond c:condList)
                {
                    int condDevKeyId = 0;
                    for(iotLaunch.DevStore d:iotLaunch.DevStoreList)
                    {
                        if (d.OpenId.trim().equals(c.DevId.trim()))
                        {
                            condDevKeyId = d.KeyId;
                            break;
                        }
                    }
                    cmd = cmd + condDevKeyId+"+"+c.DevClass+"+"+c.CondData+"+";
                }
                cmd = cmd + "-";
                ArrayList<String> CmdList = new ArrayList<>();
                for(Cmd c:cmdList)
                {
                    int cmdDevKeyId = 0;
                    for(iotLaunch.DevStore d:iotLaunch.DevStoreList)
                    {
                        if (d.OpenId.trim().equals(c.DevId.trim()))
                        {
                            cmdDevKeyId = d.KeyId;
                            break;
                        }
                    }
                    String Cmd = cmd + cmdDevKeyId +"-"+c.DevClass+"-"+c.CmdData+"-";
                    CmdList.add(Cmd);
                }
                String Arm_Dev_ID = "1234566";
                for(iotLaunch.DevStore d:iotLaunch.DevStoreList)
                {
                    if(d.DevClass == 99)
                    {
                        Arm_Dev_ID = d.OpenId;
                        break;
                    }
                }
                for(String s:CmdList) {
                    Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                    Bundle tmp = new Bundle();
                    tmp.putString("TalkToid", Arm_Dev_ID);
                    tmp.putString("data", s);
                    tmp.putString("Checkcode", "SCE");
                    iotLaunch.sendMessage(tmp);
                }
                popBackStack();
            }
        });
        mTopBar.setTitle("添加智能场景");
    }

    /*******************************************************************************/
    private void showDevList(boolean gravityCenter,
                             boolean addCancelBtn,
                             CharSequence title,
                             int itemCount,
                             boolean allowDragDismiss, ArrayList<String> itemList, QMUICommonListItemView devChoose) {
        QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(getContext());
        builder.setGravityCenter(gravityCenter)
                .setSkinManager(QMUISkinManager.defaultInstance(getContext()))
                .setTitle(title)
                .setAddCancelBtn(addCancelBtn)
                .setAllowDrag(allowDragDismiss)
                .setNeedRightMark(false)
                .setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                    dialog.dismiss();
                    trigger.DevId = itemList.get(position);
                    devChoose.setDetailText(trigger.DevId);
                    trigger.DevClass = "";
                    trigger.TriggerData = "";
                });
        for (int i = 0; i < itemCount; i++) {
            builder.addItem(itemList.get(i));
        }
        builder.build().show();
    }
    /**
     * 使用正则表达式提取中括号中的内容
     * @param msg
     * @return
     */
    public static List<String> extractMessageByRegular(String msg){

        List<String> list=new ArrayList<String>();
        Pattern p = Pattern.compile("(\\[[^\\]]*\\])");
        Matcher m = p.matcher(msg);
        while(m.find()){
            list.add(m.group().substring(1, m.group().length()-1));
        }
        return list;
    }

    private void showDevClass(boolean gravityCenter,
                              boolean addCancelBtn,
                              CharSequence title,
                              int itemCount,
                              boolean allowDragDismiss, ArrayList<String> itemList, QMUICommonListItemView devClassChoose) {
        QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(getContext());
        builder.setGravityCenter(gravityCenter)
                .setSkinManager(QMUISkinManager.defaultInstance(getContext()))
                .setTitle(title)
                .setAddCancelBtn(addCancelBtn)
                .setAllowDrag(allowDragDismiss)
                .setNeedRightMark(false)
                .setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                    dialog.dismiss();
                    trigger.DevClass = extractMessageByRegular(itemList.get(position)).get(0);
                    devClassChoose.setDetailText(trigger.DevClass);
                    trigger.TriggerData = "";
                });
        for (int i = 0; i < itemCount; i++) {
            builder.addItem(itemList.get(i));
        }
        builder.build().show();
    }

    /***********************************************************************************/
    private void showDevListCond(boolean gravityCenter,
                                 boolean addCancelBtn,
                                 CharSequence title,
                                 int itemCount,
                                 boolean allowDragDismiss, ArrayList<String> itemList, QMUICommonListItemView devChoose, Cond cond) {
        QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(getContext());
        builder.setGravityCenter(gravityCenter)
                .setSkinManager(QMUISkinManager.defaultInstance(getContext()))
                .setTitle(title)
                .setAddCancelBtn(addCancelBtn)
                .setAllowDrag(allowDragDismiss)
                .setNeedRightMark(false)
                .setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                    dialog.dismiss();
                    cond.DevId = itemList.get(position);
                    devChoose.setDetailText(cond.DevId);
                    cond.DevClass = "";
                    cond.CondData = "";
                });
        for (int i = 0; i < itemCount; i++) {
            builder.addItem(itemList.get(i));
        }
        builder.build().show();
    }

    private void showDevClassCond(boolean gravityCenter,
                                  boolean addCancelBtn,
                                  CharSequence title,
                                  int itemCount,
                                  boolean allowDragDismiss, ArrayList<String> itemList, QMUICommonListItemView devClassChoose, Cond cond) {
        QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(getContext());
        builder.setGravityCenter(gravityCenter)
                .setSkinManager(QMUISkinManager.defaultInstance(getContext()))
                .setTitle(title)
                .setAddCancelBtn(addCancelBtn)
                .setAllowDrag(allowDragDismiss)
                .setNeedRightMark(false)
                .setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                    dialog.dismiss();
                    cond.DevClass = extractMessageByRegular(itemList.get(position)).get(0);
                    devClassChoose.setDetailText(cond.DevClass);
                    cond.CondData = "";
                });
        for (int i = 0; i < itemCount; i++) {
            builder.addItem(itemList.get(i));
        }
        builder.build().show();
    }
    /**************************************************************************************/
    private void showDevListCmd(boolean gravityCenter,
                                 boolean addCancelBtn,
                                 CharSequence title,
                                 int itemCount,
                                 boolean allowDragDismiss, ArrayList<String> itemList, QMUICommonListItemView devChoose, Cmd cmd) {
        QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(getContext());
        builder.setGravityCenter(gravityCenter)
                .setSkinManager(QMUISkinManager.defaultInstance(getContext()))
                .setTitle(title)
                .setAddCancelBtn(addCancelBtn)
                .setAllowDrag(allowDragDismiss)
                .setNeedRightMark(false)
                .setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                    dialog.dismiss();
                    cmd.DevId = itemList.get(position);
                    devChoose.setDetailText(cmd.DevId);
                    cmd.DevClass = "";
                    cmd.CmdData = "";
                });
        for (int i = 0; i < itemCount; i++) {
            builder.addItem(itemList.get(i));
        }
        builder.build().show();
    }

    private void showDevClassCmd(boolean gravityCenter,
                                  boolean addCancelBtn,
                                  CharSequence title,
                                  int itemCount,
                                  boolean allowDragDismiss, ArrayList<String> itemList, QMUICommonListItemView devClassChoose, Cmd cmd) {
        QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(getContext());
        builder.setGravityCenter(gravityCenter)
                .setSkinManager(QMUISkinManager.defaultInstance(getContext()))
                .setTitle(title)
                .setAddCancelBtn(addCancelBtn)
                .setAllowDrag(allowDragDismiss)
                .setNeedRightMark(false)
                .setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                    dialog.dismiss();
                    cmd.DevClass = extractMessageByRegular(itemList.get(position)).get(0);
                    devClassChoose.setDetailText(cmd.DevClass);
                    cmd.CmdData = "";
                });
        for (int i = 0; i < itemCount; i++) {
            builder.addItem(itemList.get(i));
        }
        builder.build().show();
    }
}