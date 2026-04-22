package org.example.wishlist.service;

import org.example.wishlist.exception.InvalidInputException;
import org.example.wishlist.exception.UserAlreadyExistsException;
import org.example.wishlist.model.User;
import org.example.wishlist.model.Wishlist;
import org.example.wishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository repository;

    public WishlistService(WishlistRepository repository) {
        this.repository = repository;
    }

    public User userLogin(String username, String password) {
        if (username == null || password == null) {
            throw new InvalidInputException("Username or password cannot be null");
        }
        return repository.login(username, password);
    }

    public User registerUser(User user) {
        if (repository.userExists(user.getUsername())) {
            throw new UserAlreadyExistsException("User already exists");
        }
        return repository.registerUser(user);
    }

    public List<Wishlist> findUsersWishlists(String username) {
        var user = repository.findUser(username);
        return user.getWishlists();
    }

    public Wishlist findWishlist(String username, String wishlistName) {
        return repository.findWishlist(username, wishlistName);
    }

    public Wishlist saveWishlist(Wishlist wishlist) {
        if (wishlist == null) {
            throw new InvalidInputException("Wishlist cannot be empty");
        }
        return repository.saveWishlist(wishlist);
    }

    public void updateWishlist(String username, Wishlist wishlist) {
        repository.updateWishlist(username, wishlist);
    }

    public void deleteWishlist(Wishlist wishlist) {
        if (!repository.deleteWishlist(wishlist)) {
            throw new RuntimeException("Something went wrong, wishlist wasn't deleted");
        }
    }

    public void deleteUser(User user) {
        if (!repository.deleteUser(user)) {
            throw new RuntimeException("Something went wrong, user wasn't deleted");
        }
    }

}