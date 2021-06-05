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

import java.util.regex.Pattern;

public class ScientificActivity extends MainActivity {

    private int state;
    private TextView status;

    public static String[][] mathFunctions = { 
        {"sin", "cos", "tan", "asin", "acos", "atan"},
        {"ln", "log10", "X\u02B8", "e\u02E3", "10\u02E3", "\u221A"},
    };

    public static int mathFunctionsLen = mathFunctions.length;
    public static int mathFunctionsIdx = 0;
    public static Button buttonFunctions[];
    public static Button changeSetButton;
    public static Button degreeRadianButton;

    protected static String TAG = "ScientificActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Scientific.onCreate()...");
        super.onCreate(savedInstanceState);
        ArithmeticExpression.initMathFunctions();
        ArithmeticExpression.setUsingDegree(true);
        buttonFunctions = new Button[6];

        setContentView(R.layout.activity_scientific);
        status = (TextView) findViewById(R.id.status);
        status.setText("");
        mathFunctionsIdx = 0;
        Button functionButton;
        functionButton = (Button) findViewById(R.id.fn0_button);
        buttonFunctions[0] = functionButton;
        functionButton = (Button) findViewById(R.id.fn1_button);
        buttonFunctions[1] = functionButton;
        functionButton = (Button) findViewById(R.id.fn2_button);
        buttonFunctions[2] = functionButton;
        functionButton = (Button) findViewById(R.id.fn3_button);
        buttonFunctions[3] = functionButton;
        functionButton = (Button) findViewById(R.id.fn4_button);
        buttonFunctions[4] = functionButton;
        functionButton = (Button) findViewById(R.id.fn5_button);
        buttonFunctions[5] = functionButton;
        for (int i=0; i<6; i++)
          buttonFunctions[i].setText(mathFunctions[mathFunctionsIdx][i]);

        changeSetButton = (Button) findViewById(R.id.change_functions_button);
        changeSetButton.setText("\u21AA more\nfunctions");
        degreeRadianButton = (Button) findViewById(R.id.deg_rad_button);
        degreeRadianButton.setText("\u21AA radian");

        inputState = InputState.START_STATE;
        inputTextStack.push(new InputTextInfo(inputState, 0));
        parenCount = 0;
        toolbar = (Toolbar) findViewById(R.id.toolbar_no_actionbar);
        toolbar.inflateMenu(R.menu.scientific);
        toolbar.setTitle("Scientific (degree)");

        // Implement menu click listener to do something when menu items
        // selected
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                        case R.id.basic:
                        Log.d(TAG, "return to basic");
                        finish();
                        break;

                        case R.id.about:
                        Log.d(TAG, "about");
                        startActivity(new Intent(ScientificActivity.this, 
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

    public void onClickRotateFunction(View v) {
        Log.d(TAG, "onClickRotateFunction...");
        Log.d(TAG, inputState.getStringValue());
        mathFunctionsIdx = (mathFunctionsIdx + 1) % mathFunctionsLen;
        for (int i=0; i<6; i++)
          buttonFunctions[i].setText(mathFunctions[mathFunctionsIdx][i]);
    }

    public void onClickSwitchDegreeRadian(View v) {
        Log.d(TAG, "onClickSwitchDegreeRadian...");
        Log.d(TAG, String.valueOf(mathFunctionsIdx));

        String degreeRadianValue = degreeRadianButton.getText().toString();
        if (degreeRadianValue.equals("\u21AA radian")) {
            degreeRadianButton.setText("\u21AA degree");
            toolbar.setTitle("Scientific (radian)");
            ArithmeticExpression.setUsingDegree(false);
            status.setText("");
        } else {
            degreeRadianButton.setText("\u21AA radian");
            ArithmeticExpression.setUsingDegree(true);
            toolbar.setTitle("Scientific (degree)");
            ArithmeticExpression.setUsingDegree(true);
            status.setText("");
        }
    }

    public void onClickFunction(View v) {
        Log.d(TAG, "onClickFunction...");
        Log.d(TAG, inputState.getStringValue());

        Button b = (Button) v;
        String functionName = b.getText().toString();

        Log.d(TAG, "onClickFunction='"+functionName+"'");

        if (functionName.equals("X\u02B8") ||
            functionName.equals("\u221A")) {
            if (inputState == InputState.OPERAND ||
                inputState == InputState.CLOSE_PARENTHESIS) {
                if (functionName.equals("X\u02B8"))
                    inputText.getText().append("^");
                else if (functionName.equals("\u221A")) 
                    inputText.getText().append("\u221A");
                inputState = InputState.BINARY_OPERATOR;
                inputTextStack.push(new InputTextInfo(inputState, 1));
            }
            return;
        }

        if (inputState != InputState.START_STATE &&
            inputState != InputState.UNARY_OPERATOR &&
            inputState != InputState.BINARY_OPERATOR)
            return;

        if (functionName.equals("10\u02E3")) {
            inputText.getText().append("10^");
            inputState = InputState.BINARY_OPERATOR;
            inputTextStack.push(new InputTextInfo(InputState.OPERAND, 1));
            inputTextStack.push(new InputTextInfo(InputState.OPERAND, 1));
            inputTextStack.push(new InputTextInfo(inputState, 1));
            return;
        }

        parenCount++;
        inputState = InputState.START_STATE;

        if (functionName.equals("log10"))
            functionName = "log";
        else if (functionName.equals("e\u02E3"))
            functionName = "exp";
        inputText.getText().append(functionName+"(");
        inputTextStack.push(new InputTextInfo(inputState, (functionName+"(").length()));
    }
}
