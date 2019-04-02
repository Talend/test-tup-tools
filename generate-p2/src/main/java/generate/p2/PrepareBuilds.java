package generate.p2;

import java.io.File;
import java.io.IOException;

import constant.Constants;
import util.CommUtil;

public class PrepareBuilds {
	String destFileStr = "D:\\build";
	
	public static void main(String[] args) throws IOException {
		//String fileName = "D:\\build\\V7.2.1SNAPSHOT_20190401_1936\\licenses_721_30days_5users_20190402_to_20190502.zip";
		//    String filePath = "D:\\build\\V7.2.1SNAPSHOT_20190401_1936";
		//CommUtil.unzip(fileName, new File(filePath)				);
		new PrepareBuilds().getNewestBuild();
	}

	public String getNewestBuild() throws IOException {
		// get latest build folder
		String latstBuldRtFlderStr = CommUtil.getLatstBuldRtFlderStr();
		System.err.println("latest build: " + latstBuldRtFlderStr);
		
		destFileStr = destFileStr + File.separator + CommUtil.latstBuldRtFlderNm;
		
		// get license
		File latstLicense = CommUtil.getLatstLicenseFile(latstBuldRtFlderStr);
		System.err.println("latest license: " + latstLicense);
		String destLicenseFileStr = CommUtil.copyBuild(latstLicense, destFileStr);
		//CommUtil.unzip(destLicenseFileStr, destFile);
		//System.err.println("unzip done: " + destLicenseFileStr);
		
		// get studio
		String allFolerStr =  latstBuldRtFlderStr + File.separator + Constants.ALL;
		File latstStudioFile = CommUtil.getLatstBuildFile(allFolerStr, Constants.V_PREFIX, Constants.SUBALL, Constants.STUDIO_PREFIX, Constants.ZIP_SUFFIX);
		System.err.println("latest studio: " + latstStudioFile);
//		String destStudioFileStr = CommUtil.copyBuild(latstStudioFile, destFileStr);
//		CommUtil.unzip(destStudioFileStr, destFile);
//		System.err.println("unzip done: " + destStudioFileStr);
		
		// get swtbotP2
		String swtFolderStr =  latstBuldRtFlderStr + File.separator + Constants.SWT;
		File latstSwtbotP2File = CommUtil.getLatstBuildFile(swtFolderStr, Constants.SWT_PREFIX, "", "", Constants.ZIP_SUFFIX);
		System.err.println("latest swtbotP2: " + latstSwtbotP2File);
		String destSwtbotP2FileStr = CommUtil.copyBuild(latstSwtbotP2File, destFileStr);
		String swtbotP2FolderString= destFileStr + File.separator + latstSwtbotP2File.getName().replace(".zip", "");
		CommUtil.unzip(destSwtbotP2FileStr, swtbotP2FolderString);
		System.err.println("unzip done: " + destSwtbotP2FileStr);
		
		// copy license to Studio
		String licenseFolerStr = destLicenseFileStr.replace(".zip", "");
		
		// get swtp2
		return null;
		
	}
}
