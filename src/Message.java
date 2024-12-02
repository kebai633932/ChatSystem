import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String sender;
    private String receiver;
    private Object content;
    private LocalDateTime timestamp;
    private String messageType;

    public Message(String sender, String receiver, Object content, String messageType) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.messageType = messageType;
    }

    public String getSender() {
        if(this.sender.contains(" ")){
            String[] parts=this.sender.split(" ");
            System.out.println(parts[0]);
            this.setSender(parts[0]);
            System.out.println("Receiver"+parts[0]);
        }
        return sender;
    }

    public String getReceiver() {
        if(this.receiver.contains(" ")){
            String[] parts=this.receiver.split(" ");
            System.out.println(parts[0]);
            this.setReceiver(parts[0]);
            System.out.println("Receiver"+parts[0]);
        }
        return receiver;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setReceiver(String receiver) {
        this.receiver=receiver;
    }

    public void setSender(String sender) {
        this.sender=sender;
    }
}
