package lox;

import lox.Expr.Binary;
import lox.Expr.Grouping;
import lox.Expr.Literal;
import lox.Expr.Ternary;
import lox.Expr.Unary;

public class Interpreter implements Expr.Visitor<Object> {

    void interpret(Expr expression) {
        try {
            Object value = evalute(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private String stringify(Object object) {
        if (object == null)
            return null;

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evalute(expr.left);
        Object right = evalute(expr.right);

        switch (expr.operator.type) {
            case COMMA: // TODO: Check here for future implementations.
                return right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return !isEqual(left, right);
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                if ((left instanceof String && right instanceof Double)
                        || (left instanceof Double && right instanceof String)) {
                    return stringify(left) + stringify(right);
                }
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two number or strings");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
        }
        return null;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;
        return a.equals(b);
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evalute(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitTernaryExpr(Ternary expr) {
        Object left = evalute(expr.left);
        Object middle = evalute(expr.middle);
        Object right = evalute(expr.right);

        if (isTruthy(left))
            return middle;
        return right;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evalute(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }
        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private Object evalute(Expr expression) {
        return expression.accept(this);
    }

}
