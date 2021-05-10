## Overview

Markdown files produced by <code>semplate</code> use the markdown languages comments to embed the semantic annotation. For instance in **GFM - **[GitHub Flavored Markdown](https://github.github.com/gfm/):

    <!--{{title:format="# %s"}}-->
    # The Republic

The value of the field title is "The Republic".

The means of commenting is specified by having the following directive embedded as a comment in the first line of the markdown file. For example with markdown:

    <!--{{template.comment}}-->

For Asciidoc use the following:

    //{@template.comment}}

Values can be embedded, for instance:

    <!--{{reference.title}}{{reference.link}}-->
    * See [Plato's Republic](http://www.wikipedia.com/The_Republic)

This uses the markdowns specific inline delimiters and is defined early in the markdown file
using the delimiter directives  *(Note: the start and end delimiter need to be defined on the same line, only one delimiter pair can be defined per line)*:

    <!-{@template.delimiter.start:"("}}{@template.delimiter.end:")"}}-->
    <!-{@template.delimiter.start:"["}}{@template.delimiter.end:"]"}}-->

or less verbose as:

    <!--{@template.delimiter.pair:"()"}}-->
    <!--{@template.delimiter.pair:"[]"}}-->

Alternatively a string can be used, for instance, a HTML tag

    <!--{@template.delimiter.start:"<span>"}}{@template.delimiter.end:"</span>"}}


## Formal syntax

The formal syntax of a template file is defined as [Augmented Backus Naur format (ABNF)](https://tools.ietf.org/html/rfc5234). These are represented by rail road diagrams

*Thanks to [@katef](https://github.com/katef) for the <code>kgt</code> used to generate the rail road diagrams.

### Templates

 ABNF specification can be found [here](template.abnf), but a rail road diagram is shown below.

 ![template.abnf](template-abnf.svg)


 ### Semantically Annotated Markdown Files

Although these are generated from the <code>semplate</code> libary and not directly written by the user, the formal ABNF syntax of a semantically annotated markdown file can be found [here](template.abnf), but a rail road diagram is shown below.

  ![semantically-annotated-markdown.abnf](semantically-annotated-markdown-abnf.svg)
