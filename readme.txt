Zookeeper的数据发布与订阅模式

1、用于记录WorkServer(工作服务器)的基本信息类对象 ServerData

2、用于记录Work Server(工作服务器)的配置信息类对象ServerConfig

3、管理服务器 ManagerServer
	(1)监听servers的子节点的变化，如果发生改变，则更新工作列表
	(2)监听command节点的内容，如果数据发现改变，则读取command节点中的数据，然后执行命令
	
4、工作服务器  WorkServer
	每个work Server节点在启动时都会在Servers节点下创建一个临时节点，
	Manager Server充当mointor(监视器)，通过监视Servers节点下子节点列表的变化，
	来更新自己工作内存中，工作服务器列表信息

发布订阅架构图
	ZooKeeper
		Config：用于配置管理
				Manager Server节点通过Config节点下发配置信息，WorkServer可以通过订阅Config节点的改变，来更新自己的配置
		
		Servers：用于服务发现。
				每个Work Server节点在启动时都会在Servers节点下创建一个临时节点，Manager Server充当mointor(监视器)，
				通过监视Servers节点下子节点列表的变化，来更新自己工作内存中，工作服务器列表信息
			Work Server1
			Work Server2
			Work Server3
		
		command：作为命令传递的中介
			Control Server通过使用Command节点来作为中介向Manager Server发送控制指令。
			Control Server向Command节点写入信息，Manager Server订阅command节点的数据改变，来监听并执行命令