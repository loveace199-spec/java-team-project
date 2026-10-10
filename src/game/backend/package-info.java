/**
 * [backend] 게임 규칙과 상태. Swing(javax.swing, java.awt)을 import 하지 않는다.
 *
 * <ul>
 *   <li>model  : 카드, 카드 종류, 플레이어, 적, 내 덱(PlayerDeck), 단계 진행도</li>
 *   <li>battle : 전투 규칙(Battle), 화면이 쓰는 연결 클래스(DemoBattle)</li>
 * </ul>
 *
 * 앞으로 추가할 곳 예: deck(덱·뽑기·버린 카드), enemy(적 행동), reward(보상), run(도전 상태).
 * 카드·적 정의 데이터가 필요하면 game.database 에서 받아 온다.
 */
package game.backend;
