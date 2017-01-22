package com.github.kmizu.nub;

import java.util.*;

public class JavaScriptGenerator implements AstNode.ExpressionVisitor<Object> {

    private Set<String> functions = new HashSet<>();

    private StringBuilder stringBuilder = new StringBuilder();


    public Object visitBinaryOperation(AstNode.BinaryOperation node) {
        switch (node.operator()) {
            case "+":
            case "-":
            case "*":
            case "<=":
            case ">=":
            case "<":
            case ">":
            case "&&":
            case "||":
                stringBuilder.append('(');
                node.lhs().accept(this);
                stringBuilder.append(node.operator());
                node.rhs().accept(this);
                stringBuilder.append(')');
                return null;
            case "/":
                stringBuilder.append("((");
                node.lhs().accept(this);
                stringBuilder.append(node.operator());
                node.rhs().accept(this);
                stringBuilder.append("|0)");
                return null;
            case "==":
                stringBuilder.append('(');
                node.lhs().accept(this);
                stringBuilder.append("===");
                node.rhs().accept(this);
                stringBuilder.append(')');
                return null;
            case "!=":
                stringBuilder.append('(');
                node.lhs().accept(this);
                stringBuilder.append("!==");
                node.rhs().accept(this);
                stringBuilder.append(')');
                return null;
            default:
                throw new RuntimeException("cannot reach here");
        }
    }

    @Override
    public Object visitNumber(AstNode.Number node) {
        stringBuilder.append(node.value());
        return null;
    }

    @Override
    public Object visitLetExpression(AstNode.LetExpression node) {
        stringBuilder.append("let ");
        stringBuilder.append(node.variableName());
        stringBuilder.append(" = ");
        Object value = node.expression().accept(this);
        return null;
    }

    @Override
    public Object visitAssignmentOperation(AstNode.AssignmentOperation node) {
        stringBuilder.append("(");
        stringBuilder.append(node.variableName());
        stringBuilder.append(" = ");
        Object value = node.expression().accept(this);
        stringBuilder.append(")");
        return null;
    }

    @Override
    public Object visitPrintExpression(AstNode.PrintExpression node) {
        stringBuilder.append("console.log(");
        node.target().accept(this);
        stringBuilder.append(");");
        return null;
    }

    @Override
    public Object visitPrintlnExpression(AstNode.PrintlnExpression node) {
        stringBuilder.append("console.log(");
        node.target().accept(this);
        stringBuilder.append(")");
        return null;
    }

    private void visitExpressions(List<AstNode.Expression> nodes)
    {

        for (int i = 0, n = nodes.size(); i < n; ++i) {
            AstNode.Expression e = nodes.get(i);
            if(i < n - 1)
            {
                e.accept(this);
            }
            else {
                if (e instanceof AstNode.DefFunction) {
                    e.accept(this);
                    stringBuilder.append(";");
                    stringBuilder.append("__last__ = ");
                    stringBuilder.append(((AstNode.DefFunction) e).name());
                } else if (e instanceof AstNode.LetExpression) {
                    e.accept(this);
                    stringBuilder.append(";");
                    stringBuilder.append("__last__ = ");
                    stringBuilder.append(((AstNode.LetExpression) e).variableName());
                } else {
                    stringBuilder.append("__last__ = ");
                    e.accept(this);
                }
            }
            stringBuilder.append(";");
        }
    }

    @Override
    public Object visitExpressionList(AstNode.ExpressionList node) {
        stringBuilder.append("(function(){ let __last__ = 0;"); {
            visitExpressions(node.expressions());
            stringBuilder.append("return __last__;})();");
        }
        return null;
    }

    @Override
    public Object visitWhileExpression(AstNode.WhileExpression node) {
        stringBuilder.append("(function(){ let __last__ = 0;");
        {
            stringBuilder.append("while( ");
            node.condition().accept(this);
            stringBuilder.append(" ){");
            {
                visitExpressions(node.body());
                stringBuilder.append("}");
            }
            stringBuilder.append("return __last__;})()");
        }
        return null;
    }

    @Override
    public Object visitIfExpression(AstNode.IfExpression node) {
        stringBuilder.append("(function(){ let __last__ = 0;"); {
            stringBuilder.append("if( ");
            node.condition().accept(this);
            stringBuilder.append(" ){");
            {
                visitExpressions(node.thenClause());
                stringBuilder.append("}");
            }
            if(node.elseClause().size() > 0) {
                stringBuilder.append("else{"); {
                    visitExpressions(node.elseClause());
                    stringBuilder.append("}");
                }
            }
            stringBuilder.append("return __last__;})()");
        }
        return null;
    }

    @Override
    public Object visitDefFunction(AstNode.DefFunction node) {
        stringBuilder.append("let ");
        stringBuilder.append(node.name());
        stringBuilder.append(" = function(");
        boolean isFirst = true;
        for(String e:node.args()) {
            stringBuilder.append(e);
            if(isFirst){
                isFirst = false;
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("){ let __last__ = 0;"); {
            visitExpressions(node.body());
            stringBuilder.append("return __last__;}");
        }
        return null;
    }

    @Override
    public Object visitIdentifier(AstNode.Identifier node) {
        stringBuilder.append(node.name());
        return null;
    }

    public Object evaluate(AstNode.ExpressionList program) {
        stringBuilder = new StringBuilder();
        program.accept(this);
        return stringBuilder.toString();
    }

    @Override
    public Object visitFunctionCall(AstNode.FunctionCall node) {
        stringBuilder.append("(");
        node.name().accept(this);
        stringBuilder.append(")(");
        boolean isFirst = true;
        for(AstNode.Expression e:node.params()) {
            e.accept(this);
            if(isFirst){
                isFirst = false;
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(")");
        return null;
    }

    @Override
    public Object visitReturn(AstNode.Return node) {
        stringBuilder.append("return ");
        node.expression().accept(this);
        stringBuilder.append(";");
        return null;
    }
}
