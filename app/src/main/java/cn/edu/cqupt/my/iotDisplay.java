package cn.edu.cqupt.my;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

public class iotDisplay extends AppCompatActivity {
    QMUITipDialog tipDialog ;
    QMUIButton bt ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iot_display);
        bt = findViewById(R.id.QmuiBt);
        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord("操作成功")
                .create();
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tipDialog.dismiss();
                    }
                },2000);
            }
        });


    }

}