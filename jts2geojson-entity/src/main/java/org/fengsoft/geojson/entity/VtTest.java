package org.fengsoft.geojson.entity;
import java.math.*;
import org.beetl.sql.core.annotatoin.Table;


/* 
* 
* gen by beetlsql 2018-09-25
*/
@Table(name="vt_test")
public class VtTest   {
	
	private Integer gid ;
	private String city ;
	private BigDecimal id ;
	private String name ;
	private Object shape ;
	private BigDecimal subtype ;
	private BigDecimal type ;
	
	public VtTest() {
	}
	
	public Integer getGid(){
		return  gid;
	}
	public void setGid(Integer gid ){
		this.gid = gid;
	}
	
	public String getCity(){
		return  city;
	}
	public void setCity(String city ){
		this.city = city;
	}

	public BigDecimal getId(){
		return  id;
	}
	public void setId(BigDecimal id ){
		this.id = id;
	}
	
	public String getName(){
		return  name;
	}
	public void setName(String name ){
		this.name = name;
	}
	
	public Object getShape(){
		return  shape;
	}
	public void setShape(Object shape ){
		this.shape = shape;
	}
	
	public BigDecimal getSubtype(){
		return  subtype;
	}
	public void setSubtype(BigDecimal subtype ){
		this.subtype = subtype;
	}
	
	public BigDecimal getType(){
		return  type;
	}
	public void setType(BigDecimal type ){
		this.type = type;
	}
	

}
