package org.example.wishlist.model;

import java.util.List;

public class User {

    private String name;
    private String email;
    private int ID;
    private List<Wishlist> wishlists;

    public User(String name, String email, int ID, List<Wishlist> wishlists) {
        this.name = name;
        this.email = email;
        this.ID = ID;
        this.wishlists = wishlists;
    }

    public User(String name, String email, int ID) {
        this.name = name;
        this.email = email;
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public List<Wishlist> getWishlists() {
        return wishlists;
    }

    public void setWishlists(List<Wishlist> wishlists) {
        this.wishlists = wishlists;
    }
}
