package org.fengsoft.jts2geojson.entity;
import java.math.*;
import java.util.Date;
import java.sql.Timestamp;
import org.beetl.sql.core.annotatoin.Table;
import org.fengsoft.jts2geojson.convert.entity.GeometryEntity;


/* 
* 变电_变电站[GADGET_ID:NET_G_SUBSTATION]
* gen by beetlsql 2018-07-30
*/
@Table(name="net_g_substation")
public class NetGSubstation implements GeometryEntity<String> {
	
	/*
	位置ID
	*/
	private String devId ;
	/*
	地市局内码
	*/
	private String buro ;
	/*
	类别ID
	*/
	private String classId ;
	/*
	设备图形内码
	*/
	private Long gadgetId ;
	/*
	空间位置
	*/
	private Object shape ;
	private String shapeWkt ;
	/*
	分县局内码
	*/
	private String subburo ;
	/*
	变电站类别编码
	*/
	private Integer substCategoryCode ;
	/*
	变电站类型编码
	*/
	private Integer substTypeCode ;
	/*
	图符旋转角度
	*/
	private BigDecimal symbolRotation ;
	/*
	主变总容量
	*/
	private BigDecimal transCapSum ;
	/*
	主变台数
	*/
	private Integer transNum ;
	/*
	电压等级代码
	*/
	private Integer vlevelCode ;
	/*
	X坐标
	*/
	private Long x ;
	/*
	Y坐标
	*/
	private Long y ;
	/*
	最后更改时间
	*/
	private Date lastModifyTime ;
	
	public NetGSubstation() {
	}
	
	/**
	* 位置ID
	*@return 
	*/
	public String getDevId(){
		return  devId;
	}
	/**
	* 位置ID
	*@param  devId
	*/
	public void setDevId(String devId ){
		this.devId = devId;
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
	* 类别ID
	*@return 
	*/
	public String getClassId(){
		return  classId;
	}
	/**
	* 类别ID
	*@param  classId
	*/
	public void setClassId(String classId ){
		this.classId = classId;
	}
	
	/**
	* 设备图形内码
	*@return 
	*/
	public Long getGadgetId(){
		return  gadgetId;
	}
	/**
	* 设备图形内码
	*@param  gadgetId
	*/
	public void setGadgetId(Long gadgetId ){
		this.gadgetId = gadgetId;
	}

	@Override
	public String getId() {
		return getDevId();
	}

	/**
	* 空间位置
	*@return 
	*/
	public Object getShape(){
		return  shape;
	}
	/**
	* 空间位置
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
	
	/**
	* 变电站类别编码
	*@return 
	*/
	public Integer getSubstCategoryCode(){
		return  substCategoryCode;
	}
	/**
	* 变电站类别编码
	*@param  substCategoryCode
	*/
	public void setSubstCategoryCode(Integer substCategoryCode ){
		this.substCategoryCode = substCategoryCode;
	}
	
	/**
	* 变电站类型编码
	*@return 
	*/
	public Integer getSubstTypeCode(){
		return  substTypeCode;
	}
	/**
	* 变电站类型编码
	*@param  substTypeCode
	*/
	public void setSubstTypeCode(Integer substTypeCode ){
		this.substTypeCode = substTypeCode;
	}
	
	/**
	* 图符旋转角度
	*@return 
	*/
	public BigDecimal getSymbolRotation(){
		return  symbolRotation;
	}
	/**
	* 图符旋转角度
	*@param  symbolRotation
	*/
	public void setSymbolRotation(BigDecimal symbolRotation ){
		this.symbolRotation = symbolRotation;
	}
	
	/**
	* 主变总容量
	*@return 
	*/
	public BigDecimal getTransCapSum(){
		return  transCapSum;
	}
	/**
	* 主变总容量
	*@param  transCapSum
	*/
	public void setTransCapSum(BigDecimal transCapSum ){
		this.transCapSum = transCapSum;
	}
	
	/**
	* 主变台数
	*@return 
	*/
	public Integer getTransNum(){
		return  transNum;
	}
	/**
	* 主变台数
	*@param  transNum
	*/
	public void setTransNum(Integer transNum ){
		this.transNum = transNum;
	}
	
	/**
	* 电压等级代码
	*@return 
	*/
	public Integer getVlevelCode(){
		return  vlevelCode;
	}
	/**
	* 电压等级代码
	*@param  vlevelCode
	*/
	public void setVlevelCode(Integer vlevelCode ){
		this.vlevelCode = vlevelCode;
	}
	
	/**
	* X坐标
	*@return 
	*/
	public Long getX(){
		return  x;
	}
	/**
	* X坐标
	*@param  x
	*/
	public void setX(Long x ){
		this.x = x;
	}
	
	/**
	* Y坐标
	*@return 
	*/
	public Long getY(){
		return  y;
	}
	/**
	* Y坐标
	*@param  y
	*/
	public void setY(Long y ){
		this.y = y;
	}
	
	/**
	* 最后更改时间
	*@return 
	*/
	public Date getLastModifyTime(){
		return  lastModifyTime;
	}
	/**
	* 最后更改时间
	*@param  lastModifyTime
	*/
	public void setLastModifyTime(Date lastModifyTime ){
		this.lastModifyTime = lastModifyTime;
	}
	

}
