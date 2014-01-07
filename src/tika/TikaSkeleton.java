package tika;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class TikaSkeleton {

	List<String> keywords;
	PrintWriter logfile;
	int num_keywords, num_files, num_fileswithkeywords;
	Map<String, Integer> keyword_counts;
	Date timestamp;

	/**
	 * constructor DO NOT MODIFY
	 */
	public TikaSkeleton() {
		keywords = new ArrayList<String>();
		num_keywords = 0;
		num_files = 0;
		num_fileswithkeywords = 0;
		keyword_counts = new HashMap<String, Integer>();
		timestamp = new Date();
		try {
			logfile = new PrintWriter("log.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * destructor DO NOT MODIFY
	 */
	protected void finalize() throws Throwable {
		try {
			logfile.close();
		} finally {
			super.finalize();
		}
	}

	/**
	 * main() function instantiate class and execute DO NOT MODIFY
	 */
	public static void main(String[] args) {
		System.out.println("Processing...");
		long startTime = System.currentTimeMillis();
		TikaSkeleton instance = new TikaSkeleton();
		instance.run();
		long endTime = System.currentTimeMillis();
		float sec = (endTime - startTime) / 1000f;
		System.out.println("Documents processed. Took " + sec + "sec.");
	}

	/**
	 * execute the program DO NOT MODIFY
	 */
	private void run() {

		// Open input file and read keywords
		try {
			BufferedReader keyword_reader = new BufferedReader(new FileReader(
					"queries.txt"));
			String str;
			while ((str = keyword_reader.readLine()) != null) {
				keywords.add(str);
				num_keywords++;
				keyword_counts.put(str, 0);
			}
			keyword_reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Open all pdf files, process each one
		File pdfdir = new File("./tikadataset");
		File[] pdfs = pdfdir.listFiles(new PDFFilenameFilter());
		for (File pdf : pdfs) {
			num_files++;
			processfile(pdf);
		}

		// Print output file
		try {
			PrintWriter outfile = new PrintWriter("output.txt");
			outfile.print("Keyword(s) used: ");
			if (num_keywords > 0)
				outfile.print(keywords.get(0));
			for (int i = 1; i < num_keywords; i++)
				outfile.print(", " + keywords.get(i));
			outfile.println();
			outfile.println("No of files processed: " + num_files);
			outfile.println("No of files containing keyword(s): "
					+ num_fileswithkeywords);
			outfile.println();
			outfile.println("No of occurrences of each keyword:");
			outfile.println("----------------------------------");
			for (int i = 0; i < num_keywords; i++) {
				String keyword = keywords.get(i);
				outfile.println("\t" + keyword + ": "
						+ keyword_counts.get(keyword));
			}
			outfile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process a single file
	 * 
	 * Here, you need to: - use Tika to extract text contents from the file -
	 * search the extracted text for the given keywords - update
	 * num_fileswithkeywords and keyword_counts as needed - update log file as
	 * needed
	 * 
	 * @param f
	 *            File to be processed
	 */
	private void processfile(File f) {

		/***** YOUR CODE GOES HERE *****/
		// to update the log file with information on the language, author,
		// type, and last modification date implement
		updatelogMetaData(f);

		Tika tika = new Tika();
		String fileContent = null;

		try {
			fileContent = tika.parseToString(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Boolean contains = false;
		// to update the log file with a search hit, use:
		for (String keyword : keywords) {
			Pattern p = Pattern.compile(keyword,Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(fileContent);
			int count = 0;
			while (m.find()) {
				count += 1;
			}
			keyword_counts.put(keyword, keyword_counts.get(keyword)+ count);
			if (count > 0){
				updatelogHit(keyword, f.getName());
				contains = true;
			}
		}
		
		if(contains) num_fileswithkeywords++;
	}

	private void updatelogMetaData(File file) {
		logfile.println("\n\n -- " + " data on file \"" + file.getName() + "\"");
		/***** YOUR CODE GOES HERE *****/

		Tika tika = new Tika();
		String fileContent = null;
		try {
			fileContent = tika.parseToString(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LanguageIdentifier languageIdentifier = new LanguageIdentifier(
				fileContent);
		logfile.println("Detected lang: " + languageIdentifier.getLanguage());

		Metadata metadata = new Metadata();
		ParseContext context = new ParseContext();
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			tika.parse(inputStream, metadata);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String author = "";
		String[] authors = metadata.getValues("Author");
		for (String val : authors) {
			author += val;
		}
		String lastModified = "";
		String[] lastmodifs = metadata.getValues("Last-Modified");
		for (String val : lastmodifs) {
			lastModified += val;
		}

		String fileType = "";
		try {
			fileType = tika.detect(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logfile.println("Author: " + author);
		logfile.println("File type: " + fileType);
		logfile.println("Last modified: " + lastModified);

		logfile.println();
		logfile.flush();
	}

	/**
	 * Update the log file with search hit Appends a log entry with the system
	 * timestamp, keyword found, and filename of PDF file containing the keyword
	 * DO NOT MODIFY
	 */
	private void updatelogHit(String keyword, String filename) {
		timestamp.setTime(System.currentTimeMillis());
		logfile.println(timestamp + " -- \"" + keyword + "\" found in file \""
				+ filename + "\"");
		logfile.flush();
	}

	/**
	 * Filename filter that accepts only *.pdf DO NOT MODIFY
	 */
	static class PDFFilenameFilter implements FilenameFilter {
		private Pattern p = Pattern.compile(".*\\.pdf",
				Pattern.CASE_INSENSITIVE);

		public boolean accept(File dir, String name) {
			Matcher m = p.matcher(name);
			return m.matches();
		}
	}
}
