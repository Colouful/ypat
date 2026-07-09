package com.ypat.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "t_product")
@DynamicInsert
@DynamicUpdate
public class Product implements java.io.Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer currval;
    private Integer oldval;
    private String status;
    @Column(length = 1)
    private String recommended;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCurrval() {
        return currval;
    }

    public void setCurrval(Integer currval) {
        this.currval = currval;
    }

    public Integer getOldval() {
        return oldval;
    }

    public void setOldval(Integer oldval) {
        this.oldval = oldval;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecommended() {
        return recommended;
    }

    public void setRecommended(String recommended) {
        this.recommended = recommended;
    }
}
