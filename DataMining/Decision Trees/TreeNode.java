package DataMining.Assignment;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TreeNode {
	
	String memberQualifier; // query that defines the node contents
	String partitionBasedOn;
	int count;	
	TreeNode parent;
	List<TreeNode> children;
	Map<String,Integer> labelMap; // count of each label value
	
	
	public TreeNode(String memberQualifier,TreeNode parent) {
		this.memberQualifier = memberQualifier;
		this.parent = parent;
	}
	
	public TreeNode findNode(TreeNode node,String query) {
		
		if(node == null) return null;
		if(query.equals(node.memberQualifier))
			return node;
		TreeNode temp;
		if(query.indexOf(node.memberQualifier) > -1) {
			for(TreeNode treeNode: node.children) {
				 temp = findNode(treeNode, query);
				 if(temp != null) return temp;
			}
		}
		return null;
	}
	
	// print the pre-order form of tree
	public void printPreOrder(TreeNode node) {
		
		if(node == null) return;
		String printStat = "Node Query : " + node.memberQualifier.trim() + " Count : " + node.count;
		System.out.println(printStat.replace("1=1 AND","").replace("1=1",""));

		if(node.labelMap != null){
			for(Entry<String,Integer> pair: node.labelMap.entrySet()){
				System.out.println(" label : " + pair.getKey() + " value : " + pair.getValue());
			}
		}
		if(node.children != null) {
			for(TreeNode treeNode: node.children){
				printPreOrder(treeNode);
			}
		}
	}
	
	
	//get the nodes which obey the query
	public void findNodes(TreeNode node,String query,List<TreeNode> nodes) {
		
		if(node == null) return;
		if(node.memberQualifier.indexOf(query) > -1) {
			
			if(node.children == null || node.children.size() == 0) {
				if(node.count > 0)
					nodes.add(node);
			}		
		}
		
		if(node.children != null) {
			for(TreeNode treeNode: node.children) {
				findNodes(treeNode, query,nodes);
			}
		}	
	}
}
