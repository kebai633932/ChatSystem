import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 1111;
    private ConcurrentHashMap<String, ObjectOutputStream> clientOutputStreams = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ObjectInputStream> clientInputStreams = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Socket> clientSockets = new ConcurrentHashMap<>();
    public static UserManager userManager;
    private ConcurrentHashMap<String, String> pendingConnections = new ConcurrentHashMap<>();

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        userManager = new UserManager();
        userManager.addUser(new User("1", hashPassword("1")));
        userManager.addUser(new User("2", hashPassword("2")));
        new ChatServer().start();
    }

    public void start() {
        System.out.println("Chat server started on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                Message message = (Message) in.readObject();
                if (message.getMessageType().equals(MessageType.MESSAGE_LOGIN)) {
                    username = message.getSender();
                    String hashedPassword = hashPassword((String) message.getContent());
                    if (authenticate(username, hashedPassword)) {
                        Message loginSuccessMessage = new Message("server", username, userManager.getUser(username), MessageType.MESSAGE_LOGIN_SUCCESS);
                        out.writeObject(loginSuccessMessage);
                        out.flush();
                        clientOutputStreams.put(username, out);
                        clientInputStreams.put(username, in);
                        clientSockets.put(username, socket);
                        userManager.setUserOnline(username);
                    } else {
                        Message loginFailMessage = new Message("server", username, null, MessageType.MESSAGE_LOGIN_FAIL);
                        out.writeObject(loginFailMessage);
                        out.flush();
                        socket.close();
                        return;
                    }
                } else if (message.getMessageType().equals(MessageType.MESSAGE_REGISTER)) {
                    String newUsername = message.getSender();
                    String newPassword = (String) message.getContent();
                    if (userManager.userExists(newUsername)) {
                        Message registerFailMessage = new Message("server", newUsername, null, MessageType.MESSAGE_REGISTER_FAIL);
                        out.writeObject(registerFailMessage);
                        out.flush();
                    } else {
                        User newUser = new User(newUsername, hashPassword(newPassword));
                        userManager.addUser(newUser);
                        Message registerSuccessMessage = new Message("server", newUsername, null, MessageType.MESSAGE_REGISTER_SUCCESS);
                        out.writeObject(registerSuccessMessage);
                        out.flush();
                    }
                }

                while (message != null) {
                    try {
                        message = (Message) in.readObject();
                        handleMessage(message);  // 调用handleMessage来处理收到的消息
                    } catch (EOFException e) {
                        System.out.println("Client disconnected: " + username);
                        //handleDisconnect(message);
                        break;
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                        handleDisconnect(message);
                        break;
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                handleDisconnect(null);
            }
        }

        private boolean authenticate(String username, String hashedPassword) {
            User user = userManager.getUser(username);
            return user != null && user.getPassword().equals(hashedPassword);
        }

        private void sendMessageToClient(Message message) {
            ObjectOutputStream writer = clientOutputStreams.get(message.getReceiver());
            if (writer != null) {
                try {
                    writer.writeObject(message);
                    writer.flush();
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("No client found with username: " + message.getReceiver());
            }
        }

        private void handleMessage(Message message) {
            switch (message.getMessageType()) {
                case MessageType.MESSAGE_COMM_MES:
                    sendMessageToClient(message);  // 转发消息给接收者
                    break;
                case MessageType.MESSAGE_IMAGE:
                    sendImageMessageToClient(message);  // 转发图片消息给接收者
                    break;
                case MessageType.MESSAGE_CONNECT:
                    handleConnectRequest(message);
                    break;
                case MessageType.MESSAGE_REFRESH_REQUEST:
                    handleRefreshRequest(message);
                    break;
                case MessageType.MESSAGE_ADD_FRIEND:
                    handleAddFriendRequest(message);
                    break;
                case MessageType.MESSAGE_REMOVE_FRIEND:
                    handleRemoveFriendRequest(message);
                    break;
                case MessageType.MESSAGE_DISCONNECT:
                    handleDisconnect(message);
                    break;
                default:
                    System.out.println("Unknown message type: " + message.getMessageType());
                    break;
            }
        }

        private void handleConnectRequest(Message message) {
            String receiver = message.getReceiver();

            receiver = message.getReceiver();
            Socket receiverSocket = clientSockets.get(receiver);

            System.out.println("Handling connect request for " + receiver);
            if (receiverSocket != null) {
                try {
                    out.writeObject(new Message(receiver, username, receiverSocket.getInetAddress().getHostAddress(), MessageType.MESSAGE_CONNECT));
                    out.flush();
                    ObjectOutputStream receiverOut = clientOutputStreams.get(receiver);
                    receiverOut.writeObject(new Message(username, receiver, socket.getInetAddress().getHostAddress(), MessageType.MESSAGE_CONNECT));
                    receiverOut.flush();
                    System.out.println("Handled connect request between " + username + " and " + receiver);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("Receiver socket not found for: " + receiver);
            }
        }

        private void handleRefreshRequest(Message message) {
            String username = message.getSender();
            User user = userManager.getUser(username);
            if (user != null) {
                Map<String, Boolean> friendsStatus = new HashMap<>();
                for (String friend : user.getFriends()) {
                    User friendUser = userManager.getUser(friend);
                    if (friendUser != null) {
                        friendsStatus.put(friend, friendUser.isOnline());
                    }
                }
                try {
                    out.writeObject(new Message("server", username, friendsStatus, MessageType.MESSAGE_REFRESH_RESPONSE));
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleAddFriendRequest(Message message) {
            String username = message.getSender();
            String newFriend = (String) message.getContent();
            User user = userManager.getUser(username);
            User friend = userManager.getUser(newFriend);

            if (user != null && friend != null) {
                user.addFriend(newFriend);
                friend.addFriend(username);
                sendMessageToClient(new Message("server", username, "Friend added", MessageType.MESSAGE_ADD_FRIEND_SUCCESS));
                sendMessageToClient(new Message("server", newFriend, "Friend added", MessageType.MESSAGE_ADD_FRIEND_SUCCESS));
            }
        }

        private void handleRemoveFriendRequest(Message message) {
            String username = message.getSender();
            String friend = (String) message.getContent();
            User user = userManager.getUser(username);
            User friendUser = userManager.getUser(friend);

            if (user != null && friendUser != null) {
                user.removeFriend(friend);
                friendUser.removeFriend(username);
                sendMessageToClient(new Message("server", username, "Friend removed", MessageType.MESSAGE_REMOVE_FRIEND_SUCCESS));
                sendMessageToClient(new Message("server", friend, "Friend removed", MessageType.MESSAGE_REMOVE_FRIEND_SUCCESS));
            } else {
                sendMessageToClient(new Message("server", username, "Failed to remove friend", MessageType.MESSAGE_REMOVE_FRIEND_FAIL));
            }
        }
        private void handleDisconnect(Message message) {
            System.out.println("Handling disconnect for user: " + username);
            cleanup();
        }

        private void cleanup() {
            if (username != null) {
                clientOutputStreams.remove(username);
                clientInputStreams.remove(username);
                clientSockets.remove(username);
                userManager.setUserOffline(username);
                System.out.println("User " + username + " has disconnected.");
            }
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
