package com.gwtplatform.mvp.client.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import com.gwtplatform.mvp.rebind.TabDescriptionGenerator;

/**
 * This annotation is used on other annotations that describe tab content, such as
 * {@link TabInfo}. It indicates which class should be used to generate the tab
 * description given the information in the {@link TabInfo}-like. 
 * 
 * @author Philippe Beaudoin
 */
@Target(ElementType.TYPE)
public @interface TabInfoGenerator {
  Class<? extends TabDescriptionGenerator> value();
}
