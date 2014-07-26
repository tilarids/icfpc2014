package lispparser;

import app.Cons;

/**
 * Created by lenovo on 25.07.2014.
 */
public class GCCAsmPrinter implements ConsPrintVisitor {
    @Override
    public void printFirst(StringBuilder buffer, ConsPrinter consPrinter, Object data, Cons cons) {
        consPrinter.print(data, this);
    }


    @Override
    public void printSecond(StringBuilder buffer, ConsPrinter consPrinter, Object addr, Cons cons) {
        consPrinter.print(addr, this);
        buffer.append("CONS").append("\r\n");
    }

    @Override
    public void printInt(StringBuilder buffer, ConsPrinter consPrinter, Integer val) {
        buffer.append("LDC ").append((int) val).append("\r\n");
    }
}
