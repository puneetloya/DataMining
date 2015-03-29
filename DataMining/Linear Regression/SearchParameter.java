package DataMining.Assignment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SearchParameter {
	
	double intercept,slope;
	
	public double[][] getInverse(double[][] normalMatrix){
		
		double[][] inverseMatrix = new double[2][2];
		
		inverseMatrix[0][0] = normalMatrix[1][1];
		inverseMatrix[1][1] = normalMatrix[0][0];
		
		inverseMatrix[0][1] = -1 * normalMatrix[0][1];
		inverseMatrix[1][0] = -1 * normalMatrix[1][0];
		
		double factor = (inverseMatrix[0][0]*inverseMatrix[1][1]) - (inverseMatrix[0][1] * inverseMatrix[1][0]);  
		
		for(int i = 0; i < 2; i++){
			for(int j = 0; j < 2; j++){
				inverseMatrix[i][j] = (1/factor) * inverseMatrix[i][j]; 
			}
		}
		return inverseMatrix;
	}
	
	public double[][] getMatrix(double regressor1,double response1,double regressor2,double response2){
		
		double[][] matrix1 = new double[2][2];
		matrix1[0][0] = 1;
		matrix1[1][0] = 1;		 
		matrix1[0][1] = regressor1;
		matrix1[1][1] = regressor2;
		matrix1 = getInverse(matrix1);
		double[][] matrix2 = new double[2][1];
		matrix2[0][0] = response1;
		matrix2[1][0] = response2;
		double[][] result = new double[2][1];
		result[0][0] = 0;
		result[1][0] = 0;
		for(int i = 0; i < 2; i++) {
			for(int k = 0; k < 1; k++) {
				for( int j = 0; j < 2; j++ ) {
					result[i][k] += matrix1[i][j] * matrix2[j][k];
				}
			}
		}
		return result;
	}
	
	public static void main(String[] args){
		
		try {
			SearchParameter searchParam = new SearchParameter();
			Path filePath;
			if(args.length >= 1) {
				filePath = Paths.get(args[0]);
				String fileData = new String(Files.readAllBytes(filePath));
				String[] fileLines = fileData.split("\n");
				String[] variables = fileLines[0].split(","); 
				double regressor = Double.parseDouble(variables[0]);
				double response = Math.log10(Double.parseDouble(variables[1]));
				calculateParameters(searchParam, fileLines, variables,regressor, response);
			}
			else {
				System.out.println("PLEASE PROVIDE FILENAME AS AN ARGUMENT");
			}	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private static void calculateParameters(SearchParameter searchParam,
			String[] fileLines, String[] variables, double regressor,
			double response) {
		double newRegressor = 0,newResponse = 0;
		int count = -1;
		double[][] resultMatrix = new double[2][1];
		double beginRangeForParam1 = Double.MAX_VALUE;
		double endRangeForParam1 = Double.MIN_VALUE;
		double beginRangeForParam2 = Double.MAX_VALUE;
		double endRangeForParam2 = Double.MIN_VALUE;
		for(String line : fileLines) {
			count++;
			if(count == 0) continue;
			variables = fileLines[count].split(",");
			newRegressor =  Double.parseDouble(variables[0]);
			newResponse = Math.log10(Double.parseDouble(variables[1]));
			
			resultMatrix = searchParam.getMatrix(regressor,response,newRegressor,newResponse);
			
			beginRangeForParam1 = Math.min(beginRangeForParam1,resultMatrix[0][0]);
			endRangeForParam1 = Math.max(endRangeForParam1, resultMatrix[0][0]);
			
			beginRangeForParam2 = Math.min(beginRangeForParam2,resultMatrix[1][0]);
			endRangeForParam2 = Math.max(endRangeForParam2, resultMatrix[1][0]);
			
			regressor = (regressor*count + newRegressor)/(count+1);
			response = (response*count + newResponse)/(count+1);	
		}				
		System.out.println(" Result Coefficients : " + resultMatrix[0][0] + " , " + resultMatrix[1][0]);
		System.out.println(" Range for b0 is " +  + beginRangeForParam1 + " - " + endRangeForParam1 );
		System.out.println(" Range for b1 is " +  + beginRangeForParam2 + " - " + endRangeForParam2 );
	}

}
