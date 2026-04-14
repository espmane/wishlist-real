package org.example.wishlist.repository;

import java.util.List;

import org.example.wishlist.model.User;
import org.example.wishlist.model.Wish;
import org.example.wishlist.model.Wishlist;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WishlistRepository {

    private final JdbcTemplate template;

    public WishlistRepository(JdbcTemplate template) {
        this.template = template;
    }

    public List<Wishlist> findUsersWishlists(String username) {
        final var user = findUser(username);
        return user.getWishlists();
    }

    public Wishlist findWishlist(String username, String wishlistName) {
        return null;
    }

    // hjælpe metoder

    private User findUser(String username) {
        final String sql = """
                SELECT u.username AS name,
                       u.email AS email,
                       u.id AS ID
                FROM users u
                WHERE u.username = ?
                """;

        final RowMapper<User> rowMapper = (rs, rowNum) -> {
            final User user = new User(rs.getString("name"), rs.getString("email"), rs.getInt("ID"));
            user.setWishlists(findWishlists(user)); // løsning er ikke optimal
            return user;
        };

        return template.queryForObject(sql, rowMapper, username);
    }

    private List<Wishlist> findWishlists(User user) {
        final String sql = """
                SELECT id, name
                FROM wishlist
                WHERE user_id = ?
                """;

        final RowMapper<Wishlist> rowMapper = (rs, rowNum) -> {
            final Wishlist wishlist = new Wishlist(
                    rs.getInt("id"),
                    null,
                    rs.getString("name"));

            wishlist.setWishlist(getWishesFromWishlist(wishlist)); // løsning er ikke optimal

            return wishlist;
        };

        return template.query(sql, rowMapper, user.getID());
    }

    private List<Wish> getWishesFromWishlist(Wishlist wishlist) {
        final String sql = """
                SELECT w.id, w.name
                FROM wish w
                JOIN wishlist_wish ww ON w.id = ww.wish_id
                WHERE ww.wishlist_id = ?
                """;

        final RowMapper<Wish> rowMapper = (rs, rowNum) -> new Wish(
                rs.getInt("id"),
                rs.getString("name"));

        return template.query(sql, rowMapper, wishlist.getID());
    }
}
