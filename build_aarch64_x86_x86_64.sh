# libx265
cd /tmp/x265/build
mkdir arm64-android && cd arm64-android
cmake -DCROSS_COMPILE_ARM=1 -DCMAKE_SYSTEM_NAME=Linux -DCMAKE_SYSTEM_PROCESSOR=armv8 \
    -DCMAKE_C_COMPILER=/tmp/ndkarm64/bin/aarch64-linux-android-clang \
    -DCMAKE_CXX_COMPILER=/tmp/ndkarm64/bin/aarch64-linux-android-clang++ \
    -DCMAKE_FIND_ROOT_PATH=/tmp/ndkarm64/sysroot -DENABLE_ASSEMBLY=OFF \
    -DENABLE_CLI=OFF -DENABLE_PIC=ON -DENABLE_SHARED=OFF \
    -DCMAKE_INSTALL_PREFIX=/tmp/out/x265 -DCMAKE_C_FLAGS="" \
    -G "Unix Makefiles" ../../source
make -j8 && make install

# libde265
export CC=/tmp/ndkarm64/bin/aarch64-linux-android-clang \
    CXX=/tmp/ndkarm64/bin/aarch64-linux-android-clang++ \
    CFLAGS="-fPIE" \
    LDFLAGS="-pie -fPIE" \
    PATH=$PATH:/tmp/ndkarm64/bin

./configure --prefix=/tmp/out/libde265 \
    --enable-shared=no \
    --host=aarch64-linux-android \
    --disable-arm --disable-sse

# libpng
export CC=/tmp/ndkarm64/bin/aarch64-linux-android-clang \
    CXX=/tmp/ndkarm64/bin/aarch64-linux-android-clang++ \
    CFLAGS="-fPIE" \
    LDFLAGS="-pie -fPIE" \
    PATH=$PATH:/tmp/ndkarm64/bin

./configure --prefix=/tmp/out/libpng/ --host=aarch64-linux-android \
    --enable-shared=no --enable-arm-neon
make -j8 && make install

# libheif
export CC=/tmp/ndkarm64/bin/aarch64-linux-android-clang \
    CXX=/tmp/ndkarm64/bin/aarch64-linux-android-clang++ \
    CFLAGS="-fPIE -Wno-tautological-constant-compare" \
    CXXFLAGS="-fPIE -Wno-tautological-constant-compare" \
    LDFLAGS="-fPIE -pie -L/tmp/ndkarm64/aarch64-linux-android/lib" \
    LIBS="-lc++_shared" \
    PKG_CONFIG_PATH=/tmp/out/x265/lib/pkgconfig:/tmp/out/libde265/lib/pkgconfig:/tmp/out/libpng/lib/pkgconfig \
    PATH=$PATH:/tmp/ndkarm64/bin
    
./configure --prefix=/tmp/out/libheif --host=aarch64-linux-android
make -j8 && make install

# Android neon support
# https://developer.android.com/ndk/guides/cpu-arm-neon

# x86 build
cmake -DCMAKE_SYSTEM_NAME=Linux -DCMAKE_SYSTEM_PROCESSOR=x86 -DCMAKE_C_COMPILER=/tmp/ndkx86/bin/i686-linux-android-clang -DCMAKE_CXX_COMPILER=/tmp/ndkx86/bin/i686-linux-android-clang++ -DCMAKE_FIND_ROOT_PATH=/tmp/ndkx86/sysroot -DENABLE_ASSEMBLY=OFF -DENABLE_CLI=OFF -DENABLE_PIC=ON -DENABLE_SHARED=OFF -DCMAKE_INSTALL_PREFIX=/tmp/outx86/x265 -DCMAKE_C_FLAGS="" -G "Unix Makefiles" ../../source

# libde265
export CC=/tmp/ndkx86/bin/i686-linux-android-clang CXX=/tmp/ndkx86/bin/i686-linux-android-clang++ CFLAGS="-fPIE" LDFLAGS="-fPIE -pie" PATH=$PATH:/tmp/ndkx86/bin
./configure --prefix=/tmp/outx86/libde265 --host=i686-linux-android --enable-shared=no --disable-arm

# libpng
export CC=/tmp/ndkx86/bin/i686-linux-android-clang CXX=/tmp/ndkx86/bin/i686-linux-android-clang++ CFLAGS='-fPIE' LDFLAGS='-fPIE -pie' PATH=$PATH:/tmp/ndkx86/bin
./configure --prefix=/tmp/outx86/libpng --host=i686-linux-android --enable-shared=no

# libheif
export CC=/tmp/ndkx86/bin/i686-linux-android-clang CXX=/tmp/ndkx86/bin/i686-linux-android-clang++ CFLAGS="-fPIE -Wno-tautological-constant-compare" CXXFLAGS="-fPIE -Wno-tautological-constant-compare" LDFLAGS="-fPIE -pie" PKG_CONFIG_PATH=/tmp/outx86/x265/lib/pkgconfig:/tmp/outx86/libde265/lib/pkgconfig:/tmp/outx86/libpng/lib/pkgconfig PATH=$PATH:/tmp/ndkx86/bin
./configure --prefix=/tmp/outx86/libheif --host=i686-linux-android

# x86_64 build
# x265
cmake -DCMAKE_SYSTEM_NAME=Linux -DCMAKE_SYSTEM_PROCESSOR=x86_64 -DCMAKE_C_COMPILER=/tmp/ndkx86_64/bin/x86_64-linux-android-clang -DCMAKE_CXX_COMPILER=/tmp/ndkx86_64/bin/x86_64-linux-android-clang++ -DCMAKE_FIND_ROOT_PATH=/tmp/ndkx86_64/sysroot -DENABLE_ASSEMBLY=OFF -DENABLE_CLI=OFF -DENABLE_PIC=ON -DENABLE_SHARED=OFF -DCMAKE_INSTALL_PREFIX=/tmp/outx86_64 -DCMAKE_C_FLAGS="" -G "Unix Makefiles" ../../source

# libde265
export CC=/tmp/ndkx86_64/bin/x86_64-linux-android-clang CXX=/tmp/ndkx86_64/bin/x86_64-linux-android-clang++ CFLAGS='-fPIE' LDFLAGS='-fPIE -pie' PATH=$PATH:/tmp/ndkx86_64/bin
./configure --prefix=/tmp/outx86_64 --host=x86_64-linux-android --enable-shared=no --disable-arm

# libpng
export CC=/tmp/ndkx86_64/bin/x86_64-linux-android-clang CXX=/tmp/ndkx86_64/bin/x86_64-linux-android-clang++ CFLAGS='-fPIE' LDFLAGS='-pie -fPIE' PATH=$PATH:/tmp/ndkx86_64/bin
./configure --prefix=/tmp/outx86_64 --host=x86_64-linux-android --enable-shared=no

# libheif
export CC=/tmp/ndkx86_64/bin/x86_64-linux-android-clang CXX=/tmp/ndkx86_64/bin/x86_64-linux-android-clang++ CFLAGS="-fPIE -Wno-tautological-constant-compare" CXXFLAGS="-fPIE -Wno-tautological-constant-compare" LDFLAGS="-fPIE -pie" PKG_CONFIG_PATH=/tmp/outx86_64/lib/pkgconfig PATH=$PATH:/tmp/ndkx86_64/bin
./configure --prefix=/tmp/outx86_64 --host=x86_64-linux-android
