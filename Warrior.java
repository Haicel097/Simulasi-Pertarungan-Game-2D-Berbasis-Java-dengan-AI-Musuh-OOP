package com.mygdx.game.entities;

public class Warrior extends Character {
    public Warrior() {
        super("Prajurit", 150, 20);
    }

    @Override
    public void specialAbility(Character target) {
        target.takeDamage(getAttackPower());
    }
}