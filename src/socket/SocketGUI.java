package socket;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SocketGUI extends javax.swing.JFrame {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private JPanel chatContainer;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private JButton actionButton;
    private JLabel titleLabel;
    private Component verticalGlue;

    private ImageIcon sendIcon, cameraIcon, moonIcon, sunIcon;
    private boolean isDarkMode = true;
    private JButton themeBtn;
    private Color APP_BG, HEADER_COLOR, SENDER_BUBBLE, RECEIVER_BUBBLE, PRIMARY_TEXT, TEXT_MUTED, INPUT_BG;
    private void addImageBubble(ImageIcon icon, boolean isSender) {
        chatContainer.remove(verticalGlue);

        JLabel imageLabel = new JLabel(
                new ImageIcon(icon.getImage().getScaledInstance(150, -1, Image.SCALE_SMOOTH))
        );

        JPanel bubble = new JPanel();
        bubble.setBackground(isSender ? SENDER_BUBBLE : RECEIVER_BUBBLE);
        bubble.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        bubble.add(imageLabel);

        JPanel wrapper = new JPanel(new FlowLayout(isSender ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.add(bubble);

        chatContainer.add(wrapper);
        chatContainer.add(verticalGlue);


        chatContainer.revalidate();
        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(
                        scrollPane.getVerticalScrollBar().getMaximum()
                )
        );
    }

    public SocketGUI() {
        updateThemeColors();
        loadIcons();
        setupUI();
        connectToServer();
    }

    private void updateThemeColors() {
        if (isDarkMode) {
            APP_BG = new Color(20, 20, 22);
            HEADER_COLOR = new Color(30, 31, 35);
            SENDER_BUBBLE = new Color(0, 132, 255);
            RECEIVER_BUBBLE = new Color(70, 41, 182);
            PRIMARY_TEXT = new Color(245, 245, 245);
            TEXT_MUTED = new Color(127, 143, 163);
            INPUT_BG = new Color(50, 45, 45, 160);
        } else {
            // ONLY EDITED LIGHT MODE COLORS HERE
            APP_BG = new Color(255, 255, 255);
            HEADER_COLOR = new Color(255, 255, 255);
            SENDER_BUBBLE = new Color(0, 132, 255);
            RECEIVER_BUBBLE = new Color(70, 41, 182); // Light bubble color
            PRIMARY_TEXT = new Color(0, 0, 0);
            TEXT_MUTED = new Color(127, 143, 163);
            INPUT_BG = new Color(242, 243, 245, 236); // Light Gray for the input field
        }
    }


    private void loadIcons() {
        try {
            cameraIcon = resizeIcon(new ImageIcon("Pictures/camera.png"), 24, 24);
            sendIcon = resizeIcon(new ImageIcon("Pictures/send-message.png"), 24, 24);
            moonIcon = resizeIcon(new ImageIcon("Pictures/moon.png"), 22, 22);
            sunIcon = resizeIcon(new ImageIcon("Pictures/sun.png"), 22, 22);
        } catch (Exception e) { System.err.println("Icons missing."); }
    }

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resized = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resized);
    }

    private void setupUI() {
        setTitle("Modern Chat");
        setSize(400, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(APP_BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setPreferredSize(new Dimension(400, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, isDarkMode ? new Color(50,50,50) : new Color(210,210,210)));

        titleLabel = new JLabel("  Online Server");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_TEXT);
        header.add(titleLabel, BorderLayout.WEST);

        themeBtn = new JButton();
        themeBtn.setIcon(isDarkMode ? moonIcon : sunIcon);
        themeBtn.setFocusPainted(false);
        themeBtn.setContentAreaFilled(false);
        themeBtn.setBorderPainted(false);
        themeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeBtn.addActionListener(e -> {
            isDarkMode = !isDarkMode;
            updateThemeColors();
            themeBtn.setIcon(isDarkMode ? moonIcon : sunIcon);
            refreshUI();
        });
        header.add(themeBtn, BorderLayout.EAST);

        // Chat Container
        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setBackground(APP_BG);
        chatContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        verticalGlue = Box.createVerticalGlue();
        chatContainer.add(verticalGlue);

        scrollPane = new JScrollPane(chatContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(APP_BG);

        // Input
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(HEADER_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 25, 15));

        messageField = new JTextField();
        messageField.setFont(new Font("SansSerif", Font.PLAIN, 15));
        messageField.setBackground(INPUT_BG);
        messageField.setForeground(PRIMARY_TEXT);
        messageField.setCaretColor(PRIMARY_TEXT);
        messageField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(25, isDarkMode ? new Color(60, 60, 65) : new Color(200, 200, 205)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        messageField.addActionListener(e -> handleSendMessage());

        actionButton = new JButton();
        actionButton.setIcon(cameraIcon);
        actionButton.setFocusPainted(false);
        actionButton.setContentAreaFilled(false);
        actionButton.setBorderPainted(false);
        actionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        messageField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() { actionButton.setIcon(messageField.getText().trim().isEmpty() ? cameraIcon : sendIcon); }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        actionButton.addActionListener(e -> {
            if (messageField.getText().trim().isEmpty()) handlePhotoAction();
            else handleSendMessage();
        });



        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(actionButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }


    private void refreshUI() {
        getContentPane().setBackground(APP_BG);
        chatContainer.setBackground(APP_BG);
        scrollPane.getViewport().setBackground(APP_BG);
        titleLabel.setForeground(PRIMARY_TEXT);
        JPanel header = (JPanel) titleLabel.getParent();
        header.setBackground(HEADER_COLOR);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, isDarkMode ? new Color(50,50,50) : new Color(210,210,210)));
        JPanel bottom = (JPanel) messageField.getParent();
        bottom.setBackground(HEADER_COLOR);
        messageField.setBackground(INPUT_BG);
        messageField.setForeground(PRIMARY_TEXT);
        messageField.setCaretColor(PRIMARY_TEXT);
        messageField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(25, isDarkMode ? new Color(60, 60, 65) : new Color(200, 200, 205)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        chatContainer.revalidate();
        chatContainer.repaint();
    }

    private void handleSendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty()) {
            addBubble(msg, true);     // show locally
            messageField.setText("");  // <-- clear immediately

            if (out != null) {
                try {
                    out.writeInt(1);   // TEXT type
                    out.writeUTF(msg); // message
                    out.flush();
                } catch (IOException e) {
                    addBubble("Failed to send message", true);
                }
            }
        }
    }

    private void handlePhotoAction() {
        if (out == null) {
            addBubble("Server not connected yet", true);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                byte[] imageBytes = Files.readAllBytes(file.toPath());

                // Send to server
                out.writeInt(2); // TYPE = IMAGE
                out.writeInt(imageBytes.length);
                out.write(imageBytes);
                out.flush();

                // Show the image locally in the chat
                ImageIcon icon = new ImageIcon(imageBytes);
                addImageBubble(icon, true); // <-- THIS will show the photo

            } catch (IOException e) {
                addBubble("Failed to send image", true);
            }
        }
    }


    private void addBubble(String message, boolean isSender) {
        chatContainer.remove(verticalGlue);

        JPanel wrapper = new JPanel(new FlowLayout(isSender ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSender ? SENDER_BUBBLE : RECEIVER_BUBBLE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        bubble.setOpaque(false);

        String textCol = isSender ? "white" : (isDarkMode ? "white" : "black");
        JLabel text = new JLabel("<html><body style='width: 150px; padding: 5px; color: " + textCol + ";'>" + message + "</body></html>");
        text.setFont(new Font("SansSerif", Font.PLAIN, 14));
        text.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bubble.add(text);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        bubble.setAlignmentX(isSender ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        container.add(bubble);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        timeLabel.setForeground(TEXT_MUTED);
        timeLabel.setAlignmentX(isSender ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        container.add(Box.createVerticalStrut(2));
        container.add(timeLabel);

        wrapper.add(container);
        chatContainer.add(wrapper);
        chatContainer.add(verticalGlue);

        chatContainer.revalidate();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
    }

    private void connectToServer() {
        new Thread(() -> {
            while (socket == null || !socket.isConnected()) {
                try {
                    socket = new Socket("10.235.102.111", 5001);
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                    addBubble("Server Connection Active", false);
                    listenForMessages();
                    break;
                } catch (IOException e) {
                    addBubble("Retrying connection...", false);
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                }
            }
        }).start();
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                while (true) {
                    int type = in.readInt();
                    if (type == 1) { // TEXT
                        addBubble(in.readUTF(), false);
                    } else if (type == 2) { // IMAGE
                        int size = in.readInt();
                        byte[] imageBytes = new byte[size];
                        in.readFully(imageBytes);
                        addImageBubble(new ImageIcon(imageBytes), false);
                    }
                }
            } catch (IOException e) {
                addBubble("Disconnected from server", false);
            }
        }).start();
    }


    class RoundedBorder extends AbstractBorder {
        private int radius; Color color;
        RoundedBorder(int radius, Color color) { this.radius = radius; this.color = color; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width-1, height-1, radius, radius);
            g2.dispose();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SocketGUI().setVisible(true));
    }
}