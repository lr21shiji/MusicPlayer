package com.attackt.music;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    TextView close;
    ImageView play;
    RelativeLayout back;
    RelativeLayout next;
    SeekBar seekbar;
    TextView current;
    TextView total;
    TextView name;
    MusicData music;

    private MusicService.MusicPlaybackLocalBinder mMusicServiceBinder = null;
    private boolean mIsPlay = false;
    private MusicData mPlaySong = null;
    private int currentTime;

    /**
     * 与Service连接时交互的类
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            // 保持对Service的Binder引用，以便调用Service提供给客户端的方法
            mMusicServiceBinder = (MusicService.MusicPlaybackLocalBinder) service;

            // 传递OnPlaybackStateChangeListener对象给Service，以便音乐回放状态发生变化时通知本Activity
            mMusicServiceBinder.registerOnPlaybackStateChangeListener(mOnPlaybackStateChangeListener);

            initCurrentPlayInfo(mMusicServiceBinder.getCurrentPlayInfo());
        }

        // 与服务端连接异常丢失时才调用，调用unBindService不调用此方法哎
        public void onServiceDisconnected(ComponentName className) {
            if (mMusicServiceBinder != null) {
                mMusicServiceBinder.unregisterOnPlaybackStateChangeListener(mOnPlaybackStateChangeListener);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_play);
        Window win = this.getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);
        win.setGravity(Gravity.TOP);

        close = (TextView) findViewById(R.id.close);
        play = (ImageView) findViewById(R.id.play);
        back = (RelativeLayout) findViewById(R.id.back);
        next = (RelativeLayout) findViewById(R.id.next);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        current = (TextView) findViewById(R.id.current);
        total = (TextView) findViewById(R.id.total);
        name = (TextView) findViewById(R.id.name);
        seekbar.setOnSeekBarChangeListener(this);
        play.setOnClickListener(this);
        close.setOnClickListener(this);
        next.setOnClickListener(this);
        back.setOnClickListener(this);
        current.setText(MediaUtils.formatTime(0));
        if (getIntent().getExtras() != null) {
            music = getIntent().getExtras().getParcelable("song");
            total.setText(MediaUtils.formatTime(music.getDuration()));
            name.setText(music.getSongname());
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        bindService(new Intent(this, MusicService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        // 本界面不可见时取消绑定服务，服务端无需发送消息过来，不可见时无需更新界面
        unbindService(mServiceConnection);
        if (mMusicServiceBinder != null) {
            mMusicServiceBinder.unregisterOnPlaybackStateChangeListener(mOnPlaybackStateChangeListener);
            mMusicServiceBinder = null;
        }
    }

    /**
     * 初始化当前播放信息
     */
    private void initCurrentPlayInfo(Bundle bundle) {
        int currentPlayerState = bundle.getInt(Constant.PLAYING_STATE);
        int currenPlayPosition = bundle.getInt(Constant.CURRENT_PLAY_POSITION);
        MusicData playingSong = bundle.getParcelable(Constant.PLAYING_MUSIC_ITEM);

        // 根据播放状态，设置播放按钮的图片
        if (currentPlayerState == MusicService.State.Playing
                || currentPlayerState == MusicService.State.Preparing) {
            mIsPlay = true;
            play.setImageResource(R.mipmap.music_pause);
        } else {
            mIsPlay = false;
            play.setImageResource(R.mipmap.play);
        }

        // 设置歌曲标题、时长、当前播放时间、当前播放进度
        mPlaySong = playingSong;
        if (playingSong != null) {
            total.setText(MediaUtils.formatTime(playingSong.getDuration()));
            name.setText(playingSong.getSongname());
            current.setText(MediaUtils.formatTime(currenPlayPosition));
            seekbar.setProgress(currenPlayPosition * seekbar.getMax() / (int) playingSong.getDuration());
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (mPlaySong != null) {
                current.setText(MediaUtils.formatTime(progress * mPlaySong.getDuration() / seekBar.getMax()));
            } else {
                current.setText(MediaUtils.formatTime(progress * music.getDuration() / seekBar.getMax()));
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // 拖动播放进度条后发送消息给服务端，指示从指定进度开始播放
        if (mMusicServiceBinder != null) {
            if (mPlaySong != null) {
                mMusicServiceBinder.seekToSpecifiedPosition(seekBar.getProgress() * (int) mPlaySong.getDuration() / seekBar.getMax());
            } else {
                mMusicServiceBinder.seekToSpecifiedPosition(seekBar.getProgress() * (int) music.getDuration() / seekBar.getMax());
            }

        }
    }

    @Override
    public void onClick(View v) {
        Intent mIntent;
        switch (v.getId()) {
            case R.id.play:
                if (mIsPlay) {
                    mIntent = new Intent();
                    mIntent.setAction(MusicService.ACTION_PAUSE);
                    mIntent.setPackage(getPackageName());
                    startService(mIntent);
                } else {
                    mIntent = new Intent();
                    mIntent.setAction(MusicService.ACTION_PLAY);
                    mIntent.setPackage(getPackageName());
                    startService(mIntent);
                }
                break;
            case R.id.close:
                mIntent = new Intent();
                mIntent.setAction(MusicService.ACTION_STOP);
                mIntent.setPackage(getPackageName());
                startService(mIntent);
                finish();
                stopService(new Intent(PlayActivity.this, FloatViewService.class));
                MyApplication.show = false;
                break;
            case R.id.next:
                mIntent = new Intent();
                mIntent.setAction(MusicService.ACTION_NEXT);
                mIntent.setPackage(getPackageName());
                startService(mIntent);
                break;
            case R.id.back:
                if (currentTime < 30000) {
                    mMusicServiceBinder.seekToSpecifiedPosition(0);
                } else {
                    mMusicServiceBinder.seekToSpecifiedPosition(currentTime - 30000);
                }
                break;
        }
    }

    private OnPlaybackStateChangeListener mOnPlaybackStateChangeListener = new OnPlaybackStateChangeListener() {

        @Override
        public void onMusicPlayed() {
            // 音乐播放时，播放按钮设置为暂停的图标（意为点击暂停）
            mIsPlay = true;
            play.setImageResource(R.mipmap.music_pause);
        }

        @Override
        public void onMusicPaused() {
            // 音乐暂停时，播放按钮设置为播放的图标（意为点击播放）
            mIsPlay = false;
            play.setImageResource(R.mipmap.play);
        }

        @Override
        public void onMusicStopped() {
            // 音乐播放停止时，清空歌曲信息的显示
            mIsPlay = false;
            play.setImageResource(R.mipmap.play);
            total.setText(MediaUtils.formatTime(0));
            name.setText("");
            current.setText(MediaUtils.formatTime(0));
            seekbar.setProgress(0);
            mPlaySong = null;
        }

        @Override
        public void onPlayNewSong(MusicData playingSong) {
            // 播放新的歌曲时，更新显示的歌曲信息
            mPlaySong = playingSong;
            if (playingSong != null) {
                total.setText(MediaUtils.formatTime(playingSong.getDuration()));
                name.setText(playingSong.getSongname());
                current.setText(MediaUtils.formatTime(0));
                seekbar.setProgress(0);
            }

        }

        @Override
        public void onPlayProgressUpdate(int currentMillis) {
            // 更新当前播放时间
            currentTime = currentMillis;
            current.setText(MediaUtils.formatTime(currentMillis));
            if (mPlaySong != null) {
                // 更新当前播放进度
                seekbar.setProgress(currentMillis * seekbar.getMax() / (int) mPlaySong.getDuration());
            } else {
                seekbar.setProgress(currentMillis * seekbar.getMax() / (int) music.getDuration());
            }

        }
    };

}
