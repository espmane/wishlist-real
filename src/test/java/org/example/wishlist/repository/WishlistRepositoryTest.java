package org.example.wishlist.repository;

import org.example.wishlist.model.User;
import org.example.wishlist.model.Wish;
import org.example.wishlist.model.Wishlist;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Equals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:h2init.sql", executionPhase = BEFORE_TEST_METHOD)
class WishlistRepositoryTest {

    @Autowired
   private WishlistRepository repo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void saveWishlist() {
        Wish TestWish = new Wish(2,"https://eksempellink.com/AirPods","Airpods",1600);
//        TestWish.setName("AirPods");
//        TestWish.setLink("https://eksempellink.com/AirPods");
//        TestWish.setPrice(1600);

        Wishlist wishlist = new Wishlist(8,"Testliste",2, List.of(TestWish));
//        wishlist.setName("TestListe");
//        wishlist.setUserId(2);
//        wishlist.setWishes(List.of(TestWish));

       Wishlist test = repo.saveWishlist(wishlist);
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo("Testliste");
        assertThat(test.getUserId()).isEqualTo(2);

        Wishlist db = repo.findWishlist("marie","Testliste");
        assertThat(db).isNotNull();
        assertThat(db.getName()).isEqualTo("Testliste");
        assertThat(db.getUserId()).isEqualTo(2);
    }

    @Test
    void saveWish() {
        Wish TestWish = new Wish(2,"https://eksempellink.com/AirPods","Airpods",1600);

        repo.saveWish(TestWish,2);
    }


    @Test
    void registerUser() {
        User user = new User();
        user.setUsername("Hector");
        user.setPassword("lillebror");

        User testBruger = repo.registerUser(user);

        assertThat(testBruger).isNotNull();
        assertThat(testBruger.getId() > 0);

    }


    @Test
    void findUser() {
        User user= repo.findUser("marie");

        assertNotNull(user);
        assertEquals("marie", user.getUsername());
        assertNotNull(user.getWishlists());
        assertFalse(user.getWishlists().isEmpty());
    }

    @Test
    void userExists() {
        boolean findes = repo.userExists("marie");

        assertTrue(findes);
    }


    @Test
    void updateWish() {
        Wish wish = new Wish();
        wish.setName("Original Name");
        wish.setLink("http://link.com");
        wish.setPrice(100.0);

        Wishlist wishlist = new Wishlist();
        wishlist.setName("Test Update Wishlist");
        wishlist.setUserId(1);
        wishlist.setWishes(List.of(wish));

        Wishlist savedWishlist = repo.saveWishlist(wishlist);
        Wish savedWish = savedWishlist.getWishes().get(0);

        savedWish.setName("Updated Name");
        repo.updateWish(savedWish);

        String sql = "SELECT name FROM wish WHERE id = ?";
        String updatedName = jdbcTemplate.queryForObject(sql, String.class, savedWish.getId());
        assertThat(updatedName).isEqualTo("Updated Name");
    }
}