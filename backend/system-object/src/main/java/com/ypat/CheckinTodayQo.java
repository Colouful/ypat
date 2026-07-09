package com.ypat;

import java.io.Serializable;

public class CheckinTodayQo implements Serializable {
    private Boolean enabled;
    private Boolean checkedIn;
    private Integer rewardPpd;
    private String confirmTitle;
    private String confirmContent;
    private String checkinDate;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Boolean getCheckedIn() { return checkedIn; }
    public void setCheckedIn(Boolean checkedIn) { this.checkedIn = checkedIn; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public String getConfirmTitle() { return confirmTitle; }
    public void setConfirmTitle(String confirmTitle) { this.confirmTitle = confirmTitle; }
    public String getConfirmContent() { return confirmContent; }
    public void setConfirmContent(String confirmContent) { this.confirmContent = confirmContent; }
    public String getCheckinDate() { return checkinDate; }
    public void setCheckinDate(String checkinDate) { this.checkinDate = checkinDate; }
}
