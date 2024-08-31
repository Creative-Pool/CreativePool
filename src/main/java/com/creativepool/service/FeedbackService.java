package com.creativepool.service;

import com.creativepool.constants.Errors;
import com.creativepool.entity.ClientFeedback;
import com.creativepool.entity.FreelancerFeedback;
import com.creativepool.exception.CreativePoolException;
import com.creativepool.models.ClientFeedbackRequest;
import com.creativepool.models.FreelancerFeedbackRequest;
import com.creativepool.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Service
public class FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);


    @Autowired
    private FreelancerFeedbackRepository freelancerFeedbackRepository;

    @Autowired
    FreelancerRepository freelancerRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    private ClientFeedbackRepository clientFeedbackRepository;

    @Autowired
    ClientRepository clientRepository;


    public void submitFreelancerFeedback(FreelancerFeedbackRequest request) {
        try {
            logger.info("Submitting freelancer feedback for freelancerId: {}, ticketId: {}", request.getFreelancerId(), request.getTicketId());

            FreelancerFeedback feedback = new FreelancerFeedback();
            feedback.setTicketId(request.getTicketId());
            feedback.setFreelancerId(request.getFreelancerId());
            feedback.setOverallRating(request.getOverallRating());
            feedback.setProjectOutcome(request.getProjectOutcome());
            feedback.setCreativityRating(request.getCreativityRating());
            feedback.setTechnicalSkillsRating(request.getTechnicalSkillsRating());
            feedback.setCommunicationRating(request.getCommunicationRating());
            feedback.setResponsivenessRating(request.getResponsivenessRating());
            feedback.setProfessionalismRating(request.getProfessionalismRating());
            feedback.setDeadlineAdherence(request.getDeadlineAdherence());
            feedback.setTimeManagementRating(request.getTimeManagementRating());
            feedback.setStrengths(request.getStrengths());
            feedback.setAreasForImprovement(request.getAreasForImprovement());
            feedback.setCreatedAt(new Date());

            BigInteger totalTickets = ticketRepository.fetchTotalTicketsAssignedToFreelancer(request.getFreelancerId());
            BigDecimal rating = freelancerRepository.fetchFreelancerRating(request.getFreelancerId());
            Double updatedRating = (totalTickets.intValue() - 1) * rating.doubleValue() + request.getOverallRating().doubleValue();

            freelancerRepository.updateRating(updatedRating, request.getFreelancerId());
            freelancerFeedbackRepository.save(feedback);

            logger.info("Successfully submitted freelancer feedback for freelancerId: {}", request.getFreelancerId());
        } catch (Exception e) {
            logger.error("Error occurred while submitting freelancer feedback for freelancerId: {}", request.getFreelancerId(), e);
            throw new CreativePoolException(Errors.E00016.getMessage());
        }
    }


    public void submitClientFeedback(ClientFeedbackRequest request) {
        try {
            logger.info("Submitting client feedback for clientId: {}, ticketId: {}", request.getClientId(), request.getTicketId());

            ClientFeedback feedback = new ClientFeedback();
            feedback.setTicketId(request.getTicketId());
            feedback.setClientId(request.getClientId());
            feedback.setOverallRating(request.getOverallRating());
            feedback.setCommunicationRating(request.getCommunicationRating());
            feedback.setResponsivenessRating(request.getResponsivenessRating());
            feedback.setProfessionalismRating(request.getProfessionalismRating());
            feedback.setPaymentTimeliness(request.getPaymentTimeliness());
            feedback.setProjectScopeRating(request.getProjectScopeRating());
            feedback.setStrengths(request.getStrengths());
            feedback.setAreasForImprovement(request.getAreasForImprovement());
            feedback.setCreatedAt(new Date());

            BigInteger totalTickets = ticketRepository.fetchTotalTicketsAssignedToFreelancer(request.getClientId());
            BigDecimal rating = clientRepository.fetchClientRating(request.getClientId());
            Double updatedRating = (totalTickets.intValue() - 1) * rating.doubleValue() + request.getOverallRating().doubleValue();

            clientRepository.updateRating(updatedRating, request.getClientId());
            clientFeedbackRepository.save(feedback);

            logger.info("Successfully submitted client feedback for clientId: {}", request.getClientId());
        } catch (Exception e) {
            logger.error("Error occurred while submitting client feedback for clientId: {}", request.getClientId(), e);
            throw new CreativePoolException(Errors.E00017.getMessage());
        }
    }

}
