package com.chatroom.chatserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import com.chatroom.messages.Message;
import com.chatroom.messages.MessageType;
import com.chatroom.messages.UserInfo;
import com.chatroom.messages.Utils;

/**
 *
 * @Title: Server.java
 * @Description: TODO 后端服务器程序
 * @author ZhangJing   https://github.com/Laity000/ChatRoom-JavaFX
 * @date 2017年5月17日 上午11:21:38
 *
 */
public class Server {
	private static final Logger logger = LoggerFactory.getLogger(Server.class);

	//服务端端口号
	private static final int SERVER_PORT =12345;
	//Gson
	private static Gson gson = new Gson();
	//用户名与客户端对象的映射
	private static HashMap<String, Socket> socketsfromUserNames = new HashMap<>();
	//用户信息集合

	private static LinkedList<UserInfo> userInfoList = new LinkedList<>();

	public static void main(String[] args) throws IOException{
		logger.info("服务器启动..");

		ServerSocket ss = new ServerSocket(SERVER_PORT);
		try {
			while(true)
			{

				// 此行代码会阻塞，将一直等待别人的连接
				Socket s = ss.accept();
				//socketList.add(s);
				// 每当客户端连接后启动一条ServerThread线程为该客户端服务
				new Thread(new ServerThread(s)).start();
			}
		}catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        //listener.close();
	    }
	}

	public static class ServerThread implements Runnable
	{

		// 定义当前线程所处理的Socket
		private Socket s = null;
		// 该线程所处理的Socket所对应的输入流
		private BufferedReader br = null;
		private InputStream in = null;

		/**
		*
		* @param s
		* @throws IOException
		*/
		public ServerThread(Socket s)throws IOException{

			this.s = s;
			// 初始化该Socket对应的输入流
			br = new BufferedReader(new InputStreamReader(
				s.getInputStream(), "utf-8"));
			in = s.getInputStream();
		}

		public void run()
		{
			try
			{
				if(!s.isClosed()){
					//System.out.println(s + "用户已连接服务器！下一步将判断是否能登录成功..");
					logger.info("{} 端口已连接服务器！下一步将判断用户是否能登录成功..",s);
					//socketList.add(s);
				}

				while (s.isConnected()) {
					//读取来自客户端的消息
					String revString = br.readLine();
					if(revString != null){
						Message message = gson.fromJson(revString, Message.class);
						//System.out.println(revString);
						logger.info("服务器收到数据..");
						logger.debug("数据内容:{}",revString);
						logger.info("对数据进行解析并做响应中..");

						//对客户端的消息进行分析并作出相应的动作
						switch (message.getType()) {
						case CONNECT:
							checkConnect(message);
							break;
						case DISCONNECT:
							closeConnect(message);
							break;
						case MSG:
							sendMSG(message);
							break;
						case QUERY:
							sendUserInfoList(false);
							break;
						default:
							break;
						}

					}
					/*
					for (Socket s: Server.socketList)
					{
						PrintStream ps = new PrintStream(s.getOutputStream());
						ps.println(revString);
					}
					*/
				}
			}
			catch (IOException e)
			{
				//System.out.println("捕捉到异常：" + s);
				logger.error("捕捉到异常：{}!!",s);
				e.printStackTrace();
			}

		}
		/**
		 * 将message转化为字符串后发送指定的客户端
		 * @param message
		 * @param socket
		 */
		private void send(Message message,Socket socket) throws IOException{
			//转化为gson的字符串
			String messagesString = gson.toJson(message);
			//System.out.println(messagesString);
			logger.info("服务器发送单播数据..");
			logger.debug("数据内容:{}", messagesString);
			PrintStream ps = new PrintStream(socket.getOutputStream());
			//发送消息，注意消息格式为(messageString + "\n")
			ps.println(messagesString);
			logger.info("数据发送完成!");
		}
		/**
		 * 将message转化为字符串后发送给所有的客户端
		 * @param message
		 */
		private void sendAll(Message message, boolean isRemoveLocalUser) throws IOException{
			//转化为gson的字符串
			String messagesString = gson.toJson(message);
			//System.out.println(messagesString);
			logger.info("服务器发送广播数据..");
			logger.debug("数据内容:{}", messagesString);
			PrintStream ps = null;
			if(isRemoveLocalUser){
				for(HashMap.Entry<String, Socket> entry : socketsfromUserNames.entrySet()) {
					if(!entry.getKey().equals(message.getFrom())){
						ps = new PrintStream(entry.getValue().getOutputStream());
						//发送消息，注意消息格式为(messageString + "\n")
						ps.println(messagesString);
					}
				}
			}else {
				for(Socket socket : socketsfromUserNames.values()){
					ps = new PrintStream(socket.getOutputStream());
					//发送消息，注意消息格式为(messageString + "\n")
					ps.println(messagesString);
				}
			}
			logger.info("数据发送完成!");
		}
		/**
		 * 检查是否登录成功，并发送登录结果
		 * 登陆成功后需要向所有用户发送新用户集列表和新用户上线通知
		 * @param message
		 * @throws IOException
		 */
		private void checkConnect(Message message) throws IOException{


			String username = message.getFrom();
			logger.info("用户[{}]登录分析中..", username);
			//监测用户名是否已存在
			if(!socketsfromUserNames.containsKey(username)){

				socketsfromUserNames.put(username, s);
				userInfoList.addAll(message.getUserlist());
				/*
				for(UserInfo user : message.getUserlist()){
					userInfoList.add(user);
				}
				*/
				logger.info("用户[{}]登录成功！", username);
				//创建连接成功反馈信息
				Message sResult = new Message();
				sResult.setType(MessageType.SUCCESS);
				sResult.setContent(username + " connect successful!");
				//发送
				send(sResult, s);
				//创建用户集反馈信息
				sendUserInfoList(true);
				//发送新用户上线通知
				sendNotification(true, username +" online..");
			}else {
				logger.info("用户[{}]登录失败！", username);
				//创建连接失败反馈消息
				Message fResult = new Message();
				fResult.setType(MessageType.FAIL);
				fResult.setContent(message.getFrom() + " is existed, connect failed!");
				//发送
				send(fResult, s);
			}
		}

		/**
		 * 关闭连接，主要把将要关闭的客户端对象及其信息删除
		 * 并发送用户下线通知
		 * @param message
		 * @throws IOException
		 */
		private void closeConnect(Message message) throws IOException{
			String userName = message.getFrom();
			logger.info("用户[{}]准备下线中..", userName);
			Socket socket = socketsfromUserNames.get(userName);
			if(socket != null){
				socketsfromUserNames.remove(userName);
				for(UserInfo user : userInfoList){
					if(user.getUsername().equals(userName)){
						userInfoList.remove(user);
						break;//注意缺少break会出bug，因为remove后size的值改变了，遍历中ArrayList不可变。使用Iterator的方式也可以顺利删除和遍历。
					}
				}
				//userInfoList.remove(username);//错误
			}
			System.out.println(socketsfromUserNames);
			System.out.println(userInfoList);
			logger.info("用户[{}]下线成功！并更新用户集..", userName);
			//创建用户集反馈信息
			sendUserInfoList(true);
			//发送用户下线通知
			sendNotification(true, userName + " offline..");
		}
		/**
		 * 发送在线用户信息集
		 * @param isAllUsers
		 * @throws IOException
		 */
		private void sendUserInfoList(boolean isAllUsers) throws IOException{
			//无在线用户则不发送
			if(userInfoList.isEmpty()){
				return;
			}
			//创建用户集反馈信息
			Message uResult = new Message();
			uResult.setType(MessageType.USERLIST);
			uResult.setUserlist(userInfoList);
			if(isAllUsers){
				logger.info("向全体用户发送用户集更新消息..");
				//发送给所有用户
				sendAll(uResult, false);
			}else {
				logger.info("向当前用户发送用户集更新消息..");
				//注意将该消息发给所有用户，以便动态更新用户集数据
				send(uResult,s);
			}
			//logger.info("用户集更新成功！");
		}
		/**
		 * 发送通知消息，用于通知全体用户或当前用户
		 * @param isAllUsers
		 * @param notice
		 * @throws IOException
		 */
		private void sendNotification(boolean isAllUsers, String notice) throws IOException {
			Message message = new Message();
			message.setType(MessageType.NOTIFICATION);
			message.setContent(notice);
			if(isAllUsers){
				logger.info("向全体用户发送通知消息..");

				sendAll(message, false);
			}else {
				logger.info("向当前用户发送通知消息..");
				send(message, s);
			}
		}

		/**
		 * 对收到的聊天消息进行转发，分为单播和广播两种方式处理
		 * @param message
		 * @throws IOException
		 */
		private void sendMSG(Message message) throws IOException{
			String userName = message.getFrom();
			String toUser = message.getTo();
			if(toUser.equals(Utils.ALL)){
				//发送时需要排除自己
				logger.info("用户[{}]正在全局聊天，服务器广播转发..", userName);
				sendAll(message, true);
			}else {
				logger.info("用户[{}]正在个人聊天，服务器单播转发..", userName);
				if(socketsfromUserNames.get(toUser) != null){
					send(message, socketsfromUserNames.get(toUser));
				}else {
					logger.info("用户[{}]发送个人聊天消息失败,接受方[{}]不存在！", userName, toUser);
					sendNotification(false, "The selected user does not exist!");
				}
			}
		}
	}
}
