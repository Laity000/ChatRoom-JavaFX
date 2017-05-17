package com.chatroom.messages;

import java.util.LinkedList;

/**
 *
 * @Title: Message.java
 * @Description: TODO 封装通讯消息
 * @author ZhangJing   https://github.com/Laity000/ChatRoom-JavaFX
 * @date 2017年5月17日 上午11:22:16
 *
 */
public class Message {
	//消息类型
	private MessageType type;
	//用户集
	LinkedList<UserInfo> userlist;
	//消息来源
	private String from;
	//消息对象
	private String to;
	//消息内容
	private String content;

	public MessageType getType() {
		return type;
	}
	public void setType(MessageType type) {
		this.type = type;
	}
	public LinkedList<UserInfo> getUserlist() {
		return userlist;
	}
	public void setUserlist(LinkedList<UserInfo> userlist) {
		this.userlist = userlist;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}




}
