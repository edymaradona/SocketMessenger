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
import view.ServerGUI;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class ServerClass {

    private ArrayList<ClientThread> al;
    private static int id;
    private ServerGUI sg;
    private boolean online;
    private SimpleDateFormat sdf;
    private int port;

    public ServerClass(int port, ServerGUI sg) {
        this.port = port;
        this.sg = sg;
        al = new ArrayList<ClientThread>();
        sdf = new SimpleDateFormat("HH:mm:ss");
    }

    public void start() {
        online = true;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (online) {
                display("Server waiting for Clients on port " + port + ".");
                Socket socket = serverSocket.accept();

                if (!online) {
                    break;
                }
                ClientThread t = new ClientThread(socket);
                al.add(t);
                t.start();

// Show Client List                
                ArrayList cl = new ArrayList();
                for (int i = 0; i < al.size(); ++i) {
                    ClientThread ct = al.get(i);
                    cl.add(ct.username);
                }
                sg.appendClient(cl);
            }
            try {
                serverSocket.close();
                for (int i = 0; i < al.size(); ++i) {
                    ClientThread tc = al.get(i);
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    } catch (IOException ioE) {

                    }
                }
            } catch (Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        } catch (IOException e) {
            display(sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n");
        }
    }

    private void display(String msg) {
        sg.appendEvent(sdf.format(new Date()) + " " + msg + "\n");
    }

    public void stop() {
        online = false;
        try {
            new Socket("localhost", port);
        } catch (Exception e) {
        }
    }

    synchronized void remove(int id) {
        for (int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            if (ct.id == id) {
                al.remove(i);
                return;
            }
        }
    }

    private synchronized void broadcast(String message) {
        String time = sdf.format(new Date());
        String msgLf = time + " " + message + "\n";
        sg.appendRoom(msgLf);

        for (int i = al.size(); --i >= 0;) {
            ClientThread ct = al.get(i);
            if (!ct.writeMsg(msgLf)) {
                al.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");

            }
        }
    }

    class ClientThread extends Thread {

        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;

        String username;
        MessageFormat cm;

        String date;

        ClientThread(Socket socket) {
            id = id++;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
                display(username + " just connected.");

            } catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            } catch (ClassNotFoundException e) {
            }
            date = new Date().toString() + "\n";
        }

        public ArrayList showClient() {
            ArrayList cl = new ArrayList();
            for (int i = 0; i < al.size(); ++i) {
                ClientThread ct = al.get(i);
                cl.add(ct.username);
            }
            return cl;
        }

        public void run() {

            boolean online = true;
            while (online) {
                try {
                    cm = (MessageFormat) sInput.readObject();
                } catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;
                } catch (ClassNotFoundException e2) {
                    break;
                }
                String message = cm.getMessage();

                switch (cm.getType()) {
                    case MessageFormat.LOGOUT:
                        display(username + " disconnected with a LOGOUT message.");
                        online = false;
                        break;
                    case MessageFormat.MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case MessageFormat.WHOISIN:
                        writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
                        for (int i = 0; i < al.size(); ++i) {
                            ClientThread ct = al.get(i);
                            writeMsg((i + 1) + ") " + ct.username + " since " + ct.date);
                        }
                        break;
                }
            }

            remove(id);

            ArrayList cl = new ArrayList();
            for (int x = 0; x < al.size(); ++x) {
                ClientThread cf = al.get(x);
                cl.add(cf.username);
            }
            sg.appendClient(cl);

            close();
        }

        private void close() {
            try {
                if (sOutput != null) {
                    sOutput.close();
                }
            } catch (Exception e) {
            }
            try {
                if (sInput != null) {
                    sInput.close();
                }
            } catch (Exception e) {
            };
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
            }
        }

        private boolean writeMsg(String msg) {
            if (!socket.isConnected()) {
                close();
                return false;
            }

            try {
                sOutput.writeObject(msg);
            } catch (IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }
    }
}
