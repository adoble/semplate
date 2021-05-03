package semplate.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Used to annotate classes that can be read by the semplate library
 * 
 * @author Andrew Doble
 *
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Templatable {

}
