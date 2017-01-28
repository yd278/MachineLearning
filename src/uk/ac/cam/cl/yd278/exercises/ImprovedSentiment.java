package uk.ac.cam.cl.yd278.exercises;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Sentiment;

/**
 * Created by Anchor on 2017/1/20.
 */
public class ImprovedSentiment {
    private Sentiment mSentiment;
    private boolean isStrong;
    public ImprovedSentiment(boolean strong, Sentiment s){
        isStrong = strong;
        mSentiment = s;
    }
    public void setSentiment(Sentiment s){
        mSentiment = s;
    }
    public boolean strong() {
        return isStrong;
    }
    public Sentiment getSentiment(){
        return mSentiment;
    }
}
