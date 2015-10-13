import java.util.HashMap;
import java.util.Vector;
public class Project {
	private int prjId;
	private int userId;
	private String prjName;
	private int resultFlag;
	private String resultAddr;
	private int layerNum;
	private Vector<DataAddress> data_addr;
	private Vector<Param> global_info;
	private Vector<LayerInfo> layers;
	
	String paramFileInfo;
	public Project(int id, int userid, String name){
		prjId = id;
		userId = userid;
		prjName = name;
		resultFlag = -1;
		resultAddr = null;
		paramFileInfo = "";
		data_addr = new Vector<DataAddress>();
		layers = new Vector<LayerInfo>();
		global_info = new Vector<Param>();
	}
	public Project(int id){
		prjId = id;
		resultFlag = -1;
		resultAddr = null;
		paramFileInfo = "";
		layers = new Vector<LayerInfo>();
		data_addr = new Vector<DataAddress>();
		global_info = new Vector<Param>();
	}
	

	public void setResult(int flag, String addr){
		resultFlag = flag;
		resultAddr = addr;
	}
	public int getPrjId(){
		return prjId;
	}
	public int getUserId(){
		return userId;
	}
	public String getPrjName(){
		return prjName;
	}
	public int getLayerNum(){
		return layerNum;
	}
	public Vector<DataAddress> get_data_addr(){
		return data_addr;
	}
	public void addLayer(LayerInfo layer){
		if(layers != null){
			layers.add(layer);
			layerNum ++;
		}
		else{
			layers = new Vector<LayerInfo>();
			layers.add(layer);
			layerNum ++;
		}
	}
	public Vector<LayerInfo> getLayers(){
		return layers;
		
	}
	public Vector<Param> getGlobalInfo(){
		return global_info;
	}
	public LayerInfo getLayer(int j){
		return layers.get(j);
		
	}
	public void addGlobalInfo(Param p){
		global_info.add(p);
	}
	
	
	public void fillParamFile(){
		paramFileInfo = "Project_name:" + prjId + "\n";
		paramFileInfo += "Layer_num:"+layerNum+"\n";
		
		for(int i = 0;i < global_info.size();i++){
			global_info.get(i).fillParamInfo();
			paramFileInfo += global_info.get(i).paramInfo;
		}
		
		for(int i = 0;i < layerNum; i++){
			
			layers.get(i).fillLayerInfo();
			paramFileInfo += layers.get(i).layerInfo;
		}
		
		for(int i = 0;i < data_addr.size(); i++){
			data_addr.get(i).fillParamInfo();
			paramFileInfo += data_addr.get(i).paramInfo;
		}
		
	}
	public int hashCode(){
		return ((Integer)prjId).hashCode();
	}
}
