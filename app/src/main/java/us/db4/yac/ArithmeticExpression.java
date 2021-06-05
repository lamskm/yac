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
    ArithmeticExpression class receives a standard expression passed in the set() method,
    and evaluates the expression in the eval() method, returning a real type value.

    ArithmeticExpression.set("1+2*3");
    ArithmeticExpression.eval(); -> 7.0

    The expression operands are represented in real numbers.
    The binary operators are +, -, *, and /.  
    The unary operator is -.
    The expressions are defined by the BNF rules:

            <expression>  ::=  [ "-" | "" ]? <term> [ [ "+" | "-" ] <term> ]*

            <term>  ::=  <factor> [ [ "*" | "/" ] <factor> ]*

            <factor>  ::=  <exponent> [ [ "^" | "\" ] <exponent> ]*

            <exponent>  ::=  <number>  |  "(" <expression> ")" |  <function> "(" <expression> ")"

            <number>  ::=  [0-9]+ | [0-9]+.[0-9]*

            <function>  ::=  [a-z]+

*/

import java.util.Hashtable;

public class ArithmeticExpression {

    private static Hashtable<String,MathFunction> mathFunctions;

    /*
     * setUsingDegree sets the usingDegree field of class MathFunction.
     */
    public static void setUsingDegree(boolean usingDegree) {
        MathFunction.setUsingDegree(usingDegree);
    }

    /*
     * initMathFunctions initializes the mathFunctions hashtable with
     * string function names, and their corresponding function objects.
     */
    public static void initMathFunctions() {

        setUsingDegree(true);

        mathFunctions = new Hashtable<>();

        mathFunctions.put("sin", 
                          new MathFunction("sin") {
                          @Override
                              public double apply(double arg) {
                                  if (getUsingDegree())
                                      return Math.sin(Math.toRadians(arg));
                                  else
                                      return Math.sin(arg);
                              }
                          });

        mathFunctions.put("cos", 
                          new MathFunction("cos") {
                          @Override
                              public double apply(double arg) {
                                  if (getUsingDegree())
                                      return Math.cos(Math.toRadians(arg));
                                  else
                                      return Math.cos(arg);
                              }
                          });

        mathFunctions.put("tan", 
                          new MathFunction("tan") {
                          @Override
                              public double apply(double arg) {
                                  if (getUsingDegree())
                                      return Math.tan(Math.toRadians(arg));
                                  else
                                      return Math.tan(arg);
                              }
                          });

        mathFunctions.put("asin", 
                          new MathFunction("asin") {
                          @Override
                              public double apply(double arg) {
                                  if (getUsingDegree())
                                      return Math.toDegrees(Math.asin(arg));
                                  else
                                      return Math.asin(arg);
                              }
                          });

        mathFunctions.put("acos", 
                          new MathFunction("acos") {
                          @Override
                              public double apply(double arg) {
                                  if (getUsingDegree())
                                      return Math.toDegrees(Math.acos(arg));
                                  else
                                      return Math.acos(arg);
                              }
                          });

        mathFunctions.put("atan", 
                          new MathFunction("atan") {
                          @Override
                              public double apply(double arg) {
                                  if (getUsingDegree())
                                      return Math.toDegrees(Math.atan(arg));
                                  else
                                      return Math.atan(arg);
                              }
                          });

        mathFunctions.put("exp", 
                          new MathFunction("exp") {
                          @Override
                              public double apply(double arg) {
                                  return Math.exp(arg);
                              }
                          });

        mathFunctions.put("ln", 
                          new MathFunction("ln") {
                          @Override
                              public double apply(double arg) {
                                  return Math.log(arg);
                              }
                          });

        mathFunctions.put("log", 
                          new MathFunction("log") {
                          @Override
                              public double apply(double arg) {
                                  return Math.log10(arg);
                              }
                          });
/* Comment out for later use
        mathFunctions.put("sinh", 
                          new MathFunction("sinh") {
                          @Override
                              public double apply(double arg) {
                                  if (getUsingDegree())
                                      return Math.sinh(Math.toRadians(arg));
                                  else
                                      return Math.sinh(arg);
                              }
                          });

        mathFunctions.put("cosh", 
                          new MathFunction("cosh") {
                          @Override
                              public double apply(double arg) {
                                  if (getUsingDegree())
                                      return Math.cosh(Math.toRadians(arg));
                                  else
                                      return Math.cosh(arg);
                              }
                          });

        mathFunctions.put("tanh", 
                          new MathFunction("tanh") {
                          @Override
                              public double apply(double arg) {
                                  if (getUsingDegree())
                                      return Math.tanh(Math.toRadians(arg));
                                  else
                                      return Math.tanh(arg);
                              }
                          });

        mathFunctions.put("cot", 
                          new MathFunction("cot") {
                          @Override
                              public double apply(double arg) {
                                  double tan = Math.tan(arg);
                                  if (tan == 0d) {
                                      throw new ArithmeticException("Division by zero in cotangent!");
                                  }
                                  return 1d / tan;
                              }
                          });

        mathFunctions.put("log1p", 
                          new MathFunction("log1p") {
                          @Override
                              public double apply(double arg) {
                                  return Math.log1p(arg);
                              }
                          });

        mathFunctions.put("abs", 
                          new MathFunction("abs") {
                          @Override
                              public double apply(double arg) {
                                  return Math.abs(arg);
                              }
                          });

        mathFunctions.put("sqrt", 
                          new MathFunction("sqrt") {
                          @Override
                              public double apply(double arg) {
                                  return Math.sqrt(arg);
                              }
                          });

        mathFunctions.put("cbrt", 
                          new MathFunction("cbrt") {
                          @Override
                              public double apply(double arg) {
                                  return Math.cbrt(arg);
                              }
                          });

        mathFunctions.put("expm1", 
                          new MathFunction("expm1") {
                          @Override
                              public double apply(double arg) {
                                  return Math.expm1(arg);
                              }
                          });
*/
    }

    /*
     * set passes a string input expression inputExpr to the Scanner class.
     */
    public static void set(String inputExpr) {
        Scanner.init(inputExpr);
    }

    /*
     * eval evaluates the string input expression inputExpr passed to the Scanner,
     * resulting in a double value.
     */
    public static double eval() throws ParseError {

            double value = 0.0;

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
    private static double parseExpression() throws ParseError {
        double value;  

        value = parseTerm();

        while (Scanner.peek() != null && ( Scanner.peek().charAt(0) == '+' || Scanner.peek().charAt(0) == '-')) {

            char op = Scanner.next().charAt(0);

            // calls parseTerm() to compute the second operand, + or - to first operand.
            double nextVal = parseTerm();
            if (op == '+')
                value += nextVal;
            else
                value -= nextVal;
        }
        return value;
    }

    /*
     * parseTerm parses the Term item in the grammar, handling *,/ .
     */
    private static double parseTerm() throws ParseError {
        double value;
        value = parseFactor();
        while (Scanner.peek() != null && ( Scanner.peek().equals("*") || Scanner.peek().equals("/"))) {

            char op = Scanner.next().charAt(0);

            // calls parseFactor() to compute the second operand, * or / with first operand.
            double nextVal = parseFactor();
            if (op == '*')
                value *= nextVal;
            else
                value /= nextVal;
        }
        return value;
    }

    /*
     * parseFactor parses the Term item in the grammar, handling *,/ .
     */
    private static double parseFactor() throws ParseError {
        double value;
        value = parseExponent();
        while (Scanner.peek() != null && ( Scanner.peek().equals("^") || Scanner.peek().equals("`"))) {

            char op = Scanner.next().charAt(0);

            // calls parseFactor() to compute the second operand, * or / with first operand.
            double nextVal = parseExponent();
            if (op == '^')
                value = Math.pow(value,nextVal);
            else
                value = Math.pow(nextVal,1/value);
        }
        return value;
    }

    /*
     * parseFactor parses the Factor item in the grammar, handling number, (, function calls.
     */
    private static double parseExponent() throws ParseError {

        if (Scanner.peek() == null)
            throw new ParseError("End of input when expecting an operand.");

        double doubleOperand;
        boolean negative = false;  // True if there is a leading minus sign.

        if (Scanner.peek().charAt(0) == '-') {
            Scanner.next();
            negative = true;
        }

        String tmpstr = "";

        // parseFactor reduces to a number
        if (Scanner.peek() == null)
            throw new ParseError("End of input when expecting an operand.");
        else if (Scanner.peek().charAt(0) == ')')
            throw new ParseError("Extra right parenthesis.");
        else if ( Character.isDigit(Scanner.peek().charAt(0)) ) {
            if (negative)
                return -Double.parseDouble(Scanner.next());
            else
                return Double.parseDouble(Scanner.next());
        }
        // parseFactor reduces to an inner expression in parentheses
        else if ( Scanner.peek().charAt(0) == '(' ) {
            // Consume the "("
            Scanner.next();
            // Recursively call parseExpression to evaluate the inner expression
            double value = parseExpression();
            if (Scanner.peek() != null && Scanner.peek().charAt(0) != ')')
                throw new ParseError("Missing right parenthesis.");
            // Consume the ")"
            Scanner.next();
            if (negative)
                value = -value;
            return value;
        }
        // parseFactor reduces to a math function call.
        else if ( Character.isLowerCase(Scanner.peek().charAt(0)) ) {
            // Stores the math function name
            String functionName = Scanner.next();
            // Consume the "("
            if ( Scanner.peek().charAt(0) == '(' ) 
                Scanner.next();
            // Recursively call parseExpression to get the function arg
            double value = parseExpression();
            if (Scanner.peek() != null && Scanner.peek().charAt(0) != ')')
                throw new ParseError("Missing right parenthesis.");
            // Consume the ")"
            Scanner.next();
            MathFunction mathFn = mathFunctions.get(functionName);
            if (mathFn != null)
                value = mathFn.apply(value);
            return value;
        }
        else if (Scanner.peek().charAt(0) == '+' || 
                 Scanner.peek().charAt(0) == '-' || 
                 Scanner.peek().charAt(0) == '*' || 
                 Scanner.peek().charAt(0) == '/')
            throw new ParseError("Misplaced operator:\""+Scanner.peek().charAt(0)+"\"");
        else
            throw new ParseError("Unexpected character \"" + Scanner.peek().charAt(0) + "\" encountered.");
    }

    public static void main(String[] args) {
        initMathFunctions();
        set(args[0]);

        double value = 0;
        try {
             value = eval();
        }
        catch (ParseError e) {
             System.out.println("\nmain()*** Error in input:    " + e.getMessage());
             System.exit(0);
        }
        String strValue = Double.toString(value);
        int pointIdx = strValue.indexOf('.');
        if (strValue.length() - 1 - pointIdx >= 12) {
            String last8 = strValue.substring(strValue.length() - 8);
            // Java can have 8.4-7=1.4000000000000004, seeing 0000*, round it
            // Java can have 8.4-9=-0.5999999999999996, seeing 9999*, round it
            if (last8.indexOf("0000") == 0 || last8.indexOf("9999") == 0) {
                value = Math.round(value*100000000)/100000000.0;
            }
        }
        System.out.println(value);
    }
}
