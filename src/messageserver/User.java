package messageserver;

import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author andre
 */
public class User {
    String screenName;
    char[] password;
    Date lastLogin;
    Boolean isLoggedIn = false;
    String IPAddress;
    LinkedList<String> followers;
    LinkedList<String> following;
    LinkedList<Long> clientMessages;
    Queue<Message> messageQueue;
    Queue<Notification> notificationQueue;
    
    public User(String name, char[] pass) {
        screenName = name;
        password = pass;
        followers = new LinkedList<>();
        following = new LinkedList<>();
        clientMessages = new LinkedList<>();
        messageQueue = new LinkedList<>();
        notificationQueue = new LinkedList<>();
    }
    
    public Boolean notify(Notification notif) {
        
        if (isLoggedIn) {
            try (Socket clientSocket = new Socket(IPAddress, 2004);
                 PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // Send the notification
                clientWriter.println("NOTIFICATION: " + notif.getText());
                return true;

            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
                return false;
            }
        } else {
            notificationQueue.add(notif);
        }
        
        return false;
    }
    
    public Boolean login(char[] pass, String IP) {
        if (Arrays.equals(pass, password)) {   
            lastLogin = Date.from(Instant.now());
            isLoggedIn = true;
            IPAddress = IP;
            System.out.println(IP);
            return true;
        }
        return false;
    }
    
    public Boolean logout() {
        
        isLoggedIn = false;
        return true;
    }
    
    public Boolean follow(String username) {
        return following.add(username);
    }
    
    public Boolean addFollower(String username) {
        Boolean isAdded = followers.add(username);
        
        if (isAdded) {
            Notification notif = new Notification(0, "User " + username + " has followed you!");
            this.notify(notif);
        }
        
        return isAdded;
        
        
    }
    
    public Boolean unfollow(String username) {
        return following.remove(username);
    }
    
    public Boolean removeFollower(String username) {
        return followers.remove(username);
    }
    
    public Message newPublicMessage(String body, Set<String> tags, long ID) {
        String[] recipients = followers.toArray(new String[0]);
        Message msg = new Message(screenName, recipients, body, tags, ID);
        for (String follower : followers) {
            if(MessageServer.userExists(follower)) {
                User tempUser = MessageServer.getUser(follower);
                tempUser.queuePublicMessage(msg);
            } else {
                msg = null;
                return msg;
            }
        }
        clientMessages.add(ID);
        return msg;
    }
    
    public Boolean queuePublicMessage(Message msg) {
        return messageQueue.add(msg);
    }
    
    public Message retrievePublicMessage() {
        return messageQueue.poll();
    }
}
