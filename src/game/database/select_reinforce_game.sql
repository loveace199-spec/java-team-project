-- 해당 게임에서 선택한 증강 정보
SELECT
    INVENTORY.inventoryId,
    REINFORCE.*
FROM INVENTORY 
JOIN REINFORCE
    ON INVENTORY.reinforceId = REINFORCE.reinforceId
WHERE INVENTORY.gameId = ?;