package socket;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SocketGUI extends javax.swing.JFrame {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JPanel chatContainer;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private JButton actionButton;
    private JLabel titleLabel;
    private Component verticalGlue; // Added to manage the bottom "spring"

    private ImageIcon sendIcon, cameraIcon, moonIcon, sunIcon;
    private boolean isDarkMode = true;
    private JButton themeBtn;
    private Color APP_BG, HEADER_COLOR, SENDER_BUBBLE, RECEIVER_BUBBLE, PRIMARY_TEXT, TEXT_MUTED;

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
            RECEIVER_BUBBLE = new Color(44, 45, 49);
            PRIMARY_TEXT = new Color(245, 245, 245);
            TEXT_MUTED = new Color(142, 146, 151);
        } else {
            APP_BG = new Color(242, 243, 245);
            HEADER_COLOR = new Color(255, 255, 255);
            SENDER_BUBBLE = new Color(0, 132, 255);
            RECEIVER_BUBBLE = new Color(225, 228, 232);
            PRIMARY_TEXT = new Color(30, 30, 35);
            TEXT_MUTED = new Color(80, 80, 85);
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

        // Create the "Spring" that pushes everything up
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
        messageField.setBackground(isDarkMode ? new Color(45, 46, 50) : Color.WHITE);
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
        messageField.setBackground(isDarkMode ? new Color(45, 46, 50) : Color.WHITE);
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
            addBubble(msg, true);
            if (out != null) out.println(msg);
            messageField.setText("");
        }
    }

    private void handlePhotoAction() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            addBubble("📷 Photo: " + chooser.getSelectedFile().getName(), true);
        }
    }

    private void addBubble(String message, boolean isSender) {
        // Remove the glue temporarily to add message at the end, then put glue back
        chatContainer.remove(verticalGlue);

        JPanel wrapper = new JPanel(new FlowLayout(isSender ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); // Fixed 15px gap
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); // Prevent huge stretching

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
        timeLabel.setForeground(isDarkMode ? TEXT_MUTED : new Color(80, 80, 80));
        timeLabel.setAlignmentX(isSender ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        container.add(Box.createVerticalStrut(2));
        container.add(timeLabel);

        wrapper.add(container);
        chatContainer.add(wrapper);

        // Put the glue back at the bottom
        chatContainer.add(verticalGlue);

        chatContainer.revalidate();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 5000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                addBubble("Server Connection Active", false);
                String line;
                while ((line = in.readLine()) != null) { addBubble(line, false); }
            } catch (IOException e) { addBubble("Offline", false); }
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