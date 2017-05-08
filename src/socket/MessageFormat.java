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
import java.io.*;

public class MessageFormat implements Serializable {
    private String message;
    private int type;
    public static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;

    public MessageFormat(int type) {
        this.type = type;
    }

    public MessageFormat(String message) {
        this.message = message;
    }

    public MessageFormat(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

}
