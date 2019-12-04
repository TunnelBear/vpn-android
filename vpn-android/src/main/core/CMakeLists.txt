cmake_minimum_required(VERSION 3.6.0)

include(tools.cmake)
include(lzo.cmake)
include(openssl.cmake)

# Point to OpenVPN src files.
set(openvpn_srcs
        src/compat/compat-basename.c
        src/compat/compat-daemon.c
        src/compat/compat-dirname.c
        src/compat/compat-gettimeofday.c
        src/compat/compat-inet_ntop.c
        src/compat/compat-inet_pton.c
        src/compat/compat-lz4.c
        src/openvpn/argv.c
        src/openvpn/base64.c
        src/openvpn/buffer.c
        src/openvpn/clinat.c
        src/openvpn/console.c
        src/openvpn/console_builtin.c
        src/openvpn/crypto.c
        src/openvpn/crypto_openssl.c
        src/openvpn/crypto_mbedtls.c
        src/openvpn/cryptoapi.c
        src/openvpn/dhcp.c
        src/openvpn/error.c
        src/openvpn/event.c
        src/openvpn/env_set.c
        src/openvpn/fdmisc.c
        src/openvpn/forward.c
        src/openvpn/fragment.c
        src/openvpn/gremlin.c
        src/openvpn/helper.c
        src/openvpn/httpdigest.c
        src/openvpn/init.c
        src/openvpn/interval.c
        src/openvpn/list.c
        src/openvpn/lladdr.c
        src/openvpn/lzo.c
        src/openvpn/manage.c
        src/openvpn/mbuf.c
        src/openvpn/misc.c
        src/openvpn/mroute.c
        src/openvpn/mss.c
        src/openvpn/mstats.c
        src/openvpn/mtcp.c
        src/openvpn/mtu.c
        src/openvpn/mudp.c
        src/openvpn/multi.c
        src/openvpn/ntlm.c
        src/openvpn/occ.c
        src/openvpn/openvpn.c
        src/openvpn/options.c
        src/openvpn/otime.c
        src/openvpn/packet_id.c
        src/openvpn/perf.c
        src/openvpn/pf.c
        src/openvpn/ping.c
        src/openvpn/pkcs11.c
        src/openvpn/pkcs11_openssl.c
        src/openvpn/platform.c
        src/openvpn/plugin.c
        src/openvpn/pool.c
        src/openvpn/proto.c
        src/openvpn/proxy.c
        src/openvpn/ps.c
        src/openvpn/push.c
        src/openvpn/reliable.c
        src/openvpn/route.c
        src/openvpn/run_command.c
        src/openvpn/schedule.c
        src/openvpn/session_id.c
        src/openvpn/shaper.c
        src/openvpn/sig.c
        src/openvpn/socket.c
        src/openvpn/socks.c
        src/openvpn/ssl.c
        src/openvpn/ssl_openssl.c
        src/openvpn/ssl_mbedtls.c
        src/openvpn/ssl_verify.c
        src/openvpn/ssl_verify_openssl.c
        src/openvpn/ssl_verify_mbedtls.c
        src/openvpn/status.c
        src/openvpn/tls_crypt.c
        src/openvpn/tun.c
        src/openvpn/comp-lz4.c
        src/openvpn/comp.c
        src/openvpn/compstub.c
        )

# Set OpenVPN library with access to any necessary files and compilation flags.
PREPEND(openvpn_srcs_with_path "openvpn" ${openvpn_srcs})
add_library(openvpn SHARED ${openvpn_srcs_with_path})
target_include_directories(openvpn PRIVATE
        config
        openvpn/src/compat
        openvpn/include
        lzo/include
        openvpn
        )
target_compile_definitions(openvpn PRIVATE
        -DENABLE_CRYPTO_OPENSSL=1
        -DENABLE_LZO=0
        -DENABLE_LZ4=0
        -DHAVE_CONFIG_H
        -DCONFIGURE_GIT_REVISION=\"${OPENVPN2_GIT}\"
        -DCONFIGURE_GIT_FLAGS=\"\"
        -DTARGET_ABI=\"${ANDROID_ABI}\"
        )

# Link OpenVPN against OpenSSL and LZO
target_link_libraries(openvpn crypto ssl lzo)

# Add no-op executable that links against libopenvpn.so. Will allow for OpenVPN to be properly updated.
add_executable(pie_openvpn.${ANDROID_ABI} minivpn/minivpn.c)
target_compile_options(pie_openvpn.${ANDROID_ABI} PRIVATE -fPIE -fstack-protector)
target_link_libraries(pie_openvpn.${ANDROID_ABI} PRIVATE openvpn -fPIE -pie -fstack-protector)

add_executable(nopie_openvpn.${ANDROID_ABI} minivpn/minivpn.c)
target_link_libraries(nopie_openvpn.${ANDROID_ABI} PRIVATE openvpn -fstack-protector)

# Move binaries over to custom assets directory
SET(BINARY_ASSET_DIR ${CMAKE_SOURCE_DIR}/../../../build/binaryassets)
add_custom_target(makeassetdir ALL COMMAND ${CMAKE_COMMAND} -E make_directory ${BINARY_ASSET_DIR})
add_custom_command(TARGET pie_openvpn.${ANDROID_ABI} POST_BUILD
        COMMAND
        ${CMAKE_COMMAND} -E copy
        ${CMAKE_CURRENT_BINARY_DIR}/pie_openvpn.${ANDROID_ABI}
        ${BINARY_ASSET_DIR}
        )
add_custom_command(TARGET nopie_openvpn.${ANDROID_ABI} POST_BUILD
        COMMAND
        ${CMAKE_COMMAND} -E copy
        ${CMAKE_CURRENT_BINARY_DIR}/nopie_openvpn.${ANDROID_ABI}
        ${BINARY_ASSET_DIR}
        )

add_dependencies(pie_openvpn.${ANDROID_ABI} makeassetdir)
add_dependencies(nopie_openvpn.${ANDROID_ABI} makeassetdir)
