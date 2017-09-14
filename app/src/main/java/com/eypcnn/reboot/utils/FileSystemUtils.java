package com.eypcnn.reboot.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.os.Environment;
import android.util.Log;

/**
 * EypCnn
 */
public class FileSystemUtils {
	private static final String TAG = "FileSystemUtils";

	public static String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
	public static String USB_DISK_PATH = "/mnt/udisk";

    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
        } finally {
            // Close the streams
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error when closing the input stream", e);
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error when closing the output stream", e);
            }
        }
    }

	public static void unmountAllExternalDisks() {
        List<String> commands = new ArrayList<>();

        commands.add("sync");

		String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
        	commands.add("umount "+SDCARD_PATH);
        }

        commands.add("umount "+USB_DISK_PATH);

		SystemUtils.runAsRoot(commands);
	}

    public static List<MountPoint> getMountPoints() {
        List<MountPoint> mountPoints = new LinkedList<>();

        List<String> resultLines = SystemUtils.runAsRoot("mount");
        if (null!=resultLines) {
            for (String line : resultLines) {
                mountPoints.add(new MountPoint(line));
            }
        }

        return mountPoints;
    }

	public static void remountEverythingRO() {
		List<MountPoint> mountPoints = getMountPoints();

		// sort the list in reverse path depth order
		Collections.sort(mountPoints, new Comparator<MountPoint>() {
            @Override
            public int compare(MountPoint m1, MountPoint m2) {
                return m1.getMountPointDepth() - m2.getMountPointDepth();
            }
        });

        // build the commands list
		List<String> commands = new ArrayList<>();
		for (MountPoint mountPoint : mountPoints) {
            if (mountPoint.isWritable()) {
                commands.add("sync");
                commands.add("mount -o remount,ro " + mountPoint.getDevName() + " " +
                        mountPoint.getMountPoint().getAbsolutePath());
            }
		}

        SystemUtils.runAsRoot(commands);
	}
}
