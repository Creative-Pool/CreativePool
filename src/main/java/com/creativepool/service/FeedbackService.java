package com.creativepool.service;

import com.creativepool.entity.ClientFeedback;
import com.creativepool.entity.FreelancerFeedback;
import com.creativepool.models.ClientFeedbackRequest;
import com.creativepool.models.FreelancerFeedbackRequest;
import com.creativepool.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Service
public class FeedbackService {



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

        BigInteger totalTickets= ticketRepository.fetchTotalTicketsAssignedToFreelancer(request.getFreelancerId());

        BigDecimal rating =freelancerRepository.fetchFreelancerRating(request.getFreelancerId());

        Double updatedRating=(totalTickets.intValue()-1)*rating.doubleValue()+request.getOverallRating().doubleValue();

        freelancerRepository.updateRating(updatedRating,request.getFreelancerId());
        freelancerFeedbackRepository.save(feedback);
    }


    public void submitClientFeedback(ClientFeedbackRequest request) {
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
        BigInteger totalTickets= ticketRepository.fetchTotalTicketsAssignedToFreelancer(request.getClientId());

        BigDecimal rating =clientRepository.fetchClientRating(request.getClientId());
        Double updatedRating=(totalTickets.intValue()-1)*rating.doubleValue()+request.getOverallRating().doubleValue();
        clientRepository.updateRating(updatedRating,request.getClientId());
        clientFeedbackRepository.save(feedback);
    }
}
