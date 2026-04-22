package org.example.wishlist.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService service;

    public WishlistController(WishlistService service) {
        this.service = service;
    }

    @GetMapping({"", "/"})
    public String index() {
        return "index";
    }

    @GetMapping("/inspiration")
    public String inspirationPage(HttpSession session, Model model) {
        if (isLoggedIn(session)) {
            model.addAttribute("isLoggedIn", true);
        }
        return "gave-inspiration";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/wishlist/";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (isLoggedIn(session)) {
            var user = (User) session.getAttribute("user");
            return "redirect:/wishlist/" + user.getUsername();
        }
        return "login-page";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password, HttpSession session, Model model) {
        try {
            var user = service.userLogin(username, password);
            session.setAttribute("user", user);
            return "redirect:/wishlist/" + username;
        } catch (Exception e) {
            model.addAttribute("wrongCredentials", true);
            return "login-page";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register-page";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, HttpSession session, Model model) {
        try {
            final var registeredUser = service.registerUser(user);
            session.setAttribute("user", registeredUser);
            return "redirect:/wishlist/" + registeredUser.getUsername();
        } catch (InvalidInputException | UserAlreadyExistsException e) {
            model.addAttribute("registrationError", true);
            return "register-page";
        }
    }

    @GetMapping("/{username}")
    public String findUsersWishlists(@PathVariable String username, Model model, HttpSession session) {
        if (isOwner(session, username)) {
            model.addAttribute("isOwner", true);
        }

        var wishlists = service.findUsersWishlists(username);
        model.addAttribute("wishlists", wishlists);
        model.addAttribute("username", username);
        return "user-wishlists";
    }

//    @GetMapping("/{username}/{wishlistName}")
//    public String findWishlist(@PathVariable String username,
//                               @PathVariable String wishlistName, Model model, HttpSession session) {
//        model.addAttribute("wishlist", service.findWishlist(username, wishlistName));
//        model.addAttribute("username", username);
//        model.addAttribute("isOwner", isOwner(session, username));
//        return "user-wishlist";
//    }

    @GetMapping("/{username}/{wishlistName}")
    public String findWishlist(@PathVariable String username,
                               @PathVariable String wishlistName,
                               Model model,
                               HttpSession session) {

        System.out.println("Lookup username = [" + username + "]");
        System.out.println("Lookup wishlistName = [" + wishlistName + "]");

        model.addAttribute("wishlist", service.findWishlist(username, wishlistName));
        model.addAttribute("username", username);
        model.addAttribute("isOwner", isOwner(session, username));
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
        model.addAttribute("isEdit", true);

        return "wishlist-form";
    }

    @GetMapping("/add")
    public String addWishlist(Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/wishlist/login";
        }
        var wishlist = new Wishlist();
        wishlist.setWishes(new ArrayList<>(List.of(new Wish())));
        var loggedInUser = (User) session.getAttribute("user");

        model.addAttribute("user", loggedInUser);
        model.addAttribute("wishlist", wishlist);
        model.addAttribute("isEdit", false);
        return "wishlist-form";
    }

    @PostMapping("/save")
    public String saveWishlist(@ModelAttribute Wishlist wishlist, HttpSession session) {
        if (!isLoggedIn(session)) {
            throw new UnauthenticatedException("You need to be logged in to create wishlists");
        }

        var loggedInUser = (User) session.getAttribute("user");
        wishlist.setUserId(loggedInUser.getId());

        if (wishlist.getName() != null) {
            wishlist.setName(wishlist.getName().trim());
        }

        var savedWishlist = service.saveWishlist(wishlist);

        String encodedName = org.springframework.web.util.UriUtils.encodePathSegment(
                savedWishlist.getName().trim(),
                StandardCharsets.UTF_8
        );

        return "redirect:/wishlist/" + loggedInUser.getUsername() + "/" + encodedName;
    }

    @PostMapping("/update")
    public String updateWishlist(@ModelAttribute Wishlist wishlist, HttpSession session) {
        if (!isLoggedIn(session)) {
            throw new UnauthenticatedException("You need to be logged in to update a wishlist");
        }
        if (isNotOwner(session, wishlist.getUserId())) {
            throw new ForbiddenAccessException("You are not allowed to update this wishlist");
        }
        var user = (User) session.getAttribute("user");
        service.updateWishlist(user.getUsername(), wishlist);
        return "redirect:/wishlist/" + user.getUsername();

    }

    @PostMapping("/delete")
    public String deleteWishlist(@ModelAttribute Wishlist wishlist, HttpSession session) {
        if (!isLoggedIn(session)) {
            throw new UnauthenticatedException("You need to be logged in to delete a wishlist");
        }
        if (isNotOwner(session, wishlist.getUserId())) {
            throw new ForbiddenAccessException("You are not allowed to delete this wishlist");
        }
        var user = (User) session.getAttribute("user");
        service.deleteWishlist(wishlist);
        return "redirect:/wishlist/" + user.getUsername();
    }

    @PostMapping("/delete-user")
    public String deleteUser(@ModelAttribute User user, HttpSession session) {
        if (!isLoggedIn(session)) {
            throw new UnauthenticatedException("You need to be logged in to delete your account");
        }
        if (isNotOwner(session, user.getId())) {
            throw new ForbiddenAccessException("You are not allowed to delete this account");
        }

        service.deleteUser(user);
        session.invalidate();
        return "redirect:/wishlist/login";
    }

    // hjælpe metode

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    private boolean isOwner(HttpSession session, String username) {
        if (!isLoggedIn(session))
            return false;
        var loggedInUser = (User) session.getAttribute("user");
        return loggedInUser.getUsername().equals(username);
    }

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
}
