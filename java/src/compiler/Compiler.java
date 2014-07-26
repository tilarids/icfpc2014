package compiler;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

/**
 * todo
 * optimize list_item 0, list_item 1 etc. (faster head (tail (tail )) than call/etc)
 */

public class Compiler {

    public static final String VERSION_1_4 = "1.4";
    public static final String VERSION_1_5 = "1.5";
    public static final String VERSION_1_6 = "1.6";

    private static final Set<String> ALLOWED_TARGET_JDKS = new LinkedHashSet<String>();
    static {
        ALLOWED_TARGET_JDKS.add(VERSION_1_4);
        ALLOWED_TARGET_JDKS.add(VERSION_1_5);
        ALLOWED_TARGET_JDKS.add(VERSION_1_6);
    }

    private static final Logger log = Logger.getLogger(Compiler.class);
    public static boolean DEBUG;

    private String targetJdk = VERSION_1_4;
    private String encoding = "UTF-8";

    public void setTargetJdk( String targetJdk ) {
        if(!ALLOWED_TARGET_JDKS.contains(targetJdk))
            throw new IllegalArgumentException("Invalid value for targetJdk: [" + targetJdk + "]. Allowed are "+ALLOWED_TARGET_JDKS);

        this.targetJdk = targetJdk;
    }

    public void setEncoding( String encoding ) {
        if( encoding == null )
            throw new IllegalArgumentException("encoding is null");
        if( encoding.trim().length() == 0 )
            throw new IllegalArgumentException("encoding is empty");
        this.encoding = encoding;
    }

    public TypeDeclaration parseFile(File file) throws IOException {
        if(!file.exists())
            new IllegalArgumentException("File "+file.getAbsolutePath()+" doesn't exist");

        String source = readFileToString( file, encoding );

        return visitString( file, source );
    }

    public static String readFileToString( File file, String encoding ) throws IOException {
        FileInputStream stream = new FileInputStream( file );
        String result = null;
        try {
            result = readInputStreamToString( stream, encoding );
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return result;
    }

    public static String readInputStreamToString( InputStream stream, String encoding ) throws IOException {

        Reader r = new BufferedReader( new InputStreamReader( stream, encoding ), 16384 );
        StringBuilder result = new StringBuilder(16384);
        char[] buffer = new char[16384];

        int len;
        while((len = r.read( buffer, 0, buffer.length )) >= 0) {
            result.append(buffer, 0, len);
        }

        return result.toString();
    }

    public TypeDeclaration visitString(File file, String source) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);

        @SuppressWarnings( "unchecked" )
        Map<String,String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        parser.setCompilerOptions(options);

        parser.setResolveBindings(false);
        parser.setStatementsRecovery(false);
        parser.setBindingsRecovery(false);
        parser.setSource(source.toCharArray());
        parser.setUnitName(file.getPath());
        parser.setIgnoreMethodBodies(false);

        CompilationUnit ast = (CompilationUnit) parser.createAST(null);

        CompilationUnit root = (CompilationUnit)ast.getRoot();
        root.setProperty("sourceFile", file.getPath());
        TypeDeclaration mainClass = (TypeDeclaration)root.types().get(0);
        return mainClass;
    }

    public static void main(String[] args) throws IOException {
        try {
            new Compiler().run();
        } catch (CompilerException e) {
            CompilationUnit root = (CompilationUnit)e.node.getRoot();
            String sourceFile = (String)root.getProperty("sourceFile");
            int line = root.getLineNumber(e.node.getStartPosition());
            int col = root.getColumnNumber(e.node.getStartPosition());
            System.out.println("ERROR!! ");
            System.out.println("ERROR!! ");
            System.out.println("ERROR!! ");
            System.out.println("ERROR: "+sourceFile+"("+line+","+col+"): "+e.getMessage());
            System.out.println("ERROR:     => "+e.node);
            System.out.println("ERROR!! ");
        }

    }

    private void run() throws IOException {
        addTypes(parseFile(new File("src/app/VM.java")));
        addTypes(parseFile(new File("src/app/Sample1.java")));
        ArrayList<Opcode> global = new ArrayList<>();
        MyMethod run = getMethod("entryPoint");
        methods.remove(run);
        methods.add(0, run);
        for (int m = 0; m < methods.size(); m++) {
            MyMethod method = methods.get(m);
            method.offset = global.size();
            generateMethod(method.name, method);
            ArrayList<Opcode> opcodes = method.opcodes;
            for (int oi = 0; oi < opcodes.size(); oi++) {
                Opcode opcode = opcodes.get(oi);
                for (int i = 0; i < opcode.arguments.length; i++) {
                    Object argument = opcode.arguments[i];
                    if (argument instanceof FutureReference) {
                        FutureReference br = (FutureReference) argument;
                        opcode.arguments[i] = br.resolve();
                    }
                }
            }
            global.addAll(method.opcodes);
        }

        for (int m = 0; m < methods.size(); m++) {
            MyMethod method = methods.get(m);
            for (Opcode opcode : method.opcodes) {
                for (int i = 0; i < opcode.arguments.length; i++) {
                    Object argument = opcode.arguments[i];
                    if (argument instanceof FunctionRef) {
                        FunctionRef fr = (FunctionRef)argument;
                        opcode.arguments[i] = fr.resolve();
                        if (opcode.comment == null) {
                            opcode.comment = " @" + fr.name;
                        } else {
                            opcode.comment = opcode.comment + ", ALSO: @" + fr.name;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < global.size(); i++) {
            Opcode opcode = global.get(i);
            System.out.println(String.format("%s", opcode.toString()));
        }
        System.out.println("=========");
        System.out.println("Total ops: "+global.size());
    }

    private MyMethod generateMethod(String name, MyMethod myMethod) {
        if (myMethod.opcodes.size() == 0) {
            if (name.contains("lambda_1000")) {
                name = name;
            }
            List<VariableDeclaration> parameters = myMethod.parameters;
            for (VariableDeclaration parameter : parameters) {
                if (parameter instanceof SingleVariableDeclaration) {
                    SingleVariableDeclaration svd = (SingleVariableDeclaration) parameter;
                    myMethod.addVariable(svd.getType().toString(), parameter.getName().toString());
                } else if (parameter instanceof VariableDeclarationFragment) {
                    VariableDeclarationFragment vdf = (VariableDeclarationFragment)parameter;
                    myMethod.addVariable("@untyped lambda@", parameter.getName().toString());
                } else {
                    throw new CompilerException("Must be single variable declaration in parameter!", parameter);
                }
            }
            ASTNode b = myMethod.body;
            if (b instanceof Block) {
                Block body = (Block) b;
                List<Statement> statements = body.statements();
                for (Statement statement : statements) {
                    generateStatement(myMethod, statement);
                }
                myMethod.opcodes.get(0).comment = " <== " + name + "  " + parameters + " (as blk)";
            } else if (b instanceof Expression) {
                generateExpression(myMethod, (Expression) b);
                myMethod.addOpcode(new Opcode("RTN"));
                myMethod.opcodes.get(0).comment = " <== " + name + "  " + parameters + " (as expr)";
            } else {
                System.out.println("Oh");
            }
        }
        return myMethod;
    }

    private void generateStatement(MyMethod myMethod, Statement statement) {
        if (statement instanceof VariableDeclarationStatement) {
            VariableDeclarationStatement vds = (VariableDeclarationStatement)statement;
            List fragments = vds.fragments();
            if (fragments.size() != 1) {
                System.out.println("Do not support multiple fragments for "+statement);
            }
            VariableDeclarationFragment o = (VariableDeclarationFragment)fragments.get(0);
            SimpleName name = o.getName();
            myMethod.addVariable(vds.getType().toString(), name.toString());
            Expression initializer = o.getInitializer();
            if (initializer != null) {
                generateExpression(myMethod, initializer);
                myMethod.addOpcode(new Opcode("ST", 0, myMethod.variables.get(name.toString())));
            }

        } else if (statement instanceof ReturnStatement) {
            if (!(statement.getParent().getParent() instanceof MethodDeclaration)) {
                throw new CompilerException("Return must be from main method only", statement);
            }
            Expression expression = ((ReturnStatement) statement).getExpression();
            generateExpression(myMethod, expression);
            myMethod.addOpcode(new Opcode("RTN"));
        } else if (statement instanceof IfStatement) {
            IfStatement ifs = (IfStatement)statement;
            Expression expression = ifs.getExpression();
            Statement thenStatement = ifs.getThenStatement();
            Statement elseStatement = ifs.getElseStatement();
            generateExpression(myMethod, expression);
            myMethod.addOpcode(new Opcode("SEL", new BranchRef(myMethod, thenStatement, "then statement"), new BranchRef(myMethod, elseStatement,"else statement")));
        } else if (statement instanceof ThrowStatement) {
            myMethod.addOpcode(new Opcode("BRK"));
        } else if (statement instanceof Block) {
            Block blk = (Block)statement;
            List<Statement> statements = blk.statements();
            for (Statement stm : statements) {
                generateStatement(myMethod, stm);
            }
        } else if (statement instanceof ExpressionStatement) {
            ExpressionStatement es = (ExpressionStatement)statement;

            if (es.toString().startsWith("System.out.print")) {
                // ok
            } else if (es.getExpression() instanceof MethodInvocation &&
                    (((MethodInvocation)es.getExpression()).getName().toString().equals("debug")
                    || ((MethodInvocation)es.getExpression()).getName().toString().equals("breakpoint"))) {
                generateExpression(myMethod, es.getExpression());
            } else if (es.getExpression() instanceof Assignment) {
                Assignment as = (Assignment)es.getExpression();
                Expression leftHandSide = as.getLeftHandSide();
                if (leftHandSide instanceof SimpleName) {
                    generateExpression(myMethod, as.getRightHandSide());
                    Integer varix = myMethod.variables.get(leftHandSide.toString());
                    if (varix == null) throw new CompilerException("Assignment to unknown var", es);
                    myMethod.addOpcode(new Opcode("ST",0,varix));
                } else {
                    throw new CompilerException("Assignment is non-trivial", es);
                }
            } else {
                throw new CompilerException("void expression?",statement);
            }
        } else {
            throw new CompilerException("unknown statement", statement);
        }
    }

    public class QualifiedNameResolved {
        MyTyple tuple;
        ArrayList<Opcode> accessor;

        public QualifiedNameResolved(MyTyple tuple, ArrayList<Opcode> accessor) {
            this.tuple = tuple;
            this.accessor = accessor;
        }
    }

    private void generateExpression(MyMethod myMethod, Expression expression) {
        if (expression instanceof NumberLiteral) {
            myMethod.addOpcode(new Opcode("LDC", new Integer(expression.toString())).commented("just constant from code"));
        } else if (expression instanceof PrefixExpression) {
            PrefixExpression pe = (PrefixExpression)expression;
            if (pe.getOperator() == PrefixExpression.Operator.MINUS && pe.getOperand() instanceof NumberLiteral) {
                myMethod.addOpcode(new Opcode("LDC", -new Integer(pe.getOperand().toString())).commented("just negative constant from code"));
            } else {
                throw new CompilerException("Prefix expression not supported (yet?) ", expression);
            }
        } else if (expression instanceof InfixExpression) {
            InfixExpression ie = (InfixExpression)expression;
            if (ie.getOperator().toString().equals("+")) {
                generateExpression(myMethod, ie.getLeftOperand());
                generateExpression(myMethod, ie.getRightOperand());
                myMethod.addOpcode(new Opcode("ADD"));
            } else if (ie.getOperator().toString().equals("-")) {
                generateExpression(myMethod, ie.getLeftOperand());
                generateExpression(myMethod, ie.getRightOperand());
                myMethod.addOpcode(new Opcode("SUB"));
            } else if (ie.getOperator().toString().equals("*")) {
                generateExpression(myMethod, ie.getLeftOperand());
                generateExpression(myMethod, ie.getRightOperand());
                myMethod.addOpcode(new Opcode("MUL"));
            } else if (ie.getOperator().toString().equals("&&")) {
                generateExpression(myMethod, ie.getLeftOperand());
                generateExpression(myMethod, ie.getRightOperand());
                myMethod.addOpcode(new Opcode("MUL"));
            } else if (ie.getOperator().toString().equals("||")) {
                generateExpression(myMethod, ie.getLeftOperand());
                generateExpression(myMethod, ie.getRightOperand());
                myMethod.addOpcode(new Opcode("ADD"));
            } else if (ie.getOperator().toString().equals("/")) {
                generateExpression(myMethod, ie.getLeftOperand());
                generateExpression(myMethod, ie.getRightOperand());
                myMethod.addOpcode(new Opcode("DIV"));
            } else if (ie.getOperator().toString().equals(">")) {
                generateExpression(myMethod, ie.getLeftOperand());
                generateExpression(myMethod, ie.getRightOperand());
                myMethod.addOpcode(new Opcode("CGT"));
            } else if (ie.getOperator().toString().equals(">=")) {
                generateExpression(myMethod, ie.getLeftOperand());
                generateExpression(myMethod, ie.getRightOperand());
                myMethod.addOpcode(new Opcode("CGTE"));
            } else if (ie.getOperator().toString().equals("<")) {
                generateExpression(myMethod, ie.getRightOperand());
                generateExpression(myMethod, ie.getLeftOperand());
                myMethod.addOpcode(new Opcode("CGT"));
            } else if (ie.getOperator().toString().equals("<=")) {
                generateExpression(myMethod, ie.getRightOperand());
                generateExpression(myMethod, ie.getLeftOperand());
                myMethod.addOpcode(new Opcode("CGT"));
            } else if (ie.getOperator().toString().equals("==")) {
                if (ie.getRightOperand().toString().equals("null")) {       // compare with null
                    generateExpression(myMethod, ie.getLeftOperand());
                    myMethod.addOpcode(new Opcode("ATOM"));
                } else {
                    generateExpression(myMethod, ie.getLeftOperand());
                    generateExpression(myMethod, ie.getRightOperand());
                    myMethod.addOpcode(new Opcode("CEQ"));
                }
            } else if (ie.getOperator().toString().equals("!=")) {
                if (ie.getRightOperand().toString().equals("null")) {       // compare with null
                    generateExpression(myMethod, ie.getLeftOperand());
                    myMethod.addOpcode(new Opcode("ATOM"));
                    myMethod.addOpcode(new Opcode("LDC","1").commented("for negation"));
                    myMethod.addOpcode(new Opcode("SUB"));

                } else {
                    generateExpression(myMethod, ie.getLeftOperand());
                    generateExpression(myMethod, ie.getRightOperand());
                    myMethod.addOpcode(new Opcode("CEQ"));
                    myMethod.addOpcode(new Opcode("LDC","1").commented("for negation"));
                    myMethod.addOpcode(new Opcode("SUB"));
                }
            } else {
                throw new CompilerException("Unknown opcode", expression.getParent());
            }
        } else if (expression instanceof ClassInstanceCreation) {
            ClassInstanceCreation cic = (ClassInstanceCreation)expression;
            String className = cleanupTemplates(cic.getType().toString());
            MyTyple myTyple = tuples.get(className);
            if (myTyple == null) throw new CompilerException("Unable to instantiate unknown tuple: "+className, expression);
            List<Expression> arguments = cic.arguments();
            if (myTyple.positions.size() != arguments.size()) throw new CompilerException("Unable to instantiate tuple (size mismatch): "+className, expression);
            if (myTyple.positions.size() < 2) throw new CompilerException("Tuple must have more than 1 element: "+className, expression);
            for (int i = 0; i < arguments.size(); i++) {
                generateExpression(myMethod, arguments.get(i));
            }
            for (int i = 0; i < arguments.size()-1; i++) {
                myMethod.addOpcode(new Opcode("CONS"));
            }
        } else if (expression instanceof QualifiedName || expression instanceof SimpleName) {
            QualifiedNameResolved qnr = resolveName(myMethod, (Name)expression);
            for (Opcode opcode : qnr.accessor) {
                myMethod.addOpcode(opcode);
            }
            // qn.
        } else if (expression instanceof ConditionalExpression) {
            ConditionalExpression ce = (ConditionalExpression)expression;
            generateExpression(myMethod, ce.getExpression());
            myMethod.addOpcode(new Opcode("SEL", new ExpressionRef(myMethod, ce.getThenExpression()), new ExpressionRef(myMethod, ce.getElseExpression())));
        } else if (expression instanceof NullLiteral) {
            myMethod.addOpcode(new Opcode("LDC", 0).commented("NULL literal"));
        } else if (expression instanceof CastExpression) {
            CastExpression ca = (CastExpression)expression;
            generateExpression(myMethod, ca.getExpression());
        } else if (expression instanceof ParenthesizedExpression) {
            ParenthesizedExpression pe = (ParenthesizedExpression)expression;
            generateExpression(myMethod, pe.getExpression());
        } else if (expression instanceof LambdaExpression) {
            LambdaExpression le = (LambdaExpression)expression;
            final List parameters = ((LambdaExpression) expression).parameters();
            final ASTNode body = le.getBody();
            String name = "lambda_"+(lambdaCount++);
            MyMethod mm = new MyMethod(name, parameters, body);
            mm.parentMethod = myMethod;
            methods.add(mm);
            myMethod.addOpcode(new Opcode("LDF", new FunctionRef(name)));
        } else if (expression instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation)expression;
            List<Expression> arguments = mi.arguments();
            for (int i = 0; i < arguments.size(); i++) {
                Expression expression1 = arguments.get(i);
                generateExpression(myMethod, expression1);
            }
            String methodName = mi.getName().toString();
            if (methodName.toString().equals("cons")) {
                myMethod.addOpcode(new Opcode("CONS"));
            } else if (methodName.toString().equals("tail")) {
                myMethod.addOpcode(new Opcode("CDR"));
            } else if (methodName.toString().equals("head")) {
                myMethod.addOpcode(new Opcode("CAR"));
            } else if (methodName.toString().equals("first")) {
                myMethod.addOpcode(new Opcode("CAR"));
            } else if (methodName.toString().equals("second")) {
                myMethod.addOpcode(new Opcode("CDR"));
            } else if (methodName.toString().equals("breakpoint")) {
                myMethod.addOpcode(new Opcode("BRK"));
            } else if (methodName.toString().equals("debug")) {
                myMethod.addOpcode(new Opcode("DBUG"));
            } else {
                MyMethod userMethod = getMethod(methodName);
                if (userMethod != null) {
                    generateMethod(userMethod.name, userMethod);
                    if (userMethod.variables.size() < arguments.size()) throw new CompilerException("User Method call: fewer number of arguments", expression);
                    for(int i=arguments.size(); i<userMethod.variables.size(); i++) {
                        myMethod.addOpcode(new Opcode("LDC 0").commented("local var space"));
                    }
                    myMethod.addOpcode(new Opcode("LDF", new FunctionRef(methodName)));
                    myMethod.addOpcode(new Opcode("AP", userMethod.variables.size()));
                } else if (methodName.equals("apply")) {
                    if (mi.getExpression() != null) {
                        generateExpression(myMethod, mi.getExpression());
                        myMethod.addOpcode(new Opcode("AP", arguments.size()));
                    } else {
                        throw new CompilerException("Apply wants expression", expression);
                    }
                } else {
                    throw new CompilerException("Unknown user method expression, forgotten @Compiled? ", expression);
                }
            }
        } else {
            throw new CompilerException("Unknown expression", expression);
        }
        // expression.resolveTypeBinding();
    }

    private QualifiedNameResolved generateLoadSimpleName(MyMethod myMethod, SimpleName sn) {
        int level = 0;
        MyMethod currentMethod = myMethod;
        Integer varix = null;
        String vartype = null;
        while(currentMethod != null) {
            varix = currentMethod.variables.get(sn.toString());   // index of q
            if (varix != null) {
                vartype = currentMethod.variableTypes.get(varix);
                break;
            }
            currentMethod = currentMethod.parentMethod;
            level++;
        }
        if (varix == null) throw new CompilerException("Unable to find variable", sn);
        ArrayList<Opcode> accessor = new ArrayList<>();
        Opcode ld = new Opcode("LD", level, varix);
        ld.comment = "var "+sn.toString();
        accessor.add(ld);
        return new QualifiedNameResolved(tuples.get(cleanupTemplates(vartype)), accessor);
    }

    private QualifiedNameResolved resolveName(MyMethod myMethod, Name qualifier) {
        if (qualifier instanceof SimpleName) {
            QualifiedNameResolved qualifiedNameResolved = generateLoadSimpleName(myMethod, (SimpleName) qualifier);
            return qualifiedNameResolved;
        } else if (qualifier instanceof QualifiedName) {
            if (qualifier.toString().equals("ec.pe")) {
                qualifier = qualifier;
            }
            Name qn = ((QualifiedName) qualifier).getQualifier();
            QualifiedNameResolved mt = resolveName(myMethod, qn);
            if (mt == null) throw new CompilerException("Unable to completely resolve qualified name: ", qn);
            if (mt.tuple == null) {
                throw new CompilerException("Could not find type of base expression, maybe forgot @Compile in class declaration or untyped lambda arg?", qualifier);
            }
            ArrayList<Opcode> accessor = new ArrayList<>();
            SimpleName name = ((QualifiedName) qualifier).getName();
            Integer position = mt.tuple.positions.get(name.toString());
            accessor.addAll(mt.accessor);

            generateTupleAccess(accessor, position, mt.tuple.positions.size());
            String typleIndex = mt.tuple.types.get(position);
            MyTyple myTyple = tuples.get(typleIndex != null ? typleIndex : "XXXXX@NONEXIST");
            return new QualifiedNameResolved(myTyple, accessor);
        } else throw new CompilerException("Unsupported (yet?) name",qualifier);
    }

    private void generateTupleAccess(ArrayList<Opcode> accessor, Integer position, int tupleSize) {
        int ix = accessor.size();
        if (position == tupleSize - 1) {
            for(int i=0; i<=tupleSize-2; i++) {
                accessor.add(new Opcode("CDR"));
            }
            accessor.add(new Opcode("CDR"));
        } else {
            for(int i=0; i<position; i++) {
                accessor.add(new Opcode("CDR"));
            }
            accessor.add(new Opcode("CAR"));
        }
        accessor.get(ix).comment = "generateTupleAccess total="+tupleSize+" pos="+position;
    }

    static int lambdaCount = 1000;

    private String cleanupTemplates(String s) {
        if (s.contains("<")) {
            return s.substring(0, s.indexOf("<"));
        }
        return s;
    }


    class MyTyple {
        HashMap<String, Integer> positions = new HashMap<>();
        HashMap<Integer, String> types = new HashMap<>();
        String name;

        MyTyple(String name) {
            this.name = name;
        }
    }

    HashMap<String, MyTyple> tuples = new HashMap<>();

    ArrayList<MyMethod> methods = new ArrayList<>();


    private void addTypes(TypeDeclaration typeDeclaration) {
        TypeDeclaration[] types = typeDeclaration.getTypes();
        for (TypeDeclaration type : types) {
            for (Object o : type.modifiers()) {
                if (o instanceof MarkerAnnotation) {
                    if (o.toString().equals("@Compiled")) {
                        addTuple(type);
                    }
                }
            }
        }
        MethodDeclaration[] methods = typeDeclaration.getMethods();
        for (int i = 0; i < methods.length; i++) {
            MethodDeclaration method = methods[i];
            for (Object o : method.modifiers()) {
                if (o instanceof MarkerAnnotation) {
                    if (o.toString().equals("@Compiled")) {
                        addMethod(method);
                    }
                }
            }
        }
    }

    private void addMethod(MethodDeclaration method) {
        SimpleName name = method.getName();
        methods.add(new MyMethod(name.toString(), method.parameters(), method.getBody()));
    }

    private void addTuple(TypeDeclaration type) {
        List list = type.bodyDeclarations();
        int ix = 0;
        MyTyple mt = new MyTyple(type.getName().toString());
        for (Object o : list) {
            if (o instanceof FieldDeclaration) {
                FieldDeclaration f = (FieldDeclaration)o;
                List fragments = f.fragments();
                if (fragments.size() != 1){
                    throw new CompilerException("Invalid field declaration inside class "+type.getName(), type);
                }
                String fieldName = fragments.get(0).toString();
                mt.positions.put(fieldName, ix);
                mt.types.put(ix, f.getType().toString());
                ix++;
            }
        }
        tuples.put(type.getName().toString(), mt);
        //type.
    }

    class FunctionRef {
        String name;

        FunctionRef(String name) {
            this.name = name;
        }

        public int resolve() {
            MyMethod myMethod = getMethod(name);
            return myMethod.offset;
        }
    }

    private MyMethod getMethod(String name) {
        for (MyMethod method : methods) {
            if (method.name.equals(name)) return method;
        }
        return null;
    }

    interface FutureReference {
        public int resolve();
    }

    class BranchRef implements FutureReference {
        MyMethod mtd;
        Statement expr;
        String comment;

        BranchRef(MyMethod mtd, Statement expr, String comment) {
            this.mtd = mtd;
            this.expr = expr;
        }

        public int resolve() {
            int loffs = mtd.opcodes.size();
            if (expr != null) {
                generateStatement(mtd, expr);
            }
            Opcode join = new Opcode("JOIN");
            mtd.addOpcode(join);
            if (comment == null) {
                comment = "branch@"+(loffs+mtd.offset);
            }
            mtd.opcodes.get(loffs).comment = comment;
            return loffs + mtd.offset;
        }
    }

    class ExpressionRef implements FutureReference {
        MyMethod mtd;
        Expression expr;
        String comment;

        ExpressionRef(MyMethod mtd, Expression expr) {
            this.mtd = mtd;
            this.expr = expr;
        }

        public int resolve() {
            int loffs = mtd.opcodes.size();
            if (expr != null) {
                generateExpression(mtd, expr);
            }
            mtd.addOpcode(new Opcode("JOIN"));
            mtd.opcodes.get(loffs).comment = comment;
            return loffs + mtd.offset;
        }
    }

    static public class Opcode {
        String name;
        Object[] arguments;
        public int offset;
        String comment;

        public Opcode(String name, Object... arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        Opcode commented(String comment) {
            this.comment = comment;
            return this;
        }

        String rpad(String s, int max) {
            while (s.length() < max) {
                s = s + " ";
            }
            return s;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("");
            sb.append(rpad(name, 6));
            sb.append(" ");
            for (Object argument : arguments) {
                sb.append(argument);
                sb.append(" ");
            }
            String str = rpad(sb.toString().trim(), 20);
            if (comment != null) {
                str += "; "+comment;
            }
            return str;

        }
    }

    private class MyMethod {
        List<VariableDeclaration> parameters;
        int offset;
        String name;
        MyMethod parentMethod;

        HashMap<String, Integer> variables = new HashMap<>();

        HashMap<Integer, String> variableTypes = new HashMap<>();
        ArrayList<Opcode> opcodes = new ArrayList<>();
        public ASTNode body;

        private MyMethod(String name, List<VariableDeclaration> parameters, ASTNode body) {
            this.parameters = parameters;
            this.body = body;
            this.name = name;
        }

        public void addOpcode(Opcode op) {
            opcodes.add(op);
        }
        public int addVariable(String type, String name) {
            int ix = variables.size();
            variables.put(name, ix);
            variableTypes.put(ix, type);
            return ix;
        }
    }
}

