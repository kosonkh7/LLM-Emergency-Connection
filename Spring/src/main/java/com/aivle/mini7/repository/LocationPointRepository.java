package com.aivle.mini7.repository;

import com.aivle.mini7.model.LocationPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationPointRepository extends JpaRepository<LocationPoint, Long>{

}
