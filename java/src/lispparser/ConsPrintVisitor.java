package lispparser;

import app.Cons;

/**
 * Created by lenovo on 25.07.2014.
 */
public interface ConsPrintVisitor {

    void printFirst(StringBuilder buffer, ConsPrinter consPrinter, Object data, Cons cons);

    void printSecond(StringBuilder buffer, ConsPrinter consPrinter, Object addr, Cons cons);

    void printInt(StringBuilder buffer, ConsPrinter consPrinter, Integer val);
}
