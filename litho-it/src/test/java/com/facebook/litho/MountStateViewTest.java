/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.facebook.litho.testing.ComponentTestHelper;
import com.facebook.litho.testing.TestViewComponent;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.testing.util.InlineLayoutSpec;
import com.facebook.yoga.YogaAlign;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import static com.facebook.yoga.YogaEdge.BOTTOM;
import static com.facebook.yoga.YogaEdge.LEFT;
import static com.facebook.yoga.YogaEdge.RIGHT;
import static com.facebook.yoga.YogaEdge.TOP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ComponentsTestRunner.class)
public class MountStateViewTest {

  private ComponentContext mContext;

  @Before
  public void setup() {
    mContext = new ComponentContext(RuntimeEnvironment.application);
  }

  @Test
  public void testViewPaddingAndBackground() {
    final int color = 0xFFFF0000;
    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(
                    TestViewComponent.create(c)
                        .withLayout()
                        .paddingPx(LEFT, 5)
                        .paddingPx(TOP, 6)
                        .paddingPx(RIGHT, 7)
                        .paddingPx(BOTTOM, 8)
                        .backgroundColor(color))
                .build();
          }
        });

    final View child = lithoView.getChildAt(0);
    final Drawable background = child.getBackground();

    assertEquals(5, child.getPaddingLeft());
    assertEquals(6, child.getPaddingTop());
    assertEquals(7, child.getPaddingRight());
    assertEquals(8, child.getPaddingBottom());
    assertTrue(background instanceof ColorDrawable);
    assertEquals(color, ((ColorDrawable) background).getColor());
  }
}
