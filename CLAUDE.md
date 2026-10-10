# 프로젝트 규칙 (AI 도우미용)

- 2D RPG 카드 게임. **순수 Java + Swing 만** 사용한다.
- 외부 라이브러리(Gson 등), 프레임워크(Spring, JavaFX), 웹 프론트엔드, 외부 DB 서버(MySQL 등)를 추가하지 않는다.
- 데이터는 database 패키지(CardCatalog, StageEnemy)와 사용자 폴더 저장 파일로 관리하고 Java 기본 기능으로 읽고 쓴다.
- src/module-info.java 를 만들지 않는다 (Swing 컴파일이 깨짐).
- 구조: src/game/frontend(Swing 화면) → backend(게임 규칙) → database(데이터 파일). 역방향 의존 금지.
- backend·database 는 javax.swing / java.awt 를 import 하지 않는다.
- Git: develop 에서 feature/* 브랜치 → develop 대상 PR. 커밋 메시지 `종류: 내용` (outputs/GIT_WORKFLOW.md).
