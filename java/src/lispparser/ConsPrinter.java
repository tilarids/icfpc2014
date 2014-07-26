package lispparser;

import app.Cons;

/**
 * Created by lenovo on 25.07.2014.
 */
public class ConsPrinter {
    private final StringBuilder _buffer = new StringBuilder();

    public static String staticPrint(Object val, ConsPrintVisitor visitor) {
        ConsPrinter printer = new ConsPrinter();
        printer.print(val, visitor);
        return printer._buffer.toString();
    }

    public void print(Object val, ConsPrintVisitor visitor) {
        if (val instanceof Cons)
            printCons((Cons) val, visitor);
        else
            visitor.printInt(_buffer, this, (Integer) val);

    }


    private void printCons(Cons cons, ConsPrintVisitor visitor) {
        visitor.printFirst(_buffer, this, cons.data, cons);
        visitor.printSecond(_buffer, this, cons.addr, cons);
    }

}
