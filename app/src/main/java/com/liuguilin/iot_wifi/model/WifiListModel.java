package com.liuguilin.iot_wifi.model;

/**
 * FileName: WifiListModel
 * Founder: LiuGuiLin
 * Create Date: 2019/2/13 10:53
 * Email: lgl@szokl.com.cn
 * Profile:
 */
public class WifiListModel {

    private String name;
    private boolean state;
    private int level;
    private String ip;
    private String type;

    public boolean isState() {
        return state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

}
