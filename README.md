# BackEnd

## 🌿 Branch Strategy

- main: 배포 가능한 안정 버전
- dev: 개발 통합 브랜치
- feature/*: 기능 개발 브랜치

### Workflow

1. dev 브랜치에서 feature 브랜치 생성
2. 기능 개발 후 dev 브랜치로 merge (rebase 기반)
3. 테스트 완료 후 dev → main merge

### Branch Naming Convention

- feature/login
- feature/member
- feature/donation
- fix/bug-name
- refactor/code-improve


## 📝 Commit Convention

- feat: 새로운 기능 추가
- fix: 버그 수정
- refactor: 코드 리팩토링
- style: 코드 스타일 변경 (공백, 세미콜론 등)
- docs: 문서 수정
- test: 테스트 코드
- chore: 빌드, 설정 변경

### Example

feat: 로그인 기능 구현
fix: 회원가입 시 비밀번호 암호화 오류 수정
refactor: 회원 서비스 로직 분리
