/* 
 * Copyright (C) 2009 db4.us
 * Copyright (C) 2020 Michael Shungkai Lam
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.db4.yac;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    protected enum InputState {

        START_STATE("START_STATE"),
        OPERAND("OPERAND"),
        OPERAND_POINT("OPERAND_POINT"),
        UNARY_OPERATOR("UNARY_OPERATOR"),
        BINARY_OPERATOR("BINARY_OPERATOR"),
        CLOSE_PARENTHESIS("CLOSE_PARENTHESIS");

        private String stringValue;
 
        InputState(String stringValue) {
            this.stringValue = stringValue;
        }
 
        public String getStringValue() {
            return stringValue;
        }
    }

    protected class InputTextInfo {

        InputState state;
        int size;
        InputTextInfo(InputState state, int size) {
            this.state = state;
            this.size = size;
        }
    }

    protected class HistoryInfo {

        String inputText;
        Object inputTextStackPacked[];

        HistoryInfo(String inputText, Stack<InputTextInfo> inputTextStack) {
            this.inputText = inputText;
            int i = inputTextStack.size() - 1;
            inputTextStackPacked = inputTextStack.toArray();
        }

        Stack<InputTextInfo> getStack() {
            Stack<InputTextInfo> stackToReturn = new Stack<InputTextInfo>();
            for (Object o : inputTextStackPacked)
                stackToReturn.push((InputTextInfo)o);
            return stackToReturn;
        }
    }

    protected InputState inputState;
    protected int parenCount;
    protected EditText inputText;
    protected Stack<InputTextInfo> inputTextStack = new Stack<InputTextInfo>();
    protected InputTextInfo startStateInputTextInfo = new InputTextInfo(InputState.START_STATE, 0);
    protected InputTextInfo topInputTextInfo;
    protected TextView resultText;
    protected String result = "";
    protected Toolbar toolbar;
    protected LinkedList<HistoryInfo> history = new LinkedList<HistoryInfo>();
    protected Iterator<HistoryInfo> itr = null;
    protected HistoryInfo historyInfo;
    protected static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Main.onCreate()...");

        super.onCreate(savedInstanceState);

        // disable keyboard when this app is running
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // if a derived class is calling onCreate, return to its onCreate at this point
        if (this.getClass() != MainActivity.class) {
             Log.d(TAG, "Derived object skipping the rest of Main.onCreate()...");
             return;
        }

        setContentView(R.layout.activity_main);

        inputState = InputState.START_STATE;
        inputTextStack.push(startStateInputTextInfo);
        parenCount = 0;
        toolbar = (Toolbar) findViewById(R.id.toolbar_no_actionbar);
        toolbar.inflateMenu(R.menu.main);
        toolbar.setTitle("YAC");

        // setOnMenuItemClickListener enabled to check for menu items:
        // hex/oct (hex/octal/binary mode)
        // scientific (scientific mode)
        // about (about message popup)
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.hex_oct_bin:
                        Log.d(TAG, "calling Hex");
                        startActivity(new Intent(MainActivity.this, 
                                                 NumberBaseActivity.class));
                        break;
                    case R.id.scientific:
                        Log.d(TAG, "calling Scientific");
                        startActivity(new Intent(MainActivity.this, 
                                                 ScientificActivity.class));
                        break;

                    case R.id.about:
                        Log.d(TAG, "about");
                        startActivity(new Intent(MainActivity.this, 
                                                 AboutActivity.class));
                        break;

                }   
                return false;
            }   
        }); 

        inputText = findViewById(R.id.input_box);
        inputText.setText("");
        resultText = findViewById(R.id.result_box);
    } // onCreate

    public void onClickNumber(View v) {
        Log.d(TAG, "onClickNumber...");
        Log.d(TAG, inputState.getStringValue());

        if (inputState == InputState.CLOSE_PARENTHESIS)
            return;

        inputState = InputState.OPERAND;

        Button b = (Button) v;
        inputText.getText().append(b.getText().toString());
        inputTextStack.push(new InputTextInfo(inputState, 1));
    } // onClickNumber

    public void onClickPoint(View v) {
        Log.d(TAG, "onClickPoint...");
        Log.d(TAG, inputState.getStringValue());

        if (inputState == InputState.CLOSE_PARENTHESIS ||
            inputState == InputState.OPERAND_POINT)
            return;
     
        if (inputState == InputState.OPERAND) {
            inputText.getText().append(".");
            inputTextStack.push(new InputTextInfo(InputState.OPERAND_POINT, 1));
        }
        else {
            inputText.getText().append("0.");
            inputTextStack.push(new InputTextInfo(InputState.OPERAND, 1));
            inputTextStack.push(new InputTextInfo(InputState.OPERAND_POINT, 1));
        }

        inputState = InputState.OPERAND_POINT;
    } // onClickPoint

    public void onClickOpenParenthesis(View v) {
        Log.d(TAG, "onClickOpenParenthesis...");
        Log.d(TAG, inputState.getStringValue());

        if (inputState != InputState.BINARY_OPERATOR &&
            inputState != InputState.UNARY_OPERATOR &&
            inputState != InputState.START_STATE)
            return;

        parenCount++;
        inputState = InputState.START_STATE;
        inputTextStack.push(new InputTextInfo(inputState, 1));

        Button b = (Button) v;
        inputText.getText().append(b.getText().toString());
    } // onClickOpenParenthesis

    public void onClickCloseParenthesis(View v) {
        Log.d(TAG, "onClickCloseParenthesis...");
        Log.d(TAG, inputState.getStringValue());

        if (inputState != InputState.OPERAND && 
            inputState != InputState.OPERAND_POINT &&
            inputState != InputState.CLOSE_PARENTHESIS)
            return;

        if (parenCount == 0)
            return;

        parenCount--;
        inputState = InputState.CLOSE_PARENTHESIS;
        inputTextStack.push(new InputTextInfo(inputState, 1));

        Button b = (Button) v;
        inputText.getText().append(b.getText().toString());
    } // onClickCloseParenthesis

    public void onClickOperator(View v) {
        Log.d(TAG, "onClickOperator...");
        Log.d(TAG, inputState.getStringValue());

        if (inputState != InputState.START_STATE &&
            inputState != InputState.OPERAND &&
            inputState != InputState.OPERAND_POINT &&
            inputState != InputState.CLOSE_PARENTHESIS)
            return;

        Button b = (Button) v;

        if (inputState == InputState.START_STATE) {
            if (b.getText().charAt(0) == '-') {
                inputText.getText().append(b.getText().charAt(0));
                inputState = InputState.UNARY_OPERATOR;
                inputTextStack.push(new InputTextInfo(inputState, 1));
            }
        } else {
            String prefix = "";
            if (inputState == InputState.OPERAND_POINT)
                prefix = "0";
            inputText.getText().append(prefix + b.getText().toString());
            inputState = InputState.BINARY_OPERATOR;
            inputTextStack.push(new InputTextInfo(inputState, 1));
        }
    } // onClickOperator

    public void onClearButton(View v) {
        Log.d(TAG, "onClearButton...");
        Log.d(TAG, inputState.getStringValue());
        Log.d(TAG, "inputTextStack.size()="+Integer.toString(inputTextStack.size()));

        itr = history.descendingIterator();

        inputState = InputState.START_STATE;
        parenCount = 0;
        inputText.getText().clear();
        resultText.setText("");

        while (inputTextStack.size() > 1)
            inputTextStack.pop();

    } // onClearButton

    public void onClickEqual(View v) {
        double result = 0.0;
        Log.d(TAG, "onClickEqual...");
        Log.d(TAG, inputState.getStringValue());
        String input = inputText.getText().toString();
        Log.d(TAG, "input="+input);
        Log.d(TAG, "parenCount="+Integer.toString(input.length()));
        Log.d(TAG, "parenCount="+Integer.toString(parenCount));

        if (inputState != InputState.OPERAND &&
            inputState != InputState.CLOSE_PARENTHESIS)
            return;

        if (parenCount > 0) {
            for (int i = 0; i<parenCount; i++) {
                input += ")";
                inputTextStack.push(new InputTextInfo(InputState.CLOSE_PARENTHESIS, 1));
            }
            parenCount = 0;
            inputText.setText(input);
            inputState = InputState.CLOSE_PARENTHESIS;
        }

        Log.d(TAG, "After paren adjustment, input="+input);

        String input2 = input;
        if (input2.contains("\u00D7")) {
            input2 = input2.replaceAll("\u00D7", "*");
        }

        if (input2.contains("\u00F7")) {
            input2 = input2.replaceAll("\u00F7", "/");
        }

        if (input2.contains("\u221A")) {
            input2 = input2.replaceAll("\u221A", "`");
        }
        Log.d(TAG, "After operator adjustment, input="+input2);

        //Expression expression = new ExpressionBuilder(input).build();
        //double result = expression.evaluate();
        ArithmeticExpression.set(input2);
        try {
            result = ArithmeticExpression.eval();
            Log.d(TAG, "inside try result="+String.valueOf(result));
        }
        catch (Exception e) {
            Log.d(TAG, "\n*** MainActivity:    " + e.getMessage());
            resultText.setText("Input error:"+e.getMessage());
            return;
        }

        String strResult = Double.toString(result);
        int pointIdx = strResult.indexOf('.');
        // fix round off error beyond 12 decimal points
        if (strResult.length() - 1 - pointIdx >= 12) {
            String last8 = strResult.substring(strResult.length() - 8);
            // Java can have 8.4-7=1.4000000000000004, seeing 0000*, round it
            // Java can have 8.4-9=-0.5999999999999996, seeing 9999*, round it
            if (last8.indexOf("0000") == 0 || last8.indexOf("9999") == 0) {
                result = Math.round(result*100000000)/100000000.0;
                strResult = Double.toString(result);
            }
        }
        resultText.setText(strResult);

        Log.d(TAG, "result="+strResult);

        history.add(new HistoryInfo(input, inputTextStack));
        if (history.size() > 100)
            history.removeFirst();
    } // onClickEqual

    public void onClickDelete(View v) {
        Log.d(TAG, "onClickDelete...");
        Log.d(TAG, inputState.getStringValue());

        String newInputText = inputText.getText().toString();
        Log.d(TAG, "inputText='"+newInputText+"'");

        if (newInputText.length() == 0)
            return;

        if (newInputText.charAt(newInputText.length()-1) == '(')
            parenCount--;
        else if (newInputText.charAt(newInputText.length()-1) == ')')
            parenCount++;

        topInputTextInfo = inputTextStack.pop();
        newInputText = newInputText.substring(0, newInputText.length()-topInputTextInfo.size);
        inputState = inputTextStack.peek().state;
        inputText.setText(newInputText);

        Log.d(TAG, "New inputState="+inputState.getStringValue());
    } // onClickDelete

    public void onClickHistory(View v) {
        Log.d(TAG, "onClickHistory...");
        Log.d(TAG, inputState.getStringValue());

        if (!resultText.getText().toString().equals(""))
            return;

        if (itr == null)
            return;
        if (itr.hasNext()) {
            historyInfo = itr.next();
            inputText.setText(historyInfo.inputText);
            inputTextStack = historyInfo.getStack();
            inputState = inputTextStack.peek().state;
        }
        Log.d(TAG, "Clicked to state:"+inputState.getStringValue());
    } // onClickHistory
}
