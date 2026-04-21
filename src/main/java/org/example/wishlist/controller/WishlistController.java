package org.example.wishlist.controller;

import java.util.ArrayList;
import java.util.List;

import org.example.wishlist.exception.ForbiddenAccessException;
import org.example.wishlist.exception.InvalidInputException;
import org.example.wishlist.exception.UnauthenticatedException;
import org.example.wishlist.exception.UserAlreadyExistsException;
import org.example.wishlist.model.User;
import org.example.wishlist.model.Wish;
import org.example.wishlist.model.Wishlist;
import org.example.wishlist.service.WishlistService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class WishlistController {

    private final WishlistService service;

    public WishlistController(WishlistService service) {
        this.service = service;
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    @GetMapping("/html")
    public String page() {
        return "Login-page";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (isLoggedIn(session)) {
            var user = (User) session.getAttribute("user");
            return "redirect:/" + user.getUsername();
        }
        return "login-page"; // udfyld form med thymeleaf og submit til /login postmap
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password, HttpSession session, Model model) {
        try {
            var user = service.userLogin(username, password);
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(30);
            return "redirect:/" + username;
        } catch (Exception e) {
            model.addAttribute("wrongCredentials", true);
            return "login-page";
        }
    }

//    @PostMapping("/forgot-password")
//    public String forgotCredentials(@RequestAttribute String email) {
//        service.forgotCredentials(email);
//        return "redirect:/login";


    @GetMapping("/register")
    public String regiserPage(Model model) {
        model.addAttribute("user", new User());
        return "register-page"; // udfyld form med thymeleaf og submit til /register postmap
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, HttpSession session, Model model) {
        try {
            var registeredUser = service.registerUser(user);
            session.setAttribute("user", registeredUser);
            session.setMaxInactiveInterval(300);
            return "redirect:/" + registeredUser.getUsername();
        } catch (InvalidInputException | UserAlreadyExistsException e) {
            model.addAttribute("wrongCredentials", true);
            return "register-page";
        }
    }

    // en forside?
    @GetMapping()
    public String frontPage(HttpSession session) {
        return "front-page";
    }

    // returnere alle ønskelister fra en bruger?
    @GetMapping("/{username}")
    public String findUsersWishlists(@PathVariable String username, Model model, HttpSession session) {
        if (isLoggedIn(session)) {
            var loggedInUser = (User) session.getAttribute("user");
            var wishlists = service.findUsersWishlists(loggedInUser.getUsername());
            model.addAttribute("wishlists", wishlists);
            model.addAttribute("user", loggedInUser);
            return "user-wishlists";
        }
        return "redirect:/login";
    }

    // returnere specifik wishlist fra specifik bruger?
    @GetMapping("/{username}/{wishlistName}")
    public String findWishlist(@PathVariable String username,
                               @PathVariable String wishlistName, Model model) {
        model.addAttribute("wishlist", service.findWishlist(username, wishlistName));
        return "user-wishlist";
    }

    @GetMapping("/{username}/{wishlistName}/edit")
    public String editWishlist(@PathVariable String username,
                               @PathVariable String wishlistName, Model model, HttpSession session) {
        if (isNotOwner(session, username)) {
            throw new ForbiddenAccessException("You are not allowed to edit this wishlist");
        }

        final var wishlist = service.findWishlist(username, wishlistName);
        model.addAttribute("wishlist", wishlist);

        return "edit-wishlist"; // udfylder en form og sender til /update endpoint
    }

    @GetMapping("/add")
    public String addWishlist(Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        var loggedInUser = (User) session.getAttribute("user");
        var wishlist = new Wishlist();
        wishlist.setWishes(new ArrayList<>(List.of(new Wish())));
        wishlist.setUserId(loggedInUser.getId());

        model.addAttribute("wishlist", wishlist);
        return "add-wishlist";
    }

    @PostMapping("/save")
    public String saveWishlist(@ModelAttribute Wishlist wishlist, HttpSession session) {
        if (!isLoggedIn(session)) {
            throw new UnauthenticatedException("You need to be logged in to create wishlists");
        }
        var loggedInUser = (User) session.getAttribute("user");
        var savedWishlist = service.saveWishlist(wishlist);

        return "redirect:/" + loggedInUser.getUsername() + "/" + savedWishlist.getName();
    }

    @PostMapping("/update")
    public String updateWishlist(@ModelAttribute Wishlist wishlist, HttpSession session) {
        if (isNotOwner(session, wishlist.getUserId())) {
            throw new ForbiddenAccessException("You are not allowed to update this wishlist");
        }
        if (!isLoggedIn(session)) {
            throw new UnauthenticatedException("You need to be logged in to update a wishlist");
        }
        var user = (User) session.getAttribute("user");

        service.updateWishlist(user.getUsername(), wishlist);
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String deleteWishlist(@ModelAttribute Wishlist wishlist, HttpSession session) {
        if (isNotOwner(session, wishlist.getUserId())) {
            throw new ForbiddenAccessException("You are not allowed to delete this wishlist");
        }

        service.deleteWishlist(wishlist);
        return "redirect:/";
    }

    // hjælpe metode

    private boolean isNotOwner(HttpSession session, String username) {
        if (!isLoggedIn(session))
            return true;
        var loggedInUser = (User) session.getAttribute("user");
        return !loggedInUser.getUsername().equals(username);
    }

    private boolean isNotOwner(HttpSession session, int userId) {
        if (!isLoggedIn(session))
            return true;
        var loggedInUser = (User) session.getAttribute("user");
        return loggedInUser.getId() != userId;
    }

    @PostMapping
    public String deleteUSer(@ModelAttribute User user) {
        service.deleteUser(user);
        return "redirect:/";
    }
}
