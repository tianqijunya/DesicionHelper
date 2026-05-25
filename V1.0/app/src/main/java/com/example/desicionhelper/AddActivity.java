package com.example.desicionhelper;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class AddActivity extends AppCompatActivity {

    private EditText nameInput, countInput, pickInput;
    private LinearLayout choicesContainer;
    private ScrollView scrollView;
    private EditText[] choiceInputs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        nameInput = findViewById(R.id.input_name);
        countInput = findViewById(R.id.input_count);
        pickInput = findViewById(R.id.input_pick);
        choicesContainer = findViewById(R.id.choices_container);
        scrollView = findViewById(R.id.scroll_view);

        // 返回
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 生成选项输入框
        findViewById(R.id.btn_generate).setOnClickListener(v -> generateChoiceInputs());

        // 保存
        findViewById(R.id.btn_save).setOnClickListener(v -> save());
    }

    private void generateChoiceInputs() {
        String countStr = countInput.getText().toString().trim();
        if (countStr.isEmpty()) {
            Toast.makeText(this, "请先输入选项数量", Toast.LENGTH_SHORT).show();
            return;
        }

        int n;
        try {
            n = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
            return;
        }

        if (n <= 0) {
            Toast.makeText(this, "选项数量必须大于0", Toast.LENGTH_SHORT).show();
            return;
        }

        choicesContainer.removeAllViews();
        choiceInputs = new EditText[n];

        for (int i = 0; i < n; i++) {
            EditText et = new EditText(this);
            et.setHint("选项 " + (i + 1));
            et.setTextColor(0xFFFFFFFF);
            et.setHintTextColor(0x66666666);
            et.setTextSize(16);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 8);
            et.setLayoutParams(lp);
            choicesContainer.addView(et);
            choiceInputs[i] = et;
        }

        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void save() {
        String name = nameInput.getText().toString().trim();
        String countStr = countInput.getText().toString().trim();
        String pickStr = pickInput.getText().toString().trim();

        // 名称为空就用默认名
        if (name.isEmpty()) {
            name = "我的选择";
        }

        if (choiceInputs == null) {
            Toast.makeText(this, "请先点击生成选项输入框", Toast.LENGTH_SHORT).show();
            return;
        }

        int n, x;
        try {
            n = countStr.isEmpty() ? choiceInputs.length : Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            n = choiceInputs.length;
        }

        try {
            x = pickStr.isEmpty() ? 1 : Integer.parseInt(pickStr);
        } catch (NumberFormatException e) {
            x = 1;
        }

        if (x <= 0) x = 1;
        if (x > n) x = n;

        List<String> choices = new ArrayList<>();
        for (int i = 0; i < choiceInputs.length; i++) {
            String text = choiceInputs[i].getText().toString().trim();
            // 为空就用默认名"选项1"、"选项2"...
            if (text.isEmpty()) {
                text = "选项" + (i + 1);
            }
            choices.add(text);
        }

        // 保存到 JSON 文件
        List<ChoiceGroup> groups = DataManager.loadGroups(this);
        groups.add(new ChoiceGroup(name, choices, x));
        DataManager.saveGroups(this, groups);

        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        finish();
    }
}
