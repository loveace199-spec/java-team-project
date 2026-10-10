-- 해당 게임의 덱에 들어 있는 카드 정보
SELECT
    DECK.deckId,
    dc.deckCardId,
    CARD.*
FROM DECK 
JOIN DECK_CARD as dc
    ON DECK.deckId = dc.deckId
JOIN CARD
    ON dc.cardId = CARD.cardId
WHERE DECK.gameId = ?
ORDER BY dc.deckCardId;