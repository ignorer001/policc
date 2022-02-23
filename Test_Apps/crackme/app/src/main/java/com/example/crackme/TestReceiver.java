package com.example.crackme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.pintent.he.HomoInteger;

import java.math.BigInteger;

public class TestReceiver extends BroadcastReceiver {
    private static final String TAG = "TestReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "on TestReceiver");
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
