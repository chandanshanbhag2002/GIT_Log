/*
 * Author:Chandan Shanbhag
 * 
 * 
 */
package gitutility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Set;

import org.eclipse.jgit.api.errors.GitAPIException;

/**
 *
 * @author chandan.shanbhag
 */

public class ParseVcsLog {
	static List<String> svnJiraList = new ArrayList();
	public  List getJiraList(String log) throws IOException,
			GitAPIException {
		svnJiraList.clear();
                String s=log;
		//File inputFile = new File("GitLog"+GetProperties.rand+".txt");
		//File jiraList = new File("JiraList"+GetProperties.rand+".txt");
		BufferedReader reader = new BufferedReader(new StringReader(s));
		//BufferedWriter writer = new BufferedWriter(new FileWriter(jiraList));

		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			String trimmedLine = currentLine.trim();
			for (String st : trimmedLine.split(" ")) {
				if (st.toLowerCase().startsWith("jira")) {
					String jira = st.toLowerCase().replaceAll("jira", "");
					if (jira.length() > 4) {
						checkJira(jira);
					} else {
						Scanner scan = new Scanner(trimmedLine);
						while (scan.hasNext()) {
							String temp = scan.next();
							temp = temp.replaceAll("[^a-zA-Z0-9-]", "");
							if ((temp.toLowerCase().equals("jira"))
									&& (scan.hasNext())) {
								String jira1 = scan.next();
								checkJira(jira1.toLowerCase());
							}
						}
					}
				}
			}
		}
		Set<String> hs = new HashSet();
		hs.addAll(svnJiraList);
		svnJiraList.clear();
		svnJiraList.addAll(hs);
		Collections.sort(svnJiraList);
		System.out.println("Array List Values:");
		
		
                System.out.println(svnJiraList);
		return svnJiraList;
	}
	public static void checkJiraList(String svnUrl,
			ArrayList<String> userJiraList, long startVersion, long endVersion)
			throws IOException {
		Collections.sort(userJiraList);

		ListIterator<String> iterator = userJiraList.listIterator();
		while (iterator.hasNext()) {
			iterator.set(iterator.next().toLowerCase());
		}
		System.out.println("user jira List is " + userJiraList);

		// getJiraList(svnUrl, startVersion, endVersion);
		System.out.println("svn jira List is " + svnJiraList);

		ArrayList<String> commonJira = new ArrayList();
		for (String temp : userJiraList) {
			System.out.println("Temp value is : " + temp);
			if (svnJiraList.contains(temp)) {
				commonJira.add(temp);
			}
		}
		for (String temp : commonJira) {
			userJiraList.remove(temp);
			svnJiraList.remove(temp);
		}
		FileWriter writer = new FileWriter("checkJira.txt");

		String startVer = startVersion == 0L ? "BASE" : String
				.valueOf(startVersion);

		String endVer = endVersion == -1L ? "HEAD" : String.valueOf(endVersion);

		Date date = new Date();
		Timestamp time = new Timestamp(date.getTime());
		writer.write("Generated on : " + time
				+ System.getProperty("line.separator")
				+ System.getProperty("line.separator"));
		if (commonJira.size() > 0) {
			writer.write("The following jira ids are present in [" + svnUrl
					+ "] from r" + startVer + " to r" + endVer
					+ System.getProperty("line.separator"));
			for (int i = 0; i < commonJira.size(); i++) {
				writer.write(commonJira.get(i)
						+ System.getProperty("line.separator"));
			}
		} else {
			writer.write("No Match Found"
					+ System.getProperty("line.separator"));
		}
		if (userJiraList.size() > 0) {
			writer.write(System.getProperty("line.separator"));
			writer.write("The following jira ids are not present in [" + svnUrl
					+ "] from r" + startVer + " to r" + endVer
					+ System.getProperty("line.separator"));
			for (int i = 0; i < userJiraList.size(); i++) {
				writer.write(userJiraList.get(i)
						+ System.getProperty("line.separator"));
			}
		}
		if (svnJiraList.size() > 0) {
			writer.write(System.getProperty("line.separator"));
			writer.write("The following jira ids are available in [" + svnUrl
					+ "] from r" + startVer + " to r" + endVer
					+ " apart from the list provided"
					+ System.getProperty("line.separator"));
			for (int i = 0; i < svnJiraList.size(); i++) {
				writer.write(svnJiraList.get(i)
						+ System.getProperty("line.separator"));
			}
		}
		writer.close();
	}

	public static boolean isNumber(String string) {
		try {
			Long.parseLong(string);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static void checkJira(String jira) {
		if (jira.contains(",")) {
			String[] parts = jira.split("\\,");
			for (int i = 0; i < parts.length; i++) {
				parts[i] = parts[i].replaceAll("[^a-zA-Z0-9-]", "");
				svnJiraList.add(parts[i]);
				System.out.println(parts[i]);
			}
		} else {
			jira = jira.replaceAll("[^a-zA-Z0-9-]", "");
			if (jira.equals("pqa-158") || jira.equals("pqa-159")) {
				System.out.println("Discarding PQA-157");
			} else {
				svnJiraList.add(jira);
				System.out.println(jira);
			}

		}
	}
        
       
}
