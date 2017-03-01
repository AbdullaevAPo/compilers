package ru.bmstu.compilers
/**
 * Created by ali on 08.10.15.
 */

class GraphVertex {
    static int vertexIds = 0
    List<GraphArc> incomingNodes
    List<GraphArc> outcomingNodes
    int id;
    int level

    public GraphVertex(List<GraphArc> incomingNodes, List<GraphArc> outcomingNodes) {
        this.incomingNodes = incomingNodes;
        this.outcomingNodes = outcomingNodes;
        outcomingNodes.each {it-> it.setV1(this)}
        incomingNodes.each {it-> it.setV2(this)}
        id = ++vertexIds;
    }

    public GraphArc getFirstIncomingArc() {
        return incomingNodes.size() >= 1 ? incomingNodes[0] : null;
    }

    public GraphArc getFirstOutcomingArc() {
        return outcomingNodes.size() >= 1 ? outcomingNodes[0] : null;
    }

    public static GraphVertex getEmptyVertex() {
        return getOneToOneVertex(GraphArc.getEmptyArc(), GraphArc.getEmptyArc());
    }

    public static GraphVertex getOneToOneVertex(GraphArc arc1, GraphArc arc2) {
        GraphVertex res = new GraphVertex([arc1], [arc2]);
        arc1.v2 = res;
        arc2.v1 = res;
        return res;
    }

    public void setFirstIncomingArc(GraphArc arc) {
        if (incomingNodes.size() < 1) return
        incomingNodes[0] = arc
        arc.setV2(this)
    }

    public void setFirstOutcomingArc(GraphArc arc) {
        if (outcomingNodes.size() < 1) return
        outcomingNodes[0] = arc
        arc.setV1(this)
    }

    public void setIncomingArcs(GraphArc[] arcs) {
        arcs.each { it -> it.setV2(this) }
        this.incomingNodes = incomingNodes
    }

    public void setOutcomingArc(GraphArc[] arcs) {
        arcs.each { it -> it.setV1(this) }
        this.incomingNodes = incomingNodes
    }

    boolean isMatch(String str, int pos){
//        println "Vertex: ${id}"

        for (GraphArc arc: outcomingNodes)
            if (arc.isMatch(str, pos)) return true
        return false
    }

    def simpleOptimize(Set<GraphVertex> visitedVertexes){
        if (outcomingNodes.size() == 1 && outcomingNodes.get(0).isEmptyArc()
                && outcomingNodes.get(0).getV2() != null && outcomingNodes.get(0).getV2().getIncomingNodes().size() == 1) {
            GraphVertex intermediateVertex = outcomingNodes.get(0).getV2();
            outcomingNodes = intermediateVertex.getOutcomingNodes();
            outcomingNodes.forEach {it -> it.setV1(this) };
        } else {
            outcomingNodes.forEach {it -> it.simpleOptimize(visitedVertexes) };
            return;
        }

        visitedVertexes.add(this);
        simpleOptimize(visitedVertexes);
    }

    public void getListOfAllArcs(List<GraphArc> visitedArcs){
        outcomingNodes.forEach {if (it.getV2() != null)  it.getListOfAllArcs(visitedArcs); };
    }

    public void getListOfAllVertexes(List<GraphVertex> visitedVertexes, int level) {
        if (visitedVertexes.contains(this))
            return;
        id = vertexIds++;
        visitedVertexes.add(this);
        this.level = level;
        outcomingNodes.forEach { it.getListOfAllVertexes(visitedVertexes, level) };
    }
}

class GraphArc {
    GraphVertex v1
    GraphVertex v2
    String value

    GraphArc(GraphVertex v1, GraphVertex v2, String value) {
        this.v1 = v1;
        this.v2 = v2;
        this.value = new String(value);
    }

    boolean isMatch(String str, int pos) {
        if (v2 != null) {
            if (Arrays.asList("+", Eps, ")", "(").contains(value))
                return v2.isMatch(str, pos);
            else return pos <= str.length() - 1 && (str.charAt(pos).toString()).equals(value) && v2.isMatch(str, pos + 1);
        }
        return pos == str.length();
    }

    public static String Eps = "Eps";

    public static GraphArc getEmptyArc() { return new GraphArc(null, null, Eps) }

    public boolean isEmptyArc() { return value == Eps }

    def simpleOptimize(Set<GraphVertex> visitedVertexes){
        if (v2 && !visitedVertexes.contains(v2)) v2.simpleOptimize(visitedVertexes)
    }


    void getListOfAllArcs(List<GraphArc> visitedArcs) {
        if (visitedArcs.contains(this))
            return;
        visitedArcs.add(this);
        if (v2 != null)
            v2.getListOfAllArcs(visitedArcs);
    }

    void getListOfAllVertexes(List<GraphVertex> visitedVertexes, int level){
        if (v2 != null)
            v2.getListOfAllVertexes(visitedVertexes, ++level);
    }
}

class PairOfGraphArc {
    GraphArc arc1
    GraphArc arc2

    PairOfGraphArc(GraphArc arc1, GraphArc arc2) {
        this.arc1 = arc1;
        this.arc2 = arc2;
    }

    boolean isMatch(String str) {
        return arc1.isMatch(str, 0);
    }

    void setArc1(GraphArc arc1) {
        this.arc1.getV2().setFirstIncomingArc(arc1)
        this.arc1 = this.arc1.getV2().getFirstIncomingArc()
    }

    void setArc2(GraphArc arc2) {
        this.arc2.getV1().setFirstOutcomingArc(arc2)
        this.arc2 = this.arc2.getV1().getFirstOutcomingArc()
    }

    List<GraphArc> getListOfGraphArcs(){
        List<GraphArc> res = new ArrayList<>();
        arc1.getV2().getListOfAllArcs(res);
        return res;
    }

    List<GraphVertex> getListOfGraphVertexes(){
        List<GraphVertex> res = new ArrayList<>();
        arc1.getV2().getListOfAllVertexes(res, 0);
        return res;
    }
}

class Graph
{
    List<GraphVertex> vertexes;
    List<GraphArc> arcs;
    public Graph(List<GraphVertex> vertexes, List<GraphArc> arcs) {
        this.vertexes = vertexes;
        this.arcs = arcs;
    }

    public Graph(PairOfGraphArc pairOfGraphArc, boolean isOptimize) {
        if (isOptimize)
            pairOfGraphArc.getArc1().simpleOptimize(new HashSet<>());
        this.vertexes = pairOfGraphArc.getListOfGraphVertexes();
        this.arcs = pairOfGraphArc.getListOfGraphArcs();
    }

}
