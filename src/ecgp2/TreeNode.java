package ecgp2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.rits.cloning.Cloner;

public class TreeNode<T> implements Iterable<TreeNode<T>> {

	public T data;
	public TreeNode<T> parent;
	public List<TreeNode<T>> children;
	public String nodeType;
	
	public boolean isRoot() {
		return parent == null;
	}

	public boolean isLeaf() {
		return children.size() == 0;
	}

	public List<TreeNode<T>> elementsIndex;

	public TreeNode<T> getNode(int index) {
		return this.elementsIndex.get(index);
	}
	
	public TreeNode(T data) {
		this.data = data;
		this.children = new LinkedList<TreeNode<T>>();
		this.elementsIndex = new LinkedList<TreeNode<T>>();
		this.elementsIndex.add(this);
	}
	
	public TreeNode(TreeNode<T> node) {
		this.data = node.data;
		this.children = node.children;
		this.elementsIndex = node.elementsIndex;
	}

	public TreeNode<T> addChild(T child) {
		TreeNode<T> childNode = new TreeNode<T>(child);
		childNode.parent = this;
		this.children.add(childNode);
		this.registerChildForSearch(childNode);
		return childNode;
	}

	public void setNodeType(String type) {
		this.nodeType = type;
	}
	
	public String getNodeType() {
		return this.nodeType;
	}
	
	public int getLevel() {
		if (this.isRoot())
			return 0;
		else
			return parent.getLevel() + 1;
	}
	
	public int getRandomBranch(TreeNode<String>node, Random random) {
		return random.nextInt(node.children.size());
	}
	
	public void replaceWith(TreeNode<T> node) {
		Cloner cloner = new Cloner();
		
		this.data = node.data;
		
		// if (node.isLeaf() && node.getLevel() == 0)
			//this.parent = null;
		
		if (!node.isLeaf() && node.children.size() == 0) {
			System.out.println("ERROR: Function with no children about to used as replacement inside replaceWith");
		}
		
		this.children.clear();
		
		this.elementsIndex.clear();
		this.elementsIndex = cloner.deepClone(node.elementsIndex);
		this.elementsIndex.remove(0);
		this.elementsIndex.add(0, this);
		
		if (node.children.size() > 0) {	
			this.children = cloner.deepClone(node.children);
			this.children.get(0).parent = this;
			if (node.children.size() == 2) {
				this.children.get(1).parent = this;
			}
			this.elementsIndex.get(0).children.get(0).parent = this;
			if (node.children.size() == 2) {
				this.elementsIndex.get(0).children.get(1).parent = this;
			}
		}
	}
	
	public void recordTraversalStats(int target, List<Integer> selections) {
		GP.Log("Target: " + String.valueOf(target)); 
		GP.Log("Failed to locate a good node in our traversal...");
		System.out.print("Failed to find node at target with traversal: "); 
		for (int sel : selections) { System.out.print(sel); }
		System.out.println("");
	}
	
	public int getSubTreeTest(int target, Random random) {
		List<TreeNode<T>> candidates = new ArrayList<TreeNode<T>>();
		
		for (TreeNode<T> el : this) {
			if (el.getLevel() == target) {
				candidates.add(el);
			}
		}
		if (candidates.size() != 0) {
			TreeNode<T> subTree = candidates.get(random.nextInt(candidates.size()));
			for (int i = 0; i < this.elementsIndex.size(); i++ ){
				if (this.elementsIndex.get(i) == subTree) {
					return i;
				}
			}
		}
		GP.Log("Couldn't find candidates...");
		target--;
		
		return getSubTreeTest(target, random);
	}
	
	public TreeNode<T> getRandomTerminal() {
		Random random = new Random();
		List<TreeNode<T>> candidates = new ArrayList<TreeNode<T>>();
		
		for (TreeNode<T> el : this) {
			if (el.children.size() == 0) {
				candidates.add(el);
			}
		}
		return candidates.get(random.nextInt(candidates.size()));
	}
	
	public List<Integer> getSubTree(int target, Random random) {
		Cloner cloner = new Cloner();
		TreeNode<T> randNode = cloner.deepClone(this);
		List<Integer> selections = new ArrayList<Integer>();
		int localTarget = target;
		
		while (localTarget > 0 && selections.size() != target) { 
			if (randNode.children.size() == 0) { // If we failed to locate a function or terminal at the target level we wanted..
				// Reset various variables
				// if (target >= 2)
					// target--;
				
				recordTraversalStats(target, selections);
				
				randNode = cloner.deepClone(this);
				
				 // Reverse first step to help traversal
				int startStep= selections.get(0) == 0 ? 1 : 0;
				selections.clear();
				selections.add(startStep);
				randNode = cloner.deepClone(randNode.children.get(selections.get(0)));
				localTarget = target;
				
			} 
			else {
				localTarget--;  
				selections.add(random.nextInt(randNode.children.size()));
				randNode = cloner.deepClone(randNode.children.get(selections.get(selections.size() - 1)));
			}
		}
		if (selections.size() == 0) {
			System.out.println("ERROR: Returning selections with size zero");
		}
		return selections;
	}
	
	public TreeNode<T> getSubTreeFromMemory(List<Integer> selections) {
		TreeNode<T> subTree = this;
		
		if (selections.size() <= 0) {
			System.out.println("ERROR: Selection size is less than or equal to zero");
		}

		try { 
			while (selections.size() > 0) {
				int selection = selections.get(0);
				selections.remove(0);
				subTree = subTree.children.get(selection);
			}
			
		} catch (Throwable e) {
			System.out.println("ERROR: failed to loop through selections inside getSubTreeFromMemory");
			System.out.println(e.getMessage());
		}
			GP.Log("Random subTree found: "); subTree.print();
			return subTree;
	}
	
	private void registerChildForSearch(TreeNode<T> node) {
		elementsIndex.add(node);
		if (parent != null)
			parent.registerChildForSearch(node);
	}
	
	public TreeNode<T> findTreeNode(Comparable<T> cmp) {
		for (TreeNode<T> element : this.elementsIndex) {
			T elData = element.data;
			if (cmp.compareTo(elData) == 0)
				return element;
		}

		return null;
	}

	
	@Override
	public String toString() {
		return data != null ? data.toString() : "[data null]";
	}

	@Override
	public Iterator<TreeNode<T>> iterator() {
		TreeNodeIter<T> iter = new TreeNodeIter<T>(this);
		return iter;
	}
	
	public void print() {
		if (Genome.treeDisplay)
			print("", true);
    }

    private void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + this.data);
        for (int i = 0; i < children.size() - 1; i++) {
            children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (children.size() > 0) {
            children.get(children.size() - 1).print(prefix + (isTail ?"    " : "│   "), true);
        }
    }

}
