package com.nexarats.academy.admissions.repository;

import com.nexarats.academy.admissions.model.AdmissionOffer;
import com.nexarats.academy.admissions.model.AdmissionOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdmissionOfferRepository extends JpaRepository<AdmissionOffer, UUID> {

    Optional<AdmissionOffer> findByOfferNumber(String offerNumber);

    Optional<AdmissionOffer> findByApplicationId(UUID applicationId);

    List<AdmissionOffer> findByCohortIdAndStatus(UUID cohortId, AdmissionOfferStatus status);
}
