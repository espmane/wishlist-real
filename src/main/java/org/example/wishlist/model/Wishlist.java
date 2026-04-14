package org.example.wishlist.model;

import java.util.List;

public class Wishlist {

    private int ID = 0;
    private String name;
    private List<Wish> wishlist;

    public Wishlist() {
    }

    public Wishlist(int ID, List<Wish> wishlist, String name) {
        this.ID = ID;
        this.wishlist = wishlist;
        this.name = name;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Wish> getWishlist() {
        return wishlist;
    }

    public void setWishlist(List<Wish> wishlist) {
        this.wishlist = wishlist;
    }
}
