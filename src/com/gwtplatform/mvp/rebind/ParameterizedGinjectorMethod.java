/**
 * Copyright 2010 ArcBees Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gwtplatform.mvp.rebind;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.uibinder.rebind.IndentedWriter;
import com.google.gwt.uibinder.rebind.MortalLogger;
import com.google.inject.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class identifies a method that takes an Annotation or a Class<? extends Annotation> as a 
 * parameter. 
 * 
 * @author Philippe Beaudoin
 */
public class ParameterizedGinjectorMethod {

  private final JMethod method;
  private final TypeOracle oracle;
  private final List<Annotation> bindingAnnotations = new ArrayList<Annotation>();
  
  private JClassType providerType;
  private JClassType asyncProviderType;

  private JClassType bindingType;
  
  public ParameterizedGinjectorMethod(JMethod method, TypeOracle oracle) {
    this.method = method;
    this.oracle = oracle;
  }

  /**
   * Indicates which binding type this method is interested in. This is the
   * same as the return type of the method, unless the return type is a 
   * {@link Provider} or a {@link AsyncProvider}.  
   * @throws UnableToCompleteException 
   */
  public JClassType getBindingType(MortalLogger logger) throws UnableToCompleteException {
    ensureBindingType(logger);
    return bindingType;
  }
  
  /**
   * Adds an annotation that was used, in one of the modules, to bind the type
   * returned by {@link #getBindingType()}.
   * 
   * @param annotation The {@link Annotation} that was observed.
   */
  public void addBindingAnnotation(Annotation annotation) {
    bindingAnnotations.add(annotation);
  }
  
  /**
   * Generate all the method declarations, in the wrapped ginjector, for the getters
   * corresponding to each of the recorded {@code bindingAnnotations}.
   */
  public void writeDeclarations(IndentedWriter writer, MortalLogger logger) throws UnableToCompleteException {
    String returnTypeString = method.getReturnType().getParameterizedQualifiedSourceName();
    int index = 0;
    for (Annotation annotation : bindingAnnotations) {
      String methodDeclaration = String.format("%s %s_%d();", returnTypeString, method.getName(), index);
      writeInstantiableAnnotation(annotation, "", methodDeclaration, writer, logger);
      index++;
    }
    writer.newline();
  }

  /**
   * Generate the method implementation, in the generated class, forwarding calls to the wrapped ginjector.
   */
  public void writeImplementation(String wrappedGinjectorFieldName, 
      IndentedWriter writer, MortalLogger logger) throws UnableToCompleteException { 
    writer.write("%s %s(%s param) {",
        method.getReturnType().getParameterizedQualifiedSourceName(),
        method.getName(),
        method.getParameters()[0].getType().isClassOrInterface().getParameterizedQualifiedSourceName());
    writer.indent();
    
    int index = 0;
    for (Annotation annotation : bindingAnnotations) {
      writeIfForAnnotation(annotation, index, wrappedGinjectorFieldName, writer, logger);
      index++;
    }
    
    writer.outdent();
    writer.write("}");
    writer.newline();
  }

  private void writeIfForAnnotation(Annotation annotation, int index, String wrappedGinjectorFieldName,
      IndentedWriter writer, MortalLogger logger) throws UnableToCompleteException {    
    // Begin if condition
    writer.write("if (");
    writer.indent();

    writeAnnotationEqualityTest(annotation, "param", "", writer, logger);
    
    // End if condition
    writer.outdent();
    writer.write(") {");
    
    // Begin if body
    writer.indent();

    writer.write("return %s.%s_%d();", wrappedGinjectorFieldName, "", method.getName(), index);
    
    // End if body
    writer.outdent();
    writer.write("}");
  }

  /**
   * Writes a deep equality test for an annotation. The annotation type will be verified, as well as
   * the equality of all fields in the annotation. 
   * 
   * @param parameterName The name of the parameter for which to write the equality test.
   * @param suffix A suffix to append to the last element in the comparison.
   */
  void writeAnnotationEqualityTest(Annotation annotation,
      String parameterName, String suffix, IndentedWriter writer, MortalLogger logger) throws UnableToCompleteException {

    Class<? extends Annotation> annotationClass = annotation.annotationType();
    List<Method> annotationMethods = removeDefaultMethods(annotationClass.getMethods(), false);
    int nbMethods = annotationMethods.size();     
    for (int i = 0; i < nbMethods; ++i) {
      Method annotationMethod = annotationMethods.get(i);
      String realParameterName = parameterName;
      if (!annotationMethod.getName().equals("annotationType")) {
        // We are guaranteed that the type is right, cast the parameter
        realParameterName = String.format("((%s) %s)", annotation.annotationType().getCanonicalName(), parameterName);
      }
      writeAnnotationMethodComparison(annotationMethod, annotation, realParameterName, 
          (i == nbMethods - 1) ? suffix : " &&", writer, logger);
    }
  }

  /**
   * Writes a comparison for one method of the annotation. For example, if the specified
   * annotation method is {@code phoneNumber()} which returns a string "123-4567", this will write:
   * <pre>"123-4567".equals(param.phoneNumber())</pre>.
   * 
   * @param parameterName: The name of the parameter for which to write the equality test.
   * @param suffix: The suffix to append to the comparison.
   */
  private void writeAnnotationMethodComparison(Method annotationMethod, Annotation annotation, String parameterName,
      String suffix, IndentedWriter writer, MortalLogger logger) throws UnableToCompleteException {
    if (annotationMethod.getParameterTypes().length != 0) {
      logger.die("Method '@%s' in annotation '%s' has parameters, this should never happen.", annotationMethod.getName(), annotation.getClass().getName());
    }
    String methodInvocation = String.format("%s.%s()", parameterName, annotationMethod.getName());
    Object methodInvocationResult = invokeAnnotationMethod(annotationMethod, annotation, logger);

    if (methodInvocationResult.getClass().isArray()) {
      Object[] components = (Object[]) methodInvocationResult;
      int nbComponents = components.length;
      for (int i = 0; i < nbComponents; ++i) {
        String componentOfMethodInvocation = String.format("%s[%d]", methodInvocation, i);
        writeComponentEqualityTest(components[i], componentOfMethodInvocation, 
            (i == nbComponents - 1) ? suffix : " &&",   // The suffix is a && if not the last component 
            writer, logger);        
      }
    } else {
      writeComponentEqualityTest(methodInvocationResult, methodInvocation, suffix, writer, logger);
    }
  }

  /**
   * Writes an equality test for a single component of an annotation.
   * 
   * @param desiredResult The object corresponding to the desired result.
   * @param evaluationString A string of code which should evaluate to the desired result.
   * @param suffix A suffix to append when writing the equality test.
   */
  private void writeComponentEqualityTest(Object desiredResult, String evaluationString,
      String suffix, IndentedWriter writer,
      MortalLogger logger) throws UnableToCompleteException {
    if (Annotation.class.isAssignableFrom(desiredResult.getClass())) {
      writeAnnotationEqualityTest((Annotation) desiredResult, evaluationString, suffix, writer, logger);
    } else {
      String completeSuffix;
      if (isWrapperType(desiredResult.getClass())) {
        completeSuffix = String.format(" == %s%s", evaluationString, suffix);          
      } else {
        completeSuffix = String.format(".equals(%s)%s", evaluationString, suffix);
      }
      writeParsableObject(desiredResult, 
          "",  // No prefix 
          completeSuffix, writer, logger);
    }
  }

  /**
   * Writes out an annotation that can be instantiated by a java parser.
   * In the style {@code @Annotation(...)}. The specified {@code prefix} and {@code suffix} 
   * will be prepended or appended.
   */
  void writeInstantiableAnnotation(Annotation annotation, String prefix, String suffix, IndentedWriter writer,
      MortalLogger logger) throws UnableToCompleteException {
    Class<? extends Annotation> annotationClass = annotation.annotationType();
    List<Method> annotationMethods = removeDefaultMethods(annotationClass.getMethods(), true);
    
    String annotationWithoutParams = prefix + "@" + annotationClass.getCanonicalName();
    int nbMethods = annotationMethods.size(); 
    if (nbMethods == 0) {    
      writer.write(annotationWithoutParams);
    } else {
      writer.write(annotationWithoutParams + "(");
      writer.indent();
      
      for (int i = 0; i < nbMethods; ++i) {
        Method annotationMethod = annotationMethods.get(i);
        writeAnnotationMethodAssignation(annotationMethod, annotation, 
            (i == nbMethods - 1) ? "" : ",",  // Suffix with a comma if not the last element
            writer, logger);
      }

      writer.outdent();
      writer.write(")" + suffix);
    }    
  }

  /**
   * Removes default methods {@code equals}, {@code hashCode}, {@code toString} and optionally {@code annotationType}.
   * If {@code annotationType} is desired, it is always the first method of the returned list.
   * 
   * @param removeAnnotationType {@code true} if the {@code annotationType} method should be removed, {@code false} otherwise.
   */
  private List<Method> removeDefaultMethods(Method[] methods, boolean removeAnnotationType) {
    List<Method> result = new ArrayList<Method>(methods.length);
    for (Method currentMethod : methods) {
      if (currentMethod.getParameterTypes().length == 0) {
        String name = currentMethod.getName();
        if (!"hashCode".equals(name) &&
            !"toString".equals(name)) {
          if ("annotationType".equals(name)) {
            if (!removeAnnotationType) {
              result.add(0, currentMethod);
            }
          } else {
            result.add(currentMethod);
          }
        }            
      }
    }
    return result;
  }

  /**
   * Writes an assignation for one method of the annotation. For example, if the specified
   * annotation method is {@code phoneNumber()} which returns a string "123-4567", this will write:
   * <pre>phoneNumber = "123-4567"</pre>.
   * 
   * @param suffix A suffix to append to the last element in the comparison.
   */
  private void writeAnnotationMethodAssignation(Method annotationMethod, Annotation annotation,
      String suffix, IndentedWriter writer, MortalLogger logger) throws UnableToCompleteException {
    if (annotationMethod.getParameterTypes().length != 0) {
      logger.die("Method '@%s' in annotation '%s' has parameters, this should never happen.", annotationMethod.getName(), annotation.getClass().getName());
    }
    String assignation = String.format("%s = ", annotationMethod.getName());
    Object methodInvocationResult = invokeAnnotationMethod(annotationMethod, annotation, logger);

    if (methodInvocationResult.getClass().isArray()) {
      writer.write(assignation + "{");
      writer.indent();      
      Object[] components;
        components = (Object[]) methodInvocationResult;
      int nbComponents = components.length;
      for (int i = 0; i < nbComponents; ++i) {
        writeParsableObject(components[i], 
            "",                                       // No prefix 
            (i == nbComponents - 1) ? "" : ",",   // Suffix is a comma if not the last element 
            writer, logger);
      }
      writer.outdent();
      writer.write("}" + suffix);
    } else {
      writeParsableObject(methodInvocationResult,
          assignation,        // Prefix with assignation string
          suffix,
          writer, logger);
    }
  }

  /**
   * Invokes the specified method on the specified annotation and logs the failure if needed. 
   */
  private Object invokeAnnotationMethod(Method annotationMethod,
      Annotation annotation, MortalLogger logger)
      throws UnableToCompleteException {
    Object methodInvocationResult = null;
    try {
      methodInvocationResult = annotationMethod.invoke(annotation);
    } catch (IllegalArgumentException e) {
      logger.die("Illegal argument, invoking method '@%s' in annotation '%s'", annotationMethod.getName(), annotation.getClass().getName());
    } catch (IllegalAccessException e) {
      logger.die("Illegal access, invoking method '@%s' in annotation '%s'", annotationMethod.getName(), annotation.getClass().getName());
    } catch (InvocationTargetException e) {
      logger.die("Invalid invocation target, invoking method '@%s' in annotation '%s'", annotationMethod.getName(), annotation.getClass().getName());
    }
    return methodInvocationResult;
  }
  
  /**
   * Writes a string that can be parsed by Java to instantiate this object. 
   * Only accepted objects are primitive types, String, Class<>, Annotation-derived classes,
   * and Enum-derived classes. The specified {@code prefix} and {@code suffix} 
   * will be prepended or appended.
   * <p />
   * For example, a string will be written as {@code "content of the string"}, a class
   * will be written as {@code ClassName.class}, and an annotation as {@code @TheAnnotation(...)}.
   */
  private void writeParsableObject(Object object, String prefix, String suffix,
      IndentedWriter writer, MortalLogger logger) throws UnableToCompleteException {
    if (isWrapperType(object.getClass())) {
      writer.write(prefix + object.toString() + suffix);
    } else if (object.getClass() == String.class) {
      writer.write(prefix + "\"" + object.toString() + "\"" + suffix);
    } else if (object.getClass() == Class.class) {
      writer.write(prefix + ((Class<?>) object).getCanonicalName() + ".class" + suffix);
    } else if (Annotation.class.isAssignableFrom(object.getClass())) {
      writeInstantiableAnnotation((Annotation) object, prefix, suffix, writer, logger);
    } else if (Enum.class.isAssignableFrom(object.getClass())) {
      writer.write(prefix + 
          object.getClass().getCanonicalName() + "." + ((Enum<?>) object).name() + suffix);
    } else {
      logger.die("Encountered an annotation method returning unsupported type '%s'.", object.getClass().getCanonicalName());
    }
  }
  
  @SuppressWarnings("unchecked")
  private static final Set<Class<?>> WRAPPER_TYPES = new HashSet<Class<?>>(
      Arrays.asList(Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class));
  
  public static boolean isWrapperType(Class<?> clazz) {
      return WRAPPER_TYPES.contains(clazz);
  }

  private void ensureBindingType(MortalLogger logger) throws UnableToCompleteException {
    ensureTypes();
    
    if (bindingType != null) {
      return;
    }
    bindingType = method.getReturnType().isClassOrInterface();
    if (bindingType == null) {
      logger.die("Return type of ginjector method '%s' is not a class.", method.getName());
    }
    if (bindingType.getErasedType() == providerType.getErasedType() ||
        bindingType.getErasedType() == asyncProviderType.getErasedType()) {
      bindingType = bindingType.isParameterized().getTypeArgs()[0];
    }
  }

  private void ensureTypes() {
    providerType = oracle.findType(Provider.class.getCanonicalName());
    asyncProviderType = oracle.findType(AsyncProvider.class.getCanonicalName());
  }  
}
