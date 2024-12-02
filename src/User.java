import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private ImageIcon photograph;
    private String username;
    private String password;
    private boolean online;
    private List<String> friends;
    private List<String> groupChats;
    private Map<String, Boolean> friendsOnlineStatus;

    public User() {
        this.friends = new ArrayList<>();
        this.groupChats = new ArrayList<>();
        this.friendsOnlineStatus = new HashMap<>();
    }

    public User(String username, String password) {
        this();
        this.photograph = new ImageIcon("src/img/user_photo.jpg");
        this.username = username;
        this.password = password;
        this.online = false;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void addFriend(String friend) {
        if (!this.friends.contains(friend)) {
            this.friends.add(friend);
        }
        updateFriendsOnlineStatus();
    }

    public void removeFriend(String friend) {
        this.friends.remove(friend);
        updateFriendsOnlineStatus();
    }

    public List<String> getGroupChats() {
        return groupChats;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void addGroupChat(String groupChat) {
        if (!this.groupChats.contains(groupChat)) {
            this.groupChats.add(groupChat);
        }
    }

    public void removeGroupChat(String groupChat) {
        this.groupChats.remove(groupChat);
    }

    public Map<String, Boolean> getFriendsOnlineStatus() {
        return friendsOnlineStatus;
    }

    public boolean isFriendOnline(String friend) {
        return friendsOnlineStatus.getOrDefault(friend, false);
    }

    private void updateFriendsOnlineStatus() {
        if (this.friendsOnlineStatus == null) {
            this.friendsOnlineStatus = new HashMap<>();
        }
        for (String friend : friends) {
            friendsOnlineStatus.put(friend, ChatServer.userManager.isUserOnline(friend));
        }
    }
}
