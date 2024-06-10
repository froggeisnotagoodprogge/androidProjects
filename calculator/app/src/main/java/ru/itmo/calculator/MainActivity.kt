package ru.itmo.calculator

import android.os.Bundle
import android.view.View.OnLongClickListener
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import ru.itmo.calculator.expressions.Expression
import ru.itmo.calculator.expressions.Number
import ru.itmo.calculator.expressions.Operation
import kotlin.math.ln
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val resultText: TextView = findViewById(R.id.Total)

        var resultTextState: ResultTextState = ResultTextState.OnlyZero
        var commaEntered = false
        var numberOfOpenedBrackets = 0

        val calculationText: TextView = findViewById(R.id.Calculation)

        var x_value = 0.0
        var y_value = 0.0
        var z_value = 0.0
        var isLongClick = false

        val buttons = setButtons()
        for (button in buttons) {
            button.setOnClickListener {
                when (button.text) {
                    "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                        if (resultTextState != ResultTextState.Variables
                            && resultTextState != ResultTextState.ExpressionClosed
                        ) {
                            if (resultTextState == ResultTextState.OnlyZero) {
                                resultText.text =
                                    resultText.text.substring(0, resultText.length() - 1)
                            }

                            resultText.append(button.text)
                            resultTextState = ResultTextState.NumberEnter
                        }
                    }

                    "0" -> {
                        if (resultTextState != ResultTextState.Variables
                            && resultTextState != ResultTextState.ExpressionClosed
                            && resultTextState != ResultTextState.OnlyZero
                        ) {
                            if (resultTextState == ResultTextState.ExpressionEnter
                                || resultTextState == ResultTextState.Operation
                            ) {
                                resultTextState = ResultTextState.OnlyZero
                            }
                            resultText.append(button.text)
                        }
                    }

                    "." -> {
                        if (resultTextState != ResultTextState.Variables
                            && resultTextState != ResultTextState.ExpressionClosed
                            && !commaEntered
                        ) {
                            if (resultTextState == ResultTextState.Operation
                                || resultTextState == ResultTextState.ExpressionEnter
                            ) {
                                resultText.append('0'.toString())
                            }

                            resultText.append(button.text)
                            commaEntered = true
                            resultTextState = ResultTextState.NumberEnter
                        }
                    }

                    "+", "-", "/", "*" -> {
                        if (resultTextState != ResultTextState.ExpressionEnter) {
                            if (resultTextState == ResultTextState.Operation) {
                                resultText.text =
                                    resultText.text.substring(0, resultText.length() - 1)
                            }
                            resultText.append(button.text)
                            resultTextState = ResultTextState.Operation
                            commaEntered = false
                        }
                    }

                    "ln" -> {
                        when (resultTextState) {
                            ResultTextState.Operation,
                            ResultTextState.ExpressionEnter -> {
                                resultText.append(button.text)
                                resultText.append("(")
                                resultTextState = ResultTextState.ExpressionEnter
                                numberOfOpenedBrackets++
                            }

                            ResultTextState.OnlyZero -> {
                                resultText.text =
                                    resultText.text.substring(0, resultText.length() - 1)
                                resultText.append(button.text)
                                resultText.append("(")
                                resultTextState = ResultTextState.ExpressionEnter
                                commaEntered = false
                                numberOfOpenedBrackets++
                            }

                            ResultTextState.NumberEnter,
                            ResultTextState.Variables,
                            ResultTextState.ExpressionClosed -> {
                            }
                        }
                    }

                    "\u221a" -> {
                        when (resultTextState) {
                            ResultTextState.Operation,
                            ResultTextState.ExpressionEnter -> {
                                resultText.append(button.text)
                                resultText.append("(")
                                resultTextState = ResultTextState.ExpressionEnter
                                numberOfOpenedBrackets++
                            }

                            ResultTextState.OnlyZero -> {
                                resultText.text =
                                    resultText.text.substring(0, resultText.length() - 1)
                                resultText.append(button.text)
                                resultText.append("(")
                                resultTextState = ResultTextState.ExpressionEnter
                                commaEntered = false
                                numberOfOpenedBrackets++
                            }

                            ResultTextState.NumberEnter,
                            ResultTextState.Variables,
                            ResultTextState.ExpressionClosed -> {
                            }
                        }
                    }

                    "(" -> {
                        when (resultTextState) {
                            ResultTextState.Operation,
                            ResultTextState.ExpressionEnter -> {
                                resultText.append(button.text)
                                resultTextState = ResultTextState.ExpressionEnter
                                numberOfOpenedBrackets++
                            }

                            ResultTextState.OnlyZero -> {
                                resultText.text =
                                    resultText.text.substring(0, resultText.length() - 1)
                                resultText.append(button.text)
                                resultTextState = ResultTextState.ExpressionEnter
                                commaEntered = false
                                numberOfOpenedBrackets++
                            }

                            ResultTextState.NumberEnter,
                            ResultTextState.Variables,
                            ResultTextState.ExpressionClosed -> {
                            }
                        }
                    }

                    ")" -> {
                        if (resultTextState != ResultTextState.Operation
                            && resultTextState != ResultTextState.ExpressionEnter
                            && numberOfOpenedBrackets > 0
                        ) {
                            resultText.append(button.text)
                            numberOfOpenedBrackets--
                            resultTextState = ResultTextState.ExpressionClosed
                            commaEntered = false
                        }
                    }

                    "x", "y", "z" -> {
                        when (resultTextState) {
                            ResultTextState.NumberEnter,
                            ResultTextState.Variables,
                            ResultTextState.ExpressionClosed -> {
                            }

                            ResultTextState.OnlyZero -> {
                                resultText.text =
                                    resultText.text.substring(0, resultText.length() - 1)
                                resultTextState = ResultTextState.Variables
                                resultText.append(button.text)
                            }

                            ResultTextState.Operation,
                            ResultTextState.ExpressionEnter -> {
                                resultTextState = ResultTextState.Variables
                                resultText.append(button.text)
                            }
                        }

                    }

                    "C" -> {
                        resultText.text = "0"
                        calculationText.text = ""
                        numberOfOpenedBrackets = 0
                        commaEntered = false
                        resultTextState = ResultTextState.OnlyZero
                    }

                    "=" -> {
                        if (numberOfOpenedBrackets > 0) {
                            calculationText.text = resultText.text
                            resultText.text = getString(R.string.error_in_expression)
                        } else {
                            calculationText.text = resultText.text
                            try {
                                resultText.text = solveExpression(
                                    resultText.text.toString(),
                                    x_value,
                                    y_value,
                                    z_value
                                ).toString()
                            } catch (e: IllegalArgumentException) {
                                resultText.text = getString(R.string.division_by_zero_error)
                            }
                        }
                    }
                }
            }
        }

        val buttonX: Button = findViewById(R.id.buttonX)
        val buttonY: Button = findViewById(R.id.buttonY)
        val buttonZ: Button = findViewById(R.id.buttonZ)

        val longClickListener = OnLongClickListener {
            when (it.id) {
                R.id.buttonX -> {
                    x_value = resultText.text.toString().toDouble()
                    true // Return true to indicate that the long click is consumed
                }
                R.id.buttonY -> {
                    y_value = resultText.text.toString().toDouble()
                    true
                }
                R.id.buttonZ -> {
                    z_value = resultText.text.toString().toDouble()
                    true
                }
                else -> false
            }
        }

        buttonX.setOnLongClickListener(longClickListener)
        buttonY.setOnLongClickListener(longClickListener)
        buttonZ.setOnLongClickListener(longClickListener)
    }

    private fun setButtons(): List<Button> {
        val buttonIds = arrayOf(
            R.id.button0,
            R.id.button1,
            R.id.button2,
            R.id.button3,
            R.id.button4,
            R.id.button5,
            R.id.button6,
            R.id.button7,
            R.id.button8,
            R.id.button9,
            R.id.buttonSum,
            R.id.buttonSub,
            R.id.buttonMultiply,
            R.id.buttonDiv,
            R.id.buttonClear,
            R.id.buttonComma,
            R.id.buttonEq,
            R.id.buttonX,
            R.id.buttonY,
            R.id.buttonZ,
            R.id.buttonLn,
            R.id.buttonRoot,
            R.id.buttonLeftBr,
            R.id.buttonRightBr
        )

        val buttons = mutableListOf<Button>()

        for (buttonId in buttonIds) {
            val button: Button = findViewById(buttonId)
            buttons.add(button)
        }
        return buttons
    }
    private fun solveExpression(expressionString: String, x: Double, y: Double, z: Double): Double {
        val expression = Expression()
        expression.expressionString = expressionString
        var currentNumber = Number()
        var i = 0
        while (i < expressionString.length) {
            when (expressionString[i]) {
                '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.' -> {
                    currentNumber.valueInString += expressionString[i]
                    currentNumber.convertValue()
                }

                'x' -> {
                    currentNumber.value = x
                }

                'y' -> {
                    currentNumber.value = y
                }

                'z' -> {
                    currentNumber.value = z
                }

                '+', '-', '*', '/' -> {
                    expression.numbersStack.add(currentNumber)
                    currentNumber = Number()
                    expression.operationsStack.add(Operation(expressionString[i]))
                }

                'l' -> {
                    i += 3
                    currentNumber.value =
                        ln(solveExpression(expressionString.substring(i), x, y, z))
                    var openBrackets = 1

                    while (expressionString[i] != ')' || openBrackets > 1) {
                        if (expressionString[i] == '(') {
                            openBrackets++
                        }
                        if (expressionString[i] == ')') {
                            openBrackets--
                        }
                        i++
                    }
                }

                '\u221a' -> {
                    i += 2
                    currentNumber.value =
                        sqrt(solveExpression(expressionString.substring(i), x, y, z))
                    var openBrackets = 1

                    while (expressionString[i] != ')' || openBrackets > 1) {
                        if (expressionString[i] == '(') {
                            openBrackets++
                        }
                        if (expressionString[i] == ')') {
                            openBrackets--
                        }
                        i++
                    }
                }

                '(' -> {
                    i += 1
                    currentNumber.value = solveExpression(expressionString.substring(i), x, y, z)
                    var openBrackets = 1

                    while (expressionString[i] != ')' || openBrackets > 1) {
                        if (expressionString[i] == '(') {
                            openBrackets++
                        }
                        if (expressionString[i] == ')') {
                            openBrackets--
                        }
                        i++
                    }
                }

                ')' -> i = expressionString.length
            }
            i++
        }
        expression.numbersStack.add(currentNumber)
        solveSimpleExpression(expression)
        return solveSimpleExpression(expression)
    }

    private fun solveSimpleExpression(expression: Expression): Double {
        var i = 0
        while(i < expression.operationsStack.size){
            if (expression.operationsStack[i].operationType == '*'){
                expression.numbersStack[i].value *= expression.numbersStack[i + 1].value
                expression.operationsStack.removeAt(i)
                expression.numbersStack.removeAt(i + 1)
                i--
            }
            else if (expression.operationsStack[i].operationType == '/'){
                if(expression.numbersStack[i + 1].value == 0.0)
                    throw IllegalArgumentException("Division by zero.")
                expression.numbersStack[i].value /= expression.numbersStack[i + 1].value
                expression.operationsStack.removeAt(i)
                expression.numbersStack.removeAt(i + 1)
                i--
            }
            i++
        }
        i = 0
        while(i < expression.operationsStack.size){
            if (expression.operationsStack[i].operationType == '+'){
                expression.numbersStack[i].value += expression.numbersStack[i + 1].value
                expression.operationsStack.removeAt(i)
                expression.numbersStack.removeAt(i + 1)
            }
            else if (expression.operationsStack[i].operationType == '-'){
                expression.numbersStack[i].value -= expression.numbersStack[i + 1].value
                expression.operationsStack.removeAt(i)
                expression.numbersStack.removeAt(i + 1)
            }
        }
        return expression.numbersStack[0].value
    }
}