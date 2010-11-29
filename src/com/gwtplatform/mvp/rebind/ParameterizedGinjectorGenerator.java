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
import com.google.gwt.inject.rebind.adapter.GinModuleAdapter;
import com.google.gwt.inject.rebind.util.KeyUtil;
import com.google.gwt.uibinder.rebind.IndentedWriter;
import com.google.gwt.uibinder.rebind.MortalLogger;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  
  private KeyUtil keyUtil;

  @Override
  public String generate(TreeLogger logger, GeneratorContext genContext, String interfaceName) throws UnableToCompleteException {
    TypeOracle oracle = genContext.getTypeOracle();
    ResourceOracle resourceOracle = genContext.getResourcesOracle();
    keyUtil = new KeyUtil(oracle, null, null);  // No need for a name generator in the methods we use

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
    List<Module> modules = new ArrayList<Module>();
    Set<Class<? extends GinModule>> ginModules = new HashSet<Class<? extends GinModule>>();
    populateModulesFromInjectorInterface(interfaceType, modules, ginModules, logger);
    List<String> basicGinjectorModules = new ArrayList<String>(ginModules.size());
    for (Class<? extends GinModule> ginModule : ginModules) {
      basicGinjectorModules.add(ginModule.getCanonicalName() + ".class");
    }
    
    JMethod methods[] = interfaceType.getMethods();
    List<BasicGinjectorMethod> basicGinjectorMethods = identifyBasicGinjectorMethods(methods);

    List<ParameterizedGinjectorMethod> parameterizedGinjectorMethods = identifyParameterizedGinjectorMethods(
        methods, logger, oracle);

    Map<Class<?>, ParameterizedGinjectorMethod> bindingsOfInterest =
        createBindingsOfInterest(parameterizedGinjectorMethods, logger);

    // Use Guice SPI to identify annotated bindings concerning bound types observed in parameterizedGinjectorMethods
    List<Element> elements = Elements.getElements(modules);
    for (Element element : elements) {
      LookForAnnotatedBindingVisitor visitor = new LookForAnnotatedBindingVisitor(bindingsOfInterest);
      element.acceptVisitor(visitor);
    }
    
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

  private Map<Class<?>, ParameterizedGinjectorMethod> createBindingsOfInterest(
      List<ParameterizedGinjectorMethod> parameterizedGinjectorMethods, 
      MortalLogger logger) throws ProvisionException, UnableToCompleteException {
    Map<Class<?>, ParameterizedGinjectorMethod> result = new HashMap<Class<?>, ParameterizedGinjectorMethod>();
    for (ParameterizedGinjectorMethod method : parameterizedGinjectorMethods) {
      Key<?> key = keyUtil.getKey(method.getBindingType(logger));
      result.put(key.getTypeLiteral().getRawType(), method);
    }
    return result;
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

  /**
   * Find all the gin modules annotating this interface and any parent interface.
   * Copied from GIN source code.
   */
  private void populateModulesFromInjectorInterface(JClassType iface, List<Module> modules,
      Set<Class<? extends GinModule>> added, MortalLogger logger) throws UnableToCompleteException {
    GinModules gmodules = iface.getAnnotation(GinModules.class);
    if (gmodules != null) {
      for (Class<? extends GinModule> moduleClass : gmodules.value()) {
        if (added.contains(moduleClass)) {
          continue;
        }

        Module module = instantiateGModuleClass(moduleClass, logger);
        if (module != null) {
          modules.add(module);
          added.add(moduleClass);
        }
      }
    }

    for (JClassType superIface : iface.getImplementedInterfaces()) {
      populateModulesFromInjectorInterface(superIface, modules, added, logger);
    }
  }

  private Module instantiateGModuleClass(Class<? extends GinModule> moduleClassName,
      MortalLogger logger) throws UnableToCompleteException {
    try {
      Constructor<? extends GinModule> constructor = moduleClassName.getDeclaredConstructor();
      try {
        constructor.setAccessible(true);
        return new GinModuleAdapter(constructor.newInstance());
      } finally {
        constructor.setAccessible(false);
      }
    } catch (IllegalAccessException e) {
      logger.die("Error creating module: " + moduleClassName, e);
    } catch (InstantiationException e) {
      logger.die("Error creating module: " + moduleClassName, e);
    } catch (NoSuchMethodException e) {
      logger.die("Error creating module: " + moduleClassName, e);
    } catch (InvocationTargetException e) {
      logger.die("Error creating module: " + moduleClassName, e);
    }

    return null;
  }
  
  /**
   * A visitor only interested in elements that are bindings.
   */
  private class LookForAnnotatedBindingVisitor extends DefaultElementVisitor<Void> {
    private final Map<Class<?>, ParameterizedGinjectorMethod> bindingsOfInterest;

    public LookForAnnotatedBindingVisitor(
        Map<Class<?>, ParameterizedGinjectorMethod> bindingsOfInterest) {
      this.bindingsOfInterest = bindingsOfInterest;
    }

    @Override
    public <T> Void visit(Binding<T> binding) {
      Key<T> key = binding.getKey();
      Annotation annotation = key.getAnnotation(); 
      if (annotation != null) {
        ParameterizedGinjectorMethod methodOfInterest =
            bindingsOfInterest.get(key.getTypeLiteral().getRawType());
        if (methodOfInterest != null) {
          methodOfInterest.addBindingAnnotation(annotation);
        }
      }
      return null;
    }
  }
  
}