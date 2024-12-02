import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSelfInfUI extends JFrame {
    private ChatClient chatClient;
    private String username;
    private User user;
    private ConcurrentHashMap<String, Socket> peerSockets;
    private ConcurrentHashMap<String, ObjectOutputStream> peerOutputStreams;
    private ConcurrentHashMap<String, ObjectInputStream> peerInputStreams;
    JLabel profilePictureLabel;
    JLabel userLabel;
    JLabel errorLabel;
    final int WIDTH = 320;
    final int HEIGHT = 700;
    JPanel panel;
    JList<String> friendList;
    DefaultListModel<String> friendListModel;
    JList<String> groupChatList;
    DefaultListModel<String> groupChatListModel;
    JButton addFriendButton;
    JButton removeFriendButton;
    JButton refreshButton;
    JButton addGroupChatButton;
    JButton removeGroupChatButton;

    // 构造函数，初始化UI并请求刷新好友列表
    public UserSelfInfUI(String username, String password,Socket socket,ObjectOutputStream oos,ObjectInputStream ois) {
        this.username = username;
        this.peerSockets = new ConcurrentHashMap<>();
        this.peerOutputStreams = new ConcurrentHashMap<>();
        this.peerInputStreams = new ConcurrentHashMap<>();
        connectToServer(username, password,socket, oos,ois);
        SwingUtilities.invokeLater(() -> {
            createUI();
            this.setVisible(true);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            requestFriendListRefresh(); // 请求好友列表刷新
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                chatClient.sendDisconnect();
            }
        });
    }

    private void connectToServer(String username, String password,Socket socket,ObjectOutputStream oos,ObjectInputStream ois) {
        try {
//            Socket socket = new Socket("127.0.0.1", 1111);
//            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
//            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Message loginMessage = new Message(username, null, password, MessageType.MESSAGE_LOGIN);
            oos.writeObject(loginMessage);
            oos.flush();
            Message response = (Message) ois.readObject();
            if (MessageType.MESSAGE_LOGIN_SUCCESS.equals(response.getMessageType())) {
                user = (User) response.getContent();
                chatClient = new ChatClient(username, socket, oos, ois);
                chatClient.setUserSelfInfUI(this);
            } else {
                showErrorDialog("登录失败: " + response.getContent());
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("连接服务器失败: " + e.getMessage());
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void createUI() {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int x = (screenSize.width - WIDTH) / 2;
        int y = (screenSize.height - HEIGHT) / 2;
        this.setBounds(x, y, WIDTH, HEIGHT);

        panel = new JPanel();
        panel.setLayout(null);
        panel.setBounds(0, 0, WIDTH, HEIGHT);

        // 用户头像设置
        ImageIcon icon = new ImageIcon("src/img/user_photo.jpg");
        Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        profilePictureLabel = new JLabel(new ImageIcon(img));
        profilePictureLabel.setBounds(100, 20, 100, 100);

        // 用户名标签
        userLabel = new JLabel(username);
        userLabel.setFont(new Font("宋体", Font.BOLD, 16));
        userLabel.setBounds(125, 130, 100, 20);

        // 错误提示标签
        errorLabel = new JLabel("");
        errorLabel.setFont(new Font("宋体", Font.BOLD, 14));
        errorLabel.setForeground(Color.RED);
        errorLabel.setBounds(20, 185, 260, 20);

        // 好友列表模型
        friendListModel = new DefaultListModel<>();
        for (String friendName : user.getFriends()) {
            friendListModel.addElement(friendName + " (" +
                    (user.isFriendOnline(friendName) ? "在线" : "不在线") + ")");
        }

        // 好友列表
        friendList = new JList<>(friendListModel);
        JScrollPane friendScrollPane = new JScrollPane(friendList);
        friendScrollPane.setBounds(20, 210, 260, 150);

        // 添加好友按钮
        addFriendButton = new JButton("添加好友");
        addFriendButton.setBounds(20, 370, 100, 30);

        // 移除好友按钮
        removeFriendButton = new JButton("移除好友");
        removeFriendButton.setBounds(130, 370, 100, 30);

        // 刷新按钮
        refreshButton = new JButton("刷新");
        refreshButton.setBounds(240, 370, 60, 30);

        // 群聊列表模型
        groupChatListModel = new DefaultListModel<>();
        for (String groupChat : user.getGroupChats()) {
            groupChatListModel.addElement(groupChat);
        }

        // 群聊列表
        groupChatList = new JList<>(groupChatListModel);
        JScrollPane groupChatScrollPane = new JScrollPane(groupChatList);
        groupChatScrollPane.setBounds(20, 410, 260, 150);

        // 添加群聊按钮
        addGroupChatButton = new JButton("添加群聊");
        addGroupChatButton.setBounds(20, 570, 100, 30);

        // 移除群聊按钮
        removeGroupChatButton = new JButton("移除群聊");
        removeGroupChatButton.setBounds(130, 570, 100, 30);

        // 将组件添加到面板
        panel.add(profilePictureLabel);
        panel.add(userLabel);
        panel.add(errorLabel);
        panel.add(friendScrollPane);
        panel.add(addFriendButton);
        panel.add(removeFriendButton);
        panel.add(refreshButton);
        panel.add(groupChatScrollPane);
        panel.add(addGroupChatButton);
        panel.add(removeGroupChatButton);
        this.add(panel);

        // 双击好友列表项打开聊天窗口
        friendList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String selectedFriend = friendList.getSelectedValue();
                    if(selectedFriend.contains(" ")){
                        String[] parts=selectedFriend.split(" ");
                        System.out.println(parts[0]);
                        selectedFriend=parts[0];
                        System.out.println("selectedFriend："+parts[0]);
                    }
                    chatClient.openChat(selectedFriend);
                }
            }
        });

        // 添加好友按钮事件处理
        addFriendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newFriend = JOptionPane.showInputDialog("请输入好友用户名:");
                if (newFriend != null && !newFriend.trim().isEmpty()) {
                    if (newFriend.equals(username)) {
                        errorLabel.setText("不能添加自己为好友");
                    } else if (user.getFriends().contains(newFriend)) {
                        errorLabel.setText("该用户已经是您的好友");
                    } else {
                        sendAddFriendRequest(newFriend);
                    }
                }
            }
        });

        // 移除好友按钮事件处理
        removeFriendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedFriend = friendList.getSelectedValue().split(" ")[0];
                if (selectedFriend != null) {
                    sendRemoveFriendRequest(selectedFriend);
                }
            }
        });

        // 刷新按钮事件处理
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                requestFriendListRefresh();
            }
        });

        // 添加群聊按钮事件处理
        addGroupChatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                List<String> selectedFriends = friendList.getSelectedValuesList();
                if (selectedFriends.isEmpty()) {
                    errorLabel.setText("请选择好友以创建群聊");
                    return;
                }
                StringBuilder groupName = new StringBuilder();
                for (String friend : selectedFriends) {
                    if(friend.contains(" ")){
                        String[] parts=friend.split(" ");
                        System.out.println(parts[0]);
                        friend=parts[0];
                        System.out.println("friend"+parts[0]);
                    }
                    groupName.append(friend).append("-");
                }
                groupName.append(chatClient.getUsername()).append("的群");
                String newGroupChat = groupName.toString();
                groupChatListModel.addElement(newGroupChat);
                user.addGroupChat(newGroupChat);
                selectedFriends.add(chatClient.getUsername());
                errorLabel.setText("");
            }
        });

        groupChatList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    errorLabel.setText("群聊暂未开放");
                }
            }
        });
        // 移除群聊按钮事件处理
        removeGroupChatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedGroupChat = groupChatList.getSelectedValue();
                groupChatListModel.removeElement(selectedGroupChat);
                user.removeGroupChat(selectedGroupChat);
            }
        });
    }

    public void sendAddFriendRequest(String newFriend) {
        chatClient.sendAddFriendRequest(newFriend);
    }

    public void sendRemoveFriendRequest(String friend) {
        chatClient.sendRemoveFriendRequest(friend);
    }

    public void requestFriendListRefresh() {
        chatClient.requestFriendListRefresh();
    }

    public void handleAddFriendSuccess(Message message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "好友添加成功: " + message.getContent());
            requestFriendListRefresh(); // 刷新好友列表
        });
    }

    public void handleRemoveFriendSuccess(Message message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "好友移除成功: " + message.getContent());
            requestFriendListRefresh(); // 刷新好友列表
        });
    }
    public void handleRemoveFriendFail(Message message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "好友移除失败: " + message.getContent());
        });
    }

    // 更新好友列表
    public void updateFriendList(Map<String, Boolean> newFriendsList) {
        friendListModel.clear();
        for (Map.Entry<String, Boolean> entry : newFriendsList.entrySet()) {
            String friendName = entry.getKey();
            Boolean isOnline = entry.getValue();
            friendListModel.addElement(friendName + " (" + (isOnline ? "在线" : "不在线") + ")");
        }
    }
}
