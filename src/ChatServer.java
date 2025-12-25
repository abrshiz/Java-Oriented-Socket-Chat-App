import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 3000; // same port as your ChatGUI
    private static Set<PrintWriter> clientWriters = new HashSet<>();

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
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Initialize input/output streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Add this client's writer to the set
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                // Read messages from client and broadcast
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    broadcastMessage(message, out); // pass the sender
                }

            } catch (IOException e) {
                System.out.println("Client disconnected: " + socket.getInetAddress());
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }

        // Send message to all connected clients
        private void broadcastMessage(String message, PrintWriter sender) {
           synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    if (writer != sender) {  // skip the sender
                        writer.println(message);
                    }
                }
            }
        } 
    }
}
