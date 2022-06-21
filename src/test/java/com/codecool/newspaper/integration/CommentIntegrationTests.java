package com.codecool.newspaper.integration;

import com.codecool.newspaper.testmodels.Article;
import com.codecool.newspaper.testmodels.Category;
import com.codecool.newspaper.testmodels.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.stream.Collectors;

import static com.codecool.newspaper.data.TestComments.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class CommentIntegrationTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String entityUrl;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + port;
        entityUrl = baseUrl + "/comment";
    }

    @Test
    void emptyDatabase_getAll_shouldReturnEmptyList() {
        assertEquals(Collections.emptyList(), List.of(restTemplate.getForObject(entityUrl, Comment[].class)));
    }

    @Test
    void emptyDatabase_addOne_shouldReturnAddedComment() {
        Comment result = restTemplate.postForObject(entityUrl, THANKS, Comment.class);
        assertEquals(THANKS.getText(), result.getText());
    }

    @Test
    void someCommentsStored_getAll_shouldReturnAll() {
        List<Comment> testData = List.of(THIS_SUCKS, THANKS, LIKE_THIS);
        testData.forEach(ingredient -> restTemplate.postForObject(entityUrl, ingredient, Comment.class));

        Comment[] result = restTemplate.getForObject(entityUrl, Comment[].class);
        assertEquals(testData.size(), result.length);

        Set<String> commentNames = testData.stream().map(Comment::getText).collect(Collectors.toSet());
        assertTrue(Arrays.stream(result).map(Comment::getText).allMatch(commentNames::contains));
    }

    @Test
    void oneCommentStored_getOneById_shouldReturnCorrectComment() {
        Long id = restTemplate.postForObject(entityUrl, THANKS, Comment.class).getId();
        Comment result = restTemplate.getForObject(entityUrl + "/" + id, Comment.class);
        assertEquals(THANKS.getText(), result.getText());
        assertEquals(id, result.getId());
    }

    @Test
    void someCommentsStored_deleteOne_getAllShouldReturnRemaining() {
        List<Comment> testData = new ArrayList<>(List.of(THIS_SUCKS, THANKS, LIKE_THIS));
        testData = new ArrayList<>(testData.stream()
                .map(p -> restTemplate.postForObject(entityUrl, p, Comment.class))
                .toList());

        restTemplate.delete(entityUrl + "/" + testData.get(0).getId());
        Set<String> expectedCommentNames = testData.stream().skip(1L).map(Comment::getText).collect(Collectors.toSet());

        Comment[] response = restTemplate.getForObject(entityUrl, Comment[].class);

        assertEquals(expectedCommentNames.size(), response.length);
        for (Comment p : response) {
            assertTrue(expectedCommentNames.contains(p.getText()));
        }
    }

    @Test
    void oneCommentStored_deleteById_getAllShouldReturnEmptyList() {
        Comment testComment = restTemplate.postForObject(entityUrl, LIKE_THIS, Comment.class);
        assertNotNull(testComment.getId());
        restTemplate.delete(entityUrl + "/" + testComment.getId());
        Comment[] result = restTemplate.getForObject(entityUrl, Comment[].class);
        assertEquals(0, result.length);
    }

    @Test
    void oneCommentStoredUsedInArticle_deleteById_CommentShouldNotBeDeleted() {
        Comment testComment = restTemplate.postForObject(entityUrl, THIS_REMINDS_ME, Comment.class);
        Article testArticle = restTemplate.postForObject(
                "http://localhost:" + port + "/article",
                new Article(null, "toy car", "tc", List.of(testComment)),
                Article.class
        );
        restTemplate.delete(entityUrl + "/" + testComment.getId());
        Comment result = restTemplate.getForObject(entityUrl + "/" + testComment.getId(), Comment.class);
        assertEquals(testComment.getText(), result.getText());
    }

    @Test
    void oneCommentStored_updateIt_CommentShouldBeUpdated() {
        Comment testComment = restTemplate.postForObject(entityUrl, THIS_REMINDS_ME, Comment.class);

        testComment.setText(testComment.getText() + "update");
        String url = entityUrl + "/" + testComment.getId();
        restTemplate.put(url, testComment);

        Comment result = restTemplate.getForObject(url, Comment.class);
        assertEquals(testComment.getText(), result.getText());
    }

    @Test
    void oneCommentStored_updateWithWrongId_CommentShouldBeUnchanged() {
        Comment testComment = restTemplate.postForObject(entityUrl, THIS_REMINDS_ME, Comment.class);

        String originalName = testComment.getText();
        assertNotNull(originalName);
        Long originalId = testComment.getId();

        testComment.setText(originalName + "update");
        testComment.setId(42L);
        String url = entityUrl + "/" + originalId;
//         restTemplate.put(url, testComment, Object.class);
        ResponseEntity<?> resp = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(testComment, null), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());

        Comment result = restTemplate.getForObject(url, Comment.class);
        assertEquals(originalName, result.getText());
    }

    @Test
    void getOneByWrongId_shouldRespond404() {
        ResponseEntity<String> response = restTemplate.getForEntity(entityUrl + "/12345", String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void postInvalidCommentWithNull_shouldRespond400() {
        ResponseEntity<String> response = restTemplate.postForEntity(entityUrl, new Comment(null, null, Category.POSITIVE), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void postInvalidCommentWithBlankString_shouldRespond400() {
        ResponseEntity<String> response = restTemplate.postForEntity(entityUrl, new Comment(null, "", Category.NEGATIVE), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void putInvalidCommentWithBlankString_shouldRespond400() {
        Comment testComment = restTemplate.postForObject(entityUrl, THIS_REMINDS_ME, Comment.class);
        String url = entityUrl + "/" + testComment.getId();

        testComment.setText("");
        ResponseEntity<?> resp = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(testComment, null), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }
}
