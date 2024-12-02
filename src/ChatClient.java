
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatClient {
    private Socket socket;
    private String username;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private ConcurrentHashMap<String, MessageManager> messageManagers;
    private ConcurrentHashMap<String, UserChatUI> openChats;
    private ConcurrentHashMap<String, ObjectOutputStream> clientOutputStreams = new ConcurrentHashMap<>();
    // 存储客户端的输入流
    private ConcurrentHashMap<String, ObjectInputStream> clientInputStreams = new ConcurrentHashMap<>();
    private UserSelfInfUI userSelfInfUI;

    public ChatClient(String username, Socket socket, ObjectOutputStream oos, ObjectInputStream ois) {
        this.socket = socket;
        this.username = username;
        this.oos = oos;
        this.ois = ois;
        this.openChats = new ConcurrentHashMap<>();
        this.messageManagers = new ConcurrentHashMap<>();
        new Thread(this::receiveMessages).start();
    }

    public void setUserSelfInfUI(UserSelfInfUI userSelfInfUI) {
        this.userSelfInfUI = userSelfInfUI;
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }

    public void sendMessage(Message message) {
        if(message.getReceiver().contains(" ")){
            String[] parts=message.getReceiver().split(" ");
            System.out.println(parts[0]);
            message.setReceiver(parts[0]);
            System.out.println("Receiver"+parts[0]+"messageType"+message.getMessageType());
        }
        try {
            synchronized (oos) {  // 确保输出流是线程安全的
                oos.writeObject(message);
                oos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Failed to send message: " + e.getMessage());
        }
    }

    public void sendDisconnect() {
        try {
            sendMessage(new Message(username, "server", null, MessageType.MESSAGE_DISCONNECT));
        } finally {
            cleanup();
        }
    }

    private void receiveMessages() {
        try {
            while (true) {
                Message message = (Message) ois.readObject();
                if (message != null) {
                    handleMessage(message);  // 调用handleMessage来处理收到的消息
                }
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected: " + username);
            cleanup();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Failed to receive messages: " + e.getMessage());
        }
    }
    private void handleConnectMessage(Message message) {
        String peerAddress = (String) message.getContent();
        try {
            Socket peerSocket = new Socket(peerAddress, socket.getPort());
            ObjectOutputStream peerOos = new ObjectOutputStream(peerSocket.getOutputStream());
            ObjectInputStream peerOis = new ObjectInputStream(peerSocket.getInputStream());
            MessageManager messageManager = messageManagers.get(message.getSender());
            if (messageManager != null) {
                messageManager.updateStreams(peerOos, peerOis);
            } else {
                messageManager = new MessageManager(peerOos, peerOis, username, message.getSender());
                messageManagers.put(message.getSender(), messageManager);
            }
            System.out.println("Connected to peer at " + peerAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Message message) {
        switch (message.getMessageType()) {
            case MessageType.MESSAGE_CONNECT:
                if (userSelfInfUI != null) {
                    System.out.println("userSelfInfUI != null -client 80行--转折点1");
                    //userSelfInfUI.handleConnectMessage(message);  // 调用UI的处理方法
                    handleConnectMessage(message);
                }
                break;
            case MessageType.MESSAGE_REFRESH_RESPONSE:
                if (userSelfInfUI != null) {
                    userSelfInfUI.updateFriendList((Map<String, Boolean>) message.getContent());
                }
                break;
            case MessageType.MESSAGE_ADD_FRIEND_SUCCESS:
                if (userSelfInfUI != null) {
                    userSelfInfUI.handleAddFriendSuccess(message);
                }
                break;
            case MessageType.MESSAGE_REMOVE_FRIEND_SUCCESS:
                if (userSelfInfUI != null) {
                    userSelfInfUI.handleRemoveFriendSuccess(message);
                }
                break;
            case MessageType.MESSAGE_REMOVE_FRIEND_FAIL:
                if (userSelfInfUI != null) {
                    userSelfInfUI.handleRemoveFriendFail(message);
                }
                break;
            case MessageType.MESSAGE_DISCONNECT:
                cleanup();
                break;
            case MessageType.MESSAGE_IMAGE:
                //普通消息
                System.out.println("Received MESSAGE_IMAGE: " + message);
                handleImageMessage(message);

                break;
            case MessageType.MESSAGE_COMM_MES:// 普通文本信息
                //普通消息
                System.out.println("Received MESSAGE_COMM_MES: " + message);
                sendMessageToClient(message);
            //case MessageType.MESSAGE_ADD_FRIEND_FAIL:   添加好友失败信息
            default:
                String sender = message.getSender();
                MessageManager messageManager = messageManagers.computeIfAbsent(sender,
                        key -> new MessageManager(oos, ois, username, sender));
                messageManager.addMessage(message);
                UserChatUI chatUI = openChats.get(sender);
                if (chatUI == null) {
                    chatUI = new UserChatUI(this, sender);
                    openChats.put(sender, chatUI);
                }
                chatUI.displayMessage(sender, (String) message.getContent());
                break;
        }
    }

    private void handleImageMessage(Message message) {
        sendImageMessageToClient(message);
        String sender = message.getSender();
        MessageManager messageManager = messageManagers.computeIfAbsent(sender,
                key -> new MessageManager(oos, ois, username, sender));
        messageManager.addMessage(message);
        UserChatUI chatUI = openChats.get(sender);
        chatUI.displayImageMessage(sender, (byte[]) message.getContent());
    }

    private void sendMessageToClient(Message message) {
        ObjectOutputStream writer = clientOutputStreams.get(message.getReceiver());
        if (writer != null) {
            try {
                writer.writeObject(message);
                writer.flush();
                System.out.println("Sent message to " + message.getReceiver());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("No client found with username: " + message.getReceiver());
        }
    }
    private void sendImageMessageToClient(Message message) {
        ObjectOutputStream writer = clientOutputStreams.get(message.getReceiver());
        if (writer != null) {
            try {
                writer.writeObject(message);
                writer.flush();
                System.out.println("Sent message to " + message.getReceiver());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("No client found with username: " + message.getReceiver());
        }
    }

    public void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void requestFriendListRefresh() {
        Message message = new Message(username, "server", null, MessageType.MESSAGE_REFRESH_REQUEST);
        sendMessage(message);
    }

    public void sendAddFriendRequest(String newFriend) {
        if (newFriend == null || newFriend.isEmpty()) {
            showErrorDialog("New friend name cannot be null or empty.");
            return;
        }
        Message message = new Message(username, "server", newFriend, MessageType.MESSAGE_ADD_FRIEND);
        sendMessage(message);
    }

    public void sendRemoveFriendRequest(String friend) {
        if (friend == null || friend.isEmpty()) {
            showErrorDialog("Friend name cannot be null or empty.");
            return;
        }
        Message message = new Message(username, "server", friend, MessageType.MESSAGE_REMOVE_FRIEND);
        sendMessage(message);
    }

    public void cleanup() {
        try {
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openChat(String friendName) {
        UserChatUI chatUI = openChats.get(friendName);
        if (chatUI == null) {
            chatUI = new UserChatUI(this, friendName);
            openChats.put(friendName, chatUI);
            sendConnectRequest(friendName);
        }
        chatUI.setVisible(true);
    }
    public void sendConnectRequest(String friendName) {
        Message message = new Message(username, friendName, "", MessageType.MESSAGE_CONNECT);
        try {
            System.out.println("Attempting to send connect request to " + friendName);
            oos.writeObject(message);
            oos.flush();
            System.out.println("Sent connect request to " + friendName);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to send connect request: " + e.getMessage());
        }
    }
}
