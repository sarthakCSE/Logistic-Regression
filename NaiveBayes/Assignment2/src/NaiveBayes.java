import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaiveBayes {
    
    HashMap ham = new HashMap();
    HashMap spam = new HashMap();
    HashMap hamTest = new HashMap();
    HashMap spamTest = new HashMap();
    HashMap pH = new HashMap();
    HashMap pS = new HashMap();
    Set<String> HamWords = new HashSet<>();
    Set<String> SpamWords = new HashSet<>();
    Set<String> HamWordsTest = new HashSet<>();
    Set<String> SpamWordsTest = new HashSet<>();
    Set<String> words = new HashSet<>();
	List<String> mainString = new ArrayList<>();	
    String filesource;
    double probH = 0.73;
    double probS = 1 - probH;
    double probHDefault = 1, probSDefault = 1;
    int countHam =0, countSpam = 0;
    
    void getAllData(String folderPath) throws IOException{
        filesource = folderPath;
        File f = new File(folderPath);
        FilenameFilter wordFilter;
        
              
        wordFilter = new FilenameFilter() {
        	@Override
            public boolean accept(File dir, String name)
        	{
                return name.toLowerCase().endsWith(".txt");
            }
			
        };

        File[] files = f.listFiles(wordFilter);
        for (File file : files) 
		{
            getWords(file.getPath());
        }

    }
    
    private void getWords(String path) throws FileNotFoundException, IOException {
        String splitBy = " ";
        for(String line : Files.readAllLines(Paths.get(path) , StandardCharsets.ISO_8859_1)){		
            for(String s : line.split(splitBy)){
                Pattern p = Pattern.compile("[^A-Za-z0-9]");
                Matcher m = p.matcher(s);
                boolean b = m.find();
                if (b == false){
                    if(filesource.contains("train"))
                        mainString.add(s);
                    else {
                        mainString.add(s);
                    }
                       
                }
            }
      }

        if(filesource.contains("train")){
        makeWord(mainString);
        if(filesource.contains("ham"))
            HamCount(HamWords, "ham", mainString);
        else
            HamCount(SpamWords, "spam", mainString);
        }
        else {
            makeWord(mainString);
            if(filesource.contains("ham"))
                HamCountTest(HamWordsTest, "ham", mainString);
            else
                HamCountTest(SpamWordsTest, "spam", mainString);
            HamWordsTest.clear();
            SpamWordsTest.clear();
            mainString.clear();
        }
    }

    private void makeWord(List<String> mainString) {
        Iterator itr = mainString.listIterator();
        if(filesource.contains("train")){
            while(itr.hasNext()){
                if(filesource.contains("ham"))
                    HamWords.add((String) itr.next());
                else
                    SpamWords.add((String) itr.next());
            }
        }
        else {
            while(itr.hasNext()){
                if(filesource.contains("ham"))
                    HamWordsTest.add((String) itr.next());
                else
                    SpamWordsTest.add((String) itr.next());
            }
        }

    }
    

    void HamCount(Set<String> voc, String type, List<String> mainString) {
        Iterator itr = voc.iterator();
        if(type.equals("ham")){
        while(itr.hasNext()){
            ham.put((String) itr.next(), 0);
        }
        
        Iterator itr1 = mainString.listIterator();
        while(itr1.hasNext()){
            String key = (String) itr1.next();
            int value =  (int) ham.get(key);
            ham.put(key, value+1);           
            
        }
        }
        
        if(type.equals("spam")){
        while(itr.hasNext()){
            spam.put((String) itr.next(), 0);
        }
        Iterator itr1 = mainString.listIterator();
        while(itr1.hasNext()){
            String key = (String) itr1.next();
            int value = (int) spam.get(key);
            spam.put(key, value+1);
        }
        }       
        
    }
    
    void HamCountTest(Set<String> voc, String type, List<String> mainString) {
        Iterator itr = voc.iterator();
        hamTest.clear();
        spamTest.clear();
        if(type.equals("ham")){
        while(itr.hasNext()){
            hamTest.put((String) itr.next(),0);
        }
        
        Iterator itr1 = mainString.listIterator();
        while(itr1.hasNext()){
            String key = (String) itr1.next();
            int value =  (int) hamTest.get(key);
            hamTest.put(key, value+1);           
            
        }
        
        if(!(hamTest.containsKey("Subject")))
            hamTest.put("Subject", 1);
        else {
            int value = (int) hamTest.get("Subject");
            hamTest.put("Subject", value+1);
        }
        }
        
        if(type.equals("spam")){
        while(itr.hasNext()){
            spamTest.put((String) itr.next(), 0);
        }
        Iterator itr1 = mainString.listIterator();
        while(itr1.hasNext()){
            String key = (String) itr1.next();
            int v = (int) spamTest.get(key);
            spamTest.put(key, v+1);
        }
        if(!(spamTest.containsKey("Subject")))
            spamTest.put("Subject", 1);
        else {
            int v = (int) spamTest.get("Subject");
            spamTest.put("Subject", v+1);
        }
        }
        double probHam, probSp;
        if(type.equals("ham")){
            probHam = Math.log10(probH) + getHamProb(hamTest, "ham");    
            probSp = Math.log10(probS) + getHamProb(hamTest, "spam");  
            if(probHam > probSp)
                countHam++;           }
        else {
            probHam = Math.log10(probH) + getHamProb(spamTest, "ham");
            probSp = Math.log10(probS) + getHamProb(spamTest, "spam");
            if(probHam < probSp)
                countSpam++;     
        }
        
    }

    private void showMaps() {
        int v = (int) ham.get("subject");
        ham.put("subject", v+340);
        int v1 = (int) spam.get("subject");
        spam.put("subject", v1+123);
    }
    public void getNB(){
    double d2 = words.size(), d1 = 0;
    
    Set set = ham.entrySet();
    Iterator i = set.iterator();
    while(i.hasNext()){
        Map.Entry m = (Map.Entry) i.next();
        d1 = d1 + (int) m.getValue();
    }
    double d = d1 + d2;
    probHDefault = 1 / d;
    pH.putAll(ham);
    pH.replaceAll((k , v) -> ((((int) v) + 1) / d));
    d1 = 0;
    Set set1 = spam.entrySet();
    Iterator i2 = set1.iterator();
    while(i2.hasNext()){
        Map.Entry m1 = (Map.Entry) i2.next();
        d1 = d1 + (int) m1.getValue();
    }
    double c = d1 + d2;
    probSDefault = 1 / c;
    pS.putAll(spam);
    pS.replaceAll((k , v) -> ((((int) v) + 1) / c));
}

   
    private double getHamProb(HashMap test, String type) {
        double p = 0;
        Set set = test.entrySet();
        Iterator i = set.iterator();
        if(type.equals("ham")){
            while(i.hasNext()){
                Map.Entry m = (Map.Entry) i.next();
                if(pH.containsKey(m.getKey()))
                    p = p + (int)m.getValue() * Math.log10((double)pH.get(m.getKey()));
                else
                    p = p + (int)m.getValue() * Math.log10(probHDefault);
            }
        }
        else {
            while(i.hasNext()){
                Map.Entry m = (Map.Entry) i.next();
                if(pS.containsKey(m.getKey()))
                    p = p + (int)m.getValue() * Math.log10((double)pS.get(m.getKey()) );
                else
                    p = p + (int)m.getValue() * Math.log10(probSDefault);
            }
        }
        return p;
    }
    

    private void combineWords() 
	{
        words.addAll(HamWords);
        words.addAll(SpamWords);        
    }
	
    void prediction() throws IOException{
        mainString.clear();
        getAllData("Database\\test\\ham");
        getAllData("Database\\test\\spam");
        double AccuracyH = ((double)(countHam)/348)*100;
        double AccuracyS = ((double)(countSpam)/130)*100;
        double TotalAccuracy = ((double)(countHam+countSpam)/478)*100;
        System.out.println("Ham accuracy = "+AccuracyH+"\nSpam accuracy = "+AccuracyS+"\nOverall Accuracy = "+TotalAccuracy);
        
    }
    
	 public static void main(String[] args) throws IOException  
	{        
		String HamLoc = args[0];
		String SpamLoc = args[1];
        NaiveBayes NB = new NaiveBayes();
        NB.getAllData(HamLoc);
        NB.getAllData(SpamLoc);
        NB.combineWords();
        NB.getNB();
        NB.showMaps();
        NB.prediction();

    }
    
}
