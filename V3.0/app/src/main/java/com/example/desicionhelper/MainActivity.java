package com.example.desicionhelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private TextView editModeBtn;
    private List<ChoiceGroup> groups;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_history);
        emptyText = findViewById(R.id.text_empty);
        editModeBtn = findViewById(R.id.btn_edit_mode);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_add).setOnClickListener(v ->
                startActivity(new Intent(this, AddActivity.class))
        );

        editModeBtn.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            editModeBtn.setText(isEditMode ? "完成" : "编辑");
            if (recyclerView.getAdapter() != null) {
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isEditMode = false;
        if (editModeBtn != null) editModeBtn.setText("编辑");
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

            // 普通模式：点击进入详情
            holder.itemView.setOnClickListener(v -> {
                if (isEditMode) return;
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("group_index", position);
                startActivity(intent);
            });

            // 根据模式显示箭头或删除按钮
            if (isEditMode) {
                holder.arrow.setVisibility(View.GONE);
                holder.deleteBtn.setVisibility(View.VISIBLE);
            } else {
                holder.arrow.setVisibility(View.VISIBLE);
                holder.deleteBtn.setVisibility(View.GONE);
            }

            // 删除按钮
            holder.deleteBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("确认删除")
                        .setMessage("确定要删除「" + g.getName() + "」吗？")
                        .setPositiveButton("删除", (dialog, which) -> {
                            groups.remove(position);
                            DataManager.saveGroups(MainActivity.this, groups);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, groups.size());
                            if (groups.isEmpty()) {
                                emptyText.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return groups == null ? 0 : groups.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView nameText, infoText, arrow, deleteBtn;
            VH(View v) {
                super(v);
                nameText = v.findViewById(R.id.item_name);
                infoText = v.findViewById(R.id.item_info);
                arrow = v.findViewById(R.id.item_arrow);
                deleteBtn = v.findViewById(R.id.item_delete);
            }
        }
    }
}
