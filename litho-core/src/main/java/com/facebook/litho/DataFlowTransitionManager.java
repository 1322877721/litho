/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import java.util.ArrayList;
import java.util.WeakHashMap;

import android.support.v4.util.Pools;
import android.support.v4.util.SimpleArrayMap;

import com.facebook.litho.animation.AnimatedPropertyNode;
import com.facebook.litho.animation.AnimationBinding;
import com.facebook.litho.animation.LazyValue;
import com.facebook.litho.animation.AnimatedProperty;
import com.facebook.litho.animation.Resolver;
import com.facebook.litho.animation.ComponentProperty;
import com.facebook.litho.internal.ArraySet;

/**
 * Implementation of transitions in Litho via a dataflow graph.
 */
public class DataFlowTransitionManager {

  private static Pools.SimplePool<TransitionDiff> sTransitionDiffPool = new Pools.SimplePool<>(20);

  private static class TransitionDiff {

    public final SimpleArrayMap<AnimatedProperty, Float> beforeValues = new SimpleArrayMap<>();
    public final SimpleArrayMap<AnimatedProperty, Float> afterValues = new SimpleArrayMap<>();
    public int changeType = TransitionManager.KeyStatus.DISAPPEARED;
    public Object mountItem;

    public void reset() {
      beforeValues.clear();
      afterValues.clear();
      changeType = TransitionManager.KeyStatus.DISAPPEARED;
      mountItem = null;
    }
  }

  private final ArrayList<AnimationBinding> mAnimationBindings = new ArrayList<>();
  private final SimpleArrayMap<String, TransitionDiff> mKeyToTransitionDiffs =
      new SimpleArrayMap<>();
  private final SimpleArrayMap<String, ArrayList<AnimatedProperty>> mKeyToAnimatingProperties =
      new SimpleArrayMap<>();
  private final WeakHashMap<Object, SimpleArrayMap<String, AnimatedPropertyNode>>
      mAnimatedPropertyNodes = new WeakHashMap<>();
  private final TransitionsResolver mResolver = new TransitionsResolver();

  void onNewTransitionContext(TransitionContext transitionContext) {
    mAnimationBindings.clear();
    mKeyToAnimatingProperties.clear();
    for (int i = 0, size = mKeyToTransitionDiffs.size(); i < size; i++) {
      releaseTransitionDiff(mKeyToTransitionDiffs.valueAt(i));
    }
    mKeyToTransitionDiffs.clear();

    mAnimationBindings.addAll(transitionContext.getTransitionAnimationBindings());
    recordAllTransitioningProperties();
  }

  void onPreMountItem(String transitionKey, Object mountItem) {
    final ArrayList<AnimatedProperty> animatingProperties =
        mKeyToAnimatingProperties.get(transitionKey);
    if (animatingProperties != null) {
      final TransitionDiff info = acquireTransitionDiff();
      for (int i = 0; i < animatingProperties.size(); i++) {
        final AnimatedProperty prop = animatingProperties.get(i);
        info.beforeValues.put(prop, prop.get(mountItem));

        // Unfortunately, we have no guarantee that this mountItem won't be re-used for another
        // different component during the coming mount, so we need to reset it before the actual
        // mount happens. The proper before-values will be set again before any animations start.
        prop.reset(mountItem);
      }
      info.mountItem = mountItem;
      mKeyToTransitionDiffs.put(transitionKey, info);
    }
  }

  void onPostMountItem(String transitionKey, Object mountItem) {
    final ArrayList<AnimatedProperty> animatingProperties =
        mKeyToAnimatingProperties.get(transitionKey);
    if (animatingProperties != null) {
      TransitionDiff info = mKeyToTransitionDiffs.get(transitionKey);
      if (info == null) {
        info = acquireTransitionDiff();
        info.changeType = TransitionManager.KeyStatus.APPEARED;
        info.mountItem = mountItem;
        mKeyToTransitionDiffs.put(transitionKey, info);
      } else {
        info.changeType = TransitionManager.KeyStatus.UNCHANGED;
      }

      // If these are different, this means this transition key will be rendered with a different
      // mount item (View or Drawable) than it was during the last mount, so we need to migrate
      // animation state from the old mount item to the new one.
      if (info.mountItem != mountItem) {
        migrateToNewMountItem(info.mountItem, mountItem);
        info.mountItem = mountItem;
      }

      for (int i = 0; i < animatingProperties.size(); i++) {
        final AnimatedProperty prop = animatingProperties.get(i);
        info.afterValues.put(prop, prop.get(mountItem));
      }
    }
  }

  void activateBindings() {
    restoreInitialStates();
    setDisappearToValues();
    for (int i = 0, size = mAnimationBindings.size(); i < size; i++) {
      final AnimationBinding binding = mAnimationBindings.get(i);
      binding.start(mResolver);
    }
  }

  private void restoreInitialStates() {
    for (int i = 0; i < mKeyToTransitionDiffs.size(); i++) {
      final TransitionDiff diff = mKeyToTransitionDiffs.valueAt(i);
      if (diff.changeType == TransitionManager.KeyStatus.UNCHANGED) {
        for (int j = 0; j < diff.beforeValues.size(); j++) {
          final AnimatedProperty property = diff.beforeValues.keyAt(j);
          property.set(diff.mountItem, diff.beforeValues.valueAt(j));
        }
      }
    }
    setAppearFromValues();
  }

  private void setAppearFromValues() {
    SimpleArrayMap<ComponentProperty, LazyValue> appearFromValues = new SimpleArrayMap<>();
    for (int i = 0, size = mAnimationBindings.size(); i < size; i++) {
      final AnimationBinding binding = mAnimationBindings.get(i);
      binding.collectAppearFromValues(appearFromValues);
    }

    for (int i = 0, size = appearFromValues.size(); i < size; i++) {
      final ComponentProperty property = appearFromValues.keyAt(i);
      final LazyValue lazyValue = appearFromValues.valueAt(i);
      final TransitionDiff diff = mKeyToTransitionDiffs.get(property.getTransitionKey());
      final float value = lazyValue.resolve(mResolver, property);
      property.getProperty().set(diff.mountItem, value);
    }
  }

  private void setDisappearToValues() {
    SimpleArrayMap<ComponentProperty, LazyValue> disappearToValues = new SimpleArrayMap<>();
    for (int i = 0, size = mAnimationBindings.size(); i < size; i++) {
      final AnimationBinding binding = mAnimationBindings.get(i);
      binding.collectDisappearToValues(disappearToValues);
    }

    for (int i = 0, size = disappearToValues.size(); i < size; i++) {
      final ComponentProperty property = disappearToValues.keyAt(i);
      final LazyValue lazyValue = disappearToValues.valueAt(i);
      final TransitionDiff diff = mKeyToTransitionDiffs.get(property.getTransitionKey());
      if (diff.changeType != TransitionManager.KeyStatus.DISAPPEARED) {
        throw new RuntimeException("Wrong transition type for disappear: " + diff.changeType);
      }
      final float value = lazyValue.resolve(mResolver, property);
      diff.afterValues.put(property.getProperty(), value);
    }
  }

  /**
   * This method should record the transition key and animated properties of all animating mount
   * items so that we know whether to record them in onPre/PostMountItem
   */
  private void recordAllTransitioningProperties() {
    ArraySet<ComponentProperty> transitioningProperties = new ArraySet<>();
    for (int i = 0, size = mAnimationBindings.size(); i < size; i++) {
      final AnimationBinding binding = mAnimationBindings.get(i);
      binding.collectTransitioningProperties(transitioningProperties);
    }

    for (int i = 0, size = transitioningProperties.size(); i < size; i++) {
      final ComponentProperty property = transitioningProperties.valueAt(i);
      final String key = property.getTransitionKey();
      final AnimatedProperty animatedProperty = property.getProperty();
      ArrayList<AnimatedProperty> propertiesForKey = mKeyToAnimatingProperties.get(key);
      if (propertiesForKey == null) {
        propertiesForKey = new ArrayList<>();
        mKeyToAnimatingProperties.put(key, propertiesForKey);
      }
      if (!propertiesForKey.contains(property)) {
        propertiesForKey.add(animatedProperty);
      }
    }
  }

  private AnimatedPropertyNode getOrCreateAnimatedPropertyNode(
      Object mountItem,
      AnimatedProperty animatedProperty) {
    SimpleArrayMap<String, AnimatedPropertyNode> animatedProperties =
        mAnimatedPropertyNodes.get(mountItem);
    if (animatedProperties == null) {
      animatedProperties = new SimpleArrayMap<>();
      mAnimatedPropertyNodes.put(mountItem, animatedProperties);
    }
    AnimatedPropertyNode node = animatedProperties.get(animatedProperty.getName());
    if (node == null) {
      node = new AnimatedPropertyNode(mountItem, animatedProperty);
      animatedProperties.put(animatedProperty.getName(), node);
    }
    return node;
  }

  private void migrateToNewMountItem(Object oldMountItem, Object newMountItem) {
    SimpleArrayMap<String, AnimatedPropertyNode> oldItemNodes =
        mAnimatedPropertyNodes.remove(oldMountItem);
    if (oldItemNodes == null) {
      return;
    }
    for (int i = 0, size = oldItemNodes.size(); i < size; i++) {
      oldItemNodes.valueAt(i).setMountItem(newMountItem);
    }
    mAnimatedPropertyNodes.put(newMountItem, oldItemNodes);
  }

  private static TransitionDiff acquireTransitionDiff() {
    TransitionDiff diff = sTransitionDiffPool.acquire();
    if (diff == null) {
      diff = new TransitionDiff();
    }
    return diff;
  }

  private static void releaseTransitionDiff(TransitionDiff diff) {
    diff.reset();
    sTransitionDiffPool.release(diff);
  }

  private class TransitionsResolver implements Resolver {

    @Override
    public float getCurrentState(ComponentProperty property) {
      final TransitionDiff diff = mKeyToTransitionDiffs.get(property.getTransitionKey());
      return property.getProperty().get(diff.mountItem);
    }

    @Override
    public float getEndState(ComponentProperty property) {
      final TransitionDiff diff = mKeyToTransitionDiffs.get(property.getTransitionKey());
      return diff.afterValues.get(property.getProperty());
    }

    @Override
    public AnimatedPropertyNode getAnimatedPropertyNode(ComponentProperty property) {
      final TransitionDiff diff = mKeyToTransitionDiffs.get(property.getTransitionKey());
      return getOrCreateAnimatedPropertyNode(diff.mountItem, property.getProperty());
    }
  }
}
