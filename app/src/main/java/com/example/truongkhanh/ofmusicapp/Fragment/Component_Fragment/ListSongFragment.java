package com.example.truongkhanh.ofmusicapp.Fragment.Component_Fragment;


import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.truongkhanh.ofmusicapp.Adapter.rowSongAdapater;
import com.example.truongkhanh.ofmusicapp.Activity.AllOfflineSongActivity;
import com.example.truongkhanh.ofmusicapp.Model.Song;
import com.example.truongkhanh.ofmusicapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListSongFragment extends Fragment {

    View view;
    TextView textView_More_Song;
    Button btn_Show_More_Song;
    RecyclerView recyclerViewSong;
    rowSongAdapater rowSongAdapater;
    ArrayList<Song> arrayListSong = new ArrayList<>();

    public ListSongFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list_song, container, false);

        Mapping();
        CreateListener();
        GetData();

        return view;
    }

    private void GetData() {
        // Create variable for Getting data
        String root = Environment.getExternalStorageDirectory().getPath();
        File mp3Folder = new File(root + "/Zing MP3");
        arrayListSong = new ArrayList<Song>();

        if(mp3Folder.exists()){
            //textView_More_Song.setText("root"+"/Zing MP3");
            List<File> mp3File = getMp3File(mp3Folder);
            for (File file : mp3File){
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(file.getAbsolutePath());

                Song song = new Song();

                String nameSong = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                if(TextUtils.isEmpty(nameSong)){
                    nameSong = "Unknow Song";
                }
                song.setNameSong(nameSong);

                String nameAlbum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                if(TextUtils.isEmpty(nameAlbum)){
                    nameAlbum = "Unknow Album";
                }
                song.setNameAlbum(nameAlbum);

                String nameArtis = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                if(TextUtils.isEmpty(nameArtis)){
                    nameArtis = "Unknow Artis";
                }
                song.setNameArtis(nameArtis);

                String nameAuthor = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
                if(TextUtils.isEmpty(nameAuthor)){
                    nameArtis = "Unknow Author";
                }
                song.setNameAuthor(nameAuthor);

                song.setPathSong(file.getPath());

                song.setLinkImageSong("");

                // Add song to arrayListSong with data in it
                // We need more song in AllOfflineSong Activity
                arrayListSong.add(song);
            }

        } else {
            Toast.makeText( getContext(),"Khong thanh cong load du lieu", Toast.LENGTH_SHORT).show();
        }

        // Pass 5 song to Person Fragment
        rowSongAdapater = new rowSongAdapater(arrayListSong);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewSong.setLayoutManager(linearLayoutManager);
        recyclerViewSong.setAdapter(rowSongAdapater);
    }

    private List<File> getMp3File(File mp3Folder) {
        //ArrayList to store File and return to method call this
        ArrayList<File> fileArrayList = new ArrayList<File>();

        //Array store All File in Directory send to this method
        File[] files = mp3Folder.listFiles();

        for (File file : files){
            if(file.exists()){
                if (file.isDirectory()){
                    // Todo: Notthing todo with folder inside folder ZingMp3
                } else {
                    if(file.getName().endsWith(".mp3") && file.canRead()){
                        fileArrayList.add(file);
                    }
                }
            }
        }
        return fileArrayList;
    }

    private void CreateListener() {
        textView_More_Song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(arrayListSong.size() != 0) {
                    Intent intent = new Intent(v.getContext(), AllOfflineSongActivity.class);
                    intent.putExtra("ListSong", arrayListSong);
                    v.getContext().startActivity(intent);
                }
            }
        });

        btn_Show_More_Song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(arrayListSong.size() != 0) {
                    Intent intent = new Intent(v.getContext(), AllOfflineSongActivity.class);
                    intent.putExtra("ListSong", arrayListSong);
                    v.getContext().startActivity(intent);
                }
            }
        });
    }

    private void Mapping() {
        textView_More_Song = (TextView) view.findViewById(R.id.TextView_more_song);
        btn_Show_More_Song = (Button) view.findViewById(R.id.Button_Show_More_Song);
        recyclerViewSong = (RecyclerView) view.findViewById(R.id.RecyclerView_list_song);
        recyclerViewSong.setNestedScrollingEnabled(false);
     }

}
