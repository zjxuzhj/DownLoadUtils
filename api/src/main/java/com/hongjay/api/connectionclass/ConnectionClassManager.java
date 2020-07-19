package com.hongjay.api.connectionclass;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Class used to calculate the approximate bandwidth of a user's connection.
 * </p>
 * <p>
 * This class notifies all subscribed {@link ConnectionClassStateChangeListener} with the new
 * ConnectionClass when the network's ConnectionClass changes.
 * </p>
 */
public class ConnectionClassManager {

    /*package*/ static final double DEFAULT_SAMPLES_TO_QUALITY_CHANGE = 5;
    private static final int BYTES_TO_BITS = 8;

    /**
     * Default values for determining quality of data connection.
     * Bandwidth numbers are in Kilobits per second (kbps).
     */
    /*package*/ static final int DEFAULT_POOR_RTT = 2000;
    /*package*/ static final int DEFAULT_MODERATE_RTT = 550;
    /*package*/ static final int DEFAULT_GOOD_RTT= 150;
    /*package*/ static final long DEFAULT_HYSTERESIS_PERCENT = 20;



    private static final double HYSTERESIS_TOP_MULTIPLIER = 100.0 / (100.0 - DEFAULT_HYSTERESIS_PERCENT);
    private static final double HYSTERESIS_BOTTOM_MULTIPLIER = (100.0 - DEFAULT_HYSTERESIS_PERCENT) / 100.0;

    /**
     * The factor used to calculate the current bandwidth
     * depending upon the previous calculated value for bandwidth.
     *
     * The smaller this value is, the less responsive to new samples the moving average becomes.
     */
    private static final double DEFAULT_DECAY_CONSTANT = 0.05;

    /** Current bandwidth of the user's connection depending upon the response. */
    private ExponentialGeometricAverage mDownloadBandwidth
            = new ExponentialGeometricAverage(DEFAULT_DECAY_CONSTANT);
    private volatile boolean mInitiateStateChange = false;
    private AtomicReference<ConnectionQuality> mCurrentBandwidthConnectionQuality =
            new AtomicReference<ConnectionQuality>(ConnectionQuality.UNKNOWN);
    private AtomicReference<ConnectionQuality> mNextBandwidthConnectionQuality;
    private ArrayList<ConnectionClassStateChangeListener> mListenerList =
            new ArrayList<ConnectionClassStateChangeListener>();
    private int mSampleCounter;



    // Singleton.
    private static class ConnectionClassManagerHolder {
        public static final ConnectionClassManager instance = new ConnectionClassManager();
    }

    /**
     * Retrieval method for the DownloadBandwidthManager singleton.
     * @return The singleton instance of DownloadBandwidthManager.
     */
    public static ConnectionClassManager getInstance() {
        return ConnectionClassManagerHolder.instance;
    }

    // Force constructor to be private.
    private ConnectionClassManager() {}

    /**
     * Adds bandwidth to the current filtered latency counter. Sends a broadcast to all
     * {@link ConnectionClassStateChangeListener} if the counter moves from one bucket
     * to another (i.e. poor bandwidth -> moderate bandwidth).
     */
    public synchronized void addRTT(long timeInMs) {

        //Ignore garbage values.
        if (timeInMs == 0 ) {
            return;
        }

        mDownloadBandwidth.addMeasurement(timeInMs);

        if (mInitiateStateChange) {
            mSampleCounter += 1;
            if (getCurrentBandwidthQuality() != mNextBandwidthConnectionQuality.get()) {
                mInitiateStateChange = false;
                mSampleCounter = 1;
            }
            if (mSampleCounter >= DEFAULT_SAMPLES_TO_QUALITY_CHANGE  && significantlyOutsideCurrentBand()) {
                mInitiateStateChange = false;
                mSampleCounter = 1;
                mCurrentBandwidthConnectionQuality.set(mNextBandwidthConnectionQuality.get());
                notifyListeners();
            }
            return;
        }

        if (mCurrentBandwidthConnectionQuality.get() != getCurrentBandwidthQuality()) {
            mInitiateStateChange = true;
            mNextBandwidthConnectionQuality = new AtomicReference<ConnectionQuality>(getCurrentBandwidthQuality());
        }
    }

    private boolean significantlyOutsideCurrentBand() {
        if (mDownloadBandwidth == null) {
            // Make Infer happy. It wouldn't make any sense to call this while mDownloadBandwidth is null.
            return false;
        }
        ConnectionQuality currentQuality = mCurrentBandwidthConnectionQuality.get();
        double bottomOfBand;
        double topOfBand;
        switch (currentQuality) {
            case POOR:
                bottomOfBand = DEFAULT_POOR_RTT;
                topOfBand = Float.MAX_VALUE;
                break;
            case MODERATE:
                bottomOfBand = DEFAULT_MODERATE_RTT;
                topOfBand = DEFAULT_POOR_RTT;
                break;
            case GOOD:
                bottomOfBand = DEFAULT_GOOD_RTT;
                topOfBand = DEFAULT_MODERATE_RTT;
                break;
            case EXCELLENT:
                bottomOfBand = 0;
                topOfBand = DEFAULT_GOOD_RTT;
                break;
            default: // If current quality is UNKNOWN, then changing is always valid.
                return true;
        }
        double average = mDownloadBandwidth.getAverage();
        if (average > topOfBand) {
            if (average > topOfBand * HYSTERESIS_TOP_MULTIPLIER) {
                return true;
            }
        } else if (average < bottomOfBand * HYSTERESIS_BOTTOM_MULTIPLIER) {

            return true;
        }
        return false;
    }

    /**
     * Resets the bandwidth average for this instance of the bandwidth manager.
     */
    public void reset() {
        if (mDownloadBandwidth != null) {
            mDownloadBandwidth.reset();
        }
        mCurrentBandwidthConnectionQuality.set(ConnectionQuality.UNKNOWN);
    }

    /**
     * Get the ConnectionQuality that the moving bandwidth average currently represents.
     * @return A ConnectionQuality representing the device's bandwidth at this exact moment.
     */
    public synchronized ConnectionQuality getCurrentBandwidthQuality() {
        if (mDownloadBandwidth == null) {
            return ConnectionQuality.UNKNOWN;
        }
        return mapBandwidthQuality(mDownloadBandwidth.getAverage());
    }

    private ConnectionQuality mapBandwidthQuality(double average) {
        if (average < 0) {
            return ConnectionQuality.UNKNOWN;
        }
        if (average > DEFAULT_POOR_RTT) {
            return ConnectionQuality.POOR;
        }
        if (average > DEFAULT_MODERATE_RTT) {
            return ConnectionQuality.MODERATE;
        }
        if (average > DEFAULT_GOOD_RTT) {
            return ConnectionQuality.GOOD;
        }
        return ConnectionQuality.EXCELLENT;
    }


    /**
     * Accessor method for the current bandwidth average.
     * @return The current bandwidth average, or -1 if no average has been recorded.
     */
    public synchronized double getDownloadKBitsPerSecond() {
        return mDownloadBandwidth == null
                ? -1.0
                : mDownloadBandwidth.getAverage();
    }


    public interface ConnectionClassStateChangeListener {

        public void onRTTStateChange(ConnectionQuality bandwidthState);
    }

    /**
     * Method for adding new listeners to this class.
     * @param listener {@link ConnectionClassStateChangeListener} to add as a listener.
     */
    public ConnectionQuality register(ConnectionClassStateChangeListener listener) {
        if (listener != null) {
            mListenerList.add(listener);
        }
        return mCurrentBandwidthConnectionQuality.get();
    }

    /**
     * Method for removing listeners from this class.
     * @param listener Reference to the {@link ConnectionClassStateChangeListener} to be removed.
     */
    public void remove(ConnectionClassStateChangeListener listener) {
        if (listener != null) {
            mListenerList.remove(listener);
        }
    }

    private void notifyListeners() {
        int size = mListenerList.size();
        for (int i = 0; i < size; i++) {
            mListenerList.get(i).onRTTStateChange(mCurrentBandwidthConnectionQuality.get());
        }
    }
}