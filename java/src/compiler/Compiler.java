package compiler;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

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

        return visitString( source );
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

    public TypeDeclaration visit( InputStream stream, String encoding ) throws IOException {
        if( stream == null )
            throw new IllegalArgumentException("stream is null");
        if( encoding == null )
            throw new IllegalArgumentException("encoding is null");
        if( encoding.trim().length() == 0 )
            throw new IllegalArgumentException("encoding is empty");

        String source = readInputStreamToString( stream, encoding );

        return visitString( source );
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

    public TypeDeclaration visitString( String source ) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);

        @SuppressWarnings( "unchecked" )
        Map<String,String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        parser.setCompilerOptions(options);

        parser.setResolveBindings(false);
        parser.setStatementsRecovery(false);
        parser.setBindingsRecovery(false);
        parser.setSource(source.toCharArray());
        parser.setIgnoreMethodBodies(false);

        CompilationUnit ast = (CompilationUnit) parser.createAST(null);

        CompilationUnit root = (CompilationUnit)ast.getRoot();
        TypeDeclaration mainClass = (TypeDeclaration)root.types().get(0);
        return mainClass;
    }

    public static void main(String[] args) throws IOException {
        new Compiler().run();

    }

    private void run() throws IOException {
        TypeDeclaration typeDeclaration = parseFile(new File("src/app/VM.java"));
    }
}

