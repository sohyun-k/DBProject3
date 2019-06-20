package project3;

import java.util.Scanner;

public class ManipulateData {
	private int mode = 0;
	
	public int executeMData() {
		System.out.print("Please input the instruction number (1: Show Tables, 2: Describe Table, 3: Select, 4: Insert, 5: Delete, 6: Update, 7: Drop Table, 8: Back to main) : ");
		Scanner scan = new Scanner(System.in);
		mode = scan.nextInt();
		scan.nextLine();
		return mode;
	}

}
