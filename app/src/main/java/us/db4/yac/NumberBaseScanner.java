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
/*
    NumberBaseScanner class receives an input string in the form of arithmetic expression thru the init() method,
    and scans the expression operands, operators, function calls by methods: peek, next, isEnd.
*/

public class NumberBaseScanner extends Scanner {

/*
    next() returns the current scanned item, updating the scanning index to the next item.
*/
    public static String next() {
        while (idx < inputStr.length() && inputStr.charAt(idx) == ' ')
            idx++;

        if (idx >= inputStr.length())
            return null;

        if (inputStr.charAt(idx) == '|' ||
            inputStr.charAt(idx) == '^' ||
            inputStr.charAt(idx) == '&' ||
            inputStr.charAt(idx) == '+' ||
            inputStr.charAt(idx) == '-' ||
            inputStr.charAt(idx) == '*' ||
            inputStr.charAt(idx) == '/' ||
            inputStr.charAt(idx) == '%' ||
            inputStr.charAt(idx) == '~' ||
            inputStr.charAt(idx) == '(' ||
            inputStr.charAt(idx) == ')') {

            idx++;
            return String.valueOf(inputStr.charAt(idx-1));

        } else if (inputStr.charAt(idx) == '<' ||
                   inputStr.charAt(idx) == '>') {
            String tmpstr = "" + inputStr.charAt(idx);
            idx++;
            while (idx < inputStr.length() && inputStr.charAt(idx) == tmpstr.charAt(0)) {
                tmpstr += inputStr.charAt(idx);
                idx++;
            }
            return tmpstr;

        } else if (Character.isDigit(inputStr.charAt(idx))) {

            String tmpstr = "";

            if (inputStr.charAt(idx) == '0') {
                tmpstr += '0';
                idx++;
                if (idx < inputStr.length() && (inputStr.charAt(idx) == 'b' ||
                    inputStr.charAt(idx) == 'o' || inputStr.charAt(idx) == 'x')) {
                    tmpstr += inputStr.charAt(idx);
                    idx++;
                }
            }
            while (idx < inputStr.length() && 
                   (Character.isDigit(inputStr.charAt(idx)) || 
                   (inputStr.charAt(idx) >= 'A' && inputStr.charAt(idx) <= 'F'))) {
                tmpstr += inputStr.charAt(idx);
                idx++;
            }
            return tmpstr;

        }
        return null;
    }
}
