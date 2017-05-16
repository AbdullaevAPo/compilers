package compilers.lab5

import compilers.Grammar

/**
 * Created by ali on 13.05.17.
 */
enum PRECEDENCE_OPERATOR {EQUAL, LESS, LARGER, LARGER_MARKER, LESS_MARKER, NO }
class OperatorPrecedenceMatrixBuilder {
    Grammar grammar
    final String marker = '$'

    OperatorPrecedenceMatrixBuilder(Grammar grammar) {
        this.grammar = grammar
    }

    LinkedHashMap<String, LinkedHashMap<String, PRECEDENCE_OPERATOR>> buildMatrix() {
        def matrix = [:]

        for (terminal1 in grammar.getTerminals() + marker) {
            matrix[terminal1] = [:]
            for (terminal2 in grammar.getTerminals() + marker) {
                if (terminal1 == marker) {
                    def isLess = terminal2 in followTerminals(grammar.getStartSymbol())
                    matrix[terminal1][terminal2] = !isLess ? PRECEDENCE_OPERATOR.NO : PRECEDENCE_OPERATOR.LESS
                    continue
                }
                if (terminal2 == marker) {
                    def isLarger = terminal1 in previewTerminals(grammar.getStartSymbol())
                    matrix[terminal1][terminal2] = !isLarger ? PRECEDENCE_OPERATOR.NO : PRECEDENCE_OPERATOR.LARGER
                    continue
                }
                def operators = []
                operators << extractEqual(terminal1, terminal2)
                operators << extractLess(terminal1, terminal2)
                operators << extractLarger(terminal1, terminal2)
                operators = operators.toSet() - null
                if (operators.size() > 1)
                    throw new Exception("2 or more precedence operators for nonterminal")
                matrix[terminal1][terminal2] = !operators.size() ? PRECEDENCE_OPERATOR.NO : operators.first()
            }
        }
        return matrix
    }

    private PRECEDENCE_OPERATOR extractEqual(String terminal1, String terminal2) {
        for (def e: grammar.rules) {
            for (def rule: e.value) {
                boolean firstFinded = false
                for (String ruleElem: rule) {
                    if (ruleElem == terminal2 && firstFinded) {
                        return PRECEDENCE_OPERATOR.EQUAL
                    } else if (firstFinded && grammar.getTerminals().contains(ruleElem)) {
                        return null
                    } else if (ruleElem == terminal1) {
                        firstFinded = true
                    }
                }
            }
        }
        return null
    }

    private PRECEDENCE_OPERATOR extractLess(String terminal1, String terminal2) {
        for (a in grammar.getRules()) {
            for (arule in a.value) {
                for (int k=0; k<arule.size() - 1; k++)
                    if (arule[k] == terminal1 && arule[k+1] in grammar.getNonTerminals())
                        if (terminal2 in followTerminals(arule[k+1]))
                            return PRECEDENCE_OPERATOR.LESS
            }
        }
        return null
    }

    private PRECEDENCE_OPERATOR extractLarger(String terminal1, String terminal2) {
        for (b in grammar.getRules()) {
            for (brule in b.value) {
                for (int k=1; k<brule.size(); k++)
                    if (brule[k] == terminal2 && brule[k-1] in grammar.getNonTerminals())
                        if (terminal1 in previewTerminals(brule[k-1]))
                            return PRECEDENCE_OPERATOR.LARGER
            }
        }
        return null
    }

    Set<String> followTerminals(String nonTerminal, Set<List<String>> visitedRules = []) {
        Set<String> res = []
        for (rule in grammar.getRules()[nonTerminal]) {
            if (rule in visitedRules)
                continue
            visitedRules.add(rule)
            if (rule.first() in grammar.getNonTerminals()) {
                if (rule.size() > 1)
                    if (rule.get(1) in grammar.getTerminals())
                        res.add(rule.get(1))
                res.addAll(followTerminals(rule.first(), visitedRules))
            } else
                res.add(rule.first())
        }
        return res
    }

    Set<String> previewTerminals(String nonTerminal, Set<List<String>> visitedRules = []) {
        Set<String> res = []
        for (rule in grammar.getRules()[nonTerminal]) {
            if (rule in visitedRules)
                continue
            visitedRules.add(rule)
            if (rule.last() in grammar.getNonTerminals()) {
                if (rule.size() > 1)
                    if (rule.get(rule.size()-2) in grammar.getTerminals())
                        res.add(rule.get(rule.size()-2))
                res.addAll(previewTerminals(rule.last(), visitedRules))
            } else
                res.add(rule.last())
        }
        return res
    }
}
