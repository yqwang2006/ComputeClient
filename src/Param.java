
public class Param {
	int nameId;//唯一标识该param
	String name;
	int typeId;//指定当前param所属的层的类型
	String values;
	String paramInfo = "";
	public Param(int nameid, String name, int typeid, String value){
		nameId = nameid;
		this.name = name;
		typeId = typeid;
		values = value;
	}
	public void fillParamInfo(){
		paramInfo = name + ":" + typeId + ","+ values + "\n";
	}
	public void printInfo(){
		
		System.out.print(paramInfo);
	}
}
