--  해당 게임의 진행도
SELECT gameId, clearedStage
FROM GAME_RUN
WHERE gameId = ?;