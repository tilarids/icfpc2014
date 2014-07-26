package ghc;

import compiler.Compiler;
import sun.plugin.dom.exception.InvalidStateException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Supplier;

/**
 * Author: brox
 * Since: 2014-07-26
 */

public class Preprocessor {

    public static void main(String[] args) {

        List<String> prog = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream("src/ghc/example.ghc");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                prog.add(strLine);
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(GHCCode.process(prog, false));

    }


    static class GHCCode {
        private final Map<String, Integer> labelPos = new HashMap<>();
        private List<GHCInstruction> virtualInstructions = new ArrayList<>();
        private List<RealGHCInstruction> realInstructions = new ArrayList<>();

        public GHCCode(List<String> src) {
            virtualInstructions.add(new SimpleGHCInstruction("mov h, 255 ; initialize stack"));
            for (String line : src) {
                // 1st pass - check if line is code line, assign addr
                String trimmed = line.trim();
                // skip empty lines
                if (trimmed.length() == 0)
                    continue;
                virtualInstructions.add(parseInstruction(trimmed));
            }


            calcLabelPositions();
        }

        private void calcLabelPositions() {
            String nextComment = null;
            for (GHCInstruction instr : this.virtualInstructions) {
                if (instr instanceof LabelInstruction) {
                    LabelInstruction label = (LabelInstruction) instr;
                    String labelName = label.getLabel();
                    if (labelPos.containsKey(labelName))
                        throw new InvalidStateException("Duplicated label '" + label + "'");
                    labelPos.put(labelName, realInstructions.size());
                }

                List<RealGHCInstruction> real = instr.getRealInstructions();
                if ((nextComment != null) && (real.size() > 0))
                    real.get(0).appendComment(nextComment);
                nextComment = instr.getCommentForNextInstr();
                realInstructions.addAll(real);
            }

            for (GHCInstruction instruction : virtualInstructions) {
                String label = containsLabel(instruction.getSrcInstruction());
                if (label != null)
                    instruction.setUsedLabel(label);
            }

        }

        public String generateAsm(boolean annotateWithLineNumbers) {
            StringBuilder sb = new StringBuilder();
            for (RealGHCInstruction instr : this.realInstructions) {
                String str = instr.toAsmString(this);
                sb.append(str);
                sb.append("\r\n");
            }
            return sb.toString();
        }

        public static String process(List<String> src, boolean annotateWithLineNumbers) {
            return new GHCCode(src).generateAsm(annotateWithLineNumbers);
        }

//        public int getLabelPosition(String label) {
//            Integer pos = labelPos.get(label);
//            if (pos == null)
//                throw new InvalidStateException("Unknown label '" + label + "'");
//            return pos;
//        }

        public String containsLabel(String asm) {
            for (Map.Entry<String, Integer> p : labelPos.entrySet()) {
                if (asm.contains(p.getKey()))
                    return p.getKey();
            }
            return null;
        }

        public String replaceLabelsWithAddr(String asm) {
            String prev;
            String next = asm;
            boolean hasComment = (asm.indexOf(';') != -1);
            for (Map.Entry<String, Integer> p : labelPos.entrySet()) {
                prev = next;
                next = prev.replace(p.getKey(), p.getValue().toString());
                if (!prev.equals(next)) {
                    if (!hasComment)
                        next += " ; ";
                    next += "=>" + p.getKey();
                }
            }
            return next;
        }


        private GHCInstruction parseInstruction(String srcLine0) {
            String srcLine = srcLine0.toLowerCase();
            if (srcLine.contains(":")) {
                return new LabelInstruction(srcLine0);
            } else if (srcLine.startsWith(PushInstruction.CMD_NAME)) {
                return new PushInstruction(srcLine0);
            } else if (srcLine.startsWith(PopInstruction.CMD_NAME)) {
                return new PopInstruction(srcLine0);
            } else if (srcLine.startsWith(CallInstruction.CMD_NAME)) {
                return new CallInstruction(srcLine0);
            } else if (srcLine.startsWith(ReturnInstruction.CMD_NAME)) {
                return new ReturnInstruction(srcLine0);
            } else
                return new SimpleGHCInstruction(srcLine);
        }
    }


    static abstract class GHCInstruction {
        protected final String srcInstruction;
        protected String comment;
        protected String usedLabel;

        protected GHCInstruction(String raw) {
            int commentPos = raw.indexOf(";");
            if (commentPos != -1) {
                this.srcInstruction = raw.substring(0, commentPos).toLowerCase();
                this.comment = raw.substring(commentPos);
            } else {
                this.srcInstruction = raw.trim().toLowerCase();
                this.comment = null;
            }
        }

        protected GHCInstruction(String srcInstruction, String comment) {
            this.srcInstruction = srcInstruction;
            // appendComment also add ";" if required
            //this.comment = comment;
            appendComment(comment);
        }

        public String getSrcInstruction() {
            return srcInstruction;
        }

        public String getUsedLabel() {
            return usedLabel;
        }

        public void setUsedLabel(String usedLabel) {
            this.usedLabel = usedLabel;
        }

        public void appendComment(String comment) {
            if (this.comment == null) {
                if (!comment.contains(";"))
                    comment = ";" + comment;
                this.comment = comment;
            } else {
                this.comment += " " + comment;
            }
        }

        public abstract List<RealGHCInstruction> getRealInstructions();

        public String getCommentForNextInstr() {
            return null;
        }
    }

    static abstract class RealGHCInstruction extends GHCInstruction {
        RealGHCInstruction(String raw) {
            super(raw);
        }

        RealGHCInstruction(String srcInstruction, String comment) {
            super(srcInstruction, comment);
        }

        public String toAsmString(GHCCode code) {
            String realInstructionAsm = code.replaceLabelsWithAddr(getRealInstructionAsm());
            if (comment != null)
                return Compiler.Opcode.rpad(realInstructionAsm, 20) + comment;
            else
                return realInstructionAsm;
        }

        protected abstract String getRealInstructionAsm();

        @Override
        public List<RealGHCInstruction> getRealInstructions() {
            return Arrays.asList(this);
        }
    }

    static class SimpleGHCInstruction extends RealGHCInstruction {
        SimpleGHCInstruction(String raw) {
            super(raw);
        }

        SimpleGHCInstruction(String srcInstruction, String comment) {
            super(srcInstruction, comment);
        }

        @Override
        protected String getRealInstructionAsm() {
            return srcInstruction;
        }
    }


    static class LazyRealGHCInstruction extends RealGHCInstruction {
        private final Supplier<String> lazy;

        LazyRealGHCInstruction(String raw, Supplier<String> lazy) {
            super(raw);
            this.lazy = lazy;
        }

        @Override
        protected String getRealInstructionAsm() {
            return lazy.get();
        }
    }


    static class LabelInstruction extends GHCInstruction {
        private final String label;

        LabelInstruction(String raw) {
            super(raw);
            this.label = raw.substring(0, raw.indexOf(':'));
        }

        public String getLabel() {
            return label;
        }

        @Override
        public List<RealGHCInstruction> getRealInstructions() {
            return Collections.emptyList();
        }

        @Override
        public String getCommentForNextInstr() {
            return "<=" + getLabel();
        }
    }

    static class CallInstruction extends GHCInstruction {
        public static final String CMD_NAME = "call ";

        CallInstruction(String raw) {
            super(raw);
        }

        @Override
        public List<RealGHCInstruction> getRealInstructions() {
            List<RealGHCInstruction> actual = new ArrayList<>();
            //push pc onto stack and change it there for pop!
            actual.add(new SimpleGHCInstruction("mov [h], pc", "; call " + getUsedLabel()));
            actual.add(new SimpleGHCInstruction("add [h], 4"));
            actual.add(new SimpleGHCInstruction("sub h, 1"));
            actual.add(new LazyRealGHCInstruction(this.srcInstruction, () -> "mov pc, " + getUsedLabel()));
            return actual;
        }
    }

    static class ReturnInstruction extends GHCInstruction {
        public static final String CMD_NAME = "ret";

        ReturnInstruction(String raw) {
            super(raw);
        }

        @Override
        public List<RealGHCInstruction> getRealInstructions() {
            List<RealGHCInstruction> actual = new ArrayList<>();
            actual.add(new SimpleGHCInstruction("add h, 1", "return"));
            actual.add(new SimpleGHCInstruction("mov pc, [h]"));
            return actual;
        }
    }

    static class PopInstruction extends GHCInstruction {
        public static final String CMD_NAME = "pop ";

        PopInstruction(String raw) {
            super(raw);
        }

        @Override
        public List<RealGHCInstruction> getRealInstructions() {
            String regName = this.srcInstruction.replace(CMD_NAME, "").trim();
            List<RealGHCInstruction> actual = new ArrayList<>();
            actual.add(new SimpleGHCInstruction("mov " + regName + ", [h]", srcInstruction));
            actual.add(new SimpleGHCInstruction("add h, 1", ""));
            return actual;
        }
    }


    static class PushInstruction extends GHCInstruction {
        public static final String CMD_NAME = "push ";

        PushInstruction(String raw) {
            super(raw);
        }

        @Override
        public List<RealGHCInstruction> getRealInstructions() {
            String regName = this.srcInstruction.replace(CMD_NAME, "").trim();
            List<RealGHCInstruction> actual = new ArrayList<>();
            actual.add(new SimpleGHCInstruction("mov [h], " + regName, srcInstruction));
            actual.add(new SimpleGHCInstruction("sub h, 1", ""));
            return actual;
        }
    }
}
