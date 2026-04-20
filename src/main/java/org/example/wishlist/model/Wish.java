package org.example.wishlist.model;

public class Wish {

    private int id;
    private String link;
    private String name;
    private double price;

    public Wish(int id, String link, String name, double price) {
        this.id = id;
        this.link = link;
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}

