package com.eypcnn.reboot.utils;

import android.os.Build;
import android.util.Log;

import com.eypcnn.reboot.Constants;

/**
 * EypCnn
 */
public class Device {
    private static final String TAG = "Device";

    private int arch;
    private String productBoard;
    private String productDevice;
    private String productName;

    public Device() {
        productBoard = SystemUtils.getProp("ro.product.board");
        productDevice = SystemUtils.getProp("ro.product.device");
        productName = SystemUtils.getProp("ro.product.name");

        Log.v(TAG, "Detected arch : \"" + productBoard + "\"");
        Log.v(TAG, "Detected product name : \"" + productName + "\"");

        if (productBoard.startsWith("rk28")) {
            Log.v(TAG, "arch = ROCKCHIP_28");
            arch = Constants.ARCH_ROCKCHIP_28;
        } else if (productBoard.startsWith("rk29")) {
            Log.v(TAG, "arch = ROCKCHIP_29");
            arch = Constants.ARCH_ROCKCHIP_29;
        } else if (productBoard.startsWith("rk30")) {
            if (Build.VERSION.SDK_INT>=19 &&
                    ( productDevice.equalsIgnoreCase("rk3188") ||
                            productName.equalsIgnoreCase("rk3188") ) ) {
                Log.v(TAG, "arch = ROCKCHIP_31_KK");
                arch = Constants.ARCH_ROCKCHIP_31_KK;
            } else {
                Log.v(TAG, "arch = ROCKCHIP_30");
                arch = Constants.ARCH_ROCKCHIP_30;
            }
        } else if (productBoard.equalsIgnoreCase("crane")
                || productBoard.equalsIgnoreCase("nuclear")) {
            if (Build.VERSION.SDK_INT==9 || Build.VERSION.SDK_INT==10) {
                Log.v(TAG, "arch = ALLWINNER_GB");
                arch = Constants.ARCH_ALLWINNER_GB;
            } else {
                Log.v(TAG, "arch = ALLWINNER");
                arch = Constants.ARCH_ALLWINNER;
            }
        } else {
            Log.v(TAG, "arch = UNKNOWN");
            arch = Constants.ARCH_UNKNOWN;
        }
    }

    public int getArch() {
        return arch;
    }

    public String getProductName() {
        return productName;
    }
}
