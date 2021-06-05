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
    Scanner class receives an input string in the form of arithmetic expression thru the init() method,
    and scans the expression operands, operators, function calls by methods: peek, next, isEnd.
*/

public class Scanner {

    public static String inputStr;
    public static int idx;

/*
    init() sets class variable inputStr to the input argument
*/
    public static void init(String s) {
        idx = 0;
        inputStr = s;
        while (idx < inputStr.length() && inputStr.charAt(idx) == ' ')
            idx++;
        return;
    }

/*
    isEnd() returns true/false based on if scanner has consumed the entire input.
*/
    public static boolean isEnd() {
        return idx==inputStr.length();
    }

/*
    peek() returns the current scanned item, without forwarding scanning index, for practical purpose
*/
    public static String peek() {
        int peekIdx = idx;

        while (peekIdx < inputStr.length() && inputStr.charAt(peekIdx) == ' ')
            peekIdx++;

        if (peekIdx == inputStr.length())
           return null;

        return String.valueOf(inputStr.charAt(peekIdx));
    }

/*
    next() returns the current scanned item, updating the scanning index to the next item.
*/
    public static String next() {
        while (idx < inputStr.length() && inputStr.charAt(idx) == ' ')
            idx++;

        if (idx >= inputStr.length())
            return null;

        if (inputStr.charAt(idx) == '+' ||
            inputStr.charAt(idx) == '-' ||
            inputStr.charAt(idx) == '*' ||
            inputStr.charAt(idx) == '/' ||
            inputStr.charAt(idx) == '^' ||
            inputStr.charAt(idx) == '`' ||
            inputStr.charAt(idx) == '(' ||
            inputStr.charAt(idx) == ')') {

            idx++;
            return String.valueOf(inputStr.charAt(idx-1));

        } else if (inputStr.charAt(idx) == '.' ||
                   Character.isDigit(inputStr.charAt(idx))) {

            String tmpstr = "";

            while (idx < inputStr.length() && 
                   (Character.isDigit(inputStr.charAt(idx)) || inputStr.charAt(idx) == '.')) {
                tmpstr += inputStr.charAt(idx);
                idx++;
            }
            return tmpstr;

        } else if (Character.isLowerCase(inputStr.charAt(idx))) {

            String tmpstr = "";

            while ( idx < inputStr.length() && Character.isLowerCase(inputStr.charAt(idx))) {
                tmpstr += inputStr.charAt(idx);
                idx++;
            }
            return tmpstr;
        }
        return null;
    }
}
