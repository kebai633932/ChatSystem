import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class UserChatUI extends JFrame {
    private ChatClient chatClient;
    private String friendName;
    JLabel friendUserName;
    JTextField text;
    JTextPane chatContent;
    final int WIDTH = 600;
    final int HEIGHT = 600;
    JPanel jpanel_1;
    JPanel jpanel_2;

    public UserChatUI(ChatClient chatClient, String friendName) {
        this.chatClient = chatClient;
        this.friendName = friendName;
        this.setTitle(friendName);
        createUI(friendName);
        this.setVisible(true);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void createUI(String friendName) {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;
        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;
        this.setBounds(x, y, WIDTH, HEIGHT);

        jpanel_1 = new JPanel(new BorderLayout());
        jpanel_2 = new JPanel(new FlowLayout(FlowLayout.LEFT));

        friendUserName = new JLabel(friendName);
        jpanel_1.add(friendUserName, BorderLayout.NORTH);

        chatContent = new JTextPane();
        chatContent.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatContent);
        jpanel_1.add(chatScrollPane, BorderLayout.CENTER);

        text = new JTextField(20);
        jpanel_2.add(text);

        JButton sendButton = new JButton("发送");
        jpanel_2.add(sendButton);

        JButton sendImageButton = new JButton("发送图片");
        jpanel_2.add(sendImageButton);

        JButton openWhiteboardButton = new JButton("打开白板");
        jpanel_2.add(openWhiteboardButton);

        this.add(jpanel_1, BorderLayout.CENTER);
        this.add(jpanel_2, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String messageContent = text.getText();
                if (messageContent != null && !messageContent.isEmpty()) {
                    Message message = new Message(chatClient.getUsername(), friendName, messageContent, MessageType.MESSAGE_COMM_MES);

                    System.out.println("MESSAGE_COMM_MES getUsername"+chatClient.getUsername());
                    System.out.println("MESSAGE_COMM_MES friendName"+friendName);
                    chatClient.sendMessage(message);
                    displayMessage("我", messageContent);
                    text.setText("");
                }
            }
        });

        sendImageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(UserChatUI.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    //自己界面图片展示
                    ImageIcon imageIcon = new ImageIcon(fileChooser.getSelectedFile().getPath());
                    chatContent.insertIcon(imageIcon);
                    chatContent.setCaretPosition(chatContent.getDocument().getLength());
                    try {
                        chatContent.getDocument().insertString(chatContent.getDocument().getLength(),
                                "\n", null);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    File selectedFile = fileChooser.getSelectedFile();
                    try (FileInputStream fis = new FileInputStream(selectedFile)) {
                        byte[] imageBytes = new byte[(int) selectedFile.length()];
                        fis.read(imageBytes);//
                        Message imageMessage = new Message(chatClient.getUsername(), friendName, imageBytes,
                                MessageType.MESSAGE_IMAGE);
                        System.out.println("MESSAGE_IMAGE getUsername"+chatClient.getUsername());
                        System.out.println("MESSAGE_IMAGE friendName"+friendName);
                        chatClient.sendMessage(imageMessage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        showErrorDialog("Failed to send image: " + ex.getMessage());
                    }
                }
            }
        });

        openWhiteboardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new WhiteboardUI(chatClient.getSocket());
            }
        });
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void displayMessage(String sender, String message) {
        try {
            chatContent.getDocument().insertString(chatContent.getDocument().getLength(), sender + ": " + message + "\n", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayImageMessage(String sender, byte[] imageBytes) {
        try {
            // 将字节数组转换为图像
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            Image image = ImageIO.read(bais);
            if (image != null) {
                ImageIcon imageIcon = new ImageIcon(image);
                // 在聊天内容中插入图像
                chatContent.insertIcon(imageIcon);
                chatContent.setCaretPosition(chatContent.getDocument().getLength());
                // 插入换行符
                chatContent.getDocument().insertString(chatContent.getDocument().getLength(),
                        "\n" + sender + " sent an image.\n", null);
            } else {
                System.err.println("Failed to convert imageBytes to Image.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Failed to display image: " + e.getMessage());
        }
    }
}
