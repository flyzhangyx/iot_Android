package cn.edu.cqupt.my;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.qmuiteam.qmui.arch.QMUIFragmentActivity;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class iotNewTimerSce extends QMUIFragmentActivity {
    private QMUIGroupListView mGroupListView;
    private QMUITopBarLayout mTopBar;
    private int condIndex = 0;
    private int cmdIndex = 0;
    private boolean isCondComplete = false;
    private String Time = "00:00:00";
    private String weekDay = "1111111";
    private Context getContext()
    {
        return this;
    }

    private iotSceCreateActivity.Trigger trigger;
    private ArrayList<iotSceCreateActivity.Cond> condList;
    private ArrayList<iotSceCreateActivity.Cmd> cmdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_new_timer_sce);
        initTopBar();
        mGroupListView = findViewById(R.id.groupListView2);
        condList = new ArrayList<>();
        cmdList = new ArrayList<>();
        trigger = new iotSceCreateActivity.Trigger("", "", "");

        /****************************************************************************************/
        QMUICommonListItemView devChoose = mGroupListView.createItemView("选择触发时间:");
        devChoose.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        devChoose.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog dialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    devChoose.setDetailText(hourOfDay+":"+minute +" 执行");
                    Time = hourOfDay+":"+minute+":00";
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            dialog.show();
        });

        QMUICommonListItemView devClassChoose = mGroupListView.createItemView("选择触发周期:");
        devClassChoose.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        devClassChoose.setOnClickListener(v -> {
            showMultiChoiceDialog(devClassChoose);
        });

        QMUIGroupListView.newSection(getContext())
                .setTitle("触发时间")
                .addItemView(devChoose, null)
                .addItemView(devClassChoose, null)
                .addTo(mGroupListView);

        /********************************************************************/
        addButtonShow();

    }

    private void showMultiChoiceDialog(QMUICommonListItemView dateChoose) {
        final String[] items = new String[]{"周一", "周二", "周三", "周四", "周五", "周六","周天"};
        final QMUIDialog.MultiCheckableDialogBuilder builder = new QMUIDialog.MultiCheckableDialogBuilder(getContext())
                .setCheckedItems(new int[]{0,1,2,3,4,5,6})
                .setSkinManager(QMUISkinManager.defaultInstance(getContext()))
                .addItems(items, (dialog, which) -> {

                });
        builder.addAction("取消", (dialog, index) -> dialog.dismiss());
        builder.addAction("提交", (dialog, index) -> {
            int date[] = new int[7];
            for(int i = 0 ; i<7;i++)
                date[i] = '0';
            for (int i = 0; i < builder.getCheckedItemIndexes().length; i++) {
                date[builder.getCheckedItemIndexes()[i]] = '1';
            }
            weekDay = new String(date,0,7);
            dialog.dismiss();
            dateChoose.setDetailText(weekDay);
        });
        builder.create().show();
    }

    private void addButtonShow()
    {
        QMUICommonListItemView CondAdd;
        if(!isCondComplete)
            CondAdd = mGroupListView.createItemView("点击添加中间条件或者最终执行指令");
        else
            CondAdd = mGroupListView.createItemView("点击添加最终执行指令");
        CondAdd.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.iot_add));
        CondAdd.setOnClickListener(v -> {
            if (!isCondComplete) {
                final String[] strs = new String[]{"添加[中间条件]", "添加[直接指令]"};
                new QMUIDialog.MenuDialogBuilder(getContext())
                        .addItems(strs, (dialog, index) -> {
                            mGroupListView.removeView(CondAdd);
                            if (index == 0) {
                                AddCond();
                            } else {
                                isCondComplete = true;
                                AddCmd();
                            }
                            dialog.dismiss();
                            addButtonShow();
                        }).create().show();
            } else {
                final String[] strs = new String[]{"添加[直接指令]"};
                new QMUIDialog.MenuDialogBuilder(getContext())
                        .addItems(strs, (dialog, index) -> {
                            mGroupListView.removeView(CondAdd);
                            AddCmd();
                            dialog.dismiss();
                            addButtonShow();
                        }).create().show();
            }
        });
        QMUIGroupListView.newSection(getContext())
                .addItemView(CondAdd, null)
                .addTo(mGroupListView);
    }

    private void AddCond() {
        /****************************************************************************************/
        int Index = condIndex;
        condIndex++;
        iotSceCreateActivity.Cond cond = new iotSceCreateActivity.Cond(Index, "", "", "");
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
        iotSceCreateActivity.Cmd cmd = new iotSceCreateActivity.Cmd("", "", "");
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


    private void initTopBar() {
        mTopBar = findViewById(R.id.topbar_iot_timer);
        mTopBar.addLeftImageButton(R.drawable.qmui_icon_topbar_back, R.id.exit).setOnClickListener(v -> {
            popBackStack();
        });
        mTopBar.addRightImageButton(R.drawable.complete_sce_white_iot, R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cmd = "0-1-=0-";
                String CondCmd = "";
                for(iotSceCreateActivity.Cond c:condList)
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
                    CondCmd = CondCmd + condDevKeyId+"+"+c.DevClass+"+"+c.CondData+"+";
                }

                String Arm_Dev_ID = "1111111";
                boolean isFindArmDev = false;
                for(iotLaunch.DevStore d:iotLaunch.DevStoreList)
                {
                    if(d.DevClass == 99)
                    {
                        Arm_Dev_ID = d.OpenId;
                        isFindArmDev = true;
                        break;
                    }
                }

                boolean isEspRunOnly = false;

                if(CondCmd != "")
                {
                    CondCmd = CondCmd + "-";
                }
                else
                {
                    isEspRunOnly = true;
                }

                if(!isFindArmDev)
                {
                    Toast.makeText(getContext(),"未添加任何ARM设备，将不会添加任何中间条件只运行第一条执行指令",Toast.LENGTH_SHORT).show();
                    isEspRunOnly = true;
                }

                if(isEspRunOnly)
                    isFindArmDev = false;

                ArrayList<String> CmdList = new ArrayList<>();
                for(iotSceCreateActivity.Cmd c:cmdList)
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
                    String Cmd;
                    if(isEspRunOnly)
                    {
                        Cmd = cmdDevKeyId + "-" + c.DevClass + "-" + c.CmdData + "-";
                    }
                    else
                    {
                        Cmd = cmd + CondCmd + cmdDevKeyId + "-" + c.DevClass + "-" + c.CmdData + "-";
                    }
                    CmdList.add(Cmd);
                }
                if(CmdList.size()==0)
                {
                    Toast.makeText(getContext(),"未填写任何执行指令，请添加至少一条执行指令",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isEspRunOnly)
                {
                    String s = CmdList.get(0);
                    Toast.makeText(getContext(),"未填写任何中间条件条件或者未找到ARM设备，单板策略将只会采取第一条执行指令",Toast.LENGTH_SHORT).show();
                    Bundle tmp = new Bundle();
                    tmp.putString("TalkToid", cmdList.get(0).DevId);
                    tmp.putString("data", s+"~"+Time+"_"+weekDay+"_"+"~");
                    tmp.putString("Checkcode", "DSC");
                    iotLaunch.sendMessage(tmp);
                    //Toast.makeText(getContext(),s,Toast.LENGTH_SHORT).show();
                }

                if(isFindArmDev)
                {
                    Toast.makeText(getContext(),"找到ARM设备,将其作为执行指令主体委派者",Toast.LENGTH_SHORT).show();
                    for(String s:CmdList) {
                        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                        Bundle tmp = new Bundle();
                        tmp.putString("TalkToid", Arm_Dev_ID);
                        tmp.putString("data", s+"~"+Time+"_"+weekDay+"_"+"~");
                        tmp.putString("Checkcode", "SCE");
                        iotLaunch.sendMessage(tmp);
                    }
                }
                popBackStack();
            }
        });
        mTopBar.setTitle("添加定时指令");
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
                                 boolean allowDragDismiss, ArrayList<String> itemList, QMUICommonListItemView devChoose, iotSceCreateActivity.Cond cond) {
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
                                  boolean allowDragDismiss, ArrayList<String> itemList, QMUICommonListItemView devClassChoose, iotSceCreateActivity.Cond cond) {
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
                                boolean allowDragDismiss, ArrayList<String> itemList, QMUICommonListItemView devChoose, iotSceCreateActivity.Cmd cmd) {
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
                                 boolean allowDragDismiss, ArrayList<String> itemList, QMUICommonListItemView devClassChoose, iotSceCreateActivity.Cmd cmd) {
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
















/*button.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
@Override
public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String desc = String.format("您选择%d时%d分",hourOfDay,minute);
        //tv_distime.setText(desc);
        Toast.makeText(getContext(),desc,Toast.LENGTH_SHORT).show();
        }
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true);
        dialog.show();
        }
        });*/