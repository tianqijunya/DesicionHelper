package com.example.desicionhelper;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    private int currentCount = 0;

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

        // 监听数量输入框的变化，实时生成输入框
        countInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (text.isEmpty()) {
                    choicesContainer.removeAllViews();
                    choiceInputs = null;
                    currentCount = 0;
                    return;
                }

                int n;
                try {
                    n = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    return;
                }

                if (n <= 0) {
                    choicesContainer.removeAllViews();
                    choiceInputs = null;
                    currentCount = 0;
                    return;
                }

                generateInputs(n);
            }
        });

        // 保存
        findViewById(R.id.btn_save).setOnClickListener(v -> save());
    }

    private void generateInputs(int n) {
        // 如果数量没变就不重新生成
        if (n == currentCount) return;

        // 保留已有的输入内容
        int oldLen = Math.min(currentCount, choiceInputs == null ? 0 : choiceInputs.length);
        String[] oldTexts = new String[Math.max(oldLen, n)];
        if (choiceInputs != null) {
            for (int i = 0; i < oldLen && i < n; i++) {
                oldTexts[i] = choiceInputs[i].getText().toString();
            }
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

            // 恢复之前输入的内容
            if (i < oldTexts.length && oldTexts[i] != null) {

                et.setText(oldTexts[i]);
            }

            choicesContainer.addView(et);
            choiceInputs[i] = et;
        }

        currentCount = n;

        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void save() {
        String name = nameInput.getText().toString().trim();
        String countStr = countInput.getText().toString().trim();
        String pickStr = pickInput.getText().toString().trim();

        if (name.isEmpty()) {
            name = "我的选择";
        }

        if (choiceInputs == null) {
            Toast.makeText(this, "请输入选项数量", Toast.LENGTH_SHORT).show();
            return;
        }

        int n, x;
        try {
            n = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
            return;
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
            if (text.isEmpty()) {
                text = "选项" + (i + 1);
            }
            choices.add(text);
        }

        List<ChoiceGroup> groups = DataManager.loadGroups(this);
        groups.add(new ChoiceGroup(name, choices, x));
        DataManager.saveGroups(this, groups);

        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        finish();
    }
}
