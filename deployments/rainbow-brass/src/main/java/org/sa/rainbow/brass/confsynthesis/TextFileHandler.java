package org.sa.rainbow.brass.confsynthesis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;

import java.util.Scanner;
import java.util.ArrayList;

public class TextFileHandler{

	String m_fileName;
	
	public TextFileHandler(String fileName){
		m_fileName = fileName;
	}
	
	public ArrayList<String> readFileLines() throws IOException{		
		 ArrayList<String> list = new ArrayList<String>();
	     try (BufferedReader br = new BufferedReader(new FileReader(m_fileName)))
	     {
	         String sCurrentLine;
	         while ((sCurrentLine = br.readLine()) != null) {
	             list.add(sCurrentLine);
	         }	
	     } catch (IOException e) {
	         System.out.println("There was a problem reading from file "+m_fileName);
	         throw e;
	     } 
	     return list;
	}
	
	public ArrayList<String> readFileWords(){		
		ArrayList<String> list = new ArrayList<String>();
		try{
			Scanner s = new Scanner(new File(m_fileName));			
			while (s.hasNext()){
			    list.add(s.next());
			}
			s.close();
		} catch (IOException e){
			System.out.println("Unable to open "+m_fileName+".");
		}
	return list;
	}
	
	public void exportFile(ArrayList<String> contents){
		try {
            BufferedWriter out = new BufferedWriter (new FileWriter(m_fileName));
            for (String str: contents){
            	out.write(str+"\n");
            }
            out.close();
        }
        catch (IOException e){
            System.out.println("Error exporting file "+m_fileName);
        }
	}
	
	public void exportFile(String contents){
        try {
            BufferedWriter out = new BufferedWriter (new FileWriter(m_fileName));
            out.write(contents);
            out.close();
        }
        catch (IOException e){
            System.out.println("Error exporting file "+m_fileName);
        }
    }
}
