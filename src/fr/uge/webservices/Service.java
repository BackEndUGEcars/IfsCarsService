package fr.uge.webservices;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import javax.xml.rpc.ServiceException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.currencysystem.webservices.currencyserver.CurncsrvReturnRate;
import com.currencysystem.webservices.currencyserver.CurrencyServerLocator;




public class Service {
	private final ICarDataBase cars; 
	private final BankService bank;
	
	private final Set<Long> cart = new HashSet<>();

	public Service() throws RemoteException, ServiceException, MalformedURLException, NotBoundException {
		cars = (ICarDataBase) Naming.lookup("rmi://localhost:1099/CarDataBase");
		bank = new BankServiceServiceLocator().getBankService();
	}
	
	
	@SuppressWarnings("unchecked")
	public String getBuyableCarsJson(String currency) throws RemoteException, ServiceException{
		if (currency == null || currency.equals("EUR") || currency.isBlank()) return cars.getBuyableCarsJson();
		var c = new CurrencyServerLocator().getCurrencyServerSoap();
		
		
		var possibleCurrencies = c.activeCurrencies("");
		if (!possibleCurrencies.contains(currency)) throw new IllegalArgumentException("Currency " + currency + "not suported");
		
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = null;
		var sj = new StringJoiner(", ", "[", "]");
		try {
			jsonObject = (JSONObject) parser.parse(cars.getBuyableCarsJson());
	        var cars = (JSONArray) jsonObject.get("cars");
	        var iterator = cars.iterator();
	        while (iterator.hasNext()) {
	            var s = iterator.next().toString();
	            var jo = (JSONObject) parser.parse(s);
	            float sp = ((Double) jo.get("sellPrice")).floatValue();
	            jo.put("sellPrice", (double) c.convert("", "EUR", currency, sp, true, "", CurncsrvReturnRate.curncsrvReturnRateNumber, "", ""));
	            float rp = ((Double) jo.get("rentPrice")).floatValue();
	            jo.put("rentPrice", (double) c.convert("", "EUR", currency, rp, true, "", CurncsrvReturnRate.curncsrvReturnRateNumber, "", ""));
	            
	            sj.add(jo.toJSONString());
	        }
	        jsonObject.put("cars", sj.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		return jsonObject.toJSONString();
		
		
	}
	
	public boolean buyCar(long carId, String login, String password) throws RemoteException, ServiceException {
		var car = cars.getCar(carId);
		if (car == null || !bank.isAnAccount(login, password) || !car.isSellable()) return false;
		var p = car.getSellPrice();
		if(bank.amountAvailable(login, password, p)) {
			bank.send(login, password, p);
			cars.removeCar(carId);
			removeCart(carId);
			return true;
		} else {
			return false;
		}
		
	}

	
	public String getActiveCurencies() throws ServiceException, RemoteException {
		var c = new CurrencyServerLocator().getCurrencyServerSoap();
		return c.activeCurrencies("");
	}
	
	public boolean addCart(long carId) throws RemoteException {
		var car = cars.getCar(carId);
		if (car == null || !car.isSellable()) return false;
		
		return cart.add(carId);
	}
	
	public boolean removeCart(long carId) {
		return cart.remove(carId);
	}
	
	public boolean inCart(long carId) {
		return cart.contains(carId);
	}
	
	public String cartToJson() throws RemoteException {
		var sj = new StringJoiner(", ", "[", "]");
		for (Long carId : cart) {
			var car = cars.getCar(carId);
			sj.add(car.toJson(carId));
		}
		
		return "{" + sj.toString() + "}";
	}
	
	

}
