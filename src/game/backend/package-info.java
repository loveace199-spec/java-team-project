/**
 * [backend] 게임 규칙과 상태. Swing(javax.swing, java.awt)을 import 하지 않는다.
 *
 * <ul>
 *   <li>model  : 카드, 카드 종류, 플레이어(체력·에너지·방어도), 적, 단계 진행도</li>
 *   <li>battle : 턴 진행, 카드 효과 적용, 승패 판정 (Player/Enemy 값을 변경)</li>
 * </ul>
 *
 * 앞으로 추가할 곳 예: deck(덱·뽑기·버린 카드), enemy(적 행동), reward(보상), run(도전 상태).
 * 카드·적 정의 데이터가 필요하면 game.database 에서 받아 온다.
 */
package game.backend;
