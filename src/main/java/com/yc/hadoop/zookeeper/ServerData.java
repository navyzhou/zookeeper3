package com.yc.hadoop.zookeeper;

/**
 * WorkServer服务器基本信息
 * @company 源辰信息
 * @author navy
 */
public class ServerData {
	private String address;
    private Integer id;
    private String name;

    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "ServerData [address=" + address + ", id=" + id + ", name="
                + name + "]";
    }
}
