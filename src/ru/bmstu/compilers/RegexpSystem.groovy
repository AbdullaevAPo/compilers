package ru.bmstu.compilers

/**
 * Created by ali on 18.02.17.
 */
class RegexpSystem {

    List<Map.Entry<String, List<List<String>>>> equationsList
    Grammar grammar
    public RegexpSystem(Grammar grammar) {
        this.grammar = grammar
        this.equationsList = grammar.rules.entrySet().toList()
    }

    public String resolve() {
        directCourse()
        reverseCourse()
        Map<String, List<List<String>>> rules = equationsList.collectEntries {[(it.key): it.value]}
        List<List<String>> rulesOfSolution = rules[grammar.getStartSymbol()]
        def res = rulesOfSolution.inject([]) {res, it -> res << it.join("") }.join("|")
        println "result of resolve regexp system: $res"
        return res
    }

    def directCourse() {
        println "gauss start: $equationsList"
        for (int i=0; i<equationsList.size(); i++) {
            equationsList[i].value = extractNonTerminal(equationsList[i].key, equationsList[i].value)
            for (int j=i + 1; j < equationsList.size(); j++)
                equationsList[j].value = directCourseForRule(equationsList[i].key, equationsList[i].value, equationsList[j].value)
        }
        println "result of direct course: $equationsList"

    }

    // return new jrules
    static def directCourseForRule(String iterminal, List<List<String>> irules, List<List<String>> jrules) {
        List<List<String>> newJRules = []
        for (int k=0; k<jrules.size(); k++)
            if (jrules[k].last() == iterminal) {
                def newRule = jrules[k]
                newRule = newRule[0..newRule.size() - 2] //without last
                for (def irule: irules)
                    newJRules << newRule + irule
            } else
                newJRules << jrules[k]
        println newJRules
        newJRules
    }

    static def extractNonTerminal(String nonTerminal, List<List<String>> rules) {
        List<List<String>> newRules = []
        List<String> rulesWithNonTerminal = []
        rules.each { rule ->
            if (rule.last() == nonTerminal) {
                rulesWithNonTerminal.addAll(rule[0..rule.size() - 2])
                rulesWithNonTerminal << "|"
            } else
                newRules << rule
        }
        if (rulesWithNonTerminal.size() == 0)
            throw new IllegalArgumentException()
        else if (rulesWithNonTerminal.size() >= 2) {
            rulesWithNonTerminal = rulesWithNonTerminal[0..rulesWithNonTerminal.size() - 2]
            if (rulesWithNonTerminal.size() >= 3) {
                rulesWithNonTerminal.add(0, "(")
                rulesWithNonTerminal << ")"
            }
            rulesWithNonTerminal << '*'
        }
        String prefixForTerminal = rulesWithNonTerminal.join("")
        newRules = newRules.collect { it -> def res = it.collect(); res.add(0, prefixForTerminal); res }
        println "extractNonTerminal $nonTerminal: $newRules"
        return newRules
    }

    def reverseCourse() {
        for (int i = equationsList.size() - 1; i>0; i --)
            for (int j = 0; j < i; j++)
                equationsList[j].value = directCourseForRule(equationsList[i].key, equationsList[i].value, equationsList[j].value)
    }


}
