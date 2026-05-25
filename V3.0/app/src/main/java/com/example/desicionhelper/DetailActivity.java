package com.example.desicionhelper;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private List<ChoiceGroup> groups;
    private int groupIndex;
    private ChoiceGroup group;

    // 界面元素
    private ListView detailList;
    private ScrollView editScroll;
    private LinearLayout editContainer, editBottom;
    private Button pickButton;
    private TextView editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        groupIndex = getIntent().getIntExtra("group_index", 0);
        groups = DataManager.loadGroups(this);
        group = groups.get(groupIndex);

        // 绑定界面元素
        TextView titleText = findViewById(R.id.detail_title);
        TextView infoText = findViewById(R.id.detail_info);
        detailList = findViewById(R.id.detail_list);
        editScroll = findViewById(R.id.edit_scroll);
        editContainer = findViewById(R.id.edit_container);
        editBottom = findViewById(R.id.edit_bottom);
        pickButton = findViewById(R.id.btn_pick);
        editButton = findViewById(R.id.btn_edit);
        EditText newOptionInput = findViewById(R.id.input_new_option);
        Button addOptionBtn = findViewById(R.id.btn_add_option);
        Button saveEditBtn = findViewById(R.id.btn_save_edit);

        // 设置标题和信息
        refreshTitleAndInfo(titleText, infoText);

        // 显示选项列表
        showNormalList();

        // 按钮文字
        pickButton.setText("选" + group.getPickCount() + "个");

        // 点击"选x个"按钮
        pickButton.setOnClickListener(v -> doPick());

        // 点击"编辑"进入编辑模式
        editButton.setOnClickListener(v -> enterEditMode());

        // 点击"添加"新选项
        addOptionBtn.setOnClickListener(v -> {
            String text = newOptionInput.getText().toString().trim();
            if (text.isEmpty()) {
                text = "选项" + (group.getChoices().size() + 1);
            }
            group.getChoices().add(text);
            newOptionInput.setText("");
            refreshEditList();
            refreshTitleAndInfo(titleText, infoText);
        });

        // 点击"保存修改"
        saveEditBtn.setOnClickListener(v -> {
            // 去掉空选项
            List<String> choices = group.getChoices();
            for (int i = choices.size() - 1; i >= 0; i--) {
                if (choices.get(i).trim().isEmpty()) {
                    choices.remove(i);
                }
            }

            // 读取新的选取数量
            EditText pickCountInput = findViewById(R.id.input_pick_count);
            String pickStr = pickCountInput.getText().toString().trim();
            int newPickCount;
            try {
                newPickCount = pickStr.isEmpty() ? 1 : Integer.parseInt(pickStr);
            } catch (NumberFormatException e) {
                newPickCount = group.getPickCount();
            }
            if (newPickCount <= 0) newPickCount = 1;
            if (newPickCount > choices.size()) newPickCount = choices.size();

            // 用新的选取数量更新 group
            group = new ChoiceGroup(group.getName(), choices, newPickCount);

            // 保存到文件
            groups.set(groupIndex, group);
            DataManager.saveGroups(this, groups);

            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            enterNormalMode();
            showNormalList();
            refreshTitleAndInfo(titleText, infoText);
            pickButton.setText("选" + group.getPickCount() + "个");
        });


        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    // ===== 普通模式：显示只读列表 =====
    private void showNormalList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.item_detail_list, android.R.id.text1, group.getChoices());
        detailList.setAdapter(adapter);
    }

    // ===== 刷新标题和信息 =====
    private void refreshTitleAndInfo(TextView titleText, TextView infoText) {
        titleText.setText(group.getName());
        infoText.setText("共" + group.getTotalCount() + "个选项，选" + group.getPickCount() + "个");
    }

    // ===== 进入编辑模式 =====
    private void enterEditMode() {
        detailList.setVisibility(View.GONE);
        pickButton.setVisibility(View.GONE);
        editScroll.setVisibility(View.VISIBLE);
        editBottom.setVisibility(View.VISIBLE);
        editButton.setText("取消");
        editButton.setOnClickListener(v -> {
            groups = DataManager.loadGroups(this);
            group = groups.get(groupIndex);
            enterNormalMode();
            showNormalList();
            TextView t = findViewById(R.id.detail_title);
            TextView i = findViewById(R.id.detail_info);
            refreshTitleAndInfo(t, i);
            pickButton.setText("选" + group.getPickCount() + "个");
        });
        EditText pickCountInput = findViewById(R.id.input_pick_count);
        pickCountInput.setText(String.valueOf(group.getPickCount()));
        refreshEditList();
    }


    // ===== 退出编辑模式 =====
    private void enterNormalMode() {
        detailList.setVisibility(View.VISIBLE);
        pickButton.setVisibility(View.VISIBLE);
        editScroll.setVisibility(View.GONE);
        editBottom.setVisibility(View.GONE);
        editButton.setText("编辑");
        editButton.setOnClickListener(v -> enterEditMode());
    }

    // ===== 刷新编辑模式的选项列表 =====
    private void refreshEditList() {
        editContainer.removeAllViews();
        List<String> choices = group.getChoices();

        for (int i = 0; i < choices.size(); i++) {
            final int index = i;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.setMargins(0, 0, 0, 8);
            row.setLayoutParams(rowLp);

            // 序号
            TextView numText = new TextView(this);
            numText.setText((i + 1) + ".");
            numText.setTextColor(Color.parseColor("#888888"));
            numText.setTextSize(15);
            LinearLayout.LayoutParams numLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            numText.setLayoutParams(numLp);

            // 输入框
            EditText et = new EditText(this);
            et.setText(choices.get(i));
            et.setTextColor(Color.WHITE);
            et.setTextSize(15);
            et.setSingleLine(true);
            et.setBackground(null);
            LinearLayout.LayoutParams etLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            etLp.setMargins(8, 0, 8, 0);
            et.setLayoutParams(etLp);

            // 保存修改到列表（输入时实时更新）
            et.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String val = et.getText().toString().trim();
                    if (val.isEmpty()) val = "选项" + (index + 1);
                    choices.set(index, val);
                }
            });

            // 删除按钮
            TextView deleteBtn = new TextView(this);
            deleteBtn.setText("删除");
            deleteBtn.setTextColor(Color.parseColor("#FF4444"));
            deleteBtn.setTextSize(14);
            deleteBtn.setPadding(16, 8, 8, 8);
            deleteBtn.setOnClickListener(v -> {
                choices.remove(index);
                refreshEditList();
            });

            row.addView(numText);
            row.addView(et);
            row.addView(deleteBtn);
            editContainer.addView(row);
        }

        if (choices.isEmpty()) {
            TextView emptyHint = new TextView(this);
            emptyHint.setText("暂无选项，请在下方添加");
            emptyHint.setTextColor(Color.parseColor("#666666"));
            emptyHint.setTextSize(14);
            emptyHint.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            hintLp.setMargins(0, 40, 0, 0);
            emptyHint.setLayoutParams(hintLp);
            editContainer.addView(emptyHint);
        }
    }

    // ===== 随机选择 =====
    private void doPick() {
        if (group.getChoices().isEmpty()) {
            Toast.makeText(this, "没有可选的选项", Toast.LENGTH_SHORT).show();
            return;
        }
        if (group.getPickCount() > group.getChoices().size()) {
            Toast.makeText(this, "选项不足", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> pool = new ArrayList<>(group.getChoices());
        Collections.shuffle(pool);
        List<String> picked = pool.subList(0, group.getPickCount());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < picked.size(); i++) {
            sb.append(i + 1).append(". ").append(picked.get(i));
            if (i < picked.size() - 1) sb.append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("随机选择结果")
                .setMessage(sb.toString())
                .setPositiveButton("确定", null)
                .show();
    }
}
