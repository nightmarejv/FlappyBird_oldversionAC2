package com.gameandroid.flappygame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
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

public class Jogo extends ApplicationAdapter {

    // Texturas
    private SpriteBatch batch;
    private Texture[] passaros;
    private Texture fundo;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture gameOver;

    // Colliders
    private ShapeRenderer shapeRenderer;
    private Circle circuloPassaro;
    private Rectangle retanguloCanoCima;
    private Rectangle retanguloCanoBaixo;

    // Configurações
    private float larguraDispositivo;
    private float alturaDispositivo;
    private float variacao = 0;
    private float gravidade=2;
    private float posicaoInicialVerticalPassaro=0;
    private float posicaoCanoHorizontal;
    private float posicaoCanoVertical;
    private float espacoEntreCanos;
    private Random random;
    private int pontos=0;
    private int pontuacaoMaxima=0;
    private boolean passouCano=false;
    private int estadoJogo = 0;
    private float posicaoHorizontalPassaro = 0;

    // Textos
    BitmapFont textoPontuacao;
    BitmapFont textoReiniciar;
    BitmapFont textoMelhorPontuacao;

    // Sons
    Sound somVoando;
    Sound somColisao;
    Sound somPontuacao;
    Sound somJogo;

    // Salva pontuação
    Preferences preferencias;

    // Cria um objeto para a camera
    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 720;
    private final float VIRTUAL_HEIGHT = 1280;

    @Override
    public void create () {
        inicializarTexturas();
        inicializaObjetos();
    }

    @Override
    public void render () {

        // Otimiza limpando frames
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

        verificarEstadoJogo();

        validarPontos();

        desenharTexturas();

        detectarColisoes();


    }

    /*
     * 0 - Jogo inicial, passaro parado
	 * 1 - Começa o jogo
	 * 2 - Colidiu
	 *
    */
    private void verificarEstadoJogo(){

        boolean toqueTela = Gdx.input.justTouched();




        if( estadoJogo == 0 ){

            /* Faz um evento na tela ao tocar */
            if( toqueTela ){
                gravidade = -15;
                estadoJogo = 1;
                somVoando.play();
                somJogo.loop(1,1,0);

            }



        }else if( estadoJogo == 1 ){
            /* Faz um evento na tela ao tocar */
            if( toqueTela ){
                gravidade = -15;
                somVoando.play();

            }

            /* Faz o cano se mover */
            posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 600;
            if( posicaoCanoHorizontal < -canoTopo.getWidth() ){
                posicaoCanoHorizontal = larguraDispositivo;
                posicaoCanoVertical = random.nextInt(400) - 200;
                passouCano = false;
            }

            /* Adciona gravidade no passaro */
            if( posicaoInicialVerticalPassaro > 0 || toqueTela )
                posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

            gravidade++;


        }else if( estadoJogo == 2 ){

            //
            if( pontos > pontuacaoMaxima ){
                pontuacaoMaxima = pontos;
                preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
                preferencias.flush();
            }

            posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime()*500;

            /* Aplica evento de toque na tela */
            if( toqueTela ){
                estadoJogo = 0;
                pontos = 0;
                gravidade = 0;
                posicaoHorizontalPassaro = 0;
                posicaoInicialVerticalPassaro = alturaDispositivo / 2;
                posicaoCanoHorizontal = larguraDispositivo;
            }

        }

    }

    private void detectarColisoes(){

        circuloPassaro.set(
                50 + posicaoHorizontalPassaro + passaros[0].getWidth() / 2 ,posicaoInicialVerticalPassaro + passaros[0].getHeight()/2,passaros[0].getWidth()/2
        );

        retanguloCanoBaixo.set(
                posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical,
                canoBaixo.getWidth(), canoBaixo.getHeight()
        );
        retanguloCanoCima.set(
                posicaoCanoHorizontal,alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
                canoTopo.getWidth(), canoTopo.getHeight()
        );

        boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
        boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);

        if( colidiuCanoCima || colidiuCanoBaixo ){
            if( estadoJogo == 1 ){
                somColisao.play();
                somJogo.stop();//Som
                estadoJogo = 2;
            }

        }


    }

    private void desenharTexturas(){

        batch.setProjectionMatrix( camera.combined );

        batch.begin();

        batch.draw(fundo,0,0,larguraDispositivo, alturaDispositivo);
        batch.draw( passaros[ (int) variacao] ,50 + posicaoHorizontalPassaro,posicaoInicialVerticalPassaro);
        batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical);
        batch.draw(canoTopo, posicaoCanoHorizontal,alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical );
        textoPontuacao.draw(batch, String.valueOf(pontos),larguraDispositivo/2, alturaDispositivo -110 );

        if( estadoJogo == 2 ){
            batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth()/2, alturaDispositivo / 2 );
            textoReiniciar.draw(batch, "Toque para reiniciar!", larguraDispositivo/2 -140, alturaDispositivo/2 - gameOver.getHeight()/2 );
            textoMelhorPontuacao.draw(batch, "Seu record é: "+ pontuacaoMaxima +" pontos", larguraDispositivo/2 -140,alturaDispositivo/2 - gameOver.getHeight());
        }

        batch.end();

    }

    public void validarPontos(){

        if( posicaoCanoHorizontal < 50-passaros[0].getWidth() ){//Detecta se passou pelo passaro
            if(!passouCano){
                pontos++;
                passouCano = true;
                somPontuacao.play();
            }
        }

        variacao += Gdx.graphics.getDeltaTime() * 10;
        /* numero do bater de assas do passro*/
        if (variacao > 3 )
            variacao = 0;

    }

    private void inicializarTexturas(){
        passaros = new Texture[3];
        passaros[0] = new Texture("robot1.png");
        passaros[1] = new Texture("robot2.png");
        passaros[2] = new Texture("robot3.png");

        fundo = new Texture("city.jpg");
        canoBaixo = new Texture("cano_superior.png");
        canoTopo = new Texture("cano_inferior.png");
        gameOver = new Texture("game_over.png");

    }

    private void inicializaObjetos(){

        batch = new SpriteBatch();
        random = new Random();

        larguraDispositivo = VIRTUAL_WIDTH;
        alturaDispositivo = VIRTUAL_HEIGHT;
        posicaoInicialVerticalPassaro = alturaDispositivo / 2;
        posicaoCanoHorizontal = larguraDispositivo;
        espacoEntreCanos = 350;

        //Escala, fonte e cor do texto
        textoPontuacao = new BitmapFont();
        textoPontuacao.setColor(Color.WHITE);
        textoPontuacao.getData().setScale(10);

        textoReiniciar = new BitmapFont();
        textoReiniciar.setColor(Color.GREEN);
        textoReiniciar.getData().setScale(2);

        textoMelhorPontuacao = new BitmapFont();
        textoMelhorPontuacao.setColor(Color.YELLOW);
        textoMelhorPontuacao.getData().setScale(2);

        //objetos para as colisoes;
        shapeRenderer = new ShapeRenderer();
        circuloPassaro = new Circle();
        retanguloCanoBaixo = new Rectangle();
        retanguloCanoCima = new Rectangle();

        //Inicializa sons
        somVoando = Gdx.audio.newSound( Gdx.files.internal("som_nave.wav") );
        somColisao = Gdx.audio.newSound( Gdx.files.internal("som_batida.wav") );
        somPontuacao = Gdx.audio.newSound( Gdx.files.internal("som_pontos.wav") );
        somJogo = Gdx.audio.newSound( Gdx.files.internal("som_jogo.wav") );


        //Cria uma preferências dos objetos
        preferencias = Gdx.app.getPreferences("flappyBird");
        pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima",0);

        //Configura a posição da camera
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose () {

    }
}
