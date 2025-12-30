# 💬 Modern Socket Chat Application

A high-performance, real-time Java chat application featuring a "Discord-inspired" UI, built with **Swing** and **TCP Sockets**.

## ✨ UI Improvements (v2.0)
* **Smart Contrast Logic**: In Light Mode, receiver text automatically switches to black, while sender text stays white on blue bubbles.
* **Gap-Free Layout**: Implemented `VerticalGlue` and `EmptyBorder` logic to eliminate huge gaps at the top of the chat and between messages.
* **Modern Aesthetic**: Antialiased rounded bubbles and a dynamic theme-switching header.

## 🛠️ Installation
1. Clone the repository: `git clone https://github.com/abrshiz/Java-Oriented-Socket-Chat-App.git`
2. Ensure icons (`moon.png`, `sun.png`, `camera.png`, `send-message.png`) are in the root directory.
3. Compile: `javac socket/SocketGUI.java`
4. Run: `java socket.SocketGUI`

## ⚙️ How it Works
The application uses a `BoxLayout` with a "Spring" (Vertical Glue) at the bottom. This ensures that even if only one message exists, it is pinned to the top. When new messages arrive, the glue is shifted to the bottom, maintaining a consistent 15px gap between bubbles.
