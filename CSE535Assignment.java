import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;



public class CSE535Assignment { 
	
 public	static Map<String,List<Integer>> indexMap1DaaT = new HashMap<String,List<Integer>>();
 public static Map<String,List<Integer>> indexMap2TaaT = new HashMap<String,List<Integer>>();
 public	static Map<String,Integer> indexMapPostingLength = new HashMap<String,Integer>(26000);
// public static List<String> firstlineQueryList = new LinkedList<String>();
 //public static List<String> secondlineQueryList = new LinkedList<String>();
 public static List<String>[] queryList = new LinkedList[10];
 public static int queryLineCount =0;
 


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String indexFile,outputLog,queryTermsFile;
		List<String> topKtermsList = new LinkedList<String>();
	
		
		
		try {
			indexFile = args[0]; //first argument gives indexfile location
			outputLog = args[1]; //second argument gives output filename
			Integer toptermsCount = Integer.parseInt(args[2]); //third argument provides topKterms count
			queryTermsFile = args[3]; //fourth argument gives queryfile name
			FileWriter writer = new FileWriter(new File(args[1]),true); 
			
			createIndexFiles(indexFile); 
			
			writer.write("FUNCTION: getTopK "+toptermsCount+"\n");
			topKtermsList = getTopK(toptermsCount);
			
			String termresult =  topKtermsList.toString();
			termresult = termresult.substring(1,termresult.length()-1);
			
			writer.write("Result: "+termresult+"\n");
			writer.close();
			
			extractQuery(queryTermsFile);
			
			//iterate through every query line and do required operations
			for(int i =0;i<queryLineCount;i++)
			{
			writePostingList(queryList[i], outputLog);
			DaatAndOperation(queryList[i],outputLog);
			TaatAndOperation(queryList[i], outputLog);
			TaatOrOperation(queryList[i], outputLog);
			}
			
		}
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/* Reads index file and stores every line value into a list */
public static void createIndexFiles(String fileName)
{
	 try
	 {
	 Scanner in = new Scanner(new FileReader(fileName));
	
	 Map<Integer,Integer> docTermfreqMap = new HashMap<Integer,Integer>();
	 LinkedList<Integer>[] postingList = new LinkedList[26000];
	 LinkedList<Integer>[] postingListTF = new LinkedList[26000];
	 
	 String temp =new String();
	 int lineCounter = 0;
	 int previousNumber = 0;
	 
	   while(in.hasNextLine())
	   {
		 temp = in.nextLine();
		 String[] temp1 = temp.split("\\\\");
		
		 	Pattern p = Pattern.compile("\\d+"); //extracting digits from the string
		 	Matcher m = p.matcher(temp1[2]);
		 	
		 	int docCounter = 0; // Just to skip every second occurrence and store documentID, Termfrequency separately
		 	
		 	//initializing required lists
		 		postingList[lineCounter] = new LinkedList<Integer>();
		 		postingListTF[lineCounter] = new LinkedList<Integer>();
		 	
		 	
		 	//Iterating through all digits for creating index1 and index2 based on TF
		 	while (m.find()) {
		 	   int tt = Integer.parseInt(m.group());
		 	 
		 	   if(docCounter%2==0)
		 	        postingList[lineCounter].add(tt);
		 	   else
		 		   docTermfreqMap.put(previousNumber, tt); //storing doc-tf values in a map		     
		 		
		 	   previousNumber = tt; //previous number stores the docID
		 	   docCounter = docCounter+1;
	 	 	}
		 	//just completed collecting all digits in a line
		 	
		 	//Sort the document-term freq map based on term freq, using comparator class
		 	Map<Integer,Integer> SortedDocfreqMap = new TreeMap<Integer,Integer>(new sortMapbyValue(docTermfreqMap));
		 	SortedDocfreqMap.putAll(docTermfreqMap);
		 	
		 	//storing doc ID on the basis of decreasing term frequency
		 	postingListTF[lineCounter].addAll(SortedDocfreqMap.keySet());
		 		
		 	Collections.sort(postingList[lineCounter]);
		 	
		    //Storing values in Daat, Taat and PostingLength Map
		 	indexMap1DaaT.put(temp1[0],postingList[lineCounter]);
		 	indexMap2TaaT.put(temp1[0],postingListTF[lineCounter]);
		 	indexMapPostingLength.put(temp1[0],postingList[lineCounter].size());
		 
		 	lineCounter = lineCounter+1;
	 	 	System.out.println(lineCounter);
	 	 	
	 	 	//Emptying doc freq map for next line
	 	 	docTermfreqMap.clear();
	 	 	SortedDocfreqMap.clear();
	 	 	
	   }
	 
	 in.close();
  }
	 
	 catch(Exception e)
	 {e.printStackTrace();}
}



/* This function return the top k terms of the index file using the posting list*/
public static List<String> getTopK(int termsCount)
{
	List<String> tempList = new ArrayList<String>();	
	List<String> finalTermList = new ArrayList<String>();
	
	//passing term-length pair map to comparator class for value based sorting
	Map<String,Integer> SortedPostingLengthMap = new TreeMap<String,Integer>(new sortMapbyValue(indexMapPostingLength));
	SortedPostingLengthMap.putAll(indexMapPostingLength);
	
	tempList.addAll(SortedPostingLengthMap.keySet());
	
     // Storing required number of values on finalTermList
	 for(int count=0;count<termsCount;count++)
		 finalTermList.add(tempList.get(count));	

	return finalTermList;
}



/* This function extracts the query terms from file and stores them in public lists*/
public static void extractQuery(String fileName)
{
	int lineCounter = 0;	
	String tempLine = new String();
	
	
   	try
	 {
	   Scanner sc = new Scanner(new FileReader(fileName));
	   while(sc.hasNextLine())
	   { 
		 tempLine = sc.nextLine();	
		 
		 String queryarray[] = tempLine.split(" ");
		 queryList[lineCounter] = new LinkedList<String>(); //list used to store query terms in a line
		 	
		 for(String str: queryarray)
		 		queryList[lineCounter].add(str); //storing queries of each line in a separate list
		 
		 queryLineCount =queryLineCount+1; //having count of querylines in the queryFile
		 		   
		 lineCounter=lineCounter+1; 
	   }
	 }
   	
	catch(Exception e)
	{e.printStackTrace();}

}



//This function return Daat and Taat postings of the queries passed in the List
public static void writePostingList(List<String> list, String outputfileName)
{
	try {

		Map<String,List<Integer>> postingMap = new TreeMap<String,List<Integer>>();
		FileWriter writer = new FileWriter(new File(outputfileName),true); //second arguemtn gives output filename
		for(int i=0;i<list.size();i++)
		{
			//System.out.println("printing list items"+list.get(i));
			String key = list.get(i);
			writer.write("FUNCTION: getPostings "+key+"\n");
			if(indexMap1DaaT.containsKey(key))
			{
				String tempstringDaat = Arrays.toString(indexMap1DaaT.get(key).toArray());
				tempstringDaat = tempstringDaat.substring(1, tempstringDaat.length()-1);

				
				String tempstringTaat = Arrays.toString(indexMap2TaaT.get(key).toArray());
				tempstringTaat = tempstringTaat.substring(1, tempstringTaat.length()-1);
			
				
				writer.write("Ordered by Doc IDs: "+tempstringDaat+"\n");
				writer.write("Ordered by TF: "+tempstringTaat+"\n");
			}
			else
				writer.write("term not found\n");
		}
	   writer.close();
	}
	
	catch(Exception e)
    { e.printStackTrace(); }
}



// Performs DaatAnd operation
public static void DaatAndOperation(List<String> queryList,String outputFile )
{
	
	FileWriter filewriter;
	try {
		filewriter = new FileWriter(new File(outputFile),true);
		List<Integer>[] postingList = new LinkedList[queryList.size()+1];
		List<Integer> resultPostingList = new LinkedList<Integer>();
		List<Integer> maxCalculatorList = new LinkedList<Integer>();
		List<Integer> minCalculatorList = new LinkedList<Integer>();
			
		int smallestPosting = 0, documentcount =0, flag=0;
		long  startTime =0, endTime=0;
		
		String str = queryList.toString();
		str = str.substring(1,str.length()-1);
	
		filewriter.write("Function:documentAtATimeQueryAnd "+str);
		
		startTime = System.currentTimeMillis();
		
		//extract posting list from map and store in array of new lists
		for(int i=0; i<queryList.size();i++)
		{
			postingList[i] = new LinkedList<Integer>();
			if(indexMap1DaaT.containsKey(queryList.get(i)))
			postingList[i] = indexMap1DaaT.get(queryList.get(i));
			else
			{
			filewriter.write("terms not found");
			flag =1;
			break;
			}
			documentcount = documentcount+postingList[i].size();
		}
		
		
		int comparisons = 0,counter1 =0,counter2=0, itr=0;
		
		int check=0;
		
		
		/*if every value is same in maxlist, add value in templist - increment counter1
		else find max from the maxlist 
		increment postings which do not have maximum value -count2 variable used
		*/
		
	 //flag triggered when any query term not present in index and thus comparison skipped
	 if(flag==0)
	 { 
		int maximum = postingList[0].get(0); //assigning maximum value as the first value in first list
		
		while(true) //iterate till any one list becomes null
		{	
			
			System.out.println("querylist size"+queryList.size());
			
			//adding values from each postinglist to variable maxCalculatorList, for comparison purpose
			for(int a=0; a<queryList.size();a++)
			{
				
				if(postingList[a].size()<=counter1)
				{   check =1;
				    System.out.println("break printed alreadyyyyyyyyyyy");
					break;
				}
				else
				{	
					System.out.println("value and maximum"+postingList[a].get(counter1)+" "+maximum);
					if(postingList[a].get(counter1)<maximum)
					{
						if(postingList[a].size()<=counter2)
						{
							System.out.println("break printed");
							check=1;
							break; //break loop if any list reaches end
						}
						else
						{
							 maxCalculatorList.add(postingList[a].get(counter2));
							 System.out.println("counter2 "+postingList[a].get(counter2));
							 
						}
						
					}
					
					else
					{
						maxCalculatorList.add(postingList[a].get(counter1));
						System.out.println("counter1 "+postingList[a].get(counter1));
						System.out.println("query list sizee   "+queryList.size());
						System.out.println("value of a "+a);
						//System.out.println("query stuck is"+indexMap1DaaT.);
					}
				}	
				
				
			} //end of for loop
			
			if(check==1)   
				break;    // check variable used to denote end of any list, thus breaking while loop
				
		
			//checking if all the values pointed are equal
			for (int i = 0; i < maxCalculatorList.size()-1; i++)
			{	comparisons = comparisons+1;
				if(maxCalculatorList.get(i+1).equals(maxCalculatorList.get(i)))
					itr = itr+1;
			}
			
			//if all values are equal, write to temp list
			if(itr==queryList.size()-1)
			{
				    counter1 = counter1+1; //increment btoh counters if all values are equal
					counter2 = counter2+1;
				    resultPostingList.add(maxCalculatorList.get(0)); //adding any value from maxcalculator to the final resultset
					System.out.println("values are equal"+maxCalculatorList.get(0));
			}
			
			//else calculate maximum of the terms
			else
			{
			 for (int j = 0; j < maxCalculatorList.size()-1; j++)
			 {	comparisons = comparisons+1;
				if(maxCalculatorList.get(j)>maximum)
				 maximum = maxCalculatorList.get(j);
				
				
			 }
			 counter2 = counter2+1; //increment counter2 is terms not equal
			} 
		
			
			//System.out.println("counter1 and counter2 "+counter1+" "+counter2);
			//System.out.println("result posting list "+resultPostingList);
			
			maxCalculatorList.clear();
			itr =0;
		}	//while braces
			
	 } // if "flag" braces
		
		
		endTime = System.currentTimeMillis();
		
		//System.out.println("result set "+resultPostingList);
		
		String result = resultPostingList.toString();
		result = result.substring(1, result.length()-1);
		
		System.out.println("result set "+resultPostingList);
		filewriter.write("\n"+documentcount+" documents are found");
		filewriter.write("\n"+comparisons+" comparisons are made");
		filewriter.write("\n"+((startTime-endTime)/1000)+" seconds are used");
		filewriter.write("\nResult: "+result+"\n");
			
		filewriter.close();
			
	} // try block completes
		
		catch (IOException e)
	    {		e.printStackTrace();}
	
}


//performs Taat And operation
public static void TaatAndOperation(List<String> queryList,String outputFile )
{
	try {
		int documentcount=0;
		long startTime =0,endTime=0;
		int flag =0;
	
		startTime = System.currentTimeMillis();
		
		FileWriter filewriter;
		filewriter = new FileWriter(new File(outputFile),true);
		
		//for writing to output file
		String str = queryList.toString();
		str = str.substring(1,str.length()-1);
		
		filewriter.write("Function:termAtATimeQueryAnd "+str);
		
		List<Integer>[] postingList = new LinkedList[queryList.size()];
		List<Integer> temppostingList = new LinkedList<Integer>();
		
		//extract posting list from map and store in array of new lists
		for(int i=0; i<queryList.size();i++)
		{
			postingList[i] = new LinkedList<Integer>();
			if(indexMap2TaaT.containsKey(queryList.get(i)))
			postingList[i] = indexMap2TaaT.get(queryList.get(i));
			else
			{
			filewriter.write("terms not found");
			flag =1;
			break;
			}
			documentcount = documentcount+postingList[i].size();
		}
		
		int comparisons = 0;
		
		if(flag==0)
		{
		//for every term int the query list, And operation is done and result written to temp posting list
		for(int a=0; a<queryList.size()-1;a++)
		{
			  for(int x=0; x<postingList[a].size();x++)
				  for(int y=0; y<postingList[a+1].size();y++)
				  {   comparisons = comparisons+1;
					  if((postingList[a].get(x)).equals(postingList[a+1].get(y)))
						  temppostingList.add(postingList[a+1].get(y));
				  }
			  
		  postingList[a+1].clear();
		  postingList[a+1].addAll(temppostingList);//making the temp list the next list, so as to compare with the third list
		  
		  if(a<(queryList.size()-2))
			  temppostingList.clear(); //clearing temp map for next comparison, doesn't clear for final comparison
		  
		}
		}
		endTime = System.currentTimeMillis();

		
		Collections.sort(temppostingList); // sorting list to give docID in increasing order
		
		String result = temppostingList.toString();
		result = result.substring(1, result.length()-1);
		
		filewriter.write("\n"+documentcount+" documents are found");
		filewriter.write("\n"+comparisons+" comparisons are made");
		filewriter.write("\n"+((startTime-endTime)/1000)+" seconds are used");
		filewriter.write("\nResult: "+result+"\n");
		
		
		//System.out.println(temppostingList);
		//filewriter.write(str);
		filewriter.close();
		
	} 
	
	catch (IOException e) {
		e.printStackTrace();}

}



// Performs Taat OR operation
public static void TaatOrOperation(List<String> queryList,String outputFile )
{
	try {
		int documentcount=0;
		long startTime =0,endTime=0;
		int flag =0;
	
		startTime = System.currentTimeMillis();
		
		FileWriter filewriter;
		filewriter = new FileWriter(new File(outputFile),true);
		
		//for writing to output file
		String str = queryList.toString();
		str = str.substring(1,str.length()-1);
		
		filewriter.write("Function:termAtATimeQueryOr "+str);
		
		List<Integer>[] postingList = new LinkedList[queryList.size()];
		List<Integer> resultpostingList = new LinkedList<Integer>();
		List<Integer> temppostingList = new LinkedList<Integer>();
		
		
		//extract posting list from map and store in array of new lists
		for(int i=0; i<queryList.size();i++)
		{
			postingList[i] = new LinkedList<Integer>();
			if(indexMap2TaaT.containsKey(queryList.get(i)))
			postingList[i] = indexMap2TaaT.get(queryList.get(i));
			else
			{
			filewriter.write("terms not found");
			flag =1;
			break;
			}
			documentcount = documentcount+postingList[i].size();
		}
		
		int comparisons = 0,check=0;
		
		//Storing the values of the first posting to the result
		resultpostingList.addAll(postingList[0]);
		
		System.out.println("original result list"+resultpostingList);
		
		if(flag==0)
		{
		//for every term int the query list, OR operation is done and result written to temp posting list
		 for(int a=1; a<queryList.size();a++) //starts from a=1, since first list is already stored
		 {
			 for(int y=0; y<postingList[a].size();y++) //taking every value from posting list and checking with result set
			 {
			   for(int x=0; x<resultpostingList.size();x++)
				  {   comparisons = comparisons+1;
				  		if(resultpostingList.get(x).equals(postingList[a].get(y)))
				  		check=1; //if any value matches the value in result set
				  }
			   
			   if(check==0)
			  	  temppostingList.add(postingList[a].get(y));
			  
			  	check=0; //refreshing value for next iteration	
			   
			 }
		  System.out.println("query list size count of a  "+a);	  
		  resultpostingList.addAll(temppostingList);
		  System.out.println("appended result list"+resultpostingList);
		  temppostingList.clear(); // using temp list to reduce comparisons and clearing it for next iteration
		 }
		}
		endTime = System.currentTimeMillis();

		Collections.sort(resultpostingList); //sorting list to give ordered docID
		
		String result = resultpostingList.toString();
		result = result.substring(1, result.length()-1);
		
		filewriter.write("\n"+documentcount+" documents are found");
		filewriter.write("\n"+comparisons+" comparisons are made");
		filewriter.write("\n"+((startTime-endTime)/1000)+" seconds are used");
		filewriter.write("\nResult: "+result+"\n");
		
		filewriter.close();
		
	} 
	
	catch (IOException e) {
		e.printStackTrace();}

}


}

