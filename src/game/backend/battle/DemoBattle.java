package game.backend.battle;

import java.util.List;
import game.backend.model.Card;

/** 기존 화면의 API를 유지하는 호환 계층. 모든 전투 계산은 backend.Battle에 위임합니다. */
public final class DemoBattle {
    public static final int PLAYER_MAX_HP=Battle.PLAYER_MAX_HP;
    public static final int ENEMY_MAX_HP=Battle.ENEMY_MAX_HP;
    private final Battle battle;
    public DemoBattle() { battle=new Battle(); }
    public DemoBattle(List<Card> cards) { battle=new Battle(cards); }
    public DemoBattle(List<Card> cards, game.backend.model.RunUpgrades upgrades) { battle=new Battle(cards, upgrades); }
    public int playerMaxHp() { return battle.playerMaxHp(); }
    public void reset() { battle.reset(); }
    public void enableReactions() { battle.enableReactions(); }
    public boolean canPlay(int index) { return battle.canPlay(index); }
    public String play(int index) { return battle.play(index); }
    public String endTurn() { return battle.endTurn(); }
    public int playerHp() { return battle.playerHp(); }
    public int enemyHp() { return battle.enemyHp(); }
    public int block() { return battle.block(); }
    public int bonus() { return battle.bonus(); }
    public int turn() { return battle.turn(); }
    public int enemyIntent() { return battle.enemyIntent(); }
    public int drawCount() { return battle.drawCount(); }
    public int discardCount() { return battle.discardCount(); }
    public int poisonDamage() { return battle.poisonDamage(); }
    public int enemyHandCount() { return battle.enemyHandCount(); }
    public int enemyDrawCount() { return battle.enemyDrawCount(); }
    public int enemyDiscardCount() { return battle.enemyDiscardCount(); }
    public boolean isOver() { return battle.isOver(); }
    public boolean playerWon() { return battle.playerWon(); }
    public List<Card> hand() { return battle.hand(); }
    public Card pendingAttack() { return battle.pendingAttack(); }
    public boolean waitingForDefense() { return battle.waitingForDefense(); }
    public Card revealEnemyAttack() { return battle.revealEnemyAttack(); }
    public Card chooseEnemyDefense() { return battle.chooseEnemyDefense(); }
    public Card commitDefense(int index) { return battle.commitDefense(index); }
    public String resolveAttack(Card defense) { return battle.resolveAttack(defense); }
    public void startNextRound() { battle.startNextRound(); }
    public void prepareEnemyTurn() { battle.prepareEnemyTurn(); }
    public Card lastEnemyCard() { return battle.lastEnemyCard(); }
}
