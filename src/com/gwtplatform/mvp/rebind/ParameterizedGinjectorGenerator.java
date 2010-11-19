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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.resource.ResourceOracle;
import com.google.gwt.inject.client.GinModule;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.uibinder.rebind.IndentedWriter;
import com.google.gwt.uibinder.rebind.MortalLogger;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The generator for {@link com.gwtplatform.mvp.client.ParameterizedGinjector ParameterizedGinjector}
 * classes. This class wraps a standard {@link Ginjector} but allows the user to specify ginjector 
 * methods that take an {@link Annotation} as a parameter, making it possible to retrieve an object given
 * its classname and annotation.
 * 
 * @author Philippe Beaudoin
 */
public class ParameterizedGinjectorGenerator extends Generator {

  private static final String WRAPPED_GINJECTOR_FIELD_NAME = "internalWrappedGinjector";
  
  private JClassType ginModulesType;
  private JClassType ginjectorType;
  private JClassType gwtType;
  private JClassType annotationType;

  @Override
  public String generate(TreeLogger logger, GeneratorContext genContext, String interfaceName) throws UnableToCompleteException {
    TypeOracle oracle = genContext.getTypeOracle();
    ResourceOracle resourceOracle = genContext.getResourcesOracle();

    JClassType interfaceType;
    try {
      interfaceType = oracle.getType(interfaceName);
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    }
    
    String implementationName = interfaceType.getName().replace('.', '_') + "Impl";
    String packageName = interfaceType.getPackage().getName();
    
    PrintWriter writer = genContext.tryCreate(logger, packageName, implementationName);
    
    if (writer != null) {
      generateOnce(interfaceType, packageName, implementationName, writer, logger, oracle,
          resourceOracle);
      genContext.commit(logger, writer);
    }
    
    return packageName + "." + implementationName;    
  }

  private void generateOnce(JClassType interfaceType, String packageName,
      String implementationName, PrintWriter basicWriter, TreeLogger treeLogger,
      TypeOracle oracle, ResourceOracle resourceOracle) throws UnableToCompleteException  {
  
    findTypes(oracle);
    
    MortalLogger logger = new MortalLogger(treeLogger);
  
    // We will wrap a basic ginjector for all the standard methods we are not taking care of.
    String basicGinjectorTypeName = interfaceType.getName().replace('.', '_') + "BasicGinjector";
    
    // Copy the module annotation to the basic ginjector
    GinModules declaredModules = interfaceType.getAnnotation(GinModules.class);
    List<String> basicGinjectorModules = new ArrayList<String>(declaredModules.value().length);
    for (Class<? extends GinModule> module : declaredModules.value()) {
      basicGinjectorModules.add(module.getCanonicalName() + ".class");
    }
    
    JMethod methods[] = interfaceType.getMethods();
    List<BasicGinjectorMethod> basicGinjectorMethods = identifyBasicGinjectorMethods(methods);

    List<ParameterizedGinjectorMethod> parameterizedGinjectorMethods = identifyParameterizedGinjectorMethods(
        methods, logger, oracle);

    // TODO Use Guice SPI to identify annotated bindings concerning bound types observed in parameterizedGinjectorMethods
    
    parameterizedGinjectorMethods.get(0);
    // Package
    IndentedWriter writer = new IndentedWriter(basicWriter);
    writer.write("package %1$s;", packageName);
    writer.newline();

    // Imports
    writer.write("import %s;", ginModulesType.getQualifiedSourceName());
    writer.write("import %s;", ginjectorType.getQualifiedSourceName());
    writer.write("import %s;", gwtType.getQualifiedSourceName());
    writer.newline();
    
    // Open class
    writer.write("public class %s implements %s {",
        implementationName, interfaceType.getName());
    writer.indent();
    writer.newline();
    
    writeBasicGinjectorInterface(writer, logger, basicGinjectorTypeName, 
        basicGinjectorModules, basicGinjectorMethods, parameterizedGinjectorMethods);    

    writer.write("private %1$s %2$s = GWT.create(%1$s.class);", basicGinjectorTypeName, WRAPPED_GINJECTOR_FIELD_NAME);
    writer.newline();
    
    writeBasicForwardingMethods(writer, logger, basicGinjectorMethods);
    
    writeParameterizedForwardingMethods(writer, logger, parameterizedGinjectorMethods);
    
    writer.write("public Ginjector getGinjector() { return %s; }", WRAPPED_GINJECTOR_FIELD_NAME);
    
    // Close class
    writer.outdent();
    writer.write("}");    
  }

  /**
   * Identify all the methods with empty parameters or void return values.
   * These will be declared in the basic ginjector, and an implementation forward the call to the
   * basic ginjector will be implemented in the parameterized ginjector.
   * void parameters are kept because GIN allows 1-parameter methods that return void
   * (not sure what these are for?)
   */
  private List<BasicGinjectorMethod> identifyBasicGinjectorMethods(
      JMethod[] methods) {
    List<BasicGinjectorMethod> basicGinjectorMethods = new ArrayList<BasicGinjectorMethod>(methods.length);
    for (JMethod method : methods) {
      if (method.getParameters().length == 0 || method.getReturnType() == JPrimitiveType.VOID) {
        basicGinjectorMethods.add(new BasicGinjectorMethod(method));
      }
    }
    return basicGinjectorMethods;
  }

  /**
   * Identify all the methods with one annotation parameter and non-void return values.
   * For all annotations bound to the return type a different method will be declared in the basic ginjector.
   * A single implementation will be declared in the parameterized ginjector, routing the
   * call to the correct method of the basic ginjector.
   */
  private List<ParameterizedGinjectorMethod> identifyParameterizedGinjectorMethods(
      JMethod[] methods, MortalLogger logger, TypeOracle oracle)
      throws UnableToCompleteException {
    List<ParameterizedGinjectorMethod> parameterizedGinjectorMethods = new ArrayList<ParameterizedGinjectorMethod>(methods.length);
    for (JMethod method : methods) {
      if (method.getParameters().length == 1 && method.getReturnType() != JPrimitiveType.VOID) {
        JType parameterType = method.getParameters()[0].getType();
        JClassType asClassType = parameterType.isClassOrInterface();
        if (asClassType == annotationType) {
          parameterizedGinjectorMethods.add(new ParameterizedGinjectorMethod(method, oracle));
        } else {
          logger.die("The method %s in your ginjector takes an invalid parameter. Only Annotation is allowed as a parameter.", 
              method.getName());
        }        
      }
    }
    return parameterizedGinjectorMethods;
  }
  
  /**
   * Fills all the types that are required to generate this class.
   */
  private void findTypes(TypeOracle oracle) {
    ginModulesType = oracle.findType(GinModules.class.getCanonicalName());
    ginjectorType = oracle.findType(Ginjector.class.getCanonicalName());
    gwtType = oracle.findType(GWT.class.getCanonicalName());
    annotationType = oracle.findType(Annotation.class.getCanonicalName());
  }

  /**
   * Writes the basic ginjector wrapped in our class.
   */
  private void writeBasicGinjectorInterface(IndentedWriter writer,
      MortalLogger logger,
      String basicGinjectorTypeName,
      Collection<String> basicGinjectorModules,
      Collection<BasicGinjectorMethod> basicGinjectorMethods,
      Collection<ParameterizedGinjectorMethod> parameterizedGinjectorMethods) throws UnableToCompleteException {
    
    // Open basic ginjector class
    writer.write("@GinModules({%s})", StringUtils.join(basicGinjectorModules, ", "));
    writer.write("public interface %s extends %s {",
        basicGinjectorTypeName, Ginjector.class.getCanonicalName());
    writer.indent();

    for (BasicGinjectorMethod method : basicGinjectorMethods) {
      method.writeDeclaration(writer, logger);
    }    

    for (ParameterizedGinjectorMethod method : parameterizedGinjectorMethods) {
      method.writeDeclarations(writer, logger);
    }    
    
    // Close basic ginjector class
    writer.outdent();
    writer.write("}");
    writer.newline();
  }
  
  /**
   * Writes the methods forwarding calls from our ginjector to the wrapped one, for
   * basic non-parameterized methods.
   */
  private void writeBasicForwardingMethods(IndentedWriter writer,
      MortalLogger logger,
      Collection<BasicGinjectorMethod> basicGinjectorMethods) throws UnableToCompleteException {
    
    for (BasicGinjectorMethod method : basicGinjectorMethods) {
      method.writeImplementation(WRAPPED_GINJECTOR_FIELD_NAME, writer, logger);
    }    
  }
  
  /**
   * Writes the methods forwarding calls from our ginjector to the wrapped one, for
   * parameterized methods.
   */
  private void writeParameterizedForwardingMethods(IndentedWriter writer,
      MortalLogger logger,
      List<ParameterizedGinjectorMethod> parameterizedGinjectorMethods) throws UnableToCompleteException {
    for (ParameterizedGinjectorMethod method : parameterizedGinjectorMethods) {
      method.writeImplementation(WRAPPED_GINJECTOR_FIELD_NAME, writer, logger);
    }    
  }
  
}