package com.yl.middleware;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AppInfo {
    private XC_LoadPackage.LoadPackageParam mloadPackageParam;
    private Context mContext;

    public AppInfo(XC_LoadPackage.LoadPackageParam loadPackageParam, Context context) {
        this.mloadPackageParam = loadPackageParam;
        this.mContext = context;
    }

    // Custom method to get app requested and granted permissions from package name
    public Set<String> getPermissionsByPackageName() {

        String packageName = mloadPackageParam.packageName;
//        XposedBridge.log("packageName === " + packageName);
        // Initialize a new string builder instance
        StringBuilder builder = new StringBuilder();
        Set<String> permissionList = new HashSet<>();

        try {
            // Get the package info
            PackageManager p = mContext.getPackageManager();
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
             * ????????????????????????????????????????????????????????????????????????
             * ?????????????????????????????? <uses-permission> tag
             * ??? Manifest ????????????????????? PackageInfo.requestedPermissionsFlags
             * ????????????????????????????????????????????? PackageInfo.REQUESTED_PERMISSION_GRANTED ???
             *
             * ??????????????????????????????????????????????????????"??????"???"??????"??????????????????
             * ?????????????????????????????? PackageManager.getPermissionInfo
             * ???????????? PermissionInfo.protectionLevel ??????
             * PermissionInfo.PROTECTION_DANGEROUS ??? PermissionInfo.PROTECTION_SIGNATURE
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
//            XposedBridge.log("permission is " + builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return permissionList;
    }

    public int comparePermission(Set<String> pl1, Set<String> pl2){
        if (pl1.containsAll(pl2)){
            return 0;
        }
        //??????
        //pl1.retainAll(pl2);
        //??????
        //pl1.addAll(pl2);
        return 1;
    }
}