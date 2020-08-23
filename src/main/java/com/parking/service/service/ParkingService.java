package com.parking.service.service;

import com.parking.service.config.ApplicationProperties;
import com.parking.service.dto.DistanceMatrixDTO;
import com.parking.service.dto.ParkingDTO;
import com.parking.service.model.ParkingDetails;
import com.parking.service.repository.ParkingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.parking.service.utils.ParkingConstants.ALEKSANDRA_KOSTICA_PARKING;
import static com.parking.service.utils.ParkingConstants.A_KOSTIC_PARKING;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingService {

  private final RestTemplate restTemplate;
  private final ParkingRepository parkingRepository;
  private final ApplicationProperties applicationProperties;

  public List<ParkingDTO> getParkingSpaces(Double latitude, Double longitude) throws Exception {
    List<ParkingDTO> parkingSpaces = getFreeSpaces();
    List<ParkingDTO> newParkingSpaces = new ArrayList<>();
    List<ParkingDetails> parkingDetails = getParkingDetailsFromDB();
    for (ParkingDTO parkingDTO : parkingSpaces) {
      parkingDetails.stream()
          .filter(p -> p.getName().equals(parkingDTO.getName()))
          .findFirst()
          .map(
              p ->
                  new ParkingDTO(
                      parkingDTO.getName(),
                      parkingDTO.getFreeSpace(),
                      p.getPrice(),
                      isCriticalCapacity(parkingDTO.getFreeSpace(), p.getCapacity()),
                      ""))
          .ifPresent(newParkingSpaces::add);
    }

    DistanceMatrixDTO distances = getDistancesFromGoogleAPI(latitude, longitude, newParkingSpaces);
    return setDistancesToParking(newParkingSpaces, distances);
  }

  private List<ParkingDetails> getParkingDetails() throws Exception {
    List<ParkingDTO> parkingSpaces = getFreeSpaces();
    List<ParkingDetails> parkingDetailsList = new ArrayList<>();
    Document doc = Jsoup.connect("https://parking-servis.co.rs/garaze-i-parkiralista/").get();
    for (ParkingDTO parkingSpace : parkingSpaces) {
      ParkingDetails parkingDetails = new ParkingDetails();
      parkingDetails.setName(parkingSpace.getName());
      String parkingPricePage = doc.getElementsMatchingText(parkingSpace.getName()).attr("href");
      if (StringUtils.isNotEmpty(parkingPricePage)) {
        Document parkingPrice = Jsoup.connect(parkingPricePage).get();
        parkingDetails.setPrice(parkingPrice.getElementsByTag("td").get(1).text().split("\\.")[0]);
        parkingDetails.setCapacity(
            parkingPrice
                .getElementsContainingOwnText("Број паркинг места за аутомобиле")
                .get(0)
                .text()
                .split(":")[1]
                .trim());
        parkingDetailsList.add(parkingDetails);
      }
    }
    return parkingDetailsList;
  }

  public void saveParkings() throws Exception {
    getParkingDetails().forEach(parkingRepository::save);
  }

  private List<ParkingDTO> getFreeSpaces() throws Exception {
    List<ParkingDTO> parkingList = new ArrayList<>();
    Document doc = Jsoup.connect("https://parking-servis.co.rs/gde-mogu-da-parkiram/").get();
    Element table = doc.getElementsByClass("table-1").get(0);
    Elements tr = table.select("tr");
    Elements tdElements = tr.select("td");
    for (Element td : tdElements) {
      ParkingDTO parkingDTO = new ParkingDTO();
      parkingDTO.setName(td.selectFirst("h5").text());
      parkingDTO.setFreeSpace(td.selectFirst("h2").text());
      fixNameForPricing(parkingDTO);
      parkingList.add(parkingDTO);
    }
    return parkingList;
  }

  private void fixNameForPricing(ParkingDTO parkingDTO) {
    if (parkingDTO.getName().equals(A_KOSTIC_PARKING)) {
      parkingDTO.setName(ALEKSANDRA_KOSTICA_PARKING);
    }
  }

  private DistanceMatrixDTO getDistancesFromGoogleAPI(
      Double latitude, Double longitude, List<ParkingDTO> parkings) {
    String url = applicationProperties.getGoogle().getUrl();
    url =
        String.format(
            url,
            getOrigin(latitude, longitude),
            getDestinations(parkings),
            applicationProperties.getGoogle().getApiKey());

    return restTemplate.getForEntity(url, DistanceMatrixDTO.class).getBody();
  }

  private String getOrigin(double latitude, double longitude) {
    return StringUtils.join(latitude, ",", longitude);
  }

  private String getDestinations(List<ParkingDTO> parkings) {
    String destinations = "";
    for (ParkingDTO parking : parkings) {
      destinations =
          StringUtils.join(
              destinations,
              StringUtils.replace(parking.getName(), " ", "+"),
              "+Garaza+Beograd",
              "|");
    }
    return destinations;
  }

  private List<ParkingDTO> setDistancesToParking(
      List<ParkingDTO> parkings, DistanceMatrixDTO distances) {
    int i = 0;
    List<ParkingDTO> newParkings = new ArrayList<>();
    for (ParkingDTO parking : parkings) {
      ParkingDTO newParking = new ParkingDTO(parking);
      newParking.setDistance(
          distances.getRows().get(0).getElements().get(i).getDistance().getText());
      i++;
      newParkings.add(newParking);
    }
    return newParkings;
  }

  private boolean isCriticalCapacity(String freeSpace, String capacity) {
    return Double.parseDouble(freeSpace) / Double.parseDouble(capacity) * 100 < 10;
  }

  private List<ParkingDetails> getParkingDetailsFromDB() {
    return (List<ParkingDetails>) parkingRepository.findAll();
  }
}
