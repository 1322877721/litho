/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import java.util.HashSet;
import java.util.ArrayList;

import android.support.v4.util.SimpleArrayMap;

import com.facebook.litho.animation.AnimationBinding;

/**
 * TransitionContext is unique per LayoutState and contains all the transitions defined
 * in a component tree.
 */
class TransitionContext {

  // User defined transitions
  private final SimpleArrayMap<String, TransitionKeySet> mKeyToTransitionKeySets =
      new SimpleArrayMap<>();
  private final ArrayList<AnimationBinding> mTransitionAnimationBindings = new ArrayList<>();

  // Transition keys of given layout tree
  private final HashSet<String> mTransitionKeys = new HashSet<>(8);

  void add(Transition transition) {
    if (transition instanceof TransitionSet) {
      add((TransitionSet) transition);
    } else {
      final String key = transition.getKey();

      if (!mKeyToTransitionKeySets.containsKey(key)) {
        mKeyToTransitionKeySets.put(key, new TransitionKeySet(key));
      }

      mKeyToTransitionKeySets.get(key).add(transition);
    }
  }

  void add(TransitionSet transitions) {
    for (int i = 0, size = transitions.size(); i < size; i++) {
      add(transitions.get(i));
    }
  }

  void addTransitionAnimationBinding(AnimationBinding binding) {
    mTransitionAnimationBindings.add(binding);
  }

  SimpleArrayMap<String, TransitionKeySet> getTransitionKeySets() {
    return mKeyToTransitionKeySets;
  }

  ArrayList<AnimationBinding> getTransitionAnimationBindings() {
    return mTransitionAnimationBindings;
  }

  void reset() {
    mTransitionAnimationBindings.clear();
    mKeyToTransitionKeySets.clear();
    mTransitionKeys.clear();
  }

  void addTransitionKey(String transitionKey) {
    mTransitionKeys.add(transitionKey);
  }

  boolean hasTransitionKey(String transitionKey) {
    return mTransitionKeys.contains(transitionKey);
  }

  /**
   * @return Whether item with the given {@param transitionKey} is being removed from layout tree.
   */
  boolean isDisappearingKey(String transitionKey) {
    if (transitionKey == null) {
      return false;
    }
    final TransitionKeySet transitionKeySet = mKeyToTransitionKeySets.get(transitionKey);
    if (transitionKeySet == null || !transitionKeySet.hasDisappearingTransitions()) {
      return false;
    }
    return !mTransitionKeys.contains(transitionKey);
  }
}
