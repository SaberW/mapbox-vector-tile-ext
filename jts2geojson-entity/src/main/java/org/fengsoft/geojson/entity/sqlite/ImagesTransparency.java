package org.fengsoft.geojson.entity.sqlite;
import java.math.*;
import java.util.Date;
import java.sql.Timestamp;
import org.beetl.sql.core.annotatoin.Table;


/* 
* 
* gen by beetlsql 2018-08-25
*/
@Table(name="images_transparency")
public class ImagesTransparency   {
	
	private Integer transparency ;
	private String tileId ;
	
	public ImagesTransparency() {
	}
	
	public Integer getTransparency(){
		return  transparency;
	}
	public void setTransparency(Integer transparency ){
		this.transparency = transparency;
	}
	
	public String getTileId(){
		return  tileId;
	}
	public void setTileId(String tileId ){
		this.tileId = tileId;
	}
	

}
