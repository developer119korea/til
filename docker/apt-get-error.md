### Docker에서 패키지 설치시 에러가 발생함

apt 업데이트전에 apt/lists/\* 폴더를 삭제하는 명령어를 넣어 해결함

```sh
RUN rm -rf /var/lib/apt/lists/* -vf
RUN apt update -y && apt upgrade -y
RUN apt install -y locales
```

에러 내용

```sh
#0 20.48 Err:63 http://ports.ubuntu.com/ubuntu-ports bionic-updates/main arm64 linux-libc-dev arm64 4.15.0-200.211
#0 20.48   404  Not Found [IP: 185.125.190.36 80]
#0 20.48 Err:94 http://ports.ubuntu.com/ubuntu-ports bionic-updates/main arm64 libcurl4 arm64 7.58.0-2ubuntu3.21
#0 20.48   404  Not Found [IP: 185.125.190.36 80]
#0 20.48 Err:95 http://ports.ubuntu.com/ubuntu-ports bionic-updates/main arm64 curl arm64 7.58.0-2ubuntu3.21
#0 20.48   404  Not Found [IP: 185.125.190.36 80]
#0 25.54 Get:138 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 libpcre2-16-0 arm64 10.40-1+ubuntu18.04.1+deb.sury.org+1 [158 kB]
#0 27.04 Get:139 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 libpcre2-32-0 arm64 10.40-1+ubuntu18.04.1+deb.sury.org+1 [151 kB]
#0 28.58 Get:140 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 libpcre2-posix3 arm64 10.40-1+ubuntu18.04.1+deb.sury.org+1 [8,388 B]
#0 29.11 Get:141 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 libpcre2-dev arm64 10.40-1+ubuntu18.04.1+deb.sury.org+1 [599 kB]
#0 31.22 Get:142 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 libzip4 arm64 1.7.3-1+ubuntu18.04.1+deb.sury.org+2 [46.4 kB]
#0 32.93 Err:143 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 php7.4-xml arm64 1:7.4.33-1+ubuntu18.04.1+deb.sury.org+1
#0 32.93   404  Not Found [IP: 185.125.190.52 80]
#0 33.55 Get:144 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 php-pear all 1:1.10.13+submodules+notgz+2022032202-2+ubuntu18.04.1+deb.sury.org+1 [290 kB]
#0 35.36 Err:145 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 php7.4 all 1:7.4.33-1+ubuntu18.04.1+deb.sury.org+1
#0 35.36   404  Not Found [IP: 185.125.190.52 80]
#0 35.89 Err:146 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 php7.4-curl arm64 1:7.4.33-1+ubuntu18.04.1+deb.sury.org+1
#0 35.89   404  Not Found [IP: 185.125.190.52 80]
#0 36.43 Err:147 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 php7.4-dev arm64 1:7.4.33-1+ubuntu18.04.1+deb.sury.org+1
#0 36.43   404  Not Found [IP: 185.125.190.52 80]
#0 36.97 Err:148 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 php7.4-gd arm64 1:7.4.33-1+ubuntu18.04.1+deb.sury.org+1
#0 36.97   404  Not Found [IP: 185.125.190.52 80]
#0 37.52 Err:149 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 php7.4-intl arm64 1:7.4.33-1+ubuntu18.04.1+deb.sury.org+1
#0 37.52   404  Not Found [IP: 185.125.190.52 80]
#0 38.12 Err:150 http://ppa.launchpad.net/ondrej/php/ubuntu bionic/main arm64 php7.4-mbstring arm64 1:7.4.33-1+ubuntu18.04.1+deb.sury.org+1
```
