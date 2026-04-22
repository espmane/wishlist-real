package org.example.wishlist.repository;

import org.example.wishlist.exception.InvalidCredentialsException;
import org.example.wishlist.model.User;
import org.example.wishlist.model.Wish;
import org.example.wishlist.model.Wishlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
public class WishlistRepository {

    private final JdbcTemplate template;

    @Autowired
    private DataSource dataSource;

    public WishlistRepository(JdbcTemplate template) {
        this.template = template;
    }

    @Transactional
    public Wishlist saveWishlist(Wishlist wishlist) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final String insertWishlist = """
                INSERT INTO wishlist (name, user_id)
                VALUES (?, ?)
                """;

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertWishlist, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, wishlist.getName());
            ps.setInt(2, wishlist.getUserId());
            return ps;
        }, keyHolder);
        final int wishlistId = Objects.requireNonNull(keyHolder.getKey()).intValue();

        for (Wish wish : wishlist.getWishes()) {
            saveWish(wish, wishlistId);
        }
        return wishlist;
    }

    @Transactional
    public void saveWish(Wish wish, int wishlistId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        final String insertWishSql = """
                INSERT INTO wish (link, name, price)
                VALUES (?, ?, ?)
                """;

        final String insertWishlistWish = """
                INSERT INTO wishlist_wish (wishlist_id, wish_id)
                VALUES (?, ?)
                """;

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    insertWishSql,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, wish.getLink());
            ps.setString(2, wish.getName().trim());
            ps.setDouble(3, wish.getPrice());
            return ps;
        }, keyHolder);
        int wishId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        wish.setId(wishId);

        template.update(insertWishlistWish, wishlistId, wishId);
    }

    @Transactional
    public boolean deleteWishlist(Wishlist wishlist) {
        final String sql = """
                DELETE FROM wishlist
                WHERE id = ?
                """;
        return template.update(sql, wishlist.getId()) > 0;
    }

    // TODO: removeWish()

    public Wishlist findWishlist(String username, String wishlistName) {
        final String sql = """
                SELECT w.id, w.name, w.user_id
                FROM wishlist w
                JOIN users u ON w.user_id = u.id
                WHERE u.username = ?
                AND w.name = ?
                """;

        Wishlist wishlist = template.queryForObject(sql, (rs, rowNum) ->
                        new Wishlist(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getInt("user_id")),
                username, wishlistName);

        wishlist.setWishes(getWishesFromWishlist(wishlist));
        return wishlist;
    }

    public User login(String username, String password) {
        System.out.println("Login forsøgt: " + username);

        try {
            System.out.println("DB URL: " + dataSource.getConnection().getMetaData().getURL());
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String sql = """
                SELECT id, username, password
                FROM users
                WHERE username = ?
                """;
        final RowMapper<User> userRowMapper = (rs, rowNum) ->
                new User(rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"));
        try {
            var user = template.queryForObject(sql, userRowMapper, username);
            if (!password.equals(user.getPassword())) {
                throw new InvalidCredentialsException("Invalid username or password");
            }
            user.setPassword(null);
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Transactional
    public User registerUser(User user) {
        final String sql = """
                INSERT INTO users (username, password)
                VALUES (?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        return user;
    }

    public boolean deleteUser(User user) {
        template.update("""
                    DELETE w
                           FROM wish w
                    JOIN wishlist_wish ww ON ww.wish_id = w.id
                    JOIN wishlist wl ON wl.id = ww.wishlist_id
                    WHERE wl.user_id = ?
                """, user.getId()); // sletter wishes brugeren har lavet

        return template.update("DELETE FROM users WHERE id = ?", user.getId()) > 0;
    }

    @Transactional
    public User findUser(String username) {
        final String sql = """
                SELECT u.id,
                       u.username
                FROM users u
                WHERE u.username = ?
                """;
        final RowMapper<User> rowMapper = (rs, rowNum) -> {
            final User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"));
            user.setWishlists(findWishlists(user)); // løsning er ikke optimal
            return user;
        };

        return template.queryForObject(sql, rowMapper, username);
    }

    // hjælpe metoder

    public boolean userExists(String username) {
        final String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE username = ?
                """;

        Integer count = template.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
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
                    rs.getString("name"),
                    user.getId()
            );
            wishlist.setWishes(getWishesFromWishlist(wishlist)); // løsning er ikke optimal

            return wishlist;
        };

        return template.query(sql, rowMapper, user.getId());
    }

    private List<Wish> getWishesFromWishlist(Wishlist wishlist) {
        final String sql = """
                SELECT w.id, w.name, w.link, w.price
                FROM wish w
                JOIN wishlist_wish ww ON w.id = ww.wish_id
                WHERE ww.wishlist_id = ?
                """;
        final RowMapper<Wish> rowMapper = (rs, rowNum) -> new Wish(
                rs.getInt("id"),
                rs.getString("link"),
                rs.getString("name"),
                rs.getDouble("price"));

        return template.query(sql, rowMapper, wishlist.getId());
    }

    public void updateWishlist(String username, Wishlist wishlist) {
        final String userSql = """
                SELECT id FROM users WHERE username = ?
                """;

        Integer userId = template.queryForObject(userSql, Integer.class, username);
        final String checkSql = """
                SELECT COUNT(*) FROM wishlist
                WHERE user_id = ? AND name = ? AND id <> ?
                """;

        Integer count = template.queryForObject(
                checkSql,
                Integer.class,
                userId,
                wishlist.getName(),
                wishlist.getId()
        );

        if (count != null && count > 0) {
            throw new RuntimeException("Wishlist name already exists for this user");
        }


        final String updateSql = """
                UPDATE wishlist
                SET name = ?
                WHERE id = ? AND user_id = ?
                """;

        int rowsAffected = template.update(
                updateSql,
                wishlist.getName(),
                wishlist.getId(),
                userId
        );

        if (rowsAffected == 0) {
            throw new RuntimeException("Wishlist not found or not owned by user");
        }
    }

    public void updateWish(Wish wish) {

        final String checkSql = """
                SELECT COUNT(*) FROM wish
                WHERE name = ? AND id <> ?
                """;

        Integer count = template.queryForObject(
                checkSql,
                Integer.class,
                wish.getLink(),
                wish.getId()
        );

        if (count != null && count > 0) {
            throw new RuntimeException("Wish name already exists");
        }

        final String updateSql = """
                UPDATE wish
                SET name = ?
                WHERE id = ?
                """;

        int rows = template.update(updateSql, wish.getLink(), wish.getId());

        if (rows == 0) {
            throw new RuntimeException("Wish not found");
        }
    }
}