package generate.p2;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;

import constant.Constants;
import util.CommUtil;

public class PrepareBuilds {
	static String localDestFileStr = System.getProperty("localDestFileStr");
	static String licensePrefix = System.getProperty("licensePrefix");
	static String smbDestFolderStr = System.getProperty("smbDestFolderStr");

	static String ftpServer = System.getProperty("ftpServer");
	static String ftpUserid = System.getProperty("ftpUserid");
	static String ftpPassword = System.getProperty("ftpPassword");

	static String sambaDir = System.getProperty("sambaDir");
	static String sambaServer = System.getProperty("sambaServer");
	static String sambaUser = System.getProperty("sambaUser");
	static String sambaPasswd = System.getProperty("sambaPasswd");

	static String destLicenseFileStr = "";
	static String destStudioFileStr = "";

	public static void main(String[] args) throws IOException {
		new PrepareBuilds().generatP2();
	}

	public void generatP2() throws IOException {
		String tempFilePath = new File("").getAbsolutePath() + "/tempfile.txt";
		File tempfile = new File(tempFilePath);
		if (tempfile.exists()) {
			tempfile.delete();
		}
		tempfile.createNewFile();
		CommUtil.writeStrToFile(tempFilePath, "sambaUser=" + sambaUser, true);
		CommUtil.writeStrToFile(tempFilePath, "sambaPasswd=" + sambaPasswd, true);
		CommUtil.writeStrToFile(tempFilePath, "sambaServer=" + sambaServer, true);
		CommUtil.writeStrToFile(tempFilePath, "sambaDir=" + sambaDir, true);
		CommUtil.writeStrToFile(tempFilePath, "isClearUpLocalFolder=" + System.getProperty("isClearUpLocalFolder"), true);

		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(ftpServer, 21);		
		if (!ftpClient.login(ftpUserid, ftpPassword)) {
			ftpClient.disconnect();
			throw new IOException("Can't login to FTP server");
		} else {
			System.out.println("Connect FTP server successfully!");
		}
		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
		ftpClient.enterLocalPassiveMode();
		
		List<String> listFiles = Arrays.asList(ftpClient.listNames());
		// get latest build root folder
		String lastBuildRootFolder = CommUtil.getLastStudioRootPath(listFiles);
		System.err.println("latest build: " + lastBuildRootFolder);
		localDestFileStr = localDestFileStr + File.separator + lastBuildRootFolder;
		File localRootFile = new File(localDestFileStr);
		localRootFile.mkdirs();
		CommUtil.writeStrToFile(tempFilePath, "localDestFileStr=" + localDestFileStr, true);

		if (Boolean.getBoolean("isNeedLicense")) {
			String licenseKey = System.getProperty("licenseKey");
			destLicenseFileStr = CommUtil.getAndDownloadLicense(ftpClient, lastBuildRootFolder, localDestFileStr, tempFilePath, "license",licenseKey);
		}

		if (Boolean.getBoolean("isNeedMixedLicense")) {
			String mixedLicenseKey = System.getProperty("mixedLicenseKey");
			CommUtil.getAndDownloadLicense(ftpClient, lastBuildRootFolder, localDestFileStr, tempFilePath, "mixedlicense", mixedLicenseKey);
		}
		if (Boolean.getBoolean("isNeedReleaseLicense")) {
			String licenseKey = System.getProperty("licenseKey");
			int i;
			for(i=0;i<Constants.RVersion.length;i++) 
			{
			System.out.println(Constants.RVersion[i]);
			CommUtil.getAndDownloadReleaseLicense(ftpClient, localDestFileStr,Constants.RVersion[i], tempFilePath, "license",licenseKey);
			}
		}
		if (Boolean.getBoolean("isNeedfullP2")) {
			CommUtil.getAndDownloadOthers(ftpClient, lastBuildRootFolder, localDestFileStr, tempFilePath, "fullP2", Constants.FullP2_PREFIX);
		}

		if (Boolean.getBoolean("isNeedCIBuilder")) {
			CommUtil.getAndDownloadOthers(ftpClient, lastBuildRootFolder, localDestFileStr, tempFilePath, "cibuilder", Constants.CIBuilder_PREFIX);
		}

		if (Boolean.getBoolean("isNeedCISigner")) {
			CommUtil.getAndDownloadOthers(ftpClient, lastBuildRootFolder, localDestFileStr, tempFilePath, "cisigner", Constants.CISigner_PREFIX);
		}

		if (Boolean.getBoolean("isNeedTAC")) {
			CommUtil.getAndDownloadOthers(ftpClient, lastBuildRootFolder, localDestFileStr, tempFilePath, "tac", Constants.TAC_PREFIX);
		}
		
		if (Boolean.getBoolean("isNeedStudio")) {
			destStudioFileStr = CommUtil.getAndDownloadOthers(ftpClient, lastBuildRootFolder, localDestFileStr, tempFilePath, "studio", Constants.STUDIO_PREFIX);
		}
		
		try {
			String studioFolderStr = destStudioFileStr.replace(".zip", "");
			String studioFolderNameStr = new File(studioFolderStr).getName();
			String studioWithP2NameStr = Constants.P2_PREFIX + studioFolderNameStr + ".zip";
			boolean isExisted = CommUtil.isFileExistedOnSambaServer(studioWithP2NameStr);
			if(isExisted) {
				System.err.println("**** p2 existed will not generate again ****");
				throw new Exception(studioWithP2NameStr + " already existed on samba server");
			}

			// get SWTBotAll_p2
			String lastSWTFile = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, Constants.SWT_PREFIX);
			// download SWTBotAll_p2 from ftp to local and unzip
			String destSWTFileStr = CommUtil.ftpDownloadFiles(ftpClient, lastSWTFile, localDestFileStr);
			ftpClient.disconnect();
			CommUtil.unzip(destSWTFileStr, localDestFileStr + "/" + new File(destSWTFileStr).getName().replace(".zip", ""));
			System.err.println("unzip SWTBotAll_p2 done: " + destStudioFileStr);
			String swtbotP2FolderString = destSWTFileStr.replace(".zip", "");
			CommUtil.unzip(destLicenseFileStr, localDestFileStr);
			System.err.println("unzip license done: " + destLicenseFileStr);
			CommUtil.unzip(destStudioFileStr, localDestFileStr);
			System.err.println("unzip studio done: " + destStudioFileStr);
			// copy license to Studio
			String licenseFolderStr = destLicenseFileStr.replace(".zip", "");
			File licenseFile = CommUtil.getFilesWithStartEndFilter(new File(licenseFolderStr), licensePrefix, "");
			CommUtil.copyBuild(licenseFile, studioFolderStr);
			System.err.println("copy license to studio done");
			// generate SWT p2
			String exe = Constants.STUDIO_EXE;
			if (System.getProperty("os.name").toLowerCase().contains("linux")) {
				exe = Constants.STUDIO_EXE_Linux;
			}
			String commandStr = studioFolderStr + "/" + exe + " -nosplash -consoleLog -application org.eclipse.equinox.p2.director -repository file:///" + swtbotP2FolderString + " -installIU org.talend.swtbot.update.site.feature.feature.group";
			if (System.getProperty("os.name").toLowerCase().contains("linux")) {
				CommUtil.runCommand("chmod -R 777 "+localDestFileStr, null);
				CommUtil.runCommand(commandStr, null);
			}else {			
				CommUtil.runCommand("cmd /c " + commandStr, null);
			}
			System.err.println("generate p2 done");
			
			// zip SWT p2
			File localDestFile = new File(localDestFileStr);
			
			String zipCommStr = "jar -cMf " + studioWithP2NameStr + " " + studioFolderNameStr.replace(".zip", "");
			CommUtil.runCommand(zipCommStr, localDestFile);
			System.err.println("zip p2 done");
			String p2SrcFileStr = CommUtil.getFilesWithStartEndFilter(localDestFile, Constants.P2_PREFIX, "").getAbsolutePath();
			// add SWT p2 upload list
			CommUtil.writeStrToFile(tempFilePath, "swtp2=" + p2SrcFileStr, true);
			System.out.println("generate and upload swt p2 successfully!");
		} catch (Exception e) {
			CommUtil.writeStrToFile(tempFilePath, "swtp2=", true);
			System.out.println("generate and upload swt p2 failed!!!");
		}
	}
}
