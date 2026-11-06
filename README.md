# 2D rpg 카드 게임(자바 프로젝트)

> Java·Swing은 확정 사항이며, 나머지 도구와 버전은 팀 협의 후 확정한다.

## 개발 환경

```text
Java 21 LTS — 추천
= 게임 로직 구현 / 객체지향 설계
= 모든 팀원이 동일한 JDK 버전 사용

Java Swing — 확정
= 데스크톱 게임 화면 / 카드 UI / 버튼 / 화면 전환

Maven + Maven Wrapper — 추천
= 프로젝트 빌드 / 의존성 관리 / 테스트 실행
= 팀원 간 빌드 환경 통일

IntelliJ IDEA 또는 Eclipse
= 코드 작성 / 디버깅 / 실행
= IDE는 자유롭게 선택하되 JDK와 빌드 방식은 통일
```

## 게임 구현

```text
Swing JPanel + CardLayout
= 시작 / 경로 선택 / 전투 / 보상 / 상점 / 휴식 화면 구성
= 화면 간 이동 관리

Java2D
= 캐릭터 / 적 / 카드 이미지 / 체력바 그리기

Swing Timer
= 짧은 애니메이션 / 피해 표시 / 화면 효과
= 화면을 멈추는 대기 처리 방지

Java Collections
= 보유 덱 / 뽑기 더미 / 손패 / 버린 카드 더미 관리

java.util.Random
= 덱 셔플 / 적 선택 / 카드 보상 추첨
= 테스트에서는 고정된 시드로 결과 재현
```

## 데이터와 저장

```text
JSON + Gson — 추천
= 카드 20종 / 일반 적 5종 / 보스 1종의 정의 데이터 관리 (현재: database/CardCatalog, StageEnemy)
= 코드 수정 없이 비용과 효과 수치 조정

로컬 JSON 파일
= 발견한 기억 도감 / 음량 등 사용자 설정 저장
= 첫 버전은 도전 도중 저장·이어하기 제외

리소스 파일
= 이미지 PNG / 효과음 WAV / 데이터 JSON
= src/main/resources 아래에서 관리

저장 경로
= 게임 실행 파일과 분리된 사용자 전용 폴더
= 소스 코드와 Git 저장소에 개인 저장 데이터 포함 금지
```

## 코드 구조

Java와 Swing만 사용하며, 코드를 **frontend / backend / database** 세 영역으로 나눈다.

```text
src/
├── game/
│   ├── App.java                 실행 시작점 (Eclipse: Run As > Java Application)
│   ├── frontend/                🖥️ Swing 화면 — 입력을 받고 결과를 그린다
│   │   ├── common/              공통 색상·글꼴(Theme), 공통 버튼, 그림 위 클릭 영역(ImageHotspotButton), 이미지 로더
│   │   ├── start/               시작 화면, 메인 창(StartFrame), 게임 설명·설정 창
│   │   ├── stage/               1~5단계 선택 화면, 3단계 상점(ShopPanel · 카드 3장 버튼)
│   │   ├── deck/                내 덱 편집 (20장, 같은 카드 최대 2장)
│   │   ├── battle/              전투 화면·턴 타이머·공격/방어 대결(Clash)·효과 애니메이션·카드 그림·GAME OVER 화면
│   │   └── legacy/              이전 시안 보존용 (게임 흐름에서 사용 안 함)
│   ├── backend/                 ⚙️ 게임 규칙 — Swing 을 모른다
│   │   ├── model/               카드, 카드 종류, 플레이어, 적, 내 덱(PlayerDeck), 단계 진행도, 상점 강화(ShopItem·RunUpgrades)
│   │   └── battle/              전투 규칙(Battle) + 화면용 연결 클래스(DemoBattle)
│   ├── database/                🗄️ 데이터 — DB 서버 없이 코드·로컬 파일
│   │   ├── CardCatalog.java     카드 20종 정의
│   │   ├── StageEnemy.java      단계별 적 이름·이미지
│   │   └── SaveStore.java       사용자 폴더에 진행도·설정 저장 (화면과 아직 미연결)
│   └── test/                    검사 17개 (main 실행 → PASS 출력)
└── assets/                      이미지 PNG (배경, 적, 플레이어, 카드 그림, 상점, GAME OVER, 게임 설명)
```

### 호출 방향 (꼭 지키기)

```text
frontend  →  backend  →  database
(화면)        (규칙)        (데이터 파일)
```

- **frontend** 는 계산하지 않는다. 버튼 입력을 backend 에 전달하고 돌려받은 상태를 그린다. (예외: 덱 편집·적 그림은 database 의 카드 목록·적 이미지 정보를 직접 읽는다)
- **backend** 는 `javax.swing`, `java.awt` 를 import 하지 않는다. 그래서 창 없이 테스트할 수 있다.
- **database** 는 파일을 읽고 쓰기만 한다. 게임 규칙을 넣지 않는다.
- 카드 정의 데이터(`CardCatalog`)와 실제 보유 카드(`PlayerDeck`)는 별도로 관리한다.

### 담당 예시 (6인)

| 영역 | 주요 작업 |
|---|---|
| frontend | 시작·스테이지·전투·보상·상점·휴식 화면, 카드/적 그림, 애니메이션(Swing Timer) |
| backend | 덱·뽑기 더미·버린 카드, 카드 효과, 적 행동, 층 진행, 보상 |
| database | 카드·적 데이터 파일과 검증, 진행도·설정·기억 도감 저장 |

### 실행

- 더블클릭: `Run-Game.cmd`
- Eclipse: 저장소 폴더로 Java 프로젝트를 만들고 `src` 를 소스 폴더로 지정 → `game/App.java` 실행
- 검사: `game/test/` 의 각 클래스를 Java Application 으로 실행

이전 시제품 설명은 `docs/PROTOTYPE_NOTES.md` 에 보존했다.

## 테스트와 협업

```text
JUnit 5 — 추천
= 피해·방어 계산 / 에너지 소비 / 덱 순환 / 승패 판정 테스트

Git + GitHub
= 코드 이력 / 작업 브랜치 / Pull Request / 코드 리뷰

GitHub Issues — 추천
= 담당자 / 작업 내용 / 완료 조건 / 버그 관리

GitHub Actions — 도입 예정
= PR 제출 시 Maven 빌드와 자동 테스트 실행
= JDK와 빌드 설정 확정 후 구성

docs/ + ADR — 추천
= 게임 규칙 / 데이터 명세 / 구조 설계 / 주요 결정 근거 기록
= ADR은 중요한 기술 선택과 변경 이유를 남기는 문서

팀원 리뷰
= 다른 팀원 1명 이상 검토
= 요구사항 충족과 실제 실행 여부 확인 후 병합
```

## 실행과 배포

```text
개발 중 실행
= IDE에서 Main 클래스 실행

배포 파일
= 의존성과 리소스를 포함한 실행 가능한 JAR
= Java 21 실행 환경에서 java -jar 명령으로 실행

Windows 배포 — 여유가 있을 때
= jpackage로 실행 환경을 포함한 배포본 제작
= 첫 버전은 JAR 실행 검증을 우선
```

## 공통 개발 원칙

- Swing 화면 변경은 이벤트 처리 스레드(EDT)에서 수행한다.
- 파일 읽기·저장 등 오래 걸리는 작업은 화면 처리와 분리한다.
- 이미지와 데이터는 상대적인 리소스 경로로 불러온다. 개인 PC의 절대 경로를 코드에 넣지 않는다.
- 파일 인코딩은 UTF-8로 통일한다.
- 저장 파일이 없으면 새로 생성하고, 손상된 파일은 원본을 보존한 뒤 복구 안내를 표시한다.
- 첫 버전은 서버·로그인·외부 데이터베이스 없이 로컬 싱글플레이로 구현한다.
