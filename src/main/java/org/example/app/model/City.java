package org.example.app.model;

import java.util.HashMap;
import java.util.Map;

public record City(int id, String name) {

    private static int counter=0;
    public final static Map<Integer,City> cities=new HashMap<>();
    public City(String name){
        this(counter++,name);
        cities.put(this.id,this);
    }
}
