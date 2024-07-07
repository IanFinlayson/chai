package net.ianfinlayson.chai;

import java.util.List;

public class TypeChecker extends ChaiParserBaseVisitor<Type> {
    // each of these methods performs type checking in their part of the
    // tree, and returns they type of it (for expressions) or null (for stmts)
    // it throws exceptions for type errors that are encountered

    @Override
    public Type visitFunctiondef(ChaiParser.FunctiondefContext ctx) {
        // TODO we should also register this function with its types!
        // for now, just visit all the statements in this funciton, type checking them
        visit(ctx.statements());
        return null;
    }

    @Override
    public Type visitParamlist(ChaiParser.ParamlistContext ctx) {
        // TODO
        return null;
    }

    @Override
    public Type visitNamedParam(ChaiParser.NamedParamContext ctx) {
        // TODO
        return null;
    }

    @Override
    public Type visitDefaultParam(ChaiParser.DefaultParamContext ctx) {
        // TODO
        return null;
    }










    @Override
    public Type visitReturnStatement(ChaiParser.ReturnStatementContext ctx) {
        // TODO
        return null;
    }













    @Override
    public Type visitFunctioncall(ChaiParser.FunctioncallContext ctx) {
        // TODO
        return null;
    }

    @Override
    public Type visitArglist(ChaiParser.ArglistContext ctx) {
        // TODO
        return null;
    }

    @Override
    public Type visitArgument(ChaiParser.ArgumentContext ctx) {
        // TODO
        return null;
    }









    @Override
    public Type visitFloatType(ChaiParser.FloatTypeContext ctx) {
        return new Type(Kind.FLOAT);
    }

    @Override
    public Type visitIntType(ChaiParser.IntTypeContext ctx) {
        return new Type(Kind.INT);
    }

    @Override
    public Type visitStringType(ChaiParser.StringTypeContext ctx) {
        return new Type(Kind.STRING);
    }

    @Override
    public Type visitVoidType(ChaiParser.VoidTypeContext ctx) {
        return null;
    }

    @Override
    public Type visitTupleType(ChaiParser.TupleTypeContext ctx) {
        Type tups = new Type(Kind.TUPLE);

        for (ChaiParser.TypeContext t : ctx.type()) {
            tups.addSub(visit(t));
        }
        return tups;
    }

    @Override
    public Type visitSetType(ChaiParser.SetTypeContext ctx) {
        Type set = new Type(Kind.LIST);
        set.addSub(visit(ctx.type()));
        return set;
    }

    @Override
    public Type visitListType(ChaiParser.ListTypeContext ctx) {
        Type list = new Type(Kind.LIST);
        list.addSub(visit(ctx.type()));
        return list;
    }

    @Override
    public Type visitBoolType(ChaiParser.BoolTypeContext ctx) {
        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitDictType(ChaiParser.DictTypeContext ctx) {
        Type dict = new Type(Kind.DICT);
        dict.addSub(visit(ctx.type(0)));
        dict.addSub(visit(ctx.type(1)));
        return dict;
    }








    @Override
    public Type visitAssignStatement(ChaiParser.AssignStatementContext ctx) {
        // TODO
        return null;
    }

    @Override
    public Type visitNestedLvalue(ChaiParser.NestedLvalueContext ctx) {
        // TODO
        return null;
    }

    @Override
    public Type visitJustID(ChaiParser.JustIDContext ctx) {
        // TODO
        return null;
    }

    @Override
    public Type visitModassign(ChaiParser.ModassignContext ctx) {
        // TODO
        return null;
    }

    @Override
    public Type visitVarStatement(ChaiParser.VarStatementContext ctx) {
        // TODO we also need to keep track of variables and shit

        Type inferred = visit(ctx.expression());
        if (ctx.type() != null) {
            if (!visit(ctx.type()).equals(inferred)) {
                throw new TypeMismatchException("Declared type '" + visit(ctx.type()) +
                        "' does not match assigned type '" + inferred, ctx.getStart().getLine());
            }
        }

        return inferred;
    }











    @Override
    public Type visitAssertStatement(ChaiParser.AssertStatementContext ctx) {
        // get the type of the expression, it must be boolean
        Type exprType = visit(ctx.expression());
        if (exprType.getKind() != Kind.BOOL) {
            throw new TypeMismatchException("assert must be a boolean type", ctx.getStart().getLine());
        }

        return null;
    }

    @Override
    public Type visitForStatement(ChaiParser.ForStatementContext ctx) {
        // TODO we also need to track the variable produced

        // make sure that the expression is iterable
        Type it = visit(ctx.expression());

        switch (it.getKind()) {
            case STRING:
            case LIST:
            case DICT:
            case SET:
                // these are ok
                break;
            default:
                throw new TypeMismatchException("Value in for loop is not iterable", ctx.getStart().getLine());
        }

        // go through all of the statements
        visit(ctx.statements());
        return null;
    }

    @Override
    public Type visitWhileStatement(ChaiParser.WhileStatementContext ctx) {
        // make sure that the expression is a boolean
        Type cond = visit(ctx.expression());
        if (cond.getKind() != Kind.BOOL) {
            throw new TypeMismatchException("While condition must be boolean", ctx.getStart().getLine());
        }

        // go through all of the statements
        visit(ctx.statements());
        return null;
    }

    @Override
    public Type visitIfstmt(ChaiParser.IfstmtContext ctx) {
        // make sure that the expression is a boolean
        Type cond = visit(ctx.expression());
        if (cond.getKind() != Kind.BOOL) {
            throw new TypeMismatchException("If condition must be boolean", ctx.getStart().getLine());
        }

        // go through all of the statements
        visit(ctx.statements());

        // if there is elif clauses and an else clause, visit them
        for (ChaiParser.ElifclauseContext elif : ctx.elifclause()) {
            visit(elif);
        }
        if (ctx.elseclause() != null) {
            visit(ctx.elseclause());
        }

        return null;
    }

    @Override
    public Type visitElifclause(ChaiParser.ElifclauseContext ctx) {
        // make sure that the expression is a boolean
        Type cond = visit(ctx.expression());
        if (cond.getKind() != Kind.BOOL) {
            throw new TypeMismatchException("If condition must be boolean", ctx.getStart().getLine());
        }

        // go through all of the statements
        visit(ctx.statements());
        return null;
    }

    @Override
    public Type visitElseclause(ChaiParser.ElseclauseContext ctx) {
        // go through all of the statements
        visit(ctx.statements());
        return null;
    }

    @Override
    public Type visitShiftExpression(ChaiParser.ShiftExpressionContext ctx) {
        if (visit(ctx.expression(0)).getKind() != Kind.INT ||
            visit(ctx.expression(1)).getKind() != Kind.INT) {
            throw new TypeMismatchException("Shifts only operate on integers", ctx.getStart().getLine());
        }

        return new Type(Kind.INT);
    }

    @Override
    public Type visitBitorExpression(ChaiParser.BitorExpressionContext ctx) {
        if (visit(ctx.expression(0)).getKind() != Kind.INT ||
            visit(ctx.expression(1)).getKind() != Kind.INT) {
            throw new TypeMismatchException("Bitwise or only operates on integers", ctx.getStart().getLine());
        }

        return new Type(Kind.INT);
    }

    @Override
    public Type visitBitxorExpression(ChaiParser.BitxorExpressionContext ctx) {
        if (visit(ctx.expression(0)).getKind() != Kind.INT ||
            visit(ctx.expression(1)).getKind() != Kind.INT) {
            throw new TypeMismatchException("Bitwise xor only operate on integers", ctx.getStart().getLine());
        }

        return new Type(Kind.INT);
    }

    @Override
    public Type visitBitandExpression(ChaiParser.BitandExpressionContext ctx) {
        if (visit(ctx.expression(0)).getKind() != Kind.INT ||
            visit(ctx.expression(1)).getKind() != Kind.INT) {
            throw new TypeMismatchException("Bitwise and only operate on integers", ctx.getStart().getLine());
        }

        return new Type(Kind.INT);
    }

    @Override
    public Type visitNotExpression(ChaiParser.NotExpressionContext ctx) {
        if (visit(ctx.expression()).getKind() != Kind.BOOL) {
            throw new TypeMismatchException("Not only applies to booleans", ctx.getStart().getLine());
        }

        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitOrExpression(ChaiParser.OrExpressionContext ctx) {
        if (visit(ctx.expression(0)).getKind() != Kind.BOOL ||
            visit(ctx.expression(1)).getKind() != Kind.BOOL) {
            throw new TypeMismatchException("Arguments to or operator must be boolean", ctx.getStart().getLine());
        }

        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitAndExpression(ChaiParser.AndExpressionContext ctx) {
        if (visit(ctx.expression(0)).getKind() != Kind.BOOL ||
            visit(ctx.expression(1)).getKind() != Kind.BOOL) {
            throw new TypeMismatchException("Arguments to and operator must be boolean", ctx.getStart().getLine());
        }

        return new Type(Kind.BOOL);
    }

    private boolean numberType(Type t) {
        return t.getKind() == Kind.INT || t.getKind() == Kind.FLOAT;
    }

    @Override
    public Type visitCompareExpression(ChaiParser.CompareExpressionContext ctx) {
        Type lhs = visit(ctx.expression(0));
        Type rhs = visit(ctx.expression(1));

        // we do equality separate from order
        if (ctx.op.getType() == ChaiLexer.EQUALS || ctx.op.getType() == ChaiLexer.NOTEQUALS) {
            // they must match, except float/int is fine
            if (lhs.getKind() != rhs.getKind()) {
                if (!numberType(lhs) || !numberType(rhs)) {
                    throw new TypeMismatchException("Unsupported types to equality operator", ctx.getStart().getLine());
                }
            }
        } else {
            // we only allow these for numbers and strings
            if (lhs.getKind() == Kind.STRING && rhs.getKind() == Kind.STRING) {
                // this is ok
            } else if (numberType(lhs) && numberType(rhs)) {
                // this is ok
            } else {
                throw new TypeMismatchException("Unsupported types to comparison operator", ctx.getStart().getLine());
            }
        }

        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitPlusMinusExpression(ChaiParser.PlusMinusExpressionContext ctx) {
        Type lhs = visit(ctx.expression(0));
        Type rhs = visit(ctx.expression(1));

        if (ctx.op.getType() == ChaiLexer.PLUS) {
            if (lhs.getKind() == Kind.LIST && rhs.getKind() == Kind.LIST) {
                // must be same type of list
                if (!lhs.equals(rhs)) {
                    throw new TypeMismatchException("List types for + operator do not match", ctx.getStart().getLine());
                }
                return lhs;
            } else if (lhs.getKind() == Kind.STRING && rhs.getKind() == Kind.STRING) {
                return new Type(Kind.STRING);
            } else if (lhs.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
                return new Type(Kind.INT);
            } else if (numberType(lhs) && numberType(rhs)) {
                return new Type(Kind.FLOAT);
            } else {
                throw new TypeMismatchException("Unsupported types to + operator", ctx.getStart().getLine());
            }
        } else {
            // subtraction
            if (lhs.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
                return new Type(Kind.INT);
            } else if (numberType(lhs) && numberType(rhs)) {
                return new Type(Kind.FLOAT);
            } else {
                throw new TypeMismatchException("Unsupported types to - operator", ctx.getStart().getLine());
            }
        }
    }

    @Override
    public Type visitTimesdivExpression(ChaiParser.TimesdivExpressionContext ctx) {
        Type lhs = visit(ctx.expression(0));
        Type rhs = visit(ctx.expression(1));

        switch (ctx.op.getType()) {
            case ChaiLexer.TIMES:
                // we can do list * num, or string * num
                if ((lhs.getKind() == Kind.LIST || lhs.getKind() == Kind.STRING) && rhs.getKind() == Kind.INT) {
                    return lhs;
                } else if (lhs.getKind() == Kind.INT && (rhs.getKind() == Kind.LIST || rhs.getKind() == Kind.STRING)) {
                    return rhs;
                } else if (lhs.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
                    return new Type(Kind.INT);
                } else if (numberType(lhs) && numberType(rhs)) {
                    return new Type(Kind.FLOAT);
                } else {
                    throw new TypeMismatchException("Unsupported types to * operator", ctx.getStart().getLine());
                }

            case ChaiLexer.DIVIDE:
                if (numberType(lhs) && numberType(rhs)) {
                    return new Type(Kind.FLOAT);
                } else {
                    throw new TypeMismatchException("Unsupported types to / operator", ctx.getStart().getLine());
                }
            case ChaiLexer.INTDIV:
                if (numberType(lhs) && numberType(rhs)) {
                    return new Type(Kind.INT);
                } else {
                    throw new TypeMismatchException("Unsupported types to // operator", ctx.getStart().getLine());
                }
            case ChaiLexer.MODULUS:
                if (lhs.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
                    return new Type(Kind.INT);
                } else if (numberType(lhs) && numberType(rhs)) {
                    return new Type(Kind.FLOAT);
                } else {
                    throw new TypeMismatchException("Unsupported types to % operator", ctx.getStart().getLine());
                }
            default:
                throw new RuntimeException("Unhandled multiplicative operator in type checker");
        }
    }

    @Override
    public Type visitPowerExpression(ChaiParser.PowerExpressionContext ctx) {
        Type lhs = visit(ctx.expression(0));
        Type rhs = visit(ctx.expression(1));

        if (lhs.getKind() == Kind.INT && rhs.getKind() == Kind.INT) {
            return new Type(Kind.INT);
        } else if (numberType(lhs) && numberType(rhs)) {
            return new Type(Kind.FLOAT);
        } else {
            throw new TypeMismatchException("Unsupported types to % operator", ctx.getStart().getLine());
        }
    }

    @Override
    public Type visitUnaryExpression(ChaiParser.UnaryExpressionContext ctx) {
        Type operand = visit(ctx.expression());

        switch (ctx.op.getType()) {
            case ChaiLexer.PLUS:
            case ChaiLexer.MINUS:
                if (operand.getKind() == Kind.INT) {
                    return new Type(Kind.INT);
                } else if (operand.getKind() == Kind.FLOAT) {
                    return new Type(Kind.FLOAT);
                } else {
                    throw new TypeMismatchException("Unsupported types to % operator", ctx.getStart().getLine());
                }
            case ChaiLexer.COMPLEMENT:
                if (operand.getKind() != Kind.INT) {
                    throw new TypeMismatchException("Unsupported type to ~ operator", ctx.getStart().getLine());
                } else {
                    return new Type(Kind.INT);
                }
            default:
                throw new RuntimeException("Unhandled unary operator in type checker");
        }
    }

    // private method used for 'in' and 'not in' checks
    private Type checkInExpression(Type item, Type collection, int line) {
        System.out.println(item);
        System.out.println(collection);

        switch (collection.getKind()) {
            case STRING:
                // the item must be a string
                if (item.getKind() != Kind.STRING) {
                    throw new TypeMismatchException("Unsupported types in 'in' expression", line);
                }
                break;
            case LIST:
            case SET:
            case DICT:
                // the item must match the (first) subtype
                // TODO make sure this works for empty things?
                if (!item.equals(collection.getSubs().get(0))) {
                    System.out.println(item);
                    System.out.println(collection.getSubs().get(0));
                    throw new TypeMismatchException("Search type does not match collection type in 'in' expression", line);
                }
                break;

            default:
                throw new TypeMismatchException("Unsupported types in 'in' expression", line);
        }

        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitNotinExpression(ChaiParser.NotinExpressionContext ctx) {
        return checkInExpression(visit(ctx.expression(0)), visit(ctx.expression(1)), ctx.getStart().getLine());
    }

    @Override
    public Type visitInExpression(ChaiParser.InExpressionContext ctx) {
        return checkInExpression(visit(ctx.expression(0)), visit(ctx.expression(1)), ctx.getStart().getLine());
    }

    @Override
    public Type visitIfelseExpression(ChaiParser.IfelseExpressionContext ctx) {
        // grab the three expressions here
        Type ifpart = visit(ctx.expression(0));
        Type cond = visit(ctx.expression(1));
        Type elsepart = visit(ctx.expression(2));

        // the condition must be boolean
        if (cond.getKind() != Kind.BOOL) {
            throw new TypeMismatchException("Condition of if expression must be boolean", ctx.getStart().getLine());
        }

        // the if and then parts must match
        if (!ifpart.equals(elsepart)) {
            throw new TypeMismatchException("If and ele clauses of if expression must be of same type", ctx.getStart().getLine());
        }

        // the result is the type of the parts
        return ifpart;
    }

    @Override
    public Type visitConsExpression(ChaiParser.ConsExpressionContext ctx) {
        // get the two expression types
        Type elem = visit(ctx.expression(0));
        Type existing = visit(ctx.expression(1));

        // the existing must be a list
        if (existing.getKind() != Kind.LIST) {
            throw new TypeMismatchException("Cons operator can only apply to list type", ctx.getStart().getLine());
        }

        // either the existing list is empty or the types match
        if (existing.getSubs() == null || elem.equals(existing.getSubs().get(0))) {
            // the type is the same as the existing list
            return existing;
        } else {
            throw new TypeMismatchException("Cannot cons item to list of different type", ctx.getStart().getLine());
        }
    }









    @Override
    public Type visitListcompTerm(ChaiParser.ListcompTermContext ctx) {
        // TODO
        return null;
    }

    @Override
    public Type visitListRangeTerm(ChaiParser.ListRangeTermContext ctx) {
        // TODO
        return null;
    }

    @Override
    public Type visitListSliceTerm(ChaiParser.ListSliceTermContext ctx) {
        // TODO
        return null;
    }

    @Override
    public Type visitListIndexTerm(ChaiParser.ListIndexTermContext ctx) {
        Type collection = visit(ctx.term());
        Type index = visit(ctx.expression());

        switch (collection.getKind()) {
            case STRING:
                // the index must be an int
                if (index.getKind() != Kind.INT) {
                    throw new TypeMismatchException("Index into string must be integer type", ctx.getStart().getLine());
                }
                // it returns a string
                return new Type(Kind.STRING);

            case LIST:
                // the index must be an int
                if (index.getKind() != Kind.INT) {
                    throw new TypeMismatchException("Index into string must be integer type", ctx.getStart().getLine());
                }
                // it returns the type of the list
                return collection.getSubs().get(0);
            case DICT:
                // the index must be the first subtype
                if (!index.equals(collection.getSubs().get(0))) {
                    throw new TypeMismatchException("Type of dictionary key does not match", ctx.getStart().getLine());
                }
                // it returns the second subtype
                return collection.getSubs().get(1);

            default:
                throw new TypeMismatchException("Unsupported collection type in index expression", ctx.getStart().getLine());
        }
    }

    @Override
    public Type visitEmptydictLiteralTerm(ChaiParser.EmptydictLiteralTermContext ctx) {
        return new Type(Kind.DICT);
    }

    @Override
    public Type visitSetLiteralTerm(ChaiParser.SetLiteralTermContext ctx) {
        // get the items in the set
        List<ChaiParser.ExpressionContext> entries = ctx.expression();

        // get the type for the first entry
        Type first = visit(entries.get(0));

        // for each other one, ensure that it matches
        for (int i = 1; i < entries.size(); i++) {
            if (!visit(entries.get(i)).equals(first)) {
                throw new TypeMismatchException("Set literal has inconsistent type", ctx.getStart().getLine());
            }
        }

        // it's a set of whatever the thing is then
        Type set = new Type(Kind.SET);
        set.addSub(first);
        return set;
    }

    @Override
    public Type visitListLiteralTerm(ChaiParser.ListLiteralTermContext ctx) {
        // get the items in the list
        List<ChaiParser.ExpressionContext> entries = ctx.expression();

        // if the list is empty return list w/o subtype
        if (entries.size() == 0) {
            return new Type(Kind.LIST);
        }
        
        // get the type for the first entry
        Type first = visit(entries.get(0));

        // for each other one, ensure that it matches
        for (int i = 1; i < entries.size(); i++) {
            if (!visit(entries.get(i)).equals(first)) {
                throw new TypeMismatchException("List literal has inconsistent type", ctx.getStart().getLine());
            }
        }

        // it's a list of whatever the thing is then
        Type lst = new Type(Kind.LIST);
        lst.addSub(first);
        return lst;
    }

    @Override
    public Type visitTupleLiteralTerm(ChaiParser.TupleLiteralTermContext ctx) {
        // make a tuple type
        Type tups = new Type(Kind.TUPLE);

        // add all the things into it
        for (ChaiParser.ExpressionContext expr : ctx.expression()) {
            tups.addSub(visit(expr));
        }

        return tups;
    }

    @Override
    public Type visitDictLiteralTerm(ChaiParser.DictLiteralTermContext ctx) {
        List<ChaiParser.DictentryContext> entries = ctx.dictentry();

        // get the dict type for the first entry here
        Type first = visit(entries.get(0));

        // for each other one, ensure that it matches
        for (int i = 1; i < entries.size(); i++) {
            if (!visit(entries.get(i)).equals(first)) {
                throw new TypeMismatchException("Dictionary literal has inconsistent type", ctx.getStart().getLine());
            }
        }

        // return dictionary type
        return first;
    }

    @Override
    public Type visitDictentry(ChaiParser.DictentryContext ctx) {
        // visit both of the expressions
        Type dict = new Type(Kind.DICT);
        dict.addSub(visit(ctx.expression(0)));
        dict.addSub(visit(ctx.expression(1)));
        return dict;
    }

    @Override
    public Type visitIntLiteral(ChaiParser.IntLiteralContext ctx) {
        return new Type(Kind.INT);
    }

    @Override
    public Type visitFloatLiteral(ChaiParser.FloatLiteralContext ctx) {
        return new Type(Kind.FLOAT);
    }

    @Override
    public Type visitStringLiteral(ChaiParser.StringLiteralContext ctx) {
        return new Type(Kind.STRING);
    }

    @Override
    public Type visitTrueLiteral(ChaiParser.TrueLiteralContext ctx) {
        return new Type(Kind.BOOL);
    }

    @Override
    public Type visitFalseLiteral(ChaiParser.FalseLiteralContext ctx) {
        return new Type(Kind.BOOL);
    }
}

