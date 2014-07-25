import java.util.*;
import java.util.Map.Entry;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

public class LambdaCompiler {

	public List<String> ruleNames = null;
	
	public LambdaCompiler(List<String> rules) {
		this.ruleNames = rules;
	}
	
	public static interface IParam {
		public Integer resolve();
	}

	public static class I {  // instruction
		public enum Type {ERROR, CONS, DUM, LDC, LDF, RAP, RTN, LD, ADD, AP};
		public I (Type t) {
			this.type = t;
		}
		public Type type = Type.ERROR;
		public List<IParam> params = new ArrayList<IParam>();
		public int instruction_number = -1;
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(type.toString()+ " ");
			for (IParam param : params) {
				if (!(param instanceof IntParam)) {
					throw new RuntimeException("unsupported param" + param);
				}
				builder.append(((IntParam)param).toString());
				builder.append(" ");
			}
			builder.append("\n");
			return builder.toString();
		}
		
		public void resolveParams() {
			for (int i = 0; i < params.size(); ++i) {
				params.set(i, new IntParam(params.get(i).resolve()));
			}
		}
	}

	public static class IntParam implements IParam {
		private Integer x_ = 0;
		IntParam(int x) {
			this.x_ = x;
		}
		public Integer resolve() {
			return this.x_;
		}
		public String toString() {
			return this.x_.toString();
		}
	}
	
	public static class RefParam implements IParam {
		private I instruction_;
		public RefParam(I instruction) {
			this.instruction_ = instruction;
		}
		public Integer resolve() {
			if (this.instruction_.instruction_number == -1) {
				throw new RuntimeException("instruction number should be set already!");
			}
			return this.instruction_.instruction_number;
		}
	}
	
	public List<I> instructions = new ArrayList<I>();

	public class Func {
		public int topFrameIndex = 0;
		public I firstInstr = null;
		Func(int x, I instr) {
			this.topFrameIndex = x;
			this.firstInstr = instr;
		}
	}
	Map<String, Func> functionMap = new HashMap<String, Func>();  // name -> func def 
	public int topFrameDeclCount = 0;
	
	public List<I> processFunctionExpression(ECMAScriptParser.FunctionExpressionContext func) {
		String func_name = func.Identifier().toString();
		if (func.functionBody() == null) {
			throw new RuntimeException("function body expected");
		}
		List<I> function_body = new ArrayList<I>();
		if (func.functionBody().sourceElements() != null) {
			function_body = this.processSourceElements(func.functionBody().sourceElements());
		}
		if (func_name.length() == 0) {
			throw new RuntimeException("expecting name" + func.toStringTree(ruleNames));
		}
		functionMap.put(func_name, new Func(topFrameDeclCount, function_body.get(0)));
		topFrameDeclCount += 1;
		return function_body;
	}

	public List<I> processArrayLiteralExpression(ECMAScriptParser.ArrayLiteralExpressionContext ast) {
		List<I> instrs = new ArrayList<I>();
		ECMAScriptParser.ArrayLiteralContext arr = ast.arrayLiteral();
		if (arr == null) {
			throw new RuntimeException("array literal expected");
		}
		
		for (ECMAScriptParser.SingleExpressionContext expr : arr.elementList().singleExpression()) {
			boolean is_first = instrs.size() == 0;
			instrs.addAll(processSingleExpression(expr));
			if (!is_first) {
				instrs.add(new I(I.Type.CONS));
			}
		}
		return instrs;
	}

	public List<I> processLiteralExpression(ECMAScriptParser.LiteralExpressionContext ast) {
		List<I> instrs = new ArrayList<I>();
		ECMAScriptParser.LiteralContext lit = ast.literal();
		if (lit == null) {
			throw new RuntimeException("literal expected");
		}
		if (lit.numericLiteral() == null || lit.numericLiteral().DecimalLiteral() == null) {
			throw new RuntimeException("numeric literal expected" + lit.toStringTree(ruleNames));
		}
		Integer num = Integer.parseInt(lit.numericLiteral().DecimalLiteral().toString());
		I x = new I(I.Type.LDC);
		x.params.add(new IntParam(num));
		instrs.add(x);
		return instrs;
	}

	public List<I> processIdentifierExpression(ECMAScriptParser.IdentifierExpressionContext ast) {
		List<I> instrs = new ArrayList<I>();
		String func_name  = ast.Identifier().toString();
		if (func_name.length() == 0) {
			throw new RuntimeException("name expected");
		}
		if (!functionMap.containsKey(func_name)) {
			throw new RuntimeException("function " + func_name + " not found");
		}
		
		I ld = new I(I.Type.LD);
		ld.params.add(new IntParam(1)); // todo: recursive calls!
		ld.params.add(new IntParam(functionMap.get(func_name).topFrameIndex));
		instrs.add(ld);
		return instrs;
	}
	
	public List<I> processSingleExpression(ECMAScriptParser.SingleExpressionContext ast) {
		if (ast instanceof ECMAScriptParser.FunctionExpressionContext) {
			return processFunctionExpression((ECMAScriptParser.FunctionExpressionContext)ast);
		} else if (ast instanceof ECMAScriptParser.ArrayLiteralExpressionContext) {
			return processArrayLiteralExpression((ECMAScriptParser.ArrayLiteralExpressionContext)ast);
		} else if (ast instanceof ECMAScriptParser.LiteralExpressionContext) {
			return processLiteralExpression((ECMAScriptParser.LiteralExpressionContext)ast);
		} else if (ast instanceof ECMAScriptParser.IdentifierExpressionContext) {
			return processIdentifierExpression((ECMAScriptParser.IdentifierExpressionContext)ast);
		} else {
			throw new RuntimeException("unsupported expression" + ast.toStringTree(this.ruleNames));
		}
	}

	public List<I> processReturnStatement(ECMAScriptParser.ReturnStatementContext ast) {
		List<I> instrs = new ArrayList<I>();
		if (ast.expressionSequence() == null) {
			throw new RuntimeException("expression sequence expected");
		}
		for (ECMAScriptParser.SingleExpressionContext expr : ast.expressionSequence().singleExpression()) {
			instrs.addAll(this.processSingleExpression(expr));
		}
		instrs.add(new I(I.Type.RTN));
		return instrs;
	}
	
	public List<I> processSourceElements(ECMAScriptParser.SourceElementsContext ast) {
		List<I> instrs = new ArrayList<I>();
		for (ECMAScriptParser.SourceElementContext sourceElem : ast.sourceElement()) {
			if (sourceElem.statement() == null) {
				throw new RuntimeException("statement expected");
			}
			if (sourceElem.statement().expressionStatement() != null) {
				if (sourceElem.statement().expressionStatement().expressionSequence() == null) {
					throw new RuntimeException("expression sequence expected");
				}
				for (ECMAScriptParser.SingleExpressionContext expr : sourceElem.statement().expressionStatement().expressionSequence().singleExpression()) {
					instrs.addAll(this.processSingleExpression(expr));
				}
			} else if (sourceElem.statement().returnStatement() != null) {
				instrs.addAll(this.processReturnStatement(sourceElem.statement().returnStatement()));
			} else {
				throw new RuntimeException("unsupported statement");
			}
		}
		return instrs;
	}

	public void generateInstructions(ECMAScriptParser.ProgramContext ast) { 
		if (ast.sourceElements() == null) {
			throw new RuntimeException("expected sourceElements");
		}
		List<I> instrs = processSourceElements(ast.sourceElements());
		for (int i = 0; i < this.topFrameDeclCount; ++i) {
			// hack-hack-hack!
			Func func = null;
		    for (Entry<String, Func> entry : functionMap.entrySet()) {
		        if (entry.getValue().topFrameIndex == i) {
		            func = entry.getValue();
		        }
		    }
		    I ldf = new I(I.Type.LDF);
		    ldf.params.add(new RefParam(func.firstInstr));
		    this.instructions.add(ldf);
		}
		I ap = new I(I.Type.AP);
		ap.params.add(new IntParam(0)); // todo: support params to init;
		
		this.instructions.add(ap); // call init, it is the last function loaded
		this.instructions.add(new I(I.Type.RTN));
		this.instructions.addAll(instrs);
	}
	
	public String process(ECMAScriptParser.ProgramContext ast) {
		this.generateInstructions(ast);
		for (int i = 0; i < instructions.size(); ++i) {
			instructions.get(i).instruction_number = i;
		}
		for (I instr : instructions) {
			instr.resolveParams();
		}
		StringBuilder builder = new StringBuilder();
		for (I instr : instructions) {
			builder.append(instr.toString());
		}
		return builder.toString();
	}

	public static void main(String[] args) {
//		  DUM  2        ; 2 top-level declarations
//		  LDC  2        ; declare constant down
//		  LDF  step     ; declare function step 
//		  LDF  init     ; init function
//		  RAP  2        ; load declarations into environment and run init
//		  RTN           ; final return
//		init:
//		  LDC  42
//		  LD   0 1      ; var step
//		  CONS
//		  RTN           ; return (42, step)
//		step:
//		  LD   0 0      ; var s
//		  LDC  1 
//		  ADD
//		  LD   1 0      ; var down
//		  CONS
//		  RTN           ; return (s+1, down)

    	String expression = "function step() { return [0, 1]; }\n function init() {return [0, step];}\n ";
     
    	{
        	ECMAScriptParser dumb_parser = new Builder.Parser(expression).build();
        	dumb_parser.setBuildParseTree(true);
        	List<String> ruleNames = Arrays.asList(dumb_parser.getRuleNames());
            System.out.println("PRG:" + dumb_parser.program().toStringTree(ruleNames));
    	}

    	ECMAScriptParser parser = new Builder.Parser(expression).build();
    	parser.setBuildParseTree(true);
   
        LambdaCompiler compiler = new LambdaCompiler(Arrays.asList(parser.getRuleNames()));
        System.out.println("Compiled:" + compiler.process(parser.program()));
    }
}