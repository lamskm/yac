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
    NumberBaseOperation class receives an expression with hex, decimal, octal, binary operands 
    passed in the set() method, and evaluates the expression in the eval() method, 
    returning a integer type value.

    NumberBaseOperation.set("1+2*3");
    NumberBaseOperation.eval(); -> 7
    NumberBaseOperation.set("0b111-0b1");
    NumberBaseOperation.eval(); -> 0b110
    NumberBaseOperation.set("0o377-0x1");
    NumberBaseOperation.eval(); -> 0o376
    NumberBaseOperation.set("0xFF>>0x1");
    NumberBaseOperation.eval(); -> 0x7F

    The arithmetic and bitwise operators follow that of C/C++/Java.
    The operands follow that of C/C++/Java except Octal operands follow Python (0o377 is 255).

    The expression operands are represented in integers.
    The binary arithmetic operators are +, -, *, and /.  
    The binary bitwise operators are +, -, *, and /.  
    The unary operators are - and ~ (bitwise not).

    The operator precedence follows that of C/C++/Java:
    (higher number higher precedence)

    7   -a    ~a  Negative Sign, NOT bitwise operation
    6   a*b   a/b   a%b  Multiplication, division, and remainder
    5   a+b   a-b  Addition and subtraction
    4   <<   >>  Bitwise arithmetic left shift and right shift
    3  &  Bitwise AND
    2  ^  Bitwise XOR (exclusive or)
    1  |  Bitwise OR (inclusive or) 

    The expressions are defined by the BNF rules:

            <expression>  ::=  <term> [ "|" ] <term> ]*

            <term>  ::=  <factor> [ "^" ] <factor> ]*

            <factor>  ::=  <shift> [ "&" ] <shift> ]*

            <shift>  ::=  <addition> [ "<<" | ">>" ] <addition> ]*

            <addition>  ::=  <multiplication> [ "+" | "-" ] <multiplication> ]*

            <multiplication>  ::=  <primary> [ "*" | "/" | "%" ] <primary> ]*

            <primary>  ::=  [ "-" | "~"]? <number>  |  "(" <expression> ")"

            <number>  ::=  [0-9]+ | 0x[0-9A-F]+ | 0o[0-7]+ | ob[01]+

*/

import java.util.Hashtable;

public class NumberBaseOperation {

    /*
     * set passes a string input expression inputExpr to the NumberBaseScanner class.
     */
    public static void set(String inputExpr) {
        NumberBaseScanner.init(inputExpr);
    }

    /*
     * eval evaluates the string input expression inputExpr passed to the NumberBaseScanner,
     * resulting in a int value.
     */
    public static long eval() throws ParseError {

            long value = 0;

            try {
                value = parseExpression();
                if (Scanner.peek() != null)
                    throw new ParseError("Unexpected input at the end.");
            }
            catch (ParseError e) {
                throw e;
            }
            return value;
    }

    /*
     * parseExpression parses the Expression item in the grammar, handling +,- .
     */
    private static long parseExpression() throws ParseError {
        long value;  
        char op;
        value = parseTerm();
        while (NumberBaseScanner.peek() != null && NumberBaseScanner.peek().charAt(0) == '|') {

            op = NumberBaseScanner.next().charAt(0);

            // calls parseTerm() to compute the second operand, + or - to first operand.
            long nextVal = parseTerm();
            value |= nextVal;
        }
        return value;
    }

    /*
     * parseTerm parses the Term item in the grammar, handling *,/ .
     */
    private static long parseTerm() throws ParseError {
        long value;
        value = parseFactor();
        while (NumberBaseScanner.peek() != null && NumberBaseScanner.peek().equals("^")) {

            char op = NumberBaseScanner.next().charAt(0);

            // calls parseFactor() to compute the second operand, * or / with first operand.
            long nextVal = parseFactor();
            value ^= nextVal;
        }
        return value;
    }

    /*
     * parseFactor parses the Factor item in the grammar, handling *,/ .
     */
    private static long parseFactor() throws ParseError {
        long value;
        value = parseShift();
        while (NumberBaseScanner.peek() != null && NumberBaseScanner.peek().equals("&")) {

            char op = NumberBaseScanner.next().charAt(0);

            // calls parseShift() to compute the second operand, * or / with first operand.
            long nextVal = parseShift();
            value &= nextVal;
        }
        return value;
    }

    private static long parseShift() throws ParseError {
        long value;
        value = parseAddition();
        while (NumberBaseScanner.peek() != null && 
              (NumberBaseScanner.peek().equals("<") ||
               NumberBaseScanner.peek().equals(">"))) {

            //char op = NumberBaseScanner.next().charAt(0);
            String op = NumberBaseScanner.next();

            // calls parseAddition() to compute the second operand, * or / with first operand.
            long nextVal = parseAddition();
            //if (op == '<')
            if (op.equals("<<"))
                value <<= nextVal;
            else if (op.equals(">>"))
                value >>= nextVal;
            else 
                throw new ParseError("Expected << or >> operators.");
        }
        return value;
    }

    private static long parseAddition() throws ParseError {
        long value;
        value = parseMultiplication();
        while (NumberBaseScanner.peek() != null && 
              (NumberBaseScanner.peek().equals("+") ||
               NumberBaseScanner.peek().equals("-"))) {

            char op = NumberBaseScanner.next().charAt(0);

            // calls parseMultiplication() to compute the second operand, * or / with first operand.
            long nextVal = parseMultiplication();
            if (op == '+')
                value += nextVal;
            else
                value -= nextVal;
        }
        return value;
    }

    private static long parseMultiplication() throws ParseError {
        long value;
        value = parsePrimary();
        while (NumberBaseScanner.peek() != null && 
              (NumberBaseScanner.peek().equals("*") ||
               NumberBaseScanner.peek().equals("/") ||
               NumberBaseScanner.peek().equals("%"))) {

            char op = NumberBaseScanner.next().charAt(0);

            // calls parseMultiplication() to compute the second operand, * or / with first operand.
            long nextVal = parsePrimary();
            if (op == '*')
                value *= nextVal;
            else if (op == '/')
                value /= nextVal;
            else
                value %= nextVal;
        }
        return value;
    }

    private static long parsePrimary() throws ParseError {

        boolean unary = false; // Assume no unary operators to begin with
        long value = 0;
        char op = ' ';

        if (NumberBaseScanner.peek() != null && 
            (NumberBaseScanner.peek().charAt(0) == '-' || 
             NumberBaseScanner.peek().charAt(0) == '~')) {
            op = NumberBaseScanner.next().charAt(0);
            unary = true;
        }

        String tmpstr = "";

        // parsePrimary reduces to a number
        if (NumberBaseScanner.peek() == null)
            throw new ParseError("End of input when expecting an operand.");
        else if (Character.isDigit(NumberBaseScanner.peek().charAt(0))) {
            tmpstr = NumberBaseScanner.next();
            if (tmpstr.charAt(0) != '0')
                value = Long.parseLong(tmpstr);
            else if (tmpstr.length() == 1)
                value = Long.parseLong(tmpstr);
            else if (tmpstr.charAt(1) == 'b')
                value = Long.parseLong(tmpstr.substring(2),2);
            else if (tmpstr.charAt(1) == 'o')
                value = Long.parseLong(tmpstr.substring(2),8);
            else if (tmpstr.charAt(1) == 'x')
                value = Long.parseLong(tmpstr.substring(2),16);

            if (unary)
                if (op == '-')
                    value = -value;
                else
                    value = ~value;

            return value;
        }
        // parsePrimary reduces to an inner expression in parentheses
        else if (NumberBaseScanner.peek().charAt(0) == '(' ) {
            // Consume the "("
            NumberBaseScanner.next();
            // Recursively call parseExpression to evaluate the inner expression
            value = parseExpression();
            if (NumberBaseScanner.peek() != null && NumberBaseScanner.peek().charAt(0) != ')')
                throw new ParseError("Missing right parenthesis.");
            // Consume the ")"
            NumberBaseScanner.next();
            if (unary)
                if (op == '-')
                    value = -value;
                else
                    value = ~value;

            return value;
        }
        // parsePrimary reduces to a math function call.
        else if (NumberBaseScanner.peek().charAt(0) == ')')
            throw new ParseError("Extra right parenthesis.");
        else if (NumberBaseScanner.peek().charAt(0) == '+' || 
                 NumberBaseScanner.peek().charAt(0) == '-' || 
                 NumberBaseScanner.peek().charAt(0) == '*' || 
                 NumberBaseScanner.peek().charAt(0) == '/')
            throw new ParseError("Misplaced operator:\""+NumberBaseScanner.peek().charAt(0)+"\"");
        else
            throw new ParseError("Unexpected character \"" + NumberBaseScanner.peek().charAt(0) + "\" encountered.");
    }

    public static void main(String[] args) {
        set(args[0]);

        long value = 0;
        try {
             value = eval();
        }
        catch (ParseError e) {
             System.out.println("\nmain()*** Error in input:    " + e.getMessage());
             System.exit(0);
        }
        System.out.println(value);
    }
}
