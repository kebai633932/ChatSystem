import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import javax.imageio.ImageIO;

public class WhiteboardUI extends JFrame {
    private BufferedImage canvas;
    private Graphics2D g2d;
    private int prevX, prevY; // 上一个点的坐标
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public WhiteboardUI(Socket socket) {
        this.socket = socket;
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        setTitle("共享白板");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        canvas = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        g2d = canvas.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2d.setColor(Color.BLACK);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(canvas, 0, 0, null);
            }
        };

        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
                g2d.drawLine(prevX, prevY, prevX, prevY);
                panel.repaint();
                sendDrawingData(prevX, prevY, prevX, prevY);
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                g2d.drawLine(prevX, prevY, x, y);
                panel.repaint();
                sendDrawingData(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        });

        JButton saveButton = new JButton("保存白板");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveCanvas();
            }
        });

        JButton clearButton = new JButton("清空白板");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearCanvas();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        loadCanvas();
        setVisible(true);
        new Thread(this::receiveDrawingData).start();
    }

    private void saveCanvas() {
        try {
            ImageIO.write(canvas, "png", new File("whiteboard.png"));
            JOptionPane.showMessageDialog(this, "白板已保存为 whiteboard.png");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "保存失败: " + e.getMessage());
        }
    }

    private void loadCanvas() {
        File file = new File("whiteboard.png");
        if (file.exists()) {
            try {
                BufferedImage loadedImage = ImageIO.read(file);
                g2d.drawImage(loadedImage, 0, 0, null);
                repaint();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void clearCanvas() {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2d.setColor(Color.BLACK);
        repaint();
    }

    private void sendDrawingData(int x1, int y1, int x2, int y2) {
        try {
            DrawingData drawingData = new DrawingData(x1, y1, x2, y2);
            Message drawingMessage = new Message("Whiteboard", null, drawingData, MessageType.MESSAGE_DRAWING);
            oos.writeObject(drawingMessage);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveDrawingData() {
        try {
            while (true) {
                Message message = (Message) ois.readObject();
                if (MessageType.MESSAGE_DRAWING.equals(message.getMessageType())) {
                    DrawingData data = (DrawingData) message.getContent();
                    g2d.drawLine(data.getX1(), data.getY1(), data.getX2(), data.getY2());
                    repaint();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class DrawingData implements Serializable {
    private int x1, y1, x2, y2;

    public DrawingData(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }
}
