package com.chatroom.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.chatroom.messages.Message;
import com.chatroom.messages.MessageType;
import com.chatroom.messages.UserInfo;

/**
 *
 * @Title: ClientThread.java
 * @Description: TODO 通讯线程
 * @author ZhangJing   https://github.com/Laity000/ChatRoom-JavaFX
 * @date 2017年5月17日 上午11:18:37
 *
 */
public class ClientThread implements Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(ClientThread.class);
	//各UI控制器对象
	private LoginController loginController = LoginController.getInstance();
	private ChatController chatController = ChatController.getInstance();

	//用户名
	private String userName;

	//用户头像名
	private String userPic;
	//主机地址
	private String hostName;
	//端口号
	private int port;

	//套接字
	private Socket s;
	// 该线程所处理的Socket所对应的输入流
	private BufferedReader br = null;
	// 该线程所处理的Socket所对应的输出流
	private PrintStream ps = null;

	//Gson
	private Gson gson = new Gson();

	/**
	 * 设备连接构造函数
	 * @param context
	 * @param handler
	 * @param iPString IP地址
	 * @param portString 端口号
	 */
	public ClientThread(String hostname, int port, String username, String userpic)
	{
		this.hostName = hostname;
		this.port = port;
		this.userName = username;
		this.userPic = userpic;
		//将本线程对象赋给ChatController控制器
		chatController.setClientThread(this);
	}

	public String getUserName() {
		return userName;
	}

	public String getUserPic() {
		return userPic;
	}

	public void run() {
		// TODO Auto-generated method stub
		try
		{
			//Thread.sleep(1000);//线程暂停一秒
			s = new Socket();
			SocketAddress endpoint = new InetSocketAddress(hostName , port);
			//设置连接超时时间
			s.connect(endpoint, 5*1000);
			br = new BufferedReader(new InputStreamReader(s.getInputStream(), "utf-8"));
			ps = new PrintStream(s.getOutputStream());

			//输出连接信息
			if(s.isConnected()){
				logger.info("{} 本机已成功连接服务器！下一步用户登录..",s);
				//System.out.println("Client connected！The next step is to determine whether the login is successful..");
			}
			//连接指令
			connect();
			while (s.isConnected()) {
				//读取来自客户端的消息
				String revString = br.readLine();
				if (revString != null) {
					logger.info("收到服务器消息..");
					logger.debug("数据内容:{}",revString);
					logger.info("对数据进行解析并做响应中..");
					Message message = gson.fromJson(revString, Message.class);
					//TODO:对服务器的消息进行解析
					switch (message.getType()) {
					case SUCCESS:
						//登录成功后进入ChatUI界面，需要username和pic，并且需要更新用户集(在USERLIST消息中处理)。
						logger.info("[{}]用户登录成功！", userName);
						//切换到ChatUI界面
						loginController.changeStage(Main.ChatUIID);
						//更新Chat界面信息，如用户头像、用户名。在chatController的init()中完成，下面的方法不需要了
						//chatController.setUsernameLabel(userName);
						//chatController.setUserImgView(userPic);

						break;
					case FAIL:
						//登录失败，直接在loginIU上显示失败的原因
						loginController.setResultText(message.getContent());
						logger.info("[{}]登录失败(重名)！", userName);
						break;
					case MSG:
						logger.info("收到聊天对话消息");
						//ChatUI，分为单播和广播两种方式处理
						chatController.addOtherMessges(message);
						break;
					case USERLIST:
						logger.info("收到在线用户集消息");
						//在ChatUI中更新用户集并计算用户数量
						chatController.setUserList(message.getUserlist());
						break;
					case NOTIFICATION:
						logger.info("收到通知消息");
						//通知消息，目前只有新用户上线通知、用户下线通知
						chatController.addNotification(message.getContent());

					default:
						break;
					}

				}

			}

		}
		catch(SocketTimeoutException ex)
		{
			ex.printStackTrace();
			logger.info("{} 连接超时！",s);
			loginController.setResultText(s+"连接超时！");
		}
		catch (Exception e)
		{
			e.printStackTrace();

			logger.info("{} 连接错误！",s);
			loginController.setResultText(s+"连接错误！");

		}
	}
	/**
	 * 将message转化为字符串后发送
	 * @param message
	 */
	public void send(Message message) throws IOException{
		//转化为gson的字符串
		String messagesString = gson.toJson(message);
		logger.debug("发送的消息内容：{}",messagesString);
		//发送消息，注意消息格式为(messageString + "\n")
		ps.println(messagesString);
	}

	/**
	 * 用户连接请求类型消息
	 * @throws IOException
	 */
	public void connect() throws IOException{
		//创建个人信息
		UserInfo user = new UserInfo(userName,userPic);
		LinkedList<UserInfo> usertemp = new LinkedList<>();
		usertemp.add(user);
		//创新连接消息
		Message message = new Message();
		message.setType(MessageType.CONNECT);
		message.setUserlist(usertemp);
		message.setFrom(userName);
		logger.info("向服务器发送用户登录请求..");
		//发送
		send(message);
	}
	/**
	 * 用户注销请求类型消息
	 * @throws IOException
	 */
	public void disconnect() throws IOException{
		Message message = new Message();
		message.setType(MessageType.DISCONNECT);
		message.setFrom(userName);
		logger.info("向服务器发送用户注销通知..");
		//发送
		send(message);
	}

	/**
	 * 断开物理连接
	 * @throws IOException
	 */
	public void destroy() {
		logger.debug("正在断开物理连接..");
		new Thread(){
			@Override
			public void run() {
				try {
					if(br != null){
						br.close();
						br = null;
					}
					if(ps != null){
						ps.close();
						ps = null;
					}
				} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			}
		}.start();
		logger.debug("物理连接断开成功！");
	}



	/**
	 * 对话类型消息
	 * @param from
	 * @param to
	 * @param content
	 * @throws IOException
	 */
	public void sendMsg(String from, String to, String content) throws IOException{
		//创建Msg消息
		Message message = new Message();
		message.setType(MessageType.MSG);
		message.setFrom(from);
		message.setTo(to);
		message.setContent(content);
		logger.info("向服务器发送聊天消息..");
		//发送
		send(message);
	}
	/**
	 * 查询用户集类型消息
	 * @throws IOException
	 */
	public void queryUserList() throws IOException{
		Message message = new Message();
		message.setType(MessageType.QUERY);
		message.setTo(userName);
		logger.info("向服务器发送更新在线用户集请求..");
		//发送
		send(message);
	}



}
