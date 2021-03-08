## Overview



This uses the markdown text directly as the value, instead of duplicating it in the semantic block, e.g;

    <!--{{title:format="# %s"}}-->
    # The Republic

The value of title is "The Republic"

Values can be embedded, for instance:

    <!--{{referenceTitle}}{{referenceLink}}-->
    * See [Plato's Republic](http://www.wikipedia.com/The_Republic)

This uses the markdowns specific inline delimiters and is defined early in the markdown file
using:

    <!-{@template.delimiter.start:"("}}{@template.delimiter.end:")"}}-->
    <!-{@template.delimiter.start:"["}}{@template.delimiter.end:"]"}}-->

or less verbose as:

    <!--{@template.delimiter.pair:"()"}}{@template.delimiter.pair:"[]"}}-->

Alternatively a string can be used, for instance, a HTML tag

    <!--{@template.delimiter.start:"<span>"}}{@template.delimiter.end:"</span>"}}


## Formal syntax

 The formal syntax of the semantic markdown is represented using [Augmented Backus-Naur Form](https://en.wikipedia.org/wiki/Augmented_Backus%E2%80%93Naur_form).

    document = markdown-comment-start comment-directive [markdown-comment-end] *directive *block

    comment-directive = "{@template.comment}}"

    block = semantic-block [*newline] text-value

    block-value =  text-value / *embedded-value

    embedded-value = [character-sequence] inline-delimiter-start inline-value inline-delimiter-end [character-sequence] ; Alternatively an embedded-value can corresponds to a regular expression that is determined by the value type, e.g. a URL

    text-value = [character-sequence] 2*newline

    inline-value = [character-sequence]

    semantic-block = comment-delimiter-start
                     field-spec
                     *[field-spec]
                     comment-delimiter-end
                     newline

    field-spec = "{{" field-name [":" *format-spec "]}}""

    directive = "{@" directive-name ["=" directive-value] "}}"

    inline-delimiter-pair = inline-delimiter-start inline-delimiter-end ;for instance "()" or "[]"

    inline-delimiter-pairs = 1*inline-delimiter-pair

    inline-delimiter-start = character

    inline-delimiter-end = character

    format-spec = string-format-spec | number-format-spec | date-format-spec | url-format-spec  ;These are specific to the class of the field and are used to specify, for instance, how the text is formatted.

    string-format-spec = "format=" [character-sequence] "%s" [character-sequence]

    comment-delimiter-start = *character ; defined in the first lines of the markdown.

    comment-delimiter-end = *character ; defined in the first lines of the markdown.

    character-sequence = *character  ; a sequence of characters representing text or markdown.

    newline = CRLF

    character = VCHAR
