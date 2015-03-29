package dataMining.AssignmentTwo;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



public class AgglomerativeForAgeProfession {

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
				newMax = calculateCosineDistances(ent1,ent2);
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
		prepareCluster(clusters,xyPairs);
	}

	void prepareCluster(List<Cluster> clusters,List<XYPairs> xyPairs) {
		
		List<Cluster> removalList = new ArrayList<Cluster>();
		for(XYPairs pair: xyPairs){
					
			Cluster cluster1 = clusters.get(pair.index1);
			Cluster cluster2 = clusters.get(pair.index2);
			int ageMedian = (cluster1.clusterRepresentaive.Age + cluster2.clusterRepresentaive.Age)/2;
			int occMedian = (cluster1.clusterRepresentaive.Occupation + cluster2.clusterRepresentaive.Occupation)/2;
			Entity en = new Entity(ageMedian,occMedian);
			Cluster newCluster = new Cluster();
			newCluster.clusterRepresentaive = en;	
			newCluster.left = cluster1;
			newCluster.right = cluster2;					
			clusters.add(newCluster);
			removalList.add(cluster1);removalList.add(cluster2);
		}				
		clusters.removeAll(removalList);
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


	double calculateCosineDistances(Entity it,Entity it2) {//,List<XYPairs> xyPairs) {

			double distanceMat;										
			distanceMat = ((it.Age * it2.Age) + (it.Occupation * it2.Occupation))/(Math.sqrt(Math.pow(it.Age,2)+ Math.pow(it.Occupation,2)) * Math.sqrt(Math.pow(it2.Age,2)+Math.pow(it2.Occupation,2)));
			return distanceMat;
	}


	void analyseCluster(Cluster cluster){
		
		List<Entity> entities = new ArrayList<Entity>();
		getAllLeavesInACluster(cluster,entities);
		Map<String,Map<Double,Integer>> dict = new HashMap<String,Map<Double,Integer>>();
		int count; 
		Map<Double,Integer> ratingsDict = new HashMap<Double,Integer>();
		
		for(Entity en : entities){
			
			for(String genre:en.genres) {
				
				if(dict.containsKey(genre)) {
					ratingsDict = dict.get(genre);
					if(ratingsDict != null && ratingsDict.containsKey(en.rating)){
						count = ratingsDict.get(en.rating);
						ratingsDict.put(en.rating,count++);		
					}
					else{
						ratingsDict.put(en.rating,1);
					}	
				}
				else {
					ratingsDict = new HashMap<Double,Integer>();
					ratingsDict.put(en.rating,1);
					dict.put(genre, ratingsDict);
				}
			}
		}
		int sum;
		Double maxRating = Double.MIN_VALUE;
		for(Entry<String, Map<Double,Integer>> entry: dict.entrySet()) {
			System.out.println(" Genre: " + entry.getKey());
			sum = 0;count = 0;
			for(Entry<Double, Integer> innerEntry: entry.getValue().entrySet()) {
				sum += innerEntry.getKey() * innerEntry.getValue();
				count += innerEntry.getValue();
				if(maxRating < innerEntry.getKey())
					maxRating = innerEntry.getKey();
			}
			System.out.println(" No of People Rated the genre : " + sum);
			System.out.println(" The average rating : " + (sum/count));
			System.out.println(" The Max rating : " + maxRating);
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
		AgglomerativeForAgeProfession agg = new AgglomerativeForAgeProfession();
		
		List<Cluster> items = agg.getData();
		
		for(int i = 0; i < 13 ; i++){
			agg.calculateCosineDistancesOfClusters(items);
		}	
		
		System.out.println("No of clusters :" + items.size());
		for( Cluster cl : items )
			agg.analyseCluster(cl);		
		//System.out.println(agg.getAllClusterEntities(items));	
	}


}
	