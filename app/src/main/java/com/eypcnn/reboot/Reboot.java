package com.eypcnn.reboot;

import com.eypcnn.reboot.utils.Device;
import com.eypcnn.reboot.utils.FileSystemUtils;
import com.eypcnn.reboot.utils.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

/**
 * EypCnn
 */
public class Reboot extends Activity {
    private static final String TAG = "Reboot";

    private static final boolean TITLE_IN_LAYOUT = true;

    private static boolean performingRebootOrPowerOff = false;

    private Device device = null;

	private ImageView menuIcon = null;

	private Button rebootButton = null;
	private Button softRebootButton = null;
	private Button rebootRecoveryButton = null;
	private Button rebootBootloaderButton = null;
	private Button rebootDownloadButton = null;
	private Button powerOffButton = null;
	private Button cancelButton = null;

	private SharedPreferences sharedPref = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        /* Disabled root check at startup. Root permission should be asked later in this method
           when calling "new Device()"
           I hope removing this call solves the problem on some devices */
		/* if (!SystemUtils.isDeviceRooted()) {
			showDialog(Constants.DIALOG_NO_ROOT_ID);
		}*/

		Window window = getWindow();

        if (TITLE_IN_LAYOUT) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.main_with_title);
        } else {
            window.requestFeature(Window.FEATURE_CUSTOM_TITLE);
            setContentView(R.layout.main_without_title);
            window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        }

		menuIcon = (ImageView) findViewById(R.id.title_menu_icon);
		rebootButton = (Button) findViewById(R.id.reboot_button);
		softRebootButton = (Button) findViewById(R.id.soft_reboot_button);
		rebootRecoveryButton = (Button) findViewById(R.id.reboot_recovery_button);
		rebootBootloaderButton = (Button) findViewById(R.id.reboot_bootloader_button);
		rebootDownloadButton = (Button) findViewById(R.id.reboot_download_button);
		powerOffButton = (Button) findViewById(R.id.power_off_button);
		cancelButton = (Button) findViewById(R.id.cancel_button);

        setupButtonsListeners();

        device = new Device();

        PreferenceManager.setDefaultValues(this, R.xml.ayyarlar, false);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		updatePrefs();

		sharedPref.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                updatePrefs();
            }
        });
	}

    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_settings:
				Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
				startActivity(settingsIntent);
				return true;
			case R.id.menu_close:
				Reboot.this.finish();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	private void updatePrefs() {
		String rebootButtonVisibility = sharedPref.getString("reboot_button", "show");
		if (rebootButtonVisibility.equals("show")) {
			rebootButton.setVisibility(View.VISIBLE);			
		} else {
			rebootButton.setVisibility(View.GONE);			
		}
		
		String softRebootButtonVisibility = sharedPref.getString("soft_reboot_button", "show");
		if (softRebootButtonVisibility.equals("show")) {
			softRebootButton.setVisibility(View.VISIBLE);			
		} else {
			softRebootButton.setVisibility(View.GONE);			
		}

		String rebootRecoveryButtonVisibility = sharedPref.getString("reboot_recovery_button", "show");
		if (rebootRecoveryButtonVisibility.equals("show")) {
			rebootRecoveryButton.setVisibility(View.VISIBLE);			
		} else {
			rebootRecoveryButton.setVisibility(View.GONE);			
		}

		String rebootBootloaderButtonVisibility = sharedPref.getString("reboot_bootloader_button", "auto");
		if (rebootBootloaderButtonVisibility.equals("auto")) {
			switch (device.getArch()) {
				case Constants.ARCH_ROCKCHIP_30:
				case Constants.ARCH_ROCKCHIP_31_KK:
				case Constants.ARCH_UNKNOWN:
					rebootBootloaderButton.setVisibility(View.VISIBLE);
					break;
				default:
					rebootBootloaderButton.setVisibility(View.GONE);
			}
		} else if (rebootBootloaderButtonVisibility.equals("show")) {
			rebootBootloaderButton.setVisibility(View.VISIBLE);			
		} else {
			rebootBootloaderButton.setVisibility(View.GONE);			
		}

		String rebootDownloadButtonVisibility = sharedPref.getString("reboot_download_button", "hide");
		if (rebootDownloadButtonVisibility.equals("show")) {
			rebootDownloadButton.setVisibility(View.VISIBLE);
		} else {
			rebootDownloadButton.setVisibility(View.GONE);
		}

		String powerOffButtonVisibility = sharedPref.getString("power_off_button", "show");
		if (powerOffButtonVisibility.equals("show")) {
			powerOffButton.setVisibility(View.VISIBLE);			
		} else {
			powerOffButton.setVisibility(View.GONE);			
		}

        if (sharedPref.getBoolean("check_old_app", true) && isOldApplicationInstalled()) {
            showDialog(Constants.DIALOG_REMOVE_OLD_APP_ID);
        }
    }

    private void setupButtonsListeners() {
        menuIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                registerForContextMenu(view);
                openContextMenu(view);
                unregisterForContextMenu(view);
            }
        });

        rebootButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmActions()) {
                    showDialog(Constants.DIALOG_REBOOT_ID);
                } else {
                    reboot();
                }
            }
        });

        softRebootButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmActions()) {
                    showDialog(Constants.DIALOG_SOFT_REBOOT_ID);
                } else {
                    softReboot();
                }
            }
        });

        rebootRecoveryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmActions()) {
                    showDialog(Constants.DIALOG_REBOOT_RECOVERY_ID);
                } else {
                    rebootRecovery();
                }
            }
        });

        rebootBootloaderButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmActions()) {
                    showDialog(Constants.DIALOG_REBOOT_BOOTLOADER_ID);
                } else {
                    rebootBootloader();
                }
            }
        });

        rebootDownloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmActions()) {
                    showDialog(Constants.DIALOG_REBOOT_DOWNLOAD_ID);
                } else {
                    rebootDownload();
                }
            }
        });

        powerOffButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmActions()) {
                    showDialog(Constants.DIALOG_POWER_OFF_ID);
                } else {
                    powerOff();
                }
            }
        });

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private boolean confirmActions() {
        return sharedPref.getBoolean("confirm_actions", true);
    }

    private boolean executeInMainThread() {
        return sharedPref.getBoolean("execute_in_main_thread", true);
    }

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		switch (id) {
			case Constants.DIALOG_NO_ROOT_ID:
				builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.message_no_root)
						.setCancelable(false)
						.setPositiveButton(R.string.exit,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										Reboot.this.finish();
									}
								});
				dialog = builder.create();
				break;
			case Constants.DIALOG_REBOOT_ID:
				builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.message_confirm_reboot)
						.setCancelable(false)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										Reboot.this.reboot();
									}
								})
						.setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								});
				dialog = builder.create();
				break;
			case Constants.DIALOG_SOFT_REBOOT_ID:
				builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.message_confirm_soft_reboot)
						.setCancelable(false)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										Reboot.this.softReboot();
									}
								})
						.setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										dialog.cancel();
									}
								});
				dialog = builder.create();
				break;
			case Constants.DIALOG_REBOOT_RECOVERY_ID:
				builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.message_confirm_reboot_recovery)
						.setCancelable(false)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										Reboot.this.rebootRecovery();
									}
								})
						.setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										dialog.cancel();
									}
								});
				dialog = builder.create();
				break;
			case Constants.DIALOG_REBOOT_BOOTLOADER_ID:
				builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.message_confirm_reboot_bootloader)
						.setCancelable(false)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										Reboot.this.rebootBootloader();
									}
								})
						.setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										dialog.cancel();
									}
								});
				dialog = builder.create();
				break;
			case Constants.DIALOG_REBOOT_DOWNLOAD_ID:
					builder = new AlertDialog.Builder(this);
					builder.setMessage(R.string.message_confirm_reboot_download)
							.setCancelable(false)
							.setPositiveButton(R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,	int id) {
											Reboot.this.rebootDownload();
										}
									})
							.setNegativeButton(R.string.no,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,	int id) {
											dialog.cancel();
										}
									});
					dialog = builder.create();
					break;
			case Constants.DIALOG_POWER_OFF_ID:
				builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.message_confirm_power_off)
						.setCancelable(false)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										Reboot.this.powerOff();
									}
								})
						.setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										dialog.cancel();
									}
								});
				dialog = builder.create();
				break;
			case Constants.DIALOG_REMOVE_OLD_APP_ID:
				builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.message_confirm_old_app_removal)
						.setCancelable(false)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										Reboot.this.removeOldApplication();
									}
								})
						.setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								});
				dialog = builder.create();
				break;
			default:
				dialog = null;
		}
		return dialog;
	}

    private boolean isOldApplicationInstalled() {
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo(Constants.OLD_APPLICATION_PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void removeOldApplication() {
        if (executeInMainThread()) {
            execRemoveOldApplication();
        } else {
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... voids) {
                    execRemoveOldApplication();
                    return null;
                }
            }.execute();
        }
    }

    private void execRemoveOldApplication() {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo applicationInfo =
                    packageManager.getApplicationInfo(Constants.OLD_APPLICATION_PACKAGE_NAME, 0);
            Log.i(TAG, "dataDir : "+applicationInfo.dataDir);
            Log.i(TAG, "sourceDir : "+applicationInfo.sourceDir);
            Log.i(TAG, "publicSourceDir : "+applicationInfo.publicSourceDir);
            Log.i(TAG, "nativeLibraryDir : "+applicationInfo.nativeLibraryDir);
            if (null!=applicationInfo.sourceDir) {
                List<String> commands = new ArrayList<>();
                commands.add("rm "+applicationInfo.sourceDir);
                if (null!=applicationInfo.dataDir) {
                    commands.add("rm -rf " + applicationInfo.dataDir);
                }
                if (null!=applicationInfo.nativeLibraryDir) {
                    commands.add("rm -rf " + applicationInfo.nativeLibraryDir);
                }
                commands.add("rm /data/app/"+Constants.OLD_APPLICATION_PACKAGE_NAME+".apk");
                commands.add("rm /data/app/"+Constants.OLD_APPLICATION_PACKAGE_NAME+"-*.apk");
                commands.add("mount -o remount,rw /system");
                commands.add("rm /system/app/"+Constants.OLD_APPLICATION_PACKAGE_NAME+".apk");
                commands.add("rm /system/app/"+Constants.OLD_APPLICATION_PACKAGE_NAME+"-*.apk");
                commands.add("rm /system/priv-app/"+Constants.OLD_APPLICATION_PACKAGE_NAME+".apk");
                commands.add("rm /system/priv-app/"+Constants.OLD_APPLICATION_PACKAGE_NAME+"-*.apk");
                commands.add("mount -o remount,ro /system");
                SystemUtils.runAsRoot(commands);
                softReboot();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package \""+Constants.OLD_APPLICATION_PACKAGE_NAME+"\" not found", e);
        }
    }

    private void stopWifi() {
        boolean stop_wifi;

        String stop_wifi_pref = sharedPref.getString("stop_wifi", "auto");
        if (stop_wifi_pref.equals("auto")) {
            if (device.getProductName().equalsIgnoreCase("iMito QX1")) {
                stop_wifi = true;
            } else {
                stop_wifi = false;
            }
        } else if (stop_wifi_pref.equals("no")) {
            stop_wifi = false;
        } else {
            stop_wifi = true;
        }

		if (stop_wifi) {
			SystemUtils.runAsRoot("svc wifi disable");
		}
	}
	
	private void remountEverythingRO() {
		if (sharedPref.getBoolean("remount_everything_ro", false)) {
            FileSystemUtils.remountEverythingRO();
		}
	}

	private synchronized void reboot() {
        if (executeInMainThread()) {
            execReboot();
        } else if (!performingRebootOrPowerOff) {
            performingRebootOrPowerOff = true;
            new AsyncTask<Void, Void, Void>() {
                private ProgressDialog progressDialog = null;

                protected void onPreExecute() {
                    progressDialog = ProgressDialog.show(Reboot.this,
                            getText(R.string.reboot),
                            getText(R.string.message_please_wait),
                            true);
                }

                protected Void doInBackground(Void... voids) {
                    execReboot();
                    return null;
                }

                protected void onPostExecute(Void v) {
                    if (null!=progressDialog) {
                        progressDialog.cancel();
                        progressDialog = null;
                    }
                    performingRebootOrPowerOff = false;
                }
            }.execute();
        }
	}

	private synchronized void softReboot() {
        if (executeInMainThread()) {
            execSoftReboot();
        } else if (!performingRebootOrPowerOff) {
            performingRebootOrPowerOff = true;
            new AsyncTask<Void, Void, Void>() {
                private ProgressDialog progressDialog = null;

                protected void onPreExecute() {
                    progressDialog = ProgressDialog.show(Reboot.this,
                            getText(R.string.soft_reboot),
                            getText(R.string.message_please_wait),
                            true);
                }

                protected Void doInBackground(Void... voids) {
                    execSoftReboot();
                    return null;
                }

                protected void onPostExecute(Void v) {
                    if (null!=progressDialog) {
                        progressDialog.cancel();
                        progressDialog = null;
                    }
                    performingRebootOrPowerOff = false;
                }
            }.execute();
        }
	}

	private synchronized void rebootRecovery() {
        if (executeInMainThread()) {
            execRebootRecovery();
        } else if (!performingRebootOrPowerOff) {
            performingRebootOrPowerOff = true;
            new AsyncTask<Void, Void, Void>() {
                private ProgressDialog progressDialog = null;

                protected void onPreExecute() {
                    progressDialog = ProgressDialog.show(Reboot.this,
                            getText(R.string.reboot_recovery),
                            getText(R.string.message_please_wait),
                            true);
                }

                protected Void doInBackground(Void... voids) {
                    execRebootRecovery();
                    return null;
                }

                protected void onPostExecute(Void v) {
                    if (null!=progressDialog) {
                        progressDialog.cancel();
                        progressDialog = null;
                    }
                    performingRebootOrPowerOff = false;
                }
            }.execute();
        }
	}

	private synchronized void rebootBootloader() {
        if (executeInMainThread()) {
            execRebootBootloader();
        } else if (!performingRebootOrPowerOff) {
            performingRebootOrPowerOff = true;
            new AsyncTask<Void, Void, Void>() {
                private ProgressDialog progressDialog = null;

                protected void onPreExecute() {
                    progressDialog = ProgressDialog.show(Reboot.this,
                            getText(R.string.reboot_bootloader),
                            getText(R.string.message_please_wait),
                            true);
                }

                protected Void doInBackground(Void... voids) {
                    execRebootBootloader();
                    return null;
                }

                protected void onPostExecute(Void v) {
                    if (null!=progressDialog) {
                        progressDialog.cancel();
                        progressDialog = null;
                    }
                    performingRebootOrPowerOff = false;
                }
            }.execute();
        }
	}

	private synchronized void rebootDownload() {
        if (executeInMainThread()) {
            execRebootDownload();
        } else if (!performingRebootOrPowerOff) {
            performingRebootOrPowerOff = true;
            new AsyncTask<Void, Void, Void>() {
                private ProgressDialog progressDialog = null;

                protected void onPreExecute() {
                    progressDialog = ProgressDialog.show(Reboot.this,
                            getText(R.string.reboot_download),
                            getText(R.string.message_please_wait),
                            true);
                }

                protected Void doInBackground(Void... voids) {
                    execRebootDownload();
                    return null;
                }

                protected void onPostExecute(Void v) {
                    if (null!=progressDialog) {
                        progressDialog.cancel();
                        progressDialog = null;
                    }
                    performingRebootOrPowerOff = false;
                }
            }.execute();
        }
	}

	private synchronized void powerOff() {
        if (executeInMainThread()) {
            execPowerOff();
        } else if (!performingRebootOrPowerOff) {
            performingRebootOrPowerOff = true;
            new AsyncTask<Void, Void, Void>() {
                private ProgressDialog progressDialog = null;

                protected void onPreExecute() {
                    progressDialog = ProgressDialog.show(Reboot.this,
                            getText(R.string.power_off),
                            getText(R.string.message_please_wait),
                            true);
                }

                protected Void doInBackground(Void... voids) {
                    execPowerOff();
                    return null;
                }

                protected void onPostExecute(Void v) {
                    if (null!=progressDialog) {
                        progressDialog.cancel();
                        progressDialog = null;
                    }
                    performingRebootOrPowerOff = false;
                }
            }.execute();
        }
	}

    private void execReboot() {
        stopWifi();
        remountEverythingRO();
        SystemUtils.reboot();
    }

    private void execSoftReboot() {
        SystemUtils.softReboot();
    }

    private void execRebootRecovery() {
        stopWifi();

        boolean flash_misc;

        String flash_misc_pref = sharedPref.getString("flash_misc", "auto");
        if (flash_misc_pref.equals("auto")) {
            switch (device.getArch()) {
                case Constants.ARCH_ROCKCHIP_28:
                case Constants.ARCH_ROCKCHIP_29:
                case Constants.ARCH_ROCKCHIP_30:
                case Constants.ARCH_ALLWINNER:
                case Constants.ARCH_ALLWINNER_GB:
                    flash_misc = true;
                    break;
                case Constants.ARCH_ROCKCHIP_31_KK:
                    flash_misc = false;
                    break;
                default:
                    flash_misc = false;
                    break;
            }
        } else if (flash_misc_pref.equals("no")) {
            flash_misc = false;
        } else {
            flash_misc = true;
        }

        if (flash_misc) {
            switch (device.getArch()) {
                case Constants.ARCH_ROCKCHIP_28:
                case Constants.ARCH_ROCKCHIP_29:
                case Constants.ARCH_ROCKCHIP_30:
                case Constants.ARCH_ROCKCHIP_31_KK:
                    File recoveryImageFile = extractRecoveryImage();
                    String miscMtdDev = SystemUtils.getImageMtdDev("misc");

                    if (null != recoveryImageFile && null != miscMtdDev) {
                        SystemUtils.runAsRoot(SystemUtils.commandFromBox("dd")
                                + " if=" + recoveryImageFile.getAbsolutePath()
                                + " of=/dev/mtd/" + miscMtdDev);
                    }

                    remountEverythingRO();
                    try {
                        SystemUtils.reboot("recovery");
                    } catch (IOException e) {
                        Log.e(TAG, "Error while rebooting to recovery", e);
                    }
                    break;
                case Constants.ARCH_ALLWINNER:
                    SystemUtils.runAsRoot("echo -n boot-recovery | "
                            + SystemUtils.commandFromBox("dd")
                            + " of=/dev/block/nandf count=1 conv=sync");
                    SystemUtils.reboot();
                    break;
                case Constants.ARCH_ALLWINNER_GB:
                    SystemUtils.runAsRoot("echo -n boot-recovery | "
                            + SystemUtils.commandFromBox("dd")
                            + " of=/dev/block/nande count=1 conv=sync");
                    remountEverythingRO();
                    SystemUtils.reboot();
                    break;
                default: {
                    remountEverythingRO();
                    try {
                        SystemUtils.reboot("recovery");
                    } catch (IOException e) {
                        Log.e(TAG, "Error while rebooting to recovery", e);
                    }
                }
            }
        } else {
            remountEverythingRO();
            try {
                SystemUtils.reboot("recovery");
            } catch (IOException e) {
                Log.e(TAG, "Error while rebooting to recovery", e);
            }
        }
    }

    private void execRebootBootloader() {
        stopWifi();
        remountEverythingRO();
        SystemUtils.rebootBootloader();
    }

    private void execRebootDownload() {
        stopWifi();
        remountEverythingRO();
        try {
            SystemUtils.reboot("download");
        } catch (IOException e) {
            Log.e(TAG, "Error while rebooting in download mode", e);
        }
    }

    private void execPowerOff() {
        stopWifi();
        remountEverythingRO();
        SystemUtils.powerOff();
    }

	private File extractRecoveryImage() {
		File recoveryImageFile = new File(getApplicationContext().getFilesDir(),
                Constants.REBOOT_RECOVERY_IMG_FILENAME);
		if (!recoveryImageFile.exists()) {
			try {
                FileSystemUtils.copy(
                        getResources().openRawResource(R.raw.misc_recovery_img),
                        openFileOutput(Constants.REBOOT_RECOVERY_IMG_FILENAME, Context.MODE_PRIVATE));
			} catch (IOException e) {
				Log.e(TAG, "Error while extracting recovery image to "
						+ recoveryImageFile.getAbsolutePath(), e);
				return null;
			}
		}
		return recoveryImageFile;
	}
}