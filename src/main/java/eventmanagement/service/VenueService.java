package eventmanagement.service;

import eventmanagement.dto.ProvinceResponse;
import eventmanagement.dto.VenueResponse;
import eventmanagement.dto.WardResponse;
import eventmanagement.entity.Venue;
import eventmanagement.entity.Ward;
import eventmanagement.repository.VenueRepository;
import eventmanagement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    public List<VenueResponse> getAllVenues() {
        List<Venue> venues = venueRepository.findAll();
        return venues.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public VenueResponse getVenueById(String id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa điểm với ID: " + id));
        return mapToResponse(venue);
    }

    private VenueResponse mapToResponse(Venue venue) {
        VenueResponse response = new VenueResponse();
        response.setId(venue.getVenueId()); // Khớp với getVenueId() trong Entity Venue
        response.setVenueName(venue.getVenueName());
        response.setAddress(venue.getAddress());
        response.setCapacity(venue.getCapacity());

        // Map quan hệ Ward và Province chuẩn OOP
        if (venue.getWard() != null) {
            Ward ward = venue.getWard();
            WardResponse wardResponse = new WardResponse();

            // Giả định Ward entity có getWardId() và getWardName()
            wardResponse.setId(ward.getWardId());
            wardResponse.setWardName(ward.getWardName());

            if (ward.getProvince() != null) {
                ProvinceResponse provinceResponse = new ProvinceResponse();
                // Giả định Province entity có getId() / getProvinceId() và getProvinceName()
                provinceResponse.setId(ward.getProvince().getProvinceId());
                provinceResponse.setProvinceName(ward.getProvince().getProvinceName());
                wardResponse.setProvince(provinceResponse);
            }

            response.setWard(wardResponse);
        }

        return response;
    }
}