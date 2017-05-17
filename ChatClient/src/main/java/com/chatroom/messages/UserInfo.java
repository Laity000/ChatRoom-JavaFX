package com.chatroom.messages;

//用户个人信息定义
public class UserInfo {

	public UserInfo() {
		super();
	}

	public UserInfo(String username, String userpic) {
		super();
		this.username = username;
		this.userpic = userpic;
	}

	//用户名
	private String username;
	//用户头像
	private String userpic;

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUserpic() {
		return userpic;
	}
	public void setUserpic(String userpic) {
		this.userpic = userpic;
	}

}
