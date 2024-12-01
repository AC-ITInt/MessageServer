/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package messageserver;

/**
 *
 * @author andre
 */
public class Notification {
    private int type;
    private String text;
    
    public Notification(int type, String text) {
        this.type = type;
        this.text = text;
        
    }
    
    public String getText(){
        return this.text;
    }
    
    public int getType() {
        return this.type;
    }
    
}
