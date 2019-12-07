package org.sa.rainbow.brass.plan.p2_cp3;

import java.util.ArrayList;
import java.util.Arrays;

public class PathBuilder {

	public static String[][] l =  { {"32", "33", "31", "32"},
			{"31", "32", "30", "31"},
			{"30", "31", "29", "30"},
			{"29", "30", "28", "29"},
			{"28", "36", "28", "29"} };


	public static boolean tupleContains (String[] a, String s) {
		return (new ArrayList<String>(Arrays.asList(a)).contains(s));
	}
	
	public static ArrayList<String> getPath (String[][] l, String s, String e){
		String current = s;
		ArrayList<String> res = new ArrayList<String>();
		res.add(current);
		for (int i=0; i<l.length;i++) {
			for (int j=0; j<l[i].length;j++) {
				if (!res.contains(l[i][j])){
					if ((i<l.length-1) && (tupleContains(l[i+1],l[i][j]))) {
						res.add(l[i][j]);
					}
				}
			}
		}
		res.add(e);
//		System.out.println(res.toString());
		return res;
	}
	
	public static void main(String[] args) {
		getPath(l, "32", "36");
	}

}
