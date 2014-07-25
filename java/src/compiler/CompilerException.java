package compiler;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Created by san on 7/26/14.
 */
public class CompilerException extends RuntimeException {
    public ASTNode node;

    public CompilerException(String message, ASTNode node) {
        super(message);
        this.node = node;
    }
}
