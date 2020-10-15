package com.reelvideos.app.recorder.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reelvideos.app.recorder.adapter.ThumbRecyclerAdapter;
import com.reelvideos.app.recorder.editor.PlayerListener;
import com.reelvideos.app.recorder.entity.MediaItem;

import java.util.List;

public class ThumbLineBar extends FrameLayout {

    private static String TAG = ThumbLineBar.class.getName();
    private static final int WHAT_THUMBNAIL_VIEW_AUTO_MOVE = 1;
    private static final int WHAT_TIMELINE_ON_SEEK = 2;
    private static final int WHAT_TIMELINE_FINISH_SEEK = 3;

    private static final String KEY_RATE = "rate";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_NEED_CALLBACK = "need_callback";

    protected RecyclerView mRecyclerView;
    protected ThumbLineConfig mThumbLineConfig;
    protected long mCurrDuration = 0;
    private final Object mCurrDurationLock = new Object();
    private OnBarSeekListener mBarSeekListener;
    protected PlayerListener mLinePlayer;
    private PlayThread mPlayThread;
    private boolean mIsTouching = false;
    private float mErrorDis;
    protected float mCurrScroll;
    protected int mScrollState;
    protected long mDuration;

    /**
     * The width of the entire timeline View (number of thumbnails * width of a single thumbnail)
     */
    protected float mTimelineBarViewWidth;

    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            long duration = msg.getData().getLong(KEY_DURATION);
            switch (msg.what) {
            case WHAT_THUMBNAIL_VIEW_AUTO_MOVE:
                float rate = msg.getData().getFloat(KEY_RATE);
                boolean needCallback = msg.getData().getBoolean(KEY_NEED_CALLBACK);
                if (mBarSeekListener != null && needCallback && !mIsTouching) {
                    mBarSeekListener.onThumbLineBarSeek(duration);
                }
                scroll(rate);
                mLinePlayer.updateDuration(duration);
                break;
            case WHAT_TIMELINE_ON_SEEK:
                mBarSeekListener.onThumbLineBarSeek(duration);
                break;
            case WHAT_TIMELINE_FINISH_SEEK:
                mBarSeekListener.onThumbLineBarSeekFinish(duration);
                break;
            default:
                break;

            }
        }
    };
    private int mIndicatorMargin;
    private ThumbRecyclerAdapter mThumbRecyclerAdapter;
    private OnOperationEndListener mOnOperationEndListener;

    public ThumbLineBar(@NonNull Context context) {
        this(context, null);
    }

    public ThumbLineBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbLineBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * Initialize ui
     */
    private void initView() {
        int indicatorWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
        mIndicatorMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, getResources().getDisplayMetrics());
        mRecyclerView = new RecyclerView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                               ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, mIndicatorMargin, 0, mIndicatorMargin);
        mRecyclerView.setLayoutParams(params);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        View indicator = new View(getContext());
        setIndicatorViewLayoutParams(indicator, indicatorWidth, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER, 0xFFFCC937);

        addView(mRecyclerView);
        addView(indicator);

    }

    @SuppressLint("ClickableViewAccessibility")
    public void setup(List<MediaItem> medias, ThumbLineConfig thumbLineConfig, OnBarSeekListener barSeekListener, PlayerListener linePlayer) {
        mThumbLineConfig = thumbLineConfig;
        initLayoutParams();
        mDuration = linePlayer.getDuration();
        if (mBarSeekListener == null) {
            setOnBarSeekListener(barSeekListener);
            setThumbLinePlayer(linePlayer);
            mRecyclerView.setOnTouchListener(new OnTouchListener() {
                @Override
                @SuppressWarnings("unchecked")
                public boolean onTouch(View v, MotionEvent event) {
                    int actionMasked = MotionEventCompat.getActionMasked(event);
                    switch (actionMasked) {
                    case MotionEvent.ACTION_DOWN:
                        mIsTouching = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mIsTouching = false;
                        if (mOnOperationEndListener != null) {
                            mOnOperationEndListener.onEnd();
                        }
                        break;
                    default:
                        break;
                    }
                    return false;
                }
            });

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        Message msg = mUIHandler.obtainMessage(WHAT_TIMELINE_FINISH_SEEK);
                        Bundle data = new Bundle();
                        data.putLong(KEY_DURATION, mCurrDuration);
                        msg.setData(data);
                        mUIHandler.sendMessage(msg);
                        mLinePlayer.updateDuration(mCurrDuration);
                        Log.d(TAG, "ScrollStateChanged SCROLL_STATE_IDLE");
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        Log.d(TAG, "ScrollStateChanged SCROLL_STATE_DRAGGING");
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        Log.d(TAG, "ScrollStateChanged SCROLL_STATE_SETTLING");
                        break;
                    default:
                        break;
                    }

                    onRecyclerViewScrollStateChanged(newState);
                    mScrollState = newState;
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    mCurrScroll += dx;
                    //fixed the scroll rate to a constant to avoid the app crash condition -- StarLord
                    float rate = (float) 1.0;
                    //float rate = mCurrScroll / getTimelineBarViewWidth();
                    long duration = (long)(rate * mDuration);
                    if (mBarSeekListener != null && (mIsTouching
                                                     || mScrollState == RecyclerView.SCROLL_STATE_SETTLING)) {
                        Message msg = mUIHandler.obtainMessage(WHAT_TIMELINE_ON_SEEK);
                        Bundle data = new Bundle();
                        data.putLong(KEY_DURATION, duration);
                        msg.setData(data);
                        mUIHandler.sendMessage(msg);
                    }
                    mCurrDuration = duration;
                    mLinePlayer.updateDuration(duration);
                    onRecyclerViewScroll(dx, dy);

                }
            });
        }

        if (mThumbRecyclerAdapter == null) {
            mThumbRecyclerAdapter = new ThumbRecyclerAdapter(getContext(), medias, mThumbLineConfig.getThumbnailCount(),
                    (int)mLinePlayer.getDuration(),
                    mThumbLineConfig.getScreenWidth(),
                    mThumbLineConfig.getThumbnailPoint().x, mThumbLineConfig.getThumbnailPoint().y);
            mRecyclerView.setAdapter(mThumbRecyclerAdapter);
            mThumbRecyclerAdapter.cacheBitmaps();

        } else {
            mThumbRecyclerAdapter.setData(mThumbLineConfig.getThumbnailCount(),
                                          (int)mLinePlayer.getDuration());
            mThumbRecyclerAdapter.notifyDataSetChanged();
        }

        //The playback time is displayed through the thumbnail bar callback
        restart();
    }

    //Initialize layout parameters ->Dynamically adapt thumbnail size
    private void initLayoutParams() {

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = mThumbLineConfig.getThumbnailPoint().y + 2 * mIndicatorMargin;

    }

    /**
     * recyclerView Scroll callback，Subclass can realize expansion function
     */
    protected void onRecyclerViewScroll(int dx, int dy) {
        //empty realize
    }

    /**
     * recyclerView Scroll state change，Subclass can realize expansion function
     *
     * @param newState int
     */
    protected void onRecyclerViewScrollStateChanged(int newState) {
        //empty realize
    }

    private void scroll(float rate) {
        float scrollBy = rate * getTimelineBarViewWidth() - mCurrScroll;
        if (mErrorDis >= 1) {
            scrollBy += 1;
            mErrorDis -= 1;
        }
        mErrorDis = scrollBy - (int)scrollBy;
        mRecyclerView.scrollBy((int)scrollBy, 0);
    }

    /**
     * Indicator line and background layoutParams with background Color setting
     *
     * @param view    Target View
     * @param width   width
     * @param height  high
     * @param gravity Center of gravity
     * @param color   colour
     */
    private void setIndicatorViewLayoutParams(View view, int width, int height, int gravity, int color) {
        LayoutParams params = new LayoutParams(width, height, gravity);
        view.setLayoutParams(params);
        view.setBackgroundColor(color);

    }

    public void seekTo(long duration, boolean needCallback) {
        synchronized (mCurrDurationLock) {
            mCurrDuration = duration;
        }
        if (duration == 0) {
            Log.d(TAG, "duration  == 0");
        }
        float rate = duration * 1.0f / mDuration;
        Message msg = mUIHandler.obtainMessage(WHAT_THUMBNAIL_VIEW_AUTO_MOVE);
        Bundle data = new Bundle();
        data.putFloat(KEY_RATE, rate);
        data.putLong(KEY_DURATION, duration);
        data.putBoolean(KEY_NEED_CALLBACK, needCallback);
        msg.setData(data);
        mUIHandler.sendMessage(msg);
    }

    /**
     * Get the total width of the thumbnail
     *
     * @return int
     */
    public float getTimelineBarViewWidth() {
        if (mRecyclerView.getAdapter() == null) {
            return 0;
        }
        if (mTimelineBarViewWidth == 0) {
            this.mTimelineBarViewWidth = mThumbLineConfig.getThumbnailCount() * mThumbLineConfig.getThumbnailPoint().x;
        }
        return mTimelineBarViewWidth;
    }

    /**
     * Whether the thumbnail bar is scrolling
     * @return boolean
     */
    public boolean isScrolling() {
        return mScrollState != RecyclerView.SCROLL_STATE_IDLE;
    }

    /**
     * User swipe thumbLineBar Time monitoring
     *
     * @param barSeekListener Listener
     */
    private void setOnBarSeekListener(OnBarSeekListener barSeekListener) {
        mBarSeekListener = barSeekListener;
    }

    /**
     * Set playback time synchronization player
     *
     * @param linePlayer Synchronization interface
     */
    private void setThumbLinePlayer(PlayerListener linePlayer) {
        mLinePlayer = linePlayer;
    }

    /**
     * Whether the thumbnail bar is being operated
     * @return boolean
     */
    public boolean isTouching() {
        return mIsTouching;
    }

    public void setOperationEndListener(OnOperationEndListener listener) {
        this.mOnOperationEndListener = listener;
    }

    /**
     * hide
     */
    public void hide() {
        this.setVisibility(GONE);
    }

    /**
     * Start showing
     */
    public void show() {
        if (mPlayThread != null && mPlayThread.mState != PlayThread.STATE_PLAYING) {
            //Thumbs are not aligned when paused
            seekTo(mLinePlayer.getCurrentDuration(), false);
        }
        setVisibility(VISIBLE);
    }

    public void start() {
        Log.d(TAG, "-------------- start --------------");

        mPlayThread = new PlayThread();
        mPlayThread.startPlaying();
    }

    public void resume() {
        Log.d(TAG, "-------------- resume --------------");
        if (mPlayThread != null) {
            mPlayThread.resumePlaying();
        }
    }

    public void pause() {
        Log.d(TAG, "-------------- pause --------------");
        if (mPlayThread != null) {
            mPlayThread.pause();
        }
    }

    public void stop() {
        Log.d(TAG, "-------------- stop --------------");
        if (mPlayThread != null) {
            mPlayThread.stopPlaying();
            mPlayThread = null;
        }
    }

    public void restart() {
        Log.d(TAG, "-------------- restart --------------");
        if (mPlayThread != null && mPlayThread.isAlive()) {
            mPlayThread.mLastDuration = -1;
            mPlayThread.resumePlaying();
        } else {
            start();
        }
    }
    private final Object mStateLock = new Object();

    protected class PlayThread extends Thread {
        private static final byte STATE_PLAYING = 1;
        private static final byte STATE_PAUSING = 2;
        private static final byte STATE_STOPPING = 3;

        private long mLastDuration = -1;
        private volatile byte mState = STATE_STOPPING;


        @Override
        public void run() {
            super.run();

            synchronized (mStateLock) {
                mState = STATE_PLAYING;
                mLastDuration = -1;
            }

            while (true) {
                synchronized (mStateLock) {
                    if (mState == STATE_PAUSING) {
                        try {
                            //Log.d(TAG, "TimelineBar pausing");
                            mStateLock.wait();
                            Log.d(TAG, "TimelineBar resuming");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (mState == STATE_STOPPING) {
                        mCurrDuration = 0;
                        break;
                    }
                }
                synchronized (mCurrDurationLock) {
                    mCurrDuration = mLinePlayer.getCurrentDuration();
                }
                if (mCurrDuration != mLastDuration) {
                    seekTo(mCurrDuration, false);
                    mLastDuration = mCurrDuration;
                }
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void startPlaying() {
            this.start();
        }

        private void resumePlaying() {
            synchronized (mStateLock) {
                mState = STATE_PLAYING;
                mStateLock.notify();
            }
        }

        public void pause() {
            synchronized (mStateLock) {
                //avoid pause with stop Caused anr
                if (mState == STATE_PLAYING) {
                    mState = STATE_PAUSING;
                }
            }
        }

        private void stopPlaying() {
            synchronized (mStateLock) {
                mState = STATE_STOPPING;
                mStateLock.notify();
            }
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mCurrDuration = 0;
        }

    }

    /**
     * User swipe thumbLineBar Listener
     */
    public interface OnBarSeekListener {

        /**
         * Sliding
         *
         * @param duration time
         */
        void onThumbLineBarSeek(long duration);

        /**
         * Slide completed (Used when inertial sliding occurs during fast sliding)
         *
         * @param duration time
         */
        void onThumbLineBarSeekFinish(long duration);
    }

    /**
     * Operation monitoring of thumbnails
     */
    public interface OnOperationEndListener {
        void onEnd();
    }
}
