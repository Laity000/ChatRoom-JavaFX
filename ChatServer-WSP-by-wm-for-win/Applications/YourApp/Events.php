<?php
/**
 * This file is part of workerman.
 *
 * Licensed under The MIT License
 * For full copyright and license information, please see the MIT-LICENSE.txt
 * Redistributions of files must retain the above copyright notice.
 *
 * @author walkor<walkor@workerman.net>
 * @copyright walkor<walkor@workerman.net>
 * @link http://www.workerman.net/
 * @license http://www.opensource.org/licenses/mit-license.php MIT License
 */

/**
 * 用于检测业务代码死循环或者长时间阻塞等问题
 * 如果发现业务卡死，可以将下面declare打开（去掉//注释），并执行php start.php reload
 * 然后观察一段时间workerman.log看是否有process_timeout异常
 */
//declare(ticks=1);

use \GatewayWorker\Lib\Gateway;

/**
 * 主逻辑
 * @Title: Events.php
 * @Description: TODO 基于websocket协议的聊天室后端
 * @author ZhangJing   https://github.com/Laity000/ChatRoom-JavaFX
 * @date 2017/6/15
 */
class Events
{
  const ALL = "★☆";
    /**
     * 当客户端连接时触发
     * 如果业务不需此回调可以删除onConnect
     * 
     * @param int $client_id 连接id
     */ 
    public static function onConnect($client_id)
    {
      //info
      echo "client:{".$client_id."} connecting...\n";
    }
    
   /**
    * 当客户端发来消息时触发
    * @param int $client_id 连接id
    * @param mixed $message 具体消息
    */
   public static function onMessage($client_id, $message)
   {
      // debug
      //echo "client:{$_SERVER['REMOTE_ADDR']}:{$_SERVER['REMOTE_PORT']} gateway:{$_SERVER['GATEWAY_ADDR']}:{$_SERVER['GATEWAY_PORT']}  client_id:$client_id session:".json_encode($_SESSION)."\n";
      echo "received content:".$message."\n";
      // 客户端传递的是json数据
      $message_data = json_decode($message, true);
      if(!$message_data)
      {
        return;
      }
      //debug
      //echo '$message_data:';var_dump($message_data);
      //对客户端的消息进行分析并作出相应的动作
      switch ($message_data['type']) 
      {
        case 'CONNECT':
          self::checkConnect($client_id, $message_data);
          break;
        case 'DISCONNECT':
          if(Gateway::isOnline($client_id)){
            Gateway::closeClient($client_id);
          }
          break;
        case 'MSG':
          self::sendMSG($client_id, $message_data);
          break;
        case 'QUERY':
          //self::sendNotification(false);
          break;
        // 客户端回应服务端的心跳
        case 'pong':
          echo "client:{".$client_id."}:pong..\n";
          break;
        default:
          # code...
          break;
      }

    }  
   /**
    * 当用户断开连接时触发
    * @param int $client_id 连接id
    */
   public static function onClose($client_id)
   {
      $room_id = $_SESSION['room_id'];
      $user_name = $_SESSION['user_name'];
      //info
      echo "user{".$user_name."}:logout...\n";
      //创建用户集反馈信息
      self::sendUsersInfoList(true, $room_id);
      //发送新用户下线通知
      self::sendNotification(true, $room_id, $user_name." offline..");
       //最后：离开房间
      Gateway::leaveGroup($client_id, $room_id);

   }

   /**
   * 检查是否登录成功，并发送登录结果
   * 登陆成功后需要向所有用户发送新用户集列表和新用户上线通知
   * @param int $client_id
   * @param string $message_data
   * @return bool
   */
   private static function checkConnect($client_id, $message_data)
   {
      //info
      echo "user:{".$message_data['from']."}:login checking...\n";
      // 判断是否有房间号
      if(!isset($message_data['content']))
      {
        //throw new \Exception("\$message_data['room_id'] not set. client_ip:{$_SERVER['REMOTE_ADDR']} \$message:$message");
        //默认room-1
        $room_id = 'room-1';
      }
      else
      {
        //房间号
        $room_id = $message_data['content'];
      }
      //当前用户名
      $user_name = $message_data['from'];
      if(empty($user_name))
      {
        //用户名为空，创建登录失败反馈信息
        $new_message =array('type'=>'FAIL', 'content'=>$user_name." is empty, connect failed!");
        Gateway::sendToCurrentClient(json_encode($new_message));
        //info
        echo "user:{".$message_data['from']."}:login failed!\n";
        return false;
      }
      //用户信息
      $user_info = $message_data['userlist'];
      //用户集session
      $user_sessions = Gateway::getClientSessionsByGroup($room_id);
      //检查用户名是否重复
      foreach ($user_sessions as $temp_client_id => $temp_sessions) 
      {
        if($temp_sessions['user_name'] == $user_name)
        {
          //用户名重复，创建登录失败反馈信息
          $new_message =array('type'=>'FAIL', 'content'=>$user_name." is existed, connect failed!");
          Gateway::sendToCurrentClient(json_encode($new_message));
          //info
          echo "user:{".$message_data['from']."}:login failed!\n";
          return false;
        }

      }
      //没有发现重名
      //debug
      //echo '$user_name:'.$user_name.',$room_id:'.$room_id.',$user_info:';var_dump($user_info);

      //把用户名、用户信息、房间号存放到session中
      $_SESSION['user_name'] = $user_name;
      $_SESSION['user_info'] = $user_info;
      $_SESSION['room_id'] = $room_id;
      //当前客户加入指定房间
      Gateway::joinGroup($client_id, $room_id);

      //创建连接成功反馈信息
      $new_message =array('type'=>'SUCCESS', 'content'=>$user_name." connect successful!");
      Gateway::sendToCurrentClient(json_encode($new_message));
      //info
      echo "user:{".$message_data['from']."}:login successful!\n";
      //Gateway::sendToClient($client_id, json_encode($new_message));
      //创建用户集反馈信息
      self::sendUsersInfoList(true, $room_id);
      //发送新用户上线通知
      self::sendNotification(true, $room_id, $user_name." online..");
      return true;
   }  

   /**
    * 
    * 发送在线用户信息集
    * @param bool $isAllUsers
    * @param string $room_id 房间号
    * @return bool
    */
   private static function sendUsersInfoList($isAllUsers, $room_id)
   {
      if(empty($room_id))
      {
        //error
        echo "error:send user_info_list failed! \$room_id is empty! \n";
        return false;
      }
      //用户集session
      $user_sessions = Gateway::getClientSessionsByGroup($room_id);
      $i = 0;
      if (empty($user_sessions)) {
        //error
        echo "error:send user_info_list failed! no online users! \n";
        return false;
      }
      foreach ($user_sessions as $temp_client_id => $temp_sessions) 
      {
        $user_info_list[$i++] = $temp_sessions['user_info'][0];
      }
      $new_message = array('type'=>'USERLIST', 'userlist'=>$user_info_list);
      if($isAllUsers)
      {
          //发送给所有用户
          Gateway::sendToGroup($room_id, json_encode($new_message));
          //info
          echo "server:send user_info_list to all...\n";
          return true;
      }
      else
      {
        //发送给当前用户
        Gateway::sendToCurrentClient(json_encode($new_message));
        //info
        echo "server:send user_info_list to current user...\n";
        return true;
      }
   }

    /**
    * 发送通知消息，用于通知全体用户或当前用户
    * @param $isAllUsers 
    * @param $room_id 房间号
    * @param $notice 通知内容
    * @return bool
    */
  private static function sendNotification($isAllUsers, $room_id, $notice)
  {
    //用户集session
    $user_sessions = Gateway::getClientSessionsByGroup($room_id);
    $i = 0;
    if (empty($user_sessions)) {
      //error
      echo "error:send notification failed! no online users! \n";
      return false;
    }
    $new_message = array('type'=>'NOTIFICATION', 'content'=>$notice);
    if($isAllUsers)
    {
      //发送给所有用户
      Gateway::sendToGroup($room_id, json_encode($new_message));
      //info
      echo "server:send notification to all...\n";
      return true;
    }
    else
    {
      //发送给当前用户
      Gateway::sendToCurrentClient(json_encode($new_message));
      //info
      echo "server:send notification to current user...\n";
      return true;
    }

   }
  /**
   * 对收到的聊天消息进行转发，分为单播和广播两种方式处理
   * @param int $client_id
   * @param string $message_data
   * @return bool  
  */
  private static function sendMSG($client_id, $message_data)
  {
    //房间号
    $room_id = $_SESSION['room_id'];
    if(empty($room_id))
    {
      //error
      echo "error:sendMSG failed! \$room_id is empty! \n";
      return false;
    }
    //发送方用户名
    $from_user_name = $message_data['from'];
    //接受方用户名
    $to_user_name = $message_data['to'];
    //todo：加入消息时间
    if(empty($to_user_name))
    {
      self::sendNotification(false, $room_id, "The selected user does not exist!");
      //error
      echo "error:sendMSG failed! \$to_user_name is empty! \n";
      return false;
    }
    else if($to_user_name == self::ALL)
    {
      //群发，需要排除当前用户
      Gateway::sendToGroup($_SESSION['room_id'], json_encode($message_data), $client_id);
      //info
      echo "user:{".$from_user_name."}:send group_chat msg to all...\n";
      //debug
      echo "chat content:".json_encode($message_data)."\n";
    }
    else
    {
      //用户集session
      $user_sessions = Gateway::getClientSessionsByGroup($_SESSION['room_id']);
      //
      foreach ($user_sessions as $temp_client_id => $temp_sessions) 
      {

        if($temp_sessions['user_name'] == $to_user_name)
        {
          Gateway::sendToClient($temp_client_id, json_encode($message_data));
          //info
          echo "user{".$from_user_name."}:send private_chat to {".$to_user_name."}...\n";
          //debug
          echo "chat content:".json_encode($message_data)."\n";
          return true;
        }
       
      }
       self::sendNotification(false, $room_id, "The selected user does not exist!");
       //error
       echo "error:sendMSG failed! the selected user does not exist! \n";
       return false;
    }
  }
}
