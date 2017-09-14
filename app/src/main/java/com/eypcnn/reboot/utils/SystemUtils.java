package com.eypcnn.reboot.utils;

import java.io.IOException;
import java.util.List;

import android.util.Log;

import eu.chainfire.libsuperuser.Shell;

/**
 * EypCnn
 */
public class SystemUtils {
	public static final String TAG = "SystemUtils";

	public static boolean isDeviceRooted() {
		return Shell.SU.available();
	}

    public static List<String> run(String command) {
        return Shell.SH.run(command);
    }

    public static List<String> run(List<String> commands) {
        return Shell.SH.run(commands);
    }

	public static List<String> runAsRoot(String command) {
		return Shell.SU.run(command);
	}

	public static List<String> runAsRoot(List<String> commands) {
		return Shell.SU.run(commands);
	}

    public static String commandFromBox(String command) {
        final String[] cmdsToTest = { "busybox", "toybox", "toolbox" };
        for (String testedCmd : cmdsToTest) {
			String fullTestCommand = testedCmd + " " + command + " --help &>/dev/null;echo $?";
            List<String> result = run(fullTestCommand);
            if (null!=result && !result.isEmpty() && null!=result.get(0)) {
				try {
					int returnCode = Integer.parseInt(result.get(0));
					if (returnCode<127) {
						return testedCmd + " " + command;
					}
				} catch (NumberFormatException e) {
					Log.e(TAG, "Error when parsing return code ("+result.get(0)+") of command \""+fullTestCommand+"\"", e);
				}
            }
        }
        return command;
    }

    public static String getProp(String key) {
        StringBuilder stringBuilder = new StringBuilder();

        List<String> resultLines = run("getprop " + key);
		if (null!=resultLines) {
			for (String line : resultLines) {
				stringBuilder.append(line);
			}
		}
		return stringBuilder.toString();
	}

	public static String getImageMtdDev(String name) {
		List<String> resultLines = run("cat /proc/mtd");
        if (null!=resultLines) {
            for (String line : resultLines) {
                String[] fields = line.split(" ");
                String mtdName = fields[fields.length - 1];
                if (mtdName.equals("\"" + name + "\"")) {
                    String mtdDev = fields[0];
                    String dev = mtdDev.substring(0, mtdDev.length() - 1);
                    return dev;
                }
            }
        }
		return null;
	}

	public static void reboot() {
		try {
			reboot(null);
		} catch (IOException e) {
			Log.e(TAG, "Error while powering off", e);
		}
	}

	public static void rebootBootloader() {
		try {
			reboot("bootloader");
		} catch (IOException e) {
			Log.e(TAG, "Error while rebooting to bootloader", e);
		}
	}

	public static void powerOff() {
		try {
			reboot("-p");
		} catch (IOException e) {
			Log.e(TAG, "Error while powering off", e);
		}
	}
	
	public static void reboot(String arg) throws IOException {
		FileSystemUtils.unmountAllExternalDisks();
        if (null!=arg && arg.length()>0) {
            runAsRoot("reboot "+arg);
        } else {
            runAsRoot("reboot");
        }
	}

	public static void softReboot() {
        runAsRoot(commandFromBox("pkill") + " zygote");
        runAsRoot(commandFromBox("killall") + " zygote");
	}
}
