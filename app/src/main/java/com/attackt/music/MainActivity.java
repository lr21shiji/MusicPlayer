package com.attackt.music;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.attackt.music.rom.HuaweiUtils;
import com.attackt.music.rom.MeizuUtils;
import com.attackt.music.rom.MiuiUtils;
import com.attackt.music.rom.QikuUtils;
import com.attackt.music.rom.RomUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static android.util.Log.getStackTraceString;

public class MainActivity extends BaseActivity {
    TextView play;
    List<MusicData> list = new ArrayList<>();
    private Dialog dialog;

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
        bean.setDuration(26500000);
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

                if (checkPermission(MainActivity.this)) {
                    if (mMusicServiceBinder != null) {
                        mMusicServiceBinder.setCurrentPlayList(list);
                    }
                    Intent mIntent = new Intent();
                    mIntent.setAction(MusicService.ACTION_PLAY);
                    mIntent.putExtra(Constant.REQUEST_PLAY_ID, list.get(0).getSongid());
                    mIntent.putExtra(Constant.CLICK_ITEM_IN_LIST, true);
                    mIntent.setPackage(getPackageName());
                    startService(mIntent);
                    startService(new Intent(MainActivity.this, FloatViewService.class));
                    MyApplication.show = true;
                    play.setVisibility(View.GONE);
                } else {
                    applyPermission(MainActivity.this);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        this.bindService(new Intent(this, MusicService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyApplication.show) {
            play.setVisibility(View.GONE);
        } else {
            play.setVisibility(View.VISIBLE);
        }
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

    private boolean checkPermission(Context context) {
        //6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                return miuiPermissionCheck(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                return meizuPermissionCheck(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                return huaweiPermissionCheck(context);
            } else if (RomUtils.checkIs360Rom()) {
                return qikuPermissionCheck(context);
            }
        }
        return commonROMPermissionCheck(context);
    }

    private boolean huaweiPermissionCheck(Context context) {
        return HuaweiUtils.checkFloatWindowPermission(context);
    }

    private boolean miuiPermissionCheck(Context context) {
        return MiuiUtils.checkFloatWindowPermission(context);
    }

    private boolean meizuPermissionCheck(Context context) {
        return MeizuUtils.checkFloatWindowPermission(context);
    }

    private boolean qikuPermissionCheck(Context context) {
        return QikuUtils.checkFloatWindowPermission(context);
    }

    private boolean commonROMPermissionCheck(Context context) {
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context);
        } else {
            Boolean result = true;
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    Class clazz = Settings.class;
                    Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                    result = (Boolean) canDrawOverlays.invoke(null, context);
                } catch (Exception e) {
                    getStackTraceString(e);
                }
            }
            return result;
        }
    }

    private void applyPermission(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                miuiROMPermissionApply(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                meizuROMPermissionApply(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                huaweiROMPermissionApply(context);
            } else if (RomUtils.checkIs360Rom()) {
                ROM360PermissionApply(context);
            }
        }
        commonROMPermissionApply(context);
    }

    private void ROM360PermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    QikuUtils.applyPermission(context);
                } else {
                    Toast.makeText(MainActivity.this, "权限申请取消", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void huaweiROMPermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    HuaweiUtils.applyPermission(context);
                } else {
                    Toast.makeText(MainActivity.this, "权限申请取消", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void meizuROMPermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    MeizuUtils.applyPermission(context);
                } else {
                    Toast.makeText(MainActivity.this, "权限申请取消", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void miuiROMPermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    MiuiUtils.applyMiuiPermission(context);
                } else {
                    Toast.makeText(MainActivity.this, "权限申请取消", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 通用 rom 权限申请
     */
    private void commonROMPermissionApply(final Context context) {
        //这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            meizuROMPermissionApply(context);
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                showConfirmDialog(context, new OnConfirmResult() {
                    @Override
                    public void confirmResult(boolean confirm) {
                        if (confirm) {
                            try {
                                Class clazz = Settings.class;
                                Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");

                                Intent intent = new Intent(field.get(null).toString());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setData(Uri.parse("package:" + context.getPackageName()));
                                context.startActivity(intent);
                            } catch (Exception e) {
                                getStackTraceString(e);
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "权限申请取消", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    private void showConfirmDialog(Context context, OnConfirmResult result) {
        showConfirmDialog(context, "您的手机没有授予悬浮窗权限，请开启后再试", result);
    }

    private void showConfirmDialog(Context context, String message, final OnConfirmResult result) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new AlertDialog.Builder(context).setCancelable(true).setTitle("")
                .setMessage(message)
                .setPositiveButton("现在开启",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirmResult(true);
                                dialog.dismiss();
                            }
                        }).setNegativeButton("暂不开启",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirmResult(false);
                                dialog.dismiss();
                            }
                        }).create();
        dialog.show();
    }

    private interface OnConfirmResult {
        void confirmResult(boolean confirm);
    }
}
