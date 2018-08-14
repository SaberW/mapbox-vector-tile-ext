package org.fengsoft.jts2geojson.entity;
import java.math.*;
import java.util.Date;
import java.sql.Timestamp;
import org.beetl.sql.core.annotatoin.Table;
import org.fengsoft.jts2geojson.convert.entity.GeometryEntity;


/* 
* 供电分区表
* gen by beetlsql 2018-07-30
*/
@Table(name="pln_elec_area")
public class PlnElecArea implements GeometryEntity<String> {
	
	/*
	主键ID
	*/
	private String id ;
	/*
	面积(平方公里)
	*/
	private BigDecimal acreage ;
	/*
	颜色（默认灰色)
	*/
	private String areaColor ;
	/*
	分区等级（A+,A,B,C,D,E）
	*/
	private String areaLevel ;
	/*
	类型（1，网格；2，变电站分区；3，供电分区类型；4，城农网）
	*/
	private String areaType ;
	/*
	所属规划年份
	*/
	private String areaYear ;
	private String bakColumn1 ;
	private String bakColumn2 ;
	private String bakColumn3 ;
	/*
	地市局内码
	*/
	private String buro ;
	/*
	区域名称
	*/
	private String regionname ;
	/*
	备注
	*/
	private String remark ;
	/*
	供区的空间数据
	*/
	private Object shape ;
	private String shapeWkt ;
	/*
	分县局内码
	*/
	private String subburo ;
	private Date createDate ;
	private Date updateDate ;
	
	public PlnElecArea() {
	}
	
	/**
	* 主键ID
	*@return 
	*/
	public String getId(){
		return  id;
	}
	/**
	* 主键ID
	*@param  id
	*/
	public void setId(String id ){
		this.id = id;
	}
	
	/**
	* 面积(平方公里)
	*@return 
	*/
	public BigDecimal getAcreage(){
		return  acreage;
	}
	/**
	* 面积(平方公里)
	*@param  acreage
	*/
	public void setAcreage(BigDecimal acreage ){
		this.acreage = acreage;
	}
	
	/**
	* 颜色（默认灰色)
	*@return 
	*/
	public String getAreaColor(){
		return  areaColor;
	}
	/**
	* 颜色（默认灰色)
	*@param  areaColor
	*/
	public void setAreaColor(String areaColor ){
		this.areaColor = areaColor;
	}
	
	/**
	* 分区等级（A+,A,B,C,D,E）
	*@return 
	*/
	public String getAreaLevel(){
		return  areaLevel;
	}
	/**
	* 分区等级（A+,A,B,C,D,E）
	*@param  areaLevel
	*/
	public void setAreaLevel(String areaLevel ){
		this.areaLevel = areaLevel;
	}
	
	/**
	* 类型（1，网格；2，变电站分区；3，供电分区类型；4，城农网）
	*@return 
	*/
	public String getAreaType(){
		return  areaType;
	}
	/**
	* 类型（1，网格；2，变电站分区；3，供电分区类型；4，城农网）
	*@param  areaType
	*/
	public void setAreaType(String areaType ){
		this.areaType = areaType;
	}
	
	/**
	* 所属规划年份
	*@return 
	*/
	public String getAreaYear(){
		return  areaYear;
	}
	/**
	* 所属规划年份
	*@param  areaYear
	*/
	public void setAreaYear(String areaYear ){
		this.areaYear = areaYear;
	}
	
	public String getBakColumn1(){
		return  bakColumn1;
	}
	public void setBakColumn1(String bakColumn1 ){
		this.bakColumn1 = bakColumn1;
	}
	
	public String getBakColumn2(){
		return  bakColumn2;
	}
	public void setBakColumn2(String bakColumn2 ){
		this.bakColumn2 = bakColumn2;
	}
	
	public String getBakColumn3(){
		return  bakColumn3;
	}
	public void setBakColumn3(String bakColumn3 ){
		this.bakColumn3 = bakColumn3;
	}
	
	/**
	* 地市局内码
	*@return 
	*/
	public String getBuro(){
		return  buro;
	}
	/**
	* 地市局内码
	*@param  buro
	*/
	public void setBuro(String buro ){
		this.buro = buro;
	}
	
	/**
	* 区域名称
	*@return 
	*/
	public String getRegionname(){
		return  regionname;
	}
	/**
	* 区域名称
	*@param  regionname
	*/
	public void setRegionname(String regionname ){
		this.regionname = regionname;
	}
	
	/**
	* 备注
	*@return 
	*/
	public String getRemark(){
		return  remark;
	}
	/**
	* 备注
	*@param  remark
	*/
	public void setRemark(String remark ){
		this.remark = remark;
	}
	
	/**
	* 供区的空间数据
	*@return 
	*/
	public Object getShape(){
		return  shape;
	}
	/**
	* 供区的空间数据
	*@param  shape
	*/
	public void setShape(Object shape ){
		this.shape = shape;
	}
	
	public String getShapeWkt(){
		return  shapeWkt;
	}
	public void setShapeWkt(String shapeWkt ){
		this.shapeWkt = shapeWkt;
	}
	
	/**
	* 分县局内码
	*@return 
	*/
	public String getSubburo(){
		return  subburo;
	}
	/**
	* 分县局内码
	*@param  subburo
	*/
	public void setSubburo(String subburo ){
		this.subburo = subburo;
	}
	
	public Date getCreateDate(){
		return  createDate;
	}
	public void setCreateDate(Date createDate ){
		this.createDate = createDate;
	}
	
	public Date getUpdateDate(){
		return  updateDate;
	}
	public void setUpdateDate(Date updateDate ){
		this.updateDate = updateDate;
	}
	

}
