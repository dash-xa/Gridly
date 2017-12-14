package shevrlx.gridly;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.constraint.solver.widgets.Rectangle;

/**
 * Created by alex on 5/23/2017.
 */

public class Button {
    private Rectangle rect;
    private double x, y;
    private double endX, endY;
    private Paint paint;
    private String text;
    private boolean isDrawn = false;

    public Button(String text, Paint paint, double x, double y, double endX, double endY) {
        this.rect = rect;
        this.x = x;
        this.y = y;
        this.endX = endX;
        this.endY = endY;
        this.paint = paint;
        this.text = text;
    }

    public boolean isClicked(double mX, double mY) {
        return (isDrawn && mX >= this.x && mX < endX && mY >= this.y && mY < endY) ? true : false;
    }

    public void draw(Canvas canvas) {
        paint.setTextSize(100);
        paint.setARGB(100, 0, 0, 0);
        canvas.drawRect((float) x, (float) y, (float) endX, (float) endY, paint);
        paint.setARGB(225, 250, 250, 250);
        canvas.drawText(text, (float) (x + MainGamePanel.SCREEN_WIDTH * 0.05), (float) (y + (endY - y) * 0.6), paint);
        isDrawn = true;
    }

    public boolean isDrawn() {
        return isDrawn;
    }

    public Rectangle getRect() {
        return rect;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }
}
