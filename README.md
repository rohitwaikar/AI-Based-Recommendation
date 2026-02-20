# AI-Based-Recommendation

**Company Name:** CodeTech It Solutions

**Name:** Rohit Waikar

**Project :** AI Based Recommendation App

**Intern Id:** CTIS4163

**Domain Name:** Java Programming

**Mentor Name:** Neela Santosh

#Description

This task involves designing and implementing a functional client–server chat application using Java Sockets and multithreading. The goal of the project is to create a real-time communication system where multiple clients can connect to a central server and exchange messages simultaneously. The application demonstrates core networking concepts, concurrent programming, and real-time data communication.

The system follows the client–server architecture model. In this model, the server acts as a centralized communication hub that manages all connected clients. The clients are individual user programs that connect to the server to send and receive messages. Communication between the server and clients is established using Java’s socket programming capabilities provided in the java.net package.

The server application uses a ServerSocket to listen for incoming client connections on a specified port number. When a client attempts to connect, the server accepts the connection using the accept() method, which creates a dedicated Socket object for communication with that client. To handle multiple users simultaneously, the server uses multithreading. For each new client connection, a separate thread is created. This ensures that the server can manage multiple clients at the same time without blocking other connections.

Each client thread continuously listens for incoming messages from its respective client and broadcasts those messages to all other connected clients. This broadcasting mechanism enables group chat functionality, where every user can see messages sent by others in real time. Without multithreading, the server would only be able to process one client at a time, causing delays and preventing simultaneous communication.

On the client side, the application connects to the server using a Socket object with the server’s IP address and port number. The client program performs two main tasks: sending messages to the server and receiving messages from it. To achieve smooth two-way communication, the client also uses multithreading. One thread reads user input from the console and sends messages to the server, while another thread continuously listens for incoming messages from the server and displays them. This ensures uninterrupted communication.
