package org.example.app.model;

public record Asset(int id,String name,int city) {

    private static int counter=0;
    public Asset(String name,int city){
        this(counter++,name,city);
    }

}
