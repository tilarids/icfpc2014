package lispparser;

import app.Cons;

/**
 * Created by lenovo on 25.07.2014.
 */
public class ConsPrettyPrint implements ConsPrintVisitor {
    private final int _tabs;

    public ConsPrettyPrint() {
        this(0);
    }

    public ConsPrettyPrint(int _tabs) {
        this._tabs = _tabs;
    }

    private void addTabs(StringBuilder buffer) {
        for (int i = 0; i < _tabs; i++)
            buffer.append("  ");
//            buffer.append(i + " ");
    }

    @Override
    public void printInt(StringBuilder buffer, ConsPrinter consPrinter, Integer val) {
        buffer.append(val);
    }

    @Override
    public void printFirst(StringBuilder buffer, ConsPrinter consPrinter, Object data, Cons cons) {
        addTabs(buffer);
        buffer.append("(");
        if (data instanceof Cons) {
            buffer.append("\r\n");
            addTabs(buffer);
        }
        consPrinter.print(data, new ConsPrettyPrint(_tabs + 1));
    }

    @Override
    public void printSecond(StringBuilder buffer, ConsPrinter consPrinter, Object addr, Cons cons) {
        if (addr instanceof Cons) {
            buffer.append("\r\n");
            addTabs(buffer);
        }
        consPrinter.print(addr, new ConsPrettyPrint(_tabs + 1));
        if (cons.data instanceof Cons) {
            buffer.append("\r\n");
            addTabs(buffer);
        }
        buffer.append(")");
    }
}
