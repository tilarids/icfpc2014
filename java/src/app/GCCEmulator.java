package app;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;

/**
 * Created by tilarids on 7/27/14.
 */
public class GCCEmulator {


    static class Op {
        String op;
        String source;
        String comment;
        Callable<D> callable;
        List<Integer> param = new ArrayList();
        Op(String source, String comment, String op) {
            this.source = source;
            this.comment = comment;
            this.op = op;
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
        String comment;
        Integer index;
        Frame frame;
        Closure(String comment, Integer index, Frame frame) {
            this.comment = comment;
            this.index = index;
            this.frame = frame;
        }
    }

    static class FunctionHit {
        String comment;  // instead of name
        int hits = 0;
        FunctionHit(String comment) {
            this.comment = comment;
        }
    }

    List<Op> ops;

    int reg_c = 0; // %c
    int reg_s = 0; // %s
    int reg_d = 0; // %d
    Frame reg_e = null; // %e

    List<D> data_stack = new ArrayList<>();
    List<Object> control_stack = new ArrayList<>();

    Stack<FunctionHit> functionHits = new Stack<>();
    // Map<String, Integer> heatMap = new HashMap<>();

    void processHit(FunctionHit hit) {
        if (hit.comment.length() == 0) {
            return;
        }
        if (hit.comment.equals("@entryFactual")) {
            System.out.println("@entryFactual took " + hit.hits + " instructions");
        }
        if (hit.comment.equals("@lambda_1000")) {
            System.out.println("@lambda_1000 took " + hit.hits + " instructions");
        }

        // create absolute time spent map
    }


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

    int instructionCount = 0;

    D run(int start_c) {
        reset(start_c);
        try {
            while (reg_c < ops.size()) {
                Op op = ops.get(reg_c);
                if (trace) { System.out.print("IP: " + reg_c + "  OP: " + op.toString()); System.out.flush(); }
                // if (instructionCount % 1_000 == 0) System.out.println("Instructions: "+instructionCount/1_000+"K");
                instructionCount++;
                // if (instructionCount == 4923097-300) trace = true;
                D ret = op.callable.call();
                if (trace) System.out.println();

                if (functionHits.size() > 0) {
                    functionHits.peek().hits++;
                }

                if (ret != null) {
                    return ret;
                }
            }
        } catch (Exception e) {
            System.out.println("AT INSTRUCTION COUNT: "+instructionCount + "; %c = " + reg_c);
            e.printStackTrace();
        }
        return null;
    }

    int countStart = 0;

    public Callable<D> makeExecutable(Op op) {
        switch(op.op) {
            case "LDC": {
                return () -> {
                    push_ds(new D(op.param.get(0)));
                    reg_c++;
                    return null;
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
                    if (trace) {
                        String s = d.toString();
                        System.out.print(" loaded: "+ s.substring(0, Math.min(15, s.length())));
                    }
                    push_ds(d);
                    reg_c++;
                    return null;
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
                    return null;
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
                    return null;
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
                    return null;
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
                    return null;
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
                    return null;
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
                    return null;
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
                    return null;
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
                    return null;
                };
            }
            case "CONS": {
                return () -> {
                    D y = pop_ds();
                    D x = pop_ds();
                    D z = new D(new Cons(x, y));
                    push_ds(z);
                    reg_c++;
                    return null;
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
                    return null;
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
                    return null;
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
                    return null;
                };
            }
            case "JOIN": {
                return () -> {
                    D x = pop_control();
                    if (x.tag != Tag.Join)
                        throw new RuntimeException("ControlMismatch");
                    reg_c = x.control_p;
                    return null;
                };
            }
            case "LDF": {
                return () -> {
                    Closure c = new Closure(op.comment, op.param.get(0), reg_e);
                    D x = new D(c);
                    push_ds(x);
                    reg_c++;
                    return null;
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

                    functionHits.push(new FunctionHit(x.closure_p.comment));
                    return null;
                };
            }
            case "RTN": {
                return () -> {
                    if (control_stack.size() == 0) {  // return from CPU
                        D x = pop_ds();
                        return x;
                    }

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

                    processHit(functionHits.pop());
                    return null;
                };
            }
            case "DUM": {
                return () -> {
                    Frame fp = new Frame(op.param.get(0));
                    fp.parent = reg_e;
                    fp.tag = Tag.Dum;
                    reg_e = fp;
                    reg_c++;
                    return null;
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
                    return null;
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
                        reg_c = op.param.get(0);
                    } else {
                        reg_c = op.param.get(1);
                    }
                    return null;
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
                    return null;
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
                    return null;
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
                    return null;
                };
            }
            case "DBUG": {
                return () -> {
                    D x = pop_ds();
                    if (x.int_p != null && x.int_p == 89001) {
                        countStart = instructionCount;
                    } else if (x.int_p != null && x.int_p == 89002) {
                        countStart = instructionCount - countStart;
                        System.out.println("MEASURED COUNT: "+countStart);
                    } else {
                        System.out.println(x);
                    }
                    reg_c++;
                    return null;
                };
            }
            case "BRK": {
                return () -> {
                    reg_c++;
                    return null;
                };
            }
            default:
                throw new RuntimeException("Unsupported");
        }

    }


    public static void main(String[] args) throws IOException {
        new GCCEmulator("test.txt", 2).run(0);
    }

    public void load(D d) throws Exception {
        push_ds(d);
    }

    public void load(Object o) throws Exception {
        Field[] fields;
        if (o.getClass().getDeclaredFields().length > 0) {
            fields = o.getClass().getDeclaredFields();
        } else {
            fields = o.getClass().getFields();
        }
        for (Field field : fields) {
            Object data = field.get(o);

            if (data instanceof Integer) {
                Op ldc = new Op("", "", "LDC");
                ldc.param.add((Integer) data);
                makeExecutable(ldc).call();
            } else if (data == null) {
                Op ldc = new Op("", "", "LDC");
                ldc.param.add(0);
                makeExecutable(ldc).call();
            } else {
                load(data);
            }
        }
        for (int i = 1; i < fields.length; ++i) {
            makeExecutable(new Op("", "", "CONS")).call();
        }
    }

    public D cont(D x, Integer n) throws Exception {
        assert x.tag == Tag.Closure;
        Integer f = x.closure_p.index;
        Frame e = x.closure_p.frame;
        Frame fp = new Frame(n);
        fp.parent = e;
        for (int i = n - 1; i != -1; --i) {
            D y = pop_ds();
            fp.value.set(i, y);
        }
        // do not save anything so that return will actually return
//        push_control(new D(reg_e));
//        %d := PUSH(%e,%d)                     ; save frame pointer
//        %d := PUSH(SET_TAG(TAG_RET,%c+1),%d)  ; save return address
        reg_e = fp;

//        reg_c = f;
        return run(f);
    }

    public void storeInFrame(Integer i, D d) throws Exception {
        load(d);
        Op st = new Op("", "", "ST");
        st.param.add(0);
        st.param.add(i);
        makeExecutable(st).call();
    }

    public void storeInFrame(Integer i, Object o) throws Exception {
        load(o);
        Op st = new Op("", "", "ST");
        st.param.add(0);
        st.param.add(i);
        makeExecutable(st).call();
    }

    public void reset(int start_c) {
        reg_c = start_c;
        instructionCount = 0;
    }

    GCCEmulator(String path, int topFrameSize) throws IOException {
        reg_e = new Frame(topFrameSize);


        List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(path));
        List<Op> ops = new ArrayList<>();
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);
            String source = line;
            String comment = "";
            if (line.contains(";")) {
                int index = line.indexOf(";");
                line = line.substring(0, index);
                comment = line.substring(index);
            }
            String[] tokens = line.split("\\s");
            if (tokens[0].length() == 0) continue;
            Op op = new Op(source, comment, tokens[0]);
            for (int i = 1; i < tokens.length; ++i) {
                if (tokens[i].length() > 0) {
                    try {
                        op.param.add(Integer.parseInt(tokens[i]));
                    } catch (NumberFormatException e) {
                        System.out.println("At line: "+lineIndex);
                        throw e;
                    }
                }
            }
            op.callable = makeExecutable(op);
            ops.add(op);
        }
        this.ops = ops;
    }

}

