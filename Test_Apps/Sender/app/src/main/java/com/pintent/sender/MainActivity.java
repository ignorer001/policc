package com.pintent.sender;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pintent.he.EncryptionUtil;
import com.pintent.he.HomoInteger;

import java.math.BigInteger;


public class MainActivity extends AppCompatActivity {

    public String testString32 = "01234567890123456789012345678912";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("im_a_test_action");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.putExtra("yl", testString32);
                BigInteger test_val_here = new BigInteger("100");
                intent.putExtra("test_val_here", test_val_here);

                Log.d("YLSender",  "startActivity ---> " + System.currentTimeMillis());
                startActivity(intent);

            }
        });


        Button btnBroadcast = (Button) findViewById(R.id.btn_broadcast);
        btnBroadcast.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("im_a_test_broadcast");
                intent.putExtra("yl", testString32);
                BigInteger test_val_here = new BigInteger("99");
                intent.putExtra("test_val_here", test_val_here);

                Log.d("YLSender",  "sendBroadcast ---> " + System.currentTimeMillis());
                sendBroadcast(intent);
            }
        });

        Intent intent = getIntent();
        BigInteger test_val_here = new BigInteger("999");
        String value = "";
        if (intent != null) {
            value = intent.getStringExtra("yl");
            Object o = intent.getSerializableExtra("test_val_here");
            if (o instanceof HomoInteger) {
                HomoInteger a = (HomoInteger) o;
                Toast.makeText(MainActivity.this, "now the homo test_val_here in sender is : " + a.toString(), Toast.LENGTH_SHORT).show();
            } else if (o instanceof BigInteger) {
                test_val_here = (BigInteger) o;
                Toast.makeText(MainActivity.this, "now the test_val_here in sender is : " + test_val_here.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        Toast.makeText(MainActivity.this, "now the data in sender is : " + value, Toast.LENGTH_SHORT).show();
    }
}
