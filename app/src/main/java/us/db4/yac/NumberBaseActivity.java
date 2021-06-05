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

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.MenuItem;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.*;

public class NumberBaseActivity extends MainActivity {

    private int base;
    private int state;
    private TextView status;

    public static String[][] operatorFunctions = { 
        {"%", "+", "-", "*", "/", "\u25B2"},
        {"<<", ">>", "|", "^", "&", "~"},
    };

    public static int operatorFunctionsLen = operatorFunctions.length;
    public static int operatorFunctionsIdx = 0;
    public static Button buttonOperators[];
    public static Button changeOpButton;
    public static Button changeBaseButton;

    protected static String TAG = "NumberBaseActivity";

    public static String toBinary(int n) {
        String binaryStr = "";
        for (int i=0; i<32; i++) {
            binaryStr += '0' + (n & 0x80000000) >>> 31;
            n = n << 1;
        }
        return binaryStr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_numberbase);

        status = (TextView) findViewById(R.id.status);
        status.setText("");

        buttonOperators = new Button[6];
        buttonOperators[0] = (Button) findViewById(R.id.op0_button);
        buttonOperators[1] = (Button) findViewById(R.id.op1_button);
        buttonOperators[2] = (Button) findViewById(R.id.op2_button);
        buttonOperators[3] = (Button) findViewById(R.id.op3_button);
        buttonOperators[4] = (Button) findViewById(R.id.op4_button);
        buttonOperators[5] = (Button) findViewById(R.id.op5_button);

        operatorFunctionsIdx = 0;
        for (int i=0; i<buttonOperators.length; i++)
            buttonOperators[i].setText(operatorFunctions[operatorFunctionsIdx][i]);

        changeOpButton = (Button) findViewById(R.id.change_op_button);
        changeOpButton.setText("\u21AA more\noperators");
        changeBaseButton = (Button) findViewById(R.id.change_base_button);
        changeBaseButton.setText("\u21AAbase 16");

        inputState = InputState.START_STATE;
        inputTextStack.push(new InputTextInfo(inputState, 0));
        parenCount = 0;

        toolbar = (Toolbar) findViewById(R.id.toolbar_no_actionbar);
        toolbar.inflateMenu(R.menu.hex);
        toolbar.setTitle("Hex/Oct (base 10)");
        base = 10;

        // Implement menu click listener to do something when menu items
        // selected
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.basic:
                    Log.d(TAG, "returning to Basic mode");
                    finish();
                    break;

                    case R.id.about:
                    Log.d(TAG, "about");
                    startActivity(new Intent(NumberBaseActivity.this, AboutActivity.class));
                    break;

                }   
                return false;
            }   
        }); 

        inputText = findViewById(R.id.input_box);
        inputText.setText("");
        resultText = findViewById(R.id.result_box);
    } // onCreate

    @Override
    public void onClickDelete(View v) {
        Log.d(TAG, "onClickDelete...");
        Log.d(TAG, inputState.getStringValue());

        String newInputText = inputText.getText().toString();
        if (newInputText.length() == 0)
            return;

        if (newInputText.charAt(newInputText.length()-1) == '(')
            parenCount--;
        else if (newInputText.charAt(newInputText.length()-1) == ')')
            parenCount++;

        topInputTextInfo = (inputTextStack.pop());

        InputTextInfo peekinfo= (inputTextStack.peek());

        newInputText = newInputText.substring(0, newInputText.length()-topInputTextInfo.size);

        inputState = peekinfo.state;

        Log.d(TAG, "new inputstate:"); 
        Log.d(TAG, inputState.getStringValue());

        inputText.setText(newInputText);
    }

    @Override
    public void onClickNumber(View v) {
        Log.d(TAG, "onClickNumber...");
        Log.d(TAG, inputState.getStringValue());

        if (inputState == InputState.CLOSE_PARENTHESIS)
            return;

        String prefix = "";
        Button b = (Button) v;
        String tmpstr = b.getText().toString(); 

        if (base == 2) {
            if (tmpstr.charAt(0) > '1')
                return;
        } else if (base == 8) {
            if (tmpstr.charAt(0) > '7')
                return;
        } else if (base == 10) {
            if (tmpstr.charAt(0) > '9')
                return;
        }
        
        if (inputState != InputState.OPERAND) {
            inputState = InputState.OPERAND;
            if (base == 2)
                prefix = "0b";
            else if (base == 8)
                prefix = "0o";
            else if (base == 16)
                prefix = "0x";
        }
        inputText.getText().append(prefix + tmpstr);
        inputTextStack.push(new InputTextInfo(inputState, (prefix + tmpstr).length()));
    }

    @Override
    public void onClickOperator(View v) {
        Log.d(TAG, "onClickOperator...");
        Log.d(TAG, inputState.getStringValue());

        Button b = (Button) v;
        if (b.getText().charAt(0) == '\u25B2') {
            onClickHistory(v);
            return;
        }

        if (inputState != InputState.START_STATE &&
            inputState != InputState.OPERAND &&
            inputState != InputState.CLOSE_PARENTHESIS)
            return;

        if (inputState == InputState.START_STATE) {
            if (b.getText().charAt(0) == '-' ||
                b.getText().charAt(0) == '~') {
                inputText.getText().append(b.getText().toString());
                inputState = InputState.UNARY_OPERATOR;
            }   
        } else if (b.getText().charAt(0) != '~') {
            inputText.getText().append(b.getText().toString());
            inputState = InputState.BINARY_OPERATOR;
        }   
        inputTextStack.push(new InputTextInfo(inputState, (b.getText().toString()).length()));
    }   

    public void onClickChangeOperators(View v) {
        Log.d(TAG, "onClickChangeOperators...");
        Log.d(TAG, inputState.getStringValue());

        operatorFunctionsIdx = (operatorFunctionsIdx + 1) % operatorFunctionsLen;

        for (int i=0; i<buttonOperators.length; i++)
            buttonOperators[i].setText(operatorFunctions[operatorFunctionsIdx][i]);
    }

    public void onClickChangeBase(View v) {
        Log.d(TAG, "onClickChangeBase...");
        Log.d(TAG, String.valueOf(operatorFunctionsIdx));

        String baseValue = changeBaseButton.getText().toString();
        if (baseValue.equals("\u21AAbase 16")) {
            changeBaseButton.setText("\u21AAbase 2");
            toolbar.setTitle("Hex/Oct (base 16)");
            base = 16;
            status.setText("");
        } else if (baseValue.equals("\u21AAbase 2")) {
            changeBaseButton.setText("\u21AAbase 8");
            toolbar.setTitle("Hex/Oct (base 2)");
            base = 2;
            status.setText("");
        } else if (baseValue.equals("\u21AAbase 8")) {
            changeBaseButton.setText("\u21AAbase 10");
            toolbar.setTitle("Hex/Oct (base 8)");
            base = 8;
            status.setText("");
        } else {
            changeBaseButton.setText("\u21AAbase 16");
            toolbar.setTitle("Hex/Oct (base 10)");
            base = 10;
            status.setText("");
        }
    }

    @Override
    public void onClickEqual(View v) {
        Log.d(TAG, "onClickEqual...");

        long result = 0;
        String input = inputText.getText().toString();

        Log.d(TAG, "input="+input);
        Log.d(TAG, "parenCount=");
        Log.d(TAG, String.valueOf(parenCount));

        if (inputState != InputState.OPERAND &&
            inputState != InputState.CLOSE_PARENTHESIS)
            return;

        if (parenCount > 0) {
            for (int i=0; i<parenCount; i++)
                input += ")";
            parenCount = 0;
            Log.d(TAG, "input="+input);
            inputText.setText(input);
            inputState = InputState.CLOSE_PARENTHESIS;
        }

        Log.d(TAG, "input3="+input);

        //Expression expression = new ExpressionBuilder(input).build();
        //double result = expression.evaluate();
        NumberBaseOperation.set(input);
        try {
            result = NumberBaseOperation.eval();
            Log.d(TAG, "inside try result=");
            Log.d(TAG, String.valueOf(result));
        }
        catch (Exception e) {
            Log.d(TAG, "\n*** " + e.getMessage());
            resultText.setText("Input error:"+e.getMessage());
            return;
        }

        String binaryString = Long.toString(result, base);
        String twoscomplement = "";
        String sign = "";

        if (result < 0) {
            binaryString = binaryString.substring(1);
            sign = "-";
            int result32bit = (int) result;
            if (base == 2) {
                twoscomplement = " (0b" + toBinary(result32bit) + ")";
            } else if (base == 8) {
                twoscomplement = " (0o" + String.format("%o)", result32bit);
            } else if (base == 16) {
                twoscomplement = " (0x" + String.format("%08X)", result32bit);
            }
        }
//      resultText.setText(String.valueOf(result)+":" + String.format("0x%08X", result));
        Log.d(TAG, "base=");
        Log.d(TAG, String.valueOf(base));

        String prefix = "";
        if (base == 2) {
            prefix = "0b";
        } else if (base == 8) {
            prefix = "0o";
        } else if (base == 16) {
            prefix = "0x";
        }
        resultText.setText(sign+prefix+binaryString.toUpperCase()+twoscomplement);

        Log.d(TAG, "result=");
        Log.d(TAG, prefix+binaryString);
        Log.d(TAG, String.valueOf(result));

        history.add(new HistoryInfo(input, inputTextStack));
        if (history.size() > 100)
            history.removeFirst();

    }
}
