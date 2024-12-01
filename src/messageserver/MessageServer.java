/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package messageserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andre
 */
public class MessageServer {
    //private static int userCount;
    private static LinkedList<User> activeUsers = new LinkedList<>();
    private static HashMap<String, User> users = new HashMap<>();
    
    private static Map<String, Set<Long>> tagIndex;  // Maps each tag to a Set of message IDs
    private static Map<Long, Message> messageStore;
    
    
    private static long lastTimestamp = 0;
    private static int messageCounter = 0;
    
    public static User user1;
    public static User user2;
    public static User user3;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Boolean serverOn = true;
        String username;
        char[] pass;
        String[] messageArray;
        String IP;
        
        tagIndex = new HashMap<>();
        messageStore = new HashMap<>();
        
        user1 = new User("Joe", "test".toCharArray());
        user2 = new User("Sally", "test".toCharArray());
        user3 = new User("Bob", "test".toCharArray());
        
        users.put("Joe", user1);
        users.put("Sally", user2);
        users.put("Bob", user3);
        
        user1.follow("Sally");
        user1.follow("Bob");
        user2.addFollower("Joe");
        user2.follow("Bob");
        user3.addFollower("Joe");
        user3.addFollower("Sally");
        
        DevFrame devFrame = new DevFrame();
        devFrame.setVisible(true);
        
        
        try {
            ServerSocket serverSocket = new ServerSocket(2624);
            while (serverOn) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                InputStream input = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

                boolean clientConnected = true;
            
                while (clientConnected) {
                    try {
                        String incomingMessage = reader.readLine();
                        System.out.println(incomingMessage);
                        if (incomingMessage == null) {
                            // Client disconnected
                            System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
                            clientConnected = false;
                            break;
                        } else if (incomingMessage.startsWith("SERVER LOGIN")) { //LOGIN
                            messageArray = incomingMessage.split(" ");
                            if (messageArray.length > 2) {
                                username = messageArray[2].trim();
                                if (userExists(username)) {
                                    writer.println("CLIENT VALID PASS?");
                                    System.out.println("CLIENT VALID PASS?");
                                    incomingMessage = reader.readLine();
                                    System.out.println(incomingMessage);
                                    if(incomingMessage.startsWith("SERVER")) {
                                        messageArray = incomingMessage.split(" ");
                                        if (messageArray.length > 3) { 
                                            username = messageArray[2];
                                            pass = messageArray[3].toCharArray();
                                            IP = clientSocket.getInetAddress().toString();
                                            if (loginUser(username, pass, IP)) {
                                                writer.println("CLIENT VALID LOGGED IN");
                                                System.out.println("CLIENT VALID LOGGED IN");
                                                clientSocket.close();
                                            } else {
                                                writer.println("CLIENT LOGIN INVALID");
                                                clientSocket.close();
                                            }
                                        } else {
                                            writer.println("CLIENT LOGIN INVALID");
                                            clientSocket.close();
                                        }
                                    } else {
                                        writer.println("CLIENT LOGIN INVALID");
                                        clientSocket.close();
                                    }
                                } else {
                                    writer.println("CLIENT LOGIN INVALID");
                                    clientSocket.close();
                                }
                            } else {
                                writer.println("CLIENT LOGIN INVALID");
                                clientSocket.close();
                            }
                        } else if(incomingMessage.startsWith("SERVER LOGOUT")) { //LOGOUT
                            messageArray = incomingMessage.split(" ");
                            if (messageArray.length > 2) {
                                username = messageArray[2].trim();
                                if (userExists(username)) {
                                    if (logoutUser(username)) {
                                        System.out.println("CLIENT VALID LOGGED OUT");
                                        writer.println("CLIENT VALID LOGGED OUT");
                                        clientSocket.close();
                                    } else {
                                        writer.println("CLIENT LOGOUT 1 INVALID");
                                        clientSocket.close();
                                    }
                                } else {
                                    writer.println("CLIENT LOGOUT 2 INVALID");
                                    clientSocket.close();
                                } 
                            } else {
                                writer.println("CLIENT LOGOUT 3 INVALID");
                                clientSocket.close();
                            }
                        } else if (incomingMessage.startsWith("SERVER REGISTER")) { //REGISTER
                            messageArray = incomingMessage.split(" ");
                            if (messageArray.length > 2) {
                                username = messageArray[2].trim();
                                if (!userExists(username)) {
                                    writer.println("CLIENT VALID PASS?");
                                    System.out.println("CLIENT VALID PASS?");
                                    incomingMessage = reader.readLine();
                                    System.out.println(incomingMessage);
                                    if(incomingMessage.startsWith("SERVER")) {
                                        messageArray = incomingMessage.split(" ");
                                        if (messageArray.length > 3) {
                                            username = messageArray[2].trim();
                                            pass = messageArray[3].toCharArray();
                                            IP = clientSocket.getInetAddress().toString().replace("/", "");
                                            if (registerUser(username, pass, IP)) {
                                                writer.println("CLIENT VALID LOGGED IN");
                                                System.out.println("CLIENT VALID LOGGED IN");
                                                clientSocket.close();
                                            } else {
                                                writer.println("CLIENT REGISTER INVALID");
                                                System.out.println("CLIENT REGISTER INVALID");
                                                clientSocket.close();
                                            }
                                        } else {
                                            writer.println("CLIENT REGISTER INVALID");
                                            System.out.println("CLIENT REGISTER INVALID");
                                            clientSocket.close();
                                        }
                                    } else {
                                        writer.println("CLIENT REGISTER INVALID");
                                        System.out.println("CLIENT REGISTER INVALID");
                                        clientSocket.close();
                                    }
                                } else {
                                    writer.println("CLIENT REGISTER INVALID");
                                    System.out.println("CLIENT REGISTER INVALID");
                                    clientSocket.close();
                                }
                            } else {
                                writer.println("CLIENT REGISTER INVALID");
                                System.out.println("CLIENT REGISTER INVALID");
                                clientSocket.close();
                            }
                        } else if (incomingMessage.startsWith("SERVER FOLLOW")) {
                            messageArray = incomingMessage.split(" ");
                            if (messageArray.length > 3) {
                                String followee = messageArray[2].trim();
                                String follower = messageArray[3].trim();
                                if (!followee.equals(follower)) {
                                    if (userExists(followee) && userExists(follower)) {
                                        User followeeUser = users.get(followee);
                                        User followerUser = users.get(follower);
                                        if (isUserLoggedIn(followerUser)) {
                                            if (!followeeUser.followers.contains(follower) && !followerUser.following.contains(followee)) {
                                                followeeUser.addFollower(follower);
                                                followerUser.follow(followee);

                                                writer.println("CLIENT VALID FOLLOWED");
                                                System.out.println("CLIENT VALID FOLLOWED");
                                                clientSocket.close();
                                            } else {
                                                writer.println("CLIENT INVALID USER ALREADY FOLLOW");
                                                System.out.println("CLIENT INVALID USER ALREADY FOLLOW");
                                                clientSocket.close();
                                            }
                                        } else {
                                            writer.println("CLIENT INVALID USER FOLLOW");
                                            System.out.println("CLIENT INVALID USER FOLLOW");
                                            clientSocket.close();
                                        }
                                    } else {
                                        writer.println("CLIENT INVALID USER FOLLOW");
                                        System.out.println("CLIENT INVALID USER FOLLOW");
                                        clientSocket.close();
                                    }
                                } else {
                                    writer.println("CLIENT INVALID USER FOLLOW");
                                    System.out.println("CLIENT INVALID USER FOLLOW");
                                    clientSocket.close();
                                }
                            } else {
                                writer.println("CLIENT INVALID FOLLOW");
                                System.out.println("CLIENT INVALID FOLLOW");
                                clientSocket.close();
                            }
                        } else if (incomingMessage.startsWith("SERVER UNFOLLOW")) {
                            messageArray = incomingMessage.split(" ");
                            if (messageArray.length > 3) {
                                String unfollowee = messageArray[2].trim();
                                String unfollower = messageArray[3].trim();
                                
                                if (!unfollowee.equals(unfollower)) {
                                    if (userExists(unfollowee) && userExists(unfollower)) {
                                        User unfolloweeUser = users.get(unfollowee);
                                        User unfollowerUser = users.get(unfollower);
                                        if (isUserLoggedIn(unfollowerUser)) {
                                            if (unfolloweeUser.followers.contains(unfollower) && unfollowerUser.following.contains(unfollowee)) {
                                                unfolloweeUser.removeFollower(unfollower);
                                                unfollowerUser.unfollow(unfollowee);

                                                writer.println("CLIENT VALID UNFOLLOWED");
                                                System.out.println("CLIENT VALID UNFOLLOWED");
                                                clientSocket.close();
                                            } else {
                                                writer.println("CLIENT INVALID USER NOT FOLLOWED");
                                                System.out.println("CLIENT INVALID USER NOT FOLLOWED");
                                                clientSocket.close();
                                            }
                                        } else {
                                            writer.println("CLIENT INVALID USER UNFOLLOW");
                                            System.out.println("CLIENT INVALID USER UNFOLLOW");
                                            clientSocket.close();
                                        }
                                    } else {
                                        writer.println("CLIENT INVALID USER UNFOLLOW");
                                        System.out.println("CLIENT INVALID USER UNFOLLOW");
                                        clientSocket.close();
                                    }
                                } else {
                                    writer.println("CLIENT INVALID USER UNFOLLOW");
                                    System.out.println("CLIENT INVALID USER UNFOLLOW");
                                    clientSocket.close();
                                }
                            } else {
                                writer.println("CLIENT INVALID UNFOLLOW");
                                System.out.println("CLIENT INVALID UNFOLLOW");
                                clientSocket.close();
                            }
                        } else if (incomingMessage.startsWith("SERVER USERLIST")) {
                            messageArray = incomingMessage.split(" ");
                            if (messageArray.length > 2) {
                                String userListType = messageArray[2];
                                writer.println("CLIENT VALID SENDING USERLIST");
                                System.out.println("CLIENT VALID SENDING USERLIST");
                                incomingMessage = reader.readLine();
                                if (incomingMessage.startsWith("SERVER USERLIST CONTINUE")) {
                                    if ("ALL".equals(userListType)){
                                        for (String user : users.keySet()){
                                            writer.println("CLIENT USERLIST " + user);
                                            System.out.println("CLIENT USERLIST " + user);
                                            incomingMessage = reader.readLine();
                                            if (!incomingMessage.startsWith("SERVER USERLIST CONTINUE")){
                                                System.out.println("NOCONTINUE");
                                                break;
                                            } else {
                                                System.out.println(incomingMessage);
                                            }
                                        }
                                        writer.println("CLIENT USERLIST DONE");
                                        System.out.println("CLIENT USERLIST DONE");
                                        clientSocket.close();
                                    } else if (messageArray.length > 3) {
                                        username = messageArray[3].trim();
                                        if (userExists(username)) {
                                            User tempUser = users.get(username);
                                            LinkedList userList;
                                            switch (userListType) {
                                                case "FOLLOWING" -> {
                                                    userList = tempUser.following;
                                                }
                                                case "FOLLOWERS" -> {
                                                    userList = tempUser.followers;
                                                }
                                                default -> {
                                                    userList = new LinkedList<String>();
                                                }
                                            } 
                                            for (int i = 0; i < userList.size(); i++) {
                                                writer.println("CLIENT USERLIST " + userList.get(i));
                                                System.out.println("CLIENT USERLIST " + userList.get(i));
                                                incomingMessage = reader.readLine();
                                                if (!incomingMessage.startsWith("SERVER USERLIST CONTINUE")){
                                                    System.out.println("NOCONTINUE");
                                                    break;
                                                } else {
                                                    System.out.println(incomingMessage);
                                                }
                                            }
                                            writer.println("CLIENT USERLIST DONE");
                                            System.out.println("CLIENT USERLIST DONE");
                                            clientSocket.close();
                                        } else {
                                            writer.println("CLIENT USERLIST INVALID USER1");
                                            System.out.println("CLIENT USERLIST INVALID USER1");
                                            clientSocket.close();
                                        }
                                    } else {
                                        writer.println("CLIENT USERLIST INVALID USER2");
                                        System.out.println("CLIENT USERLIST INVALID USER2");
                                        clientSocket.close();
                                    }
                                } else {
                                    writer.println("CLIENT USERLIST NOCONFIRM INVALID");
                                    System.out.println("CLIENT USERLIST NOCONFIRM INVALID");
                                    clientSocket.close();
                                }
                            } else {
                                writer.println("CLIENT USERLIST TYPE INVALID");
                                System.out.println("CLIENT USERLIST TYPE INVALID");
                                clientSocket.close();
                            }
                        }  else if (incomingMessage.startsWith("SERVER SEND PUBLIC MESSAGE")) {
                            System.out.println("Step 1 " + incomingMessage);
                            messageArray = incomingMessage.split(" ");
                            if (messageArray.length > 5) {
                                username = messageArray[4].trim();
                                if (userExists(username)) {
                                    User tempUser = users.get(username);
                                    if (isUserLoggedIn(tempUser)) {
                                        //Build Message object
                                        String encodedBody = messageArray[5];
                                        String body = new String(Base64.getDecoder().decode(encodedBody));
                                        
                                        if (!body.isBlank()) {
                                            System.out.println("Step 2 " + body);
                                            
                                            if (messageArray.length > 6) {
                                                String encodedTags = messageArray[6];
                                                String[] tagArray = new String(Base64.getDecoder().decode(encodedTags)).split(";");

                                                if (tagArray.length != 0) {
                                                    Set<String> tags = new HashSet<>();
                                                    long ID = generateMessageId();
                                                    for (String tag : tagArray){
                                                        if (!tag.equals(" ")) {
                                                            tags.add(tag.trim());
                                                            tagIndex.computeIfAbsent(tag.trim(), k -> new HashSet<>()).add(ID);
                                                            System.out.println("Step 3 " + tag.trim());
                                                        }
                                                    }
                                                    
//                                                    tagIndex.forEach((k,v) -> {System.out.println(k + " -> " + v); });


                                                    Message msg = tempUser.newPublicMessage(body, tags, ID);
                                                    
                                                    if (msg != null) {
                                                        messageStore.put(ID, msg);
                                                        writer.println("CLIENT VALID MESSAGE SENT");
                                                        System.out.println("CLIENT VALID MESSAGE SENT");
                                                        clientSocket.close();
                                                    } else {
                                                        writer.println("CLIENT MESSAGE INVALID RECIPIENT");
                                                        System.out.println("CLIENT MESSAGE INVALID RECIPIENT");
                                                        clientSocket.close();
                                                    }
                                                } else {
                                                    writer.println("CLIENT MESSAGE INVALID NO TAGS");
                                                    System.out.println("CLIENT MESSAGE INVALID NO TAGS");
                                                    clientSocket.close();
                                                }
                                            } else {
                                                writer.println("CLIENT MESSAGE INVALID NO TAGS");
                                                System.out.println("CLIENT MESSAGE INVALID NO TAGS");
                                                clientSocket.close();
                                            }
                                        } else {
                                            writer.println("CLIENT MESSAGE INVALID NO BODY");
                                            System.out.println("CLIENT MESSAGE INVALID NO BODY");
                                            clientSocket.close();
                                        }
                                    } else {
                                        writer.println("CLIENT MESSAGE INVALID");
                                        System.out.println("CLIENT MESSAGE INVALID");
                                        clientSocket.close();
                                    }
                                } else {
                                    writer.println("CLIENT MESSAGE INVALID");
                                    System.out.println("CLIENT MESSAGE INVALID");
                                    clientSocket.close();
                                }
                            } else {
                                writer.println("CLIENT MESSAGE INVALID");
                                System.out.println("CLIENT MESSAGE INVALID");
                                clientSocket.close();
                            }
                        } else if (incomingMessage.startsWith("SERVER RETRIEVE MESSAGES")) {
                            if (incomingMessage.startsWith("SERVER RETRIEVE MESSAGES TO")) {
                                messageArray = incomingMessage.split(" ");
                                if (messageArray.length > 4) {
                                    username = messageArray[4].trim();
                                    if (userExists(username)) {
                                        User tempUser = users.get(username);
                                        if (isUserLoggedIn(tempUser)) {
                                            writer.println("CLIENT VALID SENDING MESSAGES");
                                            System.out.println("CLIENT VALID SENDING MESSAGES");
                                            incomingMessage = reader.readLine();
                                            if (incomingMessage.startsWith("SERVER MESSAGE RETRIEVAL CONTINUE")) {
                                                while (incomingMessage.startsWith("SERVER MESSAGE RETRIEVAL CONTINUE")) {
                                                    Message msg = tempUser.retrievePublicMessage();
                                                    if (msg != null) {
                                                        String encodedBody = Base64.getEncoder().encodeToString(msg.body.getBytes());
                                                        StringBuilder sb = new StringBuilder();
                                                        for (String tag : msg.tags) {
                                                            if (sb.length() > 0) {
                                                                sb.append("; ");
                                                            }
                                                            sb.append(tag);
                                                        }
                                                        String joinedTags = sb.toString();

                                                        String encodedTags = Base64.getEncoder().encodeToString(joinedTags.getBytes());
                                                        writer.println("CLIENT MESSAGE " + msg.from + " " + encodedBody + " " + encodedTags);
                                                        System.out.println("CLIENT MESSAGE " + msg.from + " " + encodedBody + " " + encodedTags);

                                                        incomingMessage = reader.readLine();
                                                        if (!incomingMessage.startsWith("SERVER MESSAGE RETRIEVAL CONTINUE")){
                                                            System.out.println("NO CONTINUE");
                                                            break;
                                                        } else {
                                                            System.out.println(incomingMessage);
                                                        }
                                                    } else {
                                                        writer.println("CLIENT MESSAGE RETRIEVAL DONE");
                                                        System.out.println("CLIENT MESSAGE RETRIEVAL DONE");
                                                        clientSocket.close();
                                                        break;
                                                    }
                                                }
                                            } else {
                                                writer.println("CLIENT MESSAGE RETRIEVAL ERROR");
                                                System.out.println("CLIENT MESSAGE RETRIEVAL ERROR");
                                                clientSocket.close();
                                            }
                                        } else {
                                            writer.println("CLIENT MESSAGE RETRIEVAL ERROR");
                                            System.out.println("CLIENT MESSAGE RETRIEVAL ERROR");
                                            clientSocket.close();
                                        }
                                    } else {
                                        writer.println("CLIENT MESSAGE RETRIEVAL ERROR");
                                        System.out.println("CLIENT MESSAGE RETRIEVAL ERROR");
                                        clientSocket.close();
                                    }
                                } else {
                                    writer.println("CLIENT MESSAGE RETRIEVAL ERROR");
                                    System.out.println("CLIENT MESSAGE RETRIEVAL ERROR");
                                    clientSocket.close();
                                }
                            } else if (incomingMessage.startsWith("SERVER RETRIEVE MESSAGES LIKE")) {
                                messageArray = incomingMessage.split(" ");
                                if (messageArray.length > 4) {
                                    String encodedTags = messageArray[4];
                                    String[] tagArray = new String(Base64.getDecoder().decode(encodedTags)).split(";");
                                    if (tagArray.length != 0) {
                                        Set<Long> resultIds = new HashSet<>();
                                        for (String tag : tagArray){
                                            if (!tag.equals(" ")) {
                                                resultIds.addAll(tagIndex.getOrDefault(tag, Collections.emptySet()));
                                            }
                                        }
                                        writer.println("CLIENT VALID SENDING MESSAGES");
                                        System.out.println("CLIENT VALID SENDING MESSAGES");
                                        incomingMessage = reader.readLine();
                                        if (incomingMessage.startsWith("SERVER MESSAGE RETRIEVAL CONTINUE")) {
                                            for(Long ID : resultIds) {
                                                Message msg = messageStore.get(ID);
                                                if (msg != null) {
                                                    String encodedBody = Base64.getEncoder().encodeToString(msg.body.getBytes());
                                                    StringBuilder sb = new StringBuilder();
                                                    for (String tag : msg.tags) {
                                                        if (sb.length() > 0) {
                                                            sb.append("; ");
                                                        }
                                                        sb.append(tag);
                                                    }
                                                    String joinedTags = sb.toString();

                                                    String msgEncodedTags = Base64.getEncoder().encodeToString(joinedTags.getBytes());
                                                    writer.println("CLIENT MESSAGE " + msg.from + " " + encodedBody + " " + msgEncodedTags);
                                                    System.out.println("CLIENT MESSAGE " + msg.from + " " + encodedBody + " " + msgEncodedTags);

                                                    incomingMessage = reader.readLine();
                                                    if (!incomingMessage.startsWith("SERVER MESSAGE RETRIEVAL CONTINUE")){
                                                        System.out.println("NO CONTINUE");
                                                        break;
                                                    } else {
                                                        System.out.println(incomingMessage);
                                                    }
                                                }
                                            }
                                            writer.println("CLIENT MESSAGE RETRIEVAL DONE");
                                            System.out.println("CLIENT MESSAGE RETRIEVAL DONE");
                                            clientSocket.close();
                                        } 
                                    }      
                                }
                            } else {
                                writer.println("CLIENT MESSAGE RETRIEVAL ERROR");
                                System.out.println("CLIENT MESSAGE RETRIEVAL ERROR");
                                clientSocket.close();
                            }
                        } else if (incomingMessage.startsWith("SERVER RETRIEVE NOTIFICATIONS TO")) {
                            messageArray = incomingMessage.split(" ");
                                if (messageArray.length > 4) {
                                    username = messageArray[4].trim();
                                    if (userExists(username)) {
                                        User tempUser = users.get(username);
                                        if (isUserLoggedIn(tempUser)) {
                                            writer.println("CLIENT VALID SENDING NOTIFICATIONS");
                                            System.out.println("CLIENT VALID SENDING NOTIFICATIONS");
                                            incomingMessage = reader.readLine();
                                            if (incomingMessage.startsWith("SERVER NOTIFICATION RETRIEVAL CONTINUE")) {
                                                while (incomingMessage.startsWith("SERVER NOTIFICATION RETRIEVAL CONTINUE")) {
                                                    Notification notif = tempUser.retrieveNotification();
                                                    if (notif != null) {
                                                        String encodedBody = Base64.getEncoder().encodeToString(notif.getText().getBytes());
                                                        
                                                        writer.println("CLIENT NOTIFICATION " + notif.getType() + " " + encodedBody);
                                                        System.out.println("CLIENT NOTIFICATION " + notif.getType() + " " + encodedBody);

                                                        incomingMessage = reader.readLine();
                                                        if (!incomingMessage.startsWith("SERVER NOTIFICATION RETRIEVAL CONTINUE")){
                                                            System.out.println("NO CONTINUE");
                                                            break;
                                                        } else {
                                                            System.out.println(incomingMessage);
                                                        }
                                                    } else {
                                                        writer.println("CLIENT NOTIFICATION RETRIEVAL DONE");
                                                        System.out.println("CLIENT NOTIFICATION RETRIEVAL DONE");
                                                        clientSocket.close();
                                                        break;
                                                    }
                                                }
                                            } else {
                                                writer.println("CLIENT NOTIFICATION RETRIEVAL ERROR");
                                                System.out.println("CLIENT NOTIFICATION RETRIEVAL ERROR");
                                                clientSocket.close();
                                            }
                                        } else {
                                            writer.println("CLIENT NOTIFICATION RETRIEVAL ERROR");
                                            System.out.println("CLIENT NOTIFICATION RETRIEVAL ERROR");
                                            clientSocket.close();
                                        }
                                    } else {
                                        writer.println("CLIENT NOTIFICATION RETRIEVAL ERROR");
                                        System.out.println("CLIENT NOTIFICATION RETRIEVAL ERROR");
                                        clientSocket.close();
                                    }
                                } else {
                                    writer.println("CLIENT NOTIFICATION RETRIEVAL ERROR");
                                    System.out.println("CLIENT NOTIFICATION RETRIEVAL ERROR");
                                    clientSocket.close();
                                }
                        } else if (incomingMessage.startsWith("SERVER ADMIN")) {
                            if (incomingMessage.equals("SERVER ADMIN SHUTDOWN")) {
                                serverOn = false;
                                writer.println("CLIENT ADMIN SHUTTING DOWN");
                                System.out.println("CLIENT ADMIN SHUTTING DOWN");
                                clientSocket.close();
                            } else if (incomingMessage.startsWith("SERVER ADMIN CHANGE PASS")) {
                                messageArray = incomingMessage.split(" ");
                                if (messageArray.length > 5) {
                                    username = messageArray[4].trim();
                                    pass = messageArray[5].toCharArray();
                                    if (userExists(username)) {
                                        User tempUser = users.get(username);
                                        tempUser.password = pass;
                                        
                                        writer.println("CLIENT ADMIN CHANGED PASS");
                                        System.out.println("CLIENT ADMIN CHANGED PASS");
                                        clientSocket.close();
                                    } else {
                                        writer.println("CLIENT ADMIN USER INVALID");
                                        System.out.println("CLIENT ADMIN USER INVALID");
                                        clientSocket.close();
                                    }
                                } else {
                                    writer.println("CLIENT ADMIN CHANGE PASS INVALID");
                                    System.out.println("CLIENT ADMIN USER INVALID");
                                    clientSocket.close();
                                }
                            }
                        }
                    } catch (SocketException se) {
                        System.out.println("Connection was closed by the client.");
                        clientConnected = false;
                        break;
                    }
                }
            }          
        } catch (IOException ex) {
            Logger.getLogger(MessageServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static Boolean loginUser(String username, char[] pass, String IP) {
        if (userExists(username)) {
            User tempUser = users.get(username);
            if (!isUserLoggedIn(tempUser)) {
//                System.out.println("User password: " + tempUser.password);
                if (tempUser.login(pass, IP)) {
                    activeUsers.add(tempUser);
                    return true;
                }
            } // else, user is logged in elsewhere, doesn't exist, or incorrect password
        }
        return false;
    }
    public static Boolean logoutUser(String username) {
        if (userExists(username)) {
            User tempUser = users.get(username);
            if (isUserLoggedIn(tempUser)) {
                if (tempUser.logout()) {
                    activeUsers.remove(tempUser);
                    return true;
                }
            }  
        }
        return false;
    }
    
    public static Boolean registerUser(String username, char[] pass, String IP) {
        if (!userExists(username)) {  
            User newUser = new User(username, pass);
            users.put(username, newUser);

//            System.out.println(users.size());

            newUser.login(pass, IP);
            activeUsers.add(newUser);
            
            
            //For Testing Purposes
            user1.follow(username);
            user2.follow(username);
            newUser.follow(user1.screenName);
            newUser.follow(user2.screenName);
            
            newUser.addFollower(user1.screenName);
            newUser.addFollower(user2.screenName);
            user1.addFollower(username);
            user2.addFollower(username);
            
//            Set<String> tags = new HashSet<>();
//            
//            tags.add("Welcome");
//            tags.add("HelloWorld");
//            
//            Message msg1 = user1.newPublicMessage("Welcome to the new system!", tags, generateMessageId());
//            Message msg2 = user2.newPublicMessage("Hey " + username + "!", tags, generateMessageId());
//            
//            for (String tag : tags){
//                tagIndex.computeIfAbsent(tag, k -> new HashSet<>()).add(msg1.ID);
//                tagIndex.computeIfAbsent(tag, k -> new HashSet<>()).add(msg2.ID);
//            }
//            messageStore.put(msg1.ID, msg1);
//            messageStore.put(msg2.ID, msg2);
//            
//            newUser.queuePublicMessage(msg1);
//            newUser.queuePublicMessage(msg2);
            

            return true;  
        }
        return false;
    }
    public static Boolean userExists(String username) {
//        for (String key : users.keySet()) {
//            System.out.println("Key: " + key);
//        }
        return users.containsKey(username);
    }
    public static Boolean isUserLoggedIn(User username) {
        return activeUsers.contains(username);
    }

    public static synchronized long generateMessageId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp != lastTimestamp) {
            lastTimestamp = timestamp;
        }
        return timestamp + (messageCounter++);
    }
    
    
    public static User getUser(String username) {
        return users.get(username);
    }
}
