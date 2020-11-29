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
	
	private final Set<Long> basket = new HashSet<>();

	public Service() throws RemoteException, ServiceException, MalformedURLException, NotBoundException {
		cars = (ICarDataBase) Naming.lookup("rmi://localhost:1099/CarDataBase");
		bank = new BankServiceServiceLocator().getBankService();
	}
	
	/**
	 * get all buyable cars with the price convert to currency 
	 * @param currency string to convert price ex: USD 
	 * @return jsonString
	 * @throws RemoteException
	 * @throws ServiceException
	 */
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
	        jsonObject.put("cars", (JSONArray) parser.parse(sj.toString()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		var tmpRes = jsonObject.toJSONString();
		tmpRes = tmpRes.replaceAll("\\\\", "");
		return tmpRes;
		
		
	}
	
	/**
	 * 
	 * @param carId
	 * @param login
	 * @param password
	 * @return 0 amount not available -1 carId do not exist -2 wrong login, password -3 car not sallable  
	 * @throws RemoteException
	 * @throws ServiceException
	 */
	public int buyCar(long carId, String login, String password) throws RemoteException, ServiceException {
		var car = cars.getCar(carId);
		
		if (car == null) {
			return -1;
		}
		if (!bank.isAnAccount(login, password)) {
			return -2;
		}
		if (!car.isSellable()) {
			return -3;
		}
		
		
		var p = car.getSellPrice();

		if(bank.amountAvailable(login, password, p)) {
			bank.send(login, password, p);
			cars.removeCar(carId);
			removeBasket(carId);
			return 1;
		}else {
			return 0;
		}
		
	}

	/**
	 * Get all active currencies 
	 * @return string of active currencies separate buy ;
	 * @throws ServiceException
	 * @throws RemoteException
	 */
	public String getActiveCurrencies() throws ServiceException, RemoteException {
		var c = new CurrencyServerLocator().getCurrencyServerSoap();
		return c.activeCurrencies("");
	}
	
	/**
	 * adding to basket
	 * @param carId
	 * @return true if carId is added to basket
	 * @throws RemoteException
	 */
	public boolean addBasket(long carId) throws RemoteException {
		var car = cars.getCar(carId);
		if (car == null || !car.isSellable() || car.isRented() != -1) return false;
		
		return basket.add(carId);
	}
	
	/**
	 * removing from basket
	 * @param carId 
	 * @return true if carId is removed from basket
	 */
	public boolean removeBasket(long carId) {
		return basket.remove(carId);
	}
	
	/**
	 * Check if carId is in the basket
	 * @param carId
	 * @return return if carId is in the basket
	 */
	public boolean isInBasket(long carId) {
		return basket.contains(carId);
	}
	
	
	/**
	 * 
	 * @return the content of the basket as a JsonString
	 * @throws RemoteException
	 */
	public String basketToJson() throws RemoteException {
		var sj = new StringJoiner(", ", "[", "]");
		for (Long carId : basket) {
			var car = cars.getCar(carId);
			sj.add(car.toJson(carId));
		}
		
		return "{ \"cars\" : " + sj.toString() + "}";
	}
	

}
