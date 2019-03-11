/*
 * Copyright 2019-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.litho;

import android.util.Log;

public class InternalNodeUtils {

  /**
   * Check that the root of the nested tree we are going to use, has valid layout directions with
   * its main tree holder node.
   */
  static boolean hasValidLayoutDirectionInNestedTree(InternalNode holder, InternalNode nestedTree) {
    return nestedTree.isLayoutDirectionSet()
        || (nestedTree.getResolvedLayoutDirection() == holder.getResolvedLayoutDirection());
  }

  /** Crash if the given node has context specific style set. */
  static void assertContextSpecificStyleNotSet(InternalNode node) {
    if (node instanceof DefaultInternalNode) {
      DefaultInternalNode.assertContextSpecificStyleNotSet((DefaultInternalNode) node);
    } else {
      Log.e("Litho", node.getClass().getSimpleName() + " does not implement style assertions");
    }
  }
}
