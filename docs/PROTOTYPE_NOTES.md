> **경로 안내:** 이 문서는 시제품 당시의 설명입니다. 코드는 `src/game/` 아래 frontend / backend / database 로 옮겨졌고, `DemoBattle` 은 `Battle` 로, 카드 수치는 `src/data/cards.csv` 로 바뀌었습니다. 최신 구조는 저장소 루트 `README.md` 의 "코드 구조"를 보세요.

# Swing 화면·카드 제작 시제품

Java, Swing만 사용합니다. 외부 라이브러리와 서버는 필요하지 않습니다.
그림판과 몬스터 카드 배치는 제외했습니다. 적은 카드가 아닌 임시 실루엣으로 표시합니다.

## Eclipse에서 실행

1. `File > Import > General > Existing Projects into Workspace`를 선택합니다.
2. `Select root directory`에서 이 `swing-prototype` 폴더를 선택합니다.
3. `CardBattlePrototype`을 체크하고 `Finish`를 누릅니다.
4. `src/game/App.java`를 우클릭해 `Run As > Java Application`을 선택합니다.

Java 환경 오류가 나오면 Eclipse의 `Preferences > Java > Installed JREs`에서 JDK 21을 등록하고,
프로젝트 `Properties > Java Build Path > Libraries`의 JRE System Library를 JavaSE-21로 지정하세요.

## 수정할 파일

### 시작 화면 3가지

현재 `App.java`는 **안개 낀 탑 배경 시작 화면**을 엽니다. 이전 세 시안 클래스는 보존했습니다.
게임 이름은 미정이며 `FantasyStartPanel.GAME_TITLE`에서 제목을 수정합니다.

- `ui/start/TowerStartPanel.java`: 어두운 탑 실루엣, 좌측 메뉴, 금색 강조.
- `ui/start/CardStartPanel.java`: 공격·방어·회복·주문 카드 4장 중심. 클릭하면 설명 표시.
- `ui/start/MinimalStartPanel.java`: 밝고 간결한 메뉴와 플레이 순서 안내.
- `ui/start/StartFrame.java`: 시작 화면과 전투 연결.
- `ui/start/FantasyStartPanel.java`: 탑 배경, 임시 로고와 메뉴 배치.
- `ui/start/FantasyMenuButton.java`: 메뉴 디자인과 선택 효과.
- `ui/start/SettingsDialog.java`: 창 크기 설정과 조작 안내.
- `src/assets/tower-menu-v1.png`: 배경 이미지. Eclipse가 빌드 출력으로 복사합니다.
- `ui/start/StartComponents.java`: 공통 버튼과 텍스트 스타일.

게임 시작/게임 설명/설정/게임 종료가 동작합니다.
게임 시작은 동일한 탑 배경의 1~5단계 안내 화면으로 연결됩니다.
`ui/start/StageSelectPanel.java`에서 단계 배치를 수정합니다.
처음에는 1단계만 금색으로 열립니다. 클리어한 단계는 초록색, 잠긴 단계는 사슬 X로 표시됩니다.
열린 단계 버튼을 눌러 전투에 입장하며, 승리해야 다음 단계가 열립니다.
패배하거나 중간에 돌아오면 다음 단계는 열리지 않습니다. 클리어한 단계는 재도전 가능합니다.
진행도는 실행 중에만 유지되며 프로그램 종료 후 저장되지 않습니다. 상점은 아직 없습니다.
시작 화면 버튼 또는 Esc로 돌아옵니다.
기존 전투 코드는 보존하되 시작 메뉴와의 직접 연결은 해제했습니다.

### 위아래 대치형 전투 화면

게임 시작 → 금색 또는 초록색 **단계 버튼**으로 실행합니다.
시작·스테이지·전투는 같은 창 안에서 전환됩니다. 현재 다섯 단계의 전투 내용은 같은 임시 규칙입니다.
전투의 '← 스테이지' 버튼으로 돌아오며, 다시 체험에 들어가면 새 전투로 시작합니다.
창 위치와 크기는 화면을 전환해도 유지됩니다. 설명·설정만 기존 보조 창을 사용합니다.
`game.frontend.battle.BattlePreviewFrame`을 직접 Java Application으로 실행해도 됩니다.

- `BattleScreenPanel.java`: 화면 배치와 카드/턴 버튼 연결.
- `BattleBoardPanel.java`: 12시 상대, 6시 플레이어, 체력·에너지 표시. 배경과 초상화는 임시 도형.
- `BattlePreviewFrame.java`: 시안 창 및 단독 실행 진입점.
- `CardView.java`: 기존 카드 디자인 재사용. 몬스터 배치/소환 규칙은 추가하지 않았습니다.
- `model/StageProgress.java`: 순차 클리어와 해금 상태.
- `ui/start/StageNodeButton.java`: 금색/초록색/사슬 X 표시와 입장 버튼.

### 상대 스켈레톤 이미지

`src/assets/skeleton-soldier-v1.png`를 현재 전투의 상대 초상화로 사용합니다.
이름은 스켈레톤 병사이며 기존 체력·공격 규칙은 변경하지 않았습니다.
내장 이미지 생성 도구로 제작한 투명 배경 PNG입니다.
프롬프트: “정면 상반신 스켈레톤 병사, 낡은 철 갑옷과 검, 푸른 눈빛,
어두운 판타지 2D 잉크풍, 작은 크기에서도 읽히는 실루엣, 투명 배경, 글자 없음.”
- 기존 `BattleFrame.java`는 이전 시안으로 보존합니다.
게임 설명 내용은 `ui/start/GameGuideDialog.java`에서 수정합니다.
설정은 실행 중 시작 창 크기에만 적용되며 저장하지 않습니다. 오디오가 없어 음량은 비활성입니다.
키 재지정은 미구현이며 조작 안내만 제공합니다. 메뉴는 ↑↓/Tab 이동, Enter/Space 선택이 가능합니다.

배경 제작: 내장 이미지 생성 도구 사용. 프롬프트 요약:
“청회색 안개와 잉크풍 폐허, 우측의 거대한 탑, 중앙에 제목·메뉴용 여백.
나무·캐릭터·문구·로고·버튼 없이 배경만 생성.”

| 작업 | 파일 |
| --- | --- |
| 전체 화면 배치 | `src/game/frontend/legacy/BattleFrame.java` |
| 카드 크기·테두리·아이콘·텍스트 배치 | `src/game/frontend/battle/CardView.java` |
| 손패 나열과 클릭 연결 | `src/game/frontend/battle/HandPanel.java` |
| 전장 배경과 적 그림 | `src/game/frontend/legacy/BattlefieldPanel.java` |
| 공통 색상·폰트 | `src/game/frontend/common/Theme.java` |
| 예시 카드 이름·비용·수치·설명 | `src/data/cards.csv` |
| 카드 데이터 구조·분류 | `src/game/backend/model/Card.java`, `CardType.java` |
| 임시 전투 처리 (백엔드 교체 대상) | `src/game/backend/battle/Battle.java` |
| 실행 시작점 | `src/game/App.java` |

카드 설명과 power 수치는 서로 자동 생성되지 않으므로 함께 수정하세요.
주문은 현재 '다음 공격 피해 증가'만 지원합니다. 새 효과는 백엔드 담당자와 협의해 추가하세요.

## 임시 동작

- 카드 클릭 시 즉시 효과 적용, 에너지 부족 시 사용 불가.
- 카드는 공격, 회복을 둘 중 하나만 사용하도록 제작/ 상대방 공격 시 방어 카드가 있을 때 방어 가능
- 턴 종료 시 적 공격을 방어도로 먼저 막고, 다음 턴에 방어도와 공격 보너스를 초기화.
- 다음 턴에는 에너지와 고정 손패가 다시 채워짐. 실제 덱/버린 카드/무작위 뽑기는 미구현.
- 체력 0이면 전투 종료, '처음부터'로 재시작.
- 층 이동, 카드 보상·강화, 저장, 정식 이미지와 애니메이션은 미구현.
- 모든 수치와 동작은 화면 확인용이며 최종 게임 규칙이 아닙니다.

## 테스트

`src/game/test/PrototypeCheck.java`를 Java Application으로 실행하면 임시 로직과
화면 컴포넌트 생성 검사를 수행합니다. 창을 띄우지 않는 테스트입니다.
