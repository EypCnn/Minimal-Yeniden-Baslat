package com.eypcnn.reboot;

/**
 * EypCnn
 */
public interface Constants {
    int ARCH_UNKNOWN        = 0;
    int ARCH_ROCKCHIP_28    = 1;
    int ARCH_ROCKCHIP_29    = 2;
    int ARCH_ROCKCHIP_30    = 3;
    int ARCH_ROCKCHIP_31_KK = 4;
    int ARCH_ALLWINNER      = 5;
    int ARCH_ALLWINNER_GB   = 6;

    String REBOOT_RECOVERY_IMG_FILENAME = "misc_recovery.img";

    String OLD_APPLICATION_PACKAGE_NAME = "petrus.tools.ic_launcher_reboot";

    int DIALOG_NO_ROOT_ID           = 0;
    int DIALOG_REBOOT_ID            = 1;
    int DIALOG_SOFT_REBOOT_ID       = 2;
    int DIALOG_REBOOT_RECOVERY_ID   = 3;
    int DIALOG_REBOOT_BOOTLOADER_ID = 4;
    int DIALOG_REBOOT_DOWNLOAD_ID   = 5;
    int DIALOG_POWER_OFF_ID         = 6;
    int DIALOG_REMOVE_OLD_APP_ID    = 7;
}
