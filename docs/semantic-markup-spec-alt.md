## Overview

This uses the markdown text directly as the value, instead of duplicating it in the semantic block, e.g;

    #
    The Republic
    <!--{{title}}-->

The value of title is "The Republic"

Values can be embedded, for instance

    * See [Plato's Republic](http://www.wikipedia.com/The_Republic)
    <!--{{referenceTitle}}{{referenceLink}}-->

This uses the markdowns specific inline delimiters and is defined early in the markdown file
using:

    <!--{{template.delimiter-pairs:"()[]"}}-->


## Formal syntax

 The formal syntax of the semantic markdown is represented using [Augmented Backus-Naur Form](https://en.wikipedia.org/wiki/Augmented_Backus%E2%80%93Naur_form).


    block = character-sequence / block-value semantic-block / block-value newline semantic-block  

    block-value =  text-value / *embedded-value

    embedded-value = [character-sequence] inline-delimiter-start inline-value inline-delimiter-end [character-sequence]

    text-value = [character-sequence] newline

    inline-value = [character-sequence]

    semantic-block = comment-delimiter-start
                     field-spec
                     *[field-spec]
                     comment-delimiter-end
                     newline

    field-spec = "{{" field-name [":" *modifier "]}}""

    inline-delimiter-pair = inline-delimiter-start inline-delimiter-end ;for instance "()" or "[]" 

    inline-delimiter-pairs = 1*inline-delimiter-pairs

    inline-delimiter-start = character

    inline-delimiter-end = character

    modifier = character-sequence ;These are specific to the class of the field and are used to specify, for instance, how the text is formatted.

    comment-delimiter-start = *character ; defined in the first lines of the markdown.

    comment-delimiter-end = *character ; defined in the first lines of the markdown.

    character-sequence = *character  ; a sequence of characters representing text or markdown.

    newline = CRLF

    character = VCHAR
