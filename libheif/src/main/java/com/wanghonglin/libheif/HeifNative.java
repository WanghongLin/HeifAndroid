/*
 * Copyright 2019 wanghonglin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanghonglin.libheif;

/**
 * Created by wanghonglin on 2018/8/7 3:31 PM.
 */
public class HeifNative {

    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("heif");
        System.loadLibrary("heif_jni");
    }

    /**
     * Encode rgba bytes to HEIF
     * @param bytes rgba bytes
     * @param width width
     * @param height height
     * @param outputPath output path
     * @return 0 if success
     */
    public static native int encodeBitmap(byte[] bytes, int width, int height, String outputPath);

    /**
     * Encode YUV bytes to HEIF
     * @param bytes yuv bytes
     * @param width desired width
     * @param height desired height
     * @param outputPath output path
     * @return 0 if success
     */
    public static native int encodeYUV(byte[] bytes, int width, int height, String outputPath);

    /**
     * Decode HEIF to rgba bytes
     * @param outSize output size
     * @param srcPath source path to decode
     * @return rgba byte, convenient to create a {@link android.graphics.Bitmap}
     */
    public static native byte[] decodeHeif2RGBA(HeifSize outSize, String srcPath);
}
