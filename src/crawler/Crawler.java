/*
 * MIT License
 *
 * Copyright (c) 2017 Rishabh Kumar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package crawler;

import link.ErrorLink;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.HttpsURLConnection;
import link.Link;
import ui.ControlDesk;

/**
 *
 * @author Rishabh
 */
public class Crawler extends Thread{

    /**
     * @param args the command line arguments
     */
    String status_code_data = "";
    TreeSet<String> linkTree = new TreeSet<>();
    HashSet<String> parsedList = new HashSet<>();
    String sitelink;
    String host;
    String filter;
    String ignore;
    ArrayList<ErrorLink> errorUrlList = new ArrayList<>();
    ArrayList<Link> urlList = new ArrayList<>();
    ConcurrentHashMap<String, String> childToParentMap = new ConcurrentHashMap<>();
    public boolean isCancelled = false;
    
    public Crawler(String sitelink, String filter, String ignore) 
    {
        this.sitelink = sitelink;
        this.filter = filter;
        this.ignore = ignore;
        try
        {
            URL url = new URL(sitelink);
            this.host = url.getProtocol()+"://"+url.getHost();
        }
        catch(Exception e)
        {
            System.exit(1);
        }
    }
    
    public static void main(String[] args) 
    {
        new ControlDesk(new Crawler("https://github.com/RishabKumar/WebCrawler-Sapphire", "", ""));
    }
    
    public ArrayList<ErrorLink> getErrorUrlList()
    {
        return this.errorUrlList;
    }
    
    public ArrayList<ErrorLink> popAllFromErrorUrlList()
    {
        ArrayList<ErrorLink> list = this.errorUrlList;
        this.errorUrlList.clear();
        return list;
    }
    
    public ArrayList<Link> getUrlList()
    {
        ArrayList<Link> list = new ArrayList<>(urlList);
        this.urlList.clear();
        return list;
    }
    
    public void parse()
    {
        parse(sitelink);
    }
    
    public void parse(String link)
    {
        if(link == null || isCancelled)
        { 
            return;
        }
        else if(!parsedList.contains(link) && link.contains(filter))
        {
            System.out.println("Number of links to parse in tree: "+linkTree.size());
            System.out.println("Parsing link => "+link);
            String html = connectAndGetHtml(link, 1);
            linkTree.addAll(getLinks(link, html));
            parsedList.add(link);
        }
        parse(linkTree.pollFirst());
    }
    
    public String connectAndGetHtml(String link, int retry)
    {
        StringBuilder sb = new StringBuilder();
        int status_code = -1;
        HttpURLConnection conn = null;
        HttpsURLConnection conns = null;
        BufferedInputStream bis = null;
        try
        {
            URL url = new URL(link);
            if(url.getProtocol().equals("https"))
            {
                conns = (HttpsURLConnection) url.openConnection();
                conns.setDoInput(true);
                conns.setDoOutput(true);
                conns.setRequestMethod("GET");
                status_code = conns.getResponseCode();
            //    status_code_data += status_code+","+link+"\n";
                byte[] bytes = new byte[1];
                bis = new BufferedInputStream(conns.getInputStream());
                while(bis.read(bytes) > -1)
                {
                    sb.append(new String(bytes));
                }
                System.out.println(sb);
                bis.close();
                
            }
            else
            {
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("GET");
                status_code = conn.getResponseCode();
            //    status_code_data += status_code+","+link+"\n";
                byte[] bytes = new byte[1];
                bis = new BufferedInputStream(conn.getInputStream());
                while(bis.read(bytes) > -1)
                {
                    sb.append(new String(bytes));
                }
                bis.close();
            }
        }
        catch(MalformedURLException me)
        {
            me.printStackTrace();
            if(retry == 1)
            {
                connectAndGetHtml(link, 0);
            }
        }
        catch(IOException ie)
        {
            System.out.println(link+ " Link doesnot exist.");
            ie.printStackTrace();
            try
            {
                status_code = conn.getResponseCode();
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
            errorUrlList.add(new ErrorLink(link, childToParentMap.get(link), status_code));
        }
        finally
        {
            urlList.add(new Link(link, childToParentMap.get(link), status_code));
            return sb.toString();
        }
    }
    
    public ArrayList<String> getLinks(String link, String html)
    {
        ArrayList<String> arr = new ArrayList<>();
        int i = 0, j = 0;
        while(i >= 0 && j >= 0)
        {
            i = html.indexOf("href=\"")+6;
            j = html.indexOf("\"", i);
        //    System.out.println("i="+i+", j="+j);
            if(i > 5 && i < j)
            {
                String href = html.substring(i, j);
                html = html.replaceFirst("href", "");
                if(href.charAt(0) == '#')
                {
                    continue;
                }
                if(href.charAt(0) == '/')
                {
                    href = host+href;
                }
                if(!parsedList.contains(href) && href.contains(filter) && !href.contains(ignore))
                {
                    System.out.println("New Link => "+href);
                    childToParentMap.put(href, link);
                	arr.add(href);
                }
            }
            else
                break;
        }
   //     System.out.println("Number of new links found: "+arr.size());
        System.out.println("Total links parsed:"+parsedList.size());
        return arr;
    }
    
    @Deprecated
    public boolean writeToCSV()
    {
        if(status_code_data != null && status_code_data.length() > 0)
        {
            try 
            {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("webcrawler-report.csv"));
                oos.writeObject(status_code_data);
                oos.close();
                return true;
            } catch ( IOException e) 
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }
    
    public boolean writeToCSV(String data)
    {
        if(data != null && data.length() > 0)
        {
            try 
            {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("webcrawler-report.csv"));
                oos.writeObject(data);
                oos.close();
                return true;
            } catch ( IOException e) 
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }
    
    public void clearReportData()
    {
        status_code_data = "";
    }
}