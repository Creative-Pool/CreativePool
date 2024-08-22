package com.creativepool.service;


import com.creativepool.constants.Errors;
import com.creativepool.entity.*;
import com.creativepool.exception.BadRequestException;
import com.creativepool.exception.CreativePoolException;
import com.creativepool.exception.ResourceNotFoundException;
import com.creativepool.models.*;
import com.creativepool.repository.*;
import com.creativepool.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);
    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    FreelancerRepository freelancerRepository;

    @Autowired
    FreelancerTicketApplicantsRepository freelancerTicketApplicantsRepository;

    @Autowired
    CloudStorageService cloudStorageService;

    @Autowired
    ClientReachOutRepository clientReachOutRepository;

    @Autowired
    FreelancerReachOutRepository freelancerReachOutRepository;

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
        responseDTO.setTicketComplexity(ticket.getTicketComplexity());
        responseDTO.setTicketBudget(ticket.getBudget());
        responseDTO.setAssignee(ticket.getAssignee());
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

        List<Object[]> freelancerDetails=freelancerRepository.getFreelancerNameAndTotalTicketsAssigned(freelancerId);

        Object[] freelancerDetail=freelancerDetails.get(0);

        String firstname= (String)freelancerDetail[0];
        String lastname =(String)freelancerDetail[1];
        Integer totalAssignedTickets=(Integer)freelancerDetail[2];



        totalAssignedTickets++;
        if(totalAssignedTickets>5){
            throw new IllegalStateException(Errors.E00008.getMessage());
        }
        freelancerRepository.updateTotalTicketsAssigned(totalAssignedTickets,freelancerId);
        ticket.setFreelancerId(freelancerId);
        ticket.setAssignee(firstname+" "+lastname);
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


    public PaginatedResponse<TicketSearchResponse> searchTickets(TicketSearchRequest ticketSearchRequest) throws IOException {
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

        UUID uuid = (ticketSearchRequest.getClientOrFreelancerId() != null) ? ticketSearchRequest.getClientOrFreelancerId() : null;

        Date[] dateRange = parseDateRange(ticketSearchRequest.getDates());
        Date startDate = (dateRange[0] != null) ? dateRange[0] : null;
        Date endDate = (dateRange[1] != null) ? dateRange[1] : null;

        List<Object[]> result = ticketRepository.searchTickets(ticketSearchRequest.getSearchType(),complexity, minPrice, maxPrice, ticketStatus, rating, startDate, endDate, uuid, page, size);

        if (result != null && !result.isEmpty()) {
            List<TicketSearchResponse> responses = convertToResponse(result, UserType.CLIENT);


            long totalRowCount = (long) result.get(0)[15];
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
            response.setAssignee((String) Utils.getOrDefault((String) row[12], response.getAssignee()));
            response.setComplexity((String) Utils.getOrDefault((String) row[13], response.getComplexity()));
            if(userType.equals(UserType.CLIENT))
                 response.setRating((Double) Utils.getOrDefault(row[14] != null ? ((BigDecimal) row[14]).doubleValue() : null, response.getRating()));
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

//    public PaginatedResponse<TicketSearchResponse> searchFreelancerTicket(TicketSearchRequest ticketSearchRequest) throws IOException {
//
//        Integer page = ticketSearchRequest.getPage();
//        Integer size = ticketSearchRequest.getSize();
//
//        if (page == null || size == null) {
//            throw new BadRequestException(Errors.E00001.getMessage());
//        }
//
//        BigDecimal rating = (ticketSearchRequest.getRating() != null)
//                ? BigDecimal.valueOf(ticketSearchRequest.getRating())
//                : null;
//
//        Integer ticketStatus = (ticketSearchRequest.getTicketStatus() != null)
//                ? ticketSearchRequest.getTicketStatus().ordinal()
//                : null;
//
//        // Fetching and parsing price range
//        BigDecimal[] priceRange = parsePriceRange(ticketSearchRequest.getPriceRange());
//        BigDecimal minPrice = (priceRange[0] != null) ? priceRange[0] : null;
//        BigDecimal maxPrice = (priceRange[1] != null) ? priceRange[1] : null;
//
//        // Fetching other parameters
//        String complexity = (ticketSearchRequest.getComplexity() != null && !ticketSearchRequest.getComplexity().isEmpty())
//                ? ticketSearchRequest.getComplexity()
//                : null;
//
//        UUID freelancerId=(ticketSearchRequest.getFreelancerId()!=null)?ticketSearchRequest.getFreelancerId():null;
//
//        Date[] dateRange = parseDateRange(ticketSearchRequest.getDates());
//        Date startDate = (dateRange[0] != null) ? dateRange[0] : null;
//        Date endDate = (dateRange[1] != null) ? dateRange[1] : null;
//
//        List<Object[]> result = ticketRepository.searchFreelancerTickets(complexity, minPrice, maxPrice,ticketStatus,rating,startDate,endDate,freelancerId,page, size);
//
//        if(result!=null && !result.isEmpty()) {
//            List<TicketSearchResponse> responses = convertToResponse(result,UserType.FREELANCER);
//
//
//            long totalRowCount = (long) result.get(0)[16];
//            boolean isLastPage = ((page + 1) * size >= totalRowCount);
//            Integer totalPages = (int) Math.ceil((double) totalRowCount / size);
//
//
//            return new PaginatedResponse<>(totalRowCount, responses, isLastPage, page + 1, totalPages);
//
//        }
//        return new PaginatedResponse<>(0, new ArrayList<>(), true, 0, 0);
//    }


    // Add a new freelancer reach out entry
    public FreelancerReachOut createFreelancerReachOut(FreelancerReachOut freelancerReachOut) {
        try {
            return freelancerReachOutRepository.save(freelancerReachOut);
        } catch (Exception e) {
            throw new CreativePoolException(Errors.E00012.getMessage());
        }
    }


    // Add a new client reach out entry
    public ClientReachOut createClientReachOut(ClientReachOut clientReachOut) {
        try {
            return clientReachOutRepository.save(clientReachOut);
        } catch (Exception e) {
            throw new CreativePoolException(Errors.E00011.getMessage());
        }
    }


    public PaginatedResponse<TicketResponseDTO> fetchTicketsReceivedByFreelancers(UUID freelancerId,Integer page,Integer size) throws IOException {

        if (page == null || size == null) {
            throw new BadRequestException(Errors.E00001.getMessage());
        }

        Pageable pageable= PageRequest.of(page,size);

        Page<Object[]> tickets = clientReachOutRepository.getClientReachOutTickets(freelancerId,pageable);
        List<TicketResponseDTO> ticketResponseDTOS = new ArrayList<>();
        List<Ticket> ticketsList = new ArrayList<>();

        if(!tickets.isEmpty()) {

            for (Object[] array : tickets) {
                TicketResponseDTO dto = new TicketResponseDTO();
                dto.setTicketID(array[0] != null ? (UUID) array[0] : null);
                dto.setTitle(array[1] != null ? (String) array[1] : null);
                dto.setDescription(array[2] != null ? (String) array[2] : null);
                dto.setReporterName(array[3] != null ? (String) array[3] : null);
                dto.setCreatedDate(array[4] != null ? (Date) array[4] : null);
                dto.setPrice(array[5] != null ? (Double) ((BigDecimal) array[5]).doubleValue() : null);
                dto.setTicketDeadline(array[6] != null ? (Date) array[6] : null);

                if (((String) array[7]) != null) {
                    String[] files = ((String) array[7]).split(",");
                    List<String> imagesUrls = new ArrayList<>();
                    for (String file : files) {
                        imagesUrls.add(cloudStorageService.generateSignedUrl(file));
                    }

                    dto.setUrls(imagesUrls);
                }

                // dto.setFilename(array[7] != null ? (String) array[7] : null);
                dto.setUrl(array[8] != null ? (String) array[8] : null);
                dto.setTicketStatus(array[9] != null ? TicketStatus.values()[(Integer) array[9]] : null);
                dto.setFreelancerId(array[10] != null ? (UUID) array[10] : null);
                dto.setClientId(array[11] != null ? (UUID) array[11] : null);
                dto.setTicketComplexity(array[12] != null ? (String) array[12] : null);
                dto.setTicketBudget(array[13] != null ? ((BigDecimal) array[13]).doubleValue() : null);

                ticketResponseDTOS.add(dto);

            }
            return new PaginatedResponse<>(tickets.getTotalElements(), ticketResponseDTOS, ((page + 1) * size >= tickets.getTotalElements()), page + 1, tickets.getTotalPages());
        }
        return new PaginatedResponse<>(0, new ArrayList<>(), true, 0, 0);
    }


    public PaginatedResponse<Profile> getApplicantsForTickets(UUID ticketId,Integer page,Integer size) throws IOException {

        if (page == null || size == null) {
            throw new BadRequestException(Errors.E00001.getMessage());
        }

        Pageable pageable= PageRequest.of(page,size);
        Page<Object[]> freelancerDetails = freelancerReachOutRepository.getFreelancersName(ticketId,pageable);
        List<Profile> profiles = new ArrayList<>();
        List<Ticket> ticketsList = new ArrayList<>();

        if(!freelancerDetails.isEmpty()) {
            for (Object[] freelancerDetail : freelancerDetails) {
                Profile profile=new Profile();
                profile.setFirstName(getOrDefault((String)freelancerDetail[0], profile.getFirstName()));
                profile.setLastName(getOrDefault((String)freelancerDetail[1], profile.getLastName()));
                profile.setFreelancerId(getOrDefault((UUID)freelancerDetail[2], profile.getFreelancerId()));
                profiles.add(profile);
            }
            return new PaginatedResponse<>(freelancerDetails.getTotalElements(), profiles, ((page + 1) * size >= freelancerDetails.getTotalElements()), page + 1, freelancerDetails.getTotalPages());
        }
        return new PaginatedResponse<>(0, new ArrayList<>(), true, 0, 0);
    }


    public PaginatedResponse<Profile> getFreelancersReachedOutByClient(UUID ticketId,Integer page,Integer size) throws IOException {
        if (page == null || size == null) {
            throw new BadRequestException(Errors.E00001.getMessage());
        }

        Pageable pageable= PageRequest.of(page,size);
        Page<Object[]> freelancerDetails = clientReachOutRepository.getFreelancersName(ticketId,pageable);
        List<Profile> profiles = new ArrayList<>();
        List<Ticket> ticketsList = new ArrayList<>();

        if(!freelancerDetails.isEmpty()) {
            for (Object[] freelancerDetail : freelancerDetails) {
                Profile profile=new Profile();
                profile.setFirstName(getOrDefault((String)freelancerDetail[0], profile.getFirstName()));
                profile.setLastName(getOrDefault((String)freelancerDetail[1], profile.getLastName()));
                profile.setFreelancerId(getOrDefault((UUID)freelancerDetail[2], profile.getFreelancerId()));
                profiles.add(profile);
            }
            return new PaginatedResponse<>(freelancerDetails.getTotalElements(), profiles, ((page + 1) * size >= freelancerDetails.getTotalElements()), page + 1, freelancerDetails.getTotalPages());
        }
        return new PaginatedResponse<>(0, new ArrayList<>(), true, 0, 0);
    }


    public PaginatedResponse<TicketResponseDTO> fetchTicketsAppliedByFreelancers(UUID freelancerId,Integer page,Integer size) throws IOException {

        if (page == null || size == null) {
            throw new BadRequestException(Errors.E00001.getMessage());
        }

        Pageable pageable= PageRequest.of(page,size);

        Page<Object[]> tickets = freelancerReachOutRepository.getTicketsAppliedByFreelancer(freelancerId,pageable);
        List<TicketResponseDTO> ticketResponseDTOS = new ArrayList<>();
        List<Ticket> ticketsList = new ArrayList<>();

        if(!tickets.isEmpty()) {

            for (Object[] array : tickets) {
                TicketResponseDTO dto = new TicketResponseDTO();
                dto.setTicketID(array[0] != null ? (UUID) array[0] : null);
                dto.setTitle(array[1] != null ? (String) array[1] : null);
                dto.setDescription(array[2] != null ? (String) array[2] : null);
                dto.setReporterName(array[3] != null ? (String) array[3] : null);
                dto.setCreatedDate(array[4] != null ? (Date) array[4] : null);
                dto.setPrice(array[5] != null ? (Double) ((BigDecimal) array[5]).doubleValue() : null);
                dto.setTicketDeadline(array[6] != null ? (Date) array[6] : null);

                if (((String) array[7]) != null) {
                    String[] files = ((String) array[7]).split(",");
                    List<String> imagesUrls = new ArrayList<>();
                    for (String file : files) {
                        imagesUrls.add(cloudStorageService.generateSignedUrl(file));
                    }

                    dto.setUrls(imagesUrls);
                }

                // dto.setFilename(array[7] != null ? (String) array[7] : null);
                dto.setUrl(array[8] != null ? (String) array[8] : null);
                dto.setTicketStatus(array[9] != null ? TicketStatus.values()[(Integer) array[9]] : null);
                dto.setFreelancerId(array[10] != null ? (UUID) array[10] : null);
                dto.setClientId(array[11] != null ? (UUID) array[11] : null);
                dto.setTicketComplexity(array[12] != null ? (String) array[12] : null);
                dto.setTicketBudget(array[13] != null ? ((BigDecimal) array[13]).doubleValue() : null);

                ticketResponseDTOS.add(dto);

            }
            return new PaginatedResponse<>(tickets.getTotalElements(), ticketResponseDTOS, ((page + 1) * size >= tickets.getTotalElements()), page + 1, tickets.getTotalPages());
        }
        return new PaginatedResponse<>(0, new ArrayList<>(), true, 0, 0);
    }


    public void rejectFreelancerRequest(UUID ticketId,UUID freelancerId) {
        log.info("Deletion of request started {},{}",ticketId,freelancerId);
        freelancerReachOutRepository.deleteAppliedTicket(ticketId,freelancerId);
        log.info("Deletion of request completed {},{}",ticketId,freelancerId);
    }

    public void rejectClientRequest(UUID ticketId,UUID freelancerId) {
        log.info("Deletion of request started {},{}",ticketId,freelancerId);
        clientReachOutRepository.deleteAppliedTicket(ticketId,freelancerId);
        log.info("Deletion of request completed {},{}",ticketId,freelancerId);
    }



}
