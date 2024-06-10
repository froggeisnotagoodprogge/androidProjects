package ru.itmo.calculator.expressions;

public class Number{
    private double value;
    public String valueInString;

    public Number() {
        value = 0;
        valueInString = "";
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void convertValue(){     // from string to double
        value = Double.parseDouble(valueInString);
    }
}
