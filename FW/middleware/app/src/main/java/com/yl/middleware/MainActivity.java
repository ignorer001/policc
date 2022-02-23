package com.yl.middleware;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ICustomService;
import android.os.Parcel;
import android.os.ServiceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.pintent.he.EncryptionUtil;
import com.pintent.he.HomoFactory;
import com.pintent.he.RSAModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.NoSuchPaddingException;


public class MainActivity extends Activity {
    private static final String path = "/data/data/com.yl.middleware/files/";
    //use service demo
    public ICustomService mService;

    // need to set the file be readable
    private void setWorldReadable() {
        File dataDir = new File(getApplicationInfo().dataDir);
        File prefsDir = new File(dataDir, "shared_prefs");
        //File prefsFile = new File(prefsDir, BuildConfig.APPLICATION_ID + "_preferences.xml");
       // File prefsFile = new File(prefsDir, getPackageName() + "_preferences.xml");
        File prefsFile = new File(prefsDir, "config.xml");
        if (prefsFile.exists()) {
            for (File file : new File[]{dataDir, prefsDir, prefsFile}) {
                file.setReadable(true, false);
                file.setExecutable(true, false);
            }
        }
    }

    private void setWorldReadable(String fileName) {
        File dataDir = new File(getApplicationInfo().dataDir);
        File prefsDir = new File(dataDir, "files");
        //File prefsFile = new File(prefsDir, BuildConfig.APPLICATION_ID + "_preferences.xml");
        // File prefsFile = new File(prefsDir, getPackageName() + "_preferences.xml");
        File prefsFile = new File(prefsDir, fileName);
        if (prefsFile.exists()) {
            for (File file : new File[]{dataDir, prefsDir, prefsFile}) {
                file.setReadable(true, false);
//                file.setWritable(true, false);
                file.setExecutable(true, false);
            }
        }
    }

    public String someMethod(Context context) {
        try {
            if (mService == null) {
                mService = ICustomService.Stub.asInterface(ServiceManager.getService("user.custom.service"));
            }
            return mService.sayHello();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return ">>> someMethod failed!!! ";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context con = this;

        Button btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("XXX", "I click it " + System.currentTimeMillis());
                // user sharedpreference
                //创建共享配置文件的时，设置为Activity.MODE_WORLD_READABLE模式
                //SharedPreferences sp = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
                //SharedPreferences sp = getSharedPreferences("config", Activity.MODE_WORLD_READABLE);
                SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                // 保存数据到共享配置文件中
                Set<String> authList = new HashSet<String>();
                authList.add("com.pintent.sender");
                editor.putStringSet("AUTH_LIST", authList);
                editor.commit();
                editor.clear();
                authList.clear();
                setWorldReadable();

//                /**
//                 * below code is testing
//                 * for the Xposed supported system service
//                 */
//                String str = someMethod(con);
//                if (str != null) {
//                    Log.d("ccccccccccccc", str);
//                }
//
//                // D2D service start:
//                try {
//                    ICustomService mService = ICustomService.Stub.asInterface(ServiceManager.getService("user.custom.service"));
//                    mService.startBTConnection();
//                    mService.sendByBT(host);
//                    mService.stopBTConnection();
//                } catch (Exception e) {
//                    System.out.println(e.getMessage());
//                    e.printStackTrace();
//                }
//
//                data.recycle();
//                hostData.recycle();

            }
        });


        /**
         * for the key management actually, we can create the key when the module has been launch,
         * and it store the public key in a public storage that can be accessed by the system serivce,
         * but for the private key, it only can be accessed by the module.
         */



        // HEBigInteger key
        File targetFile = new File(path + "HEkey");
        if (!targetFile.exists()) {
            HomoFactory.genKey();
            IOHelper ioHelper = new IOHelper(this);
            ioHelper.saveObjToFile(HomoFactory.param, "HEkey");

            // obj to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = null;
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(HomoFactory.param);
                out.flush();
                byte[] HEKeyByte = bos.toByteArray();

                SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                String HEKeyByteBase64 = Base64.encodeToString(HEKeyByte, 0);
                editor.putString("HEKey", HEKeyByteBase64);
                editor.commit();
                editor.clear();
                setWorldReadable();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    bos.close();
                } catch (IOException ex) {
                    // ignore close exception
                }
            }




        }
        setWorldReadable("HEkey");
        // RSA key
        File targetFileRSA = new File(path + "RSAkey");
        if (!targetFileRSA.exists()) {
            try {
                RSAModel rsaModel = new RSAModel(256);
                IOHelper ioHelperRSA = new IOHelper(this);
                ioHelperRSA.saveObjToFile(RSAModel.keyPair, "RSAkey");


                // obj to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = null;
                try {
                    out = new ObjectOutputStream(bos);
                    out.writeObject(RSAModel.keyPair);
                    out.flush();
                    byte[] RSAKeyByte = bos.toByteArray();

                    SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    String RSAkeyByteBase64 = Base64.encodeToString(RSAKeyByte, 0);
                    editor.putString("RSAkey", RSAkeyByteBase64);
                    editor.commit();
                    editor.clear();
                    setWorldReadable();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bos.close();
                    } catch (IOException ex) {
                        // ignore close exception
                    }
                }




            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }

        }
        setWorldReadable("RSAkey");




//        // D2D service start:
//        try {
//            if (mService == null) {
//                mService= ICustomService.Stub.asInterface(ServiceManager.getService("user.custom.service"));
//                //mService =(ICustomService)context.getSystemService("wx_custom.service");
//            }
//            mService.startBTConnection();
//            //mService.sendByBT();
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }

//        BT service start:
//        Intent intent = new Intent(this, DtDService.class);
//        this.startService(intent);

//        EncryptionParameters test = ioHelper.getObjFromFile("HEkey");


//        // 创建共享配置文件的时，设置为Activity.MODE_WORLD_READABLE模式
//        SharedPreferences sp = this.getSharedPreferences(getPackageName() + "_preferences", Activity.MODE_WORLD_READABLE);
//        SharedPreferences.Editor editor = sp.edit();
//        // 保存数据到共享配置文件中
//        HomoFactory.genKey();
//        HomoFactory.genStrKey();
//        BigInteger pubKey = HomoFactory.PublicKey;
//        BigInteger secretKey = HomoFactory.SecretKey;
//        String strKey = HomoFactory.StrKey;
//        String strPubKey = new String(pubKey.toByteArray());
//        String strSecretKey = new String(secretKey.toByteArray());
//        editor.putString("strKey", strKey);
//        editor.putString("strPubKey", strPubKey);
//        editor.putString("strSecretKey", strSecretKey);
//        editor.commit();
//        editor.clear();

    }

}
