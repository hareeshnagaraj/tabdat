package thproject.test.com.myapplication;

/**
 * Created by hareeshnagaraj on 7/25/14.
 */

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Detects left and right swipes across a view.
 */
public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private final View cardView;

    public OnSwipeTouchListener(Context context, View view) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        this.cardView = view;
    }

    public void onSwipeLeft() {
    }

    public void onSwipeLeft(View v) {

    }

    public void onSwipeRight() {
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                View v = null;
                if (distanceX > 0)
                    onSwipeRight();
                else
                    onSwipeLeft(cardView);
                return true;
            }
            return false;
        }
    }
}