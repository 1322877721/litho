/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.facebook.litho.testing.ComponentTestHelper;
import com.facebook.litho.testing.TestComponent;
import com.facebook.litho.testing.TestDrawableComponent;
import com.facebook.litho.testing.TestViewComponent;
import com.facebook.litho.testing.shadows.LayoutDirectionViewGroupShadow;
import com.facebook.litho.testing.shadows.LayoutDirectionViewShadow;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.testing.util.InlineLayoutSpec;
import com.facebook.yoga.YogaAlign;
import com.facebook.yoga.YogaDirection;
import com.facebook.yoga.YogaEdge;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.junit.Assert.assertEquals;

@Config(
    manifest = Config.NONE,
    sdk = LOLLIPOP,
    shadows = {
        LayoutDirectionViewShadow.class,
        LayoutDirectionViewGroupShadow.class})
@RunWith(ComponentsTestRunner.class)
public class LayoutDirectionTest {
  private ComponentContext mContext;

  @Before
  public void setup() {
    mContext = new ComponentContext(RuntimeEnvironment.application);
  }

  /**
   * Test that view mount items are laid out in the correct positions for LTR and RTL layout
   * directions.
   */
  @Test
  public void testViewChildrenLayoutDirection() {
    final TestComponent child1 =
        TestViewComponent.create(mContext, true, true, true, false)
            .build();
    final TestComponent child2 =
        TestViewComponent.create(mContext, true, true, true, false)
            .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
              @Override
              protected ComponentLayout onCreateLayout(ComponentContext c) {
                return Row.create(c)
                    .layoutDirection(YogaDirection.LTR)
                    .child(
                        Layout.create(c, child1)
                            .widthPx(10)
                            .heightPx(10))
                    .child(
                        Layout.create(c, child2)
                            .widthPx(10)
                            .heightPx(10))
                    .build();
              }
            },
        20,
        10);

    View view1 = lithoView.getChildAt(0);
    View view2 = lithoView.getChildAt(1);

    assertEquals(
        new Rect(0, 0, 10, 10),
        new Rect(
            view1.getLeft(),
            view1.getTop(),
            view1.getRight(),
            view1.getBottom()));

    assertEquals(
        new Rect(10, 0, 20, 10),
        new Rect(
            view2.getLeft(),
            view2.getTop(),
            view2.getRight(),
            view2.getBottom()));

    ComponentTestHelper.mountComponent(
        mContext,
        lithoView,
        new InlineLayoutSpec() {
              @Override
              protected ComponentLayout onCreateLayout(ComponentContext c) {
                return Row.create(c)
                    .layoutDirection(YogaDirection.RTL)
                    .child(
                        Layout.create(c, child1)
                            .widthPx(10)
                            .heightPx(10))
                    .child(
                        Layout.create(c, child2)
                            .widthPx(10)
                            .heightPx(10))
                    .build();
              }
            },
        20,
        10);

    view1 = lithoView.getChildAt(0);
    view2 = lithoView.getChildAt(1);

    assertEquals(
        new Rect(10, 0, 20, 10),
        new Rect(
            view1.getLeft(),
            view1.getTop(),
            view1.getRight(),
            view1.getBottom()));

    assertEquals(
        new Rect(0, 0, 10, 10),
        new Rect(
            view2.getLeft(),
            view2.getTop(),
            view2.getRight(),
            view2.getBottom()));
  }

  /**
   * Test that drawable mount items are laid out in the correct positions for LTR and RTL layout
   * directions.
   */
  @Test
  public void testDrawableChildrenLayoutDirection() {
    final TestComponent child1 = TestDrawableComponent.create(mContext)
        .build();
    final TestComponent child2 = TestDrawableComponent.create(mContext)
        .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
              @Override
              protected ComponentLayout onCreateLayout(ComponentContext c) {
                return Row.create(c)
                    .layoutDirection(YogaDirection.LTR)
                    .child(
                        Layout.create(c, child1)
                            .widthPx(10)
                            .heightPx(10))
                    .child(
                        Layout.create(c, child2)
                            .widthPx(10)
                            .heightPx(10))
                    .build();
              }
            },
        20,
        10);

    Drawable drawable1 = lithoView.getDrawables().get(0);
    Drawable drawable2 = lithoView.getDrawables().get(1);

    assertEquals(new Rect(0, 0, 10, 10), drawable1.getBounds());
    assertEquals(new Rect(10, 0, 20, 10), drawable2.getBounds());

    ComponentTestHelper.mountComponent(
        mContext,
        lithoView,
        new InlineLayoutSpec() {
              @Override
              protected ComponentLayout onCreateLayout(ComponentContext c) {
                return Row.create(c)
                    .layoutDirection(YogaDirection.RTL)
                    .child(
                        Layout.create(c, child1)
                            .widthPx(10)
                            .heightPx(10))
                    .child(
                        Layout.create(c, child2)
                            .widthPx(10)
                            .heightPx(10))
                    .build();
              }
            },
        20,
        10);

    drawable1 = lithoView.getDrawables().get(0);
    drawable2 = lithoView.getDrawables().get(1);

    assertEquals(new Rect(10, 0, 20, 10), drawable1.getBounds());
    assertEquals(new Rect(0, 0, 10, 10), drawable2.getBounds());
  }

  /**
   * Test that layout direction is propagated properly throughout a component hierarchy. This is the
   * default behaviour of layout direction.
   */
  @Test
  public void testInheritLayoutDirection() {
    final TestComponent child1 = TestDrawableComponent.create(mContext)
        .build();
    final TestComponent child2 = TestDrawableComponent.create(mContext)
        .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
              @Override
              protected ComponentLayout onCreateLayout(ComponentContext c) {
                return Row.create(c)
                    .layoutDirection(YogaDirection.RTL)
                    .child(
                        Row.create(c)
                            .wrapInView()
                            .child(
                                Layout.create(c, child1)
                                    .widthPx(10)
                                    .heightPx(10))
                            .child(
                                Layout.create(c, child2)
                                    .widthPx(10)
                                    .heightPx(10)))
                    .build();
              }
            },
        20,
        10);

    final ComponentHost host = (ComponentHost) lithoView.getChildAt(0);
    final Drawable drawable1 = host.getDrawables().get(0);
    final Drawable drawable2 = host.getDrawables().get(1);

    assertEquals(new Rect(10, 0, 20, 10), drawable1.getBounds());
    assertEquals(new Rect(0, 0, 10, 10), drawable2.getBounds());
  }

  /**
   * Test that layout direction is correctly set on child components when it differs from the layout
   * direction of it's parent.
   */
  @Test
  public void testNestedComponentWithDifferentLayoutDirection() {
    final TestComponent child1 = TestDrawableComponent.create(mContext)
        .build();
    final TestComponent child2 = TestDrawableComponent.create(mContext)
        .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
              @Override
              protected ComponentLayout onCreateLayout(ComponentContext c) {
                return Row.create(c)
                    .layoutDirection(YogaDirection.RTL)
                    .child(
                        Row.create(c)
                            .layoutDirection(YogaDirection.LTR)
                            .wrapInView()
                            .child(
                                Layout.create(c, child1)
                                    .widthPx(10)
                                    .heightPx(10))
                            .child(
                                Layout.create(c, child2)
                                    .widthPx(10)
                                    .heightPx(10)))
                    .build();
              }
            },
        20,
        10);

    final ComponentHost host = (ComponentHost) lithoView.getChildAt(0);
    final Drawable drawable1 = host.getDrawables().get(0);
    final Drawable drawable2 = host.getDrawables().get(1);

    assertEquals(new Rect(0, 0, 10, 10), drawable1.getBounds());
    assertEquals(new Rect(10, 0, 20, 10), drawable2.getBounds());
  }

  /**
   * Test that margins on START and END are correctly applied to the correct side of the component
   * depending upon the applied layout direction.
   */
  @Test
  public void testMargin() {
    final TestComponent child = TestDrawableComponent.create(mContext)
        .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
              @Override
              protected ComponentLayout onCreateLayout(ComponentContext c) {
                return Column.create(c)
                    .layoutDirection(YogaDirection.LTR)
                    .child(
                        Layout.create(c, child)
                            .widthPx(10)
                            .heightPx(10)
                            .marginPx(YogaEdge.START, 10)
                            .marginPx(YogaEdge.END, 20))
                    .build();
              }
            },
        40,
        10);

    Drawable drawable = lithoView.getDrawables().get(0);
    assertEquals(new Rect(10, 0, 20, 10), drawable.getBounds());

    ComponentTestHelper.mountComponent(
        mContext,
        lithoView,
        new InlineLayoutSpec() {
              @Override
              protected ComponentLayout onCreateLayout(ComponentContext c) {
                return Column.create(c)
                    .layoutDirection(YogaDirection.RTL)
                    .child(
                        Layout.create(c, child)
                            .widthPx(10)
                            .heightPx(10)
                            .marginPx(YogaEdge.START, 10)
                            .marginPx(YogaEdge.END, 20))
                    .build();
              }
            },
        40,
        10);

    drawable = lithoView.getDrawables().get(0);
    assertEquals(new Rect(20, 0, 30, 10), drawable.getBounds());
  }

  /**
   * Test that paddings on START and END are correctly applied to the correct side of the component
   * depending upon the applied layout direction.
   */
  @Test
  public void testPadding() {
    final TestComponent child = TestDrawableComponent.create(mContext)
        .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
              @Override
              protected ComponentLayout onCreateLayout(ComponentContext c) {
                return Column.create(c)
                    .layoutDirection(YogaDirection.LTR)
                    .paddingPx(YogaEdge.START, 10)
                    .paddingPx(YogaEdge.END, 20)
                    .child(
                        Layout.create(c, child)
                            .widthPx(10)
                            .heightPx(10))
                    .build();
              }
            },
        40,
        10);

    Drawable drawable = lithoView.getDrawables().get(0);
    assertEquals(new Rect(10, 0, 20, 10), drawable.getBounds());

    ComponentTestHelper.mountComponent(
        mContext,
        lithoView,
        new InlineLayoutSpec() {
              @Override
              protected ComponentLayout onCreateLayout(ComponentContext c) {
                return Column.create(c)
                    .layoutDirection(YogaDirection.RTL)
                    .paddingPx(YogaEdge.START, 10)
                    .paddingPx(YogaEdge.END, 20)
                    .child(
                        Layout.create(c, child)
                            .widthPx(10)
                            .heightPx(10))
                    .build();
              }
            },
        40,
        10);

    drawable = lithoView.getDrawables().get(0);
    assertEquals(new Rect(20, 0, 30, 10), drawable.getBounds());
  }

  /**
   * Tests to make sure the layout direction set on the component tree is correctly propagated to
   * mounted views.
   */
  @Test
  public void testLayoutDirectionPropagation() {
    final TestComponent child = TestViewComponent.create(mContext)
        .build();

    LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .layoutDirection(YogaDirection.RTL)
                .child(child)
                .build();
          }
        });

    final View childView = lithoView.getChildAt(0);
    assertEquals(View.LAYOUT_DIRECTION_RTL, childView.getLayoutDirection());
  }
}
