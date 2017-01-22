package com.github.kmizu.nub;
import com.github.kmizu.nub.tool.Streams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.IOException;
public class Main {
    public static void main(String[] args) throws IOException{
        if(args.length == 0) {
            CommonTokenStream stream = Streams.streamFrom("def fibo(n){ if(n<3){ 1; }else{ fibo(n-1)+fibo(n-2); } } let i = 1; while(i<=10){println(fibo(i));i=i+1;}");
            AstNode.ExpressionList program = new NubParser(stream).program().e;
            //Evaluator evaluator = new Evaluator();
            //evaluator.evaluate(program);
            JavaScriptGenerator evaluator = new JavaScriptGenerator();
            System.out.println(evaluator.evaluate(program));
        } else {
            String fileName = args[0];
            CommonTokenStream stream = Streams.streamFrom(new File(fileName));
            NubParser parser = new NubParser(stream);
            AstNode.ExpressionList program = parser.program().e;
            //Evaluator evaluator = new Evaluator();
            //evaluator.evaluate(program)
            JavaScriptGenerator evaluator = new JavaScriptGenerator();
            System.out.println(evaluator.evaluate(program));
        }
    }
}
