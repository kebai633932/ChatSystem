import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MessageManager {
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private String username;
    private String friendName;
    private List<Message> messages; // 存储所有消息的列表

    public MessageManager(ObjectOutputStream oos, ObjectInputStream ois, String username, String friendName) {
        this.oos = oos;
        this.ois = ois;
        this.username = username;
        this.friendName = friendName;
        this.messages = new ArrayList<>();
        loadMessagesFromFile(); // 从文件加载消息记录
    }

    public void addMessage(Message message) {
        messages.add(message);
        saveMessagesToFile(); // 保存接收到的消息到文件中
        System.out.println("Added message from " + message.getSender());
    }

    private void saveMessagesToFile() {
        String filename = getFilename();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) { // 不使用追加模式，覆盖文件内容
            for (Message message : messages) {
                writer.write("**" + message.getSender() + "**: " + message.getContent());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMessagesFromFile() {
        String filename = getFilename();
        File file = new File(filename);
        if (!file.exists()) {
            return; // 如果文件不存在，直接返回
        }
        messages.clear(); // 加载之前清空消息列表
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 读取聊天记录，假设每行格式为 "**sender**: content"
                String[] parts = line.split(": ", 2);
                if (parts.length == 2) {
                    String sender = parts[0].substring(2, parts[0].length() - 2); // 去掉 "**" 标记
                    messages.add(new Message(sender, friendName, parts[1], MessageType.MESSAGE_COMM_MES));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFilename() {
        return username + "-" + friendName + "-messages.md"; // 使用 Markdown 文件扩展名
    }

    // 添加 updateStreams 方法以更新输入输出流
    public void updateStreams(ObjectOutputStream newOos, ObjectInputStream newOis) {
        this.oos = newOos;
        this.ois = newOis;
    }
}
