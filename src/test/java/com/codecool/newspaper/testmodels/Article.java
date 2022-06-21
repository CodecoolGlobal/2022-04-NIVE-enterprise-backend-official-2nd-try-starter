package com.codecool.newspaper.testmodels;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class Article {
    private Long id;
    private String title;
    private String text;
    private List<Comment> comments;

    public Article(Long id, String title, String text, List<Comment> comments) {

        this.id = id;
        this.title = title;
        this.text = text;
        this.comments = comments == null ? new ArrayList<>() : new ArrayList<>(comments);
    }
}
