package project3;

import java.io.*;
import java.sql.*;
import java.util.Properties;

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

}

