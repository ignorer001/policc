package com.example.crackme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pintent.he.HomoInteger;

import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {
    //定义一个过滤器；
    private IntentFilter intentFilter;

    //定义一个广播监听器；
    private NetChangReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("YLReceiver",  "startActivity done ---> " + System.currentTimeMillis());

        //实例化过滤器；
        intentFilter = new IntentFilter();
        //添加过滤的Action值；
        intentFilter.addAction("im_a_test_broadcast");

        //实例化广播监听器；
        receiver = new NetChangReceiver();

        //将广播监听器和过滤器注册在一起；
        registerReceiver(receiver, intentFilter);




        Intent intent = getIntent();

        final Intent sendIntent = new Intent();
        sendIntent.replaceExtras(intent);
        //Intent sendIntent = new Intent();
        sendIntent.setAction("im_a_test_sender_action");
        sendIntent.addCategory("android.intent.category.DEFAULT");

        // send intent back
        Button btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                startActivity(sendIntent);
            }
        });


        boolean result = check("test_val_here", "test_another_val");
        TextView mytextView = (TextView) findViewById(R.id.textView);
        if (result)
            mytextView.setText("YES");
        else {
            mytextView.setText("NO");
        }

        TextView tv_test = (TextView) findViewById(R.id.tv_test);
        BigInteger test_val_here = new BigInteger("999");
        String value = "";
        if (intent != null) {
            value = intent.getStringExtra("yl");
            Object o = intent.getSerializableExtra("test_val_here");
            if (o instanceof HomoInteger) {
                HomoInteger a = (HomoInteger) o;
                Toast.makeText(MainActivity.this, "now the homo test_val_here is : " + a.toString(), Toast.LENGTH_SHORT).show();
            } else if (o instanceof BigInteger) {
                test_val_here = (BigInteger) o;
                Toast.makeText(MainActivity.this, "now the test_val_here is : " + test_val_here.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        Toast.makeText(MainActivity.this, "now the data is : " + value, Toast.LENGTH_SHORT).show();

        tv_test.setText(value);


//        intent.setAction("im_a_test_sender_action");
//        intent.addCategory("android.intent.category.DEFAULT");
//        startActivity(intent);

    }

    private boolean check(String a, String b) {
        if (a.equals(b))
            return true;
        return false;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        //销毁Activity时取消注册广播监听器；
        unregisterReceiver(receiver);
    }

    //创建一个继承BroadcastReceiver的广播监听器；
    class NetChangReceiver extends BroadcastReceiver {

        //重写onReceive方法，该方法的实体为，接收到广播后的执行代码；
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("YLReceiver",  "sendBroadcast done ---> " + System.currentTimeMillis());
            //Log.e("NetChangReceiver", "on TestReceiver");
            if (intent != null) {
                {
                    BigInteger test_val_here = new BigInteger("999");
                    String value = "";
                    value = intent.getStringExtra("yl");
                    Object o = intent.getSerializableExtra("test_val_here");
                    if (o instanceof HomoInteger) {
                        HomoInteger a = (HomoInteger) o;
                        Toast.makeText(context, "now the homo test_val_here is : " + a.toString(), Toast.LENGTH_SHORT).show();
                    } else if (o instanceof BigInteger) {
                        test_val_here = (BigInteger) o;
                        Toast.makeText(context, "now the test_val_here is : " + test_val_here.toString(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(context, "now the data in sender is : " + value, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
