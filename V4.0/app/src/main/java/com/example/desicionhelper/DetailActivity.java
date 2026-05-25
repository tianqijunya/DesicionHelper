package com.example.desicionhelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private List<ChoiceGroup> groups;
    private int groupIndex;
    private ChoiceGroup group;
    private List<String> currentOptions;
    private List<String> pickedResults;

    private WheelView wheelView;
    private TextView detailInfo;
    private TextView editButton;
    private LinearLayout editContainer, editBottom;
    private View editScroll, btnPick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        groupIndex = getIntent().getIntExtra("group_index", 0);
        groups = DataManager.loadGroups(this);
        group = groups.get(groupIndex);
        currentOptions = new ArrayList<>(group.getChoices());
        pickedResults = new ArrayList<>();

        TextView titleText = findViewById(R.id.detail_title);
        detailInfo = findViewById(R.id.detail_info);
        wheelView = findViewById(R.id.wheel_view);
        btnPick = findViewById(R.id.btn_pick);
        editButton = findViewById(R.id.btn_edit);
        editScroll = findViewById(R.id.edit_scroll);
        editContainer = findViewById(R.id.edit_container);
        editBottom = findViewById(R.id.edit_bottom);
        EditText newOptionInput = findViewById(R.id.input_new_option);
        EditText pickCountInput = findViewById(R.id.input_pick_count);

        titleText.setText(group.getName());

        refreshWheel();
        refreshInfoText();

        wheelView.setOnClickListener(v -> doSpin());
        btnPick.setOnClickListener(v -> doSpin());

        editButton.setOnClickListener(v -> enterEditMode());

        findViewById(R.id.btn_add_option).setOnClickListener(v -> {
            String text = newOptionInput.getText().toString().trim();
            if (text.isEmpty()) {
                text = "选项" + (currentOptions.size() + 1);
            }
            currentOptions.add(text);
            newOptionInput.setText("");
            refreshEditList();
        });

        findViewById(R.id.btn_save_edit).setOnClickListener(v -> {
            for (int i = currentOptions.size() - 1; i >= 0; i--) {
                if (currentOptions.get(i).trim().isEmpty()) {
                    currentOptions.remove(i);
                }
            }

            String pickStr = pickCountInput.getText().toString().trim();
            int newPickCount;
            try {
                newPickCount = pickStr.isEmpty() ? 1 : Integer.parseInt(pickStr);
            } catch (NumberFormatException e) {
                newPickCount = group.getPickCount();
            }
            if (newPickCount <= 0) newPickCount = 1;
            if (newPickCount > currentOptions.size()) newPickCount = currentOptions.size();

            group = new ChoiceGroup(group.getName(), currentOptions, newPickCount);
            groups.set(groupIndex, group);
            DataManager.saveGroups(this, groups);

            pickedResults.clear();
            refreshWheel();
            refreshInfoText();

            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            enterNormalMode();
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void refreshWheel() {
        wheelView.setOptions(currentOptions);
        wheelView.invalidate();
    }

    private void refreshInfoText() {
        int remain = group.getPickCount() - pickedResults.size();
        if (remain > 0 && currentOptions.size() > 0) {
            detailInfo.setText("还剩" + remain + "次选择，转盘" + currentOptions.size() + "个选项");
        } else {
            detailInfo.setText("共" + group.getTotalCount() + "个选项，选" + group.getPickCount() + "个");
        }
    }

    private void doSpin() {
        if (wheelView.isSpinning()) return;
        if (currentOptions.isEmpty()) {
            Toast.makeText(this, "没有可选的选项", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalNeed = group.getPickCount();
        if (pickedResults.size() >= totalNeed) {
            Toast.makeText(this, "已完成所有选择", Toast.LENGTH_SHORT).show();
            return;
        }

        wheelView.setOnSpinCompleteListener((selectedIndex, selectedOption) -> {
            pickedResults.add(selectedOption);
            currentOptions.remove(selectedIndex);

            if (pickedResults.size() < totalNeed && !currentOptions.isEmpty()) {
                refreshWheel();
                refreshInfoText();
                Toast.makeText(this, "已选: " + selectedOption + "，继续下一个",
                        Toast.LENGTH_SHORT).show();
            } else {
                refreshInfoText();
                showResultDialog();
            }
        });

        wheelView.spin();
        btnPick.setEnabled(false);
        btnPick.postDelayed(() -> btnPick.setEnabled(true), 4700);
    }

    private void showResultDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 30, 50, 10);

        if (pickedResults.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("未选中任何选项");
            empty.setTextColor(Color.GRAY);
            empty.setTextSize(16f);
            container.addView(empty);
        } else {
            for (int i = 0; i < pickedResults.size(); i++) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rowLp.setMargins(0, 0, 0, 20);
                row.setLayoutParams(rowLp);

                TextView dot = new TextView(this);
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(wheelView.getColor(i));
                circle.setSize(28, 28);
                dot.setBackground(circle);
                LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(28, 28);
                dotLp.setMarginEnd(24);
                dot.setLayoutParams(dotLp);

                TextView text = new TextView(this);
                text.setText("第" + (i + 1) + "次选择：" + pickedResults.get(i));
                text.setTextColor(Color.BLACK);
                text.setTextSize(16f);

                row.addView(dot);
                row.addView(text);
                container.addView(row);
            }

            TextView hint = new TextView(this);
            hint.setText("点击确定后可重新开始选择");
            hint.setTextColor(0xFF666666);
            hint.setTextSize(13f);
            hint.setPadding(0, 10, 0, 0);
            container.addView(hint);
        }

        new AlertDialog.Builder(this)
                .setTitle("选择结果")
                .setView(container)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        currentOptions = new ArrayList<>(group.getChoices());
                        pickedResults.clear();
                        refreshWheel();
                        refreshInfoText();
                    }
                })
                .show();
    }

    private void enterEditMode() {
        wheelView.setVisibility(View.GONE);
        btnPick.setVisibility(View.GONE);
        editScroll.setVisibility(View.VISIBLE);
        editBottom.setVisibility(View.VISIBLE);
        editButton.setText("取消");
        editButton.setOnClickListener(v -> {
            groups = DataManager.loadGroups(this);
            group = groups.get(groupIndex);
            currentOptions = new ArrayList<>(group.getChoices());
            pickedResults.clear();
            refreshWheel();
            refreshInfoText();
            enterNormalMode();
        });
        EditText pickCountInput = findViewById(R.id.input_pick_count);
        pickCountInput.setText(String.valueOf(group.getPickCount()));
        refreshEditList();
    }

    private void enterNormalMode() {
        wheelView.setVisibility(View.VISIBLE);
        btnPick.setVisibility(View.VISIBLE);
        editScroll.setVisibility(View.GONE);
        editBottom.setVisibility(View.GONE);
        editButton.setText("编辑");
        editButton.setOnClickListener(v -> enterEditMode());
    }

    private void refreshEditList() {
        editContainer.removeAllViews();
        for (int i = 0; i < currentOptions.size(); i++) {
            final int index = i;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.setMargins(0, 0, 0, 8);
            row.setLayoutParams(rowLp);

            TextView numText = new TextView(this);
            numText.setText((i + 1) + ".");
            numText.setTextColor(Color.parseColor("#888888"));
            numText.setTextSize(15f);

            EditText et = new EditText(this);
            et.setText(currentOptions.get(i));
            et.setTextColor(Color.WHITE);
            et.setTextSize(15f);
            et.setSingleLine(true);
            et.setBackground(null);
            LinearLayout.LayoutParams etLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            etLp.setMargins(8, 0, 8, 0);
            et.setLayoutParams(etLp);
            et.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String val = et.getText().toString().trim();
                    if (val.isEmpty()) val = "选项" + (index + 1);
                    currentOptions.set(index, val);
                }
            });

            TextView deleteBtn = new TextView(this);
            deleteBtn.setText("删除");
            deleteBtn.setTextColor(Color.parseColor("#FF4444"));
            deleteBtn.setTextSize(14f);
            deleteBtn.setPadding(16, 8, 8, 8);
            deleteBtn.setOnClickListener(v -> {
                currentOptions.remove(index);
                refreshEditList();
            });

            row.addView(numText);
            row.addView(et);
            row.addView(deleteBtn);
            editContainer.addView(row);
        }

        if (currentOptions.isEmpty()) {
            TextView emptyHint = new TextView(this);
            emptyHint.setText("暂无选项，请在下方添加");
            emptyHint.setTextColor(Color.parseColor("#666666"));
            emptyHint.setTextSize(14f);
            emptyHint.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            hintLp.setMargins(0, 40, 0, 0);
            emptyHint.setLayoutParams(hintLp);
            editContainer.addView(emptyHint);
        }
    }
}



