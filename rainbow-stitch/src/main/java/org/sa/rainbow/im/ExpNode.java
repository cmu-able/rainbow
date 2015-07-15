package org.sa.rainbow.im;

import java.util.ArrayList;

public class ExpNode {

    public String id;

    public String type;

    public String text;

    public String setExpArchTypeSelector;

    public String setExpArchVar;

    public String setExpArchConstraintSelector;

    public ArrayList<String> subexp;

    public ArrayList<String> p;

    public ExpNode(String id, String type, String text) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.subexp = new ArrayList<String>();
        this.p = new ArrayList<String>();
    }

    @Override
    public String toString() {
        return "ID: " + this.id + " TYPE: " + this.type + " TEXT: " + this.text + p.toString()+subexp.toString();

    }

}