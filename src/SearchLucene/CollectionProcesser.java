package SearchLucene;

import Classes.Path;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JS on 4/24/16.
 *
 * Get the docon, title, keywords, abstracts and body
 */
public class CollectionProcesser {
    //you can add essential private methods or variables
    private FileInputStream fis = null;
    private BufferedReader reader = null;
    private String line = null;
    private Map<String, HashMap> doc = new HashMap<String,HashMap>();
    private List<File> filelist;
    private int filenum = 0;
    private int totalFileNum = 0;

    Pattern frontBodyPat = Pattern.compile(".*<front>(.*)</front>.*<body>(.*)</body>.*");
    Pattern idPat = Pattern.compile(".*<article-id pub-id-type=\"pmc\">(\\d+)</article-id>.*");
    Pattern titlePat = Pattern.compile(".*<article-title>(.+)</article-title>.*");
    Pattern yearPat = Pattern.compile(".*<pub-date pub-type=\".*\">.*<year>(.+)</year>.*</pub-date>.*");
//    Pattern allKeywordsPat = Pattern.compile(".*<kwd-group>(<title>.*</title>)*(.*)</kwd-group>.*");
    Pattern keywordsPat = Pattern.compile("<kwd>([^>]*)</kwd>");
    Pattern abstractPat = Pattern.compile(".*<abstract>(.+)</abstract>.*");

    // YOU SHOULD IMPLEMENT THIS METHOD
    public CollectionProcesser() throws IOException {
        // This constructor should open the file in Path.DataTextDir
        // and also should make preparation for function nextDocument()
        // Do not load the whole corpus into memory!!!

        System.out.println("acquiring file list");
        filelist = new ArrayList<File>();
        filelist = getFileList(Path.DataDir);
        totalFileNum = filelist.size();

        fis = new FileInputStream(filelist.get(filenum).getAbsoluteFile());
        reader = new BufferedReader(new InputStreamReader(fis));
        System.out.println("Total reading files:" + totalFileNum);
    }


    public List<File> getFileList(String strPath) {
        File dir = new File(strPath);
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) {
                    getFileList(files[i].getAbsolutePath());
                }
                else  {
                    String strFileName = files[i].getAbsolutePath();
                    // System.out.println("---" + strFileName);
                    String needFile = ".nxml";
                    if (strFileName.endsWith(needFile)) {
                        filelist.add(files[i]);
                    }
                }
            }

        }
        return filelist;
    }

    public void refreshreader() throws IOException {

        filenum++;
        if (filenum < totalFileNum) {
            fis.close();
            reader.close();
            fis = new FileInputStream(filelist.get(filenum).getAbsoluteFile());
            reader = new BufferedReader(new InputStreamReader(fis));
        } else {
            reader = null;
        }

    }

    // YOU SHOULD IMPLEMENT THIS METHOD
    public Map<String, HashMap> nextDocument() throws IOException {
        int flag = 1;
        while (flag == 1) {
            if ( reader != null) { // reach the last file

                StringBuilder docMain = new StringBuilder("");
                String docText = "";

                Matcher mDoc = null;
                String docFront = "";
                String docBody = "";

                Matcher mId = null;
                String id = "";

                Matcher mTitle = null;
                String title = "";

                Matcher mAbstract = null;
                String absText = "";

                Matcher mYear = null;
                String year = "";

                Matcher mKeywords = null;
                String keywords = "";

                Pattern bodyBegin = Pattern.compile("<body>");
                Pattern bodyEnd = Pattern.compile("</body>");

                doc.clear();
                line = reader.readLine();

                int mark = 1;
                while(line != null && mark == 1) {
                    if(bodyEnd.matcher(line).find()) {
                        mark = 0;
                    }
                    docMain.append(line);
                    line = reader.readLine();
                }
                // if the doc with no id, ignor it
                if (idPat.matcher(docMain).matches()) {
                    // get front
                    mDoc = frontBodyPat.matcher(docMain);
                    if(mDoc.matches()) {
                        docFront = mDoc.group(1);
                        docBody = mDoc.group(2);
                    }

                    mId = idPat.matcher(docFront);
                    if(mId.matches()) {
                        id = mId.group(1);
                    }

                    mTitle = titlePat.matcher(docFront);
                    if(mTitle.matches()) {
                        title = mTitle.group(1);
                    }

                    mAbstract = abstractPat.matcher(docFront);
                    if(mAbstract.matches()) {
                        absText = mAbstract.group(1);
                    }

                    mYear = yearPat.matcher(docFront);
                    if(mYear.matches()) {
                        year = mYear.group(1);
                    }

                    mKeywords = keywordsPat.matcher(docFront);
                    while (mKeywords.find()) {
                        keywords += " " + mKeywords.group(1);
                    }

//                    docText += title + " " + keywords + " " + absText;
                    docText += title + " " + keywords + " " + absText + " " + docBody;

                    HashMap<String, String> mulDocFeilds = new HashMap<>();
                    mulDocFeilds.put("TITLE", title);
                    mulDocFeilds.put("ABSTRACT", absText);
                    mulDocFeilds.put("YEAR", year);
                    mulDocFeilds.put("KEYWORDS", keywords);
                    mulDocFeilds.put("CONTENT", docText);

                    flag = 0;
                    doc.put(id,mulDocFeilds);

                }
                refreshreader();

            } else {
                flag = 0;
                doc = null;
            }
        }


        return doc;
    }



}
