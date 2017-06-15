package com.chatroom.communication;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chatroom.client.Main;
import com.chatroom.messages.Message;
import com.google.gson.Gson;

/**
 *
 * @Title: WebsocketComm.java
 * @Description: TODO 基于websocket协议的通信线程
 * @author ZhangJing  https://github.com/Laity000/ChatRoom-JavaFX
 * @date 2017年6月15日 上午11:45:22
 *
 */
public class WebsocketComm extends Comm implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(WebsocketComm.class);

	// Java-WebSocket
	private WebSocketClient wsc;
	// url
	private String url;
	// Gson
	private Gson gson = new Gson();

	/**
	 * 构造函数
	 *
	 * @param hostName
	 * @param port
	 * @param userName
	 * @param userPic
	 */
	public WebsocketComm(String hostName, int port, String userName, String userPic) {
		super(hostName, port, userName, userPic);
		// TODO Auto-generated constructor stub
		url = "ws://" + hostName + ":" + port;
	}

	@Override
	/**
	 * 将message转化为字符串后发送 基于websocket协议
	 */
	void send(Message message) {
		// TODO Auto-generated method stub
		// 转化为gson的字符串
		String messagesString = gson.toJson(message);
		logger.debug("发送的消息内容：{}", messagesString);
		// 发送消息
		wsc.send(messagesString);
	}

	@Override
	/**
	 * 线程
	 */
	public void run() {
		// TODO Auto-generated method stub
		// websocket初始化
		try {
			wsc = new WebSocketClient(new URI(url)) {

				@Override
				public void onOpen(ServerHandshake handshake) {
					// TODO Auto-generated method stub
					logger.info("本机已成功连接{}服务器！下一步用户登录..", getURI());

				}

				@Override
				public void onMessage(String revString) {
					// TODO Auto-generated method stub
					// 读取来自客户端的消息
					if (revString != null) {
						logger.info("收到服务器消息..");
						logger.debug("数据内容:{}", revString);
						logger.info("对数据进行解析并做响应中..");
						Message message = gson.fromJson(revString, Message.class);
						// TODO:对服务器的消息进行解析
						switch (message.getType()) {
						case SUCCESS:
							// 登录成功后进入ChatUI界面，需要username和pic，并且需要更新用户集(在USERLIST消息中处理)。
							logger.info("[{}]用户登录成功！", userName);
							// 切换到ChatUI界面
							loginController.changeStage(Main.ChatUIID);
							break;
						case FAIL:
							// 登录失败，直接在loginIU上显示失败的原因
							loginController.setResultText(message.getContent());
							logger.info("[{}]登录失败(重名)！", userName);
							break;
						case MSG:
							logger.info("收到聊天对话消息");
							// ChatUI，分为单播和广播两种方式处理
							chatController.addOtherMessges(message);
							break;
						case USERLIST:
							logger.info("收到在线用户集消息");
							// 在ChatUI中更新用户集并计算用户数量
							chatController.setUserList(message.getUserlist());
							break;
						case NOTIFICATION:
							logger.info("收到通知消息");
							// 通知消息，目前只有新用户上线通知、用户下线通知
							chatController.addNotification(message.getContent());
						default:
							break;
						}
					}
				}

				@Override
				public void onError(Exception ex) {
					// TODO Auto-generated method stub
					ex.printStackTrace();
					logger.info("{} 连接错误！", getURI());
					loginController.setResultText(getURI() + "连接错误！");
				}

				@Override
				public void onClose(int arg0, String arg1, boolean arg2) {
					// TODO Auto-generated method stub

				}
			};
			wsc.connect();
			Thread.sleep(500);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("{} 连接错误！{} 这不是有效的url！", wsc.getURI(), url);
			loginController.setResultText(url + "这不是有效的url！");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("{} 连接错误！", wsc.getURI());
			loginController.setResultText(wsc.getURI() + "连接错误！");
		}

		// 登录请求
		connect();
	}

	@Override
	public void destroy() {
		wsc.close();
		logger.info("物理连接断开成功！");
	}

}
