package semplate.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Use to mark fields whose data is to be used by the #semplate.Template class.
 * 
 * @author Andrew
 *
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface TemplateField {

}
