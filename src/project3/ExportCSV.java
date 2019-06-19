package project3;

import java.io.*;
import java.sql.ResultSet;
import java.util.Scanner;
import java.util.ArrayList;

public class ExportCSV {
	private String tableName = "";
	private String fileName = "";
	private String SCHEMA_NAME="";
	private String queryStr = "";
	private ResultSet data;
	
	public ExportCSV(String schemaName) {
		SCHEMA_NAME = schemaName;
		System.out.print("Please specify the table name : ");
		Scanner scan = new Scanner(System.in);
		tableName = scan.nextLine();
		System.out.print("Please specify the CSV filename : ");
		fileName = scan.nextLine();
		queryStr = "SELECT * FROM "+SCHEMA_NAME+".\""+tableName+"\"";
	}
	
	public String getQuery() {
		return queryStr;
	}
	
	public void makeCSVFile(ArrayList<ArrayList<String>> rs) {
		File targetFile = new File(fileName);
		try {
			targetFile.createNewFile();
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile.getPath()), "UTF8"));
			
			for(int tuple_idx =0; tuple_idx<rs.size(); ++tuple_idx) {
				for(int col_idx = 0; col_idx<rs.get(tuple_idx).size(); ++col_idx) {
					output.write(rs.get(tuple_idx).get(col_idx).toString());
					if(col_idx != rs.get(tuple_idx).size()-1) {
						output.write(",");
					}
				}
				output.newLine();
			}
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("파일생성실패");
		}

	}
	
}
