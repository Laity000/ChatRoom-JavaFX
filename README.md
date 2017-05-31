ChatRoom-JavaFX
===

This is a client/server chatroom gui app by JavaFX, supporting personal and global chat.

使用JavaFX，编写了一个支持点对点和群聊的聊天室软件，分为客户端和服务端。

## 我是图片的搬运工：

![](/screenshots/loginUI.png "LoginUI")

![](/screenshots/chatUI.png "ChatUI")

![](/screenshots/emojiselectorUI.png "EmojiSelectorUI")



## Done & ~~To-Do~~:
* 基于TCP的socket通信——>~~基于WebSocket的通信~~
* java编写实现本地后端——>~~基于workerman的远程后端服务器~~
* 使用Gson封装通信消息，便于扩展
* 实现对javafx的stage控制器，并自定义stage基类
* 支持动态更新在线用户列表
* 支持私聊和群聊，通过用户列表切换
* 支持显示用户头像（固定/~~上传~~）
* 支持Emoji表情，实现Emoji选择器
* ~~支持图片或文件传输~~

## 引用：
* 我的设计思路：http://blog.csdn.net/u013480581/article/details/72355116
* 界面参考：https://github.com/DomHeal/JavaFX-Chat

