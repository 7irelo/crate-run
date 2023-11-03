package com.variate.dao.impl;

import com.variate.dao.ReviewDao;
import com.variate.model.entities.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class ReviewDaoImpl implements ReviewDao {

    private final JdbcTemplate jdbcTemplate;

    public ReviewDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Review review) {
        jdbcTemplate.update(
            "INSERT INTO reviews (product_id, user_id, rating, review_comment) VALUES (?, ?, ?, ?)",
            review.getProductId(),
            review.getUserId(),
            review.getRating(), 
            review.getReviewComment()
        );
    }

    @Override
    public Optional<Review> findOne(Long id) {
        List<Review> results = jdbcTemplate.query(
            "SELECT * FROM reviews WHERE id = ?",
            new ReviewRowMapper(), id
        );
        return results.stream().findFirst();
    }

    @Override
    public List<Review> find() {
        return jdbcTemplate.query("SELECT * FROM reviews", new ReviewRowMapper());
    }

    public List<Review> findByProductId(Long productId) {
        return jdbcTemplate.query(
            "SELECT * FROM reviews WHERE product_id = ?",
            new ReviewRowMapper(), productId
        );
    }

    @Override
    public void update(Long id, Review review) {
        jdbcTemplate.update(
            "UPDATE reviews SET product_id = ?, user_id = ?, rating = ?, review_comment = ? WHERE id = ?",
            review.getProductId(),
            review.getUserId(),
            review.getRating(), 
            review.getReviewComment(), 
            id
        );
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", id);
    }

    private static class ReviewRowMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Review.builder()
                .id(rs.getLong("id"))
                .rating(rs.getInt("rating"))
                .reviewComment(rs.getString("review_comment"))
                .build();
        }
    }
}
