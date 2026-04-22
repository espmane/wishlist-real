package org.example.wishlist.controller;

import org.example.wishlist.service.WishlistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishlistService service;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void frontPage() throws Exception{
        mockMvc.perform(get("/wishlist")).andExpect(status().isOk()).andExpect(view().name("index"));
    }

    @Test
    void findWishlists() throws Exception{

    }

    @Test
    void findWishlist() {
    }

    @Test
    void editWishlist() {
    }

    @Test
    void addWishlist() {
    }

    @Test
    void saveWishlist() {
    }

    @Test
    void updateWishlist() {
    }

    @Test
    void deleteWishlist() {
    }

}
