package com.greenshare.service.suggestion;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.greenshare.entity.Month;
import com.greenshare.entity.Suggestion;
import com.greenshare.entity.user.User;
import com.greenshare.entity.vegetable.Climate;
import com.greenshare.entity.vegetable.Flower;
import com.greenshare.entity.vegetable.Fruit;
import com.greenshare.entity.vegetable.Growth;
import com.greenshare.entity.vegetable.Soil;
import com.greenshare.entity.vegetable.Species;
import com.greenshare.helpers.IsHelper;
import com.greenshare.repository.ClimateRepository;
import com.greenshare.repository.FlowerRepository;
import com.greenshare.repository.FruitRepository;
import com.greenshare.repository.GrowthRepository;
import com.greenshare.repository.MonthRepository;
import com.greenshare.repository.SoilRepository;
import com.greenshare.repository.SpeciesRepository;
import com.greenshare.repository.SuggestionRepository;

/**
 * Implementation of SuggestionService interface
 * 
 * @author joao.silva
 */
@Service
public class SuggestionServiceImpl extends IsHelper implements SuggestionService {

	@Autowired
	SuggestionRepository suggestionRepository;

	@Autowired
	SpeciesRepository speciesRepository;

	@Autowired
	FruitRepository fruitRepository;

	@Autowired
	FlowerRepository flowerRepository;

	@Autowired
	MonthRepository monthRepository;

	@Autowired
	GrowthRepository growthRepository;

	@Autowired
	SoilRepository soilRepository;

	@Autowired
	ClimateRepository climateRepository;

	private static final int MAX_PAGE_SIZE = 100;

	@Override
	public ResponseEntity<?> delete(Long id) {
		if (isNotNull(id)) {
			Suggestion suggestionDB = suggestionRepository.findOne(id);
			if (isNotNull(suggestionDB) && suggestionDB.getUser().getId() == getCurrentUserId()) {
				if (!suggestionDB.getSpecies().getEnabled()) {
					suggestionRepository.delete(suggestionDB);
					return new ResponseEntity<String>("Sugestão desativada", HttpStatus.OK);
				}
				return new ResponseEntity<String>("Sugestão já está ativa e não pode ser deletada.",
						HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<String>("Sugestão não pertence ao usuário atual.", HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<String>("ID não pode ser nulo.", HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> enable(Long id) {
		if (isNotNull(id)) {
			Suggestion suggestionDB = suggestionRepository.findOne(id);
			if (isNotNull(suggestionDB)) {
				Species species = suggestionDB.getSpecies();
				if (isNotNull(species) && !species.getEnabled()) {
					species.enable();
					speciesRepository.save(suggestionDB.getSpecies());
					return new ResponseEntity<String>("Sugestão ativada", HttpStatus.OK);
				}
				return new ResponseEntity<String>("Sugestão já está ativa.", HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<String>("Sugestão não encontrada.", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<String>("ID não pode ser nulo.", HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> save(Suggestion suggestion) {
		if (isNotNull(suggestion) && isNotNull(suggestion.getSpecies())) {
			Species newSpecies = suggestion.getSpecies();
			Fruit newFruit = newSpecies.getFruit();
			Flower newFlower = newSpecies.getFlower();
			List<Soil> soilList = newSpecies.getSoils();
			List<Climate> climateList = newSpecies.getClimates();
			Growth growth = newSpecies.getGrowth();
			if (isNotNull(newFruit)) {
				List<Month> MonthListDB = monthRepository.findAllByNumberIn(newFruit.getMonthNumbers());
				if (newFruit.getMonthNumbers().size() != MonthListDB.size()) {
					return new ResponseEntity<String>("Meses de frutificação passados inválidos.",
							HttpStatus.BAD_REQUEST);
				}
				newFruit = new Fruit(newFruit.getFaunaConsumption(), newFruit.getHumanConsumption(), MonthListDB,
						newFruit.getDescription(), newFruit.getName());
			}
			if (isNotNull(newFlower)) {
				List<Month> MonthListDB = monthRepository.findAllByNumberIn(newFlower.getMonthNumbers());
				if (newFruit.getMonthNumbers().size() != MonthListDB.size()) {
					return new ResponseEntity<String>("Meses de floração passados inválidos.", HttpStatus.BAD_REQUEST);
				}
				newFlower = new Flower(newFlower.getIsAromatic(), newFlower.getDescription(), newFlower.getName(),
						newFlower.getSpecies(), newFlower.getColors(), MonthListDB);
			}
			if (isNotNull(growth)) {
				growth = growthRepository.findOne(growth.getId());
				if (isNull(growth)) {
					return new ResponseEntity<String>("Crescimento inválido.", HttpStatus.BAD_REQUEST);
				}
			}
			List<Long> idList = new ArrayList<Long>();
			soilList.stream().map(Soil::getId).forEach(idList::add);
			soilList = soilRepository.findAllById(idList);
			if (idList.size() != soilList.size()) {
				return new ResponseEntity<String>("Solos passados inválidos.", HttpStatus.BAD_REQUEST);
			}
			climateList.stream().map(Climate::getId).forEach(idList::add);
			climateList = climateRepository.findAllById(idList);
			if (idList.size() != climateList.size()) {
				return new ResponseEntity<String>("Climas passados inválidos.", HttpStatus.BAD_REQUEST);
			}
			newSpecies = new Species(newSpecies.getAttractBirds(), newSpecies.getDescription(),
					newSpecies.getCultivationGuide(), newSpecies.getIsMedicinal(), newSpecies.getAttractBees(),
					newSpecies.getScientificName(), newSpecies.getCommonName(), newSpecies.getIsOrnamental(),
					newSpecies.getAverageHeight(), newSpecies.getGrowth(), newSpecies.getRootDepth(),
					newSpecies.getClimates(), newSpecies.getSoils(), newFlower, newFruit);
			Suggestion newSuggestion = new Suggestion(getCurrentUser(), newSpecies);
			if (newSuggestion.isValid()) {
				newSuggestion = suggestionRepository.save(newSuggestion);
				return new ResponseEntity<Suggestion>(newSuggestion, HttpStatus.OK);
			}
			return new ResponseEntity<List<String>>(newSuggestion.getValidationErrors(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<String>("Sugestão e/ou espécie não pode ser nula.", HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> findByCurrentUser() {
		User currentUser = getCurrentUser();
		if (isNotNull(currentUser)) {
			Iterable<Suggestion> suggestionListDB = suggestionRepository.findByUser(getCurrentUser());
			return new ResponseEntity<Iterable<Suggestion>>(suggestionListDB, HttpStatus.OK);
		}
		return new ResponseEntity<String>("Nenhum usuário logado.", HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> findOne(Long id) {
		if (isNotNull(id)) {
			Suggestion suggestionDB = suggestionRepository.findOne(id);
			if (isNotNull(suggestionDB)) {
				return new ResponseEntity<Suggestion>(suggestionDB, HttpStatus.OK);
			}
			return new ResponseEntity<String>("Sugestão não encontrada.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>("ID não pode ser nulo.", HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> findAllByPage(Integer page, Integer size) {
		if (isNotNull(page) && isNotNull(size) && is(size).smallerOrEqual(MAX_PAGE_SIZE)) {
			Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "lastModificationDate"));
			Iterable<Suggestion> suggestionListDB = suggestionRepository.findAll(pageable);
			return new ResponseEntity<Iterable<Suggestion>>(suggestionListDB, HttpStatus.OK);
		}
		return new ResponseEntity<String>("Paginação inválida.", HttpStatus.BAD_REQUEST);
	}

}
