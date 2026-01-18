package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.MyGame;
import com.badlogic.gdx.audio.Music;


public class LobbyScreen implements Screen {
    private Music lobbyMusic;

    private MyGame game;
    private SpriteBatch batch;
    private BitmapFont font;

    private Texture background;
    private Texture guideImage;

    private int selectedOption = 0;
    private String[] options = {"Start Game", "Panduan Bermain", "Exit Game"};
    private boolean showGuide = false;

    // ✅ TAMBAHAN INPUT NAMA
    private boolean enteringName = false;
    private String nameInput = "";

    private GlyphLayout layout = new GlyphLayout();

    public LobbyScreen(MyGame game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(3f);

        lobbyMusic = Gdx.audio.newMusic(
                Gdx.files.internal("assets/lobby_musik/backsound-lobby_w2gj63fT.mp3")
        );
        lobbyMusic.setLooping(true);
        lobbyMusic.setVolume(0.6f);
        lobbyMusic.play();


        background = new Texture("assets/Lobby.jpeg");
        guideImage = new Texture("assets/Panduan.jpeg");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // ================= INPUT NAMA =================
        if (enteringName) {
            font.setColor(Color.CYAN);
            font.draw(batch, "Masukkan Nama Pemain:",
                    Gdx.graphics.getWidth() / 2f - 280,
                    Gdx.graphics.getHeight() / 2f + 80);

            font.setColor(Color.YELLOW);
            font.draw(batch, nameInput + "_",
                    Gdx.graphics.getWidth() / 2f - 280,
                    Gdx.graphics.getHeight() / 2f);

            font.setColor(Color.WHITE);
            batch.end();
            return;
        }

        // ================= MENU UTAMA =================
        if (!showGuide) {
            // ✅ JUDUL DENGAN WARNA EMAS DAN POSISI TETAP
            String title = "Simulasi Game Strategi PBO";
            layout.setText(font, title);
            font.setColor(Color.BLACK);
            font.draw(batch, title,
                    (Gdx.graphics.getWidth() - layout.width) / 2f + 2,
                    Gdx.graphics.getHeight() - 150 + 2);

            font.setColor(Color.CORAL);
            font.draw(batch, title,
                    (Gdx.graphics.getWidth() - layout.width) / 2f,
                    Gdx.graphics.getHeight() - 150);

            // ✅ MENU DENGAN JARAK LEBIH JAUH & WARNA PUTIH
            float startY = Gdx.graphics.getHeight() - 280; // ⬇️ turunkan menu lebih jauh
            for (int i = 0; i < options.length; i++) {
                font.setColor(i == selectedOption ? Color.YELLOW : Color.WHITE);
                layout.setText(font, options[i]);
                font.draw(batch, options[i],
                        (Gdx.graphics.getWidth() - layout.width) / 2f,
                        startY - i * 80);
            }

        } else {
            // === TAMPILKAN GAMBAR PANDUAN DI ATAS BACKGROUND LOBBY ===

            // ⚙️ Atur ukuran gambar: maksimal 80% lebar & tinggi layar
            float maxWidth = Gdx.graphics.getWidth() * 0.8f;
            float maxHeight = Gdx.graphics.getHeight() * 0.8f;

            float imgWidth = guideImage.getWidth();
            float imgHeight = guideImage.getHeight();

            float scaleX = maxWidth / imgWidth;
            float scaleY = maxHeight / imgHeight;
            float scale = Math.min(scaleX, scaleY);

            float scaledWidth = imgWidth * scale;
            float scaledHeight = imgHeight * scale;

            float imgX = (Gdx.graphics.getWidth() - scaledWidth) / 2f;
            float imgY = (Gdx.graphics.getHeight() - scaledHeight) / 2f;

            batch.draw(guideImage, imgX, imgY, scaledWidth, scaledHeight);

            // Instruksi kecil di bawah
            font.setColor(Color.SKY);
            font.getData().setScale(2.0f);
            String instruction = "Tekan ESC untuk kembali";
            layout.setText(font, instruction);
            float instrX = (Gdx.graphics.getWidth() - layout.width) / 2f;
            font.draw(batch, instruction, instrX, 80);

            font.getData().setScale(3f);
            font.setColor(Color.WHITE);
        }
        batch.end();
    }

    private void handleInput() {

        // ===== MODE INPUT NAMA =====
        if (enteringName) {
            for (int key = Input.Keys.A; key <= Input.Keys.Z; key++) {
                if (Gdx.input.isKeyJustPressed(key)) {
                    nameInput += Input.Keys.toString(key);
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && nameInput.length() > 0) {
                nameInput = nameInput.substring(0, nameInput.length() - 1);
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && !nameInput.isEmpty()) {
                game.setPlayerName(nameInput);
                lobbyMusic.stop(); // ⬅ WAJIB
                game.setScreen(new BattleScreen(game));
                enteringName = false;
            }

            return;
        }

        // ===== MENU NORMAL =====
        if (showGuide) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                showGuide = false;
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP))
                selectedOption = (selectedOption - 1 + options.length) % options.length;

            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN))
                selectedOption = (selectedOption + 1) % options.length;

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (selectedOption == 0) enteringName = true;
                else if (selectedOption == 1) showGuide = true;
                else Gdx.app.exit();
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        background.dispose();
        guideImage.dispose();
        lobbyMusic.dispose(); // ⬅ TAMBAHKAN
    }


    @Override public void show() {}
    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
