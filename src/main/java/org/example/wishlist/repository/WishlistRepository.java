package org.example.wishlist.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

import org.example.wishlist.exception.InvalidCredentialsException;
import org.example.wishlist.model.User;
import org.example.wishlist.model.Wish;
import org.example.wishlist.model.Wishlist;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class WishlistRepository {

    private final JdbcTemplate template;
    private final PasswordEncoder passwordEncoder;

    public WishlistRepository(JdbcTemplate template, PasswordEncoder passwordEncoder) {
        this.template = template;
        this.passwordEncoder = passwordEncoder;
    }

    public Wishlist saveWishlist(Wishlist wishlist) {
        final String insertWishlist = """
                INSERT INTO wishlist (name, user_id)
                VALUES (?, ?)
                """;
        final String insertWish = """
                INSERT INTO wish (link)
                VALUES (?)
                """;

        final String insertWishlistWish = """
                INSERT INTO wishlist_wish (wishlist_id, wish_id)
                VALUES (?, ?)
                """;

        final KeyHolder wishlistKeyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertWishlist, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, wishlist.getName());
            ps.setLong(2, wishlist.getUserId());
            return ps;
        }, wishlistKeyHolder);

        wishlist.setId(Objects.requireNonNull(wishlistKeyHolder.getKey()).intValue());

        for (Wish wish : wishlist.getWishes()) {
            final KeyHolder wishKeyHolder = new GeneratedKeyHolder();

            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertWish, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, wish.getLink());
                return ps;
            }, wishKeyHolder);
            wish.setId(Objects.requireNonNull(wishKeyHolder.getKey()).intValue());

            template.update(insertWishlistWish, wishlist.getId(), wish.getId());
        }
        return findWishlist(wishlist.getUserId(), wishlist.getName());
    }

    public Wishlist findWishlist(String username, String wishlistName) {
        final String sql = """
                SELECT w.id, w.name, w.user_id
                FROM wishlist w
                JOIN users u ON w.user_id = u.id
                WHERE u.username = ?
                AND w.name = ?
                """;

        return template.queryForObject(sql, (rs, rowNum) ->
                        new Wishlist(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getInt("user_id")),
                username, wishlistName);
    }

    public Wishlist findWishlist(int userId, String wishlistName) {
        final String sql = """
                SELECT w.id, w.name, w.user_id
                FROM wishlist w
                WHERE w.user_id = ?
                AND w.name = ?
                """;

        return template.queryForObject(sql, (rs, rowNum) ->
                        new Wishlist(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getInt("user_id")),
                userId, wishlistName);
    }

    public User login(String username, String password) {
        final String sql = """
                SELECT id, username, password, email
                FROM users
                WHERE username = ?
                """;
        final RowMapper<User> userRowMapper = (rs, rowNum) ->
                new User(rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"));
        final var user = template.queryForObject(sql, userRowMapper, username);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        user.setPassword(null);
        return user;
    }

    public User registerUser(User user) {
        final String sql = """
                INSERT INTO users (username, password, email)
                VALUES (?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final String hashedPassword = passwordEncoder.encode(user.getPassword());

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, hashedPassword);
            ps.setString(3, user.getEmail());
            return ps;
        }, keyHolder);

        final int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        user.setId(id);

        return findUser(user.getUsername());
    }

    public User findUser(String username) {
        final String sql = """
                SELECT u.id,
                       u.username,
                       u.email
                FROM users u
                WHERE u.username = ?
                """;
        final RowMapper<User> rowMapper = (rs, rowNum) -> {
            final User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"));
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

        int count = Objects.requireNonNull(template.queryForObject(sql, Integer.class, username));
        return count > 0;
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
                SELECT w.id, w.name
                FROM wish w
                JOIN wishlist_wish ww ON w.id = ww.wish_id
                WHERE ww.wishlist_id = ?
                """;
        final RowMapper<Wish> rowMapper = (rs, rowNum) -> new Wish(
                rs.getInt("id"),
                rs.getString("link"),
                rs.getString("name"),
                rs.getDouble("pris"));

        return template.query(sql, rowMapper, wishlist.getId());
    }
    public void deleteUser (User user){
        String sql = "DELTE *FROM users " +
                " WHERE user.id = ?";
        template.update(sql, user.getId());
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
                wish.getName(),
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

        int rows = template.update(updateSql, wish.getName(), wish.getId());

        if (rows == 0) {
            throw new RuntimeException("Wish not found");
        }
}

    public Wishlist createWishlist(String username, Wishlist wishlist) {
        wishlist.setName(wishlist.getName().trim());

        //Finder user gennem name og user id
        User user = findUser(username);

        final String insertWishlistSql = """
                INSERT INTO wishlist (name, user_id)
                VALUES (?, ?) """;

        template.update(insertWishlistSql, wishlist.getName(), user.getId());

        final String idSql = """
                SELECT id FROM wishlist
                WHERE name = ? AND user_id = ? """;

        Integer wishlistId = template.queryForObject(
                idSql, Integer.class, wishlist.getName(), user.getId());

        wishlist.setId(wishlistId);
        if (wishlist.getWishes() != null) {

            final String insertWishSql = """
                    INSERT INTO wishlist_wish (wishlist_id, wish_id)
                    VALUES (?, ?) """;

            for (Wish wish : wishlist.getWishes()) {
                template.update(insertWishSql, wishlistId, wish.getId());
            }

            return findWishlist(user.getId(), wishlist.getName());
        }
        return wishlist;
    }

    public Wish createWish(String username, Wish wish) {
        if (wish.getName() != null) {
            wish.setName(wish.getName().trim());
        }

        final String insertWishSql = "INSERT INTO wish (name) VALUES (?)";
        template.update(insertWishSql, wish.getName());

        //Retrieve generated id by unique name column
        final String idSql = "SELECT id FROM wish WHERE name = ?";
        Integer id = template.queryForObject(idSql, Integer.class, wish.getName());
        wish.setId(id == null ? 0 : id);

        wish.setLink(null);
        return wish;
    }
}
