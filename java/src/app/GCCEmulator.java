package app;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tilarids on 7/27/14.
 */
public class GCCEmulator {


    static class Op {
        String op;
        String source;
        Runnable runnable;
        List<Integer> param = new ArrayList();
        Op(String source, String op) {
            this.op = op;
            this.source = source;
        }

        @Override
        public String toString() {
            return source;
        }
    }
    enum Tag {Int, Dum, Cons, Join, Closure, Frame, Ret, Stop}

    static class D {
        Tag tag;
        Integer int_p;
        Cons<D,D> cons_p;
        Integer control_p;
        Closure closure_p;
        Frame frame_p;

        D(Integer int_p) {
            this.tag = Tag.Int;
            this.int_p = int_p;
        }
        D(Cons cons_p) {
            this.tag = Tag.Cons;
            this.cons_p = cons_p;
        }
        D(Tag t, Integer control_p) {
            this.tag = t;
            this.control_p = control_p;
        }
        D(Closure closure_p) {
            this.tag = Tag.Closure;
            this.closure_p = closure_p;
        }
        D(Frame frame_p) {
            this.tag = Tag.Frame;
            this.frame_p = frame_p;
        }

        public String toString() {
            switch (this.tag) {
                case Int:
                    return int_p.toString();
                case Dum:
                    return "DUM";
                case Closure:
                    return "Closure{" + closure_p.index.toString()+"}";
                case Cons:
                    return "("+cons_p.data.toString()+","+cons_p.addr.toString()+")";
                case Frame:
                    return "Frame{" + this.frame_p.value.size()+"}";
                case Join:
                    return "Join{" + this.control_p.toString() + "}";
                case Ret:
                    return "Ret{" + this.control_p.toString() + "}";
                case Stop:
                    return "STOP";
                default:
                    return "Unsupported";
            }
        }
    }

    static class Frame {
        Frame parent = null;
        Tag tag = Tag.Frame;
        List<D> value;
        Frame(Integer n) {
            this.value = new ArrayList<>();
            for (int i = 0; i < n; ++i) {
                this.value.add(null);
            }
        }
    }

    static class Closure {
        Integer index;
        Frame frame;
        Closure(Integer index, Frame frame) {
            this.index = index;
            this.frame = frame;
        }
    }

    int reg_c = 0; // %c
    int reg_s = 0; // %s
    int reg_d = 0; // %d
    Frame reg_e = null; // %e

    List<D> data_stack = new ArrayList<>();
    List<Object> control_stack = new ArrayList<>();

    void push_ds(D d) {
        data_stack.add(d);
        reg_s++;
    }

    D pop_ds() {
        return data_stack.remove(--reg_s);
    }

    void push_control(D d) {
        control_stack.add(d);
        reg_d++;
    }

    D pop_control() {
        return (D) control_stack.remove(--reg_d);
    }

    boolean trace = false;

    void run(List<Op> ops) {
        int instructionCount = 0;
        while (reg_c < ops.size()) {
            Op op = ops.get(reg_c);
            if (trace) { System.out.print("IP: " + reg_c + "  OP: " + op.toString()+" dss:"+data_stack.size()); System.out.flush(); }
            if (instructionCount % 1_000_000 == 0) System.out.println("Instructions: "+instructionCount/1_000_000+"M");
            instructionCount++;
            if (trace) System.out.println();
            op.runnable.run();
        }
    }

    public Runnable makeExecutable(Op op) {
        switch(op.op) {
            case "LDC": {
                return () -> {
                    push_ds(new D(op.param.get(0)));
                    reg_c++;
                };
            }
            case "LD": {
                return () -> {
                    Frame f = reg_e;
                    int n = op.param.get(0);
                    while (n > 0) {
                        f = f.parent;
                        n--;
                    }
                    if (f.tag == Tag.Dum)
                        throw new RuntimeException("FrameMismatch");
                    D d = f.value.get(op.param.get(1));
                    push_ds(d);
                    reg_c++;
                };
            }
            case "ADD": {
                return () -> {
                    D y = pop_ds();
                    D x = pop_ds();
                    if (x.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");
                    if (y.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");

                    D d = new D(x.int_p + y.int_p);
                    push_ds(d);
                    reg_c++;
                };
            }
            case "SUB": {
                return () -> {
                    D y = pop_ds();
                    D x = pop_ds();
                    if (x.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");
                    if (y.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");

                    D d = new D(x.int_p - y.int_p);
                    push_ds(d);
                    reg_c++;
                };
            }
            case "MUL": {
                return () -> {
                    D y = pop_ds();
                    D x = pop_ds();
                    if (x.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");
                    if (y.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");

                    D d = new D(x.int_p * y.int_p);
                    push_ds(d);
                    reg_c++;
                };
            }
            case "DIV": {
                return () -> {
                    D y = pop_ds();
                    D x = pop_ds();
                    if (x.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");
                    if (y.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");

                    D d = new D(x.int_p / y.int_p);
                    push_ds(d);
                    reg_c++;
                };
            }
            case "CEQ": {
                return () -> {
                    D y = pop_ds();
                    D x = pop_ds();
                    D z = new D(0);
                    if (x.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");
                    if (y.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");
                    if (x.int_p.equals(y.int_p)) {
                        z.int_p = 1;
                    } else {
                        z.int_p = 0;
                    }
                    push_ds(z);
                    reg_c++;
                };
            }
            case "CGT": {
                return () -> {
                    D y = pop_ds();
                    D x = pop_ds();
                    D z = new D(0);
                    if (x.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");
                    if (y.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");
                    if (x.int_p > y.int_p) {
                        z.int_p = 1;
                    } else {
                        z.int_p = 0;
                    }
                    push_ds(z);
                    reg_c++;
                };
            }
            case "CGTE": {
                return () -> {
                    D y = pop_ds();
                    D x = pop_ds();
                    D z = new D(0);
                    if (x.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");
                    if (y.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");
                    if (x.int_p >= y.int_p) {
                        z.int_p = 1;
                    } else {
                        z.int_p = 0;
                    }
                    push_ds(z);
                    reg_c++;
                };
            }
            case "ATOM": {
                return () -> {
                    D x = pop_ds();
                    D y = new D(0);
                    if (x.tag == Tag.Int) {
                        y.int_p = 1;
                    } else {
                        y.int_p = 0;
                    }
                    push_ds(y);
                    reg_c++;
                };
            }
            case "CONS": {
                return () -> {
                    D y = pop_ds();
                    D x = pop_ds();
                    D z = new D(new Cons(x, y));
                    push_ds(z);
                    reg_c++;
                };
            }
            case "CAR": {
                return () -> {
                    D x = pop_ds();
                    if (x.tag != Tag.Cons)
                        throw new RuntimeException("TagMismatch");
                    D y = x.cons_p.data;
                    push_ds(y);
                    reg_c++;
                };
            }
            case "CDR": {
                return () -> {
                    D x = pop_ds();
                    if (x.tag != Tag.Cons)
                        throw new RuntimeException("TagMismatch");
                    D y = x.cons_p.addr;
                    push_ds(y);
                    reg_c++;
                };
            }
            case "SEL": {
                return () -> {
                    D x = pop_ds();
                    if (x.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");
                    push_control(new D(Tag.Join, reg_c + 1));
                    if (x.int_p != 0) {
                        reg_c = op.param.get(0);
                    } else {
                        reg_c = op.param.get(1);
                    }
                };
            }
            case "JOIN": {
                return () -> {
                    D x = pop_control();
                    if (x.tag != Tag.Join)
                        throw new RuntimeException("ControlMismatch");
                    reg_c = x.control_p;
                };
            }
            case "LDF": {
                return () -> {
                    Closure c = new Closure(op.param.get(0), reg_e);
                    D x = new D(c);
                    push_ds(x);
                    reg_c++;
                };
            }
            case "AP": {
                return () -> {
                    D x = pop_ds();
                    if (x.tag != Tag.Closure)
                        throw new RuntimeException("TagMismatch");
                    Integer f = x.closure_p.index;
                    Frame e = x.closure_p.frame;
                    Frame fp = new Frame(op.param.get(0));
                    fp.parent = e;
                    int i = op.param.get(0) - 1;
                    while (i != -1) {
                        D y = pop_ds();
                        fp.value.set(i, y);
                        i--;
                    }
                    push_control(new D(reg_e));
                    push_control(new D(Tag.Ret, reg_c + 1));
                    reg_e = fp;
                    reg_c = f;
                };
            }
            case "RTN": {
                return () -> {
                    D x = pop_control();
                    if (x.tag == Tag.Stop) {
                        throw new RuntimeException("MachineStop");
                    }
                    if (x.tag != Tag.Ret)
                        throw new RuntimeException("ControlMismatch");
                    D y = pop_control();
                    assert y.tag == Tag.Frame;
                    reg_e = y.frame_p;
                    reg_c = x.control_p;
                };
            }
            case "DUM": {
                return () -> {
                    Frame fp = new Frame(op.param.get(0));
                    fp.parent = reg_e;
                    fp.tag = Tag.Dum;
                    reg_e = fp;
                    reg_c++;
                };
            }
            case "RAP": {
                return () -> {
                    D x = pop_ds();
                    if (x.tag != Tag.Closure) {
                        throw new RuntimeException("TagMismatch");
                    }
                    Integer f = x.closure_p.index;
                    Frame fp = x.closure_p.frame;
                    if (reg_e.tag != Tag.Dum)
                        throw new RuntimeException("FrameMismatch");
                    if (reg_e.value.size() != op.param.get(0))
                        throw new RuntimeException("FrameMismatch");
                    if (reg_e != fp)
                        throw new RuntimeException("FrameMismatch");
                    Integer i = op.param.get(0) - 1;
                    while (i != -1) {
                        D y = pop_ds();
                        fp.value.set(i, y);
                        i--;
                    }
                    Frame ep = reg_e.parent;
                    push_control(new D(ep));
                    push_control(new D(Tag.Ret, reg_c + 1));
                    fp.tag = Tag.Frame;
                    reg_e = fp;
                    reg_c = f;
                };
            }
            case "STOP": {
                return () -> {
                    throw new RuntimeException("STOP");
                };
            }
            case "TSEL": {
                return () -> {
                    D x = pop_ds();
                    if (x.tag != Tag.Int)
                        throw new RuntimeException("TagMismatch");
                    if (x.int_p != 0) {
                        reg_c = op.param.get(1);
                    } else {
                        reg_c = op.param.get(0);
                    }
                };
            }
            case "TAP": {
                return () -> {
                    D x = pop_ds();
                    if (x.tag != Tag.Closure)
                        throw new RuntimeException("TagMismatch");
                    Integer f = x.closure_p.index;
                    Frame e = x.closure_p.frame;
                    Frame fp = new Frame(op.param.get(0));
                    fp.parent = e;
                    Integer i = op.param.get(0) - 1;
                    while (i != -1) {
                        D y = pop_ds();
                        fp.value.set(i, y);
                        i--;
                    }
                    reg_e = fp;
                    reg_c = f;
                };
            }
            case "TRAP": {
                return () -> {
                    D x = pop_ds();
                    if (x.tag != Tag.Closure)
                        throw new RuntimeException("TagMismatch");
                    Integer f = x.closure_p.index;
                    Frame fp = x.closure_p.frame;
                    if (reg_e.tag != Tag.Dum)
                        throw new RuntimeException("FrameMismatch");
                    if (reg_e.value.size() != op.param.get(0))
                        throw new RuntimeException("FrameMismatch");
                    if (reg_e != fp)
                        throw new RuntimeException("FrameMismatch");

                    Integer i = op.param.get(0) - 1;
                    while (i != -1) {
                        D y = pop_ds();
                        fp.value.set(i, y);
                        i--;
                    }
                    fp.tag = Tag.Frame;
                    reg_e = fp;
                    reg_c = f;
                };
            }
            case "ST": {
                return () -> {
                    Frame fp = reg_e;
                    Integer n = op.param.get(0);
                    Integer i = op.param.get(1);

                    while (n > 0) {
                        fp = fp.parent;
                        n--;
                    }
                    if (fp.tag == Tag.Dum)
                        throw new RuntimeException("FrameMismatch");
                    D v = pop_ds();
                    fp.value.set(i, v);
                    reg_c++;
                };
            }
            case "DBUG": {
                return () -> {
                    D x = pop_ds();
                    System.out.println(x);
                    reg_c++;
                };
            }
            case "BRK": {
                return () -> reg_c++;
            }
            default:
                throw new RuntimeException("Unsupported");
        }

    }

    public static void main(String[] args) throws IOException {
        new GCCEmulator().go();
    }

    private void go() throws IOException {
        List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath("test.txt"));
        List<Op> ops = new ArrayList<>();
        for (String line : lines) {
            String source = line;
            if (line.contains(";")) {
                line = line.substring(0, line.indexOf(";"));
            }
            String[] tokens = line.split("\\s");
            if (tokens[0].length() == 0) continue;
            Op op = new Op(source, tokens[0]);
            for (int i = 1; i < tokens.length; ++i) {
                if (tokens[i].length() > 0) {
                    op.param.add(Integer.parseInt(tokens[i]));
                }
            }
            op.runnable = makeExecutable(op);
            ops.add(op);
        }
        run(ops);
    }
}

