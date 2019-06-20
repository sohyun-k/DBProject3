package project3;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;

public class main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JDBCConnect con = new JDBCConnect();;
		con.connectDB();
		String schemaName = con.getSchemaName();
		
		int mode = 0;
		while(mode != 4) {
			System.out.print("Please input the instruction number (1: Import from CVS, 2: Export to CVS, 3: Manipulate Data, 4: Exit):");
			Scanner scn = new Scanner(System.in);
			mode = scn.nextInt();
			scn.nextLine();

			if(mode == 1) {
				System.out.println("[Import from CSV]");
				ReadCSV rCSV = new ReadCSV(schemaName);
				String tableQ = rCSV.getMakeTableQuery();
				con.executeTableSQL(tableQ);
				ArrayList<String> tableDataQ = rCSV.getInsertDataQuery();
				if(tableDataQ!=null)
				{
					//execute insert table data
					con.executeInsertTableDB(tableDataQ, rCSV.getCSVRaw());
				}
			}
			else if(mode == 2) {
				System.out.println("[Export to CSV]");
				ExportCSV eCSV = new ExportCSV(schemaName);
				String query = eCSV.getQuery();
				ArrayList<ArrayList<String>> rs = con.getTableResultSet(query);
				eCSV.makeCSVFile(rs);
			}
			else if(mode == 3) {
				System.out.println("[Manipulate Data]");
				boolean manipulateOut = false;
				ManipulateData mData = new ManipulateData();
				int manipulateMode = 0;
				while(!manipulateOut) {
					manipulateMode = mData.executeMData();
					if(manipulateMode == 1) {
						con.showTables();
					}
					else if(manipulateMode == 2) {
						con.describeTable();
					}
					else if(manipulateMode == 3) {
						con.SelectData();
					}
					else if(manipulateMode == 4) {
						con.insertData();
					}
					else if(manipulateMode == 5) {
						con.deleteData();
					}
					else if(manipulateMode == 6) {
						con.updateTable();
					}
					else if(manipulateMode == 7) {
						con.dropTable();
					}
					else if(manipulateMode == 8) {
						manipulateOut = true;
					}
				}
			}
			else if(mode == 4) {
				
			}
		}
	}
}
