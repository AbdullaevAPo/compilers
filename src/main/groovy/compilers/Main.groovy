package compilers

import compilers.lab5.OperatorPrecedenceMatrixBuilder
import compilers.lab6.FGFuncBuilder
import groovy.json.JsonOutput
/**
 * Created by ali on 09.10.15.
 */
class Main {
    static void main(String[] args) throws IOException {
        // lab1
//        def grammar = Grammar.loadFromFile("lab1test")
//        def regexpSystem = new RegexpSystem(grammar)
//        println getGraph(regexpSystem.resolve()).isMatch("000000")
        // lab3
//        def grammar = Grammar.loadFromFile("G3plus").withoutLeftRecursion();
//        grammar.saveIntoFile("outtest")
//        grammar.topDownParsing("begin_Identifier=Digit*Digit+Digit>Digit_end;", true)

//        def grammar = Grammar.loadFromFile("lab3test").withoutLeftRecursion();
//        grammar.saveIntoFile("outtest")
//        grammar.topDownParsing("a+a*a", true)

        // lab4
//        println JsonOutput.prettyPrint(JsonOutput.toJson(new ProgramCompiler("begin_Identifier=Digit*Digit+Digit>Digit_end;").parse()))
//        println JsonOutput.prettyPrint(JsonOutput.toJson(new ProgramCompiler("begin_Identifier=_end;").parse()))

        // lab5
        def grammar = Grammar.loadFromFile("G5")
//        grammar.toSingleNonTerminal().saveIntoFile("lab5_test")
//        JsonOutput.prettyPrint(JsonOutput.toJson(grammar.buildFirstSet("E", [:])))
        def matrix = new OperatorPrecedenceMatrixBuilder(grammar).buildMatrix()
        println JsonOutput.prettyPrint(JsonOutput.toJson(matrix))
        def graph = new FGFuncBuilder().buildGraph(matrix)
        println graph.getF()
        println graph.getG()
        def regexp = "begin_ Identifier = Diit * Digit + Digit > Digit _end"
        println "PosixFGForm: " + grammar.toPosixFormWithFG(regexp.split(' ').toList())
        println "PosixForm: " + grammar.toPosixForm(regexp.split(' ').toList())

    }

    static PairOfGraphArc getGraph(String pattern){
        def regexpParser = new RegexpCompiler(pattern)
        def graph = regexpParser.root.getGraph()
        graph.getArc1().simpleOptimize([] as Set)
        return graph
    }

}
