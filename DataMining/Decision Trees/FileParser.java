package DataMining.Assignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mysql.jdbc.BufferRow;

public class FileParser {
	
	
	public FileParser(){}

	/*
	public FileParser(String fileName){
		
		file = new File(fileName); 
			
	}*/

	
	public Boolean extractColumns(List<String> temp,String fileName) throws IOException {
		File file = new File(fileName);
		if(file.exists()) {
			InputStream in = new FileInputStream(file);
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(in));			
			String columnHeader = bufferReader.readLine();		
			temp.addAll(Arrays.asList(columnHeader.split("\t")));
			bufferReader.close();
			return true;
		}
		return false;
	}
	
	public Boolean FillInfoForAlgo(Id3Algo algo,String filename){
		Boolean status = false;
		File file = new File(filename);
		if(file.exists()) {
			InputStream in;
			try {
				in = new FileInputStream(file);
				BufferedReader bufferReader = new BufferedReader(new InputStreamReader(in));
				algo.dataFile = file.getParent() + "/" + bufferReader.readLine();
				algo.label = bufferReader.readLine().trim();
				String columnHeader = bufferReader.readLine().trim();		
				algo.attributes.addAll(Arrays.asList(columnHeader.split("\\s+")));
				bufferReader.close();
				status = extractColumns(algo.allAttributes,algo.dataFile);				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return status;
	}
	
	public List<String> getFileContent(String fileName){
		List<String> content = null;
		File file = new File(fileName);
		if(file.exists()) {
			InputStream in;
			BufferedReader bufferReader;
			try {
				content = new ArrayList<String>();
				in = new FileInputStream(file);
				bufferReader = new BufferedReader(new InputStreamReader(in));
				String line;
				while((line = bufferReader.readLine()) != null){
					content.add(line.trim());
				}
				bufferReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return content;		
	}
	
}
