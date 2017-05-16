package compilers.rk

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by ali on 01.03.17.
 */
class RK1 {
    public static void main(String[] args) {
        def grammars =  SuffixGrammar.buildsFromFile("rk1/input")
        def output = new File("rk1/output")
        // truncate // быдлокод
        PrintWriter writer = new PrintWriter(output);
        writer.print("");
        writer.close();
        output
        for (int i=0; i<grammars.size(); i++) {
            def res =grammars[i].solve()
            output << "Case ${i+1}: " << (res != 0 ? "$res" : "No solution") << "\n"
        }
    }
}

class SuffixGrammar {
    Map<String, String> rules = [:]
    String input, output

    private SuffixGrammar() {}
    static List<SuffixGrammar> buildsFromFile(String fileName) {
        List<String> fileContent = Files.readAllLines(Paths.get(fileName))
        def grammars = [], i = 0
        while (i != fileContent.size()) {
            def grammar = new SuffixGrammar()
            String[] firstLine = fileContent[i].split(" ")
            grammar.input = firstLine[0]
            grammar.output = firstLine[1]
            int ruleCount = Integer.valueOf(firstLine[2])
            i++
            for (int k=0; k<ruleCount; k++) {
                String[] rule = fileContent[i].split(" ")
                grammar.rules[rule[0]] = rule[1]
                i++
            }
            grammars << grammar
        }
        return grammars
    }

    public int solve() {
        return solve(input, input, null)
    }

    int solve(String ruleSeq, String input, Map.Entry<String, String> prevRule) {
        int res = 0
        def localRules = rules.entrySet().collect()
        localRules.remove(prevRule)
        for (Map.Entry<String, String> rule: localRules) {
            String localInput = input.reverse()
            String localRuleSeq = ruleSeq + ""
            if (localInput.startsWith(rule.key.reverse())) { // reverse не просто так
                localInput = localInput.replaceFirst(rule.key.reverse(), rule.value.reverse())
                localRuleSeq += "->" + localInput.reverse()
                if (localInput.reverse() == output) {
                    res++
                    println localRuleSeq
                } else {
                    localInput = localInput.reverse()
                    res += solve(localRuleSeq, localInput, rule)
                }
            }
        }
        return res
    }



    @Override
    public String toString() {
        return "SuffixGrammar{" +
                "rules=" + rules +
                ", input='" + input + '\'' +
                ", output='" + output + '\'' +
                "} ";
    }
}
