# 프로젝트별 Node버전 설정

## .nvmrc 사용

- 프로젝트에서 사용하는 Node.js 버전을 명시할 수 있음.
- 협업 환경 유지, 팀원들간의 Node.js 버전을 통일하여 사용할 수 있음.

## 적용방법

프로젝트 루트 경로에 `.nvmrc`파일 생성하고 내용에 버전(`v20.11.1`)을 명시하면된다.
터미널의 프로젝트 경로에서 `nvm use`를 입력하면 설정된 버전으로 Node.js 버전이 설정된다.

## 터미널에서 경로 이동시 자동으로 실행시키는 방법

`.zshrc`파일에 아래와 같이 입력

```
autoload -U add-zsh-hook
load-nvmrc() {
  if [ -f .nvmrc ]; then
    nvm use
  elif [ $(nvm version) != $(nvm version default) ]; then
    nvm use default
  fi
}
add-zsh-hook chpwd load-nvmrc
load-nvmrc
```
