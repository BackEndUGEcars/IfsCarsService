/**
 * BankService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package fr.uge.webservices;

public interface BankService extends java.rmi.Remote {
    public boolean receive(java.lang.String login, java.lang.String password, double amount) throws java.rmi.RemoteException;
    public boolean send(java.lang.String login, java.lang.String password, double amount) throws java.rmi.RemoteException;
    public boolean isAnAccount(java.lang.String login, java.lang.String password) throws java.rmi.RemoteException;
    public boolean amountAvailable(java.lang.String login, java.lang.String password, double amount) throws java.rmi.RemoteException;
}
