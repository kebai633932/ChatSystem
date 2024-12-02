public interface MessageType {
    String MESSAGE_LOGIN = "1";               // 登录消息
    String MESSAGE_LOGIN_SUCCESS = "2";       // 登录成功消息
    String MESSAGE_LOGIN_FAIL = "3";          // 登录失败消息
    String MESSAGE_COMM_MES = "4";            // 普通聊天消息
    String MESSAGE_CONNECT = "5";             // 连接信息
    String MESSAGE_DRAWING = "6";             // 白板绘画消息
    String MESSAGE_REGISTER = "8";            // 注册消息
    String MESSAGE_REGISTER_FAIL = "9";       // 注册失败消息
    String MESSAGE_REGISTER_SUCCESS = "10";   // 注册成功消息
    String MESSAGE_REFRESH_REQUEST = "11";    // 刷新好友请求信息
    String MESSAGE_REFRESH_RESPONSE = "12";   // 刷新好友响应信息
    String MESSAGE_ADD_FRIEND = "13";         // 添加好友消息
    String MESSAGE_REMOVE_FRIEND = "14";      // 移除好友消息
    String MESSAGE_ADD_FRIEND_SUCCESS = "15"; // 添加好友成功消息
    String MESSAGE_IMAGE = "16";              // 发送图片消息
    String MESSAGE_DISCONNECT = "17";         // 断开连接消息
    String MESSAGE_REMOVE_FRIEND_SUCCESS = "18";  // 移除好友成功消息
    String MESSAGE_REMOVE_FRIEND_FAIL = "19";     // 移除好友失败消息
}
