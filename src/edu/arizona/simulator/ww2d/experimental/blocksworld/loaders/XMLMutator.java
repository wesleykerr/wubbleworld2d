package edu.arizona.simulator.ww2d.experimental.blocksworld.loaders;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.arizona.simulator.ww2d.experimental.blocksworld.Params;

public class XMLMutator {
	HashMap<String, String> aliasTable = new HashMap<String, String>();
	private static String _bw_path = "edu/arizona/simulator/ww2d/experimental/blocksworld/";
	private static String _mutatorFile = _bw_path + "data/levels/mutator.txt";
	private static String _levelFile = _bw_path + "data/levels/Room-Blocksworld-objs-angle-test-simple.xml";
	
	public static void main(String[] args){
		XMLMutator me = new XMLMutator();
		me.mutate(_levelFile,_mutatorFile);
	}
	
	public LinkedList<Params> mutate(String levelFile, String mutatorFile) {
		URL url = this.getClass().getClassLoader().getResource(levelFile);

		SAXReader reader = new SAXReader();
		Document doc = null;
		HashMap<String, LinkedList<String>> params = new HashMap<String, LinkedList<String>>();

		try {
			doc = reader.read(url);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		Element root = doc.getRootElement();

		
		URL url2 = this.getClass().getClassLoader().getResource(mutatorFile);
		Scanner scan = null;
		try {
			scan = new Scanner(url2.openStream());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		while (scan.hasNext()) {
			String s = scan.next();
			if (s.equals("define")) {
				aliasTable.put(scan.next(), scan.next());
			} else {
				String line = s + " " + scan.nextLine();
				Scanner scanner = new Scanner(line);
				LinkedList<String> tmp = new LinkedList<String>();
				String key = "";
				while (scanner.hasNext()) {
					String k = scanner.next();
					if (k.equals("attr:")) {
						break;
					}
					key += k + " ";
				}

				key = key.trim();

				while (scanner.hasNext()) {
					tmp.add(key + " " + scanner.next());
				}

				params.put(key, tmp);
			}
		}
		
		LinkedList<LinkedList<String>> lists = new LinkedList<LinkedList<String>>();
		for(String key : params.keySet()){
			lists.add(params.get(key));
		}
		LinkedList<LinkedList<String>> combinations = allCombinations(lists, 0);
		
		LinkedList<Element> newFiles = new LinkedList<Element>();
		for(LinkedList<String> combination : combinations){
			newFiles.add(alterXML(combination,root));
		}
		
		LinkedList<String> filenames = new LinkedList<String>();
		Integer i = 0;
		File dir = new File("src/edu/arizona/simulator/ww2d/experimental/blocksworld/data/levels");
		for(Element el : newFiles){
			File outputs = null;
			try {
				//System.out.println(dir.getAbsolutePath());
				outputs = new File(dir.getPath() + "/lvlFile_" + i + ".xml");
				outputs.createNewFile();
				FileWriter fw = new FileWriter(outputs);
				fw.write(el.asXML());
				fw.close();
				filenames.add(outputs.getPath());
				//System.out.println(outputs.getPath());
				i++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for(File f : dir.listFiles()){
			System.out.println(f.getName());
		}
		
		LinkedList<Params> toReturn = new LinkedList<Params>();
		for(String filename : filenames){
			toReturn.add(new Params(filename,true,10000));
			toReturn.add(new Params(filename,false,20000));
		}
		
		aliasTable = new HashMap<String,String>();
		return toReturn;
	}

	// i is used for recursion, for the initial call this should be 0
	// From the interwebs
	private LinkedList<LinkedList<String>> allCombinations(LinkedList<LinkedList<String>> input, int i) {

		// stop condition
		if (i == input.size()) {
			// return a list with an empty list
			LinkedList<LinkedList<String>> result = new LinkedList<LinkedList<String>>();
			result.add(new LinkedList<String>());
			return result;
		}

		LinkedList<LinkedList<String>> result = new LinkedList<LinkedList<String>>();
		LinkedList<LinkedList<String>> recursive = allCombinations(input, i + 1); // recursive call

		// for each element of the first list of input
		for (int j = 0; j < input.get(i).size(); j++) {
			// add the element to all combinations obtained for the rest of the
			// lists
			for (int k = 0; k < recursive.size(); k++) {
				// copy a combination from recursive
				LinkedList<String> newList = new LinkedList<String>();
				for (String string : recursive.get(k)) {
					newList.add(string);
				}
				// add element of the first list
				newList.add(input.get(i).get(j));
				// add new combination to result
				result.add(newList);
			}
		}
		return result;
	}
	
	private Element alterXML(LinkedList<String> combination, Element root){
		Element newRoot = root.createCopy();
		for (String name : combination){
			Scanner scanner = new Scanner(name);
			Element el = newRoot.element(scanner.next());
			List<Element> els;
			while(scanner.hasNext()){
				String k = scanner.next();
				if(!scanner.hasNext()){
					String define = aliasTable.get(k);
					int i = define.indexOf('=');
					String attr = define.substring(0,i);
					String alias = define.substring(i+1);
					
					el.attribute(attr).setValue(alias);
				} else {
					els = el.elements(k);
					if(els.size() == 1){
						el = els.get(0);
					} else {
						k = scanner.next();
						for(Element check : els){
							if(check.attribute("name").getValue().equals(k)){
								el = check;
								break;
							}
						}
					}
					
				}
				
			}
		}
		return newRoot;
	}

}
