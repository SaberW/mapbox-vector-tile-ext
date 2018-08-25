package org.fengsoft.jts2geojson.entity.sqlite;
import java.math.*;
import java.util.Date;
import java.sql.Timestamp;
import org.beetl.sql.core.annotatoin.Table;


/* 
* 
* gen by beetlsql 2018-08-25
*/
@Table(name="images")
public class Images   {
	
	private String tileData ;
	private String tileId ;
	
	public Images() {
	}
	
	public String getTileData(){
		return  tileData;
	}
	public void setTileData(String tileData ){
		this.tileData = tileData;
	}
	
	public String getTileId(){
		return  tileId;
	}
	public void setTileId(String tileId ){
		this.tileId = tileId;
	}
	

}
