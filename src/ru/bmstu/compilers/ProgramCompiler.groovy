package ru.bmstu.compilers
/**
 * Created by ali on 17.02.17.
 */

class ProgramCompiler {
    String input;
    int p;
    public ProgramCompiler(String str) {
        this.input = str;
    }

    def parse() {
        def res =  program()
        return res
    }

    def printErr(String calledMethod) {
        throw new Exception("Error at pos $p in method $calledMethod")
    }

    String checkTerminal(String str) {
        if (p + str.length() - 1 < input.length() &&
                input[p..p+str.length() - 1] == str) {
            p += str.length()
            return str
        } else return null
    }

    String checkTerminals(List<String> terminals) {
        for (String terminal: terminals)
            if (checkTerminal(terminal))
                return terminal
        return null
    }

    def program() {
        return [ "Program0": block() ];
    }

    def block() {
        def operators
        if (checkTerminal("begin_") && (operators = operatorList()) && checkTerminal("_end"))
            return ["Block0": ["begin_", operators, "_end"]];
        printErr("block")
    }

    def operatorList() {
        def operator = operator()
        if (operator) {
            def operators
            if (checkTerminal(";"))
                if ((operators = operatorList()))
                    return ["OperatorList1": [operator, ";", operators]]
                else
                    printErr("operatorList")
            else
                return ["OperatorList0": operator]
        }
        printErr("operatorList")
    }

    def operator() {
        def expression
        if (checkTerminal("Identifier") && checkTerminal("=") && (expression = expr()))
            return ["Operator0": ["Identifier", "=", expression]]
        printErr("operator")
    }

    def expr() {
        def arithexpr, relationper, _term
        if ((arithexpr = arithExpr()) && (relationper = relationOper()) && (_term = term()))
            return ["Expr0": [arithexpr, relationper, _term]]
        printErr("expr")
    }

    def arithExpr() {
        def arithexpr, _term
        if ((arithexpr = arithExprTail(true)))
            return arithexpr
        else if ((_term = term())) {
            if ((arithexpr = arithExprTail(true)))
                return ["ArithExpr3": [_term, arithexpr]]
            else
                return ["ArithExpr0": _term]
        }
    }

    def arithExprTail(boolean tail = false) {
        def sumsign, _term, arithexpr
        if ((sumsign = sumSign(tail))) {
            if ((_term = term()))
                if ((arithexpr = arithExprTail(true)))
                    return ["ArithExpr2": [sumsign, _term, arithexpr]]
                else
                    return ["ArithExpr1": [sumsign, _term]]
            else
                printErr("arithExpr")
        } else
            if (!tail)
                printErr("arithExpr")
            else
                return null;
    }

    def term() {
        def _multiplier, multisign, _term
        if ((_multiplier = multiplier())) {
            if ((multisign = multiSign(true)))
                if ((_term = term()))
                    return ["Term1": [_multiplier, multisign, _term]]
                else
                    printErr("term")
            return ["Term0": _multiplier]
        }
        printErr("term")
    }

    def multiplier() {
        def primaryexpr, _multiplier
        if ((primaryexpr = primaryExpr())) {
            if (checkTerminal("^"))
                if ((_multiplier = multiplier()))
                    return ["Multiplier1": [primaryexpr, "^", _multiplier]]
                else
                    printErr("multiplier")
            else
                return ["Multiplier0": primaryexpr]

        }
        printErr("multiplier")
    }

    def primaryExpr() {
        if (checkTerminal("Digit"))
            return ["PrimaryExpr0": "Digit"]
        else if (checkTerminal("Identifier"))
            return ["PrimaryExpr1": "Identifier"]
        else {
            def arith
            if (checkTerminal("(") && (arith = arithExpr()) && checkTerminal(')'))
                return ["PrimaryExpr2": ["(", arith, ")"]]
            printErr("primaryExpr")
        }
    }

    def sumSign(boolean withoutErr = false) {
        String selectedTerminal
        if ((selectedTerminal = checkTerminals(["+", "-"]))) {
            return ["SumSign0": selectedTerminal]
        }
        if (!withoutErr)
            printErr("sumSign")
        return null
    }

    def multiSign(boolean withoutErr = false) {
        String selectedTerminal
        if ((selectedTerminal = checkTerminals(["*", "/", "%"]))) {
            return ["MultiSign0": selectedTerminal]
        }
        if (!withoutErr)
            printErr("multiSign")
        return null
    }

    def relationOper(boolean withoutErr = false) {
        String selectedTerminal
        if ((selectedTerminal = checkTerminals(["<", "<=", "=", ">=", ">", "<>"]))) {
            return ["RelationOper0": selectedTerminal]
        }
        if (!withoutErr)
            printErr("relationOper")
        return null
    }
}
