/**
 * BankServiceService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package fr.uge.webservices;

public interface BankServiceService extends javax.xml.rpc.Service {
    public java.lang.String getBankServiceAddress();

    public fr.uge.webservices.BankService getBankService() throws javax.xml.rpc.ServiceException;

    public fr.uge.webservices.BankService getBankService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
