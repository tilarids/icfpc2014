import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;
import org.antlr.v4.runtime.tree.TerminalNode;

public class LambdaCompiler {

	public List<String> ruleNames = null;
	
	public LambdaCompiler(List<String> rules) {
		this.ruleNames = rules;
	}
	
	public static interface IParam {
		public Integer resolve();
	}

	public static class I {  // instruction
		public enum Type {ERROR, CONS, DUM, LDC, LDF, RAP, RTN, LD, ADD, AP, CAR, CDR, JOIN, SEL, 
							TSEL, ATOM, CGT, SUB,  MUL, DIV, CEQ, COMMENT, ST, DBUG, BRK};
		public I (Type t) {
			this.type = t;
		}
		public I (String s) {
			this.comment = s;
			this.type = Type.COMMENT;
		}
		public String comment;
		public Type type = Type.ERROR;
		public List<IParam> params = new ArrayList<IParam>();
		public int instruction_number = -1;
		public String toString() {
			if (this.type == Type.COMMENT) {
				return "; " + this.comment + "\n";
			}
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

	public class Func {  // todo: drop it. variables stack can be used instead, only instruction ref
						  // should be saved here.
		public int topFrameIndex = 0;
		public I firstInstr = null;
		Func(int x, I instr) {
			this.topFrameIndex = x;
			this.firstInstr = instr;
		}
	}
	Map<String, Func> functionMap = new HashMap<String, Func>();  // name -> func def 
	
	public int topFrameDeclCount = 0;
	public Stack<Map<String, Integer>> variablesStack = new Stack<Map<String, Integer>>();
	public List<I> vtable = null;
	int DEFAULT_SIZE = 15;
	int TMP_START = 10;
	int DEFAULT_SKIP = 4;
	public int currentTmpIndex = TMP_START;
	
	public List<I> createVtable(int size) {
		// SEL y x
		// x: FUN
		// JOIN
		// y: SEL
		
		// load mem
		List<I> instrs = new ArrayList<I>();
		instrs.add(new I(" vtable end "));
		instrs.add(0, new I(I.Type.JOIN));			
		for (int i = size - 1; i >=0; --i) {
			instrs.add(0, new I(I.Type.JOIN));			
			I fun = new I(I.Type.LD);
			fun.params.add(new IntParam(0));
			fun.params.add(new IntParam(DEFAULT_SKIP + i));
			instrs.add(0, fun);
			I sel = new I(I.Type.TSEL);
			sel.params.add(new RefParam(instrs.get(2)));
			sel.params.add(new RefParam(fun));
			instrs.add(0, sel);
		}
		instrs.add(0, new I(" vtable start "));

		return instrs;
	}
	
	public List<I> getOrCreateVtable() {
		if (null == vtable) {
			this.vtable = createVtable(DEFAULT_SIZE);
		}
		return vtable;
	}
	
	public List<I> processFunctionExpression(ECMAScriptParser.FunctionExpressionContext func) {
		String func_name = func.Identifier().toString();
		if (func.functionBody() == null) {
			throw new RuntimeException("function body expected");
		}

		this.variablesStack.peek().put(func_name, topFrameDeclCount);

		// prepare stack.
		if (func.formalParameterList() != null) {
			Map<String, Integer> variables = new HashMap<String, Integer>();
			for (int i = 0; i < func.formalParameterList().Identifier().size(); ++i) {
				variables.put(func.formalParameterList().Identifier().get(i).toString(), i);
			}
			this.variablesStack.push(variables);
		}


		// parse the body.
		List<I> function_body = new ArrayList<I>();
		function_body.add(new I(func_name));
		if (func.functionBody().sourceElements() != null) {
			function_body.addAll(this.processSourceElements(func.functionBody().sourceElements()));
		}
		if (func_name.length() == 0) {
			throw new RuntimeException("expecting name" + func.toStringTree(ruleNames));
		}

		// drop stack.
		if (func.formalParameterList() != null) {
			this.variablesStack.pop();
		}
		// save new function defintion.
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
		String id_name  = ast.Identifier().toString();
		if (id_name.length() == 0) {
			throw new RuntimeException("name expected");
		}
		for (int index = this.variablesStack.size() - 1; index >=0; --index) {
			Map<String, Integer> variables = this.variablesStack.get(index);
			if (variables.containsKey(id_name)) {
				I ld = new I(I.Type.LD);
				ld.params.add(new IntParam(this.variablesStack.size() - index - 1));
				ld.params.add(new IntParam(variables.get(id_name)));
				instrs.add(ld);
				break;
			}
		}

		if (instrs.size() == 0) {
			throw new RuntimeException("can't find an id " + ast.toStringTree(ruleNames));
		}
		return instrs;
	}
	
	public List<I> processJmp(I dest) {
		List<I> instrs = new ArrayList<I>();
		I ldc1 = new I(I.Type.LDC);
		ldc1.params.add(new IntParam(1));
		instrs.add(ldc1);
		
		I tsel = new I(I.Type.TSEL);
		tsel.params.add(new RefParam(dest));
		tsel.params.add(new IntParam(0));
		instrs.add(tsel);
		
		return instrs;
	}
	
	public List<I> processArgumentsExpression(ECMAScriptParser.ArgumentsExpressionContext ast) {
		List<I> instrs = new ArrayList<I>();
		if (ast.singleExpression() == null || !(ast.singleExpression() instanceof ECMAScriptParser.IdentifierExpressionContext)) {
			throw new RuntimeException("expected function call");
		}
		int count = 0;
		if (ast.arguments() != null && ast.arguments().argumentList() != null) {
			count = ast.arguments().argumentList().singleExpression().size();
			for (ECMAScriptParser.SingleExpressionContext expr : ast.arguments().argumentList().singleExpression()) {
				instrs.addAll(processSingleExpression(expr));
			}
		}
		String id_name = ((ECMAScriptParser.IdentifierExpressionContext)ast.singleExpression()).Identifier().toString(); 
		if (id_name.equals("car")) {
			instrs.add(new I(I.Type.CAR));
		} else if (id_name.equals("cdr")) {
			instrs.add(new I(I.Type.CDR));
		} else if (id_name.equals("atom")) {
			instrs.add(new I(I.Type.ATOM));
		} else if (id_name.equals("brk")) {
			instrs.add(new I(I.Type.BRK));
		} else if (id_name.equals("dbug")) {
			instrs.add(new I(I.Type.DBUG));
		} else if (id_name.equals("insert_vtable")) {
			instrs.addAll(this.getOrCreateVtable());
		} else if (id_name.equals("init_memory")) {
//			int N = 22;
//			int M = 23;
			for (int i = 0; i < DEFAULT_SIZE; ++i) {
				I ldc = new I(I.Type.LDC);
				ldc.params.add(new IntParam(i));
				instrs.add(ldc);
			}
			instrs.addAll(processSingleExpression(ast.arguments().argumentList().singleExpression(count - 1))); // add again
			I ap = new I(I.Type.AP);
			ap.params.add(new IntParam(count + DEFAULT_SIZE));
			instrs.add(ap);
		} else if (id_name.equals("load_memory")) {
			if (ast.arguments().argumentList().singleExpression().size() != 1) {
				throw new RuntimeException("should be 1");
			}
			String variableName = ((ECMAScriptParser.IdentifierExpressionContext)ast.arguments().argumentList().singleExpression(0)).Identifier().toString();
			Integer variableIndex = this.variablesStack.peek().get(variableName);
			List<I> vtable = this.getOrCreateVtable();
			Integer tmpIndex = currentTmpIndex;
			currentTmpIndex++;
			// ST X -> currentTmpIndex
			// LDC 0 --> end
			// jmp addr
			// false:
			// LDC 1
			// SEL vtable 0
			// jmp rtn
			// true:
			// LDC 43 -- for vtable
			// X' = X' - 1
			// LD X' -- for tsel
			// addr: TSEL true false
			// rtn:
			
			I addr_tsel = new I(I.Type.TSEL);
			
			I load_x = new I(I.Type.LD);
			load_x.params.add(new IntParam(0));
			load_x.params.add(new IntParam(variableIndex));
			instrs.add(load_x);
			
			I store_tmp = new I(I.Type.ST);
			store_tmp.params.add(new IntParam(0));
			store_tmp.params.add(new IntParam(tmpIndex));
			instrs.add(store_tmp);

			I ldc_stop_zero = new I(I.Type.LDC);
			ldc_stop_zero.params.add(new IntParam(0));
			instrs.add(ldc_stop_zero);

			I load_x2 = new I(I.Type.LD);
			load_x2.params.add(new IntParam(0));
			load_x2.params.add(new IntParam(variableIndex));
			instrs.add(load_x2);

			instrs.addAll(processJmp(addr_tsel));
						
			I false_ldc = new I(I.Type.LDC);
			false_ldc.params.add(new IntParam(42));
			instrs.add(false_ldc);
			
			I sel1 = new I(I.Type.SEL);
			sel1.params.add(new RefParam(vtable.get(0)));
			sel1.params.add(new IntParam(0));
			instrs.add(sel1);

			I rtn = new I("return in load_memory");
			instrs.addAll(processJmp(rtn));
			
			I ldc_for_vtable = new I(I.Type.LDC);
			ldc_for_vtable.params.add(new IntParam(43));
			instrs.add(ldc_for_vtable);

			instrs.addAll(this.processDecreaseVariable(tmpIndex));
			
			I ld_for_tsel = new I(I.Type.LD);
			ld_for_tsel.params.add(new IntParam(0));
			ld_for_tsel.params.add(new IntParam(tmpIndex));
			instrs.add(ld_for_tsel);

			addr_tsel.params.add(new RefParam(ldc_for_vtable));
			addr_tsel.params.add(new RefParam(false_ldc));
			
			instrs.add(addr_tsel);
			instrs.add(rtn);
		} else {
			instrs.addAll(processIdentifierExpression((ECMAScriptParser.IdentifierExpressionContext)ast.singleExpression()));
			I ap = new I(I.Type.AP);
			ap.params.add(new IntParam(count));
			instrs.add(ap);
		}

		return instrs;
	}
	
	
	public List<I> processBinaryExpression(List<ECMAScriptParser.SingleExpressionContext> ast, I.Type type) {
		List<I> instrs = new ArrayList<I>();
		if (ast.size() != 2) {
			throw new RuntimeException("expected two elems " + ast.toString());
		}
		for (ECMAScriptParser.SingleExpressionContext expr : ast) {
			instrs.addAll(processSingleExpression(expr));
		}
		instrs.add(new I(type));
		return instrs;
	}

	public List<I> processParenthesizedExpression(ECMAScriptParser.ParenthesizedExpressionContext ast) {
		if (ast.expressionSequence() == null || ast.expressionSequence().singleExpression().size() > 1) {
			throw new RuntimeException("expected 1-sequence" + ast.toStringTree(ruleNames));
		} else {
			return processSingleExpression(ast.expressionSequence().singleExpression(0));
		}
	}

	public List<I> processDecreaseVariable(int variableIndex) {
		List<I> instrs = new ArrayList<I>();
		I ld = new I(I.Type.LD);
		ld.params.add(new IntParam(0));
		ld.params.add(new IntParam(variableIndex));
		instrs.add(ld);

		I ldc_one = new I(I.Type.LDC);
		ldc_one.params.add(new IntParam(1));
		instrs.add(ldc_one);
		instrs.add(new I(I.Type.SUB));

		I st = new I(I.Type.ST);
		st.params.add(new IntParam(0));
		st.params.add(new IntParam(variableIndex));
		instrs.add(st);
		return instrs;
	}
	
	public List<I> processPreDecreaseExpression(ECMAScriptParser.PreDecreaseExpressionContext ast) {
		if (!(ast.singleExpression() instanceof ECMAScriptParser.IdentifierExpressionContext)) {
			throw new RuntimeException("expected identifier");
		}
		String variableName = ((ECMAScriptParser.IdentifierExpressionContext)ast.singleExpression()).Identifier().toString();
		Integer variableIndex = this.variablesStack.peek().get(variableName);
		return processDecreaseVariable(variableIndex);
	}

	public List<I> processPostDecreaseExpression(ECMAScriptParser.PostDecreaseExpressionContext ast) {
		if (!(ast.singleExpression() instanceof ECMAScriptParser.IdentifierExpressionContext)) {
			throw new RuntimeException("expected identifier");
		}
		String variableName = ((ECMAScriptParser.IdentifierExpressionContext)ast.singleExpression()).Identifier().toString();
		Integer variableIndex = this.variablesStack.peek().get(variableName);
		return processDecreaseVariable(variableIndex);
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
		} else if (ast instanceof ECMAScriptParser.ArgumentsExpressionContext) {
			return processArgumentsExpression((ECMAScriptParser.ArgumentsExpressionContext)ast);
		} else if (ast instanceof ECMAScriptParser.GreaterThanExpressionContext) {
			return processBinaryExpression(((ECMAScriptParser.GreaterThanExpressionContext)ast).singleExpression(), I.Type.CGT);
		} else if (ast instanceof ECMAScriptParser.SubtractExpressionContext) {
			return processBinaryExpression(((ECMAScriptParser.SubtractExpressionContext)ast).singleExpression(), I.Type.SUB);
		} else if (ast instanceof ECMAScriptParser.AddExpressionContext) {
			return processBinaryExpression(((ECMAScriptParser.AddExpressionContext)ast).singleExpression(), I.Type.ADD);
		} else if (ast instanceof ECMAScriptParser.MultiplyExpressionContext) {
			return processBinaryExpression(((ECMAScriptParser.MultiplyExpressionContext)ast).singleExpression(), I.Type.MUL);
		} else if (ast instanceof ECMAScriptParser.DivideExpressionContext) {
			return processBinaryExpression(((ECMAScriptParser.DivideExpressionContext)ast).singleExpression(), I.Type.DIV);
		} else if (ast instanceof ECMAScriptParser.EqualsExpressionContext) {
			return processBinaryExpression(((ECMAScriptParser.EqualsExpressionContext)ast).singleExpression(), I.Type.CEQ);
		} else if (ast instanceof ECMAScriptParser.ParenthesizedExpressionContext) {
			return processParenthesizedExpression((ECMAScriptParser.ParenthesizedExpressionContext)ast);
		} else if (ast instanceof ECMAScriptParser.PreDecreaseExpressionContext) {
			return processPreDecreaseExpression((ECMAScriptParser.PreDecreaseExpressionContext)ast);
		} else if (ast instanceof ECMAScriptParser.PostDecreaseExpressionContext) {
			return processPostDecreaseExpression((ECMAScriptParser.PostDecreaseExpressionContext)ast);
		} else {
			throw new RuntimeException("unsupported expression " + ast.toStringTree(this.ruleNames));
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

	public List<I> processIfStatement(ECMAScriptParser.IfStatementContext ast) {
		List<I> instrs = new ArrayList<I>();
		if (ast.expressionSequence() == null || ast.expressionSequence().singleExpression().size() != 1 ) {
			throw new RuntimeException("expected expr " + ast.toStringTree(ruleNames));
		}
		List<I> cond = processSingleExpression(ast.expressionSequence().singleExpression(0));
		if (ast.statement().size() != 2) {
			throw new RuntimeException("expected 2 elems " + ast.statement().toString());
		}
		
		List<I> true_st = processStatement(ast.statement(0));
		List<I> false_st = processStatement(ast.statement(1));
		
		I ldc = new I(I.Type.LDC);
		ldc.params.add(new IntParam(1));
		instrs.add(ldc);
		I sel = new I(I.Type.TSEL);
		sel.params.add(new RefParam(cond.get(0)));
		sel.params.add(new IntParam(0));
		instrs.add(sel); // unconditional
		
		instrs.addAll(true_st);
		instrs.add(new I(I.Type.JOIN));
		instrs.addAll(false_st);
		instrs.add(new I(I.Type.JOIN));

		instrs.addAll(cond);
		I cond_sel = new I(I.Type.TSEL);
		cond_sel.params.add(new RefParam(true_st.get(0)));
		cond_sel.params.add(new RefParam(false_st.get(0)));
		instrs.add(cond_sel); // conditional

		return instrs;
	}
	
	public List<I> processBlock(ECMAScriptParser.BlockContext ast) {
		List<I> instrs = new ArrayList<I>();
		if (ast.statementList() == null) {
			throw new RuntimeException("statement list expected");
		}
		for (ECMAScriptParser.StatementContext stmt : ast.statementList().statement()) {
			instrs.addAll(processStatement(stmt));
		}
		return instrs;
	}

	public List<I> processWhileStatement(ECMAScriptParser.WhileStatementContext ast) {
		if (ast.statement() == null || ast.expressionSequence() == null || ast.expressionSequence().singleExpression().size() != 1) {
			throw new RuntimeException("malformed while");
		}
		// JMP addr
		// stmt
		//   expr
		// addr:
		// TSEL stmt while_end
		// while_end:
		List<I> stmt = processStatement(ast.statement());
		List<I> expr = processSingleExpression(ast.expressionSequence().singleExpression(0));
		List<I> instrs = new ArrayList<I>();

		instrs.addAll(processJmp(expr.get(0)));
		
		instrs.addAll(stmt);
		instrs.addAll(expr);
		
		I while_end = new I("while end");
		I cond_tsel = new I(I.Type.TSEL);
		cond_tsel.params.add(new RefParam(stmt.get(0)));
		cond_tsel.params.add(new RefParam(while_end));
		instrs.add(cond_tsel);
		instrs.add(while_end);
		return instrs;
	}
	
	public List<I> processIterationStatement(ECMAScriptParser.IterationStatementContext ast) {
		if (!(ast instanceof ECMAScriptParser.WhileStatementContext)) {
			throw new RuntimeException("expected while");
		}
		return processWhileStatement((ECMAScriptParser.WhileStatementContext)ast);
	}
	
	public List<I> processStatement(ECMAScriptParser.StatementContext ast) {
		List<I> instrs = new ArrayList<I>();
		if (ast.expressionStatement() != null) {
			if (ast.expressionStatement().expressionSequence() == null) {
				throw new RuntimeException("expression sequence expected");
			}
			for (ECMAScriptParser.SingleExpressionContext expr : ast.expressionStatement().expressionSequence().singleExpression()) {
				instrs.addAll(this.processSingleExpression(expr));
			}
		} else if (ast.returnStatement() != null) {
			instrs.addAll(this.processReturnStatement(ast.returnStatement()));
		} else if (ast.ifStatement() != null) {
			instrs.addAll(this.processIfStatement(ast.ifStatement()));
		} else if (ast.block() != null) {
			instrs.addAll(this.processBlock(ast.block()));
		} else if (ast.iterationStatement() != null) {
			instrs.addAll(this.processIterationStatement(ast.iterationStatement()));
		} else if (ast.emptyStatement() != null) {
			// do nothing.
		} else {
			throw new RuntimeException("unsupported statement" + ast.toStringTree(ruleNames));
		}
		return instrs;
	}
	
	public List<I> processSourceElements(ECMAScriptParser.SourceElementsContext ast) {
		List<I> instrs = new ArrayList<I>();
		for (ECMAScriptParser.SourceElementContext sourceElem : ast.sourceElement()) {
			if (sourceElem.statement() == null) {
				throw new RuntimeException("statement expected");
			}
			instrs.addAll(processStatement(sourceElem.statement()));
		}
		return instrs;
	}

	public void generateInstructions(ECMAScriptParser.ProgramContext ast) { 
		if (ast.sourceElements() == null) {
			throw new RuntimeException("expected sourceElements");
		}
		
		this.variablesStack.push(new HashMap<String, Integer>());
		
		List<I> instrs = processSourceElements(ast.sourceElements());
		I dum = new I(I.Type.DUM);
		dum.params.add(new IntParam(this.topFrameDeclCount - 1));
		this.instructions.add(dum);
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
		I rap = new I(I.Type.RAP);
		rap.params.add(new IntParam(this.topFrameDeclCount - 1)); // todo: support correct params to init.
		
		this.instructions.add(rap); // call init, it is the last function loaded.
		this.instructions.add(new I(I.Type.RTN));
		this.instructions.addAll(instrs);
	}
	
	public String process(ECMAScriptParser.ProgramContext ast, boolean generateComments, boolean generateLines) {
		this.generateInstructions(ast);
		int index = 0;
		for (int i = 0; i < instructions.size(); ++i) {
			instructions.get(i).instruction_number = index;
			if (instructions.get(i).type != I.Type.COMMENT) {
				++index;
			}
		}
		for (I instr : instructions) {
			instr.resolveParams();
		}
		StringBuilder builder = new StringBuilder();
		for (I instr : instructions) {
			if (instr.type == I.Type.COMMENT && !generateComments) {
				continue;
			}
			if (generateLines) {
				builder.append(instr.instruction_number);
				builder.append(": ");
			}
			builder.append(instr.toString());
		}
		return builder.toString();
	}

	public static void main(String[] args) throws IOException {
		String expression = new String(Files.readAllBytes(FileSystems.getDefault().getPath("src/input.js")));
//    	String expression = "" +
//				"function step(state, world) { if(0) {return [0, state];} else {return [1, state];} }" + 
//				"function init() {return [3, step];} ";
     
    	{
        	ECMAScriptParser dumb_parser = new Builder.Parser(expression).build();
        	dumb_parser.setBuildParseTree(true);
        	List<String> ruleNames = Arrays.asList(dumb_parser.getRuleNames());
            System.out.println("PRG:" + dumb_parser.program().toStringTree(ruleNames));
    	}

    	ECMAScriptParser parser = new Builder.Parser(expression).build();
    	parser.setBuildParseTree(true);
   
        LambdaCompiler compiler = new LambdaCompiler(Arrays.asList(parser.getRuleNames()));
        String compiled = compiler.process(parser.program(), false, false);
        System.out.println("Compiled:" + compiled);
        
        PrintWriter writer = new PrintWriter("src/output.txt", "UTF-8");
        writer.println(compiled);
        writer.close();
    }
}