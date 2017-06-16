/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crawler;

import link.ErrorLink;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
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
import java.util.zip.GZIPInputStream;
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
    String domain = "http://www.lego.com/en-us/speedchampions/";
    String filter = "en-us/speedchampions/";
    String ignore = "http://cache.lego.com";
    ArrayList<ErrorLink> errorUrlList = new ArrayList<>();
    ArrayList<Link> urlList = new ArrayList<>();
    ConcurrentHashMap<String, String> childToParentMap = new ConcurrentHashMap<>();
    public boolean isCancelled = false;
    
    public Crawler(String domain, String filter, String ignore) 
    {
        this.domain = domain;
        this.filter = filter;
        this.ignore = ignore;
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
  //      new Crawler("", "", "").connectAndGetHtml("https://wwwsecure.webqa.lego.com/services/comments/api/v1/comments?csrfToken=4ead6cf2-1107-4e6b-8301-d1edf4139565&cl=en-US&sl=en-US", 0);
        
  
        new ControlDesk(new Crawler("", "", ""));
        /*/
        obj.parse(obj.domain);

        for(ErrorLink err:obj.errorUrlList)
        {
        	System.out.println("====> "+err.getErrorLink()+" found on "+err.getParentLink()+"\n");
        }
        System.out.println("Total links parsed:"+obj.parsedList.size());
        obj.writeToCSV();
/*/
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
        parse(domain);
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
        GZIPInputStream zip = null;
        try
        {
            URL url = new URL(link);
            if(link.toLowerCase().startsWith("https"))
            {
                conns = (HttpsURLConnection) url.openConnection();
                conns.setDoInput(true);
                conns.setDoOutput(true);
                    conns.setRequestMethod("POST");
                status_code = conns.getResponseCode();
                status_code_data += status_code+","+link+"\n";
              
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
                status_code_data += status_code+","+link+"\n";
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
                    href = domain+href;
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
    
    public void writeToCSV()
    {
    	try 
    	{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("D:\\statusCode"));
			oos.writeObject(status_code_data);
			oos.close();
    	} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    
}