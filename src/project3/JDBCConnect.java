package project3;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class JDBCConnect {
	private static final String path = "./connection.txt";
	private static final String DB_DRIVER = "org.postgresql.Driver";
	private String DB_CONNECTION_URL = "jdbc:postgresql://";
	private String IP = "";
	private String DB_NAME = "";
	private String SCHEMA_NAME = "";
	private String ID = "";
	private String PW = "";
	private Connection conn;
	private Statement st;
	
	private void parsingTXT() {
		// parsing txt file
		try {
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader br = new BufferedReader(fileReader);
			
			String line = "";
			while((line=br.readLine())!=null) {
				line = line.replace(" ", "");
				String first = line.split(":")[0];
				String second = line.split(":")[1];
				
				if(first.equals("IP")) {
					IP = second;
				}
				else if(first.equals("DB_NAME")) {
					DB_NAME = second;
				}
				else if(first.equals("SCHEMA_NAME")) {
					SCHEMA_NAME = second;
				}
				else if(first.equals("ID")) {
					ID = second;
				}
				else if(first.equals("PW")) {
					PW = second;
				}	
			}
			br.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("connection.txt ���� ����");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			

		// set DB_DRIVER
		DB_CONNECTION_URL = DB_CONNECTION_URL+IP+"/"+DB_NAME;
	}
	
	public void connectDB() {
		parsingTXT();
		try {
			Class.forName(DB_DRIVER);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Properties connProps = new Properties();
		
		// Setting Connection Info
		connProps.setProperty("user", ID);
		connProps.setProperty("password", PW);
		try {
			conn = DriverManager.getConnection(DB_CONNECTION_URL, connProps);
			st = conn.createStatement();
			String createSchemaSQL = "CREATE SCHEMA "+SCHEMA_NAME;
			st.executeUpdate(createSchemaSQL);
			
		} catch (SQLException sqlEX) {
			System.out.println(sqlEX);
		}
	}
	
	public String getSchemaName() {
		return SCHEMA_NAME;
	}
	
	public void executeTableSQL(String str) {
		try {
			st.executeUpdate(str);
			System.out.println("Table is newly created as described in the file");
		} catch (SQLException e) {
			System.out.println("Table already exists");
		} 
	}
	
	public void executeInsertTableDB(ArrayList<String> insertTableQ, ArrayList<String> rawCSV) {
		int fail = 0;
		ArrayList<String> failedStack = new ArrayList<String>();
		for(int idx=0; idx<insertTableQ.size(); ++idx) {
			try {
				st.executeUpdate(insertTableQ.get(idx).toString());
			} catch (SQLException e) {
				fail++;
				failedStack.add("Failed tuple : "+ (idx+1) + " line in CSV - " + rawCSV.get(idx).toString());
			}
		}
		int success = insertTableQ.size()-fail;
		System.out.println("Data import completed. (Insertion Success : "+ success +", Insertion Failure : "+fail + ")");
		for(int idx = 0; idx<failedStack.size(); ++idx) {
			System.out.println(failedStack.get(idx).toString());
		}
		
	}

	public ArrayList<ArrayList<String>> getTableResultSet(String query) {
		ResultSetMetaData metaData = null;
		ArrayList<ArrayList<String>> tableResultData = new ArrayList<ArrayList<String>>();
		
		try {
			ResultSet rs = st.executeQuery(query);
			metaData = rs.getMetaData();
			int sizeOfColumn = metaData.getColumnCount();
			
			ArrayList<String> colNameArr = new ArrayList<String>();
			for(int colIdx =0; colIdx < sizeOfColumn; ++colIdx) {
				// TODO : 왜 colIdx+1인가?
				String colName = metaData.getColumnName(colIdx+1);
				colNameArr.add(colName);
			}
			tableResultData.add(colNameArr);
			
			while(rs.next()) {
				ArrayList<String> tempData = new ArrayList<String>();
				for(int colIdx =0; colIdx < sizeOfColumn; ++colIdx) {
					tempData.add(rs.getString(colNameArr.get(colIdx).toString()));
				}
				tableResultData.add(tempData);
			}
			
		} catch (SQLException e) {
			return null;
		}
		return tableResultData;
	}
	
	public void showTables() {

		try {
		      String ShowTableSQL = "SELECT tablename FROM PG_TABLES WHERE schemaname ='"+ SCHEMA_NAME+"'";                        

		      ResultSet rs = st.executeQuery(ShowTableSQL);
	          System.out.println("===========");
	          System.out.println("Table List");
	          System.out.println("===========");
	          while (rs.next()) {
	        	  System.out.println(rs.getString(1));
	          }			
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void describeTable() {
		System.out.print("Please specify the table name : ");
		Scanner scan = new Scanner(System.in);
		String tableName = scan.nextLine();
		String DescribeTable = "SELECT column_name, data_type, character_maximum_length, numeric_precision, numeric_scale " +
                "FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '"+ tableName+"' AND table_schema='"+SCHEMA_NAME+"'";
        ResultSet rs;
		try {
			rs = st.executeQuery(DescribeTable);
	        System.out.println("====================================================================================");
	        System.out.println("Column Name | Data Type | Character Maximum Length(or Numeric Precision and Scale)");
	        System.out.println("====================================================================================");
	       while (rs.next()) {
	          System.out.print(rs.getString(1) + ", " + rs.getString(2));
	          if((rs.getString(2)).equals("integer")){
	             System.out.print(", ("+rs.getString(4)+", "+rs.getString(5)+")");
	                
	           }
	          else if((rs.getString(2)).equals("date")||(rs.getString(2)).equals("time")){
	          
	          }
	          else{
	              System.out.print(", "+rs.getString(3));
	           }
	          System.out.println();
	          
	       }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	
	
	
	
	
	
	public void updateTable() {
		String updateQuery = "UPDATE "+SCHEMA_NAME+".";
		System.out.print("Please specify the table name : ");
		Scanner scan = new Scanner(System.in);
		String tableName = scan.nextLine();
		
		String DescribeTable = "SELECT column_name, data_type " +
                "FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '"+ tableName+"' AND table_schema='"+SCHEMA_NAME+"'";
		ResultSet rs;
		ArrayList<String> colNames = new ArrayList<String>();
		ArrayList<String> dataTypes = new ArrayList<String>();
		try {
			rs = st.executeQuery(DescribeTable);
			while(rs.next()) {
				colNames.add(rs.getString(1));
				dataTypes.add(rs.getString(2));
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<String> column = new ArrayList<String>();
		ArrayList<String> condition1 = new ArrayList<String>();
		ArrayList<String> condition2 = new ArrayList<String>();
		ArrayList<String> value = new ArrayList<String>();
		
		boolean error = false;
		System.out.print("Please specify the column which you want to make condition (Press enter : skip) : ");
		String colName = scan.nextLine();
		if(!colName.equals("")){
			column.add(colName);
			if(!colNames.contains(colName)) {
				error = true;
			}
			System.out.print("Please specify the condition (1: =, 2: >, 3: <, 4: >= 5: <=, 6: !=, 7: LIKE) : ");
			String select1 = scan.nextLine();
			if(select1.equals("1")) {
				select1 = "=";
			}
			else if(select1.equals("2")) {
				select1 = ">";
			}
			else if(select1.equals("3")) {
				select1 = "<";
			}
			else if(select1.equals("4")) {
				select1 = ">=";
			}
			else if(select1.equals("5")) {
				select1 = "<=";
			}
			else if(select1.equals("6")) {
				select1 = "!=";
			}
			else if(select1.equals("7")) {
				select1 = "LIKE";
			}
			condition1.add(select1);
			System.out.print("Please specify the condition value ("+column.get(0).toString()+" "+condition1.get(0).toString()+" ?) :");
			String valStr = scan.nextLine();
			value.add(valStr);
			System.out.print("Please specify the condition (1: AND, 2: OR, 3: finish) : ");
			String select2 = scan.nextLine();
			while(!select2.equals("3")) {
				if(select2.contentEquals("1")) {
					condition2.add("and");
				}
				else if(select2.contentEquals("2")) {
					condition2.add("or");
				}
				System.out.print("Please specify the column which you want to make condition : ");
				colName = scan.nextLine();
				column.add(colName);
				if(!colNames.contains(colName)) {
					error = true;
				}
				System.out.print("Please specify the condition (1: =, 2: >, 3: <, 4: >= 5: <=, 6: !=, 7: LIKE) : ");
				select1 = scan.nextLine();
				if(select1.equals("1")) {
					select1 = "=";
				}
				else if(select1.equals("2")) {
					select1 = ">";
				}
				else if(select1.equals("3")) {
					select1 = "<";
				}
				else if(select1.equals("4")) {
					select1 = ">=";
				}
				else if(select1.equals("5")) {
					select1 = "<=";
				}
				else if(select1.equals("6")) {
					select1 = "!=";
				}
				else if(select1.equals("7")) {
					select1 = "LIKE";
				}
				condition1.add(select1);
				String makeStr = column.get(0).toString()+" "+condition1.get(0).toString();
				for(int idx=1; idx<column.size(); ++idx) {
					makeStr += " "+ value.get(idx-1).toString() +" "+ condition2.get(idx-1).toString()
							+" "+ column.get(idx).toString() +" "+ condition1.get(idx).toString();
				}
				
				
				System.out.print("Please specify the condition value ("+makeStr+" ?) :");
				valStr = scan.nextLine();
				value.add(valStr);
				System.out.print("Please specify the condition (1: AND, 2: OR, 3: finish) : ");
				select2 = scan.nextLine();
				condition2.add(select2);
				
				
				
			}
			System.out.print("Please specify column names which you want to update : ");
			String updateColumn = scan.nextLine();
			updateColumn = updateColumn.replace(" ", "");
			System.out.print("Please specify the value which you want to put : ");
			String updateVal = scan.nextLine();
			updateVal = updateVal.replace(" ", "");
			updateQuery += "\""+tableName+ "\"" +" SET ";
			
			for(int idx=0; idx<updateColumn.split(",").length; ++idx) {
				String temp = updateColumn.split(",")[idx];
				System.out.println(temp);
				if(!colNames.contains(temp)) {
					error = true;
				}
				updateQuery+= "\""+updateColumn.split(",")[idx]+"\""+"=";
				// integer가 아니라면 
				if(dataTypes.get(colNames.indexOf(temp)).toString().equals("integer")) {
					updateQuery+=updateVal.split(",")[idx];
				}
				else {
					updateQuery+="'"+updateVal.split(",")[idx]+"'";
				}
				if(idx!=updateColumn.split(",").length-1) {
					updateQuery+=", ";
				}
			}
			
			//condition만들기
			String makeStr =  "\""+column.get(0).toString()+"\" "+condition1.get(0).toString();
			for(int idx=1; idx<column.size(); ++idx) {
				makeStr += " "+ value.get(idx-1).toString() +" "+ condition2.get(idx-1).toString()
						+" \""+ column.get(idx).toString() +"\" "+ condition1.get(idx).toString();
			}
			makeStr+=value.get(value.size()-1).toString();

			updateQuery += " WHERE "+makeStr;
	
			
		}
		try {
			int rows = st.executeUpdate(updateQuery);
			if(rows == 0 || rows == 1 ) {
				System.out.println("<"+rows+" row updated>");
			}
			else {
				System.out.println("<"+rows+" rows updated>");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	public void dropTable() {
		System.out.print("Please specify the table name : ");
		Scanner scan = new Scanner(System.in);
		String tableName = scan.nextLine();
		System.out.print("If you delete this table, is it not guaranteed to recover again. Are you sure you want to delete this table (Y: yes, N: no)? ");
		String answer = scan.nextLine();
		while(!answer.equals("N") && !answer.equals("Y")) {
			System.out.print("If you delete this table, is it not guaranteed to recover again. Are you sure you want to delete this table (Y: yes, N: no)? ");
			answer = scan.nextLine();
		}
		if(answer.equals("N")) {
			System.out.println("<Deletion canceled>");
		}
		else if(answer.equals("Y")) {
			String sql = "DROP TABLE "+SCHEMA_NAME+".\""+tableName+"\"";
			try {
				st.executeUpdate(sql);
				System.out.println("<The Table "+tableName+" is deleted>");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("drop table 실패");
			}
			
		}
	}

}

