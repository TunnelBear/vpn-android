#!/bin/bash

# Copyright (C) 2016 Tunnelbear Inc.
# Make sure you have autotools installed before using this!
# Run this before running ndk-build in the root of the vpn submodule.

OPENVPN_PATH=./src/main/core/openvpn/

if [ -d $OPENVPN_PATH ]; then
    cd $OPENVPN_PATH
else
    echo "Could not find OpenVPN directory; Bailing"
    return 1
fi

# Generate configure script
autoreconf -i -v -f

# Run configure script
if [[ $OSTYPE == darwin* ]]; then
    # macOS's built-in OpenSSL is terribly outdated; Use the headers and libs from the one in Homebrew
    ./configure CFLAGS="-I/usr/local/opt/openssl/include -I/usr/local/Cellar/lzo/2.10/include -I/usr/local/Cellar/lz4/1.7.5/include" LDFLAGS="-L/usr/local/opt/openssl/lib -L/usr/local/Cellar/lzo/2.10/lib -L/usr/local/Cellar/lz4/1.7.5/lib"
else
    ./configure
fi

# Create version header
make config-version.h

# NDK clean & build
cd ../
ndk-build clean
ndk-build -j16

if [ $? = 0 ]; then
    cd ../../../libs

    # Removed compiled openssl binaries and use platform libraries instead
    rm -v */libcrypto.so */libssl.so

    for arch in *
    do
        # Rename vpn binaries to libexecvpn
        if [ -f $arch/vpn ]; then
            cp $arch/vpn $arch/libexecvpn.so
            rm $arch/vpn
        fi
    done
else
    exit $?
fi
