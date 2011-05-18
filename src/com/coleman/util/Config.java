/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.coleman.util;

/**
 * Build configuration. The constants in this class vary depending on release
 * vs. debug build. {@more}
 */
public final class Config {
    /**
     * If this is a debug build, this field will be true.
     */
    public static final boolean DEBUG = true;
    /**
     * App read file encoding.
     */
    public static final String ENCODE = "GBK";

}
