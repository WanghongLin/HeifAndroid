HEIF for Android
======

A quick demo, to investigate the possibility and efficiency of HEIF soft encode/decode on Android platform.

Just a simple wrapper around of [libheif](https://github.com/strukturag/libheif) which use [libde256](https://github.com/strukturag/libde265) to handle decoding and [libx265](http://x265.org/) to handle the encoding.

Cross compiling tool is android-ndk-r17b standalone toolchain clang/clang++ and only `armeabi-v7a` is available for now. If you need more architectures support, [check here](https://github.com/WanghongLin/miscellaneous/blob/master/docs/libheif4Android.md) to see how to generate `libheif.so` step by step.

Remove all assembly implementation of x265 in order to avoid `TEXTREL` problem, might be faster if assembly for x265 is turned on, but I have no idea how to fix this `TEXTREL` problem.

The main and very simple JNI wrapper can be found at [heif_jni.cpp](libheif/src/main/cpp/heif_jni.cpp), accept input format rgba(can easily obtained from bitmap in Android) and yuv, produce output format is rgba which can be used easily to create a bitmap in Android.

View encoded output on macOS
------

The encoded heif can be pull from device and open on macOS

```sh
$ adb pull /mnt/sdcard/output.heic
$ open output.heic
```

License
------
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.