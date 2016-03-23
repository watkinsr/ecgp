package ecgp2;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class Genome<T> {
	static String[] funcs = { "-", "+", "*", "/", "Math.sin", "Math.cos", "Math.toDegrees", "Math.asin", 
			"Math.acos",  "Math.abs", "Math.toRadians"};
	// static String[] terms = {"1", "2"};
	static String[] terms = { 
			"getX()", "getY()", "getVelocity()", "getHeading()", "0.01", "Math.random()*2 - 1", 
			"Math.random()+0.01", "Math.PI", "Math.floor((Math.random()*10))",
			"getHeadingRadians()",  "getRadarHeadingRadians()", "e.getVelocity()", "e.getHeadingRadians()", 
			"e.getEnergy()", "e.getDistance()", "e.getBearingRadians()", "getWidth()", "getHeight()", 
			"getGunHeadingRadians()", "getDistanceRemaining()", "getGunTurnRemainingRadians()", 
			"getRadarTurnRemainingRadians()"
	};
	public static final int MAX_DEPTH = 7;
	public static final int MIN_DEPTH = 2;
	private static final double PROB_TERM = 0.2;
	public StringBuilder _pheno = new StringBuilder(" ");
	private Stack<String> stack = new Stack<String>();
	public TreeNode<String> tree;
	private int terms_seen = 0, arity = 0, TYPE_0 = 0, TYPE_1 = 1, TYPE_2 = 2, TYPE_3 = 3, TYPE_4 = 4, TYPE_5 = 5,
				TYPE_6 = 6, prevLevel = 0;
	public int TREE_DEPTH = 0;
	private String oneArityStr;
	private boolean recursedArityParent = false;
	private boolean nodeUsed = false;
	private ArrayList<Tuple> unaryDepths = new ArrayList<Tuple>();
	private TreeNode<String> temp = null;
	public int nodes = 0;
	public static boolean logConversion = false;
	public static boolean treeDisplay = false;
	
	public Genome() {
		this.tree = new TreeNode<String>(func());
		this.tree = Grow(this.tree);
		if (treeDisplay) {
			this.tree.print();
		}
	}

	public StringBuilder GetPhenomeFromGenome() {
		return GenomeToPhenome();
	}

	
	
	public TreeNode<String> Grow(TreeNode<String> node) {
		Random rand = new Random();
		int arity = getArityOfFunction(node);
		if (node.getLevel() < MAX_DEPTH) { // Terminal growth
			if (rand.nextDouble() < PROB_TERM) {
				TreeNode<String> childNode = node.addChild(term());
				childNode.setNodeType("T");
				if (node.children.size() < arity) {
					Grow(node);
				}
			} else { // Function growth
				TreeNode<String> childNode = node.addChild(func());
				childNode.setNodeType("F");
				if (node.children.size() < arity) {
					Grow(node);
				}
				Grow(childNode);
			}
		} else { // Reached max depth
			if (node.getNodeType() == "F") {
				for (int i = 0; i < arity; i++) {
					node.addChild(term());
				}
			}
		}
		calculateTreeDepth();
		return node;
	}

	private void addToPheno(String arg, int type) {
		_pheno.append(arg);
		log("Appended: " + arg + " [" + String.valueOf(type) + "]", true);
	}

	private void recursiveUnary() {
		if (stack.size() > 0) {
			if (getArityOfString(stack.peek()) == 1) {
				oneArityStr = stack.pop() + "(" + oneArityStr + ")";
				recursedArityParent = true;
				recursiveUnary();
			}
		}
	}

	private void HandleMoveBranch(TreeNode<String>node) {
		boolean unableToPop = false;
		if (arity == 1 && stack.size() > 0) {
			String operand = stack.pop(), operator = stack.pop();
			oneArityStr = operator + "(" + operand + ")";
			recursiveUnary();
			recursedArityParent = true;
			addToPheno(oneArityStr, TYPE_0);
			arity = 0;
		} else if (arity == 2 && stack.size() > 0) {
			String lOp = stack.pop(), rOp = stack.pop(), op = stack.pop();
			if (stack.size() > 0 && getArityOfString(stack.peek()) == 1) {
				String unary = stack.pop();
				oneArityStr = unary + "(" + lOp + " " + op + " " + rOp + ")";
				recursiveUnary();
				recursedArityParent = true;
				addToPheno(oneArityStr, TYPE_0);
			} else {
				addToPheno(lOp + " " + op + " " + rOp + " ", TYPE_0);
			}
			arity = 0;
		} else {
			unableToPop = true;
		}
		terms_seen = 0;
		
		CheckToRemoveDepths(node.getLevel());
		
		if (stack.size() > 0 && getArityOfString(stack.peek()) != 1 && !unableToPop) {// pop off parent
			addToPheno(stack.pop() + " ", TYPE_1);
			if (recursedArityParent && stack.size() > 0 && !isOperator(stack.peek())) {
				addToPheno(stack.pop() + " ", TYPE_5); // Weird unary, need to pop 2nd child
			} else if (!isOperator(node.data) && stack.size() == 0) {
				addToPheno(node.data + " ", TYPE_5);
				nodeUsed = true;
			}
		}
		recursedArityParent = false;
		unableToPop = false;
	}
	
	private void termsOnStackWithNonTerm(TreeNode<String> node) {
		try {
			if (arity == 1 && stack.size() > 1) { // Unary
				String operand = stack.pop(), operator = stack.pop();
				if (!isOperator(operand) && !isOperator(operator)) {
					stack.push(operand);
					stack.push(operator);
					throw new Exception("Expected an operand");
				} else {
					if (getArityOfString(operator) == 1) {
						addToPheno(operator + "(" + operand + ") ", TYPE_2);
					} else { addToPheno(operand + "(" + operator + ") ", TYPE_2); }
					arity = 0;
					terms_seen = 0;
				}
			} else if (stack.size() > 1){ // Binary
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
					addToPheno(operand + " ", TYPE_2);
					CheckToRemoveDepths(node.getLevel());
					addToPheno(operator + " ", TYPE_2);
					arity = 0;
					terms_seen = 0;
				}
			} else {
				String operand = stack.pop();
				addToPheno(operand + " ", TYPE_2);
				CheckToRemoveDepths(node.getLevel());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			if (stack.size() == 1) {
				addToPheno(stack.pop(), TYPE_3);
			}
		}
		terms_seen = 0;
	}

	private void printPrefixTree() {
		log("", false);
		log("[", false);
		for (TreeNode<String> node : this.tree) {
			log(node.data + ", ", false);
		}
		log("]", false);
		log("", false);
	}
	
		
	private void AddToDepths() {
		for (int i = 0; i < unaryDepths.size(); i++) {
			unaryDepths.get(i).x = (Object) ((int) unaryDepths.get(i).x + 1);
		}
	}
	
	private boolean CheckToRemoveDepths(int level) { 
		boolean isRemoved = false;
		for (int i = 0; i < unaryDepths.size(); i++) {
			if ((int) unaryDepths.get(i).y >= level) {
				unaryDepths.remove(i);
				addToPheno(")", TYPE_6);
				i--;
				isRemoved = true;
			}
		}
		if (level == 1 && unaryDepths.size() > 0) {
			for (int i = 0; i < unaryDepths.size(); i++) {
				unaryDepths.remove(i);
				addToPheno(")", TYPE_6);
				i--;
				isRemoved = true;
			}
		}
		return isRemoved;
	}
	
	private boolean EvalUnaryCheck(TreeNode<String> node) {
		if(getArityOfString(node.data) == 1) {
			addToPheno(node.data + "(", TYPE_6); 
			unaryDepths.add(new Tuple(0, node.getLevel()));
			nodeUsed = true;
			// evaluation(node);
			return true;
		} else {
			return false;
		}
	}
	
	private void log(String msg, boolean isNewLine) {
		if (Genome.logConversion && isNewLine) {
			System.out.println(msg);
		} else if (Genome.logConversion) {
			System.out.print(msg);
		}
	}
	
	private void PreTreeToString(TreeNode<String> child) {
		log("TOADD[" + child.data + "] ", false);
		if (prevLevel < child.getLevel() ) { AddToDepths(); } // we traversed down a bit further
		
		if (prevLevel > child.getLevel() && terms_seen == arity && stack.size() >= (1 + arity)) {
			HandleMoveBranch(child);
		} else if (terms_seen > 0 && isOperator(child.data)) { // terms on stack w/ non-term
			termsOnStackWithNonTerm(child);
		} else if (prevLevel > child.getLevel() && stack.size() > 0 
				&& !isOperator(child.data)) {
			addToPheno(stack.pop(), TYPE_6);
			if (CheckToRemoveDepths(child.getLevel()) && isOperator(stack.peek())) {
				addToPheno(stack.pop(), TYPE_6);
			} else if (isOperator(stack.peek())){
				addToPheno(stack.pop(), TYPE_6);
			}
			terms_seen--;
			// nodeUsed = true;
		}
		
		if (EvalUnaryCheck(child)) {
			// System.out.println("Unary eval'd true");
		}
		
		if (!nodeUsed) {updateStack(child);}
		
		if (!isOperator(child.data) && !nodeUsed) {
			terms_seen++;
		}
		
	}
	
	private void evaluation() {
		for (TreeNode<String> child: this.tree) {
			nodes++;
			nodeUsed = false;
			temp = child;
			printStack(stack);
			PreTreeToString(child);
		}
		if (stack.size() == 1 && isOperator(stack.peek())) {
			updateStack(temp);
			printStack(stack);
		}
		if (terms_seen == 2) {
			HandleMoveBranch(temp);
		}
		// nodeUsed = true;
		// PreTreeToString(temp);
	}
	
	private void ClearDepthBrackets() {
		for (int i = 0; i < unaryDepths.size(); i++) {
			unaryDepths.remove(i);
			i--;
			addToPheno(")", TYPE_6);
		}
	}
	
	private StringBuilder GenomeToPhenome() {
		nodes = 0;
		stack.clear();
		terms_seen = 0;
		arity = 0;
		prevLevel = 0;
		nodeUsed = false;
		if (logConversion) {
			this.tree.print();
		}
		log("BEGIN", false);
		printPrefixTree();
		unaryDepths.clear();
		
		// EvalUnaryCheck(this.tree);
		// updateStack(this.tree);
		evaluation();
		
	/*	
	for (TreeNode<String> node : this.tree) {
			printStack(stack);
			// If we go back up a branch after recursion
			if (prevLevel > node.getLevel() && terms_seen == arity && stack.size() >= (1 + arity)) {
				HandleMoveBranch(node);
			} else if (terms_seen > 0 && isOperator(node.data)) { // terms on stack w/ non-term
				termsOnStackWithNonTerm(node);
			} if (!nodeUsed) {updateStack(node);}
		}

		if (_pheno.length() == 1) {
			HandleMoveBranch(null);
		}
	*/
		
		handleEndOfStack(stack);
		ClearDepthBrackets();
		this.calculateTreeDepth();
		log("Pheno... DONE - depth[" + String.valueOf(this.TREE_DEPTH) + "] " + _pheno, true);
		if (logConversion) {
			this.tree.print();
		}
		if (stack.size() > 0) {
			System.out.println("The stack should be empty, ERROR!");
			System.exit(1);
		}
		return _pheno;
	}

	private void printStack(Stack<String>stack) {
		log(" STACK [", false);
		for (String str : stack) {
			log(str + ",", false);
		}
		log("]\n", false);
	}
	
	private void handleEndOfStack(Stack<String>stack) {
		// Try and handle all combinations of an end of stack...
		log("End of stack", true); printStack(stack);
		if (stack.size() > 0) {
			String rOp = stack.pop();
			try {
				if (stack.size() > 0 && isOperator(stack.peek())) { // OP AT END OF STACK
					String op = stack.pop();
					if (getArityOfString(op) == 1) { // Indicates Unary
						oneArityStr = op + "(" + rOp + ")"; recursiveUnary();
						addToPheno(oneArityStr, TYPE_4);
					} 
					else { addToPheno(op + " " + rOp, TYPE_4); }
				} else if (stack.size() > 0 && !isOperator(stack.peek())) { // NON OP AT END OF STACK
					if (stack.size() == 1) {
						addToPheno(rOp, TYPE_4);
					} else if (stack.size() == 2) {
						String lOp = stack.pop(), op = stack.pop();
						addToPheno(lOp + " " + op + " " + rOp + " ", TYPE_4);
					}  else if (stack.size() == 3) {
						String lOp = stack.pop(), op = stack.pop(), unaryOp = stack.pop();
						addToPheno(unaryOp+"(" + lOp + " " + op + " " + rOp+")", TYPE_6);
					} else if (stack.size() > 3) {
						// System.out.println("Error: Stack[" + String.valueOf(stack.size()) + "] rOp:" + rOp);
						// System.exit(1);
					}
				} else {
					addToPheno(rOp, TYPE_6);
				}
			} catch (Throwable error) {
				System.out.println(error.getMessage());
				System.exit(1);
			}
		} else {
			log("No items at end of stack", true);
		}

	}

	private void updateStack(TreeNode<String> node) {
		if (!isOperator(node.data)) {
			// terms_seen++;
		} else {
			arity = getArityOfFunction(node);
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
			GP.Log("Error: We're encountering too little depth");
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
		} else if (rand == 0)
			getRand();

		return "ERROR: Something went wrong with random number generator";
	}

	public int getArityOfFunction(TreeNode<String> node) {
		String s = node.data;
		return getArityOfString(s);
	}

	public static int getArityOfString(String s) {
		if (s == "Math.sin" || s == "Math.cos" || s == "Math.toDegrees" || s == "Math.asin" || 
			s == "Math.acos" || s == "Math.abs" || s == "Math.toRadians") {
			return 1;
		} else {
			return 2;
		}
	}

	public static boolean isOperator(String c) {
		return c == "+" || c == "-" || c == "*" || c == "/" || c == "Math.sin" || c == "Math.cos" 
				|| c == "Math.toDegrees" || c == "Math.asin" || c == "Math.acos" || c == "Math.abs" || 
				c == "Math.toRadians";
	}

	public static double eval(final String str) {
		class Parser {
			int pos = -1, c;

			void eatChar() {
				c = (++pos < str.length()) ? str.charAt(pos) : -1;
			}

			void eatSpace() {
				while (Character.isWhitespace(c))
					eatChar();
			}

			double parse() {
				eatChar();
				double v = parseExpression();
				if (c != -1)
					throw new RuntimeException("Unexpected: " + (char) c);
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
						if (c == '*')
							eatChar();
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
					if (c == ')')
						eatChar();
				} else { // numbers
					int startIndex = this.pos;
					while ((c >= '0' && c <= '9') || c == '.')
						eatChar();
					if (pos == startIndex)
						throw new RuntimeException("Unexpected: " + (char) c);
					v = Double.parseDouble(str.substring(startIndex, pos));
				}

				eatSpace();
				if (c == '^') { // exponentiation
					eatChar();
					v = Math.pow(v, parseFactor());
				}
				if (negate)
					v = -v; // unary minus is applied after exponentiation; e.g.
							// -3^2=-9
				return v;
			}
		}
		return new Parser().parse();
	}

}
