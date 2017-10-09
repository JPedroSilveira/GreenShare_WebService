package com.seedshare.service.city;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.seedshare.entity.City;
import com.seedshare.entity.Country;
import com.seedshare.entity.State;
import com.seedshare.helpers.IsHelper;
import com.seedshare.repository.CityRepository;
import com.seedshare.repository.CountryRepository;
import com.seedshare.repository.StateRepository;

/**
 * Service Class for City
 * 
 * @author joao.silva
 */
@Service
public class CityServiceImpl extends IsHelper implements CityService {

	@Autowired
	CityRepository cityRepository;

	@Autowired
	StateRepository stateRepository;

	@Autowired
	CountryRepository countryRepository;

	@Override
	public ResponseEntity<?> save(City city) {
		if (isNotNull(city)) {
			City newCity = new City(city.getName(), city.getState());
			if (newCity.isValid()) {
				newCity = cityRepository.save(newCity);
				return new ResponseEntity<City>(newCity, HttpStatus.OK);
			}
		}
		return new ResponseEntity<String>("Cidade inválida.", HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> delete(Long id) {
		if (isNotNull(id)) {
			cityRepository.delete(id);
			return new ResponseEntity<String>("Cidade deletada.", HttpStatus.OK);
		}
		return new ResponseEntity<String>("ID não pode ser nulo.", HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> findOne(Long id) {
		if (isNotNull(id)) {
			City cityDB = cityRepository.findOne(id);
			if (isNotNull(cityDB)) {
				return new ResponseEntity<City>(cityDB, HttpStatus.OK);
			}
			return new ResponseEntity<String>("Cidade não encontrada.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>("ID não pode ser nulo.", HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> findByState(Long id) {
		if (isNotNull(id)) {
			State stateDB = stateRepository.findOne(id);
			if (isNotNull(stateDB)) {
				Iterable<City> citiesDB = cityRepository.findAllByState(stateDB);
				return new ResponseEntity<Iterable<City>>(citiesDB, HttpStatus.OK);
			}
			return new ResponseEntity<String>("Estado não encontrado.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>("Estado não pode ser nulo.", HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> findByCountry(Long id) {
		if (isNotNull(id)) {
			Country countryDB = countryRepository.findOne(id);
			if (isNotNull(countryDB)) {
				Iterable<City> citiesDB = cityRepository.findAllByCountry(countryDB);
				return new ResponseEntity<Iterable<City>>(citiesDB, HttpStatus.OK);
			}
			return new ResponseEntity<String>("Pais não encontrado.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>("Pais não pode ser nulo.", HttpStatus.BAD_REQUEST);
	}

}