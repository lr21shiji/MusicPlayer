package com.attackt.music;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {
    TextView play;
    TextView show;
    List<MusicData> list = new ArrayList<>();

    private MusicService.MusicPlaybackLocalBinder mMusicServiceBinder = null;
    private MusicData mPlayingTrack = null;


    /**
     * 与Service连接时交互的类
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            mMusicServiceBinder = (MusicService.MusicPlaybackLocalBinder) service;
            mMusicServiceBinder.registerOnPlaybackStateChangeListener(mOnPlaybackStateChangeListener);
        }

        // 与服务端连接异常丢失时才调用，调用unBindService不调用此方法哎
        public void onServiceDisconnected(ComponentName className) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list.clear();
        MusicData bean = new MusicData();
        bean.setM4a("http://ws.stream.qqmusic.qq.com/1412315.m4a?fromtag=46");
        bean.setSongid(1);
        bean.setSongname("包容");
        bean.setDuration(265000);
        MusicData bean1 = new MusicData();
        bean1.setSongid(2);
        bean1.setM4a("http://ws.stream.qqmusic.qq.com/172019.m4a?fromtag=46");
        bean1.setSongname("变了散了算了");
        bean1.setDuration(328000);
        MusicData bean2 = new MusicData();
        bean2.setSongid(3);
        bean2.setM4a("http://ws.stream.qqmusic.qq.com/100856.m4a?fromtag=46");
        bean2.setSongname("我不后悔");
        bean2.setDuration(251000);
        list.add(bean);
        list.add(bean1);
        list.add(bean2);

        play = (TextView) findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicServiceBinder != null) {
                    mMusicServiceBinder.setCurrentPlayList(list);
                }
                Intent mIntent = new Intent();
                mIntent.setAction(MusicService.ACTION_PLAY);
                mIntent.putExtra(Constant.REQUEST_PLAY_ID, list.get(0).getSongid());
                mIntent.putExtra(Constant.CLICK_ITEM_IN_LIST, true);
                mIntent.setPackage(getPackageName());
                startService(mIntent);

            }
        });

        show = (TextView) findViewById(R.id.show);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PlayActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.bindService(new Intent(this, MusicService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unbindService(mServiceConnection);
        mMusicServiceBinder.unregisterOnPlaybackStateChangeListener(mOnPlaybackStateChangeListener);
        mMusicServiceBinder = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayingTrack = null;
    }

    private OnPlaybackStateChangeListener mOnPlaybackStateChangeListener = new OnPlaybackStateChangeListener() {

        @Override
        public void onMusicPlayed() {

        }

        @Override
        public void onMusicPaused() {

        }

        @Override
        public void onMusicStopped() {

        }

        @Override
        public void onPlayNewSong(MusicData playingSong) {
            mPlayingTrack = playingSong;
            Intent intent = new Intent(MainActivity.this, PlayActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("song", mPlayingTrack);
            bundle.putParcelableArrayList("musicList", (ArrayList<MusicData>) list);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        @Override
        public void onPlayProgressUpdate(int currentMillis) {

        }

    };

}
