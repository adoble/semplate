# How to Write a Template

## Simple Templates

A simple template using [GitHub flavored markdown](https://github.github.com/gfm/)  could look like:

<!-- {% raw %} -->
```markdown
<!--{@template.comment}}-->

# {{title}}

By: {{author}}

Some boilerplate text
```
<!-- {% endraw %} -->

The first line specifies how comments are written in the markdown you are using.
This is done by commenting out the directive {@template.comment} using the markdown's commenting syntax. If you were using Asciidoc then you would specify this as:

<!-- {% raw %} -->
```txt
//{@template.comment}}
```
<!-- {% endraw %} -->

After this, the fields of the data object you are using are specified. In this
case they are `title` and `author`.

When semplate writes a markdown file using a template, these fields are
replaced with the data in the data object.

A hinted at before, a template can be written in any markdown language. Here is
the above example, but in Asciidoc:

<!-- {% raw %} -->
```txt
    //{@template.comment}}

    = {{title}}

    By: {{author}}

    Some boilerplate text
```
<!-- {% endraw %} -->

## Compound Fields

Maybe you have a data object whose fields are themselves objects, for instance:

<!-- {% raw %} -->
```java
 @Templatable
 class Customer {
     @TemplateField String name;
     @TemplateField Address address;
     // ...
 }

 @Templateable
 class Address {
     @TemplateField String street;
     @TemplateField String number;
     @TemplateField String postcode;
     @TemplateField String city;
   // ...
 }
 ```
 <!-- {% endraw %} -->

You can refer to the address fields in a template by using a compound field name, e.g.;

<!-- {% raw %} -->
```markdown
Customer {{name}} lives in {{address.city}}
```
<!-- {% endraw %} -->

Compound names can be extended indefinitely.

## Lists

If a data object has a field that is Iterable then you can refer to this list in the template using the '*' character. For instance, the following data objects;

<!-- {% raw %} -->
```java
@Templatable
class Organisation {
    @TemplateField String name;
    @TemplateField List<Member> members;
    // ...
}

@Templatable
class Member {
    @TemplateField String name;
    @TemplateField String familyName;
    // ...
}
```
<!-- {% endraw %} -->

could be used with the following template:

<!-- {% raw %} -->
```markdown
# Organisation {{name}}

## Members
- {{members.*.familyName}}, {{members.*.name}}
```
<!-- {% endraw %} -->

On generating the markdown, each member would be placed on a different line.

## Top Level Lists

If the data object itself implements an `Iterable` interface, then the first part of the compound field name is a '*' character, e.g.

<!-- {% raw %} -->
```markdown
# Files

- {{*.name}}  {{*.size}}
```
<!-- {% endraw %} -->

## Delimiters

As fields can be embedded in text, some means of identifying where in the text it is required. A template can specify a set of delimiters that are either intrinsic to the markdown or defined by the user. This definition is done using delimiter directives after the `template.comment` directive. For instance;

<!-- {% raw %} -->
```markdown
  <!--{{template.comment}}-->
  <!--{{template.delimiter.pair:"[]"}}-->
  <!--{{template.delimiter.pair:"()"}}-->
  <!--{{template.delimiter.start:": Notes "}}-->

  # Sources
  - [{{sources.*.title}}]({{sources.*.link}}) ; Notes {{sources.*.comment}}
```
<!-- {% endraw %} -->

The standard link delimiters for markdown are first specified ("[]" and "()") followed by a user defined start delimiter ": Notes ".

Note that the `template.delimiter.pair` directive is a shorthand for specifying single character delimiters and could have been written as:

<!-- {% raw %} -->
```markdown
 <!--{{template.delimiter.start:"["}}{{template.delimiter.end:"]"}}-->
 ```
 <!-- {% endraw %} -->
