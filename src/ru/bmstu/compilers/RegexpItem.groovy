package ru.bmstu.compilers

/**
 * Created by ali on 08.10.15.
 */

abstract class RegexpItem {
    public abstract PairOfGraphArc getGraph();
}

class CharRegexpItem extends RegexpItem {
    char value

    CharRegexpItem(char item) {
        value = item;
    }

    @Override
    public PairOfGraphArc getGraph() {
        def arc = new GraphArc(null, null, value.toString());
        GraphVertex vertex1 = GraphVertex.getOneToOneVertex(GraphArc.getEmptyArc(), arc);
        GraphVertex vertex2 = GraphVertex.getOneToOneVertex(arc, GraphArc.getEmptyArc());
        return new PairOfGraphArc(vertex1.getFirstIncomingArc(), vertex2.getFirstOutcomingArc());
    }
}

class BracketsRegexpItem extends RegexpItem {
    public RegexpItem union;
    public boolean isBracket;

    public BracketsRegexpItem(UnionRegexpItem union, boolean isBracket) {
        this.union = union;
        this.isBracket = isBracket;
    }

    public BracketsRegexpItem(CharRegexpItem union) {
        this.union = union;
    }

    @Override
    public PairOfGraphArc getGraph() {
        PairOfGraphArc base = union.getGraph()
        if (!isBracket) return base
        def leftBracket = new CharRegexpItem('(' as char).getGraph(), rightBracket = new CharRegexpItem(')' as char).getGraph()
        leftBracket.getArc2().getV1().setFirstOutcomingArc(base.getArc1())
        rightBracket.getArc1().getV2().setFirstIncomingArc(base.getArc2())
        base.getArc2().getV1().setFirstOutcomingArc(rightBracket.arc1)

        return new PairOfGraphArc(leftBracket.getArc1(), rightBracket.getArc2())
    }
}
// IT IS PLUS, NO STAR
class PlusRegexpItem extends RegexpItem {
    BracketsRegexpItem bracketsItem;
    String isPlus = ''

    public PlusRegexpItem(BracketsRegexpItem bracketsItem, String isPlus) {
        this.bracketsItem = bracketsItem;
        this.isPlus = isPlus;
    }

    @Override
    public PairOfGraphArc getGraph() {
        def base = bracketsItem.getGraph()
        if (isPlus == '') return base
        GraphVertex v1 = GraphVertex.getOneToOneVertex(GraphArc.getEmptyArc(), base.getArc1())
        GraphVertex v2 = GraphVertex.getOneToOneVertex(base.getArc2(), GraphArc.getEmptyArc())

        GraphArc plusArc = new GraphArc(v2, v1, '+' /* + or * */);
        v1.getIncomingNodes().add(plusArc)
        v2.getOutcomingNodes().add(plusArc)

        if (isPlus == '*') {
            GraphVertex v3 = GraphVertex.getOneToOneVertex(GraphArc.getEmptyArc(), v1.firstIncomingArc)
            GraphVertex v4 = GraphVertex.getOneToOneVertex(v2.firstOutcomingArc, GraphArc.getEmptyArc())

            GraphArc starArc = new GraphArc(v3, v4, GraphArc.Eps /* + or * */);
            v4.getIncomingNodes().add(starArc)
            v3.getOutcomingNodes().add(starArc)
            return new PairOfGraphArc(v3.firstIncomingArc, v4.firstOutcomingArc);
        }

        return new PairOfGraphArc(v1.firstIncomingArc, v2.firstOutcomingArc);
    }
}

class ConcatRegexpItem extends RegexpItem {
    PlusRegexpItem firstItem;
    ConcatRegexpItem secondItem;

    public ConcatRegexpItem(PlusRegexpItem firstItem, ConcatRegexpItem secondItem) {
        this.firstItem = firstItem;
        this.secondItem = secondItem;
    }

    public ConcatRegexpItem(PlusRegexpItem item) {
        this.firstItem = item;
    }

    @Override
    PairOfGraphArc getGraph() {
        if (secondItem == null) return firstItem.getGraph()
        def leftGraph = firstItem.getGraph(), rightGraph = secondItem.getGraph()
        GraphVertex v = GraphVertex.getOneToOneVertex(leftGraph.getArc2(), rightGraph.getArc1())
        return new PairOfGraphArc(leftGraph.getArc1(), rightGraph.getArc2())
    }
}

class UnionRegexpItem extends RegexpItem {
    ConcatRegexpItem firstItem;
    UnionRegexpItem secondItem;

    public UnionRegexpItem(ConcatRegexpItem firstItem, UnionRegexpItem secondItem) {
        this.firstItem = firstItem;
        this.secondItem = secondItem;
    }

    public UnionRegexpItem(ConcatRegexpItem item) {
        this.firstItem = item;
    }

    @Override
    PairOfGraphArc getGraph() {
        if(secondItem == null) return firstItem.getGraph()
        def firstGraph = firstItem.getGraph(), secondGraph = secondItem.getGraph()
        GraphVertex v1 = new GraphVertex([GraphArc.getEmptyArc()], [firstGraph.getArc1(), secondGraph.getArc1()])
        GraphVertex v2 = new GraphVertex([firstGraph.getArc2(), secondGraph.getArc2()], [GraphArc.getEmptyArc()])
        return new PairOfGraphArc(v1.getFirstIncomingArc(), v2.getFirstOutcomingArc())
    }
}