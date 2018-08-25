package org.fengsoft.jts2geojson.entity.sqlite;
import java.math.*;
import java.util.Date;
import java.sql.Timestamp;
import org.beetl.sql.core.annotatoin.Table;


/* 
* 
* gen by beetlsql 2018-08-25
*/
@Table(name="map")
public class Map   {
	
	private Integer tileColumn ;
	private Integer tileRow ;
	private Integer zoomLevel ;
	private String tileId ;
	
	public Map() {
	}
	
	public Integer getTileColumn(){
		return  tileColumn;
	}
	public void setTileColumn(Integer tileColumn ){
		this.tileColumn = tileColumn;
	}
	
	public Integer getTileRow(){
		return  tileRow;
	}
	public void setTileRow(Integer tileRow ){
		this.tileRow = tileRow;
	}
	
	public Integer getZoomLevel(){
		return  zoomLevel;
	}
	public void setZoomLevel(Integer zoomLevel ){
		this.zoomLevel = zoomLevel;
	}
	
	public String getTileId(){
		return  tileId;
	}
	public void setTileId(String tileId ){
		this.tileId = tileId;
	}
	

}
