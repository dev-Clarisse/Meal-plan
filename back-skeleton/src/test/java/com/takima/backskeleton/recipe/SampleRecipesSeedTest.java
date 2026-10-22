package com.takima.backskeleton.recipe;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.locations=classpath:db/migration,classpath:db/seed",
        "spring.datasource.url=jdbc:h2:mem:seedtestdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class SampleRecipesSeedTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void seedsTheSampleRecipes() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM recipes WHERE title IN ('Riz au lait', 'Pain maison', 'Poulet citron')",
                Integer.class);

        assertThat(count).isEqualTo(3);
    }

    @Test
    void linksEachSampleRecipeToItsIngredients() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM recipe_ingredients WHERE recipe_id = "
                        + "(SELECT id FROM recipes WHERE title = 'Riz au lait')",
                Integer.class);

        assertThat(count).isEqualTo(3);
    }

    @Test
    void doesNotDuplicateIngredientsAlreadyInTheCatalog() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ingredients WHERE name = 'Egg'", Integer.class);

        assertThat(count).isEqualTo(1);
    }
}
