import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class UserRegisterUI extends JFrame {
    JLabel bgimg;
    JLabel username;
    JLabel password;
    JLabel confirmPassword;
    JLabel title;
    JTextField usernametext;
    JPasswordField passwordtext;
    JPasswordField confirmPasswordText;
    JButton registerButton;
    JLabel messageLabel;
    final int WIDTH = 300;
    final int HEIGHT = 400;
    JPanel jpanel_1;

    public UserRegisterUI() {
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

        ImageIcon img = new ImageIcon("src/img/logo_2.jpg");
        bgimg = new JLabel(img);
        bgimg.setBounds(0, 0, WIDTH, HEIGHT);

        title = new JLabel("注册新用户");
        title.setFont(new Font("宋体", Font.BOLD, 26));
        title.setForeground(Color.yellow);
        title.setBounds((WIDTH - 150) / 2, 20, 150, 50);

        username = new JLabel("用户名");
        username.setFont(new Font("行楷", Font.BOLD, 16));
        username.setForeground(Color.black);
        username.setBounds((WIDTH - 150) / 2, 90, 70, 40);
        usernametext = new JTextField(15);
        usernametext.setFont(new Font("宋体", Font.PLAIN, 16));
        usernametext.setForeground(Color.black);
        usernametext.setBounds((WIDTH - 150) / 2 + 70, 90, 120, 40);
        usernametext.setOpaque(false);

        password = new JLabel("密码");
        password.setFont(new Font("行楷", Font.BOLD, 16));
        password.setForeground(Color.black);
        password.setBounds((WIDTH - 150) / 2, 140, 70, 40);
        passwordtext = new JPasswordField(15);
        passwordtext.setFont(new Font("宋体", Font.PLAIN, 16));
        passwordtext.setForeground(Color.black);
        passwordtext.setBounds((WIDTH - 150) / 2 + 70, 140, 120, 40);
        passwordtext.setOpaque(false);

        confirmPassword = new JLabel("确认密码");
        confirmPassword.setFont(new Font("行楷", Font.BOLD, 16));
        confirmPassword.setForeground(Color.black);
        confirmPassword.setBounds((WIDTH - 150) / 2, 190, 70, 40);
        confirmPasswordText = new JPasswordField(15);
        confirmPasswordText.setFont(new Font("宋体", Font.PLAIN, 16));
        confirmPasswordText.setForeground(Color.black);
        confirmPasswordText.setBounds((WIDTH - 150) / 2 + 70, 190, 120, 40);
        confirmPasswordText.setOpaque(false);

        registerButton = new JButton("注册");
        registerButton.setFont(new Font("宋体", Font.BOLD, 16));
        registerButton.setBounds((WIDTH - 140) / 2, 240, 140, 40);

        messageLabel = new JLabel("");
        messageLabel.setFont(new Font("宋体", Font.BOLD, 10));
        messageLabel.setForeground(Color.RED);
        messageLabel.setBounds((WIDTH - 220) / 2, 290, 220, 20);

        jpanel_1.add(messageLabel);
        jpanel_1.add(registerButton);
        jpanel_1.add(username);
        jpanel_1.add(usernametext);
        jpanel_1.add(passwordtext);
        jpanel_1.add(confirmPasswordText);
        jpanel_1.add(password);
        jpanel_1.add(confirmPassword);
        jpanel_1.add(title);
        jpanel_1.add(bgimg);

        this.add(jpanel_1);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });
    }

    private void handleRegister() {
        String username = usernametext.getText();
        char[] password = passwordtext.getPassword();
        char[] confirmPassword = confirmPasswordText.getPassword();

        if (!new String(password).equals(new String(confirmPassword))) {
            messageLabel.setText("密码和确认密码不一致！");
            return;
        }
        if (addUserWithServer(username, new String(password))) {
            messageLabel.setText("注册成功！");
            new UserLoginUI();
            this.dispose();
        } else {
            messageLabel.setText("注册失败！");
        }
    }

    private boolean addUserWithServer(String username, String password) {
        try {
            Socket socket = new Socket("127.0.0.1", 1111);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Message registerMessage = new Message(username, null, password, MessageType.MESSAGE_REGISTER);
            oos.writeObject(registerMessage);
            oos.flush();
            Message response = (Message) ois.readObject();
            return MessageType.MESSAGE_REGISTER_SUCCESS.equals(response.getMessageType());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
