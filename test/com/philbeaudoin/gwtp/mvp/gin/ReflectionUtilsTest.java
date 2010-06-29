package com.philbeaudoin.gwtp.mvp.gin;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.philbeaudoin.gwtp.mvp.client.EventBus;
import com.philbeaudoin.gwtp.mvp.client.PresenterImpl;
import com.philbeaudoin.gwtp.mvp.client.View;
import com.philbeaudoin.gwtp.mvp.client.proxy.Proxy;
import com.philbeaudoin.gwtp.mvp.gin.ReflectionUtils;

public class ReflectionUtilsTest {
  /**
   * A class which is not a presenter
   */
  static class A_NotAPresenter {
    // Intentionally empty
  }

  /**
   * A simple presenter
   */
  static class B_DirectPresenter extends PresenterImpl<FakeView, FakeProxy> {
    public B_DirectPresenter(EventBus eventBus, FakeView view, FakeProxy proxy) {
      super(eventBus, view, proxy);
    }

    @Override
    protected void revealInParent() {
      // Nothing to do
    }
  }

  /**
   * A base class where the two type variables are defined
   */
  static abstract class C_AbstractPresenter extends
      PresenterImpl<FakeView, FakeProxy> {
    public C_AbstractPresenter() {
      super(null, null, null);
    }

    @Override
    protected void revealInParent() {
      // Nothing to do
    }
  }

  /**
   * A presenter derived from the {@link C_AbstractPresenter} class
   */
  static class C_IndirectPresenter extends C_AbstractPresenter {
    // Intentionally empty
  }

  /**
   * A base class where one of the two type variables are defined
   */
  static abstract class D_AbstractPresenter<P extends Proxy<?>> extends
      PresenterImpl<FakeView, P> {
    public D_AbstractPresenter() {
      super(null, null, null);
    }

    @Override
    protected void revealInParent() {
      // Nothing to do
    }
  }

  /**
   * A presenter derived from the {@link D_AbstractPresenter} class
   */
  static class D_IndirectPresenter extends D_AbstractPresenter<FakeProxy> {
    // Intentionally empty
  }

  /**
   * A base class
   */
  static abstract class E_AbstractPresenter<V extends View, P extends Proxy<?>>
      extends PresenterImpl<V, P> {
    public E_AbstractPresenter() {
      super(null, null, null);
    }

    @Override
    protected void revealInParent() {
      // Nothing to do
    }
  }

  /**
   * A presenter derived from the {@link E_AbstractPresenter} class
   */
  static class E_IndirectPresenter extends
      E_AbstractPresenter<FakeView, FakeProxy> {
    // Intentionally empty
  }

  @Test
  public void testInternal() {
    Type[] args = ReflectionUtils.getTypeArguments(Object.class,
        Collection.class);
    Assert.assertArrayEquals(new Type[0], args);

    args = ReflectionUtils.getTypeArguments(Collection.class, Collection.class);
    Assert.assertArrayEquals(Collection.class.getTypeParameters(), args);

    args = ReflectionUtils.getTypeArguments(Collection.class, List.class);
    Assert.assertArrayEquals(List.class.getTypeParameters(), args);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotAPresenter() {
    ReflectionUtils
        .getTypeArguments(PresenterImpl.class, A_NotAPresenter.class);
  }

  @Test
  public void testDirect() {
    Type[] args = ReflectionUtils.getTypeArguments(PresenterImpl.class,
        B_DirectPresenter.class);
    Assert.assertEquals(2, args.length);
    Assert.assertEquals(FakeView.class, args[0]);
    Assert.assertEquals(FakeProxy.class, args[1]);
  }

  @Test
  public void testIndirectC() {
    Type[] args = ReflectionUtils.getTypeArguments(PresenterImpl.class,
        C_IndirectPresenter.class);
    Assert.assertEquals(2, args.length);
    Assert.assertEquals(FakeView.class, args[0]);
    Assert.assertEquals(FakeProxy.class, args[1]);

    args = ReflectionUtils.getTypeArguments(PresenterImpl.class,
        C_AbstractPresenter.class);
    Assert.assertEquals(2, args.length);
    Assert.assertEquals(FakeView.class, args[0]);
    Assert.assertEquals(FakeProxy.class, args[1]);
  }

  @Test
  public void testIndirectD() {
    Type[] args = ReflectionUtils.getTypeArguments(PresenterImpl.class,
        D_IndirectPresenter.class);
    Assert.assertEquals(2, args.length);
    Assert.assertEquals(FakeView.class, args[0]);
    Assert.assertEquals(FakeProxy.class, args[1]);

    args = ReflectionUtils.getTypeArguments(PresenterImpl.class,
        D_AbstractPresenter.class);
    Assert.assertEquals(2, args.length);
    Assert.assertEquals(FakeView.class, args[0]);
    Assert.assertEquals(D_AbstractPresenter.class.getTypeParameters()[0],
        args[1]);
  }

  @Test
  public void testIndirectE() {
    Type[] args = ReflectionUtils.getTypeArguments(PresenterImpl.class,
        E_IndirectPresenter.class);
    Assert.assertEquals(2, args.length);
    Assert.assertEquals(FakeView.class, args[0]);
    Assert.assertEquals(FakeProxy.class, args[1]);

    args = ReflectionUtils.getTypeArguments(PresenterImpl.class,
        E_AbstractPresenter.class);
    Assert.assertEquals(2, args.length);
    Assert.assertEquals(E_AbstractPresenter.class.getTypeParameters()[0],
        args[0]);
    Assert.assertEquals(E_AbstractPresenter.class.getTypeParameters()[1],
        args[1]);
  }
}
