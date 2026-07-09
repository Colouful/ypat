package com.ypat;

public class ProductQo extends PageQo implements java.io.Serializable {
    private Long id;
    private String name;
    private Integer currval;
    private Integer oldval;
    private String status;
    private String recommended;

    public ProductQo() {
    }

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
