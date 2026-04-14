package org.example.wishlist.service;

import java.util.List;

import org.example.wishlist.model.Wishlist;
import org.example.wishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;

@Service
public class WishlistService {

    private final WishlistRepository repository;

    public WishlistService(WishlistRepository repository) {
        this.repository = repository;
    }

    public List<Wishlist> findUsersWishlists(String username) {
        return repository.findUsersWishlists(username);
    }

    public Wishlist findWishlist(String username, String wishlistName) {
        return repository.findWishlist(username, wishlistName);
    }

    public void saveWishlist(Wishlist wishlist) {
    }

    public void updateWishlist(Wishlist wishlist) {
    }

    public void deleteWishlist(Wishlist wishlist) {
    }
}
