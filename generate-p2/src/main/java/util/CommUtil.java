package util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import constant.Constants;

public class CommUtil {
	
	static String rootFolerStr = "\\\\192.168.30.10\\nightly";
	//V7.2.1
	//V7.2.1SNAPSHOT_20190329_1937
	final static String keyContains = "V7.2.1";
	static String licenseKey = "30days_5users";
	public static String latstBuldRtFlderNm; 
	
	/**
	 * get latest build folder under nightly foler
	 * @return latest build folder string
	 * @author kwang
	 */
	public static String getLatstBuldRtFlderStr() {
		File[] buildRootFolers = getFilesWithContainFilter(new File(rootFolerStr), keyContains);
		List<String> timeStampList = new ArrayList<String>();
		Map<String, File> timeStampMap = new HashMap<String, File>();
		for(File file:buildRootFolers) {
			String timeStamp = file.getName().split("_",2)[1];
			timeStampList.add(timeStamp);
			timeStampMap.put(timeStamp, file);
		}
		File latstBuldRtFlder = timeStampMap.get(getLatestStrAfterSort(timeStampList));
		String latstBuldRtFlderStr = latstBuldRtFlder.getAbsolutePath();
		latstBuldRtFlderNm = latstBuldRtFlder.getName();
		return latstBuldRtFlderStr;
	}
	
	/**
	 * get latest license file
	 * @param latstBuldRtFlderStr latest build folder string
	 * @return latest license file
	 * @author kwang
	 */
	public static File getLatstLicenseFile(String latstBuldRtFlderStr) {
		String licenseFolerStr =  latstBuldRtFlderStr + File.separator + Constants.LICENSE;
		File licenseFolder = new File(licenseFolerStr);
		File latstLicense;
		if(licenseFolder.exists() && licenseFolder.isDirectory()) {
			File[] licenseFiles = getFilesWithContainFilter(licenseFolder, licenseKey);
			latstLicense = licenseFiles[0];
		} else {
			String licenseTempFolderStr = rootFolerStr + File.separator + Constants.LICENSE_TEMP + File.separator + keyContains.replace("V", "");
			File[] licenseFiles = getFilesWithContainFilter(new File(licenseTempFolderStr), licenseKey);
			List<File> licenseFilesList = Arrays.asList(licenseFiles);
			latstLicense = getLatestFilAfterSort(licenseFilesList);
		}
		return latstLicense;
	}
	
	/**
	 * get latest studio/swtbot_p2 file
	 * @param latstBuldSubFlderStr
	 * @param prefix1
	 * @param prefix2
	 * @param prefix3
	 * @param suffix1
	 * @return latest studio/swtbot_p2 file
	 * @author kwang
	 */
	public static File getLatstBuildFile(String latstBuldSubFlderStr, String prefix1, String prefix2, String prefix3, String suffix1) {
		File latstBuildFile = null;
		File latstBuldSubFlder = new File(latstBuldSubFlderStr);
		if (latstBuldSubFlder.exists() && latstBuldSubFlder.isDirectory()) {
			if(latstBuldSubFlderStr.contains(Constants.ALL)) {
				File subFolder = getFilesWithStartEndFilter(latstBuldSubFlder, prefix1,"");
				File subAllFolder = getFilesWithStartEndFilter(subFolder, prefix2,"");
				latstBuildFile = getFilesWithStartEndFilter(subAllFolder, prefix3, suffix1);
			} else if(latstBuldSubFlderStr.contains(Constants.SWT)) {
				latstBuildFile = getFilesWithStartEndFilter(latstBuldSubFlder, prefix1, suffix1);
			}
			
		} else {
			System.err.println("!!!!!!!!!!! there is no " + latstBuldSubFlder + " folder");
		}
		return latstBuildFile;
	}
	
	/**
	 * copy build to local
	 * @param srcFile
	 * @param destFileStr
	 * @throws IOException
	 * @author kwang
	 */
	public static String copyBuild(File srcFile, String destFileStr) throws IOException {
		String nameStr = srcFile.getName();
		destFileStr = destFileStr + File.separator + nameStr;
		File destFile = new File(destFileStr);
		FileUtils.copyFile(srcFile, destFile);
		return destFileStr;
	}
	
	/**
     * Extracts a zip file to a directory specified by destDirectory
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public static void unzip(String zipFilePath, String destDir) {
	    	File destFile = new File(destDir);
			if(!destFile.exists()) {
				destFile.mkdirs();
			}
			Runtime runtime = Runtime.getRuntime();
			Process process = null;
			try {
				process = runtime.exec("cmd /c jar -xf " + zipFilePath, null, destFile);
				process.waitFor();
			} catch (IOException e) {
				System.err.println("!!!!!!! unzip file failed: " + zipFilePath);
				e.printStackTrace(System.err);
			} catch (InterruptedException e) {
				System.err.println("!!!!!!! unzip file failed: " + zipFilePath);
				e.printStackTrace(System.err);
			} finally {
				process.destroyForcibly();
			}
    }
    
	public static File[] getFilesWithContainFilter(File file, final String filter) {
		File[] files = file.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().contains(filter);
			}
		});
		return files;
	}
	
	public static File getFilesWithStartEndFilter(File file, final String prefix, final String suffix) {
		File[] files = file.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				String pathNameString = pathname.getName();
				return pathNameString.startsWith(prefix) && pathNameString.endsWith(suffix);
			}
		});
		if (files.length > 0) {
			return files[0];
		} else {
			System.err.println("!!!!!!!!! No build start with " + prefix + " and end with " + suffix);
			return null;
		}
	}
	
	public static String getLatestStrAfterSort (List<String> list) {
		int size = list.size();
		Collections.sort(list);
		return list.get(size-1);
	}
	
	public static File getLatestFilAfterSort (List<File> list) {
		int size = list.size();
		Collections.sort(list);
		return list.get(size-1);
	}
}
