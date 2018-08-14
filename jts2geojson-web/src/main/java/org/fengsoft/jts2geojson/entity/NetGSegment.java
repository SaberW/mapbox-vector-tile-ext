package org.fengsoft.jts2geojson.entity;
import java.util.Date;
import org.beetl.sql.core.annotatoin.Table;
import org.fengsoft.jts2geojson.convert.entity.GeometryEntity;


/* 
* 线路_架空线段[SEG_ID:NET_G_SEGMENT]
* gen by beetlsql 2018-07-30
*/
@Table(name="net_g_segment")
public class NetGSegment implements GeometryEntity<String> {
	
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
	顺序号
	*/
	private Integer orderNo ;
	/*
	所属线路分支图形内码
	*/
	private Long pathId ;
	/*
	坐标点拼接串
	*/
	private String pointTmp ;
	/*
	线段图形内码
	*/
	private Long segId ;
	/*
	所属线段组
	*/
	private String sgDevId ;
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
	
	public NetGSegment() {
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
	* 顺序号
	*@return 
	*/
	public Integer getOrderNo(){
		return  orderNo;
	}
	/**
	* 顺序号
	*@param  orderNo
	*/
	public void setOrderNo(Integer orderNo ){
		this.orderNo = orderNo;
	}
	
	/**
	* 所属线路分支图形内码
	*@return 
	*/
	public Long getPathId(){
		return  pathId;
	}
	/**
	* 所属线路分支图形内码
	*@param  pathId
	*/
	public void setPathId(Long pathId ){
		this.pathId = pathId;
	}
	
	/**
	* 坐标点拼接串
	*@return 
	*/
	public String getPointTmp(){
		return  pointTmp;
	}
	/**
	* 坐标点拼接串
	*@param  pointTmp
	*/
	public void setPointTmp(String pointTmp ){
		this.pointTmp = pointTmp;
	}
	
	/**
	* 线段图形内码
	*@return 
	*/
	public Long getSegId(){
		return  segId;
	}
	/**
	* 线段图形内码
	*@param  segId
	*/
	public void setSegId(Long segId ){
		this.segId = segId;
	}
	
	/**
	* 所属线段组
	*@return 
	*/
	public String getSgDevId(){
		return  sgDevId;
	}
	/**
	* 所属线段组
	*@param  sgDevId
	*/
	public void setSgDevId(String sgDevId ){
		this.sgDevId = sgDevId;
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


	@Override
	public String getId() {
		return getDevId();
	}
}
