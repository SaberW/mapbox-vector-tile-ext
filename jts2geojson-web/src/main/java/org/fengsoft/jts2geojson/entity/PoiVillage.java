package org.fengsoft.jts2geojson.entity;

import java.math.*;
import java.util.Date;

import cn.com.enersun.dgpmicro.entity.GeoJSONEntity;
import cn.com.enersun.dgpmicro.entity.VectorTileEntity;
import org.beetl.sql.core.annotatoin.Table;


/*
 *
 * gen by beetlsql 2018-08-15
 */
@Table(name = "poi_village")
public class PoiVillage implements GeoJSONEntity<Integer>, VectorTileEntity<Integer> {

    private Integer id;
    private String city;
    private String district;
    private BigDecimal fuzhi;
    private Long latitude;
    private Long longitude;
    private String name;
    private String province;
    private Object shape;
    private BigDecimal subtype;
    private BigDecimal type;
    private Date dataT;
    private Date updated;

    public PoiVillage() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public BigDecimal getFuzhi() {
        return fuzhi;
    }

    public void setFuzhi(BigDecimal fuzhi) {
        this.fuzhi = fuzhi;
    }

    public Long getLatitude() {
        return latitude;
    }

    public void setLatitude(Long latitude) {
        this.latitude = latitude;
    }

    public Long getLongitude() {
        return longitude;
    }

    public void setLongitude(Long longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public Object getShape() {
        return shape;
    }

    public void setShape(Object shape) {
        this.shape = shape;
    }

    public BigDecimal getSubtype() {
        return subtype;
    }

    public void setSubtype(BigDecimal subtype) {
        this.subtype = subtype;
    }

    public BigDecimal getType() {
        return type;
    }

    public void setType(BigDecimal type) {
        this.type = type;
    }

    public Date getDataT() {
        return dataT;
    }

    public void setDataT(Date dataT) {
        this.dataT = dataT;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }


}
