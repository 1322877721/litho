/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import android.graphics.Color;

import com.facebook.litho.testing.ComponentTestHelper;
import com.facebook.litho.testing.TestComponent;
import com.facebook.litho.testing.TestDrawableComponent;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.testing.util.InlineLayoutSpec;
import com.facebook.litho.widget.SolidColor;
import com.facebook.litho.widget.Text;
import com.facebook.yoga.YogaAlign;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.facebook.litho.FrameworkLogEvents.EVENT_PREPARE_MOUNT;
import static com.facebook.litho.FrameworkLogEvents.PARAM_MOVED_COUNT;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(ComponentsTestRunner.class)
public class MountStateRemountInPlaceTest {
  private ComponentContext mContext;
  private ComponentsLogger mComponentsLogger;

  @Before
  public void setup() {
    mComponentsLogger = spy(new TestComponentsLogger());
    when(mComponentsLogger.newEvent(any(int.class))).thenCallRealMethod();
    when(mComponentsLogger.newPerformanceEvent(any(int.class))).thenCallRealMethod();

    mContext = new ComponentContext(RuntimeEnvironment.application, "tag", mComponentsLogger);
  }

  @Test
  public void testMountUnmountWithShouldUpdate() {
    final TestComponent firstComponent =
        TestDrawableComponent.create(mContext)
            .unique()
            .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(firstComponent)
                .build();
          }
        });

    assertTrue(firstComponent.wasOnMountCalled());
    assertTrue(firstComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());

    final TestComponent secondComponent =
        TestDrawableComponent.create(mContext)
            .unique()
            .build();

    lithoView.getComponentTree().setRoot(new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .child(secondComponent)
            .build();
      }
    });

    assertTrue(secondComponent.wasOnMountCalled());
    assertTrue(secondComponent.wasOnBindCalled());
    assertTrue(firstComponent.wasOnUnmountCalled());
  }

  @Test
  public void testMountUnmountWithNoShouldUpdate() {
    final TestComponent firstComponent =
        TestDrawableComponent.create(mContext)
            .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(firstComponent)
                .build();
          }
        });

    assertTrue(firstComponent.wasOnMountCalled());
    assertTrue(firstComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());

    final TestComponent secondComponent =
        TestDrawableComponent.create(mContext)
            .build();

    lithoView.getComponentTree().setRoot(new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .child(secondComponent)
            .build();
      }
    });

    assertFalse(secondComponent.wasOnMountCalled());
    assertTrue(secondComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());
  }

  @Test
  public void testMountUnmountWithNoShouldUpdateAndDifferentSize() {
    final TestComponent firstComponent =
        TestDrawableComponent
            .create(
                mContext,
                0,
                0,
                true,
                true,
                true,
                false,
                false,
                true /*isMountSizeDependent*/)
            .measuredHeight(10)
            .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(firstComponent)
                .build();
          }
        });

    assertTrue(firstComponent.wasOnMountCalled());
    assertTrue(firstComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());

    final TestComponent secondComponent =
        TestDrawableComponent
            .create(
                mContext,
                0,
                0,
                true,
                true,
                true,
                false,
                false,
                true /*isMountSizeDependent*/)
            .measuredHeight(11)
            .build();

    lithoView.getComponentTree().setRoot(new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .child(secondComponent)
            .build();
      }
    });

    assertTrue(secondComponent.wasOnMountCalled());
    assertTrue(secondComponent.wasOnBindCalled());
    assertTrue(firstComponent.wasOnUnmountCalled());
  }

  @Test
  public void testMountUnmountWithNoShouldUpdateAndSameSize() {
    final TestComponent firstComponent =
        TestDrawableComponent
            .create(
                mContext,
                0,
                0,
                true,
                true,
                true,
                false,
                false,
                true /*isMountSizeDependent*/)
            .measuredHeight(10)
            .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(firstComponent)
                .build();
          }
        });

    assertTrue(firstComponent.wasOnMountCalled());
    assertTrue(firstComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());

    final TestComponent secondComponent =
        TestDrawableComponent
            .create(
                mContext,
                0,
                0,
                true,
                true,
                true,
                false,
                false,
                true /*isMountSizeDependent*/)
            .measuredHeight(10)
            .build();

    lithoView.getComponentTree().setRoot(new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .child(secondComponent)
            .build();
      }
    });

    assertFalse(secondComponent.wasOnMountCalled());
    assertTrue(secondComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());
  }

  @Test
  public void testMountUnmountWithNoShouldUpdateAndDifferentMeasures() {
    final TestComponent firstComponent =
        TestDrawableComponent.create(mContext)
            .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        new LithoView(mContext),
        ComponentTree.create(mContext, new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(firstComponent)
                .build();
          }
        })
            .incrementalMount(false)
            .layoutDiffing(false)
            .build(),
        makeMeasureSpec(100, AT_MOST),
        makeMeasureSpec(100, AT_MOST));

    assertTrue(firstComponent.wasOnMountCalled());
    assertTrue(firstComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());

    final TestComponent secondComponent =
        TestDrawableComponent.create(mContext)
            .build();

    lithoView.getComponentTree().setRoot(new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .child(secondComponent)
            .widthPx(10)
            .heightPx(10)
            .build();
      }
    });

    assertTrue(lithoView.isLayoutRequested());
    assertFalse(secondComponent.wasOnMountCalled());
    assertFalse(secondComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());
  }

  @Test
  public void testMountUnmountWithNoShouldUpdateAndSameMeasures() {
    final TestComponent firstComponent =
        TestDrawableComponent.create(mContext, 0, 0, true, true, true, false, false, true)
            .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        new LithoView(mContext),
        ComponentTree.create(mContext, new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(firstComponent)
                .build();
          }
        })
            .incrementalMount(false)
            .layoutDiffing(false)
            .build(),
        makeMeasureSpec(100, EXACTLY),
        makeMeasureSpec(100, EXACTLY));

    assertTrue(firstComponent.wasOnMountCalled());
    assertTrue(firstComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());

    final TestComponent secondComponent =
        TestDrawableComponent.create(mContext)
            .build();

    lithoView.getComponentTree().setRoot(new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .child(secondComponent)
            .widthPx(10)
            .heightPx(10)
            .build();
      }
    });

    assertFalse(lithoView.isLayoutRequested());
    assertTrue(secondComponent.wasOnMountCalled());
    assertTrue(secondComponent.wasOnBindCalled());
    assertTrue(firstComponent.wasOnUnmountCalled());
  }

  @Test
  public void testRebindWithNoShouldUpdateAndSameMeasures() {
    final TestComponent firstComponent =
        TestDrawableComponent.create(mContext)
            .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        new LithoView(mContext),
        ComponentTree.create(mContext, new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(firstComponent)
                .build();
          }
        })
            .incrementalMount(false)
            .layoutDiffing(false)
            .build(),
        makeMeasureSpec(100, EXACTLY),
        makeMeasureSpec(100, EXACTLY));

    assertTrue(firstComponent.wasOnMountCalled());
    assertTrue(firstComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());

    final TestComponent secondComponent =
        TestDrawableComponent.create(mContext)
            .build();

    lithoView.getComponentTree().setRoot(new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .child(secondComponent)
            .widthPx(10)
            .heightPx(10)
            .build();
      }
    });

    assertFalse(lithoView.isLayoutRequested());
    assertFalse(secondComponent.wasOnMountCalled());
    assertTrue(secondComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());
  }

  @Test
  public void testMountUnmountWithSkipShouldUpdate() {
    final TestComponent firstComponent =
        TestDrawableComponent.create(mContext)
            .color(Color.BLACK)
            .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(firstComponent)
                .build();
          }
        });

    assertTrue(firstComponent.wasOnMountCalled());
    assertTrue(firstComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());

    final TestComponent secondComponent =
        TestDrawableComponent.create(mContext)
            .color(Color.BLACK)
            .build();

    lithoView.getComponentTree().setRoot(new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .child(secondComponent)
            .build();
      }
    });

    assertFalse(secondComponent.wasOnMountCalled());
    assertTrue(secondComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());
  }

  @Test
  public void testMountUnmountWithSkipShouldUpdateAndRemount() {
    final TestComponent firstComponent =
        TestDrawableComponent.create(mContext)
            .color(Color.BLACK)
            .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(firstComponent)
                .build();
          }
        });

    assertTrue(firstComponent.wasOnMountCalled());
    assertTrue(firstComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());

    final TestComponent secondComponent =
        TestDrawableComponent.create(mContext)
            .color(Color.WHITE)
            .build();

    lithoView.getComponentTree().setRoot(new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .child(secondComponent)
            .build();
      }
    });

    assertTrue(secondComponent.wasOnMountCalled());
    assertTrue(secondComponent.wasOnBindCalled());
    assertTrue(firstComponent.wasOnUnmountCalled());
  }

  @Test
  public void testMountUnmountDoesNotSkipShouldUpdateAndRemount() {
    final TestComponent firstComponent =
        TestDrawableComponent.create(mContext)
            .unique()
            .build();

    final LithoView firstLithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(firstComponent)
                .build();
          }
        });

    assertTrue(firstComponent.wasOnMountCalled());
    assertTrue(firstComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());

    final TestComponent secondComponent =
        TestDrawableComponent.create(mContext)
            .unique()
            .build();

    final ComponentTree secondTree = ComponentTree.create(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(secondComponent)
                .build();
          }
        })
        .incrementalMount(false)
        .layoutDiffing(false)
        .build();
    secondTree.setSizeSpec(100, 100);

    final TestComponent thirdComponent =
        TestDrawableComponent.create(mContext)
            .build();

    secondTree.setRoot(new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .child(thirdComponent)
            .build();
      }
    });

    ComponentTestHelper.mountComponent(firstLithoView, secondTree);

    assertTrue(thirdComponent.wasOnMountCalled());
    assertTrue(thirdComponent.wasOnBindCalled());
    assertTrue(firstComponent.wasOnUnmountCalled());
  }

  @Test
  public void testSkipShouldUpdateAndRemountForUnsupportedComponent() {
    final TestComponent firstComponent =
        TestDrawableComponent.create(
            mContext,
            false,
            true,
            true,
            false,
            false)
            .build();

    final LithoView firstLithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(firstComponent)
                .build();
          }
        });

    assertTrue(firstComponent.wasOnMountCalled());
    assertTrue(firstComponent.wasOnBindCalled());
    assertFalse(firstComponent.wasOnUnmountCalled());

    final TestComponent secondComponent =
        TestDrawableComponent.create(
            mContext,
            false,
            true,
            true,
            false,
            false)
            .build();

    final ComponentTree secondTree = ComponentTree.create(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(secondComponent)
                .build();
          }
        })
        .incrementalMount(false)
        .layoutDiffing(false)
        .build();
    secondTree.setSizeSpec(100, 100);

    ComponentTestHelper.mountComponent(firstLithoView, secondTree);

    assertTrue(secondComponent.wasOnMountCalled());
    assertTrue(secondComponent.wasOnBindCalled());
    assertTrue(firstComponent.wasOnUnmountCalled());
  }

  @Test
  public void testRemountSameSubTreeWithDifferentParentHost() {
    final TestComponent firstComponent =
        TestDrawableComponent.create(
            mContext,
            false,
            true,
            true,
            false,
            false)
            .build();

    InlineLayoutSpec firstLayout = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .child(
                Column.create(c)
                    .clickHandler(c.newEventHandler(3))
                    .child(
                        Text.create(c).text("test")))
            .child(
                Column.create(c)
                    .clickHandler(c.newEventHandler(2))
                    .child(
                        Text.create(c).text("test2"))
                    .child(
                        Column.create(c)
                            .clickHandler(c.newEventHandler(1))
                            .child(
                                firstComponent)
                            .child(
                                SolidColor.create(c).color(Color.GREEN))))
            .build();
      }
    };

    final InlineLayoutSpec secondLayout = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .child(
                Column.create(c)
                    .clickHandler(c.newEventHandler(3))
                    .child(
                        Text.create(c).text("test"))
                    .child(
                        Column.create(c)
                            .clickHandler(c.newEventHandler(1))
                            .child(
                                firstComponent)
                            .child(
                                SolidColor.create(c).color(Color.GREEN))))
            .child(
                Column.create(c)
                    .clickHandler(c.newEventHandler(2))
                    .child(
                        Text.create(c).text("test2")))
            .build();
      }
    };

    ComponentTree tree = ComponentTree.create(mContext, firstLayout)
        .incrementalMount(false)
        .layoutDiffing(false)
        .build();
    LithoView cv = new LithoView(mContext);
    ComponentTestHelper.mountComponent(cv, tree);
    tree.setRoot(secondLayout);

    final LogEvent event = mComponentsLogger.newPerformanceEvent(EVENT_PREPARE_MOUNT);
    event.addParam(PARAM_MOVED_COUNT, "2");
    verify(mComponentsLogger).log(eq(event));
  }
}
