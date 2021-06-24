## Overview

Markdown files produced by <code>semplate</code> use the markdown languages comments to embed the semantic annotation. For instance in **GFM - **[GitHub Flavored Markdown](https://github.github.com/gfm/):

<!-- {% raw %} -->
```markdown
<!--{{title:format="# %s"}}-->
# The Republic
```
<!-- {% endraw %} -->

The value of the field title is "The Republic".

The means of commenting is specified by having the following directive embedded as a comment in the first line of the markdown file. For example with markdown:

<!-- {% raw %} -->
```markdown
<!--{{template.comment}}-->
```
<!-- {% endraw %} -->

For Asciidoc use the following:

!-- {% raw %} -->
```txt
//{@template.comment}}
```
<!-- {% endraw %} -->

Values can be embedded, for instance:

<!-- {% raw %} -->
```markdown
<!--{{reference.title}}{{reference.link}}-->
* See [Plato's Republic](http://www.wikipedia.com/The_Republic)
```
<!-- {% endraw %} -->

This uses the markdown's specific delimiters and is defined early in the markdown file
using the delimiter directives  *(Note: the start and end delimiter need to be defined on the same line, only one delimiter pair can be defined per line)*:

<!-- {% raw %} -->
```markdown
<!-{@template.delimiter.start:"("}}{@template.delimiter.end:")"}}-->
<!-{@template.delimiter.start:"["}}{@template.delimiter.end:"]"}}-->
```
<!-- {% endraw %} -->

or less verbose as:

<!-- {% raw %} -->
```markdown
<!--{@template.delimiter.pair:"()"}}-->
<!--{@template.delimiter.pair:"[]"}}-->
```
<!-- {% endraw %} -->

Alternatively a string can be used, for instance, a HTML tag:

<!-- {% raw %} -->
```markdown
<!--{@template.delimiter.start:"<span>"}}{@template.delimiter.end:"</span>"}}
```
<!-- {% endraw %} -->

## Formal syntax

The formal syntax of a template file is defined as [Augmented Backus Naur format (ABNF)](https://tools.ietf.org/html/rfc5234). These are represented by rail road diagrams

*Thanks to [@katef](https://github.com/katef) for the <code>kgt</code> tool used to generate the rail road diagrams.*

### Templates

 The ABNF specification can be found [here](template.abnf), but a rail road diagram is shown below:

 ![template.abnf](template-abnf.svg)


### Semantically Annotated Markdown Files

Although these are generated from the **semplate** libary and not directly written by the user, the formal ABNF syntax of a semantically annotated markdown file can be found [here](template.abnf), but a rail road diagram is shown below.

  ![semantically-annotated-markdown.abnf](semantically-annotated-markdown-abnf.svg)
