package com.example.desicionhelper;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private List<ChoiceGroup> groups;
    private ChoiceGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 获取点击的是第几组
        int groupIndex = getIntent().getIntExtra("group_index", 0);
        groups = DataManager.loadGroups(this);
        group = groups.get(groupIndex);

        // 绑定界面元素
        TextView titleText = findViewById(R.id.detail_title);
        TextView infoText = findViewById(R.id.detail_info);
        ListView listView = findViewById(R.id.detail_list);
        Button pickButton = findViewById(R.id.btn_pick);

        // 设置标题和信息
        titleText.setText(group.getName());
        infoText.setText("共" + group.getTotalCount() + "个选项，选" + group.getPickCount() + "个");

        // 显示选项列表
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.item_detail_list, android.R.id.text1, group.getChoices());
        listView.setAdapter(adapter);

        // 按钮文字
        pickButton.setText("选" + group.getPickCount() + "个");

        // 点击按钮：随机选取
        pickButton.setOnClickListener(v -> {
            // 复制一份选项列表，打乱顺序
            List<String> pool = new ArrayList<>(group.getChoices());
            Collections.shuffle(pool);

            // 取前 x 个
            List<String> picked = pool.subList(0, group.getPickCount());

            // 拼接结果文字
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < picked.size(); i++) {
                sb.append(i + 1).append(". ").append(picked.get(i));
                if (i < picked.size() - 1) sb.append("\n");
            }

            // 弹窗显示结果
            new AlertDialog.Builder(this)
                    .setTitle("随机选择结果")
                    .setMessage(sb.toString())
                    .setPositiveButton("确定", null)
                    .show();
        });

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
