# 기억을 먹는 탑 — Git 협업 규칙

## 브랜치 역할

| 브랜치 | 용도 | 병합 대상 |
|---|---|---|
| main | 실행과 시연이 가능한 안정 버전 | develop에서 검증 후 반영 |
| develop | 팀원들의 작업을 모으는 통합 버전 | feature/*, fix/* |
| feature/<작업명> | 기능 하나 개발 | develop |
| fix/<작업명> | 오류 하나 수정 | develop |

예: feature/battle-turn, feature/card-deck, feature/enemy-intent, feature/battle-ui, feature/card-reward.
브랜치명은 소문자 영문과 하이픈으로 작성합니다. 한 브랜치에는 한 작업만 담습니다.

## 팀 규칙

1. main과 develop에 직접 커밋하거나 푸시하지 않습니다. 최초 저장소 등록은 예외입니다.
2. develop에서 작업 브랜치를 만들고, develop을 대상으로 Pull Request(PR)를 엽니다.
3. 다른 팀원 1명 이상의 검토를 받은 뒤 병합합니다. 작성자가 자신의 검토를 대신하지 않습니다.
4. 기능 PR은 Squash merge로 합칩니다. 병합된 원격 작업 브랜치는 삭제합니다.
5. 매일 완료된 작은 단위로 PR을 제출합니다. 실행이 깨지는 미완성 작업은 Draft PR로 공유합니다.
6. 시연 전에 develop 전체를 확인하고 main으로 PR을 엽니다. 이 PR은 merge commit 방식으로 합쳐 두 장기 브랜치의 이력을 유지합니다.
7. 공용 브랜치의 강제 푸시와 이력 재작성은 금지합니다. 충돌 시 해당 코드를 작성한 팀원과 확인합니다.

## 작업 시작

원격 저장소 등록 및 최초 업로드 후 사용합니다.

```powershell
git switch develop
git pull --ff-only origin develop
git switch -c feature/battle-turn
```

수정한 파일을 확인하고 필요한 파일만 커밋합니다. 아래 경로는 실제 수정한 경로로 바꾸세요.

```powershell
git status
git add src/main/java/example/BattleManager.java
git commit -m "feat: 플레이어 턴 진행 구현"
git push -u origin feature/battle-turn
```

GitHub에서 대상 브랜치를 develop으로 선택해 PR을 작성합니다.

## 작업 중 최신 통합 내용 반영

먼저 현재 작업을 커밋한 후 작업 브랜치에서 실행합니다.

```powershell
git fetch origin
git merge origin/develop
```

충돌이 나면 표시된 파일을 수정하고 정상 동작을 확인한 뒤 해당 파일을 git add하고 git commit합니다. 병합을 취소하려면 git merge --abort를 사용합니다.

## 커밋 메시지

형식: `종류: 변경 내용`

| 종류 | 예시 |
|---|---|
| feat | feat: 적의 다음 행동 표시 |
| fix | fix: 에너지가 부족할 때 카드 사용 차단 |
| refactor | refactor: 피해 계산 로직 분리 |
| docs | docs: 전투 규칙 문서 추가 |
| test | test: 버린 카드 재셔플 검증 |
| chore | chore: Java 빌드 설정 추가 |

## 팀원 개발 환경 연결

저장소: https://github.com/loveace199-spec/java-team-project
비공개 저장소이므로 관리자가 팀원을 초대하고 팀원이 초대를 수락해야 접근할 수 있습니다. 팀원은 저장소를 복제한 뒤 자신의 이름과 이메일을 설정합니다.

```powershell
git clone https://github.com/loveace199-spec/java-team-project.git
cd java-team-project
git config user.name "본인 이름"
git config user.email "본인 이메일"
git config pull.ff only
git config fetch.prune true
git switch develop
```

## 원격 브랜치 보호 — 아직 적용되지 않은 관리자 작업

이 문서와 로컬 Git 설정만으로는 직접 푸시를 차단할 수 없습니다. 원격 저장소에서 main과 develop에 다음 보호 규칙을 설정해야 합니다. 서비스와 요금제에 따라 지원 범위가 다를 수 있습니다.

- PR을 통한 병합을 필수로 설정
- 다른 팀원 최소 1명의 승인 요구
- 새 커밋이 추가되면 기존 승인 재검토
- 검토 대화 해결 후 병합
- 강제 푸시와 브랜치 삭제 차단
- 관리자 우회도 가능한 범위에서 제한

저장소의 병합 옵션은 Squash merge와 merge commit을 허용합니다. 빌드 자동화는 JDK와 빌드 도구를 확정한 뒤 추가하고, 그때 빌드 성공을 병합 조건으로 설정합니다.

## 이번 설정의 범위

GitHub 비공개 저장소에 Java용 제외 규칙, 줄바꿈 규칙, PR 양식과 협업 안내를 저장합니다. main은 안정 버전, develop은 통합 버전으로 사용합니다. 팀원 초대, 브랜치 보호 강제 적용, 빌드 자동화는 별도 관리자 작업입니다. 로컬 설정 스크립트는 빈 로컬 저장소 초기화용이며, 팀원은 위의 git clone 절차를 사용합니다.
