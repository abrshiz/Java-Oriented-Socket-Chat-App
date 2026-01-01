import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 5001; // same port as your SocketGUI
    private static Set<DataOutputStream> clientOutputs = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Start a new thread to handle this client
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());   // <-- add this
                out = new DataOutputStream(socket.getOutputStream()); // <-- add this
                clientOutputs.add(out);                               // <-- add this

                while (true) {
                    int type = in.readInt();

                    if (type == 1) { // TEXT
                        String msg = in.readUTF();
                        broadcastText(msg, out);
                    } else if (type == 2) { // IMAGE
                        int size = in.readInt();
                        byte[] imageBytes = new byte[size];
                        in.readFully(imageBytes);
                        broadcastImage(imageBytes, out);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    clientOutputs.remove(out); // remove client on disconnect
                    socket.close();
                } catch (IOException e) { e.printStackTrace(); }
            }
        }


        private void broadcastText(String message, DataOutputStream sender) {
            synchronized (clientOutputs) {
                for (DataOutputStream out : clientOutputs) {
                    if (out != sender) {
                        try {
                            out.writeInt(1); // text type
                            out.writeUTF(message);
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private static void broadcastImage(byte[] imageBytes, DataOutputStream sender) {
            synchronized (clientOutputs) {
                for (DataOutputStream out : clientOutputs) {
                    if (out != sender) { // skip sender
                        try {
                            out.writeInt(2);               // IMAGE type
                            out.writeInt(imageBytes.length);
                            out.write(imageBytes);
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }
}
