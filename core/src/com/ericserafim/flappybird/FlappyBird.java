package com.ericserafim.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture[] passaros;
    private Texture fundo;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture gameOver;
    private BitmapFont fonte;
    private BitmapFont mensagem;

    private Circle passarioCirculo;
    private Rectangle canoTopoRetangulo;
    private Rectangle canoBaixoRetangulo;
//    private ShapeRenderer shape;

    private float alturaDevice;
    private float larguraDevice;
    private int estadoJogo = 0; //0=Jogo não iniciado 1=Jogo iniciado 2=Game Over
    private int pontuacao = 0;

    private float variacao = 0;
    private float velocidadeQueda = 0;
    private float posicaoVertical;
    private float posicaoMovimentoCanoHorizontal;
    private float espacoEntreCanos;
    private float deltaTime;
    private Random numeroRandomico;
    private float alturaEntreCanosRadomica;
    private int posicaoHorizontalPassaro;
    private Boolean marcouPonto = false;

    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 768;
    private final float VIRTUAL_HEIGHT = 1024;

    @Override
	public void create () {
		batch = new SpriteBatch();
        numeroRandomico = new Random();
        passarioCirculo = new Circle();
        canoTopoRetangulo = new Rectangle();
        canoBaixoRetangulo = new Rectangle();
//        shape = new ShapeRenderer();

        fundo = new Texture("fundo.png");
        canoBaixo = new Texture("cano_baixo.png");
        canoTopo = new Texture("cano_topo.png");
        gameOver = new Texture("game_over.png");
        fonte = new BitmapFont();
        fonte.setColor(Color.WHITE);
        fonte.getData().setScale(6);

        mensagem = new BitmapFont();
        mensagem.setColor(Color.WHITE);
        mensagem.getData().setScale(3);

        passaros = new Texture[3];
        passaros[0] = new Texture("passaro1.png");
        passaros[1] = new Texture("passaro2.png");
        passaros[2] = new Texture("passaro3.png");

        //Configurações da camera
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        alturaDevice = VIRTUAL_HEIGHT;
        larguraDevice = VIRTUAL_WIDTH;
        posicaoVertical = alturaDevice / 2;
        posicaoMovimentoCanoHorizontal = larguraDevice;
        espacoEntreCanos = 300;
        posicaoHorizontalPassaro = 120;
	}

	@Override
	public void render () {
        camera.update();

        //Limpar frames para liberar memoria
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        deltaTime = Gdx.graphics.getDeltaTime();
        variacao += deltaTime * 10;
        if (variacao > 2) variacao = 0;

        if (estadoJogo == 0) { // Jogo não iniciado
            if (Gdx.input.justTouched()) {
                estadoJogo =1;
            }
        } else {
            velocidadeQueda++;
            if (posicaoVertical > 0 || velocidadeQueda < 0)
                posicaoVertical -= velocidadeQueda;

            if (estadoJogo == 1) {//Jogo Iniciado
                posicaoMovimentoCanoHorizontal -= deltaTime * 200;

                if (Gdx.input.justTouched()) {
                    velocidadeQueda = -15;
                }

                if (posicaoMovimentoCanoHorizontal < -canoBaixo.getWidth()) {
                    posicaoMovimentoCanoHorizontal = larguraDevice;
                    alturaEntreCanosRadomica = numeroRandomico.nextInt(400) - 200;
                    marcouPonto = false;
                }

                //Verifica pontuacao
                if (posicaoMovimentoCanoHorizontal < posicaoHorizontalPassaro) {
                    if (!marcouPonto) {
                        pontuacao++;
                        marcouPonto = true;
                    }
                }
            } else { //Game Over
                if (Gdx.input.justTouched()) {
                    estadoJogo = 0;
                    pontuacao = 0;
                    velocidadeQueda = 0;
                    posicaoVertical = alturaDevice / 2;
                    posicaoMovimentoCanoHorizontal = larguraDevice;
                }
            }
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(fundo, 0, 0, larguraDevice, alturaDevice);
        batch.draw(canoTopo, posicaoMovimentoCanoHorizontal, alturaDevice / 2 + espacoEntreCanos / 2 + alturaEntreCanosRadomica);
        batch.draw(canoBaixo, posicaoMovimentoCanoHorizontal, alturaDevice / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosRadomica);
        batch.draw(passaros[(int) variacao], posicaoHorizontalPassaro, posicaoVertical);
        fonte.draw(batch, String.valueOf(pontuacao), larguraDevice / 2, alturaDevice - 50);

        if (estadoJogo == 2) {
            batch.draw(gameOver, larguraDevice / 2 - gameOver.getWidth() / 2, alturaDevice / 2);
            mensagem.draw(batch, "Toque para reiniciar", larguraDevice / 2 - 200 , alturaDevice / 2 - gameOver.getHeight() / 2);
        }

        batch.end();

        passarioCirculo.set(posicaoHorizontalPassaro + passaros[0].getWidth() / 2, posicaoVertical + passaros[0].getHeight() / 2, passaros[0].getWidth() / 2);
        canoTopoRetangulo.set(
                posicaoMovimentoCanoHorizontal, alturaDevice / 2 + espacoEntreCanos / 2 + alturaEntreCanosRadomica,
                canoTopo.getWidth(), canoTopo.getHeight()
        );

        canoBaixoRetangulo.set(
                posicaoMovimentoCanoHorizontal, alturaDevice / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosRadomica,
                canoBaixo.getWidth(), canoBaixo.getHeight()
        );

        //Desenhas formas
        /*
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.circle(passarioCirculo.x, passarioCirculo.y, passarioCirculo.radius);
        shape.rect(canoTopoRetangulo.x, canoTopoRetangulo.y, canoTopoRetangulo.getWidth(), canoTopoRetangulo.getHeight());
        shape.rect(canoBaixoRetangulo.x, canoBaixoRetangulo.y, canoBaixoRetangulo.getWidth(), canoBaixoRetangulo.getHeight());
        shape.setColor(Color.RED);
        shape.end();
        */

        if (Intersector.overlaps(passarioCirculo, canoBaixoRetangulo) ||
                Intersector.overlaps(passarioCirculo, canoTopoRetangulo) ||
                posicaoVertical <= 0 || posicaoVertical >= alturaDevice) {
            estadoJogo = 2;
        }
	}

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }
}
