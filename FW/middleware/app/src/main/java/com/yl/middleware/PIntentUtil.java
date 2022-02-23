package com.yl.middleware;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.pintent.he.EncryptionParameters;
import com.pintent.he.HomoFactory;
import com.pintent.he.HomoInteger;
import com.pintent.he.RSAModel;

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class PIntentUtil {
    private static final long ENCRYPTED = 19870507L;
    private static final String TAG = "PIntentUtil";
    private static final boolean DEBUG = false;
//    public static XC_LoadPackage.LoadPackageParam mloadPackageParam;
//    public static Context mContext;

    // Custom method to get app requested and granted permissions from package name
    private static Set<String> getPermissionsByPackageName(String packageName, Context context) {

        //String packageName = mloadPackageParam.packageName;
        if (DEBUG)  XposedBridge.log("packageName === " + packageName);
        // Initialize a new string builder instance
        StringBuilder builder = new StringBuilder();
        Set<String> permissionList = new HashSet<>();

        try {
            // Get the package info
            PackageManager p = context.getPackageManager();
            PackageInfo packageInfo = p.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            if (packageInfo.requestedPermissions == null)
                return null;
            // Permissions counter
            int counter = 1;

            /*
                PackageInfo
                    Overall information about the contents of a package. This corresponds to all of
                    the information collected from AndroidManifest.xml.
            */
            /*
                String[] requestedPermissions
                    Array of all <uses-permission> tags included under <manifest>, or null if there
                    were none. This is only filled in if the flag GET_PERMISSIONS was set. This list
                    includes all permissions requested, even those that were not granted or known
                    by the system at install time.
            */
            /*
                int[] requestedPermissionsFlags
                    Array of flags of all <uses-permission> tags included under <manifest>, or null
                    if there were none. This is only filled in if the flag GET_PERMISSIONS was set.
                    Each value matches the corresponding entry in requestedPermissions, and will
                    have the flag REQUESTED_PERMISSION_GRANTED set as appropriate.
            */
            /*
                int REQUESTED_PERMISSION_GRANTED
                    Flag for requestedPermissionsFlags: the requested permission is currently
                    granted to the application.
            */

            /**
             * 它是可能要检查是否另一个应用程序已被授予的权限？
             * 你可以为每个检索标志 <uses-permission> tag
             * 在 Manifest 为给定的包使用 PackageInfo.requestedPermissionsFlags
             * 然后比较这些标志使用按位与运算 PackageInfo.REQUESTED_PERMISSION_GRANTED 。
             *
             * 我想要检查是否另一个应用程序已被授予"危险"或"系统"的级别权限。
             * 您可以执行此检查使用 PackageManager.getPermissionInfo
             * 然后比较 PermissionInfo.protectionLevel 之一
             * PermissionInfo.PROTECTION_DANGEROUS 或 PermissionInfo.PROTECTION_SIGNATURE
             */

            // Loop through the package info requested permissions
            for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
                if ((packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    String permission = packageInfo.requestedPermissions[i];
                    // To make permission name shorter
                    //permission = permission.substring(permission.lastIndexOf(".")+1);
                    builder.append("" + counter + ". " + permission + "\n");
                    permissionList.add(permission);
                    counter++;
                }
            }
            if (DEBUG) XposedBridge.log("permission is " + builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return permissionList;
    }

    private static int comparePermission(Set<String> reciver, Set<String> sender) {
        if ((reciver == null || reciver.isEmpty())
                && (sender == null || sender.isEmpty()))
            return 0;

        if ((reciver == null || reciver.isEmpty())
                && (sender != null && !sender.isEmpty()))
            return 1;

        if ((reciver != null && !reciver.isEmpty())
                && (sender == null || sender.isEmpty()))
            return 0;

        if (reciver.containsAll(sender))
            return 0;
        //交集
        //pl1.retainAll(pl2);
        //并集
        //pl1.addAll(pl2);
        return 1;
    }

    public static boolean checkSystemApps(String senderPackageName) {
        if (senderPackageName.equals("com.google.android.googlequicksearchbox") ||
                senderPackageName.equals("com.google.android.launcher.GEL"))
            return true;


        return false;
    }


    public static Intent replaceIntent(Intent intent, String receiverPackageName, Context context,
                                       EncryptionParameters enParam, KeyPair keyPair) throws Exception {
        ArrayList<String> senderList = intent.getStringArrayListExtra("senderList");
        Set<String> reciverPermissions = getPermissionsByPackageName(receiverPackageName, context);
        Set<String> senderPermissions = new HashSet<>();
        if (senderList != null) {
            if (DEBUG) Log.d(TAG, "senderList != null");
            for (String senderPackageName : senderList) {
                Set<String> eachSenderPermissions = getPermissionsByPackageName(senderPackageName, context);
                if (eachSenderPermissions != null) {
                    senderPermissions.addAll(eachSenderPermissions);
                    eachSenderPermissions.clear();
                }
            }
        }

        if (DEBUG) {
            if (senderPermissions != null) {
                Log.d(TAG, "senderPermissions:");
                for (String p1 : senderPermissions) {
                    Log.d(TAG, p1);
                }
            }

            if (reciverPermissions != null) {
                Log.d(TAG, "reciverPermissions:");
                for (String p2 : reciverPermissions) {
                    Log.d(TAG, p2);
                }
            }
        }
        int cmp = comparePermission(reciverPermissions, senderPermissions);
        // find the encrypt field value
        boolean isEncrypt = intent.getBooleanExtra(XposedMainInit.isEncrypted, false);

        if (cmp != 0) { // permission different - encrypt it
            Bundle bundle = intent.getExtras();
            Set<String> bundleKeySet = bundle.keySet(); // string key set
            for (String key : bundleKeySet) { // traverse and print pairs
                if (DEBUG) Log.e(TAG, key + ":" + bundle.get(key));

                Object o = bundle.get(key);
                if (o instanceof String) {
                    if (DEBUG) Log.e(TAG, "it is string");
                    // TODO check whether it has been encypted
                    String origStr = (String) o;

                    intent.removeExtra(key);

                    //String hmstr = HomoFactory.getHomoString(origStr, strKey);
                    String hmstr = null;
                    if (isEncrypt) {
                        // FIXME if already encrypted, here should first decrypt, then, encrypt
                        hmstr = RSAModel.decrypt(origStr, keyPair.getPrivate());
                    }

                    hmstr = RSAModel.encrypt(origStr, keyPair.getPublic());
                    String encryptStr = hmstr;
                    if (DEBUG) {
                        Log.d(TAG, "hmstr ==== " + hmstr.toString());
                        Log.d(TAG, "encryptStr ==== " + encryptStr.toString());
                    }
                    intent.putExtra(key, encryptStr);

                } else if ((o instanceof BigInteger)
                        && !(o instanceof HomoInteger)) {
                    if (DEBUG) Log.e(TAG, "it is BigInteger, not HomoInteger");
                    if (enParam != null) {
                        BigInteger pubKey = enParam.getPublicKey();
                        BigInteger secretKey = enParam.getSecretKey();
                        intent.removeExtra(key);
                        BigInteger origBigInt = (BigInteger) o;
                        HomoInteger ev = HomoFactory.getHomoInteger(origBigInt, pubKey);
                        intent.putExtra(key, ev);
                    } else {
                        if (DEBUG) Log.e(TAG, "Cannot replace to HomoInteger!!!");
                    }
                } else if (o instanceof HomoInteger){
                    if (DEBUG) Log.e(TAG, "it is HomoInteger, not BigInteger");
                    // all need to first decrypt and then encrypt
                    if (enParam != null) {
                        BigInteger pubKey = enParam.getPublicKey();
                        BigInteger secretKey = enParam.getSecretKey();
                        intent.removeExtra(key);
                        HomoInteger origHEInt = (HomoInteger) o;
                        BigInteger origBigInt = origHEInt.decrypt(secretKey); // decrypt it first
                        HomoInteger ev = HomoFactory.getHomoInteger(origBigInt, pubKey);
                        intent.putExtra(key, ev);
                    } else {
                        if (DEBUG) Log.e(TAG, "Cannot replace to HomoInteger!!!");
                    }
                }  // else if (o instanceof Byte) ...
            }
            intent.putExtra(XposedMainInit.isEncrypted, true);
            //return pIntent;
        } else if (cmp == 0) { // permission same - decrypt it
            Bundle bundle = intent.getExtras();
            Set<String> bundleKeySet = bundle.keySet(); // string key set
            for (String key : bundleKeySet) { // traverse and print pairs
                if (DEBUG) Log.e(TAG, key + ":" + bundle.get(key));

                Object o = bundle.get(key);
                if (o instanceof String) {
                    if (DEBUG) Log.e(TAG, "it is string");
                    if (keyPair != null && isEncrypt == true) {
                        // TODO check whether it has been encypted
                        String origStr = (String) o;
                        intent.removeExtra(key);

                        String hmstr = RSAModel.decrypt(origStr, keyPair.getPrivate());
                        String decryptStr = hmstr;
                        if (DEBUG) {
                            Log.d(TAG, "hmstr ==== " + hmstr.toString());
                            Log.d(TAG, "decryptStr ==== " + decryptStr.toString());
                        }
                        intent.putExtra(key, decryptStr);
                    }

                } else if (o instanceof HomoInteger) {
                    if (DEBUG) Log.e(TAG, "it is HomoInteger");
                    if (enParam != null && isEncrypt == true) {
                        // decrypt it
                        BigInteger pubKey = enParam.getPublicKey();
                        BigInteger secretKey = enParam.getSecretKey();
                        intent.removeExtra(key);
                        HomoInteger enHomoInt = (HomoInteger) o;
                        BigInteger origBigInt = enHomoInt.decrypt(secretKey);
                        intent.putExtra(key, origBigInt);
                    } else {
                        if (DEBUG) Log.e(TAG, "Cannot replace to HomoInteger!!!");
                    }
                }   // else if (o instanceof Byte) ...
            }
            intent.putExtra(XposedMainInit.isEncrypted, false);
        }
        return intent;
    }

    /**
     * below implementation is obsoleted
     * @param intent
     * @param receiverPackageName
     * @param context
     * @param enParam
     * @param strKey
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static Intent replaceIntent(Intent intent, String receiverPackageName,
                                       Context context, EncryptionParameters enParam, String strKey) throws NoSuchFieldException, IllegalAccessException {
        ArrayList<String> senderList = intent.getStringArrayListExtra("senderList");
        Set<String> reciverPermissions = getPermissionsByPackageName(receiverPackageName, context);
        Set<String> senderPermissions = new HashSet<>();
        if (senderList != null) {
            if (DEBUG) Log.d(TAG, "senderList != null");
            for (String senderPackageName : senderList) {
                Set<String> eachSenderPermissions = getPermissionsByPackageName(senderPackageName, context);
                if (eachSenderPermissions != null) {
                    senderPermissions.addAll(eachSenderPermissions);
                    eachSenderPermissions.clear();
                }
            }
        }

        if (DEBUG) {
            if (senderPermissions != null) {
                Log.d(TAG, "senderPermissions:");
                for (String p1 : senderPermissions) {
                    Log.d(TAG, p1);
                }
            }

            if (reciverPermissions != null) {
                Log.d(TAG, "reciverPermissions:");
                for (String p2 : reciverPermissions) {
                    Log.d(TAG, p2);
                }
            }
        }
        int cmp = comparePermission(reciverPermissions, senderPermissions);

        if (cmp != 0) { // permission different - encrypt it
            // FIXME here should first decrypt, then, encrypt

            Bundle bundle = intent.getExtras();
            Set<String> bundleKeySet = bundle.keySet(); // string key set
            for (String key : bundleKeySet) { // traverse and print pairs
                if (DEBUG) Log.e(TAG, key + ":" + bundle.get(key));

                Object o = bundle.get(key);
                if (o instanceof String) {
                    if (DEBUG) Log.e(TAG, "it is string");
                    // TODO check whether it has been encypted
                    String origStr = (String) o;
//                    Class<?> homoType = origStr.getClass();
//                    Field fieldID = homoType.getDeclaredField("serialVersionUID");
//                    fieldID.setAccessible(true);
//                    long isencrypted = fieldID.getLong(origStr);
//                    Log.d(TAG, "isencrypted ==== " + isencrypted);
//                    if (isencrypted != ENCRYPTED) { // have not been encypted
                    intent.removeExtra(key);

                    String hmstr = HomoFactory.getHomoString(origStr, strKey);
                    String encryptStr = hmstr;
                    if (DEBUG) {
                        Log.d(TAG, "hmstr ==== " + hmstr.toString());
                        Log.d(TAG, "encryptStr ==== " + encryptStr.toString());
                    }
                    intent.putExtra(key, encryptStr);
//                    }

                } else if ((o instanceof BigInteger)
                        && !(o instanceof HomoInteger)) {
                    if (DEBUG) Log.e(TAG, "it is BigInteger, not HomoInteger");
                    if (enParam != null) {
                        BigInteger pubKey = enParam.getPublicKey();
                        BigInteger secretKey = enParam.getSecretKey();
                        intent.removeExtra(key);
                        BigInteger origBigInt = (BigInteger) o;
                        HomoInteger ev = HomoFactory.getHomoInteger(origBigInt, pubKey);
                        intent.putExtra(key, ev);
                    } else {
                        if (DEBUG) Log.e(TAG, "Cannot replace to HomoInteger!!!");
                    }
                }   // else if (o instanceof Byte) ...
            }
            //return pIntent;
        } else if (cmp == 0) { // permission same - decrypt it
            Bundle bundle = intent.getExtras();
            Set<String> bundleKeySet = bundle.keySet(); // string key set
            for (String key : bundleKeySet) { // traverse and print pairs
                if (DEBUG) Log.e(TAG, key + ":" + bundle.get(key));

                Object o = bundle.get(key);
                if (o instanceof String) {
                    if (DEBUG) Log.e(TAG, "it is string");
                    // should decrypt it
//                    String origStr = (String) o;
//                    Class<?> homoType = origStr.getClass();
//                    Field fieldID = homoType.getDeclaredField("serialVersionUID");
//                    fieldID.setAccessible(true);
//                    long isencrypted = fieldID.getLong(origStr);
//                    Log.d(TAG, "isencrypted ==== " + isencrypted);
//                    if (isencrypted == ENCRYPTED) { // is encypted
//                        String dehmstr = EncryptionUtil.decrypt(strKey, origStr);
//                        String decryptStr = dehmstr;
//                        if (DEBUG) {
//                            Log.d(TAG, "dehmstr ==== " + dehmstr.toString());
//                            Log.d(TAG, "decryptStr ==== " + decryptStr.toString());
//                        }
//                        intent.removeExtra(key);
//                        intent.putExtra(key, decryptStr);
//                    }

                } else if (o instanceof HomoInteger) {
                    if (DEBUG) Log.e(TAG, "it is HomoInteger");
                    if (enParam != null) {
                        BigInteger pubKey = enParam.getPublicKey();
                        BigInteger secretKey = enParam.getSecretKey();
                        intent.removeExtra(key);
                        HomoInteger enHomoInt = (HomoInteger) o;
                        BigInteger origBigInt = enHomoInt.decrypt(secretKey);
                        intent.putExtra(key, origBigInt);
                    } else {
                        if (DEBUG) Log.e(TAG, "Cannot replace to HomoInteger!!!");
                    }
                }   // else if (o instanceof Byte) ...
            }
        }
        return intent;
    }

    public static Intent removeAllExtras(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            Set<String> bundleKeySet = bundle.keySet(); // string key set
            for (String key : bundleKeySet) { // traverse and print pairs
                if (DEBUG) Log.e(TAG, key + ":" + bundle.get(key));
                intent.removeExtra(key);
            }
        }
        return intent;
    }


    // use XSharedPreferences
    private static XSharedPreferences intance = null;

    public static XSharedPreferences getIntance(){
        if (intance == null){
            intance = new XSharedPreferences("com.yl.middleware","config");
            //intance = new XSharedPreferences("com.yl.middleware","config");
            intance.makeWorldReadable();
        }else {
            intance.reload();
        }
        return intance;
    }


}