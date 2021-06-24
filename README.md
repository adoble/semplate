![Build](https://github.com/adoble/semplate/workflows/Build/badge.svg)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# Description

**Semplate** is a Java library to create and maintain markdown documents by:
* adding  semantic information
* using user defined templates

It works with both [Markdown](https://www.markdownguide.org/getting-started/) and [Asciidoc](http://asciidoc.org/)
and may work with other markdown flavors.

More comprehensive documentation can be found in the [GitHub Pages](https://adoble.github.io/semplate/)

# Quick overview on how to use the Semplate library

:grey_exclamation: Exception handling in not shown in the following examples.

1. Create a class for the data object making sure it is annotated for semplate, e.g.

```
     import semplate.annotations.*;

       @Templatable
       public class ADR {

       @TemplateField
       private Integer id;

       @TemplateField
       private String name;

       @TemplateField
       private final String status;

       /* Setters and getters */
   }
```

2. Create a template file in markdown, e.g.:

```
  <!--{{template.comment}}-->

  # {{id}}. {{name}}

  ## Status

  {{status}}

  ## Context

  *Record the architectural decisions made on this project.*

  ## Decision

  **We will use Architecture Decision Records, as described by Michael Nygard in [this article: ](http://thinkrelevance.com/blog/2011/11/15/documenting-architecture-decisions)**


```

2. Generate a markdown file using the data in an object. The markdown file will be semantically annotated.

```
    ADR adr = new ADR();
    adr.setId(12);
    adr.setName("Use a graph database");
    adr.setStatus("Proposed");

    SemanticWriter.with(adr)
                  .usingTemplate(path_to_template_file)
                  .write(path_to_markdown_file);
```
4. Generate a data object by reading in the generated (semantically annotated) markdown file.

```
    ADR adr = (ADR) SemanticReader.with(ArchitectureDecisionRecord.class)
                                  .usingFile(path_to_markdown_file)
                                  .read();   

    assertEquals(12, adr.getId());
    assertEquals("Use a graph database", adr.getName());

```

5. Update an existing (semantically annotated) markdown file

```
   ADR updatedADR = adr.clone();
   updatedADR.setStatus("Agreed");

   SemanticWriter.with(updatedADR)                 
                .usingFile(path_to_input_semantic_file)
                .write(path_to_updated_semantic_file);       
  ```
