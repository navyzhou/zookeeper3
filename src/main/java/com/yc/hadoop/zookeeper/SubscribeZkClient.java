package com.yc.hadoop.zookeeper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

/**
 * Subscribe:订阅
 * @company 源辰信息
 * @author navy
 */
public class SubscribeZkClient {
	private static final int  CLIENT_QTY = 5; // Work Server数量
	
	//private static final String  ZOOKEEPER_SERVER = "192.168.30.130:2181,192.168.30.131:2181,192.168.30.132:2181";
	private static final String  ZOOKEEPER_SERVER = "192.168.30.130:2181"; // zookeeper服务器的地址和端口
	
	private static final String  CONFIG_PATH = "/config";  // 配置节点
	private static final String  COMMAND_PATH = "/command"; // 命令节点
	private static final String  SERVERS_PATH = "/servers";  // 服务器列表

	
	public static void main(String[] args) throws Exception {
		List<ZkClient>  clients = new ArrayList<ZkClient>(); // 用来存储所有的客户端
		
		List<WorkServer>  workServers = new ArrayList<WorkServer>(); // 用来存放所有的work server
		
		ManageServer manageServer = null;
		try {
			// 创建一个默认的配置 mysql数据库的练级
			ServerConfig initConfig = new ServerConfig();
			initConfig.setDbPwd("a");
			initConfig.setDbUrl("jdbc:mysql://127.0.0.1:3306/yc");
			initConfig.setDbUser("root");

			// 实例化一个Manage Server
			ZkClient clientManage = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new BytesPushThroughSerializer());
			
			manageServer = new ManageServer(SERVERS_PATH, COMMAND_PATH,CONFIG_PATH,clientManage,initConfig);
			manageServer.start(); // 启动Manage Server

			// 创建指定个数的工作服务器
			ZkClient client = null;
			ServerData serverData =null;
			for ( int i = 1; i <= CLIENT_QTY; ++i ) {
				client = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new BytesPushThroughSerializer());
				clients.add(client);
				
				serverData = new ServerData();
				serverData.setId(i);
				serverData.setName("WorkServer#"+i);
				serverData.setAddress("192.168.1."+i);

				WorkServer  workServer = new WorkServer(CONFIG_PATH, SERVERS_PATH, serverData, client, initConfig);
				workServers.add(workServer);
				workServer.start();    // 启动工作服务器

			}

			System.out.println("敲回车键退出！\n");
			new BufferedReader(new InputStreamReader(System.in)).readLine();

			// 在zookeeper客户端上输出命令 create /command list
			// 在zookeeper客户端上输出命令 set /command modify
			// 在zookeeper客户端上输出命令 set /command create
		} finally {
			System.out.println("Shutting down...");
			for ( WorkServer workServer : workServers ) {
				try {
					workServer.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}               
			}
			for ( ZkClient client : clients ) {
				try {
					client.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}    

}
