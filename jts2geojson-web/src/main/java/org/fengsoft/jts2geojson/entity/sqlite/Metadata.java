package org.fengsoft.jts2geojson.entity.sqlite;
import java.math.*;
import java.util.Date;
import java.sql.Timestamp;
import org.beetl.sql.core.annotatoin.Table;


/* 
* 
* gen by beetlsql 2018-08-25
*/
@Table(name="metadata")
public class Metadata   {
	
	private String name ;
	private String value ;
	
	public Metadata() {
	}
	
	public String getName(){
		return  name;
	}
	public void setName(String name ){
		this.name = name;
	}
	
	public String getValue(){
		return  value;
	}
	public void setValue(String value ){
		this.value = value;
	}
	

}
