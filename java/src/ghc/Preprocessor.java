package ghc;

import compiler.Compiler;
import sun.plugin.dom.exception.InvalidStateException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: brox
 * Since: 2014-07-26
 */

public class Preprocessor {
    private static final int COMMENT_PADDING = 30;

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

        // relative labels are "&(+2)" or "&(-42)"
        private static final Pattern RELATIVE_LABEL = Pattern.compile("([^&]+)&\\(([+\\-]?[0-9]+)\\)(.*)");

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
            int pos = 0;
            for (RealGHCInstruction instr : this.realInstructions) {
                String realInstructionAsm = processRealInstrLabels(instr, pos);
                if (annotateWithLineNumbers)
                    instr.appendComment(" #" + pos);
                String comment = instr.getComment();
                String str;
                if (comment != null) {
                    str = Compiler.Opcode.rpad(realInstructionAsm, COMMENT_PADDING) + comment;
                } else
                    str = realInstructionAsm;


                sb.append(str);
                sb.append("\r\n");
                pos++;
            }
            return sb.toString();
        }

        public static String process(List<String> src, boolean annotateWithLineNumbers) {
            return new GHCCode(src).generateAsm(annotateWithLineNumbers);
        }

        public String containsLabel(String asm) {
            for (Map.Entry<String, Integer> p : labelPos.entrySet()) {
                if (asm.contains(p.getKey()))
                    return p.getKey();
            }
            return null;
        }

        public String processRealInstrLabels(RealGHCInstruction realInstr, int curInstrAddr) {
            String asm = realInstr.getRealInstructionAsm();
            for (Map.Entry<String, Integer> p : labelPos.entrySet()) {
                if (asm.contains(p.getKey())) {
                    realInstr.appendComment("=>" + p.getKey());
                    return asm.replace(p.getKey(), p.getValue().toString());
                }
            }

            Matcher m = RELATIVE_LABEL.matcher(asm);
            if (m.matches()) {
                int shift = Integer.parseInt(m.group(2));
                realInstr.appendComment("=>"
                        + (shift > 0 ? "+" : "")
                        + shift);
                int jumpAddr = curInstrAddr + shift;
                return m.replaceFirst(m.group(1) + jumpAddr + m.group(3));
            }

            return asm;
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

        public String getComment() {
            return comment;
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

//        public String toAsmString(GHCCode code) {
//            String realInstructionAsm = code.replaceLabelsWithAddr(getRealInstructionAsm(), this);
//            if (comment != null) {
//                return Compiler.Opcode.rpad(realInstructionAsm, COMMENT_PADDING) + comment;
//            } else
//                return realInstructionAsm;
//        }

        public abstract String getRealInstructionAsm();

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
        public String getRealInstructionAsm() {
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
        public String getRealInstructionAsm() {
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
            setUsedLabel(raw.substring(CMD_NAME.length()).trim());
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
