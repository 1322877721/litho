/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.testing.treeprop;

import android.graphics.drawable.Drawable;
import com.facebook.litho.ComponentContext;
import com.facebook.litho.annotations.MountSpec;
import com.facebook.litho.annotations.OnCreateMountContent;
import com.facebook.litho.annotations.OnPrepare;
import com.facebook.litho.annotations.Prop;
import com.facebook.litho.annotations.TreeProp;

/**
 * Used in TreePropTest.
 */
@MountSpec
public class TreePropTestMountSpec {

  @OnPrepare
  static void onPrepare(
      ComponentContext c,
      @TreeProp TreePropNumberType propA,
      @Prop TreePropTestResult resultPropA) {
    resultPropA.mProp = propA;
  }

  @OnCreateMountContent
  static Drawable onCreateMountContent(ComponentContext c) {
    return null;
  }
}
