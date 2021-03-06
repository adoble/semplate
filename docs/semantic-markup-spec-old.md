## Formal syntax
 The formal syntax of the semantic markdown is represented using [Augmented Backus-Naur Form](https://en.wikipedia.org/wiki/Augmented_Backus%E2%80%93Naur_form).

    block = character-sequence / block-value semantic-block / block-value newline semantic-block  

    block-value =  text-value / *embedded-value

    embedded-value = [character-sequence] inline-delimiter-start inline-value inline-delimiter-end

    text-value = [character-sequence] newline

    inline-value = [character-sequence]

    semantic-block = comment-delimiter-start [character-sequence]
                     field-spec
                     *([character-sequence] field-spec [character-sequence])
                     comment-delimiter-end "\n"

    field-spec = "{{" field-name "=" quote field-value quote ""}}"

    quote = """ / "'"

    inline-delimiter-pair = inline-delimiter-start inline-delimiter-end ;for instance "()" or "[]"

    inline-delimiter-pairs = 1*inline-delimiter-pairs

    inline-delimiter-start = character

    inline-delimiter-end = character

    comment-delimiter-start = *character ; defined in the first lines of the markdown.

    comment-delimiter-end = *character ; defined in the first lines of the markdown.

    character-sequence = *character  ; a sequence of characters representing text or markdown.

    newline = CRLF

    character = VCHAR
