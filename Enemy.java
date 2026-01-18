package com.mygdx.game.entities;

import java.util.Random;

public class Enemy extends Character {
    private Random rand = new Random();

    public Enemy() {
        super("Goblin", 300, 20);
    }

    @Override
    public void specialAbility(Character target) {
        int damage = rand.nextBoolean() ? getAttackPower() : getAttackPower() * 2;
        target.takeDamage(damage);
    }
}