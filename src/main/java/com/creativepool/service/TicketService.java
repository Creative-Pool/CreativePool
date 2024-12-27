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

    Logger logger = LoggerFactory.getLogger(TicketService.class);
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

    @Autowired
    UserRepository userRepository;

    @Autowired
    GoogleMeetService googleMeetService;

    public TicketResponseDTO createTicket(TicketDTO ticketDTO, List<MultipartFile> multipartFiles) throws IOException {
        try {
            logger.info("Action to create ticket started {}", ticketDTO);
            List<String> filenames = new ArrayList<>();
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
            ticket.setFilename(ticketDTO.getFilenames());
            Ticket savedTicket = ticketRepository.save(ticket);
            logger.info("Action to create ticket completed {}", savedTicket);
            return mapToResponseDTO(savedTicket);
        } catch (CreativePoolException ex) {
            logger.error(ex.getMessage(), ex);
            throw new CreativePoolException(ex.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new CreativePoolException(Errors.E00018.getMessage());
        }
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
        responseDTO.setMeetingUrl(ticket.getMeetingUrl());
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
        try {
            logger.info("Assigning ticket with ID '{}' to freelancer with ID '{}'", ticketId, freelancerId);
            Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new BadRequestException(String.format(Errors.E00004.getMessage(), ticketId)));
            logger.debug("Ticket found: {}", ticket);
            List<Object[]> freelancerDetails = freelancerRepository.getFreelancerNameAndTotalTicketsAssigned(freelancerId);

            Object[] freelancerDetail = freelancerDetails.get(0);

            String firstname = (String) freelancerDetail[0];
            String lastname = (String) freelancerDetail[1];
            Integer totalAssignedTickets = freelancerDetail[2] != null ? (Integer) freelancerDetail[2] : 0;

            logger.debug("Freelancer details: {} {}, total tickets assigned: {}", firstname, lastname, totalAssignedTickets);


            totalAssignedTickets++;
            if (totalAssignedTickets > 5) {
                throw new IllegalStateException(Errors.E00008.getMessage());
            }
            freelancerRepository.updateTotalTicketsAssigned(totalAssignedTickets, freelancerId);

            List<String> attendeeEmails=userRepository.findEmailsByClientIdOrFreelancerId(ticket.getClientId(),freelancerId);

            String meetingUrl= googleMeetService.createInstantMeeting(attendeeEmails);

            ticket.setFreelancerId(freelancerId);
            ticket.setMeetingUrl(meetingUrl);
            ticket.setAssignee(firstname + " " + lastname);
            ticket.setTicketStatus(TicketStatus.IN_PROGRESS);
            Ticket savedTicket = ticketRepository.save(ticket);
            logger.info("Ticket '{}' assigned to freelancer '{}'", ticketId, freelancerId);

            return mapToResponseDTO(savedTicket);
        }catch (BadRequestException e) {
            logger.error( e.getMessage(), e);
            throw new BadRequestException(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error during ticket assignment: {}", e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error occurred while assigning ticket: {}", e.getMessage(), e);
            throw new CreativePoolException(Errors.E00019.getMessage());
        }
    }


    //This needs to be discussed.
    public void deleteTicket(UUID ticketId) {
        // Check if the ticket exists
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new BadRequestException(String.format(Errors.E00004.getMessage(), ticketId)));
        freelancerReachOutRepository.deleteByTicketId(ticketId);
        clientReachOutRepository.deleteByTicketId(ticketId);
        ticketRepository.delete(ticket);
    }


    public PaginatedResponse<TicketSearchResponse> searchTickets(TicketSearchRequest ticketSearchRequest) throws IOException {
        try {
            logger.info("Searching tickets with request: {}", ticketSearchRequest);
            Integer page = ticketSearchRequest.getPage();
            Integer size = ticketSearchRequest.getSize();

            if (page == null || size == null) {
                throw new BadRequestException(Errors.E00001.getMessage());
            }

            BigDecimal rating = (ticketSearchRequest.getRating() != null) ? BigDecimal.valueOf(ticketSearchRequest.getRating()) : null;

            Integer ticketStatus = (ticketSearchRequest.getTicketStatus() != null) ? ticketSearchRequest.getTicketStatus().ordinal() : null;

            // Fetching and parsing price range
            BigDecimal[] priceRange = parsePriceRange(ticketSearchRequest.getPriceRange());
            BigDecimal minPrice = (priceRange[0] != null) ? priceRange[0] : null;
            BigDecimal maxPrice = (priceRange[1] != null) ? priceRange[1] : null;

            // Fetching other parameters
            String complexity = (ticketSearchRequest.getComplexity() != null && !ticketSearchRequest.getComplexity().isEmpty()) ? ticketSearchRequest.getComplexity() : null;

            UUID uuid = (ticketSearchRequest.getClientOrFreelancerId() != null) ? ticketSearchRequest.getClientOrFreelancerId() : null;

            Date[] dateRange = parseDateRange(ticketSearchRequest.getDates());
            Date startDate = (dateRange[0] != null) ? dateRange[0] : null;
            Date endDate = (dateRange[1] != null) ? dateRange[1] : null;

            List<Object[]> result = ticketRepository.searchTickets(ticketSearchRequest.getSearchType(), complexity, minPrice, maxPrice, ticketStatus, rating, startDate, endDate, uuid, page, size);
            logger.info("Search completed, found {} records", result != null ? result.size() : 0);

            if (result != null && !result.isEmpty()) {
                List<TicketSearchResponse> responses = convertToResponse(result, UserType.CLIENT);


                long totalRowCount = (long) result.get(0)[16];
                boolean isLastPage = ((long) (page + 1) * size >= totalRowCount);
                Integer totalPages = (int) Math.ceil((double) totalRowCount / size);


                return new PaginatedResponse<>(totalRowCount, responses, isLastPage, page + 1, totalPages);

            }else{
                return new PaginatedResponse<>(0, new ArrayList<>(), true, 0, 0);
            }
        } catch (BadRequestException e) {
            logger.error(e.getMessage(), e);
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CreativePoolException(Errors.E00013.getMessage());
        }
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
            response.setTicketComplexity(((String) Utils.getOrDefault((String) row[13], response.getTicketComplexity())));
            response.setMeetingUrl(((String) Utils.getOrDefault((String) row[14], response.getMeetingUrl())));

            if(userType.equals(UserType.CLIENT))
                 response.setRating((Double) Utils.getOrDefault(row[15] != null ? ((BigDecimal) row[15]).doubleValue() : null, response.getRating()));
            responses.add(response);
        }
        }
        return responses;
    }

    public TicketResponseDTO editTicket(TicketDTO ticketDTO,List<MultipartFile> files) throws IOException {
        try {
            logger.info("Editing ticket with ID: {}", ticketDTO.getTicketId());
            Ticket ticket = ticketRepository.findById(ticketDTO.getTicketId()).orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id " + ticketDTO.getTicketId()));

            // Get current images
//            List<String> currentImages = new ArrayList<>(Arrays.asList(ticket.getFilename().split(",")));
//
//            // Delete specified images
//            if (ticketDTO.getDeleteUrls() != null && !ticketDTO.getDeleteUrls().isEmpty()) {
//                for (String url : ticketDTO.getDeleteUrls()) {
//                    String filename = cloudStorageService.getFilenameFromSignedUrl(url);
//                    cloudStorageService.deleteFileUsingSignedUrl(url);
//                    currentImages.remove(filename);
//                }
//            }
//
//            // Add new images
//            if (files != null && !files.isEmpty()) {
//                List<String> newUploadedUrls = new ArrayList<>();
//                for (MultipartFile file : files) {
//                    cloudStorageService.uploadFile(file, newUploadedUrls);
//                }
//                currentImages.addAll(newUploadedUrls);
//            }

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
            ticket.setFilename(ticketDTO.getFilenames());

            Ticket updatedTicket = ticketRepository.save(ticket);
            logger.info("Ticket updated successfully with ID: {}", updatedTicket.getTicketID());
            return mapToResponseDTO(updatedTicket);
        } catch (ResourceNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResourceNotFoundException(e.getMessage());
        }  catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CreativePoolException(Errors.E00020.getMessage());
        }
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



    // Add a new freelancer reach out entry
    public FreelancerReachOut createFreelancerReachOut(FreelancerReachOut freelancerReachOut) {
        logger.info("Attempting to create a new FreelancerReachOut entry with Freelancer ID: {} and Ticket ID: {}",
                freelancerReachOut.getFreelancerId(), freelancerReachOut.getTicketId());
        try {
            FreelancerReachOut savedFreelancerReachOut = freelancerReachOutRepository.save(freelancerReachOut);
            logger.info("Successfully created FreelancerReachOut entry with ID: {}", savedFreelancerReachOut.getId());
            return savedFreelancerReachOut;
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new CreativePoolException(Errors.E00012.getMessage());
        }
    }


    // Add a new client reach out entry
    public ClientReachOut createClientReachOut(ClientReachOut clientReachOut) {
        logger.info("Attempting to create a new ClientReachOut entry with Client ID: {} and Ticket ID: {}",
                clientReachOut.getClientId(), clientReachOut.getTicketId());
        try {
            ClientReachOut savedClientReachOut = clientReachOutRepository.save(clientReachOut);
            logger.info("Successfully created ClientReachOut entry with ID: {}", savedClientReachOut.getId());
            return savedClientReachOut;
        } catch (Exception e) {
            logger.error("Failed to create ClientReachOut entry. Error: {}", e.getMessage(), e);
            throw new CreativePoolException(Errors.E00011.getMessage());
        }
    }


    public PaginatedResponse<TicketResponseDTO> fetchTicketsReceivedByFreelancers(UUID freelancerId,Integer page,Integer size) throws IOException {
        logger.info("Fetching tickets received by freelancer with ID: {} for page: {} and size: {}", freelancerId, page, size);
        if (page == null || size == null) {
            throw new BadRequestException(Errors.E00001.getMessage());
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> tickets = clientReachOutRepository.getClientReachOutTickets(freelancerId, pageable);
        List<TicketResponseDTO> ticketResponseDTOS = new ArrayList<>();
        List<Ticket> ticketsList = new ArrayList<>();
        try {
            if (!tickets.isEmpty()) {
                logger.info("Found {} tickets for freelancer with ID: {}", tickets.getTotalElements(), freelancerId);
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
                logger.info("Returning paginated response with {} tickets for freelancer with ID: {}", ticketResponseDTOS.size(), freelancerId);

                return new PaginatedResponse<>(tickets.getTotalElements(), ticketResponseDTOS, ((page + 1) * size >= tickets.getTotalElements()), page + 1, tickets.getTotalPages());
            }
            return new PaginatedResponse<>(0, new ArrayList<>(), true, 0, 0);
        } catch (BadRequestException e) {
            logger.error(e.getMessage(), e);
            throw new BadRequestException(e.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new CreativePoolException(Errors.E00021.getMessage());
        }
    }


    public PaginatedResponse<Profile> getApplicantsForTickets(UUID ticketId,Integer page,Integer size) throws IOException {
        logger.info("Fetching applicants for ticket ID: {} with page: {} and size: {}", ticketId, page, size);
        if (page == null || size == null) {
            throw new BadRequestException(Errors.E00001.getMessage());
        }

        Pageable pageable = PageRequest.of(page, size);
        try {
            Page<Object[]> freelancerDetails = freelancerReachOutRepository.getFreelancersName(ticketId, pageable);
            List<Profile> profiles = new ArrayList<>();
            List<Ticket> ticketsList = new ArrayList<>();

            if (!freelancerDetails.isEmpty()) {
                for (Object[] freelancerDetail : freelancerDetails) {
                    Profile profile = new Profile();
                    profile.setFirstName(getOrDefault((String) freelancerDetail[0], profile.getFirstName()));
                    profile.setLastName(getOrDefault((String) freelancerDetail[1], profile.getLastName()));
                    profile.setFreelancerId(getOrDefault((UUID) freelancerDetail[2], profile.getFreelancerId()));
                    profiles.add(profile);
                }
                logger.info("Returning paginated response with {} profiles for ticket ID: {}", profiles.size(), ticketId);
                return new PaginatedResponse<>(freelancerDetails.getTotalElements(), profiles, ((page + 1) * size >= freelancerDetails.getTotalElements()), page + 1, freelancerDetails.getTotalPages());
            }
        }catch (BadRequestException e) {
            logger.error(e.getMessage(), e);
            throw new CreativePoolException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to fetch applicants for ticket ID: {}. Error: {}", ticketId, e.getMessage(), e);
            throw new CreativePoolException(Errors.E00022.getMessage());
        }
        return new PaginatedResponse<>(0, new ArrayList<>(), true, 0, 0);
    }


    public PaginatedResponse<Profile> getFreelancersReachedOutByClient(UUID ticketId,Integer page,Integer size) throws IOException {
        logger.info("Fetching freelancers reached out by client for ticket ID: {} with page: {} and size: {}", ticketId, page, size);
        if (page == null || size == null) {
            throw new BadRequestException(Errors.E00001.getMessage());
        }

        Pageable pageable = PageRequest.of(page, size);
        try {
            Page<Object[]> freelancerDetails = clientReachOutRepository.getFreelancersName(ticketId, pageable);
            List<Profile> profiles = new ArrayList<>();
            List<Ticket> ticketsList = new ArrayList<>();

            if (!freelancerDetails.isEmpty()) {
                for (Object[] freelancerDetail : freelancerDetails) {
                    Profile profile = new Profile();
                    profile.setFirstName(getOrDefault((String) freelancerDetail[0], profile.getFirstName()));
                    profile.setLastName(getOrDefault((String) freelancerDetail[1], profile.getLastName()));
                    profile.setFreelancerId(getOrDefault((UUID) freelancerDetail[2], profile.getFreelancerId()));
                    profiles.add(profile);
                }
                logger.info("Returning paginated response with {} profiles for ticket ID: {}", profiles.size(), ticketId);

                return new PaginatedResponse<>(freelancerDetails.getTotalElements(), profiles, ((page + 1) * size >= freelancerDetails.getTotalElements()), page + 1, freelancerDetails.getTotalPages());
            }
        } catch (BadRequestException e) {
            logger.error(e.getMessage(), e);
            throw new BadRequestException(e.getMessage());
        }catch (Exception e) {
            logger.error("Failed to fetch freelancers reached out by client for ticket ID: {}. Error: {}", ticketId, e.getMessage(), e);
            throw new CreativePoolException(Errors.E00023.getMessage());
        }
        return new PaginatedResponse<>(0, new ArrayList<>(), true, 0, 0);
    }


    public PaginatedResponse<TicketResponseDTO> fetchTicketsAppliedByFreelancers(UUID freelancerId,Integer page,Integer size) {
        logger.info("Fetching tickets applied by freelancer with ID: {} for page: {} and size: {}", freelancerId, page, size);

        if (page == null || size == null) {
            throw new BadRequestException(Errors.E00001.getMessage());
        }

        Pageable pageable = PageRequest.of(page, size);
        try {
            Page<Object[]> tickets = freelancerReachOutRepository.getTicketsAppliedByFreelancer(freelancerId, pageable);
            List<TicketResponseDTO> ticketResponseDTOS = new ArrayList<>();
            List<Ticket> ticketsList = new ArrayList<>();

            if (!tickets.isEmpty()) {

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
                    dto.setMeetingUrl(((String) Utils.getOrDefault((String) array[15], dto.getMeetingUrl())));

                    ticketResponseDTOS.add(dto);
                }
                logger.info("Returning paginated response with {} tickets for freelancer ID: {}", ticketResponseDTOS.size(), freelancerId);

                return new PaginatedResponse<>(tickets.getTotalElements(), ticketResponseDTOS, ((page + 1) * size >= tickets.getTotalElements()), page + 1, tickets.getTotalPages());
            } else {
                logger.info("No tickets found for freelancer with ID: {}", freelancerId);
            }
        } catch (BadRequestException e) {
            logger.error(e.getMessage(), e);
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to fetch tickets applied by freelancer with ID: {}. Error: {}", freelancerId, e.getMessage(), e);
            throw new CreativePoolException(Errors.E00020.getMessage());
        }
        return new PaginatedResponse<>(0, new ArrayList<>(), true, 0, 0);
    }


    public void rejectFreelancerRequest(UUID ticketId,UUID freelancerId) {
        try {
            logger.info("Deletion of request started tickerId {}, freelancerId{}", ticketId, freelancerId);
            freelancerReachOutRepository.deleteAppliedTicket(ticketId, freelancerId);
            logger.info("Deletion of request completed tickerId {}, freelancerId{}", ticketId, freelancerId);
        } catch (Exception e) {
            logger.error("Unexpected error during deletion of freelancer request: ticketId={}, freelancerId={}. Error: {}", ticketId, freelancerId, e.getMessage(), e);
            throw new CreativePoolException(Errors.E00024.getMessage());
        }
    }

    public void rejectClientRequest(UUID ticketId,UUID freelancerId) {
        logger.info("Deletion of request started tickerId {}, freelancerId {}", ticketId, freelancerId);
        try {
            clientReachOutRepository.deleteAppliedTicket(ticketId, freelancerId);
            logger.info("Deletion of request completed tickerId {}, freelancerId {}", ticketId, freelancerId);
        } catch (Exception e) {
            logger.error("Unexpected error during deletion of client request: ticketId={}, freelancerId={}. Error: {}", ticketId, freelancerId, e.getMessage(), e);
            throw new CreativePoolException(Errors.E00025.getMessage());
        }
    }

    public void markTicketAsClosed(UUID ticketId) {
        try {
            logger.info("Attempting to mark ticket with ID {} as closed.", ticketId);
            Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);

            if (ticketOpt.isPresent()) {
                Ticket ticket = ticketOpt.get();
                ticket.setTicketStatus(TicketStatus.CLOSED); // Assuming `status` is an enum
                ticketRepository.save(ticket);
                logger.info("Ticket with ID {} marked as closed successfully.", ticketId);

            }
        } catch (Exception e) {
            logger.error("An error occurred while marking ticket with ID {} as closed: {}", ticketId, e.getMessage(), e);
            throw new CreativePoolException("Unable to close ticket");
        }
    }

    public void backoffFromTicket(UUID ticketId) {
        try {
            logger.info("Attempting to back off freelancer  from ticket with ID {}.", ticketId);
            Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);

            if (ticketOpt.isPresent()) {
                Ticket ticket = ticketOpt.get();
                ticket.setTicketStatus(TicketStatus.OPEN); // Assuming `status` is an enum
                ticket.setFreelancerId(null); // Assuming `freelancerId` is a field in the Ticket entity
                ticketRepository.save(ticket);
                logger.info("successfully backed off from ticket with ID {}.", ticketId);

            }
        } catch (Exception e) {
            logger.error("An error occurred while backing off  from ticket with ID {}: {}", ticketId, e.getMessage(), e);
            throw new CreativePoolException("Unable to unassign from the ticket");
        }
    }



}
