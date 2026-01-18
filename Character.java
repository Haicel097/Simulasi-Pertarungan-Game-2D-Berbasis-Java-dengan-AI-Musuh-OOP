package com.mygdx.game.entities;

public abstract class Character {
    private String name;
    private int hp;
    private int maxHp;
    private int attackPower;

    public Character(String name, int hp, int attackPower) {
        this.name = name;
        this.maxHp = this.hp = hp;
        this.attackPower = attackPower;
    }

    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public void setHp(int hp) {
        this.hp = Math.min(hp, maxHp);
    }

    public int getAttackPower() { return attackPower; }

    public void takeDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);
    }

    public void heal(int amount) {
        this.hp = Math.min(maxHp, this.hp + amount);
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public abstract void specialAbility(Character target);
}