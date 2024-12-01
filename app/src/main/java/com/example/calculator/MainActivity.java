package com.example.calculator;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.mariuszgromada.math.mxparser.Expression;

public class MainActivity extends AppCompatActivity {

    private TextView expressionView, resultView;
    private String currentExpression = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Устанавливаем соответствующую разметку в зависимости от ориентации экрана
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.actiivity_main_land); // Ландшафтная ориентация
        } else {
            setContentView(R.layout.activity_main); // Портретная ориентация
        }

        // Инициализация полей
        expressionView = findViewById(R.id.expression);
        resultView = findViewById(R.id.result);

        // Восстанавливаем состояние после поворота экрана, если доступно
        if (savedInstanceState != null) {
            currentExpression = savedInstanceState.getString("expression", "");
            expressionView.setText(currentExpression);
            resultView.setText(savedInstanceState.getString("result", ""));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Сохраняем текущее выражение и результат
        outState.putString("expression", currentExpression);
        outState.putString("result", resultView.getText().toString());
    }

    // Метод для обработки нажатий на цифры
    public void onNumberClick(View view) {
        Button button = (Button) view;
        currentExpression += button.getText().toString();
        expressionView.setText(currentExpression);
    }

    // Метод для обработки операторов
    public void onOperatorClick(View view) {
        Button button = (Button) view;
        String operator = button.getText().toString();

        if (!currentExpression.isEmpty() && !isLastCharOperator()) {
            currentExpression += " " + operator + " ";
            expressionView.setText(currentExpression);
        }
    }

    // Метод для очистки всего
    public void onClearClick(View view) {
        currentExpression = "";
        expressionView.setText("");
        resultView.setText("");
    }

    // Метод для удаления последнего символа
    public void onDeleteClick(View view) {
        if (!currentExpression.isEmpty()) {
            currentExpression = currentExpression.substring(0, currentExpression.length() - 1);
            expressionView.setText(currentExpression);
        }
    }

    // Метод для точки
    public void onDotClick(View view) {
        if (!currentExpression.isEmpty() && !isLastCharDot()) {
            currentExpression += ".";
            expressionView.setText(currentExpression);
        }
    }

    // Метод для кнопки равно
    public void onEqualsClick(View view) {
        if (currentExpression.isEmpty()) {
            resultView.setText("Введите выражение");
            return;
        }

        // Проверяем наличие деления на ноль
        if (containsDivisionByZero(currentExpression)) {
            resultView.setText("Ошибка: деление на 0");
            return;
        }

        try {
            // Форматируем выражение для mXparser
            String formattedExpression = currentExpression
                    .replace("×", "*")
                    .replace("÷", "/");

            Expression expression = new Expression(formattedExpression);
            double result = expression.calculate();

            // Проверяем результат на корректность
            if (Double.isNaN(result)) {
                resultView.setText("Ошибка: некорректное выражение или деление на 0");
            } else if (Double.isInfinite(result)) {
                resultView.setText("Ошибка: результат слишком велик");
            } else {
                resultView.setText(String.valueOf(result));
            }
        } catch (Exception e) {
            // Ловим все другие непредвиденные ошибки
            if (!bracketsBalanced()) {
                resultView.setText("Ошибка: несбалансированные скобки");
            } else resultView.setText("Ошибка: " + e.getMessage());
        }
    }

    // Метод для обработки функций
    public void onFunctionClick(View view) {
        Button button = (Button) view;
        String function = button.getText().toString();

        if (function.equals("π") || function.equals("e")) {
            // Если функция — это константа
            if (!currentExpression.isEmpty() && (isLastCharNumber() || isLastCharRightParenthesis())) {
                currentExpression += "×";
            }
            currentExpression += function;
        } else if (function.equals("^")) {
            // Если функция — это степень
            if (!currentExpression.isEmpty() && (isLastCharNumber() || isLastCharRightParenthesis())) {
                currentExpression += "^";
            }
        } else {
            // Обычные функции
            if (currentExpression.isEmpty() || isLastCharOperator() || isLastCharLeftParenthesis()) {
                currentExpression += function + "(";
            } else if (isLastCharNumber() || isLastCharRightParenthesis()) {
                currentExpression += "×" + function + "(";
            } else {
                currentExpression += function + "(";
            }
        }

        expressionView.setText(currentExpression);
    }

    // Метод для обработки скобок
    public void onBracketClick(View view) {
        Button button = (Button) view;
        String bracket = button.getText().toString();

        if (bracket.equals("(")) {
            if (currentExpression.isEmpty() || isLastCharOperator() || isLastCharLeftParenthesis()) {
                // Если выражение пустое, последний символ оператор или открывающая скобка
                currentExpression += "(";
            } else if (isLastCharNumber() || isLastCharRightParenthesis()) {
                // Если перед скобкой число или закрывающая скобка, добавляем умножение
                currentExpression += "×(";
            } else {
                currentExpression += "(";
            }
        } else if (bracket.equals(")")) {
            if (!currentExpression.isEmpty() && !isLastCharOperator() && bracketsBalanced()) {
                currentExpression += ")";
            }
        }

        expressionView.setText(currentExpression);
    }

    // Вспомогательные методы
    private boolean isLastCharOperator() {
        return currentExpression.endsWith("+") || currentExpression.endsWith("-")
                || currentExpression.endsWith("×") || currentExpression.endsWith("÷");
    }

    // Метод для проверки деления на 0 в выражении
    private boolean containsDivisionByZero(String expression) {
        // Проверяем, есть ли в выражении деление на ноль, учитывая возможные пробелы и разные операторы
        return expression.matches(".*[÷/]\\s*0.*");
    }

    private boolean isLastCharDot() {
        return currentExpression.endsWith(".");
    }

    private boolean isLastCharLeftParenthesis() {
        return currentExpression.endsWith("(");
    }

    private boolean isLastCharRightParenthesis() {
        return currentExpression.endsWith(")");
    }

    private boolean isLastCharNumber() {
        return !currentExpression.isEmpty() && Character.isDigit(currentExpression.charAt(currentExpression.length() - 1));
    }

    private boolean bracketsBalanced() {
        int openCount = 0, closeCount = 0;
        for (char c : currentExpression.toCharArray()) {
            if (c == '(') openCount++;
            if (c == ')') closeCount++;
        }
        return openCount > closeCount;
    }
}

