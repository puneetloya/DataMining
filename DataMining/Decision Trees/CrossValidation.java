package DataMining.Assignment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class CrossValidation {

	List<Integer> inputTupleIds;
	MysqlOps mysql;
	
	public CrossValidation() throws SQLException{
		
		mysql = new MysqlOps();
		mysql.connect();
	}
	
	public Map<Integer,List<Integer>> GetCrossValidationInput(int range,int numberOfGroups) {
		Map<Integer,List<Integer>> testSet = new HashMap<Integer, List<Integer>>();
		List<Integer> listOfInts;
		int temp;
		for( int i = 1; i <= range; i++){
			temp = i%numberOfGroups+1;
			if(testSet.containsKey(temp)) {
				listOfInts = testSet.get(temp);
				listOfInts.add(i);
				testSet.put(temp,listOfInts);
			}
			else{
				listOfInts = new ArrayList<Integer>();
				listOfInts.add(i);
				testSet.put(temp,listOfInts);
			}
		}		
		return testSet;
	}
	
	public static void main(String[] args) {
		
		try {
			CrossValidation crVal = new CrossValidation();
			String filePath = args[0];
			Id3Algo algo;
			algo = new Id3Algo();
			FileParser fileParser = new FileParser();
			List<String> attrCopy = new ArrayList<String>();
			if(fileParser.FillInfoForAlgo(algo,filePath)) {
				for(String attr: algo.attributes)
					attrCopy.add(attr);
				algo.LoadData();
				int range = crVal.mysql.getCount("1=1","table1");
				int noOfSets = 3;
				
				Map<Integer,List<Integer>> crMap =  crVal.GetCrossValidationInput(range,noOfSets );
				List<Entry<Integer,List<Integer>>> entryList = new ArrayList<Map.Entry<Integer,List<Integer>>>(crMap.entrySet());
				List<Integer> trainingSet = new ArrayList<Integer>();
				List<Integer> testSet = new ArrayList<Integer>();
				String temp; TreeNode root;
				for(int i = 0; i < noOfSets; i++) {
					trainingSet.clear(); testSet.clear();
					trainingSet.addAll(entryList.get(i).getValue());
					trainingSet.addAll(entryList.get((i+1)%3).getValue());
					
					temp = trainingSet.stream().map(num -> String.valueOf(num)).collect(Collectors.joining(", "));
					crVal.mysql.createView(temp,"trainingView");
					algo.setEntityName("trainingView");
					root = Id3Algo.initialSetupForId3(algo);
					root.printPreOrder(root);
					
					testSet.addAll(entryList.get((i+2)%3).getValue());
					temp = testSet.stream().map(num -> String.valueOf(num)).collect(Collectors.joining(", "));
					crVal.mysql.createView(temp,"testView");

					List<List<String>> tempTuples = new ArrayList<List<String>>();
					for(String attr:attrCopy){
						tempTuples.add(crVal.mysql.GetData("testView",attr));
					}
					
					String temp1;
					List<String> tuples = new ArrayList<String>();
					for(int  j = 0; j < range/noOfSets; j++){
						temp1 = "";
						for(List<String> list: tempTuples){
							temp1 += " " + list.get(j);
						}
						tuples.add(temp1.trim());
					}
					algo.getTestResults(root, attrCopy, tuples);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
		
}
