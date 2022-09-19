구글 계정을 사용하여 로그인하는 경우

'Google.GoogleSignIn+SignInException' 로그인시 에러가 발생

https://developers.google.com/identity/sign-in/android/start
이 링크에서 CONFIGURE A PROJECT 버튼을 클릭하고 프로젝트를 선택하고 Android로 설정하고 패키지 이름과 SHA1을 입력합니다.
(Unity Editor의 Player Settings에 들어가 Publishing Settings에서 Keystore를 설정하고 SHA1 값을 가져와야 합니다.)

다음으로 위 링크에서 GOOGLE API CONSOLE 버튼을 클릭하면 프로젝트에 두 종류의 OAUTH 2.0 CLIENT ID가 있을 것입니다. 필요한 CLIENT ID는 '웹 클라이언트(Google 로그인을 위해 자동 생성됨)'입니다. 이 CLIENT ID를 SigninSampleScript.cs의 webClientId에 입력하십시오.