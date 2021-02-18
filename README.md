[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# Description

**Semplate** is a Java library to create and maintain markdown documents by:
* adding  semantic information
* using user defined templates

It works with both [Markdown](https://www.markdownguide.org/getting-started/) and [Asciidoc](http://asciidoc.org/).

# Usage

:warning: This is still under development and the API is liable to change.

:grey_exclamation: Exception handling in not shown in the following examples.

1. Create a class for the data object making sure it is annotated for semplate, e.g.

```
     import semplate.annotations.*;

       @Templatable
       public class Work {

       @TemplateField
       private String title;

       @TemplateField
       private String author;

       /* Setters and getters */
   }
```

2. Create a template file in markdown, e.g.:

```
  <!--{{template.comment}}-->

  # {{title}}

  By: {{author}}

  I went down yesterday to the Piraeus with Glaucon, the son of Ariston, that I might offer
  up my prayers to the goddess and also because I wanted to see in what manner they would celebrate the festival, which was a new thing.
```

2.  Create a template object and associate it with a template file

```
    Template template = new Template();

    template.config(FileSystem.getPath("/templates/myTemplate.md"));
```

3. Generate a markdown file using the data in an object.

```
    Work work = new Work();
    work.setAuthor("Plato");
    work.setTitle("The Republic");

    template.generate(work, FileSystem.getPath("/docs/output.md"));
```

4. Generate a data object by reading in the generated markdown file.

```
    Work aWork = (Work) template.read(Work.class, sourceFile);

    assertEquals(aWork.getAuthor(), "Plato");
    assertEquals(aWork.getTitle(), "The Republic");

```
