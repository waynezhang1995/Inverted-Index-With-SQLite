import java.io.*;
import java.sql.*;
import java.util.*;

public class Index {
	static Connection conn;
    static PreparedStatement stmt_insert_into_postings; 
    static PreparedStatement stmt_select_all_from_postings;
    static PreparedStatement stmt_insert_into_docmag;
    static PreparedStatement stmt_select_df_from_postings;
    static PreparedStatement stmt_select_docinfo_from_docmag;
    	
    static {
    	try { 
    		conn = ConnectionManager.getConnection();
    		conn.setAutoCommit(false);
    		stmt_insert_into_postings = conn.prepareStatement("INSERT INTO postings(word, df, docidfreqbytestream) VALUES(?,?,?)");
    		stmt_select_all_from_postings = conn.prepareStatement("SELECT word, df, docidfreqbytestream FROM postings");
    		stmt_insert_into_docmag = conn.prepareStatement("INSERT INTO docmag(docid, vectormagnitude, maxf) VALUES(?,?,?)");
    		stmt_select_df_from_postings = conn.prepareStatement("SELECT df FROM postings WHERE word=?");
    		stmt_select_docinfo_from_docmag = conn.prepareStatement("SELECT vectormagnitude, maxf FROM docmag WHERE docid=?");
    	}
    	catch (Exception e) {System.out.println(e);}; 		
    }

    //This method outputs word-docid pairs in file worddoclistfilename
    //It removes stop-words. 
	public static void fillWordList(String dirPath, String stopwordsfilename,
			String worddoclistfilename) throws Exception {
		// Reading stopwords
			//TO-DO
		
		// Opening the word-doc list output file
			//TO-DO

		// Reading documents from the directory (folder) provided
		// As we read, we write word-docid pairs to the file.
			//TO-DO
	}   
    
	//It fills the postings table.
	public static void fillWordTable (String sortedWordDocListFilename) throws Exception {
        
		/* TO-DO...
		
		stmt_insert_into_postings.setString(1, word);
		stmt_insert_into_postings.setInt(2, docid_freq_map.size());
		stmt_insert_into_postings.setBytes(3, VB.VBENCODE( Delta.sortedmap2deltalist(docid_freq_map) ) ); 					
		stmt_insert_into_postings.execute();

		TO-DO...
				
		conn.commit();
		ConnectionManager.returnConnection(conn);
		*/
    }    
    
    //It reads the postings table and creates the doc_word_freq_df_list file.
	//The code is complete.
	public static void fillDocWordFreqDfList (String DocWordFreqDfListFilename) throws Exception {
        //Creating the DocWordFreqDfList output file
    	BufferedWriter out = new BufferedWriter( new FileWriter(DocWordFreqDfListFilename) );
        
    	ResultSet rset = stmt_select_all_from_postings.executeQuery();
    	while (rset.next()) {
    		String word = rset.getString(1);
    		System.out.println(word);
    		Integer df = rset.getInt(2);
    		byte[] bytestream = rset.getBytes(3);
    		Map<Integer, Integer> docid_freq_map = Delta.deltalist2sortedmap( VB.VBDECODE(bytestream) );
    		for (Integer docid : docid_freq_map.keySet()) {
    			out.write(docid + " " + word + " " + docid_freq_map.get(docid) + " " + df + "\n");
    		}
    	}
        
    	out.close();
        ConnectionManager.returnConnection(conn);
	}
	
	//It fills the document magnitude table.
	public static void fillDocMagTable (String sortedDocWordDfListFilename, int N /*total number of docs*/) throws Exception {
        
        /*
    	TO-DO ...
    	
    			stmt_insert_into_docmag.setInt(1, docid);
    			stmt_insert_into_docmag.setDouble(2, Index.magnitude(word_freq_map, word_df_map, N));
    			stmt_insert_into_docmag.setInt(3, Index.maxf(word_freq_map) );
    			stmt_insert_into_docmag.execute();
    		}
		
		TO-DO ...
    	
    	
    	conn.commit();
    	ConnectionManager.returnConnection(conn);
    */
	}
	
	
	public static int maxf (Map<String, Integer> word_freq_map) {
		int maxf_res = 0;
		for (String word : word_freq_map.keySet()) {
			if (maxf_res < word_freq_map.get(word))
				maxf_res = word_freq_map.get(word);
		}
		return maxf_res;
	}	
	
	public static double tfidf (int f, int maxf, int df, int N) {
    	double tf = f/(double)maxf;
    	double idf = Math.log(N/(double)df);
    	return tf*idf; 		
	}
	
	
	public static double magnitude(Map<String, Integer> word_freq_map,
			Map<String, Integer> word_df_map, int N) throws Exception {

		double magsquare = 0;
		// TO-DO ...
		return Math.sqrt(magsquare);
	}
	
	
	public static double magnitude (Map<String, Integer> word_freq_map, int N) throws Exception 
	{
		Map<String, Integer> word_df_map = new HashMap<String, Integer>();
		for (String word : word_freq_map.keySet()) 
			word_df_map.put( word, Index.retrieveDf(word) );
		
		return Index.magnitude(word_freq_map, word_df_map, N);	
	}

	
	public static int retrieveDf (String word) throws Exception {
		int df = 0;
		stmt_select_df_from_postings.setString(1, word);
    	ResultSet rset = stmt_select_df_from_postings.executeQuery();
    	if (rset.next()) { 
    		df = rset.getInt(1); 
    	}
		return df;
	}
	
	
	public static DocInfo retrieveDocInfo (Integer docid) throws Exception {
	    DocInfo docinfo = new DocInfo();

	    stmt_select_docinfo_from_docmag.setInt(1, docid);
	    //TO-DO ...

		return docinfo;
	}
	
	
    public static void main(String[] args) throws Exception {
    	long startTime = System.currentTimeMillis();

    	fillWordList(
    			"html",			//directory with documents 
    			"stopwords.txt", //stop-words file
    			"word_doc_list.txt" //output text file of word-docid pairs.
    								//we will sort this file with respect to word (alphabeticaly), 
    								//then docid (numerically) 
    			);
    	//Mac and Linux users please remove ".exe" from sort
    	Runtime.getRuntime().exec("sort.exe -k1,1 -k2n,2 word_doc_list.txt -o sorted_word_doc_list.txt").waitFor();
    	fillWordTable("sorted_word_doc_list.txt");
    	
    	//This will output (docid, word, freq, df) tuples to a text file, doc_word_freq_df.txt, by reading the index table we just built 
    	fillDocWordFreqDfList("doc_word_freq_df.txt");
    	//Then, we sort this file with respect to docid (numerically), then word (alphabetically)
    	Runtime.getRuntime().exec("sort.exe -k1n,1 -k2,2 doc_word_freq_df.txt -o sorted_doc_word_freq_df.txt").waitFor();
    	//Finally, we fill in the document magnitude table
    	fillDocMagTable("sorted_doc_word_freq_df.txt", 19025);    	
    	
    	System.out.println("Time elapsed (sec) = " + (System.currentTimeMillis() - startTime) / 1000.0);
    }
}