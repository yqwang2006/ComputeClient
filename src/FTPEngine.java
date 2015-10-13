
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FTPEngine {	
	private static Log log = LogFactory.getLog(FTPEngine.class);


	public enum UploadStatus {
		Create_Directory_Fail, 
		Create_Directory_Success,
		Upload_New_File_Success, 
		Upload_New_File_Failed, 
		File_Exits,
		Remote_Bigger_Local,
		Upload_From_Break_Success,
		Upload_From_Break_Failed, 
		Delete_Remote_Faild;
	}


	public enum DownloadStatus {
		Remote_File_Noexist,
		Local_Bigger_Remote,
		Download_From_Break_Success, 
		Download_From_Break_Failed, 
		Download_New_Success, 
		Download_New_Failed; 
	}

	public FTPClient ftpClient = new FTPClient();
	private String host;
	private int port;
	private String username;
	private String password;

	// private String localFilePath;
	// private String remoteFilePath;

	public FTPEngine(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.ftpClient.addProtocolCommandListener(new PrintCommandListener(
				new PrintWriter(System.out)));
	}


	public boolean connect() throws IOException {
		ftpClient.connect(host, port);
		ftpClient.setControlEncoding("GBK");
		if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
			if (ftpClient.login(username, password)) {
				return true;
			}
		}
		disconnect();
		return false;
	}

	
	public DownloadStatus download(String remote, String local)
			throws IOException {
		ftpClient.enterLocalPassiveMode();
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		DownloadStatus result;

		FTPFile[] files = ftpClient.listFiles(new String(remote
				.getBytes("UTF-8"), "iso-8859-1"));
		if (files.length != 1) {
			log.error("error");
			return DownloadStatus.Remote_File_Noexist;
		}

		long lRemoteSize = files[0].getSize();
		File f = new File(local);
		if (f.exists()) {
			long localSize = f.length();
			if (localSize >= lRemoteSize) {
				log.error("error");
				return DownloadStatus.Local_Bigger_Remote;
			}

			FileOutputStream out = new FileOutputStream(f, true);
			ftpClient.setRestartOffset(localSize);
			InputStream in = ftpClient.retrieveFileStream(new String(remote
					.getBytes("UTF-8"), "iso-8859-1"));
			byte[] bytes = new byte[1024];
			long step = lRemoteSize / 100;
			long process = localSize / step;
			int c;
			while ((c = in.read(bytes)) != -1) {
				out.write(bytes, 0, c);
				localSize += c;
				long nowProcess = localSize / step;
				if (nowProcess > process) {
					process = nowProcess;
					if (process % 10 == 0)
						log.info("The process is " + process + "%");
				}
			}
			in.close();
			out.close();
			boolean isDo = ftpClient.completePendingCommand();
			if (isDo) {
				result = DownloadStatus.Download_From_Break_Success;
			} else {
				result = DownloadStatus.Download_From_Break_Failed;
			}
		} else {
			OutputStream out = new FileOutputStream(f);
			InputStream in = ftpClient.retrieveFileStream(new String(remote
					.getBytes("UTF-8"), "iso-8859-1"));
			byte[] bytes = new byte[1024];
			long step = lRemoteSize / 100;
			long process = 0;
			long localSize = 0L;
			int c;
			while ((c = in.read(bytes)) != -1) {
				out.write(bytes, 0, c);
				localSize += c;
				long nowProcess = localSize / step;
				if (nowProcess > process) {
					process = nowProcess;
					if (process % 10 == 0)
						log.info("The process is " + process + "%");
				}
			}
			in.close();
			out.close();
			boolean upNewStatus = ftpClient.completePendingCommand();
			if (upNewStatus) {
				result = DownloadStatus.Download_New_Success;
			} else {
				result = DownloadStatus.Download_New_Failed;
			}
		}
		return result;
	}

	public UploadStatus upload(String local, String remote) throws IOException {
		ftpClient.enterLocalPassiveMode();
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpClient.setControlEncoding("UTF-8");
		UploadStatus result;
		String remoteFileName = remote;
		if (remote.contains("/")) {
			remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
			if (CreateDirecroty(remote, ftpClient) == UploadStatus.Create_Directory_Fail) {
				return UploadStatus.Create_Directory_Fail;
			}
		}

		FTPFile[] files = ftpClient.listFiles(new String(remoteFileName
				.getBytes("UTF-8"), "iso-8859-1"));
		if (files.length == 1) {
			long remoteSize = files[0].getSize();
			File f = new File(local);
			long localSize = f.length();
			if (remoteSize == localSize) {
				return UploadStatus.File_Exits;
			} else if (remoteSize > localSize) {
				return UploadStatus.Remote_Bigger_Local;
			}

			result = uploadFile(remoteFileName, f, ftpClient, remoteSize);

			if (result == UploadStatus.Upload_From_Break_Failed) {
				if (!ftpClient.deleteFile(remoteFileName)) {
					return UploadStatus.Delete_Remote_Faild;
				}
				result = uploadFile(remoteFileName, f, ftpClient, 0);
			}
		} else {
			result = uploadFile(remoteFileName, new File(local), ftpClient, 0);
		}
		return result;
	}

	public boolean delete(String remotePath, String remoteFile) {
		boolean status = false;
		try {
			boolean flag = ftpClient.deleteFile(remotePath + remoteFile);
			if (flag)
				if (ftpClient.getReplyCode() == 250)
					status = true;
		} catch (IOException e) {
			log.error("error " + remotePath + remoteFile + "error");
			e.printStackTrace();
		}
		return status;
	}


	public void disconnect() throws IOException {
		if (ftpClient.isConnected()) {
			ftpClient.disconnect();
		}
	}

	
	public UploadStatus CreateDirecroty(String remote, FTPClient ftpClient)
			throws IOException {
		UploadStatus status = UploadStatus.Create_Directory_Success;
		String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
		if (!directory.equalsIgnoreCase("/")
				&& !ftpClient.changeWorkingDirectory(new String(directory
						.getBytes("UTF-8"), "iso-8859-1"))) {
			int start = 0;
			int end = 0;
			if (directory.startsWith("/")) {
				start = 1;
			} else {
				start = 0;
			}
			end = directory.indexOf("/", start);
			while (true) {
				String subDirectory = new String(remote.substring(start, end)
						.getBytes("UTF-8"), "iso-8859-1");
				if (!ftpClient.changeWorkingDirectory(subDirectory)) {
					if (ftpClient.makeDirectory(subDirectory)) {
						ftpClient.changeWorkingDirectory(subDirectory);
					} else {
						log.error("error");
						return UploadStatus.Create_Directory_Fail;
					}
				}

				start = end + 1;
				end = directory.indexOf("/", start);

				if (end <= start) {
					break;
				}
			}
		}
		return status;
	}

	
	public UploadStatus uploadFile(String remoteFile, File localFile,
			FTPClient ftpClient, long remoteSize) throws IOException {
		UploadStatus status;
		// 显示进度的上传
		long step = localFile.length();
		double process = 0;
		long localreadbytes = 0L;
		RandomAccessFile raf = new RandomAccessFile(localFile, "r");
		OutputStream out = ftpClient.appendFileStream(new String(remoteFile
				.getBytes("UTF-8"), "UTF-8"));
		// 断点续传
		if (remoteSize > 0) {
			ftpClient.setRestartOffset(remoteSize);
			process = remoteSize / step;
			raf.seek(remoteSize);
			localreadbytes = remoteSize;
		}
		byte[] bytes = new byte[1024];
		int c;
		while ((c = raf.read(bytes)) != -1) {
			out.write(bytes, 0, c);
			localreadbytes += c;
			if (localreadbytes / step != process) {
				process = localreadbytes / step;
				log.info("Process:" + process * 100 + "%");
			}
		}
		out.flush();
		raf.close();
		out.close();
		boolean result = ftpClient.completePendingCommand();
		if (remoteSize > 0) {
			status = result ? UploadStatus.Upload_From_Break_Success
					: UploadStatus.Upload_From_Break_Failed;
		} else {
			status = result ? UploadStatus.Upload_New_File_Success
					: UploadStatus.Upload_New_File_Failed;
		}
		return status;
	}

	public int doExistsFile(String fileName) {
		try {
			FTPFile[] file = ftpClient.listFiles(fileName);
			return file.length;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
}
