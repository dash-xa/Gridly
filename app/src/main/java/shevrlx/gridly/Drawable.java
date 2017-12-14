package shevrlx.gridly;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by alex on 4/30/2017.
 */

public class Drawable {
    protected Bitmap bitmap;
    protected float x, y;

    public Drawable(Bitmap bitmap, float x, float y) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
    }

    // moves down the dot when called
    public void update() {}

    public final Bitmap getBitmap() {
        return bitmap;
    }

    public final void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    /*
        public boolean isTouched() {
            return touched;
        }

        public void setTouched(boolean touched) {
            this.touched = touched;
        }*/
    public boolean isClicked(double mX, double mY) {
        if (mX >= this.x && mX < this.x + bitmap.getWidth() && mY >= this.y && mY < this.y + bitmap.getHeight()) {
            return true;
        } else {
            return false;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, (float) this.x, (float) this.y, null);
    }

}
