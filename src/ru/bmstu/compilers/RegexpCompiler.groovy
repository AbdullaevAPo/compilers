package ru.bmstu.compilers

/**
 * Created by ali on 08.10.15.
 */
class RegexpCompiler {

    String regularExpression;
    int pos;
    RegexpItem root;

    public RegexpCompiler(String regularExpression)
    {
        this.regularExpression = regularExpression;
        pos = 0;
        if ((root = union()) == null)
        {
            pos = 0;
            if ((root = concat()) == null)
            {
                pos = 0;
                if ((root = plus()) == null)
                {
                    pos = 0;
                    if ((root = brackets()) == null)
                        pos = 0;
                }
            }
        }


    }
    private char peek()
    {
        if (more())
            return regularExpression[pos];
        return '\0';
    }

    private char next()
    {
        if (more())
            return regularExpression[pos++];
        return '\0';
    }
    private boolean more()
    {
        return pos <= regularExpression.size() - 1;
    }

    private static Set metaChars = ['*', '+', '(', ')', '|'] as Set;

    private CharRegexpItem charParse()
    {
        int startPos = pos;
        char charOut;
        if (peek().isLetterOrDigit() && !metaChars.contains(charOut) && peek() != '\0')
            return new CharRegexpItem(next());
        pos = startPos;
        return null;
    }

    private BracketsRegexpItem brackets()
    {
        int startPos = pos;
        RegexpItem res;
        if ((res = charParse()) != null)
            return new BracketsRegexpItem((CharRegexpItem)res);
        else if (next() == '(' && (res = union()) != null && next() == ')')
            return new BracketsRegexpItem((UnionRegexpItem)res, true);
        pos = startPos;
        return null;
    }

    private PlusRegexpItem plus()
    {
        int startPos = pos;
        BracketsRegexpItem res;
        if ((res = brackets()) != null)
            if (peek() == '+') {
                next();
                return new PlusRegexpItem(res, '+');
            } else if (peek() == '*') {
                next()
                return new PlusRegexpItem(res, '*')
            } else
                return new PlusRegexpItem(res, '');
        pos = startPos;
        return null;
    }

    private ConcatRegexpItem concat()
    {
        int startPos = pos;
        PlusRegexpItem first;
        ConcatRegexpItem second;

        if ((first = plus()) != null)
            if ((second = concat()) != null)
                return new ConcatRegexpItem(first, second);
            else
                return new ConcatRegexpItem(first);
        pos = startPos;
        return null;
    }

    private UnionRegexpItem union()
    {
        int startPos = pos;
        ConcatRegexpItem first;
        UnionRegexpItem second;

        if ((first = concat()) != null)
            if (next() == '|' && (second = union()))
                return new UnionRegexpItem(first, second);
            else
            {
                pos--;
                return new UnionRegexpItem(first);
            }
        pos = startPos;
        return null;
    }

    public boolean isMatch(String str) {
        return root.getGraph().isMatch(str);
    }
}

