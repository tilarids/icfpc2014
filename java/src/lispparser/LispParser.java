package lispparser;

import app.Cons;

import java.util.Objects;

/**
 * Created by lenovo on 25.07.2014.
 */
public class LispParser {
    public static void main(String[] args) {
//        String val = "(((0, ((0, 0), ((2, 254), 0))), ((14, 0), 0)), (((14, 0), 0), (((14, 0), 0), (((14, 0), 0), 0))))";
        String val = "(((0, ((0, 0), ((2, 1), 0))), ((13, (0, 0)), ((14, 0), 0))), (((0, ((0, 0), ((2, 1), 0))), ((13, (0, 0)), ((14, 0), 0))), (((0, ((0, 0), ((2, 1), 0))), ((13, (0, 0)), ((14, 0), 0))), (((0, ((0, 0), ((2, 1), 0))), ((13, (0, 0)), ((14, 0), 0))), 0))))";
//        String val = "(0, ((1, 2), (3, 4)))";
        Object result = new LispParser().parse(val);
        System.out.println(val);
//        System.out.println(ConsPrinter.staticPrint(result, new ConsPrettyPrint()));
        System.out.println(ConsPrinter.staticPrint(result, new GCCAsmPrinter()));
    }

    public Object parse(String lispData) {
        return parse(lispData, 0, 0).result;

    }

    private int skipWhitespace(final String lispData, int position) {
        while (Character.isWhitespace(lispData.charAt(position))) {
            position++;
        }
        return position;
    }

    private ParseResult parse(final String lispData, final int startPosition, final int skipClosingBrackets) {
        int position = skipWhitespace(lispData, startPosition);

        if (lispData.charAt(position) == '(') {
            ParseResult first = parse(lispData, position + 1, 0);
            ParseResult second = parse(lispData, first.endPosition + 1, skipClosingBrackets + 1);
            return new ParseResult(new Cons(first.result, second.result), second.endPosition);
        }

        if (Character.isDigit(lispData.charAt(position))) {
            StringBuilder number = new StringBuilder();
            while (Character.isDigit(lispData.charAt(position))) {
                number.append(lispData.charAt(position));
                position++;
            }
            position = skipWhitespace(lispData, position);
            if (lispData.charAt(position) == ',')
                position++;
            int skipClosingBracketsCopy = skipClosingBrackets;
            while (skipClosingBracketsCopy > 0) {
                position = skipWhitespace(lispData, position);
                if (lispData.charAt(position) != ')')
                    throw createISE(lispData, position);
                position++;
                skipClosingBracketsCopy--;
            }
            return new ParseResult(Integer.parseInt(number.toString()), position);
        }

        throw createISE(lispData, position);
    }

    private static IllegalStateException createISE(final String lispData, final int position) {
        String context = lispData.substring(Math.max(0, position - 10), position) + "__"
                + lispData.substring(position, Math.min(lispData.length() - 1, position + 10));
        return new IllegalStateException("Pos = " + position
                + " char = '" + lispData.charAt(position) + "' "
                + " context = '" + context + "'"
                + "\nlispData ='" + lispData + "'");
    }

    static class ParseResult {
        public Object result;
        public int endPosition;

        ParseResult(Object result, int endPosition) {
            this.result = result;
            this.endPosition = endPosition;
        }
    }
}

