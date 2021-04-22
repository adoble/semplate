/**
 * <code>
 *   SemanticWriter.with(Object dataObject)                 // Creates SemanticWriter object
 *                 .usingTemplate(Path template_file)       // Creates semanticTemplateWriter object 
 *                 .write(Path output_semantic_file)        // Finalise
 * </code> 
 * 
 * <code>
 *   SemanticWriter.with(Object dataObject)                 // Creates SemanticWriter object
 *                 .usingFile(Path input_semantic file)     // Returns SemanticWriter object
 *                 .write(Path output_semantic_file)        // Finalise  
 * </code>
 * 
 * <code>
 *   SemanticReader.with(Class object_class)               // Create SemanticReader object 
 *                 .usingFile(Path input-semantic-file)    // Returns SemanticReader object
 *                 .read()                                 // Finalise, returns populated object 
 * </code>
 *
 * @author Andrew Doble
 *
 */
package semplate;