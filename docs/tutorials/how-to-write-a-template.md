# How to Write a Template

## Simple Templates

A simple template using GFM could look like:

<code>
    <!--{@template.comment}}-->

    # {{title}}

    By: {{author}}

    Some boilerplate text
</code>


The first line specifies how comments are written in the markdown you are using.
This is done by commenting out the directive {@template.comment}. If you were
using Asciidoc then you would specify this as:


    //{@template.comment}}

After this the fields of the data object you are using are specified. In this
case they are `title` and `author`.

When `semplate` writes out a markdown file using a template these fields are
replaced with the data in the data object.

A hinted at before,  a template can be writte in any markdown language. Here is
the above example, but in Asciidoc:

    //{@template.comment}}

    = {{title}}

    By: {{author}}

    Some boilerplate text


## Compound Fields

Maybe you have data object whose fields are themselves objects, for instance



     @Templatable
     class Customer {
        @TemplateField
        String name;
        @TemplateField
        Address address;
        // ...
     }

     class Address {
       @Templateable
       @TemplateField
       String street;
       @TemplateField
       String number;
       @TemplateField
       String postcode;
       @TemplateField
       String city;
       // ...
     }

You can refer to the address fields in a template by using a compound field name, e.g.;

    {{name}} lives in {{address.city}}

Compound name can be extended indefinitely.

## Lists

If a data object has a field that is Iterable then you can refer to this list in the template using the '*' character. For instance, the following data objects;

      @Templatable
      class Organisation {
         @TemplateField
         String name;
         @TemplateField
         List<Member> members;
         // ...
      }

      @Templatable
      class Member {
         @TemplateField
         String name;
         @TemplateField
         String familyName;
         // ...
      }

could be used with the following template:

    # Organisation {{name}}

    ## Members
    - {{members.*.familyName}}, {{members.*.name}}

On generating the markdown, each member would be placed on a different line.

## Top Level Lists

If the data object itself implements an Iterable interface, then the first part of the compound field name is a '*' character, e.g.

      # Files

      - {{*.name}}  {{*.size}}

## Delimiters

As fields can be embedded in text, some means of identifing wher it is in the text is required. A template can specify a set of delimiters that are either intrinsic or defined by the user. This definition is done using delimiter directives after the `template.comment` directive. For instance;

      <!--{{template.comment}}-->
      <!--{{template.delimiter.pair:"[]"}}
      <!--{{template.delimiter.pair:"()"}}
      <!--{{template.delimiter.start:": Notes "}}

      # Sources
      - [{{sources.*.title}}]({{sources.*.link}}) ; Notes {{sources.*.comment}}

The standard link delimiters for markdown are first specified ("[]" and "()") followed by a user defined start delimiter ": Notes ".

Note that the `template.delimiter.pair` directive is a shorthand for specifying single character delimiters and could have written as:

       <!--{{template.delimiter.start:"["}}{{template.delimiter.end:"]"}}
