package com.thinkipysoft.samplecalculate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.thinkipysoft.samplecalculate.databinding.ActivityMainBinding;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    private StringBuilder currentInput;

    private double num1, num2;
    private String operator;
    private double result;

    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        currentInput = new StringBuilder();
        result = 0;
        setButtonClickListeners();


    }
    private void setButtonClickListeners() {
        // Number buttons
        setNumberButtonClickListener(binding.btn0);
        setNumberButtonClickListener(binding.btn1);
        setNumberButtonClickListener(binding.btn2);
        setNumberButtonClickListener(binding.btn3);
        setNumberButtonClickListener(binding.btn4);
        setNumberButtonClickListener(binding.btn5);
        setNumberButtonClickListener(binding.btn6);
        setNumberButtonClickListener(binding.btn7);
        setNumberButtonClickListener(binding.btn8);
        setNumberButtonClickListener(binding.btn9);

        // Operator buttons
        setOperatorButtonClickListener(binding.btnAdd, "+");
        setOperatorButtonClickListener(binding.btnSubtract, "-");
        setOperatorButtonClickListener(binding.btnMultiply, "*");
        setOperatorButtonClickListener(binding.btnDivide, "/");

        // Other buttons
        binding.btnDot.setOnClickListener(v -> appendToInput("."));
        binding.btnClear.setOnClickListener(v -> clearInput());
        binding.btnEqual.setOnClickListener(v -> calculateResult());

        // Additional buttons
        binding.btnPercentage.setOnClickListener(v -> handlePercentage());
        binding.btnRoot.setOnClickListener(v -> handleSquareRoot());
        binding.btnPower.setOnClickListener(v -> handlePower());

        // Back button
        binding.btnBack.setOnClickListener(v -> handleBackspace());
    }

    private void setNumberButtonClickListener(Button button) {
        button.setOnClickListener(v -> appendToInput(button.getText().toString()));
    }

    private void setOperatorButtonClickListener(Button button, String operator) {
        button.setOnClickListener(v -> handleOperator(operator));
    }

    private void appendToInput(String value) {
        currentInput.append(value);
        updateResultTextView();
    }

    private void clearInput() {
        currentInput.setLength(0);
        updateResultTextView();
    }

    /*    private void updateResultTextView() {
            binding.resultTextView.setText(currentInput.toString());
        }*/
    private void updateResultTextView() {
        String expression = formatExpression();
        binding.resultTextView.setText(expression);
    }

    private String formatExpression() {
        if (operator != null) {
            return String.format("%s %s %s", num1, operator, currentInput.toString());
        } else {
            return currentInput.toString();
        }
    }

    private void handleOperator(String operator) {
        if (currentInput.length() > 0) {
            num1 = Double.parseDouble(currentInput.toString());
            this.operator = operator;
            clearInput();
        }
    }



    private void calculateResult() {
        if (currentInput.length() > 0 && operator != null) {
            num2 = Double.parseDouble(currentInput.toString());
            double result = performCalculation(num1, num2, operator);

            // Update the expression to include the result
            currentInput.setLength(0);
            currentInput.append(result);
            String expressionWithResult = String.format("%s %s %s = %s", num1, operator, num2, result);

            // Update the resultTextView with the expression and result
            binding.resultTextView.setText(expressionWithResult);

            // Reset operator and num1 for subsequent calculations
            operator = null;
            num1 = result;
        }
    }

    private double performCalculation(double num1, double num2, String operator) {
        switch (operator) {
            case "+":
                return num1 + num2;
            case "-":
                return num1 - num2;
            case "*":
                return num1 * num2;
            case "/":
                return num1 / num2;
            default:
                return 0;
        }
    }

    private void handlePercentage() {
        if (currentInput.length() > 0) {
            double percentageValue = Double.parseDouble(currentInput.toString()) / 100;
            currentInput.setLength(0);
            currentInput.append(percentageValue);
            updateResultTextView();
        }
    }

    private void handleSquareRoot() {
        if (currentInput.length() > 0) {
            double inputValue = Double.parseDouble(currentInput.toString());
            double sqrtValue = Math.sqrt(inputValue);
            currentInput.setLength(0);
            currentInput.append(sqrtValue);
            updateResultTextView();
        }
    }

    private void handlePower() {
        if (currentInput.length() > 0) {
            num1 = Double.parseDouble(currentInput.toString());
            operator = "^";
            clearInput();
        }
    }

    private void handleBackspace() {
        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
            updateResultTextView();
        }
    }


    private void calculate() {
        try {
            String expression = binding.resultTextView.getText().toString();
            BigDecimal result = evaluateExpression(expression);

            // Format the result without scientific notation
            String formattedResult = result.toPlainString();
            binding.resultTextView.setText(formattedResult);
        } catch (Exception e) {
            binding.resultTextView.setText("Error");
        }
    }

    private BigDecimal evaluateExpression(String expression) {
        try {
            return new BigDecimal(String.valueOf(eval(expression)));
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating expression", e);
        }
    }

    private double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean isDigitChar() {
                return (ch >= '0' && ch <= '9');
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if (isDigitChar() || ch == '.') { // numbers
                    while (isDigitChar() || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = evalFunc(func, parseFactor());
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double evalFunc(String func, double val) {
                if (func.equals("sqrt")) return Math.sqrt(val);
                if (func.equals("sin")) return Math.sin(Math.toRadians(val));
                if (func.equals("cos")) return Math.cos(Math.toRadians(val));
                if (func.equals("tan")) return Math.tan(Math.toRadians(val));
                throw new RuntimeException("Unknown function: " + func);
            }
        }.parse();
    }
}