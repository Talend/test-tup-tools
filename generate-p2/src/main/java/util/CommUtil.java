package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;

import constant.Constants;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

public class CommUtil {

	static String nightlyFolerStr = System.getProperty("nightlyFolerStr");
	final static String keyContains = System.getProperty("keyContains");
	public static String latstBuldRtFlderNm;
	static String ftpServer = System.getProperty("ftpServer");
	static String ftpUserid = System.getProperty("ftpUserid");
	static String ftpPassword = System.getProperty("ftpPassword");
	
	static String sambaUser = System.getProperty("sambaUser");
	static String sambaPasswd = System.getProperty("sambaPasswd");
	static String sambaServer = System.getProperty("sambaServer");
	static String sambaDir=System.getProperty("sambaDir");
	
	/**
	 * get latest build folder under nightly foler
	 * 
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
	 * get latest Full_Studio_p2 file
	 * 
	 * @param AllFolder
	 *            latest build/all folder
	 * @param fullP2File
	 *            get all the files contain the full p2 prefix
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
	 * 
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
	 * 
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
	 * 
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
	 * 
	 * @param zipFilePath
	 * @param destDir
	 */
	public static void unzip(String zipFilePath, String destDir) {
		File destFile = new File(destDir);
		if (!destFile.exists()) {
			destFile.mkdirs();
		}
		String commandStr = "cmd /c jar -xf " + zipFilePath;
		if (System.getProperty("os.name").toLowerCase().contains("linux")) {
			unzipFile1(zipFilePath,destDir);
		}
		runCommand(commandStr, destFile);
	}

	public static boolean unzipFile1(String zipFilePath, String targetFolderPath) {
		InputStream input = null;
		FileOutputStream output = null;
		boolean flag = false;

		String type = ".zip";
		if (zipFilePath.endsWith(".rar")) {
			type = ".rar";
		} else if (zipFilePath.endsWith(".war")) {
			type = ".war";
		} else if (zipFilePath.endsWith(".tar.gz")) {
			type = ".tar.gz";
		} else if (zipFilePath.endsWith(".jar")) {
			type = ".jar";
		}
		try {
			File file = new File(zipFilePath);
			ZipFile zipFile = new ZipFile(file);
			String unZipFileName = file.getName().substring(0, file.getName().lastIndexOf(type));
			File unzipFile = new File(targetFolderPath );

			if (!unzipFile.exists()) {
				unzipFile.mkdir();
			}
			// get zipFile entries
			Enumeration zipEnum = zipFile.entries();

			// get current enum
			while (zipEnum.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) zipEnum.nextElement();
				String entryName = new String(entry.getName());

				String[] everyEntry;
				StringBuffer newEntry = new StringBuffer();
				String fileName = "";
				File outFile = null;
				// create every file
				if (entryName.endsWith("/")) {
					everyEntry = entryName.split("/");
					if ("Linux".equals(System.getProperty("os.name"))) {
						for (int i = 0; i < everyEntry.length; i++)
							newEntry = newEntry.append(everyEntry[i]).append("/");
					} else {
						for (int i = 0; i < everyEntry.length; i++)
							newEntry = newEntry.append(everyEntry[i]).append("\\");
					}

					String newEntryName = unzipFile.getAbsolutePath() + "/" + newEntry.toString();
					if (!new File(newEntryName).exists()) {
						new File(newEntryName).mkdirs();
					}
				} else {
					everyEntry = entryName.split("/");
					if ("Linux".equals(System.getProperty("os.name"))) {
						for (int i = 0; i < everyEntry.length - 1; i++) {
							newEntry = newEntry.append(everyEntry[i]).append("/");
						}
					} else {
						for (int i = 0; i < everyEntry.length - 1; i++) {
							newEntry = newEntry.append(everyEntry[i]).append("\\");
						}
					}
					fileName = everyEntry[everyEntry.length - 1];
					String newEntryName = unzipFile.getAbsolutePath() + "/" + newEntry.toString();
					if (!new File(newEntryName).exists()) {
						new File(newEntryName).mkdirs();
					}
					outFile = new File(newEntryName + fileName);

					// create file under the root folder
					input = zipFile.getInputStream(entry);
					if (!outFile.exists()) {
						outFile.createNewFile();
					}
					output = new FileOutputStream(outFile);
					byte[] buffer = new byte[1024 * 8];
					int readLen = 0;
					while ((readLen = input.read(buffer, 0, 1024 * 8)) != -1) {
						output.write(buffer, 0, readLen);
					}
					input.close();
					output.flush();
					output.close();

				}
			}
			flag = true;
		} catch (IOException e) {
			throw new RuntimeException(e.getCause());
		}
		return flag;
	}

	public static void runCommand(String commandStr, File destFile) {
		if (System.getProperty("os.name").toLowerCase().contains("linux")) {
			commandStr = commandStr.replace("cmd /c ", "sh ");
		}
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
		return list.get(size - 1);
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

	/**
	 * @param listFiles
	 *            build folder name list
	 * @return get latest Build Root Folder
	 * @author xjguo
	 */
	public static String getLastStudioRootPath(List<String> listFiles) {
		List<String> filelist = acceptContainFilter(listFiles, keyContains);
		Collections.sort(filelist);
		return filelist.get(filelist.size() - 1);
	}

	public static List<String> acceptContainFilter(List<String> listFiles, String key) {
		List<String> result = new ArrayList<String>();
		for (String temp : listFiles) {
			if (temp != null && temp.contains(key))
				result.add(temp.trim());
		}
		return result;
	}

	/**
	 * @param listFiles
	 * @param key
	 * @return
	 * @author xjguo
	 */
	public static List<String> acceptContainFilterForPath(List<String> listFiles, String key) {
		List<String> result = new ArrayList<String>();
		for (String temp : listFiles) {
			if (getFileName(temp).contains(key))
				result.add(temp.trim());
		}
		return result;
	}

	/**
	 * @param listFiles
	 * @param key
	 * @return
	 * @author xjguo
	 */
	public static List<String> acceptStartWithFilterForPath(List<String> listFiles, String key) {
		List<String> result = new ArrayList<String>();
		for (String temp : listFiles) {
			if (getFileName(temp).startsWith(key))
				result.add(temp.trim());
		}
		return result;
	}

	/**
	 * @param listFiles
	 * @param key
	 * @return
	 * @author xjguo
	 */
	public static List<String> acceptEndsWithFilterForPath(List<String> listFiles, String key) {
		List<String> result = new ArrayList<String>();
		for (String temp : listFiles) {
			if (getFileName(temp).endsWith(key))
				result.add(temp.trim());
		}
		return result;
	}

	/**
	 * @param ftpClient
	 * @param parentPath
	 *            latest Build Root Folder
	 * @return get latest license path
	 * @throws IOException
	 * @author xjguo
	 */
	public static String getLastLicenseFile(FTPClient ftpClient, String parentPath, String keyString) throws IOException {
		if (!ftpClient.isConnected()) {
			ftpClient.connect(ftpServer, 21);
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			if (!ftpClient.login(ftpUserid, ftpPassword)) {
				ftpClient.disconnect();
				throw new IOException("Can't login to FTP server");
			}
		}
		List<String> files = new ArrayList<String>();
		String licensePath = "";
		List<String> licenseList = new ArrayList<String>();
		files = Arrays.asList(ftpClient.listNames(parentPath));
		if (files.toString().contains(Constants.LICENSE)) {
			licenseList = Arrays.asList(ftpClient.listNames(parentPath + "/" + Constants.LICENSE));
			licenseList = acceptContainFilter(licenseList, keyString);
			licensePath = licenseList.get(licenseList.size() - 1);
		} else {
			String licenseTempFolderStr = Constants.LICENSE_TEMP + "/" + keyContains.replace("V", "");
			licenseList = Arrays.asList(ftpClient.listNames(licenseTempFolderStr));
			licenseList = acceptContainFilter(licenseList, keyString);
			Collections.sort(licenseList);
			licensePath = licenseList.get(licenseList.size() - 1);
		}
		return licensePath;
	}

	/**
	 * @param ftpClient
	 * @param parentPath
	 * @return
	 * @throws IOException
	 * @author xjguo
	 */
	public static String getLastBuildFilterFile(FTPClient ftpClient, String parentPath, String buildType) throws IOException {
		if (!ftpClient.isConnected()) {
			ftpClient.connect(ftpServer, 21);
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			if (!ftpClient.login(ftpUserid, ftpPassword)) {
				ftpClient.disconnect();
				throw new IOException("Can't login to FTP server");
			}
		}
		String buildPath = "";

		List<String> buildList = new ArrayList<String>();
		if (Constants.STUDIO_PREFIX.equals(buildType)) {
			buildList = Arrays.asList(ftpClient.listNames(parentPath + "/" + Constants.ALL));// list : \\192.168.30.10\nightly\V7.3.1SNAPSHOT_20190723_1939\all
			buildList = acceptStartWithFilterForPath(buildList, Constants.V_PREFIX);
			buildList = Arrays.asList(ftpClient.listNames(buildList.get(0)));// list : \\192.168.30.10\nightly\V7.3.1SNAPSHOT_20190723_1939\all\V7.3.1SNAPSHOT
			buildList = acceptContainFilter(buildList, Constants.SUBALL);
			buildList = Arrays.asList(ftpClient.listNames(buildList.get(0)));// list : \\192.168.30.10\nightly\V7.3.1SNAPSHOT_20190723_1939\all\V7.3.1SNAPSHOT\all_731
			buildList = acceptContainFilterForPath(buildList, Constants.V_PREFIX);
			buildList = acceptContainFilterForPath(buildList, Constants.STUDIO_PREFIX);
			buildList = acceptEndsWithFilterForPath(buildList, Constants.ZIP_SUFFIX);
			buildPath = buildList.get(buildList.size() - 1);
		} else if (Constants.TAC_PREFIX.equals(buildType)) {
			buildList = Arrays.asList(ftpClient.listNames(parentPath + "/" + Constants.ALL));// list : \\192.168.30.10\nightly\V7.3.1SNAPSHOT_20190723_1939\all
			buildList = acceptStartWithFilterForPath(buildList, Constants.V_PREFIX);
			buildList = Arrays.asList(ftpClient.listNames(buildList.get(0)));// list : \\192.168.30.10\nightly\V7.3.1SNAPSHOT_20190723_1939\all\V7.3.1SNAPSHOT
			buildList = acceptContainFilter(buildList, Constants.SUBALL);
			buildList = Arrays.asList(ftpClient.listNames(buildList.get(0)));// list : \\192.168.30.10\nightly\V7.3.1SNAPSHOT_20190723_1939\all\V7.3.1SNAPSHOT\all_731
			buildList = acceptContainFilterForPath(buildList, Constants.V_PREFIX);
			buildList = acceptContainFilterForPath(buildList, Constants.TAC_PREFIX);
			buildList = acceptEndsWithFilterForPath(buildList, Constants.ZIP_SUFFIX);
			buildPath = buildList.get(buildList.size() - 1);
		} else if (Constants.FullP2_PREFIX.equals(buildType)) {
			buildList = Arrays.asList(ftpClient.listNames(parentPath + "/" + Constants.ALL));
			buildList = acceptContainFilterForPath(buildList, Constants.V_PREFIX);
			buildList = acceptContainFilterForPath(buildList, Constants.FullP2_PREFIX);
			buildList = acceptEndsWithFilterForPath(buildList, Constants.ZIP_SUFFIX);
			buildPath = buildList.get(buildList.size() - 1);
		} else if (Constants.CIBuilder_PREFIX.equals(buildType)) {
			buildList = Arrays.asList(ftpClient.listNames(parentPath + "/" + Constants.CIFolder));
			buildList = acceptContainFilterForPath(buildList, Constants.V_PREFIX);
			buildList = acceptContainFilterForPath(buildList, Constants.CIBuilder_PREFIX);
			buildList = acceptEndsWithFilterForPath(buildList, Constants.ZIP_SUFFIX);
			buildPath = buildList.get(buildList.size() - 1);
		} else if (Constants.CISigner_PREFIX.equals(buildType)) {
			buildList = Arrays.asList(ftpClient.listNames(parentPath + "/" + Constants.CIFolder));
			buildList = acceptContainFilterForPath(buildList, Constants.V_PREFIX);
			buildList = acceptContainFilterForPath(buildList, Constants.CISigner_PREFIX);
			buildList = acceptEndsWithFilterForPath(buildList, Constants.ZIP_SUFFIX);
			buildPath = buildList.get(buildList.size() - 1);
		} else if (Constants.SWT_PREFIX.equals(buildType)) {
			buildList = Arrays.asList(ftpClient.listNames(parentPath + "/" + Constants.SWT));
			buildList = acceptContainFilterForPath(buildList, Constants.V_PREFIX);
			buildList = acceptContainFilterForPath(buildList, Constants.SWT_PREFIX);
			buildList = acceptEndsWithFilterForPath(buildList, Constants.ZIP_SUFFIX);
			buildPath = buildList.get(buildList.size() - 1);
		}
		return buildPath;
	}

	/**
	 * @param path
	 * @return
	 * @author xjguo
	 */
	public static String getFileName(String path) {
		path = path.replaceAll("\\\\", "/");
		String[] pathArray = path.split("/");
		String filename = pathArray[pathArray.length - 1];
		return filename;
	}
	
	public static String getAndDownloadLicense(FTPClient ftpClient, String lastBuildRootFolder, String localDestFileStr, String tempFilePath, String fileStr, String keyString) {
		String destFileStr = null;
		try {
			// get latest file
			String latestFile = CommUtil.getLastLicenseFile(ftpClient, lastBuildRootFolder,keyString);
				
			// download license from ftp to local and unzip
			destFileStr = CommUtil.ftpDownloadFiles(ftpClient, latestFile, localDestFileStr);
			
			// add license to upload list
			CommUtil.writeStrToFile(tempFilePath, fileStr + "=" + destFileStr, true);
			System.err.println("Download " + fileStr +" successfully!");
		} catch (Exception e) {
			System.err.println("Download " + fileStr + " failed!!!");
		}
		return destFileStr;
	}
	
	public static String getAndDownloadOthers(FTPClient ftpClient, String lastBuildRootFolder, String localDestFileStr, String tempFilePath, String fileStr, String keyString) {
		String destFileStr = "";
		try {
			// get file e.g. studio
			String latestFile = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, keyString);
			
			// download studio from ftp to local and unzip
			destFileStr = CommUtil.ftpDownloadFiles(ftpClient, latestFile, localDestFileStr);
			// add studio to upload list
			CommUtil.writeStrToFile(tempFilePath, fileStr + "=" + destFileStr, true);
			System.err.println("Download " + fileStr + " successfully!");
		} catch (Exception e) {
			System.err.println("Download " + fileStr + "  failed!!!");
		}
		return destFileStr;
	}

	/**
	 * @param ftpClient
	 * @param sourcePath
	 * @param destPath
	 * @return
	 * @author xjguo
	 */
	public static String ftpDownloadFiles(FTPClient ftpClient, String sourcePath, String destPath) {
		String filename = "";
		FileOutputStream fos = null;
		filename = sourcePath.replaceAll("\\\\", "/");
		String[] pathArray = filename.split("/");
		filename = pathArray[pathArray.length - 1];
		destPath = destPath + "/" + filename;
		try {
			fos = new FileOutputStream(destPath);
			ftpClient.retrieveFile(sourcePath, fos);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return destPath;
	}

	/**
	 * @param localFilePath
	 * @author xjguo
	 */
	public static void uploadFileToSamba(String localFilePath) {
		uploadFileToSamba(localFilePath, System.getProperty("sambaServer"), System.getProperty("sambaUser"), System.getProperty("sambaPasswd"), System.getProperty("sambaDir"));
	}

	public static void writeStrToFile(String filePath, String str, Boolean overwrite) {
		str = str.replaceAll("\\\\", "/");
		File tempfile = new File(filePath);
		try {
			FileWriter fw = null;
			fw = new FileWriter(tempfile, overwrite);
			BufferedWriter out = new BufferedWriter(fw);
			out.write(str + "\r\n", 0, (str + "\r\n").length());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isFileExistedOnSambaServer(String fileStr) throws MalformedURLException, SmbException {
		boolean isExisted = false;
		String sambaServer = System.getProperty("sambaServer", "192.168.33.241");
		String sambaUser = System.getProperty("sambaUser", "automation");
		String sambaPasswd = System.getProperty("sambaPasswd", "automation.com");
		String smbURL = "smb://" + sambaServer + Constants.samba_dir_usedByJava + File.separator + fileStr;
		NtlmPasswordAuthentication auth =  new NtlmPasswordAuthentication(sambaUser + ":" + sambaPasswd);
		SmbFile samFile = new SmbFile(smbURL,auth);
		isExisted = samFile.exists() && samFile.isFile();
		return isExisted;
	}

	/**
	 * @param localSourceFilePath
	 * @param serverIP
	 * @param serverUser
	 * @param serverPwd
	 * @param targetFolderPath
	 * @author xjguo
	 */
	public static void uploadFileToSamba(String localSourceFilePath, String serverIP, String serverUser, String serverPwd, String targetFolderPath) {
		FileInputStream fileInput = null;
		SmbFileOutputStream samOut = null;
		String targetFileName = getFileName(localSourceFilePath);
		try {
			SmbFile samFolder = new SmbFile("smb://" + serverUser + ":" + serverPwd + "@" + serverIP + targetFolderPath);
			try {
				if (!samFolder.exists()) {
					samFolder.mkdirs();
				}
			} catch (Exception e) {
				System.err.println("Creating Smaba Folder failed - " + samFolder);
			}
			fileInput = new FileInputStream(new File(localSourceFilePath));
			samOut = new SmbFileOutputStream(new SmbFile("smb://" + serverUser + ":" + serverPwd + "@" + serverIP + targetFolderPath + "/" + targetFileName));
			byte[] buffer = new byte[1024];
			int c = 0;
			while ((c = fileInput.read(buffer)) != -1) {
				samOut.write(buffer);
				samOut.flush();
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			try {
				if (fileInput != null) {
					fileInput.close();
				}
				if (samOut != null) {
					samOut.close();
				}
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}
	}

}
