/**
 * Provides classes for an opinionated approach to Java reflection.
 *
 * Instead of providing means to reflectively execute methods, the {@link semplate.valuemap.ValueMap}
 * class assumes that only values are to be accessed or changed in an object. 
 * 
 * Data accessed from an object is placed in a value map data structure. This is a tree like structure consisting
 * of elements tagged with the objects field name. Each field name refers either to a primitive
 * value or to another value map. As such, complex nested structures can be build up representing the data in 
 * an object and any objects that it references. 
 * 
 * A value map can also be filled with data and then these changes applied to an object. 
 *
 */

package semplate.valuemap;