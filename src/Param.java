
public class Param {
	int nameId;//Ψһ��ʶ��param
	String name;
	int typeId;//ָ����ǰparam�����Ĳ������
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
