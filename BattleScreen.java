package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.mygdx.game.MyGame;
import com.mygdx.game.entities.Character;
import com.mygdx.game.entities.Enemy;
import com.mygdx.game.entities.Warrior;

public class BattleScreen implements Screen {

    private static final float HP_BAR_WIDTH = 180f;
    private static final float HP_BAR_HEIGHT = 10f;
    private static final float GOBLIN_HIT_DURATION = 0.30f;
    private static final float WARRIOR_ATTACK_DURATION = 0.2f;
    private static final float WARRIOR_HIT_DURATION = 0.30f;

    // Konstanta posisi
    private static final float WARRIOR_X = 50f;
    private static final float WARRIOR_Y = 100f;
    private static final float GOBLIN_X_OFFSET = 250f;
    private static final float GOBLIN_Y = 120f;
    private static final float MOVE_SPEED = 10f;
    private static final float FIRE_SPEED = 30f;
    private static final float BG_SCROLL_SPEED = 150f;
    private static final float GOBLIN_TRACKING_SPEED = 2.0f;

    // Konstanta posisi tembakan
    private static final float FIRE_OFFSET_X = 0.65f;
    private static final float FIRE_OFFSET_Y = 0.55f;

    // Konstanta GUI Pause Menu
    private static final float BUTTON_WIDTH = 350f;
    private static final float BUTTON_HEIGHT = 60f;
    private static final float BUTTON_SPACING = 20f;

    private MyGame game;
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont nameFont;
    private BitmapFont pauseFont;
    private BitmapFont buttonFont;

    private Character player;
    private Array<Enemy> goblins;
    private int currentGoblinIndex = 0;

    private String playerName;

    private Texture bg, warriorTex, warriorAttackTex, warriorHurtTex, warriorDeadTex,
            goblinNormalTex, goblinHitTex, fireTex, warriorProjectileTex;

    private Texture winTexture, loseTexture;
    private Sprite winSprite, loseSprite;

    private Sprite warrior;
    private Sprite goblin;
    private Sprite warriorFire;
    private Sprite goblinFire;

    private Texture hpBarTexture;
    private Texture pauseOverlayTexture;
    private Texture buttonTexture;
    private Texture buttonHoverTexture;

    private boolean warriorFireActive = false;
    private boolean goblinFireActive = false;

    private boolean goblinIsHit = false;
    private float goblinHitTimer = 0f;

    private boolean warriorIsAttacking = false;
    private float warriorAttackTimer = 0f;

    private boolean warriorIsHit = false;
    private float warriorHitTimer = 0f;

    private float moveSpeed = MOVE_SPEED;
    private float fireSpeed = FIRE_SPEED;

    private float bgOffsetX = 0f;
    private float bgScrollSpeed = BG_SCROLL_SPEED;

    private boolean gameOver = false;
    private boolean soundPlayed = false;

    // ✅ SISTEM PAUSE
    private boolean isPaused = false;

    // ✅ GUI BUTTONS
    private Rectangle resumeButton;
    private Rectangle lobbyButton;
    private int hoveredButton = -1; // -1 = none, 0 = resume, 1 = lobby

    // Interval tembakan yang lebih seimbang
    private float[] shootIntervals = {1.2f, 0.8f, 0.5f}; // Goblin 1, 2, 3
    private Task goblinShootTask;

    private Music battleMusic;
    private Sound winSound;
    private Sound loseSound;

    public BattleScreen(MyGame game) {
        this.game = game;
        this.playerName = game.getPlayerName();

        player = new Warrior();

        goblins = new Array<>();
        for (int i = 0; i < 3; i++) {
            Enemy goblin = new Enemy();
            goblin.setHp(goblin.getMaxHp() * 2);
            goblins.add(goblin);
        }

        batch = new SpriteBatch();

        font = new BitmapFont();
        font.getData().setScale(2f);

        nameFont = new BitmapFont();
        nameFont.getData().setScale(1.2f);

        pauseFont = new BitmapFont();
        pauseFont.getData().setScale(3f);

        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(2f);

        bg = new Texture("assets/game_background_1.png");
        warriorTex = new Texture("assets/warrior_biasa.png");
        warriorAttackTex = new Texture("assets/warrior_attack.png");
        warriorHurtTex = new Texture("assets/warrior_hurt.png");
        goblinNormalTex = new Texture("assets/0_Goblin_Running_002.png");
        goblinHitTex = new Texture("assets/goblin_hit.png");
        fireTex = new Texture("assets/fireball.png");
        warriorProjectileTex = new Texture("assets/Water Spell_Frame_01.png");

        winTexture = new Texture("assets/menang.jpeg");
        loseTexture = new Texture("assets/kalah.jpeg");

        warrior = new Sprite(warriorTex);
        winSprite = new Sprite(winTexture);
        loseSprite = new Sprite(loseTexture);

        warrior.setSize(400, 280);
        winSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        loseSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        warrior.setPosition(WARRIOR_X, WARRIOR_Y);

        goblin = new Sprite(goblinNormalTex);
        goblin.setSize(200, 200);
        goblin.setPosition(Gdx.graphics.getWidth() - GOBLIN_X_OFFSET, GOBLIN_Y);

        warriorFire = new Sprite(warriorProjectileTex);
        warriorFire.setSize(85, 60);

        goblinFire = new Sprite(fireTex);
        goblinFire.setSize(85, 100);

        // HP Bar texture
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        hpBarTexture = new Texture(pixmap);
        pixmap.dispose();

        // Pause overlay texture (semi-transparent black)
        Pixmap pausePixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pausePixmap.setColor(0, 0, 0, 0.7f);
        pausePixmap.fill();
        pauseOverlayTexture = new Texture(pausePixmap);
        pausePixmap.dispose();

        // Button textures
        createButtonTextures();

        // Initialize button rectangles
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;

        resumeButton = new Rectangle(
                centerX - BUTTON_WIDTH / 2,
                centerY - 20,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
        );

        lobbyButton = new Rectangle(
                centerX - BUTTON_WIDTH / 2,
                centerY - 20 - BUTTON_HEIGHT - BUTTON_SPACING,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
        );

        battleMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/battle_musik/game_musik.mp3"));
        battleMusic.setLooping(true);
        battleMusic.setVolume(0.5f);
        battleMusic.play();

        winSound = Gdx.audio.newSound(Gdx.files.internal("assets/sound/win.mp3"));
        loseSound = Gdx.audio.newSound(Gdx.files.internal("assets/sound/lose.mp3"));

        scheduleGoblinShooting();
    }

    private void createButtonTextures() {
        // Normal button (dark brown/gray)
        Pixmap buttonPixmap = new Pixmap((int)BUTTON_WIDTH, (int)BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        buttonPixmap.setColor(0.3f, 0.25f, 0.2f, 0.9f);
        buttonPixmap.fill();
        buttonPixmap.setColor(0.5f, 0.4f, 0.3f, 1f);
        buttonPixmap.drawRectangle(0, 0, (int)BUTTON_WIDTH, (int)BUTTON_HEIGHT);
        buttonPixmap.drawRectangle(1, 1, (int)BUTTON_WIDTH - 2, (int)BUTTON_HEIGHT - 2);
        buttonTexture = new Texture(buttonPixmap);
        buttonPixmap.dispose();

        // Hover button (lighter brown/gold)
        Pixmap hoverPixmap = new Pixmap((int)BUTTON_WIDTH, (int)BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        hoverPixmap.setColor(0.5f, 0.4f, 0.2f, 0.95f);
        hoverPixmap.fill();
        hoverPixmap.setColor(0.8f, 0.7f, 0.3f, 1f);
        hoverPixmap.drawRectangle(0, 0, (int)BUTTON_WIDTH, (int)BUTTON_HEIGHT);
        hoverPixmap.drawRectangle(1, 1, (int)BUTTON_WIDTH - 2, (int)BUTTON_HEIGHT - 2);
        hoverPixmap.drawRectangle(2, 2, (int)BUTTON_WIDTH - 4, (int)BUTTON_HEIGHT - 4);
        buttonHoverTexture = new Texture(hoverPixmap);
        hoverPixmap.dispose();
    }

    private void scheduleGoblinShooting() {
        if (goblinShootTask != null) {
            goblinShootTask.cancel();
        }

        int index = currentGoblinIndex;
        float delay = (index == 0) ? 2.0f : 0.5f;
        float interval = shootIntervals[index];

        goblinShootTask = new Task() {
            @Override
            public void run() {
                // ✅ Jangan tembak saat pause
                if (!gameOver && !isPaused && player.isAlive() && getCurrentGoblin().isAlive() && !goblinFireActive) {
                    goblinFire.setPosition(
                            goblin.getX(),
                            goblin.getY() + goblin.getHeight() / 2f - goblinFire.getHeight() / 2f
                    );
                    goblinFireActive = true;
                }
            }
        };

        Timer.schedule(goblinShootTask, delay, interval);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ✅ Handle pause input
        handlePauseInput();

        // ✅ Jangan update game logic saat pause
        if (!isPaused) {
            // Background scrolling
            bgOffsetX -= bgScrollSpeed * delta;
            if (bgOffsetX <= -Gdx.graphics.getWidth()) {
                bgOffsetX += Gdx.graphics.getWidth();
            }

            // Animation timers
            if (warriorIsAttacking) {
                warriorAttackTimer -= delta;
                if (warriorAttackTimer <= 0) {
                    warriorIsAttacking = false;
                    if (player.isAlive()) warrior.setRegion(warriorTex);
                }
            }

            if (warriorIsHit) {
                warriorHitTimer -= delta;
                if (warriorHitTimer <= 0) {
                    warriorIsHit = false;
                    if (player.isAlive()) warrior.setRegion(warriorTex);
                }
            }

            if (goblinIsHit) {
                goblinHitTimer -= delta;
                if (goblinHitTimer <= 0) {
                    goblinIsHit = false;
                    if (getCurrentGoblin().isAlive()) goblin.setRegion(goblinNormalTex);
                }
            }

            handleInput();
            updateFire(delta);

            if (!player.isAlive()) warrior.setRegion(warriorHurtTex);
        }

        // Render game
        batch.begin();

        // Background
        batch.draw(bg, bgOffsetX, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(bg, bgOffsetX + Gdx.graphics.getWidth(), 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Characters
        warrior.draw(batch);
        goblin.draw(batch);

        // Projectiles
        if (warriorFireActive) warriorFire.draw(batch);
        if (goblinFireActive) goblinFire.draw(batch);

        // HP Bars
        drawHpBarWithName(
                playerName.toUpperCase(),
                warrior.getX() + warrior.getWidth() / 2f - HP_BAR_WIDTH / 2f,
                warrior.getY() + warrior.getHeight() - 80,
                HP_BAR_WIDTH,
                HP_BAR_HEIGHT,
                player.getHp(),
                player.getMaxHp(),
                Color.GREEN
        );

        drawHpBarWithName(
                "GOBLIN " + (currentGoblinIndex + 1),
                goblin.getX() + goblin.getWidth() / 2f - HP_BAR_WIDTH / 2f,
                goblin.getY() + goblin.getHeight() - 30,
                HP_BAR_WIDTH,
                HP_BAR_HEIGHT,
                getCurrentGoblin().getHp(),
                getCurrentGoblin().getMaxHp(),
                Color.RED
        );

        batch.end();

        // ✅ Render Pause Menu dengan GUI
        if (isPaused && !gameOver) {
            renderPauseMenuGUI();
        }

        // Game Over Screen
        if (gameOver) {
            batch.begin();
            if (!player.isAlive()) {
                loseSprite.draw(batch);
            } else {
                winSprite.draw(batch);
            }

            batch.end();
        }
    }

    // ✅ RENDER PAUSE MENU DENGAN GUI
    private void renderPauseMenuGUI() {
        // Update hover state
        updateButtonHover();

        batch.begin();

        // Semi-transparent overlay
        batch.setColor(1, 1, 1, 0.7f);
        batch.draw(pauseOverlayTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);

        // Pause title
        pauseFont.setColor(Color.YELLOW);
        String pauseTitle = "GAME DIJEDA";
        GlyphLayout titleLayout = new GlyphLayout();
        titleLayout.setText(pauseFont, pauseTitle);
        float titleX = (Gdx.graphics.getWidth() - titleLayout.width) / 2;
        float titleY = Gdx.graphics.getHeight() / 2 + 120;
        pauseFont.draw(batch, pauseTitle, titleX, titleY);

        // Draw Resume Button
        batch.setColor(Color.WHITE);
        Texture resumeTex = (hoveredButton == 0) ? buttonHoverTexture : buttonTexture;
        batch.draw(resumeTex, resumeButton.x, resumeButton.y, resumeButton.width, resumeButton.height);

        // Draw Lobby Button
        Texture lobbyTex = (hoveredButton == 1) ? buttonHoverTexture : buttonTexture;
        batch.draw(lobbyTex, lobbyButton.x, lobbyButton.y, lobbyButton.width, lobbyButton.height);

        // Button texts
        buttonFont.setColor(hoveredButton == 0 ? Color.YELLOW : Color.WHITE);
        String resumeText = "LANJUTKAN";
        GlyphLayout resumeLayout = new GlyphLayout();
        resumeLayout.setText(buttonFont, resumeText);
        buttonFont.draw(batch, resumeText,
                resumeButton.x + (resumeButton.width - resumeLayout.width) / 2,
                resumeButton.y + (resumeButton.height + resumeLayout.height) / 2);

        buttonFont.setColor(hoveredButton == 1 ? Color.YELLOW : Color.WHITE);
        String lobbyText = "KEMBALI KE LOBBY";
        GlyphLayout lobbyLayout = new GlyphLayout();
        lobbyLayout.setText(buttonFont, lobbyText);
        buttonFont.draw(batch, lobbyText,
                lobbyButton.x + (lobbyButton.width - lobbyLayout.width) / 2,
                lobbyButton.y + (lobbyButton.height + lobbyLayout.height) / 2);

        batch.end();
    }

    // ✅ UPDATE BUTTON HOVER
    private void updateButtonHover() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Flip Y coordinate

        hoveredButton = -1;

        if (resumeButton.contains(mouseX, mouseY)) {
            hoveredButton = 0;
            if (Gdx.input.justTouched()) {
                resumeGame();
            }
        } else if (lobbyButton.contains(mouseX, mouseY)) {
            hoveredButton = 1;
            if (Gdx.input.justTouched()) {
                backToLobby();
            }
        }
    }

    // ✅ RESUME GAME
    private void resumeGame() {
        isPaused = false;
        battleMusic.play();
    }

    // ✅ BACK TO LOBBY
    private void backToLobby() {
        battleMusic.stop();
        if (goblinShootTask != null) goblinShootTask.cancel();
        game.setScreen(new LobbyScreen(game));
    }

    // ✅ HANDLE PAUSE INPUT
    private void handlePauseInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !gameOver) {
            if (isPaused) {
                resumeGame();
            } else {
                isPaused = true;
                battleMusic.pause();
            }
        }

        // Handle keyboard input saat pause
        if (isPaused && !gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                backToLobby();
            }
        }
    }

    private void drawHpBarWithName(String name, float x, float y,
                                   float width, float height,
                                   int hp, int maxHp, Color color) {
        float percent = Math.max(0, (float) hp / maxHp);
        batch.setColor(Color.DARK_GRAY);
        batch.draw(hpBarTexture, x, y, width, height);
        batch.setColor(color);
        batch.draw(hpBarTexture, x, y, width * percent, height);
        batch.setColor(Color.WHITE);
        nameFont.draw(batch, name, x + width / 2 - (name.length() * 6), y + height + 20);
    }

    private void handleInput() {
        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
                restartGame();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                backToLobby();
            }
            return;
        }

        if (!player.isAlive()) return;
        if (!getCurrentGoblin().isAlive()) return;

        // Movement
        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            warrior.setY(warrior.getY() + moveSpeed);
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            warrior.setY(Math.max(0, warrior.getY() - moveSpeed));

        // Shoot
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && !warriorFireActive) {
            float fireX = warrior.getX() + warrior.getWidth() * FIRE_OFFSET_X;
            float fireY = warrior.getY() + warrior.getHeight() * FIRE_OFFSET_Y;
            warriorFire.setPosition(fireX, fireY);

            warriorFireActive = true;
            warriorIsAttacking = true;
            warriorAttackTimer = WARRIOR_ATTACK_DURATION;
            warrior.setRegion(warriorAttackTex);
        }

        // Goblin tracking dengan frame-rate independence
        float dy = warrior.getY() - goblin.getY();
        goblin.setY(goblin.getY() + dy * GOBLIN_TRACKING_SPEED * Gdx.graphics.getDeltaTime());
    }

    private void updateFire(float delta) {
        if (warriorFireActive) {
            warriorFire.setX(warriorFire.getX() + fireSpeed);
            if (fireHits(warriorFire, goblin)) {
                player.specialAbility(getCurrentGoblin());
                warriorFireActive = false;
                goblinIsHit = true;
                goblinHitTimer = GOBLIN_HIT_DURATION;
                if (getCurrentGoblin().isAlive()) goblin.setRegion(goblinHitTex);
            }
            if (warriorFire.getX() > Gdx.graphics.getWidth()) warriorFireActive = false;
        }

        if (goblinFireActive) {
            goblinFire.setX(goblinFire.getX() - fireSpeed);
            if (fireHits(goblinFire, warrior)) {
                getCurrentGoblin().specialAbility(player);
                goblinFireActive = false;
                if (player.isAlive()) {
                    warriorIsHit = true;
                    warriorHitTimer = WARRIOR_HIT_DURATION;
                    warrior.setRegion(warriorHurtTex);
                }
            }
            if (goblinFire.getX() < 0) goblinFireActive = false;
        }

        // Cek kematian goblin
        if (!getCurrentGoblin().isAlive()) {
            if (currentGoblinIndex < goblins.size - 1) {
                currentGoblinIndex++;
                goblin.setRegion(goblinNormalTex);
                goblin.setPosition(Gdx.graphics.getWidth() - GOBLIN_X_OFFSET, GOBLIN_Y);
                goblinIsHit = false;
                goblinHitTimer = 0f;
                goblinFireActive = false;
                scheduleGoblinShooting();
            } else {
                gameOver = true;
                battleMusic.stop();
                if (!soundPlayed) {
                    winSound.play();
                    soundPlayed = true;
                }
            }
        }

        // Cek kematian player
        if (!player.isAlive()) {
            gameOver = true;
            battleMusic.stop();
            if (!soundPlayed) {
                loseSound.play();
                soundPlayed = true;
            }
        }
    }

    // ✅ Collision detection yang lebih akurat
    private boolean fireHits(Sprite fire, Sprite target) {
        return fire.getBoundingRectangle().overlaps(target.getBoundingRectangle());
    }

    private Enemy getCurrentGoblin() {
        return goblins.get(currentGoblinIndex);
    }

    private void restartGame() {
        player = new Warrior();
        currentGoblinIndex = 0;
        for (Enemy g : goblins) {
            g.setHp(g.getMaxHp() * 2);
        }

        warrior.setPosition(WARRIOR_X, WARRIOR_Y);
        goblin.setPosition(Gdx.graphics.getWidth() - GOBLIN_X_OFFSET, GOBLIN_Y);
        goblin.setRegion(goblinNormalTex);
        warriorFireActive = false;
        goblinFireActive = false;
        goblinIsHit = false;
        warriorIsAttacking = false;
        warriorIsHit = false;
        gameOver = false;
        soundPlayed = false;
        isPaused = false;

        battleMusic.play();
        warrior.setRegion(warriorTex);
        scheduleGoblinShooting();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        nameFont.dispose();
        pauseFont.dispose();
        buttonFont.dispose();
        bg.dispose();
        warriorTex.dispose();
        warriorAttackTex.dispose();
        warriorHurtTex.dispose();
        warriorDeadTex.dispose();
        goblinNormalTex.dispose();
        goblinHitTex.dispose();
        fireTex.dispose();
        warriorProjectileTex.dispose();
        hpBarTexture.dispose();
        pauseOverlayTexture.dispose();
        buttonTexture.dispose();
        buttonHoverTexture.dispose();
        winTexture.dispose();
        loseTexture.dispose();

        if (battleMusic != null) {
            battleMusic.dispose();
            battleMusic = null;
        }
        if (winSound != null) {
            winSound.dispose();
            winSound = null;
        }
        if (loseSound != null) {
            loseSound.dispose();
            loseSound = null;
        }

        if (goblinShootTask != null) {
            goblinShootTask.cancel();
        }
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {
        if (goblinShootTask != null) goblinShootTask.cancel();
    }
}