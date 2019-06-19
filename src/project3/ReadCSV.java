package project3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;

public class ReadCSV {
	private String txtPath = "";
	private String csvPath = "";
	private boolean hasError = true;
	
	private String tableName = "";
	private ArrayList colName = new ArrayList<String>();
	private ArrayList csvColName = new ArrayList<String>();
	private ArrayList colType = new ArrayList<String>();
	private ArrayList pkName = new ArrayList<String>();
	private ArrayList notNull = new ArrayList<String>();
	private ArrayList<ArrayList<String>> csvData = new ArrayList<ArrayList<String>>();
	private int csvColNum = 0;
	private String makeTableQ = "";
	private ArrayList<String> insertDataQ = new ArrayList<String>();
	
	private String SCHEMA_NAME="";
	
	
	public ReadCSV(String schemaName) {
		SCHEMA_NAME = schemaName;
	}
	
	private void parsingTXT() {
		try {
			File file = new File(txtPath);
			FileReader fileReader = new FileReader(file);
			BufferedReader br = new BufferedReader(fileReader);
			
			String line = "";
			while((line=br.readLine())!=null) {
				String first = line.split(":")[0];
				String second = line.split(":")[1];
				
				if(first.startsWith("Name")) {
					second = second.replace(" ", "");
					tableName = second;
				}
				else if(first.startsWith("PK")) {
					second = second.replace(" ", "");
					int pkNum = second.split(",").length;
					for(int i =0; i<pkNum; i++) {
						pkName.add(second.split(",")[i]);
					}
				}
				else if(first.startsWith("Not")){
					second = second.replace(" ", "");
					int notNullNum = second.split(",").length;
					for(int i =0; i<notNullNum; i++) {
						notNull.add(second.split(",")[i]);
					}
				}
				else if(first.startsWith("Column") && !first.endsWith("Type ")) {
					second = second.replace(" ", "");
					colName.add(second);
				}
				else if(first.startsWith("Column") && first.endsWith("Type ")) {
					second = second.replace(" ", "");
					colType.add(second);
				}

			}
			br.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("txt파일없음");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	private void parsingCSV() {
		try {
			File file = new File(csvPath);
			FileReader fileReader = new FileReader(file);
			BufferedReader br = new BufferedReader(fileReader);
			Charset.forName("UTF-8");
			String line = "";
			line = br.readLine();
			csvColNum = line.split(",").length;
			for(int i=0; i<csvColNum; i++) {
				csvColName.add(line.split(",")[i]);
			}
			while((line=br.readLine())!=null) {
				ArrayList<String> temp = new ArrayList<String>();
				for(int i=0; i<csvColNum; i++) {
					temp.add(line.split(",")[i]);
				}
				csvData.add(temp);
			}
			br.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("csv파일 없음");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	public String getMakeTableQuery() {
		System.out.println("Please specify the filename for table description : ");
		Scanner scan = new Scanner(System.in);
		txtPath = scan.nextLine();
		parsingTXT();

		String str = "CREATE TABLE "+SCHEMA_NAME+"."+tableName+" (";
		for(int idx = 0; idx<colName.size(); ++idx) {
			String curColName = colName.get(idx).toString(); 
			String add = curColName + " " + colType.get(idx).toString();
			if(notNull.contains(curColName)) {
				add += " not null";
			}
			add += ", ";
			str += add;
		}
		str += "primary key(";
		for(int idx = 0; idx<pkName.size(); ++idx) {
			str += pkName.get(idx).toString();
			if(idx!=pkName.size()-1) {
				str+=",";
			}
		}
		str += "))";
		makeTableQ = str;
		
		return makeTableQ;
	}
	
	public ArrayList<String> getInsertDataQuery(){
		System.out.println("Please specify the CSV filename : ");
		Scanner scan = new Scanner(System.in);
		csvPath = scan.nextLine();
		parsingCSV();
		if(csvColNum!=colName.size()) {
			System.out.println("Data import failure. (The number of columns does not match between the table description and the CSV file.)");
		}
		else {
			return null;
		}
		// data insert query 작성
		
		
		
		
		return insertDataQ;
	}
	
	
}
