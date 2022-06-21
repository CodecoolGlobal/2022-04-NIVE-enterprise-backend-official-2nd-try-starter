package com.codecool.newspaper.testmodels;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Comment {
    private Long id;
    private String text;
    private Category category;

    public Comment(Long id, String text, Category category) {
        this.id = id;
        this.text = text;
        this.category = category;
    }
}
