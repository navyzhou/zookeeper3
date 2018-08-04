package com.yc.hadoop.zookeeper;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import com.google.gson.Gson;

/**
 * 管理服务器
 * 1. 监听servers的子节点的变化，如果发生改变，则更新工作列表
 * 2. 监听command节点的内容，如果数据发现改变，则读取command节点中的数据，然后执行命令
 * @company 源辰信息
 * @author navy
 */
public class ManageServer {
	// zookeeper的servers节点路径
	private String serversPath;

	// zookeeper的command节点路径
	private String commandPath;

	// zookeeper的config节点路径
	private String configPath;

	private ZkClient zkClient;

	private ServerConfig config;

	// 用于监听zookeeper中servers节点的子节点列表的变化
	private IZkChildListener childListener;

	// 用于监听zookeeper中command节点数据内容的变化
	private IZkDataListener dataListener;

	// 工作服务器的列表
	private List<String> workServerList;

	/**
	 * 
	 * @param serversPath
	 * @param commandPath zookeeper中存放命令的节点路径
	 * @param configPath
	 * @param zkClient
	 * @param config
	 */
	public ManageServer(String serversPath, String commandPath,	String configPath, ZkClient zkClient, ServerConfig config) {
		this.serversPath = serversPath;
		this.commandPath = commandPath;
		this.zkClient = zkClient;
		this.config = config;
		this.configPath = configPath;
		
		this.childListener = new IZkChildListener() { // 用于监听zookeeper中servers节点的子节列表的变化
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				workServerList = currentChilds; // 更新服务器列表
				System.out.println("工作服务器列表已更改，新列表为： ");
				execList();
			}
		};

		this.dataListener = new IZkDataListener() { // 用于监听zookeeper中command节点中数据的变化
			public void handleDataDeleted(String dataPath) throws Exception { // 删除数据时触发

			}

			public void handleDataChange(String dataPath, Object data) throws Exception { // 数据被修改时触发
				String cmd = new String((byte[]) data);
				System.out.println("执行为："+cmd);
				exeCmd(cmd); // 执行命令
			}
		};
	}

	/**
	 *  启动工作服务器
	 */
	public void start() {
		initRunning(); // 初始化服务器
	}

	/**
	 * 初始化
	 */
	private void initRunning() { 
		// 执行订阅command节点数据变化和servers节点的列表变化
		zkClient.subscribeDataChanges(commandPath, dataListener);
		zkClient.subscribeChildChanges(serversPath, childListener);
	}

	/**
	 * 停止工作服务器
	 */
	public void stop() {
		// 取消订阅command节点数据变化和servers节点的列表变化
		zkClient.unsubscribeChildChanges(serversPath, childListener);
		zkClient.unsubscribeDataChanges(commandPath, dataListener);
	}

	/**
	 * 执行控制命令的函数
	 * @param cmdType 要执行的命令
	 */
	private void exeCmd(String cmdType) {
		if ("list".equals(cmdType)) { // 
			execList();
		} else if ("create".equals(cmdType)) {
			execCreate();
		} else if ("modify".equals(cmdType)) {
			execModify();
		} else {
			System.out.println("错误命令：" + cmdType);
		}

	}

	/**
	 *  列出工作服务器列表
	 */
	private void execList() {
		System.out.println(workServerList.toString());
	}

	/**
	 *  创建config节点
	 */
	private void execCreate() {
		if (!zkClient.exists(configPath)) { // 如果节点不存在
			Gson gson = new Gson();
			try {
				zkClient.createPersistent(configPath, gson.toJson(config).getBytes()); // 创建永久节点
			} catch (ZkNodeExistsException e) { // 如果节点已经存在，则直接写入数据
				zkClient.writeData(configPath, gson.toJson(config).getBytes()); 
			} catch (ZkNoNodeException e) { // 如果其中的一个节点的父节点没有被创建，则先创建其父节点
				String parentDir = configPath.substring(0, configPath.lastIndexOf('/'));
				zkClient.createPersistent(parentDir, true);
				execCreate();
			}
		}
	}

	/**
	 *  修改config节点内容
	 */
	private void execModify() {
		// 我们随意修改config的一个属性就可以了
		config.setDbUser(config.getDbUser() + "_modify");
		try {
			Gson gson = new Gson();
			// 写入zookeeper中
			zkClient.writeData(configPath, gson.toJson(config).getBytes());
		} catch (ZkNoNodeException e) {
			execCreate(); // 如果节点不存在，则创建它
		}
	}
}
