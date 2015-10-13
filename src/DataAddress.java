public class DataAddress {
	int nameId;//唯一标识该param
	String data_type; //表示属于什么类型的数据
	String file_type;//表示文件的后缀类型
	String file_addr;//表示文件地址
	String paramInfo;
	public DataAddress(String dataType, String addr ,String fileType){
		paramInfo = "";
		data_type = dataType;
		file_type = fileType;
		file_addr = addr;
	}
	public void fillParamInfo(){
		paramInfo = data_type + ":" + file_addr.replaceAll(":", "%") + ","+ file_type + "\n";
	}
	public void printInfo(){
		
		System.out.print(paramInfo);
	}
}
