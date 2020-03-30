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

	static String nightlyFolerStr = System.getProperty("nightlyFolerStr");
	final static String keyContains = System.getProperty("keyContains");
	static String licenseKey = System.getProperty("licenseKey");
	static String mixedLicenseKey = System.getProperty("mixedLicenseKey");
	public static String latstBuldRtFlderNm;

	/**
	 * get latest build folder under nightly foler
	 * @return latest build folder string
	 * @author kwang
	 */
	public static String getLatstBuldRtFlderStr() {
		File[] buildRootFolers = getFilesWithContainFilter(new File(nightlyFolerStr), keyContains);
		List<String> timeStampList = new ArrayList<String>();
		Map<String, File> timeStampMap = new HashMap<String, File>();
		for (File file : buildRootFolers) {
			String timeStamp = file.getName().split("_", 2)[1];
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
		String licenseFolerStr = latstBuldRtFlderStr + File.separator + Constants.LICENSE;
		File licenseFolder = new File(licenseFolerStr);
		File latstLicense;
		if (licenseFolder.exists() && licenseFolder.isDirectory()) {
			File[] licenseFiles = getFilesWithContainFilter(licenseFolder, licenseKey);
			latstLicense = licenseFiles[0];
		} else {
			String licenseTempFolderStr = nightlyFolerStr + File.separator + Constants.LICENSE_TEMP + File.separator + keyContains.replace("V", "");
			File[] licenseFiles = getFilesWithContainFilter(new File(licenseTempFolderStr), licenseKey);
			List<File> licenseFilesList = Arrays.asList(licenseFiles);
			latstLicense = getLatestFilAfterSort(licenseFilesList);
		}
		return latstLicense;
	}

	/**
	 * get latest mixed license file 
	 * @param latstBuldRtFlderStr latest build folder string
	 * @return latest mixed license file
	 * @author xjguo
	 */
	public static File getLatstMixedLicenseFile(String latstBuldRtFlderStr) {
		String licenseFolerStr = latstBuldRtFlderStr + File.separator + Constants.LICENSE;
		File licenseFolder = new File(licenseFolerStr);
		File latstLicense;
		if (licenseFolder.exists() && licenseFolder.isDirectory()) {
			File[] licenseFiles = getFilesWithContainFilter(licenseFolder, mixedLicenseKey);
			latstLicense = licenseFiles[0];
		} else {
			String licenseTempFolderStr = nightlyFolerStr + File.separator + Constants.LICENSE_TEMP + File.separator + keyContains.replace("V", "");
			File[] licenseFiles = getFilesWithContainFilter(new File(licenseTempFolderStr), mixedLicenseKey);
			List<File> licenseFilesList = Arrays.asList(licenseFiles);
			latstLicense = getLatestFilAfterSort(licenseFilesList);
		}
		return latstLicense;
	}

	/**
	 * get latest Full_Studio_p2 file
	 * @param AllFolder latest build/all folder
	 * @param fullP2File get all the files contain the full p2 prefix
	 * @return return latest Talend_Full_Studio_p2_repositoryxxxx.zip
	 * @author yhe
	 */
	public static File getLatstFullP2BuildFile(String allFolerStr, String fullp2Prefix, String zipSuffix) {
		// TODO Auto-generated method stub
		File AllFolder = new File(allFolerStr);
		File[] fullP2File = getFilesWithContainFilter(AllFolder, fullp2Prefix);
		for (int i = 0; i < fullP2File.length; i++) {
			if (!fullP2File[i].toString().endsWith(zipSuffix)) {
				continue;
			} else {
				return fullP2File[i];
			}
		}
		return null;
	}
	/**
	 * get latest ci builder
	 * @param ciFolerStr
	 * @param cibuilderPrefix
	 * @param zipSuffix
	 * @return latest Talend-CI-Builder-Maven-Pluginxxx.zip
	 * @author yhe
	 */
	public static File getLatstCIBuildERFile(String ciFolerStr, String cibuilderPrefix, String zipSuffix) {
		// TODO Auto-generated method stub
		File CIBuilderPath = new File(ciFolerStr);
		File[] CIBuilderFile = getFilesWithContainFilter(CIBuilderPath, cibuilderPrefix);
		for (int i = 0; i < CIBuilderFile.length; i++) {
			if (!CIBuilderFile[i].toString().endsWith(zipSuffix)) {
				continue;
			} else {
				return CIBuilderFile[i];
			}
		}
		return null;
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
			if (latstBuldSubFlderStr.contains(Constants.ALL)) {
				File subFolder = getFilesWithStartEndFilter(latstBuldSubFlder, prefix1, "");
				File subAllFolder = getFilesWithStartEndFilter(subFolder, prefix2, "");
				latstBuildFile = getFilesWithStartEndFilter(subAllFolder, prefix3, suffix1);
			} else if (latstBuldSubFlderStr.contains(Constants.SWT)) {
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
		if (nameStr.endsWith(Constants.LICENSE_SUFFIX)) {
			nameStr = Constants.LICENSE_INSTUDIO;
		}
		destFileStr = destFileStr + File.separator + nameStr;
		File destFile = new File(destFileStr);
		FileUtils.copyFile(srcFile, destFile);
		return destFileStr;
	}

	/**
	 * Extracts a zip file to a directory specified by destDir
	 * @param zipFilePath
	 * @param destDir
	 */
	public static void unzip(String zipFilePath, String destDir) {
		File destFile = new File(destDir);
		if (!destFile.exists()) {
			destFile.mkdirs();
		}
		String commandStr = "cmd /c jar -xf " + zipFilePath;
		runCommand(commandStr, destFile);
	}

	public static void runCommand(String commandStr, File destFile) {
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(commandStr, null, destFile);
			process.waitFor();
		} catch (IOException e) {
			e.printStackTrace(System.err);
		} catch (InterruptedException e) {
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

	public static String getLatestStrAfterSort(List<String> list) {
		int size = list.size();
		Collections.sort(list);
		
		return size==1?list.get(0):list.get(size - 1);
	}

	public static File getLatestFilAfterSort(List<File> list) {
		int size = list.size();
		Collections.sort(list);
		return list.get(size - 1);
	}

	public static void deleteFolder(String file) {
		boolean success = deleteDir(new File(file));
		if (success) {
			System.err.println("Successfully deleted file: " + file);
		} else {
			System.err.println("Failed to delete file: " + file);
		}
	}

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

}
