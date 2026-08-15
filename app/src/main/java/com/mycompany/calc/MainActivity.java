package com.mycompany.calc;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText display;
    String currentInput = "";
    double result = 0;
    char pendingOp = ' ';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = findViewById(R.id.display);

        int[] numberIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};

        for (int id : numberIds) {
            Button b = findViewById(id);
            b.setOnClickListener(v -> {
                currentInput += ((Button) v).getText().toString();
                display.setText(currentInput);
            });
        }

        findViewById(R.id.btnDot).setOnClickListener(v -> {
            if (!currentInput.contains(".")) {
                currentInput += ".";
                display.setText(currentInput);
            }
        });

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            currentInput = "";
            result = 0;
            pendingOp = ' ';
            display.setText("0");
        });

        findViewById(R.id.btnPlus).setOnClickListener(v -> applyOp('+'));
        findViewById(R.id.btnMinus).setOnClickListener(v -> applyOp('-'));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> applyOp('*'));
        findViewById(R.id.btnDivide).setOnClickListener(v -> applyOp('/'));

        findViewById(R.id.btnEquals).setOnClickListener(v -> {
            calculate();
            display.setText(String.valueOf(result));
            currentInput = String.valueOf(result);
            pendingOp = ' ';
        });
    }

    void applyOp(char op) {
        if (!currentInput.isEmpty()) {
            calculate();
            pendingOp = op;
            currentInput = "";
        }
    }

    void calculate() {
        double val = currentInput.isEmpty() ? 0 : Double.parseDouble(currentInput);
        switch (pendingOp) {
            case '+': result += val; break;
            case '-': result -= val; break;
            case '*': result *= val; break;
            case '/':
                if (val == 0) {
                    display.setText("Error");
                    result = 0;
                    return;
                }
                result /= val;
                break;
            default: result = val;
        }
    }
}
