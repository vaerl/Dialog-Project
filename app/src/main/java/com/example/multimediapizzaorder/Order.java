package com.example.multimediapizzaorder;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Order implements Serializable {

    private static int COUNTER = 1;

    public int id;
    public String name;
    public String size;
    public String dough;
    public String topping;

    public Order(String name){
        this.id = COUNTER++;
        this.name = name;
    }

    public Order(String name, String size, String dough, String topping) {
        this(name);
        this.size = size;
        this.dough = dough;
        this.topping = topping;
    }

    @NonNull
    @Override
    public String toString() {
        return "ORDER " + id + " by " + name + ": " + topping + "-Pizza mit " + dough + "-Teig in " + size + ".";
    }
}
