import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserManager implements Serializable {
    private Map<String, User> users;

    public UserManager() {
        users = new HashMap<>();
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public void setUserOnline(String username) {
        User user = users.get(username);
        if (user != null) {
            user.setOnline(true);
        }
    }

    public void setUserOffline(String username) {
        User user = users.get(username);
        if (user != null) {
            user.setOnline(false);
        }
    }

    public boolean isUserOnline(String username) {
        User user = users.get(username);
        return user != null && user.isOnline();
    }
}
