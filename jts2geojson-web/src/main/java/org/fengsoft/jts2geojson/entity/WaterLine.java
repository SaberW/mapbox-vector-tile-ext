package org.fengsoft.jts2geojson.entity;

import java.util.Date;

import cn.com.enersun.dgpmicro.entity.GeoJSONEntity;
import cn.com.enersun.dgpmicro.entity.VectorTileEntity;
import org.beetl.sql.core.annotatoin.Table;


/*
 *
 * gen by beetlsql 2018-08-14
 */
@Table(name = "water_line")
public class WaterLine implements GeoJSONEntity<Integer>, VectorTileEntity<Integer>{

    private Integer id;
    private String comment;
    private Object shape;
    private String name;
    private Long subtype;
    private Long type;
    private Date dataT;

    public WaterLine() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSubtype() {
        return subtype;
    }

    public void setSubtype(Long subtype) {
        this.subtype = subtype;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public Date getDataT() {
        return dataT;
    }

    public void setDataT(Date dataT) {
        this.dataT = dataT;
    }


    public Object getShape() {
        return shape;
    }

    public void setShape(Object shape) {
        this.shape = shape;
    }
}
