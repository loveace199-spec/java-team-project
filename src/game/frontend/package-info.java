/**
 * [frontend] Swing 화면. 사용자 입력을 받아 backend 에 전달하고, 결과를 그립니다.
 *
 * <ul>
 *   <li>common : 공통 색상·글꼴(Theme), 공통 버튼·글자(StartComponents)</li>
 *   <li>start  : 시작 화면, 메인 창(StartFrame), 게임 설명·설정 창</li>
 *   <li>stage  : 1~5단계 선택 화면</li>
 *   <li>battle : 전투 화면, 카드 그림(CardView), 손패(HandPanel)</li>
 *   <li>legacy : 이전 시안 보존용 (현재 게임 흐름에서 사용하지 않음)</li>
 * </ul>
 *
 * 규칙: 게임 수치 계산을 하지 않는다. game.database 를 직접 import 하지 않는다.
 * Swing 화면 변경은 EDT 에서 수행한다.
 */
package game.frontend;
