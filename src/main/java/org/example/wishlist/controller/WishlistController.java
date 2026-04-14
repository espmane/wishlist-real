package org.example.wishlist.controller;

import org.example.wishlist.model.Wishlist;
import org.example.wishlist.service.WishlistService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WishlistController {

    private final WishlistService service;

    public WishlistController(WishlistService service) {
        this.service = service;
    }

    // en forside?
    @GetMapping()
    public String frontPage() {
        return "front-page";
    }

    // returnere alle ønskelister fra en bruger?
    @GetMapping("/{username}")
    public String findUsersWishlists(@PathVariable String username, Model model) {
        service.findUsersWishlists(username);
        return "user-wishlists";
    }

    // returnere specifik wishlist fra specifik bruger?
    @GetMapping("/{username}/{wishlistName}")
    public String findWishlist(@PathVariable String username, @PathVariable String wishlistName, Model model) {
        service.findWishlist(username, wishlistName);
        return "user-wishlist";
    }

    @GetMapping("/{username}/{wishlistName}/edit")
    public String editWishlist(@PathVariable String username, @PathVariable String wishlistName, Model model) {
        final var wishlist = service.findWishlist(username, wishlistName);
        model.addAttribute("wishlist", wishlist);

        return "edit-wishlist"; // udfylder en form og sender til update endpoint
    }

    @GetMapping("/add")
    public String addWishlist(Model model) {
        model.addAttribute("wishlist", new Wishlist());

        return "add-wishlist"; // udfylder en form og sender til save endpoint
    }

    @PostMapping("/save")
    public String saveWishlist(@ModelAttribute Wishlist wishlist) {
        service.saveWishlist(wishlist);
        return "redirect:/";
    }

    @PostMapping("/update")
    public String updateWishlist(@ModelAttribute Wishlist wishlist) {
        service.updateWishlist(wishlist);
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String deleteWishlist(@ModelAttribute Wishlist wishlist) {
        service.deleteWishlist(wishlist);
        return "redirect:/";
    }
}
