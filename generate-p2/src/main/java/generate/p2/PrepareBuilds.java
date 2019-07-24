package generate.p2;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;

import constant.Constants;
import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import util.CommUtil;

public class PrepareBuilds {

	public static void main(String[] args) throws IOException {
		new PrepareBuilds().generatP2();
	}

	public void generatP2() throws IOException {
		System.setProperty("localDestFileStr", "D:/build_1");
		System.setProperty("licensePrefix", "TP_ALL");
		System.setProperty("keyContains", "V7.3.1");
		System.setProperty("licenseKey", "30days_5users");
		System.setProperty("mixedLicenseKey", "_mixed");

		System.setProperty("ftpServer", "192.168.30.10");
		System.setProperty("ftpUserid", "anonymous");
		System.setProperty("ftpPassword", "");

		System.setProperty("sambaDir", "/talendqa/public/TUP_P2_builds_debug");
		System.setProperty("sambaServer", "192.168.33.241");
		System.setProperty("sambaUser", "automation");
		System.setProperty("sambaPasswd", "automation.com");
		System.setProperty("isNeedCISigner", "true");
		System.setProperty("isNeedStudio", "true");
		System.setProperty("isNeedCISigner", "true");
		System.setProperty("isNeedLicense", "true");
		System.setProperty("isNeedMixedLicense", "true");
		System.setProperty("isNeedfullP2", "false");
		System.setProperty("isNeedCIBuilder", "true");
		System.setProperty("isNeedCIBuilder", "true");
		System.setProperty("isNeedCISigner", "true");
		System.setProperty("isClearUpLocalFolder", "false");
		System.setProperty("isNeedTAC", "true");

		String localDestFileStr = System.getProperty("localDestFileStr");
		String licensePrefix = System.getProperty("licensePrefix");
		String smbDestFolderStr = System.getProperty("smbDestFolderStr");
		String ftpServer = System.getProperty("ftpServer");
		String ftpUserid = System.getProperty("ftpUserid");
		String ftpPassword = System.getProperty("ftpPassword");

		String sambaDir = System.getProperty("sambaDir");
		String sambaServer = System.getProperty("sambaServer");
		String sambaUser = System.getProperty("sambaUser");
		String sambaPasswd = System.getProperty("sambaPasswd");
		String destLicenseFileStr = "";
		String destStudioFileStr = "";

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
				// upload license
				CommUtil.uploadFileToSamba(destLicenseFileStr);
				System.err.println("upload license done");
			} catch (Exception e) {
				System.err.println("upload license failed");
			}
		}

		if (Boolean.getBoolean("isNeedMixedLicense")) {
			try {
				// get mixed license
				String latstMixedLicense = CommUtil.getLatstMixedLicenseFile(ftpClient, lastBuildRootFolder);
				// download Mixed license from ftp to local
				String destMixLicenseFileStr = CommUtil.ftpDownloadFiles(ftpClient, latstMixedLicense, localDestFileStr);
				// upload mixed license
				CommUtil.uploadFileToSamba(destMixLicenseFileStr);
				System.err.println("upload mixed license done");
			} catch (Exception e) {
				System.err.println("upload mixed license failed");
			}
		}

		if (Boolean.getBoolean("isNeedStudio")) {
			try {
				// get studio
				String lastStudio = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, Constants.STUDIO_PREFIX);
				// download studio from ftp to local and unzip
				destStudioFileStr = CommUtil.ftpDownloadFiles(ftpClient, lastStudio, localDestFileStr);
				// upload studio
				CommUtil.uploadFileToSamba(destStudioFileStr);
				System.err.println("upload studio done");
			} catch (Exception e) {
				System.err.println("upload studio failed");
			}
		}

		if (Boolean.getBoolean("isNeedfullP2")) {
			try {
				// get full p2
				String lastFullP2 = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, Constants.FullP2_PREFIX);
				// download full p2 from ftp to local
				String destFullP2FileStr = CommUtil.ftpDownloadFiles(ftpClient, lastFullP2, localDestFileStr);
				// upload full p2
				CommUtil.uploadFileToSamba(destFullP2FileStr);
				System.err.println("upload full p2 done");
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("upload full p2 failed");
			}
		}

		if (Boolean.getBoolean("isNeedCIBuilder")) {
			try {
				// get ci builder
				String lastCIBuilder = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, Constants.CIBuilder_PREFIX);
				// download ci builder from ftp to local
				String destCIBuilderFileStr = CommUtil.ftpDownloadFiles(ftpClient, lastCIBuilder, localDestFileStr);
				// upload ci builder
				CommUtil.uploadFileToSamba(destCIBuilderFileStr);
				System.err.println("upload ci builder done");
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("upload ci builder failed");
			}
		}

		if (Boolean.getBoolean("isNeedCISigner")) {
			try {
				// get CI signer file
				String lastSignerFile = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, Constants.CISigner_PREFIX);
				// download CI signer file from ftp to local
				String destSignerFileStr = CommUtil.ftpDownloadFiles(ftpClient, lastSignerFile, localDestFileStr);
				// upload CI signer file
				CommUtil.uploadFileToSamba(destSignerFileStr);
				System.err.println("upload CI Signer file done");
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("upload CI Signer file failed");
			}
		}

		if (Boolean.getBoolean("isNeedTAC")) {
			try {
				// get tac
				String lastTacFile = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, Constants.TAC_PREFIX);
				// // download tac from ftp to local
				String destTacFileStr = CommUtil.ftpDownloadFiles(ftpClient, lastTacFile, localDestFileStr);
				// upload tac
				CommUtil.uploadFileToSamba(destTacFileStr);
				System.err.println("upload TAC done");
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("upload TAC failed");
			}
		}

		try {// generate SWT p2
			CommUtil.unzip(destLicenseFileStr, localDestFileStr);
			System.err.println("unzip license done: " + destLicenseFileStr);
			CommUtil.unzip(destStudioFileStr, localDestFileStr);
			System.err.println("unzip studio done: " + destStudioFileStr);
			// get SWTBotAll_p2
			String lastSWTFile = CommUtil.getLastBuildFilterFile(ftpClient, lastBuildRootFolder, Constants.SWT_PREFIX);
			// download SWTBotAll_p2 from ftp to local and unzip
			String destSWTFileStr = CommUtil.ftpDownloadFiles(ftpClient, lastSWTFile, localDestFileStr);
			CommUtil.unzip(destSWTFileStr, localDestFileStr + "/" + new File(destSWTFileStr).getName().replace(".zip", ""));
			System.err.println("unzip SWTBotAll_p2 done: " + destStudioFileStr);
			String swtbotP2FolderString = destSWTFileStr.replace(".zip", "");
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
			String zipCommStr = "jar -cMf " + Constants.P2_PREFIX + studioFolderNameStr + " " + studioFolderNameStr.replace(".zip", "");
			CommUtil.runCommand(zipCommStr, localDestFile);
			System.err.println("zip p2 done");
			// upload SWT p2
			String p2SrcFileStr = CommUtil.getFilesWithStartEndFilter(localDestFile, Constants.P2_PREFIX, "").getAbsolutePath();
			CommUtil.uploadFileToSamba(p2SrcFileStr);
			System.err.println("upload SWT p2 done");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("upload SWT P2 failed");
		}

		if (Boolean.getBoolean("isClearUpLocalFolder")) {
			try { // clear up local folder
				CommUtil.deleteFolder(localDestFileStr);
			} catch (Exception e) {
				System.err.println("clear up local folder failed");
			}
		}

		ftpClient.disconnect();
	}
}
