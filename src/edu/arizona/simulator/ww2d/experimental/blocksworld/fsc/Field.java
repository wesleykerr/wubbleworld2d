package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

public class Field {
	public Object data;
	public String name;
	
	public Field(String name,Object data){
		this.data = data;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Object getData(){
		return data;
	}

}
