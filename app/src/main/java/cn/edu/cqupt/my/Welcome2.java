package cn.edu.cqupt.my;

import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class Welcome2 extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        ActionBar actionbar=getSupportActionBar();
        if (actionbar!=null) {
            actionbar.hide();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Boolean AutoLogin = LoginConfig.getBoolean(getApplicationContext(), "AutoLogin", true);
                // if(AutoLogin){
               finish();
                // }else{
                //   Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //  startActivity(intent);
                // }
            }
        }, 3000);
    }
    @Override
    public void onBackPressed()
    {

    }

}
