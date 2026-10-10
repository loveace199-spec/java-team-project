/**
 * [database] 게임 데이터. 외부 DB 서버나 라이브러리 없이 Java 기본 기능만 사용한다.
 *
 * <ul>
 *   <li>CardCatalog : 카드 20종 정의 (이름·비용·효과)</li>
 *   <li>StageEnemy  : 단계별 적 이름·이미지</li>
 *   <li>SaveStore   : 사용자 폴더에 진행도·설정을 저장한다 (아직 화면과 미연결)</li>
 * </ul>
 *
 * 규칙: Swing 을 import 하지 않는다. 게임 규칙 계산을 하지 않는다.
 */
package game.database;
