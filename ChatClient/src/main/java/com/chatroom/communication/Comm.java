package com.chatroom.communication;

import java.io.IOException;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chatroom.client.ChatController;
import com.chatroom.client.LoginController;
import com.chatroom.messages.Message;
import com.chatroom.messages.MessageType;
import com.chatroom.messages.UserInfo;

/**
 *
 * @Title: Comm.java
 * @Description: TODO 通信基类
 * @author ZhangJing https://github.com/Laity000/ChatRoom-JavaFX
 * @date 2017年6月15日 上午11:30:59
 *
 */
public abstract class Comm implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Comm.class);
	// 各UI控制器对象
	protected LoginController loginController = LoginController.getInstance();
	protected ChatController chatController = ChatController.getInstance();

	// 用户名
	protected String userName;
	// 用户头像名
	protected String userPic;
	// 主机地址
	protected String hostName;
	// 端口号
	protected int port;

	/**
	 * 构造函数
	 *
	 * @param hostName
	 * @param port
	 * @param userName
	 * @param userPic
	 */
	public Comm(String hostName, int port, String userName, String userPic) {
		this.userName = userName;
		this.userPic = userPic;
		this.hostName = hostName;
		this.port = port;
		// 将通信对象注册到chat控制器中
		chatController.setConnection(this);
	}

	public String getUserName() {
		return userName;
	}

	public String getUserPic() {
		return userPic;
	}

	// 发送，由子类具体实现
	abstract void send(Message message);

	// 断开物理连接，由子类具体实现
	public void destroy() {
		logger.error("destroy()函数应由子类具体实现");
	}

	/**
	 * 用户连接请求类型消息
	 */
	public void connect() {
		// 创建个人信息
		UserInfo user = new UserInfo(userName, userPic);
		LinkedList<UserInfo> usertemp = new LinkedList<>();
		usertemp.add(user);
		// 创新连接消息
		Message message = new Message();
		message.setType(MessageType.CONNECT);
		message.setUserlist(usertemp);
		message.setFrom(userName);
		logger.info("向服务器发送用户登录请求..");
		// 发送
		send(message);
	}

	/**
	 * 用户注销请求类型消息
	 */
	public void disconnect() {
		Message message = new Message();
		message.setType(MessageType.DISCONNECT);
		message.setFrom(userName);
		logger.info("向服务器发送用户注销通知..");
		// 发送
		send(message);
	}

	/**
	 * 对话类型消息
	 *
	 * @param from
	 * @param to
	 * @param content
	 */
	public void sendMsg(String from, String to, String content) {
		// 创建Msg消息
		Message message = new Message();
		message.setType(MessageType.MSG);
		message.setFrom(from);
		message.setTo(to);
		message.setContent(content);
		logger.info("向服务器发送聊天消息..");
		// 发送
		send(message);
	}

	/**
	 * 查询用户集类型消息
	 */
	public void queryUserList() {
		Message message = new Message();
		message.setType(MessageType.QUERY);
		message.setTo(userName);
		logger.info("向服务器发送更新在线用户集请求..");
		// 发送
		send(message);
	}
}
