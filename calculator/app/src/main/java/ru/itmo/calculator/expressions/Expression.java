package ru.itmo.calculator.expressions;

import java.util.ArrayList;

public class Expression{

    public ArrayList<Number> numbersStack;
    public ArrayList<Operation> operationsStack;
    public String expressionString;

    public Expression() {
        this.numbersStack = new ArrayList<>();
        this.operationsStack = new ArrayList<>();
    }

    public double solve(){
        return 0;
    }
}
