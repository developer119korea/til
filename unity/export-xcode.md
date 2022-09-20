## Export Xcode Pojrect -> Pod install error 발생

어떤 조건인지 정확히 파악된것은 아니지만, 유니티 프로젝트에서 Xcode 프로젝트 추출시
Pod install 관련에러가 발생하고 Xcode.workspace가 생성되지 않는 현상이 발생하면
Xcode 프로젝트에서 아래 커맨드를 사용하여, 수동으로 생성해줄수 있다

```bash
M1 Chip Macs CocoaPods Commands:

    arch -x86_64 pod install
or
    sudo arch -x86_64 gem install ffi
Then
    arch -x86_64 pod install
```