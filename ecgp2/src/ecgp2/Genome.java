package ecgp2;

import java.util.Random;
import java.util.Stack;

public class Genome<T> {
    static String[] funcs = {"-", "+", "*", "/"};
    static String[] terms = {"getX()", "getY()", "getVelocity()", "getGunHeading()", "getGunHeat()", "getHeading()"};
    public static final int MAX_DEPTH = 4;
    public static final int MIN_DEPTH = 2;
    private static final double PROB_TERM = 0.2;
    public StringBuilder _pheno = new StringBuilder(" ");
    private Stack<String> stack = new Stack<String>();
    public TreeNode<String> tree;
    private int terms_seen = 0, arity = 2,  prevArity = 0, TYPE_0 = 0, TYPE_1 = 1, TYPE_2 = 2, TYPE_3 = 3, TYPE_4 = 4, TYPE_5 = 5, prevLevel = 0;
    public int TREE_DEPTH = 0;
    private String oneArityStr;
    private boolean recursedArityParent = false;

    public Genome() {
	this.tree = new TreeNode<String>(func());
	this.tree = Grow(this.tree);	 
	this.tree.print();	
    }

    public StringBuilder GetPhenomeFromGenome () {
	return GenomeToPhenome();
    }

    public TreeNode <String> Grow(TreeNode<String> node) {
	int arity = getArityOfFunction(node);
	if (node.getLevel() < MAX_DEPTH) {			
	    if (Math.random() < PROB_TERM){
		TreeNode<String> childNode = node.addChild(term()); 
		childNode.setNodeType("T");
		if (node.children.size() < arity) {
		    Grow(node);
		}
	    }

	    // If function
	    else {
		TreeNode<String> childNode = node.addChild(func());
		childNode.setNodeType("F");
		if (node.children.size() < arity) {
		    Grow(node);
		}	

		Grow(childNode);

	    }
	} else { // Reached max depth
	    if(node.getNodeType() == "F") {
		for (int i = 0; i < arity; i++) { node.addChild(term()); }
	    }
	}
	calculateTreeDepth();
	return node;
    }



    private void addToPheno(String arg, int type) {
		_pheno.append(arg);
		System.out.println("Appended: " + arg + " [" + String.valueOf(type) + "]");
    }

    private void recursiveOneArity() {
    	if (getArityOfString(stack.peek()) == 1) {
    		oneArityStr = stack.pop() + "(" + oneArityStr + ")";
    		recursedArityParent = true;
    		recursiveOneArity();
	    } 
    }
    
    private StringBuilder GenomeToPhenome() {
		stack.clear();
		Stack<String> _stack;
		StringBuilder pheno;
		terms_seen = 0;
		prevArity = 0;
		prevLevel =0;
	
		for (TreeNode<String> node : this.tree) {
		    pheno = _pheno;
		    _stack = stack;
	
		    // If we go back up a branch after recursion
		    if (prevLevel > node.getLevel() && terms_seen == prevArity && stack.size() >= (1 + prevArity)) {
				if (prevArity == 1) {
				    String operand = stack.pop(), operator = stack.pop();
				    oneArityStr = "";
				    oneArityStr = operator + "(" + operand + ")";
				    recursiveOneArity();
				    addToPheno(oneArityStr, TYPE_0);
				    prevArity = 0;
				} else if (prevArity == 2) {
				    String lOp = stack.pop(), rOp = stack.pop(), op = stack.pop();
				    if (getArityOfString(stack.peek()) == 1) {
				    	addToPheno(stack.pop() + "(" + lOp + " " + op + " " + rOp + ") ", TYPE_0);
				    } else {
				    	addToPheno(lOp + " " + op + " " + rOp + " ", TYPE_0);
				    }
				    prevArity = 0;
				}
				terms_seen = 0;
				if (stack.size() > 0 && getArityOfString(stack.peek()) != 1) {// pop off parent
				    addToPheno(stack.pop() + " ", TYPE_1);
				    if (recursedArityParent && stack.size() > 0) {
				    	addToPheno(stack.pop() + " ", TYPE_5); // Weird one arity stuff, need to pop second child 
				    	recursedArityParent = false; 		   // after recursing out of one arity
				    }
				} else{
					System.out.println("Huh?");
				}
		    } else if (terms_seen > 0 && isOperator(node.data)) { // If terminals on stack and we've now reached a non-terminal 
		    	try {
		    		if (terms_seen == 3) {
		    			System.out.println("this is fucked");
		    		}
				    if (prevArity == 1) {
						String operand = stack.pop(), operator = stack.pop();
						if (!isOperator(operand) && !isOperator(operator)) {
							// addToPheno(operand + " ", TYPE_5);
							stack.push(operand);
				    		stack.push(operator);
				    		throw new Exception("Expected an operator");
				    	} else {
				    		addToPheno(operator + "(" + operand + ") ", TYPE_2);
							prevArity = 0;
							terms_seen = 0;
				    	}
				    } else {
				    	String operator = null, operand = null;
				    	if (terms_seen == 2) { 
				    		operator = stack.pop();
				    		operand = stack.pop();
				    	} else {
				    		operand = stack.pop();
				    		operator = stack.pop();
				    	}
				    	if (!isOperator(operand) && !isOperator(operator)) {
				    		// addToPheno(operand + " ", TYPE_5);
				    		stack.push(operand);
				    		stack.push(operator);
				    		throw new Exception("Expected an operator");
				    	} else {
				    		addToPheno(operand + " " + operator + " ", TYPE_2);
				    		prevArity = 0;
				    		terms_seen = 0;
				    	}
				    }
		    	} catch( Exception e ) {
		    		System.out.println(e.getMessage());
		    		if (stack.size() == 1) {
		    			addToPheno(stack.pop(), TYPE_3);
		    		}
		    	}
		    }  	
		    updateStack(node);
		}		
	
		handleEndOfStack();
		this.calculateTreeDepth();
		System.out.println("Pheno... DONE - depth[" + String.valueOf(this.TREE_DEPTH) +"] " + _pheno);
		return _pheno;
    }

    private void handleEndOfStack() {
    	Stack<String> _stack = stack;
		StringBuilder pheno = _pheno;
		// Try and handle all combinations of an end of stack...
		if (stack.size() > 0) {
		    String rOp = stack.pop();
		    try { 
			if (stack.size() > 0 && isOperator(stack.peek())) {
			    String op = stack.pop();
			    if (getArityOfString(op) == 1) {
			    	addToPheno(op + "(" + rOp + ")", TYPE_4);
			    }
			    else {
			    	addToPheno(op + " " + rOp, TYPE_4);
			    }
			} else if (stack.size() > 0 && !isOperator(stack.peek())){
			    String lOp = stack.pop(), op = stack.pop();
			    addToPheno(lOp + " " + op + " " + rOp + " ", TYPE_4);
			} else {
			    addToPheno(rOp, TYPE_4);
			}
		    } catch (Throwable error) {
		    	System.out.println(error.getMessage());
		    }
		}

    }

    private void updateStack(TreeNode<String>node) {
	if (!isOperator(node.data)) {
	    terms_seen++;
	} else {
	    prevArity = getArityOfFunction(node);
	}
	stack.push(node.data); // Push to stack
	prevLevel = node.getLevel();
    }

    public void calculateTreeDepth() {
	int LOCAL_TREE_DEPTH = 0;
	for (TreeNode<String> node : this.tree) {
	    LOCAL_TREE_DEPTH = node.getLevel() > LOCAL_TREE_DEPTH ? node.getLevel() : LOCAL_TREE_DEPTH;
	}
	this.TREE_DEPTH = LOCAL_TREE_DEPTH;
	// GP.log("Tree depth calculated: " + String.valueOf(TREE_DEPTH));
	if (TREE_DEPTH < 1) {
	    GP.log("Error: We're encountering too little depth");
	}
    }

    public String term() {
	Random random = new Random();		
	return terms[random.nextInt(terms.length)];
    }

    public String func() {
	Random random = new Random();
	return funcs[random.nextInt(funcs.length)];
    }

    public String getRand() {
	double rand = Math.random() * 2 - 1;
	if (rand != 0) {
	    return String.valueOf(rand);
	}
	else if (rand == 0)
	    getRand();

	return "ERROR: Something went wrong with random number generator";
    }

    public int getArityOfFunction(TreeNode<String> node) {
	String s = node.data;
	return getArityOfString(s);
    }

    public int getArityOfString(String s) {
	if (s == "Math.sin") {
	    return 1;
	} else {
	    return 2;
	}
    }

    public static boolean isOperator(String c)
    {
	return c == "+" || c == "-" || c == "*" || c == "/" || c == "Math.sin" || c == "Math.cos";
    }	

    public static double eval(final String str) {
	class Parser {
	    int pos = -1, c;

	    void eatChar() {
		c = (++pos < str.length()) ? str.charAt(pos) : -1;
	    }

	    void eatSpace() {
		while (Character.isWhitespace(c)) eatChar();
	    }

	    double parse() {
		eatChar();
		double v = parseExpression();
		if (c != -1) throw new RuntimeException("Unexpected: " + (char)c);
		return v;
	    }

	    // Grammar:
	    // expression = term | expression `+` term | expression `-` term
	    // term = factor | term `*` factor | term `/` factor | term brackets
	    // factor = brackets | number | factor `^` factor
	    // brackets = `(` expression `)`

	    double parseExpression() {
		double v = parseTerm();
		for (;;) {
		    eatSpace();
		    if (c == '+') { // addition
			eatChar();
			v += parseTerm();
		    } else if (c == '-') { // subtraction
			eatChar();
			v -= parseTerm();
		    } else {
			return v;
		    }
		}
	    }

	    double parseTerm() {
		double v = parseFactor();
		for (;;) {
		    eatSpace();
		    if (c == '/') { // division
			eatChar();
			v /= parseFactor();
		    } else if (c == '*' || c == '(') { // multiplication
			if (c == '*') eatChar();
			v *= parseFactor();
		    } else {
			return v;
		    }
		}
	    }

	    double parseFactor() {
		double v;
		boolean negate = false;
		eatSpace();
		if (c == '+' || c == '-') { // unary plus & minus
		    negate = c == '-';
		    eatChar();
		    eatSpace();
		}
		if (c == '(') { // brackets
		    eatChar();
		    v = parseExpression();
		    if (c == ')') eatChar();
		} else { // numbers
		    int startIndex = this.pos;
		    while ((c >= '0' && c <= '9') || c == '.') eatChar();
		    if (pos == startIndex) throw new RuntimeException("Unexpected: " + (char)c);
		    v = Double.parseDouble(str.substring(startIndex, pos));
		}

		eatSpace();
		if (c == '^') { // exponentiation
		    eatChar();
		    v = Math.pow(v, parseFactor());
		}
		if (negate) v = -v; // unary minus is applied after exponentiation; e.g. -3^2=-9
		return v;
	    }
	}
	return new Parser().parse();
    }

}
