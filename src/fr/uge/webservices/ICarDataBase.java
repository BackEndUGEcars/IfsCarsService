package fr.uge.webservices;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface ICarDataBase extends Remote {
	ICar getCar(Long id) throws RemoteException;

	boolean removeCar(Long id) throws RemoteException;

	boolean addCar(ICar t) throws RemoteException;

	Map<Long, ICar> getBuyableCar() throws RemoteException;

	String toJson() throws RemoteException;

	Map<Long, ICar> getAllCars() throws RemoteException;

	void init() throws IOException, ParseException, RemoteException;

	boolean rent(Long carId, long employeeId) throws RemoteException;

	long unrent(Long id) throws RemoteException;

	String getBuyableCarsJson() throws RemoteException;

	String getCarJson(long id) throws RemoteException;

	float getPriceOfCar(long id) throws RemoteException;

}
