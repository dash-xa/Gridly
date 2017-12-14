package shevrlx.gridly;

import android.content.Context;
import android.graphics.BitmapFactory;

/**
 * Created by alex on 4/30/2017.
 */

public class Dot extends Drawable{
    private boolean touched = false;
    private static final int DOT = R.drawable.dot;
    private static final int DOT_SELECTED = R.drawable.dot2;
    private Context context;
    private boolean isTouched = false;

    public Dot(Context context, float x, float y) {
        //makes bitmap and calls superclass
        super(BitmapFactory.decodeResource(context.getResources(), DOT), x, y);
        this.context = context;
    }
    public Dot(Context context) {
        //makes bitmap and calls superclass
        super(BitmapFactory.decodeResource(context.getResources(), DOT), 0, 0);
    }
    // moves down the dot when called
    public void update(float delta) {
        this.y += delta;
    }
    @Override
    public boolean isClicked(double mX, double mY) {
        if(touched) return false;
        return super.isClicked(mX, mY);
    }
    public void wasClicked() {
        bitmap = BitmapFactory.decodeResource(context.getResources(), DOT_SELECTED);
    }
    public boolean isOutOfBounds() {
        return this.getY() > MainGamePanel.SCREEN_HEIGHT && !this.isTouched();
    }
    public static Dot generateRandomDot(Context context) {
        Dot d = new Dot(context, 0, 0);
        // probably better way to do this
        float dotX = (float) Math.floor(Math.random() * (MainGamePanel.getScreenWidth()-d.getBitmap().getWidth())); // random x location within screen
        return new Dot(context, dotX, 0-d.getBitmap().getHeight());
    }
    public boolean isTouched() {
        return touched;
    }
    public void setTouched(boolean touched) {
        this.touched = touched;
    }
}