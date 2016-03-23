package ecgp2.Testing;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Stack;

import org.junit.*;
import ecgp2.Tuple;

public class UnaryTreeTest {

	private StringBuilder t = new StringBuilder();
	private Stack<Tuple> s = new Stack<Tuple>();
	private Stack<String> r = new Stack<String>();
	private ArrayList<Integer> unaryDepths = new ArrayList<Integer>();
	
	@Test
	public void testComponent() {
		int prevLevel = 0;
		s.push(new Tuple("2", 2));
		s.push(new Tuple("2", 4));
		s.push(new Tuple("1", 4));
		s.push(new Tuple("-", 3));
		s.push(new Tuple("Math.sin", 2));
		s.push(new Tuple("*", 1));
		s.push(new Tuple("Math.sin", 0));

		while (!s.empty()) {
			Tuple o = s.pop();
			
			if ( (int) o.y < prevLevel) {
				processUnaryBranch();
			}
			r.push( (String) o.x );
			if ( ecgp2.Genome.getArityOfString((String) o.x) == 1) {
				t.append(o.x + "(");
				unaryDepths.add(0);
				r.pop();
			} else {
				int i = unaryDepths.size() - 1;
				unaryDepths.set(i, unaryDepths.get(i) + 1);
			}
			prevLevel = (int) o.y;
		}
		// Stack could be non-empty after reaching end of a branch
		handleEndOfStack();
		assertEquals("Math.sin(Math.sin(1-2)*2)", t.toString());
	}

	private void handleEndOfStack() {
		if (r.size() == 1 ) {
			t.append(r.pop());
		} else if (r.size() == 2) {
			String operand = r.pop(), operator = r.pop();
			String ket = unaryDepths.size() > 0 ? ")" : "";
			t.append(operator + operand + ket);
		}
	}
	
	private void processUnaryBranch() {
		System.out.println("Reached new level"); // => we know we've finished recursing the branch
		int i = unaryDepths.size() - 1;
		int depth = unaryDepths.get(i);
		System.out.println("Depth of unary branch: " + String.valueOf(depth));
		if (depth == 1) {
			t.append(r.pop() + ")");
		} else if (depth > 1) {
			System.out.println("Expression inside unary branch found");
		}
		unaryDepths.remove(unaryDepths.size() - 1);
	}
	
}
