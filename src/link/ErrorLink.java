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
public class ErrorLink {
    
    String errorLink;
    int statusCode;
    String parentLink;

    public String getErrorLink() {
        return errorLink;
    }

    public void setErrorLink(String errorLink) {
        this.errorLink = errorLink;
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

    public ErrorLink(String errorLink, String parentLink, int statusCode) {
        this.errorLink = errorLink;
        this.statusCode = statusCode;
        this.parentLink = parentLink;
    }
    
    
}
