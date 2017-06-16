/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package link;

/**
 *
 * @author in8RisKu
 */
public class Link {
    
    String Link;
    int statusCode;
    String parentLink;

    public String getLink() {
        return Link;
    }

    public void setLink(String Link) {
        this.Link = Link;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getParentLink() {
        return parentLink;
    }

    public void setParentLink(String parentLink) {
        this.parentLink = parentLink;
    }

    public Link(String Link, String parentLink, int statusCode) {
        this.Link = Link;
        this.statusCode = statusCode;
        this.parentLink = parentLink;
    }
    
    
}
