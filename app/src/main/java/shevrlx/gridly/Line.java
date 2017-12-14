package shevrlx.gridly;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by alex on 5/5/2017.
 */

public class Line {

    private static Paint paint = new Paint();
    private Dot startingDot = null;
    private Dot endDot = null;

    public Line(Dot[] dots, Paint paint) {
        this.startingDot = dots[0];
        this.endDot = dots[1];
        this.paint = paint;
//        paint.setColor(Color.BLACK);
//        paint.setStrokeWidth(25);
    }

    // draws line from one dot to other
    public void draw(Canvas canvas) {
        double centerOffset = startingDot.getBitmap().getWidth()/2;
        canvas.drawLine((float)(startingDot.getX() + centerOffset), (float)(startingDot.getY() + centerOffset), (float)(endDot.getX() + centerOffset), (float)(endDot.getY() + centerOffset), paint);

    }

    public Dot getStartingDot() {
        return startingDot;
    }

    public void setStartingDot(Dot startingDot) {
        this.startingDot = startingDot;
    }

    public Dot getEndDot() {
        return endDot;
    }

    public void setEndDot(Dot endDot) {
        this.endDot = endDot;
    }
}
