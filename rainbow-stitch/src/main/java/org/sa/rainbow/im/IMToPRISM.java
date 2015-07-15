package org.sa.rainbow.im;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;
import org.sa.rainbow.im.parser.IMBaseListener;
import org.sa.rainbow.im.parser.IMParser;

public class IMToPRISM extends IMBaseListener {

    public HashMap<String,String> archTypes;
    public HashMap<String,ArrayList> archVars;
    public HashMap<String,String> archVarMin;
    public HashMap<String,String> archVarMax;
    public HashMap<String,String> archVarInit;
    public HashMap sets;
    public HashMap functions;

    public Tree<String> expTree;
    //public String curParent;
    public HashMap<String,ExpNode> nodes;

    public Stack<String> curParent;

    public boolean DEBUG=true;

    public IMToPRISM() {
        archTypes = new HashMap<String,String>();
        archVars = new HashMap<String,ArrayList>();
        archVarMin = new HashMap<String,String>();
        archVarMax = new HashMap<String,String>();
        archVarInit = new HashMap<String,String>();
        sets = new HashMap();
        functions = new HashMap();


        nodes = new HashMap<String,ExpNode>();
        expTree = new Tree<String>("root");
        //curParent = new String("root");
        curParent = new Stack<String>();
        curParent.push("root");

    }


    public String formatSetExpSelectorString(String setExpSelectorString, String archVarString){
        String selector = new String(setExpSelectorString);
        selector= selector.replace("true","1");
        selector= selector.replace("false","0");
        selector= selector.replace("==","=");
        selector= selector.replace(archVarString+".",archVarString+"_");
        return selector;
    }


    public ArrayList<String> getArchitectureElements(String type){
        ArrayList<String> res = new ArrayList<String>();

        Iterator it = archTypes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            //if (DEBUG) System.out.println("Element type ref: "+type +" Element type: " + archTypes.get(pairs.getKey().toString()));
            if (type.equals(archTypes.get(pairs.getKey().toString()))){
                res.add(pairs.getKey().toString());
            } 
        }
        return res;
    }

    public String getExpId(ParserRuleContext ctx) {
        return(ctx.getSourceInterval().toString());
    }

    public String getParentId(ParserRuleContext ctx) {
        return (expTree.getTree(this.getExpId(ctx)).getParent().getHead().toString());
    }

    public ExpNode getNode(String id){
        return (nodes.get(id));
    }

    public void addNode(String parentId, String id, String type, String text){
        expTree.addLeaf(parentId, id);
        nodes.put(id,new ExpNode(id,type,text));
    }

    public void printExpTree(Tree<String> tree, int inc){
        //System.out.println("**\""+tree.getHead().toString()+"\"&&");

        //System.out.println(this.getNode(tree.getHead().toString()).toString());
        try{
            System.out.println(nodes.get(tree.getHead().toString()).toString());
        } catch(Exception e){
            System.out.println("Exception when retrieving: " + tree.getHead().toString());
        }

        for (int i=0; i < inc; i++) {
            System.out.print("--");
        }

        Iterator <Tree<String>>it = tree.getSubTrees().iterator();

        while (it.hasNext()){

            printExpTree(it.next(),inc+1);
        }
    }

    @Override
    public void enterArch_declaration(IMParser.Arch_declarationContext ctx) {

        // Get string for architecture element name and type and add them to archTypes Map
        String compId = ctx.getChild(1).getText();
        String compType = ctx.getChild(0).getText();

        archTypes.put(compId, compType);

        if (DEBUG) {
            System.out.println("* Architecture Element "+compId+":"+archTypes.get(compId)+" declaration.");
        }

        // Build list of variables local to each of the component exploring child nodes of type Arch_var_declaration in the AST subtree
        ArrayList<String> localArchVars = new ArrayList<String>();		
        for (int i=3; i < ctx.getChildCount()-1; i++){
            localArchVars.add(ctx.getChild(i).getChild(1).getText());
            archVarMin.put(compId+"_"+localArchVars.get(i-3), ctx.getChild(i).getChild(3).getText());
            archVarMax.put(compId+"_"+localArchVars.get(i-3), ctx.getChild(i).getChild(5).getText());
            archVarInit.put(compId+"_"+localArchVars.get(i-3), ctx.getChild(i).getChild(8).getText());
        }

        archVars.put(compId, localArchVars);

        // Captured correctly?
        for (int i = 0; i < archVars.get(compId).size(); i++){
            if (DEBUG) {
                System.out.println("\t\t - Variable "+ archVars.get(compId).get(i) 
                        +" [" 
                        + archVarMin.get(compId+"_"+archVars.get(compId).get(i)) 
                        +".."
                        + archVarMax.get(compId+"_"+archVars.get(compId).get(i)) 
                        + "] : " 
                        + archVarInit.get(compId+"_"+archVars.get(compId).get(i)) 
                        + " declaration."
                        );
            }
        }                   

    }


    @Override
    public void enterInit(IMParser.InitContext ctx) {
        System.out.println("// PRISM CODE ---------------------------------------------------");
        System.out.println("\n dtmc \n");
    }


    @Override
    public void exitInit(IMParser.InitContext ctx) {
        System.out.println("// END OF PRISM CODE --------------------------------------------");
        System.out.println("\n");

        //System.out.println(expTree.toString());

        //System.out.println(nodes.toString());

        this.printExpTree(expTree,0);
    }

    @Override
    public void enterSimple_prexpr(IMParser.Simple_prexprContext ctx) {
        String primedvar=ctx.COMP_ID().getText();
        String uexpr = ctx.expr().getText();

        this.addNode(curParent.peek(), this.getExpId(ctx),"SIMPLE",ctx.getText());

        ExpNode thisNode = this.getNode(this.getExpId(ctx));

        thisNode.text=primedvar.replace(".","_")+"\'=" + uexpr.replace(".","_");	
        thisNode.subexp.add(thisNode.text);
        thisNode.p.add("(1)");			
    }	

    @Override
    public void enterForeach_prexpr(IMParser.Foreach_prexprContext ctx) {
        System.out.println("Entering Foreach");

        this.addNode(curParent.peek(), this.getExpId(ctx),"FOREACH","");
        curParent.push(this.getExpId(ctx));

    }

    @Override
    public void exitForeach_prexpr(IMParser.Foreach_prexprContext ctx) {

        String archVar=ctx.archvar.getText();

        ExpNode thisNode = this.getNode(this.getExpId(ctx));

        String selector = this.formatSetExpSelectorString(thisNode.setExpArchConstraintSelector, archVar);

        ArrayList <String> archElements = this.getArchitectureElements(thisNode.setExpArchTypeSelector);
        ArrayList <String> successors = new ArrayList(Arrays.asList(expTree.getSuccessors(this.getExpId(ctx)).toArray()));

        for (int i=0; i<archElements.size(); i++){
            for (int j=0; j< successors.size(); j++){
                ExpNode successorNode = this.getNode(successors.get(j));
                String successorProbability = successorNode.p.get(j);
                String baseString = successorNode.subexp.get(j);
                thisNode.subexp.add(baseString.replace( archVar+"_", archElements.get(i) + "_"));
                thisNode.p.add("("+ selector.replace(archVar+"_",archElements.get(i)+"_") +"?(1/n):0)"+"*"+successorProbability);
            }
        }


        System.out.println("Exiting Foreach");
        curParent.pop();
        //curParent=expTree.getTree(this.getExpId(ctx)).getParent().getHead().toString();


    }

    @Override
    public void enterForall_prexpr(IMParser.Forall_prexprContext ctx) {
        System.out.println("Entering Forall");
        this.addNode(curParent.peek(), this.getExpId(ctx),"FORALL","");
        curParent.push(this.getExpId(ctx));
    }

    @Override
    public void exitForall_prexpr(IMParser.Forall_prexprContext ctx) {

        String archVar=ctx.archvar.getText();

        ExpNode thisNode = this.getNode(this.getExpId(ctx));

        ArrayList <String> archElements = this.getArchitectureElements(thisNode.setExpArchTypeSelector);
        ArrayList <String> successors = new ArrayList(Arrays.asList(expTree.getSuccessors(this.getExpId(ctx)).toArray()));

        String thisNodeSubexp="";
        String thisNodeProbability="";
        boolean firstElement=true;
        for (int i=0; i<archElements.size(); i++){
            for (int j=0; j< successors.size(); j++){
                ExpNode successorNode = this.getNode(successors.get(j));
                thisNodeProbability = successorNode.p.get(j);
                String baseString = successorNode.subexp.get(j);
                if (!firstElement) {
                    thisNodeSubexp+=" & ";
                }
                thisNodeSubexp+="("+baseString.replace( archVar+"_", archElements.get(i) + "_")+")";
                firstElement=false;
            }
        }
        thisNode.subexp.add(thisNodeSubexp);
        thisNode.p.add(thisNodeProbability);

        System.out.println("Exiting Forall");
        curParent.pop();
        //curParent=expTree.getTree(this.getExpId(ctx)).getParent().getHead().toString();	
    }


    @Override
    public void enterOr_prexpr(IMParser.Or_prexprContext ctx) {
        System.out.println("Entering OR");
        this.addNode(curParent.peek(), this.getExpId(ctx),"OR","");
        curParent.push(this.getExpId(ctx));

    }

    @Override
    public void exitOr_prexpr(IMParser.Or_prexprContext ctx) {		

        ExpNode thisNode = this.getNode(this.getExpId(ctx));

        ArrayList <String> successors = new ArrayList(Arrays.asList(expTree.getSuccessors(this.getExpId(ctx)).toArray()));

        for (int i=0; i< successors.size(); i++){
            ExpNode successorNode=this.getNode(successors.get(i));	

            // Just propagate subexpression and probability up one level from the single successor
            thisNode.subexp.add(successorNode.subexp.get(0));
            thisNode.p.add(successorNode.p.get(0));        		

        }

        System.out.println("Exiting OR");
        curParent.pop();
        //curParent=expTree.getTree(this.getExpId(ctx)).getParent().getHead().toString();		

    }


    @Override
    public void enterOr_prexpr_simple(IMParser.Or_prexpr_simpleContext ctx) {
        this.addNode(curParent.peek(), this.getExpId(ctx),"OR_S","");
        curParent.push(this.getExpId(ctx));
        System.out.println("Entering OR_SIMPLE");

    }

    @Override
    public void exitOr_prexpr_simple(IMParser.Or_prexpr_simpleContext ctx) {

        String probString=ctx.prob.getText();
        ExpNode thisNode = this.getNode(this.getExpId(ctx));

        ArrayList <String> successors = new ArrayList(Arrays.asList(expTree.getSuccessors(this.getExpId(ctx)).toArray()));

        for (int i=0; i< successors.size(); i++){
            ExpNode successorNode=this.getNode(successors.get(i));

            if (successorNode.type.equals("FOREACH")||successorNode.type.equals("OR")){
                System.out.println("FOREACH-AND-OR: >>" + successorNode.type);
                // To implement
            } else {
                // Just propagate subexpression and probability up one level from the single successor
                thisNode.subexp.add(successorNode.subexp.get(0));
                thisNode.p.add("("+probString+")*"+successorNode.p.get(0));        		
            }

        }


        System.out.println("Exiting OR_SIMPLE");
        curParent.pop();
        //curParent=expTree.getTree(this.getExpId(ctx)).getParent().getHead().toString();	
    }


    @Override
    public void enterAnd_prexpr(IMParser.And_prexprContext ctx) {
        this.addNode(curParent.peek(), this.getExpId(ctx),"AND","");
        curParent.push(this.getExpId(ctx));

    }

    @Override
    public void exitAnd_prexpr(IMParser.And_prexprContext ctx) {

        ExpNode thisNode = this.getNode(this.getExpId(ctx));

        ArrayList <String> successors = new ArrayList(Arrays.asList(expTree.getSuccessors(this.getExpId(ctx)).toArray()));

        boolean firstElement=true;
        String thisNodeSubexp="";
        String thisNodeProbability="";
        for (int i=0; i< successors.size(); i++){
            ExpNode successorNode = this.getNode(successors.get(i));

            for (int j=0; j<successorNode.subexp.size();j++) {
                thisNodeProbability = successorNode.p.get(j);
                String baseString = successorNode.subexp.get(j);
                if (!firstElement) {
                    thisNodeSubexp+=" & ";
                }
                thisNodeSubexp+=baseString; 
                firstElement=false;
            }
            // Insert probability factor code here
        }

        thisNode.subexp.add(thisNodeSubexp);
        thisNode.p.add(thisNodeProbability);

        System.out.println("Exiting AND");
        curParent.pop();
        //curParent=expTree.getTree(this.getExpId(ctx)).getParent().getHead().toString();		

    }

    @Override
    public void enterSet_expression_basic(IMParser.Set_expression_basicContext ctx) {
        String archType = ctx.archtype.getText();

        ExpNode parentNode=this.getNode(curParent.peek());
        if (parentNode.type.equals("FOREACH") || parentNode.type.equals("FORALL")) {
            parentNode.setExpArchTypeSelector=archType;
        }	
    }

    @Override
    public void enterSet_expression_complex(IMParser.Set_expression_complexContext ctx) {
        String archType = ctx.archtype.getText();
        String constraint = ctx.constraint.getText();

        ExpNode parentNode=this.getNode(curParent.peek());
        if (parentNode.type.equals("FOREACH") || parentNode.type.equals("FORALL")) {
            parentNode.setExpArchTypeSelector=archType;
            parentNode.setExpArchConstraintSelector=constraint;
        }	
    }

}