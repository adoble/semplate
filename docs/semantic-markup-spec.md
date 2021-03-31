## Overview



This uses the markdown text directly as the value, instead of duplicating it in the semantic block, e.g;

    <!--{{title:format="# %s"}}-->
    # The Republic

The value of title is "The Republic"

Values can be embedded, for instance:

    <!--{{referenceTitle}}{{referenceLink}}-->
    * See [Plato's Republic](http://www.wikipedia.com/The_Republic)

This uses the markdowns specific inline delimiters and is defined early in the markdown file
using *(Note: the start and end delimiter need to be defined on the same line, only one delimiter pair can be defined per line)*:

    <!-{@template.delimiter.start:"("}}{@template.delimiter.end:")"}}-->
    <!-{@template.delimiter.start:"["}}{@template.delimiter.end:"]"}}-->

or less verbose as:

    <!--{@template.delimiter.pair:"()"}}{@template.delimiter.pair:"[]"}}-->

*(Note: more than one pair can be defined on a line)*
Alternatively a string can be used, for instance, a HTML tag

    <!--{@template.delimiter.start:"<span>"}}{@template.delimiter.end:"</span>"}}


## Formal syntax

 The formal syntax of the semantic markdown is represented using [Extended Backus-Naur Form](https://www.w3.org/TR/REC-xml/#sec-notation).

    template ::= markdown-comment-start comment-directive markdown-comment-end? directive* template-block*

    template-text ::= character-sequence? template-field-spec? template-text

    template-block = template-text newline newline+

    template-field-spec ::= "{{" field-name "}}"

    field-name ::= simple-field-name | complex-field-name

    simple-field-name ::= character-sequence

    complex-field-name ::= (complex-field-name ".")? simple-field-name  

    document ::= markdown-comment-start comment-directive markdown-comment-end? directive* block*

    comment-directive ::= "{@template.comment}}"

    block ::= (semantic-block newline text-value) | (text-block)

    block-value ::=  text-value | embedded-value*

    embedded-value ::= character-sequence? inline-delimiter-start inline-value inline-delimiter-end character-sequence? /* Alternatively an embedded-value can corresponds to a regular expression that is determined by the value type, e.g. a URL */

    text-value ::= character-sequence newline newline+

    inline-value ::= character-sequence

    semantic-block ::= comment-delimiter-start
                     (outline-field-spec | inline-field-spec*)
                     comment-delimiter-end
                     newline

    field-spec ::= inline-field-space | outline-field-spec

    outline-field-spec ::= "{{" field-name (":" *format-spec ")? "}}"

    inline-field-spec ::= "{{" field-name pattern-spec "}}"

    directive ::= "{@" directive-name ("=" directive-value)? "}}"

    inline-delimiter-pair ::= inline-delimiter-start inline-delimiter-end /* For instance "()" or "[]" +/

    inline-delimiter-pairs ::= inline-delimiter-pair+

    inline-delimiter-start ::= character

    inline-delimiter-end ::= character

    pattern-spec ::= string-format-spec | date-format-spec | url-format-spec   /* These are specific to the class of the field and are used to specify, for instance, how the text is formatted. */

    string-pattern-spec ::= "pattern=" character-sequence? "%s" character-sequence?

    date-format-spec ::= /* to be defined */

    url-format-spec ::= /* to be defined */

    comment-delimiter-start ::= character* /* Defined in the first lines of the markdown. */

    comment-delimiter-end ::= character*  /* Defined in the first lines of the markdown.

    character-sequence ::= character*  /* A sequence of characters representing text or markdown. */

    newline ::= "CR" "LF"

    character ::= /* Any visible character including whitespace */
