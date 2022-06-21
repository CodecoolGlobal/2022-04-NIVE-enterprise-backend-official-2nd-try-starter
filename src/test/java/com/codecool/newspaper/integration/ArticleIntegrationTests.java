package com.codecool.newspaper.integration;

import com.codecool.newspaper.data.TestArticle;
import com.codecool.newspaper.testmodels.Article;
import com.codecool.newspaper.testmodels.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codecool.newspaper.data.TestArticle.*;
import static com.codecool.newspaper.data.TestComments.I_HATE_THIS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class ArticleIntegrationTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;
    private String entityUrl;
    private String baseUrl;

    private Article postArticle(Article article) {

        List<Comment> postedComments = article.getComments().stream()
                .map(p -> restTemplate.postForObject(baseUrl + "/comment", p, Comment.class))
                .toList();
        Article articleToPost = new Article(article.getId(), article.getTitle(), article.getText(), postedComments);
        return restTemplate.postForObject(entityUrl, articleToPost, Article.class);
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        entityUrl = baseUrl + "/article";
    }

    @RepeatedTest(2)
    void emptyDatabase_getAll_shouldReturnEmptyList() {
        assertEquals(Collections.emptyList(), List.of(restTemplate.getForObject(entityUrl, Article[].class)));
    }

    @Test
    void emptyDatabase_addOne_shouldReturnAdded() {
        KITTENS.getComments().forEach(ing -> System.out.println(ing.getId()));
        Article result = postArticle(KITTENS);
        assertEquals(KITTENS.getTitle(), result.getTitle());
    }

    @Test
    void someStored_getAll_shouldReturnAll() {
        List<Article> testData = List.of(BORING, BAD);
        Set<String> expectedNames = testData.stream().map(Article::getTitle).collect(Collectors.toSet());
        testData.forEach(this::postArticle);

        Article[] response = restTemplate.getForObject(entityUrl, Article[].class);

        assertEquals(testData.size(), response.length);
        for (Article a : response) {
            assertTrue(expectedNames.contains(a.getTitle()));
        }
    }

    @Test
    void oneStored_getOneById_shouldReturnCorrect() {
        Long id = postArticle(KITTENS).getId();
        Article response = restTemplate.getForObject(entityUrl + "/" + id, Article.class);
        assertEquals(KITTENS.getTitle(), response.getTitle());
        assertEquals(KITTENS.getComments().size(), response.getComments().size());

        List<String> responseCommentNames = response.getComments().stream().map(Comment::getText).toList();
        List<String> expectedCommentNames = KITTENS.getComments().stream().map(Comment::getText).toList();
        assertThat(responseCommentNames).hasSameElementsAs(expectedCommentNames);
    }

    @Test
    void getOneByWrongId_shouldRespond404() {
        ResponseEntity<String> response = restTemplate.getForEntity(entityUrl + "/12345", String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void someArticlesStored_deleteOne_getAllShouldReturnRemaining() {
        List<Article> testData = new ArrayList<>(List.of(KITTENS, BORING));
        testData.replaceAll(this::postArticle);

        String url = entityUrl + "/" + testData.get(0).getId();
        restTemplate.delete(url);
        testData.remove(testData.get(0));

        Article[] result = restTemplate.getForObject(entityUrl, Article[].class);

        assertEquals(testData.size(), result.length);
        for (int i = 0; i < testData.size(); i++) {
            assertEquals(testData.get(i).getTitle(), result[i].getTitle());
        }
    }

    @Test
    void oneArticleStored_deleteById_getAllShouldReturnEmptyList() {
        Article testData = postArticle(BORING);

        restTemplate.delete(entityUrl + "/" + testData.getId());

        Article[] result = restTemplate.getForObject(entityUrl, Article[].class);

        assertEquals(0, result.length);
    }

    @Test
    void postInvalidCommentWithNull_shouldRespond400() {
        var data = new Article(null, null, null, List.of(I_HATE_THIS));
        ResponseEntity<String> response = restTemplate.postForEntity(entityUrl, data, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void postInvalidCommentWithBlankString_shouldRespond400() {
        var data = new Article(null, "", "", List.of());
        ResponseEntity<String> response = restTemplate.postForEntity(entityUrl, data, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void postInvalidCommentWithBlankText_shouldRespond400() {
        var data = new Article(null, "ABC", "", null);
        ResponseEntity<String> response = restTemplate.postForEntity(entityUrl, data, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void postAssemblies_whenRequestingControversial_ReturnOnlyTheControversial() {
        postArticle(ANNOYING);
        postArticle(BORING);
        postArticle(KITTENS);
        postArticle(BAD);

        Article[] response = restTemplate.getForObject(entityUrl + "/controversial", Article[].class);
        Set<String> responseNames = Arrays.stream(response).map(Article::getTitle).collect(Collectors.toSet());

        var expectedNames = Stream.of(BORING).map(Article::getTitle).collect(Collectors.toSet());

        assertThat(response).hasSize(expectedNames.size());
        assertThat(responseNames).hasSameElementsAs(expectedNames);
    }
}
