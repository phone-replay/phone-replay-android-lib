package com.phonereplay.frankenstein_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phonereplay.frankenstein_app.databinding.ActivityMainBinding;
import com.phonereplay.tasklogger.PhoneReplayApi;
import com.smartlook.android.core.api.Smartlook;

public class MainActivity extends AppCompatActivity {
    private static final int SCREEN_RECORD_REQUEST_CODE = 777;
    ActivityMainBinding binding;
    Smartlook smartlookInstance = Smartlook.getInstance();
    private NoteViewModel noteViewModel;
    private TextView textViewTimer;

    public void startMethods() {
        //smartlookInstance.start();
        PhoneReplayApi.startRecording();
    }

    public void stopMethods() {
        //smartlookInstance.stop();
        PhoneReplayApi.stopRecording();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        noteViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(NoteViewModel.class);
        binding.floatingActionButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, DataInsertActivity.class);

            intent.putExtra("type", "addMode");
            startActivityForResult(intent, 1);
        });
        binding.buttonFirst.setOnClickListener(view -> startMethods());

        binding.buttonStop.setOnClickListener(view -> stopMethods());

        binding.Rv.setLayoutManager(new LinearLayoutManager(this));
        binding.Rv.setHasFixedSize(true);
        RVAdapter adapter = new RVAdapter();
        binding.Rv.setAdapter(adapter);

        noteViewModel.getAllNotes().observe(this, adapter::submitList);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int a = viewHolder.getAdapterPosition();
                return false;
            }


            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int a = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.RIGHT) {
                    noteViewModel.delete(adapter.getNote(viewHolder.getAdapterPosition()));
                    Toast.makeText(MainActivity.this, "note deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, DataInsertActivity.class);
                    intent.putExtra("type", "update");
                    intent.putExtra("title", adapter.getNote(viewHolder.getAdapterPosition()).getTitle());
                    intent.putExtra("disp", adapter.getNote(viewHolder.getAdapterPosition()).getDisp());
                    intent.putExtra("id", adapter.getNote(viewHolder.getAdapterPosition()).getId());

                    startActivityForResult(intent, 2);
                }
            }
        }).attachToRecyclerView(binding.Rv);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //Start screen
            }
        }
        if (data != null) {
            if (requestCode == 1) {
                String title = data.getStringExtra("title");
                String disp = data.getStringExtra("disp");
                Note note = new Note(title, disp);
                noteViewModel.insert(note);
                Toast.makeText(this, "note added", Toast.LENGTH_SHORT).show();

            } else if (requestCode == 2) {
                String title = data.getStringExtra("title");
                String disp = data.getStringExtra("disp");
                Note note = new Note(title, disp);
                note.setId(data.getIntExtra("id", 0));
                noteViewModel.update(note);
                Toast.makeText(this, "note updated", Toast.LENGTH_SHORT).show();
            }
        }
    }
}