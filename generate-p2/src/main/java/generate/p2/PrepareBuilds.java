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
		CommUtil.writeStrToFile(tempFilePath, "sambaUser=" + System.getProperty("sambaUser"), true);
		CommUtil.writeStrToFile(tempFilePath, "sambaPasswd=" + System.getProperty("sambaPasswd"), true);
		CommUtil.writeStrToFile(tempFilePath, "sambaServer=" + System.getProperty("sambaServer"), true);
		CommUtil.writeStrToFile(tempFilePath, "sambaDir=" + System.getProperty("sambaDir"), true);

		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(ftpServer, 21);
		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
		if (!ftpClient.login(ftpUserid, ftpPassword)) {
			ftpClient.disconnect();
			throw new IOException("Can't login to FTP server");
		} else {
			System.out.println("Connect FTP server successfully!");
		}
		List<String> listFiles = Arrays.asList(ftpClient.listNames());
		// get latest build root folder
		String lastBuildRootFolder = CommUtil.getLastStudioRootPath(listFiles);
		System.err.println("latest build: " + lastBuildRootFolder);
		localDestFileStr = localDestFileStr + File.separator + lastBuildRootFolder;
		File localRootFile = new File(localDestFileStr);
		if (localRootFile.exists()) {
			CommUtil.deleteFolder(localDestFileStr);
		}
		localRootFile.mkdirs();

		if (Boolean.getBoolean("isNeedLicense")) {
			try {
				// get license
				String lastLicense = CommUtil.getLastLicenseFile(ftpClient, lastBuildRootFolder);
				// download license from ftp to local and unzip
				destLicenseFileStr = CommUtil.ftpDownloadFiles(ftpClient, lastLicense, localDestFileStr);
				// add license to upload list
				CommUtil.writeStrToFile(tempFilePath, "license=" + destLicenseFileStr, true);
			} catch (Exception e) {
			}
		}

		if (Boolean.getBoolean("isNeedMixedLicense")) {
			try {
				// get mixed license
				String latstMixedLicense = CommUtil.getLatstMixedLicenseFile(ftpClient, lastBuildRootFolder);
				// download Mixed license from ftp to local
				String destMixLicenseFileStr = CommUtil.ftpDownloadFiles(ftpClient, latstMixedLicense, localDestFileStr);
				// add mixed license to upload list
				CommUtil.writeStrToFile(tempFilePath, "mixedlicense=" + destMixLicenseFileStr, true);
			} catch (Exception e) {
			}
		}

		if (Boolean.getBoolean("isNeedStudio")) {
			try {
				// get studio
				String lastStudio = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, Constants.STUDIO_PREFIX);
				// download studio from ftp to local and unzip
				destStudioFileStr = CommUtil.ftpDownloadFiles(ftpClient, lastStudio, localDestFileStr);
				// add studio to upload list
				CommUtil.writeStrToFile(tempFilePath, "studio=" + destStudioFileStr, true);
			} catch (Exception e) {
			}
		}

		if (Boolean.getBoolean("isNeedfullP2")) {
			try {
				// get full p2
				String lastFullP2 = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, Constants.FullP2_PREFIX);
				// download full p2 from ftp to local
				String destFullP2FileStr = CommUtil.ftpDownloadFiles(ftpClient, lastFullP2, localDestFileStr);
				// add full p2 to upload list
				CommUtil.writeStrToFile(tempFilePath, "fullP2=" + destFullP2FileStr, true);
			} catch (Exception e) {
			}
		}

		if (Boolean.getBoolean("isNeedCIBuilder")) {
			try {
				// get ci builder
				String lastCIBuilder = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, Constants.CIBuilder_PREFIX);
				// download ci builder from ftp to local
				String destCIBuilderFileStr = CommUtil.ftpDownloadFiles(ftpClient, lastCIBuilder, localDestFileStr);
				// add ci builder to upload list
				CommUtil.writeStrToFile(tempFilePath, "cibuilder=" + destCIBuilderFileStr, true);
			} catch (Exception e) {
			}
		}

		if (Boolean.getBoolean("isNeedCISigner")) {
			try {
				// get CI signer file
				String lastSignerFile = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, Constants.CISigner_PREFIX);
				// download CI signer file from ftp to local
				String destSignerFileStr = CommUtil.ftpDownloadFiles(ftpClient, lastSignerFile, localDestFileStr);
				// add CI signer to upload list
				CommUtil.writeStrToFile(tempFilePath, "cisigner=" + destSignerFileStr, true);
			} catch (Exception e) {
			}
		}

		if (Boolean.getBoolean("isNeedTAC")) {
			try {
				// get tac
				String lastTacFile = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, Constants.TAC_PREFIX);
				// // download tac from ftp to local
				String destTacFileStr = CommUtil.ftpDownloadFiles(ftpClient, lastTacFile, localDestFileStr);
				// add tac to upload list
				CommUtil.writeStrToFile(tempFilePath, "tac=" + destTacFileStr, true);
			} catch (Exception e) {
			}
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
		String studioFolderStr = destStudioFileStr.replace(".zip", "");
		File licenseFile = CommUtil.getFilesWithStartEndFilter(new File(licenseFolderStr), licensePrefix, "");
		CommUtil.copyBuild(licenseFile, studioFolderStr);
		System.err.println("copy license to studio done");
		// generate SWT p2
		String exe = Constants.STUDIO_EXE;
		if (System.getProperty("os.name").toLowerCase().contains("linux")) {
			exe = Constants.STUDIO_EXE_Linux;
		}
		String commandStr = studioFolderStr + "/" + exe + " -nosplash -consoleLog -application org.eclipse.equinox.p2.director -repository file:///" + swtbotP2FolderString + " -installIU org.talend.swtbot.update.site.feature.feature.group";
		CommUtil.runCommand("cmd /c " + commandStr, null);
		System.err.println("generate p2 done");
		// zip SWT p2
		File localDestFile = new File(localDestFileStr);
		String studioFolderNameStr = new File(studioFolderStr).getName();
		String zipCommStr = "jar -cMf " + Constants.P2_PREFIX + studioFolderNameStr + ".zip " + studioFolderNameStr.replace(".zip", "");
		CommUtil.runCommand(zipCommStr, localDestFile);
		System.err.println("zip p2 done");
		String p2SrcFileStr = CommUtil.getFilesWithStartEndFilter(localDestFile, Constants.P2_PREFIX, "").getAbsolutePath();
		// add SWT p2 upload list
		CommUtil.writeStrToFile(tempFilePath, "swtp2=" + p2SrcFileStr, true);

		System.err.println("Finish upload list!!!");
		if (Boolean.getBoolean("isClearUpLocalFolder")) {
			try { // clear up local folder
				CommUtil.deleteFolder(localDestFileStr);
			} catch (Exception e) {
				System.err.println("clear up local folder failed");
				e.printStackTrace();
			}
		}

	}
}
