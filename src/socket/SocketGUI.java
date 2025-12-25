package socket;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class SocketGUI extends javax.swing.JFrame {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private JPanel chatContainer;

    public SocketGUI() {
        initComponents();
        initChatContainer();
        connectToServer();
        MessageTextField.addActionListener(e -> SendButtonActionPerformed(null));
    }

    // Initialize the panel that holds chat bubbles inside your existing scroll pane
    private void initChatContainer() {
        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setBackground(new Color(153, 255, 204));

        // top padding
        chatContainer.add(Box.createVerticalStrut(10));

        jScrollPane1.setViewportView(chatContainer);
}


    private void addBubble(String message, boolean isSender) {
        ChatBubble bubble = new ChatBubble(message, isSender);
        chatContainer.add(bubble);
        bubble.setMaximumSize(new Dimension(400,30));
        bubble.setBackground(new Color(255,255,255,150));
        bubble.setOpaque(false);
        chatContainer.add(Box.createVerticalStrut(5)); // spacing
          
        chatContainer.revalidate();
        chatContainer.repaint();

        // auto-scroll
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = jScrollPane1.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private void SendButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String message = MessageTextField.getText().trim();
        if (message.isEmpty()) return;

        addBubble(message, true);

        if (out != null) {
            out.println(message);
        } else {
            addBubble("❌ Not connected to server", false);
        }

        MessageTextField.setText("");
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost",3000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                addBubble("✅ Connected to server", false);
                receiveMessages();
            } catch (IOException e) {
                addBubble("❌ Server not available", false);
            }
        }).start();
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                addBubble(message, false);
            }
        } catch (IOException e) {
            addBubble("🔌 Disconnected from server", false);
        }
    }

    // Chat bubble panel class (sender right / receiver left)
    class ChatBubble extends JPanel {
    public ChatBubble(String text, boolean isSender) {
        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel label = new JLabel("<html><p style='width:200px'>" + text + "</p></html>");

        label.setOpaque(true); // ⭐ THIS FIXES COLOR
        label.setForeground(isSender ? Color.WHITE : Color.BLACK);
        label.setBackground(isSender ? new Color(0, 153, 255) : Color.WHITE);

        label.setBorder(
            BorderFactory.createCompoundBorder(
                new RoundedBorder(15),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            )
        );

        if (isSender) {
            add(label, BorderLayout.EAST);
        } else {
            add(label, BorderLayout.WEST);
        }
    }
}


    // ──────────────────────────────── NetBeans UI ────────────────────────────────

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        TextLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        SendButton = new javax.swing.JButton();
        MessageTextField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat App");

        jPanel1.setBackground(new java.awt.Color(153, 255, 204));
        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jPanel2.setBackground(new java.awt.Color(153, 255, 204));

        TextLabel.setFont(new java.awt.Font("DejaVu Serif", 1, 18)); // NOI18N
        TextLabel.setText("Chat App Using Socket Programming");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(TextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(TextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(153, 255, 204));

        SendButton.setBackground(new java.awt.Color(153, 255, 204));
        SendButton.setFont(new java.awt.Font("LM Mono 12", 1, 18)); // NOI18N
        SendButton.setForeground(new java.awt.Color(51, 51, 51));
        SendButton.setText("Send");
        SendButton.setActionCommand("SendButton");
        SendButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        SendButton.addActionListener(this::SendButtonActionPerformed);

        MessageTextField.setBackground(new java.awt.Color(255, 255, 153));
        MessageTextField.setToolTipText("Message");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(MessageTextField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SendButton)
                                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(MessageTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                                        .addComponent(SendButton))
                                .addContainerGap())
        );

        jScrollPane1.setBackground(new java.awt.Color(153, 255, 204));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setAutoscrolls(true);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane1)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );

        pack();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SocketGUI().setVisible(true));
    }

    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel TextLabel;
    private javax.swing.JButton SendButton;
    private javax.swing.JTextField MessageTextField;
    private javax.swing.JScrollPane jScrollPane1;
}
