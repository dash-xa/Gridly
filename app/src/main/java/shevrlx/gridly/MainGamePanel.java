package shevrlx.gridly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;

import static android.media.AudioAttributes.USAGE_GAME;

/**
 * Author: Alex S.
 * Date: July 29, 2017
 */

public class MainGamePanel extends SurfaceView implements SurfaceHolder.Callback {

    /* Variables */
    private final MainThread thread; // thread where drawing will be done
    private Canvas canvas; // canvas to draw on
    private Paint paint;
    private SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

    private SoundPool soundPool = null; // plays media
    private AudioManager mAudioManager = null; // handles audio focus
    private final int soundID;

    private ArrayList<Dot> dots = new ArrayList<>(); // contains all the dots
    private ArrayList<Line> lines = new ArrayList<>(); // contains lines
    private int dotsTouched = 0; // how many dots player has touched
    private Dot lastDotClicked = null; // dot last touched by user
    private long lastSpawn = 0; // spawn time of the most recent dot

    private boolean screenTouched = false; // whether screen is currently being touched by the user or not

    private float currentX, currentY; // current location of user's touch

    private Drawable resumeButton = null; // button to resume game when paused
    private PauseButton pauseButton = null; // button to pause game when running

    private ArrayList<Line> removeLines = new ArrayList<>(); // contains lines that are out of bounds
    private ArrayList<Dot> removeDots = new ArrayList<>();  // contains dots that are out of bounds

    private double dy = SPEED; // speed of dots
    private int spawnTime = SPAWN_INTERVAL;
    private boolean gamePaused = false;

    private long deltaPause = 0; // used for pause screen
    private int updates = 0, frames = 0;

    private InterstitialAd mInterstitialAd;

    // Constants
    private static final String TAG = MainGamePanel.class.getSimpleName();

    public static final int SCREEN_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
    public static final int SCREEN_WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;

    public static final int COLOR = Color.rgb(20, 20, 20); // default game color

    private static final int SPAWN_INTERVAL = 400; // how fast the dots will spawn
    private static final int SPEED = (int) (getScreenHeight() * 0.01); // default speed
    private static final int DELTA = SPEED / 6;// how fast speed and spawn interval will change

    // constructor
    public MainGamePanel(Context context) {
        super(context);
        // add callback(this) to the surface holder to intercept events
        getHolder().addCallback(this);
        // create game loop thread
        thread = new MainThread(getHolder(), this);
        // make the game panel focusable so it can handle events
        setFocusable(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        gamePaused = false;
        setDefVarValues(context); // renew all variable values
        dy = SPEED;
        spawnTime = SPAWN_INTERVAL;
        // sound
        soundPool = makeSoundPool();
        soundID = soundPool.load(context, R.raw.click, 1);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    // shutting down the thread
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try shut down thread again
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                final float eX = event.getX();
                final float eY = event.getY();
                if (gamePaused) {
                    // handle pause input
                    if (resumeButton.isClicked(eX, eY)) {
                        gamePaused = false;
                        lastSpawn = System.currentTimeMillis() - deltaPause;
                        paint.setTextSize(160);
                    }
                } else {
                    if (pauseButton.isClicked(eX, eY)) {
                        gamePaused = true;
                        deltaPause = System.currentTimeMillis() - lastSpawn;
                    }
                    currentX = eX;
                    currentY = eY;
                    screenTouched = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // draw line from lastDotClicked to mX, mY
                // update location of the user's press
                if (!gamePaused) {
                    currentX = event.getX();
                    currentY = event.getY();
                    screenTouched = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                screenTouched = false;
                break;
        }
        return true;
    }

    private void connectDots(Dot currentDot) {
        // draw line and do other stuff
        Dot[] dotsToAdd = new Dot[2];
        currentDot.setTouched(true);

        if (lastDotClicked == null) {
            dotsToAdd[0] = currentDot;
            dotsToAdd[1] = currentDot;
        } else {
            dotsToAdd[0] = currentDot;
            dotsToAdd[1] = lastDotClicked;
        }

        lines.add(new Line(dotsToAdd, paint));

        // tell dots that a line has been added to them
        if (lastDotClicked != null)
            lastDotClicked.setTouched(true);
        dotsTouched++;

        // speed it up
        if (dotsTouched % 7 == 0) {
            spawnTime = (int) ((double) spawnTime / (1 + DELTA / dy));
            dy += DELTA;
        }
        // update the last dot
        lastDotClicked = currentDot;
        lastDotClicked.setTouched(true);
    }

    // updates position of entities
    public void update() {
        // don't move anything is game is paused
        if (gamePaused) return;
        // remove dots and lines that are out of bounds
        for (Line l : lines) {
            if (l.getStartingDot().getY() > SCREEN_HEIGHT * 2 && l.getEndDot().getY() > SCREEN_HEIGHT * 2) {
                removeLine(l);
                removeDot(l.getStartingDot());
            }
        }

        // remove all dots
        dots.removeAll(removeDots);
        lines.removeAll(removeLines);
        removeDots.clear();
        removeLines.clear();

        // if enough time has passed generate dot
        if (System.currentTimeMillis() - lastSpawn > spawnTime) {
            dots.add(Dot.generateRandomDot(getContext()));
            lastSpawn = System.currentTimeMillis();
        }

        // check if user lost
        for (Dot d : dots)
            if (d.isOutOfBounds()) gameLost();

        if (screenTouched) {
            // check if user has clicked a dot
            for (Dot currentDot : dots)
                if (currentDot.isClicked(currentX, currentY) && !currentDot.isTouched()) {
                    currentDot.wasClicked();
                    if (sharedPreferences.getBoolean("sound", true))
                        soundPool.play(soundID, 1, 1, 0, 0, 1);
                    connectDots(currentDot);
                    lastDotClicked = currentDot;
                }
        }
        // move dots down
        for (Dot dot : dots)
            dot.update((float) dy);
        updates++;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        paint.setTextSize(160);
        this.canvas = canvas;

        // set bckgr color
        setCanvasColor(R.color.colorPrimary);

        // draw mouse on screen line
        paint.setColor(Color.argb(150, 255, 255, 255));
        if (screenTouched && lastDotClicked != null) {
            int centerOffset = lastDotClicked.getBitmap().getWidth() / 2;
            canvas.drawLine((float) (lastDotClicked.getX() + centerOffset), (float) (lastDotClicked.getY() + centerOffset), (float) (currentX + centerOffset), (float) (currentY + centerOffset), paint);
        }
        paint.setColor(Color.argb(125, 255, 255, 255));

        // draws all the lines
        for (Line line : lines) line.draw(canvas);
        // draw all the dots
        for (Dot d : dots) d.draw(canvas);

        // draw text
        paint.setColor(Color.WHITE);
        canvas.drawText(dotsTouched + "", (float) (SCREEN_WIDTH * 0.45), (float) (SCREEN_HEIGHT * 0.10), paint);
        paint.setColor(Color.GRAY);

        // draw pause button
        if (!gamePaused) pauseButton.draw(canvas);

        // pause screen stuff
        if (gamePaused) {
            // draw tint screen
            int colorCode = Color.argb(100, 50, 50, 50);
            canvas.drawColor(colorCode);
            resumeButton.draw(canvas);
        }

        frames++;
    }

    private void gameLost() {
        // game has been lost
        // make intent with current score data
        Intent intent = new Intent(getContext(), endGameActivity.class);
        // create the bundle
        Bundle bundle = new Bundle();
        // add data to bundle
        bundle.putInt("score", dotsTouched);
        // add bundle to the intent
        intent.putExtras(bundle);
        // start intent
        getContext().startActivity(intent);
        thread.setRunning(false);
    }

    private void setDefVarValues(Context context) {
        // Initialize and create buttons
        Bitmap pauseButtonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause);
        pauseButton = new PauseButton(pauseButtonBitmap, (float) (SCREEN_WIDTH - pauseButtonBitmap.getWidth() - SCREEN_WIDTH * 0.03), (float) (SCREEN_HEIGHT * 0.02));
        Bitmap resumeButtonBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.play);
        resumeButton = new Drawable(resumeButtonBitmap, SCREEN_WIDTH / 2 - resumeButtonBitmap.getWidth() / 2, SCREEN_HEIGHT / 2 - resumeButtonBitmap.getWidth() / 2);
        // Paint properties
        paint = new Paint();
        paint.setTextSize(160);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(25);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void shutDownThread() {
        gamePaused = true;
        thread.setRunning(false);
    }

    private void setCanvasColor(int color) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            canvas.drawColor(getResources().getColor(color, null));
        else
            canvas.drawColor(getResources().getColor(color));
    }

    private SoundPool makeSoundPool() {
        // adds support for api levels < 21
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            // Do something for lollipop and above versions
            return new SoundPool.Builder().setMaxStreams(4)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .build();
        else
            // do something for phones running an SDK before lollipop
            return new SoundPool(4, AudioManager.STREAM_SYSTEM, 0);
    }

    private void removeLine(Line l) {
        removeLines.add(l);
    }

    private void removeDot(Dot l) {
        removeDots.add(l);
    }

    public static int getScreenHeight() {
        return SCREEN_HEIGHT;
    }

    public static int getScreenWidth() {
        return SCREEN_WIDTH;
    }
}
