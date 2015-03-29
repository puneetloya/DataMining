package DataMining.Assignment;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Id3Algo {

	MysqlOps my;
	List<String> attributes;
	List<String> allAttributes;
	String dataFile,label,entityName;
	TreeNode root;
	
	public Id3Algo() throws SQLException {
		my = new MysqlOps();
		my.connect();	
		attributes = new ArrayList<String>();
		allAttributes = new ArrayList<String>();
		entityName = "table1";
		root = null;
	}

	// load the given data into the Database
	public void LoadData(){
		my.LoadDataIntoDB(dataFile,allAttributes);
	}
	
	public void setEntityName(String name){
		entityName = name;
	}
	
	public List<String> getUniqueLabels() {
		return my.GetUniqueValues(label,entityName);
	}
	
	
	public String getHighestGainAttribute(TreeNode node) {
		
		List<String> variety = new ArrayList<String>();
		List<String> varietyLabels = my.GetUniqueValues(label,entityName);
		int totalCount = my.getCount(node.memberQualifier,entityName);
		String initialQuery = ""; int sum1,sum2;
		double score = 0,totalScore = 0,gain = Double.MAX_VALUE;
		String decidedAttr = "";
		for(String attr : attributes) {
			variety = my.GetUniqueValues(attr,entityName);
			for(String attrValue : variety) {
				initialQuery = formDynamicQuery(node.memberQualifier,attr,attrValue);
				sum1 = my.getCount(initialQuery,entityName);
				sum2 = 0; 
				for(String vlab : varietyLabels ) {
					sum2 = my.getCount(formDynamicQuery(initialQuery,label,vlab),entityName);
					if(sum2 == 0)
						score = 0;
	
				else {
						// calculate entropy
						score = sum2/(double)sum1;
						score = -1 * score * Math.log(score)/Math.log(2);
						totalScore += score;
					}
				}
				if(totalCount == 0)
					totalScore = totalScore * (sum1/(double)totalCount);  
				else
					totalScore = 0;
			}
			if(totalScore < gain) {
				gain = totalScore;
				decidedAttr = attr; 
			}
			totalScore = 0;
		}
		return decidedAttr;
	}
	
	public String formDynamicQuery(String query,String label,String value){
		
		if(query == null || query.length() == 0)
			query = "1=1";
		query += String.format(" AND %s = '%s'", label,value);		
		return query;
	}
	
	//update node with label values it is holding
	public void getLabelCountOnNode(TreeNode node,String query) {
		node.labelMap = new HashMap<String, Integer>();
		List<String> variety = my.GetUniqueValues(label,entityName);
		for(String attrValue: variety) 
			node.labelMap.put(attrValue,my.getCount(formDynamicQuery(query,label,attrValue),entityName));
	}
	
	//Build the decision tree
	public void getClassificationTree(TreeNode node) {
		
		if(attributes.size() > 0 && node != null) {
			String query = node.memberQualifier;
			node.partitionBasedOn = getHighestGainAttribute(node);
			List<String> varietyLabels = my.GetUniqueValues(node.partitionBasedOn,entityName);
			node.children = new ArrayList<TreeNode>();
			String tempQuery;TreeNode tempNode = null;
			int index;
			for(String attrValue: varietyLabels) {
				tempQuery = formDynamicQuery(query, node.partitionBasedOn, attrValue);
				tempNode = new TreeNode(tempQuery,node);
				getLabelCountOnNode(tempNode,tempQuery);
				index = attributes.indexOf(node.partitionBasedOn);
				if(index > -1)
					attributes.remove(index);
				tempNode.count = my.getCount(tempNode.memberQualifier,entityName);
				getClassificationTree(tempNode);
				node.children.add(tempNode);
				attributes.add(node.partitionBasedOn);
			}
		}
	}
	
	// calculate conditional probability for attribute
	public double getConditionalProbability(TreeNode node ,String query,String label) {
		
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		node.findNodes(node, query, nodes);
		int count = 0; double condProb = 0;
		if(nodes.size() > 0) {
			for(TreeNode item: nodes){
				if(item.labelMap.containsKey(label))
					count += item.labelMap.get(label);
			}
		}
		if(count == 0) 
			condProb = node.labelMap.get(label)/(double)node.count;
		else
			condProb = count/(double)node.labelMap.get(label);
		return condProb;
	}
	
	//if missing values then apply weighted conditional probability
	public double getConditionalProbabilityForMissingValues(TreeNode node ,String query,String label) {
		
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		node.findNodes(node, query, nodes);
		int count = 0; double condProb = 0;
		if(nodes.size() > 0) {
			for(TreeNode item: nodes){
				if(item.labelMap.containsKey(label))
					count += item.labelMap.get(label);
			}
		}
		
		if(count == 0) 
			condProb = node.labelMap.get(label)/(double)node.count;
		else
			condProb = count/(double)node.labelMap.get(label);
		return condProb*count; // weighted conditional probability for missing values
	}
	
	// Naive bayes for predicting the class
	public double applyNaiveBayes(TreeNode node,List<String> attrList,List<String> tuples,String labelValue){
		int i = 0;
		double prob = 1,probTemp;
		String temp; String attrValue;
		List<String> possibleAttrValue = new ArrayList<String>();
		for(String attr : attrList) {
			attrValue = tuples.get(i);
			if(attrValue.indexOf('*') > -1) {
				possibleAttrValue = my.GetUniqueValues(attr, entityName);
				probTemp = 0; 
				for(String possVal: possibleAttrValue) {
					temp = String.format("%s = '%s'",attr,possVal);
					probTemp += getConditionalProbabilityForMissingValues(node,temp,labelValue);
				}
				prob = prob * probTemp;
			}
			else {
				temp = String.format("%s = '%s'",attr,attrValue);
				prob = prob * getConditionalProbability(node,temp,labelValue);
			}
			i++;
		}
		prob = prob * node.labelMap.get(labelValue)/(double)node.count;
		return prob;	
	}
	
	//As probabilities may not sum up to 1, scale them ( because of Naive Bayes)
	public void normalizeFinalProbability(List<Double> probList) {
		List<String> labelVals = my.GetUniqueValues(label, entityName);
		double sum = probList.stream().reduce(0d, (a,b) -> a+b);
		int i = 0;
		DecimalFormat df = new DecimalFormat("#.000");
		for(double val:probList) {
			System.out.print(labelVals.get(i));
			System.out.print("\t");
			System.out.println(df.format((double)val/sum));
			i++;
		}
	}
	
	
	public static void main(String[] args) throws IOException, SQLException{
			//get the tree file
			String filePath = args[0];
			Id3Algo algo = new Id3Algo();
			FileParser fileParser = new FileParser();
			if(fileParser.FillInfoForAlgo(algo,filePath)) {
				
				List<String> attrCopy = new ArrayList<String>();
				for(String attr: algo.attributes)
					attrCopy.add(attr);
				
				TreeNode root = initialSetupForId3(algo);
				//root.printPreOrder(root);
				String testFile = args[1];
				List<String> tuples = fileParser.getFileContent(testFile);
				algo.getTestResults(root,attrCopy, tuples);
			}
	}

	// Build the decision tree
	public static TreeNode initialSetupForId3(Id3Algo algo) {
		TreeNode root = new TreeNode("1=1",null); // need to assign this guy the total count
		algo.getLabelCountOnNode(root, root.memberQualifier);
		root.count = algo.my.getCount(root.memberQualifier, algo.entityName);
		algo.getClassificationTree(root);
		return root;
	}

	// for each test case get the test results
	public void getTestResults(TreeNode node,List<String> attrCopy,List<String> tuples) {
		List<String> attrValues;
		List<Double> probList;
		for(String tuple: tuples) {
			attrValues = Arrays.asList(tuple.split("\\s+",-1));
			probList = new ArrayList<Double>();
			for(String labelValue: getUniqueLabels()) {
				probList.add(applyNaiveBayes(node, attrCopy, attrValues, labelValue));		
			}
			System.out.println(tuple);
			normalizeFinalProbability(probList);
		}
	}
}
