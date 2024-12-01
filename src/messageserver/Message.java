/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package messageserver;

import java.util.Set;

/**
 *
 * @author andre
 */
public class Message {
    String from;
    String[] recipients;
    String body;
    Set<String> tags;
    long ID;
    
    public Message(String from, String[] recipients, String body, Set<String> tags, long ID) {
        this.from = from;
        this.recipients = recipients;
        this.body = body;
        this.tags = tags;
        this.ID = ID;
    }
}
