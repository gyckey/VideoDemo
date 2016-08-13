package com.fuzhen.videodemo;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnInfoListener{

    /**
     * SD卡路径
     */
    public static String SDCARD_PATH = Environment
            .getExternalStorageDirectory().getPath();

    public static String VIDEO_PATH = "";

    /**
     * 当前播放的视频序号
     */
    public static String INDEX = "index";
    /**
     * 当前视频播放进度
     */
    public static String PLAY_POSITION = "playPosition";
    /**
     * 当前播放器播放状态
     */
    public static String IS_PLAYING = "isPlaying";

    public VideoView vVideo;

    /**
     * 视频列表数据
     */
    ArrayList<String> videos;
    /**
     * 当前播放视频的序号
     */
    int index = -1;
    /**
     * 当前播放的进度
     */
    int playPosition;

    View mediaController;
    /**
     * 播放暂停按钮
     */
    private ImageButton mPauseButton;
    /**
     * 快进按钮(未显示)
     */
    private ImageButton mFfwdButton;
    /**
     * 快退按钮(未显示)
     */
    private ImageButton mRewButton;
    /**
     * 下一个按钮
     */
    private ImageButton mNextButton;
    /**
     * 上一个按钮
     */
    private ImageButton mPrevButton;
    /**
     * 进度条
     */
    private SeekBar mSeekBar;
    /**
     * 最大化|恢复按钮
     */
    public ImageView mMinMaxButton;
    /**
     * 当前播放时间
     */
    private TextView mCurrentTime;
    /**
     * 视频时间
     */
    private TextView mEndTime;
    /**
     * 播放暂停状态
     */
    private boolean isPlaying;

    /**
     * 加载进度条
     */
    private ProgressBar mProgressBar;

    String APP_NAME= "VideoDemo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        APP_NAME = getResources().getString(
                R.string.app_name);
        VIDEO_PATH = SDCARD_PATH + "/" + APP_NAME + "/videos/";
        videoDemoCreate();
        initViews();
    }

    protected void initViews() {
        findView();
        initData();
    }

    /**
     * 初始化视图
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void findView() {

        vVideo = (VideoView) findViewById(R.id.videoview);
        vVideo.setOnPreparedListener(this);
        vVideo.setOnErrorListener(this);
        vVideo.setOnCompletionListener(this);
        vVideo.setOnInfoListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mediaController = findViewById(R.id.mediacontroller);
        mPrevButton = (ImageButton) mediaController.findViewById(R.id.prev);
        mRewButton = (ImageButton) mediaController.findViewById(R.id.rew);
        mPauseButton = (ImageButton) mediaController.findViewById(R.id.pause);
        mFfwdButton = (ImageButton) mediaController.findViewById(R.id.ffwd);
        mNextButton = (ImageButton) mediaController.findViewById(R.id.next);
        mMinMaxButton = (ImageView) mediaController.findViewById(R.id.max);
        mCurrentTime = (TextView) mediaController.findViewById(R.id.time_current);
        mEndTime = (TextView) mediaController.findViewById(R.id.time);
        mSeekBar = (SeekBar) mediaController.findViewById(R.id.mediacontroller_progress);

        mPrevButton.setOnClickListener(this);
        mFfwdButton.setOnClickListener(this);
        mPauseButton.setOnClickListener(this);
        mRewButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mMinMaxButton.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);

    }

    /**
     * 初始化数据
     *
     */
    private void initData() {
        File tmpfile = getCacheDir();
        File file= new File(tmpfile.getAbsolutePath() + "/" + APP_NAME + "/flv.mp4");
        videos = new ArrayList<>();
        videos.add(file.getAbsolutePath());
        loadVideo(0);
    }


    /**
     * 检查文件是否存在
     */
    public boolean checkData() {
        try {
            File tmpfile = getCacheDir();
            File file= new File(tmpfile.getAbsolutePath() + "/" + APP_NAME + "/");
            if(!file.exists()){
                file.mkdir();
            }
            File file2 = new File(file.getAbsolutePath() + "/flv.mp4");
            if(file2.exists()){
                file2.delete();
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建文件
     */
    public void videoDemoCreate() {
        // 创建
        if (!checkData()) {
            try {
                File tmpfile = getCacheDir();
                File file= new File(tmpfile.getAbsolutePath() + "/" + APP_NAME + "/flv.mp4");
                if(!file.exists()){
                    file.createNewFile();
                }
                InputStream Input = null;
                OutputStream Output = null;
                Input = getResources().openRawResource(R.raw.flv);
                Output = new FileOutputStream(file);
                byte[] buffer = new byte[128];
                int length;
                while ((length = Input.read(buffer)) != -1) {
                    Output.write(buffer, 0, length);
                }
                // Close the streams
                Output.flush();
                Output.close();
                Input.close();
                Input = null;
                Output = null;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载视频
     *
     * @param index 视频序号
     * @return true:加载成功|false:加载失败
     */
    private boolean loadVideo(int index) {
        if (index < 0) {
            index = 0;
            Toast.makeText(this,"没有上一个了",Toast.LENGTH_LONG).show();
            return false;
        } else if (index >= videos.size()) {
            index = videos.size() - 1;
            Toast.makeText(this,"没有下一个了",Toast.LENGTH_LONG).show();
            return false;
        }
        if (this.index != index) {
            // 切换视频
            this.index = index;
            vVideo.setVideoPath(videos.get(this.index));
            vVideo.start();
            setPausePlay(true);
            return true;
        } else {
            // 相同视频，则直接加载完毕
            vVideo.start();
            setPausePlay(true);
        }
        return false;
    }


    /**
     * 统一设置播放暂停
     *
     * @param isPlay true:播放|false:暂停
     */
    private void setPausePlay(boolean isPlay) {
        showProgress(false);
        if (isPlay) {
            // 设置播放
            if (!vVideo.isPlaying()) {
                // 当视频不在播放中，则开始播放
                vVideo.start();
            }
            // 打开计时器
            startTimer();
            mPauseButton.setImageResource(R.mipmap.mm_music_btn_pause_n);
            isPlaying = true;
        } else {
            // 设置暂停
            if (vVideo.isPlaying()) {
                // 当视频在播放中，则暂停
                vVideo.pause();
            }
            // 取消计时器
            cancelTimer();
            mPauseButton.setImageResource(R.mipmap.mm_music_btn_play_sound_n);
            isPlaying = false;
        }
    }

    private void showProgress(boolean isShow) {
        if (isShow) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pause:
                setPausePlay(!isPlaying);
                break;
            case R.id.prev:
                loadVideo(index - 1);
                break;
            case R.id.next:
                loadVideo(index + 1);
                break;
            default:
                Toast.makeText(this,R.string.development,Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        cancelTimer();
        super.onDestroy();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case 1:
                Toast.makeText(this,"无法播放此视频",Toast.LENGTH_LONG).show();
                setPausePlay(false);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // 视频播放完成后暂停
        setPausePlay(false);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPauseButton.setEnabled(true);
        mSeekBar.setEnabled(true);
        mSeekBar.setMax(vVideo.getDuration());
        mEndTime.setText(getTimeText(vVideo.getDuration()));
        if (playPosition - vVideo.getCurrentPosition() > 1000) {
            // 跳至播放进度
            vVideo.seekTo(playPosition);
            playPosition = 0;
        } else {
            setPausePlay(isPlaying);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mCurrentTime.setText(getTimeText(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelTimer();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        vVideo.seekTo(seekBar.getProgress());
    }

    /**
     * 计时器
     */
    private Timer timer;
    /**
     * 计时任务
     */
    private TimerTask task;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (timer != null) {
                mSeekBar.setProgress(vVideo.getCurrentPosition());
            }
        }
    };

    /**
     * 取消计时器，停止计时
     */
    private void cancelTimer() {
        if (handler != null) {
            handler.removeMessages(1);
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * 开始计时
     */
    private void startTimer() {

        cancelTimer();

        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        // 每秒执行一次
        timer.schedule(task, 0, 1000);
    }

    /**
     * 根据传入的毫秒数获取分秒文本
     *
     * @param ms 毫秒数
     * @return MM:ss
     */
    private String getTimeText(int ms) {
        ms = ms + 500;
        int time = ms / 1000;
        String ss = String.format("%02d", time % 60);
        time = time / 60;
        String mm = String.format("%02d", time % 60);
        return mm + ":" + ss;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                showProgress(true);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                // 进度缓冲结束后，设置播放状态，如果此时暂停则暂停视频
                setPausePlay(isPlaying);
                break;
        }
        return false;
    }
}
