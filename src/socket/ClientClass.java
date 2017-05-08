/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Socket;

/**
 *
 * @author Luqman
 */
import view.ClientGUI;

import java.util.*;
import java.net.*;
import java.io.*;

public class ClientClass {

    private Socket socket;
    private String server, username;
    private int port;
    private ClientGUI cg;
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;

    public ClientClass(String server, int port, String username, ClientGUI cg) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.cg = cg;
    }

    public boolean start() {
        try {
            socket = new Socket(server, port);
        } catch (Exception ec) {
            display("Error connecting to server:" + ec);
            return false;
        }

        display("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        new Listener().start();

        try {
            sOutput.writeObject(username);
        } catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        return true;
    }

    private void display(String msg) {
        cg.append(msg + "\n");
    }

    public void sendMessage(MessageFormat msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            display("Exception writing to server: " + e);
        }
    }

    private void disconnect() {
        try {
            if (sInput != null) {
                sInput.close();
            }
        } catch (Exception e) {
        }
        try {
            if (sOutput != null) {
                sOutput.close();
            }
        } catch (Exception e) {
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
        }

        cg.connectionFailed();

    }

    class Listener extends Thread {

        public void run() {
            while (true) {
                try {
                    cg.append((String) sInput.readObject());
                } catch (IOException e) {
                    display("Server has close the connection: " + e);
                    cg.connectionFailed();
                    break;
                } catch (ClassNotFoundException e2) {
                }
            }
        }
    }
}
