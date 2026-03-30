package com.solarlab.adboard.repository;

import com.solarlab.adboard.model.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
}
