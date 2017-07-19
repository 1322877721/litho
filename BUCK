# Copyright (c) 2017-present, Facebook, Inc.
# All rights reserved.
#
# This source code is licensed under the BSD-style license found in the
# LICENSE file in the root directory of this source tree. An additional grant
# of patent rights can be found in the PATENTS file in the same directory.

include_defs("//COMPONENTS_DEFS")

litho_android_library(
    name = "components",
    exported_deps = [
        COMPONENTS_STETHO_JAVA_TARGET,
        COMPONENTS_JAVA_TARGET,
        COMPONENTS_ANDROIDSUPPORT_TARGET,
        COMPONENTS_YOGA_TARGET,
    ],
    visibility = [
        "PUBLIC",
    ],
    deps = [
        ":build_config",
    ],
)

android_aar(
    name = "release-litho-core",
    include_build_config_class = True,
    manifest_skeleton = "litho-core/src/main/AndroidManifest.xml",
    visibility = [
        "PUBLIC",
    ],
    deps = [
        COMPONENTS_JAVA_TARGET,
        COMPONENTS_CONFIG_TARGET,
        ":build_config",
        ":res",
    ],
)

android_resource(
    name = "res",
    package = "com.facebook.litho",
    res = "litho-core/src/main/res",
    visibility = [
        "PUBLIC",
    ],
    deps = [
    ],
)

android_build_config(
    name = "build_config",
    package = "com.facebook.litho",
    values = [
        "boolean IS_INTERNAL_BUILD = true",
    ],
    visibility = [
        COMPONENTS_VISIBILITY,
    ],
)
