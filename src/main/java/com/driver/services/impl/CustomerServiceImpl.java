package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer= customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		Customer customer = customerRepository2.findById(customerId).get();
		List<Driver> driverList = driverRepository2.findAll();

		int driverId =Integer.MAX_VALUE;
		int bill =0;

		for(Driver driver:driverList){
			if(driver.getDriverId()<driverId && driver.getCab().isAvailable()==true){
				driverId =driver.getDriverId();
				bill = distanceInKm*driver.getCab().getPerKmRate();
			}
		}
		if(driverId == Integer.MAX_VALUE){
			throw new Exception("No cab available!");
		}
		Driver driver = driverRepository2.findById(driverId).get();
		Cab cab = driver.getCab();
		cab.setAvailable(false);
		TripBooking tripBooking = new TripBooking();

		tripBooking.setCustomer(customer);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setBill(bill);
		tripBooking.setDriver(driver);
		tripBooking.setStatus(TripStatus.CONFIRMED);

		List<TripBooking> tripBookingList = driver.getTripBookingList();
		tripBookingList.add(tripBooking);
		driver.setTripBookingList(tripBookingList);

		List<TripBooking> tripBookingList1 = customer.getTripBookingList();
		tripBookingList1.add(tripBooking);
		customer.setTripBookingList(tripBookingList1);

		driverRepository2.save(driver);
		//customerRepository2.save(customer);
		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		Customer customer = tripBooking.getCustomer();
		List<TripBooking> tripBookingList = customer.getTripBookingList();
		tripBookingList.remove(tripBooking);


		Driver driver =tripBooking.getDriver();
		List<TripBooking> tripBookingList1 = driver.getTripBookingList();
		tripBookingList1.remove(tripBooking);


		Cab cab = driver.getCab();
		cab.setAvailable(true);

		tripBooking.setStatus(TripStatus.CANCELED);
		customerRepository2.save(customer);
		driverRepository2.save(driver);
        tripBookingRepository2.delete(tripBooking);


	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		Customer customer = tripBooking.getCustomer();
		List<TripBooking> tripBookingList = customer.getTripBookingList();
		tripBookingList.remove(tripBooking);


		Driver driver =tripBooking.getDriver();
		List<TripBooking> tripBookingList1 = driver.getTripBookingList();
		tripBookingList1.remove(tripBooking);

		Cab cab = driver.getCab();
		cab.setAvailable(true);

		tripBooking.setStatus(TripStatus.COMPLETED);




		customerRepository2.save(customer);
		driverRepository2.save(driver);
		tripBookingRepository2.save(tripBooking);





	}
}
