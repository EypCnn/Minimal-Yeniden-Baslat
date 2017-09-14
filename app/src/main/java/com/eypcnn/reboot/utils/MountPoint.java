package com.eypcnn.reboot.utils;

import java.io.File;

/**
 * EypCnn
 */
public class MountPoint {
	private String devName = null;
	private String fsType = null;
	private File mountPoint = null;
	private String[] options = null;
	private boolean writable = true;
	private boolean mounted = true;
	private int mountPointDepth = 0;
	
	public MountPoint(String mountLine) {
		String[] rows = mountLine.split(" ");
		devName = rows[0];
		if ("on".equalsIgnoreCase(rows[1]) && "type".equalsIgnoreCase(rows[3]) ) {
			mountPoint = new File(rows[2]);
			fsType = rows[4];
			String optString = rows[5].replace("(", "").replace(")", "");
			options = optString.split(",");
		} else {
			mountPoint = new File(rows[1]);
			fsType = rows[2];
			String optString = rows[3].replace("(", "").replace(")", "");
			options = optString.split(",");
		}
		writable = true;
		for (String option : options) {
			option = option.trim();
			if ("ro".equalsIgnoreCase(option)) {
				writable = false;
			}
		}
		mounted = true;
        mountPointDepth = computePathDepth(mountPoint);
	}
	
	public String getDevName() {
		return devName;
	}

	public String getFsType() {
		return fsType;
	}

	public File getMountPoint() {
		return mountPoint;
	}

	public String[] getOptions() {
		return options;
	}
	
	public String getOptions(String separator) {
		String result = null;
    	if (options.length>0) {
    		result = options[0];
    		for (int i=1; i<options.length; i++) {
    			result += separator + options[i];
    		}
    	}
    	return result;
	}

	public boolean isWritable() {
		return writable;
	}
	
	public boolean isMounted() {
		return mounted;
	}

    public int getMountPointDepth() {
        return mountPointDepth;
    }

	void setMounted(boolean mounted) {
		this.mounted = mounted;
	}

    private int computePathDepth(File file) {
        if (null==file) {
            return 0;
        } else {
            return 1 + computePathDepth(file.getParentFile());
        }
    }
}
