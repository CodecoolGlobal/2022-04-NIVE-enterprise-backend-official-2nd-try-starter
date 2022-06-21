package com.codecool.newspaper.data;

import com.codecool.newspaper.testmodels.Comment;
import com.codecool.newspaper.testmodels.Category;

public interface TestComments {
    Comment LIKE_THIS = new Comment(null, "i like", Category.POSITIVE);
    Comment LMAO = new Comment(null, "funny", Category.POSITIVE);
    Comment THANKS = new Comment(null, "this helped", Category.POSITIVE);
    Comment MAYBE = new Comment(null, "i donno", Category.NEUTRAL);
    Comment THIS_REMINDS_ME = new Comment(null, "hmmm", Category.NEUTRAL);
    Comment I_HATE_THIS = new Comment(null, "no no", Category.NEGATIVE);
    Comment WHY_WOULD_YOU_DO_THIS = new Comment(null, "no no no", Category.NEGATIVE);
    Comment THIS_SUCKS = new Comment(null, "no no no no", Category.NEGATIVE);
}
