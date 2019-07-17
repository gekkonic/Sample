package com.Rwang.to.Exin;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    private ConstraintLayout mRoot;
    private EditText mSeed;
    private EditText mFrom;
    private EditText mTo;
    private EditText mLength;
    private EditText mResult;
    private Button mClear;
    private Button mSample;
    private Button mResample;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mRoot = findViewById(R.id.contentPanel);
        mSeed = findViewById(R.id.editTextSeed);
        mFrom = findViewById(R.id.editTextFrom);
        mTo = findViewById(R.id.editTextTo);
        mLength = findViewById(R.id.editTextLength);
        mResult = findViewById(R.id.editTextResult);

        mClear = findViewById(R.id.buttonClear);
        mSample = findViewById(R.id.buttonCalcu);
        mResample = findViewById(R.id.buttonRecalcu);
        // 用TextView的setMovementMethod方法设置一个滚动实例
        mResample.setMovementMethod(ScrollingMovementMethod.getInstance());

        // 清空
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFrom.setText("");
                mTo.setText("");
                mLength.setText("");
            }
        });

        // 结果框分配布局
        mResult.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                controlKeyboardLayout(mRoot, mResult);
            }
        });

        // 第一次抽取
        mSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int from = getNumFromEditText(mFrom);
                int to = getNumFromEditText(mTo);
                int length = getNumFromEditText(mLength);
                int seed = checkSeed(mSeed);
                char separator = '\n';

                if(from > to) {
                    int var = from;
                    from = to;
                    to = var;
                }
                if(from == to) {
                    to = from + 99;
                }
                if(length > to - from) {
                    length = (to - from)/10 + 1;
                }

                Random random = new Random(seed);

                //抽取并转换为String
                List<Integer> empty = new ArrayList<>();
                List<Integer> result_list = sample(random, from, to, length, empty);
                String result = listToString(result_list, separator);

                mSeed.setText(String.valueOf(seed));
                mFrom.setText(String.valueOf(from));
                mTo.setText(String.valueOf(to));
                mLength.setText(String.valueOf(length));
                mResult.setText(result);

                // 将文本内容放到系统剪贴板里
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simple text", result);
                cm.setPrimaryClip(clip);

                // 提示框
                String text = getToast(seed);
                showToast(text);
            }
        });

        // 第n次抽取
        mResample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int from = getNumFromEditText(mFrom);
                int to = getNumFromEditText(mTo);
                int length = getNumFromEditText(mLength);
                String init_res = mResult.getText().toString().trim();
                int seed = checkSeed(mSeed);
                char separator = '\n';

                if(from > to) {
                    int var = from;
                    from = to;
                    to = var;
                }
                if(from == to) {
                    to = from + 99;
                }
                if(length > to - from) {
                    length = (to - from)/10 + 1;
                }

                Random random = new Random(seed);

                //获得第一次的结果
                List<Integer> init_result_list = transResult(init_res, from, to);
                String init_result = listToString(init_result_list, separator);
                List<Integer> result_list = sample(random, from, to, length, init_result_list);
                String result = listToString(result_list, separator);

                String final_result = result + "\n" +init_result;

                mSeed.setText(String.valueOf(seed));
                mFrom.setText(String.valueOf(from));
                mTo.setText(String.valueOf(to));
                mLength.setText(String.valueOf(length));
                mResult.setText(final_result);

                // 将文本内容放到系统剪贴板里
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simple text", result);
                cm.setPrimaryClip(clip);

                String text = getToast(seed);
                // 提示框
                showToast(text);
            }
        });
    }

    private void initView(){

    }
    private void initEvent(){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToastHelper.cancelToast();
    }

    /**
     * 将文本框中的数字提取
     * @param editText 需要提取数字的文本框
     * @return 文本框中提取的数字
     */
    private int getNumFromEditText(EditText editText) {
        String str = editText.getText().toString().trim();
        int num = 1;
        if(str.length() != 0) {
            num = Integer.parseInt(str);
        }
        if(num <= 0) {
            num = 1;
        }

        return num;
    }

    /**
     * 检查seed
     * @param editText 需要提取数字的文本框
     * @return 文本框中提取的数字
     */
    private int checkSeed(EditText editText) {
        int num = 0;
        String str = editText.getText().toString().trim();
        if(str.length() == 0){
            num = new Random().nextInt(10000);
        } else {
            num = Integer.parseInt(str);
        }

        return num;
    }

    /**
     * 第一次抽取随机数
     * @param random 已设置种子的Random类
     * @param from   样本集起点（包含）
     * @param to     样本集终点（包含）
     * @param length 抽取样本的容量
     * @return 抽取的结果
     */
    private List<Integer> sample(Random random, int from, int to, int length, List<Integer> init) {
        Set<Integer> random_set = new HashSet<>();
        Set<Integer> init_set = new HashSet<>(init);
        int redo = 0;
        int num;

        //将to包含在样本空间内
        ++to;

        while(random_set.size() < length) {
            num = random.nextInt(to - from) + from;
            ++redo;
            if(!random_set.contains(num) && !init_set.contains(num)) {
                redo = 0;
                random_set.add(num);
            }
            if(to - from <= init_set.size() + random_set.size()) {
                break;
            }
        }
        List<Integer> random_list = new ArrayList<>(random_set);
        Collections.sort(random_list);

        return random_list;
    }

    /**
     * 将List转换为String
     * @param list 需要转换的list
     * @param separator 分隔符
     * @return 转换的字符串
     */
    private String listToString(List list, char separator) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i)).append(separator);
        }
        return sb.toString();
    }

    /**
     * 将Result框中的文本转换为数字List
     * @param init 从多行框获取的文本
     * @param from 样本集起点（包含）
     * @param to   样本集终点（包含）
     * @return 转换的数字列表
     */
    private List<Integer> transResult(String init, int from, int to) {
        Set<Integer> init_set = new HashSet<>();
        int num = 0;

        init = init.trim();
        String[] split = init.split("\n");
        for(String str : split) {
            if(str.length() > 0 && isNumeric(str)) {
                num = Integer.parseInt(str);
                if(num >= from && num <= to) {
                    init_set.add(num);
                }
            }
        }
        List<Integer> random_list = new ArrayList<>(init_set);
        Collections.sort(random_list);

        return random_list;
    }

    /**
     * 弹出提示框
     * @param text 提示文字
     */
    private void showToast(String text) {
        ToastHelper.showToast(MainActivity.this, text, Toast.LENGTH_SHORT);
    }

    /**
     * 利用正则表达式判断字符串是否是数字
     * @param str 需要验证的字符串
     * @return boolean
     */
    private static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }

    /**
     * 调整布局
     * @param root 最外层布局，需要调整的布局
     * @param scrollToView 被键盘遮挡的scrollToView，滚动root,使scrollToView在root可视区域的底部
     */
    private void controlKeyboardLayout(final View root, final View scrollToView) {
        root.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                //获取root在窗体的可视区域
                root.getWindowVisibleDisplayFrame(rect);
                //获取root在窗体的不可视区域高度(被其他View遮挡的区域高度)
                int rootInvisibleHeight = root.getRootView().getHeight() - rect.bottom;
                int screenHeight = root.getRootView().getHeight();
                //若不可视区域高度大于屏幕高度的1/4，则键盘显示
                if (rootInvisibleHeight > screenHeight / 4) {
                    int[] location = new int[2];
                    //获取scrollToView在窗体的坐标
                    scrollToView.getLocationInWindow(location);
                    //计算root滚动高度，使scrollToView在可见区域
                    int srollHeight = (location[1] + scrollToView.getHeight()) - rect.bottom + 90;
                    root.scrollTo(0, srollHeight);
                } else {
                    //键盘隐藏
                    root.scrollTo(0, 0);
                }
            }
        });
    }

    private String getToast(int num) {
        String str = "复制成功";
        return str;
    }
}
