package com.creativepool.service;


import com.creativepool.constants.Errors;
import com.creativepool.entity.FreelancerTicketApplicants;
import com.creativepool.entity.Ticket;
import com.creativepool.entity.TicketStatus;
import com.creativepool.entity.UserType;
import com.creativepool.exception.BadRequestException;
import com.creativepool.exception.ResourceNotFoundException;
import com.creativepool.models.*;
import com.creativepool.repository.FreelancerRepository;
import com.creativepool.repository.FreelancerTicketApplicantsRepository;
import com.creativepool.repository.TicketRepository;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.creativepool.utils.Utils.getOrDefault;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    FreelancerRepository freelancerRepository;

    @Autowired
    FreelancerTicketApplicantsRepository freelancerTicketApplicantsRepository;

    @Autowired
    UploadService uploadService;

    public TicketResponseDTO createTicket(TicketDTO ticketDTO, List<MultipartFile> multipartFiles) throws IOException {
        List<String> uploadedUrls=new ArrayList<>();
        Ticket ticket = new Ticket();
        // Map DTO fields to entity
        ticket.setTitle(ticketDTO.getTitle());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setReporterName(ticketDTO.getReporterName());
        ticket.setPrice(ticketDTO.getPrice());
        ticket.setTicketDeadline(ticketDTO.getTicketDeadline());
        ticket.setUrl(ticketDTO.getUrl());
        ticket.setCreatedDate(new Date());
        ticket.setClientId(ticketDTO.getClientId());
        ticket.setTicketStatus(TicketStatus.OPEN); // or any default status
        ticket.setTicketComplexity(ticketDTO.getTicketComplexity());
        if(multipartFiles!=null && !multipartFiles.isEmpty()) {
            for (MultipartFile file : multipartFiles) {
                uploadService.uploadFile(file, uploadedUrls);
            }
        }
        ticket.setImages(String.join(",", uploadedUrls));
        Ticket savedTicket = ticketRepository.save(ticket);

        return mapToResponseDTO(savedTicket);
    }

    public List<TicketResponseDTO> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream().map(this::mapToResponseDTO).collect(Collectors.toList());

    }

    private TicketResponseDTO mapToResponseDTO(Ticket ticket) {
        TicketResponseDTO responseDTO = new TicketResponseDTO();
        // Map entity fields to DTO
        responseDTO.setTicketID(ticket.getTicketID());
        responseDTO.setTitle(ticket.getTitle());
        responseDTO.setDescription(ticket.getDescription());
        responseDTO.setReporterName(ticket.getReporterName());
        responseDTO.setPrice(ticket.getPrice());
        responseDTO.setTicketDeadline(ticket.getTicketDeadline());
        responseDTO.setUrl(ticket.getUrl());
        responseDTO.setTicketStatus(ticket.getTicketStatus());
        responseDTO.setFreelancerId(ticket.getFreelancerId());
        responseDTO.setClientId(ticket.getClientId());
        responseDTO.setImages(ticket.getImages());
        return responseDTO;
    }

    public TicketResponseDTO assignTicket(UUID ticketId, UUID freelancerId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new BadRequestException(String.format(Errors.E00004.getMessage(),ticketId)));

        Integer totalTicketsAssigned=freelancerRepository.getTotalTicketsAssigned(freelancerId);
        totalTicketsAssigned++;
        if(totalTicketsAssigned>5){
            throw new IllegalStateException(Errors.E00008.getMessage());
        }
        freelancerRepository.updateTotalTicketsAssigned(totalTicketsAssigned,freelancerId);
        ticket.setFreelancerId(freelancerId);
        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToResponseDTO(savedTicket);
    }


    public void applyForTicket(UUID freelancerId, UUID ticketId, UserType userType) {
        FreelancerTicketApplicants application = new FreelancerTicketApplicants();
        application.setFreelancerID(freelancerId);
        application.setTicketID(ticketId);
        application.setUserType(userType);
        freelancerTicketApplicantsRepository.save(application);
    }

    public void getTicketApplicants(UUID ticketId,UserType userType){




    }


    public void deleteTicket(UUID ticketId) {
        // Check if the ticket exists
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new BadRequestException(String.format(Errors.E00004.getMessage(), ticketId)));
        // Delete all applications related to the ticket
        freelancerTicketApplicantsRepository.deleteByTicketID(ticketId);
        // Delete the ticket
        ticketRepository.delete(ticket);
    }


    public PaginatedResponse<TicketSearchResponse> searchUser(TicketSearchRequest ticketSearchRequest) {



        Integer page = ticketSearchRequest.getPage();
        Integer size = ticketSearchRequest.getSize();

        if (page == null || size == null) {
            throw new BadRequestException(Errors.E00001.getMessage());
        }

        BigDecimal rating = (ticketSearchRequest.getRating() != null)
                ? BigDecimal.valueOf(ticketSearchRequest.getRating())
                : null;

        Integer ticketStatus = (ticketSearchRequest.getTicketStatus() != null)
                ? ticketSearchRequest.getTicketStatus().ordinal()
                : null;

        // Fetching and parsing price range
        BigDecimal[] priceRange = parsePriceRange(ticketSearchRequest.getPriceRange());
        BigDecimal minPrice = (priceRange[0] != null) ? priceRange[0] : null;
        BigDecimal maxPrice = (priceRange[1] != null) ? priceRange[1] : null;

        // Fetching other parameters
        String complexity = (ticketSearchRequest.getComplexity() != null && !ticketSearchRequest.getComplexity().isEmpty())
                ? ticketSearchRequest.getComplexity()
                : null;

        Date[] dateRange = parseDateRange(ticketSearchRequest.getDates());
        Date startDate = (dateRange[0] != null) ? dateRange[0] : null;
        Date endDate = (dateRange[1] != null) ? dateRange[1] : null;

        List<Object[]> result = ticketRepository.searchTickets(complexity, minPrice, maxPrice,ticketStatus,rating,startDate,endDate,page, size);
        System.out.println(result.get(0).length);
        List<TicketSearchResponse> responses=convertToResponse(result);
        long totalRowCount = (long)result.get(0)[17];
        boolean isLastPage = ((page + 1) * size >= totalRowCount);
        Integer totalPages = (int) Math.ceil((double) totalRowCount / size);


        return new PaginatedResponse<>(totalRowCount, responses, isLastPage, page + 1, totalPages);


    }

    private BigDecimal[] parsePriceRange(String priceRange) {
        if (priceRange == null) {
            return new BigDecimal[]{null, null};
        }

        String[] parts = priceRange.split("-");
        BigDecimal minPrice = parts.length > 0 ? new BigDecimal(parts[0]) : null;
        BigDecimal maxPrice = parts.length > 1 ? new BigDecimal(parts[1]) : null;

        return new BigDecimal[]{minPrice, maxPrice};
    }

    private Date[] parseDateRange(String dates) {

        SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        if (dates == null || dates.isEmpty()) {
            return new Date[] { null, null };
        }
        try {
            String[] parts = dates.split(" to ");
            String startDateString = parts[0].trim() + " 00:00:00.000";
            String endDateString = parts[1].trim() + " 23:59:59.999";
            Date startDate = DATE_TIME_FORMATTER.parse(startDateString);
            Date endDate = DATE_TIME_FORMATTER.parse(endDateString);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            endDate = calendar.getTime();

            return new Date[] { startDate, endDate };
        } catch (ParseException | ArrayIndexOutOfBoundsException e) {
            // Handle parsing error or invalid format
            return new Date[] { null, null };
        }
    }

    private List<TicketSearchResponse> convertToResponse(List<Object[]> result) {
        List<TicketSearchResponse> responses = new ArrayList<>();
        for (Object[] row : result) {
            TicketSearchResponse response = new TicketSearchResponse();
            response.setTicketID((UUID) row[0]);
            response.setTitle((String) row[1]);
            response.setDescription((String) row[2]);
            response.setReporterName((String) row[3]);
            response.setCreatedDate((Date) row[4]);
            response.setPrice(((BigDecimal) row[5]).doubleValue());
            response.setTicketDeadline((Date) row[6]);
            response.setImages((String) row[7]);
            response.setUrl((String) row[8]);
            response.setTicketStatus(TicketStatus.values()[(Integer) row[9]]);
            response.setFreelancerId((UUID) row[10]);
            response.setClientId((UUID) row[11]);
            response.setUsername((String) row[12]);
            response.setClientFirstName((String) row[13]);
            response.setClientLastName((String) row[14]);
            response.setRating(((BigDecimal) row[15]).doubleValue());
            response.setComplexity((String) row[16]);
            responses.add(response);
        }
        return responses;
    }

    public TicketResponseDTO editTicket(TicketDTO ticketDTO,List<MultipartFile> files) throws IOException {
        Ticket ticket = ticketRepository.findById(ticketDTO.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id " + ticketDTO.getTicketId()));

        // Get current images
        List<String> currentImages = new ArrayList<>(Arrays.asList(ticket.getImages().split(",")));

        // Delete specified images
        if (ticketDTO.getDeleteUrls() != null && !ticketDTO.getDeleteUrls().isEmpty()) {
            for (String url : ticketDTO.getDeleteUrls()) {
                uploadService.deleteFileUsingSignedUrl(url);
                currentImages.remove(url);
            }
        }

        // Add new images
        if (files != null && !files.isEmpty()) {
            List<String> newUploadedUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                uploadService.uploadFile(file, newUploadedUrls);
            }
            currentImages.addAll(newUploadedUrls);
        }

        // Update ticket details
        ticket.setTitle(getOrDefault(ticketDTO.getTitle(), ticket.getTitle()));
        ticket.setDescription(getOrDefault(ticketDTO.getDescription(), ticket.getDescription()));
        ticket.setReporterName(getOrDefault(ticketDTO.getReporterName(), ticket.getReporterName()));
        ticket.setPrice(getOrDefault(ticketDTO.getPrice(), ticket.getPrice()));
        ticket.setTicketDeadline(getOrDefault(ticketDTO.getTicketDeadline(), ticket.getTicketDeadline()));
        ticket.setUrl(getOrDefault(ticketDTO.getUrl(), ticket.getUrl()));
        ticket.setClientId(getOrDefault(ticketDTO.getClientId(), ticket.getClientId()));
        ticket.setTicketStatus(getOrDefault(ticketDTO.getTicketStatus(), ticket.getTicketStatus()));
        ticket.setTicketComplexity(getOrDefault(ticketDTO.getTicketComplexity(), ticket.getTicketComplexity()));
        ticket.setImages(currentImages.stream().collect(Collectors.joining(",")));

        Ticket updatedTicket = ticketRepository.save(ticket);
        return mapToResponseDTO(updatedTicket);
    }
}
