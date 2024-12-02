import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class UserLoginUI extends JFrame {
    private Socket socket;
    JLabel bgimg;
    JLabel username;
    JLabel password;
    JLabel title;
    JTextField usernametext;
    JPasswordField passwordtext;
    JButton loginButton;
    JButton registerButton;
    JLabel messageLabel;
    final int WIDTH = 400;
    final int HEIGHT = 300;
    JPanel jpanel_1;

    public UserLoginUI() {
        SwingUtilities.invokeLater(() -> {
            createUI();
            this.setVisible(true);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        });
    }
    private void createUI() {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;
        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;
        this.setBounds(x, y, WIDTH, HEIGHT);

        jpanel_1 = new JPanel();
        jpanel_1.setBounds(0, 0, WIDTH, HEIGHT);
        jpanel_1.setLayout(null);

        ImageIcon img = new ImageIcon("src/img/logo_3.jpg");
        bgimg = new JLabel(img);
        bgimg.setBounds(0, 0, img.getIconWidth(), img.getIconHeight());

        title = new JLabel("Java版即时聊天程序");
        title.setFont(new Font("宋体", Font.BOLD, 26));
        title.setForeground(Color.yellow);
        title.setBounds(60, 20, 250, 50);

        username = new JLabel("用户名");
        username.setFont(new Font("行楷", Font.BOLD, 16));
        username.setForeground(Color.black);
        username.setBounds(80, 90, 70, 40);
        usernametext = new JTextField(15);
        usernametext.setFont(new Font("宋体", Font.PLAIN, 16));
        usernametext.setForeground(Color.black);
        usernametext.setBounds(150, 90, 150, 40);
        usernametext.setOpaque(false);

        password = new JLabel("密码");
        password.setFont(new Font("行楷", Font.BOLD, 16));
        password.setForeground(Color.black);
        password.setBounds(80, 140, 70, 40);
        passwordtext = new JPasswordField(15);
        passwordtext.setFont(new Font("宋体", Font.PLAIN, 16));
        passwordtext.setForeground(Color.black);
        passwordtext.setBounds(150, 140, 150, 40);
        passwordtext.setOpaque(false);

        loginButton = new JButton("登录");
        loginButton.setFont(new Font("宋体", Font.BOLD, 16));
        loginButton.setBounds(160, 200, 140, 40);

        registerButton = new JButton("注册");
        registerButton.setFont(new Font("宋体", Font.BOLD, 16));
        registerButton.setBounds(80, 200, 70, 40);

        messageLabel = new JLabel("");
        messageLabel.setFont(new Font("宋体", Font.BOLD, 10));
        messageLabel.setForeground(Color.RED);
        messageLabel.setBounds(80, 180, 220, 20);

        jpanel_1.add(messageLabel);
        jpanel_1.add(registerButton);
        jpanel_1.add(loginButton);
        jpanel_1.add(username);
        jpanel_1.add(usernametext);
        jpanel_1.add(passwordtext);
        jpanel_1.add(password);
        jpanel_1.add(title);
        jpanel_1.add(bgimg);
        this.add(jpanel_1);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });
    }

    private void handleLogin() {
        String username = usernametext.getText();
        char[] password = passwordtext.getPassword();

        try {
            if (authenticateWithServer(username, new String(password)) != null) {
                Socket socket = new Socket("127.0.0.1", 1111);
                messageLabel.setText("登录成功！");
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                //ChatClient chatClient = new ChatClient(username, socket, oos, ois);
                UserSelfInfUI userinf_ui = new UserSelfInfUI(username, new String(password), socket, oos,ois);// 传递用户信息
//                chatClient.setUserSelfInfUI(userinf_ui);
//                chatClient.requestFriendListRefresh();
                //this.dispose(); // 关闭登录窗口
            } else {
                messageLabel.setText("登录账号密码有误");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            passwordtext.setText("");
        }
    }

    private Socket authenticateWithServer(String username, String password) {
        try {
            Socket socket = new Socket("127.0.0.1", 1111);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Message loginMessage = new Message(username, null, password, MessageType.MESSAGE_LOGIN);
            oos.writeObject(loginMessage);
            oos.flush();
            Message response = (Message) ois.readObject();

            boolean loginSuccess = MessageType.MESSAGE_LOGIN_SUCCESS.equals(response.getMessageType());
            if (loginSuccess) {
                return socket;
            } else {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void handleRegister() {
        new UserRegisterUI();
        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserLoginUI().setVisible(true));
    }
}
