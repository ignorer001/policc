package com.yl.middleware;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CustomService;
import android.os.ICustomService;
import android.os.Parcel;
import android.os.ServiceManager;
import android.util.Base64;
import android.util.Log;

import com.pintent.he.EncryptionParameters;
import com.pintent.he.EncryptionUtil;
import com.pintent.he.HomoFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class XposedMainInit implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public static final String isEncrypted = "isEncrypted";
    private static final String TAG = "XposedMainInit";
    private static final boolean DEBUG = false;
    private static final boolean MEASURE_A = false;
    private static final boolean MEASURE_B = false;
    // Parasite intent
    private static final String PARASITE = "PARASITE";
    private final static String modulePackageName = XposedMainInit.class.getPackage().getName();
    private static final String path = "/data/data/com.yl.middleware/files/";
    private static String intentKey = "polyintent";
    private static String mStrKey = "defaultkey";
    private static EncryptionParameters mEnParam = null;
    private static KeyPair mKeyPair = null;
    private static Context mGbContext = null;
    // for check whether the package name is ok for passing across devices
    // this list is obtained from user's preference
    private Set<String> authList = null;
    private Set<String> sysBroadcast = null;
    private IXposedHookZygoteInit.StartupParam startupparam;
    private XSharedPreferences prefs;

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (DEBUG) {
            XposedBridge.log("XposedMainInit handleLoadPackage has been executed!!!");
            //使用XposedBridge的log来输出载入的app，在tag过滤器中添加Xposed即可读取log
            XposedBridge.log("Loaded app: " + loadPackageParam.packageName);
        }

        if (loadPackageParam.packageName.equals("com.yl.middleware"))
            return;

//        // filter the system broadcast
//        sysBroadcast = new HashSet<String>();
//        sysBroadcast.add(Intent.ACTION_BATTERY_CHANGED);
//        sysBroadcast.add(Intent.ACTION_TIME_CHANGED);
//        sysBroadcast.add(Intent.ACTION_PACKAGE_CHANGED);


//        File targetFile = new File(path + "HEkey");
//        if (targetFile.exists()) {
//            XposedBridge.log("targetFile.exists()");
//            Context context = (Context) AndroidAppHelper.currentApplication();
//            if (context != null) {
//                mGbContext = context.createPackageContext(modulePackageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
//                if (mGbContext != null) {
//                    IOHelper ioHelper = new IOHelper(mGbContext);
//                    EncryptionParameters param = ioHelper.getObjFromFile("HEkey");
//                    HomoFactory.param = param;
//                    XposedBridge.log("mEnParam = param");
//                    mEnParam = param;
//                }
//            }
//
//        }


/*        //这里是一个hook应用的实例，主要目的是hook com.example.crackme中的check函数，使其返回值为true
        if (loadPackageParam.packageName.equals("com.example.crackme")) {
            //loadPackageParam为载入的package的参数，其中packageName存了包名，如果包名不一致，不做处理
            //这里就是关键了，hook方法（函数）的时候用到这个方法
            XposedHelpers.findAndHookMethod(
                    "com.example.crackme.MainActivity",//要hook的类
                    loadPackageParam.classLoader,//获取classLoader
                    "check",//要hook的方法（函数）
                    String.class,//第一个参数
                    String.class,//第二个参数
                    new XC_MethodHook() {
                        //这里是hook回调函数
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            //afterHookedMethod，顾名思义，在被hook的方法之后调用这个函数
                            //param.args作为数组，存了调用函数时的参数，通过getResult得到返回值
                            XposedBridge.log("after hook:" + param.args[0]);
                            XposedBridge.log("after hook 2:" + param.args[1]);
                            XposedBridge.log("result:" + param.getResult());
                            param.setResult(true);//通过setResult更改返回值
                            XposedBridge.log("result(settled):" + param.getResult());
                        }
                    });
        }*/

        /**
         * Hook sendBroadcast
         */
        findAndHookMethod(ContextWrapper.class, "sendBroadcast",
                Intent.class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        Intent intent = (Intent) param.args[0];
                        if (DEBUG) XposedBridge.log(TAG + "sendBroadcast ++ ");

                        if (MEASURE_B) {
                            Intent temp_intent_here = (Intent) param.args[0];
                            if (temp_intent_here == null ||
                                    !temp_intent_here.getAction().equals("im_a_test_broadcast"))
                                return;

                            if (temp_intent_here != null) {
                                String xxx = temp_intent_here.toString();
                                if (!xxx.contains("de.robv.android.xposed.installer")) {
                                    Log.d("Xposed", "原始Intent数据: " + temp_intent_here.toString());
                                    Log.d("Xposed", "hook sendBroadcast start ---> " + System.currentTimeMillis());
                                }
                            }
                        }
                        // TODO: currently, we only hook the sendBroadcast function here

                        String currentPkgName = loadPackageParam.packageName;
                        if (DEBUG) Log.d("Xposed", "loadPackageParam.packageName: " + currentPkgName);
                        boolean isSysApp = PIntentUtil.checkSystemApps(currentPkgName);
                        // 排除系统应用
                        if (isSysApp || ((loadPackageParam.appInfo.flags
                                & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 1)) {
                            return;
                        }

                        Intent originalIntent = (Intent) param.args[0];
                        if (DEBUG) Log.d("Xposed", "原始Intent数据: " + originalIntent.toString());

                        Intent hostIntent = new Intent(originalIntent);
                        Intent parasiteIntent;

                        /**
                         * if hostIntent already contains parasateIntent --- check getExtra(PARASITE) != null
                         * and deserilize the byte[] to parasateIntent, adding new sendlist, serialize it again
                         * finally put into the host intent again
                         * TODO: we have not tested below logic yet
                         */
                        if (DEBUG) Log.d(TAG, "getByteArrayExtra(\"PARASITE\")");
                        byte[] byteTry = hostIntent.getByteArrayExtra("PARASITE");
                        if (byteTry != null) {
                            if (DEBUG) Log.e(TAG, "has PARASITE intent");
                            // recover PARASITE intent to Normal originial intent
                            byte[] decyptedParasite = EncryptionUtil.decrypt(byteTry, intentKey);
                            Parcel p = Parcel.obtain();
                            p.unmarshall(decyptedParasite, 0, decyptedParasite.length);
                            p.setDataPosition(0);

                            Intent originalParasiteIntent;

                            try {
                                if (DEBUG) Log.d(TAG, "back to normal Intent");
                                p.setDataPosition(0);
                                originalParasiteIntent = Intent.CREATOR.createFromParcel(p);
                            } catch (Exception ex) {
                                // if cannot reconstruct to be a normal Intent
                                // then try RemoteIntent
                                if (DEBUG) Log.e(TAG, "cannot be a normal intent" + ex.toString());
                                if (DEBUG) Log.d(TAG, "back to Remote Intent");
                                p.setDataPosition(0);
                                RemoteIntent originalParasiteIntentBT = RemoteIntent.CREATOR.createFromParcel(p);
                                Intent back2Normal = originalParasiteIntentBT.backtoIntent();
                                back2Normal.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                originalParasiteIntent = back2Normal;
                            }

                            // if parasiteIntent is null, it will throw null pointer exception after a while
                            parasiteIntent = originalParasiteIntent;
                        } else {
                            parasiteIntent = new Intent(originalIntent);
                        }

                        /**
                         * The process is following:
                         * 1. get Context and package name
                         * 2. getPermissions by the package name
                         * 3. insert the sender list into the intent
                         * 4. serialize it
                         * 5. encrypt the serialized byte
                         * 6. insert the encrypted byte to the host intent
                         * 7. replace the original intent ot the host intent
                         * TODO: at the entry point (StartActivity) encrypt every string and biginteger
                         * TODO: at the exit point (performLaunchActivity) decrypt above variables
                         */

                        /**
                         * get Context
                         * https://stackoverflow.com/questions/28059264/how-to-get-context-through-hooking-in-android
                         * https://github.com/rovo89/XposedBridge/blob/master/src/android/app/AndroidAppHelper.java#L131
                         */
//                        Context context = (Context) AndroidAppHelper.currentApplication();

                        // create parasite intent
                        // store necessary information
                        String packageName = loadPackageParam.packageName;
                        if (DEBUG) Log.d("Xposed", "loadPackageParam.packageName: " + packageName);

                        ArrayList<String> senderList = parasiteIntent.getStringArrayListExtra("senderList");
                        if (senderList == null) {
                            senderList = new ArrayList<String>();
                        }

                        // 排除系统应用
                        // if ((loadPackageParam.appInfo.flags &
                        // (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 1) {...}

                        //check system app, exclude all system apps' package name
//                            boolean isSysApp = PIntentUtil.checkSystemApps(packageName);

//                        if (!isSysApp) {
                        senderList.add(packageName);
                        parasiteIntent.removeExtra("senderList");
                        parasiteIntent.putStringArrayListExtra("senderList", senderList);
//                        }

                        // clear host intent
                        hostIntent = PIntentUtil.removeAllExtras(hostIntent);

                        // for send via BT
                        RemoteIntent hostIntentBT = new RemoteIntent(hostIntent);
                        RemoteIntent parasiteIntentBT = new RemoteIntent(parasiteIntent);

                        // parcel it
                        // https://bitbucket.org/yinliu/poly-intent/commits/7af53e3793d50e47f46709f6b0f6b933d09c911b
                        Parcel data = Parcel.obtain();
                        parasiteIntent.writeToParcel(data, 0);
                        byte[] parasite = data.marshall();


                        // encrypt the parasite data
                        // parasite it to the host
                        byte[] encyptedParasite = EncryptionUtil.encrypt(parasite, intentKey);
                        hostIntent.putExtra(PARASITE, encyptedParasite);

                        // replace the parameter
                        param.args[0] = hostIntent;

                        // TODO user set which app can send across the device, they also can restrict all intent for sending out
                        XSharedPreferences intance = PIntentUtil.getIntance();
                        authList = intance.getStringSet("AUTH_LIST", null);

                        if (!authList.isEmpty() && authList.contains(packageName)) {
                            // send via BT
                            Parcel dataBT = Parcel.obtain();
                            parasiteIntentBT.writeToParcel(dataBT, 0);
                            byte[] parasiteBT = dataBT.marshall();

                            // parasite it to the host
                            byte[] encyptedParasiteBT = EncryptionUtil.encrypt(parasiteBT, intentKey);
                            hostIntentBT.putParasiteExtra(encyptedParasiteBT);

                            // finally parcel it to byte array
                            Parcel hostDataBT = Parcel.obtain();
                            hostIntentBT.writeToParcel(hostDataBT, 0);
                            byte[] hostBT = hostDataBT.marshall();

                            // D2D service start:
                            try {
                                ICustomService mService = ICustomService.Stub.asInterface(ServiceManager.getService("user.custom.service"));
                                mService.startBTConnection();
                                //mService.sendByBT(hostBT);
                                //TODO add -- send broadcast by BT instead of sendByBT
                                mService.sendBroadcastByBT(hostBT);
                                mService.stopBTConnection();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                e.printStackTrace();
                            }

                            dataBT.recycle();
                            hostDataBT.recycle();
                        }

                        senderList.clear();
                        data.recycle();


                        if (MEASURE_B) {
                            Log.d("Xposed", "hook sendBroadcast stop ---> " + System.currentTimeMillis());
                        }

                    }
                });


        findAndHookMethod(ContextWrapper.class, "sendBroadcast",
                Intent.class, String.class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Intent intent = (Intent) param.args[0];
                        if (DEBUG) XposedBridge.log(TAG + "sendBroadcast with string " + intent);
//                        if(intent !=null && !intent.getAction().contains("mobi.acpm.inspeckage")) {
//
//                        }
                    }
                });
        /**
         * Hook receiveBroadcast
         * deliverToRegisteredReceiverLocked
         * BroadcastQueue.java
         */
//        Class build = XposedHelpers.findClass(com.android.server.am.BroadcastQueue, loadPackageParam.classLoader);
        try {
            Class<?> broadcastQueue = XposedHelpers.findClass("com.android.server.am.BroadcastQueue", loadPackageParam.classLoader);

            // FIXME for Android 7.1.1 --- added int.class field
//            XposedHelpers.findAndHookMethod(broadcastQueue,
//                    "deliverToRegisteredReceiverLocked",
//                    "com.android.server.am.BroadcastRecord", "com.android.server.am.BroadcastFilter", boolean.class, int.class

            // FIXME for Android 5.1, 5.1.1
            XposedHelpers.findAndHookMethod(broadcastQueue,
                    "deliverToRegisteredReceiverLocked",
                    "com.android.server.am.BroadcastRecord", "com.android.server.am.BroadcastFilter", boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (DEBUG) {
                                XposedBridge.log("beforeHookedMethod deliverToRegisteredReceiverLocked ++ ");
                            }


                            if (MEASURE_B) {
                                Object test_obj_here = param.args[0];
                                Intent temp_intent_here = (Intent) XposedHelpers.getObjectField(test_obj_here, "intent");
                                Log.d("Xposed", "原始Intent数据: " + temp_intent_here.toString());
                                Log.d("Xposed", "hook deliverToRegisteredReceiverLocked start ---> " + System.currentTimeMillis());
                            }


                            Context context = (Context) AndroidAppHelper.currentApplication();
                            Object r = param.args[0];
                            Intent hostIntent = (Intent) XposedHelpers.getObjectField(r, "intent");
                            String callerPkg = (String) XposedHelpers.getObjectField(r, "callerPackage");

                            if (hostIntent == null) return;
                            if (DEBUG) Log.d("Xposed", "目的地hostIntent数据: " + hostIntent.toString());

                            boolean mysign = hostIntent.getBooleanExtra("IM", false);
                            if (mysign == false) {
                                try {
                                    // if caller is system app, return
                                    ApplicationInfo app = context.getPackageManager().getApplicationInfo(callerPkg, 0);

                                    boolean isSysApp = PIntentUtil.checkSystemApps(callerPkg);
                                    // 排除系统应用
                                    if (isSysApp || ((app.flags
                                            & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 1)) {
                                        return;
                                    }

                                } catch (PackageManager.NameNotFoundException e) {
                                    return;
                                }

                            }


                            if (DEBUG) {
                                XposedBridge.log("hostIntent = " + hostIntent);
                                XposedBridge.log("callerPackage = " + callerPkg);
                            }

//                            String action = hostIntent.getAction();
//                            if (action != null && (action.startsWith("android.intent.action") ||
//                                    action.startsWith("android.net.conn") ||
//                                    action.startsWith("android.bluetooth.adapter.action") ||
//                                    action.startsWith("com.google.android.intent.action") ||
//                                    action.startsWith("android.telephony.action") ||
//                                    action.startsWith("android.bluetooth.device.action") ||
//                                    action.startsWith("android.net.wifi") ||
//                                    action.startsWith("com.google.gservices.intent.action") ||
//                                    action.startsWith("android.os.action") ||
//                                    action.startsWith("rcs.intent.action") ||
//                                    action.startsWith("android.hardware"))) {
//                                return;
//                            }


//                            XposedBridge.log("hostIntent = " + hostIntent);
//                            XposedBridge.log("callerPackage = " + callerPkg);

                            Object filter = param.args[1];
                            // seems like the package Name is enough
                            String receiverPkgName = (String) XposedHelpers.getObjectField(filter, "packageName");
                            //Object receiverList = (Object) XposedHelpers.getObjectField(filter, "receiverList");

                            if (receiverPkgName == null) return;

                            if (DEBUG) {
                                XposedBridge.log("receiverPkgName = " + receiverPkgName);
                                //XposedBridge.log("receiverList = " + receiverList);
                            }
//                            String activityName = param.thisObject.getClass().getName();
                            if (DEBUG) Log.d("Xposed", "param.thisObject: " + param.thisObject);


                            // get original intent back
                            if (DEBUG) Log.d("Xposed", "getByteArrayExtra(\"PARASITE\")");
                            byte[] mbyte = hostIntent.getByteArrayExtra("PARASITE");
                            if (mbyte == null) {
                                if (DEBUG) Log.e("Xposed", "PARASITE intent is null, return");
                                return;
                            }
                            byte[] decyptedParasite = EncryptionUtil.decrypt(mbyte, intentKey);
                            Parcel p = Parcel.obtain();
                            p.unmarshall(decyptedParasite, 0, decyptedParasite.length);
                            p.setDataPosition(0);

                            Intent originalIntent = new Intent();
                            if (DEBUG) Log.d("Xposed", "PolyIntent.CREATOR.createFromParcel(p)");
                            try {
                                p.setDataPosition(0);
                                originalIntent = Intent.CREATOR.createFromParcel(p);
                            } catch (Exception ex) {
                                // if cannot reconstruct to be a normal Intent
                                // then try RemoteIntent
                                if (DEBUG) {
                                    Log.e("Xposed", "cannot be a normal intent" + ex.toString());
                                    //TODO: for RemoteIntent
                                    Log.d("Xposed", "TAG PolyIntent.CREATOR.createFromParcel(p)");
                                }
                                p.setDataPosition(0);
                                RemoteIntent originalhostIntent = RemoteIntent.CREATOR.createFromParcel(p);
                                if (DEBUG) {
                                    Log.d("Xposed", "originalhostIntent.mAction" + originalhostIntent.mAction);
                                    Log.d("Xposed", "originalhostIntent.mCategories" + originalhostIntent.mCategories);
                                }
                                Intent back2Normal = originalhostIntent.backtoIntent();
                                back2Normal.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                originalIntent = back2Normal;
                            }

                            // NOTE: important to set the classloader
                            originalIntent.setExtrasClassLoader(loadPackageParam.classLoader);

                            File targetFile = new File(path + "HEkey");
//                            if (targetFile.exists() && context != null) {
                            if (targetFile.exists()) {
                                // NOTE: should not use flags "Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY" like
                                // mGbContext = context.createPackageContext(modulePackageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                                // otherwise, Xposed will trigger performLaunchActivity function again and load a new com.yl.middleware package
//                                mGbContext = context.createPackageContext(modulePackageName, 0);
//                                if (mGbContext != null) {
//                                    IOHelper ioHelper = new IOHelper(mGbContext);

                                    // get HEKey and RSAKey back
                                    XSharedPreferences intance = PIntentUtil.getIntance();
                                    String HEKeyBase64 = intance.getString("HEKey", null);
                                    String RSAkeyBase64 = intance.getString("RSAkey", null);
                                    byte[] HEKeybytes = Base64.decode(HEKeyBase64, 0);
                                    byte[] RSAkeybytes = Base64.decode(RSAkeyBase64, 0);
                                    ByteArrayInputStream bisHEKey = new ByteArrayInputStream(HEKeybytes);
                                    ByteArrayInputStream bisRSAkey = new ByteArrayInputStream(RSAkeybytes);
                                    ObjectInput inHE = null;
                                    ObjectInput inRSA = null;
                                    try {
                                        inHE = new ObjectInputStream(bisHEKey);
                                        Object oHE = inHE.readObject();
                                        inRSA = new ObjectInputStream(bisRSAkey);
                                        Object oRSA = inRSA.readObject();

                                        HomoFactory.param = (EncryptionParameters) oHE;
                                        mEnParam = (EncryptionParameters) oHE;
                                        mKeyPair = (KeyPair) oRSA;

                                    } finally {
                                        try {
                                            if (inHE != null)
                                                inHE.close();
                                            if (inRSA != null)
                                                inRSA.close();
                                            if (bisHEKey != null)
                                                bisHEKey.close();
                                            if (bisRSAkey != null)
                                                bisRSAkey.close();
                                        } catch (IOException ex) {
                                            // ignore close exception
                                        }
                                    }
//                                }
                            }

                            if (DEBUG) Log.d("Xposed", "after setting class loader and context loadPackageParam.packageName: " + loadPackageParam.packageName);

                            Intent replaceIntent = PIntentUtil.replaceIntent(originalIntent,
                                    receiverPkgName, context, mEnParam, mKeyPair);
                            // replace the intent
                            XposedHelpers.setObjectField(param.args[0], "intent", replaceIntent);

                            p.recycle();

                            if (MEASURE_B) {
                                Log.d("Xposed", "hook deliverToRegisteredReceiverLocked stop ---> " + System.currentTimeMillis());
                            }
                            if (DEBUG) XposedBridge.log("beforeHookedMethod deliverToRegisteredReceiverLocked -- ");
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (DEBUG) {
                                XposedBridge.log("afterHookedMethod deliverToRegisteredReceiverLocked");
                            }
                        }
                    });
        } catch (XposedHelpers.ClassNotFoundError ex) {
            if (DEBUG) XposedBridge.log("ERROR: ClassNotFoundError");
        }


        /**
         * Hook StartActivity
         */
        findAndHookMethod(Activity.class, "startActivity", Intent.class, Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (DEBUG) XposedBridge.log("beforeHookedMethod - startActivity");

                if (MEASURE_A) {
                    Intent ninini = (Intent) param.args[0];
                    Log.d("Xposed", "原始Intent数据: " + ninini.toString());
                    Log.d("Xposed", "hook startActivity start ---> " + System.currentTimeMillis());
                }


//                String activityName = param.thisObject.getClass().getName();
//                if (DEBUG) Log.d("Xposed", "Started activity: " + activityName);

                Intent originalIntent = (Intent) param.args[0];
                if (DEBUG) Log.d("Xposed", "原始Intent数据: " + originalIntent.toString());

                Intent hostIntent = new Intent(originalIntent);
                Intent parasiteIntent;

                /**
                 * if hostIntent already contains parasateIntent --- check getExtra(PARASITE) != null
                 * and deserilize the byte[] to parasateIntent, adding new sendlist, serialize it again
                 * finally put into the host intent again
                 * TODO: we have not tested below logic yet
                 */
                if (DEBUG) Log.d(TAG, "getByteArrayExtra(\"PARASITE\")");
                byte[] byteTry = hostIntent.getByteArrayExtra("PARASITE");
                if (byteTry != null) {
                    if (DEBUG) Log.e(TAG, "has PARASITE intent");
                    // recover PARASITE intent to Normal originial intent
                    byte[] decyptedParasite = EncryptionUtil.decrypt(byteTry, intentKey);
                    Parcel p = Parcel.obtain();
                    p.unmarshall(decyptedParasite, 0, decyptedParasite.length);
                    p.setDataPosition(0);

                    Intent originalParasiteIntent;

                    try {
                        if (DEBUG) Log.d(TAG, "back to normal Intent");
                        p.setDataPosition(0);
                        originalParasiteIntent = Intent.CREATOR.createFromParcel(p);
                    } catch (Exception ex) {
                        // if cannot reconstruct to be a normal Intent
                        // then try RemoteIntent
                        if (DEBUG) Log.e(TAG, "cannot be a normal intent" + ex.toString());
                        if (DEBUG) Log.d(TAG, "back to Remote Intent");
                        p.setDataPosition(0);
                        RemoteIntent originalParasiteIntentBT = RemoteIntent.CREATOR.createFromParcel(p);
                        Intent back2Normal = originalParasiteIntentBT.backtoIntent();
                        back2Normal.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        originalParasiteIntent = back2Normal;
                    }

                    // if parasiteIntent is null, it will throw null pointer exception after a while
                    parasiteIntent = originalParasiteIntent;

                } else {
                    parasiteIntent = new Intent(originalIntent);
                }

                /**
                 * The process is following:
                 * 1. get Context and package name
                 * 2. getPermissions by the package name
                 * 3. insert the sender list into the intent
                 * 4. serialize it
                 * 5. encrypt the serialized byte
                 * 6. insert the encrypted byte to the host intent
                 * 7. replace the original intent ot the host intent
                 * TODO: at the entry point (StartActivity) encrypt every string and biginteger
                 * TODO: at the exit point (performLaunchActivity) decrypt above variables
                 */

                /**
                 * get Context
                 * https://stackoverflow.com/questions/28059264/how-to-get-context-through-hooking-in-android
                 * https://github.com/rovo89/XposedBridge/blob/master/src/android/app/AndroidAppHelper.java#L131
                 */
//                Context context = (Context) AndroidAppHelper.currentApplication();

                // create parasite intent
                // store necessary information
                String packageName = loadPackageParam.packageName;
                ArrayList<String> senderList = parasiteIntent.getStringArrayListExtra("senderList");
                if (senderList == null) {
                    senderList = new ArrayList<String>();
                }

                // 排除系统应用
                // if ((loadPackageParam.appInfo.flags &
                // (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 1) {...}

                //check system app, exclude all system apps' package name
                boolean isSysApp = PIntentUtil.checkSystemApps(packageName);

                if (!isSysApp) {
                    senderList.add(packageName);
                    parasiteIntent.removeExtra("senderList");
                    parasiteIntent.putStringArrayListExtra("senderList", senderList);
                }

                // clear host intent
                hostIntent = PIntentUtil.removeAllExtras(hostIntent);

                // for send via BT
                RemoteIntent hostIntentBT = new RemoteIntent(hostIntent);
                RemoteIntent parasiteIntentBT = new RemoteIntent(parasiteIntent);

                // parcel it
                // https://bitbucket.org/yinliu/poly-intent/commits/7af53e3793d50e47f46709f6b0f6b933d09c911b
                Parcel data = Parcel.obtain();
                parasiteIntent.writeToParcel(data, 0);
                byte[] parasite = data.marshall();

                /* calculate a random id and set it to the intent field ??? */
                /* calculate SHA-256 for integrity check ??? */

                // encrypt the parasite data
                // parasite it to the host
                byte[] encyptedParasite = EncryptionUtil.encrypt(parasite, intentKey);
                hostIntent.putExtra(PARASITE, encyptedParasite);

                // replace the parameter
                param.args[0] = hostIntent;

                // TODO user set which app can send across the device, they also can restrict all intent for sending out
                XSharedPreferences intance = PIntentUtil.getIntance();
                authList = intance.getStringSet("AUTH_LIST", null);

                //if ("com.pintent.sender".equals(packageName)) {
                if (!authList.isEmpty() && authList.contains(packageName)) {
                    // send via BT
                    Parcel dataBT = Parcel.obtain();
                    parasiteIntentBT.writeToParcel(dataBT, 0);
                    byte[] parasiteBT = dataBT.marshall();
                    // parasite it to the host
                    byte[] encyptedParasiteBT = EncryptionUtil.encrypt(parasiteBT, intentKey);
                    hostIntentBT.putParasiteExtra(encyptedParasiteBT);

                    // finally parcel it to byte array
                    Parcel hostDataBT = Parcel.obtain();
                    hostIntentBT.writeToParcel(hostDataBT, 0);
                    byte[] hostBT = hostDataBT.marshall();

                    // D2D service start:
                    try {
                        ICustomService mService = ICustomService.Stub.asInterface(ServiceManager.getService("user.custom.service"));
                        mService.startBTConnection();
                        mService.sendByBT(hostBT);
                        mService.stopBTConnection();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }

                    dataBT.recycle();
                    hostDataBT.recycle();
                }

                senderList.clear();
                data.recycle();
                if (MEASURE_A) {
                    Log.d("Xposed", "hook startActivity stop ---> " + System.currentTimeMillis());
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (DEBUG) XposedBridge.log("afterHookedMethod - startActivity");
            }
        });

        /**
         * HOOK performLaunchActivity
         * in  /frameworks/base/core/java/android/app/ActivityThread.java
         *
         * Reference:
         *         final Class activityClientRecordClazz  = loadPackageParam.classLoader.loadClass("android.app.ActivityThread$ActivityClientRecord");
         *         Log.d("Xposed", "activityClientRecordClazz == " + activityClientRecordClazz);
         *         final Class<?> Map= XposedHelpers.findClass("java.util.Map", loadPackageParam.classLoader);
         */
        findAndHookMethod("android.app.ActivityThread",
                loadPackageParam.classLoader,//获取classLoader
                "performLaunchActivity",
                "android.app.ActivityThread$ActivityClientRecord", Intent.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (DEBUG) XposedBridge.log("beforeHookedMethod performLaunchActivity ++");

                        if (MEASURE_A) {
                            String test_string_here = param.thisObject.getClass().getName();
                            Log.d("Xposed", "Started activity: " + test_string_here);

                            String ninini = loadPackageParam.packageName;
                            Log.d("Xposed", "receiverPackageName: " + ninini);

                            Log.d("Xposed", "hook performLaunchActivity start ---> " + System.currentTimeMillis());
                        }

//                        String activityName = param.thisObject.getClass().getName();
//                        if (DEBUG) {
//                            Log.d("Xposed", "Started activity: " + activityName);
//                            Log.d("Xposed", "param length" + param.args.length);
//                        }

                        String receiverPackageName = loadPackageParam.packageName;
                        if (DEBUG) Log.d("Xposed", "receiverPackageName: " + receiverPackageName);

//                        final Class activityClientRecordClazz = XposedHelpers.findClass("android.app.ActivityThread$ActivityClientRecord", loadPackageParam.classLoader);
                        Object r = param.args[0];

                        if (DEBUG) {
//                            Log.d("Xposed", "activityClientRecordClazz == " + activityClientRecordClazz);
                            Log.d("Xposed", "data.getClass() == " + r.getClass().toString());
                            Log.d("Xposed", "param.args[0].getClass().toString() == " + param.args[0].getClass().toString());
                        }

                        Intent hostIntent = (Intent) XposedHelpers.getObjectField(r, "intent");
                        if (hostIntent == null) {
                            if (DEBUG) Log.e("Xposed", "host intent is null, return");
                            return;
                        }
                        if (DEBUG) Log.d("Xposed", "目的地hostIntent数据: " + hostIntent.toString());

                        // get original intent back
                        if (DEBUG) Log.d("Xposed", "getByteArrayExtra(\"PARASITE\")");
                        byte[] mbyte = hostIntent.getByteArrayExtra("PARASITE");
                        if (mbyte == null) {
                            if (DEBUG) Log.e("Xposed", "PARASITE intent is null, return");
                            return;
                        }
                        byte[] decyptedParasite = EncryptionUtil.decrypt(mbyte, intentKey);
                        Parcel p = Parcel.obtain();
                        p.unmarshall(decyptedParasite, 0, decyptedParasite.length);
                        p.setDataPosition(0);


                        Intent originalIntent = new Intent();
                        // Intent originalIntent = Intent.CREATOR.createFromParcel(p);
                        if (DEBUG) Log.d("Xposed", "PolyIntent.CREATOR.createFromParcel(p)");
                        try {
                            p.setDataPosition(0);
                            originalIntent = Intent.CREATOR.createFromParcel(p);
                        } catch (Exception ex) {
                            // if cannot reconstruct to be a normal Intent
                            // then try RemoteIntent
                            if (DEBUG) {
                                Log.e("Xposed", "cannot be a normal intent" + ex.toString());
                                //TODO: for RemoteIntent
                                Log.d("Xposed", "TAG PolyIntent.CREATOR.createFromParcel(p)");
                            }
                            p.setDataPosition(0);
                            RemoteIntent originalhostIntent = RemoteIntent.CREATOR.createFromParcel(p);
                            if (DEBUG) {
                                Log.d("Xposed", "originalhostIntent.mAction" + originalhostIntent.mAction);
                                Log.d("Xposed", "originalhostIntent.mCategories" + originalhostIntent.mCategories);
                            }
                            Intent back2Normal = originalhostIntent.backtoIntent();
                            back2Normal.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            originalIntent = back2Normal;
                        }

                        // NOTE: important to set the classloader
                        originalIntent.setExtrasClassLoader(loadPackageParam.classLoader);

                        Context context = (Context) AndroidAppHelper.currentApplication();

                        File targetFile = new File(path + "HEkey");
//                        if (targetFile.exists() && context != null) {
                        if (targetFile.exists()) {

                            // get HEKey and RSAKey back
                            XSharedPreferences intance = PIntentUtil.getIntance();
                            String HEKeyBase64 = intance.getString("HEKey", null);
                            String RSAkeyBase64 = intance.getString("RSAkey", null);
                            byte[] HEKeybytes = Base64.decode(HEKeyBase64, 0);
                            byte[] RSAkeybytes = Base64.decode(RSAkeyBase64, 0);
                            ByteArrayInputStream bisHEKey = new ByteArrayInputStream(HEKeybytes);
                            ByteArrayInputStream bisRSAkey = new ByteArrayInputStream(RSAkeybytes);
                            ObjectInput inHE = null;
                            ObjectInput inRSA = null;
                            try {
                                inHE = new ObjectInputStream(bisHEKey);
                                Object oHE = inHE.readObject();
                                inRSA = new ObjectInputStream(bisRSAkey);
                                Object oRSA = inRSA.readObject();

                                HomoFactory.param = (EncryptionParameters) oHE;
                                mEnParam = (EncryptionParameters) oHE;
                                mKeyPair = (KeyPair) oRSA;

                            } finally {
                                try {
                                    if (inHE != null)
                                        inHE.close();
                                    if (inRSA != null)
                                        inRSA.close();
                                    if (bisHEKey != null)
                                        bisHEKey.close();
                                    if (bisRSAkey != null)
                                        bisRSAkey.close();
                                } catch (IOException ex) {
                                    // ignore close exception
                                }
                            }


                            /**
                             * below file IO solution is obsoleted
                             */
                            // NOTE: should not use flags "Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY" like
                            // mGbContext = context.createPackageContext(modulePackageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                            // otherwise, Xposed will trigger performLaunchActivity function again and load a new com.yl.middleware package
/*                            mGbContext = context.createPackageContext(modulePackageName, 0);
                            if (mGbContext != null) {
                                IOHelper ioHelper = new IOHelper(mGbContext);
                                // get HEKey back
                                EncryptionParameters enparam = (EncryptionParameters) ioHelper.getObjFromFile("HEkey");
                                HomoFactory.param = enparam;
                                mEnParam = enparam;
                                // get RSAKey back
                                KeyPair keyPair = (KeyPair) ioHelper.getObjFromFile("RSAkey");
                                mKeyPair = keyPair;
                            }*/
                        }

                        if (DEBUG) Log.d("Xposed", "after setting class loader and context loadPackageParam.packageName: " + loadPackageParam.packageName);
                        //String receiverPackageName = loadPackageParam.packageName;
//                        Intent replaceIntent = PIntentUtil.replaceIntent(originalIntent,
//                                receiverPackageName, context, mEnParam, mStrKey);
                        Intent replaceIntent = PIntentUtil.replaceIntent(originalIntent,
                                receiverPackageName, context, mEnParam, mKeyPair);
                        // replace the intent
                        XposedHelpers.setObjectField(param.args[0], "intent", replaceIntent);

                        p.recycle();

                        if (MEASURE_A) {
                            Log.d("Xposed", "hook performLaunchActivity stop ---> " + System.currentTimeMillis());
                        }

                        if (DEBUG) XposedBridge.log("beforeHookedMethod performLaunchActivity --");
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (DEBUG) {
                            XposedBridge.log("afterHookedMethod performLaunchActivity ++ ");
                            XposedBridge.log("afterHookedMethod performLaunchActivity -- ");
                        }
                    }
                });

//        // release
//        sysBroadcast.clear();
//        sysBroadcast = null;
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        if (DEBUG) XposedBridge.log("initZygote ++ ");
        //this.startupparam = startupParam;

        //final Class<?> activitythread= XposedHelpers.findClass("android.app.ActivityThread", null);
        XposedBridge.hookAllMethods(ActivityThread.class, "systemMain", new XC_MethodHook() {
            private CustomService oInstance;
            private boolean systemHooked;

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (systemHooked) {
                    return;
                }
                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Class activityManagerServiceClazz = null;
                try {
                    activityManagerServiceClazz = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", classLoader);
                } catch (RuntimeException e) {
                    // do nothing
                }
                if (!systemHooked && activityManagerServiceClazz != null) {
                    systemHooked = true;
                    //系统服务启动完毕，通知自定义服务
                    XposedBridge.hookAllMethods(
                            activityManagerServiceClazz,
                            "systemReady",
                            new XC_MethodHook() {
                                @Override
                                protected final void afterHookedMethod(final MethodHookParam param) {
                                    oInstance.systemReady();
//                                    XposedBridge.log(">>>systemReady!!!!");
                                }
                            }
                    );
                    //注册自定义服务到系统服务中
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        XposedBridge.hookAllConstructors(activityManagerServiceClazz, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                                registerService(classLoader, context);
                            }
                        });
                    } else {
                        XposedBridge.hookAllMethods(
                                activityManagerServiceClazz,
                                "main",
                                new XC_MethodHook() {
                                    @Override
                                    protected final void afterHookedMethod(final MethodHookParam param) {
                                        Context context = (Context) param.getResult();
                                        registerService(classLoader, context);
                                    }
                                }
                        );
                    }
                }
            }

            private void registerService(final ClassLoader classLoader, Context context) {
                if (DEBUG) XposedBridge.log(">>>register service, Build.VERSION.SDK_INT" + Build.VERSION.SDK_INT);
                oInstance = new CustomService(context);
                Class<?> ServiceManager = XposedHelpers.findClass("android.os.ServiceManager", classLoader);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    //避免java.lang.SecurityException错误,从5.0开始，selinux服务名称需要加前缀"user."
                    XposedHelpers.callStaticMethod(
                            ServiceManager,
                            "addService",
                            "user.custom.service",
                            oInstance,
                            true
                    );
                } else {
                    XposedHelpers.callStaticMethod(
                            ServiceManager,
                            "addService",
                            "custom.service",
                            oInstance
                    );
                }
            }
        });
    }
}
