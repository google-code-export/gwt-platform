package com.philbeaudoin.gwtp.mvp.gin;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Some reflection tools. Based on Ian Robertson post
 * http://www.artima.com/weblogs/viewpost.jsp?thread=208860.
 * 
 * @author Olivier Monaco
 */
public class ReflectionUtils {
  /**
   * Return the {@link Class} of a {@link Type}. It can be directly the
   * {@link Type} when it's a {@link Class}, it raw type for
   * {@link ParameterizedType}...
   * 
   * @param type
   *          The type
   * @return Its class, <tt>null</tt> when this type as no class (wildcard, type
   *         variable...)
   */
  public static Class<?> getClass(Type type) {
    // Trivial: it's a class!
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    }

    // For parameterized type, we recurse on its raw type (commonly a class)
    else if (type instanceof ParameterizedType) {
      return getClass(((ParameterizedType) type).getRawType());
    }

    // For array of generics, we recurse on the component type before building
    // an array of its class
    else if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      Class<?> componentClass = getClass(componentType);
      if (componentClass != null) {
        return Array.newInstance(componentClass, 0).getClass();
      }
    }

    // Any other condition give null
    return null;
  }

  /**
   * Retrieves the type arguments corresponding to the type variable of the
   * {@link Class} <tt>target</tt> for the {@link Type} <tt>source</tt>. This
   * method walk through the inheritance tree to resolve all intermediary type
   * variables.
   * <p>
   * If the <tt>source</tt> is the target, this method returns the type
   * variables of this class.
   * <p>
   * If the <tt>source</tt> has some type variables, the result can contains
   * them. For example, using {@link List}.class as the source and
   * {@link Collection}.class as the target will result in the type argument of
   * {@link List} ({@link Class#getTypeParameters()}).
   * <p>
   * If the target has no type arguments, the result is an empty array.
   * 
   * @param target
   *          The class for which we want type variable resolution
   * @param source
   *          The type
   * @return The type arguments
   * @throws IllegalArgumentException
   *           if the <tt>source</tt> does not extend/implement the
   *           <tt>target</tt>
   */
  public static Type[] getTypeArguments(Class<?> target, Type source)
      throws IllegalArgumentException {
    // Source is target, just returns the type parameters
    if (source == target) {
      return target.getTypeParameters();
    }

    // The source must be erasable to a class
    Class<?> sourceRaw = getClass(source);
    if (sourceRaw == null) {
      throw new IllegalArgumentException("The source type (" + source
          + ") can't extend/implement the target class (" + target + ").");
    }

    // The source class must extend/implement the target
    if (!target.isAssignableFrom(sourceRaw)) {
      throw new IllegalArgumentException("The source type (" + source
          + ") does not extends/implement the target class (" + target + ").");
    }

    // The target class must have type variables
    TypeVariable<?>[] params = target.getTypeParameters();
    if (params.length == 0) {
      return new Type[0];
    }

    // This is the resolution map
    Map<Type, Type> resolver = new HashMap<Type, Type>();

    Type currentType = source;
    Class<?> currentClass = null;

    // Stop when target class is matched
    while (!target.equals(currentClass)) {
      // When a parameterized type is found, we link type variables to type
      // arguments
      if (currentType instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType) currentType;
        currentClass = getClass(pt.getRawType());
        Type[] args = pt.getActualTypeArguments();
        Type[] vars = currentClass.getTypeParameters();
        for (int i = 0; i < args.length; ++i) {
          resolver.put(vars[i], args[i]);
        }
      }

      // Otherwise, it must be a class and nothing has to be done
      else if (currentType instanceof Class<?>) {
        currentClass = (Class<?>) currentType;
      }

      // Normally it's not possible. But we avoid strange bugs.
      else {
        throw new Error("Case not possible");
      }

      // If the target is an interface, check implemented interfaces
      if (target.isInterface()) {
        Class<?>[] intfs = currentClass.getInterfaces();
        Type[] types = currentClass.getGenericInterfaces();
        for (int i = 0; i < intfs.length; ++i) {
          // If an implemented interface extends the target, loop on it
          if (target.isAssignableFrom(intfs[i])) {
            currentType = types[i];
            break;
          }
        }
      }

      // If the superclass extends/implements the target, loop on it
      if ((currentClass.getSuperclass() != null)
          && target.isAssignableFrom(currentClass.getSuperclass())) {
        currentType = currentClass.getGenericSuperclass();
      }
    }

    Type[] args = new Type[params.length];
    for (int i = 0; i < params.length; ++i) {
      Type arg = params[i];
      while (resolver.containsKey(arg)) {
        arg = resolver.get(arg);
      }
      args[i] = arg;
    }

    return args;
  }
}
