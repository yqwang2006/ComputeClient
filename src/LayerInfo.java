import java.util.Vector;
public class LayerInfo {
	int layerId;
	int calOrder;
	int typeId;
	int paramNum;
	String type;
	String layerInfo="";
	Vector<Param> paramVec;
	public LayerInfo(int id, int order, int tyid, String type){
		layerId = id;
		calOrder = order;
		typeId = tyid;
		this.type = type;
		paramNum = 0;
		paramVec = new Vector<Param>();
	}
	public void setType(int tyid){
		this.typeId = tyid;
	}
	public void addParam(Param p){
		paramVec.add(p);
		paramNum ++;
	}
	public int getLayerId(){
		return layerId;
	}
	public int calOrder(){
		return calOrder;
	}
	public int getTypeId(){
		return typeId;
	}
	public String getType(){
		return type;
	}
	public void setType(String type){
		this.type = type;
	}
	public void fillLayerInfo(){
		layerInfo = "Layer_order:" + calOrder + "\n";
		layerInfo += "Algorithm:" + type + "\n";
		for(int i = 0;i < paramVec.size(); i++){
			paramVec.get(i).fillParamInfo();
			layerInfo += paramVec.get(i).paramInfo;
		}
	}
	public void printInfo(){
		System.out.print(layerInfo);
		
	}
}
