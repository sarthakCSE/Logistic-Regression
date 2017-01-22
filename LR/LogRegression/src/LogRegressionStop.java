

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogRegressionStop {

    String file,path,folder;

    private LogRegressionStop(String arg, String arg0) {
        
    }

    public void setPath(String path) {
        this.path = path;
    }
    List<String> wordsList = new ArrayList<>();
    static List<String> stopWord = new ArrayList<>();
    boolean includeStopWords = false, testSet = false;
    Set<String> sW = new HashSet<>();
    ArrayList<HashMap <String, Integer>> wtList = new ArrayList<HashMap <String, Integer>>(1000);
    int index = 0,hamCount=0,spamCount=0;;
    HashMap comList = new HashMap();
    HashMap wtMap = new HashMap();
    HashMap tMap = new HashMap();
    HashMap pMap = new HashMap();
    HashMap dw = new HashMap();

    public void setDw() {
        dw.putAll(wtMap);
        dw.replaceAll((k,v) -> 0);
    }
    public static void main(String[] args) throws IOException {
        
        LogRegressionStop log = new LogRegressionStop(args[0],args[1]);
        File fin = new File("Database/stopword.txt");
        
        StopWords(fin);
        log.setPath(args[0]);
        log.readWordsFile();
        log.setPath(args[1]);
        log.readWordsFile();
        log.setDw();
        log.trainForLR(args[2],args[3]);
    }
    
    void readWordsFile() throws IOException{
        file = path;
        File f = new File(path);
        FilenameFilter textFilter;
        textFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        };

        File[] files = f.listFiles(textFilter);
        for (File file : files) {

            getWords(file.getPath());
        }
    }
    
    public static void StopWords(File fin) throws IOException
    {
    	FileInputStream fis = new FileInputStream(fin);   	 
    	String csvSplitBy = " ";
    	BufferedReader br = new BufferedReader(new InputStreamReader(fis));
     
    	String line = null;
    	while ((line = br.readLine()) != null) {
    		String[] newStr = line.split(csvSplitBy);
            stopWord.addAll(Arrays.asList(newStr)); 
    	}
     
    	br.close();

    }
    
    private void getWords(String path) throws FileNotFoundException, IOException {
        String splitBy = " ";
        for(String line : Files.readAllLines(Paths.get(path) , StandardCharsets.ISO_8859_1)){		
            for(String s : line.split(splitBy)){
                Pattern p = Pattern.compile("[^A-Za-z0-9]");
                Matcher m = p.matcher(s);
                boolean b = m.find();
                if (b == false){
                    if(file.contains("train"))
                        wordsList.add(s);
                    else {
                        wordsList.add(s);
                    }
                       
                }
            }
      }
        
        Iterator i = wordsList.listIterator();
        HashMap<String,Integer> tempMap = new HashMap<String, Integer>();
        while(i.hasNext()){
            String k = (String) i.next();
            if(tempMap.containsKey(k))
                tempMap.replace(k, tempMap.get(k)+1);
            else
                tempMap.put(k, 1);
            if(!testSet)
                wtMap.put(k, 0.1);        
        }
        
        wtList.ensureCapacity(1000);
        wtList.add(index, tempMap);
        pMap.put(index, 0.5);      
        index++;        
        comList.putAll(tempMap);
        wordsList.clear();
    
}
    
    public void trainForLR(String p1, String p2) throws IOException{
        int conv = 100;
        for(int i=0;i<conv;i++){
            calculateProbability(i);

            calculatedW();
            findWtMap(i);
        }
        
        testLR(p1, p2);
    }

    private void calculateProbability(int i) {
        int mark = 0;
        while(mark!=index){
            double val1 = sumn(mark,i);
            double val =  sgn(val1);
            pMap.replace(mark, val);
            mark++;
        }
    }
    
    double sumn(int index,int count) {
        double add = 0,num;
        Set s = wtMap.entrySet();
        Iterator i = s.iterator();
        HashMap<String, Integer> tmpData = (HashMap<String, Integer>) wtList.get(index);
        while(i.hasNext()){
            Map.Entry m = (Map.Entry) i.next();
            String k = (String) m.getKey();
            if(tmpData.containsKey(k))    
                num=tmpData.get(k);
            else 
                num = 0;
            double p = 0;
            if(count==0)
                p = (double)m.getValue()*num;
            else
                p = (double)m.getValue()*num;
            add+=p;
        }
        return add+1;
    }

    private Double sgn(double sumnt) {
     
        if(sumnt>=100)
            return 1.0;
        if(sumnt<-100)
            return 0.0;
        double ret = Math.exp(-sumnt)/(1.0+Math.exp(-sumnt));
        return ret;     
    
    }

    private void calculatedW() {
        dw.replaceAll((k,v)->0);
        Set s = wtMap.entrySet();
        Iterator it = s.iterator();
        while(it.hasNext()){
            int mark = 0;
            double thisdw = 0.0;
            Map.Entry m = (Map.Entry) it.next();
            String k = (String) m.getKey();
            while(mark!=index){
                HashMap<String, Integer> tempWtList = (HashMap<String, Integer>) wtList.get(mark);
                int iData = 0,nData = 1;
                if(tempWtList.containsKey(k)){
                    iData = tempWtList.get(k);
                }
                if(mark>=340)
                    nData = 0;
                double p = (double) pMap.get(mark);
                if (mark == 0){
                    thisdw = thisdw + (double)(iData*(nData-p));
                }
                else
                    thisdw = thisdw + (double)(iData*(nData-p));
                mark++;
            }
            dw.replace(k, thisdw);
        }
    }

    private void findWtMap(int track) {
        double n = 0.04, l = 0.001, thisWeight;
        Set s = wtMap.entrySet();
        Iterator it = s.iterator();
        while(it.hasNext()){
            Map.Entry m = (Map.Entry) it.next();
            String k = (String) m.getKey();
            if(track > 0)
                thisWeight = (double)m.getValue() + (double)n*((double)dw.get(k) - l*(double)m.getValue());
                
            else
                thisWeight = (double)m.getValue() + (double)n*((double)dw.get(k) - l*(double)m.getValue());
            wtMap.replace(k, thisWeight);
        }
    }

    private void testLR(String t1, String t2) throws IOException {
        testSet = true;
        index = 0;
        wtList.clear();
        pMap.clear();
        readAllTestFiles(t1);
        double accuracy = (hamCount/348d) *100;
        int spCounter = spamCount,hCounter = hamCount;

        
        readAllTestFiles(t2);
        accuracy = (spamCount - spCounter)/130d *100;

        accuracy = (hCounter+spamCount-spCounter)/478d * 100;
        System.out.println("Overall accuracy="+accuracy);
        
        wtList.clear();
        
    }

    private void readAllTestFiles(String folder) throws IOException {
        path = folder;
        File f = new File(folder);
        FilenameFilter textFilter;
        textFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        };

        File[] files = f.listFiles(textFilter);
        for (File file : files) {

            extractWordsFromFileTest(file.getPath());
        }
    }

    private void extractWordsFromFileTest(String path) throws IOException {
        String csvSplitBy = " ";
        for(String line : Files.readAllLines(Paths.get(path) , StandardCharsets.ISO_8859_1)){		
            for(String s : line.split(csvSplitBy)){
                Pattern p = Pattern.compile("[^A-Za-z0-9]");
                Matcher m = p.matcher(s);
                boolean b = m.find();
                if (b == false){
                    if(path.contains("train")){
                        wordsList.add(s);
                        if(stopWord.contains(s))
                        	wordsList.remove(s);
                        }
                    else {
                        wordsList.add(s);
                        if(stopWord.contains(s))
                        	wordsList.remove(s);
                    }
                       
                }
            }
      }
        Iterator i = wordsList.listIterator();
        HashMap<String,Integer> myMap1 = new HashMap<String, Integer>();
        while(i.hasNext()){
            String k = (String) i.next();
            if(myMap1.containsKey(k))
                myMap1.replace(k, myMap1.get(k)+1);
            else
                myMap1.put(k, 1);
            
        }
        index++;
        tMap.putAll(myMap1);
        Set s = wtMap.entrySet();
        double sum = 0.0,wt;
        int z = 0;
        Iterator it = s.iterator();
        while(it.hasNext()){
            Map.Entry m = (Map.Entry) it.next();
            if(tMap.containsKey(m.getKey()))
                z = (int)tMap.get(m.getKey());
            wt = (double)m.getValue();
            sum+=wt*z;
        }
        double sig = (double) sgn(sum+1);
        if(sig>=0.5)
            hamCount++;
        else
            spamCount++;
        tMap.clear();
        wordsList.clear();
    }

     
}
