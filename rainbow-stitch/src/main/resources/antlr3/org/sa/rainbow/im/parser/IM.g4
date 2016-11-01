// Grammar for StitchV2 impact models.
// V0.1. Javier Camara (7-31-2013)


grammar IM;

@header {
    package org.sa.rainbow.im.parser;
    import java.util.*;
    import java.lang.*;
} 

@members {
    HashMap archTypes = new HashMap();
    HashMap<String,ArrayList> archVars = new HashMap<String,ArrayList>();
    HashMap archVarMin = new HashMap();
    HashMap archVarMax = new HashMap();
    HashMap archVarInit = new HashMap();
    
    HashMap sets = new HashMap();
    HashMap functions = new HashMap();
    
    
}

init  : architecture declaration* body 
     {
      
          
      
      // Generates function definitions
            
      
      String function_defs=new String("");
      String q_function_defs= new String("");
      String var_defs= new String("");
    
      Iterator it = archTypes.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry)it.next();
        
        Iterator <String> it2 = archVars.get(pairs.getKey()).iterator();
        while (it2.hasNext()){
        
            String varName=it2.next();
            String fullVarName=pairs.getKey()+"_"+varName;
        
            Iterator itf = functions.entrySet().iterator();
            while (itf.hasNext()){
                Map.Entry function_pairs = (Map.Entry)itf.next();
                function_defs=function_defs+"\n formula "+ fullVarName + "_" + function_pairs.getKey() + " = " 
                + function_pairs.getValue().toString().replace("x", fullVarName) + ";" ;
                
                String functionName=fullVarName + "_" + function_pairs.getKey();
                q_function_defs=q_function_defs+"\n formula "+ "q_" + functionName + " = " 
                      + functionName + " >= " + archVarMin.get(fullVarName) + " ? (" + functionName + " <= "
                      + archVarMax.get(fullVarName) + " ? floor(" + functionName +") : " + archVarMax.get(fullVarName)
                      + ") : " + archVarMin.get(fullVarName);                
                
            }
            
            var_defs=var_defs + "\n\t"+ fullVarName +" : ["+ 
                               archVarMin.get(fullVarName)+".."+archVarMax.get(fullVarName)+
                              "] init "+ archVarInit.get(fullVarName) +";";
        }
        it.remove(); // avoids a ConcurrentModificationException
        
       }
      
      System.out.println ("\n // Update function declarations \n"+function_defs);
      System.out.println ("\n // Quantization function declarations \n"+q_function_defs);
      
       // Generates arch module declarations with arch element variables, ranges, and initialization
      System.out.println("\n module arch \n");
      System.out.println (var_defs);
      
      // Closing declaration of arch module
      System.out.println ("\n endmodule");
      
      }
   ;

// Basic arch declaration
architecture : 'architecture' ID '{' arch_declaration+ '}' ;

arch_declaration : archtype=COMP_ID compid=ID '{'arch_var_declaration+ '}' ;

arch_var_declaration : 'int' varname+=ID '[' varmin+=INTEGER ',' varmax+=INTEGER ']' ':' varinit+=INTEGER ';' ;

// Further declarations (Arch element sets, vars, functions...)
declaration : 'define' 'set' s=ID '=' '{' 'select' svar=ID ':' ingroup=COMP_ID 'in' COMP_ID ( '|' inexpr=expr )? '}' ';' 
              {sets.put($s.text, new ArrayList<String>(Arrays.asList($ingroup.text, $inexpr.text)));
               System.out.println("* Set "+$s.text+" declared as " +sets.get($s.text));}
            | 'define' ('int' | 'float') ID '=' expr ';'
            | 'define' 'function' id=ID '(' ID? ')' '=' expression=expr ';'
              {
               functions.put($id.text,$expression.text);
               }
            ;

body  locals[HashMap commands=new HashMap()
            ] : 'impactmodel' imid=ID '{' (bucket)+ '}' 
         {
          
          Iterator it = $commands.entrySet().iterator();
          while (it.hasNext()){
            Map.Entry command_pairs = (Map.Entry)it.next();
            System.out.println ("[" + $imid.text + "]("+ command_pairs.getKey()  +") -> " ); 
          }
        }
       ;         // match keyword impact model followed by set of impact model "buckets" (i.e., pairs expression, probabilistic expression)

bucket : '(' guard=expr ')' ( '{' expression=prexpr '}' | expression=prexpr )
         {
          $body::commands.put($guard.text, $expression.text);
          }
       ; 


// Probabilistic expressions
prexpr : simple_prexpr
       | foreach_prexpr 
       | forall_prexpr
       | or_prexpr
       | and_prexpr
       ;

simple_prexpr : primedvar=COMP_ID '\'' '=' uexpr=expr ;

foreach_prexpr : 'foreach' archvar=ID ':' set_expression '|' pexp=prexpr ;

forall_prexpr : 'forall' archvar=ID ':' set_expression '|' pexp=prexpr ;

and_prexpr    :'{' prexpr ('&' prexpr )* '}' ;

or_prexpr     : '{'  or_prexpr_simple  ('++' or_prexpr_simple )* '}' ;

or_prexpr_simple : '[' prob=probability ']' pexp=prexpr ;

probability   : FLOAT | INTEGER | ID ;
                
// Other

set_expression : set_expression_id | set_expression_basic | set_expression_complex ;

set_expression_id : archvar=ID ;

set_expression_basic : archtype=COMP_ID 'in' COMP_ID ;

set_expression_complex : '{' archtype=COMP_ID 'in' COMP_ID ( '|' constraint=expr )? '}' ;



//comp_id : ID '.' ID ; // Identifier for property in architectural element E (e.g., "E.propname") 



//Tokens

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

COMP_ID     :   ID '.' ID ;
ID          :   LETTER (LETTER|DIGIT|'_')*;
INTEGER     :   DIGIT+ ;
FLOAT       :   DIGIT+ ('.' DIGIT+)? ;
LETTER      :   [a-zA-Z\u0080-\u00FF_] ;
DIGIT       :   [0-9] ;
BOOLEAN     :   'true' | 'false' ;

// Expressions

expr :   ID
       | FLOAT
       | INTEGER
       | BOOLEAN
       | COMP_ID
       | expr OP_ARITH expr
       | OP_LOGIC_UN expr
       | expr OP_REL expr       
       | '(' expr ')'
       | 'log' '(' expr ',' expr ')'
       | ID '(' expr ')'
       ;

OP_ARITH    :   '*' | '-' | '+' | '/' ;
OP_REL      :   '>' | '<' | '>=' | '<=' | '==' | '!=' ;
OP_LOGIC_UN :   '!' ;  
OP_LOGIC    :   '&&' | '||' ;
ATOM        :   ID | FLOAT | BOOLEAN ;
