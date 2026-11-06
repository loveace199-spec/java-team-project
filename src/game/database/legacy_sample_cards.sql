-- 추가: 구형 시안·검사의 샘플 카드 5종입니다. 필요한 경우 DB에서 한 번 실행합니다.
-- 실제 게임의 기본 카드 20종과 구분하기 위해 artworkIndex를 -1로 저장합니다.
-- 아래 입력 순서는 sampleHand()가 사용하던 순서입니다. Java가 자동 실행하지 않습니다.
INSERT INTO CARD (cardCode, cardName, cardType, cardEffect, cost, damage, `block`, heal, poison, duration, artworkIndex)
VALUES
('slash', '베기', 'ATTACK', '적에게 피해 6', 1, 6, 0, 0, 0, 0, -1),
('guard', '방패', 'DEFENSE', '방어도 5 획득', 1, 0, 5, 0, 0, 0, -1),
('heal', '응급처치', 'HEAL', '체력 4 회복', 1, 0, 0, 4, 0, 0, -1),
('focus', '집중', 'SPELL', '이번 턴 다음 공격 피해 +2', 0, 2, 0, 0, 0, 0, -1),
('heavy', '강타', 'ATTACK', '적에게 피해 11', 2, 11, 0, 0, 0, 0, -1);
