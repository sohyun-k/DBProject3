package project3;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

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
	
	public void SelectData() {
		String SelectQuery = "";
		HashMap<String, String> conditionMap_e = new HashMap<String, String>();
		conditionMap_e.put("1", "=");
		conditionMap_e.put("2", ">");
		conditionMap_e.put("3", "<");
		conditionMap_e.put("4", ">=");
		conditionMap_e.put("5", "<=");
		conditionMap_e.put("6", "!=");
		conditionMap_e.put("7", "LIKE");

		HashMap<String, String> conditionMap_c = new HashMap<String, String>();
		conditionMap_c.put("1", "AND");
		conditionMap_c.put("2", "OR");
		conditionMap_c.put("3", "");
		
		int mode = 0;
		int mode_c = 0;
		String mode_v = "";
		String p_col[] = null;
		String ConditionQuery = "";
		
		Scanner scn = new Scanner(System.in);
		System.out.print("Please specify the table name :");
		String table_name = scn.nextLine();
		
		System.out.print("Please specify the column which you want to retrive (ALL :*): ");
		String c_retrieve = scn.nextLine();
		if(!c_retrieve.equals("*")){
			SelectQuery = "SELECT ";
			c_retrieve = c_retrieve.replaceAll(" ", "");
			p_col = c_retrieve.split(",");
			for( String x : p_col){
				SelectQuery = SelectQuery + "\""+ x + "\"," ;
			}
			SelectQuery=SelectQuery.substring(0, SelectQuery.length()-1);
			SelectQuery += " FROM "+ "\""+SCHEMA_NAME+"\".\""+table_name+ "\"";
			
		}else{
			SelectQuery = "SELECT * FROM "+ "\""+SCHEMA_NAME+"\".\""+table_name+ "\"";
		}
		System.out.print("Please specify the column which you want to make condition (Press enter:skip) :");
		String c_condition = scn.nextLine();

		
		if(!c_condition.isEmpty()){
			while(mode != 3){
				
				if(mode!=0){
					System.out.print("Please specify the column which you want to make condition :");
					c_condition = scn.nextLine();
				}
				
				
	               System.out.println("Please specify the condition(1: =, 2: >, 3: <, 4: >=, 5: <=. 6: !=, 7: LIKE);");
	               mode_c = scn.nextInt();
	               scn.nextLine();
	               ConditionQuery = ConditionQuery + "\""+c_condition + "\" " + conditionMap_e.get(String.valueOf(mode_c));      
	               System.out.println("Please specify the value ("+ConditionQuery+" ?)");
	               mode_v = scn.nextLine();
	               
	               System.out.println("Please specify the condition (1: AND, 2:OR, 3:finish) : ");
	               mode = scn.nextInt();
	               scn.nextLine();
	              
	        ConditionQuery = ConditionQuery +" " + mode_v + " " +conditionMap_c.get(String.valueOf(mode)) + " ";
	        }
			SelectQuery += " WHERE " + ConditionQuery;
		}
					
		System.out.print("Please specify the column name for ordering (Press enter : skip) :");
		String c_ordering = scn.nextLine();
		if(!c_ordering.isEmpty()){
			System.out.print("Please specify the sorting criteria (Press enter:skip) :");
			String sortingCriteria = scn.nextLine();
			
			c_ordering = c_ordering.replace(" ", "");
			sortingCriteria = sortingCriteria.replace("ASCEND", "ASC");
			sortingCriteria = sortingCriteria.replace("DESCEND", "DESC");
			sortingCriteria = sortingCriteria.replace(" ", "");
			
			String p_co[] = c_ordering.split(",");
			String p_sc[] = sortingCriteria.split(",");

			SelectQuery += " ORDER BY ";
			for(int i=0; i<p_co.length; i++){
				SelectQuery += "\""+p_co[i] + "\" " + p_sc[i] + ", ";
			}
			SelectQuery = SelectQuery.substring(0, SelectQuery.length()-2);
		}

			
		//Describe Table
		HashMap<String, String> columnInfo = new HashMap<String, String>();
		String DescribeTable1 = "SELECT column_name, data_type FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '"+ table_name+"' AND table_schema='"+SCHEMA_NAME+"'";
		ResultSet tempRS;
		ResultSet rs;
		Set<Map.Entry<String, String>> colEntries;
		try {
			tempRS = st.executeQuery(DescribeTable1);
			while(tempRS.next()){
				columnInfo.put(tempRS.getString(1), tempRS.getString(2));
				//System.out.println("columnInfo : " +tempRS.getString(1) + ", " + tempRS.getString(2));
			}
			colEntries = columnInfo.entrySet();
			
			SelectQuery+=";";

			rs = st.executeQuery(SelectQuery);
			

			String temp ="";
		    System.out.println("=================================================");
		    if(c_retrieve.equals("*")){
		    	for(String key : columnInfo.keySet()){
		    		String value = key;
		    		temp = temp + value + " | ";
		    	}
		    }else{
		    	for(String x : p_col){
		    		temp = temp + x + " | ";
		    		//System.out.println("p_col test: "+ columnInfo.get(x));
		    	}
		    }temp = temp.substring(0, temp.length()-2);
		    System.out.println(temp);
		    System.out.println("=================================================");
		    
		    String result = "";
		    int label = 1;
			while (rs.next()) {

				int cnt = 1;
				if(c_retrieve.equals("*")){
					for(Map.Entry<String, String> colEntry : colEntries){
						//System.out.println(colEntry.getValue());
						if(colEntry.getValue().contains("int")){
							result += rs.getInt(cnt);
						}else if(colEntry.getValue().contains("date")){
							result += rs.getDate(cnt);
						}else if(colEntry.getValue().contains("time")){
							result += rs.getTime(cnt);
						}
						else{
							result += rs.getString(cnt);
						}
						cnt++; result += ", ";
					}
					
					result=result.substring(0, result.length()-2);
					System.out.println(result);
					result = "";
				}else{
					
					for(String x : p_col){
						//System.out.println("p_col : "+x);
						String value = columnInfo.get(x);
						if(value.contains("int")){
							result += rs.getInt(cnt);
						}else if(value.contains("date")){
							result += rs.getDate(cnt);
						}else if(value.contains("time")){
							result += rs.getTime(cnt);
						}else{
							result += rs.getString(cnt);
						}
						cnt++; result += ", ";
						
					}
					result=result.substring(0, result.length()-2);
					System.out.println(result);
					result = "";
				}
				if(label%10 == 0) {
					System.out.println("Press Enter");
					Scanner sc = new Scanner(System.in);
					sc.nextLine();
				}
				label++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("<error detected>");
		}
		
	}
	
	public void insertData() {
		String insertQuery = "INSERT INTO "+SCHEMA_NAME+".";
		
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
		
		
		insertQuery += "\""+tableName+"\" (";
		System.out.print("Please specify all the columns in order of which you want to insert :");
		String colum_names = scan.nextLine();
		colum_names = colum_names.replace(" ", "");
		ArrayList<String> insertCol = new ArrayList<String>();
		ArrayList<String> insertVal = new ArrayList<String>();
		
		for(int idx=0; idx<colum_names.split(",").length; ++idx) {
			insertCol.add(colum_names.split(",")[idx]);
		}
		
		System.out.print("Please specify values for each column :");
		String values = scan.nextLine();
		values = values.replace(" ", "");
		
		for(int idx=0; idx<values.split(",").length; ++idx) {
			insertVal.add(values.split(",")[idx]);
		}

		for(int idx=0; idx<insertCol.size(); ++idx) {
			insertQuery += "\"" + insertCol.get(idx).toString() + "\"";
			if(idx!=insertCol.size()-1) {
				insertQuery += ", ";
			}
		}
		insertQuery += ") values (";
		
		for(int idx=0; idx<insertVal.size(); ++idx) {
			if(colNames.indexOf(insertCol.get(idx).toString())!= -1) {
				if(dataTypes.get(colNames.indexOf(insertCol.get(idx).toString())).toString().equals("integer")) {
					insertQuery += insertVal.get(idx).toString();
				}
				else {
					insertQuery += "'"+insertVal.get(idx).toString()+"'";
				}
				if(idx!=insertVal.size()-1) {
					insertQuery += ", ";
				}
			}
		}
		insertQuery += ")";
		
		try {

			st.executeUpdate(insertQuery);
			System.out.println("<1 row updated>");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("<0 row inserted due to error>");
			return;
		}
				
	}
	
	
	
	
	
	public void deleteData() {
		String DeleteQuery = "";
		HashMap<String, String> conditionMap_e1 = new HashMap<String, String>();
		conditionMap_e1.put("1", "=");
		conditionMap_e1.put("2", ">");
		conditionMap_e1.put("3", "<");
		conditionMap_e1.put("4", ">=");
		conditionMap_e1.put("5", "<=");
		conditionMap_e1.put("6", "!=");
		conditionMap_e1.put("7", "LIKE");

		HashMap<String, String> conditionMap_c1 = new HashMap<String, String>();
		conditionMap_c1.put("1", "AND");
		conditionMap_c1.put("2", "OR");
		conditionMap_c1.put("3", "");

		int mode1 = 0;
		int mode_c1 = 0;
		String mode_v1 = "";
		
		String ConditionQuery1 = "";
		
		Scanner scn11 = new Scanner(System.in);
		System.out.println("Please specify the table name :");
		String table_name11 = scn11.nextLine();

		DeleteQuery = "DELETE FROM " + "\"" + SCHEMA_NAME + "\".\"" + table_name11 + "\" ";
		System.out.println("Please specify the column which you want to make condition (Press enter:skip) :");
		String c_condition1 = scn11.nextLine();

		
		if(!c_condition1.isEmpty()){
			while(mode1 != 3){
				
				if(mode1!=0){
					System.out.println("Please specify the column which you want to make condition :");
					c_condition1 = scn11.nextLine();
				}
				
				
				System.out.println("Please specify the condition(1: =, 2: >, 3: <, 4: >=, 5: <=. 6: !=, 7: LIKE): ");
				mode_c1 = scn11.nextInt();
				scn11.nextLine();
				ConditionQuery1 = ConditionQuery1 + "\""+c_condition1 + "\" " + conditionMap_e1.get(String.valueOf(mode_c1));		
				System.out.println("Please specify the value ("+ConditionQuery1+" ?)");
				mode_v1 = scn11.nextLine();
				
				System.out.println("Please specify the condition (1: AND, 2:OR, 3:finish) : ");
				mode1 = scn11.nextInt();
				scn11.nextLine();
				
				ConditionQuery1 = ConditionQuery1 +" " + mode_v1 + " " +conditionMap_c1.get(String.valueOf(mode1)) + " ";
			}
			DeleteQuery += "WHERE " + ConditionQuery1;
		}
			
		DeleteQuery+=";";
		System.out.println(DeleteQuery);
		try {
			int rows = st.executeUpdate(DeleteQuery);
			if(rows == 0 || rows == 1 ) {
				System.out.println("<"+rows+" row updated>");
			}
			else {
				System.out.println("<"+rows+" rows updated>");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("<error detected>");
			return;
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
		
		System.out.print("Please specify the column which you want to make condition (Press enter : skip) : ");
		String colName = scan.nextLine();
		if(!colName.equals("")){
			column.add(colName);

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
		else {
			System.out.println("<0 row updated>");
			return ;
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
			System.out.println("<error detected>");
			return;
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

