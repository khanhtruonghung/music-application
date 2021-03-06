package com.example.truongkhanh.ofmusicapp.Activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.truongkhanh.ofmusicapp.Adapter.multiFragmentViewPagerAdapter;
import com.example.truongkhanh.ofmusicapp.Fragment.Component_Fragment.ListSongInMediaPlayerFragment;
import com.example.truongkhanh.ofmusicapp.Fragment.Component_Fragment.MyDialogFragment;
import com.example.truongkhanh.ofmusicapp.Model.Song;
import com.example.truongkhanh.ofmusicapp.R;
import com.example.truongkhanh.ofmusicapp.Service.MusicService;

import java.util.ArrayList;

public class MediaPlayerActivity extends AppCompatActivity {

    //ImageView imageViewSong;
    public static Button btnStart, btnBack, btnForward, btnLike, btnMoreOption, btnRandom, btnRepeat;
    public static SeekBar seekBar;
    Toolbar toolbarMediaPlayer;
    ListSongInMediaPlayerFragment listSongInMediaPlayerFragment;
    public static multiFragmentViewPagerAdapter multiFragmentViewPagerAdapter;
    ViewPager viewPager;
    public static ArrayList<Song> songArrayList;
    int songPosition;
    public static final Handler mSeekbarUpdateHandler = new Handler();
    final Handler mWaitServiceReadyHandler = new Handler();
    public static Runnable mUpdateSeekbar;
    private boolean OnlineSong;
    Runnable mWaitServiceReady;

    private Intent musicIntent;
    private MusicService musicService;
    private boolean musicBound = false;

    public MediaPlayerActivity(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        Mapping();
        GetData();
        AddNavBar();

        MainActivity.floatingActionButton.show();
    }

    @Override
    public void onResume(){
        super.onResume();
        // Nếu musicService được chạy rồi thì ta không cần khởi tạo lại SeekBarListener và TimeSong
        if (musicBound==false) {
            mWaitServiceReady = new Runnable() {
                @Override
                public void run() {
                    if (musicBound == true) {
                        createSeekBarListener();
                        TimeSong();
                    } else {
                        mWaitServiceReadyHandler.postDelayed(this, 1000);
                    }
                }
            };
            mWaitServiceReadyHandler.postDelayed(mWaitServiceReady, 50);
        }

        if(musicIntent==null) {
            musicIntent = new Intent(this, MusicService.class);
            bindService(musicIntent, MusicConnection, BIND_AUTO_CREATE);
        }
    }

    // On Destroy Activity
    @Override
    public void onDestroy(){
        // onDestroy we unbind Music Service and remove Runnable mUpdateSeekbar
        super.onDestroy();
        unbindService(MusicConnection);
        mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
    }

    // Avoid Destroy Activity
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void TimeSong() {
        seekBar.setMax(musicService.timeSong());
        Log.d("SeekBarMax", String.valueOf(seekBar.getMax()));
        mUpdateSeekbar = new Runnable() {
            @Override
            public void run() {
                if(seekBar.getMax() != musicService.timeSong()){
                    seekBar.setMax(musicService.timeSong());
                }
                if(musicService.getmMediaPlayer() != null) {
                    int progress = musicService.currentPosition();
                    seekBar.setProgress(progress);
                    mSeekbarUpdateHandler.postDelayed(this, 1000);
                }
            }
        };
        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 50);
        mWaitServiceReadyHandler.removeCallbacks(mWaitServiceReady);
    }

    private void createSeekBarListener(){
        // SeekBar Listener when user change seekbar progress
        // Set time song to play in that seekbar progress
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(musicService.getmMediaPlayer() != null && fromUser) {
                    musicService.setProgressMediaPlayer(seekBar.getProgress());
                }
                UpdateUI();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    // Thêm Navigation Bar vào activity
    private void AddNavBar() {
        setActionBar(toolbarMediaPlayer);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Song song = songArrayList.get(songPosition);
        getActionBar().setTitle(song.getNameSong());
        toolbarMediaPlayer.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbarMediaPlayer.setTitleTextColor(Color.WHITE);
    }

    // Mapping component bên UI
    private void Mapping() {
        btnStart = (Button) findViewById(R.id.Button_Play);
        btnBack = (Button) findViewById(R.id.Button_Back);
        btnForward = (Button) findViewById(R.id.Button_Forward);
        btnLike = (Button) findViewById(R.id.Button_star);
        btnMoreOption = (Button) findViewById(R.id.Button_More_Option);
        btnRandom = (Button) findViewById(R.id.Button_Random);
        btnRepeat = (Button) findViewById(R.id.Button_Repeat);
        seekBar = (SeekBar) findViewById(R.id.SeekBar_Media_Player);
        toolbarMediaPlayer = (Toolbar) findViewById(R.id.Toolbar_Media_Player);
        viewPager = findViewById(R.id.ViewPager_Media_Player);

        multiFragmentViewPagerAdapter = new multiFragmentViewPagerAdapter(getSupportFragmentManager());
        listSongInMediaPlayerFragment = new ListSongInMediaPlayerFragment();
        multiFragmentViewPagerAdapter.AddFragment(listSongInMediaPlayerFragment);
        viewPager.setAdapter(multiFragmentViewPagerAdapter);

        // Change Theme SeekBar
        seekBar.getProgressDrawable().setColorFilter(
                getResources().getColor(R.color.MainColor), PorterDuff.Mode.MULTIPLY);
    }

    // Lấy dữ liệu truyền vào activity, sử dụng intent
    private void GetData() {
        Intent intent = getIntent();
        if(intent.hasExtra("PlaySong")) {
            songArrayList = intent.getParcelableArrayListExtra("PlaySong");
        }
        if(intent.hasExtra("SongPosition")){
            songPosition = intent.getIntExtra("SongPosition", 0);
        }
        if(intent.hasExtra("TopSong")){
            songArrayList = intent.getParcelableArrayListExtra("TopSong");
        }
        if(intent.hasExtra("OnlineSong")){
            OnlineSong = intent.getBooleanExtra("OnlineSong", false);
        }
    }

    public void UpdateUI() {
        songPosition = musicService.getSongPosition();
        Song song = songArrayList.get(songPosition);
        String nameSong = song.getNameSong();
        getActionBar().setTitle(nameSong);
    }

    public void onClickForward(View view) {
        if(musicService.getmRepeat()) {
            musicService.playNext();
        } else {
            musicService.setmRepeat(true);
            musicService.playNext();
            musicService.setmRepeat(false);
        }
    }

    public void onClickBack(View view) {
            musicService.playBack();
    }

    public void onClickPlay(View view) {
//        musicService.pausePlaySong();
        if(musicService.getmMediaPlayer().isPlaying()){
            musicService.pauseSong();
            btnStart.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24px);
        } else {
            musicService.playSong();
            btnStart.setBackgroundResource(R.drawable.ic_baseline_pause_24px);
        }
    }

    public void onClickRandom(View view) {
        if(musicService.getmRandom()){
            musicService.setmRandom(false);
            btnRandom.setBackgroundResource(R.drawable.ic_baseline_all_inclusive_24px);
        } else {
            musicService.setmRandom(true);
            btnRandom.setBackgroundResource(R.drawable.ic_baseline_all_inclusive_onclick_24px);
        }
    }

    public void onClickRepeat(View view) {
        if (musicService.getmRepeat() == true){
            musicService.setmRepeat(false);
            btnRepeat.setBackgroundResource(R.drawable.ic_baseline_autorenew_24px);
        } else {
            musicService.setmRepeat(true);
            btnRepeat.setBackgroundResource(R.drawable.ic_baseline_autorenew_onclick_24px);
        }
    }

    public void onClickLike(View view) {
        Toast.makeText(this,"You Like This Song", Toast.LENGTH_SHORT).show();
    }

    public void onClickMoreOption(View view) {
        if(OnlineSong) {
            Song song= songArrayList.get(songPosition);
            MyDialogFragment myDialogFragment = new MyDialogFragment();
            myDialogFragment.show(getFragmentManager(), "");
            myDialogFragment.setAttribute(song.getPathSong(), song.getNameSong()+".mp3");
        } else {
            Toast.makeText(this, "You already have this song !!",Toast.LENGTH_LONG).show();
        }
    }

    public ServiceConnection MusicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            // Get service
            musicService = binder.getService();
            // Send Data
            if(musicService.isSongsEmty()){
                musicService.clearSongs();
                musicService.getData(songArrayList);
            }
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
}
