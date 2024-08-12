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
import com.creativepool.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    CloudStorageService cloudStorageService;

    public TicketResponseDTO createTicket(TicketDTO ticketDTO, List<MultipartFile> multipartFiles) throws IOException {
        List<String> filenames=new ArrayList<>();
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
                cloudStorageService.uploadFile(file, filenames);
            }
        }
        ticket.setFilename(String.join(",", filenames));
        Ticket savedTicket = ticketRepository.save(ticket);

        return mapToResponseDTO(savedTicket);
    }



    public List<TicketResponseDTO> getAllTickets()  {
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream()
                .map(ticket -> {
                    try {
                        return mapToResponseDTO(ticket);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

    }

    private TicketResponseDTO mapToResponseDTO(Ticket ticket) throws IOException {
        TicketResponseDTO responseDTO = new TicketResponseDTO();
        List<String> fileUrls=new ArrayList<>();
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

        if(!StringUtils.isEmpty(ticket.getFilename())) {
            String[] files = ticket.getFilename().split(",");
            for (String file : files) {
                fileUrls.add(cloudStorageService.generateSignedUrl(file));
            }
            responseDTO.setUrls(fileUrls);
        }
        return responseDTO;
    }

    public TicketResponseDTO assignTicket(UUID ticketId, UUID freelancerId) throws IOException {
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


    public PaginatedResponse<TicketSearchResponse> searchUser(TicketSearchRequest ticketSearchRequest) throws IOException {



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

        UUID clientId=(ticketSearchRequest.getClientId()!=null)?ticketSearchRequest.getClientId():null;

        Date[] dateRange = parseDateRange(ticketSearchRequest.getDates());
        Date startDate = (dateRange[0] != null) ? dateRange[0] : null;
        Date endDate = (dateRange[1] != null) ? dateRange[1] : null;

        List<Object[]> result = ticketRepository.searchTickets(complexity, minPrice, maxPrice,ticketStatus,rating,startDate,endDate,clientId,page, size);

        if(result!=null && !result.isEmpty()) {
            List<TicketSearchResponse> responses = convertToResponse(result,UserType.CLIENT);


            long totalRowCount = (long) result.get(0)[17];
            boolean isLastPage = ((long) (page + 1) * size >= totalRowCount);
            Integer totalPages = (int) Math.ceil((double) totalRowCount / size);


            return new PaginatedResponse<>(totalRowCount, responses, isLastPage, page + 1, totalPages);

        }
        return new PaginatedResponse<>(0, new ArrayList<>(), true, 0, 0);
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

    private List<TicketSearchResponse> convertToResponse(List<Object[]> result,UserType userType) throws IOException {
        List<TicketSearchResponse> responses = new ArrayList<>();

        if(result!=null && !result.isEmpty()){
        for (Object[] row : result) {
            List<String> imagesUrl = new ArrayList<>();
            TicketSearchResponse response = new TicketSearchResponse();
            response.setTicketID((UUID) Utils.getOrDefault((UUID) row[0], response.getTicketID()));
            response.setTitle((String) Utils.getOrDefault((String) row[1], response.getTitle()));
            response.setDescription((String) Utils.getOrDefault((String) row[2], response.getDescription()));
            response.setReporterName((String) Utils.getOrDefault((String) row[3], response.getReporterName()));
            response.setCreatedDate((Date) Utils.getOrDefault((Date) row[4], response.getCreatedDate()));
            response.setPrice((Double) Utils.getOrDefault(row[5] != null ? ((BigDecimal) row[5]).doubleValue() : null, response.getPrice()));
            response.setTicketDeadline((Date) Utils.getOrDefault((Date) row[6], response.getTicketDeadline()));

            if(((String)row[7])!=null)
            {
                String[] files=((String)row[7]).split(",");

                for(String file:files) {
                    imagesUrl.add(cloudStorageService.generateSignedUrl(file));
                }
                response.setImages(String.join(",", imagesUrl));
            }
            response.setUrl((String) Utils.getOrDefault((String) row[8], response.getUrl()));
            response.setTicketStatus((TicketStatus) Utils.getOrDefault(row[9] != null ? TicketStatus.values()[(Integer) row[9]] : null, response.getTicketStatus()));
            response.setFreelancerId((UUID) Utils.getOrDefault((UUID) row[10], response.getFreelancerId()));
            response.setClientId((UUID) Utils.getOrDefault((UUID) row[11], response.getClientId()));
            response.setUsername((String) Utils.getOrDefault((String) row[12], response.getUsername()));
            response.setFirstName((String) Utils.getOrDefault((String) row[13], response.getFirstName()));
            response.setLastName((String) Utils.getOrDefault((String) row[14], response.getLastName()));
            response.setComplexity((String) Utils.getOrDefault((String) row[15], response.getComplexity()));
            if(userType.equals(UserType.CLIENT))
                 response.setRating((Double) Utils.getOrDefault(row[16] != null ? ((BigDecimal) row[16]).doubleValue() : null, response.getRating()));
            responses.add(response);
        }
        }
        return responses;
    }

    public TicketResponseDTO editTicket(TicketDTO ticketDTO,List<MultipartFile> files) throws IOException {
        Ticket ticket = ticketRepository.findById(ticketDTO.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id " + ticketDTO.getTicketId()));

        // Get current images
        List<String> currentImages = new ArrayList<>(Arrays.asList(ticket.getFilename().split(",")));

        // Delete specified images
        if (ticketDTO.getDeleteUrls() != null && !ticketDTO.getDeleteUrls().isEmpty()) {
            for (String url : ticketDTO.getDeleteUrls()) {
                String filename=cloudStorageService.getFilenameFromSignedUrl(url);
                cloudStorageService.deleteFileUsingSignedUrl(url);
                currentImages.remove(filename);
            }
        }

        // Add new images
        if (files != null && !files.isEmpty()) {
            List<String> newUploadedUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                cloudStorageService.uploadFile(file, newUploadedUrls);
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
        ticket.setFilename(currentImages.stream().collect(Collectors.joining(",")));

        Ticket updatedTicket = ticketRepository.save(ticket);
        return mapToResponseDTO(updatedTicket);
    }

    public List<TicketResponseDTO> getFreelancerTickets(UUID freelancerId)  {
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream()
                .map(ticket -> {
                    try {
                        return mapToResponseDTO(ticket);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

    }

    public PaginatedResponse<TicketSearchResponse> searchFreelancerTicket(TicketSearchRequest ticketSearchRequest) throws IOException {

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

        UUID freelancerId=(ticketSearchRequest.getFreelancerId()!=null)?ticketSearchRequest.getFreelancerId():null;

        Date[] dateRange = parseDateRange(ticketSearchRequest.getDates());
        Date startDate = (dateRange[0] != null) ? dateRange[0] : null;
        Date endDate = (dateRange[1] != null) ? dateRange[1] : null;

        List<Object[]> result = ticketRepository.searchFreelancerTickets(complexity, minPrice, maxPrice,ticketStatus,rating,startDate,endDate,freelancerId,page, size);

        if(result!=null && !result.isEmpty()) {
            List<TicketSearchResponse> responses = convertToResponse(result,UserType.FREELANCER);


            long totalRowCount = (long) result.get(0)[16];
            boolean isLastPage = ((page + 1) * size >= totalRowCount);
            Integer totalPages = (int) Math.ceil((double) totalRowCount / size);


            return new PaginatedResponse<>(totalRowCount, responses, isLastPage, page + 1, totalPages);

        }
        return new PaginatedResponse<>(0, new ArrayList<>(), true, 0, 0);
    }


}
