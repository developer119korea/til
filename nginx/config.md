# config 파일 위치

오랜만에 엔진엑스 설정을 변경하려고 했는데 파일 위치를 까먹었다.
아래의 명령어로 쉽게 파일 위치를 찾을 수 있다.

---

엔진엑스(nginx)는 일반적으로 macOS에서 Homebrew를 이용하여 설치할 경우, 설정 파일(Configuration file)이 /usr/local/etc/nginx/nginx.conf 경로에 있습니다. 그러나 일부 설정은 /usr/local/etc/nginx/conf.d 디렉토리에 저장될 수 있고, 각 개별 사이트에 대한 설정은 /usr/local/etc/nginx/servers 디렉토리에 있을 수 있습니다. 그 외에도 일부 설정은 /usr/local/etc/nginx/includes 디렉토리에 있을 수 있습니다.

```sh
nginx -V

nginx version: nginx/1.19.2
built by clang 10.0.1 (clang-1001.0.46.4)
built with OpenSSL 1.1.1g  21 Apr 2020
TLS SNI support enabled
configure arguments: --prefix=/usr/local/Cellar/nginx/1.19.2 --with-cc-opt='-I/usr/local/opt/pcre/include -I/usr/local/opt/openssl@1.1/include -I/usr/local/opt/zlib/include' --with-ld-opt='-L/usr/local/opt/pcre/lib -L/usr/local/opt/openssl@1.1/lib -L/usr/local/opt/zlib/lib' --with-compat --with-file-aio --with-threads --with-http_addition_module --with-http_auth_request_module --with-http_dav_module --with-http_flv_module --with-http_gunzip_module --with-http_gzip_static_module --with-http_mp4_module --with-http_random_index_module --with-http_realip_module --with-http_slice_module --with-http_ssl_module --with-http_sub_module --with-http_stub_status_module --with-http_v2_module --with-http_secure_link_module --with-mail --with-mail_ssl_module --with-stream --with-stream_realip_module --with-stream_ssl_module --with-stream_ssl_preread_module --with-debug --with-pcre --with-pcre-jit --with-openssl=openssl@1.1 --with-opens

```

출력된 정보중에서 --conf-path 값을 보면 된다.

```sh
--conf-path=/opt/homebrew/etc/nginx/nginx.conf
```
