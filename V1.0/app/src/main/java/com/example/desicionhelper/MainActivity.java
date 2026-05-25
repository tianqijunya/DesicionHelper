package com.example.desicionhelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private List<ChoiceGroup> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_history);
        emptyText = findViewById(R.id.text_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_add).setOnClickListener(v ->
                startActivity(new Intent(this, AddActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        groups = DataManager.loadGroups(this);
        if (groups.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new GroupAdapter());
        }
    }

    private class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            ChoiceGroup g = groups.get(position);
            holder.nameText.setText(g.getName());
            holder.infoText.setText("共" + g.getTotalCount() + "个选项，选" + g.getPickCount() + "个");
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("group_index", position);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return groups == null ? 0 : groups.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView nameText, infoText;
            VH(View v) {
                super(v);
                nameText = v.findViewById(R.id.item_name);
                infoText = v.findViewById(R.id.item_info);
            }
        }
    }
}
