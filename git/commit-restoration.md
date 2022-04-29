깃 유실된 커밋 복구

유실된 커밋을 찾고 해당 커밋의 커밋ID를 참조
```sh
git reflog
```

유실된 커밋을 head로 삼는 tree 전체를 복구
```sh
git reset --hard {commitID}
```

현재 브랜치로 작업 내용 가져오기
```sh
git cherry-pick {commitID}
```