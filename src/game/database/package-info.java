/**
 * [database] 데이터 파일 읽기·쓰기. 외부 DB 서버나 라이브러리 없이 Java 기본 기능만 사용한다.
 *
 * <ul>
 *   <li>CardCatalog : src/data/cards.csv 의 카드 정의를 읽는다</li>
 *   <li>SaveStore   : 사용자 폴더에 진행도·설정을 저장한다 (아직 화면과 미연결)</li>
 * </ul>
 *
 * 앞으로 추가할 곳 예: EnemyCatalog (src/data/enemies.csv), 기억 도감 저장.
 * 규칙: Swing 을 import 하지 않는다. 게임 규칙 계산을 하지 않는다.
 */
package game.database;
