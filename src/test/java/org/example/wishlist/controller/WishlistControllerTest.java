package org.example.wishlist.controller;

import org.example.wishlist.exception.InvalidInputException;
import org.example.wishlist.model.User;
import org.example.wishlist.model.Wish;
import org.example.wishlist.model.Wishlist;
import org.example.wishlist.service.WishlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishlistService service;

    @Test
    void index() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(get(""))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void inspirationPage() throws Exception {
        var user = new User(1, "user", "password", List.of());

        mockMvc.perform(get("/wishlist/inspiration")
                        .sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("gave-inspiration"))
                .andExpect(model().attribute("isLoggedIn", true));
    }

    @Test
    void logout() throws Exception {
        var user = new User(1, "user", "password", List.of());

        mockMvc.perform(get("/wishlist/logout")
                        .sessionAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlist/"));
    }

    @Test
    void loginPage() throws Exception {
        mockMvc.perform(get("/wishlist/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login-page"));
    }

    @Test
    void login_correct() throws Exception {
        var user = new User(1, "user", "password", List.of());

        when(service.userLogin("user", "password")).thenReturn(user);
        mockMvc.perform(post("/wishlist/login")
                        .param("username", "user")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlist/user"));
    }

    @Test
    void login_fail() throws Exception {
        when(service.userLogin("user", "password"))
                .thenThrow(new InvalidInputException("Username or password cannot be null"));

        mockMvc.perform(post("/wishlist/login")
                        .param("username", "user")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("login-page"))
                .andExpect(model().attributeExists("wrongCredentials"));
    }

    @Test
    void registerPage() throws Exception {
        mockMvc.perform(get("/wishlist/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register-page"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void registerPagePost() throws Exception {
        var user = new User(1, "user", "password", List.of());

        when(service.registerUser(any(User.class))).thenReturn(user);
        mockMvc.perform(post("/wishlist/register")
                        .param("username", "user")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlist/user"));
    }

    @Test
    void profilePage() throws Exception {
        var user = new User(1, "user", "password", List.of());

        mockMvc.perform(get("/wishlist/profile")
                        .sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("user-profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", user));
    }

    @Test
    void findUsersWishlists() throws Exception {
        var wishlist = new Wishlist();
        wishlist.setWishes(List.of(new Wish()));
        var user = new User(1, "user", "password", List.of(wishlist));

        when(service.findUsersWishlists("user")).thenReturn(List.of(wishlist));
        mockMvc.perform(get("/wishlist/{name}", user.getUsername())
                        .sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("user-wishlists"))
                .andExpect(model().attributeExists("wishlists"))
                .andExpect(model().attribute("username", "user"))
                .andExpect(model().attribute("wishlists", user.getWishlists()));
    }

    @Test
    void findWishlist() throws Exception {
        final String username = "user";
        final String wishlistname = "test";

        var wishlist = new Wishlist();
        wishlist.setName(wishlistname);

        when(service.findWishlist(username, wishlistname))
                .thenReturn(wishlist);

        mockMvc.perform(get("/wishlist/{username}/{wishlistName}", username, wishlistname))
                .andExpect(status().isOk())
                .andExpect(view().name("user-wishlist"))
                .andExpect(model().attributeExists("wishlist"));
    }

    @Test
    void addWishlist() throws Exception {
        var user = new User(1, "user", "password", List.of());

        mockMvc.perform(get("/wishlist/add")
                        .sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlist-form"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("wishlist"))
                .andExpect(model().attributeExists("isEdit"))
                .andExpect(model().attribute("isEdit", false));
    }
}