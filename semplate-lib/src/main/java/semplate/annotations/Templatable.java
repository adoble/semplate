package semplate.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
/**Used to annotate classes that can be be used by #semplate.Template
 * 
 * @author Andrew Doble
 *
 */
public @interface Templatable {

}
