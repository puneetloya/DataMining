package dataMining.AssignmentTwo;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Agglomerative {

	class XYPairs {
		
		int index1;
		int index2;
		
		public XYPairs(int x,int y) {
			index1 = x;
			index2 = y;
		}
	}
	
	void calculateCosineDistancesOfClusters(List<Cluster> clusters) {
		
		List<XYPairs> xyPairs = new ArrayList<XYPairs>();
		double max = Double.MIN_VALUE,newMax;	
		int i=0,j=0,index = -1;
		Map<Integer,Boolean> visited = new HashMap<Integer,Boolean>(); 
		Entity ent1,ent2;
		for(Cluster cl1: clusters.subList(0,clusters.size() - 2)) {
			j=i+1;		
			ent1 = cl1.clusterRepresentaive;
			max = Double.MIN_VALUE;
			for(Cluster cl2 : clusters.subList(i+1,clusters.size() - 1)) {				
				ent2 = cl2.clusterRepresentaive;				
				//newMax = calculateCosineDistances(ent1,ent2);
				newMax = calculateDistancesForGenreRatings(ent1,ent2);
				if(newMax > max && (!visited.containsKey(j) && !visited.containsKey(i))) {
					max = newMax;
					index = j;
				}
				j++;
			}
			if( !visited.containsKey(index) && !visited.containsKey(i) && index != -1) {
				visited.put(i,true);
				visited.put(index,true);
				xyPairs.add(new XYPairs(i,index));
			}		
			i++;
		}
		prepareClusterForGenreRating(clusters,xyPairs);
	}
	
	
	
	
	void prepareClusterForGenreRating(List<Cluster> clusters,List<XYPairs> xyPairs) {
		
		List<Cluster> removalList = new ArrayList<Cluster>();
		for(XYPairs pair: xyPairs){
					
			Cluster cluster1 = clusters.get(pair.index1);
			Cluster cluster2 = clusters.get(pair.index2);			
			List<String> clusteredGenreList = new ArrayList<String>(cluster1.clusterRepresentaive.genres); 
			clusteredGenreList.retainAll(cluster2.clusterRepresentaive.genres);
			double avgRating = (cluster1.clusterRepresentaive.rating + cluster2.clusterRepresentaive.rating)/2; 
			Entity en = new Entity(avgRating,clusteredGenreList);
			Cluster newCluster = new Cluster();
			newCluster.clusterRepresentaive = en;	
			newCluster.left = cluster1;
			newCluster.right = cluster2;					
			clusters.add(newCluster);
			removalList.add(cluster1);removalList.add(cluster2);
		}				
		clusters.removeAll(removalList);
	}

	
	int getAllClusterEntities(List<Cluster> clusters) {
		
		List<Entity> temp = new ArrayList<Entity>();
		int count = 0;
		for(Cluster cl: clusters) {
			temp = new ArrayList<Entity>();
			getAllLeavesInACluster(cl,temp);
			count += temp.size(); 
		}
		return count;
	}
	
	void getAllLeavesInACluster(Cluster cl,List<Entity> entities) {
		
		if(cl == null)
			return;
		if(cl.left == null && cl.right == null){
			entities.add(cl.clusterRepresentaive);
		}
		getAllLeavesInACluster(cl.left,entities);
		getAllLeavesInACluster(cl.right,entities);
	}
	
	
	double calculateCosineDistances(List<Entity> items1,List<Entity> items2) {//,List<XYPairs> xyPairs) {

		double max = Double.MIN_VALUE;
		int i = 0,j; double distanceMat;
		for(Entity it: items1) {
			j = 0;
			for(Entity it2 : items2) {								
				distanceMat = ((it.Age * it2.Age) + (it.Occupation * it2.Occupation))/(Math.sqrt(Math.pow(it.Age,2)+ Math.pow(it.Occupation,2)) * Math.sqrt(Math.pow(it2.Age,2)+Math.pow(it2.Occupation,2)));
				if(distanceMat > max) {
					max = distanceMat;
				}
				j++;
			}
			i++;
		}
		return max;
	} 
	
	
	double calculateCosineDistances(Entity it,Entity it2) {

		int i = 0; double distanceMat;										
		distanceMat = ((it.Age * it2.Age) + (it.Occupation * it2.Occupation))/(Math.sqrt(Math.pow(it.Age,2)+ Math.pow(it.Occupation,2)) * Math.sqrt(Math.pow(it2.Age,2)+Math.pow(it2.Occupation,2)));
		return distanceMat;
	}
	
	
	double calculateDistancesForGenreRatings(Entity it,Entity it2) {		
		int similarGenrecount = 0;
		List<String> common = new ArrayList<String>(it.genres);
		//Collections.copy(common,it.genres);
		common.retainAll(it2.genres);		
		similarGenrecount = common.size();
		double ratingDiff = Math.abs(it.rating - it2.rating);		
		if(similarGenrecount == 0) return 0;
			
		double score =  similarGenrecount/(it.genres.size() + it2.genres.size() - similarGenrecount);
		if(ratingDiff == 0){
			score += 1.3;		//kept as a high score because similar rating for same genre can be a top priority
		}
		else
			score += (1/ratingDiff);  
		return score;
	}
	
	
	void analyseClusterForGenreRating(Cluster cluster){
		
		List<Entity> entities = new ArrayList<Entity>();
		getAllLeavesInACluster(cluster,entities);
		
		System.out.println(" Cluster Representation :");
		
		Entity ent = cluster.clusterRepresentaive;
		
		for(String genre: ent.genres){
			System.out.print(genre+ ",");
		}
		System.out.println();
		System.out.println("The average rating :" + ent.rating);
		
		
		
		Map<String,Map<Integer,Integer>> dict = new HashMap<String,Map<Integer,Integer>>();
		Map<Integer,Integer> occDict = new HashMap<Integer,Integer>();
		Map<Integer,Integer> ageDict = new HashMap<Integer,Integer>(); 
		int maleCount = 0,femaleCount = 0;
		int occCount = 0,ageCount = 0;
		for(Entity en : entities) {
			
			 if(en.sex == 'M') 
				 maleCount++; 
			 else
				 femaleCount++;
			
			 if(occDict.containsKey(en.Occupation)){
				 occCount = occDict.get(en.Occupation);
				 occDict.put(en.Occupation,++occCount);
			 }
			 else
				 occDict.put(en.Occupation,1);
			 
			 if(ageDict.containsKey(en.Age)) {
				 ageCount = ageDict.get(en.Age);
				 ageDict.put(en.Age,++ageCount);
			 }
			 else
				 ageDict.put(en.Age,1);
		}
		System.out.println(" Total Males in the cluster : " + maleCount);
		System.out.println(" Total Females in the cluster : " + femaleCount);
		for(Entry<Integer, Integer> entry: ageDict.entrySet()){		
			System.out.println(" For age range " +  entry.getKey());
			System.out.println(" People in the age range " +  entry.getValue());
		}	
	}
	
	
	List<Cluster> getData() throws SQLException {
		
		MysqlConnector mysql = new MysqlConnector();		
		mysql.connect();
		List<Cluster> items = mysql.getData();
		mysql.disconnect();
		return items;
	}
	
	public static void main(String[] args) throws SQLException{
		Agglomerative agg = new Agglomerative();
		List<Cluster> items = agg.getData();
		
		for(int i = 0; i < 13 ; i++){
			agg.calculateCosineDistancesOfClusters(items);
		}		
		System.out.println("No of clusters :" + items.size());
		for( Cluster cl : items )
			agg.analyseClusterForGenreRating(cl);
		
		System.out.println(agg.getAllClusterEntities(items));	
	}
}
