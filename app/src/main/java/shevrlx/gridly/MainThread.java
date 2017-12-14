package shevrlx.gridly;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * Created by alex on 4/29/2017.
 */

public class MainThread extends Thread {
    private boolean running;
    private long lastUpdateTime;
    private static final double UPDATE_INTERVAL = 1_000_000_000D / 60D;
    private SurfaceHolder surfaceHolder;
    private MainGamePanel gamePanel;
    private double interpolation = 0;
    private static final int TICKS_PER_SECOND = 25;
    private static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
    private static final int MAX_FRAMESKIP = 5;
    private static final int FPS = 60;

    private static final String TAG = MainThread.class.getSimpleName();

    public MainThread(SurfaceHolder surfaceHolder, MainGamePanel gamePanel) {
        super();

        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;

    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        Canvas canvas;
        Log.d(TAG, "Starting game loop");
        int updates = 0, frames = 0;
        long lastTimer = System.currentTimeMillis(); // used for counting frames

        long targetTime = 1000 / FPS;

        long startTime, waitTime, timeMillis; // used in loop
        while (running) {
            startTime = System.nanoTime();
            canvas = null;
            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {

                    // update game state
                    this.gamePanel.update();

                    // draws canvas on the panel
                    this.gamePanel.draw(canvas);
                    frames++;

                    // log frames and ticks(updates)
                    if (System.currentTimeMillis() - lastTimer >= 1000) {
                        lastTimer = System.currentTimeMillis();
                        Log.d("STATS", "ticks: " + updates + " frames: " + frames);
                        frames = 0;
                        updates = 0;
                    }
                }
            } finally {
                // in case of an exception the surface is not left in an inconsistent state
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            } // finally
            timeMillis = (System.nanoTime() - startTime) / 1_000_000;
            waitTime = targetTime - timeMillis;
            try {
                if (waitTime > 0) this.sleep(waitTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
