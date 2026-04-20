package org.example.wishlist.service;

import java.util.List;

import org.example.wishlist.exception.InvalidInputException;
import org.example.wishlist.exception.UserAlreadyExistsException;
import org.example.wishlist.model.User;
import org.example.wishlist.model.Wishlist;
import org.example.wishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;

@Service
public class WishlistService {

    private final WishlistRepository repository;

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

    public void forgotCredentials(String email) {
        // beep boop
        // gider slet ik engang tænke på hvordan man ville implementere det her fis
        // beep boop
        // send mail til bruger med éngangs adgangskode eller noget
    }

    public WishlistService(WishlistRepository repository) {
        this.repository = repository;
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
            throw new InvalidInputException("Wishlist cannot be null");
        }
        return repository.saveWishlist(wishlist);
    }

    public void updateWishlist(Wishlist wishlist) {
    }

    public void deleteWishlist(Wishlist wishlist) {
    }
    public void deleteUser(User user){repository.deleteUser(user);}
}