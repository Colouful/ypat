package com.ypat;

import com.ypat.enums.UserStatus;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

public class OauthQo implements java.io.Serializable {

    private Long userid;
    @NotEmpty(message = "name不能为空")
    private String name;
    @NotEmpty(message = "certcode不能为空")
    private String certcode;
    private List<String> pics;
    private String status;
    private String statusTxt;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCertcode() {
        return certcode;
    }

    public void setCertcode(String certcode) {
        this.certcode = certcode;
    }

    public String getStatusTxt() {
        return UserStatus.getNameByCode(this.status);
    }

    public void setStatusTxt(String statusTxt) {
        this.statusTxt = statusTxt;
    }

    public List<String> getPics() {
        return pics;
    }

    public void setPics(List<String> pics) {
        this.pics = pics;
    }

    @Override
    public String toString() {
        return "OauthQo{" +
                "userid=" + userid +
                ", name='" + name + '\'' +
                ", certcode='" + certcode + '\'' +
                ", status='" + status + '\'' +
                ", statusTxt='" + statusTxt + '\'' +
                '}';
    }
}
