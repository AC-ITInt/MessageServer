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
    private String from;
    private String body;
    private Set<String> tags;
    private long ID;
    
    public Message(String from, String[] recipients, String body, Set<String> tags, long ID) {
        this.from = from;
        this.body = body;
        this.tags = tags;
        this.ID = ID;
    }
    
    public String getSender() {
        return from;
    }
    
    public String getBody() {
        return body;
    }
    
    public Set<String> getTags() {
        return tags;
    }
    
    public long getID() {
        return ID;
    }
}
