package org.fengsoft.jts2geojson.entity.sqlite;

import org.beetl.sql.core.annotatoin.AutoID;
import org.beetl.sql.core.annotatoin.Table;


/* 
* 
* gen by beetlsql 2018-08-31
*/
@Table(name="error_tile_info")
public class ErrorTileInfo   {

	@AutoID
	private Integer id ;
	private Integer x ;
	private Integer y ;
	private Integer z ;
	
	public ErrorTileInfo() {
	}

	public ErrorTileInfo(int x, int y, int z) {
		this.x=x;
		this.y=y;
		this.z=z;
	}

	public Integer getId(){
		return  id;
	}
	public void setId(Integer id ){
		this.id = id;
	}
	
	public Integer getX(){
		return  x;
	}
	public void setX(Integer x ){
		this.x = x;
	}
	
	public Integer getY(){
		return  y;
	}
	public void setY(Integer y ){
		this.y = y;
	}
	
	public Integer getZ(){
		return  z;
	}
	public void setZ(Integer z ){
		this.z = z;
	}
	

}
