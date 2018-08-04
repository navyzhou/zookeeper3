package com.yc.hadoop.zookeeper;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import com.google.gson.Gson;

/**
 * 工作服务器
 * @company 源辰信息
 * @author navy
 */
public class WorkServer {
	// ZooKeeper 客户端
	private ZkClient zkClient;

	// config文件路径
	private String configPath;

	// ZooKeeper集群中servers节点的路径
	private String serversPath;

	// 当前工作服务器的基本信息
	private ServerData serverData;

	// 当前工作服务器的配置信息
	private ServerConfig serverConfig;

	// 数据监听器
	private IZkDataListener dataListener;

	/**
	 * 
	 * @param configPath 代表config节点的路径
	 * @param serversPath 代表servers节点的路径
	 * @param serverData   代表当前服务器的基本信息
	 * @param zkClient     底层与zookeeper集群通信的组件
	 * @param initconfig   当前服务器的初始配置
	 */
	public WorkServer(String configPath, String serversPath, ServerData serverData, ZkClient zkClient, ServerConfig initConfig) {
		this.zkClient = zkClient;
		this.serversPath = serversPath;
		this.configPath = configPath;
		this.serverConfig = initConfig;
		this.serverData = serverData;

		this.dataListener = new IZkDataListener() { // 用于监听config节点的数据变化

			public void handleDataDeleted(String dataPath) throws Exception {

			}

			/**
			 * 当数据的值改变时处理的
			 * 这个data是将ServerConfig对象转成json字符串存入
			 * 可以通过参数中的 data 拿到当前数据节点最新的配置信息
			 * 拿到这个data信息后将它反序列化成ServerConfig对象，然后更新到自己的serverconfig属性中
			 */
			public void handleDataChange(String dataPath, Object data) throws Exception {
				String retJson = new String((byte[])data);
				Gson gson = new Gson();
				ServerConfig serverConfigLocal = (ServerConfig) gson.fromJson(retJson,ServerConfig.class);
				updateConfig(serverConfigLocal); // 更新配置
				System.out.println("新的工作服务器配置为 :"+serverConfig.toString());
			}
		};
	}
	
	/**
	 *  启动服务器
	 */
	public void start() {
		System.out.println("服务器启动...");
		initRunning();
	}

	/**
	 *  服务器初始化
	 */
	private void initRunning() {
		registMeToZooKeeper(); // 注册自己到ZooKeeper
		zkClient.subscribeDataChanges(configPath, dataListener); // 订阅config节点的改变事件
	}
	

	/**
	 * 启动时向zookeeper注册自己的注册信息
	 */
	private void registMeToZooKeeper() {
		// 向zookeeper中注册自己的过程,其实就是向servers节点下注册一个临时节点
		String mePath = serversPath.concat("/").concat(serverData.getAddress());  // 构造临时节点

		try {
			Gson gson = new Gson();
			zkClient.createEphemeral(mePath, gson.toJson(serverData).getBytes()); 
		} catch (ZkNoNodeException e) { // 如果节点不存在，则先创建节点
			zkClient.createPersistent(serversPath, true);
			registMeToZooKeeper();
		}
	}

	/**
	 *  停止服务器
	 */
	public void stop() {
		System.out.println("服务器关闭...");
		zkClient.unsubscribeDataChanges(configPath, dataListener); // 取消监听config节点
	}

	/**
	 * 更新自己的配置信息
	 * @param serverConfig
	 */
	private void updateConfig(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
}
