package compilers.lab6

import compilers.lab5.PRECEDENCE_OPERATOR
import org.jgrapht.DirectedGraph
import org.jgrapht.alg.CycleDetector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph
/**
 * Created by ali on 14.05.17.
 */
class FGFuncBuilder {

    Map<String, Integer> f = [:], g = [:]
    private final static F = "F", G = "G"

    def buildGraph(LinkedHashMap<String, LinkedHashMap<String, PRECEDENCE_OPERATOR>> matrix) {
        DirectedGraph<List<String>, DefaultEdge> graph =
            new SimpleDirectedGraph<>(DefaultEdge.class);
        Map<String, List<String>> fSet = [:], gSet = [:]
        for (terminal in matrix.keySet()) {
            fSet.put(terminal, [F, terminal])
            gSet.put(terminal, [G, terminal])
            graph.addVertex(fSet.get(terminal))
            graph.addVertex(gSet.get(terminal))
        }
        for (terminal1 in matrix.keySet()) {
            def vertex1 = fSet.get(terminal1)
            for (terminal2 in matrix[terminal1].keySet()) {
                def vertex2 = gSet.get(terminal2)
                switch (matrix[terminal1][terminal2]) {
                    case PRECEDENCE_OPERATOR.EQUAL:
                        def incoming = graph.incomingEdgesOf(vertex1) + graph.incomingEdgesOf(vertex2)
                        def outcoming  = graph.outgoingEdgesOf(vertex1) + graph.outgoingEdgesOf(vertex2)
                        def newVertex = vertex1 + "," + vertex2
                        graph.addVertex(newVertex)
                        graph.removeVertex(vertex1)
                        graph.removeVertex(vertex2)
                        incoming.each {it ->
                            graph.addEdge(graph.getEdgeSource(it), newVertex)
                        }
                        outcoming.each {it ->
                            graph.addEdge(newVertex, graph.getEdgeTarget(it))
                        }
                        fSet[terminal1] = newVertex
                        gSet[terminal2] = newVertex
                        vertex1 = newVertex
                        break
                    case PRECEDENCE_OPERATOR.LARGER:
                        try {
                            graph.addEdge(vertex1, vertex2)
                        } catch (Exception e) {
                            e.printStackTrace()
                        }
                        break
                    case PRECEDENCE_OPERATOR.LESS:
                        graph.addEdge(vertex2, vertex1)
                        break
                }
                if (checkCycle(graph)) {
                    println "Noo"
                    return null
                }
            }
        }

        for (def iterator = graph.edgeSet().iterator(); iterator.hasNext();) {
            def e = iterator.next()
            if (graph.getEdgeSource(e) == graph.getEdgeTarget(e))
                iterator.remove()
        }

        for (DefaultEdge e : graph.edgeSet()) {
            System.out.println(graph.getEdgeSource(e).toString() + " --> " + graph.getEdgeTarget(e).toString());
        }

        def longestPathLengths = getLongestPathLengths(graph)
        fSet.each{ terminal, fVertex -> f[terminal] = longestPathLengths[fVertex] }
        gSet.each{ terminal, gVertex -> g[terminal] = longestPathLengths[gVertex] }

        return this
    }

    private boolean checkCycle(DirectedGraph<List<String>, DefaultEdge> graph) {
        return new CycleDetector(graph).detectCycles()
    }

    private Map<List<String>, Integer> getLongestPathLengths(DirectedGraph<List<String>, DefaultEdge> graph) {
        Map<List<String>, Integer> res = [:]
        graph.vertexSet().each { res[it] = 0 }
        boolean flag = true
        while (flag) {
            def oldRes = res.collectEntries{k, v -> [k, v]}
            for (n in graph.vertexSet()) {
                res[n] = 0
                for (outEdge in graph.outgoingEdgesOf(n)) {
                    def n2 = graph.getEdgeTarget(outEdge)
                    res[n] = Math.max(res[n], 1+ res[n2])
                }
            }
            println res
            println oldRes
            if (oldRes == res)
                flag = false
        }
        return res
    }
}
