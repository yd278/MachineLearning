package uk.ac.cam.cl.yd278.exercises;

/**
 * Created by Anchor on 2017/1/23.
 */
public class Word {
    private int mPositiveCount = 0;
    private int mNegativeCount = 0;

    public int getPositiveCount() {
        return mPositiveCount;
    }

    public int getNegativeCount() {
        return mNegativeCount;
    }

    public void plusOnePositive() {
        mPositiveCount++;
    }

    public void plusOneNegative() {
        mNegativeCount++;
    }
}
