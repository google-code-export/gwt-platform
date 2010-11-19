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
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;
import com.google.gwt.uibinder.rebind.IndentedWriter;
import com.google.gwt.uibinder.rebind.MortalLogger;

import static org.junit.Assert.assertEquals;

import org.jukito.All;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Philippe Beaudoin
 */
@RunWith(JukitoRunner.class)
public class ParameterizedGinjectorMethodTests {
  
  /**
   * Test module.
   */
  public static class Module extends JukitoModule {
    @Override
    protected void configureTest() {
      bindManyInstances(TestData.class,
        new TestData("objectWithAnnotation1",
            // Instantiation
            "@com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation1\n",
            // Equality test
            "com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation1.class.equals(param.annotationType())\n"),
        new TestData("objectWithAnnotation2",
            // Instantiation
            "@com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation2(\n" +
            "  value = \"dummy\"\n" +
            ")\n",
            // Equality test
            "com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation2.class.equals(param.annotationType()) &&\n" +
            "\"dummy\".equals(((com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation2) param).value())\n"),
        new TestData("objectWithAnnotation3",
            // Instantiation
            "@com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation3(\n" +
            "  a = 10,\n" +
            "  b = {\n" +
            "    com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.MyEnum.V3,\n" +
            "    com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.MyEnum.V2,\n" +
            "    com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.MyEnum.V1\n" +
            "  }\n" +
            ")\n",
            // Equality test
            "com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation3.class.equals(param.annotationType()) &&\n" +
            "10 == ((com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation3) param).a() &&\n" +
            "com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.MyEnum.V3.equals(((com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation3) param).b()[0]) &&\n" +
            "com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.MyEnum.V2.equals(((com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation3) param).b()[1]) &&\n" +
            "com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.MyEnum.V1.equals(((com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation3) param).b()[2])\n"),
        new TestData("objectWithAnnotation4",
            // Instantiation
            "@com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation4(\n" +
            "  x = @com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation3(\n" +
            "    a = 50,\n" +
            "    b = {\n" +
            "      com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.MyEnum.V2\n" +
            "    }\n" +
            "  ),\n" +
            "  y = java.lang.String.class\n" +
            ")\n",
            // Equality test
            "com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation4.class.equals(param.annotationType()) &&\n" +
            "com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation3.class.equals(((com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation4) param).x().annotationType()) &&\n" +
            "50 == ((com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation3) ((com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation4) param).x()).a() &&\n" +
            "com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.MyEnum.V2.equals(((com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation3) ((com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation4) param).x()).b()[0]) &&\n" +
            "java.lang.String.class.equals(((com.gwtplatform.mvp.rebind.ParameterizedGinjectorMethodTests.Annotation4) param).y())\n")            
      );      
    }
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  @interface Annotation1 { }
  @Annotation1 public Object objectWithAnnotation1;

  @Retention(RetentionPolicy.RUNTIME)
  @interface Annotation2 {
    String value();
  }
  @Annotation2("dummy") public Object objectWithAnnotation2;

  enum MyEnum { V1, V2, V3 }; 
  @Retention(RetentionPolicy.RUNTIME)
  @interface Annotation3 {
    int a();
    MyEnum[] b(); 
  }
  @Annotation3(
      a = 10,
      b = {MyEnum.V3, MyEnum.V2, MyEnum.V1}) public Object objectWithAnnotation3;
  
  @Retention(RetentionPolicy.RUNTIME)
  @interface Annotation4 {
    Annotation3 x(); 
    Class<?> y();
  }
  @Annotation4(
      x = @Annotation3(a = 50, b = {MyEnum.V2}),
      y = String.class) public Object objectWithAnnotation4;
  
  static class TestData {
    final String fieldName;
    final String expectedInstantiationResult;
    final String expectedEqualityTestResult;
    TestData(String fieldName, String expectedInstantiationResult, 
        String expectedEqualityTestResult) {
      this.fieldName = fieldName;
      this.expectedInstantiationResult = expectedInstantiationResult;
      this.expectedEqualityTestResult = expectedEqualityTestResult;
    }
  }
  
  @Test
  public void writeInstantiableAnnotationTest(@All TestData testData) throws SecurityException, NoSuchFieldException, UnableToCompleteException {
    // GIVEN
    ParameterizedGinjectorMethod sut = new ParameterizedGinjectorMethod(null, null); 
    
    StringWriter stringWriter = new StringWriter();
    IndentedWriter writer = new IndentedWriter(new PrintWriter(stringWriter));
    MortalLogger logger = new MortalLogger(new PrintWriterTreeLogger());    
    Annotation annotation = ParameterizedGinjectorMethodTests.class.getField(testData.fieldName).getAnnotations()[0];
    
    // WHEN
    sut.writeInstantiableAnnotation(annotation, "", "", writer, logger);
    
    // THEN
    assertEquals(testData.expectedInstantiationResult, stringWriter.toString());
  }
  
  @Test
  public void writeAnnotationEqualityTestTest(@All TestData testData) throws SecurityException, NoSuchFieldException, UnableToCompleteException {
    // GIVEN
    ParameterizedGinjectorMethod sut = new ParameterizedGinjectorMethod(null, null); 
    
    StringWriter stringWriter = new StringWriter();
    IndentedWriter writer = new IndentedWriter(new PrintWriter(stringWriter));
    MortalLogger logger = new MortalLogger(new PrintWriterTreeLogger());    
    Annotation annotation = ParameterizedGinjectorMethodTests.class.getField(testData.fieldName).getAnnotations()[0];    
    
    // WHEN
    sut.writeAnnotationEqualityTest(annotation, "param", "", writer, logger);
    
    // THEN
    assertEquals(testData.expectedEqualityTestResult, stringWriter.toString());
  }   
}
