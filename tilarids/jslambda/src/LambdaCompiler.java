import java.util.*;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

public class LambdaCompiler {

    public static void main(String[] args) {
    	String expression = "function foo() { return function() {return 2 + 2; }; }";
     
        //parser generates abstract syntax tree
    	ECMAScriptParser parser = new Builder.Parser(expression).build();
    	parser.setBuildParseTree(true);
    	List<String> ruleNames = Arrays.asList(parser.getRuleNames());
        System.out.println(parser.program().toStringTree(ruleNames));    	
    }
}