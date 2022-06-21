package com.codecool.newspaper.data;

import com.codecool.newspaper.testmodels.Article;

import java.util.List;

import static com.codecool.newspaper.data.TestComments.*;

public interface TestArticle {
    Article KITTENS = new Article(null, "kittens", "The cute kittens were born today.", List.of(LIKE_THIS, THIS_REMINDS_ME, THANKS, LMAO, THIS_SUCKS, MAYBE));
    Article BORING = new Article(null, "booring", "Hello, this is the new webisode of Game-Booared!", List.of(MAYBE, THIS_SUCKS, I_HATE_THIS, WHY_WOULD_YOU_DO_THIS, THIS_REMINDS_ME));
    Article BAD = new Article(null, "bad", "bad...", List.of(THIS_SUCKS, I_HATE_THIS, WHY_WOULD_YOU_DO_THIS, THIS_REMINDS_ME));
    Article ANNOYING = new Article(null, "football", "Is genetic engineering a blessing or a curse?", List.of(MAYBE));
}
