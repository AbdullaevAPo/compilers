package compilers

import compilers.lab5.OperatorPrecedenceMatrixBuilder
import compilers.lab5.PRECEDENCE_OPERATOR
import compilers.lab6.FGFuncBuilder

import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
/**
 * Created by ali on 14.02.17.
 */
class Grammar {
    Set<String> nonTerminals = []
    Set<String> terminals = []

    Map<String, List<List<String>>> rules = [:]

    String startSymbol

    Grammar(Set<String> nonTerminals, Set<String> terminals, Map<String, List<List<String>>> rules, String startSymbol) {
        this.nonTerminals = nonTerminals
        this.terminals = terminals
        this.rules = rules
        this.startSymbol = startSymbol
    }

    public static Grammar loadFromFile(String fileName) throws IOException {
        List<String> fileContent = Files.readAllLines(Paths.get(fileName));
        Set<String> nonTerminals = null, terminals = null;
        Map<String, List<List<String>>> rules = new HashMap<>();
        String startSymbol;
        for (int i=0; i<fileContent.size() - 1; i++) {
            String str = fileContent.get(i);
            switch (i) {
                case 0:
                    nonTerminals = Arrays.stream(str.split(" ")).collect(Collectors.toList());
                    break;
                case 1:
                    terminals = Arrays.stream(str.split(" ")).collect(Collectors.toList());

                    break;
                default:
                    String[] ruleStr = str.split(" -> ");
                    String leftPart = ruleStr[0];
                    if (!rules.containsKey(leftPart))
                        rules.put(leftPart, new ArrayList<>());
                    rules.get(leftPart).add(ruleStr[1].split(" ").toList());
                    break;
            }
        }
        startSymbol = fileContent.get(fileContent.size() - 1);
        return new Grammar(nonTerminals, terminals, rules, startSymbol);
    }

    public void saveIntoFile(String fileName) throws IOException {
        new File(fileName).newWriter().withWriter { writer ->
            writer << nonTerminals.join(' ') + "\n"
            writer << terminals.join(' ') + "\n"
            rules.each { k, entry ->
                for (def el : entry)
                    writer << k + " -> " + el.join(" ") + "\n";
            }
            writer << (startSymbol + "\n");
        }
    }

    // lab2
    // http://window.edu.ru/resource/038/78038/files/%D0%9C%D0%A3%20%D0%BA%D0%BE%D0%BD%D1%82%D1%80%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D0%B0%20-%20%D0%9E%D0%A2%20(32%20%D1%81.).pdf
    Grammar withoutLeftRecursion() {
        Set<String> nonTerminals = this.nonTerminals.collect().toSet();
        Map<String, List<List<String>>> rules = new HashMap<>();
        for (int i=0; i<this.nonTerminals.size(); i++) {
            String nonterminalI = this.nonTerminals[i];
            List<List<String>> nonterminalRule = this.rules[nonterminalI];
            if (!nonterminalRule || !nonterminalRule.size()) continue;
            List<List<String>> betas = [], alphas = [];
            for (List<String> concatenation: nonterminalRule)
                if (concatenation[0] == nonterminalI)
                    alphas << concatenation[1 .. -1] // копию
                else
                    betas << concatenation
            if (alphas.size() == 0) // не левая рекурсия
                rules.put(nonterminalI, nonterminalRule);
            else { // генерируем штришок
                String nonterminalHatch = "$nonterminalI\'"
                List<List<String>> newNonTerminalRule = betas.collect(),
                    terminalHatchRule = alphas.collect()
                betas = betas.collect { el -> el.collect() << nonterminalHatch }
                alphas = alphas.collect { el -> el.collect() << nonterminalHatch }
                newNonTerminalRule += betas;
                terminalHatchRule += alphas;
                rules.put(nonterminalI, newNonTerminalRule);
                rules.put(nonterminalHatch, terminalHatchRule);
                nonTerminals.add(nonterminalHatch);
            }

            List<List<String>> newNonTerminalIRule = new ArrayList<>();
            for (List<String> concatenation: rules.get(nonterminalI)) {
                if (concatenation.size() > 0 && nonTerminals.contains(concatenation.get(0)) && rules.get(concatenation.get(0)) != null) {
                    List<String> alpha = new ArrayList<>(concatenation.subList(1, concatenation.size()));
                    List<List<String>> terminalIRulePart = new ArrayList<>(rules.get(concatenation.get(0)));
                    for (int l = 0; l < terminalIRulePart.size(); l++)
                        terminalIRulePart.set(l, new ArrayList<>(terminalIRulePart.get(l)));
                    terminalIRulePart.each {el -> el.addAll(alpha) };
                    newNonTerminalIRule.addAll(terminalIRulePart);

                } else
                    newNonTerminalIRule.add(concatenation);
            }
            rules.put(nonterminalI, newNonTerminalIRule);
        }
        return new Grammar(nonTerminals, terminals, rules, startSymbol);
    }

//    Grammar withoutLeftFactorization() {
//        for ()
//        return new Grammar(nonTerminals, terminals, rules, startSymbol);
//
//    }

    // lab2
    public Grammar removeUnreachableSymbols() {
        Set<String> nonterminals = [startSymbol];
        Set<String> terminals = [];
        boolean changed = true;
        while (changed) {
            changed = false;
            Set<String> newNonTerminals = nonterminals.toSet();
            for (String nonterminal : nonterminals)
                if (rules.containsKey(nonterminal))
                    for (List<String> concatenation : rules[nonterminal])
                        for (String symbol : concatenation)
                            if (this.nonTerminals.contains(symbol))
                                changed = changed || newNonTerminals.add(symbol);
                            else
                                terminals.add(symbol);
            nonterminals = newNonTerminals;
        };
        Map<String, List<List<String>>> newRules = new HashMap<>();
        for (Map.Entry<String, List<List<String>>> rule: rules.entrySet()) {
            if (nonterminals.contains(rule.getKey()))
                newRules.put(rule.getKey(), rule.getValue());
        }
        return new Grammar(nonterminals.toSet(), terminals.toSet(), newRules, startSymbol);
    }

    // lab3
    public List<String> topDownParsing(String seq, boolean debug) {
        Stack<Pair<String, Integer>> L1 = []
        Stack<String> L2 = []
        int pos = 0
        char state = 'q'
        def printState = { if (debug) println "($state, $pos, " +
                "${L1.collect {it.second != -1 ? it.first + it.second : it.first}.join("")}, " +
                "${L2.reverse().join(" ")})"}
        L2 << startSymbol
        printState()
        while (!L2.empty()) {
            switch (state) {
                case 'q':
                    String stackPop  = L2.peek()
                    if (stackPop in nonTerminals) {
                        L1.push(new Pair(stackPop, 0))
                        L2.pop()
                        rules[stackPop][0].reverse().each { L2.push(it)}
                    } else { // terminal
                        if (pos + stackPop.size() - 1 < seq.size() && seq[pos..pos+stackPop.size() - 1] == stackPop) {
                            L1.push(new Pair(stackPop, -1))
                            pos += stackPop.size()
                            L2.pop()
                        } else {
                            state = 'b'
                        }
                    }
                    break;
                case 'b':
                    Pair<String, Integer> l1pop = L1.pop();
                    if (l1pop.second == -1) {
                        pos -= l1pop.first.length()
                        L2.push(l1pop.first)
                    } else {
                        for (int i = 0; i < rules[l1pop.first][l1pop.second].size(); i++)
                            L2.pop();
                        if (l1pop.second < rules[l1pop.first].size() - 1) {
                            l1pop.second++
                            L1.push(l1pop)
                            rules[l1pop.first][l1pop.second].reverse().each { L2.push(it) }
                            state = 'q'
                        } else {
                            L2.push(l1pop.first)
                        }
                    }
                    break;
            }
            printState()
        }
        if (!L1.empty()) {
            state = 't'
            printState()
        }
        return L1.collect {it.second != -1 ? it.first + it.second : it.first}
    }

    def isLL1() {
        Map<String, List<Set<Character>>> firstSet = [:]
        nonTerminals.each { firstSet[it] = [] }
        nonTerminals.each {buildFirstSet(it, firstSet)}
        println firstSet
        firstSet.each { terminal, firstForTerminal ->
            Set<Character> firstSetResForTerminal = []
            firstForTerminal.each { firstForRule -> firstSetResForTerminal.addAll(firstForRule) }
            firstForTerminal.each { firstForRule ->
                if (firstSetResForTerminal.intersect(firstForRule).size() == firstForRule.size())
                    firstSetResForTerminal.removeAll(firstForRule);
                else
                    throw new Exception("rule $firstForRule.toString()")
            }
        }
    }

    def buildFirstSet(String symbol, Map<String, List<Set<Character>>> firstSet) {
        rules[symbol].each { rule ->
            Set<Character> firstSymbolsForRule = []
            if (rule[0] in terminals)
                firstSymbolsForRule.add(rule[0].toCharArray()[0])
            else {
                buildFirstSet(rule[0], firstSet)
                firstSet[rule[0]].each { firstSymbolsForRule.addAll(it) }
            }
            firstSet[symbol].add(firstSymbolsForRule)
        }

    }

    def toSingleNonTerminal() {
        for (nonTerminal in nonTerminals) {
            if (nonTerminal == startSymbol)
                continue
            def currentRules = rules[nonTerminal]
            rules.remove(nonTerminal)
            List<List<String>> newRules = []
            for (e in rules) {
                for (rule in e.value)
                    if (nonTerminal in rule) {
                        for (currentRule in currentRules) {
                            def ruleCopy = rule.collect()
                            def indexOfNonTerminal = ruleCopy.indexOf(nonTerminal)
                            ruleCopy.remove(indexOfNonTerminal)
                            ruleCopy.addAll(indexOfNonTerminal, currentRule)
                            newRules << ruleCopy
                        }
                    } else
                        newRules << rule
            }
            rules[nonTerminal] = newRules
        }
        return this
    }

    public static final String marker = '$'
    public List<String> toPosixForm(List<String> input) {
        def matrix = new OperatorPrecedenceMatrixBuilder(this).buildMatrix()
        input << marker
        def stack = new Stack<String>()
        stack.push(marker)
        int i = 0
        def res = []
        while (!(stack.peek() == marker && input[i] == marker)) {
            switch (matrix[stack.peek()][input[i]]) {
                case PRECEDENCE_OPERATOR.LESS:
                case PRECEDENCE_OPERATOR.EQUAL:
                    stack.push(input[i])
                    i++
                    break
                case PRECEDENCE_OPERATOR.LARGER:
                    while(true) {
                        def stackPop = stack.pop()
                        res << stackPop
                        if (matrix[stack.peek()][stackPop] == PRECEDENCE_OPERATOR.LESS)
                            break
                    }
                    break
                default:
                    throw new Exception("Error")
                    break
            }
        }
        return res
    }

    public List<String> toPosixFormWithFG(List<String> input) {
        def matrix = new OperatorPrecedenceMatrixBuilder(this).buildMatrix()
        def funcBuilder = new FGFuncBuilder().buildGraph(matrix)
        def f = funcBuilder.getF()
        def g = funcBuilder.getG()
        input << marker
        def stack = new Stack<String>()
        stack.push(marker)
        int i = 0
        def res = []
        while (!(stack.peek() == marker && input[i] == marker)) {
            if (f[stack.peek()] <= g[input[i]]) {
                stack.push(input[i])
                i++
            } else {
                while (true) {
                    def stackPop = stack.pop()
                    res << stackPop
                    if (f[stack.peek()] < g[stackPop])
                        break
                }
            }
        }
        return res
    }
}
