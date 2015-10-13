import java.sql.*;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.*;
import java.net.ServerSocket;  
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
public class ComputeClient {
	static String remote_file_path = "/wyq/result/";
	static String ROOT_PATH = "C:/Project/DeepLearning/";
	//static String ROOT_PATH = "F:/testDL/";

	static String local_file_path = ROOT_PATH + "data/";
	static String local_result_path = ROOT_PATH + "result/";
	static String local_param_path = ROOT_PATH + "param/";
	static String web_server_ip = "172.16.248.231";
	static int web_server_port = 21;
	static int master_port = 8081;
	static String master_ip = "172.16.248.77";

	public static void main(String[] args){ 
		
		
		
		String taskInfo = new String();
		Vector<String> resultFileNames = new Vector<String>();
		String paramName = local_param_path;

		while(true){
			
			int taskId = getProjectInfoBySocket();
			//TODO:
			if(taskId == -1){
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}

			//writeBatFile(paramName);
			//"cmd /k start " +
			String exe_command =  ROOT_PATH + "DeepLearningFrame.exe " + paramName + "prj_" + taskId + ".param";

			Process process = null;
			try {
				process = Runtime.getRuntime().exec(exe_command);
				BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String s = br.readLine();
				String temp = "";
				while(s!=null){
					if(!"".equals(s.trim())) temp = s;
					s = br.readLine();
				}
				br.close();
				process.waitFor();
				process.destroy();
				System.out.println("p1 has been executed!");
				//process = Runtime.getRuntime().exec(exe_command1);
				//process.waitFor();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*			if(prj.getTrainDataAddr() != "" && prj.getTrainDataAddr() != null){
				executeMatlabProgram();
				while(true){
					if(findProcess("DeepLearningFrame.exe")){
						System.out.println("Matlab has been start");
						break;
					}
				}
				while(true){
					if(!findProcess("DeepLearningFrame.exe")){
						System.out.println("Matlab has been killed");
						break;
					}

				}	
			}*/
			sendIpToMaster(master_ip,master_port);
			String zipFileName = local_result_path + "prj_"+taskId + ".zip";
			String remoteFileName = remote_file_path + "prj_"+taskId + ".zip";
			File inputFile = new File(local_result_path+"prj_"+taskId+"/");
			compressResultFiles(inputFile,zipFileName);
			sendResultFileToWebServer(zipFileName,remoteFileName,web_server_ip,web_server_port);

		}

	}
	private static void compressResultFiles(File inputFile, String zipFileName) {
		System.out.println("Compressing");
		try{
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
			BufferedOutputStream bo = new BufferedOutputStream(out);
			zip(out,inputFile,inputFile.getName(),bo);
			bo.close();
			out.close();
		}catch (Exception e){
			e.printStackTrace();
			
		}
		System.out.print("Complete Compressing");


	}
	private static void zip(ZipOutputStream out,
			File f, String base, BufferedOutputStream bo) {
		// TODO Auto-generated method stub
		try {
			if(f.isDirectory()){
				File[] fl = f.listFiles();
				if(fl.length == 0){

					out.putNextEntry(new ZipEntry(base + "/"));
				} 
				System.out.println("1:"+base+"/");

				for(int i = 0;i < fl.length;i++){
					zip(out,fl[i],base+"/"+fl[i].getName(),bo);
					System.out.println("2:"+base + "/" + fl[i].getName());
				}
			}else{

				out.putNextEntry(new ZipEntry(base));

				FileInputStream in = new FileInputStream(f);
				BufferedInputStream bi = new BufferedInputStream(in);
				int cnt = 0;
				byte[] buffer = new byte[1024];
				while((cnt = bi.read(buffer))!= -1){
					bo.write(buffer,0,cnt);
					bo.flush();
				}
				bi.close();
				in.close();


			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}

	}



	/** 
	 * 
	 * 
	 * @param strUrl 
	 * @param fileName 
	 * @return 
	 * @throws IOException 
	 */ 
	private static boolean getRemoteFile(String strUrl, String fileName) throws IOException { 
		File file = new File(fileName);
		System.out.println("Download the remote files.");
		if(file.exists()){
			return true;
		}

		try {
			URL url = new URL(strUrl);
			InputStream is = url.openStream();


			OutputStream os = new FileOutputStream(fileName);

			int bytesRead = 0;
			byte[] buffer = new byte[1024];

			while((bytesRead = is.read(buffer,0,1024)) != -1){
				os.write(buffer, 0, bytesRead);
			}
		} catch (Exception e2) {
			e2.printStackTrace(); 
			System.exit(-1);
		}
		System.out.println("Download over.");
		return true; 
	}

	private static void writeFile(String fileName,String content) throws IOException{

		FileWriter fw=new FileWriter(fileName);

		fw.write(content);//重新写文件

		fw.close();

	}

	/**
	 * send file to web server
	 */
	private static boolean sendResultFileToWebServer(String zipFileName,String remote_file,String ip, int port){
		FTPEngine client = new FTPEngine(web_server_ip,web_server_port,"admin","admin");
		try {
			client.connect();
			client.upload(zipFileName, remote_file);
			client.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true; 		
	}


	private static void sendIpToMaster(String ipAddr, int port) {
		// TODO Auto-generated method stub

		Socket socket = null;  
		PrintWriter pw = null;  
		try {  
			//setup socket
			socket = new Socket(ipAddr, port);  
			System.out.println("Socket=" + socket);  
			//send ip
			String localIp = socket.getLocalAddress().getHostAddress();
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(  
					socket.getOutputStream())));  
			pw.println(localIp);
			pw.flush();
			pw.println("END");  
			pw.flush();
		} catch (Exception e) {  
			e.printStackTrace();  
			System.exit(-1);
		} finally {  
			try {  
				System.out.println("close......");  
				pw.close();  
				socket.close();  
			} catch (IOException e) {  
				// TODO Auto-generated catch block   
				e.printStackTrace();
				System.exit(-1);
			}  
		}  
	}
	/**
	 * 
	 * @return task info from master node
	 */
	private static int getProjectInfoBySocket() {
		int taskId = -1;
		String taskInfo = new String();
		Vector<DataAddress> addr_info = new Vector<DataAddress>();
		Vector<String> all_remote_files = new Vector<String>();
		try {
			ServerSocket server = null;  
			Socket socket  = null;
			server = new ServerSocket(8080);
			System.out.println("socket has been start up ");
			socket = server.accept(); 
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));   
			PrintWriter out=new PrintWriter(socket.getOutputStream());   
		
			while(true){   
				String str=in.readLine();   
				
				if(str.equals("END")) 	
					break;
				if(str.equals("")){
					continue;
				}
				if(str.indexOf(":")==-1)
					continue;
				else{
					
					out.println(str);
					//System.out.println(str);
					String[] str_split = new String[2];
					str_split = str.split(":");
					if(str_split[0].equals("Project_name")){
						taskId = Integer.parseInt(str_split[1]);
						continue;
					}
					//System.out.println("split[0]: " + str_split[0]);
					String[] values = str_split[1].split(",");
					if(    str_split[0].equals("trainData") || str_split[0].equals("trainLabels") 
						|| str_split[0].equals("testData") || str_split[0].equals("testLabels")
						|| str_split[0].equals("Finetune_data") || str_split[0].equals("Finetune_labels")
						|| str_split[0].equals("Weight_addr")){
						
						String remote_file_name = values[0].replaceAll("%", ":");
						all_remote_files.add(remote_file_name);
						String remote_path = new String();
						if(remote_file_name.lastIndexOf("/")!=-1)
							remote_path = remote_file_name.substring(0,remote_file_name.lastIndexOf("/"));
						else if(remote_file_name.lastIndexOf("\\")!=-1){
							remote_path = remote_file_name.substring(0,remote_file_name.lastIndexOf("\\"));
						}else{
							remote_path = remote_file_name;
						}
								
						String local_file = remote_file_name.replace(remote_path, local_file_path);
						
						//System.out.println("Local path : " + local_file);
						
						DataAddress file_addr = new DataAddress(str_split[0],local_file,values[1]);
						
						addr_info.add(file_addr);
						continue;
					}
					taskInfo += str + "\n";
					out.flush();   
				}
			}   
			out.close();
			
			socket.close();   
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//System.exit(-1);
		}
		
		/**
		 * Now write param file.
		 */
		
		String paramFileName = ROOT_PATH + "param/prj_" + taskId + ".param";
		
		for(int i = 0;i < addr_info.size();i++){
			addr_info.get(i).fillParamInfo();
			taskInfo += addr_info.get(i).paramInfo;
		}
		
		System.out.println(taskInfo);
		System.out.println(taskId);
		try {
			writeFile(paramFileName,taskInfo);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		/**
		 * Now, get the remote files
		 */
		for(int i = 0;i < all_remote_files.size(); i++){
			String remote_path = all_remote_files.get(i).substring(0,all_remote_files.get(i).lastIndexOf("/"));
			try {
				getRemoteFile(all_remote_files.get(i),all_remote_files.get(i).replace(remote_path, local_file_path));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
			
		}
		
		
		return taskId;
	}
	/** 
	 * 执行批处理文件，并获取输出流重新输出到控制台 
	 */ 
	public static void executeMatlabProgram() { 
		Process process; 
		try { 
			//执行命令 
			process = Runtime.getRuntime().exec("C:/Project/Deeplearning/startExe.bat"); 
			//取得命令结果的输出流 
			InputStream fis = process.getInputStream(); 
			//用一个读输出流类去读 
			BufferedReader br = new BufferedReader(new InputStreamReader(fis)); 
			String line = null; 
			//逐行读取输出到控制台 
			while ((line = br.readLine()) != null) { 
				System.out.println(line); 
			} 
		} catch (IOException e) { 
			e.printStackTrace(); 
			System.exit(-1);
		} 
	}
	/**
	 * 检测程序。
	 * 
	 * @param processName 线程的名字，请使用准确的名字
	 * @return 找到返回true,没找到返回false
	 */
	public static boolean findProcess(String processName) {
		BufferedReader bufferedReader = null;
		try {
			Process proc = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq " + processName + "\"");
			bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains(processName)) {
					return true;
				}
			}
			return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception ex) {
					System.exit(-1);
				}
			}
		}
	} 	
}